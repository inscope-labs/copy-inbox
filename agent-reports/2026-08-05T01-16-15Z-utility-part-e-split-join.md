# Process Report: Utility Package Part E (Split & Join Clips)

## Requested Task
Utility package — Part E (Split / Join, clip-level):
1. Create `ClipSplitter.kt` pure Kotlin utility supporting DELIMITER and FIXED_LENGTH modes, trimming and filtering blank pieces, and returning `listOf(content)` if fewer than 2 valid parts result.
2. Create `ClipJoiner.kt` pure Kotlin utility wrapping `joinToString`.
3. Create `dialog_split_clip.xml` with split modes (RadioGroup), delimiter field (default `\n`), fixed length chunk size field (initially disabled), and delete original checkbox.
4. Create `SplitClipDialogHelper.kt` with validation (Toast on blank delimiter or invalid/non-positive chunk size, Toast on < 2 parts effect) keeping dialog open on failure, logging via Logger.
5. Create `JoinClipsDialogHelper.kt` programmatically building dialog view with separator input (prefilled with `\n\n`) and delete original checkbox, logging via Logger.
6. Add `btn_split_clip` to `bottom_sheet_clip_actions.xml` next to `btn_find_replace`.
7. Update `ClipActionBottomSheet.kt` adding `onSplitClip` to Callback interface, toggling `btn_split_clip` visibility (`!isEditing`), and wiring click listener.
8. Add `btn_action_join` ImageButton to contextual selection bar in `fragment_home.xml`.
9. Update `HomeFragment.kt`:
   - Wire `btn_action_join`: requires >= 2 selected clips, sorts selected clips by timestamp ascending, joins via `ClipJoiner`, inherits earliest clip's `categoryId` and `tags`, deletes originals if requested, and clears selection.
   - Implement `onSplitClip`: saves split parts into repository, inherits original clip's `categoryId` and `tags`, deletes original clip if requested, and shows split count Toast.
   - Confirm neither split nor join path invokes `CategoryPickerDialogHelper`.
10. Add strings to `res/values/strings.xml`.

## Files Touched
- `app/src/main/java/com/inscopelabs/abx/clipinbox/utility/ClipSplitter.kt` (Created): Pure Kotlin split logic.
- `app/src/main/java/com/inscopelabs/abx/clipinbox/utility/ClipJoiner.kt` (Created): Pure Kotlin join logic.
- `app/src/main/res/layout/dialog_split_clip.xml` (Created): Layout for split clip options.
- `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/SplitClipDialogHelper.kt` (Created): Dialog helper for splitting clips with validation.
- `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/JoinClipsDialogHelper.kt` (Created): Dialog helper for joining clips.
- `app/src/main/res/layout/bottom_sheet_clip_actions.xml`: Added `btn_split_clip`.
- `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/ClipActionBottomSheet.kt`: Added `onSplitClip` to Callback, wired `btn_split_clip` click & visibility (`!isEditing`).
- `app/src/main/res/layout/fragment_home.xml`: Added `btn_action_join` ImageButton to selection bar.
- `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt`: Implemented join action listener and `onSplitClip` callback.
- `app/src/main/res/values/strings.xml`: Added strings for split and join dialogues, buttons, and toasts.

## Category & Tags Inheritance Confirmation
- **Split Parts**: Each generated part from `onSplitClip` is saved via `repository.saveClipText(part)`. The generated clip entity is fetched and updated with `newClip.copy(categoryId = clip.categoryId, tags = clip.tags)`.
- **Joined Clip**: In `btnActionJoin` listener, `selectedClips` are sorted by `timestamp` ascending (`sortedClips = selectedClips.sortedBy { it.timestamp }`). The newly saved joined clip is fetched and updated with `newClip.copy(categoryId = sortedClips.first().categoryId, tags = sortedClips.first().tags)`.

## CategoryPickerDialogHelper Suppression Confirmation
Neither split nor join triggers `CategoryPickerDialogHelper`. Bulk-generated clips from split and join bypass the category picker prompt to prevent stacking dialogs over user actions.

## Verification & Commands Ran
- `compile_applet`: Executed debug build successfully (`BUILD SUCCESSFUL`). No release tasks were run.

## Logging Gap Flagged
- LOGGING GAP FLAGGED: `app/src/main/java/com/inscopelabs/abx/clipinbox/utility/ClipSplitter.kt` — Pure utility class without logging calls.
- LOGGING GAP FLAGGED: `app/src/main/java/com/inscopelabs/abx/clipinbox/utility/ClipJoiner.kt` — Pure utility class without logging calls.
