# Port Diagnostics Package Report

## Overview
Ported the full `diagnostics` package from `inscope-labs/xtools` into `inscope-labs/copy-inbox` across all three source-set variants (`main`, `debug`, and `release`).

## 1. Files Created

### `app/src/main/java/com/inscopelabs/abx/clipinbox/diagnostics/`
- `CrashActivity.kt`
- `CrashReporter.kt`
- `CrashReporterManager.kt`
- `DiagnosticPreferences.kt`
- `DiagnosticSettings.kt`
- `FirebaseCrashReporter.kt`
- `GlobalExceptionHandler.kt`
- `NoOpCrashReporter.kt`
- `UserFacingErrorActivity.kt`

### `app/src/debug/java/com/inscopelabs/abx/clipinbox/diagnostics/`
- `AnrWatchdog.kt`
- `DebugToolsLauncher.kt`
- `DeviceInformation.kt`
- `DiagnosticBundle.kt`
- `DiagnosticExporter.kt`
- `DiagnosticService.kt`
- `DiagnosticsInitializer.kt`
- `LogEntryListAdapter.kt`
- `LogFormatter.kt`
- `LogRotationManager.kt`
- `LogSearchEngine.kt`
- `LogViewerActivity.kt`
- `LogViewerAdapter.kt`
- `LogViewerBottomSheet.kt`
- `LogWriter.kt`
- `Logger.kt`
- `RuntimeDiagnostics.kt`
- `SessionManager.kt`
- `StartupDiagnostics.kt`

### `app/src/release/java/com/inscopelabs/abx/clipinbox/diagnostics/`
- `DebugToolsLauncher.kt`
- `DiagnosticsInitializer.kt`
- `Logger.kt`

### Associated Layouts, Resources & Manifests
- `app/src/main/res/drawable/ic_download.xml`
- `app/src/main/res/drawable/ic_search.xml`
- `app/src/main/res/layout/activity_crash.xml`
- `app/src/main/res/layout/activity_user_facing_error.xml`
- `app/src/debug/res/layout/fragment_log_viewer_bottom_sheet.xml`
- `app/src/debug/res/layout/item_log_entry.xml`
- `app/src/debug/AndroidManifest.xml`
- Updated `app/src/main/res/values/strings.xml` with crash and error strings
- Updated `app/src/main/AndroidManifest.xml` with `CrashActivity` and `UserFacingErrorActivity`

## 2. `sourceSets` Diff (`app/build.gradle.kts`)

```kotlin
  sourceSets {
    getByName("main") {
      java.srcDirs("src/main/java")
    }
    getByName("debug") {
      java.srcDirs("src/debug/java")
    }
    getByName("release") {
      java.srcDirs("src/release/java")
    }
  }
```

## 3. API Contract Preservation Confirmation
The debug-vs-release API contract is strictly preserved with matching public method signatures:

- **`Logger`**:
  - `fun initialize(context: Context)`
  - `fun d(component: String, message: String)`
  - `fun i(component: String, message: String)`
  - `fun w(component: String, message: String, throwable: Throwable? = null)`
  - `fun e(component: String, message: String, throwable: Throwable? = null)`
  - `fun getLogFile(): File?`

- **`DiagnosticsInitializer`**:
  - `fun initialize(context: Context)`
  - `fun shutdown()`

- **`DebugToolsLauncher`**:
  - `fun showLogViewer(activity: FragmentActivity)`

In `debug`, these execute the real diagnostics engine, logging, ANR watchdog, and UI tools. In `release`, they are no-op stand-ins with zero side-effects and minimal overhead.

## 4. Initialization Order in `ClipInBoxApplication.onCreate()`

In `ClipInBoxApplication.onCreate()`, initialization was placed inside the `app_init` stage immediately following `BootGuard.stageStart("app_init")` in the exact required sequence:

1. `CrashReporterManager.initialize(this)`
2. `Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))`
3. `DiagnosticsInitializer.initialize(this)`

## Build Verification
- Build tool: `compile_applet`
- Status: **SUCCESS**
