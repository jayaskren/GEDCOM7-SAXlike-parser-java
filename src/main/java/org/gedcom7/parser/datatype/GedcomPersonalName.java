package org.gedcom7.parser.datatype;

import java.util.Objects;

/**
 * Immutable value class representing a parsed GEDCOM 7 personal name.
 * The GEDCOM personal name format uses slashes to delimit the surname:
 * "Given /Surname/ Suffix"
 */
public final class GedcomPersonalName {

    private final String namePrefix;
    private final String givenName;
    private final String surname;
    private final String nameSuffix;
    private final String fullText;

    /**
     * Constructs a new GedcomPersonalName.
     *
     * @param namePrefix the name prefix (e.g. "Dr."), or null if absent
     * @param givenName  the given name(s), or null if absent
     * @param surname    the surname, or null if absent
     * @param nameSuffix the name suffix (e.g. "Jr."), or null if absent
     * @param fullText   the original full text of the name
     */
    public GedcomPersonalName(String namePrefix, String givenName, String surname,
                              String nameSuffix, String fullText) {
        this.namePrefix = namePrefix;
        this.givenName = givenName;
        this.surname = surname;
        this.nameSuffix = nameSuffix;
        this.fullText = Objects.requireNonNull(fullText, "fullText must not be null");
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public String getFullText() {
        return fullText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomPersonalName)) return false;
        GedcomPersonalName that = (GedcomPersonalName) o;
        return Objects.equals(namePrefix, that.namePrefix)
                && Objects.equals(givenName, that.givenName)
                && Objects.equals(surname, that.surname)
                && Objects.equals(nameSuffix, that.nameSuffix)
                && Objects.equals(fullText, that.fullText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namePrefix, givenName, surname, nameSuffix, fullText);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GedcomPersonalName{");
        sb.append("fullText='").append(fullText).append('\'');
        if (namePrefix != null) {
            sb.append(", namePrefix='").append(namePrefix).append('\'');
        }
        if (givenName != null) {
            sb.append(", givenName='").append(givenName).append('\'');
        }
        if (surname != null) {
            sb.append(", surname='").append(surname).append('\'');
        }
        if (nameSuffix != null) {
            sb.append(", nameSuffix='").append(nameSuffix).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
