# Architecture

This document describes the internal design of the GEDCOM SAX-like parser. The library supports both GEDCOM 7 and GEDCOM 5.5.5 for parsing, writing, and version conversion.

## Design Principles

1. **Streaming** -- the parser never holds the entire file in memory. Lines are read, tokenized, and emitted one at a time.
2. **Layered** -- each layer has a single responsibility and communicates through narrow interfaces.
3. **Pluggable** -- version-specific behavior (encoding, payload assembly, escape handling) is isolated behind strategy interfaces. Both GEDCOM 7 and 5.5.5 strategies are provided, and custom strategies can be injected.
4. **Zero dependencies** -- the library uses only the JDK.

## Layer Diagram

```
 InputStream
      |
      v
 ┌──────────────────┐
 │ GedcomInputDecoder│   Detects/strips BOM, wraps InputStream -> Reader
 │  (Utf8InputDecoder)│
 └────────┬─────────┘
          |  Reader
          v
 ┌──────────────────┐
 │GedcomLineTokenizer│   Reads lines, parses into GedcomLine tokens
 │                   │   Handles LF/CR/CRLF, leading whitespace, max line length
 └────────┬─────────┘
          |  GedcomLine (level, xref, tag, value, isPointer)
          v
 ┌──────────────────┐
 │   GedcomReader    │   Orchestrator -- manages nesting stack, HEAD pre-scan,
 │                   │   CONT assembly, validation, cross-ref tracking,
 │                   │   and fires events to the GedcomHandler
 └────────┬─────────┘
          |  events (startRecord, startStructure, ...)
          v
 ┌──────────────────┐
 │  GedcomHandler    │   User-supplied callback handler
 │    (your code)    │
 └──────────────────┘
```

## Key Components

### Public API (`org.gedcom7.parser`)

| Class | Role |
|-------|------|
| `GedcomReader` | Entry point. Takes an `InputStream`, a `GedcomHandler`, and a `GedcomReaderConfig`. Call `parse()` to run. Implements `AutoCloseable`. |
| `GedcomHandler` | Abstract class with no-op defaults. Override the callbacks you need. |
| `GedcomReaderConfig` | Immutable configuration. Factory methods `gedcom7()` and `gedcom7Strict()`, or use the `Builder` for custom settings. |
| `GedcomHeaderInfo` | Immutable snapshot of HEAD metadata (version, source system, language, SCHMA map). Delivered via `startDocument()`. |
| `GedcomVersion` | Parsed version number (e.g., 7.0, 5.5.1). Provides `isGedcom7()` and `isGedcom5()`. |
| `GedcomParseError` | Describes a warning, error, or fatal error. Includes severity, line number, byte offset, message, and raw line content. |
| `GedcomFatalException` | Unchecked exception wrapping a `GedcomParseError`. Thrown in strict mode or for unrecoverable errors. |
| `GedcomTag` | String constants for GEDCOM tags, organized by record type. Nested classes: `Indi`, `Fam`, `Sour`, `Repo`, `Obje`, `Snote`, `Subm`, `Head`, event sub-classes like `Indi.Birt`, `Fam.Marr`, and common substructure classes: `Plac`, `Map`, `Date`, `Addr`, `File`, `Form`, `Gedc`, `Name`, `Refn`, `Exid`, `Asso`, `Famc`, `Chan`, `Crea`, `SourCitation`, `Schma`. |
| `GedcomValue` | String constants for GEDCOM enumeration values. Nested classes: `Sex`, `NameType`, `Pedi`, `Resn`, `Role`, `Medi`, `Adop`. |

### Internal Layer (`org.gedcom7.parser.internal`)

This package is not exported by the JPMS module. Users cannot depend on it.

| Interface / Class | Role |
|-------------------|------|
| `GedcomInputDecoder` | Strategy: converts `InputStream` to `Reader`. |
| `Utf8InputDecoder` | GEDCOM 7 implementation: detects/strips UTF-8 BOM via `PushbackInputStream`. |
| `GedcomLineTokenizer` | Reads the character stream line by line, parses each line into a `GedcomLine` token. Handles line endings, leading whitespace tolerance, and max line length enforcement. |
| `GedcomLine` | Mutable, reusable token holding one parsed line's fields: level, xref, tag, value, isPointer, lineNumber, rawLine. |
| `PayloadAssembler` | Strategy: recognizes pseudo-structures and assembles multi-line payloads. |
| `ContOnlyAssembler` | GEDCOM 7 implementation: recognizes CONT (not CONC) and joins with newline. |
| `AtEscapeStrategy` | Strategy: unescapes `@@` sequences in line values. |
| `LeadingAtEscapeStrategy` | GEDCOM 7 implementation: only a leading `@@` is decoded to `@`. |

