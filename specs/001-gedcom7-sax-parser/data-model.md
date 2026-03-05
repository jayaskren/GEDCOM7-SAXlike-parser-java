# Data Model: GEDCOM 7 SAX-like Streaming Parser

**Branch**: `001-gedcom7-sax-parser` | **Date**: 2026-03-04

## Entity Relationship Overview

```
GedcomReaderConfig ─configures─> GedcomReader
GedcomReader ─uses─> GedcomInputDecoder (pluggable)
GedcomReader ─uses─> GedcomLineTokenizer
GedcomReader ─uses─> PayloadAssembler (pluggable)
GedcomReader ─uses─> AtEscapeStrategy (pluggable)
GedcomReader ─produces─> GedcomLine (internal token)
GedcomReader ─fires events to─> GedcomHandler
GedcomReader ─optionally uses─> StructureDefinitions (validation)

GedcomHandler ─receives─> GedcomHeaderInfo (via startDocument)
GedcomHandler ─receives─> GedcomParseError (via warning/error/fatalError)
GedcomHandler ─receives─> GedcomVersion (via GedcomHeaderInfo)

GedcomHeaderInfo ─contains─> GedcomVersion
GedcomHeaderInfo ─contains─> SCHMA tag-to-URI map

GedcomDataTypes ─produces─> GedcomDate, GedcomDateRange,
  GedcomDatePeriod, GedcomTime, GedcomAge,
  GedcomPersonalName, GedcomCoordinate
```

## Public API Entities

### GedcomReader

The streaming parser entry point.

| Field/Method | Type | Description |
|-------------|------|-------------|
| constructor | (InputStream, GedcomHandler, GedcomReaderConfig) | Creates a configured parser |
| parse() | void | Reads the stream and fires events to the handler |
| close() | void | AutoCloseable; releases the input stream |

**Constraints**:
- Implements `AutoCloseable`
- Single-threaded per instance (no synchronization)
- Does NOT buffer the entire input in memory
- Constructed via public constructor or factory method

**Lifecycle**: Created -> parse() called -> events fire ->
close(). Not reusable after close.

### GedcomReaderConfig

Immutable configuration for parser behavior.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| strict | boolean | false | true = strict mode (all errors fatal), false = lenient |
| maxNestingDepth | int | 1000 | Maximum level depth before fatal error |
| maxLineLength | int | 1_048_576 | Maximum line length in chars before fatal error |
| structureValidation | boolean | false | Enable opt-in structure/cardinality validation |
| inputDecoder | GedcomInputDecoder | UTF-8 | Pluggable encoding strategy |
| payloadAssembler | PayloadAssembler | CONT-only | Pluggable CONT/CONC strategy |
| atEscapeStrategy | AtEscapeStrategy | leading-only | Pluggable @@ decode strategy |

**Constraints**:
- All fields immutable (final)
- Builder pattern or factory methods for construction
- `gedcom7()` factory returns default GEDCOM 7 config
- Future: `gedcom555()` factory for GEDCOM 5.5.5

### GedcomHandler

Callback interface for parse events. Abstract class with
no-op default implementations.

| Method | Parameters | Fires When |
|--------|-----------|------------|
| startDocument | GedcomHeaderInfo header | Before first record event; after HEAD pre-scan |
| endDocument | (none) | After TRLR processed |
| startRecord | int level, String xref, String tag | At each level-0 line (including HEAD) |
| endRecord | String tag | When a record's scope ends |
| startStructure | int level, String xref, String tag, String value, boolean isPointer | At each sub-record line (level > 0, not CONT) |
| endStructure | String tag | When a structure's scope ends |
| warning | GedcomParseError error | Non-fatal issue detected |
| error | GedcomParseError error | Recoverable error (lenient mode continues) |
| fatalError | GedcomParseError error | Unrecoverable error (parsing stops) |

**Constraints**:
- Abstract class with empty default methods (not an interface)
  so consumers override only what they need
- Methods MUST NOT throw checked exceptions

### GedcomHeaderInfo

Immutable value class carrying pre-parsed HEAD metadata.

