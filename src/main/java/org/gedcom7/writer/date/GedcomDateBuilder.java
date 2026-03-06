package org.gedcom7.writer.date;

/**
 * Static factory methods for constructing type-safe {@link WriterDate} objects.
 *
 * <p>Example usage:
 * <pre>{@code
 * import static org.gedcom7.writer.date.GedcomDateBuilder.*;
 * import static org.gedcom7.writer.date.Month.*;
 *
 * WriterDate birthday = date(15, MAR, 1955);
 * WriterDate approximate = about(1880);
 * WriterDate range = between(date(1880), date(1890));
 * }</pre>
 */
public final class GedcomDateBuilder {

    private GedcomDateBuilder() {}

    // --- Exact dates ---

    public static WriterDate date(int day, Month month, int year) {
        validateDay(day, month);
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.GREGORIAN,
                new WriterDate.DateComponents(day, month.abbreviation(), year, false), null);
    }

    public static WriterDate date(Month month, int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.GREGORIAN,
                new WriterDate.DateComponents(0, month.abbreviation(), year, false), null);
    }

    public static WriterDate date(int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.GREGORIAN,
                new WriterDate.DateComponents(0, null, year, false), null);
    }

    // --- BCE dates ---

    public static WriterDate dateBce(int day, Month month, int year) {
        validateDay(day, month);
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.GREGORIAN,
                new WriterDate.DateComponents(day, month.abbreviation(), year, true), null);
    }

    public static WriterDate dateBce(Month month, int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.GREGORIAN,
                new WriterDate.DateComponents(0, month.abbreviation(), year, true), null);
    }

    public static WriterDate dateBce(int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.GREGORIAN,
                new WriterDate.DateComponents(0, null, year, true), null);
    }

    // --- Approximate dates ---

    public static WriterDate about(WriterDate date) {
        return wrapDate(WriterDate.DateType.ABOUT, date);
    }

    public static WriterDate about(int year) {
        return about(date(year));
    }

    public static WriterDate about(Month month, int year) {
        return about(date(month, year));
    }

    public static WriterDate calculated(WriterDate date) {
        return wrapDate(WriterDate.DateType.CALCULATED, date);
    }

    public static WriterDate estimated(WriterDate date) {
        return wrapDate(WriterDate.DateType.ESTIMATED, date);
    }

    // --- Ranges ---

    public static WriterDate before(WriterDate date) {
        return wrapDate(WriterDate.DateType.BEFORE, date);
    }

    public static WriterDate before(int year) {
        return before(date(year));
    }

    public static WriterDate before(Month month, int year) {
        return before(date(month, year));
    }

    public static WriterDate after(WriterDate date) {
        return wrapDate(WriterDate.DateType.AFTER, date);
    }

    public static WriterDate after(int year) {
        return after(date(year));
    }

    public static WriterDate after(Month month, int year) {
        return after(date(month, year));
    }

    public static WriterDate between(WriterDate start, WriterDate end) {
        validateChronologicalOrder(start, end, "between");
        return new WriterDate(WriterDate.DateType.BETWEEN,
                start.calendar != null ? start.calendar : WriterDate.Calendar.GREGORIAN,
                start.date1, end.date1);
    }

    // --- Periods ---

    public static WriterDate from(WriterDate date) {
        return wrapDate(WriterDate.DateType.FROM, date);
    }

    public static WriterDate to(WriterDate date) {
        return wrapDate(WriterDate.DateType.TO, date);
    }

    public static WriterDate fromTo(WriterDate start, WriterDate end) {
        validateChronologicalOrder(start, end, "fromTo");
        return new WriterDate(WriterDate.DateType.FROM_TO,
                start.calendar != null ? start.calendar : WriterDate.Calendar.GREGORIAN,
                start.date1, end.date1);
    }

    // --- Non-Gregorian calendars ---

    public static WriterDate julian(int day, Month month, int year) {
        validateDay(day, month);
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.JULIAN,
                new WriterDate.DateComponents(day, month.abbreviation(), year, false), null);
    }

    public static WriterDate julian(Month month, int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.JULIAN,
                new WriterDate.DateComponents(0, month.abbreviation(), year, false), null);
    }

    public static WriterDate julian(int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.JULIAN,
                new WriterDate.DateComponents(0, null, year, false), null);
    }

    public static WriterDate hebrew(int day, HebrewMonth month, int year) {
        validateYear(year);
        if (day < 1 || day > 30) {
            throw new IllegalArgumentException("Day must be 1..30 for Hebrew months, got " + day);
        }
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.HEBREW,
                new WriterDate.DateComponents(day, month.abbreviation(), year, false), null);
    }

    public static WriterDate hebrew(HebrewMonth month, int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.HEBREW,
                new WriterDate.DateComponents(0, month.abbreviation(), year, false), null);
    }

    public static WriterDate hebrew(int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.HEBREW,
                new WriterDate.DateComponents(0, null, year, false), null);
    }

    public static WriterDate frenchRepublican(int day, FrenchRepublicanMonth month, int year) {
        validateYear(year);
        if (day < 1 || day > 30) {
            throw new IllegalArgumentException("Day must be 1..30 for French Republican months, got " + day);
        }
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.FRENCH_REPUBLICAN,
                new WriterDate.DateComponents(day, month.abbreviation(), year, false), null);
    }

    public static WriterDate frenchRepublican(FrenchRepublicanMonth month, int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.FRENCH_REPUBLICAN,
                new WriterDate.DateComponents(0, month.abbreviation(), year, false), null);
    }

    public static WriterDate frenchRepublican(int year) {
        validateYear(year);
        return new WriterDate(WriterDate.DateType.EXACT, WriterDate.Calendar.FRENCH_REPUBLICAN,
                new WriterDate.DateComponents(0, null, year, false), null);
    }

    // --- Internal validation ---

    private static void validateDay(int day, Month month) {
        if (day < 1 || day > month.maxDay()) {
            throw new IllegalArgumentException(
                    "Day must be 1.." + month.maxDay() + " for " + month + ", got " + day);
        }
    }

    private static void validateYear(int year) {
        if (year < 1) {
            throw new IllegalArgumentException("Year must be >= 1, got " + year);
        }
    }

    private static void validateChronologicalOrder(WriterDate start, WriterDate end, String method) {
        if (start.calendar != null && end.calendar != null
                && start.calendar == end.calendar
                && start.date1 != null && end.date1 != null) {
            int cmp = compareDateComponents(start.date1, end.date1);
            if (cmp > 0) {
                throw new IllegalArgumentException(
                        method + "() requires start date to precede end date");
            }
        }
    }

    private static int compareDateComponents(WriterDate.DateComponents a, WriterDate.DateComponents b) {
        if (a.year != b.year) return Integer.compare(a.year, b.year);
        // Compare months by abbreviation order if both present
        if (a.monthAbbreviation != null && b.monthAbbreviation != null) {
            int ma = monthOrder(a.monthAbbreviation);
            int mb = monthOrder(b.monthAbbreviation);
            if (ma != mb) return Integer.compare(ma, mb);
        }
        return Integer.compare(a.day, b.day);
    }

    private static int monthOrder(String abbr) {
        switch (abbr) {
            case "JAN": return 1; case "FEB": return 2; case "MAR": return 3;
            case "APR": return 4; case "MAY": return 5; case "JUN": return 6;
            case "JUL": return 7; case "AUG": return 8; case "SEP": return 9;
            case "OCT": return 10; case "NOV": return 11; case "DEC": return 12;
            default: return 0;
        }
    }

    private static WriterDate wrapDate(WriterDate.DateType type, WriterDate base) {
        return new WriterDate(type,
                base.calendar != null ? base.calendar : WriterDate.Calendar.GREGORIAN,
                base.date1, null);
    }
}
