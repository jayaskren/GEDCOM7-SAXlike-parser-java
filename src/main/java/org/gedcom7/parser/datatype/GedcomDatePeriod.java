package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 date period.
 * Covers period types: FROM...TO, FROM, and TO.
 */
public final class GedcomDatePeriod {

    private final GedcomDate from;
    private final GedcomDate to;
    private final String type;

    /**
     * Constructs a new GedcomDatePeriod.
     *
     * @param from the start of the period, or null if absent (e.g. for "TO" only)
     * @param to   the end of the period, or null if absent (e.g. for "FROM" only)
     * @param type the period type: "FROM_TO", "FROM", or "TO"
     */
    public GedcomDatePeriod(GedcomDate from, GedcomDate to, String type) {
        this.from = from;
        this.to = to;
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public GedcomDate getFrom() {
        return from;
    }

    public GedcomDate getTo() {
        return to;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomDatePeriod)) return false;
        GedcomDatePeriod that = (GedcomDatePeriod) o;
        return Objects.equals(from, that.from)
                && Objects.equals(to, that.to)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomDatePeriod{");
        sb.append("type='").append(type).append('\'');
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
