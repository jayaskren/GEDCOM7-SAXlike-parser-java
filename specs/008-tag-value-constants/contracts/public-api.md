# Public API Contract: GedcomTag and GedcomValue

## GedcomTag

Package: `org.gedcom7.parser`

### Record-level constants

```java
public final class GedcomTag {
    private GedcomTag() {}

    /** Individual record. Contains personal information about a person. */
    public static final String INDI = "INDI";
    /** Family record. Links partners and children. */
    public static final String FAM = "FAM";
    /** Source record. Describes a source of information. */
    public static final String SOUR = "SOUR";
    /** Repository record. An archive or institution holding sources. */
    public static final String REPO = "REPO";
    /** Multimedia object record. References a media file. */
    public static final String OBJE = "OBJE";
    /** Shared note record. A note referenced by multiple records. */
    public static final String SNOTE = "SNOTE";
    /** Submitter record. Identifies who submitted the data. */
    public static final String SUBM = "SUBM";
    /** Header. Contains file metadata. Always the first record. */
    public static final String HEAD = "HEAD";
    /** Trailer. Marks the end of the file. Always the last record. */
    public static final String TRLR = "TRLR";
    /** Inline note (not a record-level tag, but a common substructure). */
    public static final String NOTE = "NOTE";
}
```

### Nested class pattern (example: GedcomTag.Indi)

```java
public static final class Indi {
    private Indi() {}

    /** Personal name of the individual.
     * @see GedcomDataTypes#parsePersonalName(String) */
    public static final String NAME = "NAME";
    /** Biological sex of the individual. Value is one of GedcomValue.Sex constants. */
    public static final String SEX = "SEX";
    /** Birth event. Use GedcomTag.Indi.Birt for substructure tags. */
    public static final String BIRT = "BIRT";
    /** Death event. Use GedcomTag.Indi.Deat for substructure tags. */
    public static final String DEAT = "DEAT";
    // ... all INDI substructures from StructureDefinitions

    /** Substructures of a birth event within an individual record. */
    public static final class Birt {
        private Birt() {}
        /** Date of the event. @see GedcomDataTypes#parseDateValue(String) */
        public static final String DATE = "DATE";
        /** Place where the event occurred. */
        public static final String PLAC = "PLAC";
        /** Age of the individual at the time of the event. @see GedcomDataTypes#parseAge(String) */
        public static final String AGE = "AGE";
        // ... all BIRT substructures from StructureDefinitions
    }
}
```

### Contract guarantees

1. Every constant's value is a non-null String matching the GEDCOM 7 specification tag name
2. Constants are `public static final` — usable in switch/case statements
3. Class hierarchy is stable: record-level → record nested class → event nested class (max 3 levels)
4. Adding new constants in future versions is backward-compatible (no existing constants removed or renamed)

## GedcomValue

Package: `org.gedcom7.parser`

### Nested class pattern (example: GedcomValue.Sex)

```java
public final class GedcomValue {
    private GedcomValue() {}

    /** Biological sex enumeration values for the SEX structure. */
    public static final class Sex {
        private Sex() {}
        /** Male. GEDCOM value: "M" */
        public static final String MALE = "M";
        /** Female. GEDCOM value: "F" */
        public static final String FEMALE = "F";
        /** Intersex or other. GEDCOM value: "X" */
        public static final String INTERSEX = "X";
        /** Unknown sex. GEDCOM value: "U" */
        public static final String UNKNOWN = "U";
    }
}
```

### Contract guarantees

1. Every constant's value matches exactly what the parser delivers for that enumeration
2. Descriptive constant names map to GEDCOM codes (e.g., `MALE` → `"M"`)
3. Constants are `public static final` — usable in switch/case and equals() comparisons
4. Adding new enumeration values in future versions is backward-compatible
