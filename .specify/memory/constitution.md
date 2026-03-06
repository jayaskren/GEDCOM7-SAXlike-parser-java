<!--
Sync Impact Report
==================
Version change: 1.0.0 -> 1.1.0
Modified principles:
  - V. Test-Driven Development: expanded to include
    post-task agent review and final evaluation requirements
Added sections:
  - VIII. Independent Verification (new principle)
Removed sections: None
Templates requiring updates:
  - .specify/templates/tasks-template.md: ⚠ pending
    (task generation commands should add review tasks per
    story and a final evaluation task; handled by
    /speckit.tasks command instructions, not template itself)
  - .specify/templates/plan-template.md: OK (no updates needed)
  - .specify/templates/spec-template.md: OK (no updates needed)
  - .specify/templates/checklist-template.md: OK (no updates needed)
  - .specify/templates/agent-file-template.md: OK (no updates needed)
Follow-up TODOs: None
-->

# GEDCOM 7 SAX-like Parser Constitution

## Core Principles

### I. GEDCOM 7 Specification Compliance

All parsing behavior MUST conform to the GEDCOM 7.0
specification maintained at
https://github.com/FamilySearch/GEDCOM/tree/main and rendered
at https://gedcom.io. Specifically:

- The parser MUST handle UTF-8 encoded input exclusively
- The parser MUST correctly parse the line format: level,
  optional cross-reference identifier (`@id@`), tag, optional
  line value, and line terminator (CR, LF, or CRLF)
- The parser MUST reconstruct multi-line payloads via `CONT`
  pseudo-structures
- The parser MUST enforce hierarchical structure via level
  numbers (level N+1 is a child of the nearest preceding
  level N)
- The parser MUST reject banned characters (C0 controls
  except tab and line endings, DEL, surrogates)
- The parser MUST handle both standard tags and extension
  tags (underscore-prefixed)
- The parser MUST support all seven record types: Individual,
  Family, Multimedia, Repository, Shared Note, Source,
  Submitter
- The parser MUST require HEAD and TRLR records in every
  dataset

**Rationale**: The specification is the single source of
truth. Deviation from the spec produces data corruption or
interoperability failures with other GEDCOM tools.

### II. SAX-like Event-Driven API

The parser MUST expose a streaming, event-driven API modeled
after SAX (Simple API for XML):

- Callers register callbacks/handlers for parse events
  (start record, end record, structure, line value, etc.)
- The parser MUST NOT require loading the entire file into
  memory before delivering events
- The API MUST allow callers to process GEDCOM data
  incrementally

**Rationale**: GEDCOM files can be very large (millions of
records for institutional datasets). A SAX-like streaming
model enables processing files that exceed available heap
without sacrificing correctness.

### III. Mechanical Sympathy

All implementation decisions MUST consider how the code
interacts with the underlying hardware and JVM runtime:

- Minimize object allocation in the hot parsing loop; prefer
  reusable buffers and primitive types where feasible
- Avoid unnecessary copying of byte arrays and strings;
  prefer slicing or view-based access over the input buffer
- Be cache-friendly: process data sequentially and keep
  working-set data structures compact and contiguous
- Prefer `byte[]` or `ByteBuffer` for I/O rather than
  wrapping in higher-level abstractions prematurely
- Avoid boxing primitives (e.g., `Integer` where `int`
  suffices)
- Be aware of JIT compilation behavior: keep hot methods
  small and predictable to aid inlining

**Rationale**: A parser is CPU- and memory-bound. Respecting
hardware realities (cache lines, branch prediction, memory
hierarchy) directly determines throughput and latency.

### IV. Java Best Practices

The codebase MUST follow established Java conventions:

- Use clear, descriptive naming (classes: PascalCase,
  methods/fields: camelCase, constants: UPPER_SNAKE_CASE)
- Favor immutability: make fields `final` where possible,
  return unmodifiable collections from public APIs
- Use checked exceptions only for recoverable conditions;
  use unchecked exceptions for programming errors
- Follow the principle of least privilege: minimize
  visibility (`private` by default, widen only as needed)
- Provide clear Javadoc on all public API types and methods
- Use `try-with-resources` for all `Closeable`/`AutoCloseable`
  resources
- Target a minimum of Java 11 for language features and API
  compatibility

**Rationale**: Consistent Java idioms reduce cognitive load,
prevent common bugs, and make the library easy to adopt for
Java developers.

### V. Test-Driven Development

All parser behavior MUST be verified by automated tests:

- Write tests before or alongside implementation; tests MUST
  fail before the corresponding implementation is written
- Unit tests MUST cover every GEDCOM line format variation
  (with/without xref, with/without value, CONT lines, etc.)
- Integration tests MUST validate end-to-end parsing of
  representative GEDCOM 7 files
- Edge cases from the GEDCOM 7 spec (escaped `@@`, `@VOID@`,
  empty payloads, maximum nesting) MUST have explicit tests
- Tests MUST run without network access or external
  dependencies

