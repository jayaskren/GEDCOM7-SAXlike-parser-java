# Tutorial: Parsing GEDCOM Files

This tutorial walks you through using the GEDCOM 7 SAX-like parser, from your first parse to building a real genealogy application. No prior GEDCOM knowledge is required.

## What is GEDCOM?

GEDCOM (Genealogical Data Communication) is a plain-text file format for exchanging family history data between genealogy software. A `.ged` file contains **records** (individuals, families, sources, etc.) organized in a hierarchical, line-based structure.

Here is a tiny GEDCOM 7 file:

```
0 HEAD
1 GEDC
2 VERS 7.0
0 @I1@ INDI
1 NAME John /Smith/
1 BIRT
2 DATE 6 APR 1952
2 PLAC Springfield, IL
0 @I2@ INDI
1 NAME Jane /Doe/
0 @F1@ FAM
1 HUSB @I1@
1 WIFE @I2@
0 TRLR
```

Each line has this structure: `level [xref] tag [value]`

- **Level** (0, 1, 2, ...) -- defines the hierarchy. Level 0 is a top-level record. Deeper levels are substructures.
- **Xref** (`@I1@`) -- optional cross-reference identifier for linking records.
- **Tag** (`INDI`, `NAME`, `BIRT`) -- tells you what kind of data this is.
- **Value** -- the actual data (`John /Smith/`, `6 APR 1952`).

## Step 1: Your First Parse

The parser works like SAX for XML: you provide a handler with callback methods, and the parser calls them as it reads through the file.

```java
import org.gedcom7.parser.*;
import java.io.FileInputStream;

public class FirstParse {
    public static void main(String[] args) throws Exception {
        GedcomHandler handler = new GedcomHandler() {
            @Override
            public void startRecord(int level, String xref, String tag) {
                System.out.println("Found record: " + tag
                    + (xref != null ? " with id @" + xref + "@" : ""));
            }
        };

        try (FileInputStream in = new FileInputStream("family.ged");
             GedcomReader reader = new GedcomReader(
                     in, handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }
    }
}
```

Output:

```
Found record: HEAD
Found record: INDI with id @I1@
Found record: INDI with id @I2@
Found record: FAM with id @F1@
Found record: TRLR
```

That is the entire API: create a handler, create a reader, call `parse()`.

## Step 2: Reading Substructures

Records contain substructures (level 1, 2, 3, ...). Override `startStructure` to see them:

```java
GedcomHandler handler = new GedcomHandler() {
    @Override
    public void startRecord(int level, String xref, String tag) {
        System.out.println(tag + (xref != null ? " @" + xref + "@" : ""));
    }

    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {
        String indent = "  ".repeat(level);
        String display = indent + tag;
        if (value != null) {
            display += " = " + value;
        }
        if (isPointer) {
            display += " (pointer)";
        }
        System.out.println(display);
    }
};
```

Output for the example file:

```
HEAD
  GEDC
    VERS = 7.0
INDI @I1@
  NAME = John /Smith/
  BIRT
    DATE = 6 APR 1952
    PLAC = Springfield, IL
INDI @I2@
  NAME = Jane /Doe/
FAM @F1@
  HUSB = @I1@ (pointer)
  WIFE = @I2@ (pointer)
TRLR
```

Notice that `HUSB` and `WIFE` have `isPointer = true` -- their values are cross-references to other records, not literal text.

## Step 3: Tracking Context with Start/End Events

The parser fires `endRecord` and `endStructure` when a record or structure's children are all processed. This lets you know when you have seen all the data for a given element.

```java
GedcomHandler handler = new GedcomHandler() {
    @Override
    public void startRecord(int level, String xref, String tag) {
        System.out.println(">> START " + tag);
    }

    @Override
    public void endRecord(String tag) {
        System.out.println("<< END " + tag);
    }

    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {
        System.out.println("  >> start " + tag);
    }

    @Override
    public void endStructure(String tag) {
        System.out.println("  << end " + tag);
    }
};
```

