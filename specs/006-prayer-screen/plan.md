# Implementation Plan: Prayer Tracking Screen

**Branch**: `006-prayer-screen` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)

---

## Summary

Implement FR-1 (Prayer Tracking System) end-to-end:

1. Extend `LogStatus` with `MISSED` in `feature:habits:domain` and audit all exhaustive `when` sites.
2. Create three new Gradle modules — `:feature:prayer:domain`, `:feature:prayer:data`, `:feature:prayer:presentation` — following the same conventions as `:feature:habits`.
3. Add a `LocationProvider` interface + platform `expect`/`actual` implementations in `:shared:core`.
4. Add a `PrayerTimeCacheEntity` Room entity and `PrayerTimeCacheDao` to `:shared:core:database`; bump Room schema version to 2.
5. Seed the 5 obligatory prayers as core habits via an idempotent `SeedPrayerHabitsUseCase` called from app startup.
6. Fetch prayer times from the Aladhan API (`/v1/timings`) via Ktor in `:feature:prayer:data`; cache by ISO date in Room.
7. Replace `PrayerPlaceholderScreen` with a full `PrayerScreen` — date strip, completion hero, 5 prayer rows — wired to `PrayerViewModel` (MVI).
8. Wire all new modules into the app's Koin graph and `MudawamaAppShell`.

---

## Technical Context

**Language/Version**: Kotlin 2.3.20 (KMP), targeting Android (minSdk 30) and iOS 15+
**Primary Dependencies**: Ktor 3.4.1 (network), Room 2.8.4 (cache), Koin 4.2.0 (DI), Compose Multiplatform 1.10.3 (UI), `kotlinx-serialization-json` 1.10.0 (JSON), `kotlinx-datetime` 0.7.1 (date strip)
**Storage**: Room — existing `MudawamaDatabase` (version → 2); one new entity `PrayerTimeCacheEntity`
**Testing**: `FakeTimeProvider` for deterministic date tests; Ktor `MockEngine` for API response tests
**Target Platform**: Android + iOS (KMP `commonMain` for all domain/data logic)
**Project Type**: Kotlin Multiplatform mobile app (KMP + CMP)
**Performance Goals**: Prayer list visible in < 1 s from cache; first-time API fetch < 3 s on 4G
**Constraints**: Offline-first (no network required after first fetch per day); all domain code pure Kotlin; no `Dispatchers.IO` hardcoding; all strings in `shared/designsystem/strings.xml`
**Scale/Scope**: 5 fixed prayers, 1 Aladhan API endpoint, 3 new Gradle modules, 1 new Room entity, 1 new `expect`/`actual` interface

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Rule | Status | Notes |
|---|---|---|
| Domain layer: pure Kotlin, no platform imports | ✅ | `feature:prayer:domain` will mirror `feature:habits:domain` conventions |
| Presentation: Compose Multiplatform only | ✅ | `feature:prayer:presentation` uses `mudawama.kmp.compose` plugin |
| Dependency direction: `presentation → domain ← data` | ✅ | No cross-feature module dependencies |
| Feature modules MUST NOT depend on other feature modules | ✅ | Prayer data shares `HabitDao`/`HabitLogDao` via `shared:core:database` only |
| Network: Ktor only (no Retrofit) | ✅ | `bundles.ktor` used in `feature:prayer:data` |
| Database: Room only (no SQLDelight) | ✅ | `PrayerTimeCacheEntity` via Room |
| DI: Koin only | ✅ | `factoryOf` / `single<Interface>` pattern |
| `safeCall` wraps all network/DB calls | ✅ | All data implementations use `safeCall { } onError { }` |
| `CoroutineDispatcher` injected, not hardcoded | ✅ | All use-cases accept injected dispatcher |
| All strings in `shared/designsystem/strings.xml` | ✅ | New prayer strings added there only |
| UI matches `docs/ui/daily_prayer_tracker.png` | ✅ | `PrayerScreen` composable references that file |
| `LogStatus.MISSED`: exhaustive `when` required, no `else` | ✅ | Audit step listed explicitly in task list |

**No gate violations detected. Proceeding.**

---

## Project Structure

### Documentation (this feature)

