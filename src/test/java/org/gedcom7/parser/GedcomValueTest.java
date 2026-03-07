package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that GedcomValue constants resolve to the expected GEDCOM 7 string values (SC-003).
 */
class GedcomValueTest {

    // ─── Sex constants ───────────────────────────────────────

    @Test
    void sexConstants() {
        assertEquals("M", GedcomValue.Sex.MALE);
        assertEquals("F", GedcomValue.Sex.FEMALE);
        assertEquals("X", GedcomValue.Sex.INTERSEX);
        assertEquals("U", GedcomValue.Sex.UNKNOWN);
    }

    @Test
    void sexConstantsUsableInSwitch() {
        String value = "M";
        String matched = null;
        switch (value) {
            case GedcomValue.Sex.MALE: matched = "male"; break;
            case GedcomValue.Sex.FEMALE: matched = "female"; break;
            case GedcomValue.Sex.INTERSEX: matched = "intersex"; break;
            case GedcomValue.Sex.UNKNOWN: matched = "unknown"; break;
        }
        assertEquals("male", matched);
    }

    // ─── NameType constants ──────────────────────────────────

    @Test
    void nameTypeConstants() {
        assertEquals("BIRTH", GedcomValue.NameType.BIRTH);
        assertEquals("IMMIGRANT", GedcomValue.NameType.IMMIGRANT);
        assertEquals("MAIDEN", GedcomValue.NameType.MAIDEN);
        assertEquals("MARRIED", GedcomValue.NameType.MARRIED);
        assertEquals("PROFESSIONAL", GedcomValue.NameType.PROFESSIONAL);
        assertEquals("OTHER", GedcomValue.NameType.OTHER);
    }

    // ─── Pedi constants ──────────────────────────────────────

    @Test
    void pediConstants() {
        assertEquals("BIRTH", GedcomValue.Pedi.BIRTH);
        assertEquals("ADOPTED", GedcomValue.Pedi.ADOPTED);
        assertEquals("FOSTER", GedcomValue.Pedi.FOSTER);
        assertEquals("SEALING", GedcomValue.Pedi.SEALING);
        assertEquals("OTHER", GedcomValue.Pedi.OTHER);
    }

    // ─── Resn constants ──────────────────────────────────────

    @Test
    void resnConstants() {
        assertEquals("CONFIDENTIAL", GedcomValue.Resn.CONFIDENTIAL);
        assertEquals("LOCKED", GedcomValue.Resn.LOCKED);
        assertEquals("PRIVACY", GedcomValue.Resn.PRIVACY);
    }

    // ─── Role constants ──────────────────────────────────────

    @Test
    void roleConstants() {
        assertEquals("CHIL", GedcomValue.Role.CHILD);
        assertEquals("CLERGY", GedcomValue.Role.CLERGY);
        assertEquals("FATH", GedcomValue.Role.FATHER);
        assertEquals("FRIEND", GedcomValue.Role.FRIEND);
        assertEquals("GODP", GedcomValue.Role.GODPARENT);
        assertEquals("HUSB", GedcomValue.Role.HUSBAND);
        assertEquals("MOTH", GedcomValue.Role.MOTHER);
        assertEquals("MULT", GedcomValue.Role.MULTIPLE);
        assertEquals("NGHBR", GedcomValue.Role.NEIGHBOR);
        assertEquals("OFFICIATOR", GedcomValue.Role.OFFICIATOR);
        assertEquals("PARENT", GedcomValue.Role.PARENT);
        assertEquals("SPOU", GedcomValue.Role.SPOUSE);
        assertEquals("WIFE", GedcomValue.Role.WIFE);
        assertEquals("WITN", GedcomValue.Role.WITNESS);
        assertEquals("OTHER", GedcomValue.Role.OTHER);
    }

    // ─── Medi constants ──────────────────────────────────────

    @Test
    void mediConstants() {
        assertEquals("AUDIO", GedcomValue.Medi.AUDIO);
        assertEquals("BOOK", GedcomValue.Medi.BOOK);
        assertEquals("CARD", GedcomValue.Medi.CARD);
        assertEquals("ELECTRONIC", GedcomValue.Medi.ELECTRONIC);
        assertEquals("FICHE", GedcomValue.Medi.FICHE);
        assertEquals("FILM", GedcomValue.Medi.FILM);
        assertEquals("MAGAZINE", GedcomValue.Medi.MAGAZINE);
        assertEquals("MANUSCRIPT", GedcomValue.Medi.MANUSCRIPT);
        assertEquals("MAP", GedcomValue.Medi.MAP);
        assertEquals("NEWSPAPER", GedcomValue.Medi.NEWSPAPER);
        assertEquals("PHOTO", GedcomValue.Medi.PHOTO);
        assertEquals("TOMBSTONE", GedcomValue.Medi.TOMBSTONE);
        assertEquals("VIDEO", GedcomValue.Medi.VIDEO);
        assertEquals("OTHER", GedcomValue.Medi.OTHER);
    }

    // ─── Adop constants ──────────────────────────────────────

    @Test
    void adopConstants() {
        assertEquals("HUSB", GedcomValue.Adop.HUSBAND);
        assertEquals("WIFE", GedcomValue.Adop.WIFE);
        assertEquals("BOTH", GedcomValue.Adop.BOTH);
    }
}
