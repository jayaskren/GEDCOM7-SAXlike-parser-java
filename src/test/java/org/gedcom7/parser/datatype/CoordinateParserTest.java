package org.gedcom7.parser.datatype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for {@link GedcomDataTypes#parseLatitude(String)}
 * and {@link GedcomDataTypes#parseLongitude(String)}.
 * Covers FR-110 (latitude N|S + 0-90) and FR-111 (longitude E|W + 0-180).
 */
class CoordinateParserTest {

    // ─── parseLatitude ─────────────────────────────────────

    @ParameterizedTest(name = "parseLatitude(\"{0}\") -> degrees={1}, direction={2}")
    @CsvSource({
            "N50.1234, 50.1234, N",
            "S33.8688, 33.8688, S",
            "N0, 0.0, N",
            "S90, 90.0, S"
    })
    void validLatitudes(String input, double degrees, char direction) {
        GedcomCoordinate coord = GedcomDataTypes.parseLatitude(input);
        assertNotNull(coord);
        assertEquals(degrees, coord.getValue(), 0.0001);
        assertEquals(direction, coord.getDirection());
    }

    @ParameterizedTest(name = "parseLatitude(\"{0}\") throws IllegalArgumentException")
    @ValueSource(strings = {"E50", "W50", "X50", "50.0", "N"})
    void invalidLatitudes_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseLatitude(input));
    }

    @ParameterizedTest(name = "parseLatitude(nullOrEmpty) throws IllegalArgumentException")
    @NullAndEmptySource
    void latitudeNullAndEmpty_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseLatitude(input));
    }

    // ─── parseLongitude ────────────────────────────────────

    @ParameterizedTest(name = "parseLongitude(\"{0}\") -> degrees={1}, direction={2}")
    @CsvSource({
            "E151.2093, 151.2093, E",
            "W122.4194, 122.4194, W",
            "E0, 0.0, E",
            "W180, 180.0, W"
    })
    void validLongitudes(String input, double degrees, char direction) {
        GedcomCoordinate coord = GedcomDataTypes.parseLongitude(input);
        assertNotNull(coord);
        assertEquals(degrees, coord.getValue(), 0.0001);
        assertEquals(direction, coord.getDirection());
    }

    @ParameterizedTest(name = "parseLongitude(\"{0}\") throws IllegalArgumentException")
    @ValueSource(strings = {"N50", "S50", "X50", "50.0", "E"})
    void invalidLongitudes_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseLongitude(input));
    }

    @ParameterizedTest(name = "parseLongitude(nullOrEmpty) throws IllegalArgumentException")
    @NullAndEmptySource
    void longitudeNullAndEmpty_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseLongitude(input));
    }

    // ─── General invalid direction tests ───────────────────

    @Test
    void invalidDirection_noLetter() {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseLatitude("50.0"));
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parseLongitude("50.0"));
    }
}
