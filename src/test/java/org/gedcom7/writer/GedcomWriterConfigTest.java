package org.gedcom7.writer;

import org.gedcom7.parser.GedcomVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GedcomWriterConfigTest {

    @Test
    void gedcom7FactoryDefaults() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7();
        assertTrue(config.getVersion().isGedcom7());
        assertFalse(config.isStrict());
        assertNotNull(config.getWarningHandler());
        assertEquals(0, config.getMaxLineLength());
        assertEquals("\n", config.getLineEnding());
        assertFalse(config.isEscapeAllAt());
        assertFalse(config.isConcEnabled());
    }

    @Test
    void gedcom7StrictFactory() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7Strict();
        assertTrue(config.getVersion().isGedcom7());
        assertTrue(config.isStrict());
    }

    @Test
    void gedcom555FactoryDefaults() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom555();
        assertTrue(config.getVersion().isGedcom5());
        assertFalse(config.isStrict());
        assertTrue(config.isEscapeAllAt());
        assertTrue(config.isConcEnabled());
        assertEquals(255, config.getMaxLineLength());
    }

    @Test
    void gedcom555StrictFactory() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom555Strict();
        assertTrue(config.getVersion().isGedcom5());
        assertTrue(config.isStrict());
        assertTrue(config.isEscapeAllAt());
        assertTrue(config.isConcEnabled());
    }

    @Test
    void toBuilderPreservesValues() {
        GedcomWriterConfig original = GedcomWriterConfig.gedcom555Strict();
        GedcomWriterConfig copy = original.toBuilder().build();

        assertEquals(original.getVersion(), copy.getVersion());
        assertEquals(original.isStrict(), copy.isStrict());
        assertEquals(original.getMaxLineLength(), copy.getMaxLineLength());
        assertEquals(original.getLineEnding(), copy.getLineEnding());
        assertEquals(original.isEscapeAllAt(), copy.isEscapeAllAt());
        assertEquals(original.isConcEnabled(), copy.isConcEnabled());
    }

    @Test
    void toBuilderAllowsOverride() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7()
                .toBuilder()
                .lineEnding("\r\n")
                .build();

        assertTrue(config.getVersion().isGedcom7());
        assertEquals("\r\n", config.getLineEnding());
    }

    @Test
    void nullWarningHandlerAllowed() {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7()
                .toBuilder()
                .warningHandler(null)
                .build();

        assertNull(config.getWarningHandler());
    }

    @Test
    void customWarningHandler() {
        WarningHandler custom = warning -> {};
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7()
                .toBuilder()
                .warningHandler(custom)
                .build();

        assertSame(custom, config.getWarningHandler());
    }
}
