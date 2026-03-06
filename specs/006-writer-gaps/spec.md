# Feature Specification: GEDCOM Writer API Gaps and Improvements

**Feature Branch**: `006-writer-gaps`
**Created**: 2026-03-05
**Status**: Draft
**Input**: Gaps identified by evaluation agents reviewing the GEDCOM SAX-like writer (feature 004). Ten items ranging from convenience API additions to internal refactoring and documentation improvements.

## Context

The GEDCOM writer (feature 004) was evaluated by multiple agents who exercised the API in realistic scenarios. The evaluation surfaced ten gaps, categorized by severity (MEDIUM, LOW, INFO). This specification addresses all ten gaps as incremental improvements to the existing writer. None of these gaps block basic usage; all represent polish, developer experience, and correctness improvements.

## Clarifications

### Session 2026-03-05

- Q: How should `sharedNote(String text, Consumer)` be disambiguated from the existing `sharedNote(String id, Consumer)` overload, given both have identical Java signatures `(String, Consumer)`? → A: Use a distinct method name `sharedNoteWithText` for the text overload, keeping existing `sharedNote` overloads unchanged.
- Q: Should calendar escape detection in FR-009 use a generic regex `@#D[^@]+@` or an allow-list of 6 known GEDCOM calendars? → A: Allow-list of known GEDCOM calendars (GREGORIAN, JULIAN, HEBREW, FRENCH R, ROMAN, UNKNOWN). GEDCOM is mature and unlikely to add new calendars, and an allow-list is more precise.
- Q: Should US11 (Structure Validation) be implemented in this feature or fully deferred? → A: Fully defer US11. Implement US1-US10 only. US11 remains documented in the spec for future work.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Personal Name Convenience Overload (Priority: P1)

As a developer writing GEDCOM files, I want to pass given name and surname as separate arguments to a convenience method, so that the writer auto-formats the GEDCOM personal name string (`"Given /Surname/"`) and auto-populates GIVN and SURN substructures without requiring me to know the GEDCOM name encoding convention.

**Why this priority**: This is the highest-impact usability improvement. The GEDCOM personal name format (`"John /Doe/"`) with slash-delimited surnames is non-obvious and error-prone. Most developers working with names have given and surname as separate fields (from a database, form input, etc.). A convenience overload eliminates the most common source of formatting mistakes.

**Independent Test**: Call `personalName("John", "Doe")` and verify the output produces `1 NAME John /Doe/` followed by `2 GIVN John` and `2 SURN Doe`. Verify the existing `personalName(String)` overload still works unchanged.

**Acceptance Scenarios**:

1. **Given** an IndividualContext, **When** the developer calls `indi.personalName("John", "Doe")`, **Then** the output contains `1 NAME John /Doe/` followed by `2 GIVN John` and `2 SURN Doe`.
2. **Given** an IndividualContext, **When** the developer calls `indi.personalName("John", "Doe", name -> { name.nickname("Johnny"); })`, **Then** the output contains `1 NAME John /Doe/`, `2 GIVN John`, `2 SURN Doe`, and `2 NICK Johnny`.
3. **Given** an IndividualContext, **When** the developer calls `indi.personalName("Maria", null)`, **Then** the output contains `1 NAME Maria` with `2 GIVN Maria` and no SURN line (null surname is omitted).
4. **Given** an IndividualContext, **When** the developer calls `indi.personalName(null, "Doe")`, **Then** the output contains `1 NAME /Doe/` with `2 SURN Doe` and no GIVN line (null given name is omitted from both NAME value prefix and GIVN).
5. **Given** an IndividualContext, **When** the developer calls the existing `indi.personalName("John /Doe/")`, **Then** behavior is unchanged from the current implementation -- no auto-population of GIVN/SURN.

---

### User Story 2 - Shared Note Text Value (Priority: P2)

As a developer, I want to pass note text directly to the `sharedNote()` method, so that I can create shared notes with inline text values without requiring a separate method call inside the lambda body.

