package org.gedcom7.converter;

import org.gedcom7.parser.GedcomParseError;
import org.gedcom7.parser.GedcomVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable summary of a completed GEDCOM version conversion.
 *
 * <p>Contains record counts, detected source version, target version,
 * and any warnings or errors encountered during conversion.
 */
public final class ConversionResult {

    private final GedcomVersion sourceVersion;
    private final GedcomVersion targetVersion;
    private final int recordCount;
    private final List<ConversionWarning> warnings;
    private final List<GedcomParseError> parseErrors;

    private ConversionResult(Builder builder) {
        this.sourceVersion = builder.sourceVersion;
        this.targetVersion = builder.targetVersion;
        this.recordCount = builder.recordCount;
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.parseErrors = Collections.unmodifiableList(new ArrayList<>(builder.parseErrors));
    }

    /** Returns the detected source GEDCOM version. */
    public GedcomVersion getSourceVersion() {
        return sourceVersion;
    }

    /** Returns the target GEDCOM version. */
    public GedcomVersion getTargetVersion() {
        return targetVersion;
    }

    /** Returns the total number of records converted (excluding HEAD and TRLR). */
    public int getRecordCount() {
        return recordCount;
    }

    /** Returns the number of conversion warnings. */
    public int getWarningCount() {
        return warnings.size();
    }

    /** Returns the number of parse errors encountered. */
    public int getErrorCount() {
        return parseErrors.size();
    }

    /** Returns an unmodifiable list of conversion warnings. */
    public List<ConversionWarning> getWarnings() {
        return warnings;
    }

    /** Returns an unmodifiable list of parse errors. */
    public List<GedcomParseError> getParseErrors() {
        return parseErrors;
    }

    /**
     * Mutable builder used internally during conversion to accumulate results.
     */
    static final class Builder {
        private GedcomVersion sourceVersion;
        private GedcomVersion targetVersion;
        private int recordCount;
        private final List<ConversionWarning> warnings = new ArrayList<>();
        private final List<GedcomParseError> parseErrors = new ArrayList<>();

        Builder targetVersion(GedcomVersion version) {
            this.targetVersion = version;
            return this;
        }

        Builder sourceVersion(GedcomVersion version) {
            this.sourceVersion = version;
            return this;
        }

        void incrementRecordCount() {
            recordCount++;
        }

        void addWarning(ConversionWarning warning) {
            warnings.add(warning);
        }

        void addParseError(GedcomParseError error) {
            parseErrors.add(error);
        }

        ConversionResult build() {
            return new ConversionResult(this);
        }
    }
}
