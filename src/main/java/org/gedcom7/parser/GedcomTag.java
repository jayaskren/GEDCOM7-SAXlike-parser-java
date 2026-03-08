package org.gedcom7.parser;

import org.gedcom7.parser.datatype.GedcomDataTypes;

/**
 * String constants for standard GEDCOM 7 tags.
 *
 * <p>Use these constants in {@link GedcomHandler} callbacks instead of
 * string literals to get IDE autocomplete and avoid typos. Constants are
 * organized by record type via nested static classes.
 *
 * <p>Example usage in a handler:
 * <pre>{@code
 * public void startRecord(int level, String xref, String tag) {
 *     switch (tag) {
 *         case GedcomTag.INDI:
 *             // process individual record
 *             break;
 *         case GedcomTag.FAM:
 *             // process family record
 *             break;
 *     }
 * }
 *
 * public void startStructure(int level, String xref, String tag,
 *                            String value, boolean isPointer) {
 *     switch (tag) {
 *         case GedcomTag.Indi.NAME:
 *             // process individual name
 *             break;
 *         case GedcomTag.Indi.BIRT:
 *             // process birth event
 *             break;
 *     }
 * }
 * }</pre>
 *
 * <p>All constants are {@code public static final String} and can be used
 * in {@code switch} statements and {@code equals()} comparisons. Extension
 * tags (underscore-prefixed) are not covered; use raw string comparison
 * for those.
 */
public final class GedcomTag {

    private GedcomTag() {}

    // ─── Record-level tags (level 0) ─────────────────────────

    /** Header record. Contains file metadata. Always the first record in a GEDCOM file. */
    public static final String HEAD = "HEAD";

    /** Trailer record. Marks the end of a GEDCOM file. Always the last record. */
    public static final String TRLR = "TRLR";

    /** Individual record. Contains personal information about a person. */
    public static final String INDI = "INDI";

    /** Family record. Links partners and children into a family group. */
    public static final String FAM = "FAM";

    /** Multimedia object record. References a media file (photo, document, etc.). */
    public static final String OBJE = "OBJE";

    /** Source record. Describes a source of genealogical information. */
    public static final String SOUR = "SOUR";

    /** Repository record. An archive or institution holding source materials. */
    public static final String REPO = "REPO";

    /** Inline note. A note attached directly to a structure (not shared). */
    public static final String NOTE = "NOTE";

    /** Shared note record. A note that can be referenced by multiple records. */
    public static final String SNOTE = "SNOTE";

    /** Submitter record. Identifies who submitted the genealogical data. */
    public static final String SUBM = "SUBM";

    // ─── Individual record substructures ─────────────────────

    /**
     * Substructure tags valid inside an Individual (INDI) record.
     *
     * <p>Use these constants when processing structures at level 1+
     * inside an INDI record. For event substructures (DATE, PLAC, etc.),
     * see the nested event classes like {@link Birt}, {@link Deat}, etc.
     */
    public static final class Indi {
        private Indi() {}