**Why this priority**: The current `sharedNote()` produces `0 @N1@ SNOTE` with no value on the record line. GEDCOM 7 specifies that SNOTE records carry their text as the record-level value (e.g., `0 @N1@ SNOTE This is the note text`). Without this, creating a shared note with text requires using the escape hatch or a workaround.

**Independent Test**: Call `writer.sharedNoteWithText("This is the note text", note -> {})` and verify the output produces `0 @N1@ SNOTE This is the note text`.

**Acceptance Scenarios**:

1. **Given** a GedcomWriter, **When** the developer calls `writer.sharedNoteWithText("This is a note", note -> { ... })`, **Then** the output contains `0 @N1@ SNOTE This is a note`.
2. **Given** a GedcomWriter, **When** the developer calls `writer.sharedNoteWithText("Line one\nLine two", note -> { ... })`, **Then** the output contains `0 @N1@ SNOTE Line one` followed by `1 CONT Line two`.
3. **Given** a GedcomWriter, **When** the developer calls `writer.sharedNoteWithText("id1", "Note text", note -> { ... })` with a developer-provided ID, **Then** the output contains `0 @id1@ SNOTE Note text`.
4. **Given** a GedcomWriter, **When** the developer calls the existing `writer.sharedNote(note -> { ... })` (no text), **Then** behavior is unchanged -- produces `0 @N1@ SNOTE` with no value.
5. **Given** a GedcomWriter, **When** the developer calls the existing `writer.sharedNote("id1", note -> { ... })` (ID, no text), **Then** behavior is unchanged -- produces `0 @id1@ SNOTE` with no value.

---

### User Story 3 - LDS Ordinance Typed Methods (Priority: P2)

As a developer writing GEDCOM files for families with LDS (Latter-day Saints) ordinance data, I want typed convenience methods for LDS ordinance events (baptism, confirmation, endowment, sealing to parents, sealing to spouse), so that I can write LDS ordinance records with proper GEDCOM tags (BAPL, CONL, ENDL, INIL, SLGC, SLGS) and typed `EventContext` access without needing to use the raw `structure()` escape hatch.

**Why this priority**: LDS ordinance tags are part of the GEDCOM 7 specification and are widely used by FamilySearch, Ancestral Quest, RootsMagic, and other major genealogy vendors. A significant portion of real-world GEDCOM files contain these structures. Without typed methods, developers must know the exact GEDCOM tag abbreviations and use escape hatches, which defeats the purpose of the typed API.

**Independent Test**: Call `indi.ldsBaptism(body -> { body.date("15 JAN 1900"); body.place("Salt Lake City, UT"); body.structure("TEMP", "SLAKE"); body.structure("STAT", "COMPLETED"); })` and verify the output contains `1 BAPL` with `2 DATE 15 JAN 1900`, `2 PLAC Salt Lake City, UT`, `2 TEMP SLAKE`, and `2 STAT COMPLETED`.

**Acceptance Scenarios**:

1. **Given** an IndividualContext, **When** the developer calls `indi.ldsBaptism(body -> { body.date("15 JAN 1900"); })`, **Then** the output contains `1 BAPL` followed by `2 DATE 15 JAN 1900`.
2. **Given** an IndividualContext, **When** the developer calls `indi.ldsConfirmation(body -> { ... })`, **Then** the output contains `1 CONL`.
3. **Given** an IndividualContext, **When** the developer calls `indi.ldsEndowment(body -> { ... })`, **Then** the output contains `1 ENDL`.
4. **Given** an IndividualContext, **When** the developer calls `indi.ldsInitiatory(body -> { ... })`, **Then** the output contains `1 INIL`.
5. **Given** an IndividualContext, **When** the developer calls `indi.ldsSealingToParents(body -> { ... })`, **Then** the output contains `1 SLGC`.
6. **Given** a FamilyContext, **When** the developer calls `fam.ldsSealingToSpouse(body -> { ... })`, **Then** the output contains `1 SLGS`.
7. **Given** LDS ordinance methods, **When** the lambda body receives an `EventContext`, **Then** typed `date()`, `place()`, and `structure()` methods are available for TEMP, STAT, and other LDS substructures.
8. **Given** a developer using GEDCOM 5.5.5 mode, **When** LDS ordinance methods are called, **Then** the output is identical (LDS tags are the same across versions).

