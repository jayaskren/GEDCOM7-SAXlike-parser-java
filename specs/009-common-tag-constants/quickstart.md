# Quickstart: Common Substructure Tag Constants

## Before (string literals)

```java
@Override
public void startStructure(int level, String xref, String tag,
                           String value, boolean isPointer) {
    if ("PLAC".equals(currentSubTag) && level == 2) {
        if ("MAP".equals(tag)) {
            // entering map structure
        }
    } else if ("MAP".equals(currentSubTag) && level == 3) {
        if ("LATI".equals(tag)) {
            latitude = value;
        } else if ("LONG".equals(tag)) {
            longitude = value;
        }
    }
}
```

## After (constants)

```java
@Override
public void startStructure(int level, String xref, String tag,
                           String value, boolean isPointer) {
    if (GedcomTag.Indi.Birt.PLAC.equals(currentSubTag) && level == 2) {
        if (GedcomTag.Plac.MAP.equals(tag)) {
            // entering map structure
        }
    } else if (GedcomTag.Plac.MAP.equals(currentSubTag) && level == 3) {
        if (GedcomTag.Map.LATI.equals(tag)) {
            latitude = value;
        } else if (GedcomTag.Map.LONG.equals(tag)) {
            longitude = value;
        }
    }
}
```

## More Examples

### Address Components

```java
// Before
if ("CITY".equals(tag)) city = value;
if ("STAE".equals(tag)) state = value;
if ("POST".equals(tag)) postalCode = value;
if ("CTRY".equals(tag)) country = value;

// After
if (GedcomTag.Addr.CITY.equals(tag)) city = value;
if (GedcomTag.Addr.STAE.equals(tag)) state = value;
if (GedcomTag.Addr.POST.equals(tag)) postalCode = value;
if (GedcomTag.Addr.CTRY.equals(tag)) country = value;
```

### Date with Time

```java
// Before
if ("DATE".equals(currentSubTag) && "TIME".equals(tag)) {
    time = value;
}

// After
if (GedcomTag.Chan.DATE.equals(currentSubTag) && GedcomTag.Date.TIME.equals(tag)) {
    time = value;
}
```

### Name Parts

```java
// Before
if ("GIVN".equals(tag)) givenName = value;
if ("SURN".equals(tag)) surname = value;
if ("NICK".equals(tag)) nickname = value;

// After
if (GedcomTag.Name.GIVN.equals(tag)) givenName = value;
if (GedcomTag.Name.SURN.equals(tag)) surname = value;
if (GedcomTag.Name.NICK.equals(tag)) nickname = value;
```

### Source Citation

```java
// Before
if ("SOUR".equals(currentSubTag)) {
    if ("PAGE".equals(tag)) page = value;
    if ("QUAY".equals(tag)) quality = value;
}

// After
if (GedcomTag.Indi.SOUR.equals(currentSubTag)) {
    if (GedcomTag.SourCitation.PAGE.equals(tag)) page = value;
    if (GedcomTag.SourCitation.QUAY.equals(tag)) quality = value;
}
```

### Switch Statement

```java
switch (tag) {
    case GedcomTag.Map.LATI:
        latitude = GedcomDataTypes.parseLatitude(value);
        break;
    case GedcomTag.Map.LONG:
        longitude = GedcomDataTypes.parseLongitude(value);
        break;
}
```
