package org.gedcom7.parser.internal;

import org.gedcom7.parser.spi.GedcomInputDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Decodes a GEDCOM byte stream as UTF-8, stripping a leading
 * BOM (U+FEFF) if present at the start of the stream.
 */
public final class Utf8InputDecoder implements GedcomInputDecoder {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Override
    public Reader decode(InputStream input) throws IOException {
        PushbackInputStream pis = new PushbackInputStream(input, 3);
        byte[] bom = new byte[3];
        int read = pis.read(bom, 0, 3);
        if (read >= 3
                && bom[0] == UTF8_BOM[0]
                && bom[1] == UTF8_BOM[1]
                && bom[2] == UTF8_BOM[2]) {
            // BOM found and consumed — do not push back
        } else if (read > 0) {
            pis.unread(bom, 0, read);
        }
        return new InputStreamReader(pis, StandardCharsets.UTF_8);
    }
}
