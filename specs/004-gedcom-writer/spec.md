# Feature Specification: GEDCOM SAX-like Writer

**Feature Branch**: `004-gedcom-writer`
**Created**: 2026-03-05
**Status**: Draft
**Input**: User description: "GEDCOM SAX-like writer with Level 3 typed context API — event-push model with typed context classes providing domain-specific methods, lambda-scoped nesting with auto-close, string-based escape hatches, automatic CONT/CONC splitting, @@ escaping, and GedcomWriterConfig supporting both GEDCOM 7 and 5.5.5"

## Clarifications

### Session 2026-03-05

- Q: How should the developer customize the HEAD record (SOUR, DEST, SUBM, etc.)? → A: Provide `writer.head(head -> {...})` with a typed HeadContext. If `head()` is never called, auto-generate a minimal HEAD and log a warning so the developer is aware they should customize it.
- Q: How should the writer handle structures that have both a value and substructures? → A: Add `structure(tag, value, body)` and `pointer(tag, xref, body)` overloads to the escape hatch set. Typed methods (e.g., `personalName(value, body)`) use these internally.
- Q: How should the writer handle dates? → A: Provide a GedcomDateBuilder with static factory methods and type-safe month enums for guided date creation, plus WriterDate.raw() as an expert escape hatch.
- Q: How should the writer handle strict vs lenient modes and warnings? → A: Support strict mode (throws on issues) and lenient mode (logs warnings). Developers can configure a WarningHandler callback on GedcomWriterConfig, or set it to null to suppress all warnings.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Write a Simple GEDCOM 7 File (Priority: P1)

As a developer, I want to write a valid GEDCOM 7 file by calling typed methods on context objects (e.g., `writer.individual(...)`, `indi.birth(...)`), so that I can produce correct GEDCOM output without memorizing tag strings or level numbers.

**Why this priority**: This is the core value proposition — if you can't write a basic GEDCOM 7 file with typed contexts, nothing else matters. A developer should be able to create individuals, families, and basic structures using IDE-discoverable methods.

**Independent Test**: Create a writer, add a HEAD record, an INDI record with name and birth, a FAM record linking two individuals, and a TRLR. Verify the output is a valid GEDCOM 7 file that the existing parser can read back.

**Acceptance Scenarios**:

1. **Given** a developer creates a GedcomWriter with GEDCOM 7 config, **When** they call `writer.individual(indi -> { indi.personalName("John /Doe/", name -> { name.givenName("John"); name.surname("Doe"); }); indi.birth(birt -> { birt.date(date(15, MAR, 1955)); birt.place("Springfield, IL"); }); })`, **Then** the output contains correctly structured INDI, NAME (with GIVN/SURN), and BIRT (with DATE/PLAC) lines with proper level numbers, and the method returns an Xref handle.
2. **Given** a developer creates a family record, **When** they call `writer.family(fam -> { fam.husband(johnXref); fam.wife(janeXref); })`, **Then** the output contains a FAM record with HUSB and WIFE pointer lines referencing the correct xref identifiers.
3. **Given** a developer calls `writer.head(head -> { head.source("MyApp"); })`, **When** the file is complete, **Then** the output starts with `0 HEAD` containing `1 GEDC` / `2 VERS 7.0` and `1 SOUR MyApp`, and ends with `0 TRLR`.
4. **Given** a developer never calls `writer.head(...)`, **When** the writer is closed, **Then** a minimal HEAD (with GEDC.VERS) is auto-generated and a warning is delivered to the configured WarningHandler.
5. **Given** a developer writes a complete file, **When** the output is fed to the existing GedcomReader parser, **Then** the parser reads it without errors.
6. **Given** a developer cites a source on a birth event, **When** they call `birt.sourceCitation(censusRef, cite -> { cite.page("Roll 108, Page 42"); })`, **Then** the output contains `SOUR @S1@` followed by `PAGE Roll 108, Page 42` at the correct levels.

---

### User Story 2 - Automatic Payload Handling (CONT Splitting and @@ Escaping) (Priority: P2)

