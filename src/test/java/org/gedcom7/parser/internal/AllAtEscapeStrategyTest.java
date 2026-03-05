package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AllAtEscapeStrategyTest {

    private final AllAtEscapeStrategy strategy = new AllAtEscapeStrategy();

    @Test
    void nullReturnsNull() {
        assertNull(strategy.unescape(null));
    }

    @Test
    void noDoubleAtReturnsUnchanged() {
        assertEquals("plain text", strategy.unescape("plain text"));
    }

    @Test
    void leadingDoubleAtDecoded() {
        assertEquals("@value", strategy.unescape("@@value"));
    }

    @Test
    void middleDoubleAtDecoded() {
        assertEquals("email@host", strategy.unescape("email@@host"));
    }

    @Test
    void multipleDoubleAtAllDecoded() {
        assertEquals("a@b@c", strategy.unescape("a@@b@@c"));
    }

    @Test
    void singleDoubleAtBecomesAt() {
        assertEquals("@", strategy.unescape("@@"));
    }

    @Test
    void doubleAtUserDoubleAtDecoded() {
        assertEquals("@user@", strategy.unescape("@@user@@"));
    }
}
