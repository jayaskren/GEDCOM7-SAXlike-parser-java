# Data Model: Writer Gaps Remediation

**Feature**: 006-writer-gaps | **Date**: 2026-03-05

## New Entities

### Sex (Enum)

| Field | Type | Description |
|-------|------|-------------|
| MALE | enum constant | Maps to GEDCOM code `"M"` |
| FEMALE | enum constant | Maps to GEDCOM code `"F"` |
| INTERSEX | enum constant | Maps to GEDCOM code `"X"` |
| UNKNOWN | enum constant | Maps to GEDCOM code `"U"` |

**Properties**:
- `code`: `String` — the single-character GEDCOM tag value
- Constructor: `Sex(String code)`
- Method: `getCode()` returns the GEDCOM code string

**Package**: `org.gedcom7.writer`
**Visibility**: `public`

## Modified Entities

### GedcomWriteException

| Change | Before | After |
|--------|--------|-------|
| Superclass | `Exception` | `RuntimeException` |

No new fields or methods. Constructors unchanged.

### GedcomWriterConfig.Builder

| Method | Before | After |
|--------|--------|-------|
| `escapeAllAt(boolean)` | package-private | `public` |
| `concEnabled(boolean)` | package-private | `public` |

### CommonContext

| Addition | Signature | Visibility |
|----------|-----------|------------|
| emitEvent | `protected void emitEvent(String tag, Consumer<EventContext> body)` | `protected` |

Extracted from identical `private` methods in IndividualContext and FamilyContext.

### IndividualContext

| Addition | Signature | Notes |
|----------|-----------|-------|
| personalName | `public void personalName(String givenName, String surname)` | Auto-formats NAME + GIVN + SURN |
| personalName | `public void personalName(String givenName, String surname, Consumer<PersonalNameContext> body)` | Same + lambda body |
| sex | `public void sex(Sex value)` | Delegates to `sex(value.getCode())` |
| event | `public void event(String tag, Consumer<EventContext> body)` | Generic event with EventContext |
| ldsBaptism | `public void ldsBaptism(Consumer<EventContext> body)` | Emits BAPL |
| ldsConfirmation | `public void ldsConfirmation(Consumer<EventContext> body)` | Emits CONL |
| ldsEndowment | `public void ldsEndowment(Consumer<EventContext> body)` | Emits ENDL |
| ldsInitiatory | `public void ldsInitiatory(Consumer<EventContext> body)` | Emits INIL |
| ldsSealingToParents | `public void ldsSealingToParents(Consumer<EventContext> body)` | Emits SLGC |

Removes: `private void emitEvent(...)` (moved to CommonContext)

### FamilyContext

| Addition | Signature | Notes |
|----------|-----------|-------|
| event | `public void event(String tag, Consumer<EventContext> body)` | Generic event with EventContext |
| ldsSealingToSpouse | `public void ldsSealingToSpouse(Consumer<EventContext> body)` | Emits SLGS |

Removes: `private void emitEvent(...)` (moved to CommonContext)

### GedcomWriter

| Addition | Signature | Notes |
|----------|-----------|-------|
| sharedNoteWithText | `public Xref sharedNoteWithText(String text, Consumer<NoteContext> body)` | SNOTE with text value |
| sharedNoteWithText | `public Xref sharedNoteWithText(String id, String text, Consumer<NoteContext> body)` | SNOTE with dev ID + text |

HEAD auto-generation: `head()` method modified to emit `1 CHAR UTF-8` after GEDC/VERS when config version is GEDCOM 5.5.5.

### EventContext

| Change | Method | Notes |
|--------|--------|-------|
| Modified | `date(String)` | Detects calendar escape prefix from allow-list, passes through without `@`-doubling |

### LineEmitter (internal)

| Change | Method | Notes |
|--------|--------|-------|
| Modified | `emitLine(...)` or new overload | Support for pre-escaped values (calendar prefix) or EventContext handles splitting |

## Relationships

```
Sex ──uses──> IndividualContext.sex(Sex)
CommonContext ──inherited by──> IndividualContext, FamilyContext
  └── protected emitEvent() ──creates──> EventContext
GedcomWriter ──creates──> NoteContext (via sharedNoteWithText)
EventContext.date() ──detects──> GEDCOM 5.5.5 calendar escapes
```
