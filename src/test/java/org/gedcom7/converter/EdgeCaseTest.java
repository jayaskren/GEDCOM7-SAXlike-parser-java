package org.gedcom7.converter;

import org.gedcom7.parser.GedcomFatalException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T025: Edge case tests for GEDCOM version conversion.
 *
 * <p>Exercises unusual but valid inputs and error handling paths
 * in {@link GedcomConverter#convert}.
 */
class EdgeCaseTest {

    // ── 1. Unrecognized version ──────────────────────────────────────

    @Test
    void unrecognizedVersionStrictThrowsException() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 4.0\n0 TRLR\n";
        GedcomConverterConfig config = GedcomConverterConfig.toGedcom7Strict();

        assertThrows(GedcomFatalException.class,
                () -> convertOutput(input, config),
                "Strict mode should throw GedcomFatalException for unrecognized version 4.0");
    }

    @Test
    void unrecognizedVersionLenientProducesWarning() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 4.0\n0 TRLR\n";
        List<ConversionWarning> captured = new ArrayList<>();
        GedcomConverterConfig config = GedcomConverterConfig.builder()
                .targetVersion(new org.gedcom7.parser.GedcomVersion(7, 0))
                .strict(false)
                .warningHandler(captured::add)
                .build();

        ConversionResult result = convertResult(input, config);

        assertTrue(result.getWarningCount() > 0,
                "Lenient mode should produce at least one warning for unrecognized version");
        boolean hasVersionWarning = result.getWarnings().stream()
                .anyMatch(w -> w.getMessage().contains("Unrecognized")
                        || w.getMessage().contains("4.0"));
        assertTrue(hasVersionWarning,
                "Should have a warning mentioning the unrecognized version");
        assertFalse(captured.isEmpty(),
                "Warning handler should have been called");
    }

    // ── 2. Same-version normalization ────────────────────────────────

    @Test
    void gedcom7ToGedcom7Normalization() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        assertTrue(output.contains("2 VERS 7.0"), "Should retain GEDCOM 7.0 version");
        assertTrue(output.contains("0 @I1@ INDI"), "INDI record preserved");
        assertTrue(output.contains("1 NAME John /Doe/"), "NAME structure preserved");
        assertTrue(output.startsWith("0 HEAD\n"), "Should start with HEAD");
        assertTrue(output.endsWith("0 TRLR\n"), "Should end with TRLR");
        assertFalse(output.contains("CHAR"), "GEDCOM 7 should not have CHAR");
    }

    @Test
    void gedcom555ToGedcom555Normalization() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n1 CHAR UTF-8\n"
                + "0 @I1@ INDI\n1 NAME Jane /Smith/\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom555());

        assertTrue(output.contains("2 VERS 5.5.5"), "Should retain GEDCOM 5.5.5 version");
        assertTrue(output.contains("2 FORM LINEAGE-LINKED"), "Should have FORM");
        assertTrue(output.contains("1 CHAR UTF-8"), "Should have CHAR UTF-8");
        assertTrue(output.contains("0 @I1@ INDI"), "INDI record preserved");
        assertTrue(output.contains("1 NAME Jane /Smith/"), "NAME structure preserved");
    }

    // ── 3. Empty/null values ─────────────────────────────────────────

    @Test
    void emptyValueStructurePreserved() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME\n2 GIVN John\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        // NAME with no value should appear as "1 NAME" (no trailing value)
        assertTrue(output.contains("1 NAME\n") || output.contains("1 NAME\r\n"),
                "NAME with no value should be preserved as-is");
        assertTrue(output.contains("2 GIVN John"), "GIVN with value preserved");
    }

    @Test
    void recordWithNoValuePreserved() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        assertTrue(output.contains("0 @I1@ INDI"),
                "INDI record with no substructures should be preserved");

        ConversionResult result = convertResult(input, GedcomConverterConfig.toGedcom7());
        assertEquals(1, result.getRecordCount(), "One INDI record counted");
    }

    // ── 4. Deeply nested structures ──────────────────────────────────

    @Test
    void deeplyNestedStructuresPreserved() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NAME John /Doe/\n"
                + "2 GIVN John\n"
                + "3 NOTE A note about the given name\n"
                + "4 LANG en\n"
                + "5 SOUR @S1@\n"
                + "6 PAGE Page 42\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        assertTrue(output.contains("0 @I1@ INDI"), "Level 0 preserved");
        assertTrue(output.contains("1 NAME John /Doe/"), "Level 1 preserved");
        assertTrue(output.contains("2 GIVN John"), "Level 2 preserved");
        assertTrue(output.contains("3 NOTE A note about the given name"), "Level 3 preserved");
        assertTrue(output.contains("4 LANG en"), "Level 4 preserved");
        assertTrue(output.contains("5 SOUR @S1@"), "Level 5 preserved");
        assertTrue(output.contains("6 PAGE Page 42"), "Level 6 preserved");
    }

    @Test
    void deeplyNestedStructuresPreservedAcrossConversion() throws Exception {
        // GEDCOM 7 -> 5.5.5 with deep nesting
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NAME John\n"
                + "2 GIVN John\n"
                + "3 NOTE A note\n"
                + "4 LANG en\n"
                + "5 SOUR @S1@\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom555());

        assertTrue(output.contains("2 VERS 5.5.5"), "Converted to 5.5.5");
        assertTrue(output.contains("1 NAME John"), "Level 1 preserved");
        assertTrue(output.contains("2 GIVN John"), "Level 2 preserved");
        assertTrue(output.contains("3 NOTE A note"), "Level 3 preserved");
        assertTrue(output.contains("4 LANG en"), "Level 4 preserved");
        assertTrue(output.contains("5 SOUR @S1@"), "Level 5 preserved");
    }

    // ── 5. SNOTE with inline text ────────────────────────────────────

    @Test
    void snoteWithInlineTextPreservedToGedcom7() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @N1@ SNOTE This is a shared note\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        assertTrue(output.contains("0 @N1@ SNOTE This is a shared note"),
                "SNOTE with inline text value should be preserved in GEDCOM 7");
    }

    @Test
    void snoteWithInlineTextPreservedTo555() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @N1@ SNOTE This is a shared note\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom555());

        assertTrue(output.contains("0 @N1@ SNOTE This is a shared note"),
                "SNOTE with inline text value should be preserved when converting to 5.5.5");
        assertTrue(output.contains("2 VERS 5.5.5"), "Should be converted to 5.5.5");
    }

    @Test
    void snoteWithInlineTextRoundTripsFrom555() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n1 CHAR UTF-8\n"
                + "0 @N1@ SNOTE This is a shared note\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        assertTrue(output.contains("0 @N1@ SNOTE This is a shared note"),
                "SNOTE with inline text should be preserved when converting from 5.5.5 to 7");
    }

    // ── 6. Extension tags with SCHMA ─────────────────────────────────

    @Test
    void extensionTagsWithSchmaPreserved() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "1 SCHMA\n2 TAG _CUSTOM https://example.com/custom\n"
                + "0 @I1@ INDI\n1 _CUSTOM some value\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        // SCHMA should be preserved in the output HEAD
        assertTrue(output.contains("SCHMA"),
                "SCHMA should be present in output");
        assertTrue(output.contains("_CUSTOM") && output.contains("https://example.com/custom"),
                "SCHMA TAG entry for _CUSTOM should be preserved");
        assertTrue(output.contains("1 _CUSTOM some value"),
                "Extension tag usage in body should be preserved");
    }

    @Test
    void extensionTagsWithSchmaPreservedTo555() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "1 SCHMA\n2 TAG _CUSTOM https://example.com/custom\n"
                + "0 @I1@ INDI\n1 _CUSTOM some value\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom555());

        assertTrue(output.contains("2 VERS 5.5.5"), "Should be converted to 5.5.5");
        // SCHMA and extension tag should be preserved
        assertTrue(output.contains("SCHMA"),
                "SCHMA should be present in 5.5.5 output");
        assertTrue(output.contains("_CUSTOM") && output.contains("https://example.com/custom"),
                "SCHMA TAG entry should be preserved in 5.5.5");
        assertTrue(output.contains("1 _CUSTOM some value"),
                "Extension tag usage should be preserved in 5.5.5");
    }

    @Test
    void multipleExtensionTagsPreserved() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "1 SCHMA\n"
                + "2 TAG _CUSTOM1 https://example.com/tag1\n"
                + "2 TAG _CUSTOM2 https://example.com/tag2\n"
                + "0 @I1@ INDI\n"
                + "1 _CUSTOM1 value1\n"
                + "1 _CUSTOM2 value2\n"
                + "0 TRLR\n";
        String output = convertOutput(input, GedcomConverterConfig.toGedcom7());

        assertTrue(output.contains("_CUSTOM1") && output.contains("https://example.com/tag1"),
                "First SCHMA TAG entry should be preserved");
        assertTrue(output.contains("_CUSTOM2") && output.contains("https://example.com/tag2"),
                "Second SCHMA TAG entry should be preserved");
        assertTrue(output.contains("1 _CUSTOM1 value1"),
                "First extension tag usage should be preserved");
        assertTrue(output.contains("1 _CUSTOM2 value2"),
                "Second extension tag usage should be preserved");
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private String convertOutput(String input, GedcomConverterConfig config) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomConverter.convert(in, out, config);
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private ConversionResult convertResult(String input, GedcomConverterConfig config) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return GedcomConverter.convert(in, out, config);
    }
}
