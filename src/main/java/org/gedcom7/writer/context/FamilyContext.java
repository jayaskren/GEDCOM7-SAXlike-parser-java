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
}
