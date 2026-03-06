# Tasks: Writer Gaps Remediation

**Input**: Design documents from `/specs/006-writer-gaps/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Tests are included per Constitution Principle V (Test-Driven Development).

**Organization**: Tasks are grouped by user story. Foundational tasks (US9, US6) MUST complete before parallel user story work begins.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: No setup tasks needed — existing project structure and build system are already in place.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: US9 (emitEvent refactor) and US6 (unchecked exception) modify shared infrastructure that all other user stories depend on. MUST complete before parallel work begins.

**⚠️ CRITICAL**: No user story work (Phase 3+) can begin until this phase is complete.

### US9: Extract emitEvent to CommonContext (FR-010)

- [X] T001 [US9] Add `protected void emitEvent(String tag, Consumer<EventContext> body)` method to `src/main/java/org/gedcom7/writer/context/CommonContext.java` — extract the identical private method from IndividualContext, keeping the same implementation (emit tag line, create EventContext, call body, catch IOException)
- [X] T002 [US9] Update `src/main/java/org/gedcom7/writer/context/IndividualContext.java` — remove private `emitEvent` method, change `birth()`, `death()`, `christening()`, `burial()`, `residence()` to call inherited `emitEvent()` from CommonContext
- [X] T003 [US9] Update `src/main/java/org/gedcom7/writer/context/FamilyContext.java` — remove private `emitEvent` method, change `marriage()`, `divorce()`, `annulment()` to call inherited `emitEvent()` from CommonContext
- [X] T004 [US9] Write regression tests in `src/test/java/org/gedcom7/writer/EmitEventRefactorTest.java` — verify `indi.birth(body -> { body.date("15 MAR 1955"); })` and `fam.marriage(body -> { body.date("1 JUN 1980"); })` produce identical output to pre-refactoring behavior
- [X] T005 [US9] Run `./gradlew clean test` and verify all existing tests pass with no regressions

### US6: Unchecked Exception (FR-007)

- [X] T006 [US6] Change `src/main/java/org/gedcom7/writer/GedcomWriteException.java` — replace `extends Exception` with `extends RuntimeException`
- [X] T007 [US6] Write test in `src/test/java/org/gedcom7/writer/UncheckedExceptionTest.java` — verify `GedcomWriteException` is instanceof `RuntimeException`, verify `writer.individual(indi -> { ... })` compiles and runs without try/catch for `GedcomWriteException`, verify error propagation still works
- [X] T008 [US6] Update `src/main/java/org/gedcom7/writer/GedcomWriter.java` — (a) in `writeRecord()` (lines 260-264), remove the catch block that unwraps `RuntimeException` wrapping `GedcomWriteException`, since `GedcomWriteException` is now itself a `RuntimeException` and propagates naturally; (b) in `ensureHead()` (lines 278-291), review/simplify exception handling since `head()` no longer throws checked `GedcomWriteException`; (c) in `close()` (lines 224-241), remove the try/catch for `GedcomWriteException` around `head()` call since it's now unchecked; (d) keep `throws GedcomWriteException` on public method signatures for documentation purposes.
- [X] T009 [US6] Run `./gradlew clean test` and verify all existing tests pass — some tests may need minor updates if they explicitly catch `Exception` vs `RuntimeException`

**Checkpoint**: Foundation ready — emitEvent is shared in CommonContext, GedcomWriteException is unchecked. All parallel user story work can now begin.

---

## Phase 3: User Story 1 — Personal Name Convenience Overload (Priority: P1)

**Goal**: `personalName(String givenName, String surname)` auto-formats NAME + GIVN + SURN

**Independent Test**: Call `indi.personalName("John", "Doe")` → output has `1 NAME John /Doe/`, `2 GIVN John`, `2 SURN Doe`

- [X] T010 [P] [US1] Write tests in `src/test/java/org/gedcom7/writer/PersonalNameOverloadTest.java` — test cases: (1) `personalName("John", "Doe")` → NAME + GIVN + SURN, (2) `personalName("John", "Doe", name -> { name.nickname("Johnny"); })` → NAME + GIVN + SURN + NICK, (3) `personalName("Maria", null)` → NAME Maria + GIVN only, (4) `personalName(null, "Doe")` → NAME /Doe/ + SURN only, (5) `personalName("", "")` → treated as absent, (6) existing `personalName("John /Doe/")` unchanged
- [X] T011 [P] [US1] Add `personalName(String givenName, String surname)` and `personalName(String givenName, String surname, Consumer<PersonalNameContext> body)` methods to `src/main/java/org/gedcom7/writer/context/IndividualContext.java` — format NAME value as `"givenName /surname/"`, auto-emit `2 GIVN givenName` and `2 SURN surname` substructures, handle null/empty given and surname

---

## Phase 4: User Story 2 — Shared Note Text Value (Priority: P2)

**Goal**: `sharedNoteWithText(String text, Consumer<NoteContext>)` emits SNOTE with inline text

**Independent Test**: Call `writer.sharedNoteWithText("This is the note text", note -> {})` → output has `0 @N1@ SNOTE This is the note text`

- [X] T012 [P] [US2] Write tests in `src/test/java/org/gedcom7/writer/SharedNoteTextTest.java` — test cases: (1) `sharedNoteWithText("This is a note", note -> {})` → SNOTE with text, (2) `sharedNoteWithText("Line one\nLine two", note -> {})` → SNOTE + CONT, (3) `sharedNoteWithText("id1", "Note text", note -> {})` → custom ID + text, (4) existing `sharedNote(note -> {})` unchanged, (5) existing `sharedNote("id1", note -> {})` unchanged
- [X] T013 [P] [US2] Add `sharedNoteWithText(String text, Consumer<NoteContext> body)` and `sharedNoteWithText(String id, String text, Consumer<NoteContext> body)` methods to `src/main/java/org/gedcom7/writer/GedcomWriter.java` — emit SNOTE record with text as the record-level value using `emitter.emitValueWithCont()` for multi-line support

---

## Phase 5: User Story 3 — LDS Ordinance Typed Methods (Priority: P2)

**Goal**: Typed methods for LDS ordinances (BAPL, CONL, ENDL, INIL, SLGC, SLGS) with EventContext

**Independent Test**: Call `indi.ldsBaptism(body -> { body.date("15 JAN 1900"); body.place("Salt Lake City, UT"); })` → output has `1 BAPL`, `2 DATE`, `2 PLAC`

- [X] T014 [P] [US3] Write tests in `src/test/java/org/gedcom7/writer/LdsOrdinanceTest.java` — test cases: (1) `ldsBaptism` → BAPL with date/place/TEMP/STAT, (2) `ldsConfirmation` → CONL, (3) `ldsEndowment` → ENDL, (4) `ldsInitiatory` → INIL, (5) `ldsSealingToParents` → SLGC, (6) `ldsSealingToSpouse` on FamilyContext → SLGS, (7) verify EventContext provides typed date/place/structure access, (8) verify same behavior in GEDCOM 5.5.5 mode
- [X] T015 [P] [US3] Add LDS individual ordinance methods to `src/main/java/org/gedcom7/writer/context/IndividualContext.java` — `ldsBaptism(Consumer<EventContext>)` emitting BAPL, `ldsConfirmation(Consumer<EventContext>)` emitting CONL, `ldsEndowment(Consumer<EventContext>)` emitting ENDL, `ldsInitiatory(Consumer<EventContext>)` emitting INIL, `ldsSealingToParents(Consumer<EventContext>)` emitting SLGC — all delegate to `emitEvent(tag, body)`
- [X] T016 [P] [US3] Add `ldsSealingToSpouse(Consumer<EventContext> body)` method to `src/main/java/org/gedcom7/writer/context/FamilyContext.java` — emits SLGS via `emitEvent("SLGS", body)`

---

## Phase 6: User Story 4 — Public Builder Methods (Priority: P4)

**Goal**: `escapeAllAt(boolean)` and `concEnabled(boolean)` on Builder are public

**Independent Test**: From test outside `org.gedcom7.writer` package (or verify via reflection), call `new GedcomWriterConfig.Builder().escapeAllAt(true).build()` compiles and works

- [X] T017 [P] [US4] Write tests in `src/test/java/org/gedcom7/writer/BuilderVisibilityTest.java` — test cases: (1) `new Builder().escapeAllAt(true).build()` → `isEscapeAllAt()` returns true, (2) `new Builder().concEnabled(true).maxLineLength(200).build()` → `isConcEnabled()` returns true, (3) existing `gedcom7()` and `gedcom555()` factory methods unchanged
- [X] T018 [P] [US4] Change `escapeAllAt(boolean)` and `concEnabled(boolean)` from package-private to `public` in `src/main/java/org/gedcom7/writer/GedcomWriterConfig.java` (Builder class, ~lines 141-149)

---

## Phase 7: User Story 5 — Automatic HEAD.CHAR for GEDCOM 5.5.5 (Priority: P5)

**Goal**: Writer auto-emits `1 CHAR UTF-8` in HEAD for 5.5.5 mode

**Independent Test**: Create writer with `gedcom555()`, call `writer.head(head -> {})`, verify output contains `1 CHAR UTF-8`

- [X] T019 [P] [US5] Write tests in `src/test/java/org/gedcom7/writer/HeadCharTest.java` — test cases: (1) `gedcom555()` + explicit `head()` → output contains `1 CHAR UTF-8` after GEDC/VERS, (2) `gedcom7()` + `head()` → no CHAR line, (3) `gedcom555()` + auto-generated HEAD (via close without explicit head) → includes `1 CHAR UTF-8`
- [X] T020 [P] [US5] Modify `head()` method in `src/main/java/org/gedcom7/writer/GedcomWriter.java` (~line 58-76) — after emitting `2 VERS 5.5.5`, add: if version is GEDCOM 5.5.5, emit `emitter.emitLine(1, null, "CHAR", "UTF-8")`

---

## Phase 8: User Story 7 — Generic Event Method (Priority: P7)

**Goal**: `event(String tag, Consumer<EventContext>)` on IndividualContext and FamilyContext

**Independent Test**: Call `indi.event("IMMI", body -> { body.date("15 JAN 1905"); body.place("Ellis Island, NY"); })` → output has `1 IMMI`, `2 DATE`, `2 PLAC`

- [X] T021 [P] [US7] Write tests in `src/test/java/org/gedcom7/writer/GenericEventTest.java` — test cases: (1) `indi.event("IMMI", body -> { body.date(...); body.place(...); })` → IMMI with date/place, (2) `indi.event("CENS", body -> { body.date(...); })` → CENS, (3) `fam.event("ENGA", body -> { ... })` → ENGA, (4) existing typed methods (`birth`, `marriage`) still work
- [X] T022 [P] [US7] Add `public void event(String tag, Consumer<EventContext> body)` method to `src/main/java/org/gedcom7/writer/context/IndividualContext.java` — delegates to `emitEvent(tag, body)`
- [X] T023 [P] [US7] Add `public void event(String tag, Consumer<EventContext> body)` method to `src/main/java/org/gedcom7/writer/context/FamilyContext.java` — delegates to `emitEvent(tag, body)`

---

## Phase 9: User Story 8 — Date String @-Escaping Fix (Priority: P8)

**Goal**: Calendar escape prefixes (`@#DJULIAN@`, etc.) not double-escaped in 5.5.5 mode

