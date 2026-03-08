# Tasks: Common Substructure Tag Constants

**Input**: Design documents from `/specs/009-common-tag-constants/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are included — Constitution Principle V requires test-driven development.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: No setup needed — this feature adds constants to an existing file. Proceed directly to user stories.

---

## Phase 2: Foundational

**Purpose**: No foundational work needed — existing GedcomTag.java is the target file. No blocking prerequisites.

---

## Phase 3: User Story 1 - Deep Substructure Tag Constants (Priority: P1) MVP

**Goal**: Add constants for MAP, LATI, LONG and the core common substructure classes (Plac, Map, Date, Addr, File, Form, Gedc)

**Independent Test**: Constants exist, resolve to correct values, work in switch statements

### Tests for User Story 1

- [X] T001 [US1] Write unit tests for Plac, Map, Date, Addr, File, Form, Gedc nested classes in src/test/java/org/gedcom7/parser/GedcomTagTest.java — verify all constants resolve to correct string values and are usable in switch statements

### Implementation for User Story 1

- [X] T002 [US1] Add GedcomTag.Plac nested class with MAP, FORM, LANG, TRAN, EXID, NOTE, SNOTE, SOUR constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T003 [US1] Add GedcomTag.Map nested class with LATI, LONG constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T004 [US1] Add GedcomTag.Date nested class with TIME, PHRASE constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T005 [US1] Add GedcomTag.Addr nested class with ADR1, ADR2, ADR3, CITY, STAE, POST, CTRY constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T006 [US1] Add GedcomTag.File nested class with FORM, TITL, TRAN constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T007 [US1] Add GedcomTag.Form nested class with MEDI constant in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T008 [US1] Add GedcomTag.Gedc nested class with VERS constant in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T009 [US1] Add @see Javadoc cross-references from existing PLAC, DATE, ADDR constants in event classes to new Plac, Date, Addr classes in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T010 [US1] Run ./gradlew test to verify all tests pass including new US1 tests

### Review for User Story 1

- [X] T011 [US1] Independent agent review of US1 implementation against spec.md and constitution

**Checkpoint**: Core common substructure constants (Plac, Map, Date, Addr, File, Form, Gedc) are available and tested

---

## Phase 4: User Story 2 - Remaining Common Substructure Constants (Priority: P2)

**Goal**: Add constants for Name, Refn, Exid, Asso, Famc, Chan, Crea, SourCitation, Schma

**Independent Test**: All remaining depth-2+ tags have constants, all resolve correctly

### Tests for User Story 2

- [X] T012 [US2] Write unit tests for Name, Refn, Exid, Asso, Famc, Chan, Crea, SourCitation, Schma nested classes in src/test/java/org/gedcom7/parser/GedcomTagTest.java

### Implementation for User Story 2

- [X] T013 [US2] Add GedcomTag.Name nested class with GIVN, SURN, NPFX, NSFX, SPFX, NICK, TYPE, TRAN, NOTE, SNOTE, SOUR constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T014 [US2] Add GedcomTag.Refn nested class with TYPE constant in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T015 [US2] Add GedcomTag.Exid nested class with TYPE constant in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T016 [US2] Add GedcomTag.Asso nested class with ROLE, PHRASE, NOTE, SNOTE, SOUR constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T017 [US2] Add GedcomTag.Famc nested class with PEDI, STAT, NOTE, SNOTE constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T018 [US2] Add GedcomTag.Chan nested class with DATE, NOTE, SNOTE constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T019 [US2] Add GedcomTag.Crea nested class with DATE constant in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T020 [US2] Add GedcomTag.SourCitation nested class with PAGE, DATA, EVEN, QUAY, NOTE, SNOTE, OBJE constants in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T021 [US2] Add GedcomTag.Schma nested class with TAG constant in src/main/java/org/gedcom7/parser/GedcomTag.java
- [X] T022 [US2] Run ./gradlew test to verify all tests pass including new US2 tests

### Review for User Story 2

- [X] T023 [US2] Independent agent review of US2 implementation against spec.md and constitution

**Checkpoint**: All depth-2+ substructure tags have constants

---

## Phase 5: User Story 3 - Documentation and Discoverability (Priority: P3)

**Goal**: Update documentation and examples to use new constants

**Independent Test**: Documentation references new constants; no raw string literals for tags that have constants

### Implementation for User Story 3

- [X] T024 [P] [US3] Update tutorial constants section in docs/tutorial.md to list common substructure constants (Plac.MAP, Map.LATI, Map.LONG, Addr.CITY, etc.)
- [X] T025 [P] [US3] Update architecture.md Public API table GedcomTag description to include new nested classes in docs/architecture.md
- [X] T026 [P] [US3] Update FamilyTreeExample to use GedcomTag.Plac.MAP, GedcomTag.Map.LATI, GedcomTag.Map.LONG for place coordinates in src/main/example/FamilyTreeExample.java
- [X] T027 [US3] Write integration test using new constants to parse place coordinates from a GEDCOM file in src/test/java/org/gedcom7/parser/TagConstantsUsageTest.java

### Review for User Story 3

- [X] T028 [US3] Independent agent review of US3 implementation against spec.md and constitution

**Checkpoint**: Documentation and examples fully updated

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T029 Run full test suite ./gradlew clean test to verify all existing + new tests pass
- [X] T030 Final independent evaluation of complete implementation against all spec requirements, acceptance scenarios, and constitution principles

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A — no setup needed
- **Foundational (Phase 2)**: N/A — no foundational work needed
- **User Story 1 (Phase 3)**: Can start immediately — adds core common classes
- **User Story 2 (Phase 4)**: Depends on US1 (same file, sequential edits)
- **User Story 3 (Phase 5)**: Depends on US1 + US2 (references new constants in docs)
- **Polish (Phase 6)**: Depends on all user stories being complete

### Within Each User Story

- Tests written first (TDD per Constitution Principle V)
- Constants added per data-model.md inventory
- @see cross-references added after constants exist
- Test run verifies correctness
- Independent review validates against spec

### Parallel Opportunities

- T024, T025, T026 in US3 can run in parallel (different files)
- US1 tests (T001) and US2 tests (T012) could theoretically be written in parallel, but implementation is sequential (same file)

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 3: User Story 1 (T001-T011)
2. **STOP and VALIDATE**: MAP, LATI, LONG plus core common classes available
3. This alone delivers the user's primary request

### Incremental Delivery

1. User Story 1 → Core common classes (Plac, Map, Date, Addr, File, Form, Gedc) → MVP
2. User Story 2 → Remaining classes (Name, Refn, Exid, Asso, Famc, Chan, Crea, SourCitation, Schma) → Complete coverage
3. User Story 3 → Documentation updates → Full discoverability
4. Polish → Final validation → Feature complete

---

## Notes

- All constants go in a single file (GedcomTag.java) — tasks are sequential within US1 and US2
- [P] tasks in US3 target different files and can run in parallel
- Each constant MUST have descriptive Javadoc (FR-006)
- Backward compatibility is non-negotiable (FR-004)
- Total new classes: 16, total new constants: ~70
