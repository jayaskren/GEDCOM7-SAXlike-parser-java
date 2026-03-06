package org.gedcom7.writer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that calendar escape prefixes in date strings are preserved (not @@-doubled)
 * in GEDCOM 5.5.5 mode, while other @ signs in the remainder are still doubled.
 */
class CalendarEscapeTest {

    private String write555(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom555())) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private String writeGedcom7(java.util.function.Consumer<GedcomWriter> action) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom7())) {
            writer.head(head -> head.source("Test"));
            action.accept(writer);
            writer.trailer();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    void julianCalendarEscapePreservedIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DJULIAN@ 15 JAN 1700"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE @#DJULIAN@ 15 JAN 1700\n"),
                "Julian calendar escape should be preserved, got: " + output);
    }

    @Test
    void gregorianCalendarEscapePreservedIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DGREGORIAN@ 1 JAN 1900"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE @#DGREGORIAN@ 1 JAN 1900\n"),
                "Gregorian calendar escape should be preserved, got: " + output);
    }

    @Test
    void hebrewCalendarEscapePreservedIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DHEBREW@ 15 SVN 5765"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE @#DHEBREW@ 15 SVN 5765\n"),
                "Hebrew calendar escape should be preserved, got: " + output);
    }

    @Test
    void frenchRepublicanCalendarEscapePreservedIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DFRENCH R@ 1 VEND 12"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE @#DFRENCH R@ 1 VEND 12\n"),
                "French Republican calendar escape should be preserved, got: " + output);
    }

    @Test
    void romanCalendarEscapePreservedIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DROMAN@ 15 JAN 1700"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE @#DROMAN@ 15 JAN 1700\n"),
                "Roman calendar escape should be preserved, got: " + output);
    }

    @Test
    void unknownCalendarEscapePreservedIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DUNKNOWN@ 15 JAN 1700"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE @#DUNKNOWN@ 15 JAN 1700\n"),
                "Unknown calendar escape should be preserved, got: " + output);
    }

    @Test
    void noCalendarEscapeDateUnchangedIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("15 JAN 1900"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE 15 JAN 1900\n"),
                "Date without calendar escape should be unchanged, got: " + output);
    }

    @Test
    void calendarEscapePreservedButOtherAtDoubledIn555() throws Exception {
        String output = write555(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DJULIAN@ @unusual@text"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(output.contains("2 DATE @#DJULIAN@ @@unusual@@text\n"),
                "Calendar prefix preserved but other @ should be doubled, got: " + output);
    }

    @Test
    void gedcom7ModeUnchangedNoEscaping() throws Exception {
        String output = writeGedcom7(writer -> {
            try {
                writer.individual(indi -> {
                    indi.birth(birt -> birt.date("@#DJULIAN@ 15 JAN 1700"));
                });
            } catch (GedcomWriteException e) {
                throw new RuntimeException(e);
            }
        });
        // GEDCOM 7 only doubles leading @, but the whole value starts with @
        // so the leading @ gets doubled: @@#DJULIAN@ 15 JAN 1700
        // With the calendar escape fix, GEDCOM 7 mode should be unchanged from before
        assertTrue(output.contains("2 DATE @@#DJULIAN@ 15 JAN 1700\n"),
                "GEDCOM 7 mode should apply normal leading-@ doubling, got: " + output);
    }
}
