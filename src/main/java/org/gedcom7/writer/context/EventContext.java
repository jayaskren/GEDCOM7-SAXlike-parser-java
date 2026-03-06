package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Context for event substructures (BIRT, DEAT, MARR, etc.).
 */
public final class EventContext extends CommonContext {

    /**
     * Recognized GEDCOM 5.5.5 calendar escape prefixes.
     * These must NOT be subject to @@-doubling when writing date values.
     */
    private static final List<String> CALENDAR_ESCAPES = Arrays.asList(
            "@#DGREGORIAN@",
            "@#DJULIAN@",
            "@#DHEBREW@",
            "@#DFRENCH R@",
            "@#DROMAN@",
            "@#DUNKNOWN@"
    );

    public EventContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    /**
     * Sets the date using a WriterDate object.
     * The date is rendered to a GEDCOM string using the configured version.
     */
    public void date(org.gedcom7.writer.date.WriterDate date) {
        if (date == null) return;
        structure("DATE", date.toGedcomString(config().getVersion()));
    }

    /**
     * Sets the date using a raw string.
     * In GEDCOM 5.5.5 mode, calendar escape prefixes (e.g., {@code @#DJULIAN@})
     * are preserved without @@-doubling, while the remainder of the date value
     * is still subject to normal @-escaping.
     */
    public void date(String rawDateString) {
        if (rawDateString == null) return;

        if (config().isEscapeAllAt()) {
            // GEDCOM 5.5.5 mode: check for calendar escape prefix
            for (String escape : CALENDAR_ESCAPES) {
                if (rawDateString.startsWith(escape)) {
                    String remainder = rawDateString.substring(escape.length());
                    try {
                        emitter().emitLinePreEscaped(
                                level() + 1, null, "DATE", escape, remainder);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    return;
                }
            }
        }

        // No calendar escape or GEDCOM 7 mode: use normal structure emission
        structure("DATE", rawDateString);
    }

    /**
     * Sets the place (PLAC).
     */
    public void place(String value) {
        if (value == null) return;
        structure("PLAC", value);
    }

    /**
     * Sets the place with children (PLAC).
     */
    public void place(String value, Consumer<GeneralContext> body) {
        if (value == null) return;
        structure("PLAC", value, body);
    }

    /**
     * Adds an address substructure (ADDR).
     */
    public void address(Consumer<AddressContext> body) {
        try {
            emitter().emitLine(level() + 1, null, "ADDR", null);
            if (body != null) {
                AddressContext ctx = new AddressContext(emitter(), level() + 1);
                body.accept(ctx);
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    /**
     * Sets the cause (CAUS).
     */
    public void cause(String value) {
        if (value == null) return;
        structure("CAUS", value);
    }

    /**
     * Sets the responsible agency (AGNC).
     */
    public void agency(String value) {
        if (value == null) return;
        structure("AGNC", value);
    }

    /**
     * Sets the type classification (TYPE).
     */
    public void type(String value) {
        if (value == null) return;
        structure("TYPE", value);
    }
}
