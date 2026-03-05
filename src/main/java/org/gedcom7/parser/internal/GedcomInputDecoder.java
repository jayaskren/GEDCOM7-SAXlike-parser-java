package org.gedcom7.parser.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Strategy interface for decoding a GEDCOM byte stream into
 * characters. Implementations handle encoding detection (e.g.,
 * BOM stripping for UTF-8).
 *
 * <p>This is an internal interface and not part of the public API.
 */
public interface GedcomInputDecoder {

    /**
     * Wraps the raw input stream as a character Reader,
     * performing any necessary encoding detection and setup.
     *
     * @param input the raw byte stream
     * @return a Reader over the decoded character stream
     * @throws IOException if an I/O error occurs during setup
     */
    Reader decode(InputStream input) throws IOException;
}
