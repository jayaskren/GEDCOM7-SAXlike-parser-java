package org.gedcom7.parser.internal;

/**
 * Strategy interface for unescaping {@code @@} sequences in
 * GEDCOM line values. GEDCOM 7 only escapes leading {@code @@};
 * GEDCOM 5.5.x escapes all {@code @@} sequences.
 *
 * <p>This is an internal interface and not part of the public API.
 */
public interface AtEscapeStrategy {

    /**
     * Unescapes {@code @@} sequences in the given value
     * according to the version-specific rules.
     *
     * @param value the raw line value (may be null)
     * @return the unescaped value, or null if input was null
     */
    String unescape(String value);
}