### Data Types (`org.gedcom7.parser.datatype`)

| Class | Description |
|-------|-------------|
| `GedcomDataTypes` | Static utility with parse methods for all GEDCOM 7 data types. |
| `GedcomDate` | Single date (calendar, year, month, day, epoch). |
| `GedcomDateRange` | Range or approximate date (BET...AND, BEF, AFT, ABT, CAL, EST, EXACT). |
| `GedcomDatePeriod` | Period (FROM...TO, FROM, TO). |
| `GedcomTime` | Time of day (hours, minutes, seconds, milliseconds, UTC flag). |
| `GedcomAge` | Age duration (years, months, weeks, days, modifier). |
| `GedcomPersonalName` | Personal name parsed from `Given /Surname/ Suffix` format. |
| `GedcomCoordinate` | Geographic coordinate with direction (N/S/E/W) and decimal value. |

### Writer (`org.gedcom7.writer`)

| Class | Role |
|-------|------|
| `GedcomWriter` | Writes well-formed GEDCOM output. Takes an `OutputStream` and a `GedcomWriterConfig`. Methods: `writeRecord()`, `writeStructure()`, `close()`. |
| `GedcomWriterConfig` | Immutable configuration. Factory methods `gedcom7()`, `gedcom7Strict()`, `gedcom555()`, `gedcom555Strict()`, or use the `Builder`. Controls version, line length, CONC splitting, and `@@` escaping. |

### Converter (`org.gedcom7.converter`)

| Class | Role |
|-------|------|
| `GedcomConverter` | Static utility that converts GEDCOM files between versions 5.5.5 and 7.0. Reads with auto-detection, writes in the target format. |
| `GedcomConverterConfig` | Immutable configuration for conversions. Factory methods `toGedcom7()` and `toGedcom555()`. |
| `ConversionResult` | Result of a conversion: line counts, warnings, and success status. |

### Validation (`org.gedcom7.parser.validation`)

| Class | Description |
|-------|-------------|
| `StructureDefinitions` | HashMap-based lookup tables for valid substructures and cardinality rules. Used when `structureValidation` is enabled in config. |

## Parse Lifecycle

When you call `reader.parse()`, the following happens:

```
1. Decode input
   GedcomInputDecoder converts InputStream -> Reader (strips BOM if present)

2. Pre-scan HEAD
   Read lines until the first non-HEAD level-0 record.
   Extract: version, source system, source name, source version,
            default language, SCHMA tag-to-URI mappings.
   Build GedcomHeaderInfo. In auto-detect mode, switch to
   GEDCOM 5.5.5 strategies if version is 5.x.

3. Fire startDocument(headerInfo)

4. Replay HEAD as events
   The HEAD lines collected in step 2 are replayed through processLine()
   so the handler receives startRecord(HEAD) and its substructures.

5. Process remaining lines
   For each line from the tokenizer:
     a. Validate characters (banned C0/C1 controls, DEL, surrogates)
     b. Warn on leading whitespace
     c. Detect CONT pseudo-structures and assemble payloads
     d. Validate level jumps (increase > 1 is an error)
     e. Flush pending structure: unescape @@, track xrefs, resolve
        extension URIs, fire startRecord or startStructure
     f. Pop closed elements from the nesting stack, fire endRecord
        or endStructure
     g. Check max nesting depth
     h. Validate TRLR (no value, no xref, no content after)
     i. Optionally validate structure context and cardinality

6. Close open elements
   Pop everything remaining from the stack, firing end events.

7. Post-parse checks
   Warn if last record was not TRLR.
   Warn for each unresolved cross-reference.

8. Fire endDocument()
```

## Nesting Stack

The parser maintains a `List<StackEntry>` that tracks open records and structures. Each entry stores:

- `level` -- the GEDCOM level number
- `tag` -- the structure tag
- `isRecord` -- true for level-0 entries
- `contextId` -- the structure's identity for validation lookups
- `childCounts` -- map of child structure IDs to occurrence counts (for cardinality)

