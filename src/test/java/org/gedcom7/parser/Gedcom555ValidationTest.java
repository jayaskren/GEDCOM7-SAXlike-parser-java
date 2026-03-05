package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GEDCOM 5.5.5 validation checks:
 * line length, BOM requirement, level jumps.
 */
class Gedcom555ValidationTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    private InputStream stringStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void strictModeLongLineFatal() {
        GedcomHandler handler = new GedcomHandler() {};

        assertThrows(GedcomFatalException.class, () -> {
            try (GedcomReader reader = new GedcomReader(
                    resource("gedcom555/long-lines.ged"), handler,
                    GedcomReaderConfig.gedcom555Strict())) {
                reader.parse();
            }
        });
    }

    @Test
    void lenientModeLongLineNotFatal() {
        GedcomHandler handler = new GedcomHandler() {};

        // Lenient mode has default maxLineLength of 1MB, so the 300-char line is fine
        assertDoesNotThrow(() -> {
            try (GedcomReader reader = new GedcomReader(
                    resource("gedcom555/long-lines.ged"), handler,
                    GedcomReaderConfig.gedcom555())) {
                reader.parse();
            }
        });
    }

    @Test
    void strictModeNoBomFatal() {
        // no-char-tag.ged has a UTF-8 BOM but no CHAR tag.
        // We need a file with NO BOM for this test.
        // Create a 5.5.5 file without BOM.
        String content = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n1 CHAR UTF-8\n0 TRLR\n";
        GedcomHandler handler = new GedcomHandler() {};

        assertThrows(GedcomFatalException.class, () -> {
            try (GedcomReader reader = new GedcomReader(
                    stringStream(content), handler,
                    GedcomReaderConfig.gedcom555Strict())) {
                reader.parse();
            }
        });
    }

    @Test
    void lenientModeNoBomWarns() {
        // 5.5.5 file without BOM
        String content = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n1 CHAR UTF-8\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                stringStream(content), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        boolean hasBomWarning = warnings.stream()
                .anyMatch(msg -> msg.toLowerCase().contains("bom"));
        assertTrue(hasBomWarning,
                "Should warn about missing BOM; warnings: " + warnings);
    }

    @Test
    void levelJumpValidation() {
        // Level jumps from 0 directly to 3
        String content = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n1 CHAR UTF-8\n0 @I1@ INDI\n3 NAME Bad /Jump/\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) {
                errors.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                stringStream(content), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        boolean hasLevelJumpError = errors.stream()
                .anyMatch(msg -> msg.toLowerCase().contains("level"));
        assertTrue(hasLevelJumpError,
                "Should report level jump error; errors: " + errors);
    }

    @Test
    void xrefExceeding22CharsWarns() {
        // @AVERYLONGIDENTIFIER123@ = 25 chars including @ signs
        String content = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n1 CHAR UTF-8\n"
                + "0 @AVERYLONGIDENTIFIER123@ INDI\n1 NAME Test /Person/\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                stringStream(content), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        boolean hasXrefWarning = warnings.stream()
                .anyMatch(msg -> msg.contains("22-character"));
        assertTrue(hasXrefWarning,
                "Should warn about xref exceeding 22 chars; warnings: " + warnings);
    }

    @Test
    void bareAtInValueErrors() {
        String content = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n1 CHAR UTF-8\n"
                + "0 @I1@ INDI\n1 NOTE email@example.com\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) {
                errors.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                stringStream(content), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        boolean hasBareAtError = errors.stream()
                .anyMatch(msg -> msg.contains("Bare @"));
        assertTrue(hasBareAtError,
                "Should error on bare @ in value; errors: " + errors);
    }

    @Test
    void escapedAtDoesNotError() {
        String content = "0 HEAD\n1 GEDC\n2 VERS 5.5.5\n1 CHAR UTF-8\n"
                + "0 @I1@ INDI\n1 NOTE email@@example.com\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) {
                errors.add(error.getMessage());
            }
        };

        try (GedcomReader reader = new GedcomReader(
                stringStream(content), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        boolean hasBareAtError = errors.stream()
                .anyMatch(msg -> msg.contains("Bare @"));
        assertFalse(hasBareAtError,
                "Escaped @@ should not trigger bare @ error; errors: " + errors);
    }
}
