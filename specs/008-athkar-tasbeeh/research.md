# Research: 008-athkar-tasbeeh

Generated during `/speckit.plan` for branch `008-athkar-tasbeeh`.

---

## Decision 1 — Room DB version bump strategy

**Decision**: Bump schema version 3 → 4 using `AutoMigration(from = 3, to = 4)` (simple, no spec class needed because all migration operations are pure table additions — no column renames or deletions).

**Rationale**: The existing 2→3 migration used a spec class (`AutoMigration_2_3`) only because it deleted columns. The 3→4 migration adds three new tables (`athkar_daily_logs`, `tasbeeh_goals`, `tasbeeh_daily_totals`) and zero structural changes to existing tables, so Room can infer the migration automatically.

**Alternatives considered**: Manual `Migration(3, 4)` block — rejected because it requires hand-writing SQL that Room already generates correctly for pure-addition migrations.

---

## Decision 2 — AthkarDailyLog per-item counter storage strategy

**Decision**: Store per-item counters as a `String` column in `AthkarDailyLogEntity` containing a JSON-serialized `Map<String, Int>` (itemId → count). One row per (groupId, date). Use `kotlinx-serialization-json` for serialization via a Room `TypeConverter`.

**Rationale**:
- Spec explicitly states "one row per (groupId, date); per-item counters stored as itemId → count map within that row".
- The alternative (one row per (groupId, itemId, date)) would produce ~30–40 rows per day group and require a JOIN or multi-query to load the group state — unnecessary complexity for a read-by-key access pattern.
- `kotlinx-serialization-json` is already a project dependency (in `shared:core:data`); adding it to `shared:core:database` is a one-line addition. A Room `TypeConverter` keeps the DAO API clean (accept/return domain-level `Map<String, Int>`) and keeps the entity itself simple.
- The item list is static (embedded in code), so there is no risk of orphaned counter keys.

**Alternatives considered**:
- One row per item (normalised): cleaner schema but over-engineered for a static, small dataset.
- Storing as a delimited string: fragile, no type safety.

**New dependency needed**: `kotlinx-serialization-json` must be added to `shared:core:database`'s `commonMain` dependencies (and the `kotlinxSerialization` plugin applied). It is already present in `libs.versions.toml`.

---

## Decision 3 — TasbeehGoal + TasbeehDailyTotal persistence strategy

**Decision**: Both stored in Room, not DataStore.

- `TasbeehGoalEntity` — singleton row (primary key = `id: Int = 1`), single `goalCount: Int` column. Mirrors `QuranGoalEntity`.
- `TasbeehDailyTotalEntity` — one row per date (`date: String` primary key, `totalCount: Int`). Mirrors `PrayerTimeCacheEntity` / `QuranDailyLogEntity` keyed-by-date pattern.

**Rationale**: DataStore is appropriate for key-value preferences (auth tokens, display settings). The Tasbeeh daily total is append-only historical data that benefits from Room's per-date key, Flow-based observation, and automatic cleanup queries. Using Room keeps all habit-related persistence in one database and avoids standing up a second DataStore file for what is functionally a daily log.

**Alternatives considered**: DataStore for goal + in-memory counter with no persistence for daily total — rejected because FR-016b requires per-day total persistence across app restarts.

---

## Decision 4 — Notification scheduling abstraction

**Decision**: Implement a new `NotificationScheduler` interface in `shared:core:domain` with `expect`/`actual` implementations split across `androidMain` and `iosMain` in a new module **`shared:core:notifications`** (following the same physical module split as `shared:core:location`).

**Rationale**:
- The spec explicitly calls for the Location-permission pattern: "uses platform notification APIs via expect/actual in shared:core".
- The existing Location handling uses an **interface + platform class** pattern (not Kotlin `expect`/`actual` keywords), but the approach is equivalent in practice: a `commonMain` interface, platform implementations wired via Koin.
- There is zero existing notification infrastructure in the project — starting in a dedicated `shared:core:notifications` module prevents polluting the existing `shared:core:data` or `shared:core:domain` modules and allows future notification features (prayer reminders, habit reminders) to reuse the same scheduler without depending on `feature:athkar`.
- Android implementation: `AlarmManager` + `NotificationCompat.Builder` + `NotificationChannel`. `AlarmManager.setExactAndAllowWhileIdle` is required for accurate daily reminders. Notification permission (`POST_NOTIFICATIONS`) needs Android 13+ handling.
- iOS implementation: `UNUserNotificationCenter` + `UNCalendarNotificationTrigger` for daily scheduled notifications.

