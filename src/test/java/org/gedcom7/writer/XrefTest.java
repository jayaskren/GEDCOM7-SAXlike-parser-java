package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XrefTest {

    @Test
    void ofCreatesXref() {
        Xref xref = Xref.of("I1");
        assertEquals("I1", xref.getId());
    }

    @Test
    void ofRejectsNull() {
        assertThrows(NullPointerException.class, () -> Xref.of(null));
    }

    @Test
    void equalsAndHashCode() {
        Xref a = Xref.of("I1");
        Xref b = Xref.of("I1");
        Xref c = Xref.of("I2");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toStringReturnsAtDelimited() {
        Xref xref = Xref.of("I1");
        assertEquals("@I1@", xref.toString());
    }

    @Test
    void equalsNull() {
        Xref xref = Xref.of("I1");
        assertNotEquals(null, xref);
    }

    @Test
    void equalsDifferentType() {
        Xref xref = Xref.of("I1");
        assertNotEquals("I1", xref);
    }

    @Test
    void equalsSameInstance() {
        Xref xref = Xref.of("I1");
        assertEquals(xref, xref);
    }
}
