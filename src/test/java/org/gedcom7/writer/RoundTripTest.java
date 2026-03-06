package org.gedcom7.writer;

import org.gedcom7.parser.GedcomReader;
import org.gedcom7.parser.GedcomReaderConfig;
import org.gedcom7.parser.GedcomHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundTripTest {

    @Test
    void writeAndParseBack() throws Exception {
        // Write a GEDCOM file
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {
            writer.head(head -> head.source("RoundTripTest"));
            Xref john = writer.individual(indi -> {
                indi.personalName("John /Doe/");
                indi.sex("M");
                indi.birth(birt -> {
                    birt.date("15 MAR 1955");
                    birt.place("Springfield, IL");
                });
            });
            Xref jane = writer.individual(indi -> {
                indi.personalName("Jane /Smith/");
                indi.sex("F");
            });
            writer.family(fam -> {
                fam.husband(john);
                fam.wife(jane);
            });
            writer.trailer();
        }

        // Parse it back
        byte[] gedcomBytes = out.toByteArray();
        List<String> tags = new ArrayList<>();

        ByteArrayInputStream in = new ByteArrayInputStream(gedcomBytes);
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                tags.add(tag);
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                tags.add(tag);
            }
        };

        GedcomReader reader = new GedcomReader(in, handler, GedcomReaderConfig.gedcom7());
        reader.parse();

        // Verify no parse errors and key tags present
        assertTrue(tags.contains("HEAD"), "Should contain HEAD");
        assertTrue(tags.contains("INDI"), "Should contain INDI");
        assertTrue(tags.contains("NAME"), "Should contain NAME");
        assertTrue(tags.contains("BIRT"), "Should contain BIRT");
        assertTrue(tags.contains("DATE"), "Should contain DATE");
        assertTrue(tags.contains("PLAC"), "Should contain PLAC");
        assertTrue(tags.contains("FAM"), "Should contain FAM");
        assertTrue(tags.contains("HUSB"), "Should contain HUSB");
        assertTrue(tags.contains("WIFE"), "Should contain WIFE");
        assertTrue(tags.contains("TRLR"), "Should contain TRLR");
    }
}
