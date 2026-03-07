# Quickstart: Using Tag and Value Constants

## Before (string literals)

```java
GedcomHandler handler = new GedcomHandler() {
    final String[] currentIndi = {null};

    @Override
    public void startRecord(int level, String xref, String tag) {
        if ("INDI".equals(tag)) {
            currentIndi[0] = xref;
        } else {
            currentIndi[0] = null;
        }
    }

    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {
        if (currentIndi[0] != null) {
            if ("NAME".equals(tag) && value != null) {
                System.out.println(currentIndi[0] + ": " + value);
            }
            if ("SEX".equals(tag) && "M".equals(value)) {
                System.out.println(currentIndi[0] + " is male");
            }
        }
    }
};
```

## After (with constants)

```java
GedcomHandler handler = new GedcomHandler() {
    final String[] currentIndi = {null};

    @Override
    public void startRecord(int level, String xref, String tag) {
        switch (tag) {
            case GedcomTag.INDI:
                currentIndi[0] = xref;
                break;
            default:
                currentIndi[0] = null;
        }
    }

    @Override
    public void startStructure(int level, String xref, String tag,
                               String value, boolean isPointer) {
        if (currentIndi[0] != null) {
            switch (tag) {
                case GedcomTag.Indi.NAME:
                    if (value != null) {
                        GedcomPersonalName name = GedcomDataTypes.parsePersonalName(value);
                        System.out.println(currentIndi[0] + ": " + name.getSurname());
                    }
                    break;
                case GedcomTag.Indi.SEX:
                    if (GedcomValue.Sex.MALE.equals(value)) {
                        System.out.println(currentIndi[0] + " is male");
                    }
                    break;
                case GedcomTag.Indi.BIRT:
                    // BIRT is an event — use GedcomTag.Indi.Birt.DATE
                    // for its substructures at the next level
                    break;
            }
        }
    }
};
```

## Distinguishing DATE by event context

```java
// Inside a handler tracking parent context:
if ("BIRT".equals(parentTag)) {
    switch (tag) {
        case GedcomTag.Indi.Birt.DATE:
            birthDate = GedcomDataTypes.parseDateValue(value);
            break;
        case GedcomTag.Indi.Birt.PLAC:
            birthPlace = value;
            break;
    }
} else if ("DEAT".equals(parentTag)) {
    switch (tag) {
        case GedcomTag.Indi.Deat.DATE:
            deathDate = GedcomDataTypes.parseDateValue(value);
            break;
    }
}
```

## Using value constants

```java
// Check pedigree type
switch (value) {
    case GedcomValue.Pedi.BIRTH:
        // biological child
        break;
    case GedcomValue.Pedi.ADOPTED:
        // adopted child
        break;
    case GedcomValue.Pedi.FOSTER:
        // foster child
        break;
}

// Check restriction type
if (GedcomValue.Resn.CONFIDENTIAL.equals(value)) {
    // skip confidential records
}
```
