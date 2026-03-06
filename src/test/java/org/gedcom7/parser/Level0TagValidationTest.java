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

/**
 * Tests for level-0 record type validation (US9).
 * When structure validation is enabled, unrecognized record types
 * at level 0 should produce a warning. Standard types and extension
 * tags (starting with _) should not warn.
 */
class Level0TagValidationTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private GedcomReaderConfig validationConfig() {
        return new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();
    }

    @Test
    void unknownLevel0Tag_emitsWarning() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @X1@ ZZUNKNOWN\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, validationConfig())) {
            reader.parse();
        }
        assertTrue(warnings.stream().anyMatch(
                        w -> w.contains("Unknown record type at level 0") && w.contains("ZZUNKNOWN")),
                "Should warn about unknown level-0 record type ZZUNKNOWN: " + warnings);
    }

    @Test
    void standardINDI_noWarning() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, validationConfig())) {
            reader.parse();
        }
        assertFalse(warnings.stream().anyMatch(w -> w.contains("Unknown record type")),
                "INDI should not produce unknown record type warning: " + warnings);
    }

    @Test
    void extensionTag_noWarning() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @X1@ _CUSTOM\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, validationConfig())) {
            reader.parse();
        }
        assertFalse(warnings.stream().anyMatch(w -> w.contains("Unknown record type")),
                "Extension tags (_CUSTOM) should not produce unknown record type warning: " + warnings);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INDI", "FAM", "OBJE", "REPO", "SNOTE", "SOUR", "SUBM"})
    void allStandardRecordTypes_noWarning(String tag) {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @X1@ " + tag + "\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, validationConfig())) {
            reader.parse();
        }
        assertFalse(warnings.stream().anyMatch(w -> w.contains("Unknown record type")),
                tag + " should not produce unknown record type warning: " + warnings);
    }

    @Test
    void headDoesNotWarn() {
        // HEAD is processed separately but should not warn
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, validationConfig())) {
            reader.parse();
        }
        assertFalse(warnings.stream().anyMatch(w -> w.contains("Unknown record type")),
                "HEAD should not produce unknown record type warning: " + warnings);
    }

    @Test
    void trlrDoesNotWarn() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, validationConfig())) {
            reader.parse();
        }
        assertFalse(warnings.stream().anyMatch(w -> w.contains("Unknown record type") && w.contains("TRLR")),
                "TRLR should not produce unknown record type warning: " + warnings);
    }

    @Test
    void validationDisabled_noWarningForUnknownTag() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @X1@ ZZUNKNOWN\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error.getMessage());
            }
        };
        // Default config has structure validation disabled
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertFalse(warnings.stream().anyMatch(w -> w.contains("Unknown record type")),
                "With validation disabled, should not warn about unknown record types: " + warnings);
    }
}
