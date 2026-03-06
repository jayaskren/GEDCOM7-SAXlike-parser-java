package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

/**
 * Fallback context for escape hatch lambdas.
 *
 * <p>Provides all methods inherited from {@link CommonContext}
 * (escape hatches + common substructures) but adds no
 * additional typed methods.
 */
public final class GeneralContext extends CommonContext {

    public GeneralContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }
}