**Independent Test**: In 5.5.5 mode, `event.date("@#DJULIAN@ 15 JAN 1700")` → output `2 DATE @#DJULIAN@ 15 JAN 1700`

- [X] T024 [P] [US8] Write tests in `src/test/java/org/gedcom7/writer/CalendarEscapeTest.java` — test cases: (1) 5.5.5 mode + `date("@#DJULIAN@ 15 JAN 1700")` → preserved, (2) 5.5.5 mode + `date("@#DGREGORIAN@ 1 JAN 1900")` → preserved, (3) 5.5.5 mode + `date("@#DHEBREW@ ...")` → preserved, (4) 5.5.5 mode + `date("@#DFRENCH R@ ...")` → preserved, (5) 5.5.5 mode + `date("15 JAN 1900")` (no calendar escape) → unchanged behavior, (6) 5.5.5 mode + `date("@#DJULIAN@ @unusual@text")` → calendar prefix preserved, other `@` doubled, (7) GEDCOM 7 mode → all behavior unchanged, (8) `WriterDate.raw(...)` unaffected
- [X] T025 [P] [US8] Modify `date(String)` method in `src/main/java/org/gedcom7/writer/context/EventContext.java` (~line 28-31) and add `emitLinePreEscaped(int level, String xref, String tag, String preEscapedPrefix, String remainingValue)` method to `src/main/java/org/gedcom7/writer/internal/LineEmitter.java` — when config is GEDCOM 5.5.5 and rawDateString starts with a recognized calendar escape prefix (`@#DGREGORIAN@`, `@#DJULIAN@`, `@#DHEBREW@`, `@#DFRENCH R@`, `@#DROMAN@`, `@#DUNKNOWN@`), split the string into the calendar prefix and the remainder, then call the new LineEmitter method which concatenates the pre-escaped prefix (no `@`-doubling) with the `escapeAt()`-processed remainder into a single DATE value. For non-calendar dates or GEDCOM 7 mode, delegate to existing `structure("DATE", rawDateString)` unchanged.

