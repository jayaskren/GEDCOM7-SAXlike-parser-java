# Contract: SimpleGedcomHandler

**Story**: US10 (Simplified Handler for Beginners)
**Package**: `org.gedcom7.parser`

## Class Definition

```java
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

    /**
     * Called for both level-0 records and nested substructures.
     *
     * @param level the level number (0 for records, >0 for substructures)
     * @param xref  the cross-reference identifier, or null
     * @param tag   the structure tag
     * @param value the structure's value, or null
     */
    public void onStructure(int level, String xref, String tag, String value) {
        // no-op default
    }

    /**
     * Called when a record or substructure ends.
     *
     * @param level the level number of the ending structure
     * @param tag   the structure tag
     */
    public void onEndStructure(int level, String tag) {
        // no-op default
    }

    // --- Internal delegation (final to prevent override conflicts) ---

    @Override
    public final void startRecord(int level, String xref, String tag,
                                  String value) {
        onStructure(level, xref, tag, value);
    }

    @Override
    public final void endRecord(String tag) {
        onEndStructure(0, tag);
    }

    @Override
    public final void startStructure(int level, String xref, String tag,
                                     String value, boolean isPointer,
                                     String uri) {
        onStructure(level, xref, tag, value);
    }

    @Override
    public final void endStructure(String tag) {
        // Note: level tracking needed here - store current level
        onEndStructure(currentLevel(), tag);
    }
}
```

## Usage Example

```java
GedcomHandler handler = new SimpleGedcomHandler() {
    @Override
    public void onStructure(int level, String xref, String tag,
                            String value) {
        System.out.println("  ".repeat(level) + tag
            + (value != null ? " " + value : ""));
    }

    @Override
    public void onEndStructure(int level, String tag) {
        // optional
    }
};
```

## Design Notes

- Delegation methods are `final` to prevent accidentally overriding both
  the unified callback and the original callback
- `startDocument`, `endDocument`, `warning`, `error`, `fatalError` remain
  directly overridable
- Level tracking for `endStructure` requires maintaining a level counter
  (since the original `endStructure(String tag)` doesn't include level)
