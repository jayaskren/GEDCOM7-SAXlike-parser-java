package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 age value.
 * Format: [&gt;|&lt;] [Ny] [Nm] [Nw] [Nd]
 */
public final class GedcomAge {

    private final int years;
    private final int months;
    private final int weeks;
    private final int days;
    private final String modifier;

    /**
     * Constructs a new GedcomAge.
     *
     * @param years    the number of years, or -1 if absent
     * @param months   the number of months, or -1 if absent
     * @param weeks    the number of weeks, or -1 if absent
     * @param days     the number of days, or -1 if absent
     * @param modifier the modifier ("&gt;" or "&lt;"), or null if absent
     */
    public GedcomAge(int years, int months, int weeks, int days, String modifier) {
        this.years = years;
        this.months = months;
        this.weeks = weeks;
        this.days = days;
        this.modifier = modifier;
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getWeeks() {
        return weeks;
    }

    public int getDays() {
        return days;
    }

    public String getModifier() {
        return modifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomAge)) return false;
        GedcomAge that = (GedcomAge) o;
        return years == that.years
                && months == that.months
                && weeks == that.weeks
                && days == that.days
                && Objects.equals(modifier, that.modifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(years, months, weeks, days, modifier);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomAge{");
        if (modifier != null) {
            sb.append("modifier='").append(modifier).append("', ");
        }
        if (years != -1) {
            sb.append("years=").append(years).append(", ");
        }
        if (months != -1) {
            sb.append("months=").append(months).append(", ");
        }
        if (weeks != -1) {
            sb.append("weeks=").append(weeks).append(", ");
        }
        if (days != -1) {
            sb.append("days=").append(days).append(", ");
        }
        // Remove trailing ", " if present
        if (sb.length() > "GedcomAge{".length() && sb.charAt(sb.length() - 2) == ',') {
            sb.setLength(sb.length() - 2);
        }
        sb.append('}');
        return sb.toString();
    }
}
