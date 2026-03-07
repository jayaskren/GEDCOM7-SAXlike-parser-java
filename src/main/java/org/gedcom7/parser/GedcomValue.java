package org.gedcom7.parser;

/**
 * String constants for standard GEDCOM 7 enumeration values.
 *
 * <p>Use these constants when comparing values delivered by the parser
 * for structures that have a fixed set of known values. This avoids
 * typos and provides IDE autocomplete for discovering valid options.
 *
 * <p>Example usage:
 * <pre>{@code
 * public void startStructure(int level, String xref, String tag,
 *                            String value, boolean isPointer) {
 *     if (GedcomTag.Indi.SEX.equals(tag)) {
 *         switch (value) {
 *             case GedcomValue.Sex.MALE:
 *                 // handle male
 *                 break;
 *             case GedcomValue.Sex.FEMALE:
 *                 // handle female
 *                 break;
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>All constants are {@code public static final String} and can be used
 * in {@code switch} statements and {@code equals()} comparisons.
 * Values not covered by these constants (e.g., extension values) can
 * still be compared using raw strings.
 */
public final class GedcomValue {

    private GedcomValue() {}

    // ─── Sex enumeration ─────────────────────────────────────

    /**
     * Biological sex values for the SEX structure.
     *
     * <p>Used when processing the value of a {@link GedcomTag.Indi#SEX} structure.
     */
    public static final class Sex {
        private Sex() {}

        /** Male. GEDCOM value: {@code "M"} */
        public static final String MALE = "M";
        /** Female. GEDCOM value: {@code "F"} */
        public static final String FEMALE = "F";
        /** Intersex or other non-binary designation. GEDCOM value: {@code "X"} */
        public static final String INTERSEX = "X";
        /** Unknown or not recorded. GEDCOM value: {@code "U"} */
        public static final String UNKNOWN = "U";
    }

    // ─── Name Type enumeration ───────────────────────────────

    /**
     * Name type values for the NAME.TYPE structure.
     *
     * <p>Describes the type or origin of a personal name.
     */
    public static final class NameType {
        private NameType() {}

        /** Birth name — the name given at birth. */
        public static final String BIRTH = "BIRTH";
        /** Immigrant name — name used upon immigration. */
        public static final String IMMIGRANT = "IMMIGRANT";
        /** Maiden name — surname before marriage. */
        public static final String MAIDEN = "MAIDEN";
        /** Married name — name acquired through marriage. */
        public static final String MARRIED = "MARRIED";
        /** Professional name — name used in a professional context. */
        public static final String PROFESSIONAL = "PROFESSIONAL";
        /** Other type of name not covered by the above categories. */
        public static final String OTHER = "OTHER";
    }

    // ─── Pedigree enumeration ────────────────────────────────

    /**
     * Pedigree values for the FAMC.PEDI structure.
     *
     * <p>Describes the relationship between a child and the family
     * they belong to (how the child entered the family).
     */
    public static final class Pedi {
        private Pedi() {}

        /** Biological child of both parents in the family. */
        public static final String BIRTH = "BIRTH";
        /** Child was adopted into the family. */
        public static final String ADOPTED = "ADOPTED";
        /** Child was a foster child in the family. */
        public static final String FOSTER = "FOSTER";
        /** Child was sealed to the family (LDS ordinance). */
        public static final String SEALING = "SEALING";
        /** Other relationship not covered by the above categories. */
        public static final String OTHER = "OTHER";
    }

    // ─── Restriction enumeration ─────────────────────────────

    /**
     * Restriction values for the RESN structure.
     *
     * <p>Indicates access restrictions on a record or structure.
     */
    public static final class Resn {
        private Resn() {}

        /** Data is confidential and should not be shared. */
        public static final String CONFIDENTIAL = "CONFIDENTIAL";
        /** Record is locked and should not be modified. */
        public static final String LOCKED = "LOCKED";
        /** Data has privacy restrictions. */
        public static final String PRIVACY = "PRIVACY";
    }

