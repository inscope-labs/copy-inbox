# Agent Task Report: Version Increment Rule in AGENTS.md

- **Timestamp (UTC)**: 2026-08-06T17:10:55Z
- **Short Slug**: version-increment-rule-agents-md

## What Was Asked
Create a version increment rule in `AGENTS.md` for `copy-inbox` similar to the version code increment rule used in `inscope-labs` repositories.

## What Was Changed
- Updated `/AGENTS.md`:
  - Replaced section `## 2. Version Increment Rule — NOT YET ACTIVE` with `## 2. Version Increment Rule`.
  - Defined explicit instructions that every task/feature/bugfix in `copy-inbox` must increment the app's `versionCode` in `app/build.gradle.kts` by 1 and maintain or increment `versionName` as appropriate.
- Created mandatory task process report at `agent-reports/2026-08-06T17-10-55Z-version-increment-rule-agents-md.md`.

## Commands Executed & Results
- `grep -rn "version" agent-reports/`: Confirmed previous task references to versioning and `version.properties`.
- Attempted viewing `/version.properties` and `/app/version.properties`: Confirmed neither file exists in this repository; `versionCode` resides directly in `app/build.gradle.kts`.

## Assumptions
- Because `copy-inbox` manages build versioning directly in `app/build.gradle.kts` rather than a separate `version.properties` file, the version increment rule specifies incrementing `versionCode` in `app/build.gradle.kts`.

## Errors / Unverified Items
- None.

## Logging Gap Flags
- N/A (no Kotlin source files were modified during this task).
