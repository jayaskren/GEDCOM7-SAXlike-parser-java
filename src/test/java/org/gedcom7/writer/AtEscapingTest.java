package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AtEscapingTest {

    private String writeGedcom7(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom7())) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private String writeGedcom555(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom555())) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void leadingAtDoubledInGedcom7() throws Exception {
        String output = writeGedcom7(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", "@handle"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE @@handle\n"));
    }

    @Test
    void noLeadingAtUnchangedInGedcom7() throws Exception {
        String output = writeGedcom7(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", "no at sign"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE no at sign\n"));
    }

    @Test
    void middleAtUnchangedInGedcom7() throws Exception {
        String output = writeGedcom7(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", "email@example.com"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE email@example.com\n"));
    }

    @Test
    void allAtDoubledInGedcom555() throws Exception {
        String output = writeGedcom555(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", "@a@b"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE @@a@@b\n"));
    }
}
