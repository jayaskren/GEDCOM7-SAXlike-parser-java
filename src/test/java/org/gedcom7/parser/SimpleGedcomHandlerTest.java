package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SimpleGedcomHandler}, which unifies record and structure
 * events into {@code onStructure} / {@code onEndStructure} callbacks.
 */
class SimpleGedcomHandlerTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    /**
     * onStructure fires for both INDI (level 0) and NAME (level 1).
     */
    @Test
    void onStructureFiresForRecordAndSubstructure() {
        String gedcom = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NAME John /Doe/\n0 TRLR\n";

        List<String> structures = new ArrayList<>();
        SimpleGedcomHandler handler = new SimpleGedcomHandler() {
            @Override
            public void onStructure(int level, String xref, String tag, String value) {
                structures.add("onStructure(" + level + "," + xref + "," + tag + "," + value + ")");
            }
        };

        try (GedcomReader reader = new GedcomReader(stream(gedcom), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertTrue(structures.contains("onStructure(0,I1,INDI,null)"),
                "Should fire onStructure for INDI record; got: " + structures);
        assertTrue(structures.contains("onStructure(1,null,NAME,John /Doe/)"),
                "Should fire onStructure for NAME substructure; got: " + structures);
    }

    /**
     * onEndStructure fires for both endRecord and endStructure events.
     */
    @Test
    void onEndStructureFiresForRecordAndSubstructure() {
        String gedcom = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NAME John /Doe/\n0 TRLR\n";

        List<String> endings = new ArrayList<>();
        SimpleGedcomHandler handler = new SimpleGedcomHandler() {
            @Override
            public void onEndStructure(int level, String tag) {
                endings.add("onEndStructure(" + level + "," + tag + ")");
            }
        };

        try (GedcomReader reader = new GedcomReader(stream(gedcom), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertTrue(endings.contains("onEndStructure(0,INDI)"),
                "Should fire onEndStructure for INDI record; got: " + endings);
        assertTrue(endings.contains("onEndStructure(1,NAME)"),
                "Should fire onEndStructure for NAME substructure; got: " + endings);
    }

    /**
     * startDocument and endDocument still fire normally when overridden.
     */
    @Test
    void documentEventsStillFire() {
        String gedcom = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";

        List<String> docEvents = new ArrayList<>();
        SimpleGedcomHandler handler = new SimpleGedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                docEvents.add("startDocument");
            }

            @Override
            public void endDocument() {
                docEvents.add("endDocument");
            }
        };

        try (GedcomReader reader = new GedcomReader(stream(gedcom), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertTrue(docEvents.contains("startDocument"), "startDocument should fire");
        assertTrue(docEvents.contains("endDocument"), "endDocument should fire");
    }

    /**
     * Parse a complete small file with only onStructure and onEndStructure
     * overrides -- works correctly with proper nesting levels.
     */
    @Test
    void completeFileWithOnlyStructureOverrides() {
        List<String> events = new ArrayList<>();
        SimpleGedcomHandler handler = new SimpleGedcomHandler() {
            @Override
            public void onStructure(int level, String xref, String tag, String value) {
                events.add("start(" + level + "," + tag + ")");
            }

            @Override
            public void onEndStructure(int level, String tag) {
                events.add("end(" + level + "," + tag + ")");
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("nested-structures.ged"), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // Verify HEAD record
        assertTrue(events.contains("start(0,HEAD)"));
        assertTrue(events.contains("end(0,HEAD)"));

        // Verify INDI record
        assertTrue(events.contains("start(0,INDI)"));
        assertTrue(events.contains("end(0,INDI)"));

        // Verify NAME at level 1
        assertTrue(events.contains("start(1,NAME)"));
        assertTrue(events.contains("end(1,NAME)"));

        // Verify BIRT at level 1 and DATE at level 2
        assertTrue(events.contains("start(1,BIRT)"));
        assertTrue(events.contains("end(1,BIRT)"));
        assertTrue(events.contains("start(2,DATE)"));
        assertTrue(events.contains("end(2,DATE)"));

        // Verify TRLR record
        assertTrue(events.contains("start(0,TRLR)"));
        assertTrue(events.contains("end(0,TRLR)"));
    }

    /**
     * Verify endStructure levels are correct even with deep nesting --
     * tests that the stack-based level tracking works properly.
     */
    @Test
    void endStructureLevelsCorrectWithDeepNesting() {
        String gedcom = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n"
                + "1 BIRT\n"
                + "2 DATE 1 JAN 1900\n"
                + "2 PLAC Springfield\n"
                + "1 DEAT\n"
                + "2 DATE 31 DEC 1980\n"
                + "0 TRLR\n";

        List<String> endEvents = new ArrayList<>();
        SimpleGedcomHandler handler = new SimpleGedcomHandler() {
            @Override
            public void onEndStructure(int level, String tag) {
                endEvents.add("end(" + level + "," + tag + ")");
            }
        };

        try (GedcomReader reader = new GedcomReader(stream(gedcom), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // Verify level-2 structures end at level 2
        assertTrue(endEvents.contains("end(2,DATE)"),
                "DATE should end at level 2; got: " + endEvents);
        assertTrue(endEvents.contains("end(2,PLAC)"),
                "PLAC should end at level 2; got: " + endEvents);

        // Verify level-1 structures end at level 1
        assertTrue(endEvents.contains("end(1,BIRT)"),
                "BIRT should end at level 1; got: " + endEvents);
        assertTrue(endEvents.contains("end(1,DEAT)"),
                "DEAT should end at level 1; got: " + endEvents);

        // Verify level-0 records end at level 0
        assertTrue(endEvents.contains("end(0,INDI)"),
                "INDI should end at level 0; got: " + endEvents);
    }
}
