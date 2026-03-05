package org.gedcom7.parser.datatype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GedcomDataTypes#parsePersonalName(String)}.
 * Covers FR-109: PersonalName ABNF with surname extracted from / delimiters.
 *
 * <p>GedcomPersonalName fields per data model:
 * <ul>
 *   <li>fullName: complete name string</li>
 *   <li>surname: extracted from between / delimiters, or null</li>
 *   <li>givenPrefix: text before first /</li>
 *   <li>surnameSuffix: text after last /</li>
 * </ul>
 */
class PersonalNameParserTest {

    @Test
    void nameWithSurname() {
        GedcomPersonalName name = GedcomDataTypes.parsePersonalName("John /Doe/");
        assertNotNull(name);
        assertEquals("John /Doe/", name.getFullText());
        assertEquals("Doe", name.getSurname());
        assertEquals("John", name.getGivenName().trim());
    }

    @Test
    void nameWithoutSlash() {
        GedcomPersonalName name = GedcomDataTypes.parsePersonalName("Madonna");
        assertNotNull(name);
        assertEquals("Madonna", name.getFullText());
        assertNull(name.getSurname());
        assertEquals("Madonna", name.getGivenName());
    }

    @Test
    void emptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parsePersonalName(""));
    }

    @Test
    void nameWithPrefixAndSuffix() {
        GedcomPersonalName name = GedcomDataTypes.parsePersonalName("Dr. John /Smith/ Jr.");
        assertNotNull(name);
        assertEquals("Dr. John /Smith/ Jr.", name.getFullText());
        assertEquals("Smith", name.getSurname());
        assertEquals("Dr. John", name.getGivenName().trim());
        assertEquals("Jr.", name.getNameSuffix().trim());
    }

    @Test
    void surnameOnly() {
        GedcomPersonalName name = GedcomDataTypes.parsePersonalName("/Johnson/");
        assertNotNull(name);
        assertEquals("Johnson", name.getSurname());
    }

    @ParameterizedTest(name = "parsePersonalName(null) throws")
    @NullSource
    void nullInput_throwsIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> GedcomDataTypes.parsePersonalName(input));
    }
}
