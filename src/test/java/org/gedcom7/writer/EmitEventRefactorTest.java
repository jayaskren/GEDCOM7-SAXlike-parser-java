package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the emitEvent extraction from IndividualContext/FamilyContext
 * to CommonContext (FR-010).
 */
class EmitEventRefactorTest {

    @Test
    void birth_producesCorrectOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.individual(indi -> {
                indi.birth(body -> {
                    body.date("15 MAR 1955");
                    body.place("Springfield, IL");
                });
            });
        }
        String result = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("1 BIRT"));
        assertTrue(result.contains("2 DATE 15 MAR 1955"));
        assertTrue(result.contains("2 PLAC Springfield, IL"));
    }

    @Test
    void death_producesCorrectOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.individual(indi -> {
                indi.death(body -> {
                    body.date("1 JAN 2020");
                });
            });
        }
        String result = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("1 DEAT"));
        assertTrue(result.contains("2 DATE 1 JAN 2020"));
    }

    @Test
    void marriage_producesCorrectOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.family(fam -> {
                fam.marriage(body -> {
                    body.date("1 JUN 1980");
                    body.place("Chicago, IL");
                });
            });
        }
        String result = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("1 MARR"));
        assertTrue(result.contains("2 DATE 1 JUN 1980"));
        assertTrue(result.contains("2 PLAC Chicago, IL"));
    }

    @Test
    void divorce_producesCorrectOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.family(fam -> {
                fam.divorce(body -> {
                    body.date("15 DEC 1995");
                });
            });
        }
        String result = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("1 DIV"));
        assertTrue(result.contains("2 DATE 15 DEC 1995"));
    }

    @Test
    void christening_burial_residence_work() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.individual(indi -> {
                indi.christening(body -> body.date("25 DEC 1955"));
                indi.burial(body -> body.place("Oak Cemetery"));
                indi.residence(body -> body.place("Springfield, IL"));
            });
        }
        String result = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("1 CHR"));
        assertTrue(result.contains("2 DATE 25 DEC 1955"));
        assertTrue(result.contains("1 BURI"));
        assertTrue(result.contains("2 PLAC Oak Cemetery"));
        assertTrue(result.contains("1 RESI"));
        assertTrue(result.contains("2 PLAC Springfield, IL"));
    }

    @Test
    void annulment_producesCorrectOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.family(fam -> {
                fam.annulment(body -> {
                    body.date("1 MAR 1990");
                });
            });
        }
        String result = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("1 ANUL"));
        assertTrue(result.contains("2 DATE 1 MAR 1990"));
    }
}
