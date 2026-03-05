package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrossReferenceTrackingTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    static class XrefRecorder extends GedcomHandler {
        final List<String> events = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) {}
        @Override
        public void endDocument() {}
        @Override
        public void startRecord(int level, String xref, String tag) {
            events.add("startRecord(" + xref + "," + tag + ")");
        }
        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            String ptr = isPointer ? ",ptr" : "";
            events.add("startStructure(" + tag + "," + value + ptr + ")");
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

    @Test
    void pointerValuesAreDistinguished() {
        XrefRecorder rec = new XrefRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("cross-references.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // FAMS @F1@ should be a pointer
        assertTrue(rec.events.stream().anyMatch(e -> e.contains("FAMS") && e.contains(",ptr")));
        // NAME should NOT be a pointer
        assertTrue(rec.events.stream().anyMatch(e -> e.contains("NAME") && !e.contains(",ptr")));
    }

    @Test
    void voidPointerIsSkippedInTracking() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 FAMC @VOID@\n0 TRLR\n";
        XrefRecorder rec = new XrefRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream(input), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // @VOID@ should NOT produce an unresolved reference warning
        assertTrue(rec.warnings.stream().noneMatch(w -> w.contains("VOID")),
                "@VOID@ should not be reported as unresolved");
    }

    @Test
    void unresolvedReferenceProducesWarning() {
        XrefRecorder rec = new XrefRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("cross-references.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        // @I3@ is referenced but not defined
        assertTrue(rec.warnings.stream().anyMatch(w -> w.contains("I3")),
                "Unresolved @I3@ should produce a warning");
    }

    @Test
    void allResolvedReferencesNoWarning() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 FAMS @F1@\n0 @F1@ FAM\n1 HUSB @I1@\n0 TRLR\n";
        XrefRecorder rec = new XrefRecorder();
        try (GedcomReader reader = new GedcomReader(
                stream(input), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.warnings.stream().noneMatch(w -> w.contains("Unresolved")),
                "All references resolved, no warnings expected");
    }

    @Test
    void duplicateXrefProducesError() {
        XrefRecorder rec = new XrefRecorder();
        try (GedcomReader reader = new GedcomReader(
                resource("duplicate-xref.ged"), rec, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(rec.errors.stream().anyMatch(e -> e.contains("Duplicate") && e.contains("I1")),
                "Duplicate @I1@ should produce an error");
    }
}
