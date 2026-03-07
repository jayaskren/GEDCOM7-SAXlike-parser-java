package org.gedcom7.converter;

import org.gedcom7.parser.GedcomFatalException;
import org.gedcom7.parser.GedcomHandler;
import org.gedcom7.parser.GedcomHeaderInfo;
import org.gedcom7.parser.GedcomParseError;
import org.gedcom7.parser.GedcomReader;
import org.gedcom7.parser.GedcomReaderConfig;
import org.gedcom7.parser.GedcomVersion;
import org.gedcom7.writer.GedcomWriterConfig;
import org.gedcom7.writer.internal.LineEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Converts GEDCOM files between versions 5.5.5 and 7.0.
 *
 * <p>The converter reads a GEDCOM file using the SAX-like parser with
 * auto-detection and writes it in the target version using a {@link LineEmitter}
 * configured for the target format. All version-specific formatting (CONT/CONC
 * splitting, @-escaping, HEAD.CHAR, line length limits) is handled automatically.
 *
 * <p>Usage:
 * <pre>{@code
 * ConversionResult result = GedcomConverter.convert(
 *     inputStream, outputStream,
 *     GedcomConverterConfig.toGedcom7());
 * }</pre>
 */
public final class GedcomConverter {

    private GedcomConverter() {} // static utility class

    /**
     * Converts a GEDCOM file from its detected version to the target version.
     *
     * @param input  the GEDCOM input stream (any supported version)
     * @param output the output stream for the converted GEDCOM
     * @param config conversion configuration (target version, error mode)
     * @return conversion result with record counts and any warnings/errors
     * @throws IOException          if an I/O error occurs
     * @throws GedcomFatalException if a fatal parse error occurs
     */
    public static ConversionResult convert(InputStream input,
                                           OutputStream output,
                                           GedcomConverterConfig config)
            throws IOException {

        GedcomWriterConfig writerConfig = buildWriterConfig(config);
        LineEmitter emitter = new LineEmitter(output, writerConfig);

        ConversionResult.Builder resultBuilder = new ConversionResult.Builder()
                .targetVersion(config.getTargetVersion());

        ConvertingHandler handler = new ConvertingHandler(
                emitter, config, resultBuilder);

        GedcomReaderConfig readerConfig = config.isStrict()
                ? GedcomReaderConfig.autoDetectStrict()
                : GedcomReaderConfig.autoDetect();

        try (GedcomReader reader = new GedcomReader(input, handler, readerConfig)) {
            reader.parse();
        }

        emitter.flush();
        return resultBuilder.build();
    }

    private static GedcomWriterConfig buildWriterConfig(GedcomConverterConfig config) {
        GedcomVersion target = config.getTargetVersion();
        GedcomWriterConfig.Builder builder = new GedcomWriterConfig.Builder()
                .version(target)
                .lineEnding(config.getLineEnding());

        if (target.isGedcom5()) {
            builder.escapeAllAt(true)
                   .concEnabled(true)
                   .maxLineLength(255);
        }

        return builder.build();
    }

    /**
     * Internal GedcomHandler that bridges parser events to LineEmitter output.
     */
    private static final class ConvertingHandler extends GedcomHandler {

        private static final Set<String> SKIP_HEAD_TAGS = Set.of("GEDC", "CHAR");

        private final LineEmitter emitter;
        private final GedcomConverterConfig config;
        private final ConversionResult.Builder resultBuilder;

        private boolean inHead;
        private int skipDepth; // >0 means we're inside a subtree to skip

        ConvertingHandler(LineEmitter emitter,
                          GedcomConverterConfig config,
                          ConversionResult.Builder resultBuilder) {
            this.emitter = emitter;
            this.config = config;
            this.resultBuilder = resultBuilder;
        }

