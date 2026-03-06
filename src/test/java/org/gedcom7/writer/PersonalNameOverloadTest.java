package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@code personalName(String givenName, String surname)}
 * convenience overloads on {@link org.gedcom7.writer.context.IndividualContext}.
 */
class PersonalNameOverloadTest {

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
    void givenAndSurnameProducesNameGivnSurn() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.personalName("John", "Doe"));
            writer.trailer();
        });
        assertTrue(output.contains("1 NAME John /Doe/\n"),
                "Expected '1 NAME John /Doe/' in output:\n" + output);
        assertTrue(output.contains("2 GIVN John\n"),
                "Expected '2 GIVN John' in output:\n" + output);
        assertTrue(output.contains("2 SURN Doe\n"),
                "Expected '2 SURN Doe' in output:\n" + output);
    }

    @Test
    void givenAndSurnameWithBodyAddsSubstructures() throws Exception {
        String output = write(writer -> {
            writer.individual(indi ->
                    indi.personalName("John", "Doe", name -> {
                        name.nickname("Johnny");
                    }));
            writer.trailer();
        });
        assertTrue(output.contains("1 NAME John /Doe/\n"),
                "Expected '1 NAME John /Doe/' in output:\n" + output);
        assertTrue(output.contains("2 GIVN John\n"),
                "Expected '2 GIVN John' in output:\n" + output);
        assertTrue(output.contains("2 SURN Doe\n"),
                "Expected '2 SURN Doe' in output:\n" + output);
        assertTrue(output.contains("2 NICK Johnny\n"),
                "Expected '2 NICK Johnny' in output:\n" + output);
    }

    @Test
    void givenOnlyWhenSurnameIsNull() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.personalName("Maria", (String) null));
            writer.trailer();
        });
        assertTrue(output.contains("1 NAME Maria\n"),
                "Expected '1 NAME Maria' in output:\n" + output);
        assertTrue(output.contains("2 GIVN Maria\n"),
                "Expected '2 GIVN Maria' in output:\n" + output);
        assertFalse(output.contains("SURN"),
                "Expected no SURN line when surname is null:\n" + output);
    }

    @Test
    void surnameOnlyWhenGivenIsNull() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.personalName(null, "Doe"));
            writer.trailer();
        });
        assertTrue(output.contains("1 NAME /Doe/\n"),
                "Expected '1 NAME /Doe/' in output:\n" + output);
        assertTrue(output.contains("2 SURN Doe\n"),
                "Expected '2 SURN Doe' in output:\n" + output);
        assertFalse(output.contains("GIVN"),
                "Expected no GIVN line when given name is null:\n" + output);
    }

    @Test
    void emptyStringsProduceNoNameLine() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.personalName("", ""));
            writer.trailer();
        });
        assertFalse(output.contains("NAME"),
                "Expected no NAME line when both given and surname are empty:\n" + output);
    }

    @Test
    void existingStringOverloadStillWorks() throws Exception {
        String output = write(writer -> {
            writer.individual(indi -> indi.personalName("John /Doe/"));
            writer.trailer();
        });
        assertTrue(output.contains("1 NAME John /Doe/\n"),
                "Expected '1 NAME John /Doe/' from string overload:\n" + output);
    }
}