**Rationale**: A parser must be rigorously correct. TDD
ensures specification compliance is continuously verified
and regressions are caught immediately.

### VI. Simplicity and YAGNI

The parser MUST do one thing well: stream GEDCOM 7 events.

- Do NOT build a DOM/tree model into the parser itself;
  that is a separate concern for consumers
- Do NOT add validation beyond what is required for correct
  parsing (e.g., cardinality enforcement is the consumer's
  responsibility unless the SAX API explicitly supports it)
- Do NOT add serialization/writing capabilities unless
  explicitly scoped as a separate feature
- Prefer fewer, well-designed public types over a large API
  surface

**Rationale**: Scope creep turns a focused library into an
unmaintainable monolith. Consumers can layer higher-level
abstractions on top of a clean streaming API.

### VII. Zero External Runtime Dependencies

The parser library MUST NOT depend on any third-party
runtime libraries:

- Only the Java standard library is permitted at runtime
- Test-scoped dependencies (JUnit, assertion libraries) are
  acceptable
- Build tool plugins are acceptable

**Rationale**: A parser library is foundational
infrastructure. External dependencies create version
conflicts, increase attack surface, and complicate adoption.
Mechanical sympathy is best achieved when you control the
entire code path.

### VIII. Independent Verification

All implementation work MUST be independently verified at
two levels:

- **Per-task review**: After each implementation task is
  completed, a separate independent agent MUST review the
  implementation against the feature requirements (spec.md)
  and this constitution. The review agent MUST NOT be the
  same agent that performed the implementation. The review
  MUST check:
  - Functional correctness against acceptance scenarios
  - Compliance with all constitution principles
  - Test coverage for the implemented behavior
  - No regressions in existing tests

- **Final evaluation**: After all tasks for a feature are
  implemented and all tests pass, a comprehensive final
  evaluation MUST be performed by an independent agent. The
  final evaluation MUST:
  - Compare the complete implementation against every
    functional requirement in the spec
  - Verify all acceptance scenarios from every user story
  - Check compliance with every constitution principle
  - Identify any gaps, deviations, or unaddressed
    requirements
  - Produce a written report with findings categorized as
    PASS, GAP (with severity), or DEVIATION (with
    justification)

- Review and evaluation findings that identify gaps MUST be
  addressed before the feature is considered complete.
  LOW-severity gaps MAY be deferred with documented
  rationale.

**Rationale**: Self-review is prone to blind spots. An
independent reviewer catches errors, omissions, and
constitution violations that the implementer overlooked.
Two-level verification (per-task + final) provides both
incremental quality gates and holistic validation.

## GEDCOM 7 Compliance Requirements

The following GEDCOM 7 specification rules are
non-negotiable constraints on the parser:

- **Encoding**: UTF-8 only. BOM (U+FEFF) MUST be accepted
  at file start but MUST NOT be required.
- **Line grammar**: As defined by the GEDCOM 7 ABNF grammar
  in the specification's `extracted-files/` directory.
- **Level semantics**: Level 0 denotes a record. Level N > 0
  is a substructure of the nearest preceding level N-1.
- **CONT handling**: `CONT` at level N+1 appends a newline
  and its payload to the enclosing structure's value.
- **Pointer syntax**: `@identifier@` for cross-references;
  `@VOID@` for null pointers.
- **Tag resolution**: The same tag MAY represent different
  structure types depending on its superstructure context.
  The parser MUST expose sufficient context for consumers
  to disambiguate.
- **Extension tags**: Tags beginning with `_` are extensions.
  The parser MUST parse them without error even if their
  semantics are unknown.

## Development Workflow

- **Build system**: Maven or Gradle (to be determined by
  initial project setup)
- **Branching**: Feature branches off `main`; merge via pull
  request
- **Code review**: All changes MUST be reviewed before merge
- **CI**: Automated tests MUST pass on every PR
- **Versioning**: Semantic versioning (MAJOR.MINOR.PATCH)
  for the library itself
- **Task verification**: Each implementation task MUST be
  followed by an independent agent review (per Principle
  VIII)
- **Feature completion**: Each feature MUST undergo a final
  independent evaluation before being marked complete (per
  Principle VIII)

## Governance

This constitution is the authoritative guide for all
architectural and process decisions in this project.
All pull requests and code reviews MUST verify compliance
with the principles above.

To amend this constitution:

1. Propose the change with rationale in a pull request
2. Document the change in the Sync Impact Report
3. Increment the constitution version per semantic versioning:
   - MAJOR: Principle removal or incompatible redefinition
   - MINOR: New principle or material expansion
   - PATCH: Clarification, wording, or typo fix
4. Update all dependent templates and documentation as
   identified in the Sync Impact Report

Complexity MUST be justified. Any deviation from these
principles MUST be documented with a rationale in the
relevant plan or spec document.

**Version**: 1.1.0 | **Ratified**: 2026-03-04 | **Last Amended**: 2026-03-05
