package org.gedcom7.parser;

import org.gedcom7.parser.spi.AtEscapeStrategy;
import org.gedcom7.parser.spi.GedcomInputDecoder;
import org.gedcom7.parser.spi.PayloadAssembler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PluggableStrategyTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void defaultConfig_hasNullStrategies() {
        GedcomReaderConfig config = GedcomReaderConfig.gedcom7();
        assertNull(config.getInputDecoderOrNull());
        assertNull(config.getPayloadAssemblerOrNull());
        assertNull(config.getAtEscapeStrategyOrNull());
    }

    @Test
    void builderPreservesStrategies() {
        GedcomInputDecoder mockDecoder = input -> new InputStreamReader(input, StandardCharsets.UTF_8);
        PayloadAssembler mockAssembler = new PayloadAssembler() {
            @Override public boolean isPseudoStructure(String tag) { return false; }
            @Override public void appendPayload(StringBuilder payload, String continuationValue, String tag) {}
        };
        AtEscapeStrategy mockEscape = value -> value;

        GedcomReaderConfig config = new GedcomReaderConfig.Builder()
                .inputDecoder(mockDecoder)
                .payloadAssembler(mockAssembler)
                .atEscapeStrategy(mockEscape)
                .build();

        assertSame(mockDecoder, config.getInputDecoderOrNull());
        assertSame(mockAssembler, config.getPayloadAssemblerOrNull());
        assertSame(mockEscape, config.getAtEscapeStrategyOrNull());
    }

    @Test
    void toBuilder_preservesStrategies() {
        GedcomInputDecoder mockDecoder = input -> new InputStreamReader(input, StandardCharsets.UTF_8);
        GedcomReaderConfig original = new GedcomReaderConfig.Builder()
                .inputDecoder(mockDecoder)
                .build();
        GedcomReaderConfig copy = original.toBuilder().build();
        assertSame(mockDecoder, copy.getInputDecoderOrNull());
    }

    @Test
    void defaultConfig_parsesSuccessfully() {
        // Default strategies should work for normal GEDCOM 7 parsing
        String input = "0 HEAD\n1 GEDC\n2 VERS 7.0\n0 TRLR\n";
        List<String> events = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                events.add(tag);
            }
        };
        try (GedcomReader reader = new GedcomReader(
                stream(input), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
        assertTrue(events.contains("HEAD"));
        assertTrue(events.contains("TRLR"));
    }
}
