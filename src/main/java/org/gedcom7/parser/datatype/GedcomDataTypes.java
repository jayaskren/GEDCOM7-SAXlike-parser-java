package org.gedcom7.parser.datatype;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility class providing parse methods for GEDCOM 7 data types.
 * All parse methods throw {@link IllegalArgumentException} on invalid input.
 */
public final class GedcomDataTypes {

    private GedcomDataTypes() {
        // utility class
    }

    // --- Month sets ---

    private static final Set<String> GREGORIAN_MONTHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
            "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    )));

    private static final Set<String> JULIAN_MONTHS = GREGORIAN_MONTHS;

    private static final Set<String> FRENCH_R_MONTHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "VEND", "BRUM", "FRIM", "NIVO", "PLUV", "VENT",
            "GERM", "FLOR", "PRAI", "MESS", "THER", "FRUC", "COMP"
    )));

    private static final Set<String> HEBREW_MONTHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "TSH", "CSH", "KSL", "TVT", "SHV", "ADR", "ADS",
            "NSN", "IYR", "SVN", "TMZ", "AAV", "ELL"
    )));

    private static final Set<String> CALENDARS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "GREGORIAN", "JULIAN", "FRENCH_R", "HEBREW"
    )));

    // --- Integer parsing ---

    /**
     * Parses a non-negative integer. No leading zeros are allowed except for the value "0" itself.
     *
     * @param text the text to parse
     * @return the parsed integer value
     * @throws IllegalArgumentException if text is null, empty, negative, or has leading zeros
     */
    public static int parseInteger(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Integer value must not be null or empty");
        }
        // No leading zeros (except "0" itself)
        if (text.length() > 1 && text.charAt(0) == '0') {
            throw new IllegalArgumentException("Leading zeros not allowed: " + text);
        }
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer: " + text, e);
        }
        if (value < 0) {
            throw new IllegalArgumentException("Integer must be non-negative: " + text);
        }
        return value;
    }

    // --- Date parsing ---

    /**
     * Parses a GEDCOM date value string. Returns one of:
     * <ul>
     *   <li>{@link GedcomDate} for exact dates (via {@link GedcomDateRange} with type "EXACT")</li>
     *   <li>{@link GedcomDateRange} for range/approximate dates (BET...AND, BEF, AFT, ABT, CAL, EST)</li>
     *   <li>{@link GedcomDatePeriod} for period dates (FROM...TO, FROM, TO)</li>
     * </ul>
     * All returned objects implement {@link GedcomDateValue}.
     *
     * <p>For unparseable input, returns a {@link GedcomDateValue} with type
     * {@link DateValueType#UNPARSEABLE} instead of throwing an exception.
     *
     * @param text the date value string
     * @return a GedcomDateValue (never null)
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static GedcomDateValue parseDateValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Date value must not be null or empty");
        }
        String trimmed = text.trim();
        String[] tokens = trimmed.split("\\s+");

        try {
            // Check for period keywords
            if (tokens[0].equals("FROM")) {
                return parsePeriod(tokens, 1, trimmed);
            }
            if (tokens[0].equals("TO")) {
                GedcomDateResult toDate = parseSingleDate(tokens, 1);
                return new GedcomDatePeriod(null, toDate.date, "TO", trimmed);
            }

            // Check for range/approximate keywords
            if (tokens[0].equals("BET")) {
                return parseBetweenRange(tokens, 1, trimmed);
            }
            if (tokens[0].equals("BEF")) {
                GedcomDateResult befDate = parseSingleDate(tokens, 1);
                return new GedcomDateRange(null, befDate.date, "BEF", trimmed);
            }
            if (tokens[0].equals("AFT")) {
                GedcomDateResult aftDate = parseSingleDate(tokens, 1);
                return new GedcomDateRange(aftDate.date, null, "AFT", trimmed);
            }
            if (tokens[0].equals("ABT") || tokens[0].equals("CAL") || tokens[0].equals("EST")) {
                String type = tokens[0];
                GedcomDateResult approxDate = parseSingleDate(tokens, 1);
                return new GedcomDateRange(approxDate.date, null, type, trimmed);
            }

            // Otherwise it is an exact date
            GedcomDateResult exactDate = parseSingleDate(tokens, 0);
            return new GedcomDateRange(exactDate.date, null, "EXACT", trimmed);
        } catch (IllegalArgumentException e) {
            return new UnparseableDateValue(trimmed);
        }
    }

    /**
     * Internal implementation for unparseable date values.
     */
    static final class UnparseableDateValue implements GedcomDateValue {
        private final String originalText;

        UnparseableDateValue(String originalText) {
            this.originalText = originalText;
        }

        @Override
        public DateValueType getType() {
            return DateValueType.UNPARSEABLE;
        }

        @Override
        public String getOriginalText() {
            return originalText;
        }

        @Override
        public String toString() {
            return "UnparseableDateValue{originalText='" + originalText + "'}";
        }
    }

    private static GedcomDatePeriod parsePeriod(String[] tokens, int startPos, String originalText) {
        // FROM date [TO date]
        // Find "TO" keyword to split
        int toIndex = -1;
        for (int i = startPos; i < tokens.length; i++) {
            if (tokens[i].equals("TO")) {
                toIndex = i;
                break;
            }
        }

        if (toIndex == -1) {
            // FROM only
            GedcomDateResult fromDate = parseSingleDate(tokens, startPos);
            return new GedcomDatePeriod(fromDate.date, null, "FROM", originalText);
        } else {
            // FROM...TO
            String[] fromTokens = Arrays.copyOfRange(tokens, 0, toIndex);
            GedcomDateResult fromDate = parseSingleDate(fromTokens, startPos);
            GedcomDateResult toDate = parseSingleDate(tokens, toIndex + 1);
            return new GedcomDatePeriod(fromDate.date, toDate.date, "FROM_TO", originalText);
        }
    }

    private static GedcomDateRange parseBetweenRange(String[] tokens, int startPos, String originalText) {
        // BET date AND date
        int andIndex = -1;
        for (int i = startPos; i < tokens.length; i++) {
            if (tokens[i].equals("AND")) {
                andIndex = i;
                break;
            }
        }
        if (andIndex == -1) {
            throw new IllegalArgumentException("BET requires AND: " + String.join(" ", tokens));
        }
        String[] startTokens = Arrays.copyOfRange(tokens, 0, andIndex);
        GedcomDateResult startDate = parseSingleDate(startTokens, startPos);
        GedcomDateResult endDate = parseSingleDate(tokens, andIndex + 1);
        return new GedcomDateRange(startDate.date, endDate.date, "BET_AND", originalText);
    }

    /**
     * Internal result wrapper for date parsing that tracks position consumed.
     */
    private static class GedcomDateResult {
        final org.gedcom7.parser.datatype.GedcomDate date;
        final int nextPos;

        GedcomDateResult(org.gedcom7.parser.datatype.GedcomDate date, int nextPos) {
            this.date = date;
            this.nextPos = nextPos;
        }
    }

    // Renamed to avoid confusion with the public class
    private static GedcomDateResult parseSingleDate(String[] tokens, int pos) {
        if (pos >= tokens.length) {
            throw new IllegalArgumentException("Expected date components but found end of input");
        }

        String calendar = "GREGORIAN";

        // Check for calendar prefix
        if (CALENDARS.contains(tokens[pos])) {
            calendar = tokens[pos];
            pos++;
            if (pos >= tokens.length) {
                throw new IllegalArgumentException("Expected date after calendar prefix");
            }
        }

        Set<String> validMonths = getValidMonths(calendar);

        // Try to parse components: [day] [month] year [epoch]
        // Possible formats:
        //   year
        //   month year
        //   day month year
        //   any of the above followed by BCE

        int day = -1;
        String month = null;
        int year;
        String epoch = null;

        // Determine how many date tokens remain (up to end, or up to a keyword)
        int endPos = tokens.length;
        for (int i = pos; i < tokens.length; i++) {
            if (tokens[i].equals("AND") || tokens[i].equals("TO")
                    || tokens[i].equals("FROM") || tokens[i].equals("BET")
                    || tokens[i].equals("BEF") || tokens[i].equals("AFT")
                    || tokens[i].equals("ABT") || tokens[i].equals("CAL")
                    || tokens[i].equals("EST")) {
                endPos = i;
                break;
            }
        }

        int dateTokenCount = endPos - pos;

        if (dateTokenCount == 1) {
            // Just year
            year = parseYear(tokens[pos]);
        } else if (dateTokenCount == 2) {
            // Could be: month year, year BCE, or day month (unlikely without year)
            if (tokens[endPos - 1].equals("BCE")) {
                year = parseYear(tokens[pos]);
                epoch = "BCE";
            } else if (validMonths.contains(tokens[pos])) {
                month = tokens[pos];
                year = parseYear(tokens[pos + 1]);
            } else {
                // Try as year BCE or similar
                year = parseYear(tokens[pos]);
                epoch = tokens[pos + 1];
            }
        } else if (dateTokenCount == 3) {
            // Could be: day month year, month year BCE
            if (validMonths.contains(tokens[pos])) {
                // month year BCE
                month = tokens[pos];
                year = parseYear(tokens[pos + 1]);
                epoch = tokens[pos + 2];
            } else {
                // day month year
                day = parseDayNumber(tokens[pos]);
                month = tokens[pos + 1];
                if (!validMonths.contains(month)) {
                    throw new IllegalArgumentException("Invalid month '" + month + "' for calendar " + calendar);
                }
                year = parseYear(tokens[pos + 2]);
            }
        } else if (dateTokenCount == 4) {
            // day month year epoch
            day = parseDayNumber(tokens[pos]);
            month = tokens[pos + 1];
            if (!validMonths.contains(month)) {
                throw new IllegalArgumentException("Invalid month '" + month + "' for calendar " + calendar);
            }
            year = parseYear(tokens[pos + 2]);
            epoch = tokens[pos + 3];
        } else {
            throw new IllegalArgumentException("Unexpected number of date tokens: " + dateTokenCount);
        }

        return new GedcomDateResult(
                new org.gedcom7.parser.datatype.GedcomDate(calendar, year, month, day, epoch),
                endPos
        );
    }

    private static Set<String> getValidMonths(String calendar) {
        switch (calendar) {
            case "GREGORIAN":
                return GREGORIAN_MONTHS;
            case "JULIAN":
                return JULIAN_MONTHS;
            case "FRENCH_R":
                return FRENCH_R_MONTHS;
            case "HEBREW":
                return HEBREW_MONTHS;
            default:
                throw new IllegalArgumentException("Unknown calendar: " + calendar);
        }
    }

    private static int parseYear(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid year: " + token, e);
        }
    }

    private static int parseDayNumber(String token) {
        try {
            int day = Integer.parseInt(token);
            if (day < 1 || day > 31) {
                throw new IllegalArgumentException("Day out of range: " + day);
            }
            return day;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid day: " + token, e);
        }
    }

    // --- Time parsing ---

    /**
     * Parses a GEDCOM time string.
     * Formats: "HH:MM", "HH:MM:SS", "HH:MM:SSZ", "HH:MM:SS.fffZ", etc.
     *
     * @param text the time string
     * @return the parsed GedcomTime
     * @throws IllegalArgumentException if the text cannot be parsed
     */
    public static GedcomTime parseTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Time value must not be null or empty");
        }
        String trimmed = text.trim();
        boolean utc = false;
        if (trimmed.endsWith("Z")) {
            utc = true;
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        String[] mainParts = trimmed.split(":");
        if (mainParts.length < 2 || mainParts.length > 3) {
            throw new IllegalArgumentException("Invalid time format: " + text);
        }

        int hours = Integer.parseInt(mainParts[0]);
        int minutes = Integer.parseInt(mainParts[1]);
        int seconds = -1;
        int milliseconds = -1;

        if (mainParts.length == 3) {
            String secPart = mainParts[2];
            if (secPart.contains(".")) {
                String[] secFrac = secPart.split("\\.");
                seconds = Integer.parseInt(secFrac[0]);
                if (secFrac.length > 1) {
                    String frac = secFrac[1];
                    // Pad or truncate to 3 digits for milliseconds
                    while (frac.length() < 3) {
                        frac = frac + "0";
                    }
                    milliseconds = Integer.parseInt(frac.substring(0, 3));
                }
            } else {
                seconds = Integer.parseInt(secPart);
            }
        }

        if (hours < 0 || hours > 23) {
            throw new IllegalArgumentException("Hours out of range: " + hours);
        }
        if (minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutes);
        }
        if (seconds != -1 && (seconds < 0 || seconds > 59)) {
            throw new IllegalArgumentException("Seconds out of range: " + seconds);
        }

        return new GedcomTime(hours, minutes, seconds, milliseconds, utc);
    }

    // --- Age parsing ---

    private static final Pattern AGE_PART_PATTERN = Pattern.compile("(\\d+)([yYmMwWdD])");

    /**
     * Parses a GEDCOM age string.
     * Format: [&gt;|&lt;] [Ny] [Nm] [Nw] [Nd]
     *
     * @param text the age string
     * @return the parsed GedcomAge
     * @throws IllegalArgumentException if the text cannot be parsed
     */
    public static GedcomAge parseAge(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Age value must not be null or empty");
        }
        String trimmed = text.trim();
        String modifier = null;

        if (trimmed.startsWith(">") || trimmed.startsWith("<")) {
            modifier = String.valueOf(trimmed.charAt(0));
            trimmed = trimmed.substring(1).trim();
        }

        int years = -1;
        int months = -1;
        int weeks = -1;
        int days = -1;
        boolean foundAny = false;

        Matcher matcher = AGE_PART_PATTERN.matcher(trimmed);
        while (matcher.find()) {
            foundAny = true;
            int val = Integer.parseInt(matcher.group(1));
            char unit = Character.toLowerCase(matcher.group(2).charAt(0));
            switch (unit) {
                case 'y':
                    years = val;
                    break;
                case 'm':
                    months = val;
                    break;
                case 'w':
                    weeks = val;
                    break;
                case 'd':
                    days = val;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown age unit: " + unit);
            }
        }

        if (!foundAny) {
            throw new IllegalArgumentException("No age components found in: " + text);
        }

        return new GedcomAge(years, months, weeks, days, modifier);
    }

    // --- Personal name parsing ---

    /**
     * Parses a GEDCOM personal name from the "Given /Surname/ Suffix" format.
     *
     * @param text the name string
     * @return the parsed GedcomPersonalName
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static GedcomPersonalName parsePersonalName(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Personal name must not be null or empty");
        }
        String trimmed = text.trim();
        String givenName = null;
        String surname = null;
        String nameSuffix = null;

        int slashStart = trimmed.indexOf('/');
        int slashEnd = trimmed.indexOf('/', slashStart + 1);

        if (slashStart >= 0 && slashEnd > slashStart) {
            // Extract surname between slashes
            surname = trimmed.substring(slashStart + 1, slashEnd).trim();
            if (surname.isEmpty()) {
                surname = null;
            }

            // Given name is everything before the first slash
            String before = trimmed.substring(0, slashStart).trim();
            if (!before.isEmpty()) {
                givenName = before;
            }

            // Suffix is everything after the second slash
            String after = trimmed.substring(slashEnd + 1).trim();
            if (!after.isEmpty()) {
                nameSuffix = after;
            }
        } else {
            // No slashes; treat entire text as given name
            givenName = trimmed;
        }

        // namePrefix is not parsed from the raw text format (it comes from sub-structures)
        return new GedcomPersonalName(null, givenName, surname, nameSuffix, text);
    }

    // --- Coordinate parsing ---

    /**
     * Parses a GEDCOM latitude string (e.g. "N50.1234" or "S33.8688").
     *
     * @param text the latitude string
     * @return the parsed GedcomCoordinate
     * @throws IllegalArgumentException if the text cannot be parsed
     */
    public static GedcomCoordinate parseLatitude(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Latitude must not be null or empty");
        }
        String trimmed = text.trim();
        char dir = Character.toUpperCase(trimmed.charAt(0));
        if (dir != 'N' && dir != 'S') {
            throw new IllegalArgumentException("Latitude must start with N or S: " + text);
        }
        double value;
        try {
            value = Double.parseDouble(trimmed.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid latitude value: " + text, e);
        }
        return new GedcomCoordinate(value, dir);
    }

    /**
     * Parses a GEDCOM longitude string (e.g. "E1.2345" or "W122.4194").
     *
     * @param text the longitude string
     * @return the parsed GedcomCoordinate
     * @throws IllegalArgumentException if the text cannot be parsed
     */
    public static GedcomCoordinate parseLongitude(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Longitude must not be null or empty");
        }
        String trimmed = text.trim();
        char dir = Character.toUpperCase(trimmed.charAt(0));
        if (dir != 'E' && dir != 'W') {
            throw new IllegalArgumentException("Longitude must start with E or W: " + text);
        }
        double value;
        try {
            value = Double.parseDouble(trimmed.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid longitude value: " + text, e);
        }
        return new GedcomCoordinate(value, dir);
    }

    // --- Passthrough / simple parsing ---

    /**
     * Parses a BCP 47 language tag. Returns the tag as-is after validation (non-empty).
     *
     * @param text the language tag
     * @return the language tag string
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static String parseLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Language tag must not be null or empty");
        }
        return text.trim();
    }

    /**
     * Parses a MIME media type. Returns the type as-is after validation (non-empty).
     *
     * @param text the media type
     * @return the media type string
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static String parseMediaType(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Media type must not be null or empty");
        }
        return text.trim();
    }

    /**
     * Parses an enumeration value. Returns the value as-is after validation (non-empty).
     *
     * @param text the enum value
     * @return the enum value string
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static String parseEnum(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Enum value must not be null or empty");
        }
        return text.trim();
    }

    /**
     * Parses a delimited text list into individual strings.
     *
     * @param text      the text to split
     * @param delimiter the delimiter to split on
     * @return a list of trimmed, non-empty strings
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static List<String> parseListText(String text, String delimiter) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("List text must not be null or empty");
        }
        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter must not be null or empty");
        }
        String[] parts = text.split(Pattern.quote(delimiter), -1);
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("List contains no non-empty elements: " + text);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Parses a delimited enumeration list into individual strings.
     *
     * @param text      the text to split
     * @param delimiter the delimiter to split on
     * @return a list of trimmed, non-empty enum values
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static List<String> parseListEnum(String text, String delimiter) {
        return parseListText(text, delimiter);
    }

    /**
     * Parses a URI string.
     *
     * @param text the URI string
     * @return the parsed URI
     * @throws IllegalArgumentException if the text is null, empty, or not a valid URI
     */
    public static URI parseUri(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("URI must not be null or empty");
        }
        try {
            return new URI(text.trim());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + text, e);
        }
    }

    /**
     * Parses a file path. Returns the path as-is after validation (non-empty).
     *
     * @param text the file path
     * @return the file path string
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static String parseFilePath(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }
        return text.trim();
    }

    /**
     * Parses a GEDCOM tag definition, splitting by the first space into {tag, uri}.
     *
     * @param text the tag definition string (e.g. "_MYTAG https://example.com/mytag")
     * @return a String array of {tag, uri}
     * @throws IllegalArgumentException if the text is null, empty, or does not contain a space
     */
    public static String[] parseTagDef(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag definition must not be null or empty");
        }
        String trimmed = text.trim();
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex == -1) {
            throw new IllegalArgumentException("Tag definition must contain a space separating tag and URI: " + text);
        }
        String tag = trimmed.substring(0, spaceIndex);
        String uri = trimmed.substring(spaceIndex + 1).trim();
        if (tag.isEmpty() || uri.isEmpty()) {
            throw new IllegalArgumentException("Tag and URI must both be non-empty: " + text);
        }
        return new String[]{tag, uri};
    }
}
