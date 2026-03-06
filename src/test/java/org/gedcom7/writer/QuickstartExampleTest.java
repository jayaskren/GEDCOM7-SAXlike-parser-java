package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.gedcom7.writer.date.GedcomDateBuilder.*;
import static org.gedcom7.writer.date.Month.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the complete family tree example from quickstart.md Section 10.
 */
class QuickstartExampleTest {

    @Test
    void completeFamilyTreeExample() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out)) {

            // Header
            writer.head(head -> {
                head.source("FamilyTreeApp", src -> {
                    src.structure("VERS", "1.0");
                    src.structure("NAME", "My Family Tree App");
                });
            });

            // Submitter
            Xref subm = writer.submitter(s -> {
                s.name("Jane Researcher");
            });

            // Sources
            Xref census = writer.source(src -> {
                src.title("1880 US Federal Census");
                src.author("US Census Bureau");
            });

            // Grandparents
            Xref grandpa = writer.individual(indi -> {
                indi.personalName("William /Doe/", name -> {
                    name.givenName("William");
                    name.surname("Doe");
                });
                indi.sex("M");
                indi.birth(birt -> {
                    birt.date(about(1855));
                    birt.place("County Cork, Ireland");
                });
                indi.death(deat -> {
                    deat.date(date(12, FEB, 1920));
                    deat.place("Boston, MA");
                });
            });

            Xref grandma = writer.individual(indi -> {
                indi.personalName("Mary /O'Brien/", name -> {
                    name.givenName("Mary");
                    name.surname("O'Brien");
                });
                indi.sex("F");
                indi.birth(birt -> {
                    birt.date(about(1860));
                    birt.place("County Kerry, Ireland");
                });
            });

            // Parents
            Xref dad = writer.individual(indi -> {
                indi.personalName("John /Doe/", name -> {
                    name.givenName("John");
                    name.surname("Doe");
                });
                indi.sex("M");
                indi.birth(birt -> {
                    birt.date(date(15, MAR, 1885));
                    birt.place("Boston, MA");
                    birt.sourceCitation(census, cite -> {
                        cite.page("Roll 108, Page 42");
                    });
                });
            });

            Xref mom = writer.individual(indi -> {
                indi.personalName("Sarah /Jones/", name -> {
                    name.givenName("Sarah");
                    name.surname("Jones");
                });
                indi.sex("F");
                indi.birth(birt -> {
                    birt.date(date(22, JUL, 1890));
                });
            });

            // Children
            Xref child1 = writer.individual(indi -> {
                indi.personalName("Robert /Doe/");
                indi.sex("M");
                indi.birth(birt -> birt.date(date(3, JAN, 1915)));
            });

            Xref child2 = writer.individual(indi -> {
                indi.personalName("Elizabeth /Doe/");
                indi.sex("F");
                indi.birth(birt -> birt.date(date(17, SEP, 1918)));
            });

            // Families
            writer.family(fam -> {
                fam.husband(grandpa);
                fam.wife(grandma);
                fam.child(dad);
                fam.marriage(marr -> {
                    marr.date(about(1882));
                    marr.place("Boston, MA");
                });
            });

            writer.family(fam -> {
                fam.husband(dad);
                fam.wife(mom);
                fam.child(child1);
                fam.child(child2);
                fam.marriage(marr -> {
                    marr.date(date(8, JUN, 1912));
                    marr.place("Cambridge, MA");
                });
            });
        }

        String output = out.toString(StandardCharsets.UTF_8.name());

        // Verify structure
        assertTrue(output.contains("0 HEAD\n"));
        assertTrue(output.contains("1 GEDC\n"));
        assertTrue(output.contains("2 VERS 7.0\n"));
        assertTrue(output.contains("1 SOUR FamilyTreeApp\n"));
        assertTrue(output.contains("2 VERS 1.0\n"));
        assertTrue(output.contains("2 NAME My Family Tree App\n"));

        // Submitter
        assertTrue(output.contains("0 @U1@ SUBM\n"));
        assertTrue(output.contains("1 NAME Jane Researcher\n"));

        // Source
        assertTrue(output.contains("0 @S1@ SOUR\n"));
        assertTrue(output.contains("1 TITL 1880 US Federal Census\n"));
        assertTrue(output.contains("1 AUTH US Census Bureau\n"));

        // Grandpa
        assertTrue(output.contains("1 NAME William /Doe/\n"));
        assertTrue(output.contains("2 GIVN William\n"));
        assertTrue(output.contains("2 DATE ABT 1855\n"));
        assertTrue(output.contains("2 PLAC County Cork, Ireland\n"));
        assertTrue(output.contains("2 DATE 12 FEB 1920\n"));

        // Dad with source citation
        assertTrue(output.contains("3 PAGE Roll 108, Page 42\n"));

        // Children
        assertTrue(output.contains("1 NAME Robert /Doe/\n"));
        assertTrue(output.contains("2 DATE 3 JAN 1915\n"));
        assertTrue(output.contains("1 NAME Elizabeth /Doe/\n"));
        assertTrue(output.contains("2 DATE 17 SEP 1918\n"));

        // Families
        assertTrue(output.contains("0 @F1@ FAM\n"));
        assertTrue(output.contains("0 @F2@ FAM\n"));
        assertTrue(output.contains("2 DATE ABT 1882\n"));
        assertTrue(output.contains("2 DATE 8 JUN 1912\n"));
        assertTrue(output.contains("2 PLAC Cambridge, MA\n"));

        // Trailer
        assertTrue(output.contains("0 TRLR\n"));
    }
}
