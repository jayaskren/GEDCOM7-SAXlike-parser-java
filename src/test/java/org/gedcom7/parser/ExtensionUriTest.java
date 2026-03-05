package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionUriTest {

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream resource(String name) {
        InputStream is = getClass().getResourceAsStream("/" + name);
        assertNotNull(is, "Test resource not found: " + name);
        return is;
    }

    @Test
    void sixParamStartStructure_defaultDelegatesToFiveParam() {
        // Verify backward compatibility: handler overriding only 5-param still works
        List<String> events = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                events.add("5param:" + tag);
            }
        };
        // Call the 6-param version directly
        handler.startStructure(1, null, "_CUSTOM", "value", false, "https://example.com");
        assertEquals(1, events.size());
        assertEquals("5param:_CUSTOM", events.get(0));
    }

    @Test
    void sixParamOverride_receivesUri() {
        List<String> uris = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer, String uri) {
                uris.add(tag + "=" + uri);
            }
        };
        handler.startStructure(1, null, "_CUSTOM", null, false, "https://example.com/ext");
        assertEquals("_CUSTOM=https://example.com/ext", uris.get(0));
    }

    @Test
    void sixParamOverride_nullUriForStandardTags() {
        List<String> uris = new ArrayList<>();
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer, String uri) {
                uris.add(tag + "=" + uri);
            }
        };
        handler.startStructure(1, null, "NAME", "John", false, null);
        assertEquals("NAME=null", uris.get(0));
    }
}
