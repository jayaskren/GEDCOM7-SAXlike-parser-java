package org.gedcom7.converter;

import org.gedcom7.parser.GedcomVersion;

/**
 * Immutable configuration for a GEDCOM version conversion operation.
 *
 * <p>Use the factory methods for common configurations or the {@link Builder}
 * for custom settings.
 */
public final class GedcomConverterConfig {

    private final GedcomVersion targetVersion;
    private final boolean strict;
    private final ConversionWarningHandler warningHandler;
    private final String lineEnding;

    private GedcomConverterConfig(Builder builder) {
        this.targetVersion = builder.targetVersion;
        this.strict = builder.strict;
        this.warningHandler = builder.warningHandler;
        this.lineEnding = builder.lineEnding;
    }

    /** Creates a lenient configuration targeting GEDCOM 7.0. */
    public static GedcomConverterConfig toGedcom7() {
        return builder().targetVersion(new GedcomVersion(7, 0)).build();
    }

    /** Creates a lenient configuration targeting GEDCOM 5.5.5. */
    public static GedcomConverterConfig toGedcom555() {
        return builder().targetVersion(new GedcomVersion(5, 5, 5)).build();
    }

    /** Creates a strict configuration targeting GEDCOM 7.0. */
    public static GedcomConverterConfig toGedcom7Strict() {
        return builder().targetVersion(new GedcomVersion(7, 0)).strict(true).build();
    }

    /** Creates a strict configuration targeting GEDCOM 5.5.5. */
    public static GedcomConverterConfig toGedcom555Strict() {
        return builder().targetVersion(new GedcomVersion(5, 5, 5)).strict(true).build();
    }

    /** Creates a new builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the target GEDCOM version. */
    public GedcomVersion getTargetVersion() {
        return targetVersion;
    }

    /** Returns {@code true} if strict mode is enabled. */
    public boolean isStrict() {
        return strict;
    }

    /** Returns the warning handler, or {@code null} if none configured. */
    public ConversionWarningHandler getWarningHandler() {
        return warningHandler;
    }

    /** Returns the line ending string for output. */
    public String getLineEnding() {
        return lineEnding;
    }

    /**
     * Builder for {@link GedcomConverterConfig}.
     */
    public static final class Builder {
        private GedcomVersion targetVersion;
        private boolean strict;
        private ConversionWarningHandler warningHandler;
        private String lineEnding = "\n";

        private Builder() {}

        /** Sets the target GEDCOM version. Required. */
        public Builder targetVersion(GedcomVersion version) {
            this.targetVersion = version;
            return this;
        }

        /** Enables or disables strict mode. Default: {@code false}. */
        public Builder strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        /** Sets the warning handler for real-time warning notifications. */
        public Builder warningHandler(ConversionWarningHandler handler) {
            this.warningHandler = handler;
            return this;
        }

        /** Sets the line ending for output. Default: {@code "\n"}. */
        public Builder lineEnding(String ending) {
            this.lineEnding = ending;
            return this;
        }

        /** Builds the configuration. */
        public GedcomConverterConfig build() {
            if (targetVersion == null) {
                throw new IllegalStateException("targetVersion is required");
            }
            return new GedcomConverterConfig(this);
        }
    }
}
