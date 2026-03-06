# Contract: GedcomHandler startRecord with Value

**Story**: US5 (Record Payloads for Level-0 Records)
**Package**: `org.gedcom7.parser`

## New Method on GedcomHandler

```java
/**
 * Called when a level-0 record is encountered.
 * This overload includes the record's payload value (if present).
 *
 * <p>The default implementation delegates to
 * {@link #startRecord(int, String, String)} for backward compatibility.
 *
 * @param level  the level number (always 0 for records)
 * @param xref   the cross-reference identifier, or null
 * @param tag    the record tag (e.g., "INDI", "FAM", "SNOTE")
 * @param value  the record's payload value, or null if none
 */
public void startRecord(int level, String xref, String tag, String value) {
    startRecord(level, xref, tag);
}
```

## Parser Behavior Change

The parser calls `handler.startRecord(level, xref, tag, value)` instead of
`handler.startRecord(level, xref, tag)`. Handlers that override only the
3-parameter version continue to receive events via the default delegation.

## Examples

```java
// Input:  0 @N1@ SNOTE This is a note
// Calls:  handler.startRecord(0, "@N1@", "SNOTE", "This is a note")

// Input:  0 @I1@ INDI
// Calls:  handler.startRecord(0, "@I1@", "INDI", null)
```