**Alternatives considered**:
- WorkManager for Android: provides battery-friendly periodic work but has ~15-minute minimum interval precision, which is unacceptable for a "fire at 05:30" reminder. Rejected.
- Third-party push (Firebase): explicitly out of scope (no cloud sync).
- In-lining notification code in `feature:athkar:data`: violates the "features must not depend on other features" rule and prevents reuse.

---

## Decision 5 — Athkar item list definition location

**Decision**: Define the static Athkar item lists as `object` or top-level `val` lists inside `feature:athkar:domain` (e.g., `MorningAthkarItems.kt`, `EveningAthkarItems.kt`, `PostPrayerAthkarItems.kt`). Each item holds a stable `id: String`, a `transliterationKey: String` (maps to a `Res.string.*` key), a `translationKey: String`, and a `targetCount: Int`.

**Rationale**: The lists are static (never fetched from network/DB) and are pure domain knowledge. Placing them in `:domain` keeps `:presentation` and `:data` free of hardcoded text while still allowing both layers to reference item IDs consistently. Transliteration and translation strings resolve to `Res.string.*` keys at the presentation layer.

**Alternatives considered**: Hardcoding lists in `:presentation` — violates single-responsibility and prevents data layer from referencing item IDs when serializing counters.

---

## Decision 6 — AthkarNotificationPreference persistence strategy

**Decision**: Use DataStore (key-value preferences) for notification preferences (enabled flag + hour + minute per group).

**Rationale**: These are user preferences (not historical data) with exactly 4 scalar values. DataStore is the idiomatic choice for simple key-value settings. It avoids a Room table for what is effectively a config record. The existing DataStore infrastructure in `shared:core:data` can be reused directly.

**Alternatives considered**: Room singleton table — functional but over-engineered for 4 scalar values.

---

## Decision 7 — Module structure

**Confirmed module layout**:

```
feature/
  athkar/
    domain/     — AthkarItem, AthkarGroup, AthkarDailyLog domain models;
                  AthkarRepository, TasbeehRepository interfaces;
                  GetAthkarGroupUseCase, LogAthkarProgressUseCase,
                  GetTasbeehGoalUseCase, UpdateTasbeehGoalUseCase,
                  IncrementTasbeehUseCase use cases
    data/       — Room entities + DAOs (AthkarDailyLogEntity,
                  TasbeehGoalEntity, TasbeehDailyTotalEntity);
                  Repository implementations; Koin module
    presentation/ — AthkarScreen, AthkarGroupScreen, TasbeehScreen,
                    TasbeehGoalBottomSheet; AthkarViewModel,
                    TasbeehViewModel; Koin module

shared/
  core/
    notifications/   ← NEW module
      — NotificationScheduler interface (commonMain)
      — AndroidNotificationScheduler (androidMain)
      — IosNotificationScheduler (iosMain)
      — NotificationPermissionChecker interface (commonMain)
      — Koin DI module
```

---

## Decision 8 — Session vs. persisted Tasbeeh count

**Decision**: Session count is in-memory only, held in `TasbeehViewModel` state. On Reset, the current `sessionCount` is flushed to `TasbeehDailyTotalEntity` (DB write) and then cleared to 0. When the user navigates away and returns, session count starts at 0 but `TasbeehDailyTotalEntity.totalCount` is loaded from DB and shown as `TODAY'S TOTAL`.

**Rationale**: Matches the spec requirement (`TODAY'S TOTAL = sum of all completed session counts since midnight`), where "completed session" means a session ended by Reset. Avoids continuous DB writes on every tap (excessive); produces exactly one DB write per Reset action instead.

**Implementation note**: Each tap increments only the in-memory `sessionCount`. On Reset: (1) call `AddToTasbeehDailyUseCase` with the current `sessionCount` to upsert `TasbeehDailyTotalEntity`, (2) clear `sessionCount` to 0 in ViewModel state. `TODAY'S TOTAL` is driven by a `Flow` from `ObserveTasbeehDailyTotalUseCase` and updates reactively after each flush.