---

### User Story 4 - Public Builder Methods for escapeAllAt and concEnabled (Priority: P4)

As an expert developer, I want to independently control `@` escaping strategy and CONC splitting behavior through the `GedcomWriterConfig.Builder`, so that I can create custom configurations not covered by the version presets (e.g., GEDCOM 7 mode with all-`@@` escaping for compatibility with older readers, or GEDCOM 5.5.5 mode without CONC splitting).

**Why this priority**: The current `escapeAllAt()` and `concEnabled()` methods on `GedcomWriterConfig.Builder` are package-private. While the version preset factory methods (`gedcom7()`, `gedcom555()`) set these appropriately, experts cannot override them independently. This blocks legitimate use cases like writing GEDCOM 7 files targeted at older readers that expect all-`@@` escaping.

**Independent Test**: From a package outside `org.gedcom7.writer`, call `new GedcomWriterConfig.Builder().escapeAllAt(true).build()` and verify compilation succeeds and the config reflects the setting.

**Acceptance Scenarios**:

1. **Given** a developer in an external package, **When** they call `new GedcomWriterConfig.Builder().escapeAllAt(true).build()`, **Then** the code compiles and `config.isEscapeAllAt()` returns `true`.
2. **Given** a developer in an external package, **When** they call `new GedcomWriterConfig.Builder().concEnabled(true).maxLineLength(200).build()`, **Then** the code compiles and `config.isConcEnabled()` returns `true`.
3. **Given** the existing `gedcom7()` and `gedcom555()` factory methods, **When** they are called, **Then** their behavior is unchanged.

---

### User Story 5 - Automatic HEAD.CHAR for GEDCOM 5.5.5 (Priority: P5)

As a developer writing GEDCOM 5.5.5 files, I want the writer to automatically include `1 CHAR UTF-8` in the HEAD block, so that GEDCOM 5.5.5 readers can correctly identify the character encoding without requiring me to add it manually.

**Why this priority**: GEDCOM 5.5.5 requires the `CHAR` tag in the HEAD record to declare the character encoding. Without it, some 5.5.5 readers may assume ASCII or another encoding. The writer already auto-generates `GEDC`/`VERS` in the HEAD, so `CHAR` should follow the same pattern for 5.5.5 mode.

**Independent Test**: Create a writer with `gedcom555()` config, call `writer.head(head -> {})`, and verify the output contains `1 CHAR UTF-8` within the HEAD block.

**Acceptance Scenarios**:

1. **Given** a GedcomWriter configured with `gedcom555()`, **When** `writer.head(head -> { head.source("MyApp"); })` is called, **Then** the HEAD output contains `1 CHAR UTF-8` (auto-generated alongside `1 GEDC` / `2 VERS 5.5.5`).
2. **Given** a GedcomWriter configured with `gedcom7()`, **When** `writer.head(...)` is called, **Then** no CHAR line is emitted (CHAR is not part of GEDCOM 7).
3. **Given** a GedcomWriter configured with `gedcom555()`, **When** the developer never calls `writer.head(...)` and the auto-generated HEAD is emitted on close, **Then** the auto-generated HEAD includes `1 CHAR UTF-8`.

---

### User Story 6 - Unchecked Exception for Writer Errors (Priority: P6)

As a developer using the writer API with lambdas, I want `GedcomWriteException` to be unchecked (extend `RuntimeException`), so that I can use the lambda-based API without needing `try/catch` blocks or wrapper patterns inside `Consumer<T>` lambdas.

