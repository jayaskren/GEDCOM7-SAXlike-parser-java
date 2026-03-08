package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that GedcomTag constants resolve to the expected GEDCOM 7 string values.
 */
class GedcomTagTest {

    // ─── Record-level constants ──────────────────────────────

    @Test
    void recordLevelConstants() {
        assertEquals("HEAD", GedcomTag.HEAD);
        assertEquals("TRLR", GedcomTag.TRLR);
        assertEquals("INDI", GedcomTag.INDI);
        assertEquals("FAM", GedcomTag.FAM);
        assertEquals("OBJE", GedcomTag.OBJE);
        assertEquals("SOUR", GedcomTag.SOUR);
        assertEquals("REPO", GedcomTag.REPO);
        assertEquals("NOTE", GedcomTag.NOTE);
        assertEquals("SNOTE", GedcomTag.SNOTE);
        assertEquals("SUBM", GedcomTag.SUBM);
    }

    @Test
    void recordLevelConstantsUsableInSwitch() {
        String tag = "INDI";
        String matched = null;
        switch (tag) {
            case GedcomTag.INDI: matched = "individual"; break;
            case GedcomTag.FAM: matched = "family"; break;
            case GedcomTag.SOUR: matched = "source"; break;
        }
        assertEquals("individual", matched);
    }

    // ─── Indi nested class constants ─────────────────────────

    @Test
    void indiSubstructureConstants() {
        assertEquals("NAME", GedcomTag.Indi.NAME);
        assertEquals("SEX", GedcomTag.Indi.SEX);
        assertEquals("BIRT", GedcomTag.Indi.BIRT);
        assertEquals("DEAT", GedcomTag.Indi.DEAT);
        assertEquals("ADOP", GedcomTag.Indi.ADOP);
        assertEquals("BAPM", GedcomTag.Indi.BAPM);
        assertEquals("BURI", GedcomTag.Indi.BURI);
        assertEquals("CHR", GedcomTag.Indi.CHR);
        assertEquals("EVEN", GedcomTag.Indi.EVEN);
        assertEquals("FAMC", GedcomTag.Indi.FAMC);
        assertEquals("FAMS", GedcomTag.Indi.FAMS);
        assertEquals("NOTE", GedcomTag.Indi.NOTE);
        assertEquals("OBJE", GedcomTag.Indi.OBJE);
        assertEquals("SOUR", GedcomTag.Indi.SOUR);
        assertEquals("RESN", GedcomTag.Indi.RESN);
        assertEquals("OCCU", GedcomTag.Indi.OCCU);
        assertEquals("RESI", GedcomTag.Indi.RESI);
        assertEquals("UID", GedcomTag.Indi.UID);
        assertEquals("CHAN", GedcomTag.Indi.CHAN);
        assertEquals("EXID", GedcomTag.Indi.EXID);
    }

    @Test
    void indiConstantsUsableInSwitch() {
        String tag = "NAME";
        String matched = null;
        switch (tag) {
            case GedcomTag.Indi.NAME: matched = "name"; break;
            case GedcomTag.Indi.SEX: matched = "sex"; break;
            case GedcomTag.Indi.BIRT: matched = "birth"; break;
        }
        assertEquals("name", matched);
    }

    // ─── Fam nested class constants ──────────────────────────

    @Test
    void famSubstructureConstants() {
        assertEquals("HUSB", GedcomTag.Fam.HUSB);
        assertEquals("WIFE", GedcomTag.Fam.WIFE);
        assertEquals("CHIL", GedcomTag.Fam.CHIL);
        assertEquals("MARR", GedcomTag.Fam.MARR);
        assertEquals("DIV", GedcomTag.Fam.DIV);
        assertEquals("ANUL", GedcomTag.Fam.ANUL);
        assertEquals("EVEN", GedcomTag.Fam.EVEN);
        assertEquals("NOTE", GedcomTag.Fam.NOTE);
        assertEquals("SOUR", GedcomTag.Fam.SOUR);
        assertEquals("UID", GedcomTag.Fam.UID);
    }

    // ─── Sour nested class constants ─────────────────────────

    @Test
    void sourSubstructureConstants() {
        assertEquals("TITL", GedcomTag.Sour.TITL);
        assertEquals("AUTH", GedcomTag.Sour.AUTH);
        assertEquals("PUBL", GedcomTag.Sour.PUBL);
        assertEquals("REPO", GedcomTag.Sour.REPO);
        assertEquals("NOTE", GedcomTag.Sour.NOTE);
        assertEquals("UID", GedcomTag.Sour.UID);
    }

