package org.gedcom7.writer.context;

import org.gedcom7.writer.GedcomWriter;
import org.gedcom7.writer.GedcomWriteException;
import org.gedcom7.writer.Xref;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ContextIntegrationTest {

    private String write(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    // --- SourceContext ---

    @Test
    void sourceWithTitleAndAuthor() throws Exception {
        String output = write(writer -> {
            try {
                writer.source(src -> {
                    src.title("1880 U.S. Census");
                    src.author("U.S. Bureau of the Census");
                    src.publicationFacts("Washington, D.C.");
                    src.abbreviation("1880 Census");
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("0 @S1@ SOUR\n"));
        assertTrue(output.contains("1 TITL 1880 U.S. Census\n"));
        assertTrue(output.contains("1 AUTH U.S. Bureau of the Census\n"));
        assertTrue(output.contains("1 PUBL Washington, D.C.\n"));
        assertTrue(output.contains("1 ABBR 1880 Census\n"));
    }

    @Test
    void sourceWithRepositoryCitation() throws Exception {
        String output = write(writer -> {
            try {
                Xref repo = writer.repository(r -> r.name("National Archives"));
                writer.source(src -> {
                    src.title("Records");
                    src.repositoryCitation(repo);
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("0 @R1@ REPO\n"));
        assertTrue(output.contains("1 REPO @R1@\n"));
    }

    // --- RepositoryContext ---

    @Test
    void repositoryWithNameAndAddress() throws Exception {
        String output = write(writer -> {
            try {
                writer.repository(repo -> {
                    repo.name("Library of Congress");
                    repo.address(addr -> {
                        addr.line1("101 Independence Ave");
                        addr.city("Washington");
                        addr.state("DC");
                        addr.postalCode("20540");
                        addr.country("USA");
                    });
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("1 NAME Library of Congress\n"));
        assertTrue(output.contains("1 ADDR\n"));
        assertTrue(output.contains("2 ADR1 101 Independence Ave\n"));
        assertTrue(output.contains("2 CITY Washington\n"));
        assertTrue(output.contains("2 STAE DC\n"));
        assertTrue(output.contains("2 POST 20540\n"));
        assertTrue(output.contains("2 CTRY USA\n"));
    }

    // --- MultimediaContext ---

    @Test
    void multimediaWithFile() throws Exception {
        String output = write(writer -> {
            try {
                writer.multimedia(obje -> {
                    obje.file("photo.jpg");
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("0 @O1@ OBJE\n"));
        assertTrue(output.contains("1 FILE photo.jpg\n"));
    }

    @Test
    void multimediaWithFileAndBody() throws Exception {
        String output = write(writer -> {
            try {
                writer.multimedia(obje -> {
                    obje.file("photo.jpg", ctx -> ctx.structure("FORM", "JPEG"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("1 FILE photo.jpg\n"));
        assertTrue(output.contains("2 FORM JPEG\n"));
    }

    // --- SubmitterContext ---

    @Test
    void submitterWithName() throws Exception {
        String output = write(writer -> {
            try {
                writer.submitter(subm -> subm.name("Jay Askren"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("0 @U1@ SUBM\n"));
        assertTrue(output.contains("1 NAME Jay Askren\n"));
    }

    // --- NoteContext ---

    @Test
    void sharedNote() throws Exception {
        String output = write(writer -> {
            try {
                writer.sharedNote(note -> {
                    note.structure("MIME", "text/plain");
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("0 @N1@ SNOTE\n"));
        assertTrue(output.contains("1 MIME text/plain\n"));
    }

    // --- AddressContext ---

    @Test
    void addressAllFields() throws Exception {
        String output = write(writer -> {
            try {
                writer.individual(indi -> {
                    indi.residence(resi -> {
                        resi.address(addr -> {
                            addr.line1("123 Main St");
                            addr.line2("Apt 4B");
                            addr.line3("Building A");
                            addr.city("Springfield");
                            addr.state("IL");
                            addr.postalCode("62701");
                            addr.country("USA");
                        });
                    });
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("3 ADR1 123 Main St\n"));
        assertTrue(output.contains("3 ADR2 Apt 4B\n"));
        assertTrue(output.contains("3 ADR3 Building A\n"));
        assertTrue(output.contains("3 CITY Springfield\n"));
        assertTrue(output.contains("3 STAE IL\n"));
        assertTrue(output.contains("3 POST 62701\n"));
        assertTrue(output.contains("3 CTRY USA\n"));
    }
}
