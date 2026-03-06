package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

import java.util.function.Consumer;

/**
 * Context for OBJE (multimedia) records.
 */
public final class MultimediaContext extends CommonContext {

    public MultimediaContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    public void file(String value) {
        if (value == null) return;
        structure("FILE", value);
    }

    public void file(String value, Consumer<GeneralContext> body) {
        if (value == null) return;
        structure("FILE", value, body);
    }
}
