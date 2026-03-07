# Research: Tag and Value Constants

## R1: Tag Data Source

**Decision**: Derive all tag constants from `StructureDefinitions.java` which is itself generated from the official GEDCOM 7 specification TSV files.

**Rationale**: Single source of truth already exists in the codebase. Using StructureDefinitions ensures constants match exactly what the parser's validation layer recognizes.

**Alternatives considered**:
- Manually transcribe from GEDCOM 7 spec PDF: error-prone, harder to maintain
- Parse the TSV files at build time: adds build complexity for no benefit since StructureDefinitions already has the data

## R2: Record Types Requiring Nested Classes

**Decision**: Create nested static classes for these record types (those with substructures in StructureDefinitions):
- `Indi` (individual)
- `Fam` (family)
- `Sour` (source record)
- `Repo` (repository)
- `Obje` (multimedia object)
- `Snote` (shared note)
- `Subm` (submitter)
- `Head` (header)

TRLR has no substructures and does not need a nested class.

**Rationale**: Every record type that has substructures defined in StructureDefinitions gets a nested class. This gives complete coverage of the GEDCOM 7 standard.

## R3: Event Structures Requiring Sub-nesting

**Decision**: The following event tags get their own nested class within their parent record class, containing substructure constants (DATE, PLAC, AGE, CAUS, etc.):

Individual events (inside `GedcomTag.Indi`):
- BIRT, DEAT, ADOP, BAPM, BARM, BASM, BLES, BURI, CAST, CENS, CHR, CHRA, CONF, CREM, DSCR, EDUC, EMIG, EVEN, FACT, FCOM, GRAD, IDNO, IMMI, NATI, NATU, NCHI, NMR, OCCU, ORDN, PROB, PROP, RELI, RESI, RETI, SSN, TITL, WILL

Family events (inside `GedcomTag.Fam`):
- MARR, MARB, MARC, MARL, MARS, ANUL, DIV, DIVF, ENGA, CENS, EVEN, FACT, NCHI, RESI

**Refinement**: Many INDI substructures are "individual attribute" or "individual event" types that share the same substructure set (DATE, PLAC, ADDR, NOTE, SOUR, etc.). Rather than creating 30+ nearly-identical nested classes, only the most commonly used events will get sub-nesting: **BIRT, DEAT, BURI, CHR, BAPM, MARR, DIV, ANUL, EVEN**. Other events share the same tag names (DATE, PLAC) so developers can use any event's nested class for the tag value — e.g., `GedcomTag.Indi.Birt.DATE` equals `"DATE"` which works for any event context.

**Rationale**: Balances discoverability with API surface area. The 9 most common events cover the vast majority of developer needs. The string values are identical across events, so no functionality is lost.

## R4: Value Constant Naming Mapping

**Decision**: Use descriptive names for GEDCOM codes. Complete mapping:

### Sex
| Constant | GEDCOM Value |
|----------|-------------|
| MALE | M |
| FEMALE | F |
| INTERSEX | X |
| UNKNOWN | U |

### NameType
| Constant | GEDCOM Value |
|----------|-------------|
| BIRTH | BIRTH |
| IMMIGRANT | IMMIGRANT |
| MAIDEN | MAIDEN |
| MARRIED | MARRIED |
| PROFESSIONAL | PROFESSIONAL |
| OTHER | OTHER |

### Pedi
| Constant | GEDCOM Value |
|----------|-------------|
| BIRTH | BIRTH |
| ADOPTED | ADOPTED |
| FOSTER | FOSTER |
| SEALING | SEALING |
| OTHER | OTHER |

### Resn
| Constant | GEDCOM Value |
|----------|-------------|
| CONFIDENTIAL | CONFIDENTIAL |
| LOCKED | LOCKED |
| PRIVACY | PRIVACY |

### Role
| Constant | GEDCOM Value |
|----------|-------------|
| CHILD | CHIL |
| CLERGY | CLERGY |
| FATHER | FATH |
| FRIEND | FRIEND |
| GODPARENT | GODP |
| HUSBAND | HUSB |
| MOTHER | MOTH |
| MULTIPLE | MULT |
| NEIGHBOR | NGHBR |
| OFFICIATOR | OFFICIATOR |
| PARENT | PARENT |
| SPOUSE | SPOU |
| WIFE | WIFE |
| WITNESS | WITN |
| OTHER | OTHER |

### Medi
| Constant | GEDCOM Value |
|----------|-------------|
| AUDIO | AUDIO |
| BOOK | BOOK |
| CARD | CARD |
| ELECTRONIC | ELECTRONIC |
| FICHE | FICHE |
| FILM | FILM |
| MAGAZINE | MAGAZINE |
| MANUSCRIPT | MANUSCRIPT |
| MAP | MAP |
| NEWSPAPER | NEWSPAPER |
| PHOTO | PHOTO |
| TOMBSTONE | TOMBSTONE |
| VIDEO | VIDEO |
| OTHER | OTHER |

### Adop (adoption type within FAMC)
| Constant | GEDCOM Value |
|----------|-------------|
| HUSBAND | HUSB |
| WIFE | WIFE |
| BOTH | BOTH |

**Rationale**: Most enumeration values are already descriptive (BIRTH, ADOPTED, CONFIDENTIAL). Only Sex (M/F/X/U), Role (CHIL/FATH/MOTH/HUSB/etc.), and Adop (HUSB/WIFE) need descriptive expansion. Consistent naming pattern: always use the most readable form.

## R5: Package Placement

**Decision**: Place `GedcomTag` and `GedcomValue` in `org.gedcom7.parser` alongside `GedcomHandler`.

**Rationale**: Developers import `GedcomHandler` from this package; co-locating constants here means they discover them naturally. No need for a separate `util` or `constants` sub-package — that would add indirection for two simple classes.

**Alternatives considered**:
- `org.gedcom7.parser.constants`: adds a package for just 2 files, harder to discover
- `org.gedcom7.parser.util`: vague name, doesn't communicate purpose

## R6: Javadoc @see Mapping

**Decision**: Tag constants that hold parseable values link to their corresponding `GedcomDataTypes` method:

| Tag | @see Target |
|-----|------------|
| NAME (in INDI context) | `GedcomDataTypes#parsePersonalName(String)` |
| DATE | `GedcomDataTypes#parseDateValue(String)` |
| AGE | `GedcomDataTypes#parseAge(String)` |
| TIME | `GedcomDataTypes#parseTime(String)` |
| LATI, LONG | `GedcomDataTypes#parseCoordinate(String)` |

**Rationale**: These are the primary datatype parsers that developers need when they encounter these tags. Other tags (NAME in REPO, TITL, etc.) are plain strings with no dedicated parser.
