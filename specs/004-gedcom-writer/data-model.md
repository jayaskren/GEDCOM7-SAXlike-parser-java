# Data Model: GEDCOM SAX-like Writer

**Feature**: 004-gedcom-writer | **Date**: 2026-03-05

## Core Entities

### GedcomWriter

The main entry point. Wraps an `OutputStream` and `GedcomWriterConfig`.

| Field | Type | Description |
|-------|------|-------------|
| out | OutputStream | Target output stream |
| config | GedcomWriterConfig | Immutable configuration |
| emitter | LineEmitter | Internal line formatter/writer |
| xrefGenerator | XrefGenerator | Auto-generates xref IDs |
| headWritten | boolean | Tracks whether HEAD has been emitted |
| trlrWritten | boolean | Tracks whether TRLR has been emitted |
| closed | boolean | Guards against post-close usage |

**Lifecycle**: Created → records written → close() auto-appends TRLR if needed → closed.

**Public methods**:
- `head(Consumer<HeadContext>)` — write HEAD record
- `individual(Consumer<IndividualContext>)` → Xref — write INDI record (auto-generated xref)
- `individual(String id, Consumer<IndividualContext>)` → Xref — write INDI with developer ID
- `family(Consumer<FamilyContext>)` → Xref
- `family(String id, Consumer<FamilyContext>)` → Xref
- `source(Consumer<SourceContext>)` → Xref
- `source(String id, Consumer<SourceContext>)` → Xref
- `repository(Consumer<RepositoryContext>)` → Xref
- `multimedia(Consumer<MultimediaContext>)` → Xref
- `submitter(Consumer<SubmitterContext>)` → Xref
- `sharedNote(Consumer<NoteContext>)` → Xref
- `record(String tag, Consumer<GeneralContext>)` → Xref — escape hatch for arbitrary record types
- `record(String id, String tag, Consumer<GeneralContext>)` → Xref
- `record(String tag, String value)` — value-only record escape hatch
- `close()` — auto-generate HEAD if needed, auto-append TRLR, flush, close

### GedcomWriterConfig

Immutable configuration. Builder pattern with factory methods.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| version | GedcomVersion | GEDCOM_7 | Target GEDCOM version |
| strict | boolean | false | Strict mode throws on issues |
| warningHandler | WarningHandler | LoggingWarningHandler | Receives warnings in lenient mode; null suppresses |
| maxLineLength | int | 0 (unlimited) | Max line length for CONC splitting (5.5.5); 0 disables |
| lineEnding | String | "\n" | Line terminator (LF or CRLF) |
| escapeAllAt | boolean | false | GEDCOM 7: leading-only; 5.5.5: all @@ |
| concEnabled | boolean | false | GEDCOM 7: no CONC; 5.5.5: CONC splitting enabled |

**Factory methods**: `gedcom7()`, `gedcom7Strict()`, `gedcom555()`, `gedcom555Strict()`

### Xref

Lightweight, immutable cross-reference handle.

| Field | Type | Description |
|-------|------|-------------|
| id | String | The bare xref identifier (without @ delimiters) |

**Construction**: Returned from record-creating methods or created from a string via `Xref.of("I1")`.

**Usage**: Passed to pointer methods. The writer wraps in `@..@` automatically.

### WarningHandler

Functional interface.

```java
@FunctionalInterface
public interface WarningHandler {
    void handle(GedcomWriteWarning warning);
}
```

### GedcomWriteWarning

Structured warning value object.

| Field | Type | Description |
|-------|------|-------------|
| message | String | Human-readable warning message |
| tag | String | GEDCOM tag that triggered the warning (nullable) |

### GedcomWriteException

Checked exception thrown in strict mode. Extends `Exception`.

| Field | Type | Description |
|-------|------|-------------|
| message | String | Error description |

## Date Entities

### WriterDate

Immutable value object representing a GEDCOM date to be written. Created via `GedcomDateBuilder` factory methods or `WriterDate.raw(String)`.

| Field | Type | Description |
|-------|------|-------------|
| type | DateType | EXACT, ABOUT, CALCULATED, ESTIMATED, BEFORE, AFTER, BETWEEN, FROM, TO, FROM_TO, RAW |
| calendar | Calendar | GREGORIAN, JULIAN, HEBREW, FRENCH_REPUBLICAN |
| date1 | DateComponents | Primary date (or start date for ranges/periods) |
| date2 | DateComponents | End date for BETWEEN/FROM_TO (nullable) |
| rawString | String | Raw string for RAW type (nullable) |

### DateComponents

Internal value holding the parts of a single date point.

| Field | Type | Description |
|-------|------|-------------|
| day | int | Day of month, or -1 if absent |
| month | Object | Month enum (Month, HebrewMonth, or FrenchRepublicanMonth), nullable |
| year | int | Year (always present) |
| bce | boolean | Whether this is a BCE date |

### Month (enum)

Gregorian/Julian month abbreviations: `JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC`

Each value has:
- `abbreviation()` → String (e.g., "JAN")
- `maxDay()` → int (e.g., 31 for JAN, 28 for FEB — no leap year enforcement)

### HebrewMonth (enum)

`TSH, CSH, KSL, TVT, SHV, ADR, ADS, NSN, IYR, SVN, TMZ, AAV, ELL`

### FrenchRepublicanMonth (enum)

`VEND, BRUM, FRIM, NIVO, PLUV, VENT, GERM, FLOR, PRAI, MESS, THER, FRUC, COMP`

## Context Entities

### CommonContext (abstract base)

All contexts share these methods:

