# Implementation Plan: GEDZip Support and GEDCOM 5.5.5 Compatibility

**Branch**: `002-gedzip-gedcom555` | **Date**: 2026-03-05 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-gedzip-gedcom555/spec.md`

## Summary

Add GEDCOM 5.5.5 parsing support alongside existing GEDCOM 7, with version auto-detection and GEDZip archive reading. The existing pluggable strategy architecture (GedcomInputDecoder, PayloadAssembler, AtEscapeStrategy) enables this by adding new strategy implementations for 5.5.5 (CONC+CONT assembly, all-@@ decoding, UTF-16 encoding) without modifying core parser logic. GEDZip support uses `java.util.zip` to read `.gdz` archives, maintaining the zero-dependency constraint.

## Technical Context

**Language/Version**: Java 11+
**Primary Dependencies**: None at runtime (zero-dependency library); JUnit 5 for testing
**Storage**: N/A (streaming parser, no persistence)
**Testing**: JUnit 5 via Gradle (`./gradlew test`)
**Target Platform**: Any JVM (Java 11+)
**Project Type**: Library (Java library with JPMS module `org.gedcom7.parser`)
**Performance Goals**: Streaming with minimal allocation; GEDZip overhead < 100ms for archives under 10 MB
**Constraints**: Zero runtime dependencies; Java 11 source/target compatibility; existing 301 tests must continue passing
**Scale/Scope**: Single-module library; ~15 source files currently; adding ~6-8 new classes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. GEDCOM 7 Compliance | JUSTIFIED EXTENSION | Adding 5.5.5 support extends scope beyond "UTF-8 only" and "CONT only". GEDCOM 7 behavior is fully preserved (FR-026, FR-027). UTF-16 and CONC are isolated in new strategy implementations. |
| II. SAX-like Event-Driven API | PASS | Unified handler API (FR-009): same GedcomHandler works for both versions. GedzipReader delegates to GedcomReader for parsing. |
| III. Mechanical Sympathy | PASS | New strategies follow same patterns as existing ones. BOM detection uses PushbackInputStream (already proven). No new allocations in hot path. |
| IV. Java Best Practices | PASS | New classes follow existing conventions: final classes, immutable configs, try-with-resources for GedzipReader. Java 11 target maintained. |
| V. Test-Driven Development | PASS | Each new strategy and integration point will have dedicated tests. Test resources for 5.5.5 sample files. |
| VI. Simplicity and YAGNI | JUSTIFIED EXTENSION | GedzipReader is a new public class but is narrowly scoped (open archive, parse GEDCOM, access media). GEDCOM 5.5.5 support leverages existing strategy architecture. |
| VII. Zero External Dependencies | PASS | GEDZip uses `java.util.zip.ZipFile` from the standard library. No new dependencies. |

## Project Structure

### Documentation (this feature)

```text
specs/002-gedzip-gedcom555/
├── spec.md
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── public-api.md    # New and modified public API surface
└── checklists/
    └── requirements.md  # Spec quality checklist
```

### Source Code (repository root)

```text
src/main/java/org/gedcom7/parser/
├── GedcomReader.java              # Modified: auto-detect strategy swap after HEAD scan
├── GedcomReaderConfig.java        # Modified: new factory methods (gedcom555, autoDetect)
├── GedcomHeaderInfo.java          # Modified: add characterEncoding field
├── GedcomHandler.java             # Unchanged
├── GedcomVersion.java             # Unchanged
├── GedcomParseError.java          # Unchanged
├── GedcomFatalException.java      # Unchanged
├── GedzipReader.java              # NEW: GEDZip archive reader
├── internal/
│   ├── GedcomInputDecoder.java    # Unchanged (interface)
│   ├── PayloadAssembler.java      # Modified: add tag parameter to assemblePayload
│   ├── AtEscapeStrategy.java      # Unchanged (interface)
│   ├── Utf8InputDecoder.java      # Unchanged
│   ├── BomDetectingDecoder.java   # NEW: UTF-8/UTF-16 BOM detection
│   ├── ContOnlyAssembler.java     # Unchanged
│   ├── ContConcAssembler.java     # NEW: CONT+CONC for 5.5.5
│   ├── LeadingAtEscapeStrategy.java # Unchanged
│   ├── AllAtEscapeStrategy.java   # NEW: all @@ decoded for 5.5.5
│   ├── GedcomLine.java            # Unchanged
│   └── GedcomLineTokenizer.java   # Unchanged
└── validation/
    └── StructureDefinitions.java  # Unchanged

