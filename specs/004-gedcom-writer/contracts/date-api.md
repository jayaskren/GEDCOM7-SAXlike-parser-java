# Contract: Date API

**Package**: `org.gedcom7.writer.date`

## GedcomDateBuilder

Static factory methods for constructing type-safe `WriterDate` objects.

```java
public final class GedcomDateBuilder {

    // --- Exact dates ---
    public static WriterDate date(int day, Month month, int year);
    public static WriterDate date(Month month, int year);
    public static WriterDate date(int year);

    // --- BCE dates ---
    public static WriterDate dateBce(int day, Month month, int year);
    public static WriterDate dateBce(Month month, int year);
    public static WriterDate dateBce(int year);

    // --- Approximate dates ---
    public static WriterDate about(WriterDate date);
    public static WriterDate about(int year);
    public static WriterDate about(Month month, int year);
    public static WriterDate calculated(WriterDate date);
    public static WriterDate estimated(WriterDate date);

    // --- Ranges ---
    public static WriterDate before(WriterDate date);
    public static WriterDate before(int year);
    public static WriterDate before(Month month, int year);
    public static WriterDate after(WriterDate date);
    public static WriterDate after(int year);
    public static WriterDate after(Month month, int year);
    public static WriterDate between(WriterDate start, WriterDate end);

    // --- Periods ---
    public static WriterDate from(WriterDate date);
    public static WriterDate to(WriterDate date);
    public static WriterDate fromTo(WriterDate start, WriterDate end);

    // --- Non-Gregorian calendars ---
    public static WriterDate julian(int day, Month month, int year);
    public static WriterDate julian(Month month, int year);
    public static WriterDate julian(int year);

    public static WriterDate hebrew(int day, HebrewMonth month, int year);
    public static WriterDate hebrew(HebrewMonth month, int year);
    public static WriterDate hebrew(int year);

    public static WriterDate frenchRepublican(int day, FrenchRepublicanMonth month, int year);
    public static WriterDate frenchRepublican(FrenchRepublicanMonth month, int year);
    public static WriterDate frenchRepublican(int year);

    private GedcomDateBuilder() {} // no instantiation
}
```

## WriterDate

Immutable date value object. Renders to GEDCOM string format.

```java
public final class WriterDate {

    // Expert escape hatch — no validation
    public static WriterDate raw(String gedcomDateString);

    // Render to GEDCOM string (version-aware)
    public String toGedcomString(GedcomVersion version);

    @Override public boolean equals(Object o);
    @Override public int hashCode();
    @Override public String toString();
}
```

## Month (enum)

```java
public enum Month {
    JAN(31), FEB(28), MAR(31), APR(30), MAY(31), JUN(30),
    JUL(31), AUG(31), SEP(30), OCT(31), NOV(30), DEC(31);

    public String abbreviation();
    public int maxDay();
}
```

## HebrewMonth (enum)

```java
public enum HebrewMonth {
    TSH, CSH, KSL, TVT, SHV, ADR, ADS, NSN, IYR, SVN, TMZ, AAV, ELL;

    public String abbreviation();
}
```

## FrenchRepublicanMonth (enum)

```java
public enum FrenchRepublicanMonth {
    VEND, BRUM, FRIM, NIVO, PLUV, VENT, GERM, FLOR, PRAI, MESS, THER, FRUC, COMP;

    public String abbreviation();
}
```

## Validation Rules

| Rule | Enforcement | Exception |
|------|-------------|-----------|
| Day must be 1..maxDay for the month | Construction time | IllegalArgumentException |
| Year must be >= 1 | Construction time | IllegalArgumentException |
| `between()` start must precede end (if both have same calendar) | Construction time | IllegalArgumentException |
| `fromTo()` start must precede end (if both have same calendar) | Construction time | IllegalArgumentException |
| No BCE with Hebrew calendar | Construction time | IllegalArgumentException |
| `raw()` — no validation | N/A | None |

## Rendering Rules

### GEDCOM 7 Format

| Form | Example |
|------|---------|
| Exact | `15 MAR 1955` |
| Month-year | `MAR 1955` |
| Year-only | `1955` |
| BCE | `44 BCE` |
| About | `ABT 1880` |
| Calculated | `CAL 1880` |
| Estimated | `EST 1880` |
| Before | `BEF JUN 1900` |
| After | `AFT 1850` |
| Between | `BET 1880 AND 1890` |
| From | `FROM 1 JAN 1940` |
| To | `TO 31 DEC 1945` |
| From-To | `FROM 1 JAN 1940 TO 31 DEC 1945` |
| Julian | `JULIAN 1 JAN 1700` |
| Hebrew | `HEBREW 15 NSN 5765` |
| French Republican | `FRENCH_R 1 VEND 1` |

### GEDCOM 5.5.5 Format

Non-Gregorian calendars use `@#D...@` escape format:

| Calendar | GEDCOM 7 | GEDCOM 5.5.5 |
|----------|----------|--------------|
| Julian | `JULIAN 1 JAN 1700` | `@#DJULIAN@ 1 JAN 1700` |
| Hebrew | `HEBREW 15 NSN 5765` | `@#DHEBREW@ 15 NSN 5765` |
| French Rep. | `FRENCH_R 1 VEND 1` | `@#DFRENCH R@ 1 VEND 1` |

## Test Expectations

### Exact Dates
```java
assertEquals("15 MAR 1955", date(15, MAR, 1955).toGedcomString(GEDCOM_7));
assertEquals("MAR 1955", date(MAR, 1955).toGedcomString(GEDCOM_7));
assertEquals("1955", date(1955).toGedcomString(GEDCOM_7));
```

### Approximate Dates
```java
assertEquals("ABT 1880", about(1880).toGedcomString(GEDCOM_7));
assertEquals("CAL MAR 1880", calculated(date(MAR, 1880)).toGedcomString(GEDCOM_7));
assertEquals("EST 1750", estimated(date(1750)).toGedcomString(GEDCOM_7));
```

### Ranges and Periods
```java
assertEquals("BET 1880 AND 1890", between(date(1880), date(1890)).toGedcomString(GEDCOM_7));
assertEquals("BEF JUN 1900", before(JUN, 1900).toGedcomString(GEDCOM_7));
assertEquals("FROM 1 JAN 1940 TO 31 DEC 1945",
    fromTo(date(1, JAN, 1940), date(31, DEC, 1945)).toGedcomString(GEDCOM_7));
```

### Non-Gregorian
```java
assertEquals("HEBREW 15 NSN 5765", hebrew(15, NSN, 5765).toGedcomString(GEDCOM_7));
assertEquals("@#DHEBREW@ 15 NSN 5765", hebrew(15, NSN, 5765).toGedcomString(V5_5_5));
```

### Validation
```java
assertThrows(IllegalArgumentException.class, () -> date(32, JAN, 1955));
assertThrows(IllegalArgumentException.class, () -> date(0, JAN, 1955));
assertThrows(IllegalArgumentException.class, () -> date(0));
```

### Raw Escape Hatch
```java
assertEquals("BET JULIAN 1 JAN 1700 AND JULIAN 31 DEC 1710",
    WriterDate.raw("BET JULIAN 1 JAN 1700 AND JULIAN 31 DEC 1710").toGedcomString(GEDCOM_7));
```
