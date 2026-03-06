package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Gedcom555WriterTest {

    private String write555(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom555())) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void headerVers555() throws Exception {
        String output = write555(writer -> {});
        assertTrue(output.contains("2 VERS 5.5.5\n"));
    }

    @Test
    void allAtDoubled() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", "@a@b"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("1 NOTE @@a@@b\n"));
    }

    @Test
    void famsNoWarningIn555() throws Exception {
        // FAMS should not warn in 5.5.5 mode
        StringBuilder warnings = new StringBuilder();
        GedcomWriterConfig config = GedcomWriterConfig.gedcom555().toBuilder()
                .warningHandler(w -> warnings.append(w.getMessage()))
                .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, config)) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            writer.individual(indi -> indi.familyAsSpouse(fam));
            writer.trailer();
        }
        assertEquals("", warnings.toString());
    }

    @Test
    void famcNoWarningIn555() throws Exception {
        StringBuilder warnings = new StringBuilder();
        GedcomWriterConfig config = GedcomWriterConfig.gedcom555().toBuilder()
                .warningHandler(w -> warnings.append(w.getMessage()))
                .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, config)) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            writer.individual(indi -> indi.familyAsChild(fam));
            writer.trailer();
        }
        assertEquals("", warnings.toString());
    }

    @Test
    void nonGregorianDatePrefix555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> {
                        birt.date("@#DJULIAN@ 25 DEC 1752");
                    });
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        // The calendar escape prefix should pass through as-is (not @@-doubled)
        assertTrue(output.contains("2 DATE @#DJULIAN@ 25 DEC 1752\n"),
                "Calendar escape should be preserved, got: " + output);
    }
}