    // ─── Repo nested class constants ─────────────────────────

    @Test
    void repoSubstructureConstants() {
        assertEquals("NAME", GedcomTag.Repo.NAME);
        assertEquals("ADDR", GedcomTag.Repo.ADDR);
        assertEquals("EMAIL", GedcomTag.Repo.EMAIL);
        assertEquals("PHON", GedcomTag.Repo.PHON);
        assertEquals("WWW", GedcomTag.Repo.WWW);
    }

    // ─── Obje nested class constants ─────────────────────────

    @Test
    void objeSubstructureConstants() {
        assertEquals("FILE", GedcomTag.Obje.FILE);
        assertEquals("NOTE", GedcomTag.Obje.NOTE);
        assertEquals("RESN", GedcomTag.Obje.RESN);
        assertEquals("SOUR", GedcomTag.Obje.SOUR);
    }

    // ─── Snote nested class constants ────────────────────────

    @Test
    void snoteSubstructureConstants() {
        assertEquals("LANG", GedcomTag.Snote.LANG);
        assertEquals("MIME", GedcomTag.Snote.MIME);
        assertEquals("SOUR", GedcomTag.Snote.SOUR);
        assertEquals("TRAN", GedcomTag.Snote.TRAN);
    }

    // ─── Subm nested class constants ─────────────────────────

    @Test
    void submSubstructureConstants() {
        assertEquals("NAME", GedcomTag.Subm.NAME);
        assertEquals("ADDR", GedcomTag.Subm.ADDR);
        assertEquals("LANG", GedcomTag.Subm.LANG);
        assertEquals("EMAIL", GedcomTag.Subm.EMAIL);
    }

    // ─── Head nested class constants ─────────────────────────

    @Test
    void headSubstructureConstants() {
        assertEquals("GEDC", GedcomTag.Head.GEDC);
        assertEquals("SOUR", GedcomTag.Head.SOUR);
        assertEquals("SCHMA", GedcomTag.Head.SCHMA);
        assertEquals("DATE", GedcomTag.Head.DATE);
        assertEquals("LANG", GedcomTag.Head.LANG);
    }

    // ─── Event sub-nested class constants ────────────────────

    @Test
    void indiBirtEventSubstructures() {
        assertEquals("DATE", GedcomTag.Indi.Birt.DATE);
        assertEquals("PLAC", GedcomTag.Indi.Birt.PLAC);
        assertEquals("AGE", GedcomTag.Indi.Birt.AGE);
        assertEquals("ADDR", GedcomTag.Indi.Birt.ADDR);
        assertEquals("CAUS", GedcomTag.Indi.Birt.CAUS);
        assertEquals("NOTE", GedcomTag.Indi.Birt.NOTE);
        assertEquals("SOUR", GedcomTag.Indi.Birt.SOUR);
        assertEquals("FAMC", GedcomTag.Indi.Birt.FAMC);
    }

    @Test
    void indiDeatEventSubstructures() {
        assertEquals("DATE", GedcomTag.Indi.Deat.DATE);
        assertEquals("PLAC", GedcomTag.Indi.Deat.PLAC);
        assertEquals("AGE", GedcomTag.Indi.Deat.AGE);
        assertEquals("CAUS", GedcomTag.Indi.Deat.CAUS);
    }

    @Test
    void indiBuriEventSubstructures() {
        assertEquals("DATE", GedcomTag.Indi.Buri.DATE);
        assertEquals("PLAC", GedcomTag.Indi.Buri.PLAC);
    }

    @Test
    void indiChrEventSubstructures() {
        assertEquals("DATE", GedcomTag.Indi.Chr.DATE);
        assertEquals("PLAC", GedcomTag.Indi.Chr.PLAC);
        assertEquals("FAMC", GedcomTag.Indi.Chr.FAMC);
    }

    @Test
    void indiBapmEventSubstructures() {
        assertEquals("DATE", GedcomTag.Indi.Bapm.DATE);
        assertEquals("PLAC", GedcomTag.Indi.Bapm.PLAC);
    }

    @Test
    void indiEvenSubstructures() {
        assertEquals("DATE", GedcomTag.Indi.Even.DATE);
        assertEquals("PLAC", GedcomTag.Indi.Even.PLAC);
        assertEquals("TYPE", GedcomTag.Indi.Even.TYPE);
        assertEquals("AGE", GedcomTag.Indi.Even.AGE);
    }

