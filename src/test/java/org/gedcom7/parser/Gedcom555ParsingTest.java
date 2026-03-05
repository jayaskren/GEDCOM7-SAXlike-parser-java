package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for parsing GEDCOM 5.5.5 files using
 * {@link GedcomReaderConfig#gedcom555()}.
 */
class Gedcom555ParsingTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    @Test
    void parseBasic555() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        List<String> recordTags = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
            @Override
            public void startRecord(int level, String xref, String tag) {
                recordTags.add(tag);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        GedcomHeaderInfo header = headerRef.get();
        assertNotNull(header);
        assertTrue(header.getVersion().isGedcom5());
        assertEquals("5.5.5", header.getVersion().toString());
        assertTrue(recordTags.contains("INDI"), "Should have parsed INDI records");
    }

    @Test
    void parseConcValues() {
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
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        assertFalse(noteValues.isEmpty(), "Should have captured a NOTE value");
        String assembled = noteValues.get(0);
        // CONC concatenates without separator
        assertFalse(assembled.contains("\n"),
                "CONC should not introduce newlines, got: " + assembled);
        assertTrue(assembled.contains("long note"),
                "Should contain full text");
    }

    @Test
    void parseMixedContConc() {
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
                resource("gedcom555/mixed-cont-conc.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        assertFalse(noteValues.isEmpty());
        String assembled = noteValues.get(0);
        // CONT adds newline, CONC does not
        // Expected: "Line one part one complete.\nLine two.\nLine three part complete."
        assertTrue(assembled.contains("Line one part one complete."),
                "CONC should concat without separator, got: " + assembled);
        assertTrue(assembled.contains("\nLine two."),
                "CONT should add newline before Line two, got: " + assembled);
        assertTrue(assembled.contains("\nLine three part complete."),
                "CONC should concat and CONT should add newline, got: " + assembled);
    }

    @Test
    void parseAtEscapeAll() {
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
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        assertFalse(noteValues.isEmpty());
        String value = noteValues.get(0);
        assertFalse(value.contains("@@"),
                "All @@ should be decoded in 5.5.5 mode, got: " + value);
        assertTrue(value.contains("@leading"),
                "Leading @@ should decode, got: " + value);
    }

    @Test
    void parseUtf16Le() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/utf16-le.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        assertNotNull(headerRef.get(), "Should parse UTF-16 LE file");
        assertTrue(headerRef.get().getVersion().isGedcom5());
    }

    @Test
    void parseUtf16Be() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/utf16-be.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        assertNotNull(headerRef.get(), "Should parse UTF-16 BE file");
        assertTrue(headerRef.get().getVersion().isGedcom5());
    }

    @Test
    void parseMissingCharTagWarns() {
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/no-char-tag.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        boolean hasCharWarning = warnings.stream()
                .anyMatch(msg -> msg.toLowerCase().contains("char"));
        assertTrue(hasCharWarning,
                "Should warn about missing HEAD.CHAR; warnings: " + warnings);
    }
}
