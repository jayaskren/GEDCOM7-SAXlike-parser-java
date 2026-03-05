# Feature Specification: GEDZip Support and GEDCOM 5.5.5 Compatibility

**Feature Branch**: `002-gedzip-gedcom555`
**Created**: 2026-03-05
**Status**: Draft
**Input**: User description: "Add support for Gedzip. Research how it is used so we can integrate it. Additionally, support GEDCOM 5.5.5 while also supporting GEDCOM 7."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Parse GEDCOM 5.5.5 Files (Priority: P1)

A developer has a collection of `.ged` files exported from various genealogy applications over the years. Many are in GEDCOM 5.5.5 format. The developer wants to use the same parser library and callback API to read both GEDCOM 5.5.5 and GEDCOM 7.0 files without writing separate parsing logic.

**Why this priority**: GEDCOM 5.5.5 files vastly outnumber GEDCOM 7 files in the wild. Supporting them makes this library useful for real-world genealogy applications.

**Independent Test**: Can be fully tested by parsing GEDCOM 5.5.5 sample files and verifying that the same `GedcomHandler` callbacks fire with correct data.

**Acceptance Scenarios**:

1. **Given** a GEDCOM 5.5.5 file with `HEAD.GEDC.VERS 5.5.5` and `HEAD.CHAR UTF-8`, **When** the parser is configured for GEDCOM 5.5.5, **Then** all records and structures fire correct handler events, and CONC lines are joined without whitespace or newlines.
2. **Given** a GEDCOM 5.5.5 file encoded in UTF-16 (BOM present, `HEAD.CHAR UNICODE`), **When** the parser reads it, **Then** the text is decoded correctly and handler callbacks receive proper Unicode strings.
3. **Given** a GEDCOM 5.5.5 file with `@@` sequences in text values, **When** the parser reads them, **Then** every `@@` is decoded to a single `@` (not just leading occurrences).
4. **Given** a GEDCOM 5.5.5 file with `CONC` substructures splitting a long value, **When** the parser assembles the payload, **Then** the resulting value is the direct concatenation of the parts (no added whitespace or newlines between CONC segments).
5. **Given** a GEDCOM 5.5.5 file with both `CONT` and `CONC` substructures, **When** the parser assembles the payload, **Then** `CONT` joins with a newline and `CONC` joins without one.

---

### User Story 2 - Auto-Detect GEDCOM Version (Priority: P1)

A developer wants the parser to automatically detect whether a file is GEDCOM 7.0 or GEDCOM 5.5.5 and apply the correct parsing rules, without the developer needing to specify the version in advance.

**Why this priority**: Users should not need to inspect files manually before parsing. Auto-detection is essential for any practical genealogy application that imports user-provided files.

**Independent Test**: Can be tested by feeding both GEDCOM 7 and GEDCOM 5.5.5 files to the auto-detecting configuration and verifying correct behavior for each.

**Acceptance Scenarios**:

1. **Given** a file with `HEAD.GEDC.VERS 7.0`, **When** parsed with auto-detect configuration, **Then** GEDCOM 7 rules are applied (CONT only, leading-only `@@` decode, UTF-8).
2. **Given** a file with `HEAD.GEDC.VERS 5.5.5`, **When** parsed with auto-detect configuration, **Then** GEDCOM 5.5.5 rules are applied (CONT + CONC, all `@@` decoded, UTF-8/UTF-16 encoding).
3. **Given** a file with `HEAD.GEDC.VERS 5.5.1`, **When** parsed with auto-detect, **Then** a warning is issued indicating that 5.5.1 is not fully supported, and the parser applies 5.5.5 rules as a best-effort fallback.

---

### User Story 3 - Open GEDZip Archives (Priority: P2)

A developer receives a `.gdz` file containing a GEDCOM dataset along with photos and documents. The developer wants to parse the genealogical data from the archive and access the included media files.

