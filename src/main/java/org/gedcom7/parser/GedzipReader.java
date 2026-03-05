package org.gedcom7.parser;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.*;

/**
 * Reads a GEDZip archive ({@code .gdz} file), providing access to the
 * contained GEDCOM data and any referenced media files.
 *
 * <p>GEDZip is a ZIP archive (ISO/IEC 21320-1:2015) that must contain
 * a {@code gedcom.ged} entry at the archive root.
 *
 * <p>Usage:
 * <pre>
 * try (GedzipReader gdz = new GedzipReader(Path.of("family.gdz"))) {
 *     InputStream gedcom = gdz.getGedcomStream();
 *     GedcomReader reader = new GedcomReader(gedcom, handler, config);
 *     reader.parse();
 *
 *     InputStream photo = gdz.getEntry("photos/grandma.jpg");
 * }
 * </pre>
 */
public final class GedzipReader implements AutoCloseable {

    private static final String GEDCOM_ENTRY_NAME = "gedcom.ged";

    private final ZipFile zipFile;
    private final ZipEntry gedcomEntry;

    /**
     * Opens a GEDZip archive from the given path.
     *
     * @param path the path to the .gdz file
     * @throws IOException if the file cannot be read or does not contain a gedcom.ged entry
     */
    public GedzipReader(Path path) throws IOException {
        this(path.toFile());
    }

    /**
     * Opens a GEDZip archive from the given file.
     *
     * @param file the .gdz file
     * @throws IOException if the file cannot be read or does not contain a gedcom.ged entry
     */
    public GedzipReader(File file) throws IOException {
        this.zipFile = new ZipFile(file);
        this.gedcomEntry = zipFile.getEntry(GEDCOM_ENTRY_NAME);
        if (gedcomEntry == null) {
            zipFile.close();
            throw new IOException("Not a valid GEDZip archive: missing " + GEDCOM_ENTRY_NAME + " entry");
        }
    }

    /**
     * Returns an InputStream for the gedcom.ged entry.
     */
    public InputStream getGedcomStream() throws IOException {
        return zipFile.getInputStream(gedcomEntry);
    }

    /**
     * Returns an InputStream for the specified archive entry.
     * The path is percent-decoded before matching against ZIP entries.
     *
     * @param path the entry path (as it would appear in a GEDCOM FILE value)
     * @return input stream for the entry, or null if not found
     */
    public InputStream getEntry(String path) {
        if (path == null) return null;
        String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8);
        ZipEntry entry = zipFile.getEntry(decoded);
        if (entry == null) return null;
        try {
            return zipFile.getInputStream(entry);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns true if the archive contains an entry at the given path.
     * The path is percent-decoded before matching.
     */
    public boolean hasEntry(String path) {
        if (path == null) return false;
        String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8);
        return zipFile.getEntry(decoded) != null;
    }

    /**
     * Returns the set of all entry names in the archive.
     *
     * @return unmodifiable set of entry names
     */
    public Set<String> getEntryNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            names.add(entries.nextElement().getName());
        }
        return Collections.unmodifiableSet(names);
    }

    /**
     * Returns true if the given path appears to be an external URL
     * rather than a local archive entry reference.
     *
     * @param path the FILE value from a GEDCOM structure
     * @return true if the path starts with a URL scheme (http, https, ftp, ftps)
     */
    public static boolean isExternalReference(String path) {
        if (path == null) return false;
        return path.startsWith("http://") || path.startsWith("https://")
                || path.startsWith("ftp://") || path.startsWith("ftps://");
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }
}
