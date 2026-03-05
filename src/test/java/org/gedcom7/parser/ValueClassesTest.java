package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValueClassesTest {

    // ─── GedcomParseError ──────────────────────────────────

    @Test
    void parseError_accessors() {
        GedcomParseError e = new GedcomParseError(
                GedcomParseError.Severity.ERROR, 42, 1024L,
                "bad line", "0 @I1 INDI");
        assertEquals(GedcomParseError.Severity.ERROR, e.getSeverity());
        assertEquals(42, e.getLineNumber());
        assertEquals(1024L, e.getByteOffset());
        assertEquals("bad line", e.getMessage());
        assertEquals("0 @I1 INDI", e.getRawLine());
    }

    @Test
    void parseError_nullRawLine() {
        GedcomParseError e = new GedcomParseError(
                GedcomParseError.Severity.WARNING, 1, 0L, "warn", null);
        assertNull(e.getRawLine());
    }

    @Test
    void parseError_equalsAndHashCode() {
        GedcomParseError a = new GedcomParseError(
                GedcomParseError.Severity.FATAL, 10, 500L, "msg", "raw");
        GedcomParseError b = new GedcomParseError(
                GedcomParseError.Severity.FATAL, 10, 500L, "msg", "raw");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void parseError_notEqual() {
        GedcomParseError a = new GedcomParseError(
                GedcomParseError.Severity.ERROR, 1, 0L, "a", null);
        GedcomParseError b = new GedcomParseError(
                GedcomParseError.Severity.WARNING, 1, 0L, "a", null);
        assertNotEquals(a, b);
    }

    @Test
    void parseError_toString() {
        GedcomParseError e = new GedcomParseError(
                GedcomParseError.Severity.ERROR, 5, 100L, "oops", null);
        assertTrue(e.toString().contains("ERROR"));
        assertTrue(e.toString().contains("line 5"));
        assertTrue(e.toString().contains("oops"));
    }

    @Test
    void parseError_rejectsNullSeverity() {
        assertThrows(NullPointerException.class, () ->
                new GedcomParseError(null, 1, 0L, "msg", null));
    }

    @Test
    void parseError_rejectsNullMessage() {
        assertThrows(NullPointerException.class, () ->
                new GedcomParseError(GedcomParseError.Severity.ERROR, 1, 0L, null, null));
    }

    // ─── GedcomVersion ─────────────────────────────────────

    @Test
    void version_majorMinor() {
        GedcomVersion v = new GedcomVersion(7, 0);
        assertEquals(7, v.getMajor());
        assertEquals(0, v.getMinor());
        assertEquals(-1, v.getPatch());
        assertTrue(v.isGedcom7());
        assertFalse(v.isGedcom5());
        assertEquals("7.0", v.toString());
    }

    @Test
    void version_majorMinorPatch() {
        GedcomVersion v = new GedcomVersion(5, 5, 1);
        assertEquals(5, v.getMajor());
        assertEquals(5, v.getMinor());
        assertEquals(1, v.getPatch());
        assertFalse(v.isGedcom7());
        assertTrue(v.isGedcom5());
        assertEquals("5.5.1", v.toString());
    }

    @Test
    void version_equalsAndHashCode() {
        GedcomVersion a = new GedcomVersion(7, 0);
        GedcomVersion b = new GedcomVersion(7, 0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void version_notEqual() {
        assertNotEquals(new GedcomVersion(7, 0), new GedcomVersion(7, 1));
    }

    @Test
    void version_parse70() {
        GedcomVersion v = GedcomVersion.parse("7.0");
        assertEquals(new GedcomVersion(7, 0), v);
    }

    @Test
    void version_parse551() {
        GedcomVersion v = GedcomVersion.parse("5.5.1");
        assertEquals(new GedcomVersion(5, 5, 1), v);
    }

    @Test
    void version_parseInvalid() {
        assertThrows(IllegalArgumentException.class, () -> GedcomVersion.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> GedcomVersion.parse(""));
        assertThrows(IllegalArgumentException.class, () -> GedcomVersion.parse(null));
        assertThrows(IllegalArgumentException.class, () -> GedcomVersion.parse("7"));
    }

    // ─── GedcomFatalException ──────────────────────────────

    @Test
    void fatalException_wrapsError() {
        GedcomParseError err = new GedcomParseError(
                GedcomParseError.Severity.FATAL, 1, 0L, "fatal", null);
        GedcomFatalException ex = new GedcomFatalException(err);
        assertSame(err, ex.getError());
        assertEquals("fatal", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    // ─── GedcomHeaderInfo ──────────────────────────────────

    @Test
    void headerInfo_accessors() {
        Map<String, String> schema = new HashMap<>();
        schema.put("_CUSTOM", "https://example.com/custom");
        GedcomHeaderInfo info = new GedcomHeaderInfo(
                new GedcomVersion(7, 0),
                "MY_APP", "1.0", "My App", "en",
                schema);
        assertEquals(new GedcomVersion(7, 0), info.getVersion());
        assertEquals("MY_APP", info.getSourceSystem());
        assertEquals("1.0", info.getSourceVersion());
        assertEquals("My App", info.getSourceName());
        assertEquals("en", info.getDefaultLanguage());
        assertEquals(1, info.getSchemaMap().size());
        assertEquals("https://example.com/custom", info.getSchemaMap().get("_CUSTOM"));
    }

    @Test
    void headerInfo_schemaMapIsUnmodifiable() {
        GedcomHeaderInfo info = new GedcomHeaderInfo(
                new GedcomVersion(7, 0), null, null, null, null,
                new HashMap<>());
        assertThrows(UnsupportedOperationException.class, () ->
                info.getSchemaMap().put("_X", "http://x"));
    }

    @Test
    void headerInfo_nullSchemaMapBecomesEmpty() {
        GedcomHeaderInfo info = new GedcomHeaderInfo(
                new GedcomVersion(7, 0), null, null, null, null, null);
        assertNotNull(info.getSchemaMap());
        assertTrue(info.getSchemaMap().isEmpty());
    }

    @Test
    void headerInfo_equalsAndHashCode() {
        GedcomHeaderInfo a = new GedcomHeaderInfo(
                new GedcomVersion(7, 0), "S", "1", "N", "en",
                Collections.emptyMap());
        GedcomHeaderInfo b = new GedcomHeaderInfo(
                new GedcomVersion(7, 0), "S", "1", "N", "en",
                Collections.emptyMap());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ─── GedcomReaderConfig ────────────────────────────────

    @Test
    void config_gedcom7Defaults() {
        GedcomReaderConfig cfg = GedcomReaderConfig.gedcom7();
        assertFalse(cfg.isStrict());
        assertEquals(1000, cfg.getMaxNestingDepth());
        assertEquals(1_048_576, cfg.getMaxLineLength());
        assertFalse(cfg.isStructureValidationEnabled());
    }

    @Test
    void config_gedcom7Strict() {
        GedcomReaderConfig cfg = GedcomReaderConfig.gedcom7Strict();
        assertTrue(cfg.isStrict());
    }

    @Test
    void config_builder() {
        GedcomReaderConfig cfg = new GedcomReaderConfig.Builder()
                .strict(true)
                .maxNestingDepth(50)
                .maxLineLength(4096)
                .structureValidation(true)
                .build();
        assertTrue(cfg.isStrict());
        assertEquals(50, cfg.getMaxNestingDepth());
        assertEquals(4096, cfg.getMaxLineLength());
        assertTrue(cfg.isStructureValidationEnabled());
    }

    @Test
    void config_toBuilder() {
        GedcomReaderConfig original = GedcomReaderConfig.gedcom7Strict();
        GedcomReaderConfig copy = original.toBuilder()
                .maxNestingDepth(100)
                .build();
        assertTrue(copy.isStrict());
        assertEquals(100, copy.getMaxNestingDepth());
    }

    @Test
    void config_builderRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () ->
                new GedcomReaderConfig.Builder().maxNestingDepth(0));
        assertThrows(IllegalArgumentException.class, () ->
                new GedcomReaderConfig.Builder().maxLineLength(-1));
    }
}
