# GEDCOM SAX-like Parser for Java

A streaming, event-driven parser, writer, and converter for [GEDCOM 7.0](https://gedcom.io/specifications/FamilySearchGEDCOMv7.html) and GEDCOM 5.5.5 genealogy files. Inspired by SAX (Simple API for XML), the parser reads a GEDCOM file line by line and fires callbacks to your handler -- no DOM tree, no memory pressure, no runtime dependencies.

## Features

- **Streaming** -- processes files of any size without loading them into memory
- **Event-driven** -- override only the callbacks you need
- **Zero dependencies** -- just the JDK (Java 11+)
- **GEDCOM 7 and 5.5.5** -- full support for both versions, with auto-detection
- **Parser** -- GEDCOM 7: UTF-8, CONT assembly, `@@` escape, SCHMA extension tags; GEDCOM 5.5.5: CONT + CONC assembly, all-`@@` escaping, BOM/CHAR detection
- **Writer** -- generates well-formed GEDCOM 7 or 5.5.5 output with version-appropriate formatting
- **Converter** -- converts files between GEDCOM 5.5.5 and 7.0 with automatic version-specific handling
- **Lenient by default** -- recovers from errors; strict mode available
- **Data type parsers** -- built-in helpers for dates, ages, names, coordinates, and more
- **JPMS module** -- `org.gedcom7.parser`

## Quick Start

Add the dependency (Gradle Kotlin DSL):

```kotlin
dependencies {
    implementation("org.gedcom7:gedcom7-parser:0.1.0-SNAPSHOT")
}
```

Parse a file:

```java
import org.gedcom7.parser.*;

try (var in = new java.io.FileInputStream("family.ged");
     var reader = new GedcomReader(in, new GedcomHandler() {

         @Override
         public void startRecord(int level, String xref, String tag) {
             System.out.println("Record: " + tag + (xref != null ? " @" + xref + "@" : ""));
         }

         @Override
         public void startStructure(int level, String xref, String tag,
                                    String value, boolean isPointer) {
             System.out.println("  ".repeat(level) + tag + (value != null ? " = " + value : ""));
         }

     }, GedcomReaderConfig.gedcom7())) {
    reader.parse();
}
```

## Configuration

```java
// GEDCOM 7: lenient mode, generous limits
GedcomReaderConfig config = GedcomReaderConfig.gedcom7();

// GEDCOM 7: strict mode -- stop on first error
GedcomReaderConfig strict = GedcomReaderConfig.gedcom7Strict();

// GEDCOM 5.5.5: lenient mode (CONT+CONC, all-@@ escaping)
GedcomReaderConfig config555 = GedcomReaderConfig.gedcom555();

// GEDCOM 5.5.5: strict mode
GedcomReaderConfig strict555 = GedcomReaderConfig.gedcom555Strict();

// Auto-detect: reads HEAD.GEDC.VERS and applies the right strategies
GedcomReaderConfig auto = GedcomReaderConfig.autoDetect();

// Custom: tune limits and enable structure validation
GedcomReaderConfig custom = new GedcomReaderConfig.Builder()
        .strict(false)
        .maxNestingDepth(500)
        .maxLineLength(65536)
        .structureValidation(true)
        .build();
```

| Option                  | Default     | Description                                   |
|-------------------------|-------------|-----------------------------------------------|
| `strict`                | `false`     | Stop on first error (throw `GedcomFatalException`) |
| `maxNestingDepth`       | `1000`      | Fatal error if a line's level exceeds this    |
| `maxLineLength`         | `1,048,576` | Fatal error if a single line exceeds this (chars) |
| `structureValidation`   | `false`     | Warn on unknown substructures and cardinality violations |

## Handler Callbacks

Extend `GedcomHandler` and override the methods you need. All methods have no-op defaults.

| Callback | When it fires |
|----------|---------------|
| `startDocument(GedcomHeaderInfo)` | Before any record events; carries pre-parsed HEAD metadata |
| `endDocument()` | After TRLR is processed |
| `startRecord(level, xref, tag)` | At each level-0 record (HEAD, INDI, FAM, ...) |
| `endRecord(tag)` | When a record's substructures are complete |
| `startStructure(level, xref, tag, value, isPointer)` | At each substructure (level > 0) |
| `startStructure(level, xref, tag, value, isPointer, uri)` | Same, but includes resolved SCHMA URI for extension tags |
| `endStructure(tag)` | When a structure's children are complete |
| `warning(GedcomParseError)` | Non-fatal issue (e.g., leading whitespace, unresolved xref) |
| `error(GedcomParseError)` | Recoverable error (lenient mode continues) |
| `fatalError(GedcomParseError)` | Unrecoverable error (parsing stops) |

## Data Type Parsers

The `GedcomDataTypes` utility class parses GEDCOM value strings into typed objects:

```java
import org.gedcom7.parser.datatype.*;

// Dates
Object date = GedcomDataTypes.parseDateValue("6 APR 1952");
// Returns GedcomDateRange with type "EXACT", start date year=1952, month="APR", day=6

// Ages
GedcomAge age = GedcomDataTypes.parseAge("30y 6m");
// age.getYears() == 30, age.getMonths() == 6

// Personal names
GedcomPersonalName name = GedcomDataTypes.parsePersonalName("John /Smith/ Jr.");
// name.getGivenName() == "John", name.getSurname() == "Smith", name.getNameSuffix() == "Jr."

// Coordinates
GedcomCoordinate lat = GedcomDataTypes.parseLatitude("N51.5074");
GedcomCoordinate lon = GedcomDataTypes.parseLongitude("W0.1278");
```

See [docs/tutorial.md](docs/tutorial.md) for a full walkthrough.

## Documentation

- [Tutorial](docs/tutorial.md) -- step-by-step guide for developers new to the parser
- [Architecture](docs/architecture.md) -- internal design, layers, and extension points

## Building

```bash
./gradlew build        # compile + test
./gradlew test         # run the 301-test suite
```

Requires Java 11+ to build and run. The project uses Gradle 8.12 with Kotlin DSL.

## License

See [LICENSE](LICENSE) for details.
