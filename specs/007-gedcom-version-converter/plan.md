# Implementation Plan: GEDCOM Version Converter

**Branch**: `007-gedcom-version-converter` | **Date**: 2026-03-06 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/007-gedcom-version-converter/spec.md`

## Summary

Implement a bidirectional GEDCOM version converter that reads a GEDCOM file in one version (5.5.5 or 7) and writes it in the other version, using the existing SAX-like parser and fluent writer. The converter implements a GedcomHandler that bridges parser events to writer calls, letting each side handle its own version-specific formatting (CONT/CONC, @-escaping, HEAD.CHAR, line length limits). A ConversionResult captures record counts and warnings.

**Multi-agent implementation strategy**: The converter is a single cohesive class plus configuration and result. Implementation will be distributed among specialized agents for parallel development of independent concerns (core converter, error handling, edge cases). Per Constitution Principle VIII, each task receives independent verification from a separate agent, and a final comprehensive evaluation is performed after all tasks complete.

## Technical Context

**Language/Version**: Java 11+ (matching existing parser/writer)
**Primary Dependencies**: None at runtime (zero-dependency library — Constitution Principle VII)
**Storage**: N/A (streaming converter — InputStream to OutputStream)
**Testing**: JUnit 5
**Target Platform**: JVM (any platform)
**Project Type**: Library
**Performance Goals**: Streaming conversion — bounded memory regardless of file size
**Constraints**: Zero external runtime dependencies; must use existing parser/writer APIs
**Scale/Scope**: Files from a few records to millions of records

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. GEDCOM 7 Specification Compliance | PASS | Converter uses existing parser/writer which already implement spec compliance |
| II. SAX-like Event-Driven API | PASS | Converter implements GedcomHandler to receive streaming events from parser |
| III. Mechanical Sympathy | PASS | Streaming design — no DOM, no full-file buffering, single pass |
| IV. Java Best Practices | PASS | Immutable config via builder, try-with-resources, clear naming |
| V. Test-Driven Development | PASS | Tests for each user story, edge cases, round-trip fidelity |
| VI. Simplicity and YAGNI | PASS | Single converter class + config + result; delegates formatting to parser/writer |
| VII. Zero External Runtime Dependencies | PASS | Uses only JDK + existing library classes |
| VIII. Independent Verification | PASS | Per-task agent reviews + final evaluation agent built into task plan |

No violations. All gates pass.

## Project Structure

### Documentation (this feature)

```text
specs/007-gedcom-version-converter/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── converter-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/org/gedcom7/converter/
├── GedcomConverter.java        # Main converter — implements GedcomHandler
├── GedcomConverterConfig.java  # Converter configuration (builder pattern)
├── ConversionResult.java       # Conversion summary (counts, warnings, errors)
└── ConversionWarning.java      # Warning details (tag, message, line number)

