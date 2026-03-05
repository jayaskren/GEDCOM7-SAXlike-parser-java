package org.gedcom7.parser;

/**
 * Unchecked exception thrown when an unrecoverable error
 * is encountered during GEDCOM parsing.
 */
public final class GedcomFatalException extends RuntimeException {

    private final GedcomParseError error;

    public GedcomFatalException(GedcomParseError error) {
        super(error.getMessage());
        this.error = error;
    }

    public GedcomParseError getError() {
        return error;
    }
}
