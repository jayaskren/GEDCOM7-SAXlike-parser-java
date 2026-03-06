# GEDCOM Writer — Quickstart Tutorial

**Feature**: 004-gedcom-writer | **Date**: 2026-03-05

This tutorial walks through using the GEDCOM SAX-like Writer to create GEDCOM files. No GEDCOM knowledge is required — the typed API guides you with IDE auto-complete.

---

## 1. Your First GEDCOM File

The simplest possible GEDCOM file — one person:

```java
import org.gedcom7.writer.GedcomWriter;
import java.io.FileOutputStream;

try (var out = new FileOutputStream("my-family.ged");
     var writer = new GedcomWriter(out)) {

    writer.head(head -> head.source("MyApp"));

    writer.individual(indi -> {
        indi.personalName("John /Doe/");
    });
}
```

**What happens:**
- `writer.head(...)` writes the GEDCOM header with your app name
- `writer.individual(...)` writes an INDI record. The level numbers are automatic.
- When `writer.close()` runs (via try-with-resources), a TRLR trailer is auto-appended.

**Output:**
```
0 HEAD
1 GEDC
2 VERS 7.0
1 SOUR MyApp
0 @I1@ INDI
1 NAME John /Doe/
0 TRLR
```

> **Tip**: If you forget to call `writer.head(...)`, the writer auto-generates a minimal header and logs a warning.

---

## 2. Adding Details to a Person

Add birth, death, and name parts:

```java
writer.individual(indi -> {
    indi.personalName("John /Doe/", name -> {
        name.givenName("John");
        name.surname("Doe");
    });

    indi.sex("M");

    indi.birth(birt -> {
        birt.date(date(15, MAR, 1955));
        birt.place("Springfield, IL");
    });

    indi.death(deat -> {
        deat.date(date(3, OCT, 2020));
        deat.place("Chicago, IL");
    });
});
```

**Import for dates:**
```java
import static org.gedcom7.writer.date.GedcomDateBuilder.*;
import static org.gedcom7.writer.date.Month.*;
```

**Output:**
```
0 @I1@ INDI
1 NAME John /Doe/
2 GIVN John
2 SURN Doe
1 SEX M
1 BIRT
2 DATE 15 MAR 1955
2 PLAC Springfield, IL
1 DEAT
2 DATE 3 OCT 2020
2 PLAC Chicago, IL
```

---

## 3. Linking People to Families

Record methods return `Xref` handles that you use to link records together:

```java
import org.gedcom7.writer.Xref;

// Write individuals — save the Xref handles
Xref john = writer.individual(indi -> {
    indi.personalName("John /Doe/");
    indi.sex("M");
});

Xref jane = writer.individual(indi -> {
    indi.personalName("Jane /Smith/");
    indi.sex("F");
});

Xref jimmy = writer.individual(indi -> {
    indi.personalName("Jimmy /Doe/");
    indi.sex("M");
});

// Write a family linking them
writer.family(fam -> {
    fam.husband(john);
    fam.wife(jane);
    fam.child(jimmy);
    fam.marriage(marr -> {
        marr.date(date(14, JUN, 1980));
        marr.place("Springfield, IL");
    });
});
```

**Output (family portion):**
```
0 @F1@ FAM
1 HUSB @I1@
1 WIFE @I2@
1 CHIL @I3@
1 MARR
2 DATE 14 JUN 1980
2 PLAC Springfield, IL
```

---

## 4. Exporting from a Database

If you already have IDs (e.g., database primary keys), pass them directly:

```java
// Assume you have database records with IDs
for (Person person : database.getAllPersons()) {
    writer.individual(String.valueOf(person.getId()), indi -> {
        indi.personalName(person.getFirstName() + " /" + person.getLastName() + "/");
        if (person.getBirthDate() != null) {
            indi.birth(birt -> birt.date(person.getBirthDate()));
        }
    });
}

for (Family family : database.getAllFamilies()) {
    writer.family(String.valueOf(family.getId()), fam -> {
        fam.husband(String.valueOf(family.getHusbandId()));
        fam.wife(String.valueOf(family.getWifeId()));
        for (Long childId : family.getChildIds()) {
            fam.child(String.valueOf(childId));
        }
    });
}
```

**Key insight**: You can write families before individuals, or vice versa. GEDCOM allows forward references — the records don't need to be in any particular order.

---

## 5. Working with Dates

The `GedcomDateBuilder` provides static factory methods for all GEDCOM date forms:

