package org.gedcom7.parser;

import java.util.Objects;

/**
 * Describes a warning, error, or fatal error encountered during parsing.
 *
 * <p>Instances are immutable.
 */
public final class GedcomParseError {

    public enum Severity {
        WARNING, ERROR, FATAL
    }

    private final Severity severity;
    private final int lineNumber;
    private final long byteOffset;
    private final String message;
    private final String rawLine;

    public GedcomParseError(Severity severity, int lineNumber, long byteOffset,
                            String message, String rawLine) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.lineNumber = lineNumber;
        this.byteOffset = byteOffset;
        this.message = Objects.requireNonNull(message, "message");
        this.rawLine = rawLine;
    }

    public Severity getSeverity() { return severity; }
    public int getLineNumber() { return lineNumber; }
    public long getByteOffset() { return byteOffset; }
    public String getMessage() { return message; }
    public String getRawLine() { return rawLine; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomParseError)) return false;
        GedcomParseError that = (GedcomParseError) o;
        return severity == that.severity
                && lineNumber == that.lineNumber
                && byteOffset == that.byteOffset
                && message.equals(that.message)
                && Objects.equals(rawLine, that.rawLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, lineNumber, byteOffset, message, rawLine);
    }

    @Override
    public String toString() {
        return severity + " at line " + lineNumber + " (byte " + byteOffset + "): " + message;
    }
}
