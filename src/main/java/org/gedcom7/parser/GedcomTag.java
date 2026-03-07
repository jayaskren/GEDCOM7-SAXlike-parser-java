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
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the birth occurred. */
            public static final String PLAC = "PLAC";
            /** Address where the birth occurred. */
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
             * @see GedcomDataTypes#parseDateValue(String) */
            public static final String DATE = "DATE";
            /** Place where the marriage occurred. */
            public static final String PLAC = "PLAC";
            /** Address of the marriage location. */
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
}
