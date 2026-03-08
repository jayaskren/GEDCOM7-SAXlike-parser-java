# Research: Common Substructure Tag Constants

## Decision 1: Complete Tag Inventory

**Decision**: Add constants for all depth-2+ substructure tags defined in the GEDCOM 7 specification, organized by parent structure.

**Rationale**: The StructureDefinitions.java validation file and the GEDCOM 7 data files (substructures.tsv, cardinalities.tsv) define the authoritative list of valid parent-child relationships. Cross-referencing these against existing GedcomTag constants reveals ~35 tag values that lack constants.

**Alternatives considered**:
- Only add MAP/LATI/LONG as the user mentioned — rejected because it leaves the same gap problem for other tags.
- Add every possible tag regardless of depth — the current approach (depth 2+) is the right scope since level-1 record substructures are already covered.

## Decision 2: Nested Class Placement

**Decision**: Add new nested classes directly inside `GedcomTag` as peers to the existing record-type classes (Indi, Fam, etc.). New classes: `Plac`, `Map`, `Date`, `File`, `Form`, `Addr`, `Gedc`, `Name`, `Refn`, `Exid`, `Asso`, `Famc`, `Chan`, `Crea`, `SourCitation`, `Schma`, `Crop`.

**Rationale**: The user explicitly requested "a common area" for these tags. Placing them as top-level nested classes in GedcomTag makes them discoverable via `GedcomTag.Plac.MAP`, `GedcomTag.Map.LATI`, etc. This mirrors the existing pattern where `GedcomTag.Indi`, `GedcomTag.Fam` are top-level nested classes.

**Alternatives considered**:
- Nest under record-specific classes (e.g., `GedcomTag.Indi.Birt.Plac.MAP`) — rejected because PLAC, ADDR, DATE, etc. appear in many record contexts. Duplicating in each context would be excessive.
- Create a separate `GedcomCommonTag` class — rejected to maintain a single entry point for all tag constants.

## Decision 3: Handling Tags with Multiple Parent Contexts

**Decision**: Tags that appear as substructures in multiple parent contexts (e.g., FORM under both PLAC and FILE, TYPE under NAME and REFN) get a constant in each parent's nested class. Since all constants are `public static final String`, the duplicated string values compile to the same constant and cause no overhead.

**Rationale**: A developer processing PLAC.FORM should find it at `GedcomTag.Plac.FORM`, while a developer processing FILE.FORM should find it at `GedcomTag.File.FORM`. Both resolve to `"FORM"` — same pattern used for existing duplicates like NOTE, SOUR, UID across Indi, Fam, Sour, etc.

## Decision 4: Scope of New Classes

**Decision**: The following new nested classes will be added to GedcomTag:

| Class | Parent Structure | Constants |
|-------|-----------------|-----------|
| `Plac` | PLAC | MAP, FORM, LANG, TRAN, EXID, NOTE, SNOTE, SOUR |
| `Map` | MAP (child of PLAC) | LATI, LONG |
| `Date` | DATE | TIME, PHRASE |
| `File` | FILE (in OBJE) | FORM, TITL, TRAN |
| `Form` | FORM (in FILE) | MEDI |
| `Addr` | ADDR | ADR1, ADR2, ADR3, CITY, STAE, POST, CTRY |
| `Gedc` | GEDC (in HEAD) | VERS |
| `Name` | INDI-NAME | GIVN, SURN, NPFX, NSFX, SPFX, NICK, TYPE, TRAN, NOTE, SNOTE, SOUR |
| `Refn` | REFN | TYPE |
| `Exid` | EXID | TYPE |
| `Asso` | ASSO | ROLE, PHRASE, NOTE, SNOTE, SOUR |
| `Famc` | INDI-FAMC | PEDI, STAT, NOTE, SNOTE |
| `Chan` | CHAN | DATE, NOTE, SNOTE |
| `Crea` | CREA | DATE |
| `SourCitation` | SOUR (citation) | PAGE, DATA, EVEN, QUAY, NOTE, SNOTE, OBJE |
| `Schma` | SCHMA (in HEAD) | TAG |

**Rationale**: This covers all depth-2+ structures from the GEDCOM 7 specification. Structures with only 1-2 children are still included for completeness and discoverability.

## Decision 5: Naming Conventions

**Decision**: Follow existing GedcomTag naming conventions:
- Class names use PascalCase matching the GEDCOM tag (e.g., `Plac` not `Place`, `Addr` not `Address`)
- Exception: `SourCitation` distinguishes citation-level SOUR substructures from the record-level SOUR class already in GedcomTag
- Constant names use UPPER_SNAKE_CASE matching the GEDCOM tag exactly (e.g., `LATI`, `LONG`, `ADR1`)

**Rationale**: Consistency with existing code. GEDCOM tags are abbreviations (PLAC, ADDR, SOUR) and using the same abbreviation as the class name makes the mapping obvious.

## Decision 6: Existing Event Sub-nested Classes

**Decision**: Do NOT add Plac, Map, Date, etc. as sub-classes inside existing event classes (e.g., no `GedcomTag.Indi.Birt.Plac`). The developer navigates from event context to common context: `GedcomTag.Indi.Birt.PLAC` (the tag string) then `GedcomTag.Plac.MAP` (substructures of PLAC).

**Rationale**: PLAC appears in BIRT, DEAT, BURI, CHR, BAPM, MARR, DIV, ANUL, and EVEN — duplicating a Plac sub-class in each would be excessive. The common classes provide a single source. The @see Javadoc on PLAC constants can point to `GedcomTag.Plac` for discoverability.