**Why this priority**: `Consumer<T>` cannot throw checked exceptions. The current API declares `throws GedcomWriteException` on top-level methods like `individual()`, `family()`, etc., but the lambdas receiving typed contexts cannot propagate checked exceptions. The existing wrapping/unwrapping pattern (catching `RuntimeException` whose cause is `GedcomWriteException`) works but creates friction in test code and production code alike. Making the exception unchecked eliminates this mismatch.

**Independent Test**: Write code that calls `writer.individual(indi -> { ... })` without any `try/catch` for `GedcomWriteException`. Verify the code compiles. Verify that if an error occurs during writing, a `GedcomWriteException` (now extending `RuntimeException`) is thrown and can be caught.

**Acceptance Scenarios**:

1. **Given** `GedcomWriteException` extends `RuntimeException`, **When** the developer calls `writer.individual(indi -> { ... })`, **Then** no `throws` declaration is required on the calling method.
2. **Given** a write error occurs inside a lambda, **When** the exception propagates, **Then** it is a `GedcomWriteException` (not wrapped in another `RuntimeException`).
3. **Given** existing code that catches `GedcomWriteException`, **When** it is recompiled against the updated library, **Then** catch blocks still work (catching a `RuntimeException` subclass in a `catch(GedcomWriteException)` is valid Java).
4. **Given** the change from checked to unchecked, **When** existing calling code has `throws GedcomWriteException` declarations, **Then** the code still compiles (Java allows declaring unchecked exceptions in throws clauses, though they are unnecessary).

**Migration note**: This is a source-compatible but binary-incompatible change. Existing compiled code that specifically catches `GedcomWriteException` would need to be recompiled. Since the writer is pre-1.0, this is acceptable.

---

### User Story 7 - Generic Event Method on IndividualContext (Priority: P7)

As a developer, I want a generic `event(String tag, Consumer<EventContext> body)` method on `IndividualContext`, so that I can emit individual events not covered by the typed convenience methods (immigration, emigration, census, probate, naturalization, etc.) without dropping down to the raw `structure()` escape hatch.

**Why this priority**: The current `IndividualContext` provides typed methods for `birth()`, `death()`, `christening()`, `burial()`, and `residence()`. GEDCOM defines many more individual events (IMMI, EMIG, CENS, PROB, NATU, ADOP, GRAD, RETI, WILL, EVEN, etc.). Without a generic `event()` method, developers must use `structure("IMMI", body -> { ... })` which yields a `GeneralContext` instead of `EventContext`, losing access to typed date/place/cause methods.

**Independent Test**: Call `indi.event("IMMI", body -> { body.date(...); body.place("New York"); })` and verify the output contains `1 IMMI` with `2 DATE ...` and `2 PLAC New York`. Verify the lambda receives an `EventContext`, not a `GeneralContext`.

**Acceptance Scenarios**:

1. **Given** an IndividualContext, **When** the developer calls `indi.event("IMMI", body -> { body.date("15 JAN 1905"); body.place("Ellis Island, NY"); })`, **Then** the output contains `1 IMMI` followed by `2 DATE 15 JAN 1905` and `2 PLAC Ellis Island, NY`.
2. **Given** an IndividualContext, **When** the developer calls `indi.event("CENS", body -> { body.date("1 JUN 1900"); })`, **Then** the output contains `1 CENS` followed by `2 DATE 1 JUN 1900`.
3. **Given** a FamilyContext, **When** a similar `event(String tag, Consumer<EventContext>)` method exists, **Then** family-specific events not covered by `marriage()`/`divorce()`/`annulment()` (e.g., ENGA, MARB, MARC) can be emitted with typed EventContext.
4. **Given** an IndividualContext, **When** the developer calls `indi.birth(body -> { ... })`, **Then** the existing typed method still works unchanged.

---

### User Story 8 - Date String @-Escaping Fix for 5.5.5 Calendar Escapes (Priority: P8)

