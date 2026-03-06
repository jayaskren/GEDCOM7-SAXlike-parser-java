# Tasks: GEDCOM Version Converter

**Input**: Design documents from `/specs/007-gedcom-version-converter/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/converter-api.md

**Tests**: Included — TDD approach per Constitution Principle V.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Create the converter package structure and foundational types

- [X] T001 Create converter package directory at src/main/java/org/gedcom7/converter/ and test directory at src/test/java/org/gedcom7/converter/
- [X] T002 [P] Create ConversionWarning immutable class in src/main/java/org/gedcom7/converter/ConversionWarning.java — fields: message (String), tag (String, nullable), lineNumber (int); constructor, getters, toString
- [X] T003 [P] Create ConversionWarningHandler functional interface in src/main/java/org/gedcom7/converter/ConversionWarningHandler.java — single method: void handle(ConversionWarning warning)
- [X] T004 [P] Create ConversionResult immutable class in src/main/java/org/gedcom7/converter/ConversionResult.java — fields: sourceVersion (GedcomVersion), targetVersion (GedcomVersion), recordCount (int), warnings (List\<ConversionWarning\>), parseErrors (List\<GedcomParseError\>); getters including warningCount() and errorCount(); internal static Builder class with incrementRecordCount(), addWarning(), addParseError(), build() methods
- [X] T005 Create GedcomConverterConfig immutable class in src/main/java/org/gedcom7/converter/GedcomConverterConfig.java — fields: targetVersion (GedcomVersion), strict (boolean), warningHandler (ConversionWarningHandler, nullable), lineEnding (String); Builder with all setters; factory methods: toGedcom7(), toGedcom555(), toGedcom7Strict(), toGedcom555Strict()
- [X] T006 Run all existing tests to verify setup classes compile and no regressions: ./gradlew clean test

**Checkpoint**: Foundational types ready — converter implementation can begin

---

## Phase 2: Foundational (Core Converter)

**Purpose**: Implement the core GedcomConverter that bridges parser events to writer output

**⚠️ CRITICAL**: This phase implements the core converter logic that all user stories depend on

- [X] T007 Create GedcomConverter class in src/main/java/org/gedcom7/converter/GedcomConverter.java — implements GedcomHandler; static convert(InputStream, OutputStream, GedcomConverterConfig) method; internal handler that creates LineEmitter with target GedcomWriterConfig; handles startDocument by writing HEAD with adapted version substructures (GEDC.VERS for target, CHAR UTF-8 for 5.5.5, GEDC.FORM LINEAGE-LINKED for 5.5.5); handles startRecord/startStructure by emitting lines via LineEmitter; tracks inHead/skipGedcSubtree/skipCharLine state to skip source-version HEAD substructures (GEDC, GEDC.VERS, GEDC.FORM, CHAR) during HEAD replay; handles pointer values (isPointer=true) by emitting pointer format; handles endDocument by writing TRLR; forwards parser warnings/errors to ConversionResult.Builder; returns ConversionResult
- [X] T008 Create basic converter test in src/test/java/org/gedcom7/converter/GedcomConverterBasicTest.java — test that a minimal GEDCOM 7 file (HEAD + TRLR only) converts to GEDCOM 7 (identity); test that a minimal GEDCOM 5.5.5 file converts to GEDCOM 5.5.5 (identity); verify HEAD.GEDC.VERS is correct in output
- [X] T009 Run all tests to verify core converter works: ./gradlew clean test

**Checkpoint**: Core converter functional — user story tests can now be written

---

## Phase 3: User Story 1 — Convert GEDCOM 5.5.5 to GEDCOM 7 (Priority: P1) 🎯 MVP

**Goal**: Convert a GEDCOM 5.5.5 file to valid GEDCOM 7 format with all records preserved

**Independent Test**: Pass a GEDCOM 5.5.5 input stream to the converter with target GEDCOM 7, verify output has correct HEAD (no CHAR, VERS 7.0), no CONC, leading-@ escaping, all records preserved

### Tests for User Story 1

- [X] T010 [US1] Create test class src/test/java/org/gedcom7/converter/Convert555To7Test.java with tests: (1) convert 5.5.5 file with HEAD, INDI, FAM, SOUR, TRLR → output is valid GEDCOM 7, HEAD.GEDC.VERS is "7.0", no HEAD.CHAR line; (2) 5.5.5 file with CONC-split long lines → values reassembled into single lines in output (no CONC); (3) 5.5.5 file with @@-escaped values → output uses GEDCOM 7 leading-@ rules; (4) 5.5.5 file with FAMS/FAMC pointers → preserved in output; (5) 5.5.5 file with multi-line CONT values → preserved correctly in output

### Implementation for User Story 1

- [X] T011 [US1] Ensure GedcomConverter handles 5.5.5→7 conversion: reader uses autoDetect config; writer config uses gedcom7() settings (no CONC, leading-@ escape); HEAD adaptation removes CHAR and GEDC.FORM, sets VERS to 7.0. Implement any missing logic in src/main/java/org/gedcom7/converter/GedcomConverter.java
- [X] T012 [US1] Run Convert555To7Test and all tests: ./gradlew clean test — fix any failures

**Checkpoint**: User Story 1 complete — 5.5.5 → 7 conversion works

---

## Phase 4: User Story 2 — Convert GEDCOM 7 to GEDCOM 5.5.5 (Priority: P2)

**Goal**: Convert a GEDCOM 7 file to valid GEDCOM 5.5.5 format with all records preserved

**Independent Test**: Pass a GEDCOM 7 input to converter with target 5.5.5, verify output has CHAR UTF-8, VERS 5.5.5, CONC-split long lines, @@ escaping

### Tests for User Story 2

- [X] T013 [US2] Create test class src/test/java/org/gedcom7/converter/Convert7To555Test.java with tests: (1) convert GEDCOM 7 file → output is valid 5.5.5 with HEAD.GEDC.VERS "5.5.5", HEAD.CHAR "UTF-8", all records present; (2) GEDCOM 7 file with lines >255 chars → long lines split using CONC; (3) GEDCOM 7 file with @ characters in values → all @ doubled (@@); (4) GEDCOM 7 file with extension tags (_TAG) → preserved in output; (5) GEDCOM 7 file with HEAD.SCHMA → SCHMA preserved as-is in 5.5.5 output

### Implementation for User Story 2

- [X] T014 [US2] Ensure GedcomConverter handles 7→5.5.5 conversion: writer config uses gedcom555() settings (CONC enabled, all-@@ escape, maxLineLength=255); HEAD adaptation adds CHAR UTF-8, adds GEDC.FORM LINEAGE-LINKED, sets VERS to 5.5.5. HEAD.SCHMA passes through (not skipped). Implement any missing logic in src/main/java/org/gedcom7/converter/GedcomConverter.java
- [X] T015 [US2] Run Convert7To555Test and all tests: ./gradlew clean test — fix any failures

**Checkpoint**: User Story 2 complete — 7 → 5.5.5 conversion works

---

## Phase 5: User Story 3 — Streaming Conversion (Priority: P3)

**Goal**: Converter works in streaming fashion — bounded memory for any file size

**Independent Test**: Convert a 1,000+ record file, verify output correct and memory bounded

### Tests for User Story 3

- [X] T016 [US3] Create test class src/test/java/org/gedcom7/converter/StreamingConversionTest.java with tests: (1) generate a GEDCOM file with 1,000 INDI records, convert 5.5.5→7, verify all 1,000 records present in output; (2) convert using InputStream/OutputStream, verify single-pass (no seekable input required); (3) verify ConversionResult.recordCount matches expected count

### Implementation for User Story 3

- [X] T017 [US3] Verify streaming architecture in GedcomConverter — confirm no buffering of entire file, each parser event emits immediately to LineEmitter. The core design (Phase 2) should already be streaming; this task verifies and fixes any buffering issues in src/main/java/org/gedcom7/converter/GedcomConverter.java
- [X] T018 [US3] Run StreamingConversionTest and all tests: ./gradlew clean test — fix any failures

**Checkpoint**: User Story 3 complete — streaming conversion verified

---

## Phase 6: User Story 4 — Conversion Error Reporting (Priority: P4)

**Goal**: Converter reports parsing errors and conversion warnings through configurable handler

**Independent Test**: Convert malformed file and file with version-specific features, verify warnings reported

### Tests for User Story 4

- [X] T019 [US4] Create test class src/test/java/org/gedcom7/converter/ConversionErrorTest.java with tests: (1) malformed GEDCOM file in lenient mode → parsing errors in ConversionResult, conversion continues; (2) file with version-specific structures → conversion warnings emitted; (3) strict mode → converter stops at first error (throws exception); (4) warningHandler callback receives each warning as it occurs; (5) ConversionResult contains all warnings and errors

### Implementation for User Story 4

- [X] T020 [US4] Implement error forwarding in GedcomConverter — warning/error/fatalError handler methods forward to ConversionResult.Builder and invoke ConversionWarningHandler if configured; strict mode checks in convert() method throw on first warning/error. Implement in src/main/java/org/gedcom7/converter/GedcomConverter.java
- [X] T021 [US4] Run ConversionErrorTest and all tests: ./gradlew clean test — fix any failures

**Checkpoint**: User Story 4 complete — error reporting works

---

## Phase 7: User Story 5 — Xref Preservation (Priority: P5)

**Goal**: Cross-reference identifiers preserved exactly from input to output

**Independent Test**: Convert file with custom xref IDs, verify all xrefs match exactly

### Tests for User Story 5

- [X] T022 [US5] Create test class src/test/java/org/gedcom7/converter/XrefPreservationTest.java with tests: (1) GEDCOM file with xref IDs @I1@, @F1@, @S1@ → output uses same xref IDs; (2) pointer references (HUSB @I1@, WIFE @I2@, CHIL @I3@) → preserved in output; (3) round-trip: convert 5.5.5→7→5.5.5, verify all xrefs match original

### Implementation for User Story 5

- [X] T023 [US5] Verify xref passthrough in GedcomConverter — startRecord receives xref from parser, emits it directly via LineEmitter.emitLine(level, xref, tag, value); pointer values (isPointer=true) emit @id@ format. The core design should already preserve xrefs; fix any issues in src/main/java/org/gedcom7/converter/GedcomConverter.java
- [X] T024 [US5] Run XrefPreservationTest and all tests: ./gradlew clean test — fix any failures

**Checkpoint**: User Story 5 complete — xref preservation verified

---

## Phase 8: Edge Cases & Round-Trip

**Purpose**: Edge case handling and round-trip fidelity verification

- [X] T025 [P] Create edge case test class src/test/java/org/gedcom7/converter/EdgeCaseTest.java with tests: (1) unrecognized GEDCOM version (not 5.5.5 or 7.0) → error reported, conversion refused; (2) input already in target version → processed (normalization); (3) empty/null values → preserved; (4) deeply nested structures → preserved at same depth; (5) SNOTE records with inline text → preserved in both directions; (6) extension tags with HEAD.SCHMA → passed through
- [X] T026 [P] Create round-trip test class src/test/java/org/gedcom7/converter/RoundTripTest.java with tests: (1) convert 5.5.5→7→5.5.5, verify output equivalent to original (excluding formatting); (2) convert 7→5.5.5→7, verify output equivalent to original; (3) round-trip preserves all record types (INDI, FAM, SOUR, REPO, OBJE, SUBM, SNOTE)
- [X] T027 Implement edge case handling in GedcomConverter — unrecognized version detection (check GedcomHeaderInfo.getVersion() in startDocument, report fatal error if not 5.x or 7.x); ensure normalization works (same source/target version). Fix any issues in src/main/java/org/gedcom7/converter/GedcomConverter.java
- [X] T028 Run all edge case and round-trip tests: ./gradlew clean test — fix any failures

**Checkpoint**: All edge cases handled, round-trip fidelity verified

---

## Phase 9: Per-Story Independent Reviews (Constitution Principle VIII)

**Purpose**: Each user story implementation reviewed by an independent agent

- [X] T029 [P] Independent agent review of US1 (5.5.5→7) — verify all 5 acceptance scenarios pass, constitution compliance, test coverage
- [X] T030 [P] Independent agent review of US2 (7→5.5.5) — verify all 5 acceptance scenarios pass (including SCHMA preservation), constitution compliance, test coverage
- [X] T031 [P] Independent agent review of US3 (Streaming) — verify 2 acceptance scenarios pass, no buffering, constitution compliance
- [X] T032 [P] Independent agent review of US4 (Error Reporting) — verify 3 acceptance scenarios pass, lenient/strict modes, constitution compliance
- [X] T033 [P] Independent agent review of US5 (Xref Preservation) — verify 2 acceptance scenarios pass, constitution compliance
- [X] T034 [P] Independent agent review of Edge Cases — verify all 6 edge cases handled, round-trip fidelity (SC-003)

---

## Phase 10: Final Evaluation (Constitution Principle VIII)

**Purpose**: Comprehensive final evaluation by independent agent

- [X] T035 Run full test suite: ./gradlew clean test — all tests must pass
- [X] T036 Independent agent final evaluation — compare complete implementation against all 11 FRs, all acceptance scenarios from all 5 user stories, all 6 success criteria, all 8 constitution principles; produce written report with PASS/GAP/DEVIATION for each item
- [X] T037 Address any gaps or deviations identified in T036

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user stories
- **Phase 3-7 (User Stories)**: All depend on Phase 2 completion
  - US1 and US2 can run in parallel (different conversion directions)
  - US3, US4, US5 can run in parallel after US1 or US2 (they test cross-cutting concerns)
- **Phase 8 (Edge Cases)**: Depends on Phase 2; can run in parallel with US phases
- **Phase 9 (Reviews)**: Each review depends on its corresponding US phase completion
- **Phase 10 (Final)**: Depends on ALL previous phases

### User Story Dependencies

- **US1 (5.5.5→7)**: Depends on Phase 2 only — no other story dependencies
- **US2 (7→5.5.5)**: Depends on Phase 2 only — no other story dependencies
- **US3 (Streaming)**: Depends on Phase 2; tests use both conversion directions
- **US4 (Error Reporting)**: Depends on Phase 2; tests use conversion in either direction
- **US5 (Xref Preservation)**: Depends on Phase 2; tests use both conversion directions

### Parallel Opportunities

- T002, T003, T004 can run in parallel (different files, no dependencies)
- T010 and T013 can run in parallel (different test files)
- T016, T019, T022 can run in parallel (different test files)
- T025 and T026 can run in parallel (different test files)
- T029-T034 can all run in parallel (independent review agents)

---

## Parallel Example: Phase 1 Setup

```text
# Launch foundational types in parallel:
Agent A: T002 ConversionWarning + T003 ConversionWarningHandler (2 small files)
Agent B: T004 ConversionResult (1 file with Builder)
Agent C: T005 GedcomConverterConfig (1 file with Builder + factories)
```

## Parallel Example: User Stories

```text
# After Phase 2, launch US1 and US2 in parallel:
Agent A: T010-T012 (US1: 5.5.5→7 tests + implementation)
Agent B: T013-T015 (US2: 7→5.5.5 tests + implementation)

