package org.gedcom7.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CharacterValidationTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    static class ErrorCollector extends GedcomHandler {
        final List<String> errors = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        final List<String> events = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) {}
        @Override
        public void endDocument() {}
        @Override
        public void startRecord(int level, String xref, String tag) {
            events.add("startRecord(" + tag + ")");
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
            errors.add(error.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(chars = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, /* skip 0x09=TAB */ 0x0B, 0x0C, /* skip 0x0D=CR, 0x0A=LF */
            0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
            0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x7F})
    void bannedC0ControlsAndDelProduceErrors(char banned) {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n1 NOTE x" + banned + "y\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(col.errors.stream().anyMatch(e -> e.contains("Banned character")),
                "Should report banned character 0x" + Integer.toHexString(banned));
    }

    @Test
    void tabIsAccepted() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n1 NOTE before\tafter\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(col.errors.isEmpty(), "TAB should be accepted, no errors");
        assertTrue(col.events.stream().anyMatch(e -> e.contains("before\tafter")));
    }

    @Test
    void multiByteUtf8Accepted() {
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                resource("multibyte-utf8.ged"), col, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(col.errors.isEmpty(), "Multi-byte UTF-8 should not produce errors");
    }

    @Test
    void bannedCharsFileProducesErrors() {
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                resource("banned-chars.ged"), col, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertFalse(col.errors.isEmpty(), "Banned chars file should produce errors");
    }
}
