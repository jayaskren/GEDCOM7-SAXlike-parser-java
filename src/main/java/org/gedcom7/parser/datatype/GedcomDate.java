package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 date.
 * A date consists of a calendar system, year, optional month, optional day,
 * and optional epoch marker (e.g. "BCE").
 */
public final class GedcomDate {

    private final String calendar;
    private final int year;
    private final String month;
    private final int day;
    private final String epoch;

    /**
     * Constructs a new GedcomDate.
     *
     * @param calendar the calendar system (e.g. "GREGORIAN", "JULIAN", "FRENCH_R", "HEBREW")
     * @param year     the year
     * @param month    the month abbreviation, or null if absent
     * @param day      the day of the month, or -1 if absent
     * @param epoch    the epoch marker (e.g. "BCE"), or null if absent
     */
    public GedcomDate(String calendar, int year, String month, int day, String epoch) {
        this.calendar = Objects.requireNonNull(calendar, "calendar must not be null");
        this.year = year;
        this.month = month;
        this.day = day;
        this.epoch = epoch;
    }

    public String getCalendar() {
        return calendar;
    }

    public int getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getEpoch() {
        return epoch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomDate)) return false;
        GedcomDate that = (GedcomDate) o;
        return year == that.year
                && day == that.day
                && Objects.equals(calendar, that.calendar)
                && Objects.equals(month, that.month)
                && Objects.equals(epoch, that.epoch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calendar, year, month, day, epoch);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomDate{");
        sb.append("calendar='").append(calendar).append('\'');
        sb.append(", year=").append(year);
        if (month != null) {
            sb.append(", month='").append(month).append('\'');
        }
        if (day != -1) {
            sb.append(", day=").append(day);
        }
        if (epoch != null) {
            sb.append(", epoch='").append(epoch).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
