# Public API Contract: GEDZip Support and GEDCOM 5.5.5 Compatibility

**Date**: 2026-03-05 | **Branch**: `002-gedzip-gedcom555`

## New Public API

### GedzipReader

```java
package org.gedcom7.parser;

/**
 * Reads a GEDZip archive (.gdz file), providing access to the
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
 *     // Access media files
 *     InputStream photo = gdz.getEntry("photos/grandma.jpg");
 * }
 * </pre>
 */
public final class GedzipReader implements AutoCloseable {

    /**
     * Opens a GEDZip archive from the given path.
     *
     * @param path the path to the .gdz file
     * @throws IOException if the file cannot be read or does not
     *         contain a {@code gedcom.ged} entry
     */
    public GedzipReader(Path path) throws IOException;

    /**
     * Opens a GEDZip archive from the given file.
     *
     * @param file the .gdz file
     * @throws IOException if the file cannot be read or does not
     *         contain a {@code gedcom.ged} entry
     */
    public GedzipReader(File file) throws IOException;

    /**
     * Returns an InputStream for the {@code gedcom.ged} entry.
     * The stream is valid until this GedzipReader is closed.
     *
     * @return input stream for the GEDCOM data
     * @throws IOException if the entry cannot be read
     */
    public InputStream getGedcomStream() throws IOException;

    /**
     * Returns an InputStream for the specified archive entry.
     * The path is percent-decoded before matching against ZIP entries.
     *
     * @param path the entry path (as it would appear in a GEDCOM FILE value)
     * @return input stream for the entry, or null if not found
     */
    public InputStream getEntry(String path);

    /**
     * Returns true if the archive contains an entry at the given path.
     * The path is percent-decoded before matching.
     *
     * @param path the entry path to check
     * @return true if the entry exists
     */
    public boolean hasEntry(String path);

    /**
     * Returns the set of all entry names in the archive.
     *
     * @return unmodifiable set of entry names
     */
    public Set<String> getEntryNames();

    /**
     * Closes the underlying ZIP archive.
     */
    @Override
    public void close() throws IOException;
}
```

## Modified Public API

### GedcomReaderConfig — New Factory Methods

```java
// Existing (unchanged):
public static GedcomReaderConfig gedcom7();
public static GedcomReaderConfig gedcom7Strict();

// New:

/**
 * GEDCOM 5.5.5 configuration (lenient mode).
 * Uses BOM-detecting decoder, CONT+CONC assembler,
 * and all-@@ escape strategy.
 */
public static GedcomReaderConfig gedcom555();

/**
 * GEDCOM 5.5.5 strict configuration.
 * Same strategies as gedcom555(), plus strict=true
 * and maxLineLength=255.
 */
public static GedcomReaderConfig gedcom555Strict();

/**
 * Auto-detecting configuration (lenient mode).
 * Uses BOM-detecting decoder. After scanning HEAD,
 * automatically selects GEDCOM 7 or 5.5.5 strategies
 * based on HEAD.GEDC.VERS.
 */
public static GedcomReaderConfig autoDetect();

/**
 * Auto-detecting strict configuration.
 * Same as autoDetect() with strict=true.
 */
public static GedcomReaderConfig autoDetectStrict();
```

### GedcomReaderConfig.Builder — New Method

```java
// New:

/**
 * Sets the auto-detect flag.
 *
 * @param autoDetect true to enable version auto-detection
 * @return this builder
 */
public Builder autoDetect(boolean autoDetect);
```

### GedcomHeaderInfo — New Field

```java
// New constructor parameter and accessor:

/**
 * Returns the character encoding declared in HEAD.CHAR,
 * or null if not present (GEDCOM 7 files do not use HEAD.CHAR).
 * Typical values: "UTF-8", "UNICODE".
 */
public String getCharacterEncoding();
```

### PayloadAssembler — Modified Method Signature

```java
// Before (internal, not public API):
String assemblePayload(String existing, String continuationValue);

// After:
String assemblePayload(String existing, String continuationValue, String tag);
```

This is an internal interface change. Existing `ContOnlyAssembler` is updated to accept but ignore the tag parameter.

## Backward Compatibility

| Change | Impact |
|--------|--------|
| New factory methods on GedcomReaderConfig | Additive — no existing code breaks |
| New GedzipReader class | Additive — new class in existing package |
| New field on GedcomHeaderInfo | Constructor gains a parameter. Existing code using the old 6-param constructor will need updating. Mitigate by adding the new field as a 7th parameter with null default via overloaded constructor. |
| PayloadAssembler signature change | Internal interface — not visible to library users. Existing strategy implementations updated. |
| autoDetect field on GedcomReaderConfig | Package-private getter; no public API impact |

## Test Contract

Each new public API element must have:

1. **Unit tests**: Individual method behavior, null handling, edge cases
2. **Integration tests**: End-to-end parsing of sample files through the new API
3. **Backward compatibility tests**: Existing tests pass unchanged after modifications
