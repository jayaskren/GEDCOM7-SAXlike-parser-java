# Data Model: GEDCOM Version Converter

**Feature**: 007-gedcom-version-converter
**Date**: 2026-03-06

## Entities

### GedcomConverter

The main converter that bridges parser events to writer output.

**Responsibilities**:
- Implements `GedcomHandler` to receive parser events
- Emits GEDCOM lines to the output stream in the target format
- Tracks HEAD state to adapt version-specific substructures
- Accumulates conversion statistics and warnings in ConversionResult

**Key State**:
- `LineEmitter emitter` — writes formatted lines to output stream
- `GedcomConverterConfig config` — converter configuration
- `ConversionResult.Builder resultBuilder` — accumulates counts/warnings
- `boolean inHead` — true while processing HEAD record events
- `boolean skipGedcSubtree` — true while inside HEAD.GEDC (to skip version-specific substructures)
- `boolean skipCharLine` — true to skip HEAD.CHAR in GEDCOM 7 output
- `int skipDepth` — nesting depth tracker for skipping subtrees
- `GedcomHeaderInfo headerInfo` — captured from startDocument for HEAD adaptation

**Lifecycle**: Created per conversion. Not reusable — one instance per convert() call.

### GedcomConverterConfig

Immutable configuration for a conversion operation.

**Fields**:
| Field | Type | Default | Description |
|-------|------|---------|-------------|
| targetVersion | GedcomVersion | (required) | Target GEDCOM version (7.0 or 5.5.5) |
| strict | boolean | false | Stop on first error/warning |
| warningHandler | ConversionWarningHandler | null | Optional callback for warnings |
| lineEnding | String | "\n" | Line ending for output |

**Builder pattern**: `GedcomConverterConfig.builder().targetVersion(...).strict(true).build()`

**Factory methods**:
- `GedcomConverterConfig.toGedcom7()` — target 7.0, lenient
- `GedcomConverterConfig.toGedcom555()` — target 5.5.5, lenient
- `GedcomConverterConfig.toGedcom7Strict()` — target 7.0, strict
- `GedcomConverterConfig.toGedcom555Strict()` — target 5.5.5, strict

### ConversionResult

Immutable summary of a completed conversion.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| sourceVersion | GedcomVersion | Detected source version |
| targetVersion | GedcomVersion | Target version used |
| recordCount | int | Total records converted (excluding HEAD/TRLR) |
| warningCount | int | Number of conversion warnings |
| errorCount | int | Number of parse errors encountered |
| warnings | List\<ConversionWarning\> | Unmodifiable list of conversion warnings |
| parseErrors | List\<GedcomParseError\> | Unmodifiable list of parse errors |

**Builder**: Internal `ConversionResult.Builder` used by converter during processing.

### ConversionWarning

Immutable detail about a conversion-specific warning.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| message | String | Human-readable warning description |
| tag | String | GEDCOM tag that triggered the warning (nullable) |
| lineNumber | int | Source line number (0 if unknown) |

### ConversionWarningHandler (functional interface)

```java
@FunctionalInterface
public interface ConversionWarningHandler {
    void handle(ConversionWarning warning);
}
```

## Relationships

```
GedcomConverter
  ├── uses → GedcomConverterConfig (immutable, provided at construction)
  ├── uses → LineEmitter (created internally for target format)
  ├── implements → GedcomHandler (receives parser events)
  ├── builds → ConversionResult.Builder (accumulates during conversion)
  └── produces → ConversionResult (returned after conversion completes)

GedcomConverterConfig
  ├── contains → GedcomVersion (target version)
  └── contains → ConversionWarningHandler (optional callback)

ConversionResult
  ├── contains → GedcomVersion (source + target)
  ├── contains → List<ConversionWarning>
  └── contains → List<GedcomParseError>
```

## Static Factory: GedcomConverter.convert()

The primary entry point is a static method:

```java
ConversionResult result = GedcomConverter.convert(
    inputStream,
    outputStream,
    GedcomConverterConfig.toGedcom7()
);
```

This creates the reader (auto-detect), the converter handler, and the line emitter internally, then runs the parse-and-convert pipeline.
