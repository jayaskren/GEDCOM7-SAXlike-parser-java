package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Utf8InputDecoderTest {

    private final Utf8InputDecoder decoder = new Utf8InputDecoder();

    @Test
    void bomIsStripped() throws IOException {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '0', ' ', 'H', 'E', 'A', 'D'};
        try (Reader r = decoder.decode(new ByteArrayInputStream(bom))) {
            char[] buf = new char[20];
            int len = r.read(buf);
            String result = new String(buf, 0, len);
            assertEquals("0 HEAD", result);
        }
    }

    @Test
    void noBomPassthrough() throws IOException {
        byte[] data = "0 HEAD".getBytes(StandardCharsets.UTF_8);
        try (Reader r = decoder.decode(new ByteArrayInputStream(data))) {
            char[] buf = new char[20];
            int len = r.read(buf);
            assertEquals("0 HEAD", new String(buf, 0, len));
        }
    }

    @Test
    void multiByteUtf8() throws IOException {
        // Chinese character 家 (U+5BB6) is 3 bytes in UTF-8
        String input = "1 NOTE 家";
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        try (Reader r = decoder.decode(new ByteArrayInputStream(data))) {
            char[] buf = new char[20];
            int len = r.read(buf);
            assertEquals(input, new String(buf, 0, len));
        }
    }

    @Test
    void emptyInput() throws IOException {
        try (Reader r = decoder.decode(new ByteArrayInputStream(new byte[0]))) {
            assertEquals(-1, r.read());
        }
    }

    @Test
    void bomOnlyInput() throws IOException {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        try (Reader r = decoder.decode(new ByteArrayInputStream(bom))) {
            assertEquals(-1, r.read());
        }
    }
}
