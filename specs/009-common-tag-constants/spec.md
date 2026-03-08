# Feature Specification: Common Substructure Tag Constants

**Feature Branch**: `009-common-tag-constants`
**Created**: 2026-03-07
**Status**: Draft
**Input**: User description: "For things like MAP, LATI, and LONG, that don't have a constant. Create a common area for those to live please"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Core Common Substructure Constants (Priority: P1)

A developer parsing GEDCOM structures needs constants for the most commonly used depth-2+ substructure tags. Currently, no constants exist for tags like MAP, LATI, LONG, TIME, FORM, MEDI, VERS, or address components — they must use raw string literals. The developer wants constants organized in nested classes that reflect the GEDCOM structural context (e.g., LATI and LONG inside a Map class, CITY and STAE inside an Addr class).

This story covers the core common classes: Plac (MAP, FORM, LANG, TRAN, EXID, NOTE, SNOTE, SOUR), Map (LATI, LONG), Date (TIME, PHRASE), Addr (ADR1, ADR2, ADR3, CITY, STAE, POST, CTRY), File (FORM, TITL, TRAN), Form (MEDI), and Gedc (VERS).

**Why this priority**: These are the most frequently needed deep substructure tags, including the MAP/LATI/LONG tags the user specifically called out. Place coordinates, dates with times, addresses, and file metadata are core genealogy data.

**Independent Test**: Can be tested by verifying that constants exist for all tags in the 7 core classes, resolve to the correct string values, and work in switch statements.

**Acceptance Scenarios**:

1. **Given** a developer processing PLAC substructures, **When** they type `GedcomTag.Plac.` in their IDE, **Then** they can discover and use a constant for MAP without resorting to a string literal.
2. **Given** a developer processing MAP substructures, **When** they type `GedcomTag.Map.`, **Then** they can discover LATI and LONG constants.
3. **Given** a developer processing ADDR substructures, **When** they look for address component constants (CITY, STAE, POST, CTRY), **Then** they find them organized under `GedcomTag.Addr`.
4. **Given** a developer using these constants in a switch statement, **When** the code compiles, **Then** the constants are usable as case labels (compile-time constants).

---

### User Story 2 - Remaining Common Substructure Constants (Priority: P2)

Beyond the core classes, many other depth-2+ structures need constants. These include personal name parts (GIVN, SURN, NICK, NPFX, NSFX, SPFX), association details (ROLE, PHRASE), family-child link attributes (PEDI, STAT), change/creation timestamps (DATE), source citation details (PAGE, QUAY, DATA, EVEN), reference number types (TYPE), external identifier types (TYPE), and schema definitions (TAG).

This story covers the remaining classes: Name, Refn, Exid, Asso, Famc, Chan, Crea, SourCitation, and Schma.

**Why this priority**: Completes the constant coverage for all depth-2+ substructure tags, ensuring no gaps that force developers back to string literals.

**Independent Test**: Can be tested by verifying that constants exist for all tags in the 9 remaining classes, resolve to correct values, and are discoverable via their parent structure's nested class.

**Acceptance Scenarios**:

1. **Given** a developer processing NAME substructures, **When** they look for name part constants (GIVN, SURN), **Then** they find them organized under `GedcomTag.Name`.
2. **Given** a developer processing SOUR citation substructures, **When** they look for PAGE and QUAY constants, **Then** they find them under `GedcomTag.SourCitation`.
3. **Given** a developer processing ASSO substructures, **When** they look for ROLE, **Then** they find it under `GedcomTag.Asso`.

---

### User Story 3 - Documentation and Discoverability (Priority: P3)

A developer new to the library reads the documentation and wants to know what constants are available for deep substructure tags. The documentation and Javadoc should make it clear where to find constants for structures like PLAC.MAP.LATI, DATE.TIME, FILE.FORM.MEDI, etc.

**Why this priority**: Constants that exist but can't be found are nearly as useless as no constants at all. Documentation ensures adoption.

**Independent Test**: Can be tested by reviewing Javadoc and documentation for completeness and accuracy of the new constant classes.

**Acceptance Scenarios**:

1. **Given** a developer reading the Javadoc for the new constant classes, **When** they browse the nested class hierarchy, **Then** each class documents what GEDCOM structure it represents and lists all available constants.
2. **Given** a developer reading the tutorial, **When** they look at examples involving place coordinates or dates with times, **Then** examples use the new constants rather than string literals.

---

### Edge Cases

