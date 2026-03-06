package org.gedcom7.writer;

/**
 * Exception thrown in strict mode when the writer detects
 * an issue that would produce invalid or non-standard GEDCOM output.
 *
 * <p>This is an unchecked exception (extends {@link RuntimeException})
 * to allow clean use with lambda-based {@link java.util.function.Consumer}
 * APIs without requiring try/catch blocks.
 */
public class GedcomWriteException extends RuntimeException {

    public GedcomWriteException(String message) {
        super(message);
    }

    public GedcomWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
