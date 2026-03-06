# Contract: Writer API

**Package**: `org.gedcom7.writer`

## GedcomWriter

```java
public final class GedcomWriter implements AutoCloseable {

    // Construction
    public GedcomWriter(OutputStream out, GedcomWriterConfig config);
    public GedcomWriter(OutputStream out); // uses GedcomWriterConfig.gedcom7()

    // HEAD record
    public void head(Consumer<HeadContext> body) throws GedcomWriteException;

    // Top-level records (auto-generated xref)
    public Xref individual(Consumer<IndividualContext> body) throws GedcomWriteException;
    public Xref family(Consumer<FamilyContext> body) throws GedcomWriteException;
    public Xref source(Consumer<SourceContext> body) throws GedcomWriteException;
    public Xref repository(Consumer<RepositoryContext> body) throws GedcomWriteException;
    public Xref multimedia(Consumer<MultimediaContext> body) throws GedcomWriteException;
    public Xref submitter(Consumer<SubmitterContext> body) throws GedcomWriteException;
    public Xref sharedNote(Consumer<NoteContext> body) throws GedcomWriteException;

    // Top-level records (developer-provided xref ID)
    public Xref individual(String id, Consumer<IndividualContext> body) throws GedcomWriteException;
    public Xref family(String id, Consumer<FamilyContext> body) throws GedcomWriteException;
    public Xref source(String id, Consumer<SourceContext> body) throws GedcomWriteException;
    public Xref repository(String id, Consumer<RepositoryContext> body) throws GedcomWriteException;
    public Xref multimedia(String id, Consumer<MultimediaContext> body) throws GedcomWriteException;
    public Xref submitter(String id, Consumer<SubmitterContext> body) throws GedcomWriteException;
    public Xref sharedNote(String id, Consumer<NoteContext> body) throws GedcomWriteException;

    // Escape hatches for arbitrary record types
    public Xref record(String tag, Consumer<GeneralContext> body) throws GedcomWriteException;
    public Xref record(String id, String tag, Consumer<GeneralContext> body) throws GedcomWriteException;
    public void record(String tag, String value) throws GedcomWriteException;

    // Trailer
    public void trailer();

    // AutoCloseable
    @Override
    public void close() throws IOException;
}
```

## GedcomWriterConfig

```java
public final class GedcomWriterConfig {

    // Factory methods
    public static GedcomWriterConfig gedcom7();
    public static GedcomWriterConfig gedcom7Strict();
    public static GedcomWriterConfig gedcom555();
    public static GedcomWriterConfig gedcom555Strict();

    // Getters
    public GedcomVersion getVersion();
    public boolean isStrict();
    public WarningHandler getWarningHandler();
    public int getMaxLineLength();
    public String getLineEnding();

    // Builder access
    public Builder toBuilder();

    public static final class Builder {
        public Builder strict(boolean strict);
        public Builder warningHandler(WarningHandler handler);
        public Builder maxLineLength(int maxLength);
        public Builder lineEnding(String ending);
        public GedcomWriterConfig build();
    }
}
```

## WarningHandler

```java
@FunctionalInterface
public interface WarningHandler {
    void handle(GedcomWriteWarning warning);
}
```

## GedcomWriteWarning

```java
public final class GedcomWriteWarning {
    public GedcomWriteWarning(String message, String tag);
    public String getMessage();
    public String getTag();
    @Override public String toString();
}
```

## GedcomWriteException

```java
public class GedcomWriteException extends Exception {
    public GedcomWriteException(String message);
    public GedcomWriteException(String message, Throwable cause);
}
```

## Xref

```java
public final class Xref {
    // Construction
    public static Xref of(String id);

    // Access
    public String getId();

    @Override public boolean equals(Object o);
    @Override public int hashCode();
    @Override public String toString();
}
```

## Test Expectations

### Basic Writer Test
```java
ByteArrayOutputStream out = new ByteArrayOutputStream();
try (GedcomWriter writer = new GedcomWriter(out)) {
    writer.head(head -> head.source("MyApp"));
    Xref john = writer.individual(indi -> {
        indi.personalName("John /Doe/");
    });
    // john.getId() returns auto-generated ID like "I1"
}
String output = out.toString(StandardCharsets.UTF_8);
// output starts with "0 HEAD\n1 GEDC\n2 VERS 7.0\n1 SOUR MyApp\n"
// output contains "0 @I1@ INDI\n1 NAME John /Doe/\n"
// output ends with "0 TRLR\n"
```

### Config Test
```java
GedcomWriterConfig config = GedcomWriterConfig.gedcom555();
assertFalse(config.isStrict());
assertEquals(GedcomVersion.V5_5_5, config.getVersion());

GedcomWriterConfig strict = GedcomWriterConfig.gedcom7Strict();
assertTrue(strict.isStrict());
```

### Strict Mode Test
```java
ByteArrayOutputStream out = new ByteArrayOutputStream();
try (GedcomWriter writer = new GedcomWriter(out, GedcomWriterConfig.gedcom7Strict())) {
    // Missing head() call before individual() in strict mode
    assertThrows(GedcomWriteException.class, () -> {
        writer.individual(indi -> indi.personalName("John /Doe/"));
    });
}
```