```java
import static org.gedcom7.writer.date.GedcomDateBuilder.*;
import static org.gedcom7.writer.date.Month.*;
import static org.gedcom7.writer.date.HebrewMonth.*;

// Exact dates
date(15, MAR, 1955)           // "15 MAR 1955"
date(MAR, 1955)               // "MAR 1955"
date(1955)                    // "1955"

// Approximate dates
about(1880)                   // "ABT 1880"
estimated(date(MAR, 1750))    // "EST MAR 1750"
calculated(date(1800))        // "CAL 1800"

// Ranges
before(JUN, 1900)             // "BEF JUN 1900"
after(1850)                   // "AFT 1850"
between(date(1880), date(1890))  // "BET 1880 AND 1890"

// Periods
from(date(1, JAN, 1940))     // "FROM 1 JAN 1940"
to(date(31, DEC, 1945))      // "TO 31 DEC 1945"
fromTo(date(1, JAN, 1940), date(31, DEC, 1945))
                              // "FROM 1 JAN 1940 TO 31 DEC 1945"

// BCE dates
dateBce(44)                   // "44 BCE"

// Non-Gregorian calendars
hebrew(15, NSN, 5765)         // "HEBREW 15 NSN 5765"
julian(1, JAN, 1700)          // "JULIAN 1 JAN 1700"

// Expert escape hatch (no validation)
WriterDate.raw("INT 1 JAN 1900 (estimated from census)")
```

**Using dates in events:**
```java
indi.birth(birt -> {
    birt.date(about(1880));
    birt.place("Somewhere, England");
});
```

**Validation**: Invalid dates throw `IllegalArgumentException` at construction time:
```java
date(32, JAN, 1955)  // throws — day out of range
date(0)              // throws — year must be >= 1
```

---

## 6. Source Citations

Cite your sources on events:

```java
// Create source records
Xref census1880 = writer.source(src -> {
    src.title("1880 United States Federal Census");
    src.author("United States Census Bureau");
});

Xref birthCert = writer.source(src -> {
    src.title("Birth Certificate #12345");
});

// Cite them on events
writer.individual(indi -> {
    indi.personalName("John /Doe/");
    indi.birth(birt -> {
        birt.date(date(15, MAR, 1955));
        birt.place("Springfield, IL");

        // Simple citation
        birt.sourceCitation(birthCert);

        // Citation with details
        birt.sourceCitation(census1880, cite -> {
            cite.page("Roll 108, Page 42, Line 15");
            cite.quality("3");
        });
    });
});
```

**Output:**
```
1 BIRT
2 DATE 15 MAR 1955
2 PLAC Springfield, IL
2 SOUR @S2@
2 SOUR @S1@
3 PAGE Roll 108, Page 42, Line 15
3 QUAY 3
```

---

## 7. Custom and Extension Tags

Use escape hatches for tags the typed API doesn't cover:

```java
writer.individual(indi -> {
    indi.personalName("John /Doe/");

    // Custom extension tag (simple value)
    indi.structure("_DNA", "Haplogroup R1b");

    // Custom extension tag with children
    indi.structure("_PHOTO", "family_photo.jpg", photo -> {
        photo.structure("FORM", "image/jpeg");
        photo.structure("_DATE", "2020-01-15");
    });
});
```

**Declare extension tags in the header (GEDCOM 7):**
```java
writer.head(head -> {
    head.source("MyApp");
    head.schema(schma -> {
        schma.tag("_DNA", "https://example.com/ext/dna");
        schma.tag("_PHOTO", "https://example.com/ext/photo");
    });
});
```

**Arbitrary top-level records:**
```java
Xref dna = writer.record("_DNATEST", body -> {
    body.structure("_RESULT", "positive");
    body.structure("_DATE", "2024-01-15");
});
```

---

## 8. Writing GEDCOM 5.5.5 Files

Switch to GEDCOM 5.5.5 by changing the configuration — your code stays the same:

```java
import org.gedcom7.writer.GedcomWriterConfig;

try (var out = new FileOutputStream("family-555.ged");
     var writer = new GedcomWriter(out, GedcomWriterConfig.gedcom555())) {

    writer.head(head -> head.source("MyApp"));

    Xref john = writer.individual(indi -> {
        indi.personalName("John /Doe/");
        indi.birth(birt -> {
            birt.date(date(15, MAR, 1955));
        });
    });

    Xref fam1 = writer.family(fam -> {
        fam.husband(john);
    });

    // In 5.5.5 mode, FAMS is standard (no warning)
    writer.individual(indi -> {
        indi.personalName("Jane /Smith/");
        indi.familyAsSpouse(fam1);
    });
}
```

**Differences handled automatically:**
- Header says `5.5.5` instead of `7.0`
- Long lines are split with CONC (if `maxLineLength` is configured)
- All `@` characters are doubled (not just leading)
- Non-Gregorian calendar dates use `@#D...@` escape format
- FAMS/FAMC produce no warning

---

## 9. Strict Mode and Warning Control

### Strict Mode — Catch Mistakes Early

```java
var config = GedcomWriterConfig.gedcom7Strict();

try (var writer = new GedcomWriter(out, config)) {
    // This would throw GedcomWriteException because head() wasn't called
    writer.individual(indi -> indi.personalName("John /Doe/"));
}
```

### Custom Warning Handler

