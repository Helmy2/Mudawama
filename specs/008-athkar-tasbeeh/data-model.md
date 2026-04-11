# Data Model: 008-athkar-tasbeeh

Generated during `/speckit.plan` for branch `008-athkar-tasbeeh`.

---

## Overview

This feature adds three new Room entities to `MudawamaDatabase` (schema **v3 → v4**), one new DataStore preference group, and a suite of pure-domain models. No existing entities are modified.

---

## Domain Models (`feature:athkar:domain`)

### AthkarGroupType (enum)

Identifies one of the three Athkar groups.

```
MORNING, EVENING, POST_PRAYER
```

### AthkarItem (data class)

A single dhikr entry in a group. Static — embedded in domain code, never stored in DB.

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Stable identifier (e.g., `"morning_subhanallah_33"`). Used as the map key in `AthkarDailyLog.counters`. |
| `transliterationKey` | `String` | Res.string key for the transliterated Arabic label. |
| `translationKey` | `String` | Res.string key for the English meaning. |
| `targetCount` | `Int` | Required repetition count (e.g., 33, 100). Always ≥ 1. |

### AthkarGroup (data class)

The runtime representation of one checklist group (group definition + today's progress merged).

| Field | Type | Description |
|---|---|---|
| `type` | `AthkarGroupType` | Identifies the group. |
| `items` | `List<AthkarItem>` | Ordered list of dhikr items (static). |

### AthkarDailyLog (data class)

Domain view of the persisted log for one group on one date.

| Field | Type | Description |
|---|---|---|
| `groupType` | `AthkarGroupType` | Which group this log belongs to. |
| `date` | `String` | ISO date `"yyyy-MM-dd"`. |
| `counters` | `Map<String, Int>` | itemId → current count (0 ≤ count ≤ item.targetCount). |
| `isComplete` | `Boolean` | True when all items reach their target. Derived, but stored for fast query. |

### TasbeehGoal (data class)

Singleton user preference.

| Field | Type | Description |
|---|---|---|
| `goalCount` | `Int` | Target count (≥ 1). Default: 100. |

### TasbeehDailyTotal (data class)

Cumulative count for a given calendar date.

| Field | Type | Description |
|---|---|---|
| `date` | `String` | ISO date `"yyyy-MM-dd"`. |
| `totalCount` | `Int` | Sum of all flushed session counts since midnight. Always ≥ 0. |

### NotificationPreference (data class)

User preference for one Athkar group's daily reminder. Stored in DataStore, not Room.

| Field | Type | Description |
|---|---|---|
| `groupType` | `AthkarGroupType` | `MORNING` or `EVENING` only (POST_PRAYER has no notification). |
| `enabled` | `Boolean` | Whether the reminder is active. |
| `hour` | `Int` | 0–23. |
| `minute` | `Int` | 0–59. |

---

## Room Entities (`shared:core:database`)

### Schema Version

**3 → 4** via `AutoMigration(from = 3, to = 4)` (pure table additions; no spec class needed).

### AthkarDailyLogEntity

**Table**: `athkar_daily_logs`

Uses a **composite primary key** on `(group_type, date)` — the same pattern as `QuranDailyLogEntity`. No surrogate `id` column.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `group_type` | `TEXT` | PRIMARY KEY (part 1) | Serialized `AthkarGroupType` name (`"MORNING"`, `"EVENING"`, `"POST_PRAYER"`). |
| `date` | `TEXT` | PRIMARY KEY (part 2) | `"yyyy-MM-dd"` ISO string. |
| `counters_json` | `TEXT` | NOT NULL | JSON-serialized `Map<String, Int>`. TypeConverter converts to/from `Map<String, Int>`. |
| `is_complete` | `INTEGER` | NOT NULL | Boolean stored as 0/1. |

**Entity annotation**:
```kotlin
@Entity(tableName = "athkar_daily_logs", primaryKeys = ["group_type", "date"])
```

No separate index needed — the composite primary key already enforces uniqueness on `(group_type, date)`.

**TypeConverter**: `AthkarCountersConverter` — uses `kotlinx-serialization-json` to serialize/deserialize `Map<String, Int>`. Added to `MudawamaDatabase` via `@TypeConverters(AthkarCountersConverter::class)`.

**New dependency**: `kotlinx-serialization-json` must be added to `shared:core:database`'s `commonMain` dependencies and the `kotlinxSerialization` plugin applied to that module's `build.gradle.kts`.

### TasbeehGoalEntity

**Table**: `tasbeeh_goals`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `INTEGER` | PRIMARY KEY | Always `1` (singleton). |
| `goal_count` | `INTEGER` | NOT NULL | Default `100`. |

Mirrors `QuranGoalEntity`.

### TasbeehDailyTotalEntity

**Table**: `tasbeeh_daily_totals`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `date` | `TEXT` | PRIMARY KEY | `"yyyy-MM-dd"` ISO string. |
| `total_count` | `INTEGER` | NOT NULL | Default `0`. |

---

## DAOs (`shared:core:database`)

### AthkarDailyLogDao

```
getLog(groupType: String, date: String): Flow<AthkarDailyLogEntity?>
upsertLog(entity: AthkarDailyLogEntity)
getCompletionStatusForDate(date: String): Flow<List<AthkarDailyLogEntity>>
```

### TasbeehGoalDao

```
getGoal(): Flow<TasbeehGoalEntity?>
upsertGoal(entity: TasbeehGoalEntity)
```

### TasbeehDailyTotalDao

```
getTotalForDate(date: String): Flow<TasbeehDailyTotalEntity?>
upsertTotal(entity: TasbeehDailyTotalEntity)
```

---

## DataStore Preferences (`shared:core:data`)

**File**: `DATA_STORE_FILE_NAME = "session.preferences_pb"` (existing file; new keys added)

| Key name | Type | Default | Description |
|---|---|---|---|
| `athkar_morning_notif_enabled` | `Boolean` | `false` | Morning reminder on/off. |
| `athkar_morning_notif_hour` | `Int` | `6` | Hour of morning reminder (24h). |
| `athkar_morning_notif_minute` | `Int` | `0` | Minute of morning reminder. |
| `athkar_evening_notif_enabled` | `Boolean` | `false` | Evening reminder on/off. |
| `athkar_evening_notif_hour` | `Int` | `18` | Hour of evening reminder (24h). |
| `athkar_evening_notif_minute` | `Int` | `0` | Minute of evening reminder. |

Keys use `booleanPreferencesKey` / `intPreferencesKey` from `androidx.datastore.preferences.core`.

---

## Validation Rules

| Rule | Source | Entity/Field |
|---|---|---|
| `counters[itemId]` must be ≥ 0 and ≤ `item.targetCount` | FR-007, FR-007b | `AthkarDailyLog.counters` values |
| `TasbeehGoal.goalCount` must be ≥ 1 | FR-012 | `TasbeehGoalEntity.goal_count` |
| `TasbeehDailyTotal.totalCount` must be ≥ 0 | FR-016b | `TasbeehDailyTotalEntity.total_count` |
| `NotificationPreference.hour` ∈ [0, 23] | FR-019/020 | DataStore preference |
| `NotificationPreference.minute` ∈ [0, 59] | FR-019/020 | DataStore preference |

---

## State Transitions

### Athkar item counter

```
IDLE (count=0) → COUNTING (0 < count < target) → COMPLETE (count == target)
                                                    └─ no further increment (FR-007b)
```

### Athkar group

```
INCOMPLETE → COMPLETE   (when all items reach target)
COMPLETE   → INCOMPLETE (not applicable in current spec — groups are not manually reset)
```

### Tasbeeh session

```
count=0 → count++ on each tap
count == goal → GOAL_REACHED (completion haptic fires; future taps continue incrementing beyond goal)
Reset → flush sessionCount to dailyTotal DB → count=0
```

Note: The Tasbeeh counter continues incrementing after the goal is reached (for users who wish to do additional repetitions within the same session). Only the *first* time `count == goal` triggers the completion haptic.

---

## Entity Relationship Diagram (text)

```
MudawamaDatabase (v4)
│
├── [existing] HabitEntity          habits
├── [existing] HabitLogEntity       habit_logs
├── [existing] QuranBookmarkEntity  quran_bookmarks
├── [existing] QuranDailyLogEntity  quran_daily_logs
├── [existing] QuranGoalEntity      quran_goals
├── [existing] PrayerTimeCacheEntity prayer_time_cache
│
├── [NEW v4]   AthkarDailyLogEntity  athkar_daily_logs   ← composite PK (group_type, date)
├── [NEW v4]   TasbeehGoalEntity     tasbeeh_goals       ← singleton id=1
└── [NEW v4]   TasbeehDailyTotalEntity tasbeeh_daily_totals ← keyed by date

DataStore ("session.preferences_pb")
└── [NEW keys] athkar_morning_notif_* / athkar_evening_notif_*

shared:core:notifications (NEW module)
└── NotificationScheduler interface (no DB entities)
```
