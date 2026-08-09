# DESIGN-DECISION-PENDING Issue: Tag Repository

- **Feature Scope**: New feature — first-class Tag Repository for copy-inbox (distinct from Category), supporting CRUD on tag definitions and lightweight add/remove tag assignment on clips, for user task coordination (e.g. PENDING/IN-PROGRESS/COMPLETED/BLOCKED plus freeform user tags).
- **Issue Type**: DESIGN-DECISION-PENDING
- **Date Flagged**: 2026-08-09
- **Source**: Chairman discussion, dated 2026-08-09

## Open Decisions Blocking Implementation
These require chairman (John) sign-off before any implementation task can be written:

1. **Delete vs. Archive Semantics**: Does deleting a tag definition hard-delete it (cascade removes all clip associations) or soft-delete/archive it (stays visible on previously-tagged clips, disappears from the add-tag picker)?
2. **Filter Semantics**: Does multi-tag filtering default to AND, OR, or is this deferred to a saved-filter-view feature where each view pins its own logic?
3. **Status Tag System Protection**: Confirm whether the four status tags (`PENDING`, `IN-PROGRESS`, `COMPLETED`, `BLOCKED`) should be modeled as system-reserved rows (not renamable/deletable through normal tag CRUD) versus ordinary user tags with no special protection.

## Proposed Data Model (Reference Only)
- `TagEntity` (`id`, `label`, `color`, `isSystemReserved: Boolean`, `createdAt`)
- `ClipTagCrossRef` (`clipId`, `tagId`) — many-to-many join table
- **Schema Isolation**: This is a distinct schema effort from CBX-DAG's blocks/blocked-by dependency-linking between clips (an edge relationship, not a tag) — the two must not be merged into one feature or one migration.
- **Migration Strategy**: Expected to require its own Room schema migration, landing after whatever version the existing Category v4->v5 migration left the schema at.
