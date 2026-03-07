# Context-Aware Handler

## Problem

Every `GedcomHandler` implementation must manually track which record and parent structure the current event belongs to. This is error-prone boilerplate that every developer writes.

### Current approach (from QuickstartExamplesTest.java)

```java
List<String> indiXrefs = new ArrayList<>();
Map<String, List<String>> indiNames = new HashMap<>();
final String[] currentIndi = {null};  // ugly hack for anonymous class scope

GedcomHandler handler = new GedcomHandler() {
    @Override
    public void startRecord(int level, String xref, String tag) {
        if ("INDI".equals(tag)) {
            currentIndi[0] = xref;
            indiXrefs.add(xref);
            indiNames.put(xref, new ArrayList<>());
        } else {
            currentIndi[0] = null;  // manual cleanup
        }
    }

    @Override
    public void endRecord(String tag) {
        if ("INDI".equals(tag)) {
            currentIndi[0] = null;  // must remember to clear
        }
    }

    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {
        // Must check currentIndi manually every time
        if (currentIndi[0] != null && "NAME".equals(tag) && value != null) {
            indiNames.get(currentIndi[0]).add(value);
        }
    }
};
```

Problems with this pattern:
- Developer must declare state variables outside the handler (`currentIndi[0]`)
- Must remember to reset state in both `startRecord` (for new records) and `endRecord`
- No way to know the parent structure (e.g., is this DATE inside BIRT or MARR?)
- Every handler reimplements the same tracking logic
- The `String[]` hack is needed because anonymous classes can't capture mutable locals

## Proposed Solution

An optional `ContextAwareHandler` base class that automatically tracks record and structure context. Developers extend it instead of `GedcomHandler` when they need context.

### API

```java
public abstract class ContextAwareHandler extends GedcomHandler {

    /**
     * Returns the tag of the current level-0 record (e.g., "INDI", "FAM", "SOUR").
     * Returns null before the first record or after endDocument.
     */
    public final String currentRecordTag() { ... }

    /**
     * Returns the xref of the current level-0 record (e.g., "I1", "F1").
     * Returns null if the current record has no xref (HEAD, TRLR) or
     * before the first record.
     */
    public final String currentRecordXref() { ... }

    /**
     * Returns the tag of the immediate parent structure.
     * For level-1 structures, this is the record tag.
     * For level-2+ structures, this is the enclosing structure's tag.
     * Returns null at level 0.
     *
     * <p>Example: for a DATE inside BIRT inside INDI, parentTag() returns "BIRT".
     */
    public final String parentTag() { ... }

    /**
     * Returns the current nesting depth (0 = record level, 1 = first
     * substructure, etc.). Matches the level parameter from parser events.
     */
    public final int depth() { ... }
}
```

### Developer's code with ContextAwareHandler

```java
GedcomHandler handler = new ContextAwareHandler() {
    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {
        // Know which record we're in — no manual tracking
        if ("INDI".equals(currentRecordTag())) {

            if ("NAME".equals(tag) && value != null) {
                // We know this is an INDI NAME, not a REPO NAME
                String indiXref = currentRecordXref();
                names.computeIfAbsent(indiXref, k -> new ArrayList<>()).add(value);
            }

            if ("DATE".equals(tag) && "BIRT".equals(parentTag())) {
                // We know this DATE is inside BIRT, not DEAT or MARR
                birthDates.put(currentRecordXref(), value);
            }

            if ("DATE".equals(tag) && "DEAT".equals(parentTag())) {
                // Different handling for death dates
                deathDates.put(currentRecordXref(), value);
            }
        }
    }
};
```

### Extracting families with context

```java
GedcomHandler handler = new ContextAwareHandler() {
    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {
        if ("FAM".equals(currentRecordTag()) && isPointer) {
            String famXref = currentRecordXref();
            switch (tag) {
                case "HUSB":
                    families.get(famXref).setHusband(value);
                    break;
                case "WIFE":
                    families.get(famXref).setWife(value);
                    break;
                case "CHIL":
                    families.get(famXref).addChild(value);
                    break;
            }
        }
    }

    @Override
    public void startRecord(int level, String xref, String tag, String value) {
        if ("FAM".equals(tag)) {
            families.put(xref, new Family(xref));
        }
    }
};
```

## Internal Implementation Sketch

The implementation would maintain a small array (not a growing collection) to track the tag at each level:

```java
public abstract class ContextAwareHandler extends GedcomHandler {

    private String recordTag;
    private String recordXref;
    private final String[] tagStack = new String[128]; // max nesting depth
    private int currentDepth = -1;

    @Override
    public final void startRecord(int level, String xref, String tag, String value) {
        recordTag = tag;
        recordXref = xref;
        currentDepth = 0;
        tagStack[0] = tag;
        onStartRecord(level, xref, tag, value);
    }

    @Override
    public final void startStructure(int level, String xref, String tag,
                                     String value, boolean isPointer) {
        currentDepth = level;
        tagStack[level] = tag;
        onStartStructure(level, xref, tag, value, isPointer);
    }

    @Override
    public final void endRecord(String tag) {
        onEndRecord(tag);
        recordTag = null;
        recordXref = null;
        currentDepth = -1;
    }

    // Context accessors
    public final String currentRecordTag()  { return recordTag; }
    public final String currentRecordXref() { return recordXref; }
    public final String parentTag()         { return currentDepth > 0 ? tagStack[currentDepth - 1] : null; }
    public final int    depth()             { return currentDepth; }

    // Override points for subclasses (instead of the GedcomHandler methods)
    protected void onStartRecord(int level, String xref, String tag, String value) {}
    protected void onStartStructure(int level, String xref, String tag,
                                    String value, boolean isPointer) {}
    protected void onEndRecord(String tag) {}
    protected void onEndStructure(String tag) {}
    // ... other forwarding methods
}
```

Key implementation details:
- Fixed-size `tagStack` array — no allocations in the hot loop (Principle III)
- `final` on the `GedcomHandler` overrides — ensures context is always maintained
- Subclasses override `onStartRecord` / `onStartStructure` etc. instead
- Context accessors are `final` — no risk of subclass breaking invariants

## Design Boundaries

### In scope (keeps Principle VI compliance)
- Current record tag and xref
- Parent tag (one level up)
- Current depth
- Optional base class — plain `GedcomHandler` still works

### Out of scope (would violate Principle VI)
- Full path tracking (e.g., `INDI > BIRT > DATE > TIME`) — that's approaching a tree
- Accumulating children or counting siblings
- Remembering values from previous structures
- Any form of record buffering or accumulation

## Constitution Compliance

| Principle | Assessment |
|-----------|-----------|
| I. GEDCOM 7 Compliance | No impact — doesn't change parsing behavior |
| II. SAX-like Streaming | Compliant — still event-driven, no buffering |
| III. Mechanical Sympathy | Fixed-size array, no allocations in hot loop |
| IV. Java Best Practices | Clean inheritance, final methods for invariants |
| V. Test-Driven | Would need tests for context accuracy |
| VI. Simplicity/YAGNI | Borderline — justified by reducing universal boilerplate |
| VII. Zero Dependencies | No new dependencies |

## Open Questions

1. Should `parentTag()` return only the immediate parent, or should we expose `tagAt(int level)` for arbitrary depth access?
2. Should the `onStart*` methods mirror the exact `GedcomHandler` signatures, or simplify them (e.g., drop the `level` parameter since `depth()` provides it)?
3. Should this live in `org.gedcom7.parser` alongside `GedcomHandler`, or in a separate `org.gedcom7.parser.util` package?
