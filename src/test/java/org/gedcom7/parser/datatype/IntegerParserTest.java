package org.gedcom7.parser.datatype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for {@link GedcomDataTypes#parseInteger(String)}.
 * Covers FR-101: non-negative base-10 integers per the Integer ABNF production.
 */
class IntegerParserTest {

    @ParameterizedTest(name = "parseInteger(\"{0}\") == {1}")
    @CsvSource({
            "0, 0",
            "42, 42",
            "999, 999",
            "1, 1",
            "100, 100"
    })
    void validIntegers(String input, int expected) {
        assertEquals(expected, GedcomDataTypes.parseInteger(input));
    }

    @ParameterizedTest(name = "parseInteger(\"{0}\") throws IllegalArgumentException")
    @ValueSource(strings = {"-5", "007", "abc", "1.5", "00", " 42", "42 "})
    void invalidIntegers_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseInteger(input));
    }

    @ParameterizedTest(name = "parseInteger(nullOrEmpty) throws IllegalArgumentException")
    @NullAndEmptySource
    void nullAndEmpty_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseInteger(input));
    }

    @Test
    void leadingZero_rejected() {
        // "0" itself is valid, but "01" has a leading zero
        assertEquals(0, GedcomDataTypes.parseInteger("0"));
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseInteger("01"));
    }
}