        @Override
        public void startDocument(GedcomHeaderInfo header) {
            GedcomVersion sourceVersion = header.getVersion();
            resultBuilder.sourceVersion(sourceVersion);

            // Validate source version
            if (!sourceVersion.isGedcom7() && !sourceVersion.isGedcom5()) {
                ConversionWarning warning = new ConversionWarning(
                        "Unrecognized GEDCOM version: " + sourceVersion
                                + "; expected 5.5.x or 7.x",
                        "GEDC", 0);
                resultBuilder.addWarning(warning);
                if (config.getWarningHandler() != null) {
                    config.getWarningHandler().handle(warning);
                }
                if (config.isStrict()) {
                    throw new GedcomFatalException(new GedcomParseError(
                            GedcomParseError.Severity.FATAL, 0, 0,
                            "Unrecognized GEDCOM version: " + sourceVersion, null));
                }
            }

            // Write HEAD record
            try {
                emitter.emitLine(0, null, "HEAD", null);

                // Write GEDC block for target version
                emitter.emitLine(1, null, "GEDC", null);
                emitter.emitLine(2, null, "VERS", config.getTargetVersion().toString());
                if (config.getTargetVersion().isGedcom5()) {
                    emitter.emitLine(2, null, "FORM", "LINEAGE-LINKED");
                }

                // Write CHAR for 5.5.5
                if (config.getTargetVersion().isGedcom5()) {
                    emitter.emitLine(1, null, "CHAR", "UTF-8");
                }

                // Write SCHMA if present in source
                Map<String, String> schemaMap = header.getSchemaMap();
                if (schemaMap != null && !schemaMap.isEmpty()) {
                    if (config.getTargetVersion().isGedcom5()) {
                        ConversionWarning schmaWarning = new ConversionWarning(
                                "HEAD.SCHMA is a GEDCOM 7 structure with no"
                                        + " direct equivalent in 5.5.5; preserved"
                                        + " as-is for extension tag interoperability",
                                "SCHMA", 0);
                        resultBuilder.addWarning(schmaWarning);
                        if (config.getWarningHandler() != null) {
                            config.getWarningHandler().handle(schmaWarning);
                        }
                    }
                    emitter.emitLine(1, null, "SCHMA", null);
                    for (Map.Entry<String, String> entry : schemaMap.entrySet()) {
                        emitter.emitLine(2, null, "TAG",
                                entry.getKey() + " " + entry.getValue());
                    }
                }
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }

            inHead = true;
        }

        @Override
        public void startRecord(int level, String xref, String tag) {
            startRecord(level, xref, tag, null);
        }

        @Override
        public void startRecord(int level, String xref, String tag, String value) {
            if ("HEAD".equals(tag)) {
                // HEAD is handled in startDocument; skip parser's HEAD replay
                inHead = true;
                return;
            }

            if ("TRLR".equals(tag)) {
                // TRLR handled in endDocument
                return;
            }

            inHead = false;
            resultBuilder.incrementRecordCount();

            try {
                if (value != null && value.contains("\n")) {
                    emitter.emitValueWithCont(level, xref, tag, value);
                } else {
                    emitter.emitLine(level, xref, tag, value);
                }
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }

        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            if (inHead) {
                handleHeadStructure(level, xref, tag, value, isPointer);
                return;
            }

            emitStructure(level, xref, tag, value, isPointer);
        }

        @Override
        public void startStructure(int level, String xref, String tag,
                                   String value, boolean isPointer, String uri) {
            // URI is informational — we emit the tag, not the URI
            startStructure(level, xref, tag, value, isPointer);
        }

        @Override
        public void endRecord(String tag) {
            if ("HEAD".equals(tag)) {
                inHead = false;
            }
        }

        @Override
        public void endStructure(String tag) {
            if (skipDepth > 0) {
                skipDepth--;
            }
        }

        @Override
        public void endDocument() {
            try {
                emitter.emitLine(0, null, "TRLR", null);
                emitter.flush();
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }

        @Override
        public void warning(GedcomParseError error) {
            resultBuilder.addParseError(error);
        }

        @Override
        public void error(GedcomParseError error) {
            resultBuilder.addParseError(error);
        }

        @Override
        public void fatalError(GedcomParseError error) {
            resultBuilder.addParseError(error);
        }

        // --- HEAD handling ---

        private void handleHeadStructure(int level, String xref, String tag,
                                         String value, boolean isPointer) {
            // If we're already inside a skipped subtree, keep skipping
            if (skipDepth > 0) {
                skipDepth++;
                return;
            }

            // Skip GEDC (and all children) and CHAR — converter writes its own
            if (level == 1 && SKIP_HEAD_TAGS.contains(tag)) {
                skipDepth = 1;
                return;
            }

            // Skip SCHMA at level 1 — converter writes it from header metadata
            if (level == 1 && "SCHMA".equals(tag)) {
                skipDepth = 1;
                return;
            }

            // Pass through all other HEAD substructures
            emitStructure(level, xref, tag, value, isPointer);
        }

        private void emitStructure(int level, String xref, String tag,
                                   String value, boolean isPointer) {
            try {
                if (isPointer && value != null) {
                    // Pointer values must not be @-escaped
                    emitter.emitPointerLine(level, tag, value);
                } else if (value != null && value.contains("\n")) {
                    emitter.emitValueWithCont(level, xref, tag, value);
                } else {
                    emitter.emitLine(level, xref, tag, value);
                }
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }
    }
}
