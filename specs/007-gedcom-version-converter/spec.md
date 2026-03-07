# Feature Specification: GEDCOM Version Converter

**Feature Branch**: `007-gedcom-version-converter`
**Created**: 2026-03-06
**Status**: Draft
**Input**: The library can now read and write both GEDCOM 5.5.5 and GEDCOM 7. This feature adds bidirectional conversion between the two versions so that users of different genealogy software (which may support only one version) can exchange data.

## Clarifications

### Session 2026-03-06

- Q: When converting GEDCOM 7 to 5.5.5, should HEAD.SCHMA (a GEDCOM 7-only structure) be dropped or preserved? → A: Preserve HEAD.SCHMA as-is in the 5.5.5 output, treating it as an unknown structure.

## Context

The GEDCOM SAX-like parser (feature 001/002/005) reads both GEDCOM 5.5.5 and GEDCOM 7 files, normalizing format-specific differences (CONT/CONC payload assembly, @-escaping, character encoding). The writer (feature 004/006) outputs either version with correct formatting (line length limits, @-escaping, HEAD.CHAR, CONT splitting). A converter bridges the two: it reads a file in one version and writes it in the other, letting the parser and writer handle the version-specific formatting automatically.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Convert GEDCOM 5.5.5 to GEDCOM 7 (Priority: P1)

As a developer integrating with modern genealogy software, I want to convert a GEDCOM 5.5.5 file to GEDCOM 7 format, so that legacy data exported from older software can be used with applications that only support the newer standard.

**Why this priority**: GEDCOM 5.5.5 is the legacy format with the largest existing corpus of files. Converting to GEDCOM 7 is the most common direction as the ecosystem migrates to the newer standard. This delivers the highest immediate value.

**Independent Test**: Pass a GEDCOM 5.5.5 input stream to the converter with target version GEDCOM 7, then verify the output is valid GEDCOM 7 — correct HEAD (no CHAR line, VERS 7.0), no CONC lines, leading-@-only escaping, and all records preserved.

**Acceptance Scenarios**:

1. **Given** a valid GEDCOM 5.5.5 file with HEAD, individuals, families, sources, and TRLR, **When** the developer converts it to GEDCOM 7, **Then** the output is a valid GEDCOM 7 file with all records present, HEAD.GEDC.VERS is "7.0", and there is no HEAD.CHAR line.
2. **Given** a GEDCOM 5.5.5 file with CONC-split long lines, **When** converted to GEDCOM 7, **Then** the values are reassembled into single lines (CONT only for actual newlines, no CONC).
3. **Given** a GEDCOM 5.5.5 file with @@-escaped values, **When** converted to GEDCOM 7, **Then** the escaping follows GEDCOM 7 rules (leading-@ only).
4. **Given** a GEDCOM 5.5.5 file with FAMS/FAMC pointers on individual records, **When** converted to GEDCOM 7, **Then** those pointers are preserved as-is (they are valid, if deprecated, in GEDCOM 7).
5. **Given** a GEDCOM 5.5.5 file with multi-line values using CONT, **When** converted to GEDCOM 7, **Then** the multi-line values are preserved correctly.

---

### User Story 2 - Convert GEDCOM 7 to GEDCOM 5.5.5 (Priority: P2)

As a developer integrating with legacy genealogy software, I want to convert a GEDCOM 7 file to GEDCOM 5.5.5 format, so that data from modern applications can be imported into older software that only supports the legacy standard.

**Why this priority**: While less common than the forward conversion, backward compatibility is essential for interoperability with the large installed base of legacy genealogy software.

**Independent Test**: Pass a GEDCOM 7 input stream to the converter with target version GEDCOM 5.5.5, then verify the output is valid GEDCOM 5.5.5 — HEAD has CHAR UTF-8, VERS 5.5.5, lines are limited to 255 characters (CONC-split if needed), and all @ signs are doubled.

**Acceptance Scenarios**:

