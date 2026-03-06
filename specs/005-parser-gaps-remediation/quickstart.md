# Quickstart: Parser Gaps Remediation

**Feature**: 005-parser-gaps-remediation

## Scenario 1: Parse with Full Validation (US1, US2, US9)

```java
// All standard event types recognized; min-cardinality enforced;
// unknown level-0 tags warned
GedcomReaderConfig config = GedcomReaderConfig.gedcom7Strict();

GedcomHandler handler = new GedcomHandler() {
    @Override
    public void warning(GedcomParseError error) {
        System.err.println("Warning: " + error.getMessage());
    }
};

try (GedcomReader reader = new GedcomReader(inputStream, handler, config)) {
    reader.parse();
}
// Expected: No false "Unknown structure" warnings for standard events
// Expected: Warning if ASSO lacks ROLE child
// Expected: Warning for unknown level-0 tags (not extension tags)
```

## Scenario 2: Type-Safe Date Parsing (US3)

```java
GedcomDateValue date = GedcomDataTypes.parseDateValue("BET 1 JAN 1900 AND 31 DEC 1900");

switch (date.getType()) {
    case EXACT:
        GedcomDate exact = (GedcomDate) date;
        break;
    case RANGE:
        GedcomDateRange range = (GedcomDateRange) date;
        break;
    case PERIOD:
        GedcomDatePeriod period = (GedcomDatePeriod) date;
        break;
    case APPROXIMATE:
        GedcomDateRange approx = (GedcomDateRange) date;
        break;
}
// No instanceof needed for type discrimination
```

## Scenario 3: Custom Input Decoder (US4)

```java
import org.gedcom7.parser.spi.GedcomInputDecoder;

// Custom ANSEL decoder for legacy files
GedcomInputDecoder anselDecoder = input -> {
    // Custom decoding logic
    return new InputStreamReader(input, anselCharset);
};

GedcomReaderConfig config = GedcomReaderConfig.gedcom7()
    .toBuilder()
    .inputDecoder(anselDecoder)
    .build();
```

## Scenario 4: Record with Payload (US5)

```java
GedcomHandler handler = new GedcomHandler() {
    @Override
    public void startRecord(int level, String xref, String tag, String value) {
        if ("SNOTE".equals(tag)) {
            System.out.println("Shared note: " + value);
        }
    }
};
// Input: 0 @N1@ SNOTE This is a note
// Output: "Shared note: This is a note"
```

## Scenario 5: SimpleGedcomHandler for Beginners (US10)

```java
GedcomHandler handler = new SimpleGedcomHandler() {
    @Override
    public void onStructure(int level, String xref, String tag, String value) {
        String indent = "  ".repeat(level);
        System.out.println(indent + tag + (value != null ? " " + value : ""));
    }
};

try (GedcomReader reader = new GedcomReader(inputStream, handler,
        GedcomReaderConfig.autoDetect())) {
    reader.parse();
}
// Both INDI (level 0) and NAME (level 1) come through onStructure
```

## Scenario 6: Byte Offset Error Reporting (US7)

```java
GedcomHandler handler = new GedcomHandler() {
    @Override
    public void warning(GedcomParseError error) {
        System.err.printf("Warning at byte %d, line %d: %s%n",
            error.getByteOffset(), error.getLineNumber(),
            error.getMessage());
    }
};
// Byte offsets are accurate for InputStream-based parsing
// Returns -1 when parsing from Reader
```

## Verification Checklist

- [ ] Scenario 1: Parse file with all event types → zero false warnings
- [ ] Scenario 1: Parse file with missing required child → warning emitted
- [ ] Scenario 2: parseDateValue returns GedcomDateValue, not Object
- [ ] Scenario 3: Custom decoder compiles from external package
- [ ] Scenario 4: SNOTE value accessible in startRecord
- [ ] Scenario 5: SimpleGedcomHandler receives both records and structures
- [ ] Scenario 6: Byte offsets match actual file positions