As a developer writing GEDCOM 5.5.5 files with raw date strings containing calendar escape prefixes (e.g., `@#DGREGORIAN@`, `@#DJULIAN@`), I want the date method to not double-escape the `@` characters in these calendar prefixes, so that the output remains valid GEDCOM 5.5.5.

**Why this priority**: In GEDCOM 5.5.5, calendar escapes use `@#D...@` syntax which looks like an `@`-delimited value. When `EventContext.date(String)` passes this through the standard value escaping path in 5.5.5 mode (which doubles all `@`), the calendar escape becomes `@@#DGREGORIAN@@`, which is incorrect. This is a correctness bug in a specific edge case.

**Independent Test**: In GEDCOM 5.5.5 mode, call `event.date("@#DJULIAN@ 15 JAN 1700")` and verify the output is `2 DATE @#DJULIAN@ 15 JAN 1700` (no double-escaping of the calendar prefix `@` characters).

**Acceptance Scenarios**:

1. **Given** GEDCOM 5.5.5 mode, **When** the developer calls `event.date("@#DJULIAN@ 15 JAN 1700")`, **Then** the output contains `2 DATE @#DJULIAN@ 15 JAN 1700` (calendar escapes are preserved, not double-escaped).
2. **Given** GEDCOM 5.5.5 mode, **When** the developer calls `event.date("@#DGREGORIAN@ 1 JAN 1900")`, **Then** the output contains `2 DATE @#DGREGORIAN@ 1 JAN 1900`.
3. **Given** GEDCOM 5.5.5 mode, **When** the developer calls `event.date("15 JAN 1900")` (no calendar escape), **Then** standard escaping rules apply unchanged.
4. **Given** a developer uses `WriterDate.raw("@#DJULIAN@ 15 JAN 1700")`, **Then** the raw string is emitted as-is (existing behavior, unaffected by this change).
5. **Given** GEDCOM 7 mode, **When** the developer calls `event.date("15 JAN 1700")`, **Then** behavior is unchanged (GEDCOM 7 does not use `@#D...@` calendar escapes).

---

### User Story 9 - Extract Duplicate emitEvent to CommonContext (Priority: P9)

As a maintainer of the writer codebase, I want the duplicated `emitEvent()` private method to be extracted into `CommonContext` as a protected method, so that the codebase follows DRY principles and future context classes with events do not need to re-implement the same logic.

**Why this priority**: Both `IndividualContext.emitEvent()` and `FamilyContext.emitEvent()` contain identical code (emit a tag line, create an `EventContext`, call the body). This is a straightforward refactoring that reduces maintenance burden and prevents the two copies from diverging.

**Independent Test**: After refactoring, run the full existing test suite. Verify all tests pass unchanged. Verify the output of `indi.birth(...)` and `fam.marriage(...)` is identical to the pre-refactoring output.

**Acceptance Scenarios**:

1. **Given** the refactored code, **When** `indi.birth(body -> { body.date("15 MAR 1955"); })` is called, **Then** the output is identical to the current implementation.
2. **Given** the refactored code, **When** `fam.marriage(body -> { body.date("1 JUN 1980"); })` is called, **Then** the output is identical to the current implementation.
3. **Given** the refactored code, **When** a new context class needs to emit events, **Then** it can call the shared `emitEvent()` method from `CommonContext` without duplicating the logic.

---

### User Story 10 - Sex Enum Overload (Priority: P10)

As a beginner developer, I want a `Sex` enum that constrains the valid values for `indi.sex(...)`, so that I am guided by IDE auto-complete to use valid GEDCOM values (`M`, `F`, `X`, `U`) instead of accidentally writing invalid values like `"Male"` or `"Female"`.

**Why this priority**: The current `sex(String)` method accepts any string. A beginner unaware of GEDCOM conventions might write `indi.sex("Male")` which produces syntactically valid but semantically wrong GEDCOM. Adding an enum overload provides guide rails while keeping the string overload for experts or future values.

**Independent Test**: Call `indi.sex(Sex.MALE)` and verify the output is `1 SEX M`. Verify `indi.sex("M")` still works unchanged.

