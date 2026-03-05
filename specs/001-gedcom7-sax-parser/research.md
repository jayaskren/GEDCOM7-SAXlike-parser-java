# Research: GEDCOM 7 SAX-like Streaming Parser

**Branch**: `001-gedcom7-sax-parser` | **Date**: 2026-03-04

## R-001: Build System

**Decision**: Gradle with Kotlin DSL (`build.gradle.kts`)

**Rationale**: Build-time code generation (compiling GEDCOM 7
structure definitions from TSV into Java source) is
significantly simpler in Gradle via custom tasks than Maven
(which requires a dedicated plugin). Gradle's `maven-publish`
and `signing` plugins cover Maven Central publishing. Modern
Java libraries (Google, Square) use Gradle. Kotlin DSL
provides type-safe, IDE-autocompleted build scripts.

**Alternatives considered**:
- Maven: More verbose build-time codegen configuration
  (exec-maven-plugin or custom plugin). No advantage for this
  project given the codegen requirement.

## R-002: Package Naming and Maven Coordinates

**Decision**: `org.gedcom7.parser` root package

| Package | Purpose |
|---------|---------|
| `org.gedcom7.parser` | Core public API (GedcomReader, GedcomHandler, config, events) |
| `org.gedcom7.parser.datatype` | Data type parsing utilities (GedcomDataTypes, value classes) |
| `org.gedcom7.parser.validation` | Opt-in structure/cardinality validation layer |
| `org.gedcom7.parser.internal` | Internal implementation (tokenizer, assembler, strategies) |

Maven coordinates (placeholder; final groupId depends on
domain verification):
```
groupId:    org.gedcom7
artifactId: gedcom7-parser
version:    0.1.0
```

**Rationale**: Short, descriptive, follows Java naming
conventions. The `internal` package signals non-public API
per Java convention (module-info.java will not export it).

**Alternatives considered**:
- `io.github.<username>.gedcom7.parser`: Viable for personal
  GitHub publishing but less clean as a community project.

## R-003: GEDCOM 7 Machine-Readable Structure Definitions

**Decision**: Use TSV files from `extracted-files/` in the
GEDCOM 7 specification repository for build-time code
generation.

**Source files** (all TSV, no header row):

| File | Columns | Purpose |
|------|---------|---------|
| `substructures.tsv` | superstructure_URI, tag, substructure_URI | Maps (context + tag) -> structure type. **Primary disambiguation table.** |
| `cardinalities.tsv` | superstructure_URI, substructure_URI, cardinality | `{0:1}`, `{0:M}`, `{1:1}`, `{1:M}` |
| `payloads.tsv` | structure_URI, payload_type | Links structure to its data type |
| `enumerations.tsv` | enumset_URI, enum_value_URI | Enum set memberships |
| `enumerationsets.tsv` | structure_URI, enumset_URI | Links structures to their enum sets |

**ABNF grammar**: `extracted-files/grammar.abnf` defines the
line-level grammar (Level, Tag, Xref, LineVal, EOL) plus all
data type grammars (DateValue, Time, Age, PersonalName, etc.).

**Context-dependent tag resolution**: The same tag string
(e.g., `NAME`, `ADOP`) maps to different structure type URIs
depending on the superstructure context. For example:
- `NAME` under `record-INDI` -> `g7:INDI-NAME` (PersonalName
  payload, substructures: GIVN, SURN, NICK, etc.)
- `NAME` under `record-REPO` -> `g7:NAME` (string payload,
  no substructures)

The `substructures.tsv` lookup `(superstructure_URI, tag)
-> substructure_URI` is the primary resolution mechanism.

**Build-time code generation approach**:
1. Download/vendor the TSV files from the GEDCOM spec repo
   (pinned to a specific release tag for reproducibility)
2. A Gradle task reads the TSV files and generates Java source:
   - A `StructureDefinitions` class containing lookup maps
     for structure resolution, cardinality, and payload types
   - Enumeration constants for standard structure URIs
3. Generated sources are placed in
   `build/generated/sources/gedcom/` and compiled alongside
   hand-written source
