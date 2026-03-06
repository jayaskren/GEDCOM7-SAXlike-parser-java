package org.gedcom7.parser;

import org.gedcom7.parser.internal.GedcomLine;
import org.gedcom7.parser.internal.GedcomLineTokenizer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and correctness tests for the tokenizer with large inputs.
 */
class TokenizerPerformanceTest {

    private static final int LINE_COUNT = 100_000;

    /**
     * Generates a realistic GEDCOM file with the specified number of lines.
     */
    private static String generateLargeGedcom(int lineCount) {
        StringBuilder sb = new StringBuilder(lineCount * 30);
        sb.append("0 HEAD\n");
        sb.append("1 GEDC\n");
        sb.append("2 VERS 7.0\n");
        int remaining = lineCount - 5; // HEAD, GEDC, VERS, one INDI record, TRLR
        int indiCount = 0;
        int linesEmitted = 3;
        while (linesEmitted < lineCount - 1) {
            indiCount++;
            sb.append("0 @I").append(indiCount).append("@ INDI\n");
            linesEmitted++;
            if (linesEmitted < lineCount - 1) {
                sb.append("1 NAME John /Doe").append(indiCount).append("/\n");
                linesEmitted++;
            }
            if (linesEmitted < lineCount - 1) {
                sb.append("1 BIRT\n");
                linesEmitted++;
            }
            if (linesEmitted < lineCount - 1) {
                sb.append("2 DATE 1 JAN 2000\n");
                linesEmitted++;
            }
            if (linesEmitted < lineCount - 1) {
                sb.append("1 SEX M\n");
                linesEmitted++;
            }
        }
        sb.append("0 TRLR\n");
        return sb.toString();
    }

    /**
     * Correctness test: parse a 100,000-line GEDCOM file and verify
     * the parse results match expected values.
     */
    @Test
    void largeFileCorrectnessTest() throws IOException {
        String gedcom = generateLargeGedcom(LINE_COUNT);
        GedcomLineTokenizer tokenizer = new GedcomLineTokenizer(new StringReader(gedcom));
        GedcomLine line = new GedcomLine();

        List<String> tags = new ArrayList<>();
        int count = 0;
        while (tokenizer.nextLine(line)) {
            count++;
            if (count <= 3) {
                tags.add(line.getTag());
            }
        }

        // Verify first 3 tags
        assertEquals("HEAD", tags.get(0));
        assertEquals("GEDC", tags.get(1));
        assertEquals("VERS", tags.get(2));

        // Verify total line count
        assertEquals(LINE_COUNT, count);
    }

    /**
     * Performance test: measure tokenizer throughput on a large file.
     * Tagged as "performance" so it can be excluded from CI if needed.
     */
    @Test
    @Tag("performance")
    void largeFilePerformanceTest() throws IOException {
        String gedcom = generateLargeGedcom(LINE_COUNT);

        // Warm-up runs
        for (int i = 0; i < 3; i++) {
            parseAll(gedcom);
        }

        // Timed runs
        long totalNanos = 0;
        int runs = 5;
        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            int count = parseAll(gedcom);
            long elapsed = System.nanoTime() - start;
            totalNanos += elapsed;
            assertEquals(LINE_COUNT, count);
        }

        double avgMs = (totalNanos / (double) runs) / 1_000_000.0;
        System.out.printf("Tokenizer performance: %,d lines in %.1f ms avg (%.0f lines/ms)%n",
                LINE_COUNT, avgMs, LINE_COUNT / avgMs);

        // Soft assertion: should parse 100K lines in under 500ms on modern hardware
        // This is a very generous threshold and should not fail on CI
        assertTrue(avgMs < 5000,
                "Tokenizer took too long: " + avgMs + " ms for " + LINE_COUNT + " lines");
    }

    /**
     * Correctness test via GedcomReader (full integration).
     */
    @Test
    void largeFileThroughGedcomReader() {
        String gedcom = generateLargeGedcom(LINE_COUNT);
        byte[] bytes = gedcom.getBytes(StandardCharsets.UTF_8);

        int[] recordCount = {0};
        int[] structureCount = {0};
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                recordCount[0]++;
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                structureCount[0]++;
            }
        };

        try (GedcomReader reader = new GedcomReader(
                new ByteArrayInputStream(bytes), handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // Should have parsed some records and structures
        assertTrue(recordCount[0] > 0, "Expected at least some records");
        assertTrue(structureCount[0] > 0, "Expected at least some structures");
    }

    private static int parseAll(String gedcom) throws IOException {
        GedcomLineTokenizer tokenizer = new GedcomLineTokenizer(new StringReader(gedcom));
        GedcomLine line = new GedcomLine();
        int count = 0;
        while (tokenizer.nextLine(line)) {
            count++;
        }
        return count;
    }
}