1. **Given** a valid GEDCOM 7 file, **When** the developer converts it to GEDCOM 5.5.5, **Then** the output is a valid GEDCOM 5.5.5 file with HEAD.GEDC.VERS "5.5.5", HEAD.CHAR "UTF-8", and all records present.
2. **Given** a GEDCOM 7 file with lines longer than 255 characters, **When** converted to GEDCOM 5.5.5, **Then** long lines are split using CONC continuation.
3. **Given** a GEDCOM 7 file with values containing @ characters, **When** converted to GEDCOM 5.5.5, **Then** all @ signs in values are doubled (@@).
4. **Given** a GEDCOM 7 file with extension tags (prefixed with _), **When** converted to GEDCOM 5.5.5, **Then** extension tags and their substructures are preserved.

---

### User Story 3 - Streaming Conversion (Priority: P3)

As a developer processing large GEDCOM files, I want the converter to work in a streaming fashion (InputStream to OutputStream), so that I can convert files of any size without loading the entire file into memory.

**Why this priority**: Genealogy files can be very large (millions of records for community databases). Streaming conversion ensures the tool remains practical for all file sizes.

**Independent Test**: Convert a GEDCOM file with 1,000+ records using the streaming converter and verify that the output is correct and memory usage remains bounded.

**Acceptance Scenarios**:

1. **Given** a large GEDCOM file, **When** the developer converts it using the streaming converter, **Then** the output is produced incrementally without buffering the entire file in memory.
2. **Given** an InputStream and OutputStream, **When** the developer calls the converter, **Then** the converter reads from the input and writes to the output in a single pass.

---

### User Story 4 - Conversion Error Reporting (Priority: P4)

As a developer, I want the converter to report any issues encountered during conversion (e.g., structures that cannot be perfectly represented in the target version), so that I can decide how to handle imperfect conversions.

**Why this priority**: Not all structures translate perfectly between versions. Developers need visibility into potential data quality issues after conversion.

**Independent Test**: Convert a GEDCOM file that contains version-specific features and verify that warnings are reported for elements that may lose fidelity.

**Acceptance Scenarios**:

1. **Given** a malformed or partially invalid GEDCOM file, **When** the developer converts it, **Then** the converter reports parsing errors through the configured error handler and continues converting valid records (lenient mode).
2. **Given** a GEDCOM file with structures that have no direct equivalent in the target version, **When** converted, **Then** the converter emits warnings for each such structure while preserving the data as faithfully as possible.
3. **Given** a developer who wants strict conversion, **When** they configure strict mode, **Then** the converter stops at the first error rather than continuing.

---

### User Story 5 - Xref Preservation (Priority: P5)

As a developer, I want cross-reference identifiers (xrefs) to be preserved during conversion, so that external references to records (e.g., in application databases) remain valid after conversion.

**Why this priority**: Xref stability is important for round-trip fidelity and for applications that store GEDCOM xrefs as foreign keys.

**Independent Test**: Convert a file with custom xref IDs and verify all xrefs in the output match the input exactly.

**Acceptance Scenarios**:

1. **Given** a GEDCOM file where records have xref IDs like `@I1@`, `@F1@`, `@S1@`, **When** converted, **Then** the output uses the same xref IDs.
2. **Given** a GEDCOM file with pointer references (e.g., HUSB @I1@), **When** converted, **Then** pointer references in the output match the original xref IDs.

---

### Edge Cases

