import org.gedcom7.parser.*;
import org.gedcom7.parser.datatype.*;
import java.io.FileInputStream;
import java.util.*;

/**
 * Simple example: parse a GEDCOM 7 file and print a summary of individuals
 * and families. Demonstrates using GedcomTag and GedcomValue constants for
 * readable, type-safe handler code.
 *
 * Usage: java -cp gedcom7-parser.jar FamilyTreeExample path/to/file.ged
 */
public class FamilyTreeExample {

    static class Person {
        String xref;
        String name;
        String surname;
        String sex;
        String birthDate;
        String birthPlace;
        String birthLatitude;
        String birthLongitude;
        String deathDate;
    }

    static class Family {
        String xref;
        String husbandXref;
        String wifeXref;
        List<String> childXrefs = new ArrayList<>();
        String marriageDate;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: FamilyTreeExample <file.ged>");
            System.exit(1);
        }

        Map<String, Person> people = new LinkedHashMap<>();
        Map<String, Family> families = new LinkedHashMap<>();

        GedcomHandler handler = new GedcomHandler() {
            private String currentRecordTag;
            private String currentXref;
            private String currentSubTag;   // level-1 tag (e.g., BIRT)
            private String currentLevel2Tag; // level-2 tag (e.g., PLAC)

            @Override
            public void startDocument(GedcomHeaderInfo header) {
                System.out.println("GEDCOM version: " + header.getVersion());
                if (header.getSourceName() != null) {
                    System.out.println("Created by: " + header.getSourceName());
                }
                System.out.println();
            }

            @Override
            public void startRecord(int level, String xref, String tag) {
                currentRecordTag = tag;
                currentXref = xref;
                currentSubTag = null;

                switch (tag) {
                    case GedcomTag.INDI:
                        Person p = new Person();
                        p.xref = xref;
                        people.put(xref, p);
                        break;
                    case GedcomTag.FAM:
                        Family f = new Family();
                        f.xref = xref;
                        families.put(xref, f);
                        break;
                }
            }

            @Override
            public void startStructure(int level, String xref, String tag,
                                       String value, boolean isPointer) {
                if (GedcomTag.INDI.equals(currentRecordTag)) {
                    handleIndiStructure(level, tag, value, isPointer);
                } else if (GedcomTag.FAM.equals(currentRecordTag)) {
                    handleFamStructure(level, tag, value, isPointer);
                }
            }

            private void handleIndiStructure(int level, String tag,
                                             String value, boolean isPointer) {
                Person p = people.get(currentXref);
                if (level == 1) {
                    currentSubTag = tag;
                    switch (tag) {
                        case GedcomTag.Indi.NAME:
                            p.name = value;
                            if (value != null) {
                                GedcomPersonalName parsed =
                                        GedcomDataTypes.parsePersonalName(value);
                                if (parsed.getSurname() != null) {
                                    p.surname = parsed.getSurname();
                                }
                            }
                            break;
                        case GedcomTag.Indi.SEX:
                            switch (value != null ? value : "") {
                                case GedcomValue.Sex.MALE:
                                    p.sex = "Male";
                                    break;
                                case GedcomValue.Sex.FEMALE:
                                    p.sex = "Female";
                                    break;
                                default:
                                    p.sex = value;
                            }
                            break;
                    }
                } else if (level == 2) {
                    currentLevel2Tag = tag;
                    if (GedcomTag.Indi.BIRT.equals(currentSubTag)) {
                        if (GedcomTag.Indi.Birt.DATE.equals(tag)) {
                            p.birthDate = value;
                        } else if (GedcomTag.Indi.Birt.PLAC.equals(tag)) {
                            p.birthPlace = value;
                        }
                    } else if (GedcomTag.Indi.DEAT.equals(currentSubTag)) {
                        if (GedcomTag.Indi.Deat.DATE.equals(tag)) {
                            p.deathDate = value;
                        }
                    }
                } else if (level == 3) {
                    // Use common substructure constants for depth-3 tags
                    if (GedcomTag.Indi.BIRT.equals(currentSubTag)
                            && GedcomTag.Plac.MAP.equals(currentLevel2Tag)) {
                        if (GedcomTag.Map.LATI.equals(tag)) {
                            p.birthLatitude = value;
                        } else if (GedcomTag.Map.LONG.equals(tag)) {
                            p.birthLongitude = value;
                        }
                    }
                }
            }

            private void handleFamStructure(int level, String tag,
                                            String value, boolean isPointer) {
                Family f = families.get(currentXref);
                if (level == 1) {
                    currentSubTag = tag;
                    if (GedcomTag.Fam.HUSB.equals(tag) && isPointer) {
                        f.husbandXref = value;
                    } else if (GedcomTag.Fam.WIFE.equals(tag) && isPointer) {
                        f.wifeXref = value;
                    } else if (GedcomTag.Fam.CHIL.equals(tag) && isPointer) {
                        f.childXrefs.add(value);
                    }
                } else if (level == 2 && GedcomTag.Fam.MARR.equals(currentSubTag)) {
                    if (GedcomTag.Fam.Marr.DATE.equals(tag)) {
                        f.marriageDate = value;
                    }
                }
            }

            @Override
            public void warning(GedcomParseError error) {
                System.err.println("Warning (line " + error.getLineNumber()
                        + "): " + error.getMessage());
            }

            @Override
            public void error(GedcomParseError error) {
                System.err.println("Error (line " + error.getLineNumber()
                        + "): " + error.getMessage());
            }
        };

        try (FileInputStream in = new FileInputStream(args[0]);
             GedcomReader reader = new GedcomReader(
                     in, handler, GedcomReaderConfig.gedcom7())) {
            reader.parse();
        }

        // Print individuals
        System.out.println("=== Individuals ===");
        for (Person p : people.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.name != null ? p.name : "(unnamed)");
            if (p.sex != null) sb.append(" [").append(p.sex).append("]");
            if (p.birthDate != null) {
                sb.append(", born ").append(p.birthDate);
                if (p.birthPlace != null) sb.append(" at ").append(p.birthPlace);
                if (p.birthLatitude != null && p.birthLongitude != null) {
                    sb.append(" (").append(p.birthLatitude)
                      .append(", ").append(p.birthLongitude).append(")");
                }
            }
            if (p.deathDate != null) sb.append(", died ").append(p.deathDate);
            System.out.println("  " + sb);
        }

        // Print families
        System.out.println("\n=== Families ===");
        for (Family f : families.values()) {
            String husband = nameOf(people, f.husbandXref);
            String wife = nameOf(people, f.wifeXref);
            StringBuilder sb = new StringBuilder();
            sb.append(husband).append(" + ").append(wife);
            if (f.marriageDate != null) {
                sb.append(", married ").append(f.marriageDate);
            }
            if (!f.childXrefs.isEmpty()) {
                sb.append(", children: ");
                StringJoiner joiner = new StringJoiner(", ");
                for (String childXref : f.childXrefs) {
                    joiner.add(nameOf(people, childXref));
                }
                sb.append(joiner);
            }
            System.out.println("  " + sb);
        }
    }

    private static String nameOf(Map<String, Person> people, String xref) {
        if (xref == null) return "?";
        Person p = people.get(xref);
        if (p != null && p.name != null) return p.name;
        return "@" + xref + "@";
    }
}
