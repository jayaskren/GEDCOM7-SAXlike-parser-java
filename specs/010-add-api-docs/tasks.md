# Tasks: Add API Documentation

**Input**: Design documents from `/specs/010-add-api-docs/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md

**Tests**: Not applicable — this is a documentation-only feature. No test code needed.

**Organization**: Tasks are grouped by user story to enable independent implementation and verification of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Carried-Over Documentation)

**Purpose**: Commit the documentation improvements carried over from the 007/009 branches as a baseline

- [X] T001 Verify carried-over changes are intact in README.md, docs/architecture.md, and docs/tutorial.md (version-differences, 5.5.5 phrasing updates)

---

## Phase 2: User Story 1 - Writer API Tutorial (Priority: P1)

**Goal**: Add a tutorial section (Step 11) for the Writer API with complete code examples

**Independent Test**: Tutorial contains a Writer section with imports, record creation, substructure nesting, and 5.5.5 config guidance

### Implementation for User Story 1

- [X] T002 [US1] Read GedcomWriter.java, GedcomWriterConfig.java, and context classes (HeadContext, IndividualContext, FamilyContext) in src/main/java/org/gedcom7/writer/ to understand exact method signatures and context methods available
- [X] T003 [US1] Add Step 11: Writing GEDCOM Files section to docs/tutorial.md after Step 10. Include: imports, GedcomWriter construction with OutputStream, head() with HeadContext, individual() with IndividualContext showing personalName/sex/birth with nested date/place, family() with FamilyContext showing husband/wife/child, close()/try-with-resources pattern, example output, and GedcomWriterConfig.gedcom555() mention for 5.5.5 output
- [X] T004 [US1] Launch independent review agent to verify Writer tutorial section accuracy against source code API

**Checkpoint**: Writer API is fully documented in the tutorial

---

## Phase 3: User Story 2 - Converter API Tutorial (Priority: P2)

**Goal**: Add a tutorial section (Step 12) for the Converter API with examples for both conversion directions

**Independent Test**: Tutorial contains a Converter section with examples for 5.5.5→7 and 7→5.5.5, plus ConversionResult inspection

### Implementation for User Story 2

- [X] T005 [US2] Read GedcomConverter.java, GedcomConverterConfig.java, and ConversionResult.java in src/main/java/org/gedcom7/converter/ to verify exact method signatures and result getters
- [X] T006 [US2] Add Step 12: Converting Between GEDCOM Versions section to docs/tutorial.md after Step 11. Include: imports, GedcomConverter.convert() with FileInputStream/FileOutputStream, GedcomConverterConfig.toGedcom7() and toGedcom555(), ConversionResult inspection (getSourceVersion, getTargetVersion, getRecordCount, getWarningCount, getWarnings), and try-with-resources pattern
- [X] T007 [US2] Launch independent review agent to verify Converter tutorial section accuracy against source code API

**Checkpoint**: Converter API is fully documented in the tutorial

---

## Phase 4: User Story 3 - Gedzip Support Tutorial (Priority: P3)

**Goal**: Add a tutorial section (Step 13) for GedzipReader showing how to open, list, and parse .gdz archives

**Independent Test**: Tutorial contains a Gedzip section with archive opening, entry listing, and GEDCOM stream parsing

### Implementation for User Story 3

- [X] T008 [US3] Read GedzipReader.java in src/main/java/org/gedcom7/parser/ to verify constructor, getGedcomStream(), getEntryNames(), getEntry(), hasEntry(), and isExternalReference() signatures
- [X] T009 [US3] Add Step 13: Working with Gedzip Archives section to docs/tutorial.md after Step 12. Include: imports, GedzipReader construction with File or Path, getGedcomStream() piped to GedcomReader, getEntryNames() iteration, getEntry() for specific files, hasEntry() check, isExternalReference() mention, and try-with-resources pattern
- [X] T010 [US3] Launch independent review agent to verify Gedzip tutorial section accuracy against source code API

**Checkpoint**: Gedzip support is fully documented in the tutorial

---

## Phase 5: User Story 4 - Updated Summary Table (Priority: P1)

**Goal**: Update the tutorial summary table to cover all library capabilities

**Independent Test**: Summary table includes rows for Writer, Converter, Gedzip, 5.5.5 configs, and common substructure constants

### Implementation for User Story 4

- [X] T011 [US4] Update the Summary table at the end of docs/tutorial.md to add rows for: GedcomWriter (write a file), GedcomWriterConfig (configure writing with version-specific factory methods), GedcomConverter (convert between versions), GedcomConverterConfig (conversion config), GedzipReader (parse .gdz archives), GedcomReaderConfig 5.5.5 configs (.gedcom555(), .autoDetect()), and common substructure constants (GedcomTag.Plac.MAP, GedcomTag.Map.LATI, etc.)
- [X] T012 [US4] Verify no existing summary table rows were removed or broken

**Checkpoint**: Summary table reflects all library capabilities

---

## Phase 6: Polish & Final Verification

**Purpose**: Final review and evaluation across all documentation

- [X] T013 Review all new tutorial sections for consistent tone, formatting, and step numbering in docs/tutorial.md
- [X] T014 Verify README.md, docs/architecture.md, and docs/tutorial.md carried-over improvements are intact
- [X] T015 Launch final independent evaluation agent to verify all spec requirements (FR-001 through FR-007) and success criteria (SC-001 through SC-004) against the complete documentation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verify carried-over changes first
- **US1 Writer (Phase 2)**: Depends on Phase 1 — writes Step 11
- **US2 Converter (Phase 3)**: Depends on Phase 2 — writes Step 12 after Step 11
- **US3 Gedzip (Phase 4)**: Depends on Phase 3 — writes Step 13 after Step 12
- **US4 Summary Table (Phase 5)**: Depends on Phases 2-4 — needs to reference all new sections
- **Polish (Phase 6)**: Depends on all user stories complete

### User Story Dependencies

- **US1 (Writer)**: Independent — can start after setup
- **US2 (Converter)**: Depends on US1 (Step 12 follows Step 11 sequentially in the file)
- **US3 (Gedzip)**: Depends on US2 (Step 13 follows Step 12 sequentially in the file)
- **US4 (Summary Table)**: Depends on US1-3 (table must reference all new sections)

### Within Each User Story

- Read source code first to verify API signatures
- Write tutorial section
- Independent review agent verifies accuracy

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup verification
2. Complete Phase 2: Writer API tutorial section
3. **STOP and VALIDATE**: Writer section is independently useful

### Incremental Delivery

1. Setup → verify carried-over docs
2. US1: Writer tutorial (Step 11) → review → independently useful
3. US2: Converter tutorial (Step 12) → review → independently useful
4. US3: Gedzip tutorial (Step 13) → review → independently useful
5. US4: Summary table update → review → complete reference card
6. Polish: Final verification across all docs

---

## Notes

- All tasks modify only documentation files (no source code changes)
- Tutorial sections are sequential in the file, so US1→US2→US3 must be done in order
- Each review task (T004, T007, T010) launches an independent agent per Constitution Principle VIII
- T015 is the final evaluation agent per Constitution Principle VIII
