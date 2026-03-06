package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class StrictModeTest {

    @Test
    void famsInGedcom7StrictThrows() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom7Strict())) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            assertThrows(GedcomWriteException.class, () ->
                    writer.individual(indi -> indi.familyAsSpouse(fam)));
        }
    }

    @Test
    void famcInGedcom7StrictThrows() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom7Strict())) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            assertThrows(GedcomWriteException.class, () ->
                    writer.individual(indi -> indi.familyAsChild(fam)));
        }
    }

    @Test
    void missingHeadInStrictThrows() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom7Strict())) {
            // Don't call head() — strict mode should throw when writing first record
            assertThrows(GedcomWriteException.class, () ->
                    writer.individual(indi -> indi.personalName("John /Doe/")));
        }
    }

    @Test
    void famsIn555StrictDoesNotThrow() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom555Strict())) {
            writer.head(head -> head.source("Test"));
            Xref fam = writer.family(f -> {});
            assertDoesNotThrow(() ->
                    writer.individual(indi -> indi.familyAsSpouse(fam)));
        }
    }
}
