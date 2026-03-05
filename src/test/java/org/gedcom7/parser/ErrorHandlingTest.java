package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlingTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    static class ErrorRecorder extends GedcomHandler {
        final List<String> events = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        final List<String> fatals = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) { events.add("startDocument"); }
        @Override
        public void endDocument() { events.add("endDocument"); }
        @Override
        public void startRecord(int level, String xref, String tag) {
            events.add("startRecord(" + tag + ")");
        }
        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            events.add("startStructure(" + tag + ")");
        }
        @Override
        public void warning(GedcomParseError error) {
            warnings.add(error.getMessage());
        }
        @Override
        public void error(GedcomParseError error) {
            errors.add(error.getMessage());
        }
        @Override
        public void fatalError(GedcomParseError error) {
            fatals.add(error.getMessage());
        }
    }

    @Test
    void lenientModeContinuesAfterError() {
        ErrorRecorder rec = new ErrorRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("malformed-lines.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // Should still reach endDocument despite malformed line
        assertTrue(rec.events.contains("endDocument"));
        // Should still find records after the malformed line
        assertTrue(rec.events.stream().anyMatch(e -> e.contains("BIRT")));
    }

    @Test
    void strictModeStopsOnFirstError() {
        // Create input with a banned character to trigger an error
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NOTE \u0007bell\n1 BIRT\n0 TRLR\n";
        ErrorRecorder rec = new ErrorRecorder();
        assertThrows(GedcomFatalException.class, () -> {
            try (GedcomReader reader = new GedcomReader(
                    stream(input), rec, GedcomReaderConfig.gedcom7Strict())) {
                reader.parse();
            }
        });
    }

    @Test
    void errorContextIncludesLineNumber() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NOTE \u0007bell\n0 TRLR\n";
        final List<GedcomParseError> capturedErrors = new ArrayList<>();
        GedcomHandler captor = new GedcomHandler() {
            @Override public void error(GedcomParseError error) {
                capturedErrors.add(error);
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), captor, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertFalse(capturedErrors.isEmpty());
        assertTrue(capturedErrors.get(0).getLineNumber() > 0,
                "Error should include line number");
    }

    @Test
    void maxNestingDepthEnforced() {
        // Build a file that exceeds depth limit of 3
        StringBuilder sb = new StringBuilder();
        sb.append("0 HEAD\n1 GEDC\n2 VERS 7.0\n");
        sb.append("0 @I1@ INDI\n");
        sb.append("1 BIRT\n");
        sb.append("2 DATE 1900\n");
        sb.append("3 DEEP too deep\n");
        sb.append("4 DEEPER way too deep\n");
        sb.append("0 TRLR\n");

        ErrorRecorder rec = new ErrorRecorder();
        GedcomReaderConfig cfg = new GedcomReaderConfig.Builder()
                .maxNestingDepth(3)
                .build();
        assertThrows(GedcomFatalException.class, () -> {
            try (GedcomReader reader = new GedcomReader(
                    stream(sb.toString()), rec, cfg)) {
                reader.parse();
            }
        });
    }
}
