# Feature Specification: Tag and Value Constants

**Feature Branch**: `008-tag-value-constants`
**Created**: 2026-03-07
**Status**: Draft
**Input**: User description: "Please create the spec for items #1 and #2 with the goal of making this library easier to use"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Discover and use GEDCOM tag constants in handler code (Priority: P1)

A developer new to GEDCOM wants to write a handler that extracts individuals and their names. Today they must know that "INDI" is the tag for individuals and "NAME" is the tag for names, and they must type these strings correctly. With tag constants, the developer can type `GedcomTag.` in their IDE and browse available record types, then type `GedcomTag.Indi.` to see all structures valid inside an INDI record. Autocomplete guides them to the right tags without consulting the GEDCOM specification.

**Why this priority**: Tag constants eliminate the most common source of bugs (misspelled string literals) and provide the primary discoverability benefit. This is the foundation that makes the library approachable.

**Independent Test**: Can be fully tested by writing a handler that uses constants instead of string literals for all tag comparisons, and verifying the constants resolve to the correct string values.

**Acceptance Scenarios**:

1. **Given** a developer writing a GedcomHandler, **When** they type `GedcomTag.Indi.` in their IDE, **Then** they see autocomplete suggestions for NAME, SEX, BIRT, DEAT, FAMC, FAMS, and other INDI substructure tags.
2. **Given** a developer using `GedcomTag.Indi.NAME` in a switch/case statement, **When** the parser delivers a NAME tag as a String, **Then** the constant matches via `equals()` and the case arm executes.
3. **Given** a developer reading the Javadoc for `GedcomTag.Indi.BIRT`, **When** they hover over it in their IDE, **Then** they see a description of what BIRT represents and which substructures it can contain (e.g., DATE, PLAC).
4. **Given** a handler that processes both INDI and REPO records, **When** both have a NAME substructure, **Then** the developer can distinguish them using `GedcomTag.Indi.NAME` vs `GedcomTag.Repo.NAME`, even though both resolve to the string "NAME".

---

### User Story 2 - Use value constants for known GEDCOM enumeration types (Priority: P2)

A developer processing SEX structures needs to compare the value against known options ("M", "F", "X", "U"). Today they must know these values from the spec. With value constants, the developer can type `GedcomValue.Sex.` and see all valid options with descriptions. This applies to all GEDCOM enumeration types: SEX, NAME TYPE, PEDI, RESN, ROLE, and others.

**Why this priority**: Value constants complement tag constants by covering the other half of "magic strings" developers must type. They prevent a different class of bugs (wrong enumeration values) and further reduce the need to consult the GEDCOM specification.

**Independent Test**: Can be fully tested by verifying each value constant resolves to the correct string and that a handler using value constants correctly matches parser-delivered values.

**Acceptance Scenarios**:

1. **Given** a developer processing a SEX structure's value, **When** they type `GedcomValue.Sex.`, **Then** they see autocomplete for MALE, FEMALE, INTERSEX, and UNKNOWN with Javadoc describing each.
2. **Given** a developer checking a PEDI (pedigree) value, **When** they use `GedcomValue.Pedi.ADOPTED`, **Then** it equals the string "ADOPTED" delivered by the parser.
3. **Given** a developer reading the Javadoc for `GedcomValue.NameType.MARRIED`, **When** they hover over it, **Then** they see a description explaining this represents a name acquired through marriage.

---

### User Story 3 - Use record-level tag constants for top-level record identification (Priority: P1)

A developer writing `startRecord()` needs to identify which record type is being processed. Today they write `"INDI".equals(tag)`. With constants, they write `GedcomTag.INDI.equals(tag)` or use `GedcomTag.INDI` in a switch statement. Top-level record tags (INDI, FAM, SOUR, REPO, OBJE, NOTE, SNOTE, SUBM, HEAD, TRLR) are the most frequently used constants.

