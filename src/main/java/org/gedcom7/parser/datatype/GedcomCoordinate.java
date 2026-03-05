package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 geographic coordinate.
 * A coordinate consists of a numeric value and a direction character (N, S, E, or W).
 */
public final class GedcomCoordinate {

    private final double value;
    private final char direction;

    /**
     * Constructs a new GedcomCoordinate.
     *
     * @param value     the numeric coordinate value
     * @param direction the direction character: 'N', 'S', 'E', or 'W'
     */
    public GedcomCoordinate(double value, char direction) {
        this.value = value;
        this.direction = direction;
    }

    public double getValue() {
        return value;
    }

    public char getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomCoordinate)) return false;
        GedcomCoordinate that = (GedcomCoordinate) o;
        return Double.compare(that.value, value) == 0
                && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, direction);
    }

    @Override
    public String toString() {
        return "GedcomCoordinate{" +
                "value=" + value +
                ", direction=" + direction +
                '}';
    }
}
