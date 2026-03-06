package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LdsOrdinanceTest {

    @FunctionalInterface
    interface WriterAction {
        void execute(GedcomWriter writer) throws Exception;
    }

    private String write(WriterAction action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("Test"));
            action.execute(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private String write555(WriterAction action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom555())) {
            writer.head(head -> head.source("Test"));
            action.execute(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    // --- LDS Baptism (BAPL) ---

    @Test
    void ldsBaptismWithDateAndPlace() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.ldsBaptism(body -> {
                    body.date("15 JAN 1900");
                    body.place("Salt Lake City, UT");
                });
            });
        });

        assertTrue(output.contains("1 BAPL\n"));
        assertTrue(output.contains("2 DATE 15 JAN 1900\n"));
        assertTrue(output.contains("2 PLAC Salt Lake City, UT\n"));
    }

    // --- LDS Confirmation (CONL) ---

    @Test
    void ldsConfirmation() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.ldsConfirmation(body -> {
                    body.date("20 FEB 1905");
                });
            });
        });

        assertTrue(output.contains("1 CONL\n"));
        assertTrue(output.contains("2 DATE 20 FEB 1905\n"));
    }

    // --- LDS Endowment (ENDL) ---

    @Test
    void ldsEndowment() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.ldsEndowment(body -> {
                    body.date("10 MAR 1910");
                });
            });
        });

        assertTrue(output.contains("1 ENDL\n"));
        assertTrue(output.contains("2 DATE 10 MAR 1910\n"));
    }

    // --- LDS Initiatory (INIL) ---

    @Test
    void ldsInitiatory() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.ldsInitiatory(body -> {
                    body.place("Provo, UT");
                });
            });
        });

        assertTrue(output.contains("1 INIL\n"));
        assertTrue(output.contains("2 PLAC Provo, UT\n"));
    }

    // --- LDS Sealing to Parents (SLGC) ---

    @Test
    void ldsSealingToParents() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.ldsSealingToParents(body -> {
                    body.date("5 APR 1920");
                    body.place("Logan, UT");
                });
            });
        });

        assertTrue(output.contains("1 SLGC\n"));
        assertTrue(output.contains("2 DATE 5 APR 1920\n"));
        assertTrue(output.contains("2 PLAC Logan, UT\n"));
    }

    // --- LDS Sealing to Spouse (SLGS) on FamilyContext ---

    @Test
    void ldsSealingToSpouse() throws Exception {
        String output = write(writer -> {
            writer.family(fam -> {
                fam.ldsSealingToSpouse(body -> {
                    body.date("12 JUN 1925");
                    body.place("Manti, UT");
                });
            });
        });

        assertTrue(output.contains("1 SLGS\n"));
        assertTrue(output.contains("2 DATE 12 JUN 1925\n"));
        assertTrue(output.contains("2 PLAC Manti, UT\n"));
    }

    // --- EventContext typed access ---

    @Test
    void eventContextProvidesTypedDatePlaceStructureAccess() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.ldsBaptism(body -> {
                    body.date("15 JAN 1900");
                    body.place("Salt Lake City, UT");
                    body.structure("TEMP", "Salt Lake");
                });
            });
        });

        assertTrue(output.contains("1 BAPL\n"));
        assertTrue(output.contains("2 DATE 15 JAN 1900\n"));
        assertTrue(output.contains("2 PLAC Salt Lake City, UT\n"));
        assertTrue(output.contains("2 TEMP Salt Lake\n"));
    }

    // --- GEDCOM 5.5.5 mode ---

    @Test
    void ldsBaptismIn555Mode() throws Exception {
        String output = write555(writer -> {
            writer.individual(indi -> {
                indi.ldsBaptism(body -> {
                    body.date("15 JAN 1900");
                    body.place("Salt Lake City, UT");
                });
            });
        });

        assertTrue(output.contains("2 VERS 5.5.5\n"));
        assertTrue(output.contains("1 BAPL\n"));
        assertTrue(output.contains("2 DATE 15 JAN 1900\n"));
        assertTrue(output.contains("2 PLAC Salt Lake City, UT\n"));
    }

    @Test
    void ldsSealingToSpouseIn555Mode() throws Exception {
        String output = write555(writer -> {
            writer.family(fam -> {
                fam.ldsSealingToSpouse(body -> {
                    body.date("12 JUN 1925");
                });
            });
        });

        assertTrue(output.contains("2 VERS 5.5.5\n"));
        assertTrue(output.contains("1 SLGS\n"));
        assertTrue(output.contains("2 DATE 12 JUN 1925\n"));
    }
}
