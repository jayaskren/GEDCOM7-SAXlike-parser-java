# Tasks: GEDCOM 7 SAX-like Streaming Parser

**Input**: Design documents from `specs/001-gedcom7-sax-parser/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/public-api.java

**Tests**: Required per Constitution Principle V (TDD). Tests MUST be written before or alongside implementation and MUST fail before the corresponding implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, build system, package structure

- [x] T001 Initialize Gradle project with Kotlin DSL: create `settings.gradle.kts` (project name `gedcom7-parser`), `build.gradle.kts` (Java 11 target, JUnit 5 dependency), and Gradle wrapper in `gradle/wrapper/`
- [x] T002 Create directory structure: `src/main/java/org/gedcom7/parser/`, `src/main/java/org/gedcom7/parser/internal/`, `src/main/java/org/gedcom7/parser/datatype/`, `src/main/java/org/gedcom7/parser/validation/`, `src/test/java/org/gedcom7/parser/`, `src/test/resources/`, `src/main/data/gedcom7/`
- [x] T003 [P] Update `.gitignore` with Gradle entries: `build/`, `.gradle/`, `gradle/wrapper/gradle-wrapper.jar` (keep `gradle-wrapper.properties`)
- [x] T004 [P] Create `src/main/java/module-info.java` exporting `org.gedcom7.parser`, `org.gedcom7.parser.datatype`, `org.gedcom7.parser.validation`; not exporting `org.gedcom7.parser.internal`

**Checkpoint**: `gradle build` compiles successfully (empty project, tests pass trivially)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core API types and internal interfaces that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 [P] Implement `GedcomParseError` value class (immutable, Severity enum, lineNumber, byteOffset, message, rawLine) with equals/hashCode/toString in `src/main/java/org/gedcom7/parser/GedcomParseError.java`
- [x] T006 [P] Implement `GedcomVersion` value class (immutable, major/minor/patch, isGedcom7/isGedcom5, equals/hashCode/toString) in `src/main/java/org/gedcom7/parser/GedcomVersion.java`
- [x] T007 [P] Implement `GedcomFatalException` (unchecked, wraps GedcomParseError) in `src/main/java/org/gedcom7/parser/GedcomFatalException.java`
- [x] T008 [P] Implement `GedcomHandler` abstract class with no-op default methods (startDocument, endDocument, startRecord, endRecord, startStructure, endStructure, warning, error, fatalError) in `src/main/java/org/gedcom7/parser/GedcomHandler.java`
- [x] T009 [P] Implement `GedcomHeaderInfo` value class (immutable, GedcomVersion, sourceSystem, sourceVersion, sourceName, defaultLanguage, schemaMap) in `src/main/java/org/gedcom7/parser/GedcomHeaderInfo.java`
- [x] T010 [P] Implement `GedcomReaderConfig` with Builder pattern (strict, maxNestingDepth, maxLineLength, structureValidation, package-private strategy fields) and `gedcom7()`/`gedcom7Strict()` factory methods in `src/main/java/org/gedcom7/parser/GedcomReaderConfig.java`
- [x] T011 [P] Implement `GedcomLine` mutable internal token (level, xref, tag, value, isPointer, lineNumber, byteOffset) in `src/main/java/org/gedcom7/parser/internal/GedcomLine.java`
- [x] T012 [P] Define `GedcomInputDecoder` internal interface (decode method) in `src/main/java/org/gedcom7/parser/internal/GedcomInputDecoder.java`
- [x] T013 [P] Define `PayloadAssembler` internal interface (isPseudoStructure, assemblePayload) in `src/main/java/org/gedcom7/parser/internal/PayloadAssembler.java`
- [x] T014 [P] Define `AtEscapeStrategy` internal interface (unescape method) in `src/main/java/org/gedcom7/parser/internal/AtEscapeStrategy.java`
- [x] T015 Write unit tests for all foundational value classes (GedcomParseError, GedcomVersion, GedcomHeaderInfo, GedcomReaderConfig builder/factory, GedcomFatalException) in `src/test/java/org/gedcom7/parser/ValueClassesTest.java`

**Checkpoint**: `gradle test` passes. All public API value types compile and have tests. Internal interfaces exist.

---

## Phase 3: User Story 1 - Parse a GEDCOM 7 File Line-by-Line (Priority: P1) MVP

**Goal**: Feed a GEDCOM 7 file to the parser and receive SAX-like callback events for each structural element, streaming without loading the entire file into memory.

**Independent Test**: Parse a minimal valid GEDCOM 7 file (`HEAD` + `GEDC` + `VERS 7.0` + `TRLR`) and verify the correct sequence of events fires.

### Tests for User Story 1

> **Write tests FIRST, ensure they FAIL before implementation**

- [x] T016 [P] [US1] Create minimal test GEDCOM file `src/test/resources/minimal.ged` containing HEAD, GEDC, VERS 7.0, TRLR with LF line endings
- [x] T017 [P] [US1] Create test GEDCOM file `src/test/resources/minimal-bom.ged` same as minimal.ged but with UTF-8 BOM prefix
- [x] T018 [P] [US1] Create test GEDCOM file `src/test/resources/minimal-crlf.ged` same as minimal.ged but with CRLF line endings
- [x] T019 [P] [US1] Create test GEDCOM file `src/test/resources/cont-multiline.ged` with a NOTE containing CONT continuation lines
- [x] T020 [US1] Write unit tests for `Utf8InputDecoder` (BOM detection+strip, no-BOM passthrough, multi-byte UTF-8 sequences) in `src/test/java/org/gedcom7/parser/internal/Utf8InputDecoderTest.java`
- [x] T021 [US1] Write unit tests for `LeadingAtEscapeStrategy` (leading @@ decoded, non-leading @@ untouched, no-@ passthrough) in `src/test/java/org/gedcom7/parser/internal/LeadingAtEscapeStrategyTest.java`
- [x] T022 [US1] Write unit tests for `ContOnlyAssembler` (CONT recognized, CONC treated as normal tag, empty CONT value, @@ in CONT) in `src/test/java/org/gedcom7/parser/internal/ContOnlyAssemblerTest.java`
- [x] T023 [US1] Write unit tests for `GedcomLineTokenizer` (parse level/xref/tag/value, all EOL forms CR/LF/CRLF, second space is part of value, line with no value, line with xref, leading whitespace tolerance with warning) in `src/test/java/org/gedcom7/parser/internal/GedcomLineTokenizerTest.java`
- [x] T024 [US1] Write integration test: parse minimal.ged and verify event sequence (startDocument, startRecord HEAD, substructure events, endRecord HEAD, endDocument) in `src/test/java/org/gedcom7/parser/GedcomReaderTest.java`

### Implementation for User Story 1

- [x] T025 [P] [US1] Implement `Utf8InputDecoder` (UTF-8 decoding, BOM detection and stripping, byte offset tracking) in `src/main/java/org/gedcom7/parser/internal/Utf8InputDecoder.java`
- [x] T026 [P] [US1] Implement `LeadingAtEscapeStrategy` (decode leading @@ only per GEDCOM 7 rules) in `src/main/java/org/gedcom7/parser/internal/LeadingAtEscapeStrategy.java`
- [x] T027 [P] [US1] Implement `ContOnlyAssembler` (recognize CONT, join with newline, apply @@ escape to CONT values, ignore CONC) in `src/main/java/org/gedcom7/parser/internal/ContOnlyAssembler.java`
- [x] T028 [US1] Implement `GedcomLineTokenizer` (read character stream line-by-line, parse level/xref/tag/value per ABNF grammar, handle all three EOL forms, track line number and byte offset, reuse GedcomLine instance, tolerate leading whitespace with warning callback) in `src/main/java/org/gedcom7/parser/internal/GedcomLineTokenizer.java`
- [x] T029 [US1] Implement `GedcomReader.parse()` skeleton: wire tokenizer -> payload assembler -> event emission for startDocument/endDocument, startRecord/endRecord, startStructure/endStructure (flat events, no nesting yet) in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T030 [US1] Verify all US1 tests pass: minimal file parsing, BOM handling, CRLF handling, CONT payload assembly, @@ escape decoding

**Checkpoint**: Parse minimal.ged and receive correct flat event sequence. CONT payloads are assembled. BOM is stripped. All three EOL forms work.

---

## Phase 4: User Story 2 - Receive Properly Nested Start/End Events (Priority: P1)

**Goal**: Parser emits properly nested start/end events based on GEDCOM level numbers so consumers can track hierarchical context without managing a level stack.

**Independent Test**: Parse a file with nested structures (INDI with BIRT containing DATE and PLAC) and verify end events fire in correct reverse order when the level decreases.

### Tests for User Story 2

- [x] T031 [P] [US2] Create test file `src/test/resources/nested-structures.ged` with INDI record containing BIRT->DATE+PLAC, DEAT, and level decrease scenarios
- [x] T032 [US2] Write unit tests for `LevelStack` (push, pop on level decrease, pop multiple on big decrease, error on >1 level increase) in `src/test/java/org/gedcom7/parser/internal/LevelStackTest.java`
- [x] T033 [US2] Write integration test: parse nested-structures.ged and verify exact event sequence including endStructure order matches spec acceptance scenario in `src/test/java/org/gedcom7/parser/NestingTest.java`

### Implementation for User Story 2

- [x] T034 [US2] Implement `LevelStack` (tracks current nesting, emits end events on level decrease, validates level jumps) in `src/main/java/org/gedcom7/parser/internal/LevelStack.java`
- [x] T035 [US2] Integrate `LevelStack` into `GedcomReader.parse()`: on each line, pop/push the level stack and fire endStructure/endRecord events in correct reverse order before starting new structures in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T036 [US2] Add error reporting for invalid level jumps (level increases by more than 1) in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T037 [US2] Verify all US2 tests pass: nested structures, multi-level pop, invalid level jump error

**Checkpoint**: Parse nested GEDCOM files with correct start/end event nesting. Invalid level jumps produce errors.

---

## Phase 5: User Story 3 - Handle All GEDCOM 7 Character Encoding Rules (Priority: P1)

**Goal**: Enforce GEDCOM 7 UTF-8 encoding rules: clean valid text in callbacks, malformed input reported as errors with byte offsets.

**Independent Test**: Feed the parser a byte stream containing banned characters and verify error events fire with byte offsets.

### Tests for User Story 3

- [x] T038 [P] [US3] Create test file `src/test/resources/banned-chars.ged` containing lines with banned characters (BEL, DEL, C1 controls)
- [x] T039 [P] [US3] Create test file `src/test/resources/multibyte-utf8.ged` containing CJK characters and emoji in line values
- [x] T040 [US3] Write parameterized tests for banned character detection (each banned range from Appendix B) with expected error events including byte offset in `src/test/java/org/gedcom7/parser/CharacterValidationTest.java`
- [x] T041 [US3] Write tests for TAB acceptance in line values and multi-byte UTF-8 correctness in `src/test/java/org/gedcom7/parser/CharacterValidationTest.java`

### Implementation for User Story 3

- [x] T042 [US3] Add banned character validation to `GedcomLineTokenizer`: scan each line for C0 controls (except TAB, CR, LF), DEL, C1 controls, surrogates, U+FFFE/FFFF; report errors with byte offset in `src/main/java/org/gedcom7/parser/internal/GedcomLineTokenizer.java`
- [x] T043 [US3] Add mid-file BOM detection (U+FEFF not at stream start treated as character, not stripped) in `src/main/java/org/gedcom7/parser/internal/Utf8InputDecoder.java`
- [x] T044 [US3] Verify all US3 tests pass: banned chars produce errors with correct byte offsets, TAB accepted, multi-byte UTF-8 works, mid-file BOM not stripped

**Checkpoint**: All banned characters produce error events. Valid multi-byte UTF-8 and TAB pass through cleanly.

---

## Phase 6: User Story 4 - Parse Cross-References and Pointers (Priority: P1)

**Goal**: Distinguish pointer values from string values, track cross-reference identifiers, report unresolved references and duplicate xrefs.

**Independent Test**: Parse a file with INDI and FAM records linked by FAMS/FAMC pointers, verify pointer events carry target xref and unresolved references are reported at end.

### Tests for User Story 4

- [x] T045 [P] [US4] Create test file `src/test/resources/cross-references.ged` with INDI and FAM records linked by FAMS/FAMC pointers, including @VOID@ and an unresolved @F99@ reference
- [x] T046 [P] [US4] Create test file `src/test/resources/duplicate-xref.ged` with two records sharing the same @I1@ xref
- [x] T047 [US4] Write tests for pointer vs string distinction, @VOID@ handling, unresolved reference warning at endDocument, duplicate xref error in `src/test/java/org/gedcom7/parser/CrossReferenceTrackingTest.java`

### Implementation for User Story 4

- [x] T048 [US4] Add xref tracking to `GedcomReader`: maintain a Set of defined xrefs and a Set of referenced pointer targets during parsing in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T049 [US4] On startRecord with xref: add to defined set, check for duplicate and fire error if duplicate in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T050 [US4] On endDocument: compute (referenced - defined - {@VOID@}) and fire warning for each unresolved reference in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T051 [US4] Ensure isPointer flag is correctly set in startStructure events for pointer line values in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T052 [US4] Verify all US4 tests pass: pointer detection, @VOID@ skipped, duplicates reported, unresolved references reported

**Checkpoint**: Cross-references are tracked end-to-end. Pointer/string distinction works. Duplicate xrefs and unresolved references produce appropriate error/warning events.

---

## Phase 7: User Story 5 - Handle Extension Tags and SCHMA (Priority: P2)

**Goal**: Correctly handle extension tags (_-prefixed) and resolve documented extensions via HEAD.SCHMA.TAG URIs.

**Independent Test**: Parse a file containing both documented (SCHMA-mapped) and undocumented extension tags. Verify events include the URI for documented extensions.

### Tests for User Story 5

- [x] T053 [P] [US5] Create test file `src/test/resources/extensions.ged` with HEAD.SCHMA.TAG mappings and both documented and undocumented extension tags in records
- [x] T054 [US5] Write tests for: SCHMA.TAG parsing, documented extension tag carries URI in event, undocumented extension tag has no URI and no error, extension tag at level 0 as record in `src/test/java/org/gedcom7/parser/ExtensionTagTest.java`

### Implementation for User Story 5

- [x] T055 [US5] Add SCHMA.TAG parsing logic during HEAD pre-scan: extract `extTag SP URI-reference` from HEAD.SCHMA.TAG substructures and build tag-to-URI map in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T056 [US5] Store SCHMA map in `GedcomHeaderInfo` and make available in startDocument event in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T057 [US5] On startStructure/startRecord with extension tag: look up URI from SCHMA map and include in event context (add URI field or method to handler callback if needed) in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T058 [US5] Verify all US5 tests pass: SCHMA parsing, URI resolution, undocumented extensions accepted

**Checkpoint**: Extension tags with SCHMA mappings carry URIs. Undocumented extensions parse without error.

---

## Phase 8: User Story 6 - Validate HEAD and TRLR Structure (Priority: P2)

**Goal**: Validate HEAD as first record (with GEDC.VERS), TRLR as last, and detect malformed or non-GEDCOM files early.

**Independent Test**: Feed the parser files missing HEAD, missing TRLR, and with non-7.x VERS values. Verify appropriate error events.

### Tests for User Story 6

- [x] T059 [P] [US6] Create test file `src/test/resources/no-head.ged` starting with INDI instead of HEAD
- [x] T060 [P] [US6] Create test file `src/test/resources/no-trlr.ged` with HEAD but no TRLR at end
- [x] T061 [P] [US6] Create test file `src/test/resources/vers-551.ged` with HEAD.GEDC.VERS = "5.5.1"
- [x] T062 [P] [US6] Create test file `src/test/resources/vers-71.ged` with HEAD.GEDC.VERS = "7.1"
- [x] T063 [US6] Write tests for: missing HEAD fatal error, missing TRLR error, non-7.x VERS warning, 7.0 and 7.1 accepted, TRLR with value/xref/substructure errors in `src/test/java/org/gedcom7/parser/HeadTrlrValidationTest.java`
- [x] T064 [US6] Write tests for GedcomHeaderInfo population: version parsed, SOUR extracted, LANG extracted, SCHMA map built, startDocument fires before HEAD startRecord in `src/test/java/org/gedcom7/parser/GedcomHeaderInfoTest.java`

### Implementation for User Story 6

- [x] T065 [US6] Implement HEAD pre-scan in `GedcomReader.parse()`: read HEAD record substructures to extract GEDC.VERS, SOUR, SOUR.VERS, SOUR.NAME, LANG, SCHMA.TAG; build GedcomHeaderInfo; fire startDocument(headerInfo); then replay HEAD as startRecord through all substructure events to endRecord in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T066 [US6] Add HEAD-first validation: if first record tag is not HEAD, fire fatalError in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T067 [US6] Add TRLR validation: at endDocument check that last record was TRLR, fire error if missing; validate TRLR has no value/xref/substructures in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T068 [US6] Parse GEDC.VERS into GedcomVersion; fire warning if not 7.x in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T069 [US6] Verify all US6 tests pass: HEAD/TRLR validation, GedcomHeaderInfo fully populated, HEAD replay after startDocument

**Checkpoint**: Malformed files detected early. GedcomHeaderInfo carries version, source, language, SCHMA. HEAD is replayed after startDocument.

---

## Phase 9: User Story 7 - Error Handling and Recovery (Priority: P2)

**Goal**: Report errors with rich context (line number, byte offset, raw line) and optionally continue parsing after non-fatal errors.

**Independent Test**: Feed the parser a file with malformed lines interspersed with valid lines. Verify errors reported with context and valid lines still produce events.

### Tests for User Story 7

- [x] T070 [P] [US7] Create test file `src/test/resources/malformed-lines.ged` with several malformed lines (missing tag, invalid level, bad UTF-8) interspersed with valid lines
- [x] T071 [US7] Write tests for: lenient mode continues after non-fatal error, strict mode stops after first error, error context contains correct line number and byte offset and raw line content, fatal errors always stop, resource limit exceeded (max depth and max line length) in `src/test/java/org/gedcom7/parser/ErrorHandlingTest.java`

### Implementation for User Story 7

- [x] T072 [US7] Implement strict/lenient mode branching in `GedcomReader.parse()`: on non-fatal error, check config.isStrict(); if strict, throw GedcomFatalException; if lenient, fire error event and continue to next line in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T073 [US7] Ensure all error events include populated GedcomParseError with line number (from tokenizer), byte offset (from tokenizer), message, and raw line content in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T074 [US7] Implement resource limit checks: max nesting depth (fire fatalError if level exceeds config.getMaxNestingDepth()), max line length (fire fatalError if line exceeds config.getMaxLineLength()) in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T075 [US7] Verify all US7 tests pass: strict stops, lenient continues, error context correct, resource limits enforced

**Checkpoint**: Lenient mode processes imperfect files, reporting errors with full context. Strict mode stops immediately. Resource limits protect against abuse.

---

## Phase 10: User Story 8 - Parse GEDCOM 7 Data Type Payloads (Priority: P3)

**Goal**: Companion utility classes that parse GEDCOM 7 payload strings into typed Java objects (dates, ages, coordinates, personal names, etc.).

**Independent Test**: Call each data type parser with valid and invalid inputs. Verify correct parsing and error reporting.

### Tests for User Story 8

> **Parameterized tests for each data type with positive and negative cases**

- [x] T076 [P] [US8] Write parameterized tests for `parseInteger` (valid: "0", "42", "999"; invalid: "-5", "007", "abc") in `src/test/java/org/gedcom7/parser/datatype/IntegerParserTest.java`
- [x] T077 [P] [US8] Write parameterized tests for `parseDateValue` covering plain dates, BET/AND, BEF, AFT, ABT, CAL, EST, FROM/TO, all 4 calendars, BCE epoch in `src/test/java/org/gedcom7/parser/datatype/DateParserTest.java`
- [x] T078 [P] [US8] Write parameterized tests for `parseTime` (valid: "12:30:00Z", "00:00", "23:59:59.999"; invalid: "24:00:00", "12:60") in `src/test/java/org/gedcom7/parser/datatype/TimeParserTest.java`
- [x] T079 [P] [US8] Write parameterized tests for `parseAge` (valid: "> 25y 3m", "< 1y", "3d"; invalid: "25") in `src/test/java/org/gedcom7/parser/datatype/AgeParserTest.java`
- [x] T080 [P] [US8] Write parameterized tests for `parsePersonalName` (with surname in /, without /, empty) in `src/test/java/org/gedcom7/parser/datatype/PersonalNameParserTest.java`
- [x] T081 [P] [US8] Write parameterized tests for `parseLatitude` and `parseLongitude` in `src/test/java/org/gedcom7/parser/datatype/CoordinateParserTest.java`
- [x] T082 [P] [US8] Write tests for remaining parsers: parseLanguage, parseMediaType, parseEnum, parseListText, parseListEnum, parseUri, parseFilePath, parseTagDef in `src/test/java/org/gedcom7/parser/datatype/MiscParsersTest.java`

### Implementation for User Story 8

- [x] T083 [P] [US8] Implement `GedcomDate`, `GedcomDateRange`, `GedcomDatePeriod` immutable value classes in `src/main/java/org/gedcom7/parser/datatype/`
- [x] T084 [P] [US8] Implement `GedcomTime` immutable value class in `src/main/java/org/gedcom7/parser/datatype/GedcomTime.java`
- [x] T085 [P] [US8] Implement `GedcomAge` immutable value class in `src/main/java/org/gedcom7/parser/datatype/GedcomAge.java`
- [x] T086 [P] [US8] Implement `GedcomPersonalName` immutable value class in `src/main/java/org/gedcom7/parser/datatype/GedcomPersonalName.java`
- [x] T087 [P] [US8] Implement `GedcomCoordinate` immutable value class in `src/main/java/org/gedcom7/parser/datatype/GedcomCoordinate.java`
- [x] T088 [US8] Implement `GedcomDataTypes.parseInteger()` and `GedcomDataTypes.parseDateValue()` (the two most complex parsers, covering full ABNF grammar) in `src/main/java/org/gedcom7/parser/datatype/GedcomDataTypes.java`
- [x] T089 [US8] Implement `GedcomDataTypes.parseTime()`, `parseAge()`, `parsePersonalName()` in `src/main/java/org/gedcom7/parser/datatype/GedcomDataTypes.java`
- [x] T090 [US8] Implement `GedcomDataTypes.parseLatitude()`, `parseLongitude()` in `src/main/java/org/gedcom7/parser/datatype/GedcomDataTypes.java`
- [x] T091 [US8] Implement remaining parsers: `parseLanguage`, `parseMediaType`, `parseEnum`, `parseListText`, `parseListEnum`, `parseUri`, `parseFilePath`, `parseTagDef` in `src/main/java/org/gedcom7/parser/datatype/GedcomDataTypes.java`
- [x] T092 [US8] Verify all US8 tests pass: all data type parsers handle positive and negative cases

**Checkpoint**: All 16+ data type parsers work correctly. Value classes are immutable with equals/hashCode/toString.

---

## Phase 11: Structure and Cardinality Validation (Opt-In)

**Goal**: Optional validation layer that checks structures against GEDCOM 7 definitions and reports warnings/errors for unexpected contexts or cardinality violations.

**Independent Test**: Enable validation, parse a file with invalid structure placement and cardinality violations, verify appropriate warnings fire.

### Tests for Structure Validation

- [x] T093 [P] Create test file `src/test/resources/invalid-structure.ged` with structures in wrong contexts (e.g., DATE at level 0, BIRT under FAM without proper context)
- [x] T094 [P] Create test file `src/test/resources/cardinality-violation.ged` with structures exceeding {0:1} cardinality (e.g., two SEX substructures under INDI)
- [x] T095 Write tests for: unknown structure in context warns, cardinality {0:1} exceeded warns, extension tags exempt from validation, validation disabled by default (no warnings), validation enabled produces warnings in `src/test/java/org/gedcom7/parser/validation/StructureValidationTest.java`

### Implementation for Structure Validation

- [x] T096 Download GEDCOM 7 TSV files (substructures.tsv, cardinalities.tsv, payloads.tsv, enumerations.tsv, enumerationsets.tsv) from FamilySearch/GEDCOM repo and place in `src/main/data/gedcom7/`
- [x] T097 Create Gradle build task in `build.gradle.kts` that reads TSV files from `src/main/data/gedcom7/` and generates `StructureDefinitions.java` in `build/generated/sources/gedcom/main/java/org/gedcom7/parser/validation/`; wire generated sources into main source set
- [x] T098 Implement TSV-to-Java code generator: parse substructures.tsv and cardinalities.tsv into lookup tables (resolveStructure, getCardinality methods) in the generated `StructureDefinitions` class
- [x] T099 Implement validation logic in `GedcomReader`: when config.isStructureValidationEnabled(), maintain context stack of structure URIs; on startStructure, resolve (parentUri, tag) -> childUri and check cardinality; fire warning for unknown context or cardinality violation; exempt extension tags in `src/main/java/org/gedcom7/parser/GedcomReader.java`
- [x] T100 Verify all validation tests pass: context warnings, cardinality warnings, extension exemption, disabled-by-default behavior

**Checkpoint**: Opt-in validation reports structure and cardinality issues as warnings. Extension tags are exempt. Disabled by default.

---

## Phase 12: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T101 [P] Add Javadoc to all public API types and methods per NFR-007: GedcomReader, GedcomHandler, GedcomReaderConfig, GedcomHeaderInfo, GedcomVersion, GedcomParseError, GedcomFatalException, all datatype classes
- [x] T102 [P] Create integration test file `src/test/resources/all-record-types.ged` exercising all 7 standard record types (INDI, FAM, OBJE, REPO, SNOTE, SOUR, SUBM) plus HEAD/TRLR
- [x] T103 Write end-to-end integration test parsing all-record-types.ged and verifying all record type tags are recognized in `src/test/java/org/gedcom7/parser/AllRecordTypesTest.java`
- [x] T104 [P] Run quickstart.md code examples as test cases to validate they work correctly in `src/test/java/org/gedcom7/parser/QuickstartExamplesTest.java`
- [x] T105 Verify SC-005: count public types and confirm fewer than 15 in `src/test/java/org/gedcom7/parser/PublicApiSurfaceTest.java`
- [x] T106 Verify SC-006: confirm zero runtime dependencies in build.gradle.kts (only testImplementation dependencies)
- [x] T107 Final `gradle build` with all tests passing, no warnings

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion - BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 - MVP core parsing
- **US2 (Phase 4)**: Depends on US1 (nesting builds on line parsing)
- **US3 (Phase 5)**: Depends on US1 (character validation hooks into tokenizer)
- **US4 (Phase 6)**: Depends on US1 (xref tracking during parsing)
- **US5 (Phase 7)**: Depends on US1 + US6 (SCHMA is in HEAD)
- **US6 (Phase 8)**: Depends on US1 + US2 (HEAD parsing needs nesting)
- **US7 (Phase 9)**: Depends on US1 (error mode is a reader-level concern)
- **US8 (Phase 10)**: Independent of US1-7 (companion utilities, no parser wiring)
- **Validation (Phase 11)**: Depends on US2 + US6 (needs nesting and HEAD info)
- **Polish (Phase 12)**: Depends on all prior phases

### User Story Dependencies

```
Phase 1: Setup
    │
