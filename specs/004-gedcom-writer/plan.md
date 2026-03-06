# Implementation Plan: GEDCOM SAX-like Writer

**Branch**: `004-gedcom-writer` | **Date**: 2026-03-05 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-gedcom-writer/spec.md`

## Summary

Build a streaming, event-push GEDCOM writer with typed context classes (IndividualContext, FamilyContext, EventContext, etc.) that guide developers to produce valid GEDCOM output via IDE auto-complete. Lambda-scoped nesting guarantees structure closure. String-based escape hatches (`structure()`/`pointer()`) on every context prevent limitations. Supports both GEDCOM 7 and 5.5.5 via configuration. Includes GedcomDateBuilder for type-safe date construction, strict/lenient mode with configurable WarningHandler, auto-generated and developer-provided xref IDs, automatic CONT/CONC splitting, and @@ escaping.

## Technical Context

**Language/Version**: Java 11+ (matching existing parser)
**Primary Dependencies**: None at runtime (zero external dependencies — constitution Principle VII)
**Storage**: N/A (streaming writer to OutputStream)
**Testing**: JUnit 5 (test-scoped only, matching existing test infrastructure)
**Target Platform**: Any JVM (Java 11+)
**Project Type**: Library (extension of existing GEDCOM parser library)
**Performance Goals**: Streaming write with O(1) memory overhead per structure level; no buffering or reordering
**Constraints**: Zero runtime dependencies; single module; Java 11 language level; UTF-8 output with LF line endings
**Scale/Scope**: ~20-25 typed context classes, ~5 public API types, ~3 date enums, ~10 internal implementation classes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. GEDCOM 7 Spec Compliance | PASS | Writer produces spec-compliant output (levels, delimiters, CONT, xrefs, @@ escaping). Also supports 5.5.5 as first-class. |
| II. SAX-like Event-Driven API | PASS | Writer is the push-side complement to the SAX-like pull/callback parser. Streaming, no buffering. |
| III. Mechanical Sympathy | PASS | Streaming writes directly to OutputStream. No intermediate tree or buffering. StringBuilder reuse for line assembly. |
| IV. Java Best Practices | PASS | Immutable config (Builder pattern), AutoCloseable, Consumer lambdas, final fields, clear Javadoc, camelCase/PascalCase naming. |
| V. Test-Driven Development | PASS | Round-trip tests (write then parse back), unit tests per context, date builder tests, strict/lenient mode tests. |
| VI. Simplicity and YAGNI | PASS | Constitution says "Do NOT add serialization/writing capabilities unless explicitly scoped as a separate feature." This IS explicitly scoped as feature 004. |
| VII. Zero External Runtime Dependencies | PASS | Only java.io, java.util, java.util.logging from standard library. |

No violations. Gate passed.

## Project Structure

### Documentation (this feature)

```text
specs/004-gedcom-writer/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output (writer tutorial)
├── contracts/           # Phase 1 output (public API contracts)
│   ├── writer-api.md    # GedcomWriter + GedcomWriterConfig
│   ├── context-api.md   # Typed context classes
│   └── date-api.md      # GedcomDateBuilder + enums
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/
├── module-info.java                          # Add: exports org.gedcom7.writer, .writer.context, .writer.date
├── org/gedcom7/parser/                       # Existing parser (unchanged)
│   ├── datatype/                             # Existing datatypes (unchanged)
│   └── internal/                             # Existing internals (unchanged)
└── org/gedcom7/writer/                       # NEW: Writer public API
    ├── GedcomWriter.java                     # Main entry point (AutoCloseable)
    ├── GedcomWriterConfig.java               # Immutable config with factory methods
    ├── GedcomWriteException.java             # Checked exception for strict mode
    ├── WarningHandler.java                   # Functional interface for warnings
    ├── GedcomWriteWarning.java               # Structured warning value object
    ├── Xref.java                             # Cross-reference handle
    ├── context/                              # Typed context classes
    │   ├── CommonContext.java                # Abstract base (escape hatches)
    │   ├── GeneralContext.java               # Fallback context for escape hatches
    │   ├── HeadContext.java                  # HEAD record context
    │   ├── SchemaContext.java                # HEAD.SCHMA context
    │   ├── IndividualContext.java            # INDI record context
    │   ├── PersonalNameContext.java          # NAME substructure context
    │   ├── FamilyContext.java                # FAM record context
    │   ├── EventContext.java                 # BIRT/DEAT/MARR/etc. context
    │   ├── SourceCitationContext.java        # SOUR citation context
    │   ├── SourceContext.java                # SOUR record context
    │   ├── RepositoryContext.java            # REPO record context
    │   ├── MultimediaContext.java            # OBJE record context
    │   ├── SubmitterContext.java             # SUBM record context
    │   ├── NoteContext.java                  # SNOTE/NOTE context
    │   └── AddressContext.java               # ADDR substructure context
    ├── date/                                 # Date builder API
    │   ├── GedcomDateBuilder.java            # Static factory methods
    │   ├── WriterDate.java                   # Immutable date value (renders to string)
    │   ├── Month.java                        # Gregorian/Julian month enum
    │   ├── HebrewMonth.java                  # Hebrew month enum
    │   └── FrenchRepublicanMonth.java        # French Republican month enum
    └── internal/                             # Writer internals (not exported)
        ├── LineEmitter.java                  # Low-level line formatting + OutputStream writing
        ├── ContSplitter.java                 # CONT line splitting logic
        ├── ConcSplitter.java                # CONC line splitting logic (5.5.5)
        ├── AtEscaper.java                    # @@ escaping (leading-only vs all)
        └── XrefGenerator.java               # Auto-generated xref ID counter

