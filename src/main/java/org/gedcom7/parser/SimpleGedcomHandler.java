package org.gedcom7.parser;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A convenience handler that unifies record and structure events into
 * a single callback surface. Developers extend this class and override
 * {@link #onStructure} and {@link #onEndStructure} instead of dealing
 * with the distinction between records and substructures.
 *
 * <p>Document-level events ({@link #startDocument}, {@link #endDocument})
 * and error events ({@link #warning}, {@link #error}, {@link #fatalError})
 * pass through unchanged and can be overridden independently.
 */
public class SimpleGedcomHandler extends GedcomHandler {
    private final Deque<LevelAndTag> levelStack = new ArrayDeque<>();

    /**
     * Called for both level-0 records and nested substructures.
     *
     * @param level the nesting level (0 for records, 1+ for substructures)
     * @param xref  cross-reference identifier, or null
     * @param tag   structure tag
     * @param value payload value, or null
     */
    public void onStructure(int level, String xref, String tag, String value) {
        // no-op default
    }

    /**
     * Called when a record or substructure ends.
     *
     * @param level the nesting level of the structure that is ending
     * @param tag   the tag of the structure that is ending
     */
    public void onEndStructure(int level, String tag) {
        // no-op default
    }

    @Override
    public final void startRecord(int level, String xref, String tag, String value) {
        levelStack.push(new LevelAndTag(level, tag));
        onStructure(level, xref, tag, value);
    }

    @Override
    public final void endRecord(String tag) {
        int level = 0;
        if (!levelStack.isEmpty()) {
            LevelAndTag popped = levelStack.pop();
            level = popped.level;
        }
        onEndStructure(level, tag);
    }

    @Override
    public final void startStructure(int level, String xref, String tag,
                                     String value, boolean isPointer, String uri) {
        levelStack.push(new LevelAndTag(level, tag));
        onStructure(level, xref, tag, value);
    }

    @Override
    public final void endStructure(String tag) {
        int level = 0;
        if (!levelStack.isEmpty()) {
            LevelAndTag popped = levelStack.pop();
            level = popped.level;
        }
        onEndStructure(level, tag);
    }

    private static final class LevelAndTag {
        final int level;
        final String tag;

        LevelAndTag(int level, String tag) {
            this.level = level;
            this.tag = tag;
        }
    }
}
