# Pending: App Startup White Screen Delay

**Status:** Blocked — investigation required before a fix approach can be selected.

## Problem Statement

- **User-Observed**: A noticeable blank/white or plain window surface screen renders before the first meaningful UI frame is displayed on cold start.
- **Defect Classification**: This is being treated as a startup performance defect, not a cosmetic one. It must be root-caused before fixing rather than patched symptomatically.

## Investigation Checklist (Repository Inspection Findings)

1. **Launcher Activity Theme & Window Background**:
   - In `AndroidManifest.xml`, `MainActivity` uses `@style/Theme.ClipInBox`.
   - Theme configuration file: `app/src/main/res/values/themes.xml` (Note: `styles.xml` does not exist in `res/values`).
   - `Theme.ClipInBox` defines `<item name="android:windowBackground">@color/gray_surface</item>`. When cold starting, the Android OS window manager fills the window with this static background color before the activity hierarchy inflates, creating the perceived blank/white start delay.

2. **Application Subclass & `onCreate()` Synchronous Work**:
   - Subclass: `com.inscopelabs.abx.clipinbox.ClipInBoxApplication` (`app/src/main/java/com/inscopelabs/abx/clipinbox/ClipInBoxApplication.kt`).
   - Work executed synchronously in `Application.onCreate()` before `MainActivity` is created:
     - Activity lifecycle callbacks registration (`BootRoute.redirectIfNeeded`).
     - Diagnostics & Crash Reporting initialization (`CrashReporterManager`, `GlobalExceptionHandler`, `DiagnosticsInitializer`).
     - Room Database instance instantiation (`ClipboardDatabase.getDatabase(this)`).
     - Security and session store setup (`EncryptedSessionStore`, `SessionGate`, `AbxMailboxConnector`).
     - DAO and Repository instantiations (`SafPathRepository`, `CategoryRepositoryImpl`, `ClipRepositoryImpl`, `QueueRepositoryImpl`).
     - Service and Policy helper instantiations (`SensitiveClipPolicy`, `ClipClassifier`, `NotificationHelper`, `OtpAutoCapture`, `AutoClearScheduler`, `ClipboardWatcher`).
     - Installation of `ClipboardWatcher` listener and conditional posting of persistent trigger notifications.
   - **Diagnosis**: This synchronous blocking setup on the main thread during `Application.onCreate()` is a key cause of real cold-start delay before the first frame can render.

3. **Launcher Activity `onCreate()` Flow**:
   - Class: `com.inscopelabs.abx.clipinbox.ui.MainActivity` (`app/src/main/java/com/inscopelabs/abx/clipinbox/ui/MainActivity.kt`).
   - In `onCreate()` before/during first frame render:
     - Inflates `ActivityMainBinding` and calls `setContentView()`.
     - Checks and requests `POST_NOTIFICATIONS` runtime permissions (Android 13+).
     - Sets up `Toolbar` and populates `TabLayout` with 3 navigation tabs ("Inbox", "Manage", "Storage").
     - Performs a `FragmentTransaction` to instantiate and commit `HomeFragment()`.
     - Inspects `Intent` for `ACTION_SEND` text clips and launches a coroutine to save text to Room DB if shared from another app.

4. **Current Splash Screen Implementation Status**:
   - No splash screen library or custom splash theme/Activity is implemented.
   - The app currently relies solely on the default window background (`@color/gray_surface`) specified in `Theme.ClipInBox`.

5. **SDK Version Targets**:
   - `minSdk`: 26 (Android 8.0)
   - `targetSdk`: 35 (Android 15)
   - `compileSdk`: 35
   - Fix approach must seamlessly handle both pre-Android 12 (pre-API 31) and Android 12+ system splash behavior.

## Documented Industry-Standard Fix Approach

- **AndroidX Core SplashScreen Library**:
  - Integrate `androidx.core:core-splashscreen` (`androidx.core:core-splashscreen:1.0.1` or latest).
  - Unifies behavior across Android 12+ (enforced system splash screen) and pre-Android 12 devices (backported via themed `windowBackground`), avoiding anti-patterns like secondary custom splash `Activity` classes.

- **Themed Static Splash**:
  - Define a specialized splash theme extending `Theme.SplashScreen` or `Theme.SplashScreen.IconBackground`.
  - Configure `windowSplashScreenAnimatedIcon` / `windowSplashScreenBackground` and set `postSplashScreenTheme` to `Theme.ClipInBox`.
  - Execute `installSplashScreen()` in `MainActivity.onCreate()` before `super.onCreate()`.
  - Avoid artificial delays or `Thread.sleep()` — splash duration must strictly reflect actual loading time.

- **Asynchronous Deferral & Content-Ready Signaling**:
  - Move heavy synchronous initialization tasks out of `Application.onCreate()` / main thread where safe.
  - Keep the splash screen visible during genuine async startup tasks using `splashScreen.setKeepOnScreenCondition { ... }`.
  - Signal content readiness using `ViewTreeObserver.OnPreDrawListener` or `Activity.reportFullyDrawn()`.

## Explicit Non-Goals

- No dependency additions to `build.gradle.kts`.
- No theme or style XML modifications.
- No Application or Activity Kotlin code modifications.
- No splash screen implementation in this task — this issue file records findings and target design only.
