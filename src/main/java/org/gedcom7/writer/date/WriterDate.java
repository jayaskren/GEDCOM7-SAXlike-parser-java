package org.gedcom7.writer.date;

import org.gedcom7.parser.GedcomVersion;

import java.util.Objects;

/**
 * Immutable date value object for GEDCOM writing.
 *
 * <p>Created via {@link GedcomDateBuilder} static factory methods
 * or the {@link #raw(String)} escape hatch. Renders to a GEDCOM
 * date string via {@link #toGedcomString(GedcomVersion)}.
 */
public final class WriterDate {

    // Date type enum for internal use
    enum DateType {
        EXACT, ABOUT, CALCULATED, ESTIMATED,
        BEFORE, AFTER, BETWEEN,
        FROM, TO, FROM_TO,
        RAW
    }

    enum Calendar {
        GREGORIAN, JULIAN, HEBREW, FRENCH_REPUBLICAN
    }

    final DateType type;
    final Calendar calendar;
    final DateComponents date1;
    final DateComponents date2;
    final String rawString;

    WriterDate(DateType type, Calendar calendar, DateComponents date1, DateComponents date2) {
        this.type = type;
        this.calendar = calendar;
        this.date1 = date1;
        this.date2 = date2;
        this.rawString = null;
    }

    private WriterDate(String rawString) {
        this.type = DateType.RAW;
        this.calendar = null;
        this.date1 = null;
        this.date2 = null;
        this.rawString = rawString;
    }

    /**
     * Expert escape hatch — creates a WriterDate from a raw GEDCOM date string
     * with no validation.
     */
    public static WriterDate raw(String gedcomDateString) {
        return new WriterDate(gedcomDateString);
    }

    /**
     * Renders this date to a GEDCOM-format string appropriate for the given version.
     */
    public String toGedcomString(GedcomVersion version) {
        if (type == DateType.RAW) {
            return rawString;
        }

        StringBuilder sb = new StringBuilder();
        boolean is555 = version.isGedcom5();

        // Prefix for type
        switch (type) {
            case ABOUT: sb.append("ABT "); break;
            case CALCULATED: sb.append("CAL "); break;
            case ESTIMATED: sb.append("EST "); break;
            case BEFORE: sb.append("BEF "); break;
            case AFTER: sb.append("AFT "); break;
            case BETWEEN: sb.append("BET "); break;
            case FROM: sb.append("FROM "); break;
            case TO: sb.append("TO "); break;
            case FROM_TO: sb.append("FROM "); break;
            default: break;
        }

        // Calendar prefix (non-Gregorian)
        if (calendar != Calendar.GREGORIAN) {
            appendCalendarPrefix(sb, is555);
        }

        // Date1
        if (date1 != null) {
            appendDateComponents(sb, date1);
        }

        // Range/period end
        if (type == DateType.BETWEEN && date2 != null) {
            sb.append(" AND ");
            if (date2.calendar != null && date2.calendar != Calendar.GREGORIAN) {
                appendCalendarPrefixForComponents(sb, date2.calendar, is555);
            }
            appendDateComponents(sb, date2);
        } else if (type == DateType.FROM_TO && date2 != null) {
            sb.append(" TO ");
            if (date2.calendar != null && date2.calendar != Calendar.GREGORIAN) {
                appendCalendarPrefixForComponents(sb, date2.calendar, is555);
            }
            appendDateComponents(sb, date2);
        }

        return sb.toString();
    }

    private void appendCalendarPrefix(StringBuilder sb, boolean is555) {
        String calName = calendarName(calendar, is555);
        if (is555) {
            sb.append("@#D").append(calName).append("@ ");
        } else {
            sb.append(calName).append(' ');
        }
    }

    private static void appendCalendarPrefixForComponents(StringBuilder sb, Calendar cal, boolean is555) {
        String calName = calendarName(cal, is555);
        if (is555) {
            sb.append("@#D").append(calName).append("@ ");
        } else {
            sb.append(calName).append(' ');
        }
    }

    private static String calendarName(Calendar cal, boolean is555) {
        switch (cal) {
            case JULIAN: return "JULIAN";
            case HEBREW: return "HEBREW";
            case FRENCH_REPUBLICAN: return is555 ? "FRENCH R" : "FRENCH_R";
            default: return "";
        }
    }

    private static void appendDateComponents(StringBuilder sb, DateComponents dc) {
        if (dc.day > 0) {
            sb.append(dc.day).append(' ');
        }
        if (dc.monthAbbreviation != null) {
            sb.append(dc.monthAbbreviation).append(' ');
        }
        sb.append(dc.year);
        if (dc.bce) {
            sb.append(" BCE");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WriterDate)) return false;
        WriterDate that = (WriterDate) o;
        return type == that.type
                && calendar == that.calendar
                && Objects.equals(date1, that.date1)
                && Objects.equals(date2, that.date2)
                && Objects.equals(rawString, that.rawString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, calendar, date1, date2, rawString);
    }

    @Override
    public String toString() {
        return toGedcomString(new GedcomVersion(7, 0));
    }

    /**
     * Internal date component holder.
     */
    static final class DateComponents {
        final int day;
        final String monthAbbreviation;
        final int year;
        final boolean bce;
        final Calendar calendar; // for compound dates where each part may have different calendars

        DateComponents(int day, String monthAbbreviation, int year, boolean bce) {
            this(day, monthAbbreviation, year, bce, null);
        }

        DateComponents(int day, String monthAbbreviation, int year, boolean bce, Calendar calendar) {
            this.day = day;
            this.monthAbbreviation = monthAbbreviation;
            this.year = year;
            this.bce = bce;
            this.calendar = calendar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DateComponents)) return false;
            DateComponents that = (DateComponents) o;
            return day == that.day && year == that.year && bce == that.bce
                    && Objects.equals(monthAbbreviation, that.monthAbbreviation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(day, monthAbbreviation, year, bce);
        }
    }
}
