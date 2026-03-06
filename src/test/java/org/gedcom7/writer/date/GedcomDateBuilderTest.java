package org.gedcom7.writer.date;

import org.gedcom7.parser.GedcomVersion;
import org.junit.jupiter.api.Test;

import static org.gedcom7.writer.date.GedcomDateBuilder.*;
import static org.gedcom7.writer.date.Month.*;
import static org.gedcom7.writer.date.HebrewMonth.*;
import static org.gedcom7.writer.date.FrenchRepublicanMonth.*;
import static org.junit.jupiter.api.Assertions.*;

class GedcomDateBuilderTest {

    private static final GedcomVersion GEDCOM_7 = new GedcomVersion(7, 0);
    private static final GedcomVersion V5_5_5 = new GedcomVersion(5, 5, 5);

    // --- Exact dates ---

    @Test
    void exactDayMonthYear() {
        assertEquals("15 MAR 1955", date(15, MAR, 1955).toGedcomString(GEDCOM_7));
    }

    @Test
    void exactMonthYear() {
        assertEquals("MAR 1955", date(MAR, 1955).toGedcomString(GEDCOM_7));
    }

    @Test
    void exactYearOnly() {
        assertEquals("1955", date(1955).toGedcomString(GEDCOM_7));
    }

    // --- BCE dates ---

    @Test
    void bceYear() {
        assertEquals("44 BCE", dateBce(44).toGedcomString(GEDCOM_7));
    }

    @Test
    void bceDayMonthYear() {
        assertEquals("15 MAR 44 BCE", dateBce(15, MAR, 44).toGedcomString(GEDCOM_7));
    }

    // --- Approximate dates ---

    @Test
    void aboutYear() {
        assertEquals("ABT 1880", about(1880).toGedcomString(GEDCOM_7));
    }

    @Test
    void aboutMonthYear() {
        assertEquals("ABT MAR 1880", about(MAR, 1880).toGedcomString(GEDCOM_7));
    }

    @Test
    void calculatedDate() {
        assertEquals("CAL MAR 1880", calculated(date(MAR, 1880)).toGedcomString(GEDCOM_7));
    }

    @Test
    void estimatedDate() {
        assertEquals("EST 1750", estimated(date(1750)).toGedcomString(GEDCOM_7));
    }

    // --- Ranges ---

    @Test
    void beforeYear() {
        assertEquals("BEF 1900", before(1900).toGedcomString(GEDCOM_7));
    }

    @Test
    void beforeMonthYear() {
        assertEquals("BEF JUN 1900", before(JUN, 1900).toGedcomString(GEDCOM_7));
    }

    @Test
    void afterYear() {
        assertEquals("AFT 1850", after(1850).toGedcomString(GEDCOM_7));
    }

    @Test
    void betweenYears() {
        assertEquals("BET 1880 AND 1890",
                between(date(1880), date(1890)).toGedcomString(GEDCOM_7));
    }

    // --- Periods ---

    @Test
    void fromDate() {
        assertEquals("FROM 1 JAN 1940", from(date(1, JAN, 1940)).toGedcomString(GEDCOM_7));
    }

    @Test
    void toDate() {
        assertEquals("TO 31 DEC 1945", to(date(31, DEC, 1945)).toGedcomString(GEDCOM_7));
    }

    @Test
    void fromToDate() {
        assertEquals("FROM 1 JAN 1940 TO 31 DEC 1945",
                fromTo(date(1, JAN, 1940), date(31, DEC, 1945)).toGedcomString(GEDCOM_7));
    }

    // --- Non-Gregorian: Julian ---

    @Test
    void julianDateGedcom7() {
        assertEquals("JULIAN 1 JAN 1700", julian(1, JAN, 1700).toGedcomString(GEDCOM_7));
    }

    @Test
    void julianDate555() {
        assertEquals("@#DJULIAN@ 1 JAN 1700", julian(1, JAN, 1700).toGedcomString(V5_5_5));
    }

    // --- Non-Gregorian: Hebrew ---

    @Test
    void hebrewDateGedcom7() {
        assertEquals("HEBREW 15 NSN 5765", hebrew(15, NSN, 5765).toGedcomString(GEDCOM_7));
    }

    @Test
    void hebrewDate555() {
        assertEquals("@#DHEBREW@ 15 NSN 5765", hebrew(15, NSN, 5765).toGedcomString(V5_5_5));
    }

    // --- Non-Gregorian: French Republican ---

    @Test
    void frenchRepublicanGedcom7() {
        assertEquals("FRENCH_R 1 VEND 1", frenchRepublican(1, VEND, 1).toGedcomString(GEDCOM_7));
    }

    @Test
    void frenchRepublican555() {
        assertEquals("@#DFRENCH R@ 1 VEND 1", frenchRepublican(1, VEND, 1).toGedcomString(V5_5_5));
    }

    // --- Raw escape hatch ---

    @Test
    void rawPassesThrough() {
        assertEquals("BET JULIAN 1 JAN 1700 AND JULIAN 31 DEC 1710",
                WriterDate.raw("BET JULIAN 1 JAN 1700 AND JULIAN 31 DEC 1710").toGedcomString(GEDCOM_7));
    }
}
