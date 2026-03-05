# Tasks: CI/CD Build & Release Pipeline

**Input**: Design documents from `/specs/003-cicd-build-release/`
**Prerequisites**: plan.md (required), spec.md (required), research.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Commit Gradle wrapper and create workflow directory structure

- [X] T001 Commit Gradle wrapper files (gradle/wrapper/gradle-wrapper.properties, gradlew, gradlew.bat) to the repository — these are currently untracked and required by CI runners
- [X] T002 Create .github/workflows/ directory at the repository root
- [X] T003 Verify T001 and T002 against spec FR-001 through FR-010 and constitution principles IV (Java Best Practices) and VII (Zero External Runtime Dependencies)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Update build.gradle.kts to support sources JAR, Javadoc JAR, and version override from CLI

**CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Update build.gradle.kts to enable `withSourcesJar()` and `withJavadocJar()` in the `java {}` block
- [X] T005 Update build.gradle.kts to allow version override via Gradle property: if `project.hasProperty("releaseVersion")` then set `version = project.property("releaseVersion")`, otherwise keep the existing default version
- [X] T006 Verify `./gradlew build` still passes locally with the updated build.gradle.kts, and verify that `./gradlew build -PreleaseVersion=1.0.0` produces JARs named with version `1.0.0`
- [X] T007 Verify T004 through T006 against spec FR-005 (sources, Javadoc, library JARs), FR-006 (version matches tag), and constitution principle VI (Simplicity)

**Checkpoint**: build.gradle.kts ready — workflow files can now reference Gradle tasks

---

## Phase 3: User Story 1 - Automated Build & Test on Every Push (Priority: P1)

**Goal**: Every push and PR triggers automated compilation and test execution with results visible as status checks.

**Independent Test**: Push a commit to a non-main branch and verify the CI workflow runs, executes tests, and reports pass/fail.

### Implementation for User Story 1

- [X] T008 [US1] Create .github/workflows/ci.yml with: trigger on push (all branches) and pull_request (targeting main); concurrency group per branch with cancel-in-progress; single job "build" that runs on ubuntu-latest; steps: checkout, setup-java (temurin 17), gradle build with `./gradlew build`; cache Gradle dependencies via `actions/setup-java` built-in caching
- [X] T009 [US1] Verify T008 against spec FR-001 (push triggers build), FR-002 (PR triggers build), FR-003 (status checks on PR), FR-010 (completes within 10 minutes), and constitution principle V (Test-Driven Development — CI enforces tests on every push)

**Checkpoint**: Pushing to any branch or opening a PR triggers build+test with visible results

---

## Phase 4: User Story 3 - Build Matrix for Compatibility Assurance (Priority: P3)

**Goal**: The CI pipeline runs the test suite against Java 11, 17, and 21 in parallel.

**Independent Test**: Push a commit and verify 3 separate matrix jobs run (one per Java version) with individual results visible.

> **Note**: US3 is implemented before US2 because the build matrix modifies ci.yml (created in US1) and is a prerequisite for a robust release pipeline.

### Implementation for User Story 3

- [X] T010 [US3] Update .github/workflows/ci.yml to add a strategy matrix with java versions [11, 17, 21]; update the setup-java step to use `matrix.java`; name each job to show the Java version (e.g., "Build (Java 11)")
- [X] T011 [US3] Verify T010 against spec FR-008 (at least 3 runtime versions: minimum 11, middle LTS, latest LTS) and acceptance scenarios US3-1 through US3-3 (individual version results visible, specific failing version identifiable)

**Checkpoint**: CI runs 3 parallel matrix jobs (Java 11, 17, 21) with individual status

---

## Phase 5: User Story 2 - Milestone-Triggered Release Artifacts (Priority: P2)

**Goal**: Pushing a `v*.*.*` tag automatically builds the library, sources, and Javadoc JARs, then attaches them to a GitHub Release.

