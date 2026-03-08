# Research: Add API Documentation

## Decision: Documentation Scope

**Decision**: Document Writer, Converter, and Gedzip APIs in the tutorial with concise working examples.

**Rationale**: These three APIs are fully implemented but have zero tutorial coverage. The tutorial is the primary entry point for new users and currently only covers the parser.

**Alternatives considered**:
- Javadoc only — rejected because users expect a tutorial walkthrough, not just API reference
- Separate docs per API — rejected because the tutorial is the established single-document guide

## API Signature Inventory

### GedcomWriter

- Constructor: `GedcomWriter(OutputStream)` and `GedcomWriter(OutputStream, GedcomWriterConfig)`
- Record methods return `Xref`: `individual(Consumer<IndividualContext>)`, `family(Consumer<FamilyContext>)`, `source(Consumer<SourceContext>)`, `repository(Consumer<RepositoryContext>)`, `multimedia(Consumer<MultimediaContext>)`, `submitter(Consumer<SubmitterContext>)`, `sharedNote(Consumer<NoteContext>)`
- All record methods have an overload accepting `String id` for developer-provided xref IDs
- `head(Consumer<HeadContext>)` — write HEAD record
- `trailer()` — write TRLR
- `close()` — writes TRLR if not already written, then closes stream
- Implements `AutoCloseable`
- Escape hatch: `record(String tag, Consumer<GeneralContext>)` for arbitrary record types

**Context types** (inner classes): HeadContext, IndividualContext, FamilyContext, SourceContext, RepositoryContext, MultimediaContext, SubmitterContext, NoteContext, GeneralContext

**Config**: `GedcomWriterConfig.gedcom7()`, `.gedcom7Strict()`, `.gedcom555()`, `.gedcom555Strict()`

### GedcomConverter

- Static utility: `GedcomConverter.convert(InputStream, OutputStream, GedcomConverterConfig)` → `ConversionResult`
- Config: `GedcomConverterConfig.toGedcom7()`, `.toGedcom555()`, `.toGedcom7Strict()`, `.toGedcom555Strict()`
- Result: `ConversionResult.getSourceVersion()`, `.getTargetVersion()`, `.getRecordCount()`, `.getWarningCount()`, `.getErrorCount()`, `.getWarnings()`, `.getParseErrors()`

### GedzipReader

- Constructors: `GedzipReader(Path)` and `GedzipReader(File)`
- `getGedcomStream()` → InputStream for main GEDCOM file
- `getEntryNames()` → unmodifiable Set<String>
- `getEntry(String)` → InputStream or null
- `hasEntry(String)` → boolean
- `isExternalReference(String)` → static boolean
- Implements `Closeable`

## Decision: Tutorial Section Numbering

**Decision**: New sections will be Steps 11, 12, and 13 (Writer, Converter, Gedzip).

**Rationale**: The current tutorial has Steps 1-10. Continuing the sequence maintains consistency.

## Decision: Code Example Style

**Decision**: Each section gets one complete, self-contained example with imports, following existing tutorial style.

**Rationale**: Existing tutorial uses this pattern consistently. Users can copy-paste and run.
