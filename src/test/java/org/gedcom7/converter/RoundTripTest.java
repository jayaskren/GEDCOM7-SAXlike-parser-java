package org.gedcom7.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T026: Round-trip conversion tests for GEDCOM version conversion.
 *
 * <p>Verifies that converting between GEDCOM versions preserves all
 * records and data, allowing for expected HEAD formatting differences.
 */
class RoundTripTest {

    // ── 1. 5.5.5 -> 7 -> 5.5.5 round-trip ──────────────────────────

    @Test
    void roundTrip555To7To555() throws Exception {
        String original555 = "0 HEAD\n"
                + "1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n"
                + "1 CHAR UTF-8\n"
                + "0 @I1@ INDI\n"
                + "1 NAME John /Doe/\n"
                + "2 GIVN John\n"
                + "2 SURN Doe\n"
                + "1 SEX M\n"
                + "1 BIRT\n"
                + "2 DATE 1 JAN 1980\n"
                + "2 PLAC Springfield, IL\n"
                + "1 FAMS @F1@\n"
                + "0 @I2@ INDI\n"
                + "1 NAME Jane /Smith/\n"
                + "1 SEX F\n"
                + "1 FAMS @F1@\n"
                + "0 @F1@ FAM\n"
                + "1 HUSB @I1@\n"
                + "1 WIFE @I2@\n"
                + "0 @S1@ SOUR\n"
                + "1 TITL Birth Records\n"
                + "0 TRLR\n";

        // Convert 5.5.5 -> 7
        String asGedcom7 = convertOutput(original555, GedcomConverterConfig.toGedcom7());
        assertTrue(asGedcom7.contains("2 VERS 7.0"), "Intermediate should be GEDCOM 7");

        // Convert 7 -> 5.5.5
        String backTo555 = convertOutput(asGedcom7, GedcomConverterConfig.toGedcom555());
        assertTrue(backTo555.contains("2 VERS 5.5.5"), "Final should be GEDCOM 5.5.5");

        // Compare record-by-record (ignoring HEAD differences)
        Set<String> originalRecords = extractNonHeadRecords(original555);
        Set<String> roundTrippedRecords = extractNonHeadRecords(backTo555);

        for (String record : originalRecords) {
            String recordTag = extractRecordTag(record);
            boolean found = roundTrippedRecords.stream()
                    .anyMatch(r -> r.contains(recordTag));
            assertTrue(found,
                    "Record with tag '" + recordTag + "' should be present after round-trip");
        }

        // Verify specific data is preserved
        assertTrue(backTo555.contains("1 NAME John /Doe/"), "Name should be preserved");
        assertTrue(backTo555.contains("2 DATE 1 JAN 1980"), "Date should be preserved");
        assertTrue(backTo555.contains("2 PLAC Springfield, IL"), "Place should be preserved");
        assertTrue(backTo555.contains("1 HUSB @I1@"), "FAM HUSB pointer should be preserved");
        assertTrue(backTo555.contains("1 WIFE @I2@"), "FAM WIFE pointer should be preserved");
        assertTrue(backTo555.contains("1 TITL Birth Records"), "SOUR title should be preserved");
    }

    // ── 2. 7 -> 5.5.5 -> 7 round-trip ──────────────────────────────

    @Test
    void roundTrip7To555To7() throws Exception {
        String original7 = "0 HEAD\n"
                + "1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NAME Alice /Johnson/\n"
                + "2 GIVN Alice\n"
                + "2 SURN Johnson\n"
                + "1 SEX F\n"
                + "1 BIRT\n"
                + "2 DATE 15 MAR 1990\n"
                + "2 PLAC Boston, MA\n"
                + "1 FAMC @F1@\n"
                + "0 @I2@ INDI\n"
                + "1 NAME Bob /Johnson/\n"
                + "1 SEX M\n"
                + "1 FAMS @F1@\n"
                + "0 @F1@ FAM\n"
                + "1 HUSB @I2@\n"
                + "1 CHIL @I1@\n"
                + "0 @S1@ SOUR\n"
                + "1 TITL Census Records\n"
                + "1 AUTH US Government\n"
                + "0 TRLR\n";

        // Convert 7 -> 5.5.5
        String as555 = convertOutput(original7, GedcomConverterConfig.toGedcom555());
        assertTrue(as555.contains("2 VERS 5.5.5"), "Intermediate should be GEDCOM 5.5.5");
        assertTrue(as555.contains("1 CHAR UTF-8"), "Intermediate should have CHAR");

        // Convert 5.5.5 -> 7
        String backTo7 = convertOutput(as555, GedcomConverterConfig.toGedcom7());
        assertTrue(backTo7.contains("2 VERS 7.0"), "Final should be GEDCOM 7.0");
        assertFalse(backTo7.contains("CHAR"), "GEDCOM 7 should not have CHAR");

        // Compare record-by-record
        Set<String> originalRecords = extractNonHeadRecords(original7);
        Set<String> roundTrippedRecords = extractNonHeadRecords(backTo7);

        assertEquals(originalRecords.size(), roundTrippedRecords.size(),
                "Same number of non-HEAD records after round-trip");

        // Verify specific data is preserved
        assertTrue(backTo7.contains("1 NAME Alice /Johnson/"), "Name should be preserved");
        assertTrue(backTo7.contains("2 DATE 15 MAR 1990"), "Date should be preserved");
        assertTrue(backTo7.contains("2 PLAC Boston, MA"), "Place should be preserved");
        assertTrue(backTo7.contains("1 HUSB @I2@"), "FAM HUSB pointer should be preserved");
        assertTrue(backTo7.contains("1 CHIL @I1@"), "FAM CHIL pointer should be preserved");
        assertTrue(backTo7.contains("1 TITL Census Records"), "SOUR title should be preserved");
        assertTrue(backTo7.contains("1 AUTH US Government"), "SOUR auth should be preserved");
    }

