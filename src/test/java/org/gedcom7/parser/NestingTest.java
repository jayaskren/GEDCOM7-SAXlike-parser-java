package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NestingTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    static class EventRecorder extends GedcomHandler {
        final List<String> events = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) {
            events.add("startDocument");
        }
        @Override
        public void endDocument() {
            events.add("endDocument");
        }
        @Override
        public void startRecord(int level, String xref, String tag) {
            events.add("startRecord(" + tag + ")");
        }
        @Override
        public void endRecord(String tag) {
            events.add("endRecord(" + tag + ")");
        }
        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            events.add("startStructure(" + level + "," + tag + ")");
        }
        @Override
        public void endStructure(String tag) {
            events.add("endStructure(" + tag + ")");
        }
        @Override
        public void warning(GedcomParseError error) {
            events.add("warning(" + error.getMessage() + ")");
        }
        @Override
        public void error(GedcomParseError error) {
            events.add("error(" + error.getMessage() + ")");
        }
    }

    @Test
    void nestedStructuresEmitCorrectEndOrder() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("nested-structures.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // After BIRT's children (DATE, PLAC) we should see:
        // endStructure(PLAC), endStructure(BIRT) before DEAT starts
        int endPlac = rec.events.indexOf("endStructure(PLAC)");
        int endBirt = rec.events.indexOf("endStructure(BIRT)");
        int startDeat = rec.events.indexOf("startStructure(1,DEAT)");

        assertTrue(endPlac >= 0, "endStructure(PLAC) should fire");
        assertTrue(endBirt >= 0, "endStructure(BIRT) should fire");
        assertTrue(startDeat >= 0, "startStructure(DEAT) should fire");
        assertTrue(endPlac < endBirt, "PLAC should end before BIRT");
        assertTrue(endBirt < startDeat, "BIRT should end before DEAT starts");
    }

    @Test
    void multiLevelDecreaseClosesAll() {
        // Going from level 2 to level 0 should close both level 2 and level 1
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 BIRT\n2 DATE 1900\n0 TRLR\n";
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream(input), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        int endDate = rec.events.indexOf("endStructure(DATE)");
        int endBirt = rec.events.indexOf("endStructure(BIRT)");
        int endIndi = rec.events.indexOf("endRecord(INDI)");

        assertTrue(endDate >= 0);
        assertTrue(endBirt >= 0);
        assertTrue(endIndi >= 0);
        assertTrue(endDate < endBirt);
        assertTrue(endBirt < endIndi);
    }

    @Test
    void endOfFileClosesAllOpen() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("nested-structures.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // The last events should be endRecord(TRLR), endDocument
        String last = rec.events.get(rec.events.size() - 1);
        String secondLast = rec.events.get(rec.events.size() - 2);
        assertEquals("endDocument", last);
        assertEquals("endRecord(TRLR)", secondLast);
    }

    @Test
    void recordEndEventsMatch() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("nested-structures.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // Count startRecord and endRecord — should match
        long starts = rec.events.stream().filter(e -> e.startsWith("startRecord")).count();
        long ends = rec.events.stream().filter(e -> e.startsWith("endRecord")).count();
        assertEquals(starts, ends, "startRecord and endRecord counts should match");
    }

    @Test
    void structureEndEventsMatch() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("nested-structures.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        long starts = rec.events.stream().filter(e -> e.startsWith("startStructure")).count();
        long ends = rec.events.stream().filter(e -> e.startsWith("endStructure")).count();
        assertEquals(starts, ends, "startStructure and endStructure counts should match");
    }
}
