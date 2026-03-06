package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

/**
 * Context for ADDR substructures.
 */
public final class AddressContext extends CommonContext {

    public AddressContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    public void line1(String value) {
        if (value == null) return;
        structure("ADR1", value);
    }

    public void line2(String value) {
        if (value == null) return;
        structure("ADR2", value);
    }

    public void line3(String value) {
        if (value == null) return;
        structure("ADR3", value);
    }

    public void city(String value) {
        if (value == null) return;
        structure("CITY", value);
    }

    public void state(String value) {
        if (value == null) return;
        structure("STAE", value);
    }

    public void postalCode(String value) {
        if (value == null) return;
        structure("POST", value);
    }

    public void country(String value) {
        if (value == null) return;
        structure("CTRY", value);
    }
}
