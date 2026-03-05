package org.gedcom7.parser;

import org.gedcom7.parser.internal.AtEscapeStrategy;
import org.gedcom7.parser.internal.ContOnlyAssembler;
import org.gedcom7.parser.internal.GedcomInputDecoder;
import org.gedcom7.parser.internal.GedcomLine;
import org.gedcom7.parser.internal.GedcomLineTokenizer;
import org.gedcom7.parser.internal.LeadingAtEscapeStrategy;
import org.gedcom7.parser.internal.PayloadAssembler;
import org.gedcom7.parser.internal.Utf8InputDecoder;
import org.gedcom7.parser.validation.StructureDefinitions;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Streaming GEDCOM 7 parser. Reads an InputStream and fires
 * SAX-like events to a {@link GedcomHandler}.
 *
 * <p>Thread safety: each instance is confined to a single
 * thread. Create separate instances for concurrent parsing.
 *
 * <p>Usage:
 * <pre>
 * try (GedcomReader reader = new GedcomReader(
 *         inputStream, handler, GedcomReaderConfig.gedcom7())) {
 *     reader.parse();
 * }
 * </pre>
 */
public final class GedcomReader implements AutoCloseable {

    private final InputStream input;
    private final GedcomHandler handler;
    private final GedcomReaderConfig config;
    private final GedcomInputDecoder decoder;
    private final PayloadAssembler assembler;
    private final AtEscapeStrategy atEscape;

    // Cross-reference tracking (US4)
    private final Set<String> definedXrefs = new HashSet<>();
    private final Set<String> referencedXrefs = new HashSet<>();

    public GedcomReader(InputStream input, GedcomHandler handler,
                        GedcomReaderConfig config) {
        this.input = input;
        this.handler = handler;
        this.config = config;
        this.decoder = config.getInputDecoderOrNull() != null
                ? (GedcomInputDecoder) config.getInputDecoderOrNull()
                : new Utf8InputDecoder();
        this.assembler = config.getPayloadAssemblerOrNull() != null
                ? (PayloadAssembler) config.getPayloadAssemblerOrNull()
                : new ContOnlyAssembler();
        this.atEscape = config.getAtEscapeStrategyOrNull() != null
                ? (AtEscapeStrategy) config.getAtEscapeStrategyOrNull()
                : new LeadingAtEscapeStrategy();
    }

    /**
     * Parse the entire input stream, firing events to the handler.
     *
     * @throws GedcomFatalException if an unrecoverable error occurs
     */
    public void parse() throws GedcomFatalException {
        try (Reader reader = decoder.decode(input)) {
            doParse(reader);
        } catch (IOException e) {
            GedcomParseError err = new GedcomParseError(
                    GedcomParseError.Severity.FATAL, 0, 0, "I/O error: " + e.getMessage(), null);
            handler.fatalError(err);
            throw new GedcomFatalException(err);
        }
    }

