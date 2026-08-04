# Process Report: Phase 6 Part A — ClipboardWatcher and AutoClearScheduler wiring (copy-inbox)

## What Was Asked
- **1. Wire AutoClearScheduler marker into capture path**: Find method in `ClipboardHelper` that writes to clipboard (`copyToClipboard`) and attach ownership marker extras before calling `setPrimaryClip()` using `AutoClearScheduler.MARKER_KEY` and `AutoClearScheduler.MARKER_VALUE`.
- **2. Instantiate and wire ClipboardWatcher in ClipInBoxApplication**: Add `clipboardWatcher` and `autoClearScheduler` properties, initialize `SensitiveClipPolicy`, `ClipClassifier`, `NotificationHelper`, `OtpAutoCapture`, `AutoClearScheduler`, and `ClipboardWatcher` in `ClipInBoxApplication.onCreate()`, then invoke `clipboardWatcher.install()`.
- **3. Connect AutoClearScheduler to ClipboardWatcher**: Update `ClipboardWatcher` constructor to accept optional `autoClearScheduler: AutoClearScheduler? = null`, and invoke `autoClearScheduler?.scheduleClear()` inside `onPrimaryClipChanged()` when clip is sensitive (`policy.isSensitive(raw)`).

## What Was Changed
1. **`app/src/main/java/com/inscopelabs/abx/clipinbox/utils/ClipboardHelper.kt`**:
   - Exact method edited: `copyToClipboard(context: Context, text: String, label: String = "ClipInBox"): Boolean`.
   - Added import `com.inscopelabs.abx.clipinbox.security.AutoClearScheduler`.
   - Added `PersistableBundle` containing `AutoClearScheduler.MARKER_KEY` (`"android.content.extra.IS_SENSITIVE"`) and `AutoClearScheduler.MARKER_VALUE` (`"clipinbox"`) attached to `clip.description.extras`.
2. **`app/src/main/java/com/inscopelabs/abx/clipinbox/service/ClipboardWatcher.kt`**:
   - Added import `com.inscopelabs.abx.clipinbox.security.AutoClearScheduler`.
   - Updated constructor parameter `private val autoClearScheduler: AutoClearScheduler? = null`.
   - Updated `onPrimaryClipChanged()` in `policy.isSensitive(raw)` branch to call `autoClearScheduler?.scheduleClear()`.
3. **`app/src/main/java/com/inscopelabs/abx/clipinbox/ClipInBoxApplication.kt`**:
   - Added `lateinit var clipboardWatcher: ClipboardWatcher` and `lateinit var autoClearScheduler: AutoClearScheduler` properties.
   - Added imports for `ClipClassifier`, `AutoClearScheduler`, `SensitiveClipPolicy`, `ClipboardWatcher`, and `OtpAutoCapture`.
   - In `onCreate()`, instantiated dependencies and invoked `clipboardWatcher.install()`, logging `"ClipboardWatcher installed"`.

## Mandatory Confirmations
- **Exact Method Name in ClipboardHelper**: `copyToClipboard`
- **Marker Key / Value Written**: `"android.content.extra.IS_SENSITIVE"` / `"clipinbox"`
- **ClipboardWatcher.install()**: Confirmed called in `ClipInBoxApplication.onCreate()`.
- **Build Status**: `assembleDebug` compiled successfully via `compile_applet`.

## Commands Executed & Results
- `compile_applet`: Build succeeded - applet compiled cleanly.

## Assumptions
- None.

## Errors / Failures / Partial Failures
- None.
