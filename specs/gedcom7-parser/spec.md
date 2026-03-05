# Feature Specification: GEDCOM 7 SAX-like Streaming Parser

**Feature Branch**: `gedcom7-parser`
**Created**: 2026-03-04
**Status**: Draft
**Input**: GEDCOM 7.0 specification (https://gedcom.io, https://github.com/FamilySearch/GEDCOM)

## User Scenarios & Testing

### User Story 1 - Parse a GEDCOM 7 File Line-by-Line (Priority: P1)

As a Java developer, I want to feed a GEDCOM 7 file to a
streaming parser and receive SAX-like callback events for
each structural element so that I can process arbitrarily
large GEDCOM files without loading them entirely into memory.

**Why this priority**: This is the core value proposition.
Without streaming line-level parsing, nothing else works.

**Independent Test**: Parse a minimal valid GEDCOM 7 file
(`HEAD` + `GEDC` + `VERS 7.0` + `TRLR`) and verify the
correct sequence of events fires.

**Acceptance Scenarios**:

1. **Given** a valid GEDCOM 7 file with BOM, **When** parsed,
   **Then** the parser strips the BOM, emits `startDocument`,
   events for HEAD and its substructures, events for each
   record, and `endDocument` after TRLR.

2. **Given** a GEDCOM 7 file with CRLF line endings,
   **When** parsed, **Then** all lines are correctly
   tokenized regardless of line ending style (CR, LF, CRLF).

3. **Given** a GEDCOM 7 file with 1 million INDI records,
   **When** parsed, **Then** the parser delivers events
   incrementally without requiring heap proportional to
   file size.

4. **Given** a line `0 @I1@ INDI`, **When** parsed,
   **Then** a `startRecord` event fires with level=0,
   xref="@I1@", tag="INDI".

5. **Given** a structure with CONT continuation lines,
   **When** parsed, **Then** the payload delivered in the
   event contains the reconstructed multi-line string with
   newlines where CONT lines appeared.

---

### User Story 2 - Receive Properly Nested Start/End Events (Priority: P1)

As a Java developer, I want the parser to emit properly
nested `startRecord`/`endRecord` and
`startStructure`/`endStructure` events based on GEDCOM level
numbers so that I can track hierarchical context without
managing a level stack myself.

**Why this priority**: Correct nesting is fundamental to the
SAX-like contract. Without it, consumers cannot determine
parent-child relationships.

**Independent Test**: Parse a file with nested structures
(e.g., INDI with BIRT containing DATE and PLAC) and verify
that end events fire in correct reverse order when the level
decreases.

**Acceptance Scenarios**:

1. **Given** the input:
   ```
   0 @I1@ INDI
   1 BIRT
   2 DATE 6 APR 1952
   2 PLAC London
   1 DEAT Y
   0 TRLR
   ```
   **When** parsed, **Then** events fire in order:
   startRecord(INDI) -> startStructure(BIRT) ->
   startStructure(DATE, "6 APR 1952") ->
   endStructure(DATE) -> startStructure(PLAC, "London") ->
   endStructure(PLAC) -> endStructure(BIRT) ->
   startStructure(DEAT, "Y") -> endStructure(DEAT) ->
   endRecord(INDI).

2. **Given** a level jump from 1 to 3 (skipping level 2),
   **When** parsed, **Then** an error event fires indicating
   an invalid level increment.

---

### User Story 3 - Handle All GEDCOM 7 Character Encoding Rules (Priority: P1)

As a Java developer, I want the parser to enforce GEDCOM 7
UTF-8 encoding rules so that I receive clean, valid text
in all callbacks and malformed input is reported as errors.

**Why this priority**: Encoding correctness is non-negotiable
for a spec-compliant parser. Garbage-in must not silently
become garbage-out.

**Independent Test**: Feed the parser a byte stream containing
banned characters and verify error events fire with byte
offsets.

**Acceptance Scenarios**:

1. **Given** a file starting with UTF-8 BOM (EF BB BF),
   **When** parsed, **Then** the BOM is consumed silently
   and not included in any event payload.

2. **Given** a file without BOM, **When** parsed, **Then**
   parsing proceeds normally.

3. **Given** a file containing U+0007 (BEL) in a line value,
   **When** parsed, **Then** an error event fires identifying
   the banned character and its byte offset.

4. **Given** a file with valid multi-byte UTF-8 sequences
   (e.g., CJK characters), **When** parsed, **Then** all
   characters are correctly decoded and delivered in payloads.

---

### User Story 4 - Parse Cross-References and Pointers (Priority: P1)

As a Java developer, I want the parser to distinguish pointer
values from string values and to track cross-reference
identifiers so that I can resolve record linkages.

**Why this priority**: Pointers are the primary mechanism for
linking INDI to FAM, SOUR to citations, etc. Without pointer
support the data model is flat and unusable.

**Independent Test**: Parse a file with INDI and FAM records
linked by FAMS/FAMC pointers. Verify pointer events carry
the target xref string, and that unresolved references are
reported at end of document.

**Acceptance Scenarios**:

1. **Given** `1 FAMC @F1@`, **When** parsed, **Then** the
   event for this structure indicates a pointer value
   targeting "@F1@".

2. **Given** `1 NOTE @VOID@`, **When** parsed, **Then**
   the event indicates a void pointer (null reference).

3. **Given** a pointer `@F99@` that has no corresponding
   record with xref `@F99@`, **When** parsing completes,
   **Then** an error/warning event reports the unresolved
   reference.

4. **Given** two records both with xref `@I1@`, **When**
   parsed, **Then** an error event reports the duplicate
   cross-reference identifier.

---

### User Story 5 - Handle Extension Tags and SCHMA (Priority: P2)

As a Java developer, I want the parser to correctly handle
extension tags (underscore-prefixed) and resolve documented
extensions via HEAD.SCHMA.TAG so that I can process GEDCOM
files from any producing software without errors.

**Why this priority**: Extension tags are ubiquitous in
real-world GEDCOM files. A parser that rejects or
mishandles them is not practically useful.

**Independent Test**: Parse a file containing both documented
(SCHMA-mapped) and undocumented extension tags. Verify events
include the URI for documented extensions.

**Acceptance Scenarios**:

1. **Given** a HEAD with:
   ```
   1 SCHMA
   2 TAG _CUSTOM https://example.com/custom
   ```
   and a record containing `1 _CUSTOM some value`,
   **When** parsed, **Then** the structure event for
   `_CUSTOM` includes the URI `https://example.com/custom`.

2. **Given** an undocumented extension tag `_UNKN`,
   **When** parsed, **Then** the parser emits a normal
   structure event with tag="_UNKN" and no URI, without
   error.

3. **Given** extension tags used as record-level tags
   (`0 @X1@ _MYRECORD`), **When** parsed, **Then** events
   fire as startRecord/endRecord with the extension tag.

---

### User Story 6 - Validate HEAD and TRLR Structure (Priority: P2)

As a Java developer, I want the parser to validate that the
dataset begins with HEAD (containing GEDC.VERS) and ends with
TRLR so that malformed or non-GEDCOM files are detected early.

**Why this priority**: HEAD/TRLR validation catches corrupt
files and version mismatches early, before wasting resources.

**Independent Test**: Feed the parser files missing HEAD,
missing TRLR, and with non-7.x VERS values. Verify
appropriate error events.

**Acceptance Scenarios**:

1. **Given** a file whose first record is not HEAD,
   **When** parsing begins, **Then** a fatal error event
   fires.

2. **Given** a file with no TRLR at end-of-stream,
   **When** parsing completes, **Then** an error event
   fires reporting missing trailer.

3. **Given** `HEAD.GEDC.VERS` with value "5.5.1",
   **When** parsed, **Then** a warning or error event
   fires indicating unsupported major version.

4. **Given** `HEAD.GEDC.VERS` with value "7.0",
   **When** parsed, **Then** parsing proceeds normally.

5. **Given** `HEAD.GEDC.VERS` with value "7.1",
   **When** parsed, **Then** parsing proceeds normally
   (forward-compatible per spec recommendation).

---

### User Story 7 - Error Handling and Recovery (Priority: P2)

As a Java developer, I want the parser to report errors with
rich context (line number, byte offset, raw line content) and
to optionally continue parsing after non-fatal errors so that
I can process imperfect real-world files.

**Why this priority**: Real GEDCOM files from various software
often contain minor spec violations. A parser that aborts on
the first error is not practically useful.

**Independent Test**: Feed the parser a file with several
malformed lines interspersed with valid lines. Verify errors
are reported with correct context and valid lines still
produce events.

**Acceptance Scenarios**:

1. **Given** a malformed line (e.g., missing tag),
   **When** parsed in lenient mode, **Then** an error
   event fires with line number, byte offset, and raw line
   content, and parsing continues with the next line.

2. **Given** the same malformed line, **When** parsed in
   strict mode, **Then** parsing stops immediately after
   the error event.

3. **Given** a file with 1000 valid lines and 1 malformed
   line at line 500, **When** parsed in lenient mode,
   **Then** all 999 valid lines produce correct events
   and the error is reported for line 500.

---

### User Story 8 - Parse GEDCOM 7 Data Type Payloads (Priority: P3)

As a Java developer, I want companion utility classes that can
parse GEDCOM 7 payload strings into typed Java objects (dates,
ages, coordinates, personal names, etc.) so that I do not have
to implement the GEDCOM data type grammar myself.

**Why this priority**: Data type parsing is a convenience
layer. The core parser delivers raw strings; these utilities
add value but are not required for basic parsing.

**Independent Test**: Call each data type parser with valid and
invalid inputs. Verify correct parsing and error reporting.

**Acceptance Scenarios**:

1. **Given** the date string "6 APR 1952", **When** parsed
   as a DateValue, **Then** a GedcomDate object is returned
   with calendar=GREGORIAN, day=6, month=APR, year=1952.

2. **Given** "BET 1 JAN 1900 AND 31 DEC 1999", **When**
   parsed as a DateValue, **Then** a date range is returned
   with both boundary dates.

3. **Given** "FROM FRENCH_R 1 VEND 1 TO 30 COMP 14",
   **When** parsed as a DatePeriod, **Then** the French
   Republican calendar dates are correctly parsed.

4. **Given** "HEBREW 15 SVN 5765", **When** parsed as a
   DateValue, **Then** the Hebrew calendar date is returned.

5. **Given** "> 25y 3m", **When** parsed as an Age,
   **Then** a GedcomAge is returned with bound=">",
   years=25, months=3.

6. **Given** "John /Smith/ Jr.", **When** parsed as a
   PersonalName, **Then** the surname "Smith" is extracted
   from within the `/` delimiters.

7. **Given** "N50.9375", **When** parsed as Latitude,
   **Then** direction=N, degrees=50.9375 is returned.

8. **Given** "12:30:00Z", **When** parsed as Time,
   **Then** hour=12, minute=30, second=0, utc=true.

9. **Given** an invalid integer "-5", **When** parsed as
   Integer, **Then** a parse error is returned (GEDCOM
   integers are non-negative).

10. **Given** "_CUSTOM https://example.com/ext", **When**
    parsed as TagDef, **Then** tag="_CUSTOM" and
    uri="https://example.com/ext" are returned.

---

### Edge Cases

- Empty payload vs. absent payload (considered equivalent
  per spec)
- Line value starting with `@` that is not a valid pointer
  (must be `@@`-escaped or is malformed)
- `@@` at start of CONT line value (must decode to single `@`)
- CONT line with no line value (represents a blank line in
  the multi-line payload)
- Level 0 line with no xref (valid for HEAD, TRLR; may be
  valid for extension records)
- Maximum level depth (no limit in GEDCOM 7)
- Extremely long line values (thousands of characters)
- File containing only HEAD + TRLR (valid but empty dataset)
- Multiple spaces between line components (second space is
  part of the line value per spec)
- Tag `CONC` appearing in input (in GEDCOM 7 mode: treated
  as an ordinary unknown tag, not as continuation. In future
  GEDCOM 5.5.5 mode: treated as concatenation pseudo-
  structure per the PayloadAssembler strategy)
- `@VOID@` appearing as a line value (null pointer)
- Extension tag at level 0 as a record tag
- BOM appearing mid-file (should be treated as character
  U+FEFF, not stripped)
- Mixed line endings within a single file (each line may
  differ)
- Lowercase letters in xref identifiers (valid in 5.5.5,
  invalid in 7.0; permissive parsing should accept them)
- Lowercase letters in tags (valid in 5.5.5, invalid in 7.0;
  lenient mode should accept them)
- `HEAD.CHAR` present in a GEDCOM 7 file (ignore gracefully;
  required in 5.5.5)
- `HEAD.GEDC.FORM` present in a GEDCOM 7 file (ignore
  gracefully; required in 5.5.5)
- Lines exceeding 255 characters (valid in 7.0; invalid in
  strict 5.5.5 mode)

## Requirements

### Functional Requirements

#### Line Parsing and Tokenization

- **FR-001**: The line tokenizer MUST accept a stream of
  decoded characters and tokenize it into GEDCOM lines per
  the ABNF: `Line = Level D [Xref D] Tag [D LineVal] EOL`.
  For GEDCOM 7, the default input adapter reads UTF-8 bytes
  and decodes them. The input decoding layer MUST be a
  separate component from the line tokenizer so that
  alternative decoders (e.g., UTF-16, ANSEL for future
  GEDCOM 5.5.5 support) can be substituted without modifying
  the tokenizer.
- **FR-002**: The parser MUST accept all three EOL forms:
  CR (`%x0D`), LF (`%x0A`), and CRLF (`%x0D %x0A`)
- **FR-003**: The parser MUST parse `Level` as a non-negative
  integer (`"0" / nonzero *DIGIT`)
- **FR-004**: The parser MUST parse `Xref` as
  `atsign 1*tagchar atsign` (excluding `@VOID@`) and
  recognize it as a cross-reference identifier. The xref
  character validation MUST be configurable: GEDCOM 7 allows
  `[A-Z0-9_]`; a future GEDCOM 5.5.5 mode will need
  `[A-Za-z0-9]`. The tokenizer SHOULD use a permissive
  superset (`[A-Za-z0-9_]`) for parsing and defer strict
  validation to a version-aware validation layer.
- **FR-005**: The parser MUST parse `Tag` as either a
  standard tag (`ucletter *tagchar`) or extension tag
  (`underscore 1*tagchar`). In lenient mode, the parser
  SHOULD also accept lowercase letters in tags for
  compatibility with GEDCOM 5.5.5 files.
- **FR-006**: The parser MUST distinguish pointer line values
  (matching `Xref` or `@VOID@`) from string line values
  (matching `lineStr`)
- **FR-007**: The parser MUST decode the `@@` escape via a
  configurable strategy. The default GEDCOM 7 strategy: only
  a leading `@@` in a `lineStr` is decoded (first `@`
  removed). The interface MUST allow a future GEDCOM 5.5.5
  strategy where ALL `@@` occurrences in line values are
  decoded.
- **FR-008**: The parser MUST treat the single space (`%x20`)
  between line components as a delimiter. A second space
  after the tag is part of the line value, not a delimiter.
- **FR-009**: The line tokenizer MUST tolerate (with optional
  warning) leading whitespace before the level number. GEDCOM
  7 prohibits this, but GEDCOM 5.5.x permitted it and many
  real-world files include it.

#### Character Encoding

- **FR-010**: The parser MUST accept and silently strip a
  UTF-8 BOM (bytes `EF BB BF`) at the start of the input
  stream
- **FR-011**: The parser MUST NOT require a BOM
- **FR-012**: The parser MUST reject (report as error) any
  occurrence of banned characters:
  - U+0000-U+0008 (C0 controls NUL through BS)
  - U+000B (VT)
  - U+000C (FF)
  - U+000E-U+001F (C0 controls SO through US)
  - U+007F (DEL)
  - U+0080-U+009F (C1 controls)
  - U+D800-U+DFFF (surrogates -- cannot occur in valid UTF-8)
  - U+FFFE-U+FFFF
- **FR-013**: The parser MUST accept U+0009 (TAB) within
  line values
- **FR-014**: The parser MUST correctly decode multi-byte
  UTF-8 sequences (2-byte, 3-byte, and 4-byte)

#### Payload Assembly (CONT / CONC Handling)

- **FR-020**: The parser MUST recognize `CONT` at level N+1
  as a continuation of the structure at level N
- **FR-021**: The parser MUST reconstruct multi-line payloads
  by joining the parent line value, a newline character, and
  each CONT line value in order
- **FR-022**: A CONT line with no line value MUST contribute
  a newline followed by an empty string (blank line)
- **FR-023**: The parser MUST NOT treat CONT as a
  substructure; it is a payload-encoding pseudo-structure
- **FR-024**: The parser MUST apply `@@` decoding to CONT
  line values (a CONT line value starting with `@@` decodes
  to a payload segment starting with `@`)
- **FR-025**: The payload assembly component MUST be designed
  as a pluggable strategy so that CONC support can be added
  for GEDCOM 5.5.5 without modifying the core tokenizer or
  event emitter. In GEDCOM 7 mode, the default strategy
  recognizes only CONT. The strategy interface MUST support
  a future GEDCOM 5.5.5 mode that also recognizes CONC
  (concatenation without newline). In GEDCOM 7 mode, a CONC
  tag MUST be treated as an ordinary (unknown) tag, not as
  a pseudo-structure.

#### Hierarchical Structure

- **FR-030**: The parser MUST interpret level numbers to
  establish parent-child relationships: a line at level N > 0
  is a substructure of the nearest preceding line at level
  N-1
- **FR-031**: Level 0 lines MUST be treated as records (or
  pseudo-records HEAD/TRLR)
- **FR-032**: The parser MUST emit properly nested start/end
  events: when the level decreases from N to M (where M < N),
  end events MUST fire for all levels from N down to M+1 in
  reverse order
- **FR-033**: The parser MUST report an error if the level
  increases by more than 1 from one line to the next (e.g.,
  level 1 followed by level 3)

#### SAX-like Event API

- **FR-040**: The parser MUST expose a callback-based API
  with the following event types:
  - `startDocument(GedcomVersion version)` -- after HEAD is
    parsed, carrying the detected GEDCOM version (major,
    minor) extracted from HEAD.GEDC.VERS. This enables
    consumers to branch on version if needed.
  - `endDocument()` -- after TRLR
  - `startRecord(int level, String xref, String tag)` --
    at each level-0 line (except TRLR)
  - `endRecord(String tag)` -- when a record ends
  - `startStructure(int level, String tag, String value)`
    -- at each non-record, non-CONT line
  - `endStructure(String tag)` -- when a structure's scope
    ends (level decreases or sibling starts)
  - `warning(GedcomParseError error)`
  - `error(GedcomParseError error)`
  - `fatalError(GedcomParseError error)`
- **FR-041**: The parser MUST deliver fully assembled
  payloads (after CONT reconstruction) in structure events,
  not raw line-by-line fragments
- **FR-042**: The parser MUST provide a default no-op handler
  so callers only override events they care about
- **FR-043**: The parser MUST NOT require the caller to
  buffer the entire file; events MUST be delivered as lines
  are read
- **FR-044**: The parser MUST distinguish pointer values from
  string values in events (e.g., via a boolean flag,
  separate callback, or value wrapper)

#### HEAD and TRLR Validation

- **FR-050**: The parser MUST verify that the first record
  in the stream is HEAD; if not, a fatal error MUST fire
- **FR-051**: The parser MUST verify that the last record
  in the stream is TRLR; if TRLR is missing at end-of-input,
  an error MUST fire
- **FR-052**: The parser MUST parse `HEAD.GEDC.VERS` and
  extract the version string. The detected version MUST be
  stored as a `GedcomVersion` value object (major, minor,
  optional patch) and passed to the `startDocument` event.
  In GEDCOM 7 mode, a non-7.x version MUST produce a
  warning or error. The version detection logic MUST be
  designed as the primary branching point for future
  multi-version support: a GEDCOM 5.5.5 adapter would
  detect "5.5.5" here and configure the parser's pluggable
  strategies (encoding, CONC, `@@` escape) accordingly.
- **FR-053**: The parser MUST accept VERS values "7.0",
  "7.1", or any "7.x" (forward-compatible per spec)
- **FR-054**: TRLR MUST NOT have a line value, xref, or
  substructures; violations MUST be reported as errors

#### Cross-Reference Handling

- **FR-060**: The parser MUST track all xref identifiers
  defined on records
- **FR-061**: The parser MUST track all pointer values
  referencing xref identifiers
- **FR-062**: At `endDocument`, the parser MUST report any
  pointer targets that were never defined as xref identifiers
  (unresolved forward references)
- **FR-063**: The parser MUST report duplicate xref
  identifiers as errors
- **FR-064**: The parser MUST recognize `@VOID@` as a null
  pointer and not attempt resolution

#### Extension Tags and SCHMA

- **FR-070**: The parser MUST parse extension tags
  (`_`-prefixed) identically to standard tags at the line
  level
- **FR-071**: The parser MUST parse `HEAD.SCHMA.TAG` lines
  with payload format `extTag SP URI-reference` and build a
  tag-to-URI mapping
- **FR-072**: When an extension tag has a SCHMA mapping, the
  parser MUST make the URI available in the event for that
  structure
- **FR-073**: Extension tags without SCHMA mappings MUST be
  parsed without error

#### Record Type Identification

- **FR-080**: The parser MUST recognize the 7 standard record
  type tags at level 0: `INDI`, `FAM`, `OBJE`, `REPO`,
  `SNOTE`, `SOUR`, `SUBM`
- **FR-081**: The parser MUST recognize `HEAD` and `TRLR`
  as pseudo-record tags at level 0
- **FR-082**: Extension tags at level 0 MUST be accepted as
  extension record types
- **FR-083**: Standard substructure-only tags appearing at
  level 0 (e.g., `DATE` at level 0) SHOULD produce a warning

#### Error Handling

- **FR-090**: All errors MUST include: line number (1-based),
  byte offset from start of stream, error message, and raw
  line content when applicable
- **FR-091**: The parser MUST support two modes:
  - Strict mode: all errors are fatal, parsing stops
  - Lenient mode: non-fatal errors are reported and parsing
    continues from the next line
- **FR-092**: Fatal errors (missing HEAD, invalid UTF-8 byte
  sequences) MUST always stop parsing regardless of mode
- **FR-093**: The parser MUST provide a
  `GedcomParseError` value class with severity, line number,
  byte offset, message, and raw content fields

#### Data Type Parsing Utilities

- **FR-100**: A companion `GedcomDataTypes` class MUST
  provide static methods to parse payload strings into
  typed Java objects for each GEDCOM 7 data type
- **FR-101**: `parseInteger(String)` MUST parse non-negative
  base-10 integers per the `Integer` ABNF production and
  reject negative values, leading zeros (except "0" itself),
  and non-digit characters
- **FR-102**: `parseDateValue(String)` MUST parse the full
  `DateValue` ABNF production including:
  - Plain dates with optional calendar, day, month, year,
    epoch
  - Date ranges: `BET...AND`, `BEF`, `AFT`
  - Date approximations: `ABT`, `CAL`, `EST`
  - Date periods: `FROM`, `TO`, `FROM...TO`
  - All four calendars: GREGORIAN (default), JULIAN,
    FRENCH_R, HEBREW
  - Extension calendars (parsed as extension tag)
  - `BCE` epoch for Gregorian and Julian
- **FR-103**: Gregorian months: `JAN`, `FEB`, `MAR`, `APR`,
  `MAY`, `JUN`, `JUL`, `AUG`, `SEP`, `OCT`, `NOV`, `DEC`
- **FR-104**: Julian months: same as Gregorian
- **FR-105**: French Republican months: `VEND`, `BRUM`,
  `FRIM`, `NIVO`, `PLUV`, `VENT`, `GERM`, `FLOR`, `PRAI`,
  `MESS`, `THER`, `FRUC`, `COMP`
- **FR-106**: Hebrew months: `TSH`, `CSH`, `KSL`, `TVT`,
  `SHV`, `ADR`, `ADS`, `NSN`, `IYR`, `SVN`, `TMZ`, `AAV`,
  `ELL`
- **FR-107**: `parseTime(String)` MUST parse the `Time`
  ABNF: `hour:minute[:second[.fraction]][Z]` with hours
  0-23, and reject 24:00:00
- **FR-108**: `parseAge(String)` MUST parse the `Age` ABNF:
  optional bound (`<` or `>`), followed by duration
  components `Ny`, `Nm`, `Nw`, `Nd` in order
- **FR-109**: `parsePersonalName(String)` MUST parse the
  `PersonalName` ABNF, extracting surname from within `/`
  delimiters when present
- **FR-110**: `parseLatitude(String)` MUST parse
  `N|S` + decimal degrees (0-90 range)
- **FR-111**: `parseLongitude(String)` MUST parse
  `E|W` + decimal degrees (0-180 range)
- **FR-112**: `parseLanguage(String)` MUST validate BCP 47
  language tag structure
- **FR-113**: `parseMediaType(String)` MUST validate
  `type/subtype` MIME format structure
- **FR-114**: `parseEnum(String, Set<String>)` MUST validate
  the value against a provided set of valid values,
  accepting extension tags as valid enum values
- **FR-115**: `parseListText(String)` MUST split on commas
  per the `List-Text` ABNF production
- **FR-116**: `parseListEnum(String, Set<String>)` MUST split
  on commas and validate each element per the `List-Enum`
  production
- **FR-117**: `parseUri(String)` MUST validate URI-reference
  structure per RFC 3986
- **FR-118**: `parseFilePath(String)` MUST validate URL
  string structure
- **FR-119**: `parseTagDef(String)` MUST parse
  `extTag SP URI-reference` and return the tag and URI
  components

### Non-Functional Requirements

- **NFR-001**: The parser MUST have zero external runtime
  dependencies (Java standard library only)
- **NFR-002**: Test-scoped dependencies (JUnit 5, etc.) are
  permitted
- **NFR-003**: The parser MUST target Java 11 or later
- **NFR-004**: The parser MUST minimize object allocation in
  the hot parsing loop (reuse buffers, prefer primitives)
- **NFR-005**: The parser MUST process input sequentially
  and maintain cache-friendly access patterns
- **NFR-006**: The parser MUST NOT buffer the entire input
  file in memory
- **NFR-007**: All public API types and methods MUST have
  Javadoc documentation
- **NFR-008**: Fields MUST be `final` where possible; public
  API value objects MUST be immutable
- **NFR-009**: Visibility MUST be minimized (`private` by
  default)
- **NFR-010**: The parser MUST use `try-with-resources`
  for all closeable resources and implement `AutoCloseable`
  on the reader class
- **NFR-011**: The parser MUST be version-oblivious at the
  line-parsing level: the line grammar is stable across all
  GEDCOM 7.x versions, so no version-specific code paths
  are needed in the core parser
- **NFR-012**: The parser architecture MUST use a layered
  design with pluggable strategies for encoding, payload
  assembly, and escape handling. The line tokenizer and
  SAX event emitter MUST be shared components that do not
  contain version-specific logic. Version-specific behavior
  MUST be isolated to strategy implementations selected via
  `GedcomReaderConfig`. This ensures GEDCOM 5.5.5 support
  can be added as new strategy implementations without
  modifying core parsing logic.

### Key Entities

#### Core Parser (version-agnostic where possible)

- **GedcomReader**: The entry point. Accepts an `InputStream`
  and a `GedcomHandler`, orchestrates streaming parse.
  Implements `AutoCloseable`. Configured via
  `GedcomReaderConfig` which selects version-specific
  strategies.
- **GedcomReaderConfig**: Immutable configuration object
  specifying: input encoding strategy, payload assembly
  strategy (CONT-only vs CONT+CONC), `@@` escape strategy
  (leading-only vs all-positions), xref validation rules,
  strict vs lenient mode. Provides factory methods:
  `GedcomReaderConfig.gedcom7()` (default) and, in the
  future, `GedcomReaderConfig.gedcom555()`.
- **GedcomHandler**: Interface (or abstract class with no-op
  defaults) defining all event callbacks: startDocument
  (with GedcomVersion), endDocument, startRecord, endRecord,
  startStructure, endStructure, warning, error, fatalError.
- **GedcomVersion**: Immutable value class representing a
  parsed version: major (int), minor (int), optional patch
  (int). Provides `isGedcom7()`, `isGedcom5()` convenience
  methods.
- **GedcomParseError**: Immutable value class holding error
  severity, line number, byte offset, message, and raw
  line content.

#### Internal Components (pluggable per version)

- **GedcomInputDecoder** (internal interface): Converts raw
  bytes to a character stream. Default implementation:
  UTF-8 with BOM detection. Future 5.5.5 implementations:
  UTF-16 (with BOM-based endianness), ANSEL.
- **GedcomLineTokenizer** (internal): Parses character stream
  into `GedcomLine` tokens. Shared across all versions;
  uses permissive character rules for xrefs/tags.
- **GedcomLine** (internal): Reusable token representing
  a parsed line: level (int), xref (String|null),
  tag (String), value (String|null), isPointer (boolean).
- **PayloadAssembler** (internal interface): Strategy for
  assembling multi-line payloads. Default: CONT-only.
  Future 5.5.5 implementation: CONT + CONC.
- **AtEscapeStrategy** (internal interface): Strategy for
  `@@` un-escaping. Default: leading-only (GEDCOM 7).
  Future 5.5.5 implementation: all-positions.

#### Data Type Utilities (companion, not wired into parser)

- **GedcomDataTypes**: Static utility class with parse methods
  for each GEDCOM 7 data type. Future 5.5.5 support will
  add version-specific date parsing (escape-sequence
  calendars vs keyword calendars).
- **GedcomDate**: Immutable value class representing a parsed
  date with calendar, year, month, day, modifier, epoch.
- **GedcomDateRange**: Immutable value class for BET...AND
  date ranges.
- **GedcomDatePeriod**: Immutable value class for FROM...TO
  date periods.
- **GedcomTime**: Immutable value class for parsed time.
- **GedcomAge**: Immutable value class for parsed age
  durations with optional bound.
- **GedcomPersonalName**: Immutable value class with given
  name parts and extracted surname.
- **GedcomCoordinate**: Immutable value class for parsed
  latitude or longitude.

## Success Criteria

### Measurable Outcomes

- **SC-001**: The parser correctly parses all GEDCOM 7.0
  example files from the FamilySearch/GEDCOM repository
  without errors
- **SC-002**: The parser streams events for a 1-million-line
  GEDCOM file using constant memory (heap does not grow
  proportionally to file size)
- **SC-003**: All ABNF grammar productions for line format
  and data types have at least one positive and one negative
  test case
- **SC-004**: Error reports include accurate line numbers
  and byte offsets for all error conditions
- **SC-005**: The public API surface consists of fewer than
  15 public types
- **SC-006**: The parser has zero runtime dependencies
  (verified by build configuration)
- **SC-007**: All 8 user stories pass their acceptance
  scenarios
- **SC-008**: The parser correctly handles files declaring
  VERS 7.0 and VERS 7.1 without any code changes between
  them (version-oblivious design validated)

## Appendix A: GEDCOM 7 ABNF Grammar Reference

The complete grammar the parser must implement:

```abnf
Line    = Level D [Xref D] Tag [D LineVal] EOL
Level   = "0" / nonzero *DIGIT
D       = %x20
Xref    = atsign 1*tagchar atsign  ; not "@VOID@"
Tag     = stdTag / extTag
LineVal = pointer / lineStr
EOL     = %x0D [%x0A] / %x0A

stdTag  = ucletter *tagchar
extTag  = underscore 1*tagchar
tagchar = ucletter / DIGIT / underscore

pointer = voidPtr / Xref
voidPtr = %s"@VOID@"

nonAt   = %x09 / %x20-3F / %x41-10FFFF
nonEOL  = %x09 / %x20-10FFFF
lineStr = (nonAt / atsign atsign) *nonEOL
```

## Appendix B: Banned Character Code Points

| Range | Description |
|-------|-------------|
| U+0000-U+0008 | C0 controls (NUL-BS) |
| U+000B | Vertical tab |
| U+000C | Form feed |
| U+000E-U+001F | C0 controls (SO-US) |
| U+007F | DEL |
| U+0080-U+009F | C1 controls |
| U+D800-U+DFFF | Surrogates |
| U+FFFE-U+FFFF | Invalid |

## Appendix C: Calendar Month Tags

| Calendar | Months |
|----------|--------|
| GREGORIAN | JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC |
| JULIAN | JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC |
| FRENCH_R | VEND BRUM FRIM NIVO PLUV VENT GERM FLOR PRAI MESS THER FRUC COMP |
| HEBREW | TSH CSH KSL TVT SHV ADR ADS NSN IYR SVN TMZ AAV ELL |

## Appendix D: Enumeration Sets

**SEX**: M, F, X, U
**RESN**: CONFIDENTIAL, LOCKED, PRIVACY
**PEDI**: ADOPTED, BIRTH, FOSTER, SEALING, OTHER
**FAMC-STAT**: CHALLENGED, DISPROVEN, PROVEN
**QUAY**: 0, 1, 2, 3
**MEDI**: AUDIO, BOOK, CARD, ELECTRONIC, FICHE, FILM,
  MAGAZINE, MANUSCRIPT, MAP, NEWSPAPER, PHOTO, TOMBSTONE,
  VIDEO, OTHER
**ROLE**: CHIL, CLERGY, FATH, FRIEND, GODP, HUSB, MOTH,
  MULTIPLE, NGHBR, OFFICIATOR, PARENT, SPOU, WIFE, WITN,
  OTHER
**ADOP**: HUSB, WIFE, BOTH
**NAME-TYPE**: AKA, BIRTH, IMMIGRANT, MAIDEN, MARRIED,
  PROFESSIONAL, OTHER

## Appendix E: Version Compatibility Design

The GEDCOM 7.x line grammar (`grammar.abnf`) is stable across
all 7.0 and 7.1 versions. Only structure definitions (which
tags are valid in which contexts, cardinalities, new enum
values) change between minor versions. Patch versions are
purely editorial with zero data impact.

The parser is version-oblivious by design:
- The line parser implements the stable ABNF grammar
- No version-specific code paths exist in the core parser
- Unknown tags are parsed without error (treated as extensions
  per spec recommendation)
- HEAD.GEDC.VERS is read to confirm 7.x but does not alter
  parsing behavior
- Future 7.x versions are automatically supported without
  code changes

If structure-type resolution or cardinality validation is
needed in the future, it SHOULD be built as a separate
optional overlay module that loads per-version definitions
from the spec's machine-readable TSV/YAML files (compiled
into Java source at build time to maintain zero runtime
dependencies).