**Acceptance Scenarios**:

1. **Given** an IndividualContext, **When** the developer calls `indi.sex(Sex.MALE)`, **Then** the output contains `1 SEX M`.
2. **Given** an IndividualContext, **When** the developer calls `indi.sex(Sex.FEMALE)`, **Then** the output contains `1 SEX F`.
3. **Given** an IndividualContext, **When** the developer calls `indi.sex(Sex.INTERSEX)`, **Then** the output contains `1 SEX X`.
4. **Given** an IndividualContext, **When** the developer calls `indi.sex(Sex.UNKNOWN)`, **Then** the output contains `1 SEX U`.
5. **Given** an IndividualContext, **When** the developer calls the existing `indi.sex("M")`, **Then** behavior is unchanged.
6. **Given** an IndividualContext, **When** the developer calls `indi.sex(null)` (either overload), **Then** no SEX line is emitted (existing null-guard behavior).

---

### User Story 11 - Structure Validation (Advisory/Future) (Priority: P11)

As a developer, I want optional validation that warns when I write substructures that are invalid for a given record type (e.g., `HUSB` inside an `INDI`), so that I can catch mistakes early during development while retaining the freedom to write non-standard structures in production.

**Why this priority**: This is the lowest priority because the writer is intentionally not a validator (per the original spec). However, the evaluation identified that zero validation means developers can silently produce nonsensical GEDCOM. This story captures the requirement for future consideration, potentially as an opt-in strict validation mode layered on top of the existing strict/lenient system.

**Independent Test**: With validation enabled, call `indi.structure("HUSB", "@I2@")` and verify a warning is emitted. With validation disabled, verify the same call produces output silently.

**Acceptance Scenarios**:

1. **Given** validation mode is enabled (future config option), **When** the developer writes `indi.structure("HUSB", "@I2@")`, **Then** a warning is delivered indicating HUSB is not a valid INDI substructure.
2. **Given** validation mode is disabled (the default, current behavior), **When** the developer writes any structure, **Then** no structural validation is performed.
3. **Given** validation mode is enabled in strict mode, **When** an invalid substructure is written, **Then** an exception is thrown.

**Implementation note**: This story is advisory and may be deferred beyond this feature. The writer's streaming nature limits how much validation is feasible without buffering. A practical approach would be a static allow-list of valid child tags per context type, checked at emit time.

---

### Edge Cases

