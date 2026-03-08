# Public API Contract: Common Substructure Tag Constants

## Overview

This feature adds 16 new nested classes and ~70 new constants to the existing `GedcomTag` class. No new top-level classes are created. No existing APIs are modified or removed.

## Contract

### New Nested Classes in GedcomTag

All new classes follow the existing pattern:
- `public static final class` nested inside `GedcomTag`
- Private constructor (non-instantiable)
- All fields are `public static final String`
- All constants usable as `switch` case labels (compile-time constants)

### Access Patterns

```
// Place coordinates
GedcomTag.Plac.MAP      → "MAP"
GedcomTag.Map.LATI      → "LATI"
GedcomTag.Map.LONG      → "LONG"

// Date with time
GedcomTag.Date.TIME     → "TIME"
GedcomTag.Date.PHRASE   → "PHRASE"

// File format
GedcomTag.File.FORM     → "FORM"
GedcomTag.Form.MEDI     → "MEDI"

// Address components
GedcomTag.Addr.CITY     → "CITY"
GedcomTag.Addr.STAE     → "STAE"
GedcomTag.Addr.POST     → "POST"
GedcomTag.Addr.CTRY     → "CTRY"

// Name parts
GedcomTag.Name.GIVN     → "GIVN"
GedcomTag.Name.SURN     → "SURN"

// GEDCOM version
GedcomTag.Gedc.VERS     → "VERS"

// Source citation
GedcomTag.SourCitation.PAGE  → "PAGE"
GedcomTag.SourCitation.QUAY  → "QUAY"
```

### Backward Compatibility

- All existing `GedcomTag.*` constants remain unchanged
- All existing nested classes remain unchanged
- No existing method signatures change
- Module exports remain unchanged
- Binary and source compatibility: GUARANTEED

### Navigation Pattern

Developers navigate from record context to common context:

```
GedcomTag.Indi.BIRT         → "BIRT"     (record substructure)
GedcomTag.Indi.Birt.PLAC    → "PLAC"     (event substructure)
GedcomTag.Plac.MAP          → "MAP"      (common substructure)
GedcomTag.Map.LATI          → "LATI"     (common substructure)
```

### Javadoc Cross-References

PLAC constants in event classes (e.g., `GedcomTag.Indi.Birt.PLAC`) will include `@see GedcomTag.Plac` to guide developers to the deeper substructure constants.
