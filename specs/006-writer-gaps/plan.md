# Implementation Plan: Writer Gaps Remediation

**Branch**: `006-writer-gaps` | **Date**: 2026-03-05 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/006-writer-gaps/spec.md`

## Summary

Ten incremental improvements to the GEDCOM writer API: convenience overloads (personal name, shared note text, Sex enum, generic event), LDS ordinance typed methods, public builder methods, automatic HEAD.CHAR for 5.5.5, unchecked exception migration, date calendar-escape fix, and emitEvent DRY refactoring. US11 (structure validation) is deferred.

## Technical Context

**Language/Version**: Java 11+
**Primary Dependencies**: None at runtime (zero-dependency library)
**Storage**: N/A (streaming writer)
**Testing**: JUnit 5.11.4, Gradle
**Target Platform**: Any JVM (library)
**Project Type**: Library
**Performance Goals**: N/A (all changes are API additions or trivial logic; no hot-path impact)
**Constraints**: Zero external runtime dependencies (Constitution Principle VII)
**Scale/Scope**: ~10 modified/new files, ~10 test files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. GEDCOM 7 Spec Compliance | PASS | LDS tags (BAPL, CONL, ENDL, INIL, SLGC, SLGS), NAME format, SNOTE text values, calendar escapes all per GEDCOM spec |
| II. SAX-like Event-Driven API | N/A | Writer, not parser. No streaming model impact. |
| III. Mechanical Sympathy | PASS | No hot-path changes. All are API surface additions. emitEvent refactoring eliminates duplication without perf impact. |
| IV. Java Best Practices | PASS | Immutability preserved (config). Unchecked exception for GedcomWriteException aligns with "unchecked for programming errors" and lambda ergonomics. Enum for Sex follows type-safety conventions. |
| V. Test-Driven Development | PASS | Each user story has independent test criteria defined in spec. |
| VI. Simplicity and YAGNI | PASS | US11 deferred. All changes are minimal additions. No unnecessary abstractions. |
| VII. Zero External Dependencies | PASS | No new dependencies. |
| VIII. Independent Verification | PASS | Per-task review + final evaluation required per spec §Verification Requirements. |

## Project Structure

### Documentation (this feature)

```text
specs/006-writer-gaps/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 output (minimal - no unknowns)
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── writer-api.md    # Public API contract additions
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/org/gedcom7/writer/
├── GedcomWriter.java              # MODIFIED: sharedNoteWithText overloads
├── GedcomWriteException.java      # MODIFIED: extends RuntimeException
├── GedcomWriterConfig.java        # MODIFIED: public escapeAllAt/concEnabled
├── Sex.java                       # NEW: Sex enum
├── context/
│   ├── CommonContext.java         # MODIFIED: protected emitEvent method
│   ├── IndividualContext.java     # MODIFIED: personalName overloads, LDS methods, event(), sex(Sex)
│   ├── FamilyContext.java         # MODIFIED: LDS sealing, event()
│   └── EventContext.java          # MODIFIED: date() calendar escape handling
└── internal/
    └── LineEmitter.java           # MODIFIED: calendar escape detection in escapeAt

src/test/java/org/gedcom7/writer/
├── PersonalNameOverloadTest.java  # NEW: US1 tests
├── SharedNoteTextTest.java        # NEW: US2 tests
├── LdsOrdinanceTest.java          # NEW: US3 tests
├── BuilderVisibilityTest.java     # NEW: US4 tests
├── HeadCharTest.java              # NEW: US5 tests
├── UncheckedExceptionTest.java    # NEW: US6 tests
├── GenericEventTest.java          # NEW: US7 tests
├── CalendarEscapeTest.java        # NEW: US8 tests
├── EmitEventRefactorTest.java     # NEW: US9 tests (verify no regression)
└── SexEnumTest.java               # NEW: US10 tests
```

**Structure Decision**: Single project, existing Gradle structure. All changes are within the existing `org.gedcom7.writer` package hierarchy. One new public file (`Sex.java`), rest are modifications to existing files.

## Implementation Approach

### Change Impact Analysis

**Files modified by multiple user stories** (must be sequential or carefully coordinated):
- `IndividualContext.java`: US1 (personalName), US3 (LDS), US7 (event), US9 (emitEvent refactor), US10 (sex enum)
- `FamilyContext.java`: US3 (LDS sealing), US7 (event), US9 (emitEvent refactor)
- `CommonContext.java`: US9 (emitEvent extraction)

**Dependency ordering**:
1. **US9 (emitEvent refactor) MUST come first** — it moves `emitEvent` to CommonContext. US3, US7 all add methods that call `emitEvent`. Doing US9 first means US3/US7 can simply call the shared method.
2. **US6 (unchecked exception) should be early** — changes GedcomWriteException base class, which affects all `throws` declarations and test catch blocks.
3. **US1, US2, US3, US4, US5, US7, US8, US10** are independent of each other after US9 and US6 are done.

### Parallelization Strategy

**Phase A (Sequential foundation):**
- US9: Extract emitEvent to CommonContext
- US6: GedcomWriteException → RuntimeException

**Phase B (Parallel, after Phase A):**
- Agent 1: US1 (personalName) — touches IndividualContext, PersonalNameContext
- Agent 2: US2 (sharedNoteWithText) — touches GedcomWriter only
- Agent 3: US3 (LDS ordinances) — touches IndividualContext, FamilyContext
- Agent 4: US4 (public builder) — touches GedcomWriterConfig only
- Agent 5: US5 (HEAD.CHAR) — touches GedcomWriter.head() only
- Agent 6: US7 (generic event) — touches IndividualContext, FamilyContext
- Agent 7: US8 (calendar escape) — touches LineEmitter/EventContext only
- Agent 8: US10 (Sex enum) — touches IndividualContext, new Sex.java

**Conflict analysis for Phase B:**
- US1, US3, US7, US10 all modify `IndividualContext.java` — potential merge conflicts
- US3, US7 modify `FamilyContext.java` — potential merge conflicts
- Resolution: US1/US3/US7/US10 add methods to different sections of the file. Merge conflicts are additive (new methods) and should auto-resolve if agents add at different locations. Alternatively, group US3+US7 together since both add event-related methods.

**Recommended grouping to minimize conflicts:**
- Agent A: US1 + US10 (IndividualContext: name + sex, non-overlapping)
- Agent B: US2 (GedcomWriter only)
- Agent C: US3 + US7 (IndividualContext + FamilyContext: events + LDS, closely related)
- Agent D: US4 (GedcomWriterConfig only)
- Agent E: US5 (GedcomWriter.head only)
- Agent F: US8 (LineEmitter/EventContext only)

This yields 6 parallel agents in Phase B with minimal conflict risk.

## Complexity Tracking

No constitution violations to justify. All changes are straightforward additions within the existing architecture.
