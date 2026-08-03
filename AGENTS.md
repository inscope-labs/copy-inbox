# Standing Instructions for AI Studio Build Agent — copy-inbox

## 1. Mandatory Process Report on Every Task

This environment provides no way to copy, save, or download your
responses. You MUST record a report for every task you complete, saved
as an actual file in the repository (not just a chat response),
committed and pushed:

Path: agent-reports/<UTC-ISO-timestamp>-<short-task-slug>.md

Always use this single path — never app/agent-reports/ or any other
location. If a report already exists at this path for a prior task,
that is expected; each task still gets its own new timestamped file,
never an overwrite.

The report must include:
- What was asked.
- What you actually changed (files touched, with a diff or summary).
- Any commands you ran and their results.
- Any assumptions you made.
- Any errors, partial failures, or things you were unable to verify.

This folder must NOT be gitignored; it must be pushed to GitHub so it
can be read outside this environment.

## 2. Version Increment Rule — NOT YET ACTIVE

copy-inbox does not currently have a version.properties file. Do not
create one speculatively. This section will be added once that
infrastructure is scoped in a dedicated task — until then, do not
attempt version-increment logic of any kind.

## 3. Mandatory Logging Standard

Every new Activity, Fragment, feature, or discrete piece of functionality
must implement adequate logging of its own process flow — entry points,
key decision branches, and completion/failure outcomes — sufficient for
someone to reconstruct what happened after the fact from the log file
alone, without needing to reproduce the issue live. Use the existing
Logger facade (com.inscopelabs.abx.clipinbox.diagnostics.Logger — d/i/w/e)
exactly as it's already used throughout the codebase. Logger is safe to
call from any file regardless of build variant — it resolves to a real
implementation in debug builds and a true no-op in release builds
automatically, so new code never needs to guard calls to it or worry about
whether it's "allowed" to log; just call it the same way existing code
already does.

If a task requires reading, reviewing, or writing to an EXISTING file that
does not already implement adequate logging per the standard above, flag
that file explicitly and prominently in the task's mandatory agent report
(per section 1) — a dedicated, clearly-labeled line or subsection such as
"LOGGING GAP FLAGGED: <file path> — <one-line reason>", not buried in
general notes. This applies whether or not the file was otherwise in scope
for the task's actual changes — flagging a logging gap does not require
fixing it in the same task unless the task's own scope already covers that
file's logic.
