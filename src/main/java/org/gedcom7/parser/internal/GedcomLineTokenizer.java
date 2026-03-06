package org.gedcom7.parser.internal;

import java.io.IOException;
import java.io.Reader;
import java.util.function.LongSupplier;

/**
 * Reads a character stream and parses GEDCOM lines into
 * {@link GedcomLine} tokens. Handles LF, CR, and CRLF
 * line endings. Reuses a single StringBuilder buffer to
 * minimize allocation.
 *
 * <p>Uses an internal 8KB character buffer for bulk reads,
 * eliminating per-character method calls on the underlying Reader.
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

    // Buffered I/O: 8KB char buffer for bulk reads
    private final char[] buf = new char[8192];
    private int bufPos;
    private int bufLimit;

    // Byte offset tracking
    private final LongSupplier byteCountSupplier;
    private long runningByteOffset;
    private long currentLineByteOffset;

    public GedcomLineTokenizer(Reader reader) {
        this(reader, DEFAULT_MAX_LINE_LENGTH);
    }

    public GedcomLineTokenizer(Reader reader, int maxLineLength) {
        this(reader, maxLineLength, null, 0);
    }

    /**
     * Creates a tokenizer with byte offset tracking.
     *
     * @param reader         the character stream to tokenize
     * @param maxLineLength  maximum allowed line length
     * @param byteCountSupplier  supplier of current byte count from the underlying stream,
     *                           or null if byte offsets are not available (Reader-based input)
     * @param initialByteOffset  initial byte offset (e.g., 3 for UTF-8 BOM)
     */
    public GedcomLineTokenizer(Reader reader, int maxLineLength,
                               LongSupplier byteCountSupplier, long initialByteOffset) {
        this.reader = reader;
        this.maxLineLength = maxLineLength;
        this.byteCountSupplier = byteCountSupplier;
        this.runningByteOffset = initialByteOffset;
    }

    /**
     * Returns the next character from the internal buffer, refilling from
     * the underlying Reader when the buffer is exhausted. Returns -1 on EOF.
     */
    private int readChar() throws IOException {
        if (bufPos >= bufLimit) {
            bufLimit = reader.read(buf, 0, buf.length);
            bufPos = 0;
            if (bufLimit <= 0) {
                return -1;
            }
        }
        return buf[bufPos++];
    }

    /**
     * Reads the next GEDCOM line and populates the given
     * {@link GedcomLine} token. Returns false if no more
     * lines are available.
     */
    public boolean nextLine(GedcomLine line) throws IOException {
        while (true) {
            if (eof) return false;

            // Record byte offset at the start of this line
            currentLineByteOffset = runningByteOffset;

            lineBuffer.setLength(0);
            boolean foundContent = readRawLine();

            if (lineBuffer.length() == 0) {
                if (eof) return false;
                // Blank line — skip (but account for line ending bytes)
                continue;
            }

            lineNumber++;
            line.reset();
            line.setLineNumber(lineNumber);
            line.setRawLine(lineBuffer.toString());

            // Set byte offset: -1 when no InputStream-based tracking is available
            if (byteCountSupplier != null) {
                line.setByteOffset(currentLineByteOffset);
            } else {
                line.setByteOffset(-1);
            }

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
                ch = readChar();
            }

            if (ch == -1) {
                eof = true;
                return any;
            }

            if (ch == '\n') {
                // LF is 1 byte in UTF-8
                runningByteOffset += utf8ByteLength(lineBuffer) + 1;
                return true;
            }

            if (ch == '\r') {
                // Check for CRLF
                int next = readChar();
                int lineEndingBytes;
                if (next == '\n') {
                    lineEndingBytes = 2; // CRLF
                } else if (next == -1) {
                    lineEndingBytes = 1; // CR at EOF
                    eof = true;
                } else {
                    lineEndingBytes = 1; // CR only
                    pendingChar = next;
                }
                runningByteOffset += utf8ByteLength(lineBuffer) + lineEndingBytes;
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
     * Computes the UTF-8 byte length of the given char sequence.
     */
    private static long utf8ByteLength(CharSequence cs) {
        long bytes = 0;
        for (int i = 0; i < cs.length(); i++) {
            char ch = cs.charAt(i);
            if (ch <= 0x7F) {
                bytes += 1;
            } else if (ch <= 0x7FF) {
                bytes += 2;
            } else if (Character.isHighSurrogate(ch)) {
                bytes += 4; // surrogate pair = 4 bytes
                i++; // skip low surrogate
            } else {
                bytes += 3;
            }
        }
        return bytes;
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
        try {
            line.setLevel(Integer.parseInt(buf.substring(levelStart, pos)));
        } catch (NumberFormatException e) {
            line.setTag("");
            return;
        }

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
