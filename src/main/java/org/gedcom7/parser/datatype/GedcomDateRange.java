package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 date range.
 * Covers range types such as BET...AND, BEF, AFT, and approximate types ABT, CAL, EST,
 * as well as EXACT dates.
 */
public final class GedcomDateRange {

    private final GedcomDate start;
    private final GedcomDate end;
    private final String type;

    /**
     * Constructs a new GedcomDateRange.
     *
     * @param start the start date, or null if absent (e.g. for BEF)
     * @param end   the end date, or null if absent (e.g. for AFT)
     * @param type  the range type: "BET_AND", "BEF", "AFT", "ABT", "CAL", "EST", or "EXACT"
     */
    public GedcomDateRange(GedcomDate start, GedcomDate end, String type) {
        this.start = start;
        this.end = end;
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public GedcomDate getStart() {
        return start;
    }

    public GedcomDate getEnd() {
        return end;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomDateRange)) return false;
        GedcomDateRange that = (GedcomDateRange) o;
        return Objects.equals(start, that.start)
                && Objects.equals(end, that.end)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomDateRange{");
        sb.append("type='").append(type).append('\'');
        if (start != null) {
            sb.append(", start=").append(start);
        }
        if (end != null) {
            sb.append(", end=").append(end);
        }
        sb.append('}');
        return sb.toString();
    }
}
