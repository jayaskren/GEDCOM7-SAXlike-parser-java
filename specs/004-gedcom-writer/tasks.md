# Tasks: GEDCOM SAX-like Writer

**Input**: Design documents from `/specs/004-gedcom-writer/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included — the constitution mandates TDD (Principle V) and the spec requires round-trip verification.

**Organization**: Tasks are grouped by user story. Each implementation task is followed by an independent agent review task.

**Review Convention**: Tasks prefixed with `REVIEW:` are performed by an independent agent that reads the implemented code and validates it against the spec (specs/004-gedcom-writer/spec.md), contracts (specs/004-gedcom-writer/contracts/), and constitution (.specify/memory/constitution.md). The review agent should flag any deviations, missing requirements, or principle violations.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Project structure and build configuration for the writer package

- [X] T001 Create writer package directories: src/main/java/org/gedcom7/writer/, src/main/java/org/gedcom7/writer/context/, src/main/java/org/gedcom7/writer/date/, src/main/java/org/gedcom7/writer/internal/, src/test/java/org/gedcom7/writer/, src/test/java/org/gedcom7/writer/context/, src/test/java/org/gedcom7/writer/date/
- [X] T002 Update src/main/java/module-info.java to export org.gedcom7.writer, org.gedcom7.writer.context, and org.gedcom7.writer.date packages
- [X] T003 REVIEW: Agent review of T001-T002 — verify package structure matches plan.md project structure, module-info exports match contracts, and build compiles cleanly

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Implement GedcomWriteException (checked exception) in src/main/java/org/gedcom7/writer/GedcomWriteException.java per contracts/writer-api.md
- [X] T005 [P] Implement GedcomWriteWarning (immutable value object with message + tag) in src/main/java/org/gedcom7/writer/GedcomWriteWarning.java per contracts/writer-api.md
- [X] T006 [P] Implement WarningHandler functional interface in src/main/java/org/gedcom7/writer/WarningHandler.java per contracts/writer-api.md
- [X] T007 [P] Implement Xref (immutable handle with of(), getId(), equals, hashCode, toString) in src/main/java/org/gedcom7/writer/Xref.java per contracts/writer-api.md
- [X] T008 REVIEW: Agent review of T004-T007 — verify all value types match contracts/writer-api.md signatures, immutability (Principle IV), equals/hashCode contracts, and zero external dependencies (Principle VII)
- [X] T009 Implement GedcomWriterConfig with Builder pattern, factory methods (gedcom7, gedcom7Strict, gedcom555, gedcom555Strict), toBuilder(), all fields per data-model.md in src/main/java/org/gedcom7/writer/GedcomWriterConfig.java
- [X] T010 Write GedcomWriterConfigTest covering factory methods, builder, toBuilder, field defaults per contracts/writer-api.md test expectations in src/test/java/org/gedcom7/writer/GedcomWriterConfigTest.java
- [X] T011 REVIEW: Agent review of T009-T010 — verify config mirrors GedcomReaderConfig pattern (research.md Decision 7), factory method defaults match data-model.md, Builder is immutable-safe, tests cover all factory methods
- [X] T012 Implement XrefGenerator (per-prefix counter, tag-based prefixes I/F/S/R/N/O/U/X) in src/main/java/org/gedcom7/writer/internal/XrefGenerator.java per research.md Decision 5
- [X] T013 Implement LineEmitter (low-level line formatting, OutputStream writing, UTF-8 encoding, configurable line endings) in src/main/java/org/gedcom7/writer/internal/LineEmitter.java per research.md Decision 4 and data-model.md LineEmitter entity
- [X] T014 REVIEW: Agent review of T012-T013 — verify XrefGenerator prefix conventions match research.md Decision 5, LineEmitter produces correct GEDCOM line format (level + optional xref + tag + optional value + line ending), UTF-8 output (FR-015), and mechanical sympathy (Principle III)
- [X] T015 Implement CommonContext abstract base class with all escape hatch methods (structure/pointer overloads) and common methods (note, sourceCitation, uid) in src/main/java/org/gedcom7/writer/context/CommonContext.java per contracts/context-api.md and data-model.md CommonContext entity
- [X] T016 Implement GeneralContext (extends CommonContext, no additional methods) in src/main/java/org/gedcom7/writer/context/GeneralContext.java per contracts/context-api.md
- [X] T017 REVIEW: Agent review of T015-T016 — verify CommonContext has ALL 7 escape hatch method signatures from FR-003 and contracts/context-api.md, common methods match data-model.md, GeneralContext extends CommonContext correctly, lambda Consumer pattern used (FR-005)

**Checkpoint**: Foundation ready — all value types, config, line emitter, base context, and xref generator are in place. User story implementation can now begin.

---

## Phase 3: User Story 1 — Write a Simple GEDCOM 7 File (Priority: P1) 🎯 MVP

**Goal**: A developer can create a valid GEDCOM 7 file with HEAD, INDI (name + birth), FAM (husband/wife/child), source citations, and TRLR using typed context methods. Round-trip with existing parser succeeds.

**Independent Test**: Create writer, write HEAD + INDI with name and birth + FAM linking two individuals + TRLR. Parse with GedcomReader and verify no errors.

### Tests for User Story 1

> **Write these tests FIRST, ensure they FAIL before implementation**
>
> **Note on dates**: US1 acceptance scenario 1 references `date(15, MAR, 1955)` from GedcomDateBuilder, which is implemented in Phase 8 (US6). For US1 tests, use `EventContext.date(String rawDateString)` (e.g., `birt.date("15 MAR 1955")`). Full `date(WriterDate)` integration is verified after US6.

- [X] T018 [P] [US1] Write GedcomWriterTest with test cases for: basic INDI record, basic FAM record, HEAD with source, auto-HEAD when head() not called, auto-TRLR on close, IllegalStateException after close, in src/test/java/org/gedcom7/writer/GedcomWriterTest.java — tests should initially fail
- [X] T019 [P] [US1] Write XrefTest covering Xref.of(), getId(), equals/hashCode, toString in src/test/java/org/gedcom7/writer/XrefTest.java
- [X] T020 [P] [US1] Write RoundTripTest that writes a GEDCOM 7 file with GedcomWriter then parses it back with GedcomReader, verifying all structures are read without errors in src/test/java/org/gedcom7/writer/RoundTripTest.java
- [X] T021 REVIEW: Agent review of T018-T020 — verify tests cover all US1 acceptance scenarios (spec.md US1 scenarios 1-6), test expectations match contracts/writer-api.md, round-trip test uses existing GedcomReader, and TDD principle is followed (Principle V)

### Implementation for User Story 1

- [X] T022 [P] [US1] Implement HeadContext with source, source+body, destination, submitterRef, note, schema methods in src/main/java/org/gedcom7/writer/context/HeadContext.java per contracts/context-api.md
- [X] T023 [P] [US1] Implement SchemaContext with tag(extensionTag, uri) method in src/main/java/org/gedcom7/writer/context/SchemaContext.java per contracts/context-api.md
- [X] T024 REVIEW: Agent review of T022-T023 — verify HeadContext methods match contracts/context-api.md HeadContext section, SchemaContext emits SCHMA.TAG correctly (FR-011), both extend CommonContext
- [X] T025 [P] [US1] Implement PersonalNameContext with givenName, surname, namePrefix, nameSuffix, nickname, surnamePrefix, type methods in src/main/java/org/gedcom7/writer/context/PersonalNameContext.java per contracts/context-api.md
- [X] T026 [P] [US1] Implement EventContext with date(WriterDate), date(String), place, place+body, address, cause, agency, type methods in src/main/java/org/gedcom7/writer/context/EventContext.java per contracts/context-api.md
- [X] T027 [P] [US1] Implement SourceCitationContext with page, data, quality, eventType, role methods in src/main/java/org/gedcom7/writer/context/SourceCitationContext.java per contracts/context-api.md
- [X] T028 REVIEW: Agent review of T025-T027 — verify all typed context methods delegate to structure()/pointer() internally (FR-004), method signatures match contracts/context-api.md, GEDCOM tags are correct per GEDCOM 7 spec
- [X] T029 [US1] Implement IndividualContext with personalName, personalName+body, birth, death, christening, burial, residence, sex, occupation, education, religion, familyAsSpouse, familyAsChild methods in src/main/java/org/gedcom7/writer/context/IndividualContext.java per contracts/context-api.md
- [X] T030 [US1] Implement FamilyContext with husband, wife, child, marriage, divorce, annulment methods (each accepting both Xref and String) in src/main/java/org/gedcom7/writer/context/FamilyContext.java per contracts/context-api.md
- [X] T031 REVIEW: Agent review of T029-T030 — verify IndividualContext and FamilyContext match contracts/context-api.md, pointer methods accept both Xref and String (FR-018), familyAsSpouse/familyAsChild emit version-aware warnings (FR-025), all methods delegate to escape hatches (FR-004)
- [X] T032 [US1] Implement GedcomWriter main class with constructors, head(), individual(), family(), source(), repository(), multimedia(), submitter(), sharedNote() (both auto-xref and developer-provided-ID overloads), record() escape hatches, trailer(), close() in src/main/java/org/gedcom7/writer/GedcomWriter.java per contracts/writer-api.md and data-model.md GedcomWriter entity
- [X] T033 REVIEW: Agent review of T032 — verify GedcomWriter matches ALL method signatures in contracts/writer-api.md, implements AutoCloseable (Principle IV), auto-generates HEAD if not called (FR-011), auto-appends TRLR on close (FR-012), throws IllegalStateException after close (FR-013), streaming with no buffering (FR-019), record methods return Xref handles (FR-016/FR-017)
- [X] T034 [US1] Run all US1 tests (GedcomWriterTest, XrefTest, RoundTripTest) — ensure they pass. Fix any failures in src/test/java/org/gedcom7/writer/
- [X] T035 REVIEW: Agent review of US1 implementation — comprehensive review of all Phase 3 code against spec.md US1 acceptance scenarios 1-6, all referenced FRs (FR-001 through FR-005, FR-009 through FR-016, FR-019, FR-020), and constitution principles (esp. IV Java Best Practices, V TDD, VII Zero Dependencies)

**Checkpoint**: User Story 1 (MVP) complete — basic GEDCOM 7 writing works with typed contexts, auto-xref, HEAD/TRLR handling, and round-trip verification.

---

## Phase 4: User Story 2 — Automatic Payload Handling (Priority: P2)

**Goal**: Multi-line values are automatically split into CONT continuation lines. Leading `@` characters are escaped in GEDCOM 7 mode.

**Independent Test**: Write a NOTE with embedded newlines and a value starting with `@`. Verify CONT lines and `@@` escaping.

### Tests for User Story 2

- [X] T036 [P] [US2] Write ContSplittingTest covering: single-line (no split), multi-line (CONT at correct level), \r\n normalization, \r normalization, empty lines in src/test/java/org/gedcom7/writer/ContSplittingTest.java
- [X] T037 [P] [US2] Write AtEscapingTest covering: leading @ doubled in GEDCOM 7, value without @ unchanged, value with @ in middle unchanged in GEDCOM 7 in src/test/java/org/gedcom7/writer/AtEscapingTest.java
- [X] T038 REVIEW: Agent review of T036-T037 — verify tests cover all US2 acceptance scenarios (spec.md US2 scenarios 1-3), edge cases (empty values, \r\n, \r), and FR-006/FR-007

### Implementation for User Story 2

- [X] T039 [P] [US2] Implement ContSplitter (splits value at \n, normalizes \r\n and \r) in src/main/java/org/gedcom7/writer/internal/ContSplitter.java per research.md Decision 4
- [X] T040 [P] [US2] Implement AtEscaper with leading-only mode (GEDCOM 7) in src/main/java/org/gedcom7/writer/internal/AtEscaper.java per research.md Decision 4
- [X] T041 [US2] Integrate ContSplitter and AtEscaper into LineEmitter.emitValueWithCont() method in src/main/java/org/gedcom7/writer/internal/LineEmitter.java
- [X] T042 REVIEW: Agent review of T039-T041 — verify ContSplitter handles all line ending variants (FR-006, edge case: \r\n and \r normalization), AtEscaper only doubles leading @ in GEDCOM 7 mode (FR-007), integration with LineEmitter is correct, mechanical sympathy maintained (Principle III)
- [X] T043 [US2] Run all US2 tests (ContSplittingTest, AtEscapingTest) plus US1 regression tests — ensure all pass in src/test/java/org/gedcom7/writer/
- [X] T044 REVIEW: Agent review of US2 implementation — verify against spec.md US2 acceptance scenarios 1-3, FR-006, FR-007, and no regressions in US1 functionality

**Checkpoint**: Payload handling works — multi-line values produce CONT lines, leading @ is escaped.

---

## Phase 5: User Story 3 — Escape Hatch for Custom and Unsupported Tags (Priority: P3)

**Goal**: Developers can use structure()/pointer() on any context to emit arbitrary tags, and record() on GedcomWriter for custom top-level records. SCHMA declarations for extension tags.

**Independent Test**: Use structure("_CUSTOM", ...) and pointer("_LINK", ...) within an individual context. Write record("_DNATEST", ...) at top level. Verify output.

### Tests for User Story 3

- [X] T045 [P] [US3] Write EscapeHatchTest covering: structure(tag, value), structure(tag, body), structure(tag, value, body), pointer(tag, xref), pointer(tag, xref, body), pointer(tag, string), pointer(tag, string, body) on IndividualContext; also structure("BIRT", ...) produces same output as birth(...) in src/test/java/org/gedcom7/writer/context/EscapeHatchTest.java
- [X] T046 [P] [US3] Add test cases to GedcomWriterTest for record(tag, body), record(id, tag, body), record(tag, value) escape hatch methods and SCHMA tag declarations in src/test/java/org/gedcom7/writer/GedcomWriterTest.java
- [X] T047 REVIEW: Agent review of T045-T046 — verify tests cover all US3 acceptance scenarios (spec.md US3 scenarios 1-7), escape hatch equivalence with typed methods (FR-004, scenario 5), and record() top-level escape hatch (FR-020)

### Implementation for User Story 3

- [X] T048 [US3] Verify and complete all escape hatch overloads on CommonContext — ensure structure(tag, value, body) and pointer(tag, xref, body) forms work correctly with nested GeneralContext lambdas in src/main/java/org/gedcom7/writer/context/CommonContext.java (update if needed from T015)
- [X] T049 [US3] Verify and complete record() escape hatch methods on GedcomWriter — ensure record(tag, body), record(id, tag, body), record(tag, value) all work with auto-xref generation in src/main/java/org/gedcom7/writer/GedcomWriter.java (update if needed from T032)
- [X] T050 REVIEW: Agent review of T048-T049 — verify all 7 escape hatch signatures match FR-003 exactly, typed methods are wrappers over string-based methods (FR-004), record() methods match FR-020, extension tag equivalence (scenario 5)
- [X] T051 [US3] Run all US3 tests (EscapeHatchTest, updated GedcomWriterTest) plus US1/US2 regression — ensure all pass
- [X] T052 REVIEW: Agent review of US3 implementation — verify against spec.md US3 acceptance scenarios 1-7, FR-003, FR-004, FR-020, and no regressions

**Checkpoint**: Escape hatches work — developers can emit any tag on any context, and arbitrary top-level records.

---

## Phase 6: User Story 4 — GEDCOM 5.5.5 Writer Mode (Priority: P4)

**Goal**: Same API produces valid GEDCOM 5.5.5 output when configured with gedcom555(). CONC splitting for long lines, all-@@ escaping, correct version header.

**Independent Test**: Write file with 5.5.5 config. Verify header says 5.5.5, CONC splitting occurs for long lines, all @ characters doubled, FAMS produces no warning.

### Tests for User Story 4

- [X] T053 [P] [US4] Write Gedcom555WriterTest covering: header VERS 5.5.5, CONC splitting for long values, all-@@ escaping for values with @, FAMS/FAMC without warning, non-Gregorian date prefix format (@#D...@) in src/test/java/org/gedcom7/writer/Gedcom555WriterTest.java
- [X] T054 [P] [US4] Write ConcSplittingTest covering: value under maxLineLength (no split), value over maxLineLength (CONC at correct level), split at character boundary, multiple CONC lines in src/test/java/org/gedcom7/writer/ConcSplittingTest.java
- [X] T055 REVIEW: Agent review of T053-T054 — verify tests cover all US4 acceptance scenarios (spec.md US4 scenarios 1-5), FR-008 (CONC + all-@@), and 5.5.5-specific behavior

### Implementation for User Story 4

- [X] T056 [US4] Implement ConcSplitter (splits value at maxLineLength character boundary) in src/main/java/org/gedcom7/writer/internal/ConcSplitter.java per research.md Decision 4
- [X] T057 [US4] Add all-@@ escaping mode to AtEscaper (doubles all @ characters, not just leading) in src/main/java/org/gedcom7/writer/internal/AtEscaper.java
- [X] T058 [US4] Integrate ConcSplitter into LineEmitter — conditionally apply CONC splitting when config.concEnabled is true in src/main/java/org/gedcom7/writer/internal/LineEmitter.java
- [X] T059 [US4] Update HeadContext to emit version-aware GEDC.VERS value (7.0 vs 5.5.5 based on config) in src/main/java/org/gedcom7/writer/context/HeadContext.java
- [X] T060 REVIEW: Agent review of T056-T059 — verify ConcSplitter splits at character boundaries (not mid-character), AtEscaper all-@@ mode matches spec.md US2 scenario 3, LineEmitter conditionally applies CONC, HeadContext emits correct version string, same code works for both versions (SC-003)
- [X] T061 [US4] Run all US4 tests (Gedcom555WriterTest, ConcSplittingTest) plus US1/US2/US3 regression — ensure all pass
- [X] T062 REVIEW: Agent review of US4 implementation — verify against spec.md US4 acceptance scenarios 1-5, FR-008, SC-003 (zero code changes between versions), and no regressions

**Checkpoint**: GEDCOM 5.5.5 mode works — same API, different config, correct output format.

---

## Phase 7: User Story 5 — Cross-Reference Management (Priority: P5)

**Goal**: Flexible xref management with both auto-generated Xref handles and developer-provided string IDs. Forward references work. Database export pattern supported.

**Independent Test**: Write individuals and families using both auto-generated and developer-provided IDs. Write families before individuals (forward refs). Verify all cross-references resolve.

### Tests for User Story 5

- [X] T063 [P] [US5] Add cross-reference test cases to GedcomWriterTest covering: auto-generated xref from individual(), developer-provided ID from individual("42", ...), husband(Xref) vs husband(String), forward references (family before individual), database export pattern in src/test/java/org/gedcom7/writer/GedcomWriterTest.java
- [X] T064 REVIEW: Agent review of T063 — verify tests cover all US5 acceptance scenarios (spec.md US5 scenarios 1-6), both Pattern A (auto-generated) and Pattern B (database export), FR-016 through FR-019

### Implementation for User Story 5

- [X] T065 [US5] Verify and complete developer-provided ID overloads on GedcomWriter — ensure individual(String, Consumer), family(String, Consumer), etc. all wrap the ID in @..@ delimiters and return Xref in src/main/java/org/gedcom7/writer/GedcomWriter.java (update if needed from T032)
- [X] T066 [US5] Verify and complete string-based pointer methods on FamilyContext — ensure husband(String), wife(String), child(String) work alongside Xref versions in src/main/java/org/gedcom7/writer/context/FamilyContext.java (update if needed from T030)
- [X] T067 REVIEW: Agent review of T065-T066 — verify developer-provided IDs are wrapped in @..@ (FR-010), both Xref and String overloads exist on all pointer methods (FR-018), streaming order is preserved (FR-019), no xref uniqueness validation (spec assumption)
- [X] T068 [US5] Run all US5 tests plus full regression — ensure all pass
- [X] T069 REVIEW: Agent review of US5 implementation — verify against spec.md US5 acceptance scenarios 1-6, FR-016/FR-017/FR-018/FR-019, SC-006 (database export without ID mapping), and no regressions

**Checkpoint**: Cross-reference management works — auto-generated and developer-provided IDs, forward references, database export pattern.

---

## Phase 8: User Story 6 — Type-Safe Date Construction (Priority: P6)

**Goal**: GedcomDateBuilder provides static factory methods for all GEDCOM date forms with type-safe month enums. Validation catches invalid dates. Expert escape hatch via raw().

**Independent Test**: Create dates using builder (exact, approximate, range, period, BCE, non-Gregorian). Verify correct GEDCOM strings. Verify invalid dates throw.

### Tests for User Story 6

- [X] T070 [P] [US6] Write GedcomDateBuilderTest covering: exact dates (day/month/year, month/year, year-only), BCE dates, approximate dates (about, calculated, estimated), ranges (before, after, between), periods (from, to, fromTo), non-Gregorian (julian, hebrew, frenchRepublican), raw escape hatch in src/test/java/org/gedcom7/writer/date/GedcomDateBuilderTest.java per contracts/date-api.md test expectations
- [X] T071 [P] [US6] Write DateValidationTest covering: day out of range (32 JAN, 0 JAN), year < 1, between with reversed dates, fromTo with reversed dates, BCE with Hebrew calendar rejection in src/test/java/org/gedcom7/writer/date/DateValidationTest.java per contracts/date-api.md validation rules
- [X] T072 [P] [US6] Write MonthEnumTest covering: Month abbreviation() and maxDay() for all 12 months, HebrewMonth abbreviation() for all 13 months, FrenchRepublicanMonth abbreviation() for all 13 months in src/test/java/org/gedcom7/writer/date/MonthEnumTest.java
- [X] T073 REVIEW: Agent review of T070-T072 — verify tests cover all US6 acceptance scenarios (spec.md US6 scenarios 1-10), all date forms from contracts/date-api.md, all validation rules, and rendering for both GEDCOM 7 and 5.5.5 formats

### Implementation for User Story 6

- [X] T074 [P] [US6] Implement Month enum (12 Gregorian months with abbreviation() and maxDay()) in src/main/java/org/gedcom7/writer/date/Month.java per contracts/date-api.md
- [X] T075 [P] [US6] Implement HebrewMonth enum (13 Hebrew months with abbreviation()) in src/main/java/org/gedcom7/writer/date/HebrewMonth.java per contracts/date-api.md
- [X] T076 [P] [US6] Implement FrenchRepublicanMonth enum (13 months with abbreviation()) in src/main/java/org/gedcom7/writer/date/FrenchRepublicanMonth.java per contracts/date-api.md
- [X] T077 REVIEW: Agent review of T074-T076 — verify enum values match GEDCOM 7 spec abbreviations exactly, Month.maxDay() values are correct (28 for FEB), separate enums prevent cross-calendar misuse at compile time (spec.md US6 design rationale)
- [X] T078 [US6] Implement WriterDate (immutable value object with toGedcomString(GedcomVersion) rendering, raw() static factory) in src/main/java/org/gedcom7/writer/date/WriterDate.java per contracts/date-api.md and data-model.md WriterDate entity
- [X] T079 [US6] Implement GedcomDateBuilder with all static factory methods (date, dateBce, about, calculated, estimated, before, after, between, from, to, fromTo, julian, hebrew, frenchRepublican) and input validation in src/main/java/org/gedcom7/writer/date/GedcomDateBuilder.java per contracts/date-api.md
- [X] T080 REVIEW: Agent review of T078-T079 — verify WriterDate rendering matches ALL examples in contracts/date-api.md rendering rules (GEDCOM 7 and 5.5.5 formats), validation throws IllegalArgumentException per contracts/date-api.md validation rules table, GedcomDateBuilder has ALL factory method signatures from contracts/date-api.md, raw() bypasses all validation (FR-021/FR-022)
- [X] T081 [US6] Integrate WriterDate into EventContext.date(WriterDate) method — ensure it calls toGedcomString() with the writer's configured version in src/main/java/org/gedcom7/writer/context/EventContext.java
- [X] T082 REVIEW: Agent review of T081 — verify EventContext.date(WriterDate) renders version-aware date strings, integration with GedcomWriterConfig.getVersion() is correct
- [X] T083 [US6] Run all US6 tests (GedcomDateBuilderTest, DateValidationTest, MonthEnumTest) plus full regression — ensure all pass
- [X] T084 REVIEW: Agent review of US6 implementation — verify against spec.md US6 acceptance scenarios 1-10, FR-021, FR-022, SC-007, contracts/date-api.md rendering and validation rules, and no regressions

**Checkpoint**: Date construction works — all standard GEDCOM date forms via builder, validation catches invalid dates, expert escape hatch available.

---

## Phase 9: User Story 7 — Strict vs Lenient Mode and Warning Control (Priority: P7)

**Goal**: Strict mode throws GedcomWriteException on issues. Lenient mode delivers warnings to configurable WarningHandler. Null handler suppresses warnings.

**Independent Test**: Write FAMS in GEDCOM 7 mode: strict (verify exception), lenient (verify warning), null handler (verify silent).

### Tests for User Story 7

- [X] T085 [P] [US7] Write StrictModeTest covering: missing HEAD throws in strict, FAMS in GEDCOM 7 strict throws, version-inappropriate structure throws in src/test/java/org/gedcom7/writer/StrictModeTest.java
- [X] T086 [P] [US7] Write WarningHandlerTest covering: lenient mode delivers warning to handler, default handler logs to java.util.logging, null handler suppresses warnings, custom handler receives structured GedcomWriteWarning with message and tag in src/test/java/org/gedcom7/writer/WarningHandlerTest.java
- [X] T087 REVIEW: Agent review of T085-T086 — verify tests cover all US7 acceptance scenarios (spec.md US7 scenarios 1-5), FR-023, FR-024, FR-025

### Implementation for User Story 7

- [X] T088 [US7] Implement warn() helper method on GedcomWriter or CommonContext that checks strict flag (throw GedcomWriteException) then delegates to WarningHandler (if non-null) per research.md Decision 6 pattern in src/main/java/org/gedcom7/writer/GedcomWriter.java
- [X] T089 [US7] Add strict-mode checks for: HEAD not called before first record, and other warning scenarios in GedcomWriter in src/main/java/org/gedcom7/writer/GedcomWriter.java
- [X] T090 [US7] Add version-aware warning for FAMS/FAMC in IndividualContext — warn in GEDCOM 7 mode, no warning in 5.5.5 mode per FR-025 in src/main/java/org/gedcom7/writer/context/IndividualContext.java
- [X] T091 [US7] Implement default LoggingWarningHandler (logs to java.util.logging) and wire it as default in GedcomWriterConfig per FR-024 in src/main/java/org/gedcom7/writer/internal/ or inline in GedcomWriterConfig
- [X] T092 REVIEW: Agent review of T088-T091 — verify warn() pattern matches research.md Decision 6, strict throws GedcomWriteException (FR-023), lenient delivers to handler (FR-023), null handler suppresses (FR-024), FAMS/FAMC warning is version-aware (FR-025), default logs to j.u.l (FR-024)
- [X] T093 [US7] Run all US7 tests (StrictModeTest, WarningHandlerTest) plus full regression — ensure all pass
- [X] T094 REVIEW: Agent review of US7 implementation — verify against spec.md US7 acceptance scenarios 1-5, FR-023, FR-024, FR-025, SC-008, and no regressions

**Checkpoint**: Strict/lenient mode works — strict catches mistakes, lenient warns, null suppresses.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Additional typed contexts, quickstart validation, edge cases, and final review

- [X] T095 [P] Implement SourceContext with title, author, publicationFacts, abbreviation, repositoryCitation, text methods in src/main/java/org/gedcom7/writer/context/SourceContext.java per contracts/context-api.md
- [X] T096 [P] Implement RepositoryContext with name, address methods in src/main/java/org/gedcom7/writer/context/RepositoryContext.java per contracts/context-api.md
- [X] T097 [P] Implement MultimediaContext with file, file+body methods in src/main/java/org/gedcom7/writer/context/MultimediaContext.java per contracts/context-api.md
- [X] T098 [P] Implement SubmitterContext with name, address methods in src/main/java/org/gedcom7/writer/context/SubmitterContext.java per contracts/context-api.md
- [X] T099 [P] Implement NoteContext (value set at record creation, children via escape hatches) in src/main/java/org/gedcom7/writer/context/NoteContext.java per contracts/context-api.md
- [X] T100 [P] Implement AddressContext with line1/2/3, city, state, postalCode, country methods in src/main/java/org/gedcom7/writer/context/AddressContext.java per contracts/context-api.md
- [X] T101 REVIEW: Agent review of T095-T100 — verify all additional context classes match contracts/context-api.md signatures, extend CommonContext, delegate to escape hatches (FR-004), and handle null/empty values (FR-014)
- [X] T102 Write context integration tests for SourceContext, RepositoryContext, MultimediaContext, SubmitterContext, NoteContext, AddressContext in src/test/java/org/gedcom7/writer/context/ (one or more test files)
- [X] T103 REVIEW: Agent review of T102 — verify tests cover all additional context methods, output matches expected GEDCOM format
- [X] T104 Add edge case tests: null values omitted (FR-014), empty strings treated as no value (FR-014), lambda exception propagation (edge case from spec.md), deeply nested structures, all line ending variants in src/test/java/org/gedcom7/writer/GedcomWriterTest.java or new EdgeCaseTest.java
- [X] T105 REVIEW: Agent review of T104 — verify edge case tests cover all edge cases listed in spec.md Edge Cases section
- [X] T106 Validate quickstart.md examples — implement the complete family tree example from quickstart.md Section 10 as a test in src/test/java/org/gedcom7/writer/QuickstartExampleTest.java and verify it compiles and produces valid output
- [X] T107 REVIEW: Agent review of T106 — verify the quickstart example test matches quickstart.md code exactly, output is valid GEDCOM 7, and demonstrates SC-001 (developer with no GEDCOM knowledge can write valid file)
- [X] T108 Add Javadoc to all public API types and methods in org.gedcom7.writer, org.gedcom7.writer.context, and org.gedcom7.writer.date packages — every public class, interface, enum, and public method must have clear Javadoc per constitution Principle IV
- [X] T109 REVIEW: Agent review of T108 — verify Javadoc is present on ALL public types and methods, descriptions are clear and accurate, @param/@return/@throws tags are used correctly, no implementation details leak into public Javadoc
- [X] T110 Run full test suite (./gradlew clean test) — all existing parser tests and all new writer tests must pass
- [X] T111 REVIEW: Agent review of T110 — verify no regressions in existing parser tests, all writer tests pass, test count is reasonable for coverage

---

## Phase 11: Final Comprehensive Evaluation

**Purpose**: Independent agent performs a holistic review of the entire writer implementation against the full specification and constitution

- [X] T112 FINAL EVALUATION: Independent agent performs comprehensive review of the entire GEDCOM Writer implementation. The agent must:
  1. Read the complete spec (specs/004-gedcom-writer/spec.md) and verify every FR (FR-001 through FR-025) is implemented
  2. Read all contracts (specs/004-gedcom-writer/contracts/) and verify every method signature is present in the implementation
  3. Read the constitution (.specify/memory/constitution.md) and verify all 7 principles are satisfied:
     - Principle I: GEDCOM 7 Compliance — output conforms to spec
     - Principle II: SAX-like API — streaming push model
     - Principle III: Mechanical Sympathy — no unnecessary allocations in hot paths
     - Principle IV: Java Best Practices — immutability, naming, Javadoc, try-with-resources
     - Principle V: TDD — all behavior has tests
     - Principle VI: Simplicity/YAGNI — no scope creep beyond spec
     - Principle VII: Zero Dependencies — only java.* imports at runtime
  4. Verify all 8 Success Criteria (SC-001 through SC-008) from spec.md are met
  5. Verify all edge cases from spec.md Edge Cases section are handled
  6. Verify data-model.md entity relationships are correctly implemented
  7. Verify quickstart.md tutorial code works as documented
  8. Report: list of any gaps, violations, or deviations found with file paths and line numbers

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Foundational — this is the MVP
- **US2 (Phase 4)**: Depends on Foundational — can run in parallel with US1 but CONT/escape integration needs LineEmitter from Phase 2
- **US3 (Phase 5)**: Depends on US1 (tests use IndividualContext escape hatches)
- **US4 (Phase 6)**: Depends on US2 (builds on CONT/AtEscaper infrastructure)
- **US5 (Phase 7)**: Depends on US1 (extends record methods with ID overloads)
- **US6 (Phase 8)**: Depends on US1 (integrates WriterDate into EventContext)
- **US7 (Phase 9)**: Depends on US1 (adds strict/lenient behavior to existing writer)
- **Polish (Phase 10)**: Depends on US1-US7 completion
- **Final Evaluation (Phase 11)**: Depends on everything

### User Story Dependencies

- **US1 (P1)**: After Foundational — no other story dependencies (MVP)
- **US2 (P2)**: After Foundational — independent of US1 (operates on LineEmitter)
- **US3 (P3)**: After US1 — needs typed contexts to test escape hatches on
- **US4 (P4)**: After US2 — extends CONT/escape infrastructure with CONC/all-@@
- **US5 (P5)**: After US1 — extends record methods with developer-provided ID overloads
- **US6 (P6)**: After US1 — integrates WriterDate into EventContext.date()
- **US7 (P7)**: After US1 — adds warning/strict behavior across writer and contexts

### Within Each User Story

1. Tests written FIRST and verified to FAIL
2. Implementation tasks in dependency order
3. Review after each implementation group
4. Integration tests pass before moving on
5. Story-level review as final gate

### Parallel Opportunities

**Phase 2 parallel tasks**: T004, T005, T006, T007 (independent value types)
**Phase 2 parallel tasks**: T012, T013 (independent internal classes after T008 review)
**US1 parallel tests**: T018, T019, T020
**US1 parallel contexts**: T022+T023, T025+T026+T027
**US2 parallel**: T036+T037 (tests), T039+T040 (implementation)
**US4 parallel**: T053+T054 (tests)
**US6 parallel**: T070+T071+T072 (tests), T074+T075+T076 (enums)
**US7 parallel**: T085+T086 (tests)
**Phase 10 parallel**: T095+T096+T097+T098+T099+T100 (all additional contexts)

After Foundational, **US1 and US2 can run in parallel**. Once US1 completes, **US3, US5, US6, and US7 can run in parallel** (they touch different files/concerns). US4 must wait for US2.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Run round-trip test, verify basic GEDCOM 7 file creation works
5. This alone delivers the core value proposition (SC-001)

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. US1 → Basic GEDCOM 7 writing (MVP!)
3. US2 → Multi-line values and @@ escaping just work
4. US3 → Extension tags and custom structures
5. US4 → GEDCOM 5.5.5 support
6. US5 → Database export pattern
7. US6 → Type-safe dates
8. US7 → Strict/lenient mode
9. Polish → Additional contexts, edge cases, quickstart validation
10. Final Evaluation → Comprehensive verification against spec and constitution

### Parallel Team Strategy

With multiple developers after Foundational:
- Developer A: US1 (MVP) → US3 (escape hatches)
- Developer B: US2 (payload handling) → US4 (5.5.5 mode)
- Developer C: US6 (dates) — can start after US1 EventContext exists
- Developer D: US7 (warnings) — can start after US1 GedcomWriter exists
- All: Review tasks assigned to developers not on the implementation task

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- REVIEW tasks are performed by an independent agent (not the implementing agent)
- Each review validates against spec.md, contracts/, constitution.md, and relevant FRs
- Final Evaluation (T110) is a comprehensive holistic review by an independent agent
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Total tasks: 112 (including ~36 review tasks + 1 final evaluation)