As a developer, I want the writer to automatically split multi-line text values into CONT continuation lines and escape leading `@` characters, so that I don't have to handle GEDCOM encoding rules manually.

**Why this priority**: Without automatic CONT splitting and @@ escaping, developers would need deep knowledge of GEDCOM encoding rules. This makes the writer truly "just works" for text payloads.

**Independent Test**: Write a NOTE with embedded newlines and a value starting with `@`. Verify the output contains proper CONT lines and `@@` escaping.

**Acceptance Scenarios**:

1. **Given** a value containing newline characters (e.g., `"Line one\nLine two\nLine three"`), **When** the writer emits this value, **Then** the first line appears as the structure's value and subsequent lines appear as CONT substructures at the next level.
2. **Given** a value starting with `@` (e.g., `"@handle"`), **When** the writer emits this in GEDCOM 7 mode, **Then** the output escapes only the leading `@` as `@@handle`.
3. **Given** a value starting with `@` in GEDCOM 5.5.5 mode, **When** the writer emits this value, **Then** all `@` characters are doubled (e.g., `"@user@domain"` becomes `"@@user@@domain"`).

---

### User Story 3 - Escape Hatch for Custom and Unsupported Tags (Priority: P3)

As a developer working with GEDCOM extensions or less common structures, I want to use generic `structure(tag, ...)` and `pointer(tag, ...)` methods on any context to emit arbitrary tags, so that I'm not limited to only the typed convenience methods.

**Why this priority**: The typed methods cover standard GEDCOM structures, but real-world files contain extension tags (e.g., `_CUSTOM`) and less common standard tags. Without escape hatches, developers would be blocked whenever they need a tag that doesn't have a typed method.

**Independent Test**: Use `structure("_PHOTO", "image.jpg")` and `pointer("_LINK", "@S1@")` within an individual context. Verify the output contains these custom structures at the correct level.

**Acceptance Scenarios**:

1. **Given** any typed context (e.g., IndividualContext), **When** the developer calls `structure("_CUSTOM", "value")`, **Then** the output contains a line with the `_CUSTOM` tag at the correct level with the given value.
2. **Given** any typed context, **When** the developer calls `structure("_BLOCK", body -> { body.structure("_INNER", "nested"); })`, **Then** the output contains a `_BLOCK` line followed by a `_INNER` substructure at the next level.
3. **Given** any typed context, **When** the developer calls `structure("FILE", "https://example.com/photo.jpg", file -> { file.structure("FORM", "image/jpeg"); })`, **Then** the output contains a line with the value AND child substructures at the correct levels.
4. **Given** any typed context, **When** the developer calls `pointer("SOUR", "@S1@", body -> { body.structure("PAGE", "42"); })`, **Then** the output contains a pointer line with child substructures.
5. **Given** a standard tag like `BIRT`, **When** the developer calls `structure("BIRT", ...)` instead of `birth(...)`, **Then** the output is identical — typed methods are convenience wrappers over the string-based methods.
6. **Given** a developer needs a top-level record type not covered by typed methods, **When** they call `writer.record("_DNATEST", body -> { ... })`, **Then** the output contains a level-0 record with the custom tag and an auto-generated xref.
7. **Given** a developer declares extension tags, **When** they call `head.schema(schma -> { schma.tag("_CUSTOM", "https://example.com/custom"); })`, **Then** the output contains `SCHMA.TAG` entries in the HEAD record.

---

### User Story 4 - GEDCOM 5.5.5 Writer Mode (Priority: P4)

As a developer, I want to write GEDCOM 5.5.5 files by using a different writer configuration, so that I can produce files compatible with the wide ecosystem of tools that use GEDCOM 5.5.5.

**Why this priority**: GEDCOM 5.5.5 is widely used — more tools support it today than GEDCOM 7. Both versions are first-class citizens in this library. Switching between them should be a configuration change, not a separate API.

**Independent Test**: Create a writer with GEDCOM 5.5.5 config. Write a file and verify the header says `5.5.5`, CONC splitting occurs for long lines, and all `@` characters are doubled.

