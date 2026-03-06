package org.gedcom7.writer.date;

import org.junit.jupiter.api.Test;

import static org.gedcom7.writer.date.GedcomDateBuilder.*;
import static org.gedcom7.writer.date.Month.*;
import static org.junit.jupiter.api.Assertions.*;

class DateValidationTest {

    @Test
    void dayOverMaxRejects() {
        assertThrows(IllegalArgumentException.class, () -> date(32, JAN, 1955));
    }

    @Test
    void dayZeroRejects() {
        assertThrows(IllegalArgumentException.class, () -> date(0, JAN, 1955));
    }

    @Test
    void yearZeroRejects() {
        assertThrows(IllegalArgumentException.class, () -> date(0));
    }

    @Test
    void dayOver28ForFebRejects() {
        assertThrows(IllegalArgumentException.class, () -> date(29, FEB, 1900));
    }

    @Test
    void day30ForAprilOk() {
        assertDoesNotThrow(() -> date(30, APR, 1900));
    }

    @Test
    void day31ForAprilRejects() {
        assertThrows(IllegalArgumentException.class, () -> date(31, APR, 1900));
    }

    @Test
    void negativeYearRejects() {
        assertThrows(IllegalArgumentException.class, () -> date(-1));
    }

    @Test
    void betweenReversedDatesRejects() {
        assertThrows(IllegalArgumentException.class, () -> between(date(1890), date(1880)));
    }

    @Test
    void betweenSameDateOk() {
        assertDoesNotThrow(() -> between(date(1880), date(1880)));
    }

    @Test
    void fromToReversedDatesRejects() {
        assertThrows(IllegalArgumentException.class, () -> fromTo(date(1890), date(1880)));
    }

    @Test
    void betweenChronologicalOk() {
        assertDoesNotThrow(() -> between(date(1880), date(1890)));
    }
}
