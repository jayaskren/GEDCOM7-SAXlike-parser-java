package org.gedcom7.writer.date;

/**
 * French Republican calendar months with GEDCOM abbreviations.
 */
public enum FrenchRepublicanMonth {
    VEND, BRUM, FRIM, NIVO, PLUV, VENT, GERM, FLOR, PRAI, MESS, THER, FRUC, COMP;

    /**
     * Returns the GEDCOM abbreviation for this French Republican month.
     */
    public String abbreviation() {
        return name();
    }
}
