# Tasks: Parser Gaps Remediation

**Input**: Design documents from `/specs/005-parser-gaps-remediation/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Included per constitution Principle V (Test-Driven Development).

**Organization**: Tasks are grouped by user story. Most user stories are independent and can be executed by separate agents in parallel. Per constitution Principle VIII, each story has a review task performed by an independent agent, and a final evaluation task covers the entire feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the SPI package and shared interfaces that multiple stories depend on.

- [X] T001 Create SPI package directory at src/main/java/org/gedcom7/parser/spi/
- [X] T002 Move GedcomInputDecoder interface to src/main/java/org/gedcom7/parser/spi/GedcomInputDecoder.java — copy the interface with public visibility, add Javadoc per constitution Principle IV. Original in internal/ becomes a re-export or is deleted (update imports in Utf8InputDecoder, BomDetectingDecoder, GedcomReaderConfig.Builder)
- [X] T003 Move PayloadAssembler interface to src/main/java/org/gedcom7/parser/spi/PayloadAssembler.java — same pattern as T002. Update imports in ContOnlyAssembler, ContConcAssembler, GedcomReader, GedcomReaderConfig.Builder
- [X] T004 Move AtEscapeStrategy interface to src/main/java/org/gedcom7/parser/spi/AtEscapeStrategy.java — same pattern as T002. Update imports in LeadingAtEscapeStrategy, AllAtEscapeStrategy, GedcomReader, GedcomReaderConfig.Builder
- [X] T005 Update module-info.java to add `exports org.gedcom7.parser.spi;`
- [X] T006 Run `./gradlew clean test` to verify all existing tests pass after SPI migration

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the GedcomDateValue interface and startRecord overload that multiple stories depend on.

**CRITICAL**: Phase 3+ stories that depend on these must wait for this phase to complete.

- [X] T007 Create DateValueType enum in src/main/java/org/gedcom7/parser/datatype/DateValueType.java with values: EXACT, RANGE, PERIOD, APPROXIMATE, UNPARSEABLE. Add Javadoc. UNPARSEABLE is for date strings that cannot be parsed — returned instead of null or exception
- [X] T008 Create GedcomDateValue interface in src/main/java/org/gedcom7/parser/datatype/GedcomDateValue.java with methods: `DateValueType getType()`, `String getOriginalText()`. Add Javadoc
- [X] T009 Add `startRecord(int level, String xref, String tag, String value)` default method to src/main/java/org/gedcom7/parser/GedcomHandler.java — default delegates to existing 3-param startRecord. Add Javadoc per contract
- [X] T010 Run `./gradlew clean test` to verify all existing tests pass after foundational changes

**Checkpoint**: Foundation ready — user story implementation can now begin in parallel

---

## Phase 3: User Story 1 — All Standard Event Types Recognized (Priority: P1)

**Goal**: Verify all GEDCOM 7 individual/family event types and LDS ordinance tags are in StructureDefinitions with correct child structures. Add comprehensive test coverage to lock down existing definitions.

**Independent Test**: Parse GEDCOM 7 file with all standard event types with validation enabled → zero false warnings.

**Dependencies**: None (can start after Phase 2)

### Tests for User Story 1

- [X] T011 [P] [US1] Write StructureDefinitionsCompletenessTest in src/test/java/org/gedcom7/parser/validation/StructureDefinitionsCompletenessTest.java — test that resolveStructure returns non-null for every individual event type (ADOP, BAPM, BARM, BASM, BIRT, BLES, BURI, CENS, CHR, CHRA, CONF, CREM, DEAT, EMIG, FCOM, GRAD, IMMI, NATU, ORDN, PROB, RETI, WILL) under record-INDI context. Test LDS types (BAPL, CONL, ENDL, INIL, SLGC) under record-INDI. Test family events (ANUL, CENS, DIV, DIVF, ENGA, MARB, MARC, MARL, MARR, MARS) under record-FAM. Test SLGS under record-FAM. Test child structures (DATE, PLAC, SOUR, NOTE) resolve under each event. Test LDS-specific children (TEMP, STAT). Test that genuinely unknown tags return null

### Implementation for User Story 1

- [X] T012 [US1] Verify and fix any missing child structure definitions in src/main/data/gedcom7/substructures.tsv and src/main/data/gedcom7/cardinalities.tsv — specifically check TEMP and STAT are valid children of BAPL, CONL, ENDL, INIL, SLGC, SLGS. If missing, add them and run `./gradlew generateStructureDefinitions` to regenerate StructureDefinitions.java
- [X] T013 [US1] Run `./gradlew clean test` to verify T011 tests pass and no regressions

### Review for User Story 1

- [X] T014 [US1] INDEPENDENT REVIEW: A separate agent reviews US1 implementation against spec acceptance scenarios (US1 scenarios 1-3), FR-001, FR-002, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: All standard event types verified and locked down with tests

---

## Phase 4: User Story 2 — Minimum Cardinality Enforcement (Priority: P1)

**Goal**: Warn when required child structures ({1:1}, {1:M}) are missing from their parent.

**Independent Test**: Parse file with ASSO missing ROLE → warning emitted.

**Dependencies**: None (can start after Phase 2)

### Tests for User Story 2

- [X] T015 [P] [US2] Write MinCardinalityTest in src/test/java/org/gedcom7/parser/validation/MinCardinalityTest.java — test ASSO without ROLE emits warning; ASSO with ROLE emits no warning; {1:M} child present → no warning; {1:M} child absent → warning; validation disabled → no warning even when required child missing

### Implementation for User Story 2

- [X] T016 [US2] Add `isRequired(String cardinality)` static method to src/main/java/org/gedcom7/parser/validation/StructureDefinitions.java — returns true when cardinality starts with `{1:`
- [X] T017 [US2] Add `getRequiredChildren(String contextId)` static method to src/main/java/org/gedcom7/parser/validation/StructureDefinitions.java — returns Map of required child structureIds to their cardinalities for the given parent context
- [X] T018 [US2] Implement minimum cardinality check in src/main/java/org/gedcom7/parser/GedcomReader.java — at endStructure/endRecord time, if validation enabled, call getRequiredChildren for the closing structure's contextId. For each required child with count 0 in childCounts, emit a validation warning via handler.warning(). Skip check if parent already had parse errors (avoid cascading warnings per edge case spec)
- [X] T019 [US2] Run `./gradlew clean test` to verify T015 tests pass and no regressions

### Review for User Story 2

- [X] T020 [US2] INDEPENDENT REVIEW: A separate agent reviews US2 implementation against spec acceptance scenarios (US2 scenarios 1-5), FR-003, FR-004, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: Min-cardinality enforcement working with opt-in validation

---

## Phase 5: User Story 3 — Type-Safe Date Values (Priority: P2)

**Goal**: parseDateValue() returns GedcomDateValue instead of Object.

**Independent Test**: Call parseDateValue with range/period/exact strings → all implement GedcomDateValue with correct getType().

**Dependencies**: Phase 2 (T007, T008 for GedcomDateValue interface)

### Tests for User Story 3

- [X] T021 [P] [US3] Write GedcomDateValueTest in src/test/java/org/gedcom7/parser/datatype/GedcomDateValueTest.java — test parseDateValue("BET 1 JAN 1900 AND 31 DEC 1900") returns GedcomDateValue with type RANGE; test "FROM 1 JAN 1900 TO 31 DEC 1900" returns PERIOD; test "6 APR 1952" returns EXACT; test "ABT 1900" returns APPROXIMATE; test getOriginalText() returns input string; test existing instanceof GedcomDate/GedcomDateRange/GedcomDatePeriod checks still work

### Implementation for User Story 3

- [X] T022 [US3] Add `implements GedcomDateValue` to GedcomDate in src/main/java/org/gedcom7/parser/datatype/GedcomDate.java — implement getType() returning EXACT, implement getOriginalText(). Add originalText field stored at parse time
- [X] T023 [P] [US3] Add `implements GedcomDateValue` to GedcomDateRange in src/main/java/org/gedcom7/parser/datatype/GedcomDateRange.java — implement getType() returning RANGE or APPROXIMATE based on prefix: ABT/CAL/EST → APPROXIMATE; BET/BEF/AFT → RANGE (BEF/AFT are open-ended ranges per GEDCOM 7 spec's DateRange production). Add originalText field
- [X] T024 [P] [US3] Add `implements GedcomDateValue` to GedcomDatePeriod in src/main/java/org/gedcom7/parser/datatype/GedcomDatePeriod.java — implement getType() returning PERIOD. Add originalText field
- [X] T025 [US3] Change parseDateValue() return type from Object to GedcomDateValue in src/main/java/org/gedcom7/parser/datatype/GedcomDataTypes.java — pass original text to each constructor. Handle unparseable input by returning a GedcomDateValue implementation that indicates parse failure (not null, not exception)
- [X] T026 [US3] Run `./gradlew clean test` to verify T021 tests pass and no regressions

### Review for User Story 3

- [X] T027 [US3] INDEPENDENT REVIEW: A separate agent reviews US3 implementation against spec acceptance scenarios (US3 scenarios 1-4), FR-005, FR-006, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: Type-safe date values working; backward compatible

---

## Phase 6: User Story 4 — Public Strategy Interfaces (Priority: P2)

**Goal**: Strategy interfaces accessible from external packages.

**Independent Test**: From a separate test package, instantiate Builder and supply custom InputDecoder → compiles and runs.

**Dependencies**: Phase 1 (T001-T006 for SPI package)

### Tests for User Story 4

- [X] T028 [P] [US4] Write StrategyVisibilityTest in src/test/java/org/gedcom7/parser/spi/StrategyVisibilityTest.java (note: in spi test package, not internal) — test that a custom GedcomInputDecoder can be created and passed to Builder.inputDecoder(); test custom PayloadAssembler can be passed to Builder.payloadAssembler(); test custom AtEscapeStrategy can be passed to Builder.atEscapeStrategy(); verify custom decoder is actually invoked during parsing by checking side effect

### Implementation for User Story 4

- [X] T029 [US4] Verify all Builder methods in src/main/java/org/gedcom7/parser/GedcomReaderConfig.java reference the SPI types (not internal types). Update import statements if needed. Ensure Builder.inputDecoder(), Builder.payloadAssembler(), Builder.atEscapeStrategy() parameter types use org.gedcom7.parser.spi interfaces
- [X] T030 [US4] Run `./gradlew clean test` to verify T028 tests pass and no regressions

### Review for User Story 4

- [X] T031 [US4] INDEPENDENT REVIEW: A separate agent reviews US4 implementation against spec acceptance scenarios (US4 scenarios 1-4), FR-007, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: Strategy interfaces publicly accessible

---

## Phase 7: User Story 5 — Record Payloads for Level-0 Records (Priority: P2)

**Goal**: startRecord delivers the record's payload value.

**Independent Test**: Parse `0 @N1@ SNOTE This is a note` → startRecord provides "This is a note".

**Dependencies**: Phase 2 (T009 for startRecord overload)

### Tests for User Story 5

- [X] T032 [P] [US5] Write StartRecordValueTest in src/test/java/org/gedcom7/parser/StartRecordValueTest.java — test that parsing `0 @N1@ SNOTE This is a note` delivers value "This is a note" to the 4-param startRecord; test `0 @I1@ INDI` delivers null value; test SNOTE with CONT continuation lines delivers assembled multi-line value; test existing 3-param handler still works (backward compat)

### Implementation for User Story 5

- [X] T033 [US5] Modify GedcomReader in src/main/java/org/gedcom7/parser/GedcomReader.java to call `handler.startRecord(level, xref, tag, value)` instead of `handler.startRecord(level, xref, tag)` when emitting record events. Extract the record's line value from the tokenized line and pass it. For records with CONT continuation, assemble the full payload before delivering
- [X] T034 [US5] Run `./gradlew clean test` to verify T032 tests pass and no regressions

### Review for User Story 5

- [X] T035 [US5] INDEPENDENT REVIEW: A separate agent reviews US5 implementation against spec acceptance scenarios (US5 scenarios 1-3), FR-008, FR-009, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: Record payloads accessible via startRecord

---

## Phase 8: User Story 6 — Xref Validation (Priority: P2)

**Goal**: Validate cross-reference identifiers conform to GEDCOM 7 character rules.

**Independent Test**: Parse line with xref containing space → validation warning emitted.

**Dependencies**: None (can start after Phase 2)

### Tests for User Story 6

- [X] T036 [P] [US6] Write XrefValidationTest in src/test/java/org/gedcom7/parser/XrefValidationTest.java — test xref `@I 1@` (space) emits warning; test `@I1@` (valid) emits no warning; test empty xref `@@` emits warning; test xref with control character emits warning; test validation disabled → no xref warning; test `@VOID@` passes (special case)

### Implementation for User Story 6

- [X] T037 [US6] Add xref character validation to src/main/java/org/gedcom7/parser/GedcomReader.java — when validation is enabled and an xref is present on a line, validate that all characters between the @ delimiters are valid per GEDCOM 7 spec (no spaces, no control characters U+0000-U+001F and U+007F, no # character, at least 1 character). If invalid, emit warning via handler.warning() with descriptive message including the invalid character and line number
- [X] T038 [US6] Run `./gradlew clean test` to verify T036 tests pass and no regressions

### Review for User Story 6

- [X] T039 [US6] INDEPENDENT REVIEW: A separate agent reviews US6 implementation against spec acceptance scenarios (US6 scenarios 1-4), FR-010, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: Xref validation working with opt-in validation

---

## Phase 9: User Story 7 — Accurate Byte Offset Tracking (Priority: P3)

**Goal**: GedcomLine.byteOffset reports actual byte position.

**Independent Test**: Parse file with multi-byte UTF-8 → byteOffset values match actual positions.

**Dependencies**: None (can start after Phase 2)

### Tests for User Story 7

- [X] T040 [P] [US7] Write ByteOffsetTest in src/test/java/org/gedcom7/parser/ByteOffsetTest.java — test that line 3 of a known file has correct byteOffset matching manual calculation; test multi-byte UTF-8 characters correctly shift subsequent offsets; test BOM-prefixed file has first line offset at 3; test Reader-based input returns -1 for byteOffset

### Implementation for User Story 7

- [X] T041 [US7] Create CountingInputStream wrapper in src/main/java/org/gedcom7/parser/internal/CountingInputStream.java — wraps InputStream and counts bytes read via read(), read(byte[]), read(byte[], int, int). Provides `long getBytesRead()` method
- [X] T042 [US7] Integrate CountingInputStream into GedcomReader in src/main/java/org/gedcom7/parser/GedcomReader.java — wrap the input InputStream in CountingInputStream before passing to InputDecoder. Pass the counter reference to GedcomLineTokenizer so it can stamp byteOffset on each GedcomLine at the start of each new line
- [X] T043 [US7] Update GedcomLineTokenizer in src/main/java/org/gedcom7/parser/internal/GedcomLineTokenizer.java to accept an optional byte counter. At the start of each new line, set `line.setByteOffset(counter.getBytesRead())`. When no counter provided (Reader-based input), set byteOffset to -1
- [X] T044 [US7] Run `./gradlew clean test` to verify T040 tests pass and no regressions

### Review for User Story 7

- [X] T045 [US7] INDEPENDENT REVIEW: A separate agent reviews US7 implementation against spec acceptance scenarios (US7 scenarios 1-3), FR-011, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: Byte offsets accurate for InputStream-based parsing

---

## Phase 10: User Story 8 — Buffered Tokenizer I/O (Priority: P3)

**Goal**: Replace character-by-character reads with buffered bulk reads for 2x throughput.

**Independent Test**: Parse 100K+ line file → 1.5x faster than baseline (avg of 5 runs after warm-up); identical results.

**Dependencies**: None (can start after Phase 2), but should coordinate with US7 (both modify tokenizer)

**IMPORTANT**: If US7 and US8 are assigned to separate agents, US8 should start after US7 completes (both modify GedcomLineTokenizer). Alternatively, a single agent can handle both.

### Tests for User Story 8

- [X] T046 [US8] Write TokenizerPerformanceTest in src/test/java/org/gedcom7/parser/TokenizerPerformanceTest.java — generate a 100,000-line GEDCOM file in-memory, parse it after 3 warm-up iterations, measure average wall-clock time across 5 runs. Also parse with a known small file and compare results character-by-character against expected output to verify identical behavior. Tag performance assertions with `@Tag("performance")` to exclude from default CI. Target: 1.5x improvement. NOTE: This task depends on US7 (T040-T045) completing first since both modify GedcomLineTokenizer

### Implementation for User Story 8

- [X] T047 [US8] Refactor GedcomLineTokenizer in src/main/java/org/gedcom7/parser/internal/GedcomLineTokenizer.java to use buffered bulk reads — replace single-character `reader.read()` calls with `reader.read(char[] buf, 0, bufSize)` using an 8KB char[] buffer. Maintain buffer position and limit. Refill when exhausted. Preserve all existing tokenization logic and line grammar parsing. All existing tests must produce identical results
- [X] T048 [US8] Run `./gradlew clean test` to verify T046 tests pass, ALL existing tests pass (critical: identical parse results), and no regressions

### Review for User Story 8

- [X] T049 [US8] INDEPENDENT REVIEW: A separate agent reviews US8 implementation against spec acceptance scenarios (US8 scenarios 1-2), FR-012, FR-013, constitution principles I-VIII (especially III Mechanical Sympathy). Document findings and any gaps

**Checkpoint**: Tokenizer uses buffered I/O with 1.5x+ throughput improvement

---

## Phase 11: User Story 9 — Unknown Level-0 Tag Validation (Priority: P3)

**Goal**: Warn on unrecognized record types at level 0 (excluding extension tags).

**Independent Test**: Parse `0 @X1@ ZZUNKNOWN` with validation → warning. Extension tags → no warning.

**Dependencies**: None (can start after Phase 2)

### Tests for User Story 9

- [X] T050 [P] [US9] Write Level0TagValidationTest in src/test/java/org/gedcom7/parser/Level0TagValidationTest.java — test `0 @X1@ ZZUNKNOWN` emits warning; test `0 @I1@ INDI` does not; test `0 @X1@ _CUSTOM` (extension) does not; test all standard record types (INDI, FAM, OBJE, REPO, SNOTE, SOUR, SUBM, HEAD, TRLR) do not warn

### Implementation for User Story 9

- [X] T051 [US9] Add level-0 tag validation to src/main/java/org/gedcom7/parser/GedcomReader.java — when validation is enabled and a level-0 record is encountered, check if the tag is a known record type (INDI, FAM, OBJE, REPO, SNOTE, SOUR, SUBM, HEAD, TRLR) or an extension tag (starts with _). If neither, emit warning via handler.warning() with descriptive message
- [X] T052 [US9] Run `./gradlew clean test` to verify T050 tests pass and no regressions

### Review for User Story 9

- [X] T053 [US9] INDEPENDENT REVIEW: A separate agent reviews US9 implementation against spec acceptance scenarios (US9 scenarios 1-3), FR-014, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: Unknown level-0 tags generate validation warnings

---

## Phase 12: User Story 10 — SimpleGedcomHandler (Priority: P3)

**Goal**: Convenience handler that merges record/structure events.

**Independent Test**: Register SimpleGedcomHandler, parse file → single onStructure fires for both records and substructures.

**Dependencies**: Phase 2 (T009 for startRecord with value)

### Tests for User Story 10

- [X] T054 [P] [US10] Write SimpleGedcomHandlerTest in src/test/java/org/gedcom7/parser/SimpleGedcomHandlerTest.java — test onStructure fires for INDI (level 0) and NAME (level 1); test onEndStructure fires for both endRecord and endStructure; test startDocument and endDocument still fire normally; test parsing a complete file with only onStructure and onEndStructure overrides works

### Implementation for User Story 10

- [X] T055 [US10] Create SimpleGedcomHandler class in src/main/java/org/gedcom7/parser/SimpleGedcomHandler.java — extend GedcomHandler. Add `onStructure(int level, String xref, String tag, String value)` and `onEndStructure(int level, String tag)` as overridable methods with no-op defaults. Override startRecord (4-param), endRecord, startStructure (6-param), endStructure as final methods that delegate to onStructure/onEndStructure. Track current level for endStructure (which doesn't receive level). Add Javadoc per contract
- [X] T056 [US10] Run `./gradlew clean test` to verify T054 tests pass and no regressions

### Review for User Story 10

- [X] T057 [US10] INDEPENDENT REVIEW: A separate agent reviews US10 implementation against spec acceptance scenarios (US10 scenarios 1-3), FR-015, FR-016, constitution principles I-VIII. Document findings and any gaps

**Checkpoint**: SimpleGedcomHandler available for beginner-friendly parsing

---

## Phase 13: Final Evaluation

**Purpose**: Comprehensive independent evaluation of entire feature.

- [X] T058 FINAL EVALUATION: An independent agent performs comprehensive evaluation of the complete 005-parser-gaps-remediation implementation against: (1) every functional requirement FR-001 through FR-016 from spec.md, (2) all acceptance scenarios from all 10 user stories, (3) all success criteria SC-001 through SC-010, (4) compliance with every constitution principle I through VIII, (5) all edge cases from the spec. Produce a written report with findings categorized as PASS, GAP (with severity HIGH/MEDIUM/LOW), or DEVIATION (with justification). HIGH gaps must be fixed before feature completion

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 completion
- **Phases 3-12 (User Stories)**: All depend on Phase 2 completion
  - US7 and US8 both modify GedcomLineTokenizer — execute US7 first, then US8
  - All other user stories are independent and can run in parallel
- **Phase 13 (Final Evaluation)**: Depends on ALL story phases completing

### Parallel Execution Map (Agents)

After Phase 2 completes, the following can run in parallel on separate agents:

```
Agent A: US1 (T011-T014) — Structure definitions completeness
Agent B: US2 (T015-T020) — Minimum cardinality enforcement
Agent C: US3 (T021-T027) — Type-safe date values
Agent D: US4 (T028-T031) — Public strategy interfaces
Agent E: US5 (T032-T035) — Record payloads
Agent F: US6 (T036-T039) — Xref validation
Agent G: US7 (T040-T045) then US8 (T046-T049) — Byte offset + Buffered tokenizer (sequential, same files)
Agent H: US9 (T050-T053) — Level-0 tag validation
Agent I: US10 (T054-T057) — SimpleGedcomHandler
```

**Maximum parallelism**: 9 agents (8 independent + 1 sequential pair)

### Within Each User Story

- Tests FIRST (TDD: write tests, verify they fail)
- Implementation second
- Verify tests pass
- Independent review last

---

## Implementation Strategy

### MVP First (User Stories 1-2 Only)

1. Complete Phase 1: Setup (SPI package)
2. Complete Phase 2: Foundational (GedcomDateValue, startRecord overload)
3. Complete US1 + US2 in parallel (P1 stories)
4. **STOP and VALIDATE**: Run full test suite
5. These fix the most impactful gaps (false warnings, missing validation)

### Full Delivery

1. Complete Setup + Foundational
2. Launch all agents for US1-US10 in parallel (respecting US7→US8 ordering)
3. Each agent completes their story + review independently
4. Final evaluation agent runs after all stories complete
5. Address any HIGH gaps from final evaluation

### Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Commit after each story completes
- Review tasks (T014, T020, T027, T031, T035, T039, T045, T049, T053, T057) MUST be performed by a different agent than the implementer. The orchestrator MUST NOT assign a review task to the same agent instance that performed the corresponding implementation tasks (Constitution Principle VIII)
- Final evaluation (T058) MUST be performed by an agent that did not implement any story. This agent should have no prior context from implementation to ensure independent assessment
