# Externalize Strings and Locale Scaffolding Report

## Overview
Audited and extracted all hardcoded user-visible strings across `copy-inbox` Kotlin files into `res/values/strings.xml` and scaffolded locale resource folders for Spanish, French, and Portuguese (Brazil).

---

## 1. Extracted Strings Audit Log

| File Location | Old Inline String Literal | New Resource Key (`R.string.`) |
| --- | --- | --- |
| `ui/MainActivity.kt:77` | `"Saved shared text to ClipInBox!"` | `main_toast_shared_text_saved` |
| `ui/HomeFragment.kt:57` | `"Exported ${clipsToExport.size} clip(s) to TXT"` | `home_export_success_format` |
| `ui/HomeFragment.kt:62` | `"Export failed: ${e.message}"` | `home_export_failed_format` |
| `ui/HomeFragment.kt:65` | `"No clips to export"` | `home_export_empty` |
| `ui/HomeFragment.kt:131` | `"Clipboard is currently empty"` | `home_toast_clipboard_empty` |
| `ui/HomeFragment.kt:137` | `"Captured current clipboard text!"` | `home_toast_clipboard_captured` |
| `ui/HomeFragment.kt:139` | `"Clip already exists in history"` | `home_toast_clip_exists` |
| `ui/HomeFragment.kt:154` | `"Capture notification pinned"` | `home_toast_notification_pinned` |
| `ui/HomeFragment.kt:156` | `"Capture notification unpinned"` | `home_toast_notification_unpinned` |
| `ui/HomeFragment.kt:179` | `"No clips selected"` | `home_toast_no_clips_selected` |
| `ui/HomeFragment.kt:192` | `"Deleted ${selectedClips.size} clip(s)"` | `home_toast_deleted_clips_format` |
| `ui/HomeFragment.kt:215` | `"No clips matching \"$searchQuery\""` | `home_empty_matching_format` |
| `ui/HomeFragment.kt:217` | `"No clips saved yet"` | `home_empty_no_clips_saved` |
| `ui/HomeFragment.kt:228` | `"No clips available to export"` | `home_export_no_clips_available` |
| `ui/HomeFragment.kt:239` | `"Cleared unpinned clips"` | `home_toast_cleared_unpinned` |
| `ui/HomeFragment.kt:253` | `"Share Clip"` | `home_share_chooser_title` |
| `ui/HomeFragment.kt:265` | `"$selectedCount selected"` | `home_selection_count_format` |
| `ui/HomeFragment.kt:274, 327` | `"Copied to clipboard!"` | `home_toast_copied_to_clipboard` |
| `ui/HomeFragment.kt:281` | `"Pinned clip" / "Unpinned clip"` | `home_toast_clip_pinned` / `home_toast_clip_unpinned` |
| `ui/HomeFragment.kt:298` | `"Clip deleted"` | `home_toast_clip_deleted` |
| `ui/HomeFragment.kt:305` | `"Saved clip!"` | `home_toast_clip_saved` |
| `ui/HomeFragment.kt:317` | `"Clip updated"` | `home_toast_clip_updated` |
| `ui/ClipActionBottomSheet.kt:57` | `"Add New Clip"` | `sheet_title_add_new` |
| `ui/ClipActionBottomSheet.kt:65, 128, 132` | `"Cancel"` | `action_cancel` |
| `ui/ClipActionBottomSheet.kt:70, 133` | `"Save"` | `action_save` |
| `ui/ClipActionBottomSheet.kt:77` | `"Clip content cannot be empty"` | `sheet_error_empty_content` |
| `ui/ClipActionBottomSheet.kt:83, 135` | `"Clip Details"` | `sheet_title_clip_details` |
| `ui/ClipActionBottomSheet.kt:88` | `"Category: ${...} • ${...} chars • ${...} words"` | `sheet_category_meta_format` |
| `ui/ClipActionBottomSheet.kt:120` | `"Content cannot be empty"` | `sheet_error_empty_content_short` |
| `ui/ClipActionBottomSheet.kt:127` | `"Edit Clip"` | `sheet_title_edit_clip` |
| `ui/ClipActionBottomSheet.kt:136` | `"Edit"` | `action_edit` |
| `ui/ClipActionBottomSheet.kt:140` | `"Share"` | `action_share` |
| `ui/ClipActionBottomSheet.kt:141` | `"Copy"` | `action_copy` |
| `ui/ClipListAdapter.kt:90` | `"${clip.charCount} chars • ${clip.wordCount} words"` | `clip_item_meta_counts_format` |
| `ui/TransparentCaptureActivity.kt:20` | `"Clipboard is empty!"` | `capture_toast_clipboard_empty` |
| `ui/TransparentCaptureActivity.kt:30` | `"Saved to ClipInBox!"` | `capture_toast_saved` |
| `ui/TransparentCaptureActivity.kt:32` | `"Clip already exists in ClipInBox"` | `capture_toast_clip_exists` |
| `utils/NotificationHelper.kt:47` | `"Capture Clipboard"` | `notification_action_capture` |
| `utils/NotificationHelper.kt:52` | `"ClipInBox"` | `notification_title` |
| `utils/NotificationHelper.kt:53` | `"Quick clipboard capture ready"` | `notification_content_text` |
| `utils/NotificationHelper.kt:72` | `"ClipInBox Shortcut"` | `notification_channel_name` |
| `utils/NotificationHelper.kt:75` | `"Dismissible notification shortcut..."` | `notification_channel_desc` |
| `boot/RecoveryActivity.kt:34` | `"An error occurred displaying recovery UI."` | `recovery_fallback_toast` |
| `boot/RecoveryActivity.kt:113` | `"Failed to copy report"` | `recovery_copy_failed` |
| `boot/RecoveryActivity.kt:129` | `"Failed to restart application"` | `recovery_restart_failed` |
| `diagnostics/CrashActivity.kt:50` | `"An error occurred displaying the crash report."` | `crash_fallback_toast` |
| `diagnostics/CrashActivity.kt:85` | `"Failed to copy report"` | `crash_copy_failed` |
| `diagnostics/CrashActivity.kt:100` | `"Failed to restart application"` | `crash_restart_failed` |

---

## 2. Locale Resources Scaffolded

The following three locale files were created with identical string resource structures containing English placeholder strings:
- `app/src/main/res/values-es/strings.xml` (Spanish)
- `app/src/main/res/values-fr/strings.xml` (French)
- `app/src/main/res/values-pt-rBR/strings.xml` (Portuguese - Brazil)

> **Note**: Actual translation content for Spanish, French, and Portuguese is flagged as a separate follow-up task.

---

## 3. Build Verification
- Build tool: `compile_applet`
- Result: **SUCCESS**
