package org.gedcom7.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GedzipReaderTest {

    // Test 1: Open basic.gdz and parse GEDCOM data
    @Test
    void parseBasicGdz() throws Exception {
        Path gdz = Path.of("src/test/resources/gedzip/basic.gdz");
        try (GedzipReader reader = new GedzipReader(gdz)) {
            InputStream gedcom = reader.getGedcomStream();
            assertNotNull(gedcom);
            // Parse the GEDCOM to verify it's valid
            int[] recordCount = {0};
            GedcomReader parser = new GedcomReader(gedcom, new GedcomHandler() {
                @Override
                public void startRecord(int level, String xref, String tag) {
                    recordCount[0]++;
                }
            }, GedcomReaderConfig.autoDetect());
            parser.parse();
            assertTrue(recordCount[0] > 0, "Should parse at least one record");
        }
    }

    // Test 2: Access media file from archive
    @Test
    void getMediaEntry() throws Exception {
        Path gdz = Path.of("src/test/resources/gedzip/with-media.gdz");
        try (GedzipReader reader = new GedzipReader(gdz)) {
            InputStream photo = reader.getEntry("photos/test.jpg");
            assertNotNull(photo, "photos/test.jpg should exist in archive");
            byte[] data = photo.readAllBytes();
            assertTrue(data.length > 0, "Photo data should not be empty");
        }
    }

    // Test 3: getEntry returns null for nonexistent
    @Test
    void getEntryReturnsNullForMissing() throws Exception {
        Path gdz = Path.of("src/test/resources/gedzip/basic.gdz");
        try (GedzipReader reader = new GedzipReader(gdz)) {
            assertNull(reader.getEntry("nonexistent.jpg"));
        }
    }

    // Test 4: hasEntry works
    @Test
    void hasEntryWorks() throws Exception {
        Path gdz = Path.of("src/test/resources/gedzip/with-media.gdz");
        try (GedzipReader reader = new GedzipReader(gdz)) {
            assertTrue(reader.hasEntry("photos/test.jpg"));
            assertFalse(reader.hasEntry("nonexistent.jpg"));
        }
    }

    // Test 5: Missing gedcom.ged throws IOException
    @Test
    void missingGedcomGedThrowsIOException() {
        Path zip = Path.of("src/test/resources/gedzip/no-gedcom.zip");
        IOException ex = assertThrows(IOException.class, () -> new GedzipReader(zip));
        assertTrue(ex.getMessage().contains("gedcom.ged"),
                "Error message should mention gedcom.ged");
    }

    // Test 6: Percent-encoded file access
    @Test
    void percentEncodedEntryAccess() throws Exception {
        Path gdz = Path.of("src/test/resources/gedzip/percent-encoded.gdz");
        try (GedzipReader reader = new GedzipReader(gdz)) {
            // Access using percent-encoded path (as in GEDCOM FILE value)
            InputStream entry = reader.getEntry("photo%20album/family%20pic.jpg");
            assertNotNull(entry, "Percent-encoded path should resolve to entry");
        }
    }

    // Test 7: getEntryNames returns unmodifiable set
    @Test
    void getEntryNamesReturnsUnmodifiableSet() throws Exception {
        Path gdz = Path.of("src/test/resources/gedzip/with-media.gdz");
        try (GedzipReader reader = new GedzipReader(gdz)) {
            Set<String> names = reader.getEntryNames();
            assertTrue(names.contains("gedcom.ged"));
            assertTrue(names.contains("photos/test.jpg"));
            assertThrows(UnsupportedOperationException.class, () -> names.add("foo"));
        }
    }

    // Test 8: File constructor works
    @Test
    void fileConstructorWorks() throws Exception {
        File file = new File("src/test/resources/gedzip/basic.gdz");
        try (GedzipReader reader = new GedzipReader(file)) {
            assertNotNull(reader.getGedcomStream());
        }
    }

    // Test 9: null path returns null
    @Test
    void nullPathReturnsNull() throws Exception {
        Path gdz = Path.of("src/test/resources/gedzip/basic.gdz");
        try (GedzipReader reader = new GedzipReader(gdz)) {
            assertNull(reader.getEntry(null));
            assertFalse(reader.hasEntry(null));
        }
    }

    @Test
    void isExternalReferenceDetectsUrls() {
        assertTrue(GedzipReader.isExternalReference("http://example.com/photo.jpg"));
        assertTrue(GedzipReader.isExternalReference("https://example.com/photo.jpg"));
        assertTrue(GedzipReader.isExternalReference("ftp://files.example.com/doc.pdf"));
        assertFalse(GedzipReader.isExternalReference("photos/test.jpg"));
        assertFalse(GedzipReader.isExternalReference("photo%20album/family%20pic.jpg"));
        assertFalse(GedzipReader.isExternalReference(null));
    }
}
