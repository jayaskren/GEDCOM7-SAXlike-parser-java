package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionTagTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    static class ExtensionRecorder extends GedcomHandler {
        final List<String> events = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        GedcomHeaderInfo header;

        @Override
        public void startDocument(GedcomHeaderInfo header) {
            this.header = header;
            events.add("startDocument");
        }
        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            events.add("startStructure(" + tag + "," + value + ")");
        }
        @Override
        public void warning(GedcomParseError error) {
            warnings.add(error.getMessage());
        }
        @Override
        public void error(GedcomParseError error) {
            warnings.add("ERROR:" + error.getMessage());
        }
    }

    @Test
    void schmaTagsParsedIntoSchemaMap() {
        ExtensionRecorder rec = new ExtensionRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("extensions.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertNotNull(rec.header);
        assertEquals("https://example.com/custom", rec.header.getSchemaMap().get("_CUSTOM"));
        assertEquals("https://example.com/other", rec.header.getSchemaMap().get("_OTHER"));
    }

    @Test
    void documentedExtensionTagParsesWithoutError() {
        ExtensionRecorder rec = new ExtensionRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("extensions.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.events.stream().anyMatch(e -> e.contains("_CUSTOM")));
    }

    @Test
    void undocumentedExtensionTagParsesWithoutError() {
        ExtensionRecorder rec = new ExtensionRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("extensions.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // _UNDOCUMENTED should appear in events without errors
        assertTrue(rec.events.stream().anyMatch(e -> e.contains("_UNDOCUMENTED")));
        assertTrue(rec.warnings.stream().noneMatch(w -> w.contains("_UNDOCUMENTED")),
                "Undocumented extension tags should not produce warnings");
    }
}
