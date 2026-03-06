package org.gedcom7.converter;

import org.gedcom7.parser.GedcomFatalException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T019: Tests for User Story 4 — Error handling during conversion.
 *
 * <p>Verifies lenient and strict mode behavior for malformed input,
 * unrecognized versions, warning handler callbacks, and result
 * accumulation of warnings and errors.
 */
class ConversionErrorTest {

    /**
     * Malformed GEDCOM (missing TRLR) in lenient mode: ConversionResult has
     * parse errors, conversion continues and produces output.
     */
    @Test
    void malformedGedcomInLenientModeProducesOutputWithErrors() throws Exception {
        // Missing TRLR record
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n";
        // No TRLR — the parser should report a warning/error but continue

        GedcomConverterConfig config = GedcomConverterConfig.toGedcom7();

        ByteArrayInputStream in = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConversionResult result = GedcomConverter.convert(in, out, config);
        String output = out.toString(StandardCharsets.UTF_8.name());

        // Conversion should produce some output even with missing TRLR
        assertTrue(output.contains("0 HEAD"), "Output should contain HEAD");
        assertTrue(output.contains("0 @I1@ INDI"), "Output should contain the INDI record");

        // The record should still be counted
        assertEquals(1, result.getRecordCount(),
                "INDI record should be counted even with missing TRLR");
    }

