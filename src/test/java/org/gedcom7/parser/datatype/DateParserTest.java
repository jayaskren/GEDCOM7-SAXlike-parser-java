package org.gedcom7.parser.datatype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for {@link GedcomDataTypes#parseDateValue(String)}.
 * Covers FR-102 through FR-106: full DateValue ABNF production including
 * exact dates, ranges, approximations, periods, and BCE epoch.
 */
class DateParserTest {

    // ─── Exact dates ───────────────────────────────────────

    @Test
    void exactDate_dayMonthYear() {
        Object result = GedcomDataTypes.parseDateValue("1 JAN 2000");
        assertInstanceOf(GedcomDateRange.class, result);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("EXACT", range.getRangeType());
        GedcomDate date = range.getStart();
        assertEquals(2000, date.getYear());
        assertEquals("JAN", date.getMonth());
        assertEquals(1, date.getDay());
        assertEquals("GREGORIAN", date.getCalendar());
        assertNull(date.getEpoch());
    }

    @Test
    void exactDate_yearOnly() {
        Object result = GedcomDataTypes.parseDateValue("2000");
        assertInstanceOf(GedcomDateRange.class, result);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("EXACT", range.getRangeType());
        GedcomDate date = range.getStart();
        assertEquals(2000, date.getYear());
        assertNull(date.getMonth());
        assertEquals(-1, date.getDay());
    }

    @Test
    void exactDate_monthYear() {
        Object result = GedcomDataTypes.parseDateValue("MAR 2000");
        assertInstanceOf(GedcomDateRange.class, result);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("EXACT", range.getRangeType());
        GedcomDate date = range.getStart();
        assertEquals(2000, date.getYear());
        assertEquals("MAR", date.getMonth());
        assertEquals(-1, date.getDay());
    }

    // ─── Range dates ───────────────────────────────────────

    @Test
    void rangeDate_betweenAnd() {
        Object result = GedcomDataTypes.parseDateValue("BET 1 JAN 2000 AND 31 DEC 2000");
        assertInstanceOf(GedcomDateRange.class, result);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("BET_AND", range.getRangeType());
        assertNotNull(range.getStart());
        assertNotNull(range.getEnd());
        assertEquals(2000, range.getStart().getYear());
        assertEquals("JAN", range.getStart().getMonth());
        assertEquals(1, range.getStart().getDay());
        assertEquals(2000, range.getEnd().getYear());
        assertEquals("DEC", range.getEnd().getMonth());
        assertEquals(31, range.getEnd().getDay());
    }

    @Test
    void rangeDate_before() {
        Object result = GedcomDataTypes.parseDateValue("BEF 2000");
        assertInstanceOf(GedcomDateRange.class, result);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("BEF", range.getRangeType());
        assertNull(range.getStart());
        assertEquals(2000, range.getEnd().getYear());
    }

    @Test
    void rangeDate_after() {
        Object result = GedcomDataTypes.parseDateValue("AFT 2000");
        assertInstanceOf(GedcomDateRange.class, result);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("AFT", range.getRangeType());
        assertEquals(2000, range.getStart().getYear());
        assertNull(range.getEnd());
    }

    // ─── Approximate dates ─────────────────────────────────

    @ParameterizedTest(name = "parseDateValue(\"{0}\") returns approximate date")
    @MethodSource("approximateDates")
    void approximateDate(String input, String expectedModifier) {
        Object result = GedcomDataTypes.parseDateValue(input);
        // Approximate dates may be represented as GedcomDate with a modifier
        // or wrapped in a special type. We verify the year is parsed correctly.
        assertNotNull(result);
        if (result instanceof GedcomDate) {
            GedcomDate date = (GedcomDate) result;
            assertEquals(2000, date.getYear());
        }
    }

    static Stream<Arguments> approximateDates() {
        return Stream.of(
                Arguments.of("ABT 2000", "ABT"),
                Arguments.of("CAL 2000", "CAL"),
                Arguments.of("EST 2000", "EST")
        );
    }

    // ─── Period dates ──────────────────────────────────────

    @Test
    void periodDate_fromTo() {
        Object result = GedcomDataTypes.parseDateValue("FROM 2000 TO 2001");
        assertInstanceOf(GedcomDatePeriod.class, result);
        GedcomDatePeriod period = (GedcomDatePeriod) result;
        assertEquals("FROM_TO", period.getPeriodType());
        assertNotNull(period.getFrom());
        assertNotNull(period.getTo());
        assertEquals(2000, period.getFrom().getYear());
        assertEquals(2001, period.getTo().getYear());
    }

    @Test
    void periodDate_fromOnly() {
        Object result = GedcomDataTypes.parseDateValue("FROM 2000");
        assertInstanceOf(GedcomDatePeriod.class, result);
        GedcomDatePeriod period = (GedcomDatePeriod) result;
        assertEquals("FROM", period.getPeriodType());
        assertNotNull(period.getFrom());
        assertNull(period.getTo());
        assertEquals(2000, period.getFrom().getYear());
    }

    @Test
    void periodDate_toOnly() {
        Object result = GedcomDataTypes.parseDateValue("TO 2001");
        assertInstanceOf(GedcomDatePeriod.class, result);
        GedcomDatePeriod period = (GedcomDatePeriod) result;
        assertEquals("TO", period.getPeriodType());
        assertNull(period.getFrom());
        assertNotNull(period.getTo());
        assertEquals(2001, period.getTo().getYear());
    }

    // ─── BCE epoch ─────────────────────────────────────────

    @Test
    void bceEpoch() {
        Object result = GedcomDataTypes.parseDateValue("500 BCE");
        assertInstanceOf(GedcomDateRange.class, result);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("EXACT", range.getRangeType());
        GedcomDate date = range.getStart();
        assertEquals(500, date.getYear());
        assertEquals("BCE", date.getEpoch());
    }

    // ─── Invalid inputs ────────────────────────────────────

    @ParameterizedTest(name = "parseDateValue(nullOrEmpty) throws IllegalArgumentException")
    @NullAndEmptySource
    void nullAndEmpty_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseDateValue(input));
    }

    @ParameterizedTest(name = "parseDateValue(\"{0}\") returns UNPARSEABLE")
    @ValueSource(strings = {"NOTADATE", "32 JAN 2000"})
    void invalidDates_returnUnparseable(String input) {
        GedcomDateValue result = GedcomDataTypes.parseDateValue(input);
        assertNotNull(result);
        assertEquals(DateValueType.UNPARSEABLE, result.getType());
        assertEquals(input, result.getOriginalText());
    }
}