4. TSV source files stored in `src/main/data/gedcom7/`

**Rationale**: TSV files are simpler to parse than YAML (no
YAML parser dependency at build time -- Gradle's classpath
has basic I/O). The TSV files contain exactly the lookup
relationships needed. Zero runtime dependency is maintained
because definitions are compiled into Java source.

**Alternatives considered**:
- YAML tag files (`extracted-files/tags/`): Richer per-term
  data (labels, descriptions, specification text) but requires
  a YAML parser. Not needed for validation -- the TSV files
  contain all structural relationships.
- `grammar.gedstruct`: Human-readable combined view but
  custom metasyntax is harder to parse programmatically.
- Runtime YAML/JSON loading: Violates zero-dependency
  principle.

## R-004: Project Directory Layout

**Decision**: Standard Gradle Java library layout with data
directory for codegen input.

```
gedcom7-parser/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/wrapper/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/gedcom7/parser/
│   │   │       ├── GedcomReader.java
│   │   │       ├── GedcomHandler.java
│   │   │       ├── GedcomReaderConfig.java
│   │   │       ├── GedcomHeaderInfo.java
│   │   │       ├── GedcomVersion.java
│   │   │       ├── GedcomParseError.java
│   │   │       ├── datatype/
│   │   │       │   ├── GedcomDataTypes.java
│   │   │       │   ├── GedcomDate.java
│   │   │       │   └── ...
│   │   │       ├── validation/
│   │   │       │   └── ...
│   │   │       └── internal/
│   │   │           ├── GedcomInputDecoder.java
│   │   │           ├── GedcomLineTokenizer.java
│   │   │           ├── GedcomLine.java
│   │   │           ├── PayloadAssembler.java
│   │   │           ├── AtEscapeStrategy.java
│   │   │           └── ...
│   │   └── data/
│   │       └── gedcom7/
│   │           ├── substructures.tsv
│   │           ├── cardinalities.tsv
│   │           ├── payloads.tsv
│   │           ├── enumerations.tsv
│   │           └── enumerationsets.tsv
│   └── test/
│       ├── java/
│       │   └── org/gedcom7/parser/
│       │       ├── GedcomReaderTest.java
│       │       ├── GedcomLineTokenizerTest.java
│       │       └── ...
│       └── resources/
│           ├── minimal.ged
│           ├── all-record-types.ged
│           ├── extensions.ged
│           └── ...
├── build/                        # gitignored
│   └── generated/sources/gedcom/
│       └── main/java/
│           └── org/gedcom7/parser/validation/
│               └── StructureDefinitions.java
├── specs/                        # speckit artifacts
├── docs/
└── .specify/
```

**Rationale**: Standard layout minimizes configuration.
`src/main/data/` is the conventional location for non-Java
build-time input files. Generated sources follow Gradle
conventions under `build/generated/`.

## R-005: Testing Strategy

**Decision**: JUnit 5 with parameterized tests

- Unit tests per component (tokenizer, payload assembler,
  escape strategy, each data type parser)
- Parameterized tests for ABNF grammar productions (positive
  and negative cases)
- Integration tests with representative `.ged` files in
  `src/test/resources/`
- Sample GEDCOM files from the FamilySearch/GEDCOM repo's
  test suite (if available) plus hand-crafted edge case files

**Rationale**: JUnit 5 parameterized tests are ideal for
testing parser grammar rules (many inputs, expected outputs).
Aligns with Constitution Principle V (TDD).

## R-006: Java Module System

**Decision**: Provide a `module-info.java` for JPMS support

- Module name: `org.gedcom7.parser`
- Exports: `org.gedcom7.parser`,
  `org.gedcom7.parser.datatype`,
  `org.gedcom7.parser.validation`
- Does NOT export: `org.gedcom7.parser.internal`
- Requires: only `java.base` (implicit)

**Rationale**: Java 11+ target means JPMS is available.
Module-info enforces the public/internal API boundary at the
module level. The `internal` package is truly hidden from
consumers.
