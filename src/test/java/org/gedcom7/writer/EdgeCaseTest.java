package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseTest {

    private String write(GedcomWriterTest.WriterAction action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            action.execute(writer);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void nullValueOmitted() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("Test"));
            writer.individual(indi -> {
                indi.personalName("John /Doe/");
                indi.sex((String) null);
                indi.occupation(null);
            });
            writer.trailer();
        });
        assertFalse(output.contains("SEX"));
        assertFalse(output.contains("OCCU"));
    }

    @Test
    void emptyStringTreatedAsNoValue() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("Test"));
            writer.individual(indi -> {
                indi.personalName("John /Doe/");
                indi.structure("NOTE", "");
            });
            writer.trailer();
        });
        // Empty string should produce tag without value
        assertTrue(output.contains("1 NOTE\n"));
    }

    @Test
    void deeplyNestedStructures() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("Test"));
            writer.individual(indi -> {
                indi.birth(birt -> {
                    birt.place("Springfield", ctx -> {
                        ctx.structure("MAP", mapCtx -> {
                            mapCtx.structure("LATI", "N39.7817");
                            mapCtx.structure("LONG", "W89.6501");
                        });
                    });
                });
            });
            writer.trailer();
        });
        assertTrue(output.contains("2 PLAC Springfield\n"));
        assertTrue(output.contains("3 MAP\n"));
        assertTrue(output.contains("4 LATI N39.7817\n"));
        assertTrue(output.contains("4 LONG W89.6501\n"));
    }

    @Test
    void lambdaExceptionPropagation() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("Test"));
            // GedcomWriteException is now unchecked (RuntimeException), so throw directly
            assertThrows(GedcomWriteException.class, () ->
                    writer.individual(indi -> {
                        throw new GedcomWriteException("test error");
                    }));
        }
    }

    @Test
    void multipleRecordTypes() throws Exception {
        String output = write(writer -> {
            writer.head(head -> head.source("Test"));
            Xref subm = writer.submitter(s -> s.name("Jay"));
            Xref src = writer.source(s -> s.title("Census"));
            Xref repo = writer.repository(r -> r.name("Archive"));
            writer.individual(indi -> {
                indi.personalName("John /Doe/");
                indi.sourceCitation(src);
            });
            writer.family(fam -> {});
            writer.multimedia(m -> m.file("pic.jpg"));
            writer.sharedNote(n -> {});
            writer.trailer();
        });
        assertTrue(output.contains("0 @U1@ SUBM\n"));
        assertTrue(output.contains("0 @S1@ SOUR\n"));
        assertTrue(output.contains("0 @R1@ REPO\n"));
        assertTrue(output.contains("0 @I1@ INDI\n"));
        assertTrue(output.contains("0 @F1@ FAM\n"));
        assertTrue(output.contains("0 @O1@ OBJE\n"));
        assertTrue(output.contains("0 @N1@ SNOTE\n"));
    }
}
