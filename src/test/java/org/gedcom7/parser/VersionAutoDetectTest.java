package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@link GedcomReaderConfig#autoDetect()} correctly chooses
 * GEDCOM 7 vs 5.5.5 parsing rules based on the version found in the HEAD record.
 */
class VersionAutoDetectTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    // ----------------------------------------------------------------
    // 1. GEDCOM 7 file detected correctly
    // ----------------------------------------------------------------

    @Test
    void autoDetectGedcom7UsesGedcom7Rules() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo headerInfo) {
                headerRef.set(headerInfo);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), handler, GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        GedcomHeaderInfo header = headerRef.get();
        assertNotNull(header, "startDocument should have been called");
        assertTrue(header.getVersion().isGedcom7(),
                "minimal.ged should be detected as GEDCOM 7");
        assertNull(header.getCharacterEncoding(),
                "GEDCOM 7 files should not report a character encoding");
    }

    // ----------------------------------------------------------------
    // 2. GEDCOM 5.5.5 file detected correctly
    // ----------------------------------------------------------------

    @Test
    void autoDetectGedcom555Uses555Rules() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo headerInfo) {
                headerRef.set(headerInfo);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        GedcomHeaderInfo header = headerRef.get();
        assertNotNull(header, "startDocument should have been called");
        assertTrue(header.getVersion().isGedcom5(),
                "basic-555.ged should be detected as GEDCOM 5.x");
        assertEquals("UTF-8", header.getCharacterEncoding(),
                "GEDCOM 5.5.5 file with HEAD.CHAR UTF-8 should report UTF-8");
    }

    // ----------------------------------------------------------------
    // 3. CONC lines are concatenated (no newline separator) in 5.5.5
    // ----------------------------------------------------------------

    @Test
    void autoDetectConcAssembly() {
        List<String> noteValues = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if ("NOTE".equals(tag) && value != null) {
                    noteValues.add(value);
                }
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/conc-values.ged"), handler,
                GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        assertFalse(noteValues.isEmpty(), "Should have captured a NOTE value");

        // CONC lines should be concatenated without a newline separator.
        // The conc-values.ged file splits "This is a very long note that has been
        //  split across multiple CONC lines." across CONC continuations.
        String assembled = noteValues.get(0);
        assertFalse(assembled.contains("\n"),
                "CONC assembly should not introduce newlines, got: " + assembled);
        assertTrue(assembled.contains("long note"),
                "Assembled value should contain the full text");
        assertTrue(assembled.contains("split across multiple CONC lines"),
                "Assembled value should include all CONC parts");
    }

    // ----------------------------------------------------------------
    // 4. All @@ decoded to @ in 5.5.5 (not just leading)
    // ----------------------------------------------------------------

    @Test
    void autoDetectAllAtEscape() {
        List<String> noteValues = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if ("NOTE".equals(tag) && value != null) {
                    noteValues.add(value);
                }
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/at-escape-all.ged"), handler,
                GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        assertFalse(noteValues.isEmpty(), "Should have captured a NOTE value");

        String value = noteValues.get(0);
        // The raw file has: @@leading and @@middle@@ and trailing@@
        // All @@ should be decoded to single @ in 5.5.5 mode.
        assertFalse(value.contains("@@"),
                "All @@ should be decoded to @ in 5.5.5 mode, got: " + value);
        assertTrue(value.contains("@leading"),
                "Leading @@ should be decoded to @, got: " + value);
        assertTrue(value.contains("@middle@"),
                "Middle @@ should be decoded to @, got: " + value);
        assertTrue(value.contains("trailing@"),
                "Trailing @@ should be decoded to @, got: " + value);
    }

    // ----------------------------------------------------------------
    // 5. Missing HEAD.CHAR in 5.5.5 issues a warning
    // ----------------------------------------------------------------

    @Test
    void autoDetectMissingCharWarns() {
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/no-char-tag.ged"), handler,
                GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        assertFalse(warnings.isEmpty(),
                "A warning should be issued for a 5.5.5 file missing HEAD.CHAR");
        boolean hasCharWarning = warnings.stream()
                .anyMatch(msg -> msg.toLowerCase().contains("char"));
        assertTrue(hasCharWarning,
                "Warning should mention CHAR; warnings were: " + warnings);
    }

    // ----------------------------------------------------------------
    // 6. Non-5.5.5 GEDCOM 5.x warns in auto-detect (FR-012)
    // ----------------------------------------------------------------

    @Test
    void autoDetect551WarnsAboutVersion() {
        // BOM (EF BB BF) + content as raw bytes
        byte[] textBytes = "0 HEAD\n1 GEDC\n2 VERS 5.5.1\n1 CHAR UTF-8\n0 TRLR\n"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = new byte[bom.length + textBytes.length];
        System.arraycopy(bom, 0, content, 0, bom.length);
        System.arraycopy(textBytes, 0, content, bom.length, textBytes.length);

        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                new java.io.ByteArrayInputStream(content),
                handler, GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        boolean hasVersionWarning = warnings.stream()
                .anyMatch(msg -> msg.contains("5.5.1"));
        assertTrue(hasVersionWarning,
                "Should warn about applying 5.5.5 rules to 5.5.1; warnings: " + warnings);
    }
}