    // ─── Role enumeration ────────────────────────────────────

    /**
     * Role values for the ASSO.ROLE structure.
     *
     * <p>Describes the role an associated individual played in an event.
     */
    public static final class Role {
        private Role() {}

        /** Child involved in the event. GEDCOM value: {@code "CHIL"} */
        public static final String CHILD = "CHIL";
        /** Member of the clergy who officiated. */
        public static final String CLERGY = "CLERGY";
        /** Father of a principal in the event. GEDCOM value: {@code "FATH"} */
        public static final String FATHER = "FATH";
        /** Friend of a principal in the event. */
        public static final String FRIEND = "FRIEND";
        /** Godparent at a baptism or christening. GEDCOM value: {@code "GODP"} */
        public static final String GODPARENT = "GODP";
        /** Husband or male partner in the event. GEDCOM value: {@code "HUSB"} */
        public static final String HUSBAND = "HUSB";
        /** Mother of a principal in the event. GEDCOM value: {@code "MOTH"} */
        public static final String MOTHER = "MOTH";
        /** Multiple roles (person served in more than one capacity). GEDCOM value: {@code "MULT"} */
        public static final String MULTIPLE = "MULT";
        /** Neighbor of a principal in the event. GEDCOM value: {@code "NGHBR"} */
        public static final String NEIGHBOR = "NGHBR";
        /** Official who performed the event (e.g., judge, minister). */
        public static final String OFFICIATOR = "OFFICIATOR";
        /** Parent of a principal in the event. */
        public static final String PARENT = "PARENT";
        /** Spouse of a principal in the event. GEDCOM value: {@code "SPOU"} */
        public static final String SPOUSE = "SPOU";
        /** Wife or female partner in the event. */
        public static final String WIFE = "WIFE";
        /** Witness to the event. GEDCOM value: {@code "WITN"} */
        public static final String WITNESS = "WITN";
        /** Other role not covered by the above categories. */
        public static final String OTHER = "OTHER";
    }

    // ─── Media Type enumeration ──────────────────────────────

    /**
     * Media type values for the FORM.MEDI structure.
     *
     * <p>Describes the physical medium of a source or multimedia object.
     */
    public static final class Medi {
        private Medi() {}

        /** Audio recording. */
        public static final String AUDIO = "AUDIO";
        /** Bound book or publication. */
        public static final String BOOK = "BOOK";
        /** Index card or similar small document. */
        public static final String CARD = "CARD";
        /** Electronic or digital format. */
        public static final String ELECTRONIC = "ELECTRONIC";
        /** Microfiche. */
        public static final String FICHE = "FICHE";
        /** Microfilm. */
        public static final String FILM = "FILM";
        /** Magazine or periodical. */
        public static final String MAGAZINE = "MAGAZINE";
        /** Handwritten document or manuscript. */
        public static final String MANUSCRIPT = "MANUSCRIPT";
        /** Geographic map. */
        public static final String MAP = "MAP";
        /** Newspaper article or clipping. */
        public static final String NEWSPAPER = "NEWSPAPER";
        /** Photograph. */
        public static final String PHOTO = "PHOTO";
        /** Tombstone or grave marker. */
        public static final String TOMBSTONE = "TOMBSTONE";
        /** Video recording. */
        public static final String VIDEO = "VIDEO";
        /** Other media type not covered by the above categories. */
        public static final String OTHER = "OTHER";
    }

    // ─── Adoption type enumeration ───────────────────────────

    /**
     * Adoption type values for the ADOP.FAMC.ADOP structure.
     *
     * <p>Indicates which parent(s) in the family adopted the individual.
     */
    public static final class Adop {
        private Adop() {}

        /** Adopted by the husband/father. GEDCOM value: {@code "HUSB"} */
        public static final String HUSBAND = "HUSB";
        /** Adopted by the wife/mother. */
        public static final String WIFE = "WIFE";
        /** Adopted by both parents. */
        public static final String BOTH = "BOTH";
    }
}
