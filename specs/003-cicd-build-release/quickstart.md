# Quickstart: CI/CD Build & Release Pipeline

## Scenario 1: Push a Commit and Verify CI

1. Make a change on any branch and push:
   ```bash
   git checkout -b test-ci
   echo "// test" >> src/main/java/org/gedcom7/parser/GedcomReader.java
   git add -A && git commit -m "Test CI trigger"
   git push -u origin test-ci
   ```

2. Go to the repository's **Actions** tab on GitHub.

3. Verify a workflow run appears for the `CI` workflow with 3 matrix jobs (Java 11, 17, 21).

4. All 3 jobs should complete successfully within 10 minutes.

## Scenario 2: Open a Pull Request

1. From the branch above, open a PR targeting `main`:
   ```bash
   gh pr create --title "Test CI" --body "Testing CI pipeline"
   ```

2. On the PR page, verify that status checks appear showing build results for all 3 Java versions.

3. All checks must pass before the PR can be merged.

## Scenario 3: Create a Release

1. Tag a release on the main branch:
   ```bash
   git checkout main
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. Go to the repository's **Actions** tab. Verify the `Release` workflow runs.

3. Once complete, go to the **Releases** page. Verify:
   - A release named `v1.0.0` exists
   - Three artifacts are attached:
     - `gedcom7-parser-1.0.0.jar` (library)
     - `gedcom7-parser-1.0.0-sources.jar` (sources)
     - `gedcom7-parser-1.0.0-javadoc.jar` (Javadoc)

4. Download the library JAR and verify it contains the compiled classes.

## Scenario 4: Non-Version Tag Ignored

1. Create a tag that doesn't match the version pattern:
   ```bash
   git tag docs-update
   git push origin docs-update
   ```

2. Verify no release workflow runs (only CI, if applicable).

## Scenario 5: Failed Build Blocks Release

1. Introduce a failing test, tag it:
   ```bash
   # (on a test branch, intentionally break a test)
   git tag v0.0.1-bad
   git push origin v0.0.1-bad
   ```

2. Verify the release workflow runs but fails at the test step.

3. Verify no artifacts are attached to any release page — the release is not created.
