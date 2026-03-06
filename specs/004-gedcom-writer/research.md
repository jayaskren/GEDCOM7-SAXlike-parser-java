# Research: GEDCOM SAX-like Writer

**Feature**: 004-gedcom-writer | **Date**: 2026-03-05

## Decision 1: Writer Package Structure

**Decision**: New top-level `org.gedcom7.writer` package hierarchy, parallel to `org.gedcom7.parser`.

**Rationale**: The writer is a peer to the parser, not a sub-component. Java convention places parallel concerns at the same package level. The module name (`org.gedcom7.parser`) stays unchanged for backward compatibility — module names don't need to match package names.

**Alternatives considered**:
- `org.gedcom7.parser.writer` — rejected because the writer isn't subordinate to the parser; it just happens to live in the same library.
- Separate module `org.gedcom7.writer` — rejected because it adds build complexity (multi-module Gradle) for no user benefit. A single module with multiple exported packages is simpler.

## Decision 2: Date Types — Reuse vs New

**Decision**: Create new `WriterDate` in `org.gedcom7.writer.date` package. Do not reuse `org.gedcom7.parser.datatype.GedcomDate`.

**Rationale**: The existing `GedcomDate` is a parse result holding decomposed fields (calendar, year, month, day, epoch). The writer date needs to:
1. Be constructed via a builder with validation (day ranges, chronological ordering)
2. Render to a GEDCOM date string with version-aware formatting (GEDCOM 7 `JULIAN 1 JAN 1700` vs 5.5.5 `@#DJULIAN@ 1 JAN 1700`)
3. Support compound forms (ranges, periods, approximate) that `GedcomDate` doesn't model

These are fundamentally different responsibilities. Coupling them would violate SRP and complicate both.

**Alternatives considered**:
- Reuse `GedcomDate` with added `toGedcomString()` — rejected because `GedcomDate` would need to know about writer config (version) for rendering, creating a circular dependency.
- Shared interface — YAGNI for now. Can be added later if round-trip conversion becomes a user need.

**Existing date types in the parser**:
- `GedcomDate` — single date point (calendar, year, month, day, epoch)
- `GedcomDateRange` — BEF/AFT/BET...AND with two `GedcomDate` endpoints
- `GedcomDatePeriod` — FROM/TO/FROM...TO with two `GedcomDate` endpoints

The writer's `WriterDate` will model all these forms as a single sealed hierarchy (or tagged union) since the rendering logic needs to handle all forms.

## Decision 3: Context Class Hierarchy

**Decision**: Abstract `CommonContext` base class with `structure()`/`pointer()` escape hatches. All typed contexts extend it. `GeneralContext` extends it with no additional typed methods.

**Rationale**: Every context needs escape hatches (FR-003) and common methods (note, sourceCitation, uid). An abstract base provides these once. Typed contexts add domain-specific methods that delegate to `structure()`/`pointer()` internally (FR-004).

**Hierarchy**:
```
CommonContext (abstract)
├── GeneralContext          # escape hatch lambdas only
├── HeadContext             # HEAD-specific methods
│   └── SchemaContext       # SCHMA-specific methods
├── IndividualContext       # INDI-specific methods
│   └── PersonalNameContext # NAME-specific methods
├── FamilyContext           # FAM-specific methods
├── EventContext            # BIRT/DEAT/MARR-specific methods
├── SourceCitationContext   # SOUR citation methods
├── SourceContext           # SOUR record methods
├── RepositoryContext       # REPO record methods
├── MultimediaContext       # OBJE record methods
├── SubmitterContext        # SUBM record methods
├── NoteContext             # SNOTE/NOTE methods
└── AddressContext          # ADDR methods
```

**Alternatives considered**:
- Interface-based with default methods — rejected because contexts need mutable state (current level, LineEmitter reference). Abstract class is more natural.
- Single `Context` class with all methods — rejected because it undermines type safety and IDE discoverability (the whole point of Level 3 design).

## Decision 4: Line Emission Strategy

**Decision**: `LineEmitter` internal class wraps `OutputStream` and handles line formatting, CONT splitting, CONC splitting, and @@ escaping. Each context holds a reference to the shared `LineEmitter` and the current level.

