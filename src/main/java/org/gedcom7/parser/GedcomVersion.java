package org.gedcom7.parser;

import java.util.Objects;

/**
 * Parsed GEDCOM version number (e.g., 7.0, 7.1, 5.5.1).
 *
 * <p>Instances are immutable.
 */
public final class GedcomVersion {

    private final int major;
    private final int minor;
    private final int patch;

    public GedcomVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public GedcomVersion(int major, int minor) {
        this(major, minor, -1);
    }

    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getPatch() { return patch; }

    /** Returns true if this is a GEDCOM 7.x version. */
    public boolean isGedcom7() { return major == 7; }

    /** Returns true if this is a GEDCOM 5.x version. */
    public boolean isGedcom5() { return major == 5; }

    /**
     * Parse a version string such as "7.0" or "5.5.1".
     *
     * @param versionString the version string
     * @return the parsed version
     * @throws IllegalArgumentException if the string is not a valid version
     */
    public static GedcomVersion parse(String versionString) {
        if (versionString == null || versionString.isEmpty()) {
            throw new IllegalArgumentException("Version string must not be null or empty");
        }
        String[] parts = versionString.split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Invalid version format: " + versionString);
        }
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length == 3 ? Integer.parseInt(parts[2]) : -1;
            return new GedcomVersion(major, minor, patch);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version format: " + versionString, e);
        }
    }

    @Override
    public String toString() {
        return patch >= 0 ? major + "." + minor + "." + patch : major + "." + minor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomVersion)) return false;
        GedcomVersion that = (GedcomVersion) o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
