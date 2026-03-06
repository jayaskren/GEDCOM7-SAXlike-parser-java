package org.gedcom7.writer.context;

import org.gedcom7.writer.Xref;
import org.gedcom7.writer.internal.LineEmitter;

import java.util.function.Consumer;

/**
 * Context for SOUR (source) records.
 */
public final class SourceContext extends CommonContext {

    public SourceContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    public void title(String value) {
        if (value == null) return;
        emitWithCont("TITL", value);
    }

    public void author(String value) {
        if (value == null) return;
        emitWithCont("AUTH", value);
    }

    public void publicationFacts(String value) {
        if (value == null) return;
        emitWithCont("PUBL", value);
    }

    public void abbreviation(String value) {
        if (value == null) return;
        structure("ABBR", value);
    }

    public void repositoryCitation(Xref ref) {
        pointer("REPO", ref);
    }

    public void repositoryCitation(String id) {
        pointer("REPO", id);
    }

    public void repositoryCitation(Xref ref, Consumer<GeneralContext> body) {
        pointer("REPO", ref, body);
    }

    public void text(String value) {
        if (value == null) return;
        emitWithCont("TEXT", value);
    }
}