    // ── 3. All record types preserved ────────────────────────────────

    @Test
    void allRecordTypesPreservedIn7To555RoundTrip() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NAME Person /One/\n"
                + "0 @F1@ FAM\n"
                + "1 HUSB @I1@\n"
                + "0 @S1@ SOUR\n"
                + "1 TITL A Source\n"
                + "0 @R1@ REPO\n"
                + "1 NAME A Repository\n"
                + "0 @O1@ OBJE\n"
                + "1 FILE https://example.com/photo.jpg\n"
                + "2 FORM image/jpeg\n"
                + "0 @U1@ SUBM\n"
                + "1 NAME Submitter Name\n"
                + "0 @N1@ SNOTE This is a shared note\n"
                + "0 TRLR\n";

        // Convert 7 -> 5.5.5 -> 7
        String as555 = convertOutput(input, GedcomConverterConfig.toGedcom555());
        String backTo7 = convertOutput(as555, GedcomConverterConfig.toGedcom7());

        // All record types should be present
        assertTrue(backTo7.contains("0 @I1@ INDI"), "INDI record preserved");
        assertTrue(backTo7.contains("0 @F1@ FAM"), "FAM record preserved");
        assertTrue(backTo7.contains("0 @S1@ SOUR"), "SOUR record preserved");
        assertTrue(backTo7.contains("0 @R1@ REPO"), "REPO record preserved");
        assertTrue(backTo7.contains("0 @O1@ OBJE"), "OBJE record preserved");
        assertTrue(backTo7.contains("0 @U1@ SUBM"), "SUBM record preserved");
        assertTrue(backTo7.contains("0 @N1@ SNOTE"), "SNOTE record preserved");

