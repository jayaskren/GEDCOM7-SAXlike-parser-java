package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringReader;
import static org.junit.jupiter.api.Assertions.*;

class GedcomLineTokenizerGapTest {

    // Gap 1: Raw line capture
    @Test
    void rawLineCaptured() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("0 HEAD\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("0 HEAD", line.getRawLine());
        assertEquals("HEAD", line.getTag());
    }

    @Test
    void rawLineCapturedWithValue() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("1 NOTE Hello World\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("1 NOTE Hello World", line.getRawLine());
    }

    @Test
    void rawLineCapturedWithXref() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("0 @I1@ INDI\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("0 @I1@ INDI", line.getRawLine());
    }

    // Gap 2: Max line length
    @Test
    void maxLineLengthExceeded_throwsIOException() {
        String longLine = "1 NOTE " + "x".repeat(100) + "\n";
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader(longLine), 50);
        GedcomLine line = new GedcomLine();
        assertThrows(IOException.class, () -> tok.nextLine(line));
    }

    @Test
    void maxLineLengthAtLimit_succeeds() throws IOException {
        String exactLine = "1 NOTE " + "x".repeat(43) + "\n"; // exactly 50 chars
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader(exactLine), 50);
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("NOTE", line.getTag());
    }

    @Test
    void defaultMaxLineLengthIsGenerous() throws IOException {
        // Default 1MB limit should not trigger on normal lines
        String normalLine = "1 NOTE " + "x".repeat(1000) + "\n";
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader(normalLine));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
    }

    // Gap 3: Leading whitespace tolerance
    @Test
    void leadingSpaces_toleratedAndFlagged() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("  0 HEAD\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("HEAD", line.getTag());
        assertEquals(0, line.getLevel());
        assertTrue(line.hasLeadingWhitespace());
    }

    @Test
    void leadingTab_toleratedAndFlagged() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("\t1 NOTE Test\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("NOTE", line.getTag());
        assertEquals(1, line.getLevel());
        assertTrue(line.hasLeadingWhitespace());
    }

    @Test
    void noLeadingWhitespace_flagNotSet() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("0 HEAD\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("HEAD", line.getTag());
        assertFalse(line.hasLeadingWhitespace());
    }

    @Test
    void leadingWhitespace_valuePreserved() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("  1 NOTE Hello World\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("Hello World", line.getValue());
        assertTrue(line.hasLeadingWhitespace());
    }

    @Test
    void leadingWhitespace_rawLineIncludesWhitespace() throws IOException {
        GedcomLineTokenizer tok = new GedcomLineTokenizer(new StringReader("  0 HEAD\n"));
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("  0 HEAD", line.getRawLine());
    }
}
