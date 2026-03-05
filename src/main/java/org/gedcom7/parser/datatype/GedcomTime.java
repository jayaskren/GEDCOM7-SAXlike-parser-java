package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 time value.
 */
public final class GedcomTime {

    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int milliseconds;
    private final boolean utc;

    /**
     * Constructs a new GedcomTime.
     *
     * @param hours        the hour (0-23)
     * @param minutes      the minute (0-59)
     * @param seconds      the second (0-59), or -1 if absent
     * @param milliseconds the milliseconds (0-999), or -1 if absent
     * @param utc          true if the time is in UTC (indicated by trailing 'Z')
     */
    public GedcomTime(int hours, int minutes, int seconds, int milliseconds, boolean utc) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        this.utc = utc;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public boolean isUtc() {
        return utc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomTime)) return false;
        GedcomTime that = (GedcomTime) o;
        return hours == that.hours
                && minutes == that.minutes
                && seconds == that.seconds
                && milliseconds == that.milliseconds
                && utc == that.utc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hours, minutes, seconds, milliseconds, utc);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomTime{");
        sb.append("hours=").append(hours);
        sb.append(", minutes=").append(minutes);
        if (seconds != -1) {
            sb.append(", seconds=").append(seconds);
        }
        if (milliseconds != -1) {
            sb.append(", milliseconds=").append(milliseconds);
        }
        sb.append(", utc=").append(utc);
        sb.append('}');
        return sb.toString();
    }
}
