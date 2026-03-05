# Research: GEDZip Support and GEDCOM 5.5.5 Compatibility

**Date**: 2026-03-05 | **Branch**: `002-gedzip-gedcom555`

## Decision 1: PayloadAssembler Interface Change for CONC Support

**Decision**: Add the tag name as a parameter to `assemblePayload()` so the assembler can distinguish CONT (newline join) from CONC (direct concat).

**Rationale**: The current `assemblePayload(String existing, String continuationValue)` signature doesn't tell the assembler which pseudo-structure triggered it. GEDCOM 5.5.5 needs different behavior for CONT vs CONC. Passing the tag is the minimal change that enables both.

**Alternatives considered**:
- Separate methods (`assembleCont`/`assembleConc`): Breaks the strategy interface pattern and requires the caller to know about CONC.
- Two separate strategies (one for CONT, one for CONC): Over-engineered; they always work together.
- Encode join mode in a field: Requires mutable state, complicates thread-safety reasoning.

**Interface change**:
```java
// Before:
String assemblePayload(String existing, String continuationValue);

// After:
String assemblePayload(String existing, String continuationValue, String tag);
```

The `ContOnlyAssembler` ignores the tag parameter (always newline join). The new `ContConcAssembler` uses it to select the join mode.

---

## Decision 2: UTF-16 Encoding Detection via BOM

**Decision**: Create a new `BomDetectingDecoder` that detects UTF-8 BOM, UTF-16 BE BOM, and UTF-16 LE BOM, and selects the appropriate charset. Default to UTF-8 if no BOM is found.

**Rationale**: GEDCOM 5.5.5 mandates BOM for both UTF-8 and UTF-16 files. BOM detection is the most reliable encoding detection method and works before any text parsing. The existing `Utf8InputDecoder` only handles UTF-8 BOM. A new decoder handles all three BOM types.

**Alternatives considered**:
- Extend `Utf8InputDecoder`: Would violate single responsibility and muddy its name/purpose.
- Detect encoding from HEAD.CHAR only: Chicken-and-egg problem — need to decode bytes before reading HEAD.CHAR. BOM detection must come first.
- Use `java.nio.charset.CharsetDecoder` with auto-detection: JDK doesn't auto-detect encoding from BOM.

**BOM sequences**:
| BOM | Bytes | Charset |
|-----|-------|---------|
| UTF-8 | EF BB BF | UTF-8 |
| UTF-16 BE | FE FF | UTF-16BE |
| UTF-16 LE | FF FE | UTF-16LE |
| None | — | UTF-8 (default) |

---

## Decision 3: Auto-Detection Architecture

**Decision**: Use a deferred strategy swap during the existing HEAD pre-scan phase. Add an `autoDetect` boolean flag to `GedcomReaderConfig`. When true, `GedcomReader.doParse()` swaps `assembler` and `atEscape` after extracting version from HEAD.

**Rationale**: The existing parse flow already collects all HEAD lines before processing them. Inserting strategy selection between HEAD scanning and HEAD replay is minimally invasive. No two-pass parsing needed. No re-reading of the input stream.

**Alternatives considered**:
- Two-pass parsing (scan then re-read): Requires seekable input or buffering entire file. Violates streaming principle.
- Separate pre-scanner class: Over-engineered for what is a 5-line addition to `doParse()`.
- Always auto-detect: Would prevent users from forcing a specific version. Explicit factory methods are better.

**Implementation sketch**:
```java
// In doParse(), after extracting version from HEAD:
if (config.isAutoDetect()) {
    if (version.isGedcom5()) {
        this.assembler = new ContConcAssembler();
        this.atEscape = new AllAtEscapeStrategy();
    }
    // GEDCOM 7 strategies remain as defaults
}
```

---

## Decision 4: GedzipReader API Design

**Decision**: `GedzipReader` is a standalone public class that wraps a `ZipFile`, provides the GEDCOM `InputStream`, and offers entry access. It does NOT extend or replace `GedcomReader`. The caller creates a `GedzipReader`, gets the GEDCOM stream, and passes it to a standard `GedcomReader`.

**Rationale**: Keeping GedzipReader separate from GedcomReader follows single-responsibility and keeps the core parser unaware of ZIP archives. The caller controls configuration (version, strictness) independently.

**Alternatives considered**:
- Integrate ZIP reading into `GedcomReader`: Violates SRP and forces GedcomReader to know about file formats.
- Static utility methods: Doesn't provide lifecycle management (ZipFile must be closed).
- Subclass GedcomReader: Inappropriate inheritance; GEDZip is a packaging concern, not a parsing concern.

**API shape**:
```java
public final class GedzipReader implements AutoCloseable {
    public GedzipReader(Path path) throws IOException;
    public GedzipReader(File file) throws IOException;
    public InputStream getGedcomStream() throws IOException;
    public InputStream getEntry(String path);  // null if not found
    public boolean hasEntry(String path);
    public Set<String> getEntryNames();
    public void close() throws IOException;
}
```

---

## Decision 5: GEDCOM 5.5.5 Validation Mode

**Decision**: GEDCOM 5.5.5-specific validation (max line length 255, xref length 22, BOM requirement, bare @ detection) is controlled by the same `strict` flag in `GedcomReaderConfig`. The 5.5.5 factory methods set version-appropriate defaults (e.g., `maxLineLength=255` for `gedcom555Strict()`).

**Rationale**: Users already understand the strict/lenient model. Adding a separate validation flag would complicate the API for minimal benefit. The version-specific factory methods encode the right defaults.

**Alternatives considered**:
- Separate `gedcom555Validation` flag: Unnecessary complexity; strict mode already controls error severity.
- Always validate 5.5.5 constraints: Would break lenient parsing of non-conforming but readable files.

**Validation mapping**:
| Check | Lenient 5.5.5 | Strict 5.5.5 |
|-------|--------------|--------------|
| Line > 255 chars | Warning | Fatal |
| Xref > 22 chars | Warning | Warning |
| Missing BOM | Warning | Fatal |
| Bare @ in value | Error | Fatal |

---

## Decision 6: HEAD.CHAR Encoding Verification

**Decision**: The `BomDetectingDecoder` determines the actual encoding from BOM. After HEAD is parsed, the parser cross-checks `HEAD.CHAR` against the BOM-detected encoding. Mismatches produce a warning but the BOM-detected encoding takes precedence.

**Rationale**: BOM is the ground truth for encoding — it's physically present in the byte stream. HEAD.CHAR is metadata that could be wrong. Trusting BOM prevents garbled text. A warning alerts users to the inconsistency.

**Alternatives considered**:
- Trust HEAD.CHAR over BOM: Risky if the file was re-saved with different encoding but HEAD.CHAR wasn't updated.
- Error on mismatch: Too strict for real-world files which frequently have metadata inconsistencies.

---

## Decision 7: Percent-Encoded Filename Matching in GEDZip

**Decision**: When looking up a ZIP entry by a GEDCOM `FILE` path value, percent-decode the path first (e.g., `photo%20album/img.jpg` → `photo album/img.jpg`), then match against ZIP entry names verbatim. Use `java.net.URLDecoder.decode(path, StandardCharsets.UTF_8)`.

**Rationale**: The GEDCOM 7 spec states that FILE paths use percent-encoding for special characters, while ZIP entry names store the literal filename. The decoder must bridge this gap.

**Alternatives considered**:
- Match without decoding: Would fail for any filename with spaces or special characters.
- Encode ZIP entry names for comparison: More complex and error-prone (multiple valid encodings for the same character).
