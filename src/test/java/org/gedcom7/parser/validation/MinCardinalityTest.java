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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for minimum cardinality validation ({1:1} and {1:M} required children).
 */
class MinCardinalityTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    /** Records warning messages for assertion. */
    static class WarningRecorder extends GedcomHandler {
        final List<String> warnings = new ArrayList<>();

        @Override public void startDocument(GedcomHeaderInfo header) {}
        @Override public void endDocument() {}
        @Override public void startRecord(int level, String xref, String tag) {}
        @Override public void endRecord(String tag) {}
        @Override public void startStructure(int level, String xref, String tag,
                                             String value, boolean isPointer) {}
        @Override public void endStructure(String tag) {}

        @Override
        public void warning(GedcomParseError error) {
            warnings.add(error.getMessage());
        }

        @Override public void error(GedcomParseError error) {}
        @Override public void fatalError(GedcomParseError error) {}
    }

    // ─── T015a: ASSO without ROLE → warning ──────────────────

    @Test
    void assoWithoutRole_emitsWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // ASSO without ROLE child; ROLE has {1:1} cardinality under ASSO
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 ASSO @I2@\n"
                + "0 TRLR\n";

        try (GedcomReader reader = new GedcomReader(stream(input), rec, config)) {
            reader.parse();
        }

        boolean hasMinCardWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Missing required child structure")
                        && w.contains("ROLE"));
        assertTrue(hasMinCardWarning,
                "Should warn about missing ROLE under ASSO, warnings: " + rec.warnings);
    }

    // ─── T015b: ASSO with ROLE → no warning ─────────────────

    @Test
    void assoWithRole_noWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // ASSO with ROLE child present
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 ASSO @I2@\n2 ROLE GODP\n"
                + "0 TRLR\n";

        try (GedcomReader reader = new GedcomReader(stream(input), rec, config)) {
            reader.parse();
        }

        boolean hasMinCardWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Missing required child structure")
                        && w.contains("ROLE"));
        assertFalse(hasMinCardWarning,
                "Should NOT warn about ROLE when present, warnings: " + rec.warnings);
    }

    // ─── T015c: {1:M} child present → no warning ───────────

    @Test
    void requiredMultipleChildPresent_noWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // OBJE with FILE child (FILE has {1:M} under record-OBJE)
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @O1@ OBJE\n1 FILE https://example.com/photo.jpg\n2 FORM image/jpeg\n"
                + "0 TRLR\n";

        try (GedcomReader reader = new GedcomReader(stream(input), rec, config)) {
            reader.parse();
        }

        boolean hasMinCardWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Missing required child structure")
                        && w.contains("FILE"));
        assertFalse(hasMinCardWarning,
                "Should NOT warn about FILE when present, warnings: " + rec.warnings);
    }

    // ─── T015d: {1:M} child absent → warning ───────────────

    @Test
    void requiredMultipleChildAbsent_emitsWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .structureValidation(true)
                .build();

        // OBJE without FILE child (FILE has {1:M} under record-OBJE)
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @O1@ OBJE\n"
                + "0 TRLR\n";

        try (GedcomReader reader = new GedcomReader(stream(input), rec, config)) {
            reader.parse();
        }

        boolean hasMinCardWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Missing required child structure")
                        && w.contains("FILE"));
        assertTrue(hasMinCardWarning,
                "Should warn about missing FILE under OBJE, warnings: " + rec.warnings);
    }

    // ─── T015e: Validation disabled → no warning ────────────

    @Test
    void validationDisabled_noMinCardWarning() {
        WarningRecorder rec = new WarningRecorder();
        GedcomReaderConfig config = GedcomReaderConfig.gedcom7(); // default: validation OFF

        // ASSO without ROLE — but validation is disabled
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n"
                + "0 @I1@ INDI\n1 ASSO @I2@\n"
                + "0 TRLR\n";

        try (GedcomReader reader = new GedcomReader(stream(input), rec, config)) {
            reader.parse();
        }

        boolean hasMinCardWarning = rec.warnings.stream()
                .anyMatch(w -> w.contains("Missing required child structure"));
        assertFalse(hasMinCardWarning,
                "Validation disabled: should produce no min-cardinality warnings, got: "
                        + rec.warnings);
    }

    // ─── StructureDefinitions.isRequired() unit tests ───────

    @Test
    void isRequired_required11_returnsTrue() {
        assertTrue(StructureDefinitions.isRequired("{1:1}"));
    }

    @Test
    void isRequired_required1M_returnsTrue() {
        assertTrue(StructureDefinitions.isRequired("{1:M}"));
    }

    @Test
    void isRequired_optional01_returnsFalse() {
        assertFalse(StructureDefinitions.isRequired("{0:1}"));
    }

    @Test
    void isRequired_optional0M_returnsFalse() {
        assertFalse(StructureDefinitions.isRequired("{0:M}"));
    }

    @Test
    void isRequired_null_returnsFalse() {
        assertFalse(StructureDefinitions.isRequired(null));
    }

    // ─── StructureDefinitions.getRequiredChildren() unit tests ─

    @Test
    void getRequiredChildren_asso_containsRole() {
        Map<String, String> required = StructureDefinitions.getRequiredChildren("ASSO");
        assertTrue(required.containsKey("ROLE"),
                "ASSO should have ROLE as required child");
        assertEquals("{1:1}", required.get("ROLE"));
    }

    @Test
    void getRequiredChildren_asso_doesNotContainOptional() {
        Map<String, String> required = StructureDefinitions.getRequiredChildren("ASSO");
        assertFalse(required.containsKey("NOTE"),
                "NOTE is optional under ASSO, should not be in required");
    }

    @Test
    void getRequiredChildren_recordObje_containsFile() {
        Map<String, String> required = StructureDefinitions.getRequiredChildren("record-OBJE");
        assertTrue(required.containsKey("FILE"),
                "record-OBJE should have FILE as required child");
        assertEquals("{1:M}", required.get("FILE"));
    }

    @Test
    void getRequiredChildren_unknownContext_returnsEmpty() {
        Map<String, String> required = StructureDefinitions.getRequiredChildren("NONEXISTENT");
        assertTrue(required.isEmpty(),
                "Unknown context should return empty map");
    }
}
