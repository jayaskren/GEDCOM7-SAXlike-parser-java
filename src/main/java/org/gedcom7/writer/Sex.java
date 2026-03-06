package org.gedcom7.writer;

/**
 * Enum representing the sex values defined by the GEDCOM specification.
 *
 * <p>Each constant maps to the single-character code used in GEDCOM
 * {@code SEX} structures (e.g., {@code 1 SEX M}).
 */
public enum Sex {

    MALE("M"),
    FEMALE("F"),
    INTERSEX("X"),
    UNKNOWN("U");

    private final String code;

    Sex(String code) {
        this.code = code;
    }

    /**
     * Returns the single-character GEDCOM code for this sex value.
     */
    public String getCode() {
        return code;
    }
}
