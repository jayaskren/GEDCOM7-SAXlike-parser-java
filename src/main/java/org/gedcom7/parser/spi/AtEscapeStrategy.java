package org.gedcom7.parser.spi;

/**
 * SPI for unescaping {@code @@} sequences in GEDCOM line values.
 * GEDCOM 7 only escapes leading {@code @@}; GEDCOM 5.5.x escapes
 * all {@code @@} sequences.
 *
 * <p>This interface is part of the public SPI. Implementations
 * should be placed in the {@code org.gedcom7.parser.internal}
 * package or in user-provided packages.
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