        /** Personal name of the individual.
         * @see GedcomDataTypes#parsePersonalName(String) */
        public static final String NAME = "NAME";
        /** Biological sex. Compare value against {@link GedcomValue.Sex} constants. */
        public static final String SEX = "SEX";
        /** Birth event. For substructures, see {@link Birt}. */
        public static final String BIRT = "BIRT";
        /** Death event. For substructures, see {@link Deat}. */
        public static final String DEAT = "DEAT";
        /** Adoption event. */
        public static final String ADOP = "ADOP";
        /** Baptism event (not LDS). For substructures, see {@link Bapm}. */
        public static final String BAPM = "BAPM";
        /** Bar Mitzvah event. */
        public static final String BARM = "BARM";
        /** Bas Mitzvah event. */
        public static final String BASM = "BASM";
        /** Blessing event. */
        public static final String BLES = "BLES";
        /** Burial event. For substructures, see {@link Buri}. */
        public static final String BURI = "BURI";
        /** Caste or social status attribute. */
        public static final String CAST = "CAST";
        /** Census event. */
        public static final String CENS = "CENS";
        /** Christening event. For substructures, see {@link Chr}. */
        public static final String CHR = "CHR";
        /** Adult christening event. */
        public static final String CHRA = "CHRA";
        /** Confirmation event. */
        public static final String CONF = "CONF";
        /** LDS confirmation ordinance. */
        public static final String CONL = "CONL";
        /** Cremation event. */
        public static final String CREM = "CREM";
        /** Physical description attribute. */
        public static final String DSCR = "DSCR";
        /** Education attribute. */
        public static final String EDUC = "EDUC";
        /** Emigration event. */
        public static final String EMIG = "EMIG";
        /** LDS endowment ordinance. */
        public static final String ENDL = "ENDL";
        /** Generic event. For substructures, see {@link Even}. */
        public static final String EVEN = "EVEN";
        /** Generic fact or attribute. */
        public static final String FACT = "FACT";
        /** Family in which this individual is a child. */
        public static final String FAMC = "FAMC";
        /** Family in which this individual is a spouse. */
        public static final String FAMS = "FAMS";
        /** First communion event. */
        public static final String FCOM = "FCOM";
        /** Graduation event. */
        public static final String GRAD = "GRAD";
        /** National ID number attribute. */
        public static final String IDNO = "IDNO";
        /** Immigration event. */
        public static final String IMMI = "IMMI";
        /** LDS initiatory ordinance. */
        public static final String INIL = "INIL";
        /** Nationality or tribal origin attribute. */
        public static final String NATI = "NATI";
        /** Naturalization event. */
        public static final String NATU = "NATU";
        /** Count of children attribute. */
        public static final String NCHI = "NCHI";
        /** Count of marriages attribute. */
        public static final String NMR = "NMR";
        /** Negative assertion — event known not to have occurred. */
        public static final String NO = "NO";
        /** Note attached to this individual. */
        public static final String NOTE = "NOTE";
        /** Multimedia object linked to this individual. */
        public static final String OBJE = "OBJE";
        /** Occupation attribute. */
        public static final String OCCU = "OCCU";
        /** Ordination event. */
        public static final String ORDN = "ORDN";
        /** Probate event. */
        public static final String PROB = "PROB";
        /** Property ownership attribute. */
        public static final String PROP = "PROP";
        /** User-defined reference number. */
        public static final String REFN = "REFN";
        /** Religious affiliation attribute. */
        public static final String RELI = "RELI";
        /** Residence attribute. */
        public static final String RESI = "RESI";
        /** Restriction notice. Compare value against {@link GedcomValue.Resn} constants. */
        public static final String RESN = "RESN";
        /** Retirement event. */
        public static final String RETI = "RETI";
        /** LDS sealing to parents ordinance. */
        public static final String SLGC = "SLGC";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Source citation. */
        public static final String SOUR = "SOUR";
        /** Social Security Number attribute. */
        public static final String SSN = "SSN";
        /** Submitter reference. */
        public static final String SUBM = "SUBM";
        /** Nobility title or descriptive title attribute. */
        public static final String TITL = "TITL";
        /** Unique identifier. */
        public static final String UID = "UID";
        /** Will or testament event. */
        public static final String WILL = "WILL";
        /** Alias — pointer to another individual record. */
        public static final String ALIA = "ALIA";
        /** Ancestor interest indicator. */
        public static final String ANCI = "ANCI";
        /** Association — link to another individual with a role. */
        public static final String ASSO = "ASSO";
        /** LDS baptism ordinance. */
        public static final String BAPL = "BAPL";
        /** Change date — when this record was last modified. */
        public static final String CHAN = "CHAN";
        /** Creation date — when this record was created. */
        public static final String CREA = "CREA";
        /** Descendant interest indicator. */
        public static final String DESI = "DESI";
        /** External identifier (e.g., from another system). */
        public static final String EXID = "EXID";

        // ─── Birth event substructures ───────────────────────