**Acceptance Scenarios**:

1. **Given** a GedcomWriter configured for GEDCOM 5.5.5, **When** a HEAD record is written, **Then** the GEDC.VERS value is `5.5.5`.
2. **Given** GEDCOM 5.5.5 mode with a max line length configured, **When** a value exceeds the max line length, **Then** the writer splits the value using CONC continuation lines.
3. **Given** GEDCOM 5.5.5 mode, **When** a value contains `@` characters, **Then** all `@` characters are doubled (all-@@ escaping strategy).
4. **Given** GEDCOM 5.5.5 mode, **When** the same application code writes to either GEDCOM 7 or 5.5.5, **Then** only the GedcomWriterConfig differs — zero code changes required.
5. **Given** GEDCOM 5.5.5 mode, **When** a developer writes `indi.familyAsSpouse(famRef)`, **Then** the output contains `1 FAMS @F1@` on the individual record (required in 5.5.5 but optional/redundant in 7.0).

---

### User Story 5 - Cross-Reference Management (Priority: P5)

As a developer, I want flexible cross-reference management that supports both auto-generated IDs and developer-provided IDs, so that I can link records together regardless of whether I'm building from scratch or exporting from an existing database.

**Why this priority**: Cross-references are how GEDCOM links records together. Without them, you can't express relationships. This is essential but builds on top of basic record writing (US1).

**Independent Test**: Write individuals and families using both auto-generated Xref handles and developer-provided string IDs. Verify all cross-references resolve correctly in the output.

**Acceptance Scenarios**:

1. **Given** a developer writes `Xref john = writer.individual(indi -> { ... })` without providing an ID, **When** the record is emitted, **Then** the system auto-generates a unique xref identifier (e.g., `@I1@`) and returns it as an Xref handle.
2. **Given** a developer writes `Xref john = writer.individual("42", indi -> { ... })` with a developer-provided ID, **When** the record is emitted, **Then** the output line is `0 @42@ INDI` using the developer's ID.
3. **Given** an Xref handle obtained from writing an individual, **When** the developer passes it to `fam.husband(johnXref)`, **Then** the output contains the correct pointer referencing that individual's xref.
4. **Given** a developer passes a plain string ID to `fam.husband("42")`, **When** the pointer is emitted, **Then** the output line is `1 HUSB @42@` — string-based references work alongside Xref handles.
5. **Given** a developer writes families before individuals (forward references), **When** both use the same developer-provided IDs, **Then** the output is valid GEDCOM — record order does not matter because GEDCOM allows forward references.
6. **Given** a developer loops through a database writing individuals first, then families, **When** they use their database primary keys as xref IDs, **Then** the output correctly links all records without needing an intermediate Map.

**Common Usage Patterns**:

*Pattern A — Auto-generated IDs (building from scratch):*
```
Xref john = writer.individual(indi -> { indi.personalName("John /Doe/"); })
Xref jane = writer.individual(indi -> { indi.personalName("Jane /Smith/"); })
writer.family(fam -> { fam.husband(john); fam.wife(jane); })
```

*Pattern B — Developer-provided IDs (database export):*
```
for each person in database:
    writer.individual(person.id, indi -> { indi.personalName(...); })
for each family in database:
    writer.family(family.id, fam -> {
        fam.husband(family.husbandId)
        fam.wife(family.wifeId)
        for each childId in family.childIds:
            fam.child(childId)
    })
```

---

### User Story 6 - Type-Safe Date Construction (Priority: P6)

As a developer, I want to create GEDCOM dates using a guided builder API with type-safe month enums, so that I can construct valid dates without memorizing GEDCOM date syntax.

**Why this priority**: GEDCOM dates have a complex format (e.g., `BET 1 JAN 1880 AND 31 DEC 1890`, `ABT 1750`, `HEBREW 15 NSN 5765`). Without a builder, novice developers must learn GEDCOM date syntax. The builder provides guard rails while experts can bypass it.

**Independent Test**: Create dates using the builder (exact, approximate, range, period, BCE, non-Gregorian) and verify they produce correct GEDCOM date strings.

