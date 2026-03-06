package org.gedcom7.writer.internal;

import org.gedcom7.writer.GedcomWriterConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Low-level GEDCOM line formatter and writer.
 *
 * <p>Handles line formatting (level + optional xref + tag + optional value),
 * CONT splitting for multi-line values, and UTF-8 encoding.
 */
public final class LineEmitter {

    private final OutputStream out;
    private final GedcomWriterConfig config;
    private final StringBuilder lineBuffer = new StringBuilder(256);

    public LineEmitter(OutputStream out, GedcomWriterConfig config) {
        this.out = out;
        this.config = config;
    }

    /**
     * Emits a single GEDCOM line.
     *
     * @param level the structure level
     * @param xref  the cross-reference identifier (without @ delimiters), or null
     * @param tag   the GEDCOM tag
     * @param value the line value, or null
     */
    public void emitLine(int level, String xref, String tag, String value) throws IOException {
        lineBuffer.setLength(0);
        lineBuffer.append(level);
        if (xref != null) {
            lineBuffer.append(' ').append('@').append(xref).append('@');
        }
        lineBuffer.append(' ').append(tag);
        if (value != null && !value.isEmpty()) {
            String escaped = escapeAt(value);
            if (config.isConcEnabled() && config.getMaxLineLength() > 0) {
                int prefixLen = lineBuffer.length() + 1; // +1 for the space before value
                int available = config.getMaxLineLength() - prefixLen;
                if (available > 0 && escaped.length() > available) {
                    // Emit first chunk with original tag
                    lineBuffer.append(' ').append(escaped, 0, available);
                    lineBuffer.append(config.getLineEnding());
                    out.write(lineBuffer.toString().getBytes(StandardCharsets.UTF_8));
                    // Emit remaining chunks as CONC
                    int pos = available;
                    while (pos < escaped.length()) {
                        lineBuffer.setLength(0);
                        lineBuffer.append(level + 1).append(" CONC ");
                        int concPrefixLen = lineBuffer.length();
                        int concAvailable = config.getMaxLineLength() - concPrefixLen;
                        if (concAvailable <= 0) concAvailable = 1;
                        int end = Math.min(pos + concAvailable, escaped.length());
                        lineBuffer.append(escaped, pos, end);
                        lineBuffer.append(config.getLineEnding());
                        out.write(lineBuffer.toString().getBytes(StandardCharsets.UTF_8));
                        pos = end;
                    }
                    return;
                }
            }
            lineBuffer.append(' ').append(escaped);
        }
        lineBuffer.append(config.getLineEnding());
        out.write(lineBuffer.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits a value that may contain newlines, splitting into CONT lines.
     * The first line is emitted with the given tag at the given level.
     * Subsequent lines (after newline splits) become CONT at level+1.
     *
     * @param level the structure level
     * @param xref  the cross-reference identifier, or null
     * @param tag   the GEDCOM tag
     * @param value the value (may contain newlines)
     */
    public void emitValueWithCont(int level, String xref, String tag, String value) throws IOException {
        if (value == null) {
            emitLine(level, xref, tag, null);
            return;
        }

        // Normalize \r\n and \r to \n
        String normalized = value.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = splitLines(normalized);

        // First line goes with the tag
        emitLine(level, xref, tag, lines[0]);

        // Remaining lines become CONT at level+1
        for (int i = 1; i < lines.length; i++) {
            emitLine(level + 1, null, "CONT", lines[i]);
        }
    }

    /**
     * Emits a pointer line where the value is a cross-reference (@id@).
     * Pointer values are NOT subject to @@ escaping.
     */
    public void emitPointerLine(int level, String tag, String rawValue) throws IOException {
        lineBuffer.setLength(0);
        lineBuffer.append(level);
        lineBuffer.append(' ').append(tag);
        if (rawValue != null && !rawValue.isEmpty()) {
            lineBuffer.append(' ').append(rawValue);
        }
        lineBuffer.append(config.getLineEnding());
        out.write(lineBuffer.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits a tag-only line (no value).
     */
    public void emitTag(int level, String tag) throws IOException {
        emitLine(level, null, tag, null);
    }

    /**
     * Emits a line where part of the value is pre-escaped (not subject to @@-doubling)
     * and the remainder is processed through normal escapeAt() logic.
     *
     * <p>This is used for GEDCOM 5.5.5 calendar escape prefixes like {@code @#DJULIAN@}
     * which must pass through as-is, while the rest of the date value still gets
     * normal @-escaping.
     *
     * @param level            the structure level
     * @param xref             the cross-reference identifier, or null
     * @param tag              the GEDCOM tag
     * @param preEscapedPrefix the prefix that should NOT be @@-escaped
     * @param remainingValue   the remainder that SHOULD be @@-escaped (may be null or empty)
     */
    public void emitLinePreEscaped(int level, String xref, String tag,
                                    String preEscapedPrefix, String remainingValue) throws IOException {
        String escapedRemainder = escapeAt(remainingValue);
        String combinedValue = preEscapedPrefix +
                (escapedRemainder != null ? escapedRemainder : "");
        // Write the combined value directly without further escaping
        lineBuffer.setLength(0);
        lineBuffer.append(level);
        if (xref != null) {
            lineBuffer.append(' ').append('@').append(xref).append('@');
        }
        lineBuffer.append(' ').append(tag);
        if (!combinedValue.isEmpty()) {
            lineBuffer.append(' ').append(combinedValue);
        }
        lineBuffer.append(config.getLineEnding());
        out.write(lineBuffer.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Flushes the underlying output stream.
     */
    public void flush() throws IOException {
        out.flush();
    }

    public GedcomWriterConfig getConfig() {
        return config;
    }

    private String escapeAt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (config.isEscapeAllAt()) {
            // GEDCOM 5.5.5: double all @ characters
            return value.replace("@", "@@");
        } else {
            // GEDCOM 7: only double leading @
            if (value.charAt(0) == '@') {
                return "@" + value;
            }
            return value;
        }
    }

    /**
     * Splits a string at newline boundaries, preserving empty trailing segments.
     * This ensures that a value ending with \n produces a final empty CONT line.
     */
    private static String[] splitLines(String value) {
        if (value.indexOf('\n') < 0) {
            return new String[]{value};
        }
        return value.split("\n", -1);
    }
}
