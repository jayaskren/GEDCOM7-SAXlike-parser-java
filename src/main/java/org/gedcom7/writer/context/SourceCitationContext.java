package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

import java.util.function.Consumer;

/**
 * Context for SOUR citation substructures.
 */
public final class SourceCitationContext extends CommonContext {

    public SourceCitationContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    /**
     * Sets the citation page reference (PAGE).
     */
    public void page(String value) {
        if (value == null) return;
        structure("PAGE", value);
    }

    /**
     * Adds a DATA substructure with children.
     */
    public void data(Consumer<GeneralContext> body) {
        structure("DATA", body);
    }

    /**
     * Sets the quality assessment (QUAY).
     */
    public void quality(String value) {
        if (value == null) return;
        structure("QUAY", value);
    }

    /**
     * Sets the event type (EVEN).
     */
    public void eventType(String value) {
        if (value == null) return;
        structure("EVEN", value);
    }

    /**
     * Sets the role (ROLE).
     */
    public void role(String value) {
        if (value == null) return;
        structure("ROLE", value);
    }
}
