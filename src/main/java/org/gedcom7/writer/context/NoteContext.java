package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

/**
 * Context for SNOTE/NOTE records.
 *
 * <p>The note value is set at record creation. Children are added
 * via the escape hatch methods inherited from {@link CommonContext}.
 */
public final class NoteContext extends CommonContext {

    public NoteContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }
}