## Appendix F: GEDCOM 5.5.5 Forward-Compatibility Design

This parser is scoped to GEDCOM 7.x, but the architecture
is designed so that GEDCOM 5.5.5 support can be added as a
follow-on feature without modifying core parsing logic.

### Layered Architecture

```
┌──────────────────────────────────────────────┐
│         GedcomReader (orchestrator)           │
│  Configured via GedcomReaderConfig            │
├──────────────────────────────────────────────┤
│  Layer 4: SAX Event Emitter      [SHARED]    │
│  Level-stack state machine,                  │
│  start/end record/structure events           │
├──────────────────────────────────────────────┤
│  Layer 3: Payload Assembler      [PLUGGABLE] │
│  7.0: CONT only                              │
│  5.5.5: CONT + CONC                          │
├──────────────────────────────────────────────┤
│  Layer 2: Line Tokenizer         [SHARED]    │
│  Parses level, xref, tag, lineVal, EOL       │
│  Permissive char set (superset of both)      │
├──────────────────────────────────────────────┤
│  Layer 1: Input Decoder          [PLUGGABLE] │
│  7.0: UTF-8 only                             │
│  5.5.5: UTF-8, UTF-16, (ANSEL, ASCII)       │
├──────────────────────────────────────────────┤
│  Layer 0: @@ Escape Strategy     [PLUGGABLE] │
│  7.0: leading-only                           │
│  5.5.5: all-positions                        │
└──────────────────────────────────────────────┘
```

