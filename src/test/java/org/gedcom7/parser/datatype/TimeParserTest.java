package org.gedcom7.parser.datatype;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for {@link GedcomDataTypes#parseTime(String)}.
 * Covers FR-107: Time ABNF production hour:minute[:second[.fraction]][Z].
 */
class TimeParserTest {

    @ParameterizedTest(name = "parseTime(\"{0}\") -> hour={1}, minute={2}, second={3}, milliseconds={4}, utc={5}")
    @MethodSource("validTimes")
    void validTimes(String input, int hour, int minute, int second,
                    int milliseconds, boolean utc) {
        GedcomTime time = GedcomDataTypes.parseTime(input);
        assertNotNull(time);
        assertEquals(hour, time.getHours());
        assertEquals(minute, time.getMinutes());
        assertEquals(second, time.getSeconds());
        assertEquals(milliseconds, time.getMilliseconds());
        assertEquals(utc, time.isUtc());
    }

    static Stream<Arguments> validTimes() {
        return Stream.of(
                // "12:30:00Z" -> utc time
                Arguments.of("12:30:00Z", 12, 30, 0, -1, true),
                // "00:00" -> midnight, no seconds
                Arguments.of("00:00", 0, 0, -1, -1, false),
                // "23:59:59.999" -> with fractional seconds
                Arguments.of("23:59:59.999", 23, 59, 59, 999, false),
                // "14:30" -> simple time
                Arguments.of("14:30", 14, 30, -1, -1, false),
                // "01:02:03" -> with seconds, no fraction, no Z
                Arguments.of("01:02:03", 1, 2, 3, -1, false)
        );
    }

    @ParameterizedTest(name = "parseTime(\"{0}\") throws IllegalArgumentException")
    @ValueSource(strings = {"24:00:00", "12:60", "25:00", "-1:00", "12:00:60",
            "abc", "12", "12:"})
    void invalidTimes_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseTime(input));
    }

    @ParameterizedTest(name = "parseTime(nullOrEmpty) throws IllegalArgumentException")
    @NullAndEmptySource
    void nullAndEmpty_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseTime(input));
    }
}