**Acceptance Scenarios**:

1. **Given** a developer calls `date(15, MAR, 1955)`, **When** the date is emitted, **Then** the output is `15 MAR 1955`.
2. **Given** a developer calls `about(1880)`, **When** emitted, **Then** the output is `ABT 1880`.
3. **Given** a developer calls `between(date(1880), date(1890))`, **When** emitted, **Then** the output is `BET 1880 AND 1890`.
4. **Given** a developer calls `before(JUN, 1900)`, **When** emitted, **Then** the output is `BEF JUN 1900`.
5. **Given** a developer calls `fromTo(date(1, JAN, 1940), date(31, DEC, 1945))`, **When** emitted, **Then** the output is `FROM 1 JAN 1940 TO 31 DEC 1945`.
6. **Given** a developer calls `dateBce(44)`, **When** emitted, **Then** the output is `44 BCE`.
7. **Given** a developer calls `hebrew(15, NSN, 5765)`, **When** emitted, **Then** the output is `HEBREW 15 NSN 5765`.
8. **Given** a developer calls `date(32, JAN, 1955)`, **Then** an IllegalArgumentException is thrown (day out of range).
9. **Given** an expert calls `WriterDate.raw("BET JULIAN 1 JAN 1700 AND JULIAN 31 DEC 1710")`, **Then** the string is emitted as-is with no validation.
10. **Given** GEDCOM 5.5.5 mode, **When** a GedcomDate with a non-Gregorian calendar is emitted, **Then** the calendar prefix uses the 5.5.5 escape format (e.g., `@#DJULIAN@`) instead of the GEDCOM 7 keyword format.

---

### User Story 7 - Strict vs Lenient Mode and Warning Control (Priority: P7)

As a developer, I want to choose between strict mode (which catches potential issues early) and lenient mode (which tolerates non-standard output for interoperability), and I want to control how warnings are delivered or suppress them entirely.

**Why this priority**: Many existing genealogy products produce GEDCOM files with non-standard structures. Developers migrating data from these products need lenient mode to produce compatible output. Developers creating new files from scratch benefit from strict mode catching mistakes. Warning control prevents log noise in production.

**Independent Test**: Write the same non-standard structure (e.g., FAMS in GEDCOM 7) in strict mode (verify exception), lenient mode (verify warning), and with warnings suppressed (verify silent output).

**Acceptance Scenarios**:

1. **Given** strict mode is configured, **When** the writer detects a potential issue (e.g., missing HEAD, version-inappropriate structure), **Then** a GedcomWriteException is thrown immediately.
2. **Given** lenient mode is configured (the default), **When** the writer detects a potential issue, **Then** the output is emitted as requested and a warning is delivered to the configured WarningHandler.
3. **Given** a developer configures a custom WarningHandler callback, **When** a warning occurs, **Then** the callback receives a structured warning object with a message and context (tag, line context).
4. **Given** a developer sets the WarningHandler to null, **When** a warning occurs, **Then** no warning is delivered — the writer operates silently.
5. **Given** lenient mode with default WarningHandler, **When** a warning occurs, **Then** the warning is logged to java.util.logging (or stderr) by default.

---

### Edge Cases

