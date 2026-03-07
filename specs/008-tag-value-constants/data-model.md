# Data Model: Tag and Value Constants

This feature has no persistent data model. It introduces two stateless utility classes containing only compile-time string constants. This document describes the class structure.

## GedcomTag

```
GedcomTag (final, private constructor)
├── Record-level constants: HEAD, TRLR, INDI, FAM, OBJE, SOUR, REPO, NOTE, SNOTE, SUBM
│
├── Indi (static class)
│   ├── Substructure constants: NAME, SEX, BIRT, DEAT, ADOP, BAPM, BARM, BASM,
│   │   BLES, BURI, CAST, CENS, CHR, CHRA, CONF, CREM, DSCR, EDUC, EMIG, EVEN,
│   │   FACT, FAMC, FAMS, FCOM, GRAD, IDNO, IMMI, NATI, NATU, NCHI, NMR, NO,
│   │   NOTE, OBJE, OCCU, ORDN, PROB, PROP, REFN, RELI, RESI, RESN, RETI, SLGC,
│   │   SNOTE, SOUR, SSN, SUBM, TITL, UID, WILL, ALIA, ANCI, ASSO, BAPL, CHAN,
│   │   CREA, DESI, EXID, CONL, ENDL, INIL, FCHR (if present)
│   │
│   ├── Birt (static class) — DATE, PLAC, ADDR, AGE, AGNC, CAUS, NOTE, OBJE, SOUR, ...
│   ├── Deat (static class) — DATE, PLAC, ADDR, AGE, AGNC, CAUS, NOTE, OBJE, SOUR, ...
│   ├── Buri (static class) — DATE, PLAC, ADDR, ...
│   ├── Chr (static class) — DATE, PLAC, ADDR, FAMC, ...
│   ├── Bapm (static class) — DATE, PLAC, ADDR, ...
│   ├── Marr (static class) — DATE, PLAC, ADDR, HUSB, WIFE, ...
│   ├── Div (static class) — DATE, PLAC, ADDR, ...
│   ├── Anul (static class) — DATE, PLAC, ADDR, ...
│   └── Even (static class) — DATE, PLAC, ADDR, TYPE, ...
│
├── Fam (static class)
│   ├── Substructure constants: HUSB, WIFE, CHIL, MARR, MARB, MARC, MARL, MARS,
│   │   ANUL, DIV, DIVF, ENGA, CENS, EVEN, FACT, NCHI, RESI, NO, NOTE, OBJE,
│   │   REFN, RESN, SLGS, SNOTE, SOUR, SUBM, UID, ASSO, CHAN, CREA, EXID
│   │
│   ├── Marr (static class) — DATE, PLAC, ADDR, HUSB, WIFE, ...
│   ├── Div (static class) — DATE, PLAC, ADDR, ...
│   ├── Anul (static class) — DATE, PLAC, ADDR, ...
│   └── Even (static class) — DATE, PLAC, ADDR, TYPE, ...
│
├── Sour (static class)
│   └── Substructure constants: ABBR, AUTH, DATA, NOTE, OBJE, PUBL, REFN, REPO,
│       TEXT, TITL, UID, CHAN, CREA, EXID, SNOTE
│
├── Repo (static class)
│   └── Substructure constants: NAME, ADDR, NOTE, REFN, SNOTE, UID, CHAN, CREA,
│       EXID, EMAIL, FAX, PHON, WWW
│
├── Obje (static class)
│   └── Substructure constants: FILE, NOTE, REFN, RESN, SNOTE, SOUR, UID, CHAN,
│       CREA, EXID
│
├── Snote (static class)
│   └── Substructure constants: LANG, MIME, REFN, SOUR, TRAN, UID, CHAN, CREA, EXID
│
├── Subm (static class)
│   └── Substructure constants: NAME, ADDR, LANG, NOTE, OBJE, REFN, SNOTE, UID,
│       CHAN, CREA, EXID, EMAIL, FAX, PHON, WWW
│
└── Head (static class)
    └── Substructure constants: COPR, DATE, DEST, GEDC, LANG, NOTE, PLAC, SCHMA,
        SNOTE, SOUR, SUBM
```

## GedcomValue

```
GedcomValue (final, private constructor)
├── Sex: MALE="M", FEMALE="F", INTERSEX="X", UNKNOWN="U"
├── NameType: BIRTH, IMMIGRANT, MAIDEN, MARRIED, PROFESSIONAL, OTHER
├── Pedi: BIRTH, ADOPTED, FOSTER, SEALING, OTHER
├── Resn: CONFIDENTIAL, LOCKED, PRIVACY
├── Role: CHILD="CHIL", CLERGY, FATHER="FATH", FRIEND, GODPARENT="GODP",
│         HUSBAND="HUSB", MOTHER="MOTH", MULTIPLE="MULT", NEIGHBOR="NGHBR",
│         OFFICIATOR, PARENT, SPOUSE="SPOU", WIFE, WITNESS="WITN", OTHER
├── Medi: AUDIO, BOOK, CARD, ELECTRONIC, FICHE, FILM, MAGAZINE, MANUSCRIPT,
│         MAP, NEWSPAPER, PHOTO, TOMBSTONE, VIDEO, OTHER
└── Adop: HUSBAND="HUSB", WIFE, BOTH
```

## Validation Rules

- All constants are non-null `public static final String`
- String values must exactly match GEDCOM 7 specification values
- No two constants in the same class may have the same name (compile-enforced)
- Classes are final with private constructors (non-instantiable)

## Relationships

- `GedcomTag` constants are used by developers in `GedcomHandler` callback implementations
- `GedcomValue` constants are used when comparing values delivered by the parser
- Neither class has runtime coupling to the parser — they are pure compile-time references
- Javadoc `@see` links connect tag constants to `GedcomDataTypes` parser methods (documentation-only relationship)