**Why this priority**: GEDZip is the official packaging format for GEDCOM 7.0 and enables self-contained archives with media. It is less urgent than text parsing because most existing tools still exchange plain `.ged` files.

**Independent Test**: Can be tested by creating a `.gdz` file containing a `gedcom.ged` and referenced media files, then using the parser to read the GEDCOM data and list/access the media.

**Acceptance Scenarios**:

1. **Given** a valid `.gdz` file containing `gedcom.ged` at the archive root, **When** the parser opens it, **Then** the GEDCOM data is parsed and all handler callbacks fire correctly.
2. **Given** a `.gdz` file where `gedcom.ged` contains `FILE photo.jpg` referencing a local file, **When** the developer queries the archive for `photo.jpg`, **Then** an `InputStream` to that file is returned.
3. **Given** a `.gdz` file where `gedcom.ged` contains `FILE https://example.com/photo.jpg` (external URL), **When** the developer queries the archive, **Then** the file is identified as external (not in the archive) and no error is raised.
4. **Given** a ZIP file that does not contain a `gedcom.ged` entry, **When** the parser tries to open it, **Then** a clear error is reported indicating the archive is not a valid GEDZip file.

---

### User Story 4 - Unified Handler API Across Versions (Priority: P1)

A developer writing a genealogy application wants to use one `GedcomHandler` implementation that works for both GEDCOM 5.5.5 and GEDCOM 7.0 files. The handler should receive the same types of events regardless of the source file version.

**Why this priority**: A unified API is the core value proposition. If different handler implementations were needed per version, the library would offer little advantage over separate parsers.

**Independent Test**: Can be tested by implementing a single handler that extracts individuals and families, then parsing both GEDCOM 5.5.5 and GEDCOM 7.0 files and verifying equivalent results.

**Acceptance Scenarios**:

1. **Given** a handler that records `startRecord` and `startStructure` events, **When** parsing a GEDCOM 5.5.5 file and a GEDCOM 7.0 file with equivalent data, **Then** the handler receives equivalent event sequences for both.
2. **Given** a handler that checks `GedcomHeaderInfo.getVersion()`, **When** parsing a 5.5.5 file, **Then** the version reports major=5, minor=5, patch=5.
3. **Given** a handler that receives `startDocument(GedcomHeaderInfo)`, **When** parsing a 5.5.5 file, **Then** the header info includes source system, source version, source name, and character encoding information.

---

### User Story 5 - GEDCOM 5.5.5 Validation (Priority: P3)

A developer building a GEDCOM validator wants the parser to detect and report 5.5.5-specific violations such as lines exceeding 255 characters, invalid cross-reference formats, and missing mandatory structures.

**Why this priority**: Validation is a secondary concern; most users want to read data, not validate it. But it enables quality-checking tools.

**Independent Test**: Can be tested by feeding intentionally malformed 5.5.5 files and verifying the correct warnings and errors are reported.

**Acceptance Scenarios**:

1. **Given** a 5.5.5 file with a line exceeding 255 characters, **When** parsed in strict mode, **Then** a fatal error is reported.
2. **Given** a 5.5.5 file with a cross-reference identifier longer than 22 characters, **When** parsed, **Then** a warning is issued.
3. **Given** a 5.5.5 file missing the mandatory BOM, **When** parsed in strict mode, **Then** a fatal error is reported.
4. **Given** a 5.5.5 file with a bare single `@` in a text value (not doubled), **When** parsed, **Then** an error is reported.

---

### Edge Cases

- What happens when a `.gdz` file contains nested ZIP archives?
- How does the parser handle a GEDCOM 5.5.5 file with no `HEAD.CHAR` tag (encoding unknown)? → Warn and assume UTF-8; use BOM if present to detect UTF-16.
- What happens when a CONC line has an empty value in a 5.5.5 file?
- How does the parser handle UTF-16 files without a BOM?
- What happens when a 5.5.5 file uses LDS-specific tags that were obsoleted (BAPL, CONL, etc.)?
- How does the parser handle `@#DJULIAN@` calendar escape sequences in 5.5.5 date values?
- What happens when a `.gdz` file references a local file that is not present in the archive? → The API returns null; the caller is responsible for checking.

