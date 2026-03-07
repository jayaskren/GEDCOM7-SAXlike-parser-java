# Tasks: Tag and Value Constants

**Input**: Design documents from `/specs/008-tag-value-constants/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Included — Constitution Principle V (Test-Driven Development) requires automated tests.

**Organization**: Tasks grouped by user story. US3 (record-level tags) and US1 (nested substructure tags) are both P1 and target the same file, so US3 is the foundational phase and US1 builds on it.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Foundational — Record-Level Tag Constants (US3, Priority: P1)

**Goal**: Provide `GedcomTag.INDI`, `GedcomTag.FAM`, etc. for use in `startRecord()` handlers.

**Independent Test**: Write a handler using record-level constants in a switch statement; verify correct record identification against a test file.

- [x] T001 [US3] Create `GedcomTag.java` with record-level constants (HEAD, TRLR, INDI, FAM, OBJE, SOUR, REPO, NOTE, SNOTE, SUBM) with Javadoc describing each record type in `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T002 [US3] Write `GedcomTagTest.java` verifying record-level constant values match expected strings in `src/test/java/org/gedcom7/parser/GedcomTagTest.java`

**Checkpoint**: Record-level constants usable in `startRecord()` handlers. US3 independently testable.

---

## Phase 2: User Story 1 — Substructure Tag Constants (Priority: P1) MVP

**Goal**: Provide nested classes (`GedcomTag.Indi`, `GedcomTag.Fam`, etc.) with substructure constants for IDE autocomplete and switch/case usage.

**Independent Test**: Write a handler using `GedcomTag.Indi.NAME`, `GedcomTag.Indi.SEX`, etc. in a switch statement; verify correct structure identification against a test file.

### Implementation for User Story 1

- [x] T003 [US1] Add `Indi` nested class with all substructure constants to `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T004 [P] [US1] Add `Fam` nested class with all substructure constants to `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T005 [P] [US1] Add `Sour` and `Repo` nested classes to `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T006 [P] [US1] Add `Obje`, `Snote`, and `Subm` nested classes to `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T007 [P] [US1] Add `Head` nested class to `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T008 [US1] Add event sub-nested classes inside `Indi`: `Birt`, `Deat`, `Buri`, `Chr`, `Bapm`, `Even` in `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T009 [US1] Add event sub-nested classes inside `Fam`: `Marr`, `Div`, `Anul`, `Even` in `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T010 [US1] Add Javadoc to all tag constants in `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T011 [US1] Extend `GedcomTagTest.java` to verify all nested class constants in `src/test/java/org/gedcom7/parser/GedcomTagTest.java`
- [x] T012 [US1] Write `TagConstantsUsageTest.java` demonstrating handler with constants (SC-001, SC-005) in `src/test/java/org/gedcom7/parser/TagConstantsUsageTest.java`

**Checkpoint**: Full GedcomTag class with all nested classes and event sub-nesting. US1 independently testable. All existing tests still pass (SC-004).

---

## Phase 3: User Story 2 — Value Constants (Priority: P2)

**Goal**: Provide `GedcomValue.Sex.MALE`, `GedcomValue.Pedi.ADOPTED`, etc. for enumeration value comparison.

**Independent Test**: Verify each value constant resolves to the correct GEDCOM string; use value constants in a handler to match parser-delivered values.

### Implementation for User Story 2

- [x] T013 [US2] Create `GedcomValue.java` with `Sex` and `NameType` nested classes in `src/main/java/org/gedcom7/parser/GedcomValue.java`
- [x] T014 [US2] Add `Pedi`, `Resn`, and `Adop` nested classes to `src/main/java/org/gedcom7/parser/GedcomValue.java`
- [x] T015 [P] [US2] Add `Role` nested class to `src/main/java/org/gedcom7/parser/GedcomValue.java`
- [x] T016 [P] [US2] Add `Medi` nested class to `src/main/java/org/gedcom7/parser/GedcomValue.java`
- [x] T017 [US2] Add Javadoc to all value constants in `src/main/java/org/gedcom7/parser/GedcomValue.java`
- [x] T018 [US2] Write `GedcomValueTest.java` verifying all constant values (SC-003) in `src/test/java/org/gedcom7/parser/GedcomValueTest.java`

