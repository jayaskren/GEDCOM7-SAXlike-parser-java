package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AllRecordTypesTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    @Test
    void allSevenRecordTypesRecognized() {
        List<String> recordTags = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                recordTags.add(tag);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertTrue(recordTags.contains("HEAD"), "HEAD record");
        assertTrue(recordTags.contains("INDI"), "INDI record");
        assertTrue(recordTags.contains("FAM"), "FAM record");
        assertTrue(recordTags.contains("OBJE"), "OBJE record");
        assertTrue(recordTags.contains("REPO"), "REPO record");
        assertTrue(recordTags.contains("SNOTE"), "SNOTE record");
        assertTrue(recordTags.contains("SOUR"), "SOUR record");
        assertTrue(recordTags.contains("SUBM"), "SUBM record");
        assertTrue(recordTags.contains("TRLR"), "TRLR record");
    }

    @Test
    void headerInfoFullyPopulated() {
        final GedcomHeaderInfo[] captured = {null};
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                captured[0] = header;
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertNotNull(captured[0]);
        assertEquals(new GedcomVersion(7, 0), captured[0].getVersion());
        assertEquals("TEST_APP", captured[0].getSourceSystem());
        assertEquals("1.0", captured[0].getSourceVersion());
        assertEquals("Test Application", captured[0].getSourceName());
        assertEquals("en", captured[0].getDefaultLanguage());
    }

    @Test
    void startAndEndEventsBalance() {
        final int[] startRecords = {0};
        final int[] endRecords = {0};
        final int[] startStructures = {0};
        final int[] endStructures = {0};

        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) { startRecords[0]++; }
            @Override
            public void endRecord(String tag) { endRecords[0]++; }
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) { startStructures[0]++; }
            @Override
            public void endStructure(String tag) { endStructures[0]++; }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertEquals(startRecords[0], endRecords[0], "start/end record mismatch");
        assertEquals(startStructures[0], endStructures[0], "start/end structure mismatch");
    }

    @Test
    void noErrorsOrWarningsOnValidFile() {
        List<String> issues = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) { issues.add("W:" + error.getMessage()); }
            @Override
            public void error(GedcomParseError error) { issues.add("E:" + error.getMessage()); }
            @Override
            public void fatalError(GedcomParseError error) { issues.add("F:" + error.getMessage()); }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertTrue(issues.isEmpty(), "Valid file should produce no errors/warnings: " + issues);
    }
}