        /** Substructure tags valid inside a Birth (BIRT) event. */
        public static final class Birt {
            private Birt() {}
            /** Date of the birth.
             * @see GedcomDataTypes#parseDateValue(String)
             * @see Date */
            public static final String DATE = "DATE";
            /** Place where the birth occurred.
             * @see Plac */
            public static final String PLAC = "PLAC";
            /** Address where the birth occurred.
             * @see Addr */
            public static final String ADDR = "ADDR";
            /** Age of the individual at the time of birth (typically for parent's age).
             * @see GedcomDataTypes#parseAge(String) */
            public static final String AGE = "AGE";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause of the event. */
            public static final String CAUS = "CAUS";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
            /** Family in which this individual is a child (for birth context). */
            public static final String FAMC = "FAMC";
        }

        // ─── Death event substructures ───────────────────────

        /** Substructure tags valid inside a Death (DEAT) event. */
        public static final class Deat {
            private Deat() {}
            /** Date of the death.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the death occurred. */
            public static final String PLAC = "PLAC";
            /** Address where the death occurred. */
            public static final String ADDR = "ADDR";
            /** Age of the individual at the time of death.
             * @see GedcomDataTypes#parseAge(String) */
            public static final String AGE = "AGE";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause of death. */
            public static final String CAUS = "CAUS";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }

        // ─── Burial event substructures ──────────────────────

        /** Substructure tags valid inside a Burial (BURI) event. */
        public static final class Buri {
            private Buri() {}
            /** Date of the burial.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the burial occurred. */
            public static final String PLAC = "PLAC";
            /** Address of the burial location. */
            public static final String ADDR = "ADDR";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause associated with the burial. */
            public static final String CAUS = "CAUS";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }

        // ─── Christening event substructures ─────────────────

        /** Substructure tags valid inside a Christening (CHR) event. */
        public static final class Chr {
            private Chr() {}
            /** Date of the christening.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the christening occurred. */
            public static final String PLAC = "PLAC";
            /** Address of the christening location. */
            public static final String ADDR = "ADDR";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
            /** Family in which this individual is a child (for christening context). */
            public static final String FAMC = "FAMC";
        }

        // ─── Baptism event substructures ─────────────────────

        /** Substructure tags valid inside a Baptism (BAPM) event. */
        public static final class Bapm {
            private Bapm() {}
            /** Date of the baptism.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the baptism occurred. */
            public static final String PLAC = "PLAC";
            /** Address of the baptism location. */
            public static final String ADDR = "ADDR";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }

        // ─── Generic event substructures (INDI context) ──────

        /** Substructure tags valid inside a generic Event (EVEN) in an individual record. */
        public static final class Even {
            private Even() {}
            /** Date of the event.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the event occurred. */
            public static final String PLAC = "PLAC";
            /** Address of the event location. */
            public static final String ADDR = "ADDR";
            /** Age of the individual at the time of the event.
             * @see GedcomDataTypes#parseAge(String) */
            public static final String AGE = "AGE";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause of the event. */
            public static final String CAUS = "CAUS";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor (required for EVEN — describes what the event is). */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }
    }

    // ─── Family record substructures ─────────────────────────

    /**
     * Substructure tags valid inside a Family (FAM) record.
     *
     * <p>Use these constants when processing structures at level 1+
     * inside a FAM record.
     */
    public static final class Fam {
        private Fam() {}