src/test/java/org/gedcom7/parser/
├── Gedcom555ParsingTest.java      # NEW: 5.5.5 parsing integration tests
├── Gedcom555ValidationTest.java   # NEW: 5.5.5 validation tests
├── VersionAutoDetectTest.java     # NEW: auto-detect tests
├── GedzipReaderTest.java          # NEW: GEDZip integration tests
├── internal/
│   ├── BomDetectingDecoderTest.java  # NEW
│   ├── ContConcAssemblerTest.java    # NEW
│   └── AllAtEscapeStrategyTest.java  # NEW
└── ...existing tests unchanged...

src/test/resources/
├── gedcom555/                     # NEW: 5.5.5 test files
│   ├── basic-555.ged              # Basic 5.5.5 file (UTF-8)
│   ├── utf16-le.ged               # UTF-16 LE with BOM
│   ├── utf16-be.ged               # UTF-16 BE with BOM
│   ├── conc-values.ged            # CONC continuation lines
│   ├── at-escape-all.ged          # @@ throughout values
│   ├── mixed-cont-conc.ged        # Both CONT and CONC
│   ├── long-lines.ged             # Lines exceeding 255 chars
│   └── no-char-tag.ged            # Missing HEAD.CHAR
├── gedzip/                        # NEW: GEDZip test archives
│   ├── basic.gdz                  # Simple archive with gedcom.ged
│   ├── with-media.gdz             # Archive with media files
│   ├── no-gedcom.zip              # Invalid: missing gedcom.ged
│   └── percent-encoded.gdz        # Percent-encoded filenames
└── ...existing test resources...
```

**Structure Decision**: Single-module Java library. New classes are added to existing packages following established conventions. New strategy implementations go in `internal/` package. `GedzipReader` is public API in the main package.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Principle I: UTF-16 encoding support | GEDCOM 5.5.5 spec mandates UTF-16 (UNICODE) as a valid encoding. Real-world 5.5.5 files use it. | UTF-8-only would make 5.5.5 support incomplete and unusable for a significant portion of files. Isolated in BomDetectingDecoder — does not affect GEDCOM 7 path. |
| Principle I: CONC pseudo-structure | GEDCOM 5.5.5 spec uses CONC for line continuation without newline. | Cannot parse 5.5.5 files correctly without it. Isolated in ContConcAssembler — ContOnlyAssembler remains default for GEDCOM 7. |
| Principle VI: GedzipReader new public class | GEDZip is the official GEDCOM 7 packaging format. Users need to open archives. | Could have put GEDZip logic inside GedcomReader, but that violates single responsibility. GedzipReader is narrowly scoped: open, parse, access media. |

## Architecture: Auto-Detection Strategy

The key architectural challenge is auto-detection: strategies are configured at construction time but the version is only known after scanning HEAD.

**Solution: Deferred strategy swap during HEAD pre-scan.**

The existing `doParse()` method already pre-scans HEAD lines before processing them. In auto-detect mode:

1. `GedcomInputDecoder` runs first (bytes→chars). The `BomDetectingDecoder` handles both UTF-8 and UTF-16 BOMs, so encoding detection works regardless of version.
2. HEAD lines are collected and scanned. `HEAD.GEDC.VERS` reveals the version.
3. Based on the detected version, `assembler` and `atEscape` fields are swapped to the appropriate implementations.
4. HEAD lines are then replayed through `processLine()` with the correct strategies.
5. Remaining lines are processed normally.

This requires making `assembler` and `atEscape` non-final in `GedcomReader`, but only for auto-detect mode. The fields are still set once during HEAD scanning and never change afterward.

**Factory method design:**

```
GedcomReaderConfig.gedcom7()       → existing (unchanged)
GedcomReaderConfig.gedcom7Strict() → existing (unchanged)
GedcomReaderConfig.gedcom555()     → new: 5.5.5 strategies, maxLineLength=255
GedcomReaderConfig.gedcom555Strict() → new: strict + 5.5.5 strategies
GedcomReaderConfig.autoDetect()    → new: BomDetectingDecoder + auto-detect flag
```
