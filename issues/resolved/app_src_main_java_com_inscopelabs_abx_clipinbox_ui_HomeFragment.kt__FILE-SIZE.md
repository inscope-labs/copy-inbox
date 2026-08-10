# FILE-SIZE Issue: HomeFragment.kt

- **File Path**: `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt`
- **Issue Type**: FILE-SIZE
- **Reason**: File is currently 607 lines, exceeding the 300-line hard threshold in AGENTS.md Section 4.1. Per Section 4.1, this file must not be included in the scope of any non-restructuring task. A dedicated restructuring task (per Section 4.2) is required before any other work touches this file.
- **Date Flagged**: 2026-08-09
- **Source**: Repository line-count audit, dated 2026-08-09. Proactive backfill.

## RESOLVED
- **Date Resolved**: 2026-08-10
- **Resolving Report**: `agent-reports/2026-08-10T03-51-00Z-homefragment-restructure.md`
- **Resolution Note**: Extracted touch-swipe logic into `ClipSwipeCallback.kt`, multi-selection bar controller into `HomeSelectionBarController.kt`, action bottom sheet & clip actions into `HomeClipActionHandler.kt`, and search & category filter logic into `HomeFilterController.kt`. `HomeFragment.kt` was reduced from 608 lines to 178 lines, fully complying with AGENTS.md Section 4.1 and Section 4.2.
