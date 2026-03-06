package org.gedcom7.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T016: Tests for User Story 3 — Streaming conversion of GEDCOM files.
 *
 * <p>Verifies that the converter handles large files with correct record counts,
 * works with InputStream/OutputStream (single-pass, no seekable input), and
 * accurately reports record counts in ConversionResult.
 */
class StreamingConversionTest {

    /**
     * Generate GEDCOM 7 with 1,000 INDI records, convert to 5.5.5.
     * Verify all 1,000 records appear in output and recordCount == 1000.
     */
    @Test
    void convert1000IndiRecordsToGedcom555() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("0 HEAD\n");
        sb.append("1 GEDC\n");
        sb.append("2 VERS 7.0\n");
        for (int i = 1; i <= 1000; i++) {
            sb.append("0 @I").append(i).append("@ INDI\n");
            sb.append("1 NAME Person").append(i).append(" /Family").append(i).append("/\n");
        }
        sb.append("0 TRLR\n");

        String input = sb.toString();
        GedcomConverterConfig config = GedcomConverterConfig.toGedcom555();

        ConversionResult result = convertResult(input, config);
        String output = convert(input, config);

        // Verify record count
        assertEquals(1000, result.getRecordCount(),
                "ConversionResult.recordCount should be 1000 for 1000 INDI records");

        // Verify all 1000 INDI records appear in output
        for (int i = 1; i <= 1000; i++) {
            assertTrue(output.contains("0 @I" + i + "@ INDI"),
                    "Output should contain INDI record @I" + i + "@");
        }

        // Verify target version header
        assertTrue(output.contains("2 VERS 5.5.5"),
                "Output should have GEDCOM 5.5.5 version header");
        assertTrue(output.contains("1 CHAR UTF-8"),
                "Output should have CHAR UTF-8 for 5.5.5");
    }

    /**
     * Convert using InputStream/OutputStream, verify it completes
     * (single-pass, no seekable input needed).
     */
    @Test
    void streamingConversionCompletesWithStreams() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME Alice /Smith/\n"
                + "0 @I2@ INDI\n1 NAME Bob /Jones/\n"
                + "0 TRLR\n";

        ByteArrayInputStream in = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ConversionResult result = GedcomConverter.convert(
                in, out, GedcomConverterConfig.toGedcom555());

        String output = out.toString(StandardCharsets.UTF_8.name());

        // Verify conversion completed
        assertNotNull(result, "ConversionResult should not be null");
        assertTrue(output.startsWith("0 HEAD\n"), "Output should start with HEAD");
        assertTrue(output.endsWith("0 TRLR\n"), "Output should end with TRLR");

        // Verify both records present
        assertTrue(output.contains("0 @I1@ INDI"), "Should contain first INDI");
        assertTrue(output.contains("0 @I2@ INDI"), "Should contain second INDI");
        assertEquals(2, result.getRecordCount(),
                "Should have 2 records (2 INDI)");
    }

    /**
     * Verify ConversionResult.recordCount matches expected count
     * for a multi-record file with different record types.
     */
    @Test
    void recordCountMatchesForMultiRecordFile() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n"
                + "0 @I2@ INDI\n1 NAME Jane /Doe/\n"
                + "0 @F1@ FAM\n1 HUSB @I1@\n1 WIFE @I2@\n"
                + "0 @S1@ SOUR\n1 TITL My Source\n"
                + "0 @N1@ SNOTE A shared note\n"
                + "0 TRLR\n";

        ConversionResult result = convertResult(input, GedcomConverterConfig.toGedcom7());

        // 2 INDI + 1 FAM + 1 SOUR + 1 SNOTE = 5 records
        assertEquals(5, result.getRecordCount(),
                "recordCount should be 5 for 2 INDI + 1 FAM + 1 SOUR + 1 SNOTE");

        // Verify source and target versions
        assertTrue(result.getSourceVersion().isGedcom7(),
                "Source version should be GEDCOM 7");
        assertTrue(result.getTargetVersion().isGedcom7(),
                "Target version should be GEDCOM 7");
    }

    /**
     * Convert 1000 INDI records from 5.5.5 to 7.0 and verify counts.
     */
    @Test
    void convert1000IndiRecordsToGedcom7() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("0 HEAD\n");
        sb.append("1 GEDC\n");
        sb.append("2 VERS 5.5.5\n");
        sb.append("2 FORM LINEAGE-LINKED\n");
        sb.append("1 CHAR UTF-8\n");
        for (int i = 1; i <= 1000; i++) {
            sb.append("0 @I").append(i).append("@ INDI\n");
            sb.append("1 NAME Person").append(i).append(" /Family").append(i).append("/\n");
        }
        sb.append("0 TRLR\n");

        String input = sb.toString();
        GedcomConverterConfig config = GedcomConverterConfig.toGedcom7();

        ConversionResult result = convertResult(input, config);
        String output = convert(input, config);

        assertEquals(1000, result.getRecordCount(),
                "ConversionResult.recordCount should be 1000");

        // GEDCOM 7 output should not have CHAR or FORM
        assertFalse(output.contains("1 CHAR"), "GEDCOM 7 output should not have CHAR");
        assertFalse(output.contains("2 FORM"), "GEDCOM 7 output should not have FORM");
        assertTrue(output.contains("2 VERS 7.0"), "Output should have VERS 7.0");

        // Spot-check some records
        assertTrue(output.contains("0 @I1@ INDI"));
        assertTrue(output.contains("0 @I500@ INDI"));
        assertTrue(output.contains("0 @I1000@ INDI"));
    }

    /**
     * SC-004: Convert a 10,000-record file to verify streaming at scale.
     */
    @Test
    void convert10000RecordsStreaming() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("0 HEAD\n1 GEDC\n2 VERS 7.0\n");
        for (int i = 1; i <= 10000; i++) {
            sb.append("0 @I").append(i).append("@ INDI\n");
            sb.append("1 NAME Person").append(i).append(" /Family/\n");
        }
        sb.append("0 TRLR\n");

        ConversionResult result = convertResult(sb.toString(),
                GedcomConverterConfig.toGedcom555());

        assertEquals(10000, result.getRecordCount(),
                "Should convert 10,000 records");
    }

    // --- Helpers ---

    private String convert(String input, GedcomConverterConfig config) throws Exception {
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
