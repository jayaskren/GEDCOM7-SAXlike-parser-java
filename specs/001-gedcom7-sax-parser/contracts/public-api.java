/**
 * GEDCOM 7 SAX-like Streaming Parser — Public API Contract
 *
 * This file defines the public API surface as Java interface
 * and class signatures. It is a design contract, not
 * compilable source. Implementation details are omitted.
 *
 * Package: org.gedcom7.parser
 */

// ─── Core Parser ────────────────────────────────────────

/**
 * Streaming GEDCOM 7 parser. Reads an InputStream and fires
 * SAX-like events to a GedcomHandler.
 *
 * <p>Thread safety: each instance is confined to a single
 * thread. Create separate instances for concurrent parsing.
 *
 * <p>Usage:
 * <pre>
 * try (GedcomReader reader = new GedcomReader(
 *         inputStream, handler, GedcomReaderConfig.gedcom7())) {
 *     reader.parse();
 * }
 * </pre>
 */
public final class GedcomReader implements AutoCloseable {

    public GedcomReader(InputStream input,
                        GedcomHandler handler,
                        GedcomReaderConfig config);

    /** Parse the entire input stream, firing events. */
    public void parse() throws GedcomFatalException;

    @Override
    public void close();
}

// ─── Configuration ──────────────────────────────────────

/**
 * Immutable configuration for GedcomReader.
 * Use builder or factory methods.
 */
public final class GedcomReaderConfig {

    /** Default GEDCOM 7 configuration (lenient mode). */
    public static GedcomReaderConfig gedcom7();

    /** GEDCOM 7 strict mode. */
    public static GedcomReaderConfig gedcom7Strict();

    public boolean isStrict();
    public int getMaxNestingDepth();    // default 1000
    public int getMaxLineLength();      // default 1_048_576
    public boolean isStructureValidationEnabled();

    /** Builder for custom configuration. */
    public static final class Builder {
        public Builder strict(boolean strict);
        public Builder maxNestingDepth(int depth);
        public Builder maxLineLength(int length);
        public Builder structureValidation(boolean enabled);
        // Internal extension points (package-private):
        // Builder inputDecoder(GedcomInputDecoder decoder);
        // Builder payloadAssembler(PayloadAssembler assembler);
        // Builder atEscapeStrategy(AtEscapeStrategy strategy);
        public GedcomReaderConfig build();
    }

    public Builder toBuilder();
}

// ─── Event Handler ──────────────────────────────────────

/**
 * Abstract handler with no-op defaults. Override the events
 * you care about.
 */
public abstract class GedcomHandler {

    /**
     * Fires before the first record event. Carries pre-parsed
     * HEAD metadata.
     */
    public void startDocument(GedcomHeaderInfo header) {}

    /** Fires after TRLR is processed. */
    public void endDocument() {}

    /**
     * Fires at each level-0 record (including HEAD).
     *
     * @param level always 0
     * @param xref  cross-reference identifier, or null
     * @param tag   record tag (INDI, FAM, HEAD, etc.)
     */
    public void startRecord(int level,
                            String xref,
                            String tag) {}

    public void endRecord(String tag) {}

    /**
     * Fires at each sub-record structure (level > 0, not CONT).
     *
     * @param level   nesting level (1+)
     * @param xref    always null for substructures
     * @param tag     structure tag
     * @param value   assembled payload (after CONT join and
     *                @@ unescape), or null
     * @param isPointer true if value is a pointer reference
     */
    public void startStructure(int level,
                               String xref,
                               String tag,
                               String value,
                               boolean isPointer) {}

    public void endStructure(String tag) {}

    /** Non-fatal issue detected. */
    public void warning(GedcomParseError error) {}

    /** Recoverable error (lenient mode continues). */
    public void error(GedcomParseError error) {}

    /** Unrecoverable error (parsing stops). */
    public void fatalError(GedcomParseError error) {}
}

// ─── Value Classes ──────────────────────────────────────

/**
 * Pre-parsed HEAD metadata delivered via startDocument.
 */
public final class GedcomHeaderInfo {

    public GedcomVersion getVersion();
    public String getSourceSystem();       // nullable
    public String getSourceVersion();      // nullable
    public String getSourceName();         // nullable
    public String getDefaultLanguage();    // nullable

    /**
     * Unmodifiable map of extension tag -> URI from
     * HEAD.SCHMA.TAG entries.
     */
    public Map<String, String> getSchemaMap();
}

/**
 * Parsed GEDCOM version (e.g., 7.0, 7.1, 5.5.5).
 */
public final class GedcomVersion {

    public int getMajor();
    public int getMinor();
    public int getPatch();    // -1 if not present

    public boolean isGedcom7();
    public boolean isGedcom5();

    @Override public String toString();
    @Override public boolean equals(Object o);
    @Override public int hashCode();
}

/**
 * Error/warning detail.
 */
public final class GedcomParseError {

    public enum Severity { WARNING, ERROR, FATAL }

    public Severity getSeverity();
    public int getLineNumber();       // 1-based
    public long getByteOffset();      // 0-based
    public String getMessage();
    public String getRawLine();       // nullable
}

/**
 * Unchecked exception wrapping a fatal parse error.
 * Thrown by parse() when a fatal error occurs.
 */
public final class GedcomFatalException
        extends RuntimeException {

    public GedcomParseError getError();
}
