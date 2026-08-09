# FEATURE-PENDING Issue: Deferred, action-triggered permissions flow

- **File Path**: `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/MainActivity.kt`
- **Issue Type**: FEATURE-PENDING
- **Reason**: MainActivity.onCreate (currently lines 36-40) requests POST_NOTIFICATIONS immediately on launch, before the user has seen any UI or taken any action. Chairman directive: first launch should let the user move freely through the app with no permission prompt; only after a specific user action that actually requires the permission should an introductory dialog explain why it's needed, THEN trigger the system permission request. Rationale: requesting before the user has any context denies them the sequence of first knowing, then understanding, then acting -- and risks the user reflexively denying with "never ask again" before they're informed enough to decide.
- **Date Flagged**: 2026-08-09
- **Source**: chairman directive, recorded verbatim in this issue.
- **Cross-Reference**: This file already has an open issues/pending/app_src_main_java_com_inscopelabs_abx_clipinbox_ui_MainActivity.kt__LOGGING-GAP.md issue. Per AGENTS.md Section 3.1, any task implementing this feature will touch MainActivity.kt and must resolve the LOGGING-GAP issue in the same task -- do not treat them as separate tasks.
- **Scope Note**: This may extend beyond POST_NOTIFICATIONS to any other permission currently requested at launch or on first screen (check OverlayPermissionGate.kt and StoragePathsFragment.kt for similar patterns before implementation -- this issue file only confirms the MainActivity.onCreate case, it has not yet audited every permission request site in the app).
- **Status**: Not yet approved for implementation scoping -- record only. Chairman has not yet requested an implementation task be written.
