package org.gedcom7.parser.internal;

import org.gedcom7.parser.spi.AtEscapeStrategy;

/**
 * GEDCOM 7 escape strategy: only a leading {@code @@} is
 * decoded to {@code @}. Non-leading {@code @@} sequences
 * are left untouched.
 */
public final class LeadingAtEscapeStrategy implements AtEscapeStrategy {

    @Override
    public String unescape(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if (value.charAt(0) == '@' && value.charAt(1) == '@') {
            return value.substring(1);
        }
        return value;
    }
}
