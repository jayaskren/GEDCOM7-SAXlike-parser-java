package org.gedcom7.parser.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Completeness tests for {@link StructureDefinitions}.
 *
 * <p>Verifies that resolveStructure() returns non-null for all standard
 * GEDCOM 7 event types, LDS ordinance types, family events, and their
 * expected child structures.
 */
class StructureDefinitionsCompletenessTest {

    // ─── Individual event types under record-INDI ────────────────

    @ParameterizedTest(name = "record-INDI -> {0} resolves")
    @ValueSource(strings = {
            "ADOP", "BAPM", "BARM", "BASM", "BIRT", "BLES", "BURI",
            "CENS", "CHR", "CHRA", "CONF", "CREM", "DEAT", "EMIG",
            "FCOM", "GRAD", "IMMI", "NATU", "ORDN", "PROB", "RETI", "WILL"
    })
    void individualEventTypes_resolveUnderIndi(String tag) {
        String result = StructureDefinitions.resolveStructure("record-INDI", tag);
        assertNotNull(result,
                "Expected StructureDefinitions.resolveStructure(\"record-INDI\", \""
                        + tag + "\") to return non-null");
    }

    // ─── LDS ordinance types under record-INDI ──────────────────

    @ParameterizedTest(name = "record-INDI -> {0} (LDS) resolves")
    @ValueSource(strings = {"BAPL", "CONL", "ENDL", "INIL", "SLGC"})
    void ldsOrdinanceTypes_resolveUnderIndi(String tag) {
        String result = StructureDefinitions.resolveStructure("record-INDI", tag);
        assertNotNull(result,
                "Expected LDS ordinance tag " + tag
                        + " to resolve under record-INDI");
    }

    // ─── Family event types under record-FAM ────────────────────

    @ParameterizedTest(name = "record-FAM -> {0} resolves")
    @ValueSource(strings = {
            "ANUL", "CENS", "DIV", "DIVF", "ENGA",
            "MARB", "MARC", "MARL", "MARR", "MARS"
    })
    void familyEventTypes_resolveUnderFam(String tag) {
        String result = StructureDefinitions.resolveStructure("record-FAM", tag);
        assertNotNull(result,
                "Expected StructureDefinitions.resolveStructure(\"record-FAM\", \""
                        + tag + "\") to return non-null");
    }

    // ─── SLGS under record-FAM ──────────────────────────────────

    @Test
    void slgs_resolvesUnderFam() {
        String result = StructureDefinitions.resolveStructure("record-FAM", "SLGS");
        assertNotNull(result, "SLGS should resolve under record-FAM");
        assertEquals("SLGS", result);
    }

    // ─── Child structures (DATE, PLAC, SOUR, NOTE) under events ─

    @ParameterizedTest(name = "BIRT -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderBirt(String tag) {
        String result = StructureDefinitions.resolveStructure("BIRT", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under BIRT");
    }

    @ParameterizedTest(name = "DEAT -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderDeat(String tag) {
        String result = StructureDefinitions.resolveStructure("DEAT", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under DEAT");
    }

    @ParameterizedTest(name = "MARR -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderMarr(String tag) {
        String result = StructureDefinitions.resolveStructure("MARR", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under MARR");
    }

    // ─── Child structures under LDS ordinance contexts ──────────

    @ParameterizedTest(name = "BAPL -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderBapl(String tag) {
        String result = StructureDefinitions.resolveStructure("BAPL", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under BAPL");
    }

    @ParameterizedTest(name = "CONL -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderConl(String tag) {
        String result = StructureDefinitions.resolveStructure("CONL", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under CONL");
    }

    @ParameterizedTest(name = "ENDL -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderEndl(String tag) {
        String result = StructureDefinitions.resolveStructure("ENDL", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under ENDL");
    }

    @ParameterizedTest(name = "INIL -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderInil(String tag) {
        String result = StructureDefinitions.resolveStructure("INIL", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under INIL");
    }

    @ParameterizedTest(name = "SLGC -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderSlgc(String tag) {
        String result = StructureDefinitions.resolveStructure("SLGC", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under SLGC");
    }

    @ParameterizedTest(name = "SLGS -> {0} resolves")
    @ValueSource(strings = {"DATE", "PLAC", "SOUR", "NOTE"})
    void childStructures_resolveUnderSlgs(String tag) {
        String result = StructureDefinitions.resolveStructure("SLGS", tag);
        assertNotNull(result,
                "Expected child tag " + tag + " to resolve under SLGS");
    }

    // ─── LDS-specific children TEMP and STAT under LDS contexts ─

    @ParameterizedTest(name = "{0} -> TEMP resolves")
    @ValueSource(strings = {"BAPL", "CONL", "ENDL", "INIL", "SLGC", "SLGS"})
    void temp_resolvesUnderLdsOrdinances(String parentContext) {
        String result = StructureDefinitions.resolveStructure(parentContext, "TEMP");
        assertNotNull(result,
                "Expected TEMP to resolve under LDS ordinance " + parentContext);
        assertEquals("TEMP", result);
    }

    @ParameterizedTest(name = "{0} -> STAT resolves")
    @ValueSource(strings = {"BAPL", "CONL", "ENDL", "INIL", "SLGC", "SLGS"})
    void stat_resolvesUnderLdsOrdinances(String parentContext) {
        String result = StructureDefinitions.resolveStructure(parentContext, "STAT");
        assertNotNull(result,
                "Expected STAT to resolve under LDS ordinance " + parentContext);
        assertEquals("ord-STAT", result);
    }

    // ─── Genuinely unknown tags return null ──────────────────────

    @Test
    void unknownTag_underIndi_returnsNull() {
        assertNull(StructureDefinitions.resolveStructure("record-INDI", "ZZZZZ"),
                "A genuinely unknown tag should return null");
    }

    @Test
    void unknownTag_underFam_returnsNull() {
        assertNull(StructureDefinitions.resolveStructure("record-FAM", "BOGUS"),
                "A genuinely unknown tag should return null");
    }

    @Test
    void unknownTag_underBirt_returnsNull() {
        assertNull(StructureDefinitions.resolveStructure("BIRT", "MARR"),
                "MARR is not a valid child of BIRT, should return null");
    }

    @Test
    void unknownContext_returnsNull() {
        assertNull(StructureDefinitions.resolveStructure("NONEXISTENT", "DATE"),
                "A nonexistent parent context should return null");
    }
}