    @Test
    void famMarrEventSubstructures() {
        assertEquals("DATE", GedcomTag.Fam.Marr.DATE);
        assertEquals("PLAC", GedcomTag.Fam.Marr.PLAC);
        assertEquals("HUSB", GedcomTag.Fam.Marr.HUSB);
        assertEquals("WIFE", GedcomTag.Fam.Marr.WIFE);
    }

    @Test
    void famDivEventSubstructures() {
        assertEquals("DATE", GedcomTag.Fam.Div.DATE);
        assertEquals("PLAC", GedcomTag.Fam.Div.PLAC);
    }

    @Test
    void famAnulEventSubstructures() {
        assertEquals("DATE", GedcomTag.Fam.Anul.DATE);
        assertEquals("PLAC", GedcomTag.Fam.Anul.PLAC);
    }

    @Test
    void famEvenSubstructures() {
        assertEquals("DATE", GedcomTag.Fam.Even.DATE);
        assertEquals("TYPE", GedcomTag.Fam.Even.TYPE);
    }

    // ─── Cross-record tag disambiguation ─────────────────────

    @Test
    void sameTagInDifferentRecordContexts() {
        // NAME exists in both INDI and REPO — same string, different semantic context
        assertEquals(GedcomTag.Indi.NAME, GedcomTag.Repo.NAME);
        assertEquals("NAME", GedcomTag.Indi.NAME);
        assertEquals("NAME", GedcomTag.Repo.NAME);
    }

    @Test
    void sameTagInDifferentEventContexts() {
        // DATE exists in BIRT, DEAT, MARR — same string "DATE"
        assertEquals(GedcomTag.Indi.Birt.DATE, GedcomTag.Indi.Deat.DATE);
        assertEquals(GedcomTag.Indi.Birt.DATE, GedcomTag.Fam.Marr.DATE);
        assertEquals("DATE", GedcomTag.Indi.Birt.DATE);
    }

    // ─── Plac nested class constants ────────────────────────

    @Test
    void placSubstructureConstants() {
        assertEquals("MAP", GedcomTag.Plac.MAP);
        assertEquals("FORM", GedcomTag.Plac.FORM);
        assertEquals("LANG", GedcomTag.Plac.LANG);
        assertEquals("TRAN", GedcomTag.Plac.TRAN);
        assertEquals("EXID", GedcomTag.Plac.EXID);
        assertEquals("NOTE", GedcomTag.Plac.NOTE);
        assertEquals("SNOTE", GedcomTag.Plac.SNOTE);
        assertEquals("SOUR", GedcomTag.Plac.SOUR);
    }

    @Test
    void placConstantsUsableInSwitch() {
        String tag = "MAP";
        String matched = null;
        switch (tag) {
            case GedcomTag.Plac.MAP: matched = "map"; break;
            case GedcomTag.Plac.FORM: matched = "form"; break;
        }
        assertEquals("map", matched);
    }

    // ─── Map nested class constants ─────────────────────────

    @Test
    void mapSubstructureConstants() {
        assertEquals("LATI", GedcomTag.Map.LATI);
        assertEquals("LONG", GedcomTag.Map.LONG);
    }

    @Test
    void mapConstantsUsableInSwitch() {
        String tag = "LATI";
        String matched = null;
        switch (tag) {
            case GedcomTag.Map.LATI: matched = "latitude"; break;
            case GedcomTag.Map.LONG: matched = "longitude"; break;
        }
        assertEquals("latitude", matched);
    }

    // ─── Date nested class constants ────────────────────────

    @Test
    void dateSubstructureConstants() {
        assertEquals("TIME", GedcomTag.Date.TIME);
        assertEquals("PHRASE", GedcomTag.Date.PHRASE);
    }

    // ─── Addr nested class constants ────────────────────────

    @Test
    void addrSubstructureConstants() {
        assertEquals("ADR1", GedcomTag.Addr.ADR1);
        assertEquals("ADR2", GedcomTag.Addr.ADR2);
        assertEquals("ADR3", GedcomTag.Addr.ADR3);
        assertEquals("CITY", GedcomTag.Addr.CITY);
        assertEquals("STAE", GedcomTag.Addr.STAE);
        assertEquals("POST", GedcomTag.Addr.POST);
        assertEquals("CTRY", GedcomTag.Addr.CTRY);
    }

    // ─── File nested class constants ────────────────────────

    @Test
    void fileSubstructureConstants() {
        assertEquals("FORM", GedcomTag.File.FORM);
        assertEquals("TITL", GedcomTag.File.TITL);
        assertEquals("TRAN", GedcomTag.File.TRAN);
    }

