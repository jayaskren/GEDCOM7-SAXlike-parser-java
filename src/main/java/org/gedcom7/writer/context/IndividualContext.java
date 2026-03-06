package org.gedcom7.writer.context;

import org.gedcom7.writer.GedcomWriteException;
import org.gedcom7.writer.GedcomWriteWarning;
import org.gedcom7.writer.WarningHandler;
import org.gedcom7.writer.Xref;
import org.gedcom7.writer.internal.LineEmitter;

import java.util.function.Consumer;

/**
 * Context for INDI (individual) records.
 */
public final class IndividualContext extends CommonContext {

    public IndividualContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    // --- Name ---

    /**
     * Adds a personal name (NAME).
     */
    public void personalName(String value) {
        if (value == null) return;
        structure("NAME", value);
    }

    /**
     * Adds a personal name with detail children (NAME).
     */
    public void personalName(String value, Consumer<PersonalNameContext> body) {
        if (value == null) return;
        try {
            emitter().emitLine(level() + 1, null, "NAME", value);
            if (body != null) {
                PersonalNameContext ctx = new PersonalNameContext(emitter(), level() + 1);
                body.accept(ctx);
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    // --- Events ---

    public void birth(Consumer<EventContext> body) {
        emitEvent("BIRT", body);
    }

    public void death(Consumer<EventContext> body) {
        emitEvent("DEAT", body);
    }

    public void christening(Consumer<EventContext> body) {
        emitEvent("CHR", body);
    }

    public void burial(Consumer<EventContext> body) {
        emitEvent("BURI", body);
    }

    public void residence(Consumer<EventContext> body) {
        emitEvent("RESI", body);
    }

    // --- Attributes ---

    public void sex(String value) {
        if (value == null) return;
        structure("SEX", value);
    }

    public void occupation(String value) {
        if (value == null) return;
        structure("OCCU", value);
    }

    public void education(String value) {
        if (value == null) return;
        structure("EDUC", value);
    }

    public void religion(String value) {
        if (value == null) return;
        structure("RELI", value);
    }

    // --- Family links (version-aware) ---

    public void familyAsSpouse(Xref ref) {
        warnIfGedcom7("FAMS");
        pointer("FAMS", ref);
    }

    public void familyAsSpouse(String id) {
        warnIfGedcom7("FAMS");
        pointer("FAMS", id);
    }

    public void familyAsChild(Xref ref) {
        warnIfGedcom7("FAMC");
        pointer("FAMC", ref);
    }

    public void familyAsChild(String id) {
        warnIfGedcom7("FAMC");
        pointer("FAMC", id);
    }

    // --- Internal helpers ---

    private void emitEvent(String tag, Consumer<EventContext> body) {
        try {
            emitter().emitLine(level() + 1, null, tag, null);
            if (body != null) {
                EventContext ctx = new EventContext(emitter(), level() + 1);
                body.accept(ctx);
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private void warnIfGedcom7(String tag) {
        if (config().getVersion().isGedcom7()) {
            WarningHandler handler = config().getWarningHandler();
            if (config().isStrict()) {
                throw new RuntimeException(
                        new GedcomWriteException(tag + " is deprecated in GEDCOM 7; use family record pointers instead"));
            }
            if (handler != null) {
                handler.handle(new GedcomWriteWarning(
                        tag + " is deprecated in GEDCOM 7; use family record pointers instead", tag));
            }
        }
    }
}