### What Is Shared vs. Pluggable

| Component | Shared? | Branching Point |
|-----------|---------|-----------------|
| Line tokenizer | Yes | Permissive superset handles both |
| SAX event emitter | Yes | Level-stack logic identical |
| Hierarchical nesting | Yes | Level semantics identical |
| EOL handling | Yes | Same three forms in both |
| HEAD/TRLR detection | Yes | Both require HEAD first, TRLR last |
| Input decoder | No | UTF-8 only (7.0) vs multi-encoding (5.5.5) |
| Payload assembler | No | CONT only (7.0) vs CONT+CONC (5.5.5) |
| `@@` escape | No | Leading (7.0) vs all positions (5.5.5) |
| Date parsing | No | Keyword calendars (7.0) vs `@#D...@` escapes (5.5.5) |
| HEAD validation | No | Different required substructures |
| Xref validation | No | Different allowed character sets |
| Record type set | No | SNOTE (7.0) vs NOTE record (5.5.5) |

### Key Differences Requiring Future Work

1. **Encoding detection and ANSEL/UTF-16 decoders**: The
   biggest addition. Requires reading HEAD.CHAR (which is
   itself encoded in the file's encoding -- a bootstrap
   problem solved by BOM detection + fallback heuristics).

2. **CONC payload assembly**: Straightforward new
   PayloadAssembler implementation. Must respect code point
   and character boundaries when re-joining CONC splits.

3. **All-position `@@` un-escaping**: Straightforward new
   AtEscapeStrategy implementation.

4. **Date parsing with `@#DCALENDAR@` escape sequences**:
   New date parser variant in GedcomDataTypes. The existing
   GedcomDate value classes can represent both formats.

5. **HEAD.CHAR and HEAD.GEDC.FORM validation**: Version-
   specific HEAD validation rules.

6. **255-character line length enforcement** (optional strict
   5.5.5 mode).

7. **Xref length limit** (max 22 code units in 5.5.5).

### What Does NOT Need to Change

- GedcomHandler interface (events are version-agnostic)
- GedcomLine internal token format
- Level-stack nesting logic
- Error reporting infrastructure (GedcomParseError)
- All value classes (GedcomDate, GedcomAge, etc.)
- The streaming/constant-memory guarantee