src/test/java/org/gedcom7/converter/
├── Convert555To7Test.java      # US1: 5.5.5 → 7 conversion tests
├── Convert7To555Test.java      # US2: 7 → 5.5.5 conversion tests
├── StreamingConversionTest.java # US3: Streaming/memory tests
├── ConversionErrorTest.java    # US4: Error reporting tests
├── XrefPreservationTest.java   # US5: Xref preservation tests
├── EdgeCaseTest.java           # Edge cases (unrecognized version, same-version, etc.)
└── RoundTripTest.java          # SC-003: Round-trip fidelity tests
```

**Structure Decision**: New `org.gedcom7.converter` package keeps converter separate from parser and writer while co-located in the same library. This follows the existing pattern where `org.gedcom7.parser` and `org.gedcom7.writer` are peer packages.

## Architecture

### Core Design: GedcomHandler Bridge

The converter implements `GedcomHandler` and forwards parser events to writer calls:

```
InputStream → GedcomReader(autoDetect) → [GedcomConverter implements GedcomHandler] → GedcomWriter(targetConfig) → OutputStream
```

**Event mapping:**

| Parser Event | Writer Call |
|-------------|-------------|
| `startDocument(header)` | `writer.head(...)` — adapted for target version |
| `startRecord(level, xref, tag)` | `writer.record(id, tag, ...)` — with developer-provided xref |
| `startRecord(level, xref, tag, value)` | `writer.record(id, tag, ...)` — with value |
| `startStructure(level, xref, tag, value, isPointer)` | `ctx.structure(tag, value)` or `ctx.pointer(tag, id)` |
| `startStructure(level, xref, tag, value, isPointer, uri)` | Same, URI passed through via SCHMA |
| `endRecord(tag)` | Record lambda completes |
| `endStructure(tag)` | Structure lambda completes |
| `endDocument()` | `writer.trailer()` |
| `warning/error/fatalError` | Forwarded to ConversionResult + optional handler |

### HEAD Record Conversion

The `startDocument(GedcomHeaderInfo)` callback receives pre-parsed HEAD metadata. The converter writes a new HEAD using the writer's `head()` method with the target version config. Key adaptations:

- **GEDC.VERS**: Set by target GedcomWriterConfig (7.0 or 5.5.5)
- **HEAD.CHAR**: Auto-added by writer for 5.5.5, omitted for GEDCOM 7
- **GEDC.FORM**: Added as "LINEAGE-LINKED" for 5.5.5 if absent
- **HEAD.SCHMA**: Preserved as-is (per clarification — treated as unknown structure in 5.5.5)
- **Other HEAD substructures** (SOUR, DEST, NOTE, etc.): Replayed from parser HEAD events

### Level/Nesting Management

The converter needs to bridge the SAX event model (start/end callbacks) with the writer's lambda-nesting model. Two approaches considered:

1. **Deferred writing with stack** (chosen): Buffer each structure level as pending output, emit when the next event at the same or lower level arrives. This avoids requiring the writer to support imperative start/end calls.

2. **Writer imperative API**: Add startRecord()/endRecord() to writer. Rejected — would require significant writer changes and breaks the existing lambda-based API design.

The converter maintains an internal stack of `GeneralContext` objects. When `startStructure` fires, it records the pending structure. When `endStructure` fires or a sibling/parent starts, the pending items are flushed. This is a lightweight approach that uses the writer's escape-hatch `structure()` and `pointer()` methods on `GeneralContext`.

**Actually, a simpler approach**: Since the writer has `record(String id, String tag, Consumer<GeneralContext> body)` and `GeneralContext.structure(String tag, String value, Consumer<GeneralContext> body)`, we need to reconstruct the nesting. The cleanest way is to use the writer's **`LineEmitter` directly** — the converter can emit lines through the writer's internal emitter, which already handles @-escaping, CONC splitting, and line formatting.

**Revised approach — direct LineEmitter usage**: The converter creates a `GedcomWriter` for the target version. For each parser event, it calls the writer's line emitter directly. The writer's `head()` is used for HEAD, then `record()` with a `Consumer<GeneralContext>` that uses `structure()`/`pointer()` to emit substructures. Since we receive events depth-first, we can use a stack-based approach where each record's body consumer collects substructures and emits them.

**Final approach — imperative line emission**: The simplest and most mechanical-sympathy-friendly approach is to directly use the writer's `LineEmitter` to emit individual lines as the parser fires events. The `LineEmitter` already handles @-escaping, CONC splitting, and line length limits. The converter:

1. Creates a `LineEmitter` configured for the target version
2. On `startRecord(level, xref, tag)` → emits a level-0 line with xref and tag
3. On `startStructure(level, xref, tag, value, isPointer)` → emits the line at the given level
4. On `startDocument` → writes HEAD (special handling for version adaptation)
5. On `endDocument` → writes TRLR
6. No buffering needed — each event maps to exactly one emitted line

This is the most streaming-friendly approach — zero buffering beyond individual line emission.

### Error Handling

- Parse errors from reader → forwarded to ConversionResult warnings/errors
- Conversion-specific warnings (e.g., version-specific structures) → ConversionWarning
- Strict mode → throws on first error (delegates to reader's strict mode + converter strict check)
- Lenient mode → collects all warnings, continues converting

### Agent Distribution Strategy

The implementation tasks will be distributed among parallel agents:

1. **Core Agent**: GedcomConverterConfig, ConversionWarning, ConversionResult — foundational types
2. **Converter Agent**: GedcomConverter — the main handler bridge
3. **Test Agents** (parallel): Each user story's tests can run in parallel since they test independent scenarios
4. **Review Agents** (per-task): Independent verification after each implementation task
5. **Final Evaluation Agent**: Comprehensive review against all FRs and constitution

## Complexity Tracking

No constitution violations to justify. All gates pass.
