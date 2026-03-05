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
     * Assembles the continuation value onto the existing payload.
     *
     * @param existing the current payload (may be null)
     * @param continuationValue the value from the continuation line (may be null)
     * @return the assembled payload
     */
    String assemblePayload(String existing, String continuationValue);
}