# Then US3, US4, US5 in parallel:
Agent C: T016-T018 (US3: Streaming tests + verification)
Agent D: T019-T021 (US4: Error reporting tests + implementation)
Agent E: T022-T024 (US5: Xref tests + verification)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T006)
2. Complete Phase 2: Foundational converter (T007-T009)
3. Complete Phase 3: US1 — 5.5.5→7 conversion (T010-T012)
4. **STOP and VALIDATE**: Convert a real 5.5.5 file to GEDCOM 7

### Incremental Delivery

1. Setup + Foundational → Core converter ready
2. US1 (5.5.5→7) → Most common conversion works
3. US2 (7→5.5.5) → Bidirectional conversion works
4. US3-US5 → Streaming, error reporting, xref preservation verified
5. Edge cases + round-trip → Full robustness
6. Reviews + final evaluation → Quality gates complete

### Multi-Agent Strategy

Phase 1 setup types distributed across 3 parallel agents. Phase 2 core converter is sequential (single file). User stories distributed across 2-5 parallel agents. Reviews run as 6 parallel agents. Final evaluation by 1 independent agent.

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Constitution Principle VIII requires per-story reviews (Phase 9) + final evaluation (Phase 10)
- The converter's core architecture is streaming by design — US3 is primarily a verification task
- US4 and US5 primarily verify behaviors that the core converter should already support
- LineEmitter is public in org.gedcom7.writer.internal — converter can create instances directly
