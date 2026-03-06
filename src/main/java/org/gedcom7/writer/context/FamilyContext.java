package org.gedcom7.writer.context;

import org.gedcom7.writer.Xref;
import org.gedcom7.writer.internal.LineEmitter;

import java.util.function.Consumer;

/**
 * Context for FAM (family) records.
 */
public final class FamilyContext extends CommonContext {

    public FamilyContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    // --- Pointers ---

    public void husband(Xref ref) {
        pointer("HUSB", ref);
    }

    public void husband(String id) {
        pointer("HUSB", id);
    }

    public void wife(Xref ref) {
        pointer("WIFE", ref);
    }

    public void wife(String id) {
        pointer("WIFE", id);
    }

    public void child(Xref ref) {
        pointer("CHIL", ref);
    }

    public void child(String id) {
        pointer("CHIL", id);
    }

    // --- Events ---

    public void marriage(Consumer<EventContext> body) {
        emitEvent("MARR", body);
    }

    public void divorce(Consumer<EventContext> body) {
        emitEvent("DIV", body);
    }

    public void annulment(Consumer<EventContext> body) {
        emitEvent("ANUL", body);
    }

    // --- LDS Ordinances ---

    public void ldsSealingToSpouse(Consumer<EventContext> body) {
        emitEvent("SLGS", body);
    }

    // --- Generic event ---

    /**
     * Emits a generic event with the given tag.
     */
    public void event(String tag, Consumer<EventContext> body) {
        emitEvent(tag, body);
    }

}