    private void doParse(Reader reader) {
        GedcomLineTokenizer tokenizer = new GedcomLineTokenizer(reader, config.getMaxLineLength());
        GedcomLine line = new GedcomLine();

        // Phase 1: Pre-scan HEAD to build GedcomHeaderInfo
        List<GedcomLine> headLines = new ArrayList<>();
        boolean inHead = false;
        GedcomVersion version = new GedcomVersion(7, 0);
        String sourceSystem = null;
        String sourceVersion = null;
        String sourceName = null;
        String defaultLanguage = null;
        Map<String, String> schemaMap = new HashMap<>();

        try {
            // Read HEAD record lines
            while (tokenizer.nextLine(line)) {
                if (!inHead) {
                    if (line.getLevel() == 0 && "HEAD".equals(line.getTag())) {
                        inHead = true;
                        headLines.add(copyLine(line));
                    } else {
                        // First record is not HEAD — fire fatal
                        GedcomParseError err = new GedcomParseError(
                                GedcomParseError.Severity.FATAL, line.getLineNumber(), 0,
                                "First record must be HEAD, found: " + line.getTag(), line.getRawLine());
                        handler.fatalError(err);
                        throw new GedcomFatalException(err);
                    }
                } else {
                    if (line.getLevel() == 0) {
                        // HEAD is done, save this line for later processing
                        break;
                    }
                    headLines.add(copyLine(line));
                }
            }

            // Extract metadata from HEAD lines
            extractHeadMetadata(headLines, schemaMap);
            version = extractVersion(headLines, version);
            sourceSystem = extractValue(headLines, "SOUR", 1);
            sourceName = extractNestedValue(headLines, "SOUR", "NAME");
            sourceVersion = extractNestedValue(headLines, "SOUR", "VERS");
            defaultLanguage = extractValue(headLines, "LANG", 1);

            // Warn if not GEDCOM 7.x (US6)
            if (!version.isGedcom7()) {
                handler.warning(new GedcomParseError(
                        GedcomParseError.Severity.WARNING, 0, 0,
                        "Expected GEDCOM 7.x version, found: " + version, null));
            }

            GedcomHeaderInfo headerInfo = new GedcomHeaderInfo(
                    version, sourceSystem, sourceVersion, sourceName,
                    defaultLanguage, schemaMap);
            this.headerInfo = headerInfo;

            // Fire startDocument
            handler.startDocument(headerInfo);

            // Replay HEAD record as events
            replayHeadAsEvents(headLines);

            // Process remaining lines (starting from the line that ended HEAD)
            if (line.getLevel() == 0 && !"HEAD".equals(line.getTag())) {
                processLine(line);
            }

            // Continue parsing the rest
            while (tokenizer.nextLine(line)) {
                processLine(line);
            }

            // Close any open records/structures
            closeOpenElements();

            // Check TRLR is last record (US6)
            if (!"TRLR".equals(lastRecordTag)) {
                handler.warning(new GedcomParseError(
                        GedcomParseError.Severity.WARNING, 0, 0,
                        "Missing TRLR record at end of file", null));
            }

            // Report unresolved cross-references (US4)
            for (String ref : referencedXrefs) {
                if (!definedXrefs.contains(ref)) {
                    handler.warning(new GedcomParseError(
                            GedcomParseError.Severity.WARNING, 0, 0,
                            "Unresolved cross-reference: @" + ref + "@", null));
                }
            }

            handler.endDocument();

        } catch (IOException e) {
            GedcomParseError err = new GedcomParseError(
                    GedcomParseError.Severity.FATAL, 0, 0, "I/O error: " + e.getMessage(), null);
            handler.fatalError(err);
            throw new GedcomFatalException(err);
        }
    }

    // ─── Level tracking for nesting ────────────────────────

    private static final class StackEntry {
        final int level;
        final String tag;
        final boolean isRecord;
        /** Structure context ID for validation (e.g. "record-INDI", "BIRT"). Null if unknown. */
        final String contextId;
        /** Counts of child structure IDs seen under this entry, for cardinality checks. */
        final Map<String, Integer> childCounts;

        StackEntry(int level, String tag, boolean isRecord, String contextId) {
            this.level = level;
            this.tag = tag;
            this.isRecord = isRecord;
            this.contextId = contextId;
            this.childCounts = new HashMap<>();
        }

        int incrementChild(String childStructureId) {
            int count = childCounts.getOrDefault(childStructureId, 0) + 1;
            childCounts.put(childStructureId, count);
            return count;
        }
    }

    private final List<StackEntry> levelStack = new ArrayList<>();
    private GedcomLine pendingStructure = null;
    private String lastRecordTag = null;
    private boolean seenTrlr = false;
    private GedcomHeaderInfo headerInfo;

