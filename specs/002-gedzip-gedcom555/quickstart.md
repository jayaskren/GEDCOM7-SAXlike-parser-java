# Quickstart: GEDZip Support and GEDCOM 5.5.5 Compatibility

**Date**: 2026-03-05 | **Branch**: `002-gedzip-gedcom555`

## Scenario 1: Parse a GEDCOM 5.5.5 File

```java
import org.gedcom7.parser.*;

try (var in = new FileInputStream("family-555.ged");
     var reader = new GedcomReader(in, new GedcomHandler() {
         @Override
         public void startDocument(GedcomHeaderInfo info) {
             System.out.println("Version: " + info.getVersion());
             System.out.println("Encoding: " + info.getCharacterEncoding());
         }

         @Override
         public void startRecord(int level, String xref, String tag) {
             System.out.println("Record: " + tag);
         }

         @Override
         public void startStructure(int level, String xref, String tag,
                                    String value, boolean isPointer) {
             // CONC values are already assembled — no special handling needed
             if (value != null) {
                 System.out.println("  ".repeat(level) + tag + " = " + value);
             }
         }
     }, GedcomReaderConfig.gedcom555())) {
    reader.parse();
}
```

**Key point**: The handler code is identical to GEDCOM 7 usage. CONC assembly and @@ decoding happen automatically.

## Scenario 2: Auto-Detect GEDCOM Version

```java
// Works with both GEDCOM 7.0 and 5.5.5 files — no version check needed
GedcomReaderConfig config = GedcomReaderConfig.autoDetect();

try (var in = new FileInputStream("unknown-version.ged");
     var reader = new GedcomReader(in, myHandler, config)) {
    reader.parse();
}
```

The parser reads `HEAD.GEDC.VERS` and automatically applies the correct parsing rules.

## Scenario 3: Open a GEDZip Archive

```java
import org.gedcom7.parser.*;
import java.nio.file.Path;

try (var gdz = new GedzipReader(Path.of("family.gdz"))) {
    // Parse the GEDCOM data
    try (var gedcom = gdz.getGedcomStream();
         var reader = new GedcomReader(gedcom, myHandler,
                                       GedcomReaderConfig.autoDetect())) {
        reader.parse();
    }

    // Access media files referenced in the GEDCOM data
    InputStream photo = gdz.getEntry("photos/grandma.jpg");
    if (photo != null) {
        // process the photo...
    }

    // Check if a file exists in the archive
    boolean hasPhoto = gdz.hasEntry("photos/grandma.jpg");

    // List all files in the archive
    Set<String> entries = gdz.getEntryNames();
}
```

## Scenario 4: Strict 5.5.5 Validation

```java
GedcomReaderConfig strict = GedcomReaderConfig.gedcom555Strict();

try (var in = new FileInputStream("to-validate.ged");
     var reader = new GedcomReader(in, new GedcomHandler() {
         @Override
         public void error(GedcomParseError error) {
             System.err.println("ERROR line " + error.getLineNumber()
                              + ": " + error.getMessage());
         }

         @Override
         public void fatalError(GedcomParseError error) {
             System.err.println("FATAL line " + error.getLineNumber()
                              + ": " + error.getMessage());
         }
     }, strict)) {
    reader.parse();  // Throws GedcomFatalException on first error
}
```

Strict mode enforces GEDCOM 5.5.5 constraints:
- Maximum line length of 255 characters
- BOM required
- No bare `@` characters in values

## Scenario 5: Unified Handler for Both Versions

```java
// One handler implementation works for both versions
class FamilyExtractor extends GedcomHandler {
    @Override
    public void startDocument(GedcomHeaderInfo info) {
        System.out.println("Parsing " + info.getVersion() + " file");
        if (info.getCharacterEncoding() != null) {
            System.out.println("Encoding: " + info.getCharacterEncoding());
        }
    }

    @Override
    public void startStructure(int level, String xref, String tag,
                                String value, boolean isPointer) {
        if ("NAME".equals(tag) && value != null) {
            System.out.println("Found name: " + value);
        }
    }
}

FamilyExtractor handler = new FamilyExtractor();

// Parse a GEDCOM 7 file
try (var in = new FileInputStream("modern.ged");
     var reader = new GedcomReader(in, handler, GedcomReaderConfig.autoDetect())) {
    reader.parse();
}

// Parse a GEDCOM 5.5.5 file — same handler, same code
try (var in = new FileInputStream("legacy.ged");
     var reader = new GedcomReader(in, handler, GedcomReaderConfig.autoDetect())) {
    reader.parse();
}
```
