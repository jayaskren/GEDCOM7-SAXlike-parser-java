package org.gedcom7.writer;

import org.gedcom7.parser.GedcomVersion;

import java.util.logging.Logger;

/**
 * Immutable configuration for {@link GedcomWriter}.
 *
 * <p>Use factory methods like {@link #gedcom7()} or create custom
 * configurations via {@link Builder}.
 */
public final class GedcomWriterConfig {

    private static final GedcomVersion VERSION_7 = new GedcomVersion(7, 0);
    private static final GedcomVersion VERSION_555 = new GedcomVersion(5, 5, 5);
    private static final int DEFAULT_MAX_LINE_LENGTH_555 = 255;

    private final GedcomVersion version;
    private final boolean strict;
    private final WarningHandler warningHandler;
    private final int maxLineLength;
    private final String lineEnding;
    private final boolean escapeAllAt;
    private final boolean concEnabled;

    private GedcomWriterConfig(Builder builder) {
        this.version = builder.version;
        this.strict = builder.strict;
        this.warningHandler = builder.warningHandler;
        this.maxLineLength = builder.maxLineLength;
        this.lineEnding = builder.lineEnding;
        this.escapeAllAt = builder.escapeAllAt;
        this.concEnabled = builder.concEnabled;
    }

    /**
     * Default GEDCOM 7 configuration (lenient mode).
     */
    public static GedcomWriterConfig gedcom7() {
        return new Builder().build();
    }

    /**
     * GEDCOM 7 strict mode configuration.
     */
    public static GedcomWriterConfig gedcom7Strict() {
        return new Builder().strict(true).build();
    }

    /**
     * GEDCOM 5.5.5 configuration (lenient mode).
     * Enables CONC splitting and all-@@ escaping.
     */
    public static GedcomWriterConfig gedcom555() {
        return new Builder()
                .version(VERSION_555)
                .escapeAllAt(true)
                .concEnabled(true)
                .maxLineLength(DEFAULT_MAX_LINE_LENGTH_555)
                .build();
    }

    /**
     * GEDCOM 5.5.5 strict mode configuration.
     */
    public static GedcomWriterConfig gedcom555Strict() {
        return new Builder()
                .version(VERSION_555)
                .strict(true)
                .escapeAllAt(true)
                .concEnabled(true)
                .maxLineLength(DEFAULT_MAX_LINE_LENGTH_555)
                .build();
    }

    public GedcomVersion getVersion() { return version; }
    public boolean isStrict() { return strict; }
    public WarningHandler getWarningHandler() { return warningHandler; }
    public int getMaxLineLength() { return maxLineLength; }
    public String getLineEnding() { return lineEnding; }
    public boolean isEscapeAllAt() { return escapeAllAt; }
    public boolean isConcEnabled() { return concEnabled; }

    /**
     * Returns a new builder pre-populated with this config's values.
     */
    public Builder toBuilder() {
        return new Builder()
                .version(version)
                .strict(strict)
                .warningHandler(warningHandler)
                .maxLineLength(maxLineLength)
                .lineEnding(lineEnding)
                .escapeAllAt(escapeAllAt)
                .concEnabled(concEnabled);
    }

    /**
     * Default warning handler that logs to {@code java.util.logging}.
     */
    static final WarningHandler LOGGING_HANDLER = warning -> {
        Logger.getLogger(GedcomWriterConfig.class.getName())
                .warning(warning.toString());
    };

    public static final class Builder {
        private GedcomVersion version = VERSION_7;
        private boolean strict = false;
        private WarningHandler warningHandler = LOGGING_HANDLER;
        private int maxLineLength = 0;
        private String lineEnding = "\n";
        private boolean escapeAllAt = false;
        private boolean concEnabled = false;

        public Builder version(GedcomVersion version) {
            this.version = version;
            return this;
        }

        public Builder strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        public Builder warningHandler(WarningHandler handler) {
            this.warningHandler = handler;
            return this;
        }

        public Builder maxLineLength(int maxLength) {
            this.maxLineLength = maxLength;
            return this;
        }

        public Builder lineEnding(String ending) {
            this.lineEnding = ending;
            return this;
        }

        Builder escapeAllAt(boolean escapeAllAt) {
            this.escapeAllAt = escapeAllAt;
            return this;
        }

        Builder concEnabled(boolean concEnabled) {
            this.concEnabled = concEnabled;
            return this;
        }

        public GedcomWriterConfig build() {
            return new GedcomWriterConfig(this);
        }
    }
}
