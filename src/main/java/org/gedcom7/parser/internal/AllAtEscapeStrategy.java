package org.gedcom7.parser.internal;

import org.gedcom7.parser.spi.AtEscapeStrategy;

/**
 * GEDCOM 5.5.5 escape strategy: ALL @@ occurrences are
 * decoded to a single @, not just the leading one.
 */
public final class AllAtEscapeStrategy implements AtEscapeStrategy {

    @Override
    public String unescape(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("@@", "@");
    }
}
