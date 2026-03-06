# Contract: Public Strategy Interfaces

**Story**: US4 (Custom Decoding Strategies)
**Package**: `org.gedcom7.parser.spi` (new package)

## Relocated Interfaces

These interfaces move from `org.gedcom7.parser.internal` to
`org.gedcom7.parser.spi`. Their signatures remain unchanged.

### GedcomInputDecoder

```java
package org.gedcom7.parser.spi;

/**
 * Strategy for decoding a GEDCOM input byte stream into characters.
 * Implementations handle encoding detection and BOM stripping.
 */
public interface GedcomInputDecoder {
    /**
     * Decodes the given byte stream into a character reader.
     */
    Reader decode(InputStream input) throws IOException;
}
```

### PayloadAssembler

```java
package org.gedcom7.parser.spi;

/**
 * Strategy for assembling multi-line payloads from continuation
 * pseudo-structures (CONT, CONC).
 */
public interface PayloadAssembler {
    /**
     * Returns true if the given tag is a continuation pseudo-structure.
     */
    boolean isPseudoStructure(String tag);

    /**
     * Appends the continuation value to the pending payload.
     */
    void appendPayload(StringBuilder pending, String tag, String value);
}
```

### AtEscapeStrategy

```java
package org.gedcom7.parser.spi;

/**
 * Strategy for unescaping @@ sequences in GEDCOM line values.
 */
public interface AtEscapeStrategy {
    /**
     * Unescapes @@ sequences in the given value.
     */
    String unescape(String value);
}
```

## Module Info Update

```java
module org.gedcom7.parser {
    exports org.gedcom7.parser;
    exports org.gedcom7.parser.datatype;
    exports org.gedcom7.parser.validation;
    exports org.gedcom7.parser.spi;        // NEW
    exports org.gedcom7.writer;
    exports org.gedcom7.writer.context;
    exports org.gedcom7.writer.date;
}
```

## Builder Method Signature Changes

```java
// Before (using internal types - wouldn't compile from outside):
Builder inputDecoder(GedcomInputDecoder decoder)
Builder payloadAssembler(PayloadAssembler assembler)
Builder atEscapeStrategy(AtEscapeStrategy strategy)

// After (using spi types - compiles from any package):
Builder inputDecoder(GedcomInputDecoder decoder)      // same name, new package
Builder payloadAssembler(PayloadAssembler assembler)   // same name, new package
Builder atEscapeStrategy(AtEscapeStrategy strategy)    // same name, new package
```

## Migration for Internal Implementations

Existing internal implementations (`Utf8InputDecoder`, `ContOnlyAssembler`,
etc.) remain in `org.gedcom7.parser.internal` and implement the interfaces
from `org.gedcom7.parser.spi`.
