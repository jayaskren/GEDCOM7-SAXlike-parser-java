package org.gedcom7.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T022: Tests for User Story 5 — Cross-reference (xref) preservation.
 *
 * <p>Verifies that xref IDs (@I1@, @F1@, @S1@) and pointer references
 * (e.g., HUSB @I1@) are preserved exactly through conversion, and that
 * round-trip conversions maintain all xrefs.
 */
class XrefPreservationTest {

    /**
     * GEDCOM file with xref IDs @I1@, @F1@, @S1@ should use the same
     * xref IDs in output.
     */
    @Test
    void xrefIdsPreservedInOutput() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n"
                + "0 @F1@ FAM\n"
                + "0 @S1@ SOUR\n1 TITL My Source\n"
                + "0 TRLR\n";

        String output = convert(input, GedcomConverterConfig.toGedcom555());

        // Verify all xref IDs are preserved exactly
        assertTrue(output.contains("0 @I1@ INDI"),
                "Xref @I1@ should be preserved in INDI record");
        assertTrue(output.contains("0 @F1@ FAM"),
                "Xref @F1@ should be preserved in FAM record");
        assertTrue(output.contains("0 @S1@ SOUR"),
                "Xref @S1@ should be preserved in SOUR record");
    }

    /**
     * Pointer references (HUSB @I1@, WIFE @I2@, CHIL @I3@) should be
     * preserved exactly in the output.
     */
    @Test
    void pointerReferencesPreservedInOutput() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n1 SEX M\n"
                + "0 @I2@ INDI\n1 NAME Jane /Doe/\n1 SEX F\n"
                + "0 @I3@ INDI\n1 NAME Child /Doe/\n"
                + "0 @F1@ FAM\n1 HUSB @I1@\n1 WIFE @I2@\n1 CHIL @I3@\n"
                + "0 TRLR\n";

        String output = convert(input, GedcomConverterConfig.toGedcom555());

        // Verify pointer references are preserved
        assertTrue(output.contains("1 HUSB @I1@"),
                "Pointer HUSB @I1@ should be preserved");
        assertTrue(output.contains("1 WIFE @I2@"),
                "Pointer WIFE @I2@ should be preserved");
        assertTrue(output.contains("1 CHIL @I3@"),
                "Pointer CHIL @I3@ should be preserved");

        // Also verify the record xrefs
        assertTrue(output.contains("0 @I1@ INDI"),
                "Record xref @I1@ should be preserved");
        assertTrue(output.contains("0 @I2@ INDI"),
                "Record xref @I2@ should be preserved");
        assertTrue(output.contains("0 @I3@ INDI"),
                "Record xref @I3@ should be preserved");
        assertTrue(output.contains("0 @F1@ FAM"),
                "Record xref @F1@ should be preserved");
    }

    /**
     * Round-trip: convert 5.5.5 to 7.0 then back to 5.5.5, verify all
     * xrefs match original.
     */
    @Test
    void roundTripPreservesXrefs() throws Exception {
        // Start with GEDCOM 5.5.5
        String original555 = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n1 CHAR UTF-8\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n1 SEX M\n"
                + "0 @I2@ INDI\n1 NAME Jane /Smith/\n1 SEX F\n"
                + "0 @F1@ FAM\n1 HUSB @I1@\n1 WIFE @I2@\n"
                + "0 @S1@ SOUR\n1 TITL Census Records\n"
                + "0 TRLR\n";

        // Convert 5.5.5 -> 7.0
        String gedcom7 = convert(original555, GedcomConverterConfig.toGedcom7());

        // Verify intermediate (GEDCOM 7) has the xrefs
        assertTrue(gedcom7.contains("0 @I1@ INDI"),
                "GEDCOM 7 intermediate should have @I1@");
        assertTrue(gedcom7.contains("0 @I2@ INDI"),
                "GEDCOM 7 intermediate should have @I2@");
        assertTrue(gedcom7.contains("0 @F1@ FAM"),
                "GEDCOM 7 intermediate should have @F1@");
        assertTrue(gedcom7.contains("1 HUSB @I1@"),
                "GEDCOM 7 intermediate should have HUSB pointer");
        assertTrue(gedcom7.contains("1 WIFE @I2@"),
                "GEDCOM 7 intermediate should have WIFE pointer");

        // Convert 7.0 -> 5.5.5
        String roundTrip555 = convert(gedcom7, GedcomConverterConfig.toGedcom555());

        // Verify final output preserves all xref IDs
        assertTrue(roundTrip555.contains("0 @I1@ INDI"),
                "Round-trip should preserve @I1@ INDI");
        assertTrue(roundTrip555.contains("0 @I2@ INDI"),
                "Round-trip should preserve @I2@ INDI");
        assertTrue(roundTrip555.contains("0 @F1@ FAM"),
                "Round-trip should preserve @F1@ FAM");
        assertTrue(roundTrip555.contains("0 @S1@ SOUR"),
                "Round-trip should preserve @S1@ SOUR");

        // Verify pointer references survive round-trip
        assertTrue(roundTrip555.contains("1 HUSB @I1@"),
                "Round-trip should preserve HUSB @I1@ pointer");
        assertTrue(roundTrip555.contains("1 WIFE @I2@"),
                "Round-trip should preserve WIFE @I2@ pointer");

        // Verify version headers are correct
        assertTrue(roundTrip555.contains("2 VERS 5.5.5"),
                "Round-trip output should have VERS 5.5.5");
        assertTrue(roundTrip555.contains("1 CHAR UTF-8"),
                "Round-trip output should have CHAR UTF-8");
    }

    /**
     * Xrefs with longer identifiers (e.g., @PERSON1@, @FAMILY_A@) are preserved.
     */
    @Test
    void longXrefIdsPreserved() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @PERSON1@ INDI\n1 NAME Alice /Wonder/\n"
                + "0 @FAMILY_A@ FAM\n1 WIFE @PERSON1@\n"
                + "0 TRLR\n";

        String output = convert(input, GedcomConverterConfig.toGedcom555());

        assertTrue(output.contains("0 @PERSON1@ INDI"),
                "Long xref @PERSON1@ should be preserved");
        assertTrue(output.contains("0 @FAMILY_A@ FAM"),
                "Long xref @FAMILY_A@ should be preserved");
        assertTrue(output.contains("1 WIFE @PERSON1@"),
                "Pointer to long xref @PERSON1@ should be preserved");
    }

    /**
     * Source citation pointer (SOUR @S1@ in INDI) is preserved through conversion.
     */
    @Test
    void sourceCitationPointerPreserved() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n2 SOUR @S1@\n"
                + "0 @S1@ SOUR\n1 TITL Birth Records\n"
                + "0 TRLR\n";

        String output = convert(input, GedcomConverterConfig.toGedcom555());

        assertTrue(output.contains("2 SOUR @S1@"),
                "Source citation pointer @S1@ should be preserved");
        assertTrue(output.contains("0 @S1@ SOUR"),
                "Source record xref @S1@ should be preserved");
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