Phase 2: Foundational
    │
Phase 3: US1 (Line Parsing) ──── MVP ────
    │         │         │         │
Phase 4:   Phase 5:  Phase 6:  Phase 10:
US2        US3       US4       US8 (Data Types)
(Nesting)  (Chars)   (Xrefs)   [independent]
    │                   │
    ├───────────────────┤
    │                   │
Phase 8: US6           Phase 7: US5
(HEAD/TRLR)            (Extensions)
    │
Phase 9: US7 (Error Handling)
    │
Phase 11: Validation
    │
Phase 12: Polish
```

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Internal components before integration
- Core implementation before edge cases
- Story complete before moving to next priority

### Parallel Opportunities

- All Phase 2 foundational value classes (T005–T014) can run in parallel
- US1 test files (T016–T019) can be created in parallel
- US1 internal implementations (T025–T027) can run in parallel (different files)
- US3, US4, US7 can run in parallel after US1 completes (different concerns)
- US8 can run entirely in parallel with US2–US7 (companion utilities, no parser dependency)
- All US8 value classes (T083–T087) can run in parallel
- All US8 test classes (T076–T082) can run in parallel
- Phase 11 (Validation) is independent of US8

---

## Parallel Example: User Story 1

```
# After Phase 2 completes, launch US1 test files in parallel:
T016: Create minimal.ged
T017: Create minimal-bom.ged
T018: Create minimal-crlf.ged
T019: Create cont-multiline.ged

