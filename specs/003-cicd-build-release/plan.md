# Implementation Plan: CI/CD Build & Release Pipeline

**Branch**: `003-cicd-build-release` | **Date**: 2026-03-05 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-cicd-build-release/spec.md`

## Summary

Set up GitHub Actions CI/CD for the GEDCOM 7 parser library: automated build/test on every push and PR, a multi-version Java build matrix (11, 17, 21), and a release workflow that builds and publishes versioned artifacts (library JAR, sources JAR, Javadoc JAR) to a GitHub Release when a `v*.*.*` tag is pushed.

## Technical Context

**Language/Version**: Java 11+ (source/target compatibility), built with Gradle (Kotlin DSL)
**Primary Dependencies**: None at runtime (zero-dependency library); JUnit 5 for tests
**Storage**: N/A
**Testing**: JUnit 5 via `./gradlew test`
**Target Platform**: GitHub Actions (ubuntu-latest runners)
**Project Type**: Library (CI/CD infrastructure for existing library)
**Performance Goals**: Build + test pipeline completes within 10 minutes
**Constraints**: No secrets required for CI builds; release workflow needs default `GITHUB_TOKEN` only
**Scale/Scope**: Single repository, ~250 tests, small codebase

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. GEDCOM 7 Spec Compliance | N/A | CI/CD doesn't change parser behavior |
| II. SAX-like Event-Driven API | N/A | No API changes |
| III. Mechanical Sympathy | N/A | No runtime code changes |
| IV. Java Best Practices | PASS | Gradle config follows conventions; Javadoc JAR generation uses standard plugin |
| V. Test-Driven Development | PASS | CI enforces all tests run on every push/PR |
| VI. Simplicity and YAGNI | PASS | Two focused workflow files, minimal configuration |
| VII. Zero External Runtime Dependencies | PASS | GitHub Actions and Gradle plugins are build-time only |

**Gate result**: PASS — no violations.

## Project Structure

### Documentation (this feature)

```text
specs/003-cicd-build-release/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── quickstart.md        # Phase 1 output
└── checklists/
    └── requirements.md  # Spec quality checklist
```

### Source Code (repository root)

```text
.github/
└── workflows/
    ├── ci.yml           # Build + test on push/PR (US1, US3)
    └── release.yml      # Build + publish artifacts on version tag (US2)

build.gradle.kts         # Updated: add java, maven-publish, sources/javadoc JAR tasks
settings.gradle.kts      # Existing (no changes expected)
```

**Structure Decision**: Two separate workflow files for clear separation of concerns — `ci.yml` runs on every push/PR with a build matrix, `release.yml` runs only on version tags and produces release artifacts. This keeps each workflow focused and easy to maintain.

## Complexity Tracking

No constitution violations to justify.
