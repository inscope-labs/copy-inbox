# Process Report: Wire LogViewerBottomSheet into MainActivity Options Menu (copy-inbox)

## What Was Asked
Wire `LogViewerBottomSheet` into `MainActivity`'s options menu for debug builds only:
1. Create `app/src/debug/res/menu/main_toolbar_menu_debug.xml` with menu item `action_view_logs`.
2. Add string resource `menu_view_logs` ("View Logs") to `strings.xml` and locale files (`values-es`, `values-fr`, `values-pt-rBR`).
3. Create `DebugMenuInflater.kt` in both debug (`app/src/debug/java/.../ui/`) and release (`app/src/release/java/.../ui/`) source sets.
4. Create `DebugMenuHandler.kt` in both debug (delegating `action_view_logs` to `DebugToolsLauncher.showLogViewer(activity)`) and release (returning `false`) source sets.
5. Wire `DebugMenuInflater.inflate(...)` in `MainActivity.onCreateOptionsMenu` and `DebugMenuHandler.handle(...)` in `MainActivity.onOptionsItemSelected`.

## Drift-Check Status
- Attempted `git rev-parse HEAD`. As in previous tasks, the execution environment lacks `.git` metadata; operations were performed directly on workspace source files.

## What Was Changed (Files Created & Modified)

### Files Created:
1. `app/src/debug/res/menu/main_toolbar_menu_debug.xml`:
   Debug-only menu layout declaring `action_view_logs` item (`@string/menu_view_logs`).
2. `app/src/debug/java/com/inscopelabs/abx/clipinbox/ui/DebugMenuInflater.kt`:
   Debug implementation inflating `main_toolbar_menu_debug.xml`.
3. `app/src/release/java/com/inscopelabs/abx/clipinbox/ui/DebugMenuInflater.kt`:
   Release no-op implementation.
4. `app/src/debug/java/com/inscopelabs/abx/clipinbox/ui/DebugMenuHandler.kt`:
   Debug implementation handling `action_view_logs` by calling `DebugToolsLauncher.showLogViewer(activity)`.
5. `app/src/release/java/com/inscopelabs/abx/clipinbox/ui/DebugMenuHandler.kt`:
   Release no-op implementation returning `false`.
6. `agent-reports/20260804T014700Z-debug-log-viewer-menu.md`:
   This process report.

### Files Modified:
1. `app/src/main/res/values/strings.xml`:
   Added `<string name="menu_view_logs">View Logs</string>`.
2. `app/src/main/res/values-es/strings.xml`:
   Added `<string name="menu_view_logs">Ver registros</string>`.
3. `app/src/main/res/values-fr/strings.xml`:
   Added `<string name="menu_view_logs">Voir les journaux</string>`.
4. `app/src/main/res/values-pt-rBR/strings.xml`:
   Added `<string name="menu_view_logs">Ver registros</string>`.
5. `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/MainActivity.kt`:
   Wired `DebugMenuInflater.inflate(menuInflater, menu!!)` and `DebugMenuHandler.handle(item, this)`.

## Entry Point Note
- **Compose Entry Point Omission**: As instructed, `LogViewerActivity` (which uses Jetpack Compose) was **intentionally NOT used** as the entry point. The log viewer is launched via `DebugToolsLauncher.showLogViewer(activity)` which uses `LogViewerBottomSheet` (View-based).

## Build Verification
- Both `compile_applet` (debug build) and `gradle assembleRelease` (release build) compiled cleanly with zero compilation errors.

## Flagged Logging Gaps (AGENTS.md Section 3)
- `DebugMenuInflater` and `DebugMenuHandler`: Minimal static helper facades for menu inflation and dispatching; no process flow logging gaps identified.

Proposed Commit Message:
"feat: wire log viewer into options menu (debug build only)"