| Field | Type | Description |
|-------|------|-------------|
| version | GedcomVersion | Parsed from HEAD.GEDC.VERS |
| sourceSystem | String | From HEAD.SOUR (null if absent) |
| sourceVersion | String | From HEAD.SOUR.VERS (null if absent) |
| sourceName | String | From HEAD.SOUR.NAME (null if absent) |
| defaultLanguage | String | From HEAD.LANG (null if absent) |
| schemaMap | Map<String, String> | Unmodifiable map: extension tag -> URI from HEAD.SCHMA.TAG |

**Constraints**:
- All fields immutable, set at construction
- schemaMap is `Collections.unmodifiableMap()`
- Null fields use null (not Optional) for simplicity

### GedcomVersion

Immutable value class for parsed GEDCOM version.

| Field | Type | Description |
|-------|------|-------------|
| major | int | Major version number |
| minor | int | Minor version number |
| patch | int | Patch version (-1 if absent) |

| Method | Return | Description |
|--------|--------|-------------|
| isGedcom7() | boolean | major == 7 |
| isGedcom5() | boolean | major == 5 |
| toString() | String | e.g., "7.0" or "7.0.1" |

**Constraints**:
- Immutable, fields final
- Implements equals/hashCode/toString

### GedcomParseError

Immutable value class for error reporting.

| Field | Type | Description |
|-------|------|-------------|
| severity | Severity enum | WARNING, ERROR, FATAL |
| lineNumber | int | 1-based line number |
| byteOffset | long | 0-based byte offset from stream start |
| message | String | Human-readable error description |
| rawLine | String | Raw line content (null if not applicable) |

**Constraints**:
- Immutable, fields final
- Severity is a nested enum: `WARNING`, `ERROR`, `FATAL`

## Internal Entities

### GedcomInputDecoder (interface)

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| decode | InputStream | Reader (or char stream) | Converts bytes to characters |
| handleBom | byte[] header | boolean | Returns true if BOM detected and consumed |

**Implementations**:
- `Utf8InputDecoder` (default): UTF-8 with BOM detection
- Future: `Utf16InputDecoder`, `AnselInputDecoder`

### GedcomLineTokenizer

Reads character stream and produces `GedcomLine` tokens.

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| nextLine | (none) | GedcomLine or null | Parses next line; null at EOF |

**State**:
- Current line number (int)
- Current byte offset (long)
- Internal read buffer (reusable)

**Constraints**:
- Shared across all GEDCOM versions (permissive charset)
- Hot path; minimizes allocations
- Reuses internal GedcomLine instance (caller must copy
  if needed across calls)

### GedcomLine (internal mutable token)

| Field | Type | Description |
|-------|------|-------------|
| level | int | Parsed level number |
| xref | String or null | Cross-reference identifier |
| tag | String | Tag string |
| value | String or null | Line value (after @@ unescaping) |
| isPointer | boolean | True if value is a pointer reference |
| lineNumber | int | Source line number |
| byteOffset | long | Source byte offset |

**Constraints**:
- Mutable and reused in the hot loop (mechanical sympathy)
- Internal only; never exposed to public API
- Fields set by tokenizer for each line

### PayloadAssembler (interface)

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| isPseudoStructure | String tag, int parentLevel, int childLevel | boolean | Returns true if this tag is a continuation pseudo-structure |
| assemblePayload | String parentValue, List<String> continuationValues | String | Joins values into final payload |

**Implementations**:
- `ContOnlyAssembler` (default): CONT recognized; CONC ignored
- Future: `ContConcAssembler`: Both CONT and CONC recognized

### AtEscapeStrategy (interface)

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| unescape | String lineStr | String | Decodes @@ escapes |

**Implementations**:
- `LeadingAtEscapeStrategy` (default): Only leading `@@`
- Future: `AllPositionAtEscapeStrategy`: All `@@` occurrences

### StructureDefinitions (generated, internal)

Build-time-generated class containing GEDCOM 7 structure
rules.

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| resolveStructure | String superstructureUri, String tag | String (uri) or null | Tag-in-context resolution |
| getCardinality | String superstructureUri, String substructureUri | Cardinality | {0:1}, {0:M}, {1:1}, {1:M} |
| getPayloadType | String structureUri | String or null | Expected data type URI |
| getEnumValues | String enumSetUri | Set<String> | Valid enum members |

