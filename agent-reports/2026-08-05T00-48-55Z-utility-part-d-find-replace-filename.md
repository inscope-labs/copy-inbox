# Process Report: Utility Package Part D (Find & Replace, Editable Save Filename)

## Requested Task
Utility package — Part D (Find & Replace, editable save filename):
1. Create pure Kotlin `FindReplaceEngine` with literal/regex replacement and blank search checks.
2. Create `dialog_find_replace.xml` layout.
3. Create `FindReplaceDialogHelper` to handle AlertDialog creation, validation, error Toasts, and callback invocation.
4. Add `btn_find_replace` to `bottom_sheet_clip_actions.xml`.
5. Wire `btn_find_replace` in `ClipActionBottomSheet` (visible only in edit mode, non-destructive to repository until sheet save).
6. Update `bottom_sheet_save_to_path.xml` replacing `tv_filename_preview` with `et_filename_preview` (TextInputLayout + TextInputEditText).
7. Update `SaveToPathBottomSheet.kt` to allow single-clip manual filename editing while keeping batch saves non-editable/disabled, suppressing TextWatcher during programmatic updates, and handling blank name fallbacks.
8. Add strings to `res/values/strings.xml`.

## Changes Summary
- `app/src/main/java/com/inscopelabs/abx/clipinbox/utility/FindReplaceEngine.kt`:
  - New pure Kotlin object. Performs literal and regex replace with `Result<String>` return, returning `Result.failure` on empty find pattern or invalid regex syntax (`PatternSyntaxException`).
- `app/src/main/res/layout/dialog_find_replace.xml`:
  - New layout with `et_find_text`, `et_replace_text`, and `cb_use_regex`.
- `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/FindReplaceDialogHelper.kt`:
  - New helper object building an AlertDialog around `dialog_find_replace.xml`. Shows Toasts on failure and keeps dialog open, calls `onApply` and dismisses on success. Logged via `Logger`.
- `app/src/main/res/layout/bottom_sheet_clip_actions.xml`:
  - Added `btn_find_replace` TextButton below the content field.
- `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/ClipActionBottomSheet.kt`:
  - Toggled `btnFindReplace` visibility based on `isEditing`. Wired click listener to show `FindReplaceDialogHelper`.
- `app/src/main/res/layout/bottom_sheet_save_to_path.xml`:
  - Replaced `tv_filename_preview` with `et_filename_preview` (`TextInputLayout` + `TextInputEditText`).
- `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/SaveToPathBottomSheet.kt`:
  - Replaced `TextView` field with `TextInputEditText`.
  - Added `isFilenameManuallyEdited` flag.
  - Implemented TextWatcher suppression during programmatic updates in `updatePreview()` by temporarily detaching `filenameTextWatcher` before `setText()` and re-attaching it afterward.
  - Disabled `etFilenamePreview` when `clipIds.size > 1`.
  - Updated confirm listener for single-clip save to use trimmed custom filename or fall back to macro-generated name if blank.
- `app/src/main/res/values/strings.xml`:
  - Added `find_replace_title`, `find_replace_find_hint`, `find_replace_replace_hint`, `find_replace_use_regex`, `find_replace_invalid_pattern`, `action_apply`.

## Verification & Commands Ran
- `compile_applet`: Executed debug build successfully (`BUILD SUCCESSFUL`). No release tasks were run.

## TextWatcher Suppression Details
In `SaveToPathBottomSheet.kt`:
```kotlin
private fun updatePreview() {
    if (clipIds.size > 1 || !isFilenameManuallyEdited) {
        val clip = firstClip ?: return
        val path = selectedPath ?: return
        val macro = selectedMacro
        val name = if (macro == null) {
            MacroExpander.defaultFilename(clip, 0)
        } else {
            MacroExpander.expand(macro.template, clip, path, 0)
        }
        
        // Programmatic update: temporarily detach watcher so isFilenameManuallyEdited is not tripped
        filenameTextWatcher?.let { etFilenamePreview.removeTextChangedListener(it) }
        etFilenamePreview.setText(name)
        filenameTextWatcher?.let { etFilenamePreview.addTextChangedListener(it) }
    }
}
```

## Logging Gap Flagged
- LOGGING GAP FLAGGED: `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/SaveToPathBottomSheet.kt` — `PathChoiceAdapter` click callback lacks `Logger` calls when selecting a path item in the RecyclerView adapter.