        /** Husband/partner in the family. Value is a pointer to an INDI record. */
        public static final String HUSB = "HUSB";
        /** Wife/partner in the family. Value is a pointer to an INDI record. */
        public static final String WIFE = "WIFE";
        /** Child in the family. Value is a pointer to an INDI record. */
        public static final String CHIL = "CHIL";
        /** Marriage event. For substructures, see {@link Marr}. */
        public static final String MARR = "MARR";
        /** Marriage banns event. */
        public static final String MARB = "MARB";
        /** Marriage contract event. */
        public static final String MARC = "MARC";
        /** Marriage license event. */
        public static final String MARL = "MARL";
        /** Marriage settlement event. */
        public static final String MARS = "MARS";
        /** Annulment event. For substructures, see {@link Anul}. */
        public static final String ANUL = "ANUL";
        /** Divorce event. For substructures, see {@link Div}. */
        public static final String DIV = "DIV";
        /** Divorce filed event. */
        public static final String DIVF = "DIVF";
        /** Engagement event. */
        public static final String ENGA = "ENGA";
        /** Census event. */
        public static final String CENS = "CENS";
        /** Generic event. For substructures, see {@link Even}. */
        public static final String EVEN = "EVEN";
        /** Generic fact or attribute. */
        public static final String FACT = "FACT";
        /** Count of children. */
        public static final String NCHI = "NCHI";
        /** Residence attribute. */
        public static final String RESI = "RESI";
        /** Negative assertion — event known not to have occurred. */
        public static final String NO = "NO";
        /** Note attached to this family. */
        public static final String NOTE = "NOTE";
        /** Multimedia object linked to this family. */
        public static final String OBJE = "OBJE";
        /** User-defined reference number. */
        public static final String REFN = "REFN";
        /** Restriction notice. Compare value against {@link GedcomValue.Resn} constants. */
        public static final String RESN = "RESN";
        /** LDS sealing to spouse ordinance. */
        public static final String SLGS = "SLGS";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Source citation. */
        public static final String SOUR = "SOUR";
        /** Submitter reference. */
        public static final String SUBM = "SUBM";
        /** Unique identifier. */
        public static final String UID = "UID";
        /** Association to another individual. */
        public static final String ASSO = "ASSO";
        /** Change date — when this record was last modified. */
        public static final String CHAN = "CHAN";
        /** Creation date — when this record was created. */
        public static final String CREA = "CREA";
        /** External identifier. */
        public static final String EXID = "EXID";

        // ─── Marriage event substructures ────────────────────

        /** Substructure tags valid inside a Marriage (MARR) event. */
        public static final class Marr {
            private Marr() {}
            /** Date of the marriage.
             * @see GedcomDataTypes#parseDateValue(String)
             * @see Date */
            public static final String DATE = "DATE";
            /** Place where the marriage occurred.
             * @see Plac */
            public static final String PLAC = "PLAC";
            /** Address of the marriage location.
             * @see Addr */
            public static final String ADDR = "ADDR";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause associated with the event. */
            public static final String CAUS = "CAUS";
            /** Husband's age at the time of marriage. */
            public static final String HUSB = "HUSB";
            /** Wife's age at the time of marriage. */
            public static final String WIFE = "WIFE";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }

        // ─── Divorce event substructures ─────────────────────

        /** Substructure tags valid inside a Divorce (DIV) event. */
        public static final class Div {
            private Div() {}
            /** Date of the divorce.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the divorce was finalized. */
            public static final String PLAC = "PLAC";
            /** Address associated with the divorce. */
            public static final String ADDR = "ADDR";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause associated with the event. */
            public static final String CAUS = "CAUS";
            /** Husband's age at the time of divorce. */
            public static final String HUSB = "HUSB";
            /** Wife's age at the time of divorce. */
            public static final String WIFE = "WIFE";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }

        // ─── Annulment event substructures ───────────────────

        /** Substructure tags valid inside an Annulment (ANUL) event. */
        public static final class Anul {
            private Anul() {}
            /** Date of the annulment.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the annulment was finalized. */
            public static final String PLAC = "PLAC";
            /** Address associated with the annulment. */
            public static final String ADDR = "ADDR";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause associated with the event. */
            public static final String CAUS = "CAUS";
            /** Husband's age at the time of annulment. */
            public static final String HUSB = "HUSB";
            /** Wife's age at the time of annulment. */
            public static final String WIFE = "WIFE";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor. */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }

        // ─── Generic event substructures (FAM context) ───────

