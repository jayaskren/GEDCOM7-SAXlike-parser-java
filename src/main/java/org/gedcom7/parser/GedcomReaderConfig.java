package org.gedcom7.parser;

import org.gedcom7.parser.internal.AllAtEscapeStrategy;
import org.gedcom7.parser.internal.BomDetectingDecoder;
import org.gedcom7.parser.internal.ContConcAssembler;

/**
 * Immutable configuration for {@link GedcomReader}.
 * Use {@link #gedcom7()} or {@link #gedcom7Strict()} factory
 * methods, or create a custom configuration via {@link Builder}.
 */
public final class GedcomReaderConfig {

    private static final int DEFAULT_MAX_NESTING_DEPTH = 1000;
    private static final int DEFAULT_MAX_LINE_LENGTH = 1_048_576;

    private final boolean strict;
    private final int maxNestingDepth;
    private final int maxLineLength;
    private final boolean structureValidation;
    private final boolean autoDetect;
    final Object inputDecoder;
    final Object payloadAssembler;
    final Object atEscapeStrategy;

    private GedcomReaderConfig(Builder builder) {
        this.strict = builder.strict;
        this.maxNestingDepth = builder.maxNestingDepth;
        this.maxLineLength = builder.maxLineLength;
        this.structureValidation = builder.structureValidation;
        this.autoDetect = builder.autoDetect;
        this.inputDecoder = builder.inputDecoder;
        this.payloadAssembler = builder.payloadAssembler;
        this.atEscapeStrategy = builder.atEscapeStrategy;
    }

    /** Default GEDCOM 7 configuration (lenient mode). */
    public static GedcomReaderConfig gedcom7() {
        return new Builder().build();
    }

    /** GEDCOM 7 strict mode (stops on first error). */
    public static GedcomReaderConfig gedcom7Strict() {
        return new Builder().strict(true).build();
    }

    /**
     * GEDCOM 5.5.5 configuration (lenient mode).
     * Uses BOM-detecting decoder, CONT+CONC assembler,
     * and all-@@ escape strategy.
     */
    public static GedcomReaderConfig gedcom555() {
        return new Builder()
                .inputDecoder(new BomDetectingDecoder())
                .payloadAssembler(new ContConcAssembler())
                .atEscapeStrategy(new AllAtEscapeStrategy())
                .build();
    }

    /**
     * GEDCOM 5.5.5 strict configuration.
     * Same strategies as {@link #gedcom555()}, plus strict mode
     * and maxLineLength of 255.
     */
    public static GedcomReaderConfig gedcom555Strict() {
        return new Builder()
                .strict(true)
                .maxLineLength(255)
                .inputDecoder(new BomDetectingDecoder())
                .payloadAssembler(new ContConcAssembler())
                .atEscapeStrategy(new AllAtEscapeStrategy())
                .build();
    }

    /**
     * Auto-detecting configuration (lenient mode).
     * Uses BOM-detecting decoder. After scanning HEAD,
     * automatically selects GEDCOM 7 or 5.5.5 strategies
     * based on HEAD.GEDC.VERS.
     */
    public static GedcomReaderConfig autoDetect() {
        return new Builder()
                .autoDetect(true)
                .inputDecoder(new BomDetectingDecoder())
                .build();
    }

    /**
     * Auto-detecting strict configuration.
     * Same as {@link #autoDetect()} with strict mode enabled.
     */
    public static GedcomReaderConfig autoDetectStrict() {
        return new Builder()
                .strict(true)
                .autoDetect(true)
                .inputDecoder(new BomDetectingDecoder())
                .build();
    }

    public boolean isStrict() { return strict; }
    public int getMaxNestingDepth() { return maxNestingDepth; }
    public int getMaxLineLength() { return maxLineLength; }
    public boolean isStructureValidationEnabled() { return structureValidation; }
    public boolean isAutoDetect() { return autoDetect; }
    Object getInputDecoderOrNull() { return inputDecoder; }
    Object getPayloadAssemblerOrNull() { return payloadAssembler; }
    Object getAtEscapeStrategyOrNull() { return atEscapeStrategy; }

    /** Returns a new builder pre-populated with this config's values. */
    public Builder toBuilder() {
        return new Builder()
                .strict(strict)
                .maxNestingDepth(maxNestingDepth)
                .maxLineLength(maxLineLength)
                .structureValidation(structureValidation)
                .autoDetect(autoDetect)
                .inputDecoder(inputDecoder)
                .payloadAssembler(payloadAssembler)
                .atEscapeStrategy(atEscapeStrategy);
    }

    public static final class Builder {
        private boolean strict = false;
        private int maxNestingDepth = DEFAULT_MAX_NESTING_DEPTH;
        private int maxLineLength = DEFAULT_MAX_LINE_LENGTH;
        private boolean structureValidation = false;
        private boolean autoDetect = false;
        Object inputDecoder;
        Object payloadAssembler;
        Object atEscapeStrategy;

        public Builder strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        public Builder maxNestingDepth(int depth) {
            if (depth < 1) throw new IllegalArgumentException("maxNestingDepth must be >= 1");
            this.maxNestingDepth = depth;
            return this;
        }

        public Builder maxLineLength(int length) {
            if (length < 1) throw new IllegalArgumentException("maxLineLength must be >= 1");
            this.maxLineLength = length;
            return this;
        }

        public Builder structureValidation(boolean enabled) {
            this.structureValidation = enabled;
            return this;
        }

        /**
         * Sets the auto-detect flag. When true, the parser reads
         * HEAD.GEDC.VERS and selects appropriate strategies automatically.
         */
        public Builder autoDetect(boolean autoDetect) {
            this.autoDetect = autoDetect;
            return this;
        }

        Builder inputDecoder(Object decoder) {
            this.inputDecoder = decoder;
            return this;
        }

        Builder payloadAssembler(Object assembler) {
            this.payloadAssembler = assembler;
            return this;
        }

        Builder atEscapeStrategy(Object strategy) {
            this.atEscapeStrategy = strategy;
            return this;
        }

        public GedcomReaderConfig build() {
            return new GedcomReaderConfig(this);
        }
    }
}
