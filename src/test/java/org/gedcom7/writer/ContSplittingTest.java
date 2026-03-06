package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ContSplittingTest {

    private String write(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void singleLineNoSplit() throws Exception {
        String output = write(writer -> {
            try {
                writer.individual(indi -> indi.note("Single line note"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE Single line note\n"));
        assertFalse(output.contains("CONT"));
    }

    @Test
    void multiLineProducesCont() throws Exception {
        String output = write(writer -> {
            try {
                writer.individual(indi -> indi.note("Line one\nLine two\nLine three"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE Line one\n"));
        assertTrue(output.contains("2 CONT Line two\n"));
        assertTrue(output.contains("2 CONT Line three\n"));
    }

    @Test
    void crlfNormalized() throws Exception {
        String output = write(writer -> {
            try {
                writer.individual(indi -> indi.note("Line one\r\nLine two"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE Line one\n"));
        assertTrue(output.contains("2 CONT Line two\n"));
    }

    @Test
    void crNormalized() throws Exception {
        String output = write(writer -> {
            try {
                writer.individual(indi -> indi.note("Line one\rLine two"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE Line one\n"));
        assertTrue(output.contains("2 CONT Line two\n"));
    }

    @Test
    void emptyLinesPreserved() throws Exception {
        String output = write(writer -> {
            try {
                writer.individual(indi -> indi.note("Line one\n\nLine three"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE Line one\n"));
        assertTrue(output.contains("2 CONT\n"));
        assertTrue(output.contains("2 CONT Line three\n"));
    }
}
