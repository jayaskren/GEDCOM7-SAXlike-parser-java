# Data Model: GEDZip Support and GEDCOM 5.5.5 Compatibility

**Date**: 2026-03-05 | **Branch**: `002-gedzip-gedcom555`

## Modified Entities

### GedcomReaderConfig (modified)

Immutable configuration with new factory methods and an auto-detect flag.

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| strict | boolean | false | Existing |
| maxNestingDepth | int | 1000 | Existing |
| maxLineLength | int | 1,048,576 | Existing. 5.5.5 factory sets to 255 in strict mode |
| structureValidation | boolean | false | Existing |
| inputDecoder | Object | null | Existing (package-private). 5.5.5 factory sets BomDetectingDecoder |
| payloadAssembler | Object | null | Existing (package-private). 5.5.5 factory sets ContConcAssembler |
| atEscapeStrategy | Object | null | Existing (package-private). 5.5.5 factory sets AllAtEscapeStrategy |
| **autoDetect** | **boolean** | **false** | **NEW.** When true, strategies are swapped after HEAD version scan |

**New factory methods**:
- `gedcom555()` → lenient 5.5.5 config (BomDetectingDecoder, ContConcAssembler, AllAtEscapeStrategy, maxLineLength=1MB)
- `gedcom555Strict()` → strict 5.5.5 config (same strategies, maxLineLength=255, strict=true)
- `autoDetect()` → lenient auto-detect (BomDetectingDecoder, autoDetect=true, default GEDCOM 7 strategies as fallback)
- `autoDetectStrict()` → strict auto-detect

### GedcomHeaderInfo (modified)

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| version | GedcomVersion | required | Existing |
| sourceSystem | String | null | Existing |
| sourceVersion | String | null | Existing |
| sourceName | String | null | Existing |
| defaultLanguage | String | null | Existing |
| schemaMap | Map<String, String> | empty | Existing |
| **characterEncoding** | **String** | **null** | **NEW.** Value of HEAD.CHAR (e.g., "UTF-8", "UNICODE"). Null for GEDCOM 7. |

### PayloadAssembler (modified interface)

```java
// Method signature change:
String assemblePayload(String existing, String continuationValue, String tag);
```

All implementations updated to accept the tag parameter.

## New Entities

### GedzipReader

Public class wrapping a GEDZip archive (`.gdz` ZIP file).

| Field | Type | Notes |
|-------|------|-------|
| zipFile | ZipFile | The underlying ZIP archive |
| gedcomEntry | ZipEntry | The `gedcom.ged` entry (validated on construction) |

**Lifecycle**: Created → used → closed. Implements `AutoCloseable`.

**Invariants**:
- Construction fails with `IOException` if `gedcom.ged` entry is not found
- `getEntry(path)` percent-decodes the path before matching
- `getEntry(path)` returns null if entry not found
- All entry names are case-sensitive
- Thread safety: not thread-safe (same as GedcomReader)

### BomDetectingDecoder

Internal strategy implementing `GedcomInputDecoder`.

| Field | Type | Notes |
|-------|------|-------|
| detectedCharset | Charset | Set during decode(): UTF-8, UTF-16BE, or UTF-16LE |
| bomFound | boolean | Whether a BOM was detected |

**Detection logic**:
1. Read up to 3 bytes via PushbackInputStream
2. Check for UTF-16 BE BOM (FE FF) — 2 bytes
3. Check for UTF-16 LE BOM (FF FE) — 2 bytes
4. Check for UTF-8 BOM (EF BB BF) — 3 bytes
5. If no BOM: push back bytes, default to UTF-8

### ContConcAssembler

Internal strategy implementing `PayloadAssembler`.

| Behavior | Tag | Join |
|----------|-----|------|
| isPseudoStructure | CONT → true, CONC → true | — |
| assemblePayload | CONT | existing + "\n" + value |
| assemblePayload | CONC | existing + value (no separator) |

### AllAtEscapeStrategy

Internal strategy implementing `AtEscapeStrategy`.

| Behavior | Input | Output |
|----------|-------|--------|
| unescape | null | null |
| unescape | "@@user@@" | "@user@" |
| unescape | "no escapes" | "no escapes" |

Uses `value.replace("@@", "@")` — replaces all occurrences.

## Entity Relationships

```
GedzipReader ──opens──> ZipFile
    │
    ├── getGedcomStream() ──> InputStream (gedcom.ged entry)
    │                              │
    │                              v
    │                        GedcomReader ──uses──> GedcomReaderConfig
    │                              │                     │
    │                              │                     ├── GedcomInputDecoder (BomDetectingDecoder)
    │                              │                     ├── PayloadAssembler (ContConcAssembler)
    │                              │                     └── AtEscapeStrategy (AllAtEscapeStrategy)
    │                              │
    │                              └── fires events to ──> GedcomHandler
    │
    └── getEntry(path) ──> InputStream (media file)
```
