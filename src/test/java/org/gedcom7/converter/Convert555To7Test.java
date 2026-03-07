package org.gedcom7.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GEDCOM 5.5.5 to 7.0 conversion.
 *
 * <p>Covers the five acceptance scenarios:
 * <ol>
 *   <li>Full file with HEAD, INDI, FAM, SOUR, TRLR converts to valid GEDCOM 7</li>
 *   <li>CONC-split long lines are reassembled into single lines</li>
 *   <li>@@-escaped values use GEDCOM 7 leading-@ rules</li>
 *   <li>FAMS/FAMC pointers are preserved</li>
 *   <li>Multi-line CONT values are preserved</li>
 * </ol>
 */
class Convert555To7Test {

    // ---------------------------------------------------------------
    // Scenario 1: Full 5.5.5 file → valid GEDCOM 7
    // ---------------------------------------------------------------

    @Test
    void fullFileConvertsToValidGedcom7() throws Exception {
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NAME John /Doe/\n" +
                "1 SEX M\n" +
                "0 @I2@ INDI\n" +
                "1 NAME Jane /Smith/\n" +
                "1 SEX F\n" +
                "0 @F1@ FAM\n" +
                "1 HUSB @I1@\n" +
                "1 WIFE @I2@\n" +
                "0 @S1@ SOUR\n" +
                "1 TITL Birth Records\n" +
                "0 TRLR\n";

        String output = convert(input);

        // Valid GEDCOM 7 header
        assertTrue(output.startsWith("0 HEAD\n"), "Should start with HEAD");
        assertTrue(output.contains("2 VERS 7.0"), "Should have VERS 7.0");
        assertFalse(output.contains("1 CHAR"), "Should not have CHAR line");
        assertFalse(output.contains("FORM LINEAGE-LINKED"), "Should not have FORM");

        // All records present
        assertTrue(output.contains("0 @I1@ INDI"), "INDI I1 present");
        assertTrue(output.contains("1 NAME John /Doe/"), "NAME for I1 present");
        assertTrue(output.contains("1 SEX M"), "SEX for I1 present");
        assertTrue(output.contains("0 @I2@ INDI"), "INDI I2 present");
        assertTrue(output.contains("1 NAME Jane /Smith/"), "NAME for I2 present");
        assertTrue(output.contains("0 @F1@ FAM"), "FAM F1 present");
        assertTrue(output.contains("1 HUSB @I1@"), "HUSB pointer present");
        assertTrue(output.contains("1 WIFE @I2@"), "WIFE pointer present");
        assertTrue(output.contains("0 @S1@ SOUR"), "SOUR S1 present");
        assertTrue(output.contains("1 TITL Birth Records"), "TITL present");

        // Ends with TRLR
        assertTrue(output.trim().endsWith("0 TRLR"), "Should end with TRLR");
    }

    @Test
    void fullFileRecordCount() throws Exception {
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NAME John /Doe/\n" +
                "0 @F1@ FAM\n" +
                "1 HUSB @I1@\n" +
                "0 @S1@ SOUR\n" +
                "1 TITL A Source\n" +
                "0 TRLR\n";

        ConversionResult result = convertResult(input);

        assertEquals(3, result.getRecordCount(),
                "Should count INDI, FAM, SOUR (not HEAD/TRLR)");
        assertTrue(result.getSourceVersion().isGedcom5(),
                "Source should be detected as GEDCOM 5.x");
        assertTrue(result.getTargetVersion().isGedcom7(),
                "Target should be GEDCOM 7");
    }

    // ---------------------------------------------------------------
    // Scenario 2: CONC-split long lines reassembled
    // ---------------------------------------------------------------

    @Test
    void concSplitLinesReassembledIntoSingleLine() throws Exception {
        // Build a value that exceeds 255 chars to force CONC in 5.5.5
        // The prefix "1 NOTE " is 7 chars, so we need a value > 248 chars
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            longValue.append("ABCDEFGHIJ"); // 300 chars total
        }
        String fullValue = longValue.toString();

