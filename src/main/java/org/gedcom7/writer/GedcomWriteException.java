package org.gedcom7.writer;

/**
 * Checked exception thrown in strict mode when the writer detects
 * an issue that would produce invalid or non-standard GEDCOM output.
 */
public class GedcomWriteException extends Exception {

    public GedcomWriteException(String message) {
        super(message);
    }

    public GedcomWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
