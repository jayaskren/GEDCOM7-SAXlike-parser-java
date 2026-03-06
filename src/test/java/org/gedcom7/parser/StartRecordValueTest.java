package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the 4-parameter {@link GedcomHandler#startRecord(int, String, String, String)}
 * overload is called with the correct payload value from level-0 records.
 */
class StartRecordValueTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    /** Helper to capture 4-param startRecord calls. */
    private static class RecordValueCapture extends GedcomHandler {
        final List<String[]> records = new ArrayList<>();

        @Override
        public void startRecord(int level, String xref, String tag, String value) {
            records.add(new String[]{String.valueOf(level), xref, tag, value});
        }
    }

    @Test
    void snoteRecordWithValue_fourParamReceivesPayload() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @N1@ SNOTE This is a note\n0 TRLR\n";
        RecordValueCapture capture = new RecordValueCapture();
        try (GedcomReader reader = new GedcomReader(
                stream(input), capture, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // Find the SNOTE record
        String[] snote = capture.records.stream()
                .filter(r -> "SNOTE".equals(r[2]))
                .findFirst()
                .orElseThrow(() -> new AssertionError("SNOTE record not found"));
        assertEquals("0", snote[0], "level");
        assertEquals("N1", snote[1], "xref");
        assertEquals("SNOTE", snote[2], "tag");
        assertEquals("This is a note", snote[3], "value");
    }

    @Test
    void indiRecordWithoutValue_fourParamReceivesNull() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n0 TRLR\n";
        RecordValueCapture capture = new RecordValueCapture();
        try (GedcomReader reader = new GedcomReader(
                stream(input), capture, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        String[] indi = capture.records.stream()
                .filter(r -> "INDI".equals(r[2]))
                .findFirst()
                .orElseThrow(() -> new AssertionError("INDI record not found"));
        assertNull(indi[3], "INDI record should have null value");
    }

    @Test
    void snoteWithContContinuation_assembledValuePassed() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @N1@ SNOTE Line one\n1 CONT Line two\n0 TRLR\n";
        RecordValueCapture capture = new RecordValueCapture();
        try (GedcomReader reader = new GedcomReader(
                stream(input), capture, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        String[] snote = capture.records.stream()
                .filter(r -> "SNOTE".equals(r[2]))
                .findFirst()
                .orElseThrow(() -> new AssertionError("SNOTE record not found"));
        assertEquals("Line one\nLine two", snote[3],
                "SNOTE value should include CONT-assembled continuation");
    }

    @Test
    void backwardCompat_threeParamOverrideStillFires() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @N1@ SNOTE A note\n0 TRLR\n";
        List<String> threeParamTags = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                threeParamTags.add(tag);
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(threeParamTags.contains("SNOTE"),
                "3-param startRecord should still fire via default delegation: " + threeParamTags);
    }
}