- What happens when the input file has an unrecognized GEDCOM version (not 5.5.5 or 7.0)? The converter should report an error and refuse to convert.
- What happens when the input file is already in the target version? The converter should still process it (effectively re-formatting/normalizing the file).
- What happens with extension tags defined in HEAD.SCHMA? They should be passed through to the output. When converting 7→5.5.5, HEAD.SCHMA itself is preserved as-is (treated as an unknown structure), along with the extension tags it defines.
- What happens with empty or null values? They should be preserved as-is.
- What happens with deeply nested structures? They should be preserved at the same nesting depth.
- What happens with GEDCOM 7 SNOTE records that carry inline text values? They should be preserved when converting to 5.5.5 (SNOTE with text is valid in both).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The converter MUST accept a GEDCOM input stream and a target version, and produce a GEDCOM output stream in the target version.
- **FR-002**: The converter MUST correctly convert HEAD records between versions, including GEDC.VERS, CHAR (added as "UTF-8" for 5.5.5, removed for 7), and GEDC.FORM (added as "LINEAGE-LINKED" for 5.5.5 if absent, preserved if already present).
- **FR-003**: The converter MUST handle @-escaping differences between versions — double all @ for 5.5.5 output, use leading-@ rules for GEDCOM 7 output.
- **FR-004**: The converter MUST handle line-length differences — CONC splitting for 5.5.5 (max 255 chars), no CONC for GEDCOM 7.
- **FR-005**: The converter MUST preserve all record types and their complete substructure trees, including extension tags.
- **FR-006**: The converter MUST preserve cross-reference identifiers (xrefs) from the input to the output.
- **FR-007**: The converter MUST work in a streaming fashion, processing records incrementally without loading the entire file into memory.
- **FR-008**: The converter MUST support both conversion directions: 5.5.5 to 7 and 7 to 5.5.5.
- **FR-009**: The converter MUST report parsing errors and conversion warnings through a configurable handler.
- **FR-010**: The converter MUST support both lenient and strict error modes.
- **FR-011**: The converter MUST re-format the input even when the source and target versions are the same (normalization use case).

### Key Entities

- **GedcomConverter**: The main converter that bridges the parser and writer. Accepts an input stream, output stream, and target version configuration. The source version is auto-detected from the input HEAD record.
- **ConversionResult**: Summary of a conversion operation, including record counts and any warnings or errors encountered.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A GEDCOM 5.5.5 file with 100+ records converts to valid GEDCOM 7 with all records preserved and correct formatting.
- **SC-002**: A GEDCOM 7 file with 100+ records converts to valid GEDCOM 5.5.5 with all records preserved, correct HEAD.CHAR, CONC-split long lines, and @@-escaped values.
- **SC-003**: Converting a file and converting it back produces output equivalent to the original (round-trip fidelity), excluding formatting differences.
- **SC-004**: Conversion of a 10,000-record file completes without loading the entire file into memory (streaming).
- **SC-005**: All cross-reference identifiers in the output match those in the input exactly.
- **SC-006**: Conversion errors and warnings are reported to the caller for 100% of problematic structures encountered.

## Assumptions

- The parser's auto-detect mode can determine the source version from the HEAD record, so the caller does not need to specify the source version explicitly.
- The converter does not restructure FAMS/FAMC and family record pointer patterns. Both patterns are valid in both versions; the converter preserves the input structure as-is.
- Calendar escape prefixes (e.g., `@#DJULIAN@`) are handled correctly by the existing parser and writer, so the converter does not need special handling for these.
- The converter does not validate the semantic correctness of the GEDCOM data (e.g., whether a DATE value is a valid date). It focuses on format conversion only.
- Extension tags (prefixed with _) are passed through unchanged, including any HEAD.SCHMA definitions.

## Scope

### In Scope

- Bidirectional format conversion between GEDCOM 5.5.5 and GEDCOM 7
- HEAD record adaptation (VERS, CHAR, FORM)
- @-escaping conversion
- Line length and CONC/CONT handling
- Streaming conversion (bounded memory)
- Xref preservation
- Error and warning reporting
- Extension tag passthrough

### Out of Scope

- Semantic validation of GEDCOM data (date formats, name formats, etc.)
- Restructuring individual-centric (FAMS/FAMC) to family-centric (HUSB/WIFE/CHIL) patterns or vice versa
- Merging or deduplication of records
- GUI or command-line interface (this is a library API)
- Conversion to/from non-GEDCOM formats (e.g., GEDCOM-X, GRAMPS)
- Support for GEDCOM versions other than 5.5.5 and 7.0