    private void processLine(GedcomLine line) {
        // Validate characters (US3)
        validateCharacters(line);

        // Warn on leading whitespace (FR-009)
        if (line.hasLeadingWhitespace()) {
            reportWarning(line, "Line has leading whitespace before level number");
        }

        int level = line.getLevel();
        String tag = line.getTag();

        // Handle CONT pseudo-structures
        if (assembler.isPseudoStructure(tag) && pendingStructure != null) {
            String assembled = assembler.assemblePayload(
                    pendingStructure.getValue(), line.getValue());
            pendingStructure.setValue(assembled);
            return;
        }

        // Validate level jump (FR-033)
        int expectedMaxLevel = levelStack.isEmpty() ? 0 : levelStack.get(levelStack.size() - 1).level + 1;
        if (pendingStructure != null) {
            expectedMaxLevel = pendingStructure.getLevel() + 1;
        }
        if (level > expectedMaxLevel) {
            reportError(line, "Level jumps from " + (expectedMaxLevel - 1) + " to " + level
                    + " (increase by more than 1)");
        }

        // Flush any pending structure
        flushPending();

        // Pop closed elements
        while (!levelStack.isEmpty()) {
            StackEntry top = levelStack.get(levelStack.size() - 1);
            if (top.level >= level) {
                levelStack.remove(levelStack.size() - 1);
                if (top.isRecord) {
                    handler.endRecord(top.tag);
                } else {
                    handler.endStructure(top.tag);
                }
            } else {
                break;
            }
        }

        // Check max nesting depth (US7)
        if (level > config.getMaxNestingDepth()) {
            GedcomParseError err = new GedcomParseError(
                    GedcomParseError.Severity.FATAL, line.getLineNumber(),
                    line.getByteOffset(),
                    "Nesting depth " + level + " exceeds maximum " + config.getMaxNestingDepth(),
                    line.getRawLine());
            handler.fatalError(err);
            throw new GedcomFatalException(err);
        }

        // Track last record tag for TRLR validation
        if (level == 0) {
            lastRecordTag = line.getTag();
        }

        // TRLR content validation (FR-054)
        if (seenTrlr) {
            reportError(line, "Content after TRLR record");
        }
        if (level == 0 && "TRLR".equals(tag)) {
            seenTrlr = true;
        }

        // Store as pending (to collect CONT lines)
        pendingStructure = copyLine(line);
    }

    private void flushPending() {
        if (pendingStructure == null) return;

        GedcomLine p = pendingStructure;
        pendingStructure = null;

        // Unescape @@ in values
        String value = p.isPointer() ? p.getValue() : atEscape.unescape(p.getValue());

        if (p.getLevel() == 0) {
            // Track xref definition (US4)
            if (p.getXref() != null) {
                if (!definedXrefs.add(p.getXref())) {
                    reportError(p, "Duplicate cross-reference identifier: @" + p.getXref() + "@");
                }
            }
            // TRLR must not have value or xref (FR-054)
            if ("TRLR".equals(p.getTag())) {
                if (value != null && !value.isEmpty()) {
                    reportError(p, "TRLR must not have a line value");
                }
                if (p.getXref() != null) {
                    reportError(p, "TRLR must not have a cross-reference identifier");
                }
            }
            String contextId = StructureDefinitions.recordContext(p.getTag());
            handler.startRecord(p.getLevel(), p.getXref(), p.getTag());
            levelStack.add(new StackEntry(p.getLevel(), p.getTag(), true, contextId));
        } else {
            // Track xref reference (US4)
            if (p.isPointer() && value != null) {
                // Extract xref from @...@
                String ref = value;
                if (ref.startsWith("@") && ref.endsWith("@")) {
                    ref = ref.substring(1, ref.length() - 1);
                }
                if (!"VOID".equals(ref)) {
                    referencedXrefs.add(ref);
                }
            }

            // Structure validation (Phase 11)
            String resolvedContextId = validateStructure(p);

            // Resolve extension tag URI (FR-072)
            String uri = null;
            if (p.getTag() != null && p.getTag().startsWith("_") && headerInfo != null) {
                uri = headerInfo.getSchemaMap().get(p.getTag());
            }
            handler.startStructure(p.getLevel(), p.getXref(), p.getTag(),
                    value, p.isPointer(), uri);
            levelStack.add(new StackEntry(p.getLevel(), p.getTag(), false, resolvedContextId));
        }
    }