---

## Phase 10: User Story 10 — Sex Enum Overload (Priority: P10)

**Goal**: `Sex` enum with `MALE`, `FEMALE`, `INTERSEX`, `UNKNOWN` + `sex(Sex)` overload

**Independent Test**: Call `indi.sex(Sex.MALE)` → output `1 SEX M`

- [X] T026 [P] [US10] Write tests in `src/test/java/org/gedcom7/writer/SexEnumTest.java` — test cases: (1) `sex(Sex.MALE)` → `1 SEX M`, (2) `sex(Sex.FEMALE)` → `1 SEX F`, (3) `sex(Sex.INTERSEX)` → `1 SEX X`, (4) `sex(Sex.UNKNOWN)` → `1 SEX U`, (5) `sex((Sex) null)` → no SEX line, (6) existing `sex("M")` unchanged
- [X] T027 [P] [US10] Create `src/main/java/org/gedcom7/writer/Sex.java` — public enum with values `MALE("M")`, `FEMALE("F")`, `INTERSEX("X")`, `UNKNOWN("U")`, private `code` field, constructor, `getCode()` method
- [X] T028 [P] [US10] Add `public void sex(Sex value)` overload to `src/main/java/org/gedcom7/writer/context/IndividualContext.java` — if value is null return, otherwise delegate to `sex(value.getCode())`

