package org.gedcom7.parser.internal;

import org.gedcom7.parser.spi.GedcomInputDecoder;

import java.io.*;
import java.nio.charset.*;

/**
 * Detects encoding from BOM (Byte Order Mark) and returns
 * an appropriate Reader. Supports UTF-8, UTF-16 BE, and UTF-16 LE.
 * Defaults to UTF-8 if no BOM is found.
 */
public final class BomDetectingDecoder implements GedcomInputDecoder {

    private Charset detectedCharset;
    private boolean bomFound;

    @Override
    public Reader decode(InputStream input) throws IOException {
        PushbackInputStream pis = new PushbackInputStream(input, 3);
        byte[] bom = new byte[3];
        int read = pis.read(bom, 0, 3);

        if (read >= 2 && bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
            // UTF-16 BE BOM
            detectedCharset = StandardCharsets.UTF_16BE;
            bomFound = true;
            if (read > 2) pis.unread(bom, 2, read - 2);
        } else if (read >= 2 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
            // UTF-16 LE BOM
            detectedCharset = Charset.forName("UTF-16LE");
            bomFound = true;
            if (read > 2) pis.unread(bom, 2, read - 2);
        } else if (read >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            // UTF-8 BOM
            detectedCharset = StandardCharsets.UTF_8;
            bomFound = true;
        } else {
            // No BOM — default to UTF-8
            detectedCharset = StandardCharsets.UTF_8;
            bomFound = false;
            if (read > 0) pis.unread(bom, 0, read);
        }

        return new InputStreamReader(pis, detectedCharset);
    }

    /** Returns the detected charset after decode() has been called. */
    public Charset getDetectedCharset() { return detectedCharset; }

    /** Returns true if a BOM was found during decode(). */
    public boolean isBomFound() { return bomFound; }
}
