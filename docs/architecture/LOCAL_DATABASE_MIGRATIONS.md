# Local database migrations

## Version 7: academic-schema baseline repair

Version 7 makes the academic foundation coherent for both database entry paths.

- Fresh installations call `createAcademicSchema` from `onCreate`, creating courses, academic units, lessons, learning materials, assignments, assessments, and learning progress.
- Existing version-6 databases retain every existing table and row. The upgrade creates any missing academic tables, adds `courses.level_number`, `lessons.active`, and canonical `learning_progress.status` columns when absent, then copies legacy `learning_progress.state` values into `status`.
- Earlier databases continue through their existing version 2–6 migration blocks before the version-7 repair runs.

The legacy `state` column is intentionally retained on upgraded databases to avoid destructive table rebuilding. New code uses `status`, which is the field defined by the current Java model.

## Version 8: programmes tenant relaxation

- `programmes.madrassa_id` becomes nullable so canonical/global Solo content can coexist with Madrassa-owned programmes.
- Migration rebuilds `programmes` via `programmes_v8` copy (`id, madrassa_id, name, category, default_programme, active`), preserving every row, then recreates `idx_programmes_madrassa`.
- Fresh installs create the nullable schema directly in `onCreate`.

## Version 9: content sharing

- New `content_shares` table (`id, madrassa_id NOT NULL, content_type, content_id, scope, target_id, created_by, active DEFAULT 1, created_at, updated_at`) plus indexes `idx_content_shares_madrassa`, `idx_content_shares_content(content_type, content_id)`, `idx_content_shares_target(madrassa_id, scope, target_id)`.
- Enables controlled reuse of academic content without silent cross-Madrassa duplication. `TenantPolicy` remains the enforcement point.

## Version 10: student groups + flexible class groups

- New `student_groups` (`id, madrassa_id NOT NULL, name, description, active DEFAULT 1, created_at, updated_at`) and `student_group_members(group_id, student_id, created_at, PK(group_id, student_id))` with group/student indexes.
- `class_groups` gains nullable `code TEXT` and `order_index INTEGER NOT NULL DEFAULT 0` via tolerant `ALTER TABLE` (catch-and-ignore if already present).
- All migrations are additive (`CREATE TABLE/INDEX IF NOT EXISTS`, `addColumnIfMissing`, guarded `ALTER TABLE`). No `DROP` of product data except the internal `programmes -> programmes_v8` rebuild which preserves rows. `onDowngrade` remains forbidden.

Current `DATABASE_VERSION = 10`. Do not reset to 7: Batch 3F spec predates v8-v10. Future changes must add a new version block, never edit history.
