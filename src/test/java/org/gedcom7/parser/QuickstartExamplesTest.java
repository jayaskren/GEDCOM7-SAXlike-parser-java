package org.gedcom7.parser;

import org.gedcom7.parser.datatype.GedcomAge;
import org.gedcom7.parser.datatype.GedcomDataTypes;
import org.gedcom7.parser.datatype.GedcomDate;
import org.gedcom7.parser.datatype.GedcomDateRange;
import org.gedcom7.parser.datatype.GedcomPersonalName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class QuickstartExamplesTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    // ─── Test 1: minimalHandlerPattern ────────────────────────

    @Test
    void minimalHandlerPattern() {
        AtomicBoolean startDocFired = new AtomicBoolean(false);
        AtomicBoolean endDocFired = new AtomicBoolean(false);
        AtomicBoolean headRecordFound = new AtomicBoolean(false);
        List<String> errors = new ArrayList<>();

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                startDocFired.set(true);
            }

            @Override
            public void endDocument() {
                endDocFired.set(true);
            }

            @Override
            public void startRecord(int level, String xref, String tag) {
                if ("HEAD".equals(tag)) {
                    headRecordFound.set(true);
                }
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                // no-op
            }

            @Override
            public void error(GedcomParseError error) {
                errors.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertTrue(startDocFired.get(), "startDocument should have fired");
        assertTrue(headRecordFound.get(), "HEAD record should have been found");
        assertTrue(endDocFired.get(), "endDocument should have fired");
    }

    // ─── Test 2: configBuilderChain ──────────────────────────

    @Test
    void configBuilderChain() {
        // Verify factory method for strict config
        GedcomReaderConfig strictConfig = GedcomReaderConfig.gedcom7Strict();
        assertTrue(strictConfig.isStrict(), "gedcom7Strict() should be strict");

        // Verify builder chain
        GedcomReaderConfig custom = new GedcomReaderConfig.Builder()
                .strict(true)
                .maxNestingDepth(500)
                .structureValidation(true)
                .build();

        assertTrue(custom.isStrict(), "strict should be true");
        assertEquals(500, custom.getMaxNestingDepth(), "maxNestingDepth should be 500");
        assertTrue(custom.isStructureValidationEnabled(), "structureValidation should be true");
    }

    // ─── Test 3: extractIndividuals ──────────────────────────

    @Test
    void extractIndividuals() {
        List<String> indiXrefs = new ArrayList<>();
        Map<String, List<String>> indiNames = new HashMap<>();
        final String[] currentIndi = {null};

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                if ("INDI".equals(tag)) {
                    currentIndi[0] = xref;
                    indiXrefs.add(xref);
                    indiNames.put(xref, new ArrayList<>());
                } else {
                    currentIndi[0] = null;
                }
            }

            @Override
            public void endRecord(String tag) {
                if ("INDI".equals(tag)) {
                    currentIndi[0] = null;
                }
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if (currentIndi[0] != null && "NAME".equals(tag) && value != null) {
                    indiNames.get(currentIndi[0]).add(value);
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

    // ─── Test 4: dataTypeParsers ─────────────────────────────

    @Test
    void dataTypeParsers() {
        // Parse a date value
        Object dateResult = GedcomDataTypes.parseDateValue("6 APR 1952");
        assertNotNull(dateResult, "parseDateValue should return a non-null result");
        assertTrue(dateResult instanceof GedcomDateRange,
                "An exact date should be returned as a GedcomDateRange with EXACT type");
        GedcomDateRange dateRange = (GedcomDateRange) dateResult;
        assertEquals("EXACT", dateRange.getRangeType());
        GedcomDate date = dateRange.getStart();
        assertNotNull(date, "Start date should not be null for an exact date");
        assertEquals(1952, date.getYear());
        assertEquals("APR", date.getMonth());
        assertEquals(6, date.getDay());

        // Parse a personal name
        GedcomPersonalName name = GedcomDataTypes.parsePersonalName("John /Smith/ Jr.");
        assertNotNull(name, "parsePersonalName should return a non-null result");
        assertEquals("Smith", name.getSurname());
        assertEquals("John", name.getGivenName());
        assertEquals("Jr.", name.getNameSuffix());

        // Parse an age
        GedcomAge age = GedcomDataTypes.parseAge("30y 6m");
        assertNotNull(age, "parseAge should return a non-null result");
        assertEquals(30, age.getYears());
        assertEquals(6, age.getMonths());
        assertEquals(-1, age.getWeeks(), "Weeks should be -1 (absent)");
        assertEquals(-1, age.getDays(), "Days should be -1 (absent)");
        assertNull(age.getModifier(), "Modifier should be null when not specified");
    }

    // ─── Test 5: extensionUriLookup ──────────────────────────

    @Test
    void extensionUriLookup() {
        final Map<String, String> capturedSchema = new HashMap<>();

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                capturedSchema.putAll(header.getSchemaMap());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("extensions.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertFalse(capturedSchema.isEmpty(), "Schema map should not be empty");
        assertEquals("https://example.com/custom", capturedSchema.get("_CUSTOM"),
                "Looking up _CUSTOM should return its URI");
        assertEquals("https://example.com/other", capturedSchema.get("_OTHER"),
                "Looking up _OTHER should return its URI");
    }
}
