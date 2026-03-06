package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for sharedNoteWithText methods that emit SNOTE records
 * with text values at the record level.
 */
class SharedNoteTextTest {

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
    void sharedNoteWithTextEmitsSingleLine() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref note = writer.sharedNoteWithText("This is a note", n -> {});
            assertNotNull(note);
            assertEquals("N1", note.getId());
            writer.trailer();
        });

        assertTrue(output.contains("0 @N1@ SNOTE This is a note\n"));
    }

    @Test
    void sharedNoteWithTextMultilineEmitsCont() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            writer.sharedNoteWithText("Line one\nLine two", n -> {});
            writer.trailer();
        });

        assertTrue(output.contains("0 @N1@ SNOTE Line one\n"));
        assertTrue(output.contains("1 CONT Line two\n"));
    }

    @Test
    void sharedNoteWithTextCustomId() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref note = writer.sharedNoteWithText("id1", "Note text", n -> {});
            assertEquals("id1", note.getId());
            writer.trailer();
        });

        assertTrue(output.contains("0 @id1@ SNOTE Note text\n"));
    }

    @Test
    void existingSharedNoteUnchanged() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref note = writer.sharedNote(n -> {});
            assertNotNull(note);
            assertEquals("N1", note.getId());
            writer.trailer();
        });

        assertTrue(output.contains("0 @N1@ SNOTE\n"));
        // Should NOT have any text value on the SNOTE line
        assertFalse(output.contains("0 @N1@ SNOTE "));
    }

    @Test
    void existingSharedNoteWithCustomIdUnchanged() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("MyApp"));
            Xref note = writer.sharedNote("id1", n -> {});
            assertEquals("id1", note.getId());
            writer.trailer();
        });

        assertTrue(output.contains("0 @id1@ SNOTE\n"));
        assertFalse(output.contains("0 @id1@ SNOTE "));
    }
}
