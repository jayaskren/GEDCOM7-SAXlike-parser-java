package org.gedcom7.writer.date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonthEnumTest {

    @Test
    void monthAbbreviations() {
        assertEquals("JAN", Month.JAN.abbreviation());
        assertEquals("FEB", Month.FEB.abbreviation());
        assertEquals("MAR", Month.MAR.abbreviation());
        assertEquals("APR", Month.APR.abbreviation());
        assertEquals("MAY", Month.MAY.abbreviation());
        assertEquals("JUN", Month.JUN.abbreviation());
        assertEquals("JUL", Month.JUL.abbreviation());
        assertEquals("AUG", Month.AUG.abbreviation());
        assertEquals("SEP", Month.SEP.abbreviation());
        assertEquals("OCT", Month.OCT.abbreviation());
        assertEquals("NOV", Month.NOV.abbreviation());
        assertEquals("DEC", Month.DEC.abbreviation());
    }

    @Test
    void monthMaxDays() {
        assertEquals(31, Month.JAN.maxDay());
        assertEquals(28, Month.FEB.maxDay());
        assertEquals(31, Month.MAR.maxDay());
        assertEquals(30, Month.APR.maxDay());
        assertEquals(31, Month.MAY.maxDay());
        assertEquals(30, Month.JUN.maxDay());
        assertEquals(31, Month.JUL.maxDay());
        assertEquals(31, Month.AUG.maxDay());
        assertEquals(30, Month.SEP.maxDay());
        assertEquals(31, Month.OCT.maxDay());
        assertEquals(30, Month.NOV.maxDay());
        assertEquals(31, Month.DEC.maxDay());
    }

    @Test
    void hebrewMonthAbbreviations() {
        assertEquals("TSH", HebrewMonth.TSH.abbreviation());
        assertEquals("CSH", HebrewMonth.CSH.abbreviation());
        assertEquals("KSL", HebrewMonth.KSL.abbreviation());
        assertEquals("TVT", HebrewMonth.TVT.abbreviation());
        assertEquals("SHV", HebrewMonth.SHV.abbreviation());
        assertEquals("ADR", HebrewMonth.ADR.abbreviation());
        assertEquals("ADS", HebrewMonth.ADS.abbreviation());
        assertEquals("NSN", HebrewMonth.NSN.abbreviation());
        assertEquals("IYR", HebrewMonth.IYR.abbreviation());
        assertEquals("SVN", HebrewMonth.SVN.abbreviation());
        assertEquals("TMZ", HebrewMonth.TMZ.abbreviation());
        assertEquals("AAV", HebrewMonth.AAV.abbreviation());
        assertEquals("ELL", HebrewMonth.ELL.abbreviation());
    }

    @Test
    void hebrewMonthCount() {
        assertEquals(13, HebrewMonth.values().length);
    }

    @Test
    void frenchRepublicanAbbreviations() {
        assertEquals("VEND", FrenchRepublicanMonth.VEND.abbreviation());
        assertEquals("BRUM", FrenchRepublicanMonth.BRUM.abbreviation());
        assertEquals("FRIM", FrenchRepublicanMonth.FRIM.abbreviation());
        assertEquals("NIVO", FrenchRepublicanMonth.NIVO.abbreviation());
        assertEquals("PLUV", FrenchRepublicanMonth.PLUV.abbreviation());
        assertEquals("VENT", FrenchRepublicanMonth.VENT.abbreviation());
        assertEquals("GERM", FrenchRepublicanMonth.GERM.abbreviation());
        assertEquals("FLOR", FrenchRepublicanMonth.FLOR.abbreviation());
        assertEquals("PRAI", FrenchRepublicanMonth.PRAI.abbreviation());
        assertEquals("MESS", FrenchRepublicanMonth.MESS.abbreviation());
        assertEquals("THER", FrenchRepublicanMonth.THER.abbreviation());
        assertEquals("FRUC", FrenchRepublicanMonth.FRUC.abbreviation());
        assertEquals("COMP", FrenchRepublicanMonth.COMP.abbreviation());
    }

    @Test
    void frenchRepublicanMonthCount() {
        assertEquals(13, FrenchRepublicanMonth.values().length);
    }
}
