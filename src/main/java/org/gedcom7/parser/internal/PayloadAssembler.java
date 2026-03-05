package org.gedcom7.parser.internal;

/**
 * Strategy interface for assembling multi-line payloads
 * from pseudo-structures (CONT in GEDCOM 7, CONT/CONC in 5.5.x).
 *
 * <p>This is an internal interface and not part of the public API.
 */
public interface PayloadAssembler {

    /**
     * Returns true if the given tag is a pseudo-structure that
     * should be consumed for payload assembly rather than
     * emitted as a separate event.
     *
     * @param tag the tag to check
     * @return true if this tag is a continuation pseudo-structure
     */
    boolean isPseudoStructure(String tag);

    /**
     * Appends the continuation value onto the payload buffer.
     * The initial value (from the first line) should already be in the buffer
     * before this method is called for the first continuation line.
     *
     * @param payload the buffer accumulating the payload
     * @param continuationValue the value from the continuation line (may be null)
     * @param tag the pseudo-structure tag (e.g., "CONT" or "CONC")
     */
    void appendPayload(StringBuilder payload, String continuationValue, String tag);
}