```text
specs/006-prayer-screen/
├── plan.md              ← This file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── domain-api.md    ← Phase 1 output
│   └── presentation-composables.md  ← Phase 1 output
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```text
# Cross-cutting changes
feature/habits/domain/src/commonMain/.../model/
└── LogStatus.kt                        ← Add MISSED; audit all when() sites

# New shared:core addition
shared/core/src/commonMain/.../location/
├── LocationProvider.kt                 ← interface (expect not needed — pure Kotlin)
└── Coordinates.kt                      ← data class(lat: Double, lon: Double)
shared/core/src/androidMain/.../location/
└── AndroidLocationProvider.kt          ← FusedLocationProviderClient impl
shared/core/src/iosMain/.../location/
└── IosLocationProvider.kt              ← CLLocationManager impl

# New Room entity + DAO in shared:core:database
shared/core/database/src/commonMain/.../
├── entity/
│   └── PrayerTimeCacheEntity.kt        ← @Entity table="prayer_time_cache"
├── dao/
│   └── PrayerTimeCacheDao.kt           ← @Query by date
└── MudawamaDatabase.kt                 ← version 1→2, add entity + dao accessor

# New feature modules
feature/prayer/
├── domain/
│   ├── build.gradle.kts
│   └── src/commonMain/.../prayer/domain/
│       ├── di/PrayerDomainModule.kt
│       ├── error/PrayerError.kt
│       ├── model/
│       │   ├── PrayerName.kt           ← enum: FAJR, DHUHR, ASR, MAGHRIB, ISHA
│       │   ├── PrayerTime.kt           ← domain model
│       │   └── PrayerWithStatus.kt     ← projection for UI
│       ├── repository/
│       │   ├── PrayerTimesRepository.kt
│       │   └── PrayerHabitRepository.kt  ← seeds + observes prayer habits
│       └── usecase/
│           ├── ObservePrayersForDateUseCase.kt
│           ├── TogglePrayerStatusUseCase.kt
│           ├── MarkPrayerMissedUseCase.kt
│           └── SeedPrayerHabitsUseCase.kt
├── data/
│   ├── build.gradle.kts
│   └── src/commonMain/.../prayer/data/
│       ├── di/PrayerDataModule.kt
│       ├── dto/AladhanTimingsDto.kt    ← @Serializable response shape
│       ├── mapper/
│       │   └── AladhanMapper.kt
│       └── repository/
│           ├── PrayerTimesRepositoryImpl.kt  ← Ktor + Room cache
│           └── PrayerHabitRepositoryImpl.kt  ← delegates to HabitDao
└── presentation/
    ├── build.gradle.kts
    └── src/commonMain/.../prayer/presentation/
        ├── di/PrayerPresentationModule.kt
        ├── model/
        │   ├── PrayerUiState.kt
        │   ├── PrayerUiAction.kt
        │   └── PrayerUiEvent.kt
        ├── PrayerViewModel.kt
        ├── PrayerScreen.kt
        └── components/
            ├── PrayerDateStrip.kt
            ├── PrayerCompletionHero.kt
            └── PrayerRowItem.kt

