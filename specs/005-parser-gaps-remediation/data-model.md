# Data Model: Parser Gaps Remediation

**Feature**: 005-parser-gaps-remediation
**Date**: 2026-03-05

## New Entities

### GedcomDateValue (Interface)

**Package**: `org.gedcom7.parser.datatype`
**Purpose**: Common interface for all parsed date value representations.

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getType()` | `DateValueType` | Enum discriminator: EXACT, RANGE, PERIOD, APPROXIMATE, UNPARSEABLE |
| `getOriginalText()` | `String` | The original unparsed date string |

**Implementing classes** (existing, modified to implement interface):
- `GedcomDate` → `DateValueType.EXACT`
- `GedcomDateRange` → `DateValueType.RANGE` or `DateValueType.APPROXIMATE`
- `GedcomDatePeriod` → `DateValueType.PERIOD`

### DateValueType (Enum)

**Package**: `org.gedcom7.parser.datatype`
**Purpose**: Discriminator for date value types.

| Value | Description |
|-------|-------------|
| `EXACT` | A specific date (e.g., `6 APR 1952`) |
| `RANGE` | A date range: `BET...AND`, `BEF`, `AFT` (per GEDCOM 7 DateRange production) |
| `PERIOD` | A date period (e.g., `FROM 1900 TO 1910`) |
| `APPROXIMATE` | An approximate date: `ABT`, `CAL`, `EST` |
| `UNPARSEABLE` | A date string that could not be parsed |

### SimpleGedcomHandler (Class)

**Package**: `org.gedcom7.parser`
**Extends**: `GedcomHandler`
**Purpose**: Convenience adapter that unifies record/structure events.

| Method | Parameters | Description |
|--------|------------|-------------|
| `onStructure()` | `int level, String xref, String tag, String value` | Unified callback for both records and substructures |
| `onEndStructure()` | `int level, String tag` | Unified callback for both endRecord and endStructure |

**Overridden methods** (final, delegate to unified callbacks):
- `startRecord(int, String, String, String)` → calls `onStructure(level, xref, tag, value)`
- `endRecord(String)` → calls `onEndStructure(0, tag)` (level always 0 for records)
- `startStructure(int, String, String, String, boolean, String)` → calls `onStructure(level, xref, tag, value)`
- `endStructure(String)` → calls `onEndStructure(currentLevel, tag)`

## Modified Entities

### GedcomHandler (Modified)

**New method added**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `startRecord(int level, String xref, String tag, String value)` | `void` | Default implementation delegates to existing 3-param `startRecord` |

### GedcomDate, GedcomDateRange, GedcomDatePeriod (Modified)

Each class adds: `implements GedcomDateValue`

### StructureDefinitions (Modified)

**New static method**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `isRequired(String cardinality)` | `boolean` | Returns true if min cardinality > 0 (e.g., `{1:1}`, `{1:M}`) |
| `getRequiredChildren(String contextId)` | `Map<String, String>` | Returns map of required child structureIds and their cardinalities |

## Relocated Interfaces (internal → spi)

These interfaces move from `org.gedcom7.parser.internal` to
`org.gedcom7.parser.spi`:

| Interface | Methods | Description |
|-----------|---------|-------------|
| `GedcomInputDecoder` | `decode(InputStream): Reader` | Byte-to-character decoding strategy |
| `PayloadAssembler` | `isPseudoStructure(String): boolean`, `getSeparator(): String` | Multi-line payload assembly strategy |
| `AtEscapeStrategy` | `unescape(String): String` | @@ escape handling strategy |

Implementations remain in `internal` package.

## Relationships

```
GedcomDateValue (interface)
├── GedcomDate (implements)
├── GedcomDateRange (implements)
└── GedcomDatePeriod (implements)

GedcomHandler (abstract class)
└── SimpleGedcomHandler (extends)

GedcomReaderConfig.Builder
├── uses GedcomInputDecoder (from spi)
├── uses PayloadAssembler (from spi)
└── uses AtEscapeStrategy (from spi)
```
