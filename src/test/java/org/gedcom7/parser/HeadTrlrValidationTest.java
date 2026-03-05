package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeadTrlrValidationTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    static class ValidationRecorder extends GedcomHandler {
        final List<String> events = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final List<String> fatals = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) {
            events.add("startDocument(" + header.getVersion() + ")");
        }
        @Override
        public void endDocument() { events.add("endDocument"); }
        @Override
        public void startRecord(int level, String xref, String tag) {
            events.add("startRecord(" + tag + ")");
        }
        @Override
        public void warning(GedcomParseError error) { warnings.add(error.getMessage()); }
        @Override
        public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        @Override
        public void fatalError(GedcomParseError error) { fatals.add(error.getMessage()); }
    }

    @Test
    void missingHeadProducesFatalError() {
        ValidationRecorder rec = new ValidationRecorder();
        assertThrows(GedcomFatalException.class, () -> {
            try (GedcomReader reader = new GedcomReader(
                    resource("no-head.ged"), rec, GedcomReaderConfig.gedcom7())) {
                reader.parse();
            }
        });
        assertTrue(rec.fatals.stream().anyMatch(f -> f.contains("HEAD")));
    }

    @Test
    void missingTrlrProducesWarning() {
        ValidationRecorder rec = new ValidationRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("no-trlr.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.warnings.stream().anyMatch(w -> w.contains("TRLR")),
                "Missing TRLR should produce a warning");
    }

    @Test
    void version70Accepted() {
        ValidationRecorder rec = new ValidationRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream("0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n"),
                rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.events.get(0).contains("7.0"));
        assertTrue(rec.warnings.stream().noneMatch(w -> w.contains("version")));
    }

    @Test
    void version71Accepted() {
        ValidationRecorder rec = new ValidationRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream("0 HEAD\n1 GEDC\n2 VERS 7.1\n0 TRLR\n"),
                rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.events.get(0).contains("7.1"));
    }

    @Test
    void nonGedcom7VersionProducesWarning() {
        ValidationRecorder rec = new ValidationRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream("0 HEAD\n1 GEDC\n2 VERS 5.5.1\n0 TRLR\n"),
                rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.warnings.stream().anyMatch(w -> w.contains("version") || w.contains("GEDCOM 7")),
                "Non-7.x version should produce a warning");
    }

    @Test
    void headerInfoPopulated() {
        ValidationRecorder rec = new ValidationRecorder();
        final GedcomHeaderInfo[] capturedHeader = {null};
        GedcomHandler captor = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                capturedHeader[0] = header;
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream("0 HEAD\n1 GEDC\n2 VERS 7.0\n1 SOUR MY_APP\n2 VERS 2.0\n2 NAME My Application\n1 LANG en\n0 TRLR\n"),
                captor, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertNotNull(capturedHeader[0]);
        assertEquals(new GedcomVersion(7, 0), capturedHeader[0].getVersion());
        assertEquals("MY_APP", capturedHeader[0].getSourceSystem());
        assertEquals("2.0", capturedHeader[0].getSourceVersion());
        assertEquals("My Application", capturedHeader[0].getSourceName());
        assertEquals("en", capturedHeader[0].getDefaultLanguage());
    }

    @Test
    void schemaMapPopulated() {
        final GedcomHeaderInfo[] capturedHeader = {null};
        GedcomHandler captor = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                capturedHeader[0] = header;
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream("0 HEAD\n1 GEDC\n2 VERS 7.0\n1 SCHMA\n2 TAG _CUSTOM https://example.com/custom\n0 TRLR\n"),
                captor, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertNotNull(capturedHeader[0]);
        assertEquals("https://example.com/custom", capturedHeader[0].getSchemaMap().get("_CUSTOM"));
    }

    @Test
    void startDocumentFiresBeforeHeadRecord() {
        List<String> order = new ArrayList<>();
        GedcomHandler tracker = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                order.add("startDocument");
            }
            @Override
            public void startRecord(int level, String xref, String tag) {
                order.add("startRecord(" + tag + ")");
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream("0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n"),
                tracker, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(order.indexOf("startDocument") < order.indexOf("startRecord(HEAD)"),
                "startDocument should fire before HEAD startRecord");
    }
}