| Method | Signature | Description |
|--------|-----------|-------------|
| structure | (String tag, String value) | Escape hatch: tag with value |
| structure | (String tag, Consumer<GeneralContext> body) | Escape hatch: tag with children |
| structure | (String tag, String value, Consumer<GeneralContext> body) | Escape hatch: tag with value AND children |
| pointer | (String tag, Xref ref) | Escape hatch: pointer by Xref |
| pointer | (String tag, String id) | Escape hatch: pointer by string ID |
| pointer | (String tag, Xref ref, Consumer<GeneralContext> body) | Escape hatch: pointer with children |
| pointer | (String tag, String id, Consumer<GeneralContext> body) | Escape hatch: pointer with children |
| note | (String text) | Add a NOTE substructure |
| sourceCitation | (Xref ref) | Add a SOUR citation pointer |
| sourceCitation | (Xref ref, Consumer<SourceCitationContext> body) | SOUR citation with details |
| sourceCitation | (String id) | SOUR citation by string ID |
| sourceCitation | (String id, Consumer<SourceCitationContext> body) | SOUR citation with details |
| uid | (String uid) | Add a UID substructure |

### IndividualContext

Extends CommonContext. Adds:

| Method | Description |
|--------|-------------|
| personalName(String value) | NAME with value only |
| personalName(String value, Consumer<PersonalNameContext> body) | NAME with value + children |
| birth(Consumer<EventContext> body) | BIRT event |
| death(Consumer<EventContext> body) | DEAT event |
| sex(String value) | SEX |
| familyAsSpouse(Xref/String ref) | FAMS (with version-aware warning) |
| familyAsChild(Xref/String ref) | FAMC (with version-aware warning) |
| christening(Consumer<EventContext> body) | CHR |
| burial(Consumer<EventContext> body) | BURI |
| residence(Consumer<EventContext> body) | RESI |
| occupation(String value) | OCCU |
| education(String value) | EDUC |
| religion(String value) | RELI |

### FamilyContext

| Method | Description |
|--------|-------------|
| husband(Xref/String ref) | HUSB pointer |
| wife(Xref/String ref) | WIFE pointer |
| child(Xref/String ref) | CHIL pointer |
| marriage(Consumer<EventContext> body) | MARR event |
| divorce(Consumer<EventContext> body) | DIV event |
| annulment(Consumer<EventContext> body) | ANUL event |

### EventContext

| Method | Description |
|--------|-------------|
| date(WriterDate date) | DATE with GedcomDateBuilder output |
| date(String raw) | DATE with raw string |
| place(String value) | PLAC |
| place(String value, Consumer<GeneralContext> body) | PLAC with children (MAP, FORM) |
| address(Consumer<AddressContext> body) | ADDR |
| cause(String value) | CAUS |
| agency(String value) | AGNC |
| type(String value) | TYPE |

### PersonalNameContext

| Method | Description |
|--------|-------------|
| givenName(String value) | GIVN |
| surname(String value) | SURN |
| namePrefix(String value) | NPFX |
| nameSuffix(String value) | NSFX |
| nickname(String value) | NICK |
| surnamePrefix(String value) | SPFX |
| type(String value) | TYPE |

### SourceCitationContext

| Method | Description |
|--------|-------------|
| page(String value) | PAGE |
| data(Consumer<GeneralContext> body) | DATA with children |
| quality(String value) | QUAY |
| eventType(String value) | EVEN |
| role(String value) | ROLE |

### HeadContext

| Method | Description |
|--------|-------------|
| source(String value) | SOUR (simple) |
| source(String value, Consumer<GeneralContext> body) | SOUR with children (VERS, NAME, etc.) |
| destination(String value) | DEST |
| submitterRef(Xref/String ref) | SUBM pointer |
| note(String text) | NOTE |
| schema(Consumer<SchemaContext> body) | SCHMA |

### SchemaContext

| Method | Description |
|--------|-------------|
| tag(String extensionTag, String uri) | TAG entry |

### AddressContext

| Method | Description |
|--------|-------------|
| line1(String value) | ADR1 |
| line2(String value) | ADR2 |
| line3(String value) | ADR3 |
| city(String value) | CITY |
| state(String value) | STAE |
| postalCode(String value) | POST |
| country(String value) | CTRY |

## Internal Entities

### LineEmitter

Handles low-level line formatting and writing to the OutputStream.

| Method | Description |
|--------|-------------|
| emitLine(int level, String xref, String tag, String value) | Write a single GEDCOM line |
| emitValueWithCont(int level, String tag, String value) | Write value with CONT splitting |
| flush() | Flush the underlying stream |

### XrefGenerator

Maintains per-prefix counters for auto-generating unique xref IDs.

| Method | Description |
|--------|-------------|
| next(String prefix) | Returns next xref ID for the given prefix (e.g., "I" → "I1", "I2", ...) |

## Entity Relationships

```
GedcomWriter ──owns──> GedcomWriterConfig
GedcomWriter ──owns──> LineEmitter
GedcomWriter ──owns──> XrefGenerator
GedcomWriter ──creates──> *Context (all context types)
GedcomWriter ──returns──> Xref (from record methods)

GedcomWriterConfig ──references──> WarningHandler
GedcomWriterConfig ──references──> GedcomVersion (enum from parser)

*Context ──uses──> LineEmitter (writes lines)
*Context ──uses──> GedcomWriterConfig (checks strict/lenient, version)
EventContext ──accepts──> WriterDate (for date values)

WriterDate ──created-by──> GedcomDateBuilder (static factory)
WriterDate ──uses──> Month / HebrewMonth / FrenchRepublicanMonth (enums)
```