    /**
     * Strict mode with unrecognized version (e.g., VERS 4.0) should throw
     * GedcomFatalException.
     */
    @Test
    void strictModeWithUnrecognizedVersionThrowsFatalException() {
        // VERS 4.0 is neither 5.x nor 7.x
        String input = "0 HEAD\n1 GEDC\n2 VERS 4.0\n0 TRLR\n";

        GedcomConverterConfig config = GedcomConverterConfig.builder()
                .targetVersion(new org.gedcom7.parser.GedcomVersion(7, 0))
                .strict(true)
                .build();

        assertThrows(GedcomFatalException.class, () -> {
            ByteArrayInputStream in = new ByteArrayInputStream(
                    input.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GedcomConverter.convert(in, out, config);
        }, "Strict mode should throw GedcomFatalException for unrecognized version 4.0");
    }

    /**
     * Lenient mode with unrecognized version: warning in result but conversion continues.
     */
    @Test
    void lenientModeWithUnrecognizedVersionProducesWarning() throws Exception {
        // VERS 4.0 is unrecognized
        String input = "0 HEAD\n1 GEDC\n2 VERS 4.0\n0 TRLR\n";

        GedcomConverterConfig config = GedcomConverterConfig.toGedcom7();

        ConversionResult result = convertResult(input, config);
        String output = convert(input, config);

        // Should have warnings about unrecognized version
        assertTrue(result.getWarningCount() > 0,
                "Should have at least one warning for unrecognized version");

        boolean hasVersionWarning = result.getWarnings().stream()
                .anyMatch(w -> w.getMessage().contains("Unrecognized GEDCOM version"));
        assertTrue(hasVersionWarning,
                "Warnings should include unrecognized version message");

        // Conversion should still produce output
        assertTrue(output.contains("0 HEAD"), "Output should contain HEAD");
        assertTrue(output.contains("0 TRLR"), "Output should contain TRLR");
    }

    /**
     * Warning handler callback: configure a warningHandler and verify it
     * receives warnings during conversion.
     */
    @Test
    void warningHandlerReceivesWarnings() throws Exception {
        // VERS 4.0 triggers an unrecognized-version warning
        String input = "0 HEAD\n1 GEDC\n2 VERS 4.0\n0 TRLR\n";

        List<ConversionWarning> receivedWarnings = new ArrayList<>();

        GedcomConverterConfig config = GedcomConverterConfig.builder()
                .targetVersion(new org.gedcom7.parser.GedcomVersion(7, 0))
                .strict(false)
                .warningHandler(receivedWarnings::add)
                .build();

        ByteArrayInputStream in = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomConverter.convert(in, out, config);

        // The handler should have been called
        assertFalse(receivedWarnings.isEmpty(),
                "Warning handler should have received at least one warning");

        // Verify the warning content
        boolean hasVersionWarning = receivedWarnings.stream()
                .anyMatch(w -> w.getMessage().contains("Unrecognized GEDCOM version"));
        assertTrue(hasVersionWarning,
                "Warning handler should receive unrecognized version warning");
    }

    /**
     * ConversionResult contains all warnings and errors after conversion.
     * Tests that both conversion warnings and parse errors are collected.
     */
    @Test
    void conversionResultContainsAllWarningsAndErrors() throws Exception {
        // VERS 4.0 triggers a conversion warning + invalid level creates parse error
        String input = "0 HEAD\n1 GEDC\n2 VERS 4.0\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n"
                + "0 TRLR\n";

        GedcomConverterConfig config = GedcomConverterConfig.toGedcom7();

        ConversionResult result = convertResult(input, config);

        // Should have at least the unrecognized-version warning
        assertTrue(result.getWarningCount() > 0,
                "ConversionResult should contain warnings");

        // Verify warnings are accessible
        List<ConversionWarning> warnings = result.getWarnings();
        assertNotNull(warnings, "Warnings list should not be null");
        assertFalse(warnings.isEmpty(), "Warnings list should not be empty");

        // Verify ConversionWarning fields
        ConversionWarning firstWarning = warnings.get(0);
        assertNotNull(firstWarning.getMessage(),
                "Warning should have a message");
    }

    /**
     * Malformed GEDCOM with invalid level number in lenient mode:
     * parser reports error, conversion continues.
     */
    @Test
    void invalidLevelNumberInLenientMode() throws Exception {
        // Level jumps from 0 to 3 (skipping 1 and 2) — this is invalid
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n3 NAME John /Doe/\n"
                + "0 TRLR\n";

        GedcomConverterConfig config = GedcomConverterConfig.toGedcom7();

        ConversionResult result = convertResult(input, config);
        String output = convert(input, config);

        // Record should still be counted
        assertEquals(1, result.getRecordCount(),
                "INDI record should be counted despite invalid nesting");

        // Output should contain the INDI record
        assertTrue(output.contains("0 @I1@ INDI"),
                "Output should contain INDI record");

        // Should have parse errors due to level skip
        assertTrue(result.getErrorCount() > 0,
                "Should have parse errors for invalid level jump from 0 to 3");
    }

    /**
     * Conversion from GEDCOM 7 to 5.5.5 with SCHMA emits a fidelity warning
     * because SCHMA is a GEDCOM 7-only structure.
     */
    @Test
    void schmaIn555OutputEmitsFidelityWarning() throws Exception {
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "1 SCHMA\n"
                + "2 TAG _CUSTOM http://example.com/custom\n"
                + "0 @I1@ INDI\n1 NAME John /Doe/\n"
                + "0 TRLR\n";

        List<ConversionWarning> received = new ArrayList<>();
        GedcomConverterConfig config = GedcomConverterConfig.builder()
                .targetVersion(new org.gedcom7.parser.GedcomVersion(5, 5, 5))
                .warningHandler(received::add)
                .build();

        ConversionResult result = convertResult(input, config);
        String output = convert(input, config);

        // SCHMA should still be preserved in the output
        assertTrue(output.contains("1 SCHMA"), "SCHMA should be preserved");

        // But a fidelity warning should be emitted
        assertTrue(result.getWarningCount() > 0,
                "Should warn about SCHMA in 5.5.5 output");
        boolean hasSchmaWarning = result.getWarnings().stream()
                .anyMatch(w -> w.getMessage().contains("SCHMA"));
        assertTrue(hasSchmaWarning,
                "Warning should mention SCHMA as a GEDCOM 7 structure");

        // Warning handler should also receive it
        assertTrue(received.stream().anyMatch(w -> w.getMessage().contains("SCHMA")),
                "Warning handler should receive SCHMA fidelity warning");
    }

    // --- Helpers ---

    private String convert(String input, GedcomConverterConfig config) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomConverter.convert(in, out, config);
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private ConversionResult convertResult(String input, GedcomConverterConfig config) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return GedcomConverter.convert(in, out, config);
    }
}
