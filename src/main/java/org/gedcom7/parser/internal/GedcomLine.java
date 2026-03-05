package org.gedcom7.parser.internal;

/**
 * Mutable token representing a parsed GEDCOM line.
 * Reused across iterations to minimize allocation in the
 * hot parsing loop.
 *
 * <p>This is an internal class and not part of the public API.
 */
public final class GedcomLine {

    private int level;
    private String xref;
    private String tag;
    private String value;
    private boolean pointer;
    private int lineNumber;
    private long byteOffset;
    private String rawLine;
    private boolean leadingWhitespace;

    public void reset() {
        level = 0;
        xref = null;
        tag = null;
        value = null;
        pointer = false;
        lineNumber = 0;
        byteOffset = 0;
        rawLine = null;
        leadingWhitespace = false;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getXref() { return xref; }
    public void setXref(String xref) { this.xref = xref; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isPointer() { return pointer; }
    public void setPointer(boolean pointer) { this.pointer = pointer; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public long getByteOffset() { return byteOffset; }
    public void setByteOffset(long byteOffset) { this.byteOffset = byteOffset; }

    public String getRawLine() { return rawLine; }
    public void setRawLine(String rawLine) { this.rawLine = rawLine; }

    public boolean hasLeadingWhitespace() { return leadingWhitespace; }
    public void setLeadingWhitespace(boolean leadingWhitespace) { this.leadingWhitespace = leadingWhitespace; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GedcomLine{level=").append(level);
        if (xref != null) sb.append(", xref=").append(xref);
        sb.append(", tag=").append(tag);
        if (value != null) sb.append(", value=").append(value);
        if (pointer) sb.append(", pointer");
        sb.append(", line=").append(lineNumber);
        sb.append('}');
        return sb.toString();
    }
}
