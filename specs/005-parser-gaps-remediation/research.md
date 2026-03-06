# Research: Parser Gaps Remediation

**Feature**: 005-parser-gaps-remediation
**Date**: 2026-03-05

## R-1: Structure Definitions Completeness

**Question**: Which event/LDS tags are missing from StructureDefinitions?

**Finding**: All standard event types and LDS ordinance tags are already
defined in `StructureDefinitions.java`. The TSV source files
(`src/main/data/gedcom7/substructures.tsv` and `cardinalities.tsv`) include
complete coverage for:
- Individual events: ADOP, BAPM, BARM, BASM, BIRT, BLES, BURI, CENS, CHR,
  CHRA, CONF, CREM, DEAT, EMIG, FCOM, GRAD, IMMI, NATU, ORDN, PROB, RETI, WILL
- LDS ordinances: BAPL, CONL, ENDL, INIL, SLGC, SLGS
- Family events: ANUL, CENS, DIV, DIVF, ENGA, MARB, MARC, MARL, MARR, MARS

**Decision**: User Story 1 (FR-001, FR-002) is already satisfied by the
existing code. The implementation task will add a comprehensive test to
verify and lock down this coverage. If any child structures (TEMP, STAT
for LDS) are missing, they will be added to the TSV files and regenerated.

**Rationale**: Verifying existing coverage with tests prevents future
regressions and satisfies the acceptance scenarios without unnecessary
code changes.

## R-2: Minimum Cardinality Data Availability

**Question**: Does the cardinalities.tsv contain minimum cardinality info?

**Finding**: Yes. The cardinalities.tsv uses notation like `{0:1}`, `{0:M}`,
`{1:1}`, `{1:M}`. The first number is the minimum. Currently
`StructureDefinitions.isSingleton()` only checks if max is 1 for
max-cardinality enforcement. Minimum cardinality (first digit) is not
currently checked.

**Decision**: Add `isRequired(String cardinality)` method to
`StructureDefinitions` that checks if the minimum is > 0. At
endStructure/endRecord time, iterate required children and warn if
count is 0.

**Rationale**: Reuses existing data and infrastructure. No new data
sources needed.

## R-3: Date Value Type System

**Question**: What's the best Java 11-compatible approach for typed dates?

**Finding**: Current `parseDateValue()` returns `Object`, which can be
`GedcomDate`, `GedcomDateRange`, or `GedcomDatePeriod`. These three
classes share no common interface.

**Alternatives considered**:
1. **Common interface** (chosen): Add `GedcomDateValue` interface with
   `getType()` enum method. Each class implements it. Source-compatible
   since `Object` is wider than any interface.
2. **Abstract base class**: Would require changing class hierarchies.
   More invasive than an interface.
3. **Sealed interface (Java 17+)**: Better type safety but requires
   Java 17. Violates the Java 11 minimum target.
4. **Visitor pattern**: Over-engineered for 3 types.

**Decision**: Common interface with enum-based type discrimination.
Backward compatible. IDE-friendly.

## R-4: Strategy Interface Location

**Question**: Where should public strategy interfaces live?

**Finding**: Currently in `org.gedcom7.parser.internal` package which is
not exported in `module-info.java`. The Builder methods that accept these
types are in the public `org.gedcom7.parser` package.

**Alternatives considered**:
1. **New `spi` package** (chosen): Move interfaces to
   `org.gedcom7.parser.spi`, export in module-info. Keep implementations
   in `internal`. Clean SPI pattern.
2. **Export `internal` package**: Would expose GedcomLine, tokenizer
   internals. Rejected.
3. **Copy interfaces to public package**: Would create duplicate types.
   Rejected.
4. **Move to main `parser` package**: Clutters the main package with
   low-level interfaces. Rejected.

**Decision**: New `org.gedcom7.parser.spi` package for the three strategy
interfaces. Module-info exports it.

## R-5: startRecord Value Delivery

**Question**: How to add value parameter without breaking existing code?

**Finding**: `GedcomHandler` is an abstract class with empty default
implementations (not an interface). Adding a new overloaded method with
a default implementation that delegates to the old method is fully
backward compatible.

**Decision**: Add `startRecord(int, String, String, String)` with default
that calls `startRecord(int, String, String)`. Parser calls the 4-param
version. Existing handlers unaffected.

## R-6: Byte Offset Tracking Approach

**Question**: How to track byte offsets without breaking Reader-based input?

**Finding**: `GedcomLine` already has a `byteOffset` field (always 0).
The `GedcomLineTokenizer` reads from a `Reader` (character stream) and
has no byte-level awareness. The `GedcomInputDecoder` converts
`InputStream` to `Reader`.

**Decision**: Create a `CountingInputStream` wrapper that counts bytes
before they reach the decoder. Pass the counter to the tokenizer so it
can stamp each line's byte offset. When parser is constructed from a
`Reader` directly, byte offset returns -1.

**Rationale**: Counting at the byte stream level is accurate even for
multi-byte UTF-8. No changes to the Reader interface.

## R-7: Buffered Tokenizer I/O

**Question**: What's the optimal buffer strategy?

**Finding**: Current `GedcomLineTokenizer` calls `reader.read()` one
character at a time. Java's `BufferedReader` already wraps with a buffer,
but the per-character method call overhead remains.

**Decision**: Read chunks into a `char[]` buffer (default 8KB).
Scan the buffer with array index. Refill when buffer exhausted.
This eliminates per-character virtual method dispatch.

**Rationale**: Standard technique for high-throughput parsers. 8KB
aligns with typical OS page size and I/O block size.

## R-8: Xref Validation Rules

**Question**: What characters are valid in GEDCOM 7 xref identifiers?

**Finding**: Per the GEDCOM 7 spec, xref identifiers (between @ delimiters)
must match: `1*tagchar` where `tagchar` is any character except @, spaces,
control characters (U+0000-U+001F, U+007F), and the # character. The
identifier must be at least 1 character long.

**Decision**: Validate xref characters when validation is enabled. Report
invalid characters via warning handler. When validation is disabled, pass
through as-is.