        /** Substructure tags valid inside a generic Event (EVEN) in a family record. */
        public static final class Even {
            private Even() {}
            /** Date of the event.
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the event occurred. */
            public static final String PLAC = "PLAC";
            /** Address of the event location. */
            public static final String ADDR = "ADDR";
            /** Responsible agency or institution. */
            public static final String AGNC = "AGNC";
            /** Cause associated with the event. */
            public static final String CAUS = "CAUS";
            /** Husband's age at the time of the event. */
            public static final String HUSB = "HUSB";
            /** Wife's age at the time of the event. */
            public static final String WIFE = "WIFE";
            /** Note attached to this event. */
            public static final String NOTE = "NOTE";
            /** Multimedia object linked to this event. */
            public static final String OBJE = "OBJE";
            /** Phone number at the event location. */
            public static final String PHON = "PHON";
            /** Religious affiliation for this event. */
            public static final String RELI = "RELI";
            /** Restriction notice. */
            public static final String RESN = "RESN";
            /** Sort date for ordering events. */
            public static final String SDATE = "SDATE";
            /** Shared note reference. */
            public static final String SNOTE = "SNOTE";
            /** Source citation. */
            public static final String SOUR = "SOUR";
            /** Event type descriptor (required for EVEN — describes what the event is). */
            public static final String TYPE = "TYPE";
            /** Unique identifier. */
            public static final String UID = "UID";
            /** Association to another individual. */
            public static final String ASSO = "ASSO";
        }
    }

    // ─── Source record substructures ─────────────────────────

    /**
     * Substructure tags valid inside a Source (SOUR) record.
     */
    public static final class Sour {
        private Sour() {}

        /** Abbreviation for the source title. */
        public static final String ABBR = "ABBR";
        /** Author of the source. */
        public static final String AUTH = "AUTH";
        /** Data from the source. */
        public static final String DATA = "DATA";
        /** Note attached to this source. */
        public static final String NOTE = "NOTE";
        /** Multimedia object linked to this source. */
        public static final String OBJE = "OBJE";
        /** Publication facts about the source. */
        public static final String PUBL = "PUBL";
        /** User-defined reference number. */
        public static final String REFN = "REFN";
        /** Repository where the source is held. */
        public static final String REPO = "REPO";
        /** Text from the source. */
        public static final String TEXT = "TEXT";
        /** Title of the source. */
        public static final String TITL = "TITL";
        /** Unique identifier. */
        public static final String UID = "UID";
        /** Change date — when this record was last modified. */
        public static final String CHAN = "CHAN";
        /** Creation date — when this record was created. */
        public static final String CREA = "CREA";
        /** External identifier. */
        public static final String EXID = "EXID";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
    }

    // ─── Repository record substructures ─────────────────────

    /**
     * Substructure tags valid inside a Repository (REPO) record.
     */
    public static final class Repo {
        private Repo() {}

        /** Name of the repository. */
        public static final String NAME = "NAME";
        /** Mailing address or physical location. */
        public static final String ADDR = "ADDR";
        /** Note attached to this repository. */
        public static final String NOTE = "NOTE";
        /** User-defined reference number. */
        public static final String REFN = "REFN";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Unique identifier. */
        public static final String UID = "UID";
        /** Change date — when this record was last modified. */
        public static final String CHAN = "CHAN";
        /** Creation date — when this record was created. */
        public static final String CREA = "CREA";
        /** External identifier. */
        public static final String EXID = "EXID";
        /** Email address. */
        public static final String EMAIL = "EMAIL";
        /** Fax number. */
        public static final String FAX = "FAX";
        /** Phone number. */
        public static final String PHON = "PHON";
        /** Web address (URL). */
        public static final String WWW = "WWW";
    }

    // ─── Multimedia object record substructures ──────────────

    /**
     * Substructure tags valid inside a Multimedia Object (OBJE) record.
     */
    public static final class Obje {
        private Obje() {}

