package org.gedcom7.writer.date;

/**
 * Gregorian calendar months with GEDCOM abbreviations and maximum day counts.
 */
public enum Month {
    JAN(31), FEB(28), MAR(31), APR(30), MAY(31), JUN(30),
    JUL(31), AUG(31), SEP(30), OCT(31), NOV(30), DEC(31);

    private final int maxDay;

    Month(int maxDay) {
        this.maxDay = maxDay;
    }

    /**
     * Returns the GEDCOM three-letter abbreviation for this month.
     */
    public String abbreviation() {
        return name();
    }

    /**
     * Returns the maximum day number for this month (not accounting for leap years).
     */
    public int maxDay() {
        return maxDay;
    }
}