# Navigation wiring
shared/navigation/src/commonMain/.../navigation/
├── MudawamaAppShell.kt                 ← swap PrayerPlaceholderScreen → PrayerScreen lambda
└── Placeholders.kt                     ← remove PrayerPlaceholderScreen (or keep for fallback)
```

**Structure Decision**: Three-module packaging-by-feature (`domain`/`data`/`presentation`) identical to `:feature:habits`. `LocationProvider` lives in `:shared:core` (not `:shared:core:domain`) because it is a platform capability, not a domain business rule. `PrayerTimeCacheEntity` lives in `:shared:core:database` because all Room entities live there (established convention from `HabitEntity`).

---

## Phase 0: Research Decisions

*See [research.md](./research.md) for full rationale. Key decisions summarised here:*

| # | Decision | Choice |
|---|---|---|
| D-01 | Aladhan API endpoint | `GET /v1/timings/{DD-MM-YYYY}?latitude=&longitude=&method=2` |
| D-02 | Cache key | ISO date string `yyyy-MM-dd` only (no location component per spec) |
| D-03 | Cache invalidation | Absent-if-no-row-for-today — no TTL column; new day = new fetch |
| D-04 | Prayer seeding mechanism | `SeedPrayerHabitsUseCase` called from app init (Koin eager singleton); idempotent via `HabitDao.getHabitsByCategory("prayer")` check |
| D-05 | `LogStatus.MISSED` toggle rules | Tap-toggle: `PENDING↔COMPLETED`; tap on MISSED → COMPLETED; MISSED only via long-press sheet |
| D-06 | `LocationProvider` pattern | Pure Kotlin interface in `shared:core`; `actual` implementations in `androidMain`/`iosMain` of same module |
| D-07 | Prayer habit IDs | Stable deterministic UUIDs (hardcoded constants) so seeding is idempotent across installs |
| D-08 | `TogglePrayerStatusUseCase` | Delegates to existing `ToggleHabitCompletionUseCase` (prayers are habits); no duplication |
| D-09 | `MarkPrayerMissedUseCase` | Dedicated use case; sets `LogStatus.MISSED` directly, never cycles through toggle |
| D-10 | `PrayerName` enum ordering | Declared in chronological prayer order so `PrayerName.entries` can be used as display sort key |

---

## Phase 1: Design Decisions

*See [data-model.md](./data-model.md) and [contracts/](./contracts/) for full specs.*

### Key Design Choices

**`LogStatus` placement**: `LogStatus` stays in `feature:habits:domain`. The prayer feature's domain depends on `feature:habits:domain` **only** for this shared enum — this is the single allowed cross-feature dependency (both features use the same `HabitLog` / `HabitLogEntity`). All other prayer domain types are in `feature:prayer:domain`.

**Prayer logs reuse `HabitLog`**: Prayer status is stored in the existing `habit_logs` table using the prayer habit's ID. `MISSED` maps cleanly. No new table.

**`PrayerWithStatus` projection**: Composed in `ObservePrayersForDateUseCase` by joining the 5 prayer `HabitWithStatus` rows (from `ObserveHabitsWithTodayStatusUseCase` filtered to `category = "prayer"`) with the cached `PrayerTime` values for that date.

**Date strip state in ViewModel**: The selected date is `MutableStateFlow<LocalDate>` inside `PrayerViewModel`. Changing the date triggers a new `ObservePrayersForDateUseCase` collection. Today detection uses `TimeProvider.logicalDate()`.

**MudawamaAppShell wiring**: `MudawamaAppShell` will gain a `prayerScreen: @Composable () -> Unit` slot (identical pattern to `habitsScreen`). The Android/iOS app entry points inject `PrayerScreen { PrayerViewModel }`.

---

## Complexity Tracking

No constitution violations. No complexity justifications required.

---

## Build File Sketches

### `feature/prayer/domain/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.prayer.domain" }
    configureIosFramework("FeaturePrayerDomain")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(projects.shared.core.time)
            api(projects.shared.core.domain)
            // EXCEPTION: shared LogStatus enum — depends on habits:domain for that enum only
            implementation(projects.feature.habits.domain)
        }
    }
}
```

### `feature/prayer/data/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.prayer.data" }
    configureIosFramework("FeaturePrayerData")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.prayer.domain)
            implementation(projects.shared.core.database)
            implementation(projects.shared.core.time)
            implementation(projects.shared.core)        // LocationProvider
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.ktor)
        }
        androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
        iosMain.dependencies    { implementation(libs.ktor.client.darwin) }
    }
}
```

### `feature/prayer/presentation/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp.compose")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.prayer.presentation" }
    configureIosFramework("FeaturePrayerPresentation", isStatic = true)
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.prayer.domain)
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.compose)
            implementation(libs.bundles.lifecycle)
            implementation(libs.compose.resources)
            implementation(libs.koin.compose.viewmodel)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)
            implementation(libs.kotlinx.datetime)
            implementation(libs.material.icons.extended)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ui.tooling)
            implementation(libs.ui.tooling.preview)
            implementation(libs.koin.android)
        }
    }
}
```

---

## `settings.gradle.kts` additions

```kotlin
include(":shared:core")          // already exists — add LocationProvider here
include(":feature:prayer:domain")
include(":feature:prayer:data")
include(":feature:prayer:presentation")
```

---

## MudawamaDatabase migration plan

```
Version 1 → 2
  Added table: prayer_time_cache
    date        TEXT PRIMARY KEY  (yyyy-MM-dd)
    fajr        TEXT NOT NULL     (HH:mm)
    dhuhr       TEXT NOT NULL
    asr         TEXT NOT NULL
    maghrib     TEXT NOT NULL
    isha        TEXT NOT NULL
    fetchedAt   INTEGER NOT NULL  (epoch ms)

