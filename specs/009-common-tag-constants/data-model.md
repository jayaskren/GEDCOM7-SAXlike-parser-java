# Data Model: Common Substructure Tag Constants

## Class Hierarchy

All new classes are nested inside the existing `GedcomTag` class.

```
GedcomTag (existing)
├── HEAD, TRLR, INDI, FAM, OBJE, SOUR, REPO, NOTE, SNOTE, SUBM (existing)
├── Indi (existing)
│   ├── Birt, Deat, Buri, Chr, Bapm, Even (existing)
│   └── ... existing constants
├── Fam (existing)
│   ├── Marr, Div, Anul, Even (existing)
│   └── ... existing constants
├── Sour, Repo, Obje, Snote, Subm, Head (existing)
│
├── Plac (NEW)           ← substructures of PLAC
│   └── MAP, FORM, LANG, TRAN, EXID, NOTE, SNOTE, SOUR
├── Map (NEW)            ← substructures of MAP
│   └── LATI, LONG
├── Date (NEW)           ← substructures of DATE
│   └── TIME, PHRASE
├── File (NEW)           ← substructures of FILE
│   └── FORM, TITL, TRAN
├── Form (NEW)           ← substructures of FORM
│   └── MEDI
├── Addr (NEW)           ← substructures of ADDR
│   └── ADR1, ADR2, ADR3, CITY, STAE, POST, CTRY
├── Gedc (NEW)           ← substructures of GEDC
│   └── VERS
├── Name (NEW)           ← substructures of personal NAME
│   └── GIVN, SURN, NPFX, NSFX, SPFX, NICK, TYPE, TRAN, NOTE, SNOTE, SOUR
├── Refn (NEW)           ← substructures of REFN
│   └── TYPE
├── Exid (NEW)           ← substructures of EXID
│   └── TYPE
├── Asso (NEW)           ← substructures of ASSO
│   └── ROLE, PHRASE, NOTE, SNOTE, SOUR
├── Famc (NEW)           ← substructures of INDI-FAMC
│   └── PEDI, STAT, NOTE, SNOTE
├── Chan (NEW)           ← substructures of CHAN
│   └── DATE, NOTE, SNOTE
├── Crea (NEW)           ← substructures of CREA
│   └── DATE
├── SourCitation (NEW)   ← substructures of SOUR citation
│   └── PAGE, DATA, EVEN, QUAY, NOTE, SNOTE, OBJE
└── Schma (NEW)          ← substructures of SCHMA
    └── TAG
```

## Constant Inventory

### GedcomTag.Plac
| Constant | Value | Description |
|----------|-------|-------------|
| MAP | "MAP" | Geographic coordinates container |
| FORM | "FORM" | Place hierarchy format |
| LANG | "LANG" | Language of place name |
| TRAN | "TRAN" | Translated place name |
| EXID | "EXID" | External identifier |
| NOTE | "NOTE" | Note reference |
| SNOTE | "SNOTE" | Shared note reference |
| SOUR | "SOUR" | Source citation |

### GedcomTag.Map
| Constant | Value | Description |
|----------|-------|-------------|
| LATI | "LATI" | Latitude coordinate |
| LONG | "LONG" | Longitude coordinate |

### GedcomTag.Date
| Constant | Value | Description |
|----------|-------|-------------|
| TIME | "TIME" | Time of day |
| PHRASE | "PHRASE" | Narrative date phrase |

### GedcomTag.File
| Constant | Value | Description |
|----------|-------|-------------|
| FORM | "FORM" | File format/media type |
| TITL | "TITL" | Descriptive title |
| TRAN | "TRAN" | Translated file reference |

### GedcomTag.Form
| Constant | Value | Description |
|----------|-------|-------------|
| MEDI | "MEDI" | Source medium type |

### GedcomTag.Addr
| Constant | Value | Description |
|----------|-------|-------------|
| ADR1 | "ADR1" | First address line |
| ADR2 | "ADR2" | Second address line |
| ADR3 | "ADR3" | Third address line |
| CITY | "CITY" | City name |
| STAE | "STAE" | State or province |
| POST | "POST" | Postal code |
| CTRY | "CTRY" | Country name |

### GedcomTag.Gedc
| Constant | Value | Description |
|----------|-------|-------------|
| VERS | "VERS" | GEDCOM version number |

### GedcomTag.Name
| Constant | Value | Description |
|----------|-------|-------------|
| GIVN | "GIVN" | Given (first) names |
| SURN | "SURN" | Surname |
| NPFX | "NPFX" | Name prefix (e.g., Dr.) |
| NSFX | "NSFX" | Name suffix (e.g., Jr.) |
| SPFX | "SPFX" | Surname prefix (e.g., von) |
| NICK | "NICK" | Nickname |
| TYPE | "TYPE" | Name type |
| TRAN | "TRAN" | Translated name |
| NOTE | "NOTE" | Note reference |
| SNOTE | "SNOTE" | Shared note reference |
| SOUR | "SOUR" | Source citation |

### GedcomTag.Refn
| Constant | Value | Description |
|----------|-------|-------------|
| TYPE | "TYPE" | Reference number type |

### GedcomTag.Exid
| Constant | Value | Description |
|----------|-------|-------------|
| TYPE | "TYPE" | External identifier type |

### GedcomTag.Asso
| Constant | Value | Description |
|----------|-------|-------------|
| ROLE | "ROLE" | Role in the association |
| PHRASE | "PHRASE" | Descriptive phrase |
| NOTE | "NOTE" | Note reference |
| SNOTE | "SNOTE" | Shared note reference |
| SOUR | "SOUR" | Source citation |

### GedcomTag.Famc
| Constant | Value | Description |
|----------|-------|-------------|
| PEDI | "PEDI" | Pedigree linkage type |
| STAT | "STAT" | Status of family-child link |
| NOTE | "NOTE" | Note reference |
| SNOTE | "SNOTE" | Shared note reference |

### GedcomTag.Chan
| Constant | Value | Description |
|----------|-------|-------------|
| DATE | "DATE" | Date of last change |
| NOTE | "NOTE" | Note reference |
| SNOTE | "SNOTE" | Shared note reference |

### GedcomTag.Crea
| Constant | Value | Description |
|----------|-------|-------------|
| DATE | "DATE" | Creation date |

### GedcomTag.SourCitation
| Constant | Value | Description |
|----------|-------|-------------|
| PAGE | "PAGE" | Page or location within source |
| DATA | "DATA" | Source data container |
| EVEN | "EVEN" | Event type from source |
| QUAY | "QUAY" | Quality/certainty assessment |
| NOTE | "NOTE" | Note reference |
| SNOTE | "SNOTE" | Shared note reference |
| OBJE | "OBJE" | Multimedia reference |

### GedcomTag.Schma
| Constant | Value | Description |
|----------|-------|-------------|
| TAG | "TAG" | Extension tag definition |

## Total Count

- **New nested classes**: 16
- **New constants**: ~70
- **Existing classes/constants**: Unchanged
