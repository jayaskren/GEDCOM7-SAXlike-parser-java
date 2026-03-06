package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 date range.
 * Covers range types such as BET...AND, BEF, AFT, and approximate types ABT, CAL, EST,
 * as well as EXACT dates.
 */
public final class GedcomDateRange implements GedcomDateValue {

    private final GedcomDate start;
    private final GedcomDate end;
    private final String rangeType;
    private final String originalText;

    /**
     * Constructs a new GedcomDateRange.
     *
     * @param start the start date, or null if absent (e.g. for BEF)
     * @param end   the end date, or null if absent (e.g. for AFT)
     * @param rangeType  the range type: "BET_AND", "BEF", "AFT", "ABT", "CAL", "EST", or "EXACT"
     */
    public GedcomDateRange(GedcomDate start, GedcomDate end, String rangeType) {
        this(start, end, rangeType, null);
    }

    /**
     * Constructs a new GedcomDateRange with original text.
     *
     * @param start        the start date, or null if absent (e.g. for BEF)
     * @param end          the end date, or null if absent (e.g. for AFT)
     * @param rangeType    the range type: "BET_AND", "BEF", "AFT", "ABT", "CAL", "EST", or "EXACT"
     * @param originalText the original unparsed date text, or null
     */
    public GedcomDateRange(GedcomDate start, GedcomDate end, String rangeType, String originalText) {
        this.start = start;
        this.end = end;
        this.rangeType = Objects.requireNonNull(rangeType, "type must not be null");
        this.originalText = originalText;
    }

    public GedcomDate getStart() {
        return start;
    }

    public GedcomDate getEnd() {
        return end;
    }

    /**
     * Returns the range qualifier string (e.g. "BET_AND", "BEF", "AFT", "ABT", "CAL", "EST", "EXACT").
     *
     * @return the range type string
     */
    public String getRangeType() {
        return rangeType;
    }

    @Override
    public DateValueType getType() {
        switch (rangeType) {
            case "ABT":
            case "CAL":
            case "EST":
                return DateValueType.APPROXIMATE;
            case "EXACT":
                return DateValueType.EXACT;
            default:
                return DateValueType.RANGE;
        }
    }

    @Override
    public String getOriginalText() {
        return originalText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomDateRange)) return false;
        GedcomDateRange that = (GedcomDateRange) o;
        return Objects.equals(start, that.start)
                && Objects.equals(end, that.end)
                && Objects.equals(rangeType, that.rangeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, rangeType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomDateRange{");
        sb.append("rangeType='").append(rangeType).append('\'');
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
