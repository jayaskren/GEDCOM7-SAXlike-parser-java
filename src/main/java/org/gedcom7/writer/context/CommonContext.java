package org.gedcom7.writer.context;

import org.gedcom7.writer.GedcomWriterConfig;
import org.gedcom7.writer.Xref;
import org.gedcom7.writer.internal.LineEmitter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * Abstract base class for all typed context classes.
 *
 * <p>Provides escape hatch methods ({@code structure()}/{@code pointer()})
 * and common substructure methods ({@code note()}, {@code sourceCitation()},
 * {@code uid()}) inherited by every context.
 */
public abstract class CommonContext {

    private final LineEmitter emitter;
    private final int level;

    protected CommonContext(LineEmitter emitter, int level) {
        this.emitter = emitter;
        this.level = level;
    }

    protected LineEmitter emitter() {
        return emitter;
    }

    protected int level() {
        return level;
    }

    protected GedcomWriterConfig config() {
        return emitter.getConfig();
    }

    // --- Escape hatch: structure ---

    /**
     * Emits a substructure with a tag and value.
     */
    public void structure(String tag, String value) {
        emit(tag, null, value, null);
    }

    /**
     * Emits a substructure with a tag and nested children.
     */
    public void structure(String tag, Consumer<GeneralContext> body) {
        emit(tag, null, null, body);
    }

    /**
     * Emits a substructure with a tag, value, and nested children.
     */
    public void structure(String tag, String value, Consumer<GeneralContext> body) {
        emit(tag, null, value, body);
    }

    // --- Escape hatch: pointer ---

    /**
     * Emits a pointer substructure with an Xref reference.
     */
    public void pointer(String tag, Xref ref) {
        emitPointerLine(tag, ref.getId(), null);
    }

    /**
     * Emits a pointer substructure with a string ID.
     */
    public void pointer(String tag, String id) {
        emitPointerLine(tag, id, null);
    }

    /**
     * Emits a pointer substructure with an Xref reference and nested children.
     */
    public void pointer(String tag, Xref ref, Consumer<GeneralContext> body) {
        emitPointerLine(tag, ref.getId(), body);
    }

    /**
     * Emits a pointer substructure with a string ID and nested children.
     */
    public void pointer(String tag, String id, Consumer<GeneralContext> body) {
        emitPointerLine(tag, id, body);
    }

    // --- Common substructures ---

    /**
     * Adds a NOTE substructure with the given text.
     */
    public void note(String text) {
        if (text == null) return;
        emitWithCont("NOTE", text);
    }

    /**
     * Adds a SOUR citation pointer by Xref.
     */
    public void sourceCitation(Xref ref) {
        pointer("SOUR", ref);
    }

    /**
     * Adds a SOUR citation pointer by string ID.
     */
    public void sourceCitation(String id) {
        pointer("SOUR", id);
    }

    /**
     * Adds a SOUR citation pointer with detail body by Xref.
     */
    public void sourceCitation(Xref ref, Consumer<SourceCitationContext> body) {
        try {
            emitter.emitPointerLine(level + 1, "SOUR", "@" + ref.getId() + "@");
            if (body != null) {
                SourceCitationContext ctx = new SourceCitationContext(emitter, level + 1);
                body.accept(ctx);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Adds a SOUR citation pointer with detail body by string ID.
     */
    public void sourceCitation(String id, Consumer<SourceCitationContext> body) {
        sourceCitation(Xref.of(id), body);
    }

    /**
     * Adds a UID substructure.
     */
    public void uid(String uid) {
        if (uid == null) return;
        structure("UID", uid);
    }

    // --- Event helper ---

    /**
     * Emits an event substructure with the given tag and provides a typed
     * {@link EventContext} to the body lambda for adding date, place, etc.
     */
    protected void emitEvent(String tag, Consumer<EventContext> body) {
        try {
            emitter.emitLine(level + 1, null, tag, null);
            if (body != null) {
                EventContext ctx = new EventContext(emitter, level + 1);
                body.accept(ctx);
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    // --- Internal helpers ---

    /**
     * Emits a structure line with optional value and optional body.
     * Used by both typed methods and escape hatches.
     */
    protected void emit(String tag, String xref, String value, Consumer<GeneralContext> body) {
        try {
            if (value != null && value.indexOf('\n') >= 0) {
                emitter.emitValueWithCont(level + 1, xref, tag, value);
            } else {
                emitter.emitLine(level + 1, xref, tag, value);
            }
            if (body != null) {
                GeneralContext ctx = new GeneralContext(emitter, level + 1);
                body.accept(ctx);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Emits a value that may need CONT splitting.
     */
    protected void emitWithCont(String tag, String value) {
        try {
            emitter.emitValueWithCont(level + 1, null, tag, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Emits a pointer line with optional body.
     */
    private void emitPointerLine(String tag, String id, Consumer<GeneralContext> body) {
        try {
            emitter.emitPointerLine(level + 1, tag, "@" + id + "@");
            if (body != null) {
                GeneralContext ctx = new GeneralContext(emitter, level + 1);
                body.accept(ctx);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
