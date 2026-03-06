# Contract: GedcomDateValue Interface

**Story**: US3 (Type-Safe Date Values)
**Package**: `org.gedcom7.parser.datatype`

## Interface: GedcomDateValue

```java
/**
 * Common interface for all parsed GEDCOM date value representations.
 * Implementations include exact dates, date ranges, date periods,
 * and approximate dates.
 */
public interface GedcomDateValue {

    /**
     * Returns the type of this date value.
     */
    DateValueType getType();

    /**
     * Returns the original unparsed date text.
     */
    String getOriginalText();
}
```

## Enum: DateValueType

```java
public enum DateValueType {
    /** A specific date (e.g., "6 APR 1952") */
    EXACT,
    /** A date range: BET...AND, BEF, AFT (per GEDCOM 7 DateRange production) */
    RANGE,
    /** A date period (e.g., "FROM 1900 TO 1910") */
    PERIOD,
    /** An approximate date: ABT, CAL, EST */
    APPROXIMATE,
    /** A date string that could not be parsed */
    UNPARSEABLE
}
```

## Modified Method Signature

```java
// Before:
public static Object parseDateValue(String text)

// After:
public static GedcomDateValue parseDateValue(String text)
```

## Backward Compatibility

- Existing code using `Object result = parseDateValue(...)` continues to compile
- Existing `instanceof GedcomDate` checks continue to work
- New code can use `result.getType()` instead of `instanceof`