**Why this priority**: Record-level tags are used in every non-trivial handler. They are the entry point for any GEDCOM processing and the first constants a developer reaches for.

**Independent Test**: Can be fully tested by writing a handler that uses record-level constants in `startRecord()` and verifying correct record identification.

**Acceptance Scenarios**:

1. **Given** a developer writing a `startRecord` override, **When** they use `GedcomTag.INDI` in a switch statement, **Then** it matches individual records delivered by the parser.
2. **Given** a developer browsing `GedcomTag` in their IDE, **When** they see top-level constants like INDI, FAM, SOUR, **Then** the Javadoc for each describes the record type and its purpose.

---

### User Story 4 - Javadoc links guide developers to related parser methods (Priority: P3)

A developer discovers the `GedcomTag.Indi.NAME` constant and wants to know how to parse the name value. The Javadoc for the constant includes a `@see` link to `GedcomDataTypes.parsePersonalName()`. Similarly, date-related tags link to `GedcomDataTypes.parseDateValue()` and age tags link to `GedcomDataTypes.parseAge()`. This creates a guided path from tag discovery to value parsing.

**Why this priority**: This is a quality-of-life enhancement that builds on the tag constants. It connects two parts of the API that developers otherwise must discover independently.

**Independent Test**: Can be tested by verifying Javadoc `@see` annotations exist on relevant constants and that the referenced methods exist and are public.

**Acceptance Scenarios**:

1. **Given** a developer reading the Javadoc for a NAME tag constant, **When** they see the `@see` reference, **Then** it links to `GedcomDataTypes.parsePersonalName()`.
2. **Given** a developer reading the Javadoc for a DATE tag constant, **When** they see the `@see` reference, **Then** it links to `GedcomDataTypes.parseDateValue()`.

---

### Edge Cases

- What happens when the developer encounters an extension tag (e.g., `_CUSTOM`) not covered by constants? They continue using raw string comparison as today. Constants do not restrict the tag space.
- What happens when a tag appears in multiple record contexts (e.g., NAME in INDI vs REPO)? Both `GedcomTag.Indi.NAME` and `GedcomTag.Repo.NAME` exist and both resolve to the string `"NAME"`. The nested class provides semantic context in the developer's code; the string value is identical.
- What happens when a GEDCOM file uses a value not in the known enumeration (e.g., a custom SEX value)? The developer can still compare against the raw string. Value constants cover the standard set; they do not restrict what values the parser delivers.
- What happens when a future GEDCOM version adds new tags or values? New constants are added in a future library release. Existing constants remain unchanged. Developer code using raw strings for newer tags continues to work.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The library MUST provide a `GedcomTag` class containing `public static final String` constants for all standard GEDCOM 7 record-level tags (HEAD, TRLR, INDI, FAM, OBJE, SOUR, REPO, NOTE, SNOTE, SUBM).
- **FR-002**: The `GedcomTag` class MUST contain nested static classes for each record type that has substructures (e.g., `GedcomTag.Indi`, `GedcomTag.Fam`, `GedcomTag.Sour`), each containing constants for the valid substructure tags within that record context. Shared tags (DATE, PLAC, NOTE, etc.) MUST appear within each relevant record-specific class — there is no separate common/shared grouping.
- **FR-003**: Nested classes MUST include one additional nesting level ONLY for individual and family event structures (BIRT, DEAT, BURI, CHR, BAPM, MARR, DIV, ANUL, EVEN, and similar event tags). These event nested classes contain their substructure constants (e.g., `GedcomTag.Indi.Birt.DATE`, `GedcomTag.Indi.Birt.PLAC`). Non-event substructures do NOT get their own nested class.
- **FR-004**: Each tag constant MUST have Javadoc describing what the tag represents in plain language, suitable for developers unfamiliar with GEDCOM.
- **FR-005**: Tag constants for structures with parseable values (NAME, DATE, AGE) MUST include `@see` Javadoc references to the corresponding `GedcomDataTypes` parser method.
- **FR-006**: The library MUST provide a `GedcomValue` class containing nested static classes for each standard GEDCOM 7 enumeration type, with `public static final String` constants for each known value.
- **FR-007**: `GedcomValue` MUST cover at minimum the following enumeration types: Sex (M, F, X, U), NameType (BIRTH, IMMIGRANT, MAIDEN, MARRIED, PROFESSIONAL, OTHER), Pedi (BIRTH, ADOPTED, FOSTER, SEALING, OTHER), Resn (CONFIDENTIAL, LOCKED, PRIVACY), Role (CHIL, CLERGY, FATH, FRIEND, GODP, HUSB, MOTH, MULT, NGHBR, OFFICIATOR, PARENT, SPOU, WIFE, WITN, OTHER), Medi (AUDIO, BOOK, CARD, ELECTRONIC, FICHE, FILM, MAGAZINE, MANUSCRIPT, MAP, NEWSPAPER, PHOTO, TOMBSTONE, VIDEO, OTHER), and Adop (HUSB, WIFE, BOTH).
- **FR-007a**: Value constant names MUST use descriptive human-readable names rather than raw GEDCOM codes (e.g., `MALE` not `M`, `FEMALE` not `F`, `INTERSEX` not `X`). The constant's string value MUST be the exact GEDCOM code the parser delivers.
- **FR-008**: Each value constant MUST have Javadoc describing what the value means in the GEDCOM context.
- **FR-009**: All constants MUST be `public static final String` fields so they can be used in Java `switch` statements and `equals()` comparisons with parser-delivered strings.
- **FR-010**: The constants classes MUST NOT introduce any runtime dependencies beyond the existing library (zero new dependencies).
- **FR-011**: The constants MUST NOT change or constrain the parser's behavior. They are a developer convenience layer only.

