package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

import java.util.function.Consumer;

/**
 * Context for SUBM (submitter) records.
 */
public final class SubmitterContext extends CommonContext {

    public SubmitterContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    public void name(String value) {
        if (value == null) return;
        structure("NAME", value);
    }

    public void address(Consumer<AddressContext> body) {
        try {
            emitter().emitLine(level() + 1, null, "ADDR", null);
            if (body != null) {
                AddressContext ctx = new AddressContext(emitter(), level() + 1);
                body.accept(ctx);
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