src/test/java/org/gedcom7/writer/
├── GedcomWriterTest.java                     # Core writer tests
├── GedcomWriterConfigTest.java               # Config factory methods + builder tests
├── XrefTest.java                             # Xref handle tests
├── RoundTripTest.java                        # Write then parse back
├── ContSplittingTest.java                    # CONT splitting tests
├── ConcSplittingTest.java                    # CONC splitting tests (5.5.5)
├── AtEscapingTest.java                       # @@ escaping tests
├── StrictModeTest.java                       # Strict mode exception tests
├── WarningHandlerTest.java                   # Warning delivery tests
├── context/
│   ├── EscapeHatchTest.java                 # structure()/pointer() tests on typed contexts
│   └── AdditionalContextTest.java           # SourceContext, RepositoryContext, etc.
├── date/
│   ├── GedcomDateBuilderTest.java            # Date construction tests
│   ├── MonthEnumTest.java                    # Month enum coverage
│   └── DateValidationTest.java              # Invalid date rejection tests
├── Gedcom555WriterTest.java                  # GEDCOM 5.5.5 specific tests
├── EdgeCaseTest.java                         # Null values, empty strings, lambda exceptions
└── QuickstartExampleTest.java                # Validates quickstart.md tutorial code
```

**Structure Decision**: The writer lives in a new `org.gedcom7.writer` package hierarchy, parallel to the existing `org.gedcom7.parser`. This follows the existing module's pattern of public packages (`parser`, `parser.datatype`, `parser.validation`) with a `.internal` sub-package for non-exported implementation details. The module-info.java will be updated to export `org.gedcom7.writer`, `org.gedcom7.writer.context`, and `org.gedcom7.writer.date`.

### Design Decisions

**Why new `org.gedcom7.writer` package instead of `org.gedcom7.parser.writer`?**
The writer is a peer to the parser, not a sub-component. Parallel package structure (`parser`/`writer`) communicates this clearly. Both are part of the same module (`org.gedcom7.parser` — module name stays for backward compatibility).

**Why separate `WriterDate` instead of reusing `GedcomDate` from `parser.datatype`?**
The existing `GedcomDate` is a parse result — it holds parsed fields (calendar, year, month, day, epoch) from reading. The writer needs a date that renders to a GEDCOM string with version-aware formatting (GEDCOM 7 `JULIAN` vs 5.5.5 `@#DJULIAN@`). These are separate concerns. The `WriterDate` class is constructed via `GedcomDateBuilder` and renders to string. If future unification is needed, a shared interface can be introduced later (YAGNI for now).

**Why `Consumer<XContext>` lambdas instead of return-self builder pattern?**
Lambdas enforce scoping — when the lambda returns, the structure is closed. A builder pattern (`.birth().date(...).place(...).end()`) requires manual `.end()` calls and is error-prone. Lambdas also match the SAX callback style of the parser.

## Complexity Tracking

No constitution violations to justify. All decisions align with the 7 principles.
