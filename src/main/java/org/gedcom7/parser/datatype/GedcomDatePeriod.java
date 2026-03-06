package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 date period.
 * Covers period types: FROM...TO, FROM, and TO.
 */
public final class GedcomDatePeriod implements GedcomDateValue {

    private final GedcomDate from;
    private final GedcomDate to;
    private final String periodType;
    private final String originalText;

    /**
     * Constructs a new GedcomDatePeriod.
     *
     * @param from the start of the period, or null if absent (e.g. for "TO" only)
     * @param to   the end of the period, or null if absent (e.g. for "FROM" only)
     * @param periodType the period type: "FROM_TO", "FROM", or "TO"
     */
    public GedcomDatePeriod(GedcomDate from, GedcomDate to, String periodType) {
        this(from, to, periodType, null);
    }

    /**
     * Constructs a new GedcomDatePeriod with original text.
     *
     * @param from         the start of the period, or null if absent (e.g. for "TO" only)
     * @param to           the end of the period, or null if absent (e.g. for "FROM" only)
     * @param periodType   the period type: "FROM_TO", "FROM", or "TO"
     * @param originalText the original unparsed date text, or null
     */
    public GedcomDatePeriod(GedcomDate from, GedcomDate to, String periodType, String originalText) {
        this.from = from;
        this.to = to;
        this.periodType = Objects.requireNonNull(periodType, "type must not be null");
        this.originalText = originalText;
    }

    public GedcomDate getFrom() {
        return from;
    }

    public GedcomDate getTo() {
        return to;
    }

    /**
     * Returns the period qualifier string (e.g. "FROM_TO", "FROM", "TO").
     *
     * @return the period type string
     */
    public String getPeriodType() {
        return periodType;
    }

    @Override
    public DateValueType getType() {
        return DateValueType.PERIOD;
    }

    @Override
    public String getOriginalText() {
        return originalText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomDatePeriod)) return false;
        GedcomDatePeriod that = (GedcomDatePeriod) o;
        return Objects.equals(from, that.from)
                && Objects.equals(to, that.to)
                && Objects.equals(periodType, that.periodType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, periodType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomDatePeriod{");
        sb.append("periodType='").append(periodType).append('\'');
        if (from != null) {
            sb.append(", from=").append(from);
        }
        if (to != null) {
            sb.append(", to=").append(to);
        }
        sb.append('}');
        return sb.toString();
    }
}
