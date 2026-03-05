package org.gedcom7.parser.datatype;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for {@link GedcomDataTypes#parseAge(String)}.
 * Covers FR-108: Age ABNF production with optional bound and duration components.
 */
class AgeParserTest {

    @ParameterizedTest(name = "parseAge(\"{0}\") -> modifier={1}, years={2}, months={3}, weeks={4}, days={5}")
    @MethodSource("validAges")
    void validAges(String input, String modifier, int years, int months, int weeks, int days) {
        GedcomAge age = GedcomDataTypes.parseAge(input);
        assertNotNull(age);
        assertEquals(modifier, age.getModifier());
        assertEquals(years, age.getYears());
        assertEquals(months, age.getMonths());
        assertEquals(weeks, age.getWeeks());
        assertEquals(days, age.getDays());
    }

    static Stream<Arguments> validAges() {
        return Stream.of(
                // "> 25y 3m" -> greater than, 25 years 3 months
                Arguments.of("> 25y 3m", ">", 25, 3, -1, -1),
                // "< 1y" -> less than, 1 year
                Arguments.of("< 1y", "<", 1, -1, -1, -1),
                // "3d" -> 3 days, no bound
                Arguments.of("3d", null, -1, -1, -1, 3),
                // "30y" -> 30 years, no bound
                Arguments.of("30y", null, 30, -1, -1, -1),
                // "2y 6m 15d" -> 2 years, 6 months, 15 days
                Arguments.of("2y 6m 15d", null, 2, 6, -1, 15)
        );
    }

    @ParameterizedTest(name = "parseAge(\"{0}\") throws IllegalArgumentException")
    @ValueSource(strings = {"25", "abc", "y", "m3"})
    void invalidAges_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseAge(input));
    }

    @ParameterizedTest(name = "parseAge(nullOrEmpty) throws IllegalArgumentException")
    @NullAndEmptySource
    void nullAndEmpty_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseAge(input));
    }
}