- What happens when `personalName("", "")` is called with empty strings? Both given name and surname are treated as null/absent, equivalent to `personalName("")`.
- What happens when `personalName("John", "Doe")` is called and the developer also manually adds GIVN/SURN in the body lambda? Duplicate GIVN/SURN lines are emitted. The writer does not detect or prevent duplicates (consistent with its non-validating streaming design).
- What happens when `sharedNoteWithText("text", null)` is called with a null body? The note text is emitted on the record line with no child structures.
- What happens when `date("@#DJULIAN@ @unusual@text")` mixes calendar escapes with other `@` characters? Only the recognized calendar escape prefix is protected from double-escaping; other `@` characters in the date string follow standard escaping rules.
- What happens when `Sex` enum is used in GEDCOM 5.5.5 mode? The enum values are the same across versions (`M`, `F`, `X`, `U`), so behavior is identical.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: IndividualContext MUST provide a `personalName(String givenName, String surname)` overload that formats the NAME value as `"givenName /surname/"` and auto-emits `GIVN` and `SURN` substructures. A `personalName(String givenName, String surname, Consumer<PersonalNameContext> body)` variant MUST also be provided for adding additional name parts.
- **FR-002**: The existing `personalName(String)` and `personalName(String, Consumer<PersonalNameContext>)` methods MUST remain unchanged. The new overloads are additive.
- **FR-003**: `GedcomWriter` MUST provide `sharedNoteWithText(String text, Consumer<NoteContext>)` and `sharedNoteWithText(String id, String text, Consumer<NoteContext>)` methods that emit the text as the record-level value on the SNOTE line. The method is named `sharedNoteWithText` to avoid signature ambiguity with the existing `sharedNote(String id, Consumer<NoteContext>)` overload. Existing `sharedNote()` overloads remain unchanged.
- **FR-004**: IndividualContext MUST provide typed methods for LDS individual ordinances: `ldsBaptism(Consumer<EventContext>)` (emits BAPL), `ldsConfirmation(Consumer<EventContext>)` (emits CONL), `ldsEndowment(Consumer<EventContext>)` (emits ENDL), `ldsInitiatory(Consumer<EventContext>)` (emits INIL), and `ldsSealingToParents(Consumer<EventContext>)` (emits SLGC). FamilyContext MUST provide `ldsSealingToSpouse(Consumer<EventContext>)` (emits SLGS). All LDS methods provide typed EventContext for date, place, and structure (TEMP, STAT) access.
- **FR-005**: `GedcomWriterConfig.Builder.escapeAllAt(boolean)` and `concEnabled(boolean)` MUST be `public` methods, accessible from any package.
- **FR-006**: When configured for GEDCOM 5.5.5, the writer MUST auto-generate `1 CHAR UTF-8` in the HEAD block, immediately after the GEDC/VERS substructure. This MUST apply to both explicit `head()` calls and auto-generated HEAD records.
- **FR-007**: `GedcomWriteException` MUST extend `RuntimeException` instead of `Exception`. The `throws GedcomWriteException` declarations on `GedcomWriter` methods MAY be removed or retained for documentation purposes.
- **FR-008**: IndividualContext and FamilyContext MUST provide a generic `event(String tag, Consumer<EventContext> body)` method that emits an event with the given tag and provides a typed EventContext to the body lambda.
- **FR-009**: `EventContext.date(String)` MUST detect GEDCOM 5.5.5 calendar escape prefixes from the known set (`@#DGREGORIAN@`, `@#DJULIAN@`, `@#DHEBREW@`, `@#DFRENCH R@`, `@#DROMAN@`, `@#DUNKNOWN@`) and skip `@`-doubling for those recognized prefixes. Standard `@`-escaping rules apply to the remainder of the date string. Unrecognized `@#D...@` patterns follow standard escaping.
- **FR-010**: The identical `emitEvent()` private methods in `IndividualContext` and `FamilyContext` MUST be extracted to a shared `protected` method in `CommonContext`.
- **FR-011**: A `Sex` enum MUST be provided with values `MALE("M")`, `FEMALE("F")`, `INTERSEX("X")`, and `UNKNOWN("U")`. IndividualContext MUST provide a `sex(Sex)` overload. The existing `sex(String)` method MUST remain unchanged.
- **FR-012**: Structural validation (US11) is OPTIONAL for this feature and MAY be deferred. If implemented, it MUST be opt-in and MUST NOT change default behavior.

### Key Entities

