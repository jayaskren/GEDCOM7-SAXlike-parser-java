# Contract: Context API

**Package**: `org.gedcom7.writer.context`

## CommonContext (abstract base)

Every typed context inherits these escape hatches and common methods:

```java
public abstract class CommonContext {

    // Escape hatches — available on ALL contexts
    public void structure(String tag, String value);
    public void structure(String tag, Consumer<GeneralContext> body);
    public void structure(String tag, String value, Consumer<GeneralContext> body);

    public void pointer(String tag, Xref ref);
    public void pointer(String tag, String id);
    public void pointer(String tag, Xref ref, Consumer<GeneralContext> body);
    public void pointer(String tag, String id, Consumer<GeneralContext> body);

    // Common substructures
    public void note(String text);
    public void sourceCitation(Xref ref);
    public void sourceCitation(String id);
    public void sourceCitation(Xref ref, Consumer<SourceCitationContext> body);
    public void sourceCitation(String id, Consumer<SourceCitationContext> body);
    public void uid(String uid);
}
```

## GeneralContext

The fallback context for escape hatch lambdas. No additional methods beyond CommonContext.

```java
public final class GeneralContext extends CommonContext {
    // Inherits all CommonContext methods. No additions.
}
```

## IndividualContext

```java
public final class IndividualContext extends CommonContext {
    // Name
    public void personalName(String value);
    public void personalName(String value, Consumer<PersonalNameContext> body);

    // Events
    public void birth(Consumer<EventContext> body);
    public void death(Consumer<EventContext> body);
    public void christening(Consumer<EventContext> body);
    public void burial(Consumer<EventContext> body);
    public void residence(Consumer<EventContext> body);

    // Attributes
    public void sex(String value);
    public void occupation(String value);
    public void education(String value);
    public void religion(String value);

    // Family links (version-aware — warns in GEDCOM 7)
    public void familyAsSpouse(Xref ref);
    public void familyAsSpouse(String id);
    public void familyAsChild(Xref ref);
    public void familyAsChild(String id);
}
```

## PersonalNameContext

```java
public final class PersonalNameContext extends CommonContext {
    public void givenName(String value);
    public void surname(String value);
    public void namePrefix(String value);
    public void nameSuffix(String value);
    public void nickname(String value);
    public void surnamePrefix(String value);
    public void type(String value);
}
```

## FamilyContext

```java
public final class FamilyContext extends CommonContext {
    // Pointers
    public void husband(Xref ref);
    public void husband(String id);
    public void wife(Xref ref);
    public void wife(String id);
    public void child(Xref ref);
    public void child(String id);

    // Events
    public void marriage(Consumer<EventContext> body);
    public void divorce(Consumer<EventContext> body);
    public void annulment(Consumer<EventContext> body);
}
```

## EventContext

```java
public final class EventContext extends CommonContext {
    public void date(WriterDate date);
    public void date(String rawDateString);
    public void place(String value);
    public void place(String value, Consumer<GeneralContext> body);
    public void address(Consumer<AddressContext> body);
    public void cause(String value);
    public void agency(String value);
    public void type(String value);
}
```

## SourceCitationContext

```java
public final class SourceCitationContext extends CommonContext {
    public void page(String value);
    public void data(Consumer<GeneralContext> body);
    public void quality(String value);
    public void eventType(String value);
    public void role(String value);
}
```

## HeadContext

```java
public final class HeadContext extends CommonContext {
    public void source(String value);
    public void source(String value, Consumer<GeneralContext> body);
    public void destination(String value);
    public void submitterRef(Xref ref);
    public void submitterRef(String id);
    public void note(String text);  // overrides CommonContext.note for HEAD-level
    public void schema(Consumer<SchemaContext> body);
}
```

## SchemaContext

```java
public final class SchemaContext extends CommonContext {
    public void tag(String extensionTag, String uri);
}
```

## AddressContext

```java
public final class AddressContext extends CommonContext {
    public void line1(String value);
    public void line2(String value);
    public void line3(String value);
    public void city(String value);
    public void state(String value);
    public void postalCode(String value);
    public void country(String value);
}
```

## SourceContext, RepositoryContext, MultimediaContext, SubmitterContext, NoteContext

These follow the same pattern — typed methods for their record-specific structures, plus CommonContext escape hatches.

```java
public final class SourceContext extends CommonContext {
    public void title(String value);
    public void author(String value);
    public void publicationFacts(String value);
    public void abbreviation(String value);
    public void repositoryCitation(Xref ref);
    public void repositoryCitation(String id);
    public void repositoryCitation(Xref ref, Consumer<GeneralContext> body);
    public void text(String value);
}

public final class RepositoryContext extends CommonContext {
    public void name(String value);
    public void address(Consumer<AddressContext> body);
}

public final class MultimediaContext extends CommonContext {
    public void file(String value);
    public void file(String value, Consumer<GeneralContext> body);
}

public final class SubmitterContext extends CommonContext {
    public void name(String value);
    public void address(Consumer<AddressContext> body);
}

public final class NoteContext extends CommonContext {
    // Value is set at record creation. Children via escape hatches.
}
```

## Test Expectations

### Escape Hatch on Typed Context
```java
writer.individual(indi -> {
    indi.personalName("John /Doe/");
    indi.structure("_CUSTOM", "custom-value");
    indi.structure("_BLOCK", block -> {
        block.structure("_INNER", "nested");
    });
});
// Output includes:
// 1 _CUSTOM custom-value
// 1 _BLOCK
// 2 _INNER nested
```

### Value + Children
```java
writer.individual(indi -> {
    indi.personalName("John /Doe/", name -> {
        name.givenName("John");
        name.surname("Doe");
    });
});
// Output:
// 1 NAME John /Doe/
// 2 GIVN John
// 2 SURN Doe
```

### Source Citation with Details
```java
Xref census = writer.source(src -> src.title("1880 Census"));
writer.individual(indi -> {
    indi.birth(birt -> {
        birt.date(GedcomDateBuilder.date(15, Month.MAR, 1955));
        birt.place("Springfield, IL");
        birt.sourceCitation(census, cite -> {
            cite.page("Roll 108, Page 42");
        });
    });
});
// Output includes:
// 2 SOUR @S1@
// 3 PAGE Roll 108, Page 42
```
