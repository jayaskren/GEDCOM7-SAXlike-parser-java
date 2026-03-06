package org.gedcom7.parser;

/**
 * Abstract handler for GEDCOM parse events. Override the
 * methods you care about; defaults are no-ops.
 *
 * <p>Events fire in document order. For each record or
 * structure, {@code start*} fires before any children,
 * and the corresponding {@code end*} fires after all
 * children have been processed.
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
    public void startRecord(int level, String xref, String tag) {}

    /**
     * Called when a level-0 record is encountered.
     * This overload includes the record's payload value (if present).
     *
     * <p>The default implementation delegates to
     * {@link #startRecord(int, String, String)} for backward compatibility.
     *
     * @param level  the level number (always 0 for records)
     * @param xref   the cross-reference identifier, or null
     * @param tag    the record tag (e.g., "INDI", "FAM", "SNOTE")
     * @param value  the record's payload value, or null if none
     */
    public void startRecord(int level, String xref, String tag, String value) {
        startRecord(level, xref, tag);
    }

    /** Fires when a record's substructures are complete. */
    public void endRecord(String tag) {}

    /**
     * Fires at each sub-record structure (level &gt; 0, not CONT).
     *
     * @param level     nesting level (1+)
     * @param xref      always null for substructures in GEDCOM 7
     * @param tag       structure tag
     * @param value     assembled payload (after CONT join and
     *                  @@ unescape), or null
     * @param isPointer true if value is a pointer reference
     */
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {}

    /**
     * Fires at each sub-record structure, including the resolved
     * extension tag URI when available via SCHMA mapping.
     *
     * <p>Default implementation delegates to the 5-parameter version,
     * ignoring the URI. Override this method to receive extension URIs.
     *
     * @param level     nesting level (1+)
     * @param xref      always null for substructures in GEDCOM 7
     * @param tag       structure tag
     * @param value     assembled payload, or null
     * @param isPointer true if value is a pointer reference
     * @param uri       resolved URI from SCHMA for extension tags, or null
     */
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer, String uri) {
        startStructure(level, xref, tag, value, isPointer);
    }

    /** Fires when a structure's children are complete. */
    public void endStructure(String tag) {}

    /** Non-fatal issue detected. */
    public void warning(GedcomParseError error) {}

    /** Recoverable error (lenient mode continues). */
    public void error(GedcomParseError error) {}

    /** Unrecoverable error (parsing stops). */
    public void fatalError(GedcomParseError error) {}
}