---

## Phase 11: Per-Story Independent Review (Constitution Principle VIII)

**Purpose**: Each completed user story MUST be reviewed by an independent agent before proceeding to final evaluation.

- [X] T029 [P] Independent review of US9 (emitEvent refactor): verify FR-010, no regressions, constitution compliance
- [X] T030 [P] Independent review of US6 (unchecked exception): verify FR-007, exception propagation, constitution compliance
- [X] T031 [P] Independent review of US1 (personalName): verify FR-001, FR-002, all acceptance scenarios, constitution compliance
- [X] T032 [P] Independent review of US2 (sharedNoteWithText): verify FR-003, all acceptance scenarios, constitution compliance
- [X] T033 [P] Independent review of US3 (LDS ordinances): verify FR-004, all acceptance scenarios, constitution compliance
- [X] T034 [P] Independent review of US4 (public builder): verify FR-005, all acceptance scenarios, constitution compliance
- [X] T035 [P] Independent review of US5 (HEAD.CHAR): verify FR-006, all acceptance scenarios, constitution compliance
- [X] T036 [P] Independent review of US7 (generic event): verify FR-008, all acceptance scenarios, constitution compliance
- [X] T037 [P] Independent review of US8 (calendar escape): verify FR-009, all acceptance scenarios, constitution compliance
- [X] T038 [P] Independent review of US10 (Sex enum): verify FR-011, all acceptance scenarios, constitution compliance

---

## Phase 12: Polish & Final Evaluation

**Purpose**: Final validation and verification

- [X] T039 Run `./gradlew clean test` and verify all tests pass (existing + new) — 729 tests, 0 failures
- [X] T040 Run quickstart.md scenarios manually to validate all 10 user stories work end-to-end
- [X] T041 Final evaluation: independent agent reviews complete implementation against all functional requirements (FR-001 through FR-012), all acceptance scenarios, and all constitution principles (per Constitution Principle VIII)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: N/A — no setup needed
- **Phase 2 (Foundational)**: US9 then US6, sequential — BLOCKS all user stories
- **Phases 3-10 (User Stories)**: All depend on Phase 2 completion. All can run in parallel.
- **Phase 11 (Per-Story Review)**: Each review depends on its corresponding story phase completing. All reviews can run in parallel.
- **Phase 12 (Polish + Final Evaluation)**: Depends on all reviews completing

