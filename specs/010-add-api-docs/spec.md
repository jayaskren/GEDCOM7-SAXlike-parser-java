# Feature Specification: Add API Documentation

**Feature Branch**: `010-add-api-docs`
**Created**: 2026-03-07
**Status**: Draft
**Input**: User description: "Add tutorial sections for Writer, Converter, and Gedzip APIs plus update summary table and carry over documentation improvements from previous branch"

## User Scenarios & Testing

### User Story 1 - Writer API Tutorial (Priority: P1)

A developer wants to generate GEDCOM output from their application. They open the tutorial looking for guidance on the Writer API but find no documentation. They need a step-by-step walkthrough showing how to create GEDCOM files using `GedcomWriter`.

**Why this priority**: The Writer is one of the three core capabilities (parse, write, convert) and currently has zero tutorial coverage despite being a fully implemented feature.

**Independent Test**: Can be verified by confirming the tutorial contains a Writer section with working code examples that cover record creation, substructure nesting, and configuration for both GEDCOM 7 and 5.5.5.

**Acceptance Scenarios**:

1. **Given** a developer reads the tutorial, **When** they reach the Writer section, **Then** they find a complete example showing how to write GEDCOM records (HEAD, INDI, FAM) with nested substructures
2. **Given** a developer wants to write GEDCOM 5.5.5, **When** they check the Writer section, **Then** they find configuration guidance for version-specific output
3. **Given** the tutorial summary table, **When** a developer scans for Writer entries, **Then** they find rows for `GedcomWriter`, `GedcomWriterConfig`, and version-specific factory methods

---

### User Story 2 - Converter API Tutorial (Priority: P2)

A developer needs to convert GEDCOM files between version 5.5.5 and 7.0. They look for conversion guidance in the tutorial but find nothing. They need a concise example showing the one-liner conversion API.

**Why this priority**: Conversion between versions is a common real-world need and the API is simple, making documentation quick to add.

**Independent Test**: Can be verified by confirming the tutorial contains a Converter section with examples for both conversion directions (5.5.5 to 7 and 7 to 5.5.5), plus how to check conversion results.

**Acceptance Scenarios**:

1. **Given** a developer reads the tutorial, **When** they reach the Converter section, **Then** they find examples for converting in both directions
2. **Given** a developer wants to check conversion quality, **When** they read the Converter section, **Then** they learn how to inspect `ConversionResult` for warnings and line counts

---

### User Story 3 - Gedzip Support Tutorial (Priority: P3)

A developer receives a `.gdz` (Gedzip) archive and needs to parse the GEDCOM content inside it. They need documentation on the `GedzipReader` class.

**Why this priority**: Gedzip is a less common format but is part of the GEDCOM 7 specification and the library supports it. Users who encounter `.gdz` files have no tutorial guidance.

**Independent Test**: Can be verified by confirming the tutorial contains a Gedzip section showing how to open a `.gdz` file, list entries, and parse the GEDCOM content.

**Acceptance Scenarios**:

1. **Given** a developer has a `.gdz` file, **When** they read the Gedzip section, **Then** they find examples for opening the archive, listing entries, and parsing the main GEDCOM stream
2. **Given** the tutorial summary table, **When** a developer looks for Gedzip, **Then** they find a row for `GedzipReader`

---

### User Story 4 - Updated Summary Table (Priority: P1)

A developer uses the tutorial summary table as a quick-reference card. Currently it only covers parsing. It needs to include Writer, Converter, Gedzip, 5.5.5 configuration, and common substructure constants.

**Why this priority**: The summary table is the most-consulted section for returning users. It must reflect all library capabilities.

**Independent Test**: Can be verified by confirming the summary table includes rows for all library capabilities: parsing (both versions), writing, converting, Gedzip, tag/value constants (including common substructure constants), data types, error handling, and extension URIs.

**Acceptance Scenarios**:

1. **Given** the tutorial summary table, **When** a developer scans it, **Then** every major library capability has at least one row
2. **Given** the existing summary entries, **When** the table is updated, **Then** no existing entries are removed or broken

---

### Edge Cases

- What happens if code examples reference classes the reader hasn't seen before? Each new section should be self-contained with necessary imports.
- What if the tutorial becomes too long? Keep new sections concise -- favor minimal working examples over exhaustive coverage.
- The carried-over documentation improvements (README, architecture, tutorial version-differences sections) must not be lost when merging.

## Requirements

### Functional Requirements

- **FR-001**: Tutorial MUST contain a Writer API section with at least one complete code example showing record creation and substructure nesting
- **FR-002**: Tutorial MUST contain a Converter API section with examples for both 5.5.5-to-7 and 7-to-5.5.5 conversion
- **FR-003**: Tutorial MUST contain a Gedzip section showing how to open, list, and parse `.gdz` archives
- **FR-004**: Tutorial summary table MUST include rows for Writer, Converter, Gedzip, 5.5.5 configs, and common substructure constants
- **FR-005**: All code examples MUST include necessary import statements and be self-contained
- **FR-006**: Documentation improvements already made to README.md, architecture.md, and tutorial.md (version differences, 5.5.5 support phrasing) MUST be preserved in the final deliverable
- **FR-007**: Each new tutorial section MUST follow the existing step-numbered format (Step 11, Step 12, etc.)

## Success Criteria

### Measurable Outcomes

- **SC-001**: 100% of library capabilities (parse, write, convert, Gedzip) have dedicated tutorial sections
- **SC-002**: Summary table covers all major classes: GedcomReader, GedcomWriter, GedcomConverter, GedzipReader, GedcomReaderConfig (all factory methods), GedcomWriterConfig, GedcomConverterConfig
- **SC-003**: Every code example compiles and runs against the current library API (verifiable by inspection)
- **SC-004**: All three previously-modified documentation files (README.md, architecture.md, tutorial.md) retain their improvements in the final branch

## Assumptions

- The Writer uses a lambda-scoped context pattern that should be documented as-is
- The Converter is a static utility -- examples should be concise
- GedzipReader provides methods for opening archives and extracting GEDCOM streams
- New tutorial sections should maintain the same tone and depth as existing steps
- No new test files or source code changes are needed -- this is documentation-only