# Then write US1 unit tests (some parallel):
T020, T021, T022 in parallel (different test files)
T023: GedcomLineTokenizerTest (depends on understanding of tokenizer)
T024: Integration test

# Then implement US1 internals in parallel:
T025: Utf8InputDecoder
T026: LeadingAtEscapeStrategy
T027: ContOnlyAssembler

# Then sequential:
T028: GedcomLineTokenizer (depends on T025, T026)
T029: GedcomReader.parse() skeleton (depends on T027, T028)
T030: Verify all tests pass
```

## Parallel Example: User Story 8 (Data Types)

```
# Can start immediately after Phase 2 (no US1 dependency):

# All value classes in parallel:
T083: GedcomDate + GedcomDateRange + GedcomDatePeriod
T084: GedcomTime
T085: GedcomAge
T086: GedcomPersonalName
T087: GedcomCoordinate

# All test classes in parallel:
T076: IntegerParserTest
T077: DateParserTest
T078: TimeParserTest
T079: AgeParserTest
T080: PersonalNameParserTest
T081: CoordinateParserTest
T082: MiscParsersTest

# Then implement parsers:
T088: parseInteger + parseDateValue
T089: parseTime + parseAge + parsePersonalName
T090: parseLatitude + parseLongitude
T091: Remaining parsers
T092: Verify all pass
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Parse minimal.ged, verify event sequence
5. This delivers a working streaming parser that can tokenize GEDCOM 7 lines

### Incremental Delivery

1. Setup + Foundational -> Build compiles
2. US1 -> Streaming line parsing works (MVP!)
3. US2 -> Proper nesting
4. US3 + US4 -> Character validation + cross-references (parallel)
5. US6 -> HEAD/TRLR validation + GedcomHeaderInfo
6. US5 + US7 -> Extensions + error recovery (parallel)
7. US8 -> Data type utilities (can run anytime after Phase 2)
8. Validation -> Opt-in structure checking
9. Polish -> Javadoc, integration tests, quickstart validation

### Parallel Team Strategy

With multiple developers after Phase 2:
- Developer A: US1 -> US2 -> US6 -> Validation (parser core path)
- Developer B: US8 (data type utilities, fully independent)
- Developer C: US3 + US4 + US5 + US7 (after US1 completes, all can proceed)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Tests MUST fail before implementing (Constitution Principle V: TDD)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Zero runtime dependencies must be maintained throughout (Constitution Principle VII)
- Mechanical sympathy: avoid unnecessary allocations in GedcomLineTokenizer and GedcomReader hot path (Constitution Principle III)
