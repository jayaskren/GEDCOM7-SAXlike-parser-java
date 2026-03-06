package org.gedcom7.writer;

/**
 * Structured warning value object delivered to a {@link WarningHandler}
 * in lenient mode.
 */
public final class GedcomWriteWarning {

    private final String message;
    private final String tag;

    public GedcomWriteWarning(String message, String tag) {
        this.message = message;
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return tag != null
                ? "GedcomWriteWarning[tag=" + tag + ", message=" + message + "]"
                : "GedcomWriteWarning[message=" + message + "]";
    }
}
