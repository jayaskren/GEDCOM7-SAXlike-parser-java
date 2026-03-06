package org.gedcom7.parser.datatype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link GedcomDateValue} interface contract across all
 * date value types returned by {@link GedcomDataTypes#parseDateValue(String)}.
 */
class GedcomDateValueTest {

    // ---- RANGE type ----

    @Test
    void betweenRange_returnsRangeType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("BET 1 JAN 1900 AND 31 DEC 1900");
        assertEquals(DateValueType.RANGE, result.getType());
        assertInstanceOf(GedcomDateRange.class, result);
    }

    @Test
    void before_returnsRangeType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("BEF 1900");
        assertEquals(DateValueType.RANGE, result.getType());
        assertInstanceOf(GedcomDateRange.class, result);
    }

    @Test
    void after_returnsRangeType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("AFT 1900");
        assertEquals(DateValueType.RANGE, result.getType());
        assertInstanceOf(GedcomDateRange.class, result);
    }

    // ---- PERIOD type ----

    @Test
    void fromTo_returnsPeriodType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("FROM 1 JAN 1900 TO 31 DEC 1900");
        assertEquals(DateValueType.PERIOD, result.getType());
        assertInstanceOf(GedcomDatePeriod.class, result);
    }

    // ---- EXACT type (exact dates are wrapped in GedcomDateRange with rangeType "EXACT") ----

    @Test
    void exactDate_returnsExactType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("6 APR 1952");
        assertNotNull(result);
        // Exact dates parsed via parseDateValue are wrapped in GedcomDateRange with rangeType "EXACT"
        assertInstanceOf(GedcomDateRange.class, result);
        assertEquals(DateValueType.EXACT, result.getType());
    }

    // ---- APPROXIMATE type ----

    @Test
    void abt_returnsApproximateType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("ABT 1900");
        assertEquals(DateValueType.APPROXIMATE, result.getType());
        assertInstanceOf(GedcomDateRange.class, result);
    }

    @Test
    void cal_returnsApproximateType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("CAL 1900");
        assertEquals(DateValueType.APPROXIMATE, result.getType());
    }

    @Test
    void est_returnsApproximateType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("EST 1900");
        assertEquals(DateValueType.APPROXIMATE, result.getType());
    }

    // ---- getOriginalText() ----

    @Test
    void getOriginalText_returnsInputString() {
        String input = "BET 1 JAN 1900 AND 31 DEC 1900";
        GedcomDateValue result = GedcomDataTypes.parseDateValue(input);
        assertEquals(input, result.getOriginalText());
    }

    @Test
    void getOriginalText_exactDate() {
        String input = "6 APR 1952";
        GedcomDateValue result = GedcomDataTypes.parseDateValue(input);
        assertEquals(input, result.getOriginalText());
    }

    @Test
    void getOriginalText_period() {
        String input = "FROM 1 JAN 1900 TO 31 DEC 1900";
        GedcomDateValue result = GedcomDataTypes.parseDateValue(input);
        assertEquals(input, result.getOriginalText());
    }

    @Test
    void getOriginalText_approximate() {
        String input = "ABT 1900";
        GedcomDateValue result = GedcomDataTypes.parseDateValue(input);
        assertEquals(input, result.getOriginalText());
    }

    // ---- instanceof checks still work ----

    @Test
    void instanceofGedcomDateRange_stillWorks() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("BET 1 JAN 1900 AND 31 DEC 1900");
        assertTrue(result instanceof GedcomDateRange);
        GedcomDateRange range = (GedcomDateRange) result;
        assertEquals("BET_AND", range.getRangeType());
        assertNotNull(range.getStart());
        assertNotNull(range.getEnd());
    }

    @Test
    void instanceofGedcomDatePeriod_stillWorks() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("FROM 1 JAN 1900 TO 31 DEC 1900");
        assertTrue(result instanceof GedcomDatePeriod);
        GedcomDatePeriod period = (GedcomDatePeriod) result;
        assertEquals("FROM_TO", period.getPeriodType());
        assertNotNull(period.getFrom());
        assertNotNull(period.getTo());
    }

    // ---- UNPARSEABLE type ----

    @Test
    void unparseable_returnsUnparseableType() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("INVALID GARBAGE");
        assertNotNull(result, "Unparseable input should not return null");
        assertEquals(DateValueType.UNPARSEABLE, result.getType());
    }

    @Test
    void unparseable_preservesOriginalText() {
        String input = "INVALID GARBAGE";
        GedcomDateValue result = GedcomDataTypes.parseDateValue(input);
        assertEquals(input, result.getOriginalText());
    }

    @Test
    void unparseable_doesNotThrowException() {
        // Should not throw -- returns UNPARSEABLE instead
        GedcomDateValue result = GedcomDataTypes.parseDateValue("XYZZY 99 BLAH");
        assertNotNull(result);
        assertEquals(DateValueType.UNPARSEABLE, result.getType());
    }

    @Test
    void unparseable_notInstanceOfConcreteTypes() {
        GedcomDateValue result = GedcomDataTypes.parseDateValue("INVALID GARBAGE");
        assertFalse(result instanceof GedcomDate);
        assertFalse(result instanceof GedcomDateRange);
        assertFalse(result instanceof GedcomDatePeriod);
    }
}