For the BIRT structure above, you would see:

```
  >> start BIRT
    >> start DATE
    << end DATE
    >> start PLAC
    << end PLAC
  << end BIRT
```

This is useful when you need to collect all children of a structure before processing it.

## Step 4: Extracting Individuals

Here is a practical example -- extracting all individuals and their names:

```java
import org.gedcom7.parser.*;
import java.io.FileInputStream;
import java.util.*;

public class ListPeople {
    public static void main(String[] args) throws Exception {
        Map<String, String> people = new LinkedHashMap<>();

        GedcomHandler handler = new GedcomHandler() {
            private String currentXref;
            private String currentRecordTag;

            @Override
            public void startRecord(int level, String xref, String tag) {
                currentXref = xref;
                currentRecordTag = tag;
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                // Capture the NAME of each INDI record
                if ("INDI".equals(currentRecordTag)
                        && "NAME".equals(tag) && level == 1) {
                    people.put(currentXref, value);
                }
            }
        };

        try (FileInputStream in = new FileInputStream("family.ged");
             GedcomReader reader = new GedcomReader(
                     in, handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        people.forEach((xref, name) ->
                System.out.println("@" + xref + "@ " + name));
    }
}
```

Output:

```
@I1@ John /Smith/
@I2@ Jane /Doe/
```

## Step 5: Using Header Information

The `startDocument` callback gives you pre-parsed metadata from the HEAD record before any other events fire:

```java
GedcomHandler handler = new GedcomHandler() {
    @Override
    public void startDocument(GedcomHeaderInfo header) {
        System.out.println("GEDCOM version: " + header.getVersion());
        System.out.println("Created by: " + header.getSourceName()
                + " v" + header.getSourceVersion());
        System.out.println("Language: " + header.getDefaultLanguage());

        // Extension tag URIs from SCHMA
        if (!header.getSchemaMap().isEmpty()) {
            System.out.println("Extension tags:");
            header.getSchemaMap().forEach((tag, uri) ->
                    System.out.println("  " + tag + " -> " + uri));
        }
    }
};
```

## Step 6: Handling Errors

Real-world GEDCOM files often have issues. The parser reports problems through three callbacks:

```java
GedcomHandler handler = new GedcomHandler() {
    @Override
    public void warning(GedcomParseError error) {
        System.err.println("WARN line " + error.getLineNumber()
                + ": " + error.getMessage());
    }

    @Override
    public void error(GedcomParseError error) {
        System.err.println("ERROR line " + error.getLineNumber()
                + ": " + error.getMessage());
        if (error.getRawLine() != null) {
            System.err.println("  Line content: " + error.getRawLine());
        }
    }

    @Override
    public void fatalError(GedcomParseError error) {
        System.err.println("FATAL line " + error.getLineNumber()
                + ": " + error.getMessage());
    }
};
```

In **lenient mode** (the default), warnings and errors are reported but parsing continues. You get as much data as possible.

In **strict mode**, the first error throws a `GedcomFatalException`:

```java
GedcomReaderConfig strict = GedcomReaderConfig.gedcom7Strict();

try (GedcomReader reader = new GedcomReader(in, handler, strict)) {
    reader.parse();
} catch (GedcomFatalException e) {
    System.err.println("Parsing aborted: " + e.getError().getMessage());
}
```

### What Gets Detected

| Issue | Severity | Example |
|-------|----------|---------|
| Leading whitespace on a line | Warning | `  1 NAME John` |
| Unresolved cross-reference | Warning | `@I99@` referenced but never defined |
| Non-GEDCOM 7 version | Warning | `VERS 5.5.1` in HEAD |
| Missing TRLR | Warning | File ends without trailer |
| Banned control characters | Error | Bell character `U+0007` in a value |
| Duplicate cross-reference | Error | Two records with `@I1@` |
| Level jump > 1 | Error | Level 0 followed by level 3 |
| TRLR with value or xref | Error | `0 @X@ TRLR some_value` |
| Missing HEAD | Fatal | File does not start with HEAD |
| Max nesting depth exceeded | Fatal | Level exceeds configured limit |
| Max line length exceeded | Fatal | Single line exceeds configured limit |