## Requirements *(mandatory)*

### Functional Requirements

#### GEDCOM 5.5.5 Parsing

- **FR-001**: The parser MUST support GEDCOM 5.5.5 files by selecting the appropriate parsing strategies (CONC handling, `@@` decoding, encoding detection) based on the detected or configured version.
- **FR-002**: The parser MUST recognize `CONC` as a pseudo-structure in GEDCOM 5.5.5 mode and concatenate the CONC value directly to the preceding value without any separator.
- **FR-003**: The parser MUST recognize `CONT` as a pseudo-structure in GEDCOM 5.5.5 mode and join with a newline, consistent with GEDCOM 7 behavior.
- **FR-004**: The parser MUST decode every `@@` occurrence in text values to a single `@` when parsing GEDCOM 5.5.5 (not just leading `@@` as in GEDCOM 7).
- **FR-005**: The parser MUST support UTF-8 encoded GEDCOM 5.5.5 files.
- **FR-006**: The parser MUST support UTF-16 (both LE and BE) encoded GEDCOM 5.5.5 files, detected via BOM.
- **FR-007**: The parser MUST detect and strip the Byte Order Mark (BOM) from both UTF-8 and UTF-16 encoded files.
- **FR-008**: The parser MUST read `HEAD.CHAR` to determine encoding in GEDCOM 5.5.5 files (`UTF-8` or `UNICODE` for UTF-16). If `HEAD.CHAR` is missing, the parser MUST issue a warning and assume UTF-8 encoding, using BOM detection as a fallback for UTF-16.
- **FR-009**: The parser MUST fire the same handler callback events (`startDocument`, `startRecord`, `startStructure`, etc.) for GEDCOM 5.5.5 files as for GEDCOM 7 files.
- **FR-010**: The `GedcomHeaderInfo` delivered via `startDocument` MUST include the detected version (5.5.5), source system, source version, source name, and default language when parsing 5.5.5 files.

#### Version Auto-Detection

- **FR-011**: The parser MUST provide an auto-detect configuration that reads `HEAD.GEDC.VERS` and selects the appropriate parsing rules (GEDCOM 7 or GEDCOM 5.5.5).
- **FR-012**: When auto-detect encounters a version it cannot fully support (e.g., 5.5.1), the parser MUST issue a warning and apply the closest supported version's rules as a best effort.
- **FR-013**: The parser MUST provide explicit factory methods for each supported version (e.g., for GEDCOM 7, for GEDCOM 5.5.5, and for auto-detect).

#### GEDCOM 5.5.5 Validation

- **FR-014**: In GEDCOM 5.5.5 strict mode, the parser MUST enforce a maximum line length of 255 characters and report a fatal error if exceeded.
- **FR-015**: In GEDCOM 5.5.5 mode, the parser MUST report a warning if a cross-reference identifier exceeds 22 characters (including the enclosing `@` signs).
- **FR-016**: In GEDCOM 5.5.5 strict mode, the parser MUST report a fatal error if the file lacks a BOM.
- **FR-017**: In GEDCOM 5.5.5 mode, the parser MUST report an error if a bare single `@` appears in a text value where `@@` was expected.
- **FR-018**: The parser MUST validate that level numbers do not increase by more than 1 from one line to the next (consistent with existing GEDCOM 7 validation).

#### GEDZip Support