    // ─── Form nested class constants ────────────────────────

    @Test
    void formSubstructureConstants() {
        assertEquals("MEDI", GedcomTag.Form.MEDI);
    }

    // ─── Gedc nested class constants ────────────────────────

    @Test
    void gedcSubstructureConstants() {
        assertEquals("VERS", GedcomTag.Gedc.VERS);
    }

    // ─── Name nested class constants ────────────────────────

    @Test
    void nameSubstructureConstants() {
        assertEquals("GIVN", GedcomTag.Name.GIVN);
        assertEquals("SURN", GedcomTag.Name.SURN);
        assertEquals("NPFX", GedcomTag.Name.NPFX);
        assertEquals("NSFX", GedcomTag.Name.NSFX);
        assertEquals("SPFX", GedcomTag.Name.SPFX);
        assertEquals("NICK", GedcomTag.Name.NICK);
        assertEquals("TYPE", GedcomTag.Name.TYPE);
        assertEquals("TRAN", GedcomTag.Name.TRAN);
        assertEquals("NOTE", GedcomTag.Name.NOTE);
        assertEquals("SNOTE", GedcomTag.Name.SNOTE);
        assertEquals("SOUR", GedcomTag.Name.SOUR);
    }

    // ─── Refn nested class constants ────────────────────────

    @Test
    void refnSubstructureConstants() {
        assertEquals("TYPE", GedcomTag.Refn.TYPE);
    }

    // ─── Exid nested class constants ────────────────────────

    @Test
    void exidSubstructureConstants() {
        assertEquals("TYPE", GedcomTag.Exid.TYPE);
    }

    // ─── Asso nested class constants ────────────────────────

    @Test
    void assoSubstructureConstants() {
        assertEquals("ROLE", GedcomTag.Asso.ROLE);
        assertEquals("PHRASE", GedcomTag.Asso.PHRASE);
        assertEquals("NOTE", GedcomTag.Asso.NOTE);
        assertEquals("SNOTE", GedcomTag.Asso.SNOTE);
        assertEquals("SOUR", GedcomTag.Asso.SOUR);
    }

    // ─── Famc nested class constants ────────────────────────

    @Test
    void famcSubstructureConstants() {
        assertEquals("PEDI", GedcomTag.Famc.PEDI);
        assertEquals("STAT", GedcomTag.Famc.STAT);
        assertEquals("NOTE", GedcomTag.Famc.NOTE);
        assertEquals("SNOTE", GedcomTag.Famc.SNOTE);
    }

    // ─── Chan nested class constants ────────────────────────

    @Test
    void chanSubstructureConstants() {
        assertEquals("DATE", GedcomTag.Chan.DATE);
        assertEquals("NOTE", GedcomTag.Chan.NOTE);
        assertEquals("SNOTE", GedcomTag.Chan.SNOTE);
    }

    // ─── Crea nested class constants ────────────────────────

    @Test
    void creaSubstructureConstants() {
        assertEquals("DATE", GedcomTag.Crea.DATE);
    }

    // ─── SourCitation nested class constants ────────────────

    @Test
    void sourCitationSubstructureConstants() {
        assertEquals("PAGE", GedcomTag.SourCitation.PAGE);
        assertEquals("DATA", GedcomTag.SourCitation.DATA);
        assertEquals("EVEN", GedcomTag.SourCitation.EVEN);
        assertEquals("QUAY", GedcomTag.SourCitation.QUAY);
        assertEquals("NOTE", GedcomTag.SourCitation.NOTE);
        assertEquals("SNOTE", GedcomTag.SourCitation.SNOTE);
        assertEquals("OBJE", GedcomTag.SourCitation.OBJE);
    }

    // ─── Schma nested class constants ───────────────────────

    @Test
    void schmaSubstructureConstants() {
        assertEquals("TAG", GedcomTag.Schma.TAG);
    }

    // ─── Cross-context common tag disambiguation ────────────

    @Test
    void formExistsInMultipleContexts() {
        assertEquals(GedcomTag.Plac.FORM, GedcomTag.File.FORM);
        assertEquals("FORM", GedcomTag.Plac.FORM);
    }

    @Test
    void typeExistsInMultipleContexts() {
        assertEquals(GedcomTag.Name.TYPE, GedcomTag.Refn.TYPE);
        assertEquals(GedcomTag.Refn.TYPE, GedcomTag.Exid.TYPE);
        assertEquals("TYPE", GedcomTag.Name.TYPE);
    }
}
