# Implementation Plan: Add API Documentation

**Branch**: `010-add-api-docs` | **Date**: 2026-03-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/010-add-api-docs/spec.md`

## Summary

Add tutorial sections for the Writer, Converter, and Gedzip APIs, update the tutorial summary table to cover all library capabilities, and preserve documentation improvements (version-difference tables, 5.5.5 support phrasing) carried over from the 007/009 branches.

## Technical Context

**Language/Version**: Java 11+ (matching existing project)
**Primary Dependencies**: None at runtime (zero-dependency library — Constitution Principle VII)
**Storage**: N/A (documentation-only feature)
**Testing**: Manual verification by inspection (no test code needed for docs)
**Target Platform**: Documentation files (Markdown)
**Project Type**: Library documentation
**Performance Goals**: N/A
**Constraints**: Documentation must accurately reflect current API; code examples must be valid against current source
**Scale/Scope**: 3 new tutorial sections + 1 summary table update + 3 files with carried-over improvements

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. GEDCOM 7 Specification Compliance | PASS | Documentation accurately describes both GEDCOM 7 and 5.5.5 behavior |
| II. SAX-like Event-Driven API | PASS | Writer/Converter docs complement the existing parser docs |
| III. Mechanical Sympathy | N/A | No runtime code changes |
| IV. Java Best Practices | PASS | Code examples follow Java conventions |
| V. Test-Driven Development | N/A | Documentation-only; no behavioral code to test. Review agents verify accuracy. |
| VI. Simplicity and YAGNI | PASS | Only documenting existing APIs, no new features |
| VII. Zero External Runtime Dependencies | N/A | No runtime code changes |
| VIII. Independent Verification | PASS | Per-story review + final evaluation will verify doc accuracy against source |

All gates pass. No violations to justify.

## Project Structure

### Documentation (this feature)

```text
specs/010-add-api-docs/
├── plan.md              # This file
├── research.md          # Phase 0 output (API signatures from source)
├── spec.md              # Feature specification
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
docs/
├── tutorial.md          # PRIMARY: New sections added here (Steps 11-13)
├── architecture.md      # Carried-over improvements preserved
README.md                # Carried-over improvements preserved
```

**Structure Decision**: This feature modifies only documentation files. The tutorial is the primary target. README and architecture docs have carried-over improvements that must be committed.

## Key API Signatures (verified from source)

### GedcomWriter

The writer uses a lambda-scoped context pattern with typed context classes:

- `GedcomWriter(OutputStream)` — constructor with default GEDCOM 7 config
- `GedcomWriter(OutputStream, GedcomWriterConfig)` — constructor with explicit config
- `head(Consumer<HeadContext>)` — write HEAD record
- `individual(Consumer<IndividualContext>)` — write INDI record (auto xref), returns `Xref`
- `individual(String id, Consumer<IndividualContext>)` — write INDI with specific xref
- `family(Consumer<FamilyContext>)` / `family(String id, ...)` — write FAM record
- `source(Consumer<SourceContext>)` / `source(String id, ...)` — write SOUR record
- `repository(Consumer<RepositoryContext>)` / `repository(String id, ...)` — write REPO record
- `multimedia(Consumer<MultimediaContext>)` / `multimedia(String id, ...)` — write OBJE record
- `submitter(Consumer<SubmitterContext>)` / `submitter(String id, ...)` — write SUBM record
- `sharedNote(Consumer<NoteContext>)` / `sharedNoteWithText(String text, ...)` — write SNOTE record
- `record(String tag, Consumer<GeneralContext>)` — escape hatch for arbitrary record types
- `trailer()` — write TRLR
- `close()` — writes TRLR if not written, closes stream
- Implements `AutoCloseable`

**Context types**: HeadContext, IndividualContext, FamilyContext, SourceContext, RepositoryContext, MultimediaContext, SubmitterContext, NoteContext, GeneralContext

**Configuration**:
- `GedcomWriterConfig.gedcom7()` — GEDCOM 7 defaults
- `GedcomWriterConfig.gedcom7Strict()` — strict GEDCOM 7
- `GedcomWriterConfig.gedcom555()` — GEDCOM 5.5.5 defaults (CONC enabled)
- `GedcomWriterConfig.gedcom555Strict()` — strict 5.5.5

### GedcomConverter

Static utility class (private constructor):

- `GedcomConverter.convert(InputStream, OutputStream, GedcomConverterConfig)` — returns `ConversionResult`

**ConversionResult** getters:
- `.getSourceVersion()`, `.getTargetVersion()`, `.getRecordCount()`, `.getWarningCount()`, `.getErrorCount()`, `.getWarnings()`, `.getParseErrors()`

**Configuration**:
- `GedcomConverterConfig.toGedcom7()` — convert to GEDCOM 7 (lenient)
- `GedcomConverterConfig.toGedcom555()` — convert to GEDCOM 5.5.5 (lenient)
- `GedcomConverterConfig.toGedcom7Strict()` — strict GEDCOM 7
- `GedcomConverterConfig.toGedcom555Strict()` — strict 5.5.5

### GedzipReader

- `GedzipReader(Path)` and `GedzipReader(File)` — constructors
- `getGedcomStream()` → InputStream for the main GEDCOM file
- `getEntryNames()` → unmodifiable Set<String> of archive entries
- `getEntry(String)` → InputStream or null if not found
- `hasEntry(String)` → boolean
- `isExternalReference(String)` → static boolean
- Implements `Closeable`