    private void closeOpenElements() {
        flushPending();
        while (!levelStack.isEmpty()) {
            StackEntry top = levelStack.remove(levelStack.size() - 1);
            if (top.isRecord) {
                handler.endRecord(top.tag);
            } else {
                handler.endStructure(top.tag);
            }
        }
    }

    // ─── HEAD replay ───────────────────────────────────────

    private void replayHeadAsEvents(List<GedcomLine> headLines) {
        for (GedcomLine hl : headLines) {
            processLine(hl);
        }
    }

    // ─── HEAD metadata extraction ──────────────────────────

    private void extractHeadMetadata(List<GedcomLine> lines,
                                     Map<String, String> schemaMap) {
        // Extract SCHMA.TAG entries
        boolean inSchma = false;
        for (GedcomLine hl : lines) {
            if (hl.getLevel() == 1 && "SCHMA".equals(hl.getTag())) {
                inSchma = true;
            } else if (hl.getLevel() <= 1 && inSchma) {
                inSchma = false;
            }
            if (inSchma && hl.getLevel() == 2 && "TAG".equals(hl.getTag())) {
                String val = hl.getValue();
                if (val != null) {
                    int spaceIdx = val.indexOf(' ');
                    if (spaceIdx > 0) {
                        String extTag = val.substring(0, spaceIdx);
                        String uri = val.substring(spaceIdx + 1);
                        schemaMap.put(extTag, uri);
                    }
                }
            }
        }
    }

    private GedcomVersion extractVersion(List<GedcomLine> lines, GedcomVersion fallback) {
        boolean inGedc = false;
        for (GedcomLine hl : lines) {
            if (hl.getLevel() == 1 && "GEDC".equals(hl.getTag())) {
                inGedc = true;
            } else if (hl.getLevel() <= 1 && inGedc && !"GEDC".equals(hl.getTag())) {
                inGedc = false;
            }
            if (inGedc && hl.getLevel() == 2 && "VERS".equals(hl.getTag())) {
                try {
                    return GedcomVersion.parse(hl.getValue());
                } catch (IllegalArgumentException e) {
                    return fallback;
                }
            }
        }
        return fallback;
    }

    private String extractValue(List<GedcomLine> lines, String tag, int level) {
        for (GedcomLine hl : lines) {
            if (hl.getLevel() == level && tag.equals(hl.getTag())) {
                return hl.getValue();
            }
        }
        return null;
    }

    private String extractNestedValue(List<GedcomLine> lines,
                                      String parentTag, String childTag) {
        boolean inParent = false;
        int parentLevel = -1;
        for (GedcomLine hl : lines) {
            if (hl.getLevel() == 1 && parentTag.equals(hl.getTag())) {
                inParent = true;
                parentLevel = hl.getLevel();
            } else if (hl.getLevel() <= 1 && inParent && !parentTag.equals(hl.getTag())) {
                inParent = false;
            }
            if (inParent && hl.getLevel() == parentLevel + 1
                    && childTag.equals(hl.getTag())) {
                return hl.getValue();
            }
        }
        return null;
    }

    // ─── Character validation (US3) ─────────────────────────