### Key Entities

- **GedcomTag**: A utility class with no instances, containing string constants for GEDCOM tags organized by record context via nested static classes.
- **GedcomValue**: A utility class with no instances, containing string constants for GEDCOM enumeration values organized by enumeration type via nested static classes.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer can write a complete INDI-extraction handler using only constants (no string literals for standard tags), and the handler correctly identifies records and structures when run against a test file.
- **SC-002**: IDE autocomplete lists all valid substructure tags for a given record type when the developer types the nested class name followed by a dot (e.g., `GedcomTag.Indi.`).
- **SC-003**: Every standard GEDCOM 7 enumeration value listed in FR-007 has a corresponding constant in `GedcomValue`, and each constant's string value exactly matches what the parser delivers.
- **SC-004**: All existing tests continue to pass unchanged, confirming the constants layer has no impact on parser behavior.
- **SC-005**: The QuickstartExamplesTest (or a new companion test) demonstrates the same extractIndividuals pattern rewritten with constants, proving the improved developer experience.

## Clarifications

### Session 2026-03-07

- Q: Should value constant names match GEDCOM values exactly (e.g., `M`) or use descriptive names (e.g., `MALE`)? → A: Use descriptive names. Constants use human-readable names that map to the GEDCOM value (e.g., `GedcomValue.Sex.MALE = "M"`, `GedcomValue.Sex.FEMALE = "F"`).
- Q: Should shared tags (DATE, PLAC, NOTE) also appear in a common grouping, or only within record-specific classes? → A: Only within record-specific classes (e.g., `GedcomTag.Indi.DATE`, `GedcomTag.Fam.DATE`). No `GedcomTag.Common` class. New developers will navigate by record type, not by searching for a "common" bucket.
- Q: Which substructures get their own nested class for sub-nesting (FR-003)? → A: Only individual and family event structures (BIRT, DEAT, BURI, CHR, BAPM, MARR, DIV, ANUL, EVEN, etc.). These are the primary disambiguation need (DATE inside BIRT vs DEAT). Non-event substructures stay flat.
