package org.gedcom7.parser.validation;

import org.gedcom7.parser.GedcomHandler;
import org.gedcom7.parser.GedcomHeaderInfo;
import org.gedcom7.parser.GedcomParseError;
import org.gedcom7.parser.GedcomReader;
import org.gedcom7.parser.GedcomReaderConfig;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Phase 11: Structure and Cardinality Validation.
 */
class StructureValidationTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    /** Records warning messages for assertion. */
    static class WarningRecorder extends GedcomHandler {
        final List<String> warnings = new ArrayList<>();
        final List<String> events = new ArrayList<>();

        @Override
        public void startDocument(GedcomHeaderInfo header) {
            events.add("startDocument");
        }

        @Override
        public void endDocument() {
            events.add("endDocument");
        }

        @Override
        public void startRecord(int level, String xref, String tag) {
            events.add("startRecord(" + tag + ")");
        }

        @Override
        public void endRecord(String tag) {
            events.add("endRecord(" + tag + ")");
        }

        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            events.add("startStructure(" + tag + ")");
        }

        @Override
        public void endStructure(String tag) {
            events.add("endStructure(" + tag + ")");
        }

        @Override
        public void warning(GedcomParseError error) {
            warnings.add(error.getMessage());
        }

        @Override
        public void error(GedcomParseError error) {
            // ignore errors for these tests
        }

        @Override
        public void fatalError(GedcomParseError error) {
            // ignore
        }
    }

    // ─── T095a: Validation disabled by default (no warnings) ─────

    @Test
    void validationDisabledByDefault_noStructureWarnings() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = GedcomReaderConfig.gedcom7(); // default: validation OFF
        assertFalse(config.isStructureValidationEnabled());

        try (GedcomReader reader = new GedcomReader(
                resource("invalid-structure.ged"), rec, config)) {
            reader.parse();
        }

        // Should have no structure-related warnings
        boolean hasStructureWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Unknown structure") || w.contains("Cardinality"));
        assertFalse(hasStructureWarning,
                "Validation disabled: should produce no structure/cardinality warnings, but got: "
                        + rec.warnings);
    }

    // ─── T095b: Unknown structure in context warns ───────────────

    @Test
    void unknownStructureInContext_producesWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // HUSB is not a valid substructure of INDI
        try (GedcomReader reader = new GedcomReader(
                resource("invalid-structure.ged"), rec, config)) {
            reader.parse();
        }

        boolean hasUnknownWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Unknown structure") && w.contains("HUSB"));
        assertTrue(hasUnknownWarning,
                "Should warn about HUSB in INDI context, warnings: " + rec.warnings);
    }

    // ─── T095c: Cardinality {0:1} exceeded warns ────────────────

    @Test
    void cardinalityViolation_producesWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // Two SEX substructures under INDI (SEX has cardinality {0:1})
        try (GedcomReader reader = new GedcomReader(
                resource("cardinality-violation.ged"), rec, config)) {
            reader.parse();
        }

        boolean hasCardinalityWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Cardinality violation") && w.contains("SEX"));
        assertTrue(hasCardinalityWarning,
                "Should warn about duplicate SEX, warnings: " + rec.warnings);
    }

    // ─── T095d: Extension tags exempt from validation ────────────

    @Test
    void extensionTags_exemptFromValidation() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // extensions.ged has _CUSTOM, _OTHER, _UNDOCUMENTED under INDI
        try (GedcomReader reader = new GedcomReader(
                resource("extensions.ged"), rec, config)) {
            reader.parse();
        }

        // Extension tags should NOT produce "Unknown structure" warnings
        boolean hasExtensionWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Unknown structure") && w.contains("_"));
        assertFalse(hasExtensionWarning,
                "Extension tags should be exempt, but got: " + rec.warnings);
    }

    // ─── T095e: Validation enabled produces warnings for bad input ─

    @Test
    void validationEnabled_warnsOnInvalidStructure() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // DATE at level 0 is not valid, but our parser doesn't validate
        // level-0 record types — we validate substructures.
        // Instead, test a specific bad substructure: BIRT under FAM
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @F1@ FAM\n1 BIRT\n2 DATE 1 JAN 2000\n"
                + "0 TRLR\n";

        try (GedcomReader reader = new GedcomReader(
                stream(input), rec, config)) {
            reader.parse();
        }

        boolean hasWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Unknown structure") && w.contains("BIRT"));
        assertTrue(hasWarning,
                "BIRT is not valid under FAM, warnings: " + rec.warnings);
    }

    // ─── Valid file produces no structure warnings ───────────────

    @Test
    void validFile_noStructureWarnings() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        try (GedcomReader reader = new GedcomReader(
                resource("all-record-types.ged"), rec, config)) {
            reader.parse();
        }

        boolean hasStructureWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Unknown structure") || w.contains("Cardinality"));
        assertFalse(hasStructureWarning,
                "Valid file should produce no structure warnings, got: " + rec.warnings);
    }

    // ─── Cardinality violation with inline input ─────────────────

    @Test
    void duplicateSingleton_inline_producesWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // Two HUSB under FAM (cardinality {0:1})
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @F1@ FAM\n1 HUSB @I1@\n1 HUSB @I2@\n"
                + "0 TRLR\n";

        try (GedcomReader reader = new GedcomReader(
                stream(input), rec, config)) {
            reader.parse();
        }

        boolean hasCardinalityWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Cardinality violation") && w.contains("HUSB"));
        assertTrue(hasCardinalityWarning,
                "Should warn about duplicate HUSB, warnings: " + rec.warnings);
    }

    // ─── StructureDefinitions unit tests ─────────────────────────

    @Test
    void structureDefinitions_resolveKnownStructure() {
        String result = StructureDefinitions.resolveStructure("record-INDI", "NAME");
        assertEquals("INDI-NAME", result);
    }

    @Test
    void structureDefinitions_resolveUnknownReturnsNull() {
        String result = StructureDefinitions.resolveStructure("record-INDI", "HUSB");
        assertNull(result, "HUSB is not valid under INDI");
    }

    @Test
    void structureDefinitions_cardinalitySingleton() {
        String card = StructureDefinitions.getCardinality("record-INDI", "SEX");
        assertEquals("{0:1}", card);
        assertTrue(StructureDefinitions.isSingleton(card));
    }

    @Test
    void structureDefinitions_cardinalityMultiple() {
        String card = StructureDefinitions.getCardinality("record-INDI", "BIRT");
        assertEquals("{0:M}", card);
        assertFalse(StructureDefinitions.isSingleton(card));
    }

    @Test
    void structureDefinitions_recordContext() {
        assertEquals("record-INDI", StructureDefinitions.recordContext("INDI"));
        assertEquals("record-FAM", StructureDefinitions.recordContext("FAM"));
        assertEquals("HEAD", StructureDefinitions.recordContext("HEAD"));
        assertNull(StructureDefinitions.recordContext("TRLR"));
    }
}
