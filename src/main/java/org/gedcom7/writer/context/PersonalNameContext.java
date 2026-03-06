package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

/**
 * Context for NAME substructures within an individual record.
 */
public final class PersonalNameContext extends CommonContext {

    public PersonalNameContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    public void givenName(String value) {
        if (value == null) return;
        structure("GIVN", value);
    }

    public void surname(String value) {
        if (value == null) return;
        structure("SURN", value);
    }

    public void namePrefix(String value) {
        if (value == null) return;
        structure("NPFX", value);
    }

    public void nameSuffix(String value) {
        if (value == null) return;
        structure("NSFX", value);
    }

    public void nickname(String value) {
        if (value == null) return;
        structure("NICK", value);
    }

    public void surnamePrefix(String value) {
        if (value == null) return;
        structure("SPFX", value);
    }

    public void type(String value) {
        if (value == null) return;
        structure("TYPE", value);
    }
}
