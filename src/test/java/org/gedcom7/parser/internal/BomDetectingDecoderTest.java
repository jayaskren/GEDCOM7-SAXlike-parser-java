package org.gedcom7.parser.internal;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BomDetectingDecoderTest {

    @Test
    void utf8BomDetectedAndStripped() throws IOException {
        BomDetectingDecoder decoder = new BomDetectingDecoder();
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '0', ' ', 'H', 'E', 'A', 'D'};
        try (Reader r = decoder.decode(new ByteArrayInputStream(bom))) {
            char[] buf = new char[20];
            int len = r.read(buf);
            assertEquals("0 HEAD", new String(buf, 0, len));
        }
        assertEquals(StandardCharsets.UTF_8, decoder.getDetectedCharset());
        assertTrue(decoder.isBomFound());
    }

    @Test
    void utf16BeBomDetected() throws IOException {
        BomDetectingDecoder decoder = new BomDetectingDecoder();
        String content = "0 HEAD";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16BE);
        byte[] data = new byte[2 + contentBytes.length];
        data[0] = (byte) 0xFE;
        data[1] = (byte) 0xFF;
        System.arraycopy(contentBytes, 0, data, 2, contentBytes.length);

        try (Reader r = decoder.decode(new ByteArrayInputStream(data))) {
            char[] buf = new char[20];
            int len = r.read(buf);
            assertEquals("0 HEAD", new String(buf, 0, len));
        }
        assertEquals(StandardCharsets.UTF_16BE, decoder.getDetectedCharset());
        assertTrue(decoder.isBomFound());
    }

    @Test
    void utf16LeBomDetected() throws IOException {
        BomDetectingDecoder decoder = new BomDetectingDecoder();
        String content = "0 HEAD";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16LE);
        byte[] data = new byte[2 + contentBytes.length];
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xFE;
        System.arraycopy(contentBytes, 0, data, 2, contentBytes.length);

        try (Reader r = decoder.decode(new ByteArrayInputStream(data))) {
            char[] buf = new char[20];
            int len = r.read(buf);
            assertEquals("0 HEAD", new String(buf, 0, len));
        }
        assertEquals(Charset.forName("UTF-16LE"), decoder.getDetectedCharset());
        assertTrue(decoder.isBomFound());
    }

    @Test
    void noBomDefaultsToUtf8() throws IOException {
        BomDetectingDecoder decoder = new BomDetectingDecoder();
        byte[] data = "0 HEAD".getBytes(StandardCharsets.UTF_8);
        try (Reader r = decoder.decode(new ByteArrayInputStream(data))) {
            char[] buf = new char[20];
            int len = r.read(buf);
            assertEquals("0 HEAD", new String(buf, 0, len));
        }
        assertEquals(StandardCharsets.UTF_8, decoder.getDetectedCharset());
        assertFalse(decoder.isBomFound());
    }

    @Test
    void emptyStreamHandling() throws IOException {
        BomDetectingDecoder decoder = new BomDetectingDecoder();
        try (Reader r = decoder.decode(new ByteArrayInputStream(new byte[0]))) {
            assertEquals(-1, r.read());
        }
        assertEquals(StandardCharsets.UTF_8, decoder.getDetectedCharset());
        assertFalse(decoder.isBomFound());
    }

    @Test
    void getDetectedCharsetReturnsCorrectCharset() throws IOException {
        BomDetectingDecoder decoder = new BomDetectingDecoder();
        assertNull(decoder.getDetectedCharset());

        byte[] bom = {(byte) 0xFE, (byte) 0xFF, 0x00, 0x41};
        try (Reader r = decoder.decode(new ByteArrayInputStream(bom))) {
            assertNotNull(decoder.getDetectedCharset());
            assertEquals(StandardCharsets.UTF_16BE, decoder.getDetectedCharset());
        }
    }

    @Test
    void isBomFoundReturnsCorrectValue() throws IOException {
        BomDetectingDecoder decoder = new BomDetectingDecoder();
        assertFalse(decoder.isBomFound());

        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        try (Reader r = decoder.decode(new ByteArrayInputStream(data))) {
            assertFalse(decoder.isBomFound());
        }

        BomDetectingDecoder decoder2 = new BomDetectingDecoder();
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'A'};
        try (Reader r = decoder2.decode(new ByteArrayInputStream(bom))) {
            assertTrue(decoder2.isBomFound());
        }
    }
}
