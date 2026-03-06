package org.gedcom7.writer.context;

import org.gedcom7.writer.GedcomWriter;
import org.gedcom7.writer.GedcomWriteException;
import org.gedcom7.writer.Xref;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EscapeHatchTest {

    private String writeIndividual(java.util.function.Consumer<org.gedcom7.writer.context.IndividualContext> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("Test"));
            writer.individual(indi -> {
                try {
                    action.accept(indi);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void structureWithTagAndValue() throws Exception {
        String output = writeIndividual(indi -> indi.structure("_CUSTOM", "my value"));
        assertTrue(output.contains("1 _CUSTOM my value\n"));
    }

    @Test
    void structureWithTagAndBody() throws Exception {
        String output = writeIndividual(indi ->
                indi.structure("_CUSTOM", ctx -> ctx.structure("_SUB", "nested")));
        assertTrue(output.contains("1 _CUSTOM\n"));
        assertTrue(output.contains("2 _SUB nested\n"));
    }

    @Test
    void structureWithTagValueAndBody() throws Exception {
        String output = writeIndividual(indi ->
                indi.structure("_CUSTOM", "parent value", ctx -> ctx.structure("_SUB", "child")));
        assertTrue(output.contains("1 _CUSTOM parent value\n"));
        assertTrue(output.contains("2 _SUB child\n"));
    }

    @Test
    void pointerWithXref() throws Exception {
        String output = writeIndividual(indi ->
                indi.pointer("_LINK", Xref.of("R1")));
        assertTrue(output.contains("1 _LINK @R1@\n"));
    }

    @Test
    void pointerWithString() throws Exception {
        String output = writeIndividual(indi ->
                indi.pointer("_LINK", "R2"));
        assertTrue(output.contains("1 _LINK @R2@\n"));
    }

    @Test
    void pointerWithXrefAndBody() throws Exception {
        String output = writeIndividual(indi ->
                indi.pointer("_LINK", Xref.of("R3"), ctx -> ctx.structure("_NOTE", "linked")));
        assertTrue(output.contains("1 _LINK @R3@\n"));
        assertTrue(output.contains("2 _NOTE linked\n"));
    }

    @Test
    void pointerWithStringAndBody() throws Exception {
        String output = writeIndividual(indi ->
                indi.pointer("_LINK", "R4", ctx -> ctx.structure("_NOTE", "linked2")));
        assertTrue(output.contains("1 _LINK @R4@\n"));
        assertTrue(output.contains("2 _NOTE linked2\n"));
    }

    @Test
    void escapeHatchProducesSameOutputAsTypedMethod() throws Exception {
        // structure("BIRT", ...) should produce same output as birth(...)
        String viaTyped = writeIndividual(indi ->
                indi.birth(birt -> birt.date("15 MAR 1955")));
        String viaEscapeHatch = writeIndividual(indi ->
                indi.structure("BIRT", ctx -> ctx.structure("DATE", "15 MAR 1955")));

        // Both should contain the same BIRT and DATE lines
        assertTrue(viaTyped.contains("1 BIRT\n"));
        assertTrue(viaTyped.contains("2 DATE 15 MAR 1955\n"));
        assertTrue(viaEscapeHatch.contains("1 BIRT\n"));
        assertTrue(viaEscapeHatch.contains("2 DATE 15 MAR 1955\n"));
    }

    @Test
    void noteOnAnyContext() throws Exception {
        String output = writeIndividual(indi ->
                indi.birth(birt -> birt.note("A note on the birth event")));
        assertTrue(output.contains("2 NOTE A note on the birth event\n"));
    }

    @Test
    void uidOnAnyContext() throws Exception {
        String output = writeIndividual(indi -> indi.uid("abc-123-def"));
        assertTrue(output.contains("1 UID abc-123-def\n"));
    }
}