        // Verify data within records
        assertTrue(backTo7.contains("1 NAME Person /One/"), "INDI name preserved");
        assertTrue(backTo7.contains("1 HUSB @I1@"), "FAM HUSB preserved");
        assertTrue(backTo7.contains("1 TITL A Source"), "SOUR title preserved");
        assertTrue(backTo7.contains("1 NAME A Repository"), "REPO name preserved");
        assertTrue(backTo7.contains("1 NAME Submitter Name"), "SUBM name preserved");
        assertTrue(backTo7.contains("SNOTE This is a shared note"),
                "SNOTE inline text preserved");
    }

    @Test
    void allRecordTypesPreservedIn555To7RoundTrip() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n2 FORM LINEAGE-LINKED\n1 CHAR UTF-8\n"
                + "0 @I1@ INDI\n"
                + "1 NAME Person /Two/\n"
                + "0 @F1@ FAM\n"
                + "1 WIFE @I1@\n"
                + "0 @S1@ SOUR\n"
                + "1 TITL Source Title\n"
                + "0 @R1@ REPO\n"
                + "1 NAME Repo Name\n"
                + "0 @O1@ OBJE\n"
                + "1 FILE https://example.com/doc.pdf\n"
                + "2 FORM application/pdf\n"
                + "0 @U1@ SUBM\n"
                + "1 NAME A Submitter\n"
                + "0 @N1@ SNOTE A shared note text\n"
                + "0 TRLR\n";

        // Convert 5.5.5 -> 7 -> 5.5.5
        String as7 = convertOutput(input, GedcomConverterConfig.toGedcom7());
        String backTo555 = convertOutput(as7, GedcomConverterConfig.toGedcom555());

        // All record types should be present
        assertTrue(backTo555.contains("0 @I1@ INDI"), "INDI record preserved");
        assertTrue(backTo555.contains("0 @F1@ FAM"), "FAM record preserved");
        assertTrue(backTo555.contains("0 @S1@ SOUR"), "SOUR record preserved");
        assertTrue(backTo555.contains("0 @R1@ REPO"), "REPO record preserved");
        assertTrue(backTo555.contains("0 @O1@ OBJE"), "OBJE record preserved");
        assertTrue(backTo555.contains("0 @U1@ SUBM"), "SUBM record preserved");
        assertTrue(backTo555.contains("0 @N1@ SNOTE"), "SNOTE record preserved");

        // Record count verification
        ConversionResult result = convertResult(as7, GedcomConverterConfig.toGedcom555());
        assertEquals(7, result.getRecordCount(),
                "Should count 7 records (INDI, FAM, SOUR, REPO, OBJE, SUBM, SNOTE)");
    }

    @Test
    void recordOrderPreservedInRoundTrip() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME First\n"
                + "0 @I2@ INDI\n1 NAME Second\n"
                + "0 @I3@ INDI\n1 NAME Third\n"
                + "0 @F1@ FAM\n"
                + "0 @S1@ SOUR\n1 TITL Source\n"
                + "0 TRLR\n";

        String as555 = convertOutput(input, GedcomConverterConfig.toGedcom555());
        String backTo7 = convertOutput(as555, GedcomConverterConfig.toGedcom7());

        // Verify ordering is maintained
        int i1Pos = backTo7.indexOf("0 @I1@ INDI");
        int i2Pos = backTo7.indexOf("0 @I2@ INDI");
        int i3Pos = backTo7.indexOf("0 @I3@ INDI");
        int f1Pos = backTo7.indexOf("0 @F1@ FAM");
        int s1Pos = backTo7.indexOf("0 @S1@ SOUR");

        assertTrue(i1Pos > 0, "I1 should be present");
        assertTrue(i2Pos > i1Pos, "I2 should come after I1");
        assertTrue(i3Pos > i2Pos, "I3 should come after I2");
        assertTrue(f1Pos > i3Pos, "F1 should come after I3");
        assertTrue(s1Pos > f1Pos, "S1 should come after F1");
    }

    // ── 4. Round-trip fidelity for special values ──────────────────

    @Test
    void roundTripPreservesMultiLineValues() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NOTE Line one\n"
                + "2 CONT Line two\n"
                + "2 CONT Line three\n"
                + "0 TRLR\n";

        // 7 -> 5.5.5 -> 7
        String as555 = convertOutput(input, GedcomConverterConfig.toGedcom555());
        String backTo7 = convertOutput(as555, GedcomConverterConfig.toGedcom7());

        assertTrue(backTo7.contains("1 NOTE Line one"),
                "First line of multi-line value should survive round-trip");
        assertTrue(backTo7.contains("2 CONT Line two"),
                "Second line should survive round-trip");
        assertTrue(backTo7.contains("2 CONT Line three"),
                "Third line should survive round-trip");
    }

    @Test
    void roundTripPreservesAtEscaping() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NOTE Contact: user@example.com\n"
                + "0 TRLR\n";

        // 7 -> 5.5.5 (@ becomes @@) -> 7 (should be back to @)
        String as555 = convertOutput(input, GedcomConverterConfig.toGedcom555());
        assertTrue(as555.contains("user@@example.com"),
                "5.5.5 intermediate should have @@ escaping");

        String backTo7 = convertOutput(as555, GedcomConverterConfig.toGedcom7());
        assertTrue(backTo7.contains("user@example.com"),
                "@ should survive round-trip without doubling");
        assertFalse(backTo7.contains("user@@example.com"),
                "Should not have @@ in GEDCOM 7 output");
    }

    @Test
    void roundTripPreservesLongLinesViaConcSplit() throws Exception {
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longValue.append((char) ('A' + (i % 26)));
        }

        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 NOTE " + longValue + "\n"
                + "0 TRLR\n";

        // 7 -> 5.5.5 (CONC split at 255) -> 7 (reassembled)
        String as555 = convertOutput(input, GedcomConverterConfig.toGedcom555());
        assertTrue(as555.contains("2 CONC"),
                "5.5.5 intermediate should have CONC split");

        String backTo7 = convertOutput(as555, GedcomConverterConfig.toGedcom7());
        assertFalse(backTo7.contains("CONC"),
                "GEDCOM 7 output should have no CONC");
        assertTrue(backTo7.contains(longValue.toString()),
                "Full long value should survive CONC round-trip");
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

    /**
     * Extracts non-HEAD/TRLR records from a GEDCOM string.
     * Each record starts with "0 @...@ TAG" and includes all its substructures.
     */
    private Set<String> extractNonHeadRecords(String gedcom) {
        Set<String> records = new LinkedHashSet<>();
        String[] lines = gedcom.split("\\n");
        StringBuilder currentRecord = null;

        for (String line : lines) {
            if (line.startsWith("0 ")) {
                // Flush previous record
                if (currentRecord != null) {
                    records.add(currentRecord.toString().trim());
                }
                // Skip HEAD and TRLR
                if (line.contains("HEAD") || line.contains("TRLR")) {
                    currentRecord = null;
                } else {
                    currentRecord = new StringBuilder(line);
                }
            } else if (currentRecord != null) {
                currentRecord.append("\n").append(line);
            }
        }
        // Flush last record
        if (currentRecord != null) {
            records.add(currentRecord.toString().trim());
        }
        return records;
    }

    /**
     * Extracts the record tag from a record string (e.g., "INDI" from "0 @I1@ INDI\n1 NAME...").
     */
    private String extractRecordTag(String record) {
        String firstLine = record.split("\\n")[0];
        String[] parts = firstLine.split(" ");
        // Format: "0 @xref@ TAG" or "0 TAG"
        return parts.length >= 3 ? parts[2] : parts[1];
    }
}
