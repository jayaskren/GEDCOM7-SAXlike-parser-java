package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GedcomLineTokenizerTest {

    private GedcomLineTokenizer tokenizer(String input) {
        return new GedcomLineTokenizer(new StringReader(input));
    }

    @Test
    void parseLevelTagOnly() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals(0, line.getLevel());
        assertNull(line.getXref());
        assertEquals("HEAD", line.getTag());
        assertNull(line.getValue());
        assertFalse(line.isPointer());
        assertEquals(1, line.getLineNumber());
    }

    @Test
    void parseLevelTagValue() throws IOException {
        GedcomLineTokenizer tok = tokenizer("2 VERS 7.0\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals(2, line.getLevel());
        assertEquals("VERS", line.getTag());
        assertEquals("7.0", line.getValue());
    }

    @Test
    void parseXref() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 @I1@ INDI\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals(0, line.getLevel());
        assertEquals("I1", line.getXref());
        assertEquals("INDI", line.getTag());
        assertNull(line.getValue());
    }

    @Test
    void parsePointerValue() throws IOException {
        GedcomLineTokenizer tok = tokenizer("1 FAMC @F1@\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals(1, line.getLevel());
        assertEquals("FAMC", line.getTag());
        assertEquals("@F1@", line.getValue());
        assertTrue(line.isPointer());
    }

    @Test
    void secondSpaceIsPartOfValue() throws IOException {
        GedcomLineTokenizer tok = tokenizer("1 NOTE Hello World\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("Hello World", line.getValue());
    }

    @Test
    void crlfLineEnding() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD\r\n0 TRLR\r\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("HEAD", line.getTag());
        assertTrue(tok.nextLine(line));
        assertEquals("TRLR", line.getTag());
    }

    @Test
    void crOnlyLineEnding() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD\r0 TRLR\r");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("HEAD", line.getTag());
        assertTrue(tok.nextLine(line));
        assertEquals("TRLR", line.getTag());
    }

    @Test
    void lfLineEnding() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD\n0 TRLR\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("HEAD", line.getTag());
        assertTrue(tok.nextLine(line));
        assertEquals("TRLR", line.getTag());
    }

    @Test
    void multipleLines() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n");
        GedcomLine line = new GedcomLine();
        List<String> tags = new ArrayList<>();
        while (tok.nextLine(line)) {
            tags.add(line.getTag());
        }
        assertEquals(List.of("HEAD", "GEDC", "VERS", "TRLR"), tags);
    }

    @Test
    void lineNumbersAreTracked() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD\n1 GEDC\n");
        GedcomLine line = new GedcomLine();
        tok.nextLine(line);
        assertEquals(1, line.getLineNumber());
        tok.nextLine(line);
        assertEquals(2, line.getLineNumber());
    }

    @Test
    void emptyInputReturnsFalse() throws IOException {
        GedcomLineTokenizer tok = tokenizer("");
        GedcomLine line = new GedcomLine();
        assertFalse(tok.nextLine(line));
    }

    @Test
    void lineWithNoTrailingNewline() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("HEAD", line.getTag());
        assertFalse(tok.nextLine(line));
    }

    @Test
    void voidPointer() throws IOException {
        GedcomLineTokenizer tok = tokenizer("1 FAMC @VOID@\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("@VOID@", line.getValue());
        assertTrue(line.isPointer());
    }

    @Test
    void extensionTag() throws IOException {
        GedcomLineTokenizer tok = tokenizer("1 _CUSTOM value\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("_CUSTOM", line.getTag());
        assertEquals("value", line.getValue());
    }

    @Test
    void blankLinesSkipped() throws IOException {
        GedcomLineTokenizer tok = tokenizer("0 HEAD\n\n0 TRLR\n");
        GedcomLine line = new GedcomLine();
        assertTrue(tok.nextLine(line));
        assertEquals("HEAD", line.getTag());
        assertTrue(tok.nextLine(line));
        assertEquals("TRLR", line.getTag());
    }
}
