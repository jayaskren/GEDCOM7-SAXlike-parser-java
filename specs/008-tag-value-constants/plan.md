# Implementation Plan: Tag and Value Constants

**Branch**: `008-tag-value-constants` | **Date**: 2026-03-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/008-tag-value-constants/spec.md`

## Summary

Add two public utility classes (`GedcomTag` and `GedcomValue`) that provide `public static final String` constants for all standard GEDCOM 7 tags and enumeration values. These classes provide IDE discoverability and compile-time safety without changing parser behavior. Tag constants are organized by record type via nested static classes; value constants are organized by enumeration type. Event substructures (BIRT, DEAT, MARR, etc.) get one additional nesting level for disambiguation. All constants include descriptive Javadoc with `@see` links to relevant `GedcomDataTypes` parser methods.

## Technical Context

**Language/Version**: Java 11+ (matching existing project)
**Primary Dependencies**: None at runtime (zero-dependency library — Constitution Principle VII)
**Storage**: N/A
**Testing**: JUnit 5
**Target Platform**: Any JVM
**Project Type**: Library
**Performance Goals**: N/A (compile-time constants only, zero runtime cost)
**Constraints**: Constants must be `public static final String` for switch/case compatibility. No new runtime dependencies.
**Scale/Scope**: ~10 record types with nested classes, ~6-7 enumeration types with value constants

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. GEDCOM 7 Compliance | PASS | Constants derived from official GEDCOM 7 spec structure definitions. No parsing behavior change. |
| II. SAX-like Event-Driven API | PASS | Constants are a convenience layer; handler API unchanged. |
| III. Mechanical Sympathy | PASS | `static final String` constants are compile-time inlined by javac. Zero allocation, zero runtime cost. |
| IV. Java Best Practices | PASS | Constants use UPPER_SNAKE_CASE. Classes are final with private constructors. Full Javadoc on all public members. |
| V. Test-Driven Development | PASS | Tests will verify constant values match parser-delivered strings and demonstrate usage patterns. |
| VI. Simplicity/YAGNI | PASS | Pure convenience layer — no new abstractions, no behavior change, no DOM/tree model. Justified by universal boilerplate reduction. |
| VII. Zero Dependencies | PASS | No new dependencies. |
| VIII. Independent Verification | PASS | Per-story reviews and final evaluation will be performed. |

No violations. No complexity tracking needed.

## Project Structure

### Documentation (this feature)

```text
specs/008-tag-value-constants/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/org/gedcom7/parser/
├── GedcomTag.java           # NEW: Tag constants with nested record-context classes
├── GedcomValue.java         # NEW: Value constants with nested enumeration-type classes
├── GedcomHandler.java       # EXISTING: unchanged
├── GedcomReader.java        # EXISTING: unchanged
├── datatype/
│   └── GedcomDataTypes.java # EXISTING: unchanged (referenced by @see in constants)
└── validation/
    └── StructureDefinitions.java  # EXISTING: data source for tag/structure mappings

src/test/java/org/gedcom7/parser/
├── GedcomTagTest.java       # NEW: verify tag constant values
├── GedcomValueTest.java     # NEW: verify value constant values
└── TagConstantsUsageTest.java  # NEW: demonstrate handler using constants (SC-001, SC-005)
```

**Structure Decision**: New files added to the existing `org.gedcom7.parser` package alongside `GedcomHandler` — this is where developers already look for the API. Test files in the corresponding test package.
