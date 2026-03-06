package org.gedcom7.writer.context;

import org.gedcom7.writer.GedcomWriteException;
import org.gedcom7.writer.GedcomWriteWarning;
import org.gedcom7.writer.Sex;
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

    /**
     * Adds a personal name from given name and surname components.
     *
     * <p>Formats the NAME value as {@code "givenName /surname/"} and
     * automatically emits GIVN and SURN substructures for any non-null,
     * non-empty components.
     */
    public void personalName(String givenName, String surname) {
        personalName(givenName, surname, null);
    }

    /**
     * Adds a personal name from given name and surname components,
     * with an optional body for additional substructures.
     *
     * <p>Formats the NAME value as {@code "givenName /surname/"} and
     * automatically emits GIVN and SURN substructures for any non-null,
     * non-empty components.
     */
    public void personalName(String givenName, String surname, Consumer<PersonalNameContext> body) {
        boolean hasGiven = givenName != null && !givenName.isEmpty();
        boolean hasSurname = surname != null && !surname.isEmpty();

        if (!hasGiven && !hasSurname) {
            return;
        }

        // Build the NAME value: "givenName /surname/"
        StringBuilder nameValue = new StringBuilder();
        if (hasGiven) {
            nameValue.append(givenName);
        }
        if (hasSurname) {
            if (hasGiven) {
                nameValue.append(' ');
            }
            nameValue.append('/').append(surname).append('/');
        }

        try {
            emitter().emitLine(level() + 1, null, "NAME", nameValue.toString());
            PersonalNameContext ctx = new PersonalNameContext(emitter(), level() + 1);
            if (hasGiven) {
                ctx.givenName(givenName);
            }
            if (hasSurname) {
                ctx.surname(surname);
            }
            if (body != null) {
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

    /**
     * Sets the sex using the {@link Sex} enum.
     *
     * @param value the sex value, or {@code null} to omit the SEX line
     */
    public void sex(Sex value) {
        if (value == null) return;
        sex(value.getCode());
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

    // --- LDS Ordinances ---

    public void ldsBaptism(Consumer<EventContext> body) {
        emitEvent("BAPL", body);
    }

    public void ldsConfirmation(Consumer<EventContext> body) {
        emitEvent("CONL", body);
    }

    public void ldsEndowment(Consumer<EventContext> body) {
        emitEvent("ENDL", body);
    }

    public void ldsInitiatory(Consumer<EventContext> body) {
        emitEvent("INIL", body);
    }

    public void ldsSealingToParents(Consumer<EventContext> body) {
        emitEvent("SLGC", body);
    }

    // --- Generic event ---

    /**
     * Emits a generic event with the given tag.
     */
    public void event(String tag, Consumer<EventContext> body) {
        emitEvent(tag, body);
    }

    // --- Internal helpers ---

    private void warnIfGedcom7(String tag) {
        if (config().getVersion().isGedcom7()) {
            WarningHandler handler = config().getWarningHandler();
            if (config().isStrict()) {
                throw new GedcomWriteException(tag + " is deprecated in GEDCOM 7; use family record pointers instead");
            }
            if (handler != null) {
                handler.handle(new GedcomWriteWarning(
                        tag + " is deprecated in GEDCOM 7; use family record pointers instead", tag));
            }
        }
    }
}
