package org.gedcom7.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for the GedcomConverter — verifies minimal files convert correctly.
 */
class GedcomConverterBasicTest {

    @Test
    void minimalGedcom7ToGedcom7() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        ConversionResult result = convert(input, GedcomConverterConfig.toGedcom7());

        String output = result(input, GedcomConverterConfig.toGedcom7());
        assertTrue(output.contains("2 VERS 7.0"), "Output should have VERS 7.0");
        assertFalse(output.contains("CHAR"), "GEDCOM 7 output should not have CHAR");
        assertTrue(output.startsWith("0 HEAD\n"), "Should start with HEAD");
        assertTrue(output.endsWith("0 TRLR\n"), "Should end with TRLR");
        assertEquals(0, result.getRecordCount(), "HEAD and TRLR are not counted");
    }

    @Test
    void minimalGedcom555ToGedcom555() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n1 CHAR UTF-8\n0 TRLR\n";
        ConversionResult result = convert(input, GedcomConverterConfig.toGedcom555());

        String output = result(input, GedcomConverterConfig.toGedcom555());
        assertTrue(output.contains("2 VERS 5.5.5"), "Output should have VERS 5.5.5");
        assertTrue(output.contains("1 CHAR UTF-8"), "5.5.5 output should have CHAR UTF-8");
        assertTrue(output.contains("2 FORM LINEAGE-LINKED"), "5.5.5 output should have FORM");
        assertTrue(output.startsWith("0 HEAD\n"), "Should start with HEAD");
        assertTrue(output.endsWith("0 TRLR\n"), "Should end with TRLR");
    }

    @Test
    void minimalGedcom7To555() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        String output = result(input, GedcomConverterConfig.toGedcom555());

        assertTrue(output.contains("2 VERS 5.5.5"), "Target version should be 5.5.5");
        assertTrue(output.contains("1 CHAR UTF-8"), "Should add CHAR UTF-8");
        assertTrue(output.contains("2 FORM LINEAGE-LINKED"), "Should add FORM");
    }

    @Test
    void minimalGedcom555To7() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n1 CHAR UTF-8\n0 TRLR\n";
        String output = result(input, GedcomConverterConfig.toGedcom7());

        assertTrue(output.contains("2 VERS 7.0"), "Target version should be 7.0");
        assertFalse(output.contains("CHAR"), "Should remove CHAR");
        assertFalse(output.contains("FORM"), "Should remove FORM");
    }

    @Test
    void sourceVersionDetected() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        ConversionResult result = convert(input, GedcomConverterConfig.toGedcom7());

        assertNotNull(result.getSourceVersion());
        assertTrue(result.getSourceVersion().isGedcom7());
        assertTrue(result.getTargetVersion().isGedcom7());
    }

    @Test
    void sourceVersion555Detected() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n1 CHAR UTF-8\n0 TRLR\n";
        ConversionResult result = convert(input, GedcomConverterConfig.toGedcom555());

        assertNotNull(result.getSourceVersion());
        assertTrue(result.getSourceVersion().isGedcom5());
    }

    @Test
    void singleRecordConversion() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NAME John /Doe/\n0 TRLR\n";
        ConversionResult result = convert(input, GedcomConverterConfig.toGedcom7());
        String output = result(input, GedcomConverterConfig.toGedcom7());

        assertEquals(1, result.getRecordCount());
        assertTrue(output.contains("0 @I1@ INDI"));
        assertTrue(output.contains("1 NAME John /Doe/"));
    }

    // --- Helpers ---

    private ConversionResult convert(String input, GedcomConverterConfig config) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return GedcomConverter.convert(in, out, config);
    }

    private String result(String input, GedcomConverterConfig config) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomConverter.convert(in, out, config);
        return out.toString(StandardCharsets.UTF_8.name());
    }
}
