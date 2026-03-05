package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GapRemediationTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    // --- Gap 1: FR-090 Raw Line in Errors ---

    @Test
    void errorIncludesRawLine() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NOTE \u0007bell\n0 TRLR\n";
        List<GedcomParseError> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) {
                errors.add(error);
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertFalse(errors.isEmpty(), "Should have errors for banned character");
        assertNotNull(errors.get(0).getRawLine(), "Error should include raw line");
        assertTrue(errors.get(0).getRawLine().contains("NOTE"), "Raw line should contain the tag");
    }

    @Test
    void warningIncludesRawLine_leadingWhitespace() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n  0 TRLR\n";
        List<GedcomParseError> warnings = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) {
                warnings.add(error);
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        boolean hasWhitespaceWarning = warnings.stream()
                .anyMatch(w -> w.getMessage().contains("leading whitespace"));
        assertTrue(hasWhitespaceWarning, "Should warn about leading whitespace");
        GedcomParseError wsWarning = warnings.stream()
                .filter(w -> w.getMessage().contains("leading whitespace"))
                .findFirst().orElseThrow();
        assertNotNull(wsWarning.getRawLine(), "Warning should include raw line");
    }

    // --- Gap 2: FR-094 Max Line Length ---

    @Test
    void maxLineLengthEnforced_fatalError() {
        String longValue = "x".repeat(100);
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n1 NOTE " + longValue + "\n0 TRLR\n";
        List<GedcomParseError> fatals = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void fatalError(GedcomParseError error) {
                fatals.add(error);
            }
        };
        GedcomReaderConfig cfg = new GedcomReaderConfig.Builder()
                .maxLineLength(50)
                .build();
        assertThrows(GedcomFatalException.class, () -> {
            try (GedcomReader reader = new GedcomReader(stream(input), handler, cfg)) {
                reader.parse();
            }
        });
    }

    @Test
    void defaultMaxLineLength_noErrorOnNormalInput() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n1 NOTE " + "x".repeat(1000) + "\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
            @Override
            public void fatalError(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(errors.isEmpty(), "Normal-length lines should not trigger errors");
    }

    // --- Gap 3: FR-009 Leading Whitespace ---

    @Test
    void leadingWhitespace_warningFiredAndParsingContinues() {
        String input = "0 HEAD\n  1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        List<String> warnings = new ArrayList<>();
        List<String> events = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void warning(GedcomParseError error) { warnings.add(error.getMessage()); }
            @Override
            public void endDocument() { events.add("endDocument"); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(warnings.stream().anyMatch(w -> w.contains("leading whitespace")),
                "Should warn about leading whitespace");
        assertTrue(events.contains("endDocument"), "Parsing should complete despite warning");
    }

    // --- Gap 4: FR-033 Level Jump Validation ---

    @Test
    void levelJumpMoreThanOne_reportsError() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n3 DEEP bad\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(errors.stream().anyMatch(e -> e.contains("Level jumps")),
                "Should report level jump error: " + errors);
    }

    @Test
    void validNesting_noLevelJumpError() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 BIRT\n2 DATE 1900\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        boolean hasJumpError = errors.stream().anyMatch(e -> e.contains("Level jumps"));
        assertFalse(hasJumpError, "Valid nesting should not produce level jump errors: " + errors);
    }

    @Test
    void levelJump_level1ToLevel3_reportsError() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 BIRT\n3 DEEP bad\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(errors.stream().anyMatch(e -> e.contains("Level jumps")),
                "Should report level jump from 1 to 3: " + errors);
    }

    // --- Gap 5: FR-054 TRLR Validation ---

    @Test
    void trlrWithValue_reportsError() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR some_value\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(errors.stream().anyMatch(e -> e.contains("TRLR") && e.contains("value")),
                "Should report error for TRLR with value: " + errors);
    }

    @Test
    void trlrWithXref_reportsError() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @X1@ TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(errors.stream().anyMatch(e -> e.contains("TRLR") && e.contains("cross-reference")),
                "Should report error for TRLR with xref: " + errors);
    }

    @Test
    void contentAfterTrlr_reportsError() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n1 SUB oops\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(errors.stream().anyMatch(e -> e.contains("after TRLR")),
                "Should report error for content after TRLR: " + errors);
    }

    @Test
    void validTrlr_noErrors() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        List<String> errors = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void error(GedcomParseError error) { errors.add(error.getMessage()); }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        boolean hasTrlrError = errors.stream().anyMatch(e -> e.contains("TRLR"));
        assertFalse(hasTrlrError, "Valid TRLR should produce no TRLR-related errors: " + errors);
    }

    // --- Gap 6: FR-072 Extension Tag URI ---

    @Test
    void extensionTagUri_passedInSixParamCallback() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n1 SCHMA\n2 TAG _CUSTOM https://example.com/custom\n"
                + "0 @I1@ INDI\n1 _CUSTOM value\n0 TRLR\n";
        List<String> uris = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer, String uri) {
                if (uri != null) {
                    uris.add(tag + "=" + uri);
                }
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(uris.stream().anyMatch(u -> u.contains("_CUSTOM") && u.contains("https://example.com/custom")),
                "Extension tag should receive URI: " + uris);
    }

    @Test
    void standardTag_nullUri() {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NAME John\n0 TRLR\n";
        List<String> nullUriTags = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer, String uri) {
                if (uri == null) {
                    nullUriTags.add(tag);
                }
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(nullUriTags.contains("NAME"), "Standard tags should have null URI");
    }

    // --- Gap 7: NFR-013 Pluggable Strategies ---

    @Test
    void defaultStrategies_usedWhenConfigHasNone() {
        // Default config with null strategies should parse normally
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 @I1@ INDI\n1 NAME John\n0 TRLR\n";
        List<String> events = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                events.add(tag);
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(events.contains("INDI"), "Default strategies should parse normally");
    }
}