**Independent Test**: Create a tag `v0.1.0` and push it; verify the release workflow produces and attaches all 3 JARs to the GitHub Release page.

### Implementation for User Story 2

- [X] T012 [US2] Create .github/workflows/release.yml with: trigger on push tags matching `v[0-9]+.[0-9]+.[0-9]+`; single job "release" on ubuntu-latest; steps: checkout, setup-java (temurin 17), extract version from tag (`echo "${GITHUB_REF_NAME#v}"` → `RELEASE_VERSION`), run `./gradlew build -PreleaseVersion=$RELEASE_VERSION`, collect artifacts (build/libs/*.jar), create GitHub Release using `softprops/action-gh-release@v2` with the 3 JAR files as assets
- [X] T013 [US2] Add a guard step in release.yml that runs `./gradlew test` before building artifacts, ensuring FR-009 (no partial artifacts on failure) — if tests fail, the workflow stops and no release is created
- [X] T014 [US2] Verify T012 and T013 against spec FR-004 (version tag triggers release), FR-005 (library, sources, Javadoc JARs), FR-006 (version matches tag), FR-007 (artifacts attached to release page), FR-009 (no partial artifacts on failure), and acceptance scenarios US2-1 through US2-4

**Checkpoint**: Pushing a version tag produces a GitHub Release with all 3 artifact JARs

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validation and edge case handling

- [X] T015 Update .gitignore to remove `gradle/wrapper/gradle-wrapper.jar` from the ignore list (currently ignored but needs to be tracked for CI to work without requiring Gradle installation)
- [X] T016 Verify non-version tags (e.g., `docs-update`) do NOT trigger the release workflow — confirm the tag filter pattern `v[0-9]+.[0-9]+.[0-9]+` in release.yml is correct (edge case from spec)
- [ ] T017 Run quickstart.md Scenarios 1-5 as a final validation pass against a real GitHub push
- [X] T018 Final evaluation: verify the cumulative implementation satisfies all 10 functional requirements (FR-001 through FR-010), all 4 success criteria (SC-001 through SC-004), all 3 user stories with their acceptance scenarios, and all applicable constitution principles

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 (Gradle wrapper must be committed)
- **US1 (Phase 3)**: Depends on Phase 2 (build.gradle.kts must be ready)
- **US3 (Phase 4)**: Depends on Phase 3 (ci.yml must exist to add matrix)
- **US2 (Phase 5)**: Depends on Phase 2 (build.gradle.kts version override must work)
- **Polish (Phase 6)**: Depends on all previous phases

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 2 — creates ci.yml
- **US3 (P3)**: Depends on US1 — modifies ci.yml to add matrix
- **US2 (P2)**: Can start after Phase 2 — creates separate release.yml (parallel with US1/US3 if desired, but recommended after US1 for consistency)

### Parallel Opportunities

- T001 and T002 can run in parallel (different files)
- T004 and T005 modify the same file (build.gradle.kts) — must be sequential
- T008 (ci.yml) and T012 (release.yml) are different files and could theoretically run in parallel, but US3 (T010) modifies ci.yml so the recommended flow is US1 → US3 → US2

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Commit Gradle wrapper
2. Complete Phase 2: Update build.gradle.kts
3. Complete Phase 3: Create ci.yml
4. **STOP and VALIDATE**: Push a commit and verify CI runs

### Incremental Delivery

1. Setup + Foundational → Gradle wrapper committed, build config ready
2. US1 → CI runs on push/PR → Validate
3. US3 → Build matrix across Java 11, 17, 21 → Validate
4. US2 → Release artifacts on version tag → Validate
5. Polish → Final validation

---

## Notes

- All workflow files go in `.github/workflows/`
- `softprops/action-gh-release@v2` is used for release artifact upload (uses built-in GITHUB_TOKEN, no extra secrets)
- Version injection uses `-PreleaseVersion=X.Y.Z` Gradle property
- The Gradle wrapper MUST be committed before any CI workflow can run
