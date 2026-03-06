package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GedcomWriteException being unchecked (FR-007).
 */
class UncheckedExceptionTest {

    @Test
    void exceptionIsRuntimeException() {
        GedcomWriteException ex = new GedcomWriteException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void exceptionWithCauseIsRuntimeException() {
        GedcomWriteException ex = new GedcomWriteException("test", new RuntimeException("cause"));
        assertInstanceOf(RuntimeException.class, ex);
        assertNotNull(ex.getCause());
    }

    @Test
    void writerMethodsWorkWithoutTryCatch() {
        // This test verifies that writer methods can be called without
        // try/catch for GedcomWriteException (it's now unchecked)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.individual(indi -> {
                indi.personalName("John /Doe/");
                indi.birth(body -> body.date("15 MAR 1955"));
            });
            writer.family(fam -> {
                fam.marriage(body -> body.date("1 JUN 1980"));
            });
        } catch (java.io.IOException e) {
            fail("Unexpected IOException");
        }
        String result = out.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("1 NAME John /Doe/"));
        assertTrue(result.contains("1 BIRT"));
        assertTrue(result.contains("1 MARR"));
    }

    @Test
    void strictModeThrowsGedcomWriteException() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GedcomWriterConfig config = new GedcomWriterConfig.Builder()
                .strict(true)
                .build();
        GedcomWriter writer = new GedcomWriter(out, config);
        // In strict mode, writing a record without HEAD throws GedcomWriteException
        assertThrows(GedcomWriteException.class, () -> {
            writer.individual(indi -> {});
        });
    }

    @Test
    void catchBlockStillWorksForGedcomWriteException() {
        // Verify that existing catch(GedcomWriteException) still works
        GedcomWriteException ex = new GedcomWriteException("test error");
        try {
            throw ex;
        } catch (GedcomWriteException caught) {
            assertEquals("test error", caught.getMessage());
        }
    }
}