- **Sex**: Enum with values MALE, FEMALE, INTERSEX, UNKNOWN. Each value maps to the corresponding single-character GEDCOM code (M, F, X, U). Belongs in the `org.gedcom7.writer` package alongside existing public types.
- **GedcomWriteException**: Existing exception class, changed from `extends Exception` to `extends RuntimeException`. No new fields or methods required.
- **IndividualContext**: Existing context class, extended with `personalName(String, String)`, `personalName(String, String, Consumer)`, `event(String, Consumer)`, and LDS ordinance methods (`ldsBaptism`, `ldsConfirmation`, `ldsEndowment`, `ldsInitiatory`, `ldsSealingToParents`).
- **FamilyContext**: Existing context class, extended with `event(String, Consumer)` and `ldsSealingToSpouse(Consumer)` methods.
- **CommonContext**: Existing abstract base class, extended with a shared `protected emitEvent(String, Consumer)` method.
- **GedcomWriter**: Existing writer class, extended with `sharedNoteWithText(String text, Consumer)` and `sharedNoteWithText(String id, String text, Consumer)` methods.
- **GedcomWriterConfig.Builder**: Existing builder class, `escapeAllAt()` and `concEnabled()` changed from package-private to `public`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer can write `indi.personalName("John", "Doe")` and get correctly formatted `NAME`, `GIVN`, and `SURN` lines without knowing GEDCOM name encoding rules.
- **SC-002**: A developer can create shared notes with inline text via `writer.sharedNoteWithText("text", body)` without escape hatch workarounds.
- **SC-003**: Expert developers can customize `escapeAllAt` and `concEnabled` independently from outside the `org.gedcom7.writer` package.
- **SC-004**: Developers can write LDS ordinances using typed methods (`indi.ldsBaptism()`, `fam.ldsSealingToSpouse()`, etc.) with proper GEDCOM tags (BAPL, CONL, ENDL, INIL, SLGC, SLGS) and typed EventContext access.
- **SC-005**: GEDCOM 5.5.5 files produced by the writer include `1 CHAR UTF-8` in the HEAD block, satisfying 5.5.5 reader requirements.
- **SC-006**: Lambda-based writer code compiles without `try/catch` blocks for `GedcomWriteException`, reducing test and production code boilerplate.
- **SC-007**: Developers can emit any GEDCOM individual or family event (IMMI, CENS, EMIG, etc.) using `event("TAG", body)` with full typed `EventContext` access (date, place, cause).
- **SC-008**: GEDCOM 5.5.5 date strings containing `@#D...@` calendar escapes are output correctly without double-escaping.
- **SC-009**: The codebase has zero duplicated `emitEvent()` methods -- the shared implementation lives in `CommonContext`.
- **SC-010**: IDE auto-complete on `indi.sex()` shows both `Sex` enum values and the existing string overload, guiding beginners toward valid values.
- **SC-011**: All existing tests pass without modification after these changes (except for `GedcomWriteException` catch blocks which may need removal of now-unnecessary `throws` declarations).

## Verification Requirements (Constitution Principle VIII)

Per the project constitution's Independent Verification
principle, this feature MUST include:

### Per-Task Review

After each implementation task is completed, a separate
independent agent MUST review the implementation against:

- The acceptance scenarios defined in this spec
- All constitution principles (I through VIII)
- Test coverage for the implemented behavior
- No regressions in existing tests

The review agent MUST NOT be the same agent that performed
the implementation. Review findings MUST be documented and
any identified gaps addressed before proceeding to the next
task.

### Final Evaluation

After all tasks are implemented and all tests pass, a
comprehensive final evaluation MUST be performed by an
independent agent. The final evaluation MUST:

- Compare the complete implementation against every
  functional requirement (FR-001 through FR-012)
- Verify all acceptance scenarios from every user story
  (Stories 1 through 11)
- Check compliance with every constitution principle
- Identify any gaps, deviations, or unaddressed requirements
- Produce a written report with findings categorized as
  PASS, GAP (with severity HIGH/MEDIUM/LOW), or DEVIATION
  (with justification)

Gaps rated HIGH MUST be addressed before the feature is
considered complete. MEDIUM gaps SHOULD be addressed. LOW
gaps MAY be deferred with documented rationale.

## Assumptions

- The writer is pre-1.0, so binary-incompatible changes (like making `GedcomWriteException` unchecked) are acceptable.
- All changes are additive (new overloads) or access-level relaxations (package-private to public), except for the `GedcomWriteException` hierarchy change and the `emitEvent()` refactoring. No existing public method signatures are removed.
- The `personalName(String, String)` convenience method uses a simple formatting rule: `"given /surname/"`. Edge cases like multiple given names or compound surnames are handled by the existing `personalName(String)` overload or the body lambda.
- Structural validation (US11) is explicitly out of scope for the initial implementation and captured here for future reference only.
- The `CHAR UTF-8` auto-generation for 5.5.5 does not provide a way to override the character encoding value, since the writer only supports UTF-8 output.
