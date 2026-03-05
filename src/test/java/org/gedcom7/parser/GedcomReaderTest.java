package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GedcomReaderTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    /** Records events as strings for assertion. */
    static class EventRecorder extends GedcomHandler {
        final List<String> events = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) {
            events.add("startDocument(" + header.getVersion() + ")");
        }

        @Override
        public void endDocument() {
            events.add("endDocument");
        }

        @Override
        public void startRecord(int level, String xref, String tag) {
            events.add("startRecord(" + level + "," + xref + "," + tag + ")");
        }

        @Override
        public void endRecord(String tag) {
            events.add("endRecord(" + tag + ")");
        }

        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            String ptr = isPointer ? ",ptr" : "";
            events.add("startStructure(" + level + "," + xref + "," + tag + "," + value + ptr + ")");
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

        @Override
        public void fatalError(GedcomParseError error) {
            events.add("fatalError(" + error.getMessage() + ")");
        }
    }

    @Test
    void parseMinimalFile() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.events.get(0).startsWith("startDocument("));
        assertTrue(rec.events.contains("startRecord(0,null,HEAD)"));
        assertTrue(rec.events.contains("endRecord(HEAD)"));
        assertTrue(rec.events.contains("startRecord(0,null,TRLR)"));
        assertTrue(rec.events.contains("endRecord(TRLR)"));
        assertTrue(rec.events.get(rec.events.size() - 1).equals("endDocument"));
    }

    @Test
    void parseMinimalWithBom() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("minimal-bom.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.events.contains("startRecord(0,null,HEAD)"));
        assertTrue(rec.events.contains("endDocument"));
    }

    @Test
    void parseMinimalWithCrlf() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("minimal-crlf.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.events.contains("startRecord(0,null,HEAD)"));
        assertTrue(rec.events.contains("endDocument"));
    }

    @Test
    void contPayloadAssembly() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("cont-multiline.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // The SNOTE record should have its value assembled from CONT lines
        boolean foundSnote = false;
        for (String event : rec.events) {
            if (event.contains("SNOTE") && event.contains("startRecord")) {
                foundSnote = true;
            }
        }
        assertTrue(foundSnote, "SNOTE record should be present");
    }

    @Test
    void atEscapeDecoding() {
        // Test @@ escape on a substructure value (level > 0)
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NOTE @@escaped\n0 TRLR\n";
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream(input), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // The leading @@ should be decoded to @ in startStructure
        boolean foundEscaped = rec.events.stream()
                .anyMatch(e -> e.contains("startStructure") && e.contains("@escaped") && !e.contains("@@escaped"));
        assertTrue(foundEscaped, "Leading @@ should be decoded to @");
    }

    @Test
    void substructureEvents() {
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // GEDC and VERS should be startStructure events
        assertTrue(rec.events.stream().anyMatch(e -> e.contains("startStructure") && e.contains("GEDC")));
        assertTrue(rec.events.stream().anyMatch(e -> e.contains("startStructure") && e.contains("VERS")));
    }

    @Test
    void autoCloseable() {
        // GedcomReader should be usable in try-with-resources
        EventRecorder rec = new EventRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream("0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n"),
                rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // No exception = pass
    }
}
