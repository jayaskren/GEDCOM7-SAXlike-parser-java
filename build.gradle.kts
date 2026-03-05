plugins {
    `java-library`
}

group = "org.gedcom7"
version = "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// ─── GEDCOM 7 structure-definitions code generator (optional) ──────
// The StructureDefinitions.java class lives in the main source tree.
// Run this task to regenerate it from the TSV files:
//   ./gradlew generateStructureDefinitions
// Then copy the output from build/generated/sources/gedcom/main/java/
// over the hand-written file in src/main/java/.
val generatedSrcDir = layout.buildDirectory.dir("generated/sources/gedcom/main/java")

val generateStructureDefinitions by tasks.registering {
    description = "Generates StructureDefinitions.java from GEDCOM 7 TSV files (optional, for regeneration)"
    group = "code generation"

    val dataDir = file("src/main/data/gedcom7")
    val cardinalitiesFile = file("$dataDir/cardinalities.tsv")
    val substructuresFile = file("$dataDir/substructures.tsv")
    val outputDir = generatedSrcDir.get().asFile.resolve("org/gedcom7/parser/validation")

    inputs.file(cardinalitiesFile)
    inputs.file(substructuresFile)
    outputs.dir(outputDir)

    doLast {
        data class CardEntry(val parent: String, val tag: String, val cardinality: String)
        val cardinalities = mutableListOf<CardEntry>()
        cardinalitiesFile.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val parts = trimmed.split("\t")
                if (parts.size >= 3) {
                    cardinalities.add(CardEntry(parts[0].trim(), parts[1].trim(), parts[2].trim()))
                }
            }
        }

        data class SubEntry(val parent: String, val tag: String, val structureId: String)
        val substructures = mutableListOf<SubEntry>()
        substructuresFile.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val parts = trimmed.split("\t")
                if (parts.size >= 3) {
                    substructures.add(SubEntry(parts[0].trim(), parts[1].trim(), parts[2].trim()))
                }
            }
        }

        outputDir.mkdirs()
        val sb = StringBuilder()
        sb.appendLine("package org.gedcom7.parser.validation;")
        sb.appendLine()
        sb.appendLine("import java.util.Collections;")
        sb.appendLine("import java.util.HashMap;")
        sb.appendLine("import java.util.Map;")
        sb.appendLine()
        sb.appendLine("/** Auto-generated from GEDCOM 7 TSV specification files. */")
        sb.appendLine("public final class StructureDefinitions {")
        sb.appendLine("    private StructureDefinitions() {}")
        sb.appendLine()
        sb.appendLine("    private static final Map<String, String> SUB_LOOKUP;")
        sb.appendLine("    static {")
        sb.appendLine("        Map<String, String> m = new HashMap<>(${(substructures.size * 4 / 3) + 1});")
        for (s in substructures) {
            sb.appendLine("        m.put(\"${s.parent}\\t${s.tag}\", \"${s.structureId}\");")
        }
        sb.appendLine("        SUB_LOOKUP = Collections.unmodifiableMap(m);")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    private static final Map<String, String> CARD_LOOKUP;")
        sb.appendLine("    static {")
        sb.appendLine("        Map<String, String> m = new HashMap<>(${(cardinalities.size * 4 / 3) + 1});")
        for (c in cardinalities) {
            sb.appendLine("        m.put(\"${c.parent}\\t${c.tag}\", \"${c.cardinality}\");")
        }
        sb.appendLine("        CARD_LOOKUP = Collections.unmodifiableMap(m);")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    private static final Map<String, String> RECORD_CONTEXT;")
        sb.appendLine("    static {")
        sb.appendLine("        Map<String, String> m = new HashMap<>(16);")
        for (tag in listOf("INDI", "FAM", "OBJE", "REPO", "SNOTE", "SOUR", "SUBM")) {
            sb.appendLine("        m.put(\"$tag\", \"record-$tag\");")
        }
        sb.appendLine("        m.put(\"HEAD\", \"HEAD\");")
        sb.appendLine("        RECORD_CONTEXT = Collections.unmodifiableMap(m);")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    public static String resolveStructure(String parentContextId, String tag) {")
        sb.appendLine("        return SUB_LOOKUP.get(parentContextId + \"\\t\" + tag);")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    public static String getCardinality(String parentContextId, String childStructureId) {")
        sb.appendLine("        return CARD_LOOKUP.get(parentContextId + \"\\t\" + childStructureId);")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    public static String recordContext(String recordTag) {")
        sb.appendLine("        return RECORD_CONTEXT.get(recordTag);")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    public static boolean isSingleton(String cardinality) {")
        sb.appendLine("        return cardinality != null")
        sb.appendLine("                && (cardinality.equals(\"{0:1}\") || cardinality.equals(\"{1:1}\"));")
        sb.appendLine("    }")
        sb.appendLine("}")

        val outputFile = File(outputDir, "StructureDefinitions.java")
        outputFile.writeText(sb.toString())
        logger.lifecycle("Generated ${outputFile.absolutePath}")
    }
}