When a new line arrives at level N, all stack entries with level >= N are popped (firing `endStructure` or `endRecord`), then the new entry is pushed.

## Error Handling

The parser supports two modes:

**Lenient (default):** Errors are reported via `handler.error()` and parsing continues. The handler receives all events that can be extracted from the file.

**Strict:** The first error throws `GedcomFatalException`. The handler still receives the `error()` callback before the exception.

Errors include context: line number (1-based), byte offset, descriptive message, and the raw line content.

## Pluggable Strategies

The three strategy interfaces isolate version-specific behavior. Both GEDCOM 7 and 5.5.5 implementations are provided:

| Strategy | GEDCOM 7 | GEDCOM 5.5.5 |
|----------|----------|--------------|
| `GedcomInputDecoder` | UTF-8 only, strip BOM | BOM detection with HEAD.CHAR fallback |
| `PayloadAssembler` | CONT only (join with newline) | CONT + CONC (`ContConcAssembler`: CONC joins without newline) |
| `AtEscapeStrategy` | Leading `@@` only (`LeadingAtEscapeStrategy`) | All `@@` decoded to `@` (`AllAtEscapeStrategy`) |

Strategies are wired through `GedcomReaderConfig`. Use `gedcom7()`, `gedcom555()`, or `autoDetect()` factory methods. Custom strategies can also be injected at construction time.

## GEDCOM 5.5.5 vs 7: Version Differences

The library handles all version-specific differences automatically based on the chosen configuration. This table summarizes every behavioral difference.

### Parsing

| Behavior | GEDCOM 7 | GEDCOM 5.5.5 |
|----------|----------|--------------|
| Line continuations | CONT only (joined with newline) | CONT + CONC (CONC joins without newline) |
| `@@` unescaping | Leading `@@` only | All `@@` occurrences |
| Bare `@` validation | Not checked | Error if `@` is not escaped as `@@` |
| Input encoding | UTF-8 only, strip BOM | BOM-detecting: UTF-8, UTF-16 BE, UTF-16 LE |
| BOM requirement | Optional | Required (warning in lenient, fatal in strict) |
| HEAD.CHAR tag | Not used | Required (warning if missing; assumes UTF-8) |
| Xref length limit | No specific limit | 22 characters including `@` delimiters |
| Max line length (strict) | 1,048,576 chars | 255 chars |

### Writing

| Behavior | GEDCOM 7 | GEDCOM 5.5.5 |
|----------|----------|--------------|
| `@` escaping | Leading `@` only | All `@` characters doubled |
| CONC splitting | Never | Lines exceeding 255 chars split into CONC at level+1 |
| HEAD.GEDC.FORM | Not emitted | `LINEAGE-LINKED` |
| HEAD.CHAR | Not emitted | `UTF-8` |
| Calendar date format | `JULIAN date`, `FRENCH_R` | `@#DJULIAN@ date`, `@#DFRENCH R@ date` |

### Auto-Detection

When using `GedcomReaderConfig.autoDetect()`, the parser reads HEAD.GEDC.VERS during the HEAD pre-scan:

- **Version 7.x** -- applies GEDCOM 7 strategies (CONT only, leading `@@`)
- **Version 5.x** -- switches to GEDCOM 5.5.5 strategies (CONT+CONC, all `@@`, BOM detection, xref length checks). If the version is not exactly 5.5.5, a warning is issued.

### Conversion

`GedcomConverter` handles all of the above automatically when converting between versions. Additional conversion behaviors:

- HEAD record is regenerated for the target version (GEDC, FORM, CHAR)
- SCHMA (GEDCOM 7 only) is preserved as-is when converting to 5.5.5, with a conversion warning
- All output formatting (line length, escaping, CONC splitting, date syntax) adapts to the target version

## Module Structure

```
module org.gedcom7.parser {
    exports org.gedcom7.parser;           // public API
    exports org.gedcom7.parser.datatype;  // data type value classes
    exports org.gedcom7.parser.validation;// structure definitions
    // org.gedcom7.parser.internal is NOT exported
}
```

## Thread Safety

Each `GedcomReader` instance is confined to a single thread. The internal nesting stack, pending structure, and cross-reference sets are all instance state. To parse multiple files concurrently, create separate `GedcomReader` instances.
