package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GedcomHeaderInfoTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    // ─── Test 1: allFieldsPopulated ───────────────────────────

    @Test
    void allFieldsPopulated() {
        final GedcomHeaderInfo[] captured = {null};
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                captured[0] = header;
            }
        };
        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertNotNull(captured[0], "Header should have been captured");
        assertEquals(new GedcomVersion(7, 0), captured[0].getVersion());
        assertEquals("TEST_APP", captured[0].getSourceSystem());
        assertEquals("1.0", captured[0].getSourceVersion());
        assertEquals("Test Application", captured[0].getSourceName());
        assertEquals("en", captured[0].getDefaultLanguage());
    }

    // ─── Test 2: nullOptionalFields ───────────────────────────

    @Test
    void nullOptionalFields() {
        GedcomHeaderInfo info = new GedcomHeaderInfo(
                new GedcomVersion(7, 0),
                null, null, null, null, null);
        assertNull(info.getSourceSystem());
        assertNull(info.getSourceVersion());
        assertNull(info.getSourceName());
        assertNull(info.getDefaultLanguage());
        assertNotNull(info.getSchemaMap(), "schemaMap should never be null");
        assertTrue(info.getSchemaMap().isEmpty(), "schemaMap should be empty when constructed with null");
    }

    // ─── Test 3: schemaMapUnmodifiable ────────────────────────

    @Test
    void schemaMapUnmodifiable() {
        final GedcomHeaderInfo[] captured = {null};
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                captured[0] = header;
            }
        };
        try (GedcomReader reader = new GedcomReader(
                resource("extensions.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertNotNull(captured[0]);
        Map<String, String> schemaMap = captured[0].getSchemaMap();
        assertThrows(UnsupportedOperationException.class,
                () -> schemaMap.put("_NEW", "https://example.com/new"));
    }

    // ─── Test 4: schemaMapPopulated ──────────────────────────

    @Test
    void schemaMapPopulated() {
        final GedcomHeaderInfo[] captured = {null};
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                captured[0] = header;
            }
        };
        try (GedcomReader reader = new GedcomReader(
                resource("extensions.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertNotNull(captured[0]);
        Map<String, String> schemaMap = captured[0].getSchemaMap();
        assertEquals("https://example.com/custom", schemaMap.get("_CUSTOM"));
        assertEquals("https://example.com/other", schemaMap.get("_OTHER"));
        assertEquals(2, schemaMap.size());
    }

    // ─── Test 5: version551_producesWarning ───────────────────

    @Test
    void version551_producesWarning() {
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                resource("vers-551.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(warnings.stream().anyMatch(
                        w -> w.contains("version") || w.contains("GEDCOM 7") || w.contains("5.5.1")),
                "Parsing GEDCOM 5.5.1 should produce a version warning, but got: " + warnings);
    }

    // ─── Test 6: version71_noWarning ─────────────────────────

    @Test
    void version71_noWarning() {
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                resource("vers-71.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(warnings.stream().noneMatch(
                        w -> w.contains("version") || w.contains("GEDCOM 7")),
                "GEDCOM 7.1 should NOT produce a version warning, but got: " + warnings);
    }

    // ─── Test 7: versionAccessor ─────────────────────────────

    @Test
    void versionAccessor() {
        GedcomVersion v70 = new GedcomVersion(7, 0);
        assertEquals(7, v70.getMajor());
        assertEquals(0, v70.getMinor());
        assertTrue(v70.isGedcom7());

        GedcomVersion v55 = new GedcomVersion(5, 5);
        assertEquals(5, v55.getMajor());
        assertEquals(5, v55.getMinor());
        assertFalse(v55.isGedcom7());
        assertTrue(v55.isGedcom5());
    }
}