```java
var warnings = new ArrayList<String>();

var config = GedcomWriterConfig.gedcom7()
    .toBuilder()
    .warningHandler(w -> warnings.add(w.getMessage()))
    .build();

try (var writer = new GedcomWriter(out, config)) {
    writer.individual(indi -> {
        indi.personalName("John /Doe/");
        indi.familyAsSpouse(Xref.of("F1"));  // triggers warning in GEDCOM 7
    });
}

System.out.println("Warnings: " + warnings);
// ["FAMS is not part of GEDCOM 7 INDI. Use FAM.HUSB/WIFE instead."]
```

### Suppress All Warnings

```java
var config = GedcomWriterConfig.gedcom7()
    .toBuilder()
    .warningHandler(null)  // null suppresses all warnings
    .build();
```

---

## 10. Complete Example — A Family Tree

```java
import org.gedcom7.writer.*;
import org.gedcom7.writer.context.*;
import static org.gedcom7.writer.date.GedcomDateBuilder.*;
import static org.gedcom7.writer.date.Month.*;
import java.io.*;

public class FamilyTreeExample {
    public static void main(String[] args) throws Exception {
        try (var out = new FileOutputStream("family-tree.ged");
             var writer = new GedcomWriter(out)) {

            // Header
            writer.head(head -> {
                head.source("FamilyTreeApp", src -> {
                    src.structure("VERS", "1.0");
                    src.structure("NAME", "My Family Tree App");
                });
            });

            // Submitter
            Xref subm = writer.submitter(s -> {
                s.name("Jane Researcher");
            });

            // Sources
            Xref census = writer.source(src -> {
                src.title("1880 US Federal Census");
                src.author("US Census Bureau");
            });

            // Grandparents
            Xref grandpa = writer.individual(indi -> {
                indi.personalName("William /Doe/", name -> {
                    name.givenName("William");
                    name.surname("Doe");
                });
                indi.sex("M");
                indi.birth(birt -> {
                    birt.date(about(1855));
                    birt.place("County Cork, Ireland");
                });
                indi.death(deat -> {
                    deat.date(date(12, FEB, 1920));
                    deat.place("Boston, MA");
                });
            });

            Xref grandma = writer.individual(indi -> {
                indi.personalName("Mary /O'Brien/", name -> {
                    name.givenName("Mary");
                    name.surname("O'Brien");
                });
                indi.sex("F");
                indi.birth(birt -> {
                    birt.date(about(1860));
                    birt.place("County Kerry, Ireland");
                });
            });

            // Parents
            Xref dad = writer.individual(indi -> {
                indi.personalName("John /Doe/", name -> {
                    name.givenName("John");
                    name.surname("Doe");
                });
                indi.sex("M");
                indi.birth(birt -> {
                    birt.date(date(15, MAR, 1885));
                    birt.place("Boston, MA");
                    birt.sourceCitation(census, cite -> {
                        cite.page("Roll 108, Page 42");
                    });
                });
            });

            Xref mom = writer.individual(indi -> {
                indi.personalName("Sarah /Jones/", name -> {
                    name.givenName("Sarah");
                    name.surname("Jones");
                });
                indi.sex("F");
                indi.birth(birt -> {
                    birt.date(date(22, JUL, 1890));
                });
            });

            // Children
            Xref child1 = writer.individual(indi -> {
                indi.personalName("Robert /Doe/");
                indi.sex("M");
                indi.birth(birt -> birt.date(date(3, JAN, 1915)));
            });

            Xref child2 = writer.individual(indi -> {
                indi.personalName("Elizabeth /Doe/");
                indi.sex("F");
                indi.birth(birt -> birt.date(date(17, SEP, 1918)));
            });

            // Families
            writer.family(fam -> {
                fam.husband(grandpa);
                fam.wife(grandma);
                fam.child(dad);
                fam.marriage(marr -> {
                    marr.date(about(1882));
                    marr.place("Boston, MA");
                });
            });

            writer.family(fam -> {
                fam.husband(dad);
                fam.wife(mom);
                fam.child(child1);
                fam.child(child2);
                fam.marriage(marr -> {
                    marr.date(date(8, JUN, 1912));
                    marr.place("Cambridge, MA");
                });
            });
        }

        System.out.println("Family tree written to family-tree.ged");
    }
}
```

---

## Quick Reference

| Task | Code |
|------|------|
| Create writer (GEDCOM 7) | `new GedcomWriter(out)` |
| Create writer (5.5.5) | `new GedcomWriter(out, GedcomWriterConfig.gedcom555())` |
| Add a person | `Xref ref = writer.individual(indi -> { ... })` |
| Add a family | `Xref ref = writer.family(fam -> { ... })` |
| Link husband | `fam.husband(xref)` |
| Link wife | `fam.wife(xref)` |
| Link child | `fam.child(xref)` |
| Add birth event | `indi.birth(birt -> { ... })` |
| Set a date | `birt.date(date(15, MAR, 1955))` |
| Set a place | `birt.date(date(...)); birt.place("City, State")` |
| Cite a source | `birt.sourceCitation(srcRef, cite -> cite.page("..."))` |
| Use a custom tag | `indi.structure("_TAG", "value")` |
| Use developer IDs | `writer.individual("42", indi -> { ... })` |