        /** File reference — path or URL to the media file. */
        public static final String FILE = "FILE";
        /** Note attached to this object. */
        public static final String NOTE = "NOTE";
        /** User-defined reference number. */
        public static final String REFN = "REFN";
        /** Restriction notice. Compare value against {@link GedcomValue.Resn} constants. */
        public static final String RESN = "RESN";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Source citation. */
        public static final String SOUR = "SOUR";
        /** Unique identifier. */
        public static final String UID = "UID";
        /** Change date — when this record was last modified. */
        public static final String CHAN = "CHAN";
        /** Creation date — when this record was created. */
        public static final String CREA = "CREA";
        /** External identifier. */
        public static final String EXID = "EXID";
    }

    // ─── Shared note record substructures ────────────────────

    /**
     * Substructure tags valid inside a Shared Note (SNOTE) record.
     */
    public static final class Snote {
        private Snote() {}

        /** Language of the note text. */
        public static final String LANG = "LANG";
        /** MIME type of the note content. */
        public static final String MIME = "MIME";
        /** User-defined reference number. */
        public static final String REFN = "REFN";
        /** Source citation. */
        public static final String SOUR = "SOUR";
        /** Translation of the note into another language. */
        public static final String TRAN = "TRAN";
        /** Unique identifier. */
        public static final String UID = "UID";
        /** Change date — when this record was last modified. */
        public static final String CHAN = "CHAN";
        /** Creation date — when this record was created. */
        public static final String CREA = "CREA";
        /** External identifier. */
        public static final String EXID = "EXID";
    }

    // ─── Submitter record substructures ──────────────────────

    /**
     * Substructure tags valid inside a Submitter (SUBM) record.
     */
    public static final class Subm {
        private Subm() {}

        /** Name of the submitter. */
        public static final String NAME = "NAME";
        /** Mailing address or physical location. */
        public static final String ADDR = "ADDR";
        /** Preferred language for communication. */
        public static final String LANG = "LANG";
        /** Note attached to this submitter. */
        public static final String NOTE = "NOTE";
        /** Multimedia object linked to this submitter. */
        public static final String OBJE = "OBJE";
        /** User-defined reference number. */
        public static final String REFN = "REFN";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Unique identifier. */
        public static final String UID = "UID";
        /** Change date — when this record was last modified. */
        public static final String CHAN = "CHAN";
        /** Creation date — when this record was created. */
        public static final String CREA = "CREA";
        /** External identifier. */
        public static final String EXID = "EXID";
        /** Email address. */
        public static final String EMAIL = "EMAIL";
        /** Fax number. */
        public static final String FAX = "FAX";
        /** Phone number. */
        public static final String PHON = "PHON";
        /** Web address (URL). */
        public static final String WWW = "WWW";
    }

    // ─── Header record substructures ─────────────────────────

    /**
     * Substructure tags valid inside the Header (HEAD) record.
     */
    public static final class Head {
        private Head() {}

        /** Copyright statement. */
        public static final String COPR = "COPR";
        /** Date the file was created or last modified.
         * @see GedcomDataTypes#parseDateValue(String) */
        public static final String DATE = "DATE";
        /** Receiving system identifier. */
        public static final String DEST = "DEST";
        /** GEDCOM version information container. */
        public static final String GEDC = "GEDC";
        /** Default language for the file. */
        public static final String LANG = "LANG";
        /** Note attached to the header. */
        public static final String NOTE = "NOTE";
        /** Default place hierarchy format. */
        public static final String PLAC = "PLAC";
        /** Schema definitions for extension tags. */
        public static final String SCHMA = "SCHMA";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Source system that produced this file. */
        public static final String SOUR = "SOUR";
        /** Submitter of the file. */
        public static final String SUBM = "SUBM";
    }

    // ─── Common substructure tag constants ───────────────────

    // ─── Place (PLAC) substructures ─────────────────────────

    /**
     * Substructure tags valid inside a Place (PLAC) structure.
     *
     * <p>A PLAC structure appears as a substructure of events such as
     * {@link Indi.Birt#PLAC} or {@link Fam.Marr#PLAC}.
     */
    public static final class Plac {
        private Plac() {}