**Checkpoint**: Full GedcomValue class with all enumeration types. US2 independently testable.

---

## Phase 4: User Story 4 — Javadoc @see Links (Priority: P3)

**Goal**: Connect tag constants to `GedcomDataTypes` parser methods via `@see` Javadoc references.

**Independent Test**: Verify `@see` annotations exist on NAME, DATE, AGE constants and that referenced methods exist.

### Implementation for User Story 4

- [x] T019 [US4] Add `@see` references to NAME, DATE, AGE, TIME constants in `src/main/java/org/gedcom7/parser/GedcomTag.java`
- [x] T020 [US4] Verify Javadoc compiles without broken @see references

**Checkpoint**: All @see links in place. US4 independently testable.

---

## Phase 5: Verification & Regression

**Purpose**: Confirm no impact on existing parser behavior.

- [x] T021 Run full test suite (`./gradlew clean test`) and verify all existing tests pass unchanged (SC-004) — 827 tests, 0 failures

---

## Phase 6: Per-Story Independent Reviews (Constitution Principle VIII)

**Purpose**: Each user story reviewed by an independent agent.

- [x] T022 [US3] Independent review of record-level tag constants against spec acceptance scenarios US3-AS1, US3-AS2 — PASS (all 5 checks)
- [x] T023 [US1] Independent review of GedcomTag nested classes against spec acceptance scenarios US1-AS1 through US1-AS4 and edge cases — PASS (347 constants, all with Javadoc, all edge cases covered)
- [x] T024 [US2] Independent review of GedcomValue against spec acceptance scenarios US2-AS1 through US2-AS3, FR-007, FR-007a — PASS (all 6 checks, all 50 constants verified)
- [x] T025 [US4] Independent review of @see Javadoc links against spec acceptance scenarios US4-AS1, US4-AS2, FR-005 — PASS (5/6 checks, 1 low gap: TIME/LATI/LONG not applicable)

---

## Phase 7: Final Evaluation (Constitution Principle VIII)

**Purpose**: Comprehensive independent evaluation of the complete feature.

- [x] T026 Final independent evaluation: verify all FRs (FR-001 through FR-011, FR-007a), all SCs (SC-001 through SC-005), all acceptance scenarios, all edge cases, and full constitution compliance — PASS (28/28 checks, 0 gaps, 0 deviations)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (US3)**: No dependencies — creates GedcomTag.java
- **Phase 2 (US1)**: Depends on Phase 1 — extends GedcomTag.java with nested classes
- **Phase 3 (US2)**: No dependency on Phase 2 — creates separate GedcomValue.java. Can run in parallel with Phase 2.
- **Phase 4 (US4)**: Depends on Phase 2 — adds @see to existing constants in GedcomTag.java
- **Phase 5**: Depends on Phases 2, 3, 4
- **Phase 6**: Depends on respective story phases
- **Phase 7**: Depends on Phase 6

### User Story Dependencies

- **US3 (P1)**: Foundation — no dependencies
- **US1 (P1)**: Depends on US3 (extends same file)
- **US2 (P2)**: Independent of US1/US3 (different file) — **can run in parallel with US1**
- **US4 (P3)**: Depends on US1 (adds to constants created in US1)

### Parallel Opportunities

- T004, T005, T006, T007 can all run in parallel (different nested classes, no conflicts)
- T015, T016 can run in parallel (different nested classes in GedcomValue)
- **US1 and US2 can run in parallel** (GedcomTag.java vs GedcomValue.java)

---

## Notes

- All constants derived from `StructureDefinitions.java` (official GEDCOM 7 spec data)
- Value constant names use descriptive forms per clarification (MALE="M", FEMALE="F", etc.)
- No `GedcomTag.Common` class — shared tags duplicated in each record-specific class per clarification
- Event sub-nesting limited to: BIRT, DEAT, BURI, CHR, BAPM, MARR, DIV, ANUL, EVEN per clarification
- [P] tasks = different files or different classes within same file, no dependencies
- Commit after each task or logical group
