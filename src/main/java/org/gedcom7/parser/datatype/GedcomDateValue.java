package org.gedcom7.parser.datatype;

/**
 * Common interface for all parsed GEDCOM date value representations.
 *
 * <p>Implementations include exact dates ({@link GedcomDate}),
 * date ranges ({@link GedcomDateRange}), date periods
 * ({@link GedcomDatePeriod}), and approximate dates.
 *
 * <p>Use {@link #getType()} to determine the date value category
 * without {@code instanceof} checks.
 */
public interface GedcomDateValue {

    /**
     * Returns the type of this date value.
     *
     * @return the date value type, never null
     */
    DateValueType getType();

    /**
     * Returns the original unparsed date text.
     *
     * @return the original date string as it appeared in the GEDCOM data
     */
    String getOriginalText();
}
