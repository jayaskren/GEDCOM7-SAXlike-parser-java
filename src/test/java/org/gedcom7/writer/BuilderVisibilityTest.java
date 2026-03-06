package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that Builder methods for escapeAllAt and concEnabled are publicly
 * accessible and produce the expected configuration values.
 */
class BuilderVisibilityTest {

    @Test
    void escapeAllAtTrueViaBuilder() {
        GedcomWriterConfig config = new GedcomWriterConfig.Builder()
                .escapeAllAt(true)
                .build();
        assertTrue(config.isEscapeAllAt());
    }

    @Test
    void escapeAllAtDefaultIsFalse() {
        GedcomWriterConfig config = new GedcomWriterConfig.Builder().build();
        assertFalse(config.isEscapeAllAt());
    }

    @Test
    void concEnabledWithMaxLineLengthViaBuilder() {
        GedcomWriterConfig config = new GedcomWriterConfig.Builder()
                .concEnabled(true)
                .maxLineLength(200)
                .build();
        assertTrue(config.isConcEnabled());
        assertEquals(200, config.getMaxLineLength());
    }

    @Test
    void concEnabledDefaultIsFalse() {
        GedcomWriterConfig config = new GedcomWriterConfig.Builder().build();
        assertFalse(config.isConcEnabled());
    }

    @Test
    void gedcom7FactoryMethodUnchanged() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7();
        assertFalse(config.isEscapeAllAt());
        assertFalse(config.isConcEnabled());
        assertEquals(0, config.getMaxLineLength());
    }

    @Test
    void gedcom555FactoryMethodUnchanged() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom555();
        assertTrue(config.isEscapeAllAt());
        assertTrue(config.isConcEnabled());
        assertEquals(255, config.getMaxLineLength());
    }
}
