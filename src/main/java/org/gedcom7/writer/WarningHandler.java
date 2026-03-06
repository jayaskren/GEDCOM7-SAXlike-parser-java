package org.gedcom7.writer;

/**
 * Functional interface for receiving warnings during GEDCOM writing.
 *
 * <p>In lenient mode, the writer delivers warnings to the configured
 * handler instead of throwing exceptions. A {@code null} handler
 * suppresses warnings entirely.
 */
@FunctionalInterface
public interface WarningHandler {

    void handle(GedcomWriteWarning warning);
}
