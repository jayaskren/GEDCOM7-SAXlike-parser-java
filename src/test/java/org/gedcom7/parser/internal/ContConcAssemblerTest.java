package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContConcAssemblerTest {

    private final ContConcAssembler assembler = new ContConcAssembler();

    @Test
    void contIsPseudoStructure() {
        assertTrue(assembler.isPseudoStructure("CONT"));
    }

    @Test
    void concIsPseudoStructure() {
        assertTrue(assembler.isPseudoStructure("CONC"));
    }

    @Test
    void nameIsNotPseudoStructure() {
        assertFalse(assembler.isPseudoStructure("NAME"));
    }

    @Test
    void contAssemblesWithNewline() {
        StringBuilder sb = new StringBuilder("line1");
        assembler.appendPayload(sb, "line2", "CONT");
        assertEquals("line1\nline2", sb.toString());
    }

    @Test
    void concAssemblesWithoutSeparator() {
        StringBuilder sb = new StringBuilder("line1");
        assembler.appendPayload(sb, "line2", "CONC");
        assertEquals("line1line2", sb.toString());
    }

    @Test
    void emptyExistingWithCont() {
        StringBuilder sb = new StringBuilder();
        assembler.appendPayload(sb, "line2", "CONT");
        assertEquals("\nline2", sb.toString());
    }

    @Test
    void emptyExistingWithConc() {
        StringBuilder sb = new StringBuilder();
        assembler.appendPayload(sb, "line2", "CONC");
        assertEquals("line2", sb.toString());
    }

    @Test
    void nullContinuationValue() {
        StringBuilder sb = new StringBuilder("line1");
        assembler.appendPayload(sb, null, "CONT");
        assertEquals("line1\n", sb.toString());
    }

    @Test
    void bothEmpty() {
        StringBuilder sb = new StringBuilder();
        assembler.appendPayload(sb, null, "CONT");
        assertEquals("\n", sb.toString());
    }
}
