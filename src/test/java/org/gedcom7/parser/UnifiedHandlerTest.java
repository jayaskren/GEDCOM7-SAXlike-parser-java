package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that a single {@link GedcomHandler} implementation works for both
 * GEDCOM 5.5.5 and GEDCOM 7 files without modification.
 */
class UnifiedHandlerTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    @Test
    void sameHandlerParsesBothVersions() {
        List<String> events = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                events.add("startRecord:" + tag);
            }
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                events.add("startStructure:" + tag);
            }
        };

        // Parse 5.5.5
        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }
        assertFalse(events.isEmpty(), "Should have events from 5.5.5 file");

        // Clear and reuse same handler for GEDCOM 7
        events.clear();
        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), handler,
                GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertFalse(events.isEmpty(), "Should have events from GEDCOM 7 file");
    }

    @Test
    void gedcom555HeaderHasVersion555() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        GedcomHeaderInfo header = headerRef.get();
        assertNotNull(header);
        assertEquals(5, header.getVersion().getMajor());
        assertEquals(5, header.getVersion().getMinor());
        assertEquals(5, header.getVersion().getPatch());
    }

    @Test
    void gedcom555HeaderHasCharacterEncoding() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        assertEquals("UTF-8", headerRef.get().getCharacterEncoding());
    }

    @Test
    void gedcom7HeaderHasNullCharacterEncoding() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), handler,
                GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        assertNull(headerRef.get().getCharacterEncoding());
    }

    @Test
    void autoDetectRoutesBothVersions() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        // 5.5.5 via autoDetect
        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }
        assertTrue(headerRef.get().getVersion().isGedcom5());

        // 7.x via autoDetect
        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), handler,
                GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }
        assertTrue(headerRef.get().getVersion().isGedcom7());
    }
}
