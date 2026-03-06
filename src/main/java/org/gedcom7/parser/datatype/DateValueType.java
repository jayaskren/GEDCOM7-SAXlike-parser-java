package org.gedcom7.parser.datatype;

/**
 * Discriminator for GEDCOM date value types returned by
 * {@link GedcomDataTypes#parseDateValue(String)}.
 */
public enum DateValueType {
    /** A specific date (e.g., "6 APR 1952"). */
    EXACT,
    /** A date range: BET...AND, BEF, AFT (per GEDCOM 7 DateRange production). */
    RANGE,
    /** A date period (e.g., "FROM 1900 TO 1910"). */
    PERIOD,
    /** An approximate date: ABT, CAL, EST. */
    APPROXIMATE,
    /** A date string that could not be parsed. */
    UNPARSEABLE
}
