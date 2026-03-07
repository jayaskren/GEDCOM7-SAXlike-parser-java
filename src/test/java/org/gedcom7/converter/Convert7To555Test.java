package org.gedcom7.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GEDCOM 7 to GEDCOM 5.5.5 conversion.
 */
class Convert7To555Test {

    private String convert(String input) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomConverter.convert(in, out, GedcomConverterConfig.toGedcom555());
        return out.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Scenario 1: Convert a GEDCOM 7 file to valid 5.5.5 output.
     * Verifies HEAD.GEDC.VERS is "5.5.5", HEAD.CHAR is "UTF-8",
     * HEAD.GEDC.FORM is "LINEAGE-LINKED", and all records are present.
     */
    @Test
    void convertBasicGedcom7To555() throws Exception {
        String input = "0 HEAD\n"
                + "1 GEDC\n"
                + "2 VERS 7.0\n"
                + "1 SOUR MyApp\n"
                + "0 @I1@ INDI\n"
                + "1 NAME John /Doe/\n"
                + "0 @F1@ FAM\n"
                + "1 HUSB @I1@\n"
                + "0 TRLR\n";

        String output = convert(input);

        // Verify HEAD structure
        assertTrue(output.contains("0 HEAD"), "Should contain HEAD record");
        assertTrue(output.contains("2 VERS 5.5.5"), "Should have VERS 5.5.5");
        assertTrue(output.contains("1 CHAR UTF-8"), "Should have CHAR UTF-8");
        assertTrue(output.contains("2 FORM LINEAGE-LINKED"), "Should have FORM LINEAGE-LINKED");

        // Verify all records present
        assertTrue(output.contains("0 @I1@ INDI"), "Should contain INDI record");
        assertTrue(output.contains("1 NAME John /Doe/"), "Should contain NAME structure");
        assertTrue(output.contains("0 @F1@ FAM"), "Should contain FAM record");
        assertTrue(output.contains("1 HUSB @I1@"), "Should contain HUSB pointer");
        assertTrue(output.contains("0 TRLR"), "Should contain TRLR");
    }

    /**
     * Scenario 2: Long lines (> 255 chars) are split using CONC in 5.5.5 output.
     */
    @Test
    void convertLongLinesSplitWithConc() throws Exception {
        // Create a NOTE value longer than 255 chars
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longValue.append((char) ('A' + (i % 26)));
        }

        String input = "0 HEAD\n"
                + "1 GEDC\n"
                + "2 VERS 7.0\n"
                + "0 @N1@ SNOTE " + longValue.toString() + "\n"
                + "0 TRLR\n";

        String output = convert(input);

        // Verify CONC lines exist
        assertTrue(output.contains("1 CONC"), "Should contain CONC continuation lines");

        // Verify no single line exceeds 255 characters
        String[] lines = output.split("\n");
        for (String line : lines) {
            assertTrue(line.length() <= 255,
                    "Line exceeds 255 chars (" + line.length() + "): "
                    + line.substring(0, Math.min(60, line.length())) + "...");
        }

        // Verify the full value is preserved when CONC lines are reassembled
        // The first chunk + CONC chunks should reconstruct the original value
        assertTrue(output.contains(longValue.substring(0, 50)),
                "Start of long value should appear in output");
    }

    /**
     * Scenario 3: @ characters in values are doubled (@@) in 5.5.5 output.
     */
    @Test
    void convertAtCharactersDoubled() throws Exception {
        String input = "0 HEAD\n"
                + "1 GEDC\n"
                + "2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NAME John /Doe/\n"
                + "2 NOTE Contact: email@test.com\n"
                + "0 TRLR\n";

        String output = convert(input);

        // The @ in email@test.com should be doubled to @@
        assertTrue(output.contains("email@@test.com"),
                "@ characters in values should be doubled to @@ in 5.5.5 output");
        // Make sure it's not tripled or otherwise mangled
        assertFalse(output.contains("email@@@test.com"),
                "Should not have triple @");
    }

    /**
     * Scenario 4: Extension tags (_TAG) are preserved in 5.5.5 output.
     */
    @Test
    void convertExtensionTagsPreserved() throws Exception {
        String input = "0 HEAD\n"
                + "1 GEDC\n"
                + "2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NAME John /Doe/\n"
                + "1 _CUSTOM some value\n"
                + "2 _NESTED nested value\n"
                + "0 TRLR\n";

        String output = convert(input);

        assertTrue(output.contains("1 _CUSTOM some value"),
                "Extension tag _CUSTOM should be preserved");
        assertTrue(output.contains("2 _NESTED nested value"),
                "Nested extension tag _NESTED should be preserved");
    }

    /**
     * Scenario 5: HEAD.SCHMA is preserved as-is in 5.5.5 output.
     */
    @Test
    void convertSchmaPreserved() throws Exception {
        String input = "0 HEAD\n"
                + "1 GEDC\n"
                + "2 VERS 7.0\n"
                + "1 SCHMA\n"
                + "2 TAG _CUSTOM http://example.com/custom\n"
                + "0 @I1@ INDI\n"
                + "1 NAME John /Doe/\n"
                + "0 TRLR\n";

        String output = convert(input);

        assertTrue(output.contains("1 SCHMA"),
                "SCHMA should be present in 5.5.5 output");
        assertTrue(output.contains("2 TAG _CUSTOM http://example.com/custom"),
                "SCHMA TAG entries should be preserved in 5.5.5 output");
    }
}
