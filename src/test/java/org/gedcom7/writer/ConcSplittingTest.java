package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ConcSplittingTest {

    private String write555(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom555().toBuilder()
                .maxLineLength(40)  // short for testing
                .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, config)) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void shortValueNoSplit() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", "Short"));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE Short\n"));
        assertFalse(output.contains("CONC"));
    }

    @Test
    void longValueSplitsToConc() throws Exception {
        // "1 NOTE " = 7 chars, so with maxLineLength=40 we have 33 chars for value
        String longValue = "ABCDEFGHIJ".repeat(5); // 50 chars, will exceed 40
        String output = write555(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", longValue));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(output.contains("1 NOTE ABCDEFGHIJ"));
        assertTrue(output.contains("2 CONC "));
    }

    @Test
    void multipleConcLines() throws Exception {
        // With maxLineLength=40, a very long value should produce multiple CONC lines
        String longValue = "A".repeat(120);
        String output = write555(writer -> {
            try {
                writer.individual(indi -> indi.structure("NOTE", longValue));
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });

        // Count CONC occurrences
        int concCount = 0;
        int idx = 0;
        while ((idx = output.indexOf("CONC", idx)) >= 0) {
            concCount++;
            idx += 4;
        }
        assertTrue(concCount >= 2, "Expected at least 2 CONC lines for 120 chars at maxLength 40");
    }

    @Test
    void concNotUsedInGedcom7() throws Exception {
        // GEDCOM 7 mode should NOT produce CONC even for long values
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("Test"));
            writer.individual(indi -> indi.structure("NOTE", "A".repeat(500)));
            writer.trailer();
        }
        String output = out.toString(StandardCharsets.UTF_8.name());

        assertFalse(output.contains("CONC"));
    }
}
