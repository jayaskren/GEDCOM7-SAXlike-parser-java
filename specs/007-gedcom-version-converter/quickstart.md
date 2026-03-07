# Quickstart: GEDCOM Version Converter

**Feature**: 007-gedcom-version-converter

## Quick Usage

### Convert GEDCOM 5.5.5 to GEDCOM 7

```java
import org.gedcom7.converter.GedcomConverter;
import org.gedcom7.converter.GedcomConverterConfig;
import org.gedcom7.converter.ConversionResult;

try (InputStream in = new FileInputStream("legacy.ged");
     OutputStream out = new FileOutputStream("modern.ged")) {
    ConversionResult result = GedcomConverter.convert(in, out,
            GedcomConverterConfig.toGedcom7());
    System.out.println("Converted " + result.getRecordCount() + " records");
}
```

### Convert GEDCOM 7 to GEDCOM 5.5.5

```java
try (InputStream in = new FileInputStream("modern.ged");
     OutputStream out = new FileOutputStream("legacy.ged")) {
    ConversionResult result = GedcomConverter.convert(in, out,
            GedcomConverterConfig.toGedcom555());
    System.out.println("Converted " + result.getRecordCount() + " records");
}
```

### Strict mode (stop on first error)

```java
ConversionResult result = GedcomConverter.convert(input, output,
        GedcomConverterConfig.toGedcom7Strict());
```

### Custom configuration

```java
GedcomConverterConfig config = GedcomConverterConfig.builder()
        .targetVersion(GedcomVersion.parse("5.5.5"))
        .strict(false)
        .warningHandler(w -> System.err.println("Warning: " + w.getMessage()))
        .lineEnding("\r\n")
        .build();

ConversionResult result = GedcomConverter.convert(input, output, config);
```

### Check conversion results

```java
ConversionResult result = GedcomConverter.convert(in, out,
        GedcomConverterConfig.toGedcom7());

System.out.println("Source version: " + result.getSourceVersion());
System.out.println("Target version: " + result.getTargetVersion());
System.out.println("Records: " + result.getRecordCount());
System.out.println("Warnings: " + result.getWarningCount());
System.out.println("Errors: " + result.getErrorCount());

for (ConversionWarning w : result.getWarnings()) {
    System.err.printf("Line %d [%s]: %s%n",
            w.getLineNumber(), w.getTag(), w.getMessage());
}
```

## Test Scenarios

### Scenario 1: Basic 5.5.5 → 7 (US1)
- Input: GEDCOM 5.5.5 with HEAD, INDI, FAM, SOUR, TRLR
- Expected: GEDCOM 7 output with VERS 7.0, no CHAR, no CONC, leading-@ escaping

### Scenario 2: Basic 7 → 5.5.5 (US2)
- Input: GEDCOM 7 with HEAD, INDI, FAM, SOUR, TRLR
- Expected: GEDCOM 5.5.5 output with VERS 5.5.5, CHAR UTF-8, CONC for long lines, @@ escaping

### Scenario 3: Round-trip fidelity (SC-003)
- Input: GEDCOM 5.5.5 file
- Convert: 5.5.5 → 7 → 5.5.5
- Expected: Output equivalent to input (excluding formatting normalization)

### Scenario 4: Large file streaming (US3/SC-004)
- Input: Generated GEDCOM with 10,000+ records
- Expected: Conversion completes with bounded memory

### Scenario 5: Error reporting (US4)
- Input: Malformed GEDCOM file
- Expected: Errors reported in ConversionResult, lenient mode continues

### Scenario 6: Xref preservation (US5/SC-005)
- Input: GEDCOM with custom xref IDs (@PERSON1@, @FAM_SMITH@)
- Expected: All xrefs preserved exactly in output
