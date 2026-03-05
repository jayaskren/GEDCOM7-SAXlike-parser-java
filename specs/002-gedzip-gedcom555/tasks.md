# Tasks: GEDZip Support and GEDCOM 5.5.5 Compatibility

**Input**: Design documents from `/specs/002-gedzip-gedcom555/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/public-api.md

**Tests**: Included for all tasks per TDD constitution principle (V. Test-Driven Development).

**Organization**: Tasks are grouped by user story. Each task has a verification counterpart. A final evaluation task validates the complete implementation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- **[V]**: Verification task — validates preceding task(s) against requirements and constitution
- Include exact file paths in descriptions

## Agent Parallelization Guide

Tasks are designed so that independent agents can work on separate file sets simultaneously. The key constraint is: **no two agents should modify the same file at the same time**.

**Parallel agent groups** (within each phase, [P] tasks can be assigned to separate agents):

| Agent | Files Owned | Phase |
|-------|------------|-------|
| Agent A | `BomDetectingDecoder.java`, `BomDetectingDecoderTest.java` | 3 |
| Agent B | `ContConcAssembler.java`, `ContConcAssemblerTest.java` | 3 |
| Agent C | `AllAtEscapeStrategy.java`, `AllAtEscapeStrategyTest.java` | 3 |
| Agent D | `GedzipReader.java`, `GedzipReaderTest.java`, `src/test/resources/gedzip/` | 5 |
| Agent E | `PayloadAssembler.java`, `ContOnlyAssembler.java`, `ContOnlyAssemblerTest.java` | 2 |
| Agent F | `GedcomReaderConfig.java`, related config tests | 2 |
| Agent G | `GedcomHeaderInfo.java`, `GedcomHeaderInfoTest.java` | 2 |
| Agent H | `GedcomReader.java`, integration tests | 4, 6 |
| Agent I | `src/test/resources/gedcom555/` test resource files | 1 |

---

## Phase 1: Setup (Test Resources)

**Purpose**: Create all test resource files needed by subsequent phases. No source code changes.

- [X] T001 [P] Create GEDCOM 5.5.5 UTF-8 test file in `src/test/resources/gedcom555/basic-555.ged` — must include HEAD with GEDC.VERS 5.5.5, HEAD.CHAR UTF-8, BOM, FORM LINEAGE-LINKED, and at least 2 INDI records with NAME, BIRT, and nested structures
- [X] T002 [P] Create GEDCOM 5.5.5 UTF-16 LE test file in `src/test/resources/gedcom555/utf16-le.ged` — UTF-16 LE BOM (FF FE), HEAD.CHAR UNICODE, at least 1 INDI record with NAME containing non-ASCII characters
- [X] T003 [P] Create GEDCOM 5.5.5 UTF-16 BE test file in `src/test/resources/gedcom555/utf16-be.ged` — UTF-16 BE BOM (FE FF), HEAD.CHAR UNICODE, at least 1 INDI record with NAME
- [X] T004 [P] Create GEDCOM 5.5.5 CONC test file in `src/test/resources/gedcom555/conc-values.ged` — long NOTE split across multiple CONC lines, verify concatenation produces expected text
- [X] T005 [P] Create GEDCOM 5.5.5 mixed CONT+CONC test file in `src/test/resources/gedcom555/mixed-cont-conc.ged` — NOTE with both CONT (newline join) and CONC (direct concat) substructures
- [X] T006 [P] Create GEDCOM 5.5.5 @@ escape test file in `src/test/resources/gedcom555/at-escape-all.ged` — values containing `@@` at leading, middle, and trailing positions
- [X] T007 [P] Create GEDCOM 5.5.5 long lines test file in `src/test/resources/gedcom555/long-lines.ged` — at least one line exceeding 255 characters for validation testing
- [X] T008 [P] Create GEDCOM 5.5.5 no HEAD.CHAR test file in `src/test/resources/gedcom555/no-char-tag.ged` — valid 5.5.5 file but with HEAD.CHAR tag omitted entirely
- [X] T009 [P] Create GEDZip test archive `src/test/resources/gedzip/basic.gdz` — ZIP containing only `gedcom.ged` (a valid GEDCOM 7 file with HEAD, 1 INDI, TRLR)
- [X] T010 [P] Create GEDZip test archive with media `src/test/resources/gedzip/with-media.gdz` — ZIP containing `gedcom.ged` (with FILE reference to `photos/test.jpg`), plus `photos/test.jpg` (small test image or text placeholder)
- [X] T011 [P] Create invalid GEDZip test archive `src/test/resources/gedzip/no-gedcom.zip` — ZIP containing some files but NOT `gedcom.ged`
- [X] T012 [P] Create GEDZip percent-encoded test archive `src/test/resources/gedzip/percent-encoded.gdz` — ZIP containing `gedcom.ged` (with FILE reference to `photo album/family pic.jpg` using percent-encoding `photo%20album/family%20pic.jpg`), plus the actual file `photo album/family pic.jpg`
- [X] T013 [V] Verify test resources: confirm all 12 test resource files exist, are valid (GED files parseable by existing parser where applicable, ZIP files openable), and match the spec requirements (FR-001 through FR-025). Check constitution principle V (tests run without network access).

---

## Phase 2: Foundational (Interface Changes)

**Purpose**: Modify shared interfaces and classes that all user stories depend on. Must complete before user story phases.

**These tasks modify DIFFERENT files and can run in parallel.**

- [X] T014 [P] Modify `PayloadAssembler.java` interface in `src/main/java/org/gedcom7/parser/internal/PayloadAssembler.java` — add `String tag` as third parameter to `assemblePayload()` method. Update Javadoc. Per research.md Decision 1.
- [X] T015 [P] Update `ContOnlyAssembler.java` in `src/main/java/org/gedcom7/parser/internal/ContOnlyAssembler.java` — update `assemblePayload()` signature to accept the new `tag` parameter (ignore it; behavior unchanged). Update `ContOnlyAssemblerTest.java` to pass tag parameter.
- [X] T016 [P] Add `characterEncoding` field to `GedcomHeaderInfo.java` in `src/main/java/org/gedcom7/parser/GedcomHeaderInfo.java` — add private final `String characterEncoding` field, add to constructor (as 7th parameter), add `getCharacterEncoding()` getter, add backward-compatible 6-param constructor that passes null for characterEncoding. Update `GedcomHeaderInfoTest.java`.
- [X] T017 [P] Add `autoDetect` field and new factory methods to `GedcomReaderConfig.java` in `src/main/java/org/gedcom7/parser/GedcomReaderConfig.java` — add `boolean autoDetect` field (default false), `isAutoDetect()` getter, `Builder.autoDetect(boolean)` method, and four new factory methods: `gedcom555()`, `gedcom555Strict()`, `autoDetect()`, `autoDetectStrict()`. Factory methods wire appropriate strategies per data-model.md. Strategies are instantiated inline in factory methods (they are internal classes). Update `toBuilder()` to preserve autoDetect. Add tests for new factory methods and builder in a new test file `src/test/java/org/gedcom7/parser/GedcomReaderConfigFactoryTest.java`.
- [X] T018 [V] Verify foundational changes: run `./gradlew test` — all existing 301 tests must still pass (FR-026, FR-027). Verify PayloadAssembler interface change is backward-compatible. Verify GedcomHeaderInfo 6-param constructor still works. Verify GedcomReaderConfig.gedcom7() and gedcom7Strict() unchanged. Check constitution principles IV (Java best practices: final fields, immutability) and VII (zero dependencies).

**Checkpoint**: Foundational interfaces modified — user story implementation can now begin.

---

## Phase 3: User Story 1 — Parse GEDCOM 5.5.5 Files (Priority: P1) MVP

**Goal**: Parse GEDCOM 5.5.5 files using new strategy implementations (BomDetectingDecoder, ContConcAssembler, AllAtEscapeStrategy) wired through the `gedcom555()` config factory.

**Independent Test**: Parse `src/test/resources/gedcom555/basic-555.ged` using `GedcomReaderConfig.gedcom555()` and verify handler callbacks fire correctly.

**These tasks create NEW files with no conflicts — all can run in parallel.**

### Tests for User Story 1

- [X] T019 [P] [US1] Write unit tests for `BomDetectingDecoder` in `src/test/java/org/gedcom7/parser/internal/BomDetectingDecoderTest.java` — test UTF-8 BOM detection and stripping, UTF-16 BE BOM detection, UTF-16 LE BOM detection, no BOM defaults to UTF-8, empty stream handling. Per research.md Decision 2 BOM table.
- [X] T020 [P] [US1] Write unit tests for `ContConcAssembler` in `src/test/java/org/gedcom7/parser/internal/ContConcAssemblerTest.java` — test isPseudoStructure returns true for CONT and CONC (not for other tags), CONT joins with newline, CONC joins without separator, null handling, empty value handling. Per data-model.md ContConcAssembler table.
- [X] T021 [P] [US1] Write unit tests for `AllAtEscapeStrategy` in `src/test/java/org/gedcom7/parser/internal/AllAtEscapeStrategyTest.java` — test all @@ replaced with @, leading @@ replaced, middle @@ replaced, trailing @@ replaced, no @@ unchanged, null returns null. Per data-model.md AllAtEscapeStrategy table.

### Implementation for User Story 1

- [X] T022 [P] [US1] Implement `BomDetectingDecoder` in `src/main/java/org/gedcom7/parser/internal/BomDetectingDecoder.java` — implements `GedcomInputDecoder`. Uses `PushbackInputStream(input, 3)` to peek at first 3 bytes. Detects UTF-16 BE (FE FF), UTF-16 LE (FF FE), UTF-8 (EF BB BF) BOMs. If no BOM, push back and default to UTF-8. Returns `InputStreamReader` with detected charset. Add `getDetectedCharset()` and `isBomFound()` accessors. Per research.md Decision 2.
- [X] T023 [P] [US1] Implement `ContConcAssembler` in `src/main/java/org/gedcom7/parser/internal/ContConcAssembler.java` — implements `PayloadAssembler`. `isPseudoStructure()` returns true for CONT and CONC. `assemblePayload(existing, value, tag)` joins with newline for CONT, direct concatenation for CONC. Per research.md Decision 1.
- [X] T024 [P] [US1] Implement `AllAtEscapeStrategy` in `src/main/java/org/gedcom7/parser/internal/AllAtEscapeStrategy.java` — implements `AtEscapeStrategy`. `unescape(value)` returns `value.replace("@@", "@")` for non-null values, null for null. Per data-model.md.
- [X] T025 [P] [US1] Write integration tests for GEDCOM 5.5.5 parsing in `src/test/java/org/gedcom7/parser/Gedcom555ParsingTest.java` — test parsing `basic-555.ged` with `GedcomReaderConfig.gedcom555()`, verify handler receives startDocument with version 5.5.5, verify INDI records parsed correctly, verify CONC assembly from `conc-values.ged`, verify mixed CONT+CONC from `mixed-cont-conc.ged`, verify all @@ decoded from `at-escape-all.ged`, verify UTF-16 LE from `utf16-le.ged`, verify UTF-16 BE from `utf16-be.ged`, verify missing HEAD.CHAR from `no-char-tag.ged` produces warning and assumes UTF-8. Maps to spec acceptance scenarios US1.1-US1.5.
- [X] T026 [V] [US1] Verify US1 against requirements and constitution: confirm FR-001 through FR-010 are satisfied. Run `BomDetectingDecoderTest`, `ContConcAssemblerTest`, `AllAtEscapeStrategyTest`, and `Gedcom555ParsingTest`. Verify constitution principle I (GEDCOM 7 behavior preserved — run existing Utf8InputDecoderTest, ContOnlyAssemblerTest, LeadingAtEscapeStrategyTest unchanged). Verify principle III (no unnecessary allocations in strategy implementations). Verify principle VII (no new dependencies added).

**Checkpoint**: GEDCOM 5.5.5 files can be parsed using explicit `gedcom555()` config.

---

## Phase 4: User Story 2 — Auto-Detect GEDCOM Version (Priority: P1)

**Goal**: Automatically detect GEDCOM version from HEAD.GEDC.VERS and apply correct parsing strategies.

**Independent Test**: Parse a GEDCOM 7 file and a GEDCOM 5.5.5 file both using `GedcomReaderConfig.autoDetect()` and verify correct behavior for each.

**Depends on**: Phase 2 (config autoDetect flag) and Phase 3 (5.5.5 strategy implementations).

### Implementation for User Story 2

- [X] T027 [US2] Modify `GedcomReader.java` in `src/main/java/org/gedcom7/parser/GedcomReader.java` — (1) Update `assemblePayload()` call site in `processLine()` to pass the tag as third argument. (2) Make `assembler` and `atEscape` fields non-final. (3) In `doParse()`, after HEAD pre-scan extracts version, add auto-detect logic: if `config.isAutoDetect()` and `version.isGedcom5()`, swap `this.assembler = new ContConcAssembler()` and `this.atEscape = new AllAtEscapeStrategy()`. (4) Extract HEAD.CHAR value during HEAD pre-scan and pass to GedcomHeaderInfo constructor as `characterEncoding` parameter. (5) If HEAD.CHAR is missing in 5.5.5 mode, issue a warning. Per research.md Decision 3 and plan.md Architecture section.
- [X] T028 [P] [US2] Write auto-detect tests in `src/test/java/org/gedcom7/parser/VersionAutoDetectTest.java` — test: (1) GEDCOM 7 file with autoDetect() uses GEDCOM 7 rules (CONT only, leading @@ only). (2) GEDCOM 5.5.5 file with autoDetect() uses 5.5.5 rules (CONT+CONC, all @@). (3) GEDCOM 5.5.1 file with autoDetect() issues warning and uses 5.5.5 rules as fallback. (4) autoDetect() with GEDCOM 7 file produces headerInfo with null characterEncoding. (5) autoDetect() with GEDCOM 5.5.5 file produces headerInfo with characterEncoding="UTF-8". Maps to spec acceptance scenarios US2.1-US2.3.
- [X] T029 [V] [US2] Verify US2 against requirements and constitution: confirm FR-011 through FR-013 are satisfied. Run `VersionAutoDetectTest`. Verify existing GEDCOM 7 tests still pass (`./gradlew test`). Verify GedcomReader strategy swap only occurs in auto-detect mode (fields remain effectively final in non-auto-detect mode per constitution principle III). Verify principle II (streaming API not broken — no buffering or re-reading).

**Checkpoint**: Auto-detection works for both GEDCOM 7 and 5.5.5 files.

---

## Phase 5: User Story 3 — Open GEDZip Archives (Priority: P2)

**Goal**: Read GEDZip (.gdz) archives, parse the contained GEDCOM data, and access media files.

**Independent Test**: Open `src/test/resources/gedzip/basic.gdz`, parse the GEDCOM data, and verify handler events fire.

**Depends on**: Phase 2 (foundational). Can run in parallel with Phase 4 if assigned to a separate agent.

### Implementation for User Story 3

- [X] T030 [P] [US3] Implement `GedzipReader` in `src/main/java/org/gedcom7/parser/GedzipReader.java` — public final class implementing AutoCloseable. Constructors: `GedzipReader(Path)` and `GedzipReader(File)`. Opens ZipFile, validates `gedcom.ged` entry exists (throw IOException if not). Methods: `getGedcomStream()` returns InputStream for gedcom.ged entry, `getEntry(String path)` percent-decodes path via `URLDecoder.decode(path, StandardCharsets.UTF_8)` then looks up ZipEntry (returns null if not found), `hasEntry(String path)` returns boolean, `getEntryNames()` returns unmodifiable Set<String>, `close()` closes ZipFile. Add Javadoc per contracts/public-api.md. Per research.md Decisions 4 and 7.
- [X] T031 [P] [US3] Write GEDZip tests in `src/test/java/org/gedcom7/parser/GedzipReaderTest.java` — test: (1) Open `basic.gdz`, get gedcom stream, parse with GedcomReader and autoDetect(), verify handler events. (2) Open `with-media.gdz`, verify `getEntry("photos/test.jpg")` returns non-null InputStream. (3) Verify `getEntry("nonexistent.jpg")` returns null. (4) Verify `hasEntry("photos/test.jpg")` returns true. (5) Open `no-gedcom.zip`, verify IOException thrown with clear message about missing gedcom.ged. (6) Open `percent-encoded.gdz`, verify `getEntry("photo%20album/family%20pic.jpg")` returns non-null. (7) Verify `getEntryNames()` returns unmodifiable set. (8) Verify close() works via try-with-resources. Maps to spec acceptance scenarios US3.1-US3.4.
- [X] T032 [V] [US3] Verify US3 against requirements and constitution: confirm FR-019 through FR-025 are satisfied. Run `GedzipReaderTest`. Verify FR-019 auto-detection inside archive works (parse basic.gdz with autoDetect). Verify FR-020 clear error message. Verify FR-021 null return for missing entry. Verify FR-025 percent-encoding. Verify constitution principle VI (GedzipReader is narrowly scoped — single responsibility). Verify principle VII (uses java.util.zip only, no new dependencies). Verify principle IV (final class, AutoCloseable, Javadoc).

**Checkpoint**: GEDZip archives can be opened, parsed, and media accessed.

---

## Phase 6: User Story 4 — Unified Handler API (Priority: P1)

**Goal**: Verify that a single GedcomHandler implementation works for both GEDCOM 5.5.5 and GEDCOM 7.0 files, receiving equivalent events.

**Independent Test**: Implement one handler, parse both versions of equivalent data, compare event sequences.

**Depends on**: Phase 3 (5.5.5 parsing) and Phase 4 (auto-detect).

### Implementation for User Story 4

- [X] T033 [US4] Write unified handler tests in `src/test/java/org/gedcom7/parser/UnifiedHandlerTest.java` — test: (1) Implement a single handler that records all startRecord and startStructure events into a list. Parse `basic-555.ged` (5.5.5) and `minimal.ged` (GEDCOM 7) with equivalent data and compare event sequences (should be equivalent). (2) Parse a 5.5.5 file and verify GedcomHeaderInfo.getVersion() returns 5/5/5. (3) Parse a 5.5.5 file and verify GedcomHeaderInfo.getCharacterEncoding() returns encoding info. (4) Verify the same handler instance can be reused for both versions without modification. Maps to spec acceptance scenarios US4.1-US4.3.
- [X] T034 [V] [US4] Verify US4 against requirements and constitution: confirm FR-009 (same callbacks), FR-010 (headerInfo includes 5.5.5 version and encoding). Run `UnifiedHandlerTest`. Verify constitution principle II (SAX-like API unified). Verify existing handler tests (`GedcomReaderTest`, `AllRecordTypesTest`) still pass unchanged — confirms FR-026 backward compatibility.

**Checkpoint**: Unified handler API verified across both GEDCOM versions.

---

## Phase 7: User Story 5 — GEDCOM 5.5.5 Validation (Priority: P3)

**Goal**: Detect and report 5.5.5-specific violations (line length, xref length, BOM, bare @).

**Independent Test**: Parse intentionally malformed 5.5.5 files and verify correct warnings/errors.

**Depends on**: Phase 3 (5.5.5 parsing) and Phase 4 (GedcomReader changes).

### Implementation for User Story 5

- [X] T035 [US5] Add GEDCOM 5.5.5 validation checks to `GedcomReader.java` in `src/main/java/org/gedcom7/parser/GedcomReader.java` — in 5.5.5 mode (detected via version or config): (1) After HEAD scan, if BomDetectingDecoder's `isBomFound()` is false and version is 5.5.x, report warning (or fatal in strict mode per FR-016). (2) In `processLine()` or `flushPending()`, check xref length > 22 chars (including @ signs) → warning (FR-015). (3) Check for bare single `@` in non-pointer values (not `@@`) → error (FR-017). The max line length check (FR-014) is already handled by GedcomLineTokenizer via config maxLineLength — the `gedcom555Strict()` factory sets this to 255. Add a `gedcom555Mode` boolean field set during HEAD scan or from config to gate these checks.
- [X] T036 [P] [US5] Write 5.5.5 validation tests in `src/test/java/org/gedcom7/parser/Gedcom555ValidationTest.java` — test: (1) Parse `long-lines.ged` with `gedcom555Strict()` → fatal error for line > 255 chars. (2) Parse file with xref > 22 chars → warning. (3) Parse `no-char-tag.ged` without BOM with `gedcom555Strict()` → fatal error for missing BOM. (4) Parse file with bare `@` in value → error. (5) Parse `long-lines.ged` with lenient `gedcom555()` → warning (not fatal). (6) Verify level jump validation still works in 5.5.5 mode (FR-018). Maps to spec acceptance scenarios US5.1-US5.4.
- [X] T037 [V] [US5] Verify US5 against requirements and constitution: confirm FR-014 through FR-018 are satisfied. Run `Gedcom555ValidationTest`. Verify validation mapping matches research.md Decision 5 table (lenient vs strict behavior). Verify constitution principle I (GEDCOM 7 validation unchanged). Run full test suite `./gradlew test` to confirm no regressions.

**Checkpoint**: GEDCOM 5.5.5 validation working in both lenient and strict modes.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final integration, documentation, and comprehensive verification.

- [X] T038 [P] Update `module-info.java` in `src/main/java/module-info.java` — verify GedzipReader is accessible from the exported `org.gedcom7.parser` package (it should be, since it's in that package). Verify no internal classes are accidentally exported.
- [X] T039 [P] Update `PublicApiSurfaceTest.java` in `src/test/java/org/gedcom7/parser/PublicApiSurfaceTest.java` — add assertions for new public API surface: GedzipReader class and its public methods, new GedcomReaderConfig factory methods (gedcom555, gedcom555Strict, autoDetect, autoDetectStrict), GedcomHeaderInfo.getCharacterEncoding().
- [X] T040 Run quickstart.md validation — create `src/test/java/org/gedcom7/parser/Gedcom555QuickstartTest.java` implementing all 5 scenarios from `specs/002-gedzip-gedcom555/quickstart.md`: (1) Parse 5.5.5 file, (2) Auto-detect, (3) Open GEDZip, (4) Strict validation, (5) Unified handler. Each scenario must compile and run successfully.
- [X] T041 [V] Verify polish tasks: run `./gradlew test` — confirm all tests pass. Verify module-info exports are correct. Verify PublicApiSurfaceTest covers all new public API. Verify quickstart scenarios all pass.

---

## Phase 9: Final Evaluation

**Purpose**: Comprehensive evaluation of ALL implemented tasks against the complete specification, requirements, and constitution.

- [X] T042 **FINAL EVALUATION**: Run comprehensive validation of the entire implementation:
  1. **Full test suite**: Run `./gradlew clean test` — all tests must pass with zero failures
  2. **Test count**: Verify test count increased from baseline of 301 tests
  3. **Requirement coverage**: Verify each of the 27 functional requirements (FR-001 through FR-027) is satisfied:
     - FR-001 through FR-010: GEDCOM 5.5.5 parsing (strategies, CONC, CONT, @@, UTF-8, UTF-16, BOM, HEAD.CHAR, callbacks, headerInfo)
     - FR-011 through FR-013: Auto-detection (HEAD.GEDC.VERS, unsupported version warning, factory methods)
     - FR-014 through FR-018: 5.5.5 validation (line length, xref length, BOM requirement, bare @, level jumps)
     - FR-019 through FR-025: GEDZip (open archive, missing gedcom.ged error, entry access, local vs external FILE, UTF-8 filenames, ISO/IEC 21320-1, percent-encoding)
     - FR-026, FR-027: Backward compatibility (GEDCOM 7 unchanged, existing factory methods identical)
  4. **Constitution compliance**: Verify all 7 constitution principles:
     - I. GEDCOM 7 Compliance: Run all existing GEDCOM 7 tests unchanged
     - II. SAX-like API: Verify streaming, no DOM, unified handler
     - III. Mechanical Sympathy: Review new strategy implementations for unnecessary allocations
     - IV. Java Best Practices: final classes, immutable configs, Javadoc on public API, try-with-resources
     - V. TDD: Every new class has corresponding test class
     - VI. Simplicity/YAGNI: No unnecessary abstractions beyond what spec requires
     - VII. Zero Dependencies: `./gradlew dependencies --configuration runtimeClasspath` shows no external deps
  5. **Success criteria**: Verify all 6 measurable outcomes (SC-001 through SC-006)
  6. **Clarification compliance**: Verify all 3 clarification answers are implemented:
     - GEDZip auto-detects version inside archive
     - Missing HEAD.CHAR warns and assumes UTF-8
     - Missing GEDZip entry returns null
  7. **Build verification**: `./gradlew clean build` succeeds with no warnings

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup ─────────────────────────── (no dependencies)
    │
    v
Phase 2: Foundational ──────────────────── (depends on Phase 1)
    │
    ├──> Phase 3: US1 Parse 5.5.5 ──────── (depends on Phase 2)
    │        │
    │        ├──> Phase 4: US2 Auto-Detect  (depends on Phase 2 + Phase 3)
    │        │        │
    │        │        ├──> Phase 6: US4 Unified API (depends on Phase 3 + Phase 4)
    │        │        │
    │        │        └──> Phase 7: US5 Validation  (depends on Phase 3 + Phase 4)
    │        │
    │        └──────────────────────────────────────────────────────────────────────
    │
    └──> Phase 5: US3 GEDZip ───────────── (depends on Phase 2 ONLY — can run parallel with Phase 3/4)

Phase 8: Polish ─────────────────────────── (depends on all story phases)
    │
    v
Phase 9: Final Evaluation ──────────────── (depends on Phase 8)
```

