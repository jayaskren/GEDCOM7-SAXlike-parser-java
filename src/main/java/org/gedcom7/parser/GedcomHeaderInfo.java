package org.gedcom7.parser;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Pre-parsed HEAD metadata delivered via
 * {@link GedcomHandler#startDocument(GedcomHeaderInfo)}.
 *
 * <p>Instances are immutable.
 */
public final class GedcomHeaderInfo {

    private final GedcomVersion version;
    private final String sourceSystem;
    private final String sourceVersion;
    private final String sourceName;
    private final String defaultLanguage;
    private final Map<String, String> schemaMap;

    public GedcomHeaderInfo(GedcomVersion version,
                            String sourceSystem,
                            String sourceVersion,
                            String sourceName,
                            String defaultLanguage,
                            Map<String, String> schemaMap) {
        this.version = Objects.requireNonNull(version, "version");
        this.sourceSystem = sourceSystem;
        this.sourceVersion = sourceVersion;
        this.sourceName = sourceName;
        this.defaultLanguage = defaultLanguage;
        this.schemaMap = schemaMap != null
                ? Collections.unmodifiableMap(schemaMap)
                : Collections.emptyMap();
    }

    public GedcomVersion getVersion() { return version; }
    public String getSourceSystem() { return sourceSystem; }
    public String getSourceVersion() { return sourceVersion; }
    public String getSourceName() { return sourceName; }
    public String getDefaultLanguage() { return defaultLanguage; }

    /**
     * Unmodifiable map of extension tag to URI from
     * HEAD.SCHMA.TAG entries.
     */
    public Map<String, String> getSchemaMap() { return schemaMap; }

    @Override
    public String toString() {
        return "GedcomHeaderInfo{version=" + version
                + ", sourceSystem=" + sourceSystem
                + ", schemaMap=" + schemaMap + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GedcomHeaderInfo)) return false;
        GedcomHeaderInfo that = (GedcomHeaderInfo) o;
        return version.equals(that.version)
                && Objects.equals(sourceSystem, that.sourceSystem)
                && Objects.equals(sourceVersion, that.sourceVersion)
                && Objects.equals(sourceName, that.sourceName)
                && Objects.equals(defaultLanguage, that.defaultLanguage)
                && schemaMap.equals(that.schemaMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, sourceSystem, sourceVersion,
                sourceName, defaultLanguage, schemaMap);
    }
}
