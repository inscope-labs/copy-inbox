# Agent Task Report: Update AGENTS.md Standing Instructions

- **Timestamp (UTC)**: 2026-08-09T18:20:31Z
- **Short Slug**: update-agents-md-standing-instructions

## What Was Asked
Replace the entire contents of `/AGENTS.md` with the updated standing instructions provided, adding Section 1.1 (Issue Tracking Directories), Section 3.1 (Logging Gap Remediation on Touch), Section 4 (Single-Responsibility File Discipline), Section 4.1 (Size-Triggered Compliance Tiers), and Section 4.2 (Restructuring Tasks).

## Version Assessment & Probability Score
- **Assessed Probability Score**: 0 / 100 (Documentation and process rules change only; no application logic or Android source code was modified).
- **Version Action**: Score is not > 75; `versionCode` (3) and `debugCode` (0003) in `version.properties` remain unchanged.

## Build Verification
- **Build Verification Action**: Not applicable / not executed.
- **Reason**: No `.kt` Kotlin source files or application resource files were modified in this task.

## Prior Logging Gaps Found
- **PRIOR LOGGING GAPS FOUND**: none (no Kotlin source code files were touched or in scope for this task).

## Files Touched
1. `/AGENTS.md` - Overwritten with full revised standing instructions.
2. `/agent-reports/2026-08-09T18-20-31Z-update-agents-md-standing-instructions.md` - Created mandatory process report.

## Commands Executed & Results
- `git status`: Resulted in container index format error (`fatal: unknown index entry format 0xefbf0000`).

## Assumptions Made
- None.

## Errors / Partial Failures
- `git status` command output container index entry format incompatibility error.