        /** Geographic coordinates container.
         * @see Map */
        public static final String MAP = "MAP";
        /** Place hierarchy format. */
        public static final String FORM = "FORM";
        /** Language of place name. */
        public static final String LANG = "LANG";
        /** Translated place name. */
        public static final String TRAN = "TRAN";
        /** External identifier. */
        public static final String EXID = "EXID";
        /** Note reference. */
        public static final String NOTE = "NOTE";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Source citation. */
        public static final String SOUR = "SOUR";
    }

    // ─── Map (MAP) substructures ────────────────────────────

    /**
     * Substructure tags valid inside a Map (MAP) structure.
     *
     * <p>A MAP structure appears as a substructure of {@link Plac#MAP}.
     */
    public static final class Map {
        private Map() {}

        /** Latitude coordinate. */
        public static final String LATI = "LATI";
        /** Longitude coordinate. */
        public static final String LONG = "LONG";
    }

    // ─── Date (DATE) substructures ──────────────────────────

    /**
     * Substructure tags valid inside a Date (DATE) structure.
     *
     * <p>A DATE structure appears as a substructure of events and
     * other structures such as {@link Chan#DATE} or {@link Crea#DATE}.
     */
    public static final class Date {
        private Date() {}

        /** Time of day. */
        public static final String TIME = "TIME";
        /** Narrative date phrase. */
        public static final String PHRASE = "PHRASE";
    }

    // ─── Address (ADDR) substructures ───────────────────────

    /**
     * Substructure tags valid inside an Address (ADDR) structure.
     *
     * <p>An ADDR structure appears as a substructure of events,
     * repository records, and submitter records.
     */
    public static final class Addr {
        private Addr() {}

        /** First address line. */
        public static final String ADR1 = "ADR1";
        /** Second address line. */
        public static final String ADR2 = "ADR2";
        /** Third address line. */
        public static final String ADR3 = "ADR3";
        /** City name. */
        public static final String CITY = "CITY";
        /** State or province. */
        public static final String STAE = "STAE";
        /** Postal code. */
        public static final String POST = "POST";
        /** Country name. */
        public static final String CTRY = "CTRY";
    }

    // ─── File (FILE) substructures ──────────────────────────

    /**
     * Substructure tags valid inside a File (FILE) structure.
     *
     * <p>A FILE structure appears as a substructure of multimedia
     * object records ({@link Obje}).
     */
    public static final class File {
        private File() {}

        /** File format/media type. */
        public static final String FORM = "FORM";
        /** Descriptive title. */
        public static final String TITL = "TITL";
        /** Translated file reference. */
        public static final String TRAN = "TRAN";
    }

    // ─── Form (FORM) substructures ──────────────────────────

    /**
     * Substructure tags valid inside a Form (FORM) structure.
     *
     * <p>A FORM structure appears as a substructure of {@link File#FORM}
     * or {@link Plac#FORM}.
     */
    public static final class Form {
        private Form() {}

        /** Source medium type. */
        public static final String MEDI = "MEDI";
    }

    // ─── GEDC substructures ─────────────────────────────────

    /**
     * Substructure tags valid inside a GEDC structure.
     *
     * <p>A GEDC structure appears as a substructure of the
     * header record ({@link Head#GEDC}).
     */
    public static final class Gedc {
        private Gedc() {}

        /** GEDCOM version number. */
        public static final String VERS = "VERS";
    }

    // ─── Personal Name (NAME) substructures ─────────────────

    /**
     * Substructure tags valid inside a personal Name (NAME) structure.
     *
     * <p>A NAME structure appears as a substructure of individual
     * records ({@link Indi#NAME}).
     */
    public static final class Name {
        private Name() {}

        /** Given (first) names. */
        public static final String GIVN = "GIVN";
        /** Surname. */
        public static final String SURN = "SURN";
        /** Name prefix (e.g., Dr.). */
        public static final String NPFX = "NPFX";
        /** Name suffix (e.g., Jr.). */
        public static final String NSFX = "NSFX";
        /** Surname prefix (e.g., von). */
        public static final String SPFX = "SPFX";
        /** Nickname. */
        public static final String NICK = "NICK";
        /** Name type. */
        public static final String TYPE = "TYPE";
        /** Translated name. */
        public static final String TRAN = "TRAN";
        /** Note reference. */
        public static final String NOTE = "NOTE";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Source citation. */
        public static final String SOUR = "SOUR";
    }

