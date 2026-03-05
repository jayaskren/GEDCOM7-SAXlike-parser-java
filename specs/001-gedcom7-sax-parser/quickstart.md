# Quickstart: GEDCOM 7 SAX-like Parser

## Minimal Example

```java
import org.gedcom7.parser.*;

public class PrintRecords {
    public static void main(String[] args) throws Exception {
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startDocument(GedcomHeaderInfo header) {
                System.out.println("GEDCOM version: "
                    + header.getVersion());
            }

            @Override
            public void startRecord(int level,
                                    String xref,
                                    String tag) {
                System.out.println("Record: " + tag
                    + (xref != null ? " " + xref : ""));
            }

            @Override
            public void startStructure(int level,
                                       String xref,
                                       String tag,
                                       String value,
                                       boolean isPointer) {
                System.out.println("  ".repeat(level)
                    + tag
                    + (value != null ? " = " + value : ""));
            }

            @Override
            public void error(GedcomParseError error) {
                System.err.println("Error at line "
                    + error.getLineNumber() + ": "
                    + error.getMessage());
            }
        };

        try (var in = new java.io.FileInputStream(args[0]);
             var reader = new GedcomReader(
                 in, handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
    }
}
```

## Configuration

```java
// Strict mode (all errors fatal)
GedcomReaderConfig strict = GedcomReaderConfig.gedcom7Strict();

// Custom config via builder
GedcomReaderConfig config = GedcomReaderConfig.gedcom7()
    .toBuilder()
    .strict(true)
    .maxNestingDepth(500)
    .structureValidation(true)
    .build();
```

## Extracting Individuals

```java
GedcomHandler handler = new GedcomHandler() {
    private String currentXref;
    private String currentTag;
    private int depth = 0;

    @Override
    public void startRecord(int level, String xref,
                            String tag) {
        currentXref = xref;
        currentTag = tag;
        depth = 0;
    }

    @Override
    public void startStructure(int level, String xref,
                               String tag, String value,
                               boolean isPointer) {
        if ("INDI".equals(currentTag)
                && "NAME".equals(tag) && level == 1) {
            System.out.println(currentXref + ": " + value);
        }
    }
};
```

## Using Data Type Parsers

```java
import org.gedcom7.parser.datatype.*;

GedcomDate date = GedcomDataTypes.parseDateValue(
    "6 APR 1952");
// date.getYear() == 1952
// date.getMonth() == "APR"
// date.getDay() == 6

GedcomAge age = GedcomDataTypes.parseAge("> 25y 3m");
// age.getBound() == '>'
// age.getYears() == 25
// age.getMonths() == 3

GedcomPersonalName name = GedcomDataTypes.parsePersonalName(
    "John /Smith/ Jr.");
// name.getSurname() == "Smith"
```

## Using Extension Tag URIs

```java
GedcomHandler handler = new GedcomHandler() {
    private Map<String, String> schemaMap;

    @Override
    public void startDocument(GedcomHeaderInfo header) {
        schemaMap = header.getSchemaMap();
    }

    @Override
    public void startStructure(int level, String xref,
                               String tag, String value,
                               boolean isPointer) {
        if (tag.startsWith("_")) {
            String uri = schemaMap.get(tag);
            if (uri != null) {
                System.out.println("Extension " + tag
                    + " -> " + uri + " = " + value);
            }
        }
    }
};
```

## Build (Gradle)

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.gedcom7:gedcom7-parser:0.1.0")
}
```

## Requirements

- Java 11 or later
- Zero runtime dependencies
