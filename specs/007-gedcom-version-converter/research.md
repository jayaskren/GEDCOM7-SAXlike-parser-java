# Research: GEDCOM Version Converter

**Feature**: 007-gedcom-version-converter
**Date**: 2026-03-06

## Decision 1: Parser-to-Writer Bridge Architecture

**Decision**: Use direct `LineEmitter` access for streaming line-by-line conversion.

**Rationale**: The converter receives SAX-like events (startRecord, startStructure, endStructure, endRecord) one at a time in depth-first order. Each `startRecord` or `startStructure` event maps to exactly one GEDCOM output line. By emitting lines directly through the writer's `LineEmitter`, we avoid:
- Buffering entire records (which would break streaming for large records with many substructures)
- Complex stack management to reconstruct lambda-nesting for the writer's `Consumer<Context>` API
- Unnecessary object allocation in the hot path

The `LineEmitter` already handles:
- @-escaping (leading-@ for GEDCOM 7, all-@@ for 5.5.5)
- CONC splitting for 5.5.5 (max 255 chars)
- Line ending normalization
- Multi-line value emission via `emitValueWithCont()`

**Alternatives considered**:
1. **Use GedcomWriter's public API** (record(), individual(), etc.): Rejected — the typed context classes (IndividualContext, FamilyContext) require knowing the record type at compile time, but the converter processes arbitrary records generically. The escape-hatch `record()` method requires a `Consumer<GeneralContext>` lambda, which doesn't map cleanly to the streaming event model.
2. **Add imperative start/end methods to GedcomWriter**: Rejected — would require significant changes to the writer's design and break the existing lambda-based API pattern. Over-engineering for a single consumer.
3. **Buffer entire records and replay through writer**: Rejected — violates streaming requirement (FR-007). A record could have thousands of substructures.

## Decision 2: HEAD Record Handling

**Decision**: Special-case HEAD in `startDocument(GedcomHeaderInfo)` using the writer's `head()` method, then replay remaining HEAD substructures that aren't handled by the writer's auto-generation.

**Rationale**: The writer's `head()` method auto-generates GEDC.VERS and HEAD.CHAR (for 5.5.5). The parser's `startDocument` provides pre-parsed metadata (source system, version, language, SCHMA map). The converter:
1. Calls `writer.head()` to emit the HEAD record with correct version substructures
2. Within the head consumer, emits SOUR and its substructures from the header metadata
3. Replays other HEAD substructures (DEST, NOTE, SUBM pointer, LANG, SCHMA, etc.) that the parser fires as events after startDocument

However, the parser replays HEAD lines as normal events after `startDocument`. The converter must **skip** GEDC, GEDC.VERS, GEDC.FORM, and CHAR during HEAD replay (since the writer generates these), while passing through all other HEAD substructures.

**Alternative considered**: Fully reconstruct HEAD from metadata only. Rejected — would lose arbitrary HEAD substructures not captured in GedcomHeaderInfo (e.g., custom extension tags under HEAD).

## Decision 3: LineEmitter Access

**Decision**: Create `LineEmitter` directly in the converter. `LineEmitter` is already a public class in `org.gedcom7.writer.internal` with a public constructor that accepts `(OutputStream, GedcomWriterConfig)`. The converter creates a `LineEmitter` instance and calls `emitLine()` for each parser event. For HEAD, it uses special logic to adapt version-specific substructures.

**Rationale**: The converter needs to emit lines with the correct escaping and formatting for the target version. `LineEmitter` already handles @-escaping, CONC splitting, and line formatting. Since it's public, no visibility changes are needed.

**Alternatives considered**:
1. **Use GedcomWriter's public API** (record(), individual(), etc.): Rejected per Decision 1 — lambda-nesting model doesn't map to streaming events.
2. **Add imperative start/end methods to GedcomWriter**: Rejected — over-engineering for a single consumer.
3. **Duplicate escaping/CONC logic in converter**: Rejected — violates DRY and risks divergence from writer behavior.
4. **Use PipedOutputStream pattern**: Rejected — unnecessarily complex threading model.

## Decision 4: Skipping HEAD Substructures During Replay

**Decision**: The converter tracks a `boolean inHead` flag. During HEAD replay events, it skips GEDC (and children VERS, FORM) and CHAR, since the converter writes its own HEAD with the target version's correct values. All other HEAD substructures are passed through.

**Rationale**: The parser fires HEAD content as normal startStructure/endStructure events after startDocument. The converter's HEAD writing (in startDocument) generates the correct GEDC.VERS, GEDC.FORM, and CHAR for the target version. Replaying the source version's GEDC/CHAR would produce incorrect output.

## Decision 5: ConversionResult Design

**Decision**: ConversionResult is a mutable builder that accumulates counts and warnings during conversion, then produces an immutable snapshot.

**Rationale**: The converter needs to increment record counts and add warnings during streaming processing. Making ConversionResult itself mutable during conversion, then returning it as a read-only result, is the simplest approach.

**Fields**:
- `int recordCount` — total records converted
- `List<ConversionWarning> warnings` — conversion-specific warnings
- `List<GedcomParseError> parseErrors` — forwarded from parser
- `GedcomVersion sourceVersion` — detected source version
- `GedcomVersion targetVersion` — target version

## Decision 6: Error Mode Configuration

**Decision**: The converter's config accepts a `strict` boolean. In strict mode, the converter configures both the reader and writer in strict mode and throws on the first conversion warning. In lenient mode, warnings are collected in ConversionResult.

**Rationale**: Consistent with existing reader/writer strict mode patterns. The converter's strict mode is the conjunction of reader strict + writer strict + converter strict.
