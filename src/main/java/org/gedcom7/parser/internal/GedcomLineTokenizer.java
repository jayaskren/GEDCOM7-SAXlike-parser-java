package org.gedcom7.parser.internal;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads a character stream and parses GEDCOM lines into
 * {@link GedcomLine} tokens. Handles LF, CR, and CRLF
 * line endings. Reuses a single StringBuilder buffer to
 * minimize allocation.
 *
 * <p>GEDCOM line grammar (ABNF):
 * <pre>
 * line    = level D [xref D] tag [D lineVal] EOL
 * level   = "0" / nonZeroDigit *digit
 * xref    = "@" 1*xrefChar "@"
 * tag     = stdTag / extTag
 * lineVal = pointer / lineStr
 * pointer = "@" 1*xrefChar "@"
 * </pre>
 */
public final class GedcomLineTokenizer {

    private static final int DEFAULT_MAX_LINE_LENGTH = 1_048_576;

    private final Reader reader;
    private final int maxLineLength;
    private final StringBuilder lineBuffer = new StringBuilder(256);
    private int lineNumber;
    private boolean eof;
    private int pendingChar = -2; // -2 = nothing pending, -1 = EOF

    public GedcomLineTokenizer(Reader reader) {
        this(reader, DEFAULT_MAX_LINE_LENGTH);
    }

    public GedcomLineTokenizer(Reader reader, int maxLineLength) {
        this.reader = reader;
        this.maxLineLength = maxLineLength;
    }

    /**
     * Reads the next GEDCOM line and populates the given
     * {@link GedcomLine} token. Returns false if no more
     * lines are available.
     */
    public boolean nextLine(GedcomLine line) throws IOException {
        while (true) {
            if (eof) return false;

            lineBuffer.setLength(0);
            boolean foundContent = readRawLine();

            if (lineBuffer.length() == 0) {
                if (eof) return false;
                // Blank line — skip
                continue;
            }

            lineNumber++;
            line.reset();
            line.setLineNumber(lineNumber);
            line.setRawLine(lineBuffer.toString());
            parseLine(lineBuffer, line);
            return true;
        }
    }

    /**
     * Reads characters until EOL or EOF, placing them in
     * lineBuffer. Returns true if any characters were read.
     */
    private boolean readRawLine() throws IOException {
        boolean any = false;
        while (true) {
            int ch;
            if (pendingChar != -2) {
                ch = pendingChar;
                pendingChar = -2;
            } else {
                ch = reader.read();
            }

            if (ch == -1) {
                eof = true;
                return any;
            }

            if (ch == '\n') {
                return true;
            }

            if (ch == '\r') {
                // Check for CRLF
                int next = reader.read();
                if (next != '\n' && next != -1) {
                    pendingChar = next;
                } else if (next == -1) {
                    eof = true;
                }
                return true;
            }

            lineBuffer.append((char) ch);
            if (lineBuffer.length() > maxLineLength) {
                throw new IOException("Line exceeds maximum length of " + maxLineLength + " characters at line " + (lineNumber + 1));
            }
            any = true;
        }
    }

    /**
     * Parses the contents of lineBuffer into the GedcomLine fields.
     */
    private void parseLine(StringBuilder buf, GedcomLine line) {
        int pos = 0;
        int len = buf.length();

        // Skip leading whitespace (FR-009)
        while (pos < len && (buf.charAt(pos) == ' ' || buf.charAt(pos) == '\t')) {
            pos++;
        }
        if (pos > 0) {
            line.setLeadingWhitespace(true);
        }

        // Parse level (digits)
        int levelStart = pos;
        while (pos < len && buf.charAt(pos) >= '0' && buf.charAt(pos) <= '9') {
            pos++;
        }
        if (pos == levelStart) {
            // No level found — malformed
            line.setTag("");
            return;
        }
        line.setLevel(Integer.parseInt(buf.substring(levelStart, pos)));

        // Expect delimiter (space)
        if (pos >= len || buf.charAt(pos) != ' ') {
            line.setTag("");
            return;
        }
        pos++; // skip space

        // Check for optional xref (@...@)
        if (pos < len && buf.charAt(pos) == '@') {
            int xrefStart = pos + 1;
            pos++; // skip opening @
            while (pos < len && buf.charAt(pos) != '@') {
                pos++;
            }
            if (pos < len) {
                line.setXref(buf.substring(xrefStart, pos));
                pos++; // skip closing @
            }
            // Expect delimiter after xref
            if (pos >= len || buf.charAt(pos) != ' ') {
                line.setTag("");
                return;
            }
            pos++; // skip space
        }

        // Parse tag
        int tagStart = pos;
        while (pos < len && buf.charAt(pos) != ' ') {
            pos++;
        }
        if (pos == tagStart) {
            line.setTag("");
            return;
        }
        line.setTag(buf.substring(tagStart, pos));

        // Optional value after space
        if (pos < len && buf.charAt(pos) == ' ') {
            pos++; // skip space
            String value = buf.substring(pos);
            // Check if value is a pointer (@...@)
            if (value.length() >= 3
                    && value.charAt(0) == '@'
                    && value.charAt(value.length() - 1) == '@'
                    && value.indexOf('@', 1) == value.length() - 1) {
                line.setPointer(true);
            }
            line.setValue(value);
        }
    }
}
