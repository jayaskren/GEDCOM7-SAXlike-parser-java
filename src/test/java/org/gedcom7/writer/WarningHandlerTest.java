package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarningHandlerTest {

    @Test
    void lenientModeDeliversWarningToHandler() throws Exception {
        List<GedcomWriteWarning> warnings = new ArrayList<>();
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7().toBuilder()
                .warningHandler(warnings::add)
                .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, config)) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            writer.individual(indi -> indi.familyAsSpouse(fam));
        }
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains("FAMS"));
        assertEquals("FAMS", warnings.get(0).getTag());
    }

    @Test
    void nullHandlerSuppressesWarnings() throws Exception {
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7().toBuilder()
                .warningHandler(null)
                .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Should not throw
        try (GedcomWriter writer = new GedcomWriter(out, config)) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            writer.individual(indi -> indi.familyAsSpouse(fam));
        }
    }

    @Test
    void customHandlerReceivesStructuredWarning() throws Exception {
        List<GedcomWriteWarning> warnings = new ArrayList<>();
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7().toBuilder()
                .warningHandler(warnings::add)
                .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, config)) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            writer.individual(indi -> indi.familyAsChild(fam));
        }
        assertEquals(1, warnings.size());
        GedcomWriteWarning w = warnings.get(0);
        assertNotNull(w.getMessage());
        assertEquals("FAMC", w.getTag());
        assertNotNull(w.toString());
    }

    @Test
    void autoHeadDeliversWarning() throws Exception {
        List<GedcomWriteWarning> warnings = new ArrayList<>();
        GedcomWriterConfig config = GedcomWriterConfig.gedcom7().toBuilder()
                .warningHandler(warnings::add)
                .build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, config)) {
            // Don't call head() — should auto-generate and warn
            writer.individual(indi -> indi.personalName("John /Doe/"));
        }
        boolean hasAutoHeadWarning = warnings.stream()
                .anyMatch(w -> w.getMessage().contains("HEAD"));
        assertTrue(hasAutoHeadWarning, "Expected a warning about auto-generated HEAD");
    }
}