**Rationale**: Centralizing line formatting in one place keeps context classes focused on API shape. The `LineEmitter` encapsulates version-specific behavior (GEDCOM 7 vs 5.5.5 differences in escaping and CONC).

**Line format**: `{level} {optional_xref} {tag} {optional_value}\n`

**CONT handling**: Multi-line values are split at `\n`. First line goes with the structure's tag. Subsequent lines become `CONT` substructures at `level+1`.

**CONC handling (5.5.5 only)**: Long single lines are split at `maxLineLength`. The split point is chosen at a character boundary. First segment goes with the tag. Subsequent segments become `CONC` at `level+1`.

**@@ escaping**:
- GEDCOM 7: Only the leading `@` is doubled (`@value` → `@@value`)
- GEDCOM 5.5.5: All `@` characters are doubled (`@a@b` → `@@a@@b`)

## Decision 5: Xref Generation

**Decision**: Auto-generated xrefs use a per-writer counter with configurable prefix. Default: `@I{n}@` for individuals, `@F{n}@` for families, `@S{n}@` for sources, `@R{n}@` for repositories, `@N{n}@` for notes, `@O{n}@` for multimedia, `@U{n}@` for submitters, `@X{n}@` for generic records.

**Rationale**: Tag-based prefixes make output human-readable and align with common GEDCOM conventions. A simple integer counter is sufficient — uniqueness within a file is all that's needed.

**Developer-provided IDs**: Passed as a `String` parameter. The writer wraps them in `@..@` delimiters automatically (FR-010). No validation is performed on the string content — the developer is responsible for uniqueness.

## Decision 6: Strict vs Lenient Implementation

**Decision**: `GedcomWriterConfig` holds a `strict` boolean and a `WarningHandler` reference. Warning-producing code paths check strict first (throw) then call the handler (if non-null).

**Pattern**:
```java
void warn(String message, String tag) {
    if (config.isStrict()) {
        throw new GedcomWriteException(message);
    }
    WarningHandler handler = config.getWarningHandler();
    if (handler != null) {
        handler.handle(new GedcomWriteWarning(message, tag));
    }
}
```

**Warning scenarios**:
- HEAD not called before close
- FAMS/FAMC used in GEDCOM 7 mode
- Non-standard extension tags without SCHMA declaration (if validation enabled)

## Decision 7: GedcomWriterConfig Pattern

**Decision**: Follow the exact same pattern as `GedcomReaderConfig` — immutable class with `Builder`, factory methods, `toBuilder()`.

**Factory methods**:
- `gedcom7()` — lenient, GEDCOM 7 defaults
- `gedcom7Strict()` — strict, GEDCOM 7 defaults
- `gedcom555()` — lenient, GEDCOM 5.5.5 defaults (CONC splitting, all-@@ escaping)
- `gedcom555Strict()` — strict, 5.5.5 defaults

This mirrors `GedcomReaderConfig.gedcom7()`, `gedcom7Strict()`, `gedcom555()`, `gedcom555Strict()` for API consistency.

## Decision 8: Output Encoding

**Decision**: UTF-8 only, LF line endings by default. CRLF configurable via `GedcomWriterConfig.Builder.lineEnding()`.

**Rationale**: GEDCOM 7 spec requires UTF-8. LF is the GEDCOM 7 default. CRLF support covers edge cases where consumers require Windows-style line endings.

## Decision 9: Lambda Execution Model

**Decision**: All `Consumer<XContext>` lambdas execute synchronously within the calling method. When `writer.individual(indi -> { ... })` returns, all GEDCOM lines for that individual have been written to the OutputStream.

**Rationale**: This is the simplest model and matches the streaming nature. No buffering, no deferred execution, no thread safety concerns. The lambda receives a context, calls methods on it (each method writes immediately), and when the lambda completes, the record is done.

## Decision 10: Error Handling for Lambda Exceptions

**Decision**: If a lambda throws an exception, it propagates to the caller. Lines already written within that lambda remain in the output stream (cannot be retracted — streaming). The writer remains usable for further writes.

**Rationale**: Streaming writers cannot retract output. This is a fundamental trade-off of the streaming model. Documenting this clearly is more honest than attempting complex rollback logic.