### Parallel Opportunities

**Maximum parallelism** (6 agents simultaneously after Phase 2):

```
After Phase 2 completes:

  Agent A: T019 + T022 (BomDetectingDecoder tests + impl)     ─┐
  Agent B: T020 + T023 (ContConcAssembler tests + impl)        ├── Phase 3 (all parallel)
  Agent C: T021 + T024 (AllAtEscapeStrategy tests + impl)      │
  Agent I: T025 (Integration tests — can start writing)        ─┘

  Agent D: T030 + T031 (GedzipReader impl + tests)             ── Phase 5 (parallel with Phase 3)

After Phase 3 completes:
  Agent H: T027 (GedcomReader integration)                      ── Phase 4

After Phase 4 completes:
  Agent H: T033 (Unified handler tests)                         ─┐
  Agent F: T035 + T036 (Validation impl + tests)                ─┘ Phase 6 + 7 (parallel)
```

### Within Each Phase

- All [P] tasks within a phase can run in parallel
- Verification [V] tasks run AFTER their corresponding implementation tasks
- Tests should be written before/alongside implementation (TDD)

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (test resources)
2. Complete Phase 2: Foundational (interface changes)
3. Complete Phase 3: User Story 1 (5.5.5 parsing strategies)
4. **STOP and VALIDATE**: Run T026 verification
5. At this point, developers can parse GEDCOM 5.5.5 files using `GedcomReaderConfig.gedcom555()`

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready
2. Phase 3 → GEDCOM 5.5.5 parsing works (MVP)
3. Phase 4 → Auto-detection works
4. Phase 5 → GEDZip archives work (can be done in parallel with 3+4)
5. Phase 6 → Unified API verified
6. Phase 7 → 5.5.5 validation works
7. Phase 8 + 9 → Polish and final evaluation

### Parallel Agent Strategy

With maximum agents:

1. All agents work on Phase 1 (test resources) — all [P]
2. 4 agents work on Phase 2 (one per modified file) — all [P]
3. After Phase 2: 4 agents on Phase 3 (strategies) + 1 agent on Phase 5 (GEDZip)
4. After Phase 3: 1 agent on Phase 4 (GedcomReader integration)
5. After Phase 4: 2 agents on Phase 6 + 7 (verification + validation)
6. 1 agent on Phase 8 + 9 (polish + final evaluation)

---

## Notes

- [P] tasks = different files, no dependencies — safe for parallel agents
- [V] tasks = verification against requirements (FR-xxx) and constitution (7 principles)
- [USx] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing (TDD per constitution V)
- Run `./gradlew test` after each phase to catch regressions
- Final T042 evaluation validates the ENTIRE implementation comprehensively
