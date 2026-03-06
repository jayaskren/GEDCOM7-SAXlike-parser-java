# Writer API Contract: Gaps Remediation Additions

**Feature**: 006-writer-gaps | **Date**: 2026-03-05

## New Public API

### Sex Enum

```java
package org.gedcom7.writer;

public enum Sex {
    MALE("M"),
    FEMALE("F"),
    INTERSEX("X"),
    UNKNOWN("U");

    private final String code;
    Sex(String code) { this.code = code; }
    public String getCode() { return code; }
}
```

### IndividualContext — New Methods

```java
// Personal name convenience (FR-001)
public void personalName(String givenName, String surname)
public void personalName(String givenName, String surname, Consumer<PersonalNameContext> body)

// Sex enum overload (FR-011)
public void sex(Sex value)

// Generic event (FR-008)
public void event(String tag, Consumer<EventContext> body)

// LDS ordinances (FR-004)
public void ldsBaptism(Consumer<EventContext> body)      // BAPL
public void ldsConfirmation(Consumer<EventContext> body)  // CONL
public void ldsEndowment(Consumer<EventContext> body)     // ENDL
public void ldsInitiatory(Consumer<EventContext> body)    // INIL
public void ldsSealingToParents(Consumer<EventContext> body) // SLGC
```

### FamilyContext — New Methods

```java
// Generic event (FR-008)
public void event(String tag, Consumer<EventContext> body)

// LDS sealing (FR-004)
public void ldsSealingToSpouse(Consumer<EventContext> body) // SLGS
```

### GedcomWriter — New Methods

```java
// Shared note with text (FR-003)
public Xref sharedNoteWithText(String text, Consumer<NoteContext> body)
public Xref sharedNoteWithText(String id, String text, Consumer<NoteContext> body)
```

### GedcomWriterConfig.Builder — Visibility Change

```java
// FR-005: changed from package-private to public
public Builder escapeAllAt(boolean escapeAllAt)
public Builder concEnabled(boolean concEnabled)
```

### GedcomWriteException — Hierarchy Change

```java
// FR-007: changed from checked to unchecked
public class GedcomWriteException extends RuntimeException { ... }
```

## Modified Behavior

### HEAD.CHAR Auto-generation (FR-006)

When `config.getVersion()` is GEDCOM 5.5.5, `GedcomWriter.head()` emits `1 CHAR UTF-8` after the `2 VERS 5.5.5` line. Applies to both explicit and auto-generated HEAD.

### Date Calendar Escape (FR-009)

`EventContext.date(String)` in GEDCOM 5.5.5 mode detects these prefixes and prevents `@`-doubling on them:
- `@#DGREGORIAN@`
- `@#DJULIAN@`
- `@#DHEBREW@`
- `@#DFRENCH R@`
- `@#DROMAN@`
- `@#DUNKNOWN@`

The rest of the date string follows normal escaping rules.

## Unchanged API

All existing method signatures remain unchanged:
- `personalName(String)`, `personalName(String, Consumer<PersonalNameContext>)`
- `sex(String)`
- `sharedNote(Consumer<NoteContext>)`, `sharedNote(String id, Consumer<NoteContext>)`
- `GedcomWriterConfig.gedcom7()`, `gedcom555()`, etc.