- What happens when a developer writes nested structures deeper than the GEDCOM spec typically allows? The writer emits them as-is — it is not a validator.
- How does the writer handle null or empty values? Null values are omitted (tag with no payload); empty strings are treated as no value.
- What happens if the developer forgets to write a TRLR? The writer auto-appends TRLR on close if not explicitly written.
- What happens if the developer calls methods after close()? An IllegalStateException is thrown.
- How does the writer handle very long single-line values in GEDCOM 7 mode? In GEDCOM 7, values are not split by line length — only newlines produce CONT lines. Long lines are emitted as-is.
- What happens if a value contains `\r\n` or `\r`? All line-ending variants are normalized to CONT splits.
- What happens if a developer writes families before individuals (forward references)? This is valid — GEDCOM does not require any particular record order between HEAD and TRLR. The writer is streaming and emits records in the order they are written.
- What happens if auto-generated and developer-provided IDs collide? The writer does not track or validate xref uniqueness — this is entirely the developer's responsibility. No collision detection is performed in any mode.
- What happens if `writer.head(...)` is never called? A minimal HEAD (with GEDC.VERS) is auto-generated. In lenient mode, a warning is delivered. In strict mode, an exception is thrown.
- What happens if a lambda throws an exception? The exception propagates to the caller. Any lines already emitted within that lambda remain in the output stream (the writer is streaming and cannot retract). The writer remains usable for further writes.
- What happens if FAMS/FAMC is used in GEDCOM 7 mode? In lenient mode (default), the writer emits it and delivers a warning since FAMS/FAMC is not part of GEDCOM 7 INDI. In strict mode, an exception is thrown.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The writer MUST produce syntactically valid GEDCOM output (7 or 5.5.5, depending on configuration) with correct level numbering, delimiters, and line endings. GEDCOM 7 is the default configuration but GEDCOM 5.5.5 is equally supported.
- **FR-002**: The writer MUST provide typed context classes (IndividualContext, FamilyContext, EventContext, PersonalNameContext, SourceCitationContext, etc.) with domain-specific methods that guide developers to use correct tags.
- **FR-003**: Every typed context MUST expose the following universal escape hatches accepting any tag string: `structure(tag, value)`, `structure(tag, body)`, `structure(tag, value, body)`, `pointer(tag, Xref)`, `pointer(tag, String)`, `pointer(tag, Xref, body)`, `pointer(tag, String, body)`. Pointer methods accept both Xref handles and plain String IDs. The value+body and pointer+body forms allow structures that carry both a payload and child substructures.
- **FR-004**: Typed convenience methods (e.g., `birth(...)`, `husband(...)`, `personalName(value, body)`) MUST be implemented as wrappers that call the string-based `structure()`/`pointer()` methods internally.
- **FR-005**: The writer MUST use lambda-scoped nesting (e.g., `Consumer<IndividualContext>`) to guarantee that structures are properly closed — no manual `endStructure()` calls. All lambdas MUST execute synchronously within the calling method — after a method returns, all lines for that structure have been emitted.
- **FR-006**: The writer MUST automatically split multi-line values (containing `\n`) into the initial value line plus CONT substructure lines.
- **FR-007**: The writer MUST escape leading `@` characters in GEDCOM 7 mode by doubling the leading `@` only.
- **FR-008**: The writer MUST support GEDCOM 5.5.5 mode via GedcomWriterConfig, including CONC splitting for long lines and all-@@ escaping.
- **FR-009**: The writer MUST automatically manage level numbers — developers never specify levels manually.
- **FR-010**: The writer MUST wrap bare xref identifiers in `@` delimiters automatically when emitting pointers and record xrefs.
- **FR-011**: The writer MUST provide a `writer.head(Consumer<HeadContext>)` method with a typed HeadContext exposing methods for standard HEAD substructures (source with optional children, destination, submitter reference, schema declarations, etc.) plus escape hatches. If `head()` is never called, the writer MUST auto-generate a minimal HEAD record (with GEDC.VERS) and deliver a warning.
- **FR-012**: The writer MUST auto-append a TRLR record on close() if one was not explicitly written.
- **FR-013**: The writer MUST throw IllegalStateException if methods are called after close().
- **FR-014**: The writer MUST treat null values as "no value" (tag only, no payload) and empty strings the same as null.
- **FR-015**: The writer MUST produce output using UTF-8 encoding with LF line endings by default. CRLF line endings MAY be configured via GedcomWriterConfig.Builder.lineEnding().
- **FR-016**: Record-creating methods (individual, family, source, etc.) MUST return an Xref handle representing the auto-generated cross-reference identifier when called without a developer-provided ID.
- **FR-017**: Record-creating methods MUST accept an optional developer-provided string ID that is used as the xref identifier instead of an auto-generated one.
- **FR-018**: Pointer methods (husband, wife, child, etc.) MUST accept both Xref handles and plain string IDs, allowing developers to choose either referencing style.
- **FR-019**: The writer MUST be streaming — records are emitted in the order the developer writes them, with no buffering or reordering. Forward references (referencing a record not yet written) are valid because GEDCOM allows any record order between HEAD and TRLR.
- **FR-020**: GedcomWriter MUST provide `record(String tag, Consumer<GeneralContext> body)`, `record(String tag, String value)`, and `record(String id, String tag, ...)` methods as top-level escape hatches for emitting arbitrary record types not covered by typed convenience methods.
- **FR-021**: The writer MUST provide a GedcomDateBuilder with static factory methods for constructing type-safe dates: exact dates (`date(day, month, year)`), partial dates (`date(month, year)`, `date(year)`), approximate dates (`about()`, `calculated()`, `estimated()`), ranges (`before()`, `after()`, `between()`), periods (`from()`, `to()`, `fromTo()`), BCE dates (`dateBce()`), and non-Gregorian calendars (`julian()`, `hebrew()`, `frenchRepublican()`). Month values MUST use type-safe enums (Month, HebrewMonth, FrenchRepublicanMonth) to prevent invalid month names. A `WriterDate.raw(String)` escape hatch MUST be provided for expert use with no validation.
- **FR-022**: The GedcomDateBuilder MUST validate inputs at construction time: day range per month (e.g., 1-31 for JAN), year >= 1, chronological order for BET...AND and FROM...TO, and calendar-appropriate constraints (e.g., no BCE with Hebrew calendar). Invalid inputs MUST throw IllegalArgumentException.
- **FR-023**: GedcomWriterConfig MUST support strict and lenient modes. In strict mode, potential issues (missing HEAD, version-inappropriate structures, etc.) throw a GedcomWriteException. In lenient mode (the default), the output is emitted as requested and a warning is delivered. Factory methods: `gedcom7()` (lenient), `gedcom7Strict()` (strict), `gedcom555()` (lenient), `gedcom555Strict()` (strict).
- **FR-024**: GedcomWriterConfig MUST accept an optional WarningHandler callback for receiving structured warnings. The default WarningHandler logs to java.util.logging. Setting the WarningHandler to null suppresses all warnings. In strict mode, the WarningHandler is not used (issues throw exceptions instead).
- **FR-025**: IndividualContext MUST provide `familyAsSpouse(Xref/String)` and `familyAsChild(Xref/String)` methods for FAMS/FAMC pointers. In GEDCOM 7 mode, using these delivers a warning (since family linkage is on the FAM record in GEDCOM 7). In GEDCOM 5.5.5 mode, these are standard and produce no warning.

