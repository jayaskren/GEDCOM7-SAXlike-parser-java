# Research: CI/CD Build & Release Pipeline

## R1: GitHub Actions for Gradle Java Projects

**Decision**: Use GitHub Actions with `actions/setup-java` and Gradle wrapper for CI.

**Rationale**: The project is already on GitHub. GitHub Actions is free for public repositories, requires no external service accounts, and integrates natively with PR status checks. The Gradle wrapper (`gradlew`) is already in the repository.

**Alternatives considered**:
- **Jenkins**: Self-hosted, maintenance burden, overkill for a small open-source library.
- **CircleCI / Travis CI**: External services, additional account setup, configuration complexity for no tangible benefit over GitHub Actions.

## R2: Java Build Matrix Versions

**Decision**: Test against Java 11, 17, and 21.

**Rationale**:
- Java 11: Minimum supported version (declared in `build.gradle.kts` as `sourceCompatibility`).
- Java 17: Current widely-adopted LTS, covers the middle ground.
- Java 21: Latest LTS (released Sept 2023), ensures forward compatibility.

**Alternatives considered**:
- Including non-LTS versions (e.g., 22, 23): Adds CI time without meaningful coverage for a library. LTS versions are what consumers actually use.

## R3: Release Artifact Strategy

**Decision**: Use Gradle's built-in `java` plugin tasks for sources/Javadoc JARs, and `softprops/action-gh-release` to upload artifacts to GitHub Releases.

**Rationale**:
- `java` plugin provides `withSourcesJar()` and `withJavadocJar()` configuration.
- `softprops/action-gh-release` is the de-facto standard GitHub Action for creating releases and uploading assets. It uses only the built-in `GITHUB_TOKEN`, no additional secrets needed.
- Version is extracted from the tag (`v1.0.0` → `1.0.0`) and injected into the Gradle build.

**Alternatives considered**:
- **Maven Central publishing**: Out of scope per spec assumptions. Can be added later as a separate feature.
- **Manual `gh release create`**: Works but `softprops/action-gh-release` handles edge cases (idempotent uploads, draft releases) more robustly.

## R4: Version Injection from Tag

**Decision**: Pass the version from the Git tag to Gradle via `-Pversion=X.Y.Z` command-line property, overriding the `build.gradle.kts` default.

**Rationale**: This is the simplest approach — no need to modify `build.gradle.kts` to read environment variables. Gradle's `-P` flag natively overrides project properties. The workflow extracts the version by stripping the `v` prefix from `${{ github.ref_name }}`.

**Alternatives considered**:
- **Modifying `build.gradle.kts` to read `RELEASE_VERSION` env var**: More invasive, couples build file to CI.
- **Using a version plugin (e.g., `nebula.release`)**: Adds a dependency for trivial functionality.

## R5: Concurrency and Cancellation

**Decision**: Use GitHub Actions `concurrency` groups to cancel in-progress builds when a new push arrives on the same branch.

**Rationale**: Prevents wasted CI minutes on superseded commits. The `concurrency` key with `cancel-in-progress: true` is native to GitHub Actions and requires no additional tooling.

## R6: Gradle Wrapper Commitment

**Decision**: The Gradle wrapper files (`gradle/`, `gradlew`, `gradlew.bat`) must be committed to the repository.

**Rationale**: GitHub Actions (and any contributor) needs the wrapper to build without pre-installing a specific Gradle version. This is standard practice for Gradle projects. The wrapper JAR is already in `.gitignore` but the wrapper scripts and properties file must be tracked.

**Note**: Currently `gradle/`, `gradlew`, `gradlew.bat` are untracked. They must be committed as part of this feature's implementation.
