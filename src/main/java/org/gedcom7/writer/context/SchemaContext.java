package org.gedcom7.writer.context;

import org.gedcom7.writer.internal.LineEmitter;

/**
 * Context for the HEAD.SCHMA substructure.
 */
public final class SchemaContext extends CommonContext {

    public SchemaContext(LineEmitter emitter, int level) {
        super(emitter, level);
    }

    /**
     * Declares an extension tag mapping (TAG).
     *
     * @param extensionTag the extension tag (e.g., "_CUSTOM")
     * @param uri the URI defining the tag's semantics
     */
    public void tag(String extensionTag, String uri) {
        if (extensionTag == null || uri == null) return;
        structure("TAG", extensionTag + " " + uri);
    }
}
