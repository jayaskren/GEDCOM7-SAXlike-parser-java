package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Sex} enum and the {@code sex(Sex)} overload
 * on {@link org.gedcom7.writer.context.IndividualContext}.
 */
class SexEnumTest {

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
    void sexMale() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.sex(Sex.MALE));
            writer.trailer();
        });
        assertTrue(output.contains("1 SEX M\n"), "Expected '1 SEX M' in output:\n" + output);
    }

    @Test
    void sexFemale() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.sex(Sex.FEMALE));
            writer.trailer();
        });
        assertTrue(output.contains("1 SEX F\n"), "Expected '1 SEX F' in output:\n" + output);
    }

    @Test
    void sexIntersex() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.sex(Sex.INTERSEX));
            writer.trailer();
        });
        assertTrue(output.contains("1 SEX X\n"), "Expected '1 SEX X' in output:\n" + output);
    }

    @Test
    void sexUnknown() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.sex(Sex.UNKNOWN));
            writer.trailer();
        });
        assertTrue(output.contains("1 SEX U\n"), "Expected '1 SEX U' in output:\n" + output);
    }

    @Test
    void sexNullEnumProducesNoLine() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.sex((Sex) null));
            writer.trailer();
        });
        assertFalse(output.contains("SEX"), "Expected no SEX line when null enum passed:\n" + output);
    }

    @Test
    void existingStringSexUnchanged() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.sex("M"));
            writer.trailer();
        });
        assertTrue(output.contains("1 SEX M\n"), "Expected '1 SEX M' from string overload:\n" + output);
    }
}