        // Build 5.5.5 input with CONC-split NOTE
        // In GEDCOM 5.5.5, max line length is 255. "1 NOTE " = 7 chars, so first chunk = 248 chars.
        // Remaining chars go to CONC line(s).
        String firstChunk = fullValue.substring(0, 248);
        String secondChunk = fullValue.substring(248);

        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NOTE " + firstChunk + "\n" +
                "2 CONC " + secondChunk + "\n" +
                "0 TRLR\n";

        String output = convert(input);

        // GEDCOM 7 output should NOT contain CONC
        assertFalse(output.contains("CONC"), "GEDCOM 7 output should not contain CONC");

        // The full value should be on a single NOTE line
        assertTrue(output.contains("1 NOTE " + fullValue),
                "Full value should be reassembled on single NOTE line");
    }

    @Test
    void multipleConcLinesReassembled() throws Exception {
        // Create a value that requires multiple CONC splits
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < 60; i++) {
            longValue.append("ABCDEFGHIJ"); // 600 chars total
        }
        String fullValue = longValue.toString();

        // Split into chunks that fit 5.5.5 line limits
        String firstChunk = fullValue.substring(0, 248); // "1 NOTE " = 7 + 248 = 255
        String secondChunk = fullValue.substring(248, 496); // "2 CONC " = 7 + 248 = 255
        String thirdChunk = fullValue.substring(496);

        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NOTE " + firstChunk + "\n" +
                "2 CONC " + secondChunk + "\n" +
                "2 CONC " + thirdChunk + "\n" +
                "0 TRLR\n";

        String output = convert(input);

        assertFalse(output.contains("CONC"), "No CONC in GEDCOM 7 output");
        assertTrue(output.contains("1 NOTE " + fullValue),
                "All CONC chunks reassembled into single NOTE line");
    }

    // ---------------------------------------------------------------
    // Scenario 3: @@-escaped values → GEDCOM 7 leading-@ rules
    // ---------------------------------------------------------------

    @Test
    void atEscapingConvertedToLeadingAtRules() throws Exception {
        // In GEDCOM 5.5.5 input, @@ represents a literal @ in values.
        // The parser normalizes @@→@ in delivered values.
        // In GEDCOM 7 output, only a leading @ is doubled.
        //
        // Value "@I1@" in 5.5.5 file is "@@I1@@"
        // Parser delivers: "@I1@"
        // GEDCOM 7 output: "@@I1@" (only leading @ doubled)
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NOTE @@I1@@\n" +
                "0 TRLR\n";

        String output = convert(input);

        // The parser unescapes @@I1@@ → @I1@
        // GEDCOM 7 output: leading @ doubled → @@I1@
        assertTrue(output.contains("1 NOTE @@I1@"),
                "Leading @ should be doubled in GEDCOM 7; got: " +
                        extractLine(output, "NOTE"));
    }

    @Test
    void nonLeadingAtNotDoubledInGedcom7() throws Exception {
        // Value "test@@email" in 5.5.5 → parser delivers "test@email"
        // GEDCOM 7 output: "test@email" (@ is not leading, so no doubling)
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NOTE test@@email\n" +
                "0 TRLR\n";

        String output = convert(input);

        // Parser unescapes test@@email → test@email
        // GEDCOM 7: not leading @, so stays test@email
        assertTrue(output.contains("1 NOTE test@email"),
                "Non-leading @ should not be doubled; got: " +
                        extractLine(output, "NOTE"));
    }

    @Test
    void valueWithNoAtPassesThroughUnchanged() throws Exception {
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NOTE Just a regular note\n" +
                "0 TRLR\n";

        String output = convert(input);

        assertTrue(output.contains("1 NOTE Just a regular note"),
                "Regular value should pass through unchanged");
    }

    // ---------------------------------------------------------------
    // Scenario 4: FAMS/FAMC pointers preserved
    // ---------------------------------------------------------------

    @Test
    void famsPointersPreserved() throws Exception {
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NAME John /Doe/\n" +
                "1 FAMS @F1@\n" +
                "1 FAMS @F2@\n" +
                "0 @F1@ FAM\n" +
                "1 HUSB @I1@\n" +
                "0 @F2@ FAM\n" +
                "1 HUSB @I1@\n" +
                "0 TRLR\n";

        String output = convert(input);

        assertTrue(output.contains("1 FAMS @F1@"), "FAMS @F1@ pointer preserved");
        assertTrue(output.contains("1 FAMS @F2@"), "FAMS @F2@ pointer preserved");
    }

    @Test
    void famcPointersPreserved() throws Exception {
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NAME Child /Doe/\n" +
                "1 FAMC @F1@\n" +
                "0 @F1@ FAM\n" +
                "1 CHIL @I1@\n" +
                "0 TRLR\n";

        String output = convert(input);

        assertTrue(output.contains("1 FAMC @F1@"), "FAMC @F1@ pointer preserved");
        assertTrue(output.contains("1 CHIL @I1@"), "CHIL @I1@ pointer preserved");
    }

    @Test
    void famcWithMultipleFamiliesPreserved() throws Exception {
        // A child can belong to multiple families (e.g., adoptive + birth)
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NAME Child /Doe/\n" +
                "1 FAMC @F1@\n" +
                "1 FAMC @F2@\n" +
                "0 @F1@ FAM\n" +
                "1 CHIL @I1@\n" +
                "0 @F2@ FAM\n" +
                "1 CHIL @I1@\n" +
                "0 TRLR\n";

        String output = convert(input);

        assertTrue(output.contains("1 FAMC @F1@"), "First FAMC preserved");
        assertTrue(output.contains("1 FAMC @F2@"), "Second FAMC preserved");
    }

    // ---------------------------------------------------------------
    // Scenario 5: Multi-line CONT values preserved
    // ---------------------------------------------------------------

    @Test
    void contValuesPreserved() throws Exception {
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NOTE First line\n" +
                "2 CONT Second line\n" +
                "2 CONT Third line\n" +
                "0 TRLR\n";

        String output = convert(input);

        // The output should contain the NOTE with CONT lines
        assertTrue(output.contains("1 NOTE First line"),
                "First line of NOTE present");
        assertTrue(output.contains("2 CONT Second line"),
                "Second line via CONT present");
        assertTrue(output.contains("2 CONT Third line"),
                "Third line via CONT present");
    }

    @Test
    void contWithEmptyLinesPreserved() throws Exception {
        String input =
                "0 HEAD\n" +
                "1 GEDC\n" +
                "2 VERS 5.5.5\n" +
                "2 FORM LINEAGE-LINKED\n" +
                "1 CHAR UTF-8\n" +
                "0 @I1@ INDI\n" +
                "1 NOTE Line one\n" +
                "2 CONT\n" +
                "2 CONT Line three\n" +
                "0 TRLR\n";

        String output = convert(input);

        assertTrue(output.contains("1 NOTE Line one"),
                "First line present");
        // The empty CONT line represents a blank line in the value
        assertTrue(output.contains("2 CONT\n"),
                "Empty CONT line preserved");
        assertTrue(output.contains("2 CONT Line three"),
                "Third line present");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String convert(String input) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomConverter.convert(in, out, GedcomConverterConfig.toGedcom7());
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private ConversionResult convertResult(String input) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return GedcomConverter.convert(in, out, GedcomConverterConfig.toGedcom7());
    }

    /**
     * Extracts the first line containing the given tag from the output,
     * for diagnostic messages.
     */
    private String extractLine(String output, String tag) {
        for (String line : output.split("\n")) {
            if (line.contains(tag)) {
                return line;
            }
        }
        return "(not found)";
    }
}