    /**
     * Checks for banned characters in a GEDCOM line value.
     * Banned: C0 controls (except TAB, CR, LF), DEL (0x7F),
     * C1 controls (0x80-0x9F), surrogates, U+FFFE, U+FFFF.
     */
    private void validateCharacters(GedcomLine line) {
        String value = line.getValue();
        if (value == null) return;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (isBannedChar(ch)) {
                reportError(line, "Banned character U+" + String.format("%04X", (int) ch)
                        + " at position " + i);
            }
        }
        // Also check tag and xref for completeness
        String tag = line.getTag();
        if (tag != null) {
            for (int i = 0; i < tag.length(); i++) {
                if (isBannedChar(tag.charAt(i))) {
                    reportError(line, "Banned character in tag");
                    break;
                }
            }
        }
    }

    private static boolean isBannedChar(char ch) {
        // C0 controls except TAB (0x09), LF (0x0A), CR (0x0D)
        if (ch < 0x20 && ch != 0x09 && ch != 0x0A && ch != 0x0D) return true;
        // DEL
        if (ch == 0x7F) return true;
        // C1 controls
        if (ch >= 0x80 && ch <= 0x9F) return true;
        // Surrogates (0xD800-0xDFFF) — shouldn't appear in Java strings
        // U+FFFE, U+FFFF
        if (ch == 0xFFFE || ch == 0xFFFF) return true;
        return false;
    }

    private void reportError(GedcomLine line, String message) {
        GedcomParseError err = new GedcomParseError(
                GedcomParseError.Severity.ERROR, line.getLineNumber(),
                line.getByteOffset(), message, line.getRawLine());
        handler.error(err);
        if (config.isStrict()) {
            throw new GedcomFatalException(err);
        }
    }

    private void reportWarning(GedcomLine line, String message) {
        handler.warning(new GedcomParseError(
                GedcomParseError.Severity.WARNING, line.getLineNumber(),
                line.getByteOffset(), message, line.getRawLine()));
    }

    // ─── Structure validation (Phase 11) ──────────────────

    /**
     * When structure validation is enabled, resolves the child tag against
     * the parent context and checks cardinality. Extension tags (starting
     * with underscore) are exempt. Returns the resolved child structure ID,
     * or null if unknown.
     */
    private String validateStructure(GedcomLine line) {
        if (!config.isStructureValidationEnabled()) {
            return null;
        }

        String tag = line.getTag();

        // Extension tags (_prefixed) are exempt from validation
        if (tag != null && tag.startsWith("_")) {
            return null;
        }

        // Find the parent context
        if (levelStack.isEmpty()) {
            return null;
        }
        StackEntry parent = levelStack.get(levelStack.size() - 1);
        String parentContextId = parent.contextId;
        if (parentContextId == null) {
            // Parent context is unknown, skip validation for this subtree
            return null;
        }

        // Resolve the structure
        String childStructureId = StructureDefinitions.resolveStructure(parentContextId, tag);
        if (childStructureId == null) {
            reportWarning(line, "Unknown structure " + tag + " in context " + parentContextId);
            return null;
        }

        // Check cardinality
        String cardinality = StructureDefinitions.getCardinality(parentContextId, childStructureId);
        if (cardinality != null && StructureDefinitions.isSingleton(cardinality)) {
            int count = parent.incrementChild(childStructureId);
            if (count > 1) {
                reportWarning(line, "Cardinality violation: " + tag + " appears "
                        + count + " times under " + parent.tag
                        + " (max " + cardinality + ")");
            }
        } else {
            // Even for {0:M}, track count (no warning needed)
            parent.incrementChild(childStructureId);
        }

        return childStructureId;
    }

    // ─── Utility ───────────────────────────────────────────

    private GedcomLine copyLine(GedcomLine src) {
        GedcomLine copy = new GedcomLine();
        copy.setLevel(src.getLevel());
        copy.setXref(src.getXref());
        copy.setTag(src.getTag());
        copy.setValue(src.getValue());
        copy.setPointer(src.isPointer());
        copy.setLineNumber(src.getLineNumber());
        copy.setByteOffset(src.getByteOffset());
        copy.setRawLine(src.getRawLine());
        copy.setLeadingWhitespace(src.hasLeadingWhitespace());
        return copy;
    }

    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException ignored) {
            // Best effort
        }
    }
}
