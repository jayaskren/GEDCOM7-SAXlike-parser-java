# Quickstart: Writer Gaps Remediation

**Feature**: 006-writer-gaps | **Date**: 2026-03-05

## Quick Verification Scenarios

### US1: Personal Name Convenience

```java
ByteArrayOutputStream out = new ByteArrayOutputStream();
try (GedcomWriter writer = new GedcomWriter(out)) {
    writer.individual(indi -> {
        indi.personalName("John", "Doe");
    });
}
String result = out.toString(StandardCharsets.UTF_8);
// Expect: 1 NAME John /Doe/
//         2 GIVN John
//         2 SURN Doe
```

### US2: Shared Note With Text

```java
writer.sharedNoteWithText("This is a note", note -> {});
// Expect: 0 @N1@ SNOTE This is a note
```

### US3: LDS Ordinance

```java
writer.individual(indi -> {
    indi.ldsBaptism(body -> {
        body.date("15 JAN 1900");
        body.place("Salt Lake City, UT");
    });
});
// Expect: 1 BAPL
//         2 DATE 15 JAN 1900
//         2 PLAC Salt Lake City, UT
```

### US4: Public Builder Methods

```java
GedcomWriterConfig config = new GedcomWriterConfig.Builder()
    .escapeAllAt(true)
    .build();
// Compiles from any package
```

### US5: HEAD.CHAR for 5.5.5

```java
GedcomWriterConfig config = GedcomWriterConfig.gedcom555();
try (GedcomWriter writer = new GedcomWriter(out, config)) {
    writer.head(head -> {});
}
// Expect HEAD output includes: 1 CHAR UTF-8
```

### US6: Unchecked Exception

```java
// No try/catch needed — GedcomWriteException is now unchecked
writer.individual(indi -> {
    indi.personalName("John /Doe/");
});
```

### US7: Generic Event

```java
writer.individual(indi -> {
    indi.event("IMMI", body -> {
        body.date("15 JAN 1905");
        body.place("Ellis Island, NY");
    });
});
// Expect: 1 IMMI
//         2 DATE 15 JAN 1905
//         2 PLAC Ellis Island, NY
```

### US8: Calendar Escape Fix

```java
GedcomWriterConfig config = GedcomWriterConfig.gedcom555();
// In 5.5.5 mode:
indi.birth(body -> {
    body.date("@#DJULIAN@ 15 JAN 1700");
});
// Expect: 2 DATE @#DJULIAN@ 15 JAN 1700
// NOT:    2 DATE @@#DJULIAN@@ 15 JAN 1700
```

### US10: Sex Enum

```java
writer.individual(indi -> {
    indi.sex(Sex.MALE);
});
// Expect: 1 SEX M
```