- What happens when a tag name exists at multiple nesting depths (e.g., TYPE appears under NAME, REFN, EXID, and events)? Constants should be available in each relevant context.
- What happens when a tag like FORM appears in different parent contexts (PLAC.FORM vs FILE.FORM)? Both contexts should have the constant, even though the string value is the same.
- What about tags that already exist as level-1 constants in record classes (e.g., NOTE, SOUR)? These should not be duplicated in a way that creates confusion — existing constants remain as-is.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The library MUST provide constants for all GEDCOM 7 substructure tags that appear at nesting depth 2 or deeper and do not currently have constants. This includes at minimum: MAP, LATI, LONG, TIME, FORM, MEDI, VERS, TRAN, MIME, ADR1, ADR2, ADR3, CITY, STAE, POST, CTRY, and TYPE (in contexts where it lacks a constant).
- **FR-002**: Constants MUST be organized in nested classes that reflect their GEDCOM structural context (e.g., LATI and LONG are children of MAP, which is a child of PLAC).
- **FR-003**: All new constants MUST be `public static final String` values usable in switch statements and equals() comparisons.
- **FR-004**: New constants MUST NOT break any existing constants or change the existing class hierarchy. All existing code using `GedcomTag` or `GedcomValue` must continue to compile and work identically.
- **FR-005**: Constants for tags that appear in multiple parent contexts (e.g., TYPE under NAME vs TYPE under REFN) MUST be available in each relevant parent's nested class.
- **FR-006**: Each new constant MUST have Javadoc describing the GEDCOM structure it represents.
- **FR-007**: Constants for address components (ADR1, ADR2, ADR3, CITY, STAE, POST, CTRY) MUST be available under an address-related context, since ADDR appears as a substructure in multiple record types.

### Key Entities

**Core classes (US1):**

- **Plac (nested class)**: Represents substructures of PLAC — includes MAP, FORM, LANG, TRAN, EXID, NOTE, SNOTE, SOUR.
- **Map (nested class)**: Represents substructures of MAP — includes LATI, LONG.
- **Date (nested class)**: Represents substructures of DATE — includes TIME, PHRASE.
- **File (nested class)**: Represents substructures of FILE (in OBJE) — includes FORM, TITL, TRAN.
- **Form (nested class)**: Represents substructures of FORM — includes MEDI.
- **Addr (nested class)**: Represents substructures of ADDR — includes ADR1, ADR2, ADR3, CITY, STAE, POST, CTRY.
- **Gedc (nested class)**: Represents substructures of GEDC — includes VERS.

**Remaining classes (US2):**

- **Name (nested class)**: Represents substructures of personal NAME — includes GIVN, SURN, NPFX, NSFX, SPFX, NICK, TYPE, TRAN, NOTE, SNOTE, SOUR.
- **Refn (nested class)**: Represents substructures of REFN — includes TYPE.
- **Exid (nested class)**: Represents substructures of EXID — includes TYPE.
- **Asso (nested class)**: Represents substructures of ASSO — includes ROLE, PHRASE, NOTE, SNOTE, SOUR.
- **Famc (nested class)**: Represents substructures of INDI-FAMC — includes PEDI, STAT, NOTE, SNOTE.
- **Chan (nested class)**: Represents substructures of CHAN — includes DATE, NOTE, SNOTE.
- **Crea (nested class)**: Represents substructures of CREA — includes DATE.
- **SourCitation (nested class)**: Represents substructures of SOUR citation — includes PAGE, DATA, EVEN, QUAY, NOTE, SNOTE, OBJE.
- **Schma (nested class)**: Represents substructures of SCHMA — includes TAG.

Note: Tags like MIME and LANG that appear under inline NOTE already have constants in `GedcomTag.Snote` (same string values). Since inline NOTE and shared note (SNOTE) share the same child tags, developers can use `GedcomTag.Snote.MIME` and `GedcomTag.Snote.LANG` for either context.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of GEDCOM 7 substructure tags at depth 2+ have corresponding constants (verified against the GEDCOM 7 specification tag list).
- **SC-002**: All new constants are discoverable via IDE autocomplete within 2 levels of typing from `GedcomTag` (e.g., `GedcomTag.Map.LATI`, `GedcomTag.Addr.CITY`).
- **SC-003**: All new constants resolve to the correct GEDCOM tag string values (verified by unit tests).
- **SC-004**: Zero compilation errors when building existing code after the addition (backward compatibility maintained).
- **SC-005**: Tutorial and example code updated to use new constants — zero raw string literals for tags that have constants.

## Assumptions

- The 16 new nested classes for shared substructures (Plac, Map, Date, File, Form, Addr, Gedc, Name, Refn, Exid, Asso, Famc, Chan, Crea, SourCitation, Schma) will be added directly inside `GedcomTag` as top-level nested classes, similar to how Indi, Fam, Sour, etc. are organized. This follows the same pattern the library already uses.
- Tags that appear as level-1 substructures of records (already covered by Indi, Fam, etc.) do not need to be duplicated in the new common classes. The new classes cover depth-2+ tags only.
- The GEDCOM 7.0 specification is the authoritative source for which substructure tags exist and their valid parent contexts.
