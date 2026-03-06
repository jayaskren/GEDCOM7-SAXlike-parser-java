package org.gedcom7.writer.date;

/**
 * Hebrew calendar months with GEDCOM abbreviations.
 */
public enum HebrewMonth {
    TSH, CSH, KSL, TVT, SHV, ADR, ADS, NSN, IYR, SVN, TMZ, AAV, ELL;

    /**
     * Returns the GEDCOM three-letter abbreviation for this Hebrew month.
     */
    public String abbreviation() {
        return name();
    }
}
