package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XrefValidationTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    static class ErrorCollector extends GedcomHandler {
        final List<String> errors = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) {}
        @Override
        public void endDocument() {}
        @Override
        public void startRecord(int level, String xref, String tag) {}
        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {}
        @Override
        public void warning(GedcomParseError error) {
            warnings.add(error.getMessage());
        }
        @Override
        public void error(GedcomParseError error) {
            errors.add(error.getMessage());
        }
    }

    private GedcomReaderConfig validationConfig() {
        return new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();
    }

    private GedcomReaderConfig noValidationConfig() {
        return GedcomReaderConfig.gedcom7();
    }

    @Test
    void xrefWithSpaceProducesWarning() {
        // @I 1@ contains a space (U+0020), which is below U+0021
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I 1@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().anyMatch(w -> w.contains("Invalid xref")),
                "Should warn about invalid xref character (space). Warnings: " + col.warnings);
    }

    @Test
    void validXrefNoWarning() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().noneMatch(w -> w.contains("Invalid xref")),
                "Valid xref should not produce xref warnings. Warnings: " + col.warnings);
    }

    @Test
    void emptyXrefProducesWarning() {
        // @@ as xref means empty identifier between delimiters
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().anyMatch(w -> w.contains("xref") && w.contains("empty")),
                "Should warn about empty xref. Warnings: " + col.warnings);
    }

    @Test
    void xrefWithControlCharacterProducesWarning() {
        // Control character U+0001 is below U+0021
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I\u0001X@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().anyMatch(w -> w.contains("Invalid xref")),
                "Should warn about control character in xref. Warnings: " + col.warnings);
    }

    @Test
    void xrefWithHashProducesWarning() {
        // # (U+0023) is explicitly forbidden
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I#1@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().anyMatch(w -> w.contains("Invalid xref")),
                "Should warn about # in xref. Warnings: " + col.warnings);
    }

    @Test
    void validationDisabledNoXrefWarning() {
        // Same invalid xref with space, but validation disabled
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I 1@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, noValidationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().noneMatch(w -> w.contains("Invalid xref")),
                "No xref warnings when validation is disabled. Warnings: " + col.warnings);
    }

    @Test
    void voidXrefPassesValidation() {
        // @VOID@ is a special null pointer — should not produce xref warnings
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 FAMC @VOID@\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().noneMatch(w -> w.contains("Invalid xref") && w.contains("VOID")),
                "@VOID@ should pass validation. Warnings: " + col.warnings);
    }

    @Test
    void xrefExceeding20CharsProducesWarning() {
        // 21 characters between @ delimiters
        String xref = "ABCDEFGHIJKLMNOPQRSTU"; // 21 chars
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @" + xref + "@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().anyMatch(w -> w.contains("xref") && w.contains("20")),
                "Should warn about xref exceeding 20 chars. Warnings: " + col.warnings);
    }

    @Test
    void xrefExactly20CharsNoWarning() {
        // Exactly 20 characters — should be fine
        String xref = "ABCDEFGHIJKLMNOPQRST"; // 20 chars
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @" + xref + "@ INDI\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().noneMatch(w -> w.contains("xref") && w.contains("20")),
                "Xref of exactly 20 chars should not produce length warning. Warnings: " + col.warnings);
    }

    @Test
    void pointerReferenceXrefValidated() {
        // Pointer reference with invalid character (space) in the referenced xref
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 FAMC @F 1@\n0 TRLR\n";
        ErrorCollector col = new ErrorCollector();
        try (GedcomReader reader = new GedcomReader(
                stream(input), col, validationConfig())) {
            reader.parse();
        }
        assertTrue(col.warnings.stream().anyMatch(w -> w.contains("Invalid xref")),
                "Should warn about invalid character in pointer reference xref. Warnings: " + col.warnings);
    }
}