**Constraints**:
- Generated from TSV files at build time
- Loaded only when validation is enabled
- Uses compact data structures (arrays, not HashMap per
  entry) for cache-friendly lookup

## Data Type Value Classes

All immutable, all in `org.gedcom7.parser.datatype` package.

### GedcomDate

| Field | Type | Description |
|-------|------|-------------|
| calendar | String | "GREGORIAN", "JULIAN", "FRENCH_R", "HEBREW", or extension |
| year | int | Year number |
| month | String or null | Month tag (e.g., "APR", "VEND") |
| day | int | Day number (-1 if absent) |
| epoch | String or null | "BCE" or null |

### GedcomDateRange

| Field | Type | Description |
|-------|------|-------------|
| type | RangeType | BET_AND, BEF, AFT |
| date1 | GedcomDate | First boundary |
| date2 | GedcomDate or null | Second boundary (BET_AND only) |

### GedcomDatePeriod

| Field | Type | Description |
|-------|------|-------------|
| type | PeriodType | FROM, TO, FROM_TO |
| from | GedcomDate or null | Start date |
| to | GedcomDate or null | End date |

### GedcomTime

| Field | Type | Description |
|-------|------|-------------|
| hour | int | 0-23 |
| minute | int | 0-59 |
| second | int | 0-59 (-1 if absent) |
| fraction | String or null | Fractional seconds (raw string) |
| utc | boolean | True if Z suffix present |

### GedcomAge

| Field | Type | Description |
|-------|------|-------------|
| bound | char | '>' or '<' or '\0' (no bound) |
| years | int | Years component (-1 if absent) |
| months | int | Months component (-1 if absent) |
| weeks | int | Weeks component (-1 if absent) |
| days | int | Days component (-1 if absent) |

### GedcomPersonalName

| Field | Type | Description |
|-------|------|-------------|
| fullName | String | Complete name string |
| surname | String or null | Extracted from `/` delimiters |
| givenPrefix | String or null | Text before first `/` |
| surnameSuffix | String or null | Text after last `/` |

### GedcomCoordinate

| Field | Type | Description |
|-------|------|-------------|
| direction | char | 'N', 'S', 'E', or 'W' |
| degrees | double | Decimal degrees |

## State Machine: Parser Event Emission

```
INITIAL
  │
  ├─ (read HEAD) ──> PRE_SCAN_HEAD
  │                    │
  │                    ├─ (scan HEAD substructures for
  │                    │   GEDC.VERS, SOUR, LANG, SCHMA)
  │                    │
  │                    ├─ fire startDocument(GedcomHeaderInfo)
  │                    │
  │                    └─ fire startRecord(HEAD) ──> IN_RECORD
  │
IN_RECORD
  │
  ├─ (level > 0 line, not CONT) ──> fire startStructure
  │     └─ push to level stack
  │
  ├─ (CONT line) ──> append to current payload
  │
  ├─ (level decreases) ──> fire endStructure (pop stack)
  │
  ├─ (level 0 line) ──> fire endRecord for current
  │     │                 fire startRecord for new
  │     └─ (if TRLR) ──> fire endRecord(TRLR-parent)
  │                       fire endDocument ──> DONE
  │
DONE
  │
  └─ (EOF) ──> close resources
```

## Validation Layer State (opt-in)

When `structureValidation=true`, the parser additionally
tracks:

- **Context stack**: Stack of `(structureUri, tag)`
  representing the current nesting path
- **Sibling counts**: For each active parent, a map of
  `substructureUri -> count` to check cardinality
- On each `startStructure`:
  1. Resolve `(parentUri, tag) -> childUri` via
     StructureDefinitions
  2. If no match and tag is not extension: emit warning
  3. Increment sibling count for childUri under parentUri
  4. If count exceeds max cardinality: emit warning/error
- On each `endRecord` / `endStructure`:
  1. Check required substructures (`{1:1}`, `{1:M}`) have
     count >= 1; emit warning if missing
