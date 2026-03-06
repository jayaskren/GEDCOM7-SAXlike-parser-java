package org.gedcom7.parser.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * SPI for custom byte-to-character decoding of GEDCOM streams.
 * Implementations handle encoding detection (e.g., BOM stripping
 * for UTF-8) and wrap raw byte streams as character Readers.
 *
 * <p>This interface is part of the public SPI. Implementations
 * should be placed in the {@code org.gedcom7.parser.internal}
 * package or in user-provided packages.
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
