package org.gedcom7.converter;

/**
 * Describes a warning encountered during GEDCOM version conversion.
 *
 * <p>Conversion warnings indicate structures or values that may lose
 * fidelity when converted between GEDCOM versions.
 */
public final class ConversionWarning {

    private final String message;
    private final String tag;
    private final int lineNumber;

    /**
     * Creates a conversion warning.
     *
     * @param message    human-readable description of the warning
     * @param tag        the GEDCOM tag that triggered the warning, or {@code null}
     * @param lineNumber the source line number, or 0 if unknown
     */
    public ConversionWarning(String message, String tag, int lineNumber) {
        this.message = message;
        this.tag = tag;
        this.lineNumber = lineNumber;
    }

    /** Returns the human-readable warning description. */
    public String getMessage() {
        return message;
    }

    /** Returns the GEDCOM tag that triggered the warning, or {@code null}. */
    public String getTag() {
        return tag;
    }

    /** Returns the source line number, or 0 if unknown. */
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ConversionWarning{");
        if (tag != null) {
            sb.append("tag=").append(tag).append(", ");
        }
        if (lineNumber > 0) {
            sb.append("line=").append(lineNumber).append(", ");
        }
        sb.append("message='").append(message).append("'}");
        return sb.toString();
    }
}