    // ─── Reference Number (REFN) substructures ──────────────

    /**
     * Substructure tags valid inside a Reference Number (REFN) structure.
     */
    public static final class Refn {
        private Refn() {}

        /** Reference number type. */
        public static final String TYPE = "TYPE";
    }

    // ─── External Identifier (EXID) substructures ───────────

    /**
     * Substructure tags valid inside an External Identifier (EXID) structure.
     */
    public static final class Exid {
        private Exid() {}

        /** External identifier type. */
        public static final String TYPE = "TYPE";
    }

    // ─── Association (ASSO) substructures ────────────────────

    /**
     * Substructure tags valid inside an Association (ASSO) structure.
     *
     * <p>An ASSO structure appears as a substructure of individual
     * records ({@link Indi#ASSO}).
     */
    public static final class Asso {
        private Asso() {}

        /** Role in the association. */
        public static final String ROLE = "ROLE";
        /** Descriptive phrase. */
        public static final String PHRASE = "PHRASE";
        /** Note reference. */
        public static final String NOTE = "NOTE";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Source citation. */
        public static final String SOUR = "SOUR";
    }

    // ─── Family Child (FAMC) substructures ──────────────────

    /**
     * Substructure tags valid inside a Family Child (FAMC) structure
     * within an individual record.
     *
     * <p>An INDI-FAMC structure appears as a substructure of
     * individual records ({@link Indi#FAMC}).
     */
    public static final class Famc {
        private Famc() {}

        /** Pedigree linkage type. */
        public static final String PEDI = "PEDI";
        /** Status of family-child link. */
        public static final String STAT = "STAT";
        /** Note reference. */
        public static final String NOTE = "NOTE";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
    }

    // ─── Change Date (CHAN) substructures ────────────────────

    /**
     * Substructure tags valid inside a Change Date (CHAN) structure.
     *
     * <p>A CHAN structure appears as a substructure of most record types.
     */
    public static final class Chan {
        private Chan() {}

        /** Date of last change.
         * @see Date */
        public static final String DATE = "DATE";
        /** Note reference. */
        public static final String NOTE = "NOTE";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
    }

    // ─── Creation Date (CREA) substructures ─────────────────

    /**
     * Substructure tags valid inside a Creation Date (CREA) structure.
     *
     * <p>A CREA structure appears as a substructure of most record types.
     */
    public static final class Crea {
        private Crea() {}

        /** Creation date.
         * @see Date */
        public static final String DATE = "DATE";
    }

    // ─── Source Citation substructures ───────────────────────

    /**
     * Substructure tags valid inside a Source Citation (SOUR as substructure).
     *
     * <p>A source citation appears as a substructure of many record types
     * and events (e.g., {@link Indi#SOUR}).
     */
    public static final class SourCitation {
        private SourCitation() {}

        /** Page or location within source. */
        public static final String PAGE = "PAGE";
        /** Source data container. */
        public static final String DATA = "DATA";
        /** Event type from source. */
        public static final String EVEN = "EVEN";
        /** Quality/certainty assessment. */
        public static final String QUAY = "QUAY";
        /** Note reference. */
        public static final String NOTE = "NOTE";
        /** Shared note reference. */
        public static final String SNOTE = "SNOTE";
        /** Multimedia reference. */
        public static final String OBJE = "OBJE";
    }

    // ─── Schema (SCHMA) substructures ───────────────────────

    /**
     * Substructure tags valid inside a Schema (SCHMA) structure.
     *
     * <p>A SCHMA structure appears as a substructure of the
     * header record ({@link Head#SCHMA}).
     */
    public static final class Schma {
        private Schma() {}

        /** Extension tag definition. */
        public static final String TAG = "TAG";
    }
}