- **FR-019**: The parser MUST accept a `.gdz` file (ZIP archive) as input and locate the `gedcom.ged` entry for parsing. The parser MUST auto-detect the GEDCOM version of the contained `gedcom.ged` file using the same version detection logic as for standalone files.
- **FR-020**: The parser MUST report a clear error if a provided ZIP archive does not contain a `gedcom.ged` entry.
- **FR-021**: The parser MUST provide a way for the caller to access other entries in the GEDZip archive (e.g., media files referenced by `FILE` paths) as input streams. If the requested entry does not exist in the archive, the method MUST return null.
- **FR-022**: The parser MUST distinguish between local file references (relative paths within the archive) and external URL references in `FILE` structures.
- **FR-023**: The parser MUST support GEDZip files where filenames use UTF-8 encoding and are case-sensitive.
- **FR-024**: The GEDZip reader MUST work with standard ZIP archives conforming to ISO/IEC 21320-1:2015.
- **FR-025**: The GEDZip reader MUST handle percent-encoded filenames in `FILE` path values when matching them to ZIP archive entries.

#### Backward Compatibility

- **FR-026**: All existing GEDCOM 7 parsing behavior MUST remain unchanged. Existing handler implementations MUST work without modification.
- **FR-027**: The existing GEDCOM 7 configuration factory methods MUST continue to function identically.

### Key Entities

- **GedcomReaderConfig**: Extended with new factory methods for GEDCOM 5.5.5 and auto-detect, plus version-aware strategy selection.
- **GedcomHeaderInfo**: Extended to carry encoding information from `HEAD.CHAR` for 5.5.5 files.
- **GedzipReader**: A new entry point that wraps a ZIP archive, extracts `gedcom.ged`, and provides access to other archive entries.
- **PayloadAssembler (CONC+CONT variant)**: A new strategy implementation that handles both CONT (newline join) and CONC (direct concatenation) for 5.5.5.
- **AtEscapeStrategy (all-@@ variant)**: A new strategy implementation that decodes all `@@` occurrences (not just leading) for 5.5.5.
- **GedcomInputDecoder (UTF-16 variant)**: A new strategy implementation that detects UTF-8 vs UTF-16 via BOM and HEAD.CHAR for 5.5.5.

## Clarifications

### Session 2026-03-05

- Q: Should GedzipReader always apply GEDCOM 7 rules or auto-detect the version inside the archive? → A: Auto-detect inside archive — apply version detection to the contained `gedcom.ged`.
- Q: How should the parser handle a GEDCOM 5.5.5 file with no HEAD.CHAR tag? → A: Warn and assume UTF-8 — issue a warning, fall back to UTF-8, use BOM if present to detect UTF-16.
- Q: When a local file referenced in a GEDZip archive is not present, what should the API return? → A: Return null — caller checks for null before using the stream.

## Assumptions

- GEDCOM 5.5.1 files will be handled on a best-effort basis using 5.5.5 rules, with a warning. Full ANSEL encoding support is out of scope for this feature.
- Calendar escape sequences (`@#DJULIAN@`, etc.) in 5.5.5 date values will be passed through to the handler as-is. Parsing them into structured date objects is out of scope.
- The library will not write or create GEDCOM or GEDZip files -- it is read-only.
- GEDZip encryption support is out of scope. Encrypted entries will cause an error.
- The `.gdz` file will be read from a `File`, `Path`, or seekable source, not from a non-seekable input stream (ZIP archives require random access).
- Obsoleted LDS-specific tags (BAPL, CONL, ENDL, etc.) will be parsed as normal structures without special handling; no errors or warnings will be raised for their presence.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Developers can parse both GEDCOM 5.5.5 and GEDCOM 7.0 files using the same handler implementation, receiving equivalent events for equivalent data.
- **SC-002**: The auto-detect configuration correctly identifies and parses files of both versions without developer intervention for at least 95% of real-world GEDCOM files.
- **SC-003**: GEDCOM 5.5.5 files with CONC substructures produce identical assembled text as a reference implementation.
- **SC-004**: GEDZip archives are opened and parsed within the same time as parsing the contained `.ged` file directly, plus overhead of no more than 100 milliseconds for archives under 10 MB.
- **SC-005**: All existing GEDCOM 7 tests continue to pass without modification after adding 5.5.5 and GEDZip support.
- **SC-006**: The library adds zero new runtime dependencies (ZIP handling uses the standard library).