## Step 7: Parsing Data Type Values

GEDCOM values are plain strings. The `GedcomDataTypes` utility class can parse them into structured objects:

### Dates

```java
import org.gedcom7.parser.datatype.*;

// The parser returns Object because dates can be exact, ranges, or periods
Object result = GedcomDataTypes.parseDateValue("6 APR 1952");

if (result instanceof GedcomDateRange) {
    GedcomDateRange range = (GedcomDateRange) result;
    GedcomDate date = range.getStart();
    System.out.println("Year: " + date.getYear());    // 1952
    System.out.println("Month: " + date.getMonth());   // APR
    System.out.println("Day: " + date.getDay());       // 6
}

// Range: "BET 1 JAN 1950 AND 31 DEC 1960"
// Period: "FROM 1950 TO 1960"
// Approximate: "ABT 1952", "BEF 1960", "AFT 1940"
```

### Personal Names

GEDCOM encodes surnames between slashes:

```java
GedcomPersonalName name = GedcomDataTypes.parsePersonalName("John /Smith/ Jr.");
System.out.println(name.getGivenName());   // John
System.out.println(name.getSurname());     // Smith
System.out.println(name.getNameSuffix());  // Jr.
```

### Ages

```java
GedcomAge age = GedcomDataTypes.parseAge("30y 6m 2w");
System.out.println(age.getYears());   // 30
System.out.println(age.getMonths());  // 6
System.out.println(age.getWeeks());   // 2

// With modifier
GedcomAge over = GedcomDataTypes.parseAge("> 25y");
System.out.println(over.getModifier()); // >
```

### Coordinates

```java
GedcomCoordinate lat = GedcomDataTypes.parseLatitude("N51.5074");
GedcomCoordinate lon = GedcomDataTypes.parseLongitude("W0.1278");
System.out.println(lat.getDirection()); // N
System.out.println(lat.getValue());     // 51.5074
```

### Other Types

```java
// Time
GedcomTime time = GedcomDataTypes.parseTime("14:30:00Z");
// time.getHours() == 14, time.isUtc() == true

// Language tag (BCP 47)
String lang = GedcomDataTypes.parseLanguage("en");

// URI
java.net.URI uri = GedcomDataTypes.parseUri("https://example.com");

// Integer (non-negative, no leading zeros)
int n = GedcomDataTypes.parseInteger("42");
```

## Step 8: Working with Extension Tags

GEDCOM 7 allows software to define custom tags prefixed with `_`. These are mapped to URIs in the HEAD's SCHMA section:

```
0 HEAD
1 GEDC
2 VERS 7.0
1 SCHMA
2 TAG _GODP https://example.com/gedcom/godparent
0 @I1@ INDI
1 NAME John /Smith/
1 _GODP @I5@
0 TRLR
```

You can receive the resolved URI directly in the 6-parameter `startStructure`:

```java
GedcomHandler handler = new GedcomHandler() {
    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer, String uri) {
        if (uri != null) {
            System.out.println("Extension: " + tag + " -> " + uri);
            // Extension: _GODP -> https://example.com/gedcom/godparent
        }
    }
};
```

Standard tags always receive `null` for the URI parameter. If you override only the 5-parameter version, the parser delegates automatically and you simply won't see the URI.

## Step 9: Building a Family Tree

Here is a more complete example that builds a family tree data structure:

```java
import org.gedcom7.parser.*;
import java.io.FileInputStream;
import java.util.*;

public class FamilyTreeBuilder {

    static class Person {
        String xref;
        String name;
        String sex;
        String birthDate;
        String birthPlace;
    }

    static class Family {
        String xref;
        String husbandXref;
        String wifeXref;
        String marriageDate;
    }

    public static void main(String[] args) throws Exception {
        Map<String, Person> people = new LinkedHashMap<>();
        Map<String, Family> families = new LinkedHashMap<>();

        GedcomHandler handler = new GedcomHandler() {
            private String currentRecordTag;
            private String currentXref;
            private String currentSubTag;  // tracks the level-1 tag (e.g., BIRT)

            @Override
            public void startRecord(int level, String xref, String tag) {
                currentRecordTag = tag;
                currentXref = xref;
                currentSubTag = null;

                if ("INDI".equals(tag)) {
                    Person p = new Person();
                    p.xref = xref;
                    people.put(xref, p);
                } else if ("FAM".equals(tag)) {
                    Family f = new Family();
                    f.xref = xref;
                    families.put(xref, f);
                }
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if ("INDI".equals(currentRecordTag)) {
                    Person p = people.get(currentXref);
                    if (level == 1) {
                        currentSubTag = tag;
                        if ("NAME".equals(tag)) p.name = value;
                        if ("SEX".equals(tag)) p.sex = value;
                    } else if (level == 2 && "BIRT".equals(currentSubTag)) {
                        if ("DATE".equals(tag)) p.birthDate = value;
                        if ("PLAC".equals(tag)) p.birthPlace = value;
                    }
                } else if ("FAM".equals(currentRecordTag)) {
                    Family f = families.get(currentXref);
                    if (level == 1) {
                        currentSubTag = tag;
                        if ("HUSB".equals(tag) && isPointer) {
                            f.husbandXref = stripAt(value);
                        }
                        if ("WIFE".equals(tag) && isPointer) {
                            f.wifeXref = stripAt(value);
                        }
                    } else if (level == 2 && "MARR".equals(currentSubTag)) {
                        if ("DATE".equals(tag)) f.marriageDate = value;
                    }
                }
            }

            private String stripAt(String ref) {
                if (ref != null && ref.startsWith("@") && ref.endsWith("@")) {
                    return ref.substring(1, ref.length() - 1);
                }
                return ref;
            }
        };

        try (FileInputStream in = new FileInputStream(args[0]);
             GedcomReader reader = new GedcomReader(
                     in, handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // Print results
        System.out.println("=== People ===");
        for (Person p : people.values()) {
            System.out.printf("@%s@ %s (%s) born %s at %s%n",
                    p.xref, p.name, p.sex, p.birthDate, p.birthPlace);
        }

        System.out.println("\n=== Families ===");
        for (Family f : families.values()) {
            Person husband = people.get(f.husbandXref);
            Person wife = people.get(f.wifeXref);
            System.out.printf("@%s@ %s + %s, married %s%n",
                    f.xref,
                    husband != null ? husband.name : "?",
                    wife != null ? wife.name : "?",
                    f.marriageDate);
        }
    }
}
```

## Step 10: Configuration for Untrusted Input

When parsing files from unknown sources, tighten the resource limits:

```java
GedcomReaderConfig secure = new GedcomReaderConfig.Builder()
        .strict(false)                  // still lenient (collect all issues)
        .maxNestingDepth(50)            // real files rarely exceed 10
        .maxLineLength(65536)           // 64 KB per line is generous
        .structureValidation(true)      // warn on unexpected structures
        .build();
```

## Summary

| To do this... | ...use this |
|---------------|-------------|
| Parse a file | `new GedcomReader(inputStream, handler, config)` then `reader.parse()` |
| Receive events | Extend `GedcomHandler`, override callbacks |
| Configure parsing | `GedcomReaderConfig.gedcom7()`, `.gedcom7Strict()`, or `Builder` |
| Read header metadata | Override `startDocument(GedcomHeaderInfo)` |
| Parse date strings | `GedcomDataTypes.parseDateValue(value)` |
| Parse name strings | `GedcomDataTypes.parsePersonalName(value)` |
| Handle errors | Override `warning()`, `error()`, `fatalError()` |
| Get extension URIs | Override 6-param `startStructure(...)` or look up `headerInfo.getSchemaMap()` |
