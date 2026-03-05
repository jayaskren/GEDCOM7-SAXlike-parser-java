package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
        Object mockDecoder = new Object();
        Object mockAssembler = new Object();
        Object mockEscape = new Object();

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
        Object mockDecoder = new Object();
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
