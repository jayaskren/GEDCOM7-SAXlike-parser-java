# API Contract: GEDCOM Version Converter

**Package**: `org.gedcom7.converter`

## Public API Surface

### GedcomConverter (static utility class)

```java
public final class GedcomConverter {

    /**
     * Converts a GEDCOM file from its detected version to the target version.
     *
     * @param input  the GEDCOM input stream (any supported version)
     * @param output the output stream for the converted GEDCOM
     * @param config conversion configuration (target version, error mode)
     * @return conversion result with record counts and any warnings/errors
     * @throws IOException if an I/O error occurs
     * @throws GedcomFatalException if a fatal parse error occurs in strict mode
     */
    public static ConversionResult convert(InputStream input,
                                           OutputStream output,
                                           GedcomConverterConfig config)
            throws IOException;
}
```

### GedcomConverterConfig (immutable, builder pattern)

```java
public final class GedcomConverterConfig {

    // Factory methods
    public static GedcomConverterConfig toGedcom7();
    public static GedcomConverterConfig toGedcom555();
    public static GedcomConverterConfig toGedcom7Strict();
    public static GedcomConverterConfig toGedcom555Strict();

    // Builder
    public static Builder builder();

    // Getters
    public GedcomVersion getTargetVersion();
    public boolean isStrict();
    public ConversionWarningHandler getWarningHandler();
    public String getLineEnding();

    public static final class Builder {
        public Builder targetVersion(GedcomVersion version);
        public Builder strict(boolean strict);
        public Builder warningHandler(ConversionWarningHandler handler);
        public Builder lineEnding(String ending);
        public GedcomConverterConfig build();
    }
}
```

### ConversionResult (immutable)

```java
public final class ConversionResult {

    public GedcomVersion getSourceVersion();
    public GedcomVersion getTargetVersion();
    public int getRecordCount();
    public int getWarningCount();
    public int getErrorCount();
    public List<ConversionWarning> getWarnings();
    public List<GedcomParseError> getParseErrors();
}
```

### ConversionWarning (immutable)

```java
public final class ConversionWarning {

    public ConversionWarning(String message, String tag, int lineNumber);

    public String getMessage();
    public String getTag();
    public int getLineNumber();
}
```

### ConversionWarningHandler (functional interface)

```java
@FunctionalInterface
public interface ConversionWarningHandler {
    void handle(ConversionWarning warning);
}
```

## Usage Examples

### Basic 5.5.5 to 7 conversion

```java
try (InputStream in = new FileInputStream("family.ged");
     OutputStream out = new FileOutputStream("family_v7.ged")) {
    ConversionResult result = GedcomConverter.convert(in, out,
            GedcomConverterConfig.toGedcom7());
    System.out.println("Converted " + result.getRecordCount() + " records");
    if (result.getWarningCount() > 0) {
        result.getWarnings().forEach(w -> System.err.println("Warning: " + w.getMessage()));
    }
}
```

### Strict mode with warning handler

```java
GedcomConverterConfig config = GedcomConverterConfig.builder()
        .targetVersion(GedcomVersion.parse("5.5.5"))
        .strict(true)
        .warningHandler(w -> log.warn("Conversion: {}", w.getMessage()))
        .build();

ConversionResult result = GedcomConverter.convert(input, output, config);
```

### Same-version normalization

```java
// Re-format a GEDCOM 7 file (normalize formatting)
ConversionResult result = GedcomConverter.convert(input, output,
        GedcomConverterConfig.toGedcom7());
```

## Behavioral Contract

### HEAD Conversion Rules

| Source | Target | GEDC.VERS | HEAD.CHAR | GEDC.FORM |
|--------|--------|-----------|-----------|-----------|
| 5.5.5 | 7.0 | "7.0" | Removed | Removed |
| 7.0 | 5.5.5 | "5.5.5" | "UTF-8" added | "LINEAGE-LINKED" added if absent |
| 5.5.5 | 5.5.5 | "5.5.5" (unchanged) | "UTF-8" (unchanged) | Preserved |
| 7.0 | 7.0 | "7.0" (unchanged) | Not present | Not present |

### @-Escaping Rules

| Target Version | Escaping Behavior |
|---------------|-------------------|
| GEDCOM 7 | Only leading @ in values is escaped (@@); internal @ are literal |
| GEDCOM 5.5.5 | All @ in values are doubled (@@) |

### Line Length Rules

| Target Version | Behavior |
|---------------|----------|
| GEDCOM 7 | No line length limit; no CONC splitting |
| GEDCOM 5.5.5 | Lines limited to 255 characters; long values split with CONC |

### Xref Handling

- All xref IDs from input are preserved exactly in output
- Developer-provided IDs passed through `writer.record(id, tag, ...)` or via direct line emission
- Pointer references (`@ID@`) are preserved as-is

### Error Handling

| Mode | Parse Error | Conversion Warning | Fatal Error |
|------|------------|-------------------|-------------|
| Lenient | Recorded in result, conversion continues | Recorded in result, conversion continues | Throws GedcomFatalException |
| Strict | Throws GedcomFatalException | Throws GedcomFatalException | Throws GedcomFatalException |

### Thread Safety

- `GedcomConverter.convert()` is thread-safe (creates fresh internal state per call)
- `GedcomConverterConfig` is immutable and thread-safe
- `ConversionResult` is immutable and thread-safe
