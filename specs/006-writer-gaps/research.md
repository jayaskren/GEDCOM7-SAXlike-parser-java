# Research: Writer Gaps Remediation

**Feature**: 006-writer-gaps | **Date**: 2026-03-05

## Summary

No significant unknowns require research. All changes are incremental additions to a well-understood existing codebase. The technical context from the spec and clarification session resolved all ambiguities.

## Decisions

### D1: sharedNote Method Naming

- **Decision**: Use `sharedNoteWithText` for the text-bearing overloads
- **Rationale**: Avoids Java method signature ambiguity with existing `sharedNote(String id, Consumer)`. Chosen by user during clarification.
- **Alternatives considered**: (A) Replace existing sharedNote(String, Consumer) — rejected, breaks backward compatibility. (C) Builder/options pattern — rejected, over-engineering for one method.

### D2: Calendar Escape Pattern Matching

- **Decision**: Allow-list of 6 known GEDCOM calendars (`@#DGREGORIAN@`, `@#DJULIAN@`, `@#DHEBREW@`, `@#DFRENCH R@`, `@#DROMAN@`, `@#DUNKNOWN@`)
- **Rationale**: GEDCOM is a mature specification unlikely to add new calendars. Allow-list is more precise and avoids false positives on unrecognized patterns. Chosen by user during clarification.
- **Alternatives considered**: Generic regex `@#D[^@]+@` — rejected by user preference for precision.

### D3: US11 Structure Validation Scope

- **Decision**: Fully deferred. Implement US1-US10 only.
- **Rationale**: US11 adds complexity (schema allow-lists, validation mode config) with no immediate user value. The streaming writer architecture makes validation difficult without buffering. Chosen by user during clarification.
- **Alternatives considered**: Implement infrastructure only (config + interface stub) — rejected, YAGNI.

### D4: emitEvent Refactoring as Foundation

- **Decision**: Extract `emitEvent` to CommonContext as the first implementation step (before parallel work begins)
- **Rationale**: US3 (LDS), US7 (generic event) both add methods that call `emitEvent`. Extracting it first to CommonContext as a protected method means all subsequent work can use the shared method directly, avoiding merge conflicts from duplicate independent extractions.
- **Alternatives considered**: Let each agent extract independently — rejected, would cause merge conflicts.

### D5: GedcomWriteException Migration Ordering

- **Decision**: Change to RuntimeException early (Phase A), before parallel work
- **Rationale**: Changing the exception hierarchy affects all `throws` declarations and test code. Doing it first prevents parallel agents from writing code with now-unnecessary `throws GedcomWriteException` clauses or conflicting exception handling.
- **Alternatives considered**: Do it in parallel — rejected, too many files touched by exception change.

### D6: Calendar Escape Handling Location

- **Decision**: Implement in `LineEmitter.escapeAt()` rather than in `EventContext.date()`
- **Rationale**: The `escapeAt` method in LineEmitter is where `@`-doubling happens. Adding calendar escape detection there keeps the logic centralized. However, calendar escapes only appear in DATE values, not all values. Two approaches: (a) Add a flag/method to LineEmitter to skip escaping for recognized calendar prefixes, or (b) Pre-process in EventContext.date() to separate the calendar prefix from the rest. Approach (b) is simpler: EventContext.date() strips the calendar prefix, emits the DATE value with the prefix passed through unescaped, and lets LineEmitter handle normal escaping on the remainder.
- **Alternatives considered**: Modify LineEmitter.escapeAt globally — rejected, would affect all values not just dates.