### Key Entities

- **GedcomWriter**: The main entry point — wraps an OutputStream and a GedcomWriterConfig. Provides typed methods for top-level records (individual, family, source, repository, multimedia, submitter, sharedNote) plus `record()` escape hatches for arbitrary record types. Each record method returns an Xref handle. Implements AutoCloseable.
- **GedcomWriterConfig**: Immutable configuration with factory methods `gedcom7()`, `gedcom7Strict()`, `gedcom555()`, and `gedcom555Strict()`. Controls GEDCOM version, strict/lenient mode, line length limits (for CONC), @@ escaping strategy, line ending style, and WarningHandler. Builder pattern for customization.
- **WarningHandler**: Functional interface (callback) that receives structured warning objects. Default implementation logs to java.util.logging. Set to null to suppress all warnings.
- **Xref**: A lightweight, immutable handle representing a cross-reference identifier. Wraps the xref string (either auto-generated or developer-provided). Used to pass references between records in a type-safe manner. Can be obtained from record-creating methods or constructed from a string.
- **WriterDate / GedcomDateBuilder**: WriterDate is an immutable value object representing a GEDCOM date to be written. GedcomDateBuilder provides static factory methods for type-safe date construction. WriterDate.raw() is the expert escape hatch for arbitrary date strings. WriterDate handles version-aware rendering (GEDCOM 7 vs 5.5.5 calendar prefix syntax).
- **Month / HebrewMonth / FrenchRepublicanMonth**: Type-safe enums for calendar-specific month abbreviations. Separate enums per calendar prevent using Hebrew months with Gregorian dates at compile time.
- **HeadContext**: Typed context for the HEAD record, exposing methods for source (with optional children for VERS/NAME), destination, submitter reference, schema declarations, note, etc., plus escape hatches.
- **SchemaContext**: Typed context for HEAD.SCHMA, providing `tag(String extensionTag, String uri)` for declaring extension tag URIs.
- **IndividualContext**: Typed context for INDI records — personalName, birth, death, sex, familyAsSpouse, familyAsChild, and other individual-specific structures.
- **PersonalNameContext**: Typed context for NAME substructures — givenName (GIVN), surname (SURN), namePrefix (NPFX), nameSuffix (NSFX), nickname (NICK), surnamePrefix (SPFX), type (TYPE).
- **FamilyContext**: Typed context for FAM records — husband, wife, child, marriage, divorce, and other family-specific structures.
- **EventContext**: Typed context for event structures (BIRT, DEAT, MARR, etc.) — date (accepts GedcomDate), place, sourceCitation, cause, and other event substructures.
- **SourceCitationContext**: Typed context for SOUR citations within structures — page (PAGE), data with text (DATA/TEXT), quality (QUAY), eventType (EVEN), role (ROLE).
- **SourceContext / RepositoryContext / MultimediaContext / SubmitterContext / NoteContext / AddressContext**: Typed contexts for their respective record/structure types.
- **GeneralContext**: The base/fallback context used by escape hatch lambdas — provides only `structure()`, `pointer()`, and common methods.
- **CommonContext**: Abstract base class shared by all context types, providing shared methods (note, sourceCitation, uid, etc.) and the `structure()`/`pointer()` escape hatches.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer with no GEDCOM knowledge can write a valid GEDCOM 7 file containing individuals, families, names, and events using only IDE auto-complete on the typed context methods — no documentation lookup required for common structures.
- **SC-002**: Output produced by the writer is parseable by the existing GedcomReader without errors — round-trip fidelity for all standard structures.
- **SC-003**: The same application code produces valid GEDCOM 7 or 5.5.5 output by changing only the GedcomWriterConfig — zero code changes required.
- **SC-004**: Extension tags and custom structures can be emitted using the escape hatch methods within any context, without modifying the writer library.
- **SC-005**: The writer adds zero runtime dependencies beyond what the existing parser already requires (which is zero).
- **SC-006**: A developer exporting from a database can loop through their records in any order, using their database primary keys as xref IDs, and produce a valid GEDCOM file without maintaining a separate ID mapping.
- **SC-007**: A developer can construct all standard GEDCOM date forms (exact, approximate, range, period, BCE, non-Gregorian) using the GedcomDateBuilder without knowing GEDCOM date syntax.
- **SC-008**: Strict mode catches common mistakes (missing HEAD, version-inappropriate structures) at write time. Lenient mode allows non-standard output with warnings for interoperability with existing products.

## Assumptions

- The writer is a forward-only streaming writer (no buffering, random access, or reordering of previously written structures).
- The writer does not validate GEDCOM structural correctness beyond basic syntax (level numbers, delimiters) in lenient mode. Strict mode adds additional checks but still does not enforce full cardinality rules.
- Cross-reference uniqueness is the developer's responsibility — the writer does not track or validate xref uniqueness.
- The typed context classes cover the most commonly used GEDCOM 7 structures. Less common structures are accessible via the escape hatches.
- Line endings default to LF. CRLF can be configured if needed.
- GEDCOM allows records in any order between HEAD and TRLR, so forward references are valid and the writer does not need to reorder output.
- Many existing genealogy products produce GEDCOM with non-standard extensions, mixed-version conventions, or vendor-specific quirks. Lenient mode (the default) accommodates this reality by emitting whatever the developer requests and logging warnings rather than rejecting output.
- All lambdas execute synchronously — when a method returns, all GEDCOM lines for that structure have been written to the output stream.
