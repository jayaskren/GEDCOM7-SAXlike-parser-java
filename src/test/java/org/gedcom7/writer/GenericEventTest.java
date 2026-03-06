package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GenericEventTest {

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

    // --- Individual generic event ---

    @Test
    void individualGenericEventImmi() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.event("IMMI", body -> {
                    body.date("15 JAN 1905");
                    body.place("Ellis Island, NY");
                });
            });
        });

        assertTrue(output.contains("1 IMMI\n"));
        assertTrue(output.contains("2 DATE 15 JAN 1905\n"));
        assertTrue(output.contains("2 PLAC Ellis Island, NY\n"));
    }

    @Test
    void individualGenericEventCens() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.event("CENS", body -> {
                    body.date("1 APR 1910");
                });
            });
        });

        assertTrue(output.contains("1 CENS\n"));
        assertTrue(output.contains("2 DATE 1 APR 1910\n"));
    }

    // --- Family generic event ---

    @Test
    void familyGenericEventEnga() throws Exception {
        String output = write(writer -> {
            writer.family(fam -> {
                fam.event("ENGA", body -> {
                    body.date("14 FEB 1920");
                    body.place("Boston, MA");
                });
            });
        });

        assertTrue(output.contains("1 ENGA\n"));
        assertTrue(output.contains("2 DATE 14 FEB 1920\n"));
        assertTrue(output.contains("2 PLAC Boston, MA\n"));
    }

    // --- Existing typed methods still work ---

    @Test
    void existingBirthStillWorks() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> {
                indi.birth(body -> {
                    body.date("25 DEC 1950");
                    body.place("New York, NY");
                });
            });
        });

        assertTrue(output.contains("1 BIRT\n"));
        assertTrue(output.contains("2 DATE 25 DEC 1950\n"));
        assertTrue(output.contains("2 PLAC New York, NY\n"));
    }

    @Test
    void existingMarriageStillWorks() throws Exception {
        String output = write(writer -> {
            writer.family(fam -> {
                fam.marriage(body -> {
                    body.date("15 JUN 1975");
                    body.place("Chicago, IL");
                });
            });
        });

        assertTrue(output.contains("1 MARR\n"));
        assertTrue(output.contains("2 DATE 15 JUN 1975\n"));
        assertTrue(output.contains("2 PLAC Chicago, IL\n"));
    }
}
