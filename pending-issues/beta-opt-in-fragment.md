# Pending: Beta Opt-In Fragment (Production Track)

**Status:** Blocked — open decisions listed below must be resolved before implementation begins.

## Summary

- Production build hosts a UI element (fragment, banner, or settings entry) that opens the Play testing opt-in URL for the app's package name, routing eligible users into the open Beta track.
- Mechanism is a plain `ACTION_VIEW` intent to the Play-hosted opt-in URL; no Play Billing or special SDK integration required.
- Requires Beta track to be configured as open testing in Play Console — closed tracks (Alpha) have no public opt-in link.
- No reliable in-app method exists to query live enrollment status from Play Console; any "already in beta" gating must be inferred from `versionName`/`versionCode` rather than a real enrollment check.

## Open Decisions Blocking Implementation

- Where the opt-in entry point lives in the existing navigation (three-tab TabLayout: Inbox/Manage/Storage) — new menu item, settings screen entry, or dismissible banner.
- Whether `versionName` carries a track suffix (e.g., `-beta.1`) during Beta, and how the fragment's "already enrolled" heuristic would read that.
- Whether this fragment ships gated behind a remote/local flag or is always visible on Production builds.

## Explicit Non-Goals

- No implementation, no layout XML, no ViewModel/Fragment class, no manifest changes.
