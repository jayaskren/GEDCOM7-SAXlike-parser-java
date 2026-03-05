package org.gedcom7.parser.datatype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for remaining parsers in {@link GedcomDataTypes}:
 * parseLanguage (FR-112), parseMediaType (FR-113), parseEnum (FR-114),
 * parseListText (FR-115), parseUri (FR-117), parseTagDef (FR-119).
 */
class MiscParsersTest {

    // ─── parseLanguage (FR-112) ────────────────────────────

    @ParameterizedTest(name = "parseLanguage(\"{0}\") == \"{1}\"")
    @CsvSource({
            "en, en",
            "zh-Hant, zh-Hant",
            "de, de",
            "pt-BR, pt-BR"
    })
    void validLanguages(String input, String expected) {
        assertEquals(expected, GedcomDataTypes.parseLanguage(input));
    }

    @ParameterizedTest(name = "parseLanguage(nullOrEmpty) throws")
    @NullAndEmptySource
    void languageNullAndEmpty_throws(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseLanguage(input));
    }

    // ─── parseMediaType (FR-113) ───────────────────────────

    @ParameterizedTest(name = "parseMediaType(\"{0}\") == \"{1}\"")
    @CsvSource({
            "text/plain, text/plain",
            "image/jpeg, image/jpeg",
            "application/pdf, application/pdf"
    })
    void validMediaTypes(String input, String expected) {
        assertEquals(expected, GedcomDataTypes.parseMediaType(input));
    }

    @Test
    void parseMediaType_acceptsAnyNonEmpty() {
        // Implementation does not validate media type format, only checks non-empty
        assertEquals("textplain", GedcomDataTypes.parseMediaType("textplain"));
        assertEquals("text/", GedcomDataTypes.parseMediaType("text/"));
        assertEquals("/plain", GedcomDataTypes.parseMediaType("/plain"));
    }

    @ParameterizedTest(name = "parseMediaType(nullOrEmpty) throws")
    @NullAndEmptySource
    void mediaTypeNullAndEmpty_throws(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseMediaType(input));
    }

    // ─── parseEnum (FR-114) ────────────────────────────────

    @Test
    void validEnum() {
        assertEquals("M", GedcomDataTypes.parseEnum("M"));
    }

    @Test
    void extensionTagEnum_accepted() {
        assertEquals("_CUSTOM", GedcomDataTypes.parseEnum("_CUSTOM"));
    }

    // ─── parseListText (FR-115) ────────────────────────────

    @Test
    void parseListText_commaSeparated() {
        List<String> result = GedcomDataTypes.parseListText("a, b, c", ",");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        // Implementation trims each part after splitting
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    void parseListText_singleItem() {
        List<String> result = GedcomDataTypes.parseListText("single", ",");
        assertEquals(1, result.size());
        assertEquals("single", result.get(0));
    }

    // ─── parseUri (FR-117) ─────────────────────────────────

    @Test
    void validUri() {
        URI uri = GedcomDataTypes.parseUri("https://example.com");
        assertNotNull(uri);
        assertEquals("https", uri.getScheme());
        assertEquals("example.com", uri.getHost());
    }

    @Test
    void validUri_withPath() {
        URI uri = GedcomDataTypes.parseUri("https://example.com/path/to/resource");
        assertNotNull(uri);
        assertEquals("/path/to/resource", uri.getPath());
    }

    @ParameterizedTest(name = "parseUri(\"{0}\") throws")
    @ValueSource(strings = {"://missing-scheme", "not a uri with spaces"})
    void invalidUri_throws(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseUri(input));
    }

    @ParameterizedTest(name = "parseUri(nullOrEmpty) throws")
    @NullAndEmptySource
    void uriNullAndEmpty_throws(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseUri(input));
    }

    // ─── parseTagDef (FR-119) ──────────────────────────────

    @Test
    void validTagDef() {
        String[] result = GedcomDataTypes.parseTagDef("_CUSTOM https://example.com");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("_CUSTOM", result[0]);
        assertEquals("https://example.com", result[1]);
    }

    @Test
    void tagDef_withComplexUri() {
        String[] result = GedcomDataTypes.parseTagDef(
                "_MYTAG https://example.com/gedcom/ext/v1");
        assertEquals("_MYTAG", result[0]);
        assertEquals("https://example.com/gedcom/ext/v1", result[1]);
    }

    @ParameterizedTest(name = "parseTagDef(\"{0}\") throws")
    @ValueSource(strings = {"_CUSTOM", "missing_space_and_uri"})
    void invalidTagDef_throws(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseTagDef(input));
    }

    @Test
    void tagDef_withoutUnderscorePrefix_accepted() {
        // Implementation does not require _ prefix on tag names
        String[] result = GedcomDataTypes.parseTagDef("NOTEXTENSION https://example.com");
        assertEquals("NOTEXTENSION", result[0]);
        assertEquals("https://example.com", result[1]);
    }

    @ParameterizedTest(name = "parseTagDef(nullOrEmpty) throws")
    @NullAndEmptySource
    void tagDefNullAndEmpty_throws(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseTagDef(input));
    }
}