### User Story Dependencies

- **US9 (Phase 2)**: No dependencies — first task
- **US6 (Phase 2)**: No dependencies — runs after US9 in Phase 2
- **US1 (Phase 3)**: Depends on Phase 2 only — modifies IndividualContext
- **US2 (Phase 4)**: Depends on Phase 2 only — modifies GedcomWriter
- **US3 (Phase 5)**: Depends on Phase 2 (uses shared emitEvent) — modifies IndividualContext + FamilyContext
- **US4 (Phase 6)**: Depends on Phase 2 only — modifies GedcomWriterConfig
- **US5 (Phase 7)**: Depends on Phase 2 only — modifies GedcomWriter.head()
- **US7 (Phase 8)**: Depends on Phase 2 (uses shared emitEvent) — modifies IndividualContext + FamilyContext
- **US8 (Phase 9)**: Depends on Phase 2 only — modifies EventContext
- **US10 (Phase 10)**: Depends on Phase 2 only — new Sex.java + modifies IndividualContext

### File Conflict Analysis for Parallel Execution

| File | Modified by | Conflict Risk |
|------|------------|---------------|
| IndividualContext.java | US1, US3, US7, US10 | MEDIUM — all add new methods, different sections |
| FamilyContext.java | US3, US7 | LOW — both add new methods |
| GedcomWriter.java | US2, US5 | LOW — different methods (sharedNoteWithText vs head) |
| EventContext.java | US8 only | NONE |
| GedcomWriterConfig.java | US4 only | NONE |
| Sex.java | US10 only | NONE (new file) |

### Recommended Agent Grouping (minimizes file conflicts)

| Agent | Stories | Files Touched | Parallelizable |
|-------|---------|---------------|----------------|
| Agent A | US1 + US10 | IndividualContext, Sex.java | Yes (after Phase 2) |
| Agent B | US2 | GedcomWriter | Yes (after Phase 2) |
| Agent C | US3 + US7 | IndividualContext, FamilyContext | Yes (after Phase 2) |
| Agent D | US4 | GedcomWriterConfig | Yes (after Phase 2) |
| Agent E | US5 | GedcomWriter.head() | Yes (after Phase 2) |
| Agent F | US8 | EventContext | Yes (after Phase 2) |

**Conflict mitigation**: Agents A and C both touch IndividualContext.java. Agent A adds name/sex methods, Agent C adds LDS/event methods. These are additive changes in different sections of the file; merge conflicts should be resolvable automatically or with minimal manual intervention.

---

## Parallel Example: After Phase 2 Completion

```text
# Launch all 6 agents simultaneously:
Agent A: T010, T011, T026, T027, T028 (US1 + US10: personalName + Sex enum)
Agent B: T012, T013 (US2: sharedNoteWithText)
Agent C: T014, T015, T016, T021, T022, T023 (US3 + US7: LDS + generic event)
Agent D: T017, T018 (US4: public builder methods)
Agent E: T019, T020 (US5: HEAD.CHAR)
Agent F: T024, T025 (US8: calendar escape fix)
```

---

## Implementation Strategy

### Foundation First (Sequential)

1. Complete Phase 2: US9 (emitEvent refactor) → US6 (unchecked exception)
2. **VALIDATE**: `./gradlew clean test` — all existing tests pass
3. Parallel work can now begin

### Parallel Delivery (6 Agents)

4. Launch Agents A-F simultaneously
5. Each agent writes tests first, then implementation
6. Each agent runs `./gradlew clean test` in their worktree
7. Merge all agent work back to main branch
8. **VALIDATE**: `./gradlew clean test` on merged result

### Independent Verification (Constitution Principle VIII)

9. Per-story review agents (T029-T038) verify each completed story in parallel
10. Final evaluation agent (T041) reviews complete implementation

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- US11 (Structure Validation) is deferred — not included in tasks
- All test tasks reference specific test cases from spec acceptance scenarios
- Phase 2 is the critical path — all parallel work is blocked until it completes
