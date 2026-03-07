package org.gedcom7.parser;

import org.gedcom7.parser.datatype.GedcomDataTypes;
import org.gedcom7.parser.datatype.GedcomPersonalName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates using tag and value constants in handler code.
 * This test mirrors the extractIndividuals pattern from QuickstartExamplesTest
 * but uses constants instead of string literals (SC-001, SC-005).
 */
class TagConstantsUsageTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    @Test
    void extractIndividualsWithConstants() {
        List<String> indiXrefs = new ArrayList<>();
        Map<String, List<String>> indiNames = new HashMap<>();
        final String[] currentIndi = {null};

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                switch (tag) {
                    case GedcomTag.INDI:
                        currentIndi[0] = xref;
                        indiXrefs.add(xref);
                        indiNames.put(xref, new ArrayList<>());
                        break;
                    default:
                        currentIndi[0] = null;
                }
            }

            @Override
            public void endRecord(String tag) {
                if (GedcomTag.INDI.equals(tag)) {
                    currentIndi[0] = null;
                }
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if (currentIndi[0] != null) {
                    switch (tag) {
                        case GedcomTag.Indi.NAME:
                            if (value != null) {
                                indiNames.get(currentIndi[0]).add(value);
                            }
                            break;
                    }
                }
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertFalse(indiXrefs.isEmpty(), "Should have found at least one INDI record");

        boolean foundJohnDoe = false;
        for (List<String> names : indiNames.values()) {
            for (String name : names) {
                if (name.contains("John") && name.contains("Doe")) {
                    foundJohnDoe = true;
                    break;
                }
            }
        }
        assertTrue(foundJohnDoe, "Should have found an individual named John /Doe/");
    }

    @Test
    void extractIndividualsWithNameParsing() {
        Map<String, String> surnames = new HashMap<>();
        final String[] currentIndi = {null};

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                switch (tag) {
                    case GedcomTag.INDI:
                        currentIndi[0] = xref;
                        break;
                    default:
                        currentIndi[0] = null;
                }
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if (currentIndi[0] != null && GedcomTag.Indi.NAME.equals(tag) && value != null) {
                    GedcomPersonalName name = GedcomDataTypes.parsePersonalName(value);
                    if (name.getSurname() != null) {
                        surnames.put(currentIndi[0], name.getSurname());
                    }
                }
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertFalse(surnames.isEmpty(), "Should have parsed at least one surname");
        assertTrue(surnames.containsValue("Doe"), "Should have found surname Doe");
    }

    @Test
    void extractSexWithValueConstants() {
        Map<String, String> sexByXref = new HashMap<>();
        final String[] currentIndi = {null};

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                if (GedcomTag.INDI.equals(tag)) {
                    currentIndi[0] = xref;
                } else {
                    currentIndi[0] = null;
                }
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if (currentIndi[0] != null && GedcomTag.Indi.SEX.equals(tag) && value != null) {
                    switch (value) {
                        case GedcomValue.Sex.MALE:
                            sexByXref.put(currentIndi[0], "male");
                            break;
                        case GedcomValue.Sex.FEMALE:
                            sexByXref.put(currentIndi[0], "female");
                            break;
                        default:
                            sexByXref.put(currentIndi[0], value);
                    }
                }
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertFalse(sexByXref.isEmpty(), "Should have found sex values");
        assertTrue(sexByXref.containsValue("male") || sexByXref.containsValue("female"),
                "Should have found male or female individuals");
    }

    @Test
    void allRecordTypesIdentifiedWithConstants() {
        Map<String, Integer> recordCounts = new HashMap<>();

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                recordCounts.merge(tag, 1, Integer::sum);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // Verify constants match what the parser delivers
        assertTrue(recordCounts.containsKey(GedcomTag.HEAD), "HEAD record should exist");
        assertTrue(recordCounts.containsKey(GedcomTag.INDI), "INDI record should exist");
        assertTrue(recordCounts.containsKey(GedcomTag.FAM), "FAM record should exist");
        assertTrue(recordCounts.containsKey(GedcomTag.TRLR), "TRLR record should exist");
    }
}
