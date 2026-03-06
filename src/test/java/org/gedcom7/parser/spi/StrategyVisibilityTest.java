package org.gedcom7.parser.spi;

import org.gedcom7.parser.GedcomHandler;
import org.gedcom7.parser.GedcomReader;
import org.gedcom7.parser.GedcomReaderConfig;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the SPI interfaces are accessible from outside the
 * {@code org.gedcom7.parser.internal} package and can be used
 * to create custom strategy implementations that plug into
 * {@link GedcomReaderConfig.Builder}.
 *
 * <p>This test is intentionally in the {@code spi} package (not
 * {@code internal}) to verify cross-package visibility.
 */
class StrategyVisibilityTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void customInputDecoder_acceptedByBuilder() {
        GedcomInputDecoder decoder = input ->
                new InputStreamReader(input, StandardCharsets.UTF_8);

        GedcomReaderConfig config = GedcomReaderConfig.gedcom7()
                .toBuilder()
                .inputDecoder(decoder)
                .build();

        assertNotNull(config);
        assertSame(decoder, config.getInputDecoderOrNull());
    }

    @Test
    void customPayloadAssembler_acceptedByBuilder() {
        PayloadAssembler assembler = new PayloadAssembler() {
            @Override
            public boolean isPseudoStructure(String tag) {
                return "CONT".equals(tag);
            }

            @Override
            public void appendPayload(StringBuilder payload,
                                       String continuationValue, String tag) {
                payload.append('\n');
                if (continuationValue != null) {
                    payload.append(continuationValue);
                }
            }
        };

        GedcomReaderConfig config = GedcomReaderConfig.gedcom7()
                .toBuilder()
                .payloadAssembler(assembler)
                .build();

        assertNotNull(config);
        assertSame(assembler, config.getPayloadAssemblerOrNull());
    }

    @Test
    void customAtEscapeStrategy_acceptedByBuilder() {
        AtEscapeStrategy strategy = value -> value; // no-op unescape

        GedcomReaderConfig config = GedcomReaderConfig.gedcom7()
                .toBuilder()
                .atEscapeStrategy(strategy)
                .build();

        assertNotNull(config);
        assertSame(strategy, config.getAtEscapeStrategyOrNull());
    }

    @Test
    void customInputDecoder_invokedDuringParse() {
        AtomicBoolean decodeCalled = new AtomicBoolean(false);

        GedcomInputDecoder recorder = input -> {
            decodeCalled.set(true);
            return new InputStreamReader(input, StandardCharsets.UTF_8);
        };

        GedcomReaderConfig config = GedcomReaderConfig.gedcom7()
                .toBuilder()
                .inputDecoder(recorder)
                .build();

        String gedcom = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        GedcomHandler handler = new GedcomHandler() {};

        try (GedcomReader reader = new GedcomReader(
                stream(gedcom), handler, config)) {
            reader.parse();
        }

        assertTrue(decodeCalled.get(),
                "Custom GedcomInputDecoder.decode() should have been invoked");
    }
}