Migration strategy: AutoMigration(from = 1, to = 2) — table addition only,
no destructive changes, Room can auto-generate this migration.
```

---

## Aladhan API Response Shape (relevant fields only)

```
GET https://api.aladhan.com/v1/timings/08-04-2026
    ?latitude=21.3891&longitude=39.8579&method=2

Response (200):
{
  "code": 200,
  "data": {
    "timings": {
      "Fajr":    "04:52",
      "Sunrise": "06:14",
      "Dhuhr":   "12:27",
      "Asr":     "15:45",
      "Sunset":  "18:40",
      "Maghrib": "18:40",
      "Isha":    "20:10",
      "Imsak":   "04:42",
      "Midnight":"00:27",
      "Firstthird": "22:31",
      "Lastthird":  "02:23"
    }
  }
}

Only Fajr, Dhuhr, Asr, Maghrib, Isha are consumed.
Date parameter format: DD-MM-YYYY (path segment).
```

---

## `LogStatus` audit plan

Files to update after adding `MISSED`:

| File | Change |
|---|---|
| `feature/habits/domain/.../model/LogStatus.kt` | Add `MISSED` entry |
| `feature/habits/domain/.../usecase/ToggleHabitCompletionUseCase.kt` | `when(existing.status)` → add `MISSED -> LogStatus.COMPLETED` branch |
| `feature/habits/domain/.../usecase/DecrementHabitCountUseCase.kt` | `when(existing.status)` (if any) → add `MISSED` branch |
| `feature/habits/presentation/.../HabitListItem.kt` | Any `when(status)` → add `MISSED` branch (render same as PENDING for personal habits) |
| `feature/habits/presentation/.../HabitsViewModel.kt` | Any `when(status)` → add `MISSED` branch |
| `feature/prayer/domain/.../usecase/TogglePrayerStatusUseCase.kt` | Handles MISSED → COMPLETED in tap path |
| `feature/prayer/domain/.../usecase/MarkPrayerMissedUseCase.kt` | Always sets MISSED |
| `shared/core/database/.../mapper/*` | Mapper: unknown string → PENDING fallback |

---

## Idempotent Prayer Habit Seeding

```kotlin
// Stable IDs — never change after first release
object PrayerHabitIds {
    const val FAJR    = "habit-prayer-fajr-00000000"
    const val DHUHR   = "habit-prayer-dhuhr-0000000"
    const val ASR     = "habit-prayer-asr-000000000"
    const val MAGHRIB = "habit-prayer-maghrib-00000"
    const val ISHA    = "habit-prayer-isha-00000000"
}

// SeedPrayerHabitsUseCase.invoke():
//   1. dao.getHabitsByCategory("prayer").first()
//   2. If count == 5 → return (idempotent guard)
//   3. Insert missing prayers (upsert with IGNORE conflict strategy)
//   4. Called once from app startup Koin eager block
```

---

## PrayerViewModel MVI contract

```
State:  PrayerUiState(
    selectedDate: LocalDate,          // drives date strip highlight
    dateStrip: List<LocalDate>,       // 3 past + today + 3 future = 7
    prayers: List<PrayerWithStatus>,  // 5 items, sorted chronologically
    isLoading: Boolean,
    timesAvailable: Boolean,          // false = show "—" placeholder times
    usingFallbackLocation: Boolean,   // true = permission denied, using Mecca
    missedSheetPrayer: PrayerWithStatus?, // non-null = show MISSED action sheet
)

Actions: PrayerUiAction {
    SelectDate(date: LocalDate)
    TogglePrayer(prayerHabitId: String)
    MarkMissedRequested(prayer: PrayerWithStatus)   // opens sheet
    ConfirmMarkMissed(prayerHabitId: String)
    DismissMissedSheet
}

Events: PrayerUiEvent {
    ShowError(message: StringResource)
}
```
