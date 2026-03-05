package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quickstart scenarios from specs/002-gedzip-gedcom555/quickstart.md.
 */
class Gedcom555QuickstartTest {

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    @Test
    void parseFiveFiveFile() {
        AtomicBoolean documentStarted = new AtomicBoolean(false);
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                documentStarted.set(true);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }

        assertTrue(documentStarted.get());
    }

    @Test
    void autoDetectFile() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        assertTrue(headerRef.get().getVersion().isGedcom5());
    }

    @Test
    void openGedzipArchive() throws Exception {
        URL gdzUrl = getClass().getResource("/gedzip/basic.gdz");
        assertNotNull(gdzUrl, "basic.gdz not found");
        Path gdzPath = Paths.get(gdzUrl.toURI());

        AtomicBoolean documentStarted = new AtomicBoolean(false);
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                documentStarted.set(true);
            }
        };

        try (GedzipReader gdz = new GedzipReader(gdzPath);
             InputStream gedStream = gdz.getGedcomStream();
             GedcomReader reader = new GedcomReader(
                     gedStream, handler, GedcomReaderConfig.autoDetect())) {
            reader.parse();
        }

        assertTrue(documentStarted.get());
    }

    @Test
    void strictValidation() {
        // basic-555.ged is a valid short file — strict should not throw
        AtomicBoolean documentStarted = new AtomicBoolean(false);
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                documentStarted.set(true);
            }
        };

        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.gedcom555Strict())) {
            reader.parse();
        }

        assertTrue(documentStarted.get());
    }

    @Test
    void unifiedHandler() {
        AtomicReference<GedcomHeaderInfo> headerRef = new AtomicReference<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                headerRef.set(header);
            }
        };

        // Same handler for 5.5.5
        try (GedcomReader reader = new GedcomReader(
                resource("gedcom555/basic-555.ged"), handler,
                GedcomReaderConfig.gedcom555())) {
            reader.parse();
        }
        assertTrue(headerRef.get().getVersion().isGedcom5());

        // Same handler for 7.0
        try (GedcomReader reader = new GedcomReader(
                resource("minimal.ged"), handler,
                GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(headerRef.get().getVersion().isGedcom7());
    }
}
