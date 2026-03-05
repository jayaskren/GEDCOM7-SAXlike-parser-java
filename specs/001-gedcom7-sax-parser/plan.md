# Implementation Plan: GEDCOM 7 SAX-like Streaming Parser

**Branch**: `001-gedcom7-sax-parser` | **Date**: 2026-03-04 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/001-gedcom7-sax-parser/spec.md`

## Summary

Build a streaming, event-driven GEDCOM 7 parser in Java that
reads GEDCOM files line-by-line and fires SAX-like callback
events (startDocument, startRecord, startStructure, etc.)
without loading the entire file into memory. The architecture
uses pluggable strategies for encoding, payload assembly, and
escape handling to enable future GEDCOM 5.5.5 support without
modifying core logic. An opt-in validation layer checks
structure context and cardinality against GEDCOM 7 definitions
compiled from the spec's machine-readable TSV files at build
time.

## Technical Context

**Language/Version**: Java 11+
**Build System**: Gradle with Kotlin DSL (build-time code
  generation from TSV files; see R-001 in research.md)
**Primary Dependencies**: None at runtime (zero-dependency
  library). JUnit 5 for testing.
**Storage**: N/A (streaming parser, no persistence)
**Testing**: JUnit 5 with parameterized tests for grammar
  productions
**Target Platform**: JVM (cross-platform)
**Project Type**: Library
**Performance Goals**: Constant-memory streaming; minimize
  object allocation in hot parsing loop (mechanical sympathy)
**Constraints**: Zero external runtime dependencies; single-
  threaded per instance; Java 11+ compatibility
**Scale/Scope**: Handle files with millions of records;
  ~15 public API types; ~321 structure definitions for
  opt-in validation

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after
Phase 1 design.*

| # | Principle | Status | Evidence |
|---|-----------|--------|----------|
| I | GEDCOM 7 Spec Compliance | PASS | FR-001–094 cover all ABNF grammar rules, banned chars, CONT, HEAD/TRLR, extension tags, all 7 record types |
| II | SAX-like Event-Driven API | PASS | FR-040–044 define callback API; FR-043 requires streaming delivery; GedcomHandler contract in data-model.md |
| III | Mechanical Sympathy | PASS | NFR-004/005 require minimal allocation and sequential access; GedcomLine is mutable/reusable; no boxing in hot path |
| IV | Java Best Practices | PASS | NFR-007–010 cover Javadoc, immutability, visibility, try-with-resources; Java 11 target per NFR-003 |
| V | TDD | PASS | R-005 defines JUnit 5 parameterized tests; SC-003 requires positive+negative tests for all ABNF productions |
| VI | Simplicity / YAGNI | PASS | Out of Scope section excludes DOM, writing, GEDZIP; validation is opt-in not default; <15 public types (SC-005) |
| VII | Zero Runtime Dependencies | PASS | NFR-001 explicit; structure definitions compiled at build time (FR-099, R-003); only JUnit 5 at test scope |

**Potential concern**: Opt-in structure validation (FR-095–099)
adds ~5 types and build-time codegen complexity. This was
explicitly requested by the user during clarification (Q4).
It does NOT violate Principle VI because it is opt-in and
off by default. It does NOT violate Principle VII because
definitions are compiled into Java source at build time.

## Project Structure

### Documentation (this feature)

```text
specs/001-gedcom7-sax-parser/
├── plan.md              # This file
├── research.md          # Phase 0: research decisions
├── data-model.md        # Phase 1: entity definitions
├── quickstart.md        # Phase 1: usage examples
├── contracts/
│   └── public-api.java  # Phase 1: API contract signatures
└── tasks.md             # Phase 2: task breakdown (via /speckit.tasks)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/
│   │   └── org/gedcom7/parser/
│   │       ├── GedcomReader.java
│   │       ├── GedcomHandler.java
│   │       ├── GedcomReaderConfig.java
│   │       ├── GedcomHeaderInfo.java
│   │       ├── GedcomVersion.java
│   │       ├── GedcomParseError.java
│   │       ├── GedcomFatalException.java
│   │       ├── datatype/
│   │       │   ├── GedcomDataTypes.java
│   │       │   ├── GedcomDate.java
│   │       │   ├── GedcomDateRange.java
│   │       │   ├── GedcomDatePeriod.java
│   │       │   ├── GedcomTime.java
│   │       │   ├── GedcomAge.java
│   │       │   ├── GedcomPersonalName.java
│   │       │   └── GedcomCoordinate.java
│   │       ├── validation/
│   │       │   └── (opt-in validation layer)
│   │       └── internal/
│   │           ├── GedcomInputDecoder.java
│   │           ├── Utf8InputDecoder.java
│   │           ├── GedcomLineTokenizer.java
│   │           ├── GedcomLine.java
│   │           ├── PayloadAssembler.java
│   │           ├── ContOnlyAssembler.java
│   │           ├── AtEscapeStrategy.java
│   │           ├── LeadingAtEscapeStrategy.java
│   │           └── LevelStack.java
│   ├── data/
│   │   └── gedcom7/
│   │       ├── substructures.tsv
│   │       ├── cardinalities.tsv
│   │       ├── payloads.tsv
│   │       ├── enumerations.tsv
│   │       └── enumerationsets.tsv
│   └── resources/
│       └── (empty -- no runtime resources needed)
├── test/
│   ├── java/
│   │   └── org/gedcom7/parser/
│   │       ├── GedcomReaderTest.java
│   │       ├── GedcomLineTokenizerTest.java
│   │       ├── PayloadAssemblerTest.java
│   │       ├── AtEscapeStrategyTest.java
│   │       ├── GedcomHeaderInfoTest.java
│   │       ├── CrossReferenceTrackingTest.java
│   │       ├── ErrorHandlingTest.java
│   │       ├── datatype/
│   │       │   └── (one test class per data type parser)
│   │       └── validation/
│   │           └── StructureValidationTest.java
│   └── resources/
│       ├── minimal.ged
│       ├── all-record-types.ged
│       ├── extensions.ged
│       ├── cont-multiline.ged
│       ├── cross-references.ged
│       ├── malformed-lines.ged
│       └── (additional edge case files)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/wrapper/
```

**Structure Decision**: Standard Gradle single-module Java
library layout. `src/main/data/` holds GEDCOM 7 TSV files
for build-time code generation. Generated sources go to
`build/generated/sources/gedcom/` (gitignored). Module-info
exports `org.gedcom7.parser`, `org.gedcom7.parser.datatype`,
`org.gedcom7.parser.validation`; does NOT export
`org.gedcom7.parser.internal`.

## Complexity Tracking

> No constitution violations. No complexity justifications
> needed.
