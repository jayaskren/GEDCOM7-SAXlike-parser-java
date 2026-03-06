package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GedcomWriterTest {

    private String write(WriterAction action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            action.execute(writer);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @FunctionalInterface
    interface WriterAction {
        void execute(GedcomWriter writer) throws Exception;
    }

    @Test
    void basicIndiRecord() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.individual(indi -> {
                indi.personalName("John /Doe/");
            });
            writer.trailer();
        });

        assertTrue(output.contains("0 HEAD\n"));
        assertTrue(output.contains("1 SOUR MyApp\n"));
        assertTrue(output.contains("0 @I1@ INDI\n"));
        assertTrue(output.contains("1 NAME John /Doe/\n"));
        assertTrue(output.contains("0 TRLR\n"));
    }

    @Test
    void basicFamRecord() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref john = writer.individual(indi -> indi.personalName("John /Doe/"));
            Xref jane = writer.individual(indi -> indi.personalName("Jane /Smith/"));
            writer.family(fam -> {
                fam.husband(john);
                fam.wife(jane);
            });
            writer.trailer();
        });

        assertTrue(output.contains("0 @F1@ FAM\n"));
        assertTrue(output.contains("1 HUSB @I1@\n"));
        assertTrue(output.contains("1 WIFE @I2@\n"));
    }

    @Test
    void headWithGedc() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("TestApp"));
            writer.trailer();
        });

        assertTrue(output.contains("0 HEAD\n"));
        assertTrue(output.contains("1 GEDC\n"));
        assertTrue(output.contains("2 VERS 7.0\n"));
    }

    @Test
    void autoHeadWhenNotCalled() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.personalName("John /Doe/"));
            writer.trailer();
        });

        assertTrue(output.startsWith("0 HEAD\n"));
        assertTrue(output.contains("1 GEDC\n"));
        assertTrue(output.contains("2 VERS 7.0\n"));
    }

    @Test
    void autoTrlrOnClose() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.individual(indi -> indi.personalName("John /Doe/"));
            // No explicit trailer() call
        });

        assertTrue(output.endsWith("0 TRLR\n"));
    }

    @Test
    void illegalStateAfterClose() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomWriter writer = new GedcomWriter(out);
        writer.close();

        assertThrows(IllegalStateException.class, () ->
                writer.individual(indi -> indi.personalName("John /Doe/")));
    }

    @Test
    void individualReturnsXref() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("MyApp"));
            Xref xref = writer.individual(indi -> indi.personalName("John /Doe/"));
            assertNotNull(xref);
            assertEquals("I1", xref.getId());
            writer.trailer();
        }
    }

    @Test
    void familyReturnsXref() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("MyApp"));
            Xref xref = writer.family(fam -> {});
            assertNotNull(xref);
            assertEquals("F1", xref.getId());
            writer.trailer();
        }
    }

    @Test
    void birthEventWithDateAndPlace() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.individual(indi -> {
                indi.personalName("John /Doe/");
                indi.birth(birt -> {
                    birt.date("15 MAR 1955");
                    birt.place("Springfield, IL");
                });
            });
            writer.trailer();
        });

        assertTrue(output.contains("1 BIRT\n"));
        assertTrue(output.contains("2 DATE 15 MAR 1955\n"));
        assertTrue(output.contains("2 PLAC Springfield, IL\n"));
    }

    @Test
    void sourceCitationWithPage() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref src = writer.source(s -> s.title("1880 Census"));
            writer.individual(indi -> {
                indi.birth(birt -> {
                    birt.sourceCitation(src, cite -> {
                        cite.page("Roll 108, Page 42");
                    });
                });
            });
            writer.trailer();
        });

        assertTrue(output.contains("0 @S1@ SOUR\n"));
        assertTrue(output.contains("1 TITL 1880 Census\n"));
        assertTrue(output.contains("2 SOUR @S1@\n"));
        assertTrue(output.contains("3 PAGE Roll 108, Page 42\n"));
    }

    // --- Escape hatch: record() methods ---

    @Test
    void recordEscapeHatchWithBody() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref xref = writer.record("_DNATEST", ctx -> {
                ctx.structure("_METHOD", "Autosomal");
                ctx.structure("DATE", "1 JAN 2020");
            });
            assertNotNull(xref);
            writer.trailer();
        });

        assertTrue(output.contains("0 @X1@ _DNATEST\n"));
        assertTrue(output.contains("1 _METHOD Autosomal\n"));
        assertTrue(output.contains("1 DATE 1 JAN 2020\n"));
    }

    @Test
    void recordEscapeHatchWithDevId() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref xref = writer.record("DNA42", "_DNATEST", ctx -> {
                ctx.structure("_METHOD", "Y-DNA");
            });
            assertEquals("DNA42", xref.getId());
            writer.trailer();
        });

        assertTrue(output.contains("0 @DNA42@ _DNATEST\n"));
        assertTrue(output.contains("1 _METHOD Y-DNA\n"));
    }

    @Test
    void recordEscapeHatchWithValue() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.record("_EXPORT", "2020-01-01");
            writer.trailer();
        });

        assertTrue(output.contains("0 _EXPORT 2020-01-01\n"));
    }

    @Test
    void schmaTagDeclaration() throws Exception {
        String output = write(writer -> {
            writer.head(head -> {
                head.source("MyApp");
                head.schema(schma -> {
                    schma.tag("_DNATEST", "https://example.com/gedcom/dnatest");
                });
            });
            writer.trailer();
        });

        assertTrue(output.contains("1 SCHMA\n"));
        assertTrue(output.contains("2 TAG _DNATEST https://example.com/gedcom/dnatest\n"));
    }

    // --- Cross-reference management (US5) ---

    @Test
    void developerProvidedId() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref john = writer.individual("42", indi -> indi.personalName("John /Doe/"));
            assertEquals("42", john.getId());
            writer.trailer();
        });

        assertTrue(output.contains("0 @42@ INDI\n"));
    }

    @Test
    void husbandWithStringId() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.individual("P1", indi -> indi.personalName("John /Doe/"));
            writer.family(fam -> fam.husband("P1"));
            writer.trailer();
        });

        assertTrue(output.contains("0 @P1@ INDI\n"));
        assertTrue(output.contains("1 HUSB @P1@\n"));
    }

    @Test
    void forwardReferences() throws Exception {
        // Write family before individuals (forward references)
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.family("F1", fam -> {
                fam.husband("P1");
                fam.wife("P2");
            });
            writer.individual("P1", indi -> indi.personalName("John /Doe/"));
            writer.individual("P2", indi -> indi.personalName("Jane /Smith/"));
            writer.trailer();
        });

        assertTrue(output.contains("0 @F1@ FAM\n"));
        assertTrue(output.contains("1 HUSB @P1@\n"));
        assertTrue(output.contains("1 WIFE @P2@\n"));
        assertTrue(output.contains("0 @P1@ INDI\n"));
        assertTrue(output.contains("0 @P2@ INDI\n"));
    }

    @Test
    void databaseExportPattern() throws Exception {
        // Simulate database export with sequential IDs
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.individual("100", indi -> indi.personalName("Alice /A/"));
            writer.individual("200", indi -> indi.personalName("Bob /B/"));
            writer.family("300", fam -> {
                fam.husband("100");
                fam.wife("200");
                fam.child("400");
            });
            writer.individual("400", indi -> indi.personalName("Charlie /A/"));
            writer.trailer();
        });

        assertTrue(output.contains("0 @100@ INDI\n"));
        assertTrue(output.contains("0 @200@ INDI\n"));
        assertTrue(output.contains("0 @300@ FAM\n"));
        assertTrue(output.contains("1 CHIL @400@\n"));
        assertTrue(output.contains("0 @400@ INDI\n"));
    }

    @Test
    void allRecordTypesReturnXref() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("MyApp"));
            assertNotNull(writer.source(s -> s.title("A")));
            assertNotNull(writer.repository(r -> r.name("Repo")));
            assertNotNull(writer.multimedia(m -> m.file("pic.jpg")));
            assertNotNull(writer.submitter(s -> s.name("Jay")));
            assertNotNull(writer.sharedNote(n -> {}));
            writer.trailer();
        }
    }

    @Test
    void nullValueOmitted() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.individual(indi -> {
                indi.personalName("John /Doe/");
                indi.sex((String) null);  // should be omitted
            });
            writer.trailer();
        });

        assertFalse(output.contains("SEX"));
    }
}
