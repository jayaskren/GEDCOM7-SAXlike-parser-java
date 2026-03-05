package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeadingAtEscapeStrategyTest {

    private final LeadingAtEscapeStrategy strategy = new LeadingAtEscapeStrategy();

    @Test
    void leadingDoubleAtDecoded() {
        assertEquals("@value", strategy.unescape("@@value"));
    }

    @Test
    void nonLeadingDoubleAtUntouched() {
        assertEquals("email@@host", strategy.unescape("email@@host"));
    }

    @Test
    void noAtPassthrough() {
        assertEquals("plain text", strategy.unescape("plain text"));
    }

    @Test
    void nullReturnsNull() {
        assertNull(strategy.unescape(null));
    }

    @Test
    void emptyStringReturnsEmpty() {
        assertEquals("", strategy.unescape(""));
    }

    @Test
    void singleAtUntouched() {
        assertEquals("@", strategy.unescape("@"));
    }

    @Test
    void leadingDoubleAtWithMore() {
        assertEquals("@@rest", strategy.unescape("@@@rest"));
    }
}
