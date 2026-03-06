# Feature Specification: GEDCOM Parser Gaps Remediation

**Feature Branch**: `005-parser-gaps-remediation`
**Created**: 2026-03-05
**Status**: Draft
**Input**: Evaluation agent findings identifying 10 gaps in the GEDCOM 7 SAX-like parser across structure validation, type safety, API visibility, correctness, and performance.

## Clarifications

### Session 2026-03-05

- Q: Should the expanded StructureDefinitions table cover
  only GEDCOM 7.0 standard tags, or also Latter-day Saints
  (LDS) ordinance tags and common extensions?
  -> A: Cover all standard GEDCOM 7.0 tags including LDS
  ordinance tags (BAPL, CONL, ENDL, INIL, SLGC, SLGS).
  LDS tags are part of the GEDCOM 7 specification and are
  widely used by vendors such as FamilySearch, Ancestral
  Quest, and RootsMagic. Vendor-specific extensions remain
  out of scope.
- Q: For minimum cardinality enforcement, should missing
  required structures be fatal errors or warnings?
  -> A: Warnings by default. The parser already treats
  cardinality as an opt-in validation concern. Missing
  required structures follow the same pattern as existing
  max-cardinality checks: report via the warning/error
  handler, do not abort parsing.
- Q: For the date value return type, should we use a sealed
  interface hierarchy (Java 17+) or a common interface
  compatible with Java 11?
  -> A: Use a common interface compatible with Java 11 (the
  project's minimum target). A sealed hierarchy can be
  considered in a future Java 17+ baseline upgrade.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Parse Files with All Standard Event Types Without False Warnings (Priority: P1)

As a developer using the parser with structure validation
enabled, I want all standard GEDCOM 7 event types to be
recognized so that I do not receive false "Unknown structure"
warnings when processing files containing common genealogical
events like baptisms, burials, emigration, or divorce.

**Why this priority**: False warnings erode trust in the
parser. Developers who encounter spurious warnings for valid
GEDCOM structures may disable validation entirely or abandon
the library. This gap affects the most common use case:
parsing real-world GEDCOM files.

**Independent Test**: Parse a GEDCOM 7 file containing every
standard individual and family event type with validation
enabled. Verify zero false "Unknown structure" warnings.

**Acceptance Scenarios**:

1. **Given** a GEDCOM 7 file containing an INDI record with
   all individual event types (BAPM, BARM, BASM, BLES, BURI,
   CENS, CONF, CREM, EMIG, GRAD, IMMI, NATU, ORDN, PROB,
   RETI, WILL) and LDS ordinance types (BAPL, CONL, ENDL,
   INIL, SLGC) each with standard substructures (DATE, PLAC,
   SOUR, TEMP, STAT), **When** parsed with structure
   validation enabled, **Then** no "Unknown structure"
   warnings are generated for any of these event types or
   their children.

2. **Given** a GEDCOM 7 file containing a FAM record with
   family event types (ANUL, DIV, DIVF, ENGA) and LDS
   ordinance type (SLGS) each with standard substructures,
   **When** parsed with structure validation enabled,
   **Then** no "Unknown structure" warnings are generated.

3. **Given** a structure type that is genuinely unknown (not
   in the GEDCOM 7 spec and not an extension tag), **When**
   parsed with validation enabled, **Then** an "Unknown
   structure" warning is still correctly emitted.

---

### User Story 2 - Enforce Minimum Cardinality Constraints (Priority: P1)

As a developer relying on the parser's validation layer, I
want the parser to warn me when required structures are
missing (e.g., ASSO without ROLE) so that I can identify
incomplete or malformed data during import.

**Why this priority**: Currently the parser catches duplicate
structures (max cardinality) but silently accepts missing
required ones. This creates an asymmetric validation
experience and allows invalid data to pass undetected.

**Independent Test**: Parse a file with an ASSO structure
missing its required ROLE child. Verify a validation warning
is emitted indicating the missing required structure.

**Acceptance Scenarios**:

1. **Given** a GEDCOM 7 file with an ASSO structure that
   has no ROLE child, **When** parsed with validation
   enabled, **Then** a warning is emitted indicating that
   ROLE (cardinality {1:1}) is missing from ASSO.

2. **Given** a GEDCOM 7 file with an ASSO structure that
   has exactly one ROLE child, **When** parsed with
   validation enabled, **Then** no cardinality warning is
   emitted for ROLE.

3. **Given** a structure with a {1:M} child requirement
   where the child appears at least once, **When** parsed
   with validation enabled, **Then** no warning is emitted.

4. **Given** a structure with a {1:M} child requirement
   where the child is absent, **When** parsed with
   validation enabled, **Then** a warning is emitted
   indicating the missing required structure.

5. **Given** validation is disabled, **When** a required
   structure is missing, **Then** no warning is emitted
   (validation is opt-in only).

---

### User Story 3 - Use Type-Safe Date Values Without instanceof Checks (Priority: P2)

As a developer consuming parsed date values, I want the
parser to return date results through a common typed
interface so that I can handle date ranges, date periods,
and exact dates without resorting to `instanceof` checks
and unsafe casts.

**Why this priority**: The current `Object` return type
forces developers into fragile downcasting patterns. A
typed interface improves IDE discoverability and compile-time
safety, which are key differentiators for a library.

**Independent Test**: Call `parseDateValue()` with a date
range string and a date period string. Verify both results
implement the common interface and expose their specific
data through interface methods.

**Acceptance Scenarios**:

1. **Given** a date range string like
   `BET 1 JAN 1900 AND 31 DEC 1900`, **When**
   `parseDateValue()` is called, **Then** the result
   implements a common date value interface and exposes
   range-specific accessors.

2. **Given** a date period string like
   `FROM 1 JAN 1900 TO 31 DEC 1900`, **When**
   `parseDateValue()` is called, **Then** the result
   implements the same common date value interface and
   exposes period-specific accessors.

3. **Given** an exact date string like `6 APR 1952`,
   **When** `parseDateValue()` is called, **Then** the
   result implements the common date value interface.

4. **Given** the common date value interface, **When** a
   developer inspects it in their IDE, **Then** it provides
   methods to determine the date type (range, period, exact,
   approximate) without `instanceof`.

---

### User Story 4 - Provide Custom Decoding Strategies from External Code (Priority: P2)

As an expert developer working with legacy GEDCOM files
(e.g., ANSEL-encoded 5.5.x files processed through this
parser), I want the strategy interfaces for input decoding,
payload assembly, and at-escape handling to be publicly
accessible so that I can provide custom implementations
without forking the library.

**Why this priority**: The strategy pattern was intentionally
designed for extensibility, but the package-private visibility
defeats its purpose. This blocks a key advanced use case
(legacy encoding support) that differentiates this parser.

**Independent Test**: From a separate package (outside the
parser's package), instantiate a GedcomReader.Builder and
successfully supply a custom InputDecoder implementation.
Verify compilation succeeds and the custom decoder is invoked
during parsing.

**Acceptance Scenarios**:

1. **Given** a developer working in a package outside
   `org.gedcom7.parser`, **When** they call
   `Builder.inputDecoder(myCustomDecoder)`, **Then** the
   code compiles and the custom decoder is used during
   parsing.

2. **Given** a developer provides a custom
   `PayloadAssembler`, **When** they call
   `Builder.payloadAssembler(myAssembler)`, **Then** the
   code compiles and the custom assembler is used.

3. **Given** a developer provides a custom
   `AtEscapeStrategy`, **When** they call
   `Builder.atEscapeStrategy(myStrategy)`, **Then** the
   code compiles and the custom strategy is used.

4. **Given** the strategy interfaces are made public,
   **When** existing internal callers are unchanged,
   **Then** all existing tests continue to pass (backward
   compatible).

---

### User Story 5 - Receive Record Payloads for Level-0 Records (Priority: P2)

As a developer parsing GEDCOM files containing shared notes
(SNOTE) or other level-0 records with payloads, I want the
`startRecord` callback to include the record's value so that
I do not lose data from records like
`0 @N1@ SNOTE This is a note`.

**Why this priority**: Data loss is a correctness bug. While
workarounds exist (checking for a subsequent value event),
the current API contract is surprising and inconsistent
with how substructure values are delivered.

**Independent Test**: Parse a file with
`0 @N1@ SNOTE This is a note`. Verify the startRecord
event (or an associated mechanism) provides access to the
payload "This is a note".

**Acceptance Scenarios**:

1. **Given** the line `0 @N1@ SNOTE This is a note`,
   **When** parsed, **Then** the startRecord event provides
   access to the value "This is a note" in addition to
   level, xref, and tag.

2. **Given** the line `0 @I1@ INDI` (no payload),
   **When** parsed, **Then** the startRecord event provides
   a null or empty value, maintaining backward compatibility.

3. **Given** a level-0 SNOTE with CONT continuation lines,
   **When** parsed, **Then** the assembled multi-line value
   is accessible from the startRecord event.

---

### User Story 6 - Validate Cross-Reference Identifiers Per Spec (Priority: P2)

As a developer processing GEDCOM files, I want the parser to
validate that cross-reference identifiers between `@..@`
delimiters conform to the GEDCOM 7 character rules so that
malformed xrefs are caught early rather than silently
propagated into my application's data model.

**Why this priority**: Loose xref validation can allow
injection of invalid identifiers that cause subtle
downstream failures in applications that store or index
xrefs.

**Independent Test**: Parse lines containing xrefs with
spaces, control characters, and other invalid characters.
Verify validation warnings or errors are emitted.

**Acceptance Scenarios**:

1. **Given** a line with xref `@I 1@` (contains space),
   **When** parsed with validation enabled, **Then** a
   warning or error is emitted about the invalid xref
   character.

2. **Given** a line with xref `@I1@` (valid), **When**
   parsed, **Then** no xref validation warning is emitted.

3. **Given** a line with an empty xref `@@`, **When**
   parsed, **Then** a warning or error is emitted.

4. **Given** validation is disabled, **When** an invalid
   xref is encountered, **Then** no warning is emitted
   and the xref is passed through as-is.

---

### User Story 7 - Accurate Byte Offset Tracking (Priority: P3)

As a developer building error reporting or file-position
features on top of the parser, I want `GedcomLine.byteOffset`
to contain the actual byte position of each line so that
error messages can direct users to the exact location in the
original file.

**Why this priority**: The field exists in the API but always
returns 0, which is misleading. It should either work
correctly or be removed to avoid confusion.

**Independent Test**: Parse a file with multi-byte UTF-8
characters. Verify that byteOffset values correspond to the
actual byte positions of each line in the file.

**Acceptance Scenarios**:

1. **Given** a GEDCOM file where line 3 begins at byte
   offset 147, **When** parsed, **Then**
   `GedcomLine.byteOffset` for that line reports 147.

2. **Given** a file with multi-byte UTF-8 characters on
   line 1, **When** parsed, **Then** the byte offset of
   line 2 accounts for the multi-byte characters (not
   just character count).

3. **Given** a file with UTF-8 BOM (0xEF 0xBB 0xBF),
   **When** parsed, **Then** the byte offset of line 1 is 3,
   measured as the file-absolute byte position of the first
   byte of the level digit, after the BOM is consumed.

---

### User Story 8 - Improved Tokenizer I/O Performance (Priority: P3)

As a developer parsing large GEDCOM files (100MB+), I want
the tokenizer to use buffered reading rather than
character-by-character reads so that parsing throughput
is competitive with other streaming parsers.

**Why this priority**: Character-by-character reads create
excessive overhead for large files. While functionally
correct, the performance gap may lead developers to choose
alternative libraries for production workloads.

**Independent Test**: Parse a large GEDCOM file (100,000+
lines) and measure throughput. Verify that throughput meets
the performance target.

**Acceptance Scenarios**:

1. **Given** a GEDCOM file with 100,000 lines, **When**
   parsed after JVM warm-up (at least 3 warm-up iterations
   discarded), **Then** the average parsing throughput across
   5 measured runs is at least 1.5x faster than the current
   character-by-character approach (measured by wall-clock
   time). Performance tests SHOULD be tagged with
   `@Tag("performance")` and excluded from default CI runs
   to avoid flaky results on shared infrastructure.

2. **Given** any valid GEDCOM file, **When** parsed with
   the buffered tokenizer, **Then** the parse results are
   identical to the current character-by-character tokenizer
   (no behavioral regression).

---

### User Story 9 - Warn on Unknown Level-0 Record Tags (Priority: P3)

As a developer processing GEDCOM files with validation
enabled, I want the parser to warn when it encounters an
unrecognized record type at level 0 so that I can identify
non-standard or corrupt records.

**Why this priority**: Silently accepting unknown level-0
tags is inconsistent with the validation behavior for
substructures. Consistency in validation coverage improves
developer trust.

**Independent Test**: Parse a file with `0 @X1@ ZZUNKNOWN`.
Verify a validation warning is emitted for the unknown
record type.

**Acceptance Scenarios**:

1. **Given** a line `0 @X1@ ZZUNKNOWN` (not a standard
   record type), **When** parsed with validation enabled,
   **Then** a warning is emitted about the unknown level-0
   record type.

2. **Given** a line `0 @I1@ INDI` (standard record type),
   **When** parsed with validation enabled, **Then** no
   warning about unknown record type is emitted.

3. **Given** a line `0 @X1@ _CUSTOM` (extension tag),
   **When** parsed with validation enabled, **Then** no
   warning is emitted (extension tags are permitted at
   level 0).

---

### User Story 10 - Simplified Handler for Beginners (Priority: P3)

As a developer new to GEDCOM parsing, I want a convenience
handler class that merges `startRecord` and `startStructure`
into a single unified callback so that I can get started
quickly without understanding the distinction between records
and substructures.

**Why this priority**: Reducing the learning curve lowers the
barrier to adoption. Experienced users can still use the full
handler interface; this is an optional convenience.

**Independent Test**: Register a SimpleGedcomHandler, parse a
file with INDI and FAM records, and verify that a single
`onStructure` callback fires for both level-0 records and
their substructures.

**Acceptance Scenarios**:

1. **Given** a developer extends SimpleGedcomHandler and
   overrides `onStructure(level, xref, tag, value)`,
   **When** parsing a file with INDI and NAME structures,
   **Then** `onStructure` fires for both the INDI record
   (level 0) and the NAME substructure (level 1).

2. **Given** a developer extends SimpleGedcomHandler and
   overrides `onEndStructure(level, tag)`, **When** parsing
   completes, **Then** `onEndStructure` fires for both
   endRecord and endStructure events.

3. **Given** a developer uses SimpleGedcomHandler,
   **When** they also override `startDocument` and
   `endDocument`, **Then** those callbacks still fire
   normally (SimpleGedcomHandler only merges
   record/structure events, not document events).

---

### Edge Cases

- What happens when a structure type is added in a future
  GEDCOM version that the parser does not yet know about?
  The parser should emit a validation warning (unknown
  structure) but continue parsing. The definitions table
  should be designed for easy extension.

- What happens when minimum cardinality enforcement is
  enabled but the parent structure itself is malformed?
  Cardinality checks should be skipped for structures that
  already generated structural parse errors (i.e., errors
  that prevented the line from being tokenized correctly,
  such as missing tag or malformed level — NOT validation
  warnings like max-cardinality violations or unknown
  structure warnings). This avoids cascading warnings for
  fundamentally broken input.

- What happens when `parseDateValue()` receives an
  unparseable string? It should return a result indicating
  parse failure through the common interface, not throw an
  exception or return null.

- What happens when a custom InputDecoder throws an
  exception? The parser should catch it and report it
  through the error handler, not propagate the raw
  exception to the caller.

- What happens when byteOffset tracking is used with a
  Reader (character stream) instead of an InputStream
  (byte stream)? Byte offset tracking should only be
  available when the parser is constructed from an
  InputStream. When constructed from a Reader, byteOffset
  should be documented as unavailable (returns -1).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The structure definitions table MUST include
  all individual event types defined in GEDCOM 7.0: ADOP,
  BAPM, BARM, BASM, BIRT, BLES, BURI, CENS, CHR, CHRA,
  CONF, CREM, DEAT, EMIG, FCOM, GRAD, IMMI, NATU, ORDN,
  PROB, RETI, WILL, and all LDS ordinance types: BAPL,
  CONL, ENDL, INIL, SLGC, and all their valid child
  structures (including TEMP and STAT for LDS ordinances).

- **FR-002**: The structure definitions table MUST include
  all family event types defined in GEDCOM 7.0: ANUL, CENS,
  DIV, DIVF, ENGA, MARB, MARC, MARL, MARR, MARS, and the
  LDS ordinance type SLGS, and all their valid child
  structures.

- **FR-003**: The validation layer MUST enforce minimum
  cardinality constraints ({1:1} and {1:M}) by emitting a
  warning when a required child structure is not present
  within its parent structure by the time the parent's
  endStructure or endRecord event fires.

- **FR-004**: Minimum cardinality enforcement MUST be
  opt-in, controlled by the same validation configuration
  that governs maximum cardinality checks.

- **FR-005**: The `parseDateValue()` method MUST return a
  value through a common interface type (not `Object`) that
  provides a method to determine the date value category
  (exact, range, period, approximate, unparseable) without
  `instanceof`. Unparseable input MUST return a result with
  type UNPARSEABLE rather than null or an exception.

- **FR-006**: The common date value interface MUST be
  backward compatible: existing code using `instanceof`
  checks MUST continue to work.

- **FR-007**: The strategy interfaces (`InputDecoder`,
  `PayloadAssembler`, `AtEscapeStrategy`) and the
  corresponding `Builder` methods MUST be public.

- **FR-008**: The `startRecord` event MUST provide access
  to the record's payload value (if present), either
  through an additional parameter or a new overloaded
  callback method.

- **FR-009**: Changes to `startRecord` MUST be backward
  compatible. Existing handler implementations that do not
  override the new method MUST continue to work.

- **FR-010**: The parser MUST validate cross-reference
  identifiers against the GEDCOM 7 character rules when
  validation is enabled. Xref identifiers (between `@`
  delimiters) MUST contain only characters in the range
  U+0021–U+007E excluding `@` (U+0040) and `#` (U+0023),
  MUST be at least 1 character long, and MUST NOT exceed
  20 characters. Identifiers violating these rules MUST
  generate validation warnings.

- **FR-011**: `GedcomLine.byteOffset` MUST report the
  actual byte offset when the parser is constructed from
  a byte-oriented source. When constructed from a
  character-oriented source, byteOffset MUST return -1
  to indicate unavailability.

- **FR-012**: The tokenizer MUST use buffered I/O
  operations rather than single-character reads.

- **FR-013**: The buffered tokenizer MUST produce
  identical parse results to the current implementation
  for all valid and invalid inputs.

- **FR-014**: When validation is enabled, unrecognized
  record types at level 0 (excluding extension tags)
  MUST generate a validation warning.

- **FR-015**: A `SimpleGedcomHandler` convenience class
  MUST be provided that collapses `startRecord` and
  `startStructure` into a single `onStructure` callback,
  and `endRecord` and `endStructure` into a single
  `onEndStructure` callback.

- **FR-016**: `SimpleGedcomHandler` MUST be a concrete
  class with no-op default methods that developers extend
  and selectively override (same pattern as the existing
  handler adapter).

### Key Entities

- **StructureDefinition**: Represents a valid structure
  in the GEDCOM 7 specification, including its tag, valid
  parent contexts, valid child structures, and cardinality
  constraints (min and max occurrence counts).

- **GedcomDateValue**: Common interface for all parsed date
  representations (exact dates, date ranges, date periods,
  approximate dates). Provides type discrimination and
  access to constituent date parts.

- **SimpleGedcomHandler**: Convenience adapter class that
  simplifies the event handler interface by merging
  record-level and structure-level events into a unified
  callback surface.

## Verification Requirements (Constitution Principle VIII)

Per the project constitution's Independent Verification
principle, this feature MUST include:

### Per-Task Review

After each implementation task is completed, a separate
independent agent MUST review the implementation against:

- The acceptance scenarios defined in this spec
- All constitution principles (I through VIII)
- Test coverage for the implemented behavior
- No regressions in existing tests

The review agent MUST NOT be the same agent that performed
the implementation. Review findings MUST be documented and
any identified gaps addressed before proceeding to the next
task.

### Final Evaluation

After all tasks are implemented and all tests pass, a
comprehensive final evaluation MUST be performed by an
independent agent. The final evaluation MUST:

- Compare the complete implementation against every
  functional requirement (FR-001 through FR-016)
- Verify all acceptance scenarios from every user story
  (Stories 1 through 10)
- Check compliance with every constitution principle
- Identify any gaps, deviations, or unaddressed requirements
- Produce a written report with findings categorized as
  PASS, GAP (with severity HIGH/MEDIUM/LOW), or DEVIATION
  (with justification)

Gaps rated HIGH MUST be addressed before the feature is
considered complete. MEDIUM gaps SHOULD be addressed. LOW
gaps MAY be deferred with documented rationale.

## Assumptions

- The project targets Java 11 as the minimum language
  version. Features requiring Java 17+ (sealed classes,
  pattern matching) are not used.

- The GEDCOM 7.0 specification
  (https://gedcom.io/specifications/FamilySearchGEDCOMv7.html)
  is the authoritative source for valid structure types,
  their allowed contexts, and cardinality constraints.

- The existing validation infrastructure (opt-in via
  configuration, warning/error handler callbacks) is
  reused. No new validation framework is introduced.

- Backward compatibility with existing handler
  implementations is mandatory. No breaking API changes.

- Performance improvements (buffered tokenizer) target
  at least 1.5x throughput improvement on files with
  100,000+ lines, measured as average wall-clock parse
  time across 5 runs after JVM warm-up.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A GEDCOM 7 file using all 20+ standard event
  types parses with zero false "Unknown structure" warnings
  when validation is enabled.

- **SC-002**: Missing required structures (e.g., ASSO
  without ROLE) generate appropriate validation warnings
  with 100% detection rate for all {1:1} and {1:M}
  constraints defined in the specification.

- **SC-003**: Developers can determine date value types
  (range, period, exact, approximate) using interface
  methods rather than `instanceof`, verified by compiling
  code that uses the interface without any casts.

- **SC-004**: Custom strategy implementations (decoder,
  assembler, escape handler) can be provided from any
  package, verified by a test in a separate package that
  compiles and runs successfully.

- **SC-005**: Level-0 record payloads (e.g., SNOTE values)
  are accessible through the handler API with zero data
  loss, verified by round-trip parsing of all level-0
  record types that carry payloads.

- **SC-006**: Cross-reference identifiers containing
  invalid characters generate validation warnings,
  verified against a test suite of valid and invalid
  xref patterns from the GEDCOM 7 specification.

- **SC-007**: Byte offsets are accurate to the exact byte
  position for files parsed from byte-oriented sources,
  verified against manually calculated offsets for files
  with multi-byte UTF-8 characters.

- **SC-008**: Tokenizer throughput on files with 100,000+
  lines improves by at least 1.5x (average of 5 runs after
  JVM warm-up) compared to the current character-by-character
  implementation. Performance tests tagged
  `@Tag("performance")` and excluded from default CI.

- **SC-009**: Unknown level-0 record tags generate
  validation warnings, while extension tags and standard
  tags do not.

- **SC-010**: A developer using SimpleGedcomHandler can
  process a complete GEDCOM file by overriding only two
  methods (`onStructure` and `onEndStructure`), reducing
  the minimum handler method count from 4+ to 2.
