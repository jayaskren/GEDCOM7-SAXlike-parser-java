package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContOnlyAssemblerTest {

    private final ContOnlyAssembler assembler = new ContOnlyAssembler();

    @Test
    void contIsRecognized() {
        assertTrue(assembler.isPseudoStructure("CONT"));
    }

    @Test
    void concIsNotPseudoStructure() {
        assertFalse(assembler.isPseudoStructure("CONC"));
    }

    @Test
    void normalTagNotPseudo() {
        assertFalse(assembler.isPseudoStructure("NAME"));
    }

    @Test
    void assembleToExisting() {
        assertEquals("line1\nline2", assembler.assemblePayload("line1", "line2"));
    }

    @Test
    void assembleToNull() {
        assertEquals("\nline2", assembler.assemblePayload(null, "line2"));
    }

    @Test
    void assembleEmptyCont() {
        assertEquals("line1\n", assembler.assemblePayload("line1", null));
    }

    @Test
    void assembleNullToNull() {
        assertEquals("\n", assembler.assemblePayload(null, null));
    }
}
