package org.gedcom7.parser;

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
    final Object inputDecoder;
    final Object payloadAssembler;
    final Object atEscapeStrategy;

    private GedcomReaderConfig(Builder builder) {
        this.strict = builder.strict;
        this.maxNestingDepth = builder.maxNestingDepth;
        this.maxLineLength = builder.maxLineLength;
        this.structureValidation = builder.structureValidation;
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

    public boolean isStrict() { return strict; }
    public int getMaxNestingDepth() { return maxNestingDepth; }
    public int getMaxLineLength() { return maxLineLength; }
    public boolean isStructureValidationEnabled() { return structureValidation; }
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
                .inputDecoder(inputDecoder)
                .payloadAssembler(payloadAssembler)
                .atEscapeStrategy(atEscapeStrategy);
    }

    public static final class Builder {
        private boolean strict = false;
        private int maxNestingDepth = DEFAULT_MAX_NESTING_DEPTH;
        private int maxLineLength = DEFAULT_MAX_LINE_LENGTH;
        private boolean structureValidation = false;
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
