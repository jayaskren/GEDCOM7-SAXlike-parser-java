package org.gedcom7.writer.context;

import org.gedcom7.writer.Xref;
import org.gedcom7.writer.internal.LineEmitter;

import java.util.function.Consumer;

/**
 * Context for the HEAD record.
 */
public final class HeadContext extends CommonContext {

    public HeadContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    /**
     * Sets the source system (SOUR).
     */
    public void source(String value) {
        if (value == null) return;
        structure("SOUR", value);
    }

    /**
     * Sets the source system with children (SOUR).
     */
    public void source(String value, Consumer<GeneralContext> body) {
        if (value == null) return;
        structure("SOUR", value, body);
    }

    /**
     * Sets the destination system (DEST).
     */
    public void destination(String value) {
        if (value == null) return;
        structure("DEST", value);
    }

    /**
     * Sets the submitter reference (SUBM).
     */
    public void submitterRef(Xref ref) {
        pointer("SUBM", ref);
    }

    /**
     * Sets the submitter reference by string ID (SUBM).
     */
    public void submitterRef(String id) {
        pointer("SUBM", id);
    }

    /**
     * Adds a NOTE to the HEAD record.
     */
    @Override
    public void note(String text) {
        if (text == null) return;
        emitWithCont("NOTE", text);
    }

    /**
     * Adds a SCHMA (schema) substructure.
     */
    public void schema(Consumer<SchemaContext> body) {
        try {
            emitter().emitLine(level() + 1, null, "SCHMA", null);
            if (body != null) {
                SchemaContext ctx = new SchemaContext(emitter(), level() + 1);
                body.accept(ctx);
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
