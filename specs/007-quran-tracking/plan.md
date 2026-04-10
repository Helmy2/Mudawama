# Implementation Plan: Quran Reading Tracker

**Branch**: `007-quran-tracking` | **Date**: 2026-04-10 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/007-quran-tracking/spec.md`

---

## Summary

Implement the Quran Reading Tracker tab for Mudawama. Users log daily Quran pages additively via a bottom sheet, track a reading streak computed from historical session logs, set a daily page goal (defaulting to 5), save a Surah/Ayah bookmark, and navigate past days in read-only mode via a date strip identical to the prayer screen. The feature is fully offline — no network calls. All data persists in Room via three new/modified entities in `shared:core:database` (version 3). The feature is structured as three new KMP modules: `feature:quran:domain`, `feature:quran:data`, `feature:quran:presentation`, following the established prayer/habits module patterns exactly.

---

## Technical Context

**Language/Version**: Kotlin 2.3.20 (Kotlin Multiplatform)  
**Primary Dependencies**: Room 2.8.4 (local storage), Koin 4.2.0 (DI), Compose Multiplatform 1.10.3 (UI), kotlinx-datetime 0.7.1 (date handling), kotlinx-coroutines 1.10.2  
**Storage**: Room for KMP — `shared:core:database`, version bump 2→3 with AutoMigration  
**Testing**: Existing project testing conventions (no new test infrastructure needed)  
**Target Platform**: Android (minSdk 30) + iOS 15+  
**Project Type**: Mobile app (KMP + Compose Multiplatform)  
**Performance Goals**: Log interaction completes in < 1s; date strip navigation < 2s (spec SC-001, SC-005)  
**Constraints**: 100% offline (spec SC-008, A-002); no network calls; all strings via `stringResource`; no hardcoded UI strings  
**Scale/Scope**: Single feature tab; 3 new modules; 3 new/modified Room entities; 5 use cases; 1 ViewModel; 3 bottom sheets; ~35 new string keys

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Rule | Status | Notes |
|------|--------|-------|
| Domain layer: no Android/Room/Ktor/Compose imports | ✅ PASS | `feature:quran:domain` has no platform deps; `SurahMetadata` is pure Kotlin |
| Presentation: Compose Multiplatform only, no XML/Context/UIKit | ✅ PASS | `mudawama.kmp.compose` convention plugin enforced |
| Dependency direction: `:presentation → :domain ← :data` | ✅ PASS | Confirmed in planned build.gradle.kts; presentation MUST NOT depend on `:data` or `:database` |
| No cross-feature dependencies | ✅ PASS | `feature:quran:*` depends only on `shared:core:*` and `shared:designsystem`; no dependency on `feature:prayer` or `feature:habits` |
| Result<D,E> + DomainError pattern | ✅ PASS | `QuranError : DomainError`; all repo interfaces return `Result<T, QuranError>` or `EmptyResult<QuranError>` |
| safeCall{} in data layer | ✅ PASS | All DAO writes wrapped in `safeCall { }` with `onError = { QuranError.DatabaseError }` |
| MVI: State/Action/Event | ✅ PASS | `QuranUiState` / `QuranUiAction` / `QuranUiEvent` defined; ViewModel extends `MviViewModel` |
| No hardcoded strings in @Composable | ✅ PASS | All ~35 strings declared in `shared:designsystem` `strings.xml` before any UI code is written |
| Single strings.xml | ✅ PASS | `feature:quran:presentation` must NOT create its own `composeResources/` |
| Correct `Res` import: `mudawama.shared.designsystem.Res` | ✅ PASS | All composables import from designsystem only |
| Koin only; no Dagger/Hilt | ✅ PASS | `mudawama.kmp.koin` plugin + `viewModelOf` / `factoryOf` DSL |
| CoroutineDispatcher injected; no Dispatchers.IO/Main hardcoded | ✅ PASS | All use cases constructor-inject `CoroutineDispatcher`; bound to `Dispatchers.Default` in Koin module |
| Build plugins single-responsibility | ✅ PASS | No new convention plugins; existing three plugins reused |
| UI must match `docs/ui/` reference screens | ✅ PASS | Contracts defined against `quran_daily_reading_tracker.png`, `log_reading_bottom_sheet.png`, `quran_reading_goal_bottom_sheet.png`, `select_surah_ayah_bottom_sheet.png` |

---

## Project Structure

### Documentation (this feature)

```text
specs/007-quran-tracking/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── contracts/
│   └── ui-contracts.md  ← Phase 1 output
└── checklists/
    └── requirements.md  ← spec quality checklist
```

### Source Code

```text
shared/core/database/
├── src/commonMain/kotlin/…/core/database/
│   ├── MudawamaDatabase.kt                    MODIFY: version 2→3, add 2 entities, AutoMigration spec
│   ├── dao/
│   │   ├── QuranBookmarkDao.kt                MODIFY: remove resetDailyPages()
│   │   ├── QuranDailyLogDao.kt                NEW
│   │   └── QuranGoalDao.kt                    NEW
│   ├── entity/
│   │   ├── QuranBookmarkEntity.kt             MODIFY: remove dailyGoalPages, pagesReadToday columns
│   │   ├── QuranDailyLogEntity.kt             NEW
│   │   └── QuranGoalEntity.kt                 NEW
│   └── di/
│       └── DatabaseModule.kt                  MODIFY: register 2 new DAOs
└── schemas/                                   NEW: schema JSON for version 3 (auto-generated by Room)

feature/quran/
├── domain/
│   ├── build.gradle.kts                       NEW
│   └── src/commonMain/kotlin/…/quran/domain/
│       ├── di/
│       │   └── QuranDomainModule.kt           NEW
│       ├── error/
│       │   └── QuranError.kt                  NEW
│       ├── model/
│       │   ├── QuranDailyLog.kt               NEW
│       │   ├── QuranGoal.kt                   NEW
│       │   ├── QuranBookmark.kt               NEW
│       │   ├── QuranScreenState.kt            NEW  (domain projection, not UI state)
│       │   └── SurahMetadata.kt               NEW  (ALL_SURAHS in-memory list)
│       ├── repository/
│       │   ├── QuranDailyLogRepository.kt     NEW
│       │   ├── QuranGoalRepository.kt         NEW
│       │   └── QuranBookmarkRepository.kt     NEW
│       └── usecase/
│           ├── LogReadingUseCase.kt            NEW
│           ├── ObserveQuranStateUseCase.kt     NEW
│           ├── UpdateBookmarkUseCase.kt        NEW
│           ├── SetGoalUseCase.kt               NEW
│           └── ComputeStreakUseCase.kt         NEW
│
├── data/
│   ├── build.gradle.kts                       NEW
│   └── src/commonMain/kotlin/…/quran/data/
│       ├── di/
│       │   └── QuranDataModule.kt             NEW
│       ├── mapper/
│       │   ├── QuranDailyLogMapper.kt         NEW
│       │   ├── QuranGoalMapper.kt             NEW
│       │   └── QuranBookmarkMapper.kt         NEW
│       └── repository/
│           ├── QuranDailyLogRepositoryImpl.kt NEW
│           ├── QuranGoalRepositoryImpl.kt     NEW
│           └── QuranBookmarkRepositoryImpl.kt NEW
│
└── presentation/
    ├── build.gradle.kts                       NEW
    └── src/commonMain/kotlin/…/quran/presentation/
        ├── di/
        │   └── QuranPresentationModule.kt     NEW
        ├── model/
        │   └── QuranUiState.kt                NEW  (State + Action + Event)
        ├── components/
        │   ├── QuranDateStrip.kt              NEW  (reuse DateChip pattern from prayer)
        │   ├── QuranProgressRing.kt           NEW
        │   ├── QuranGoalCard.kt               NEW
        │   ├── QuranResumeReadingCard.kt      NEW
        │   └── QuranRecentLogsList.kt         NEW
        ├── sheets/
        │   ├── LogReadingSheet.kt             NEW
        │   ├── SetGoalSheet.kt                NEW
        │   └── UpdatePositionSheet.kt         NEW
        ├── QuranScreen.kt                     NEW
        └── QuranViewModel.kt                  NEW

shared/designsystem/
└── src/commonMain/composeResources/values/
    └── strings.xml                            MODIFY: add ~35 quran_* string keys

shared/navigation/
├── src/commonMain/kotlin/…/navigation/
│   ├── Routes.kt                              NO CHANGE (QuranRoute already exists)
│   └── MudawamaAppShell.kt                   MODIFY: add quranScreen lambda, replace placeholder

settings.gradle.kts                            MODIFY: add 3 new module includes
androidApp/ (or iOS entry)                     MODIFY: wire quranPresentationModule() in DI + pass QuranScreen lambda
```

---

## Implementation Phases

### Phase A — Database Changes (`shared:core:database`)

**Deliverables**: version 3 DB with new entities + DAOs, modified bookmark entity, AutoMigration spec.

**Files to create/modify:**

1. **`QuranDailyLogEntity.kt`** (NEW)  
   Fields: `id: String` (PK, UUID), `date: String` ("yyyy-MM-dd"), `pagesRead: Int`, `loggedAt: Long`  
   Table: `quran_daily_logs`

2. **`QuranGoalEntity.kt`** (NEW)  
   Fields: `id: Int = 1` (PK, singleton), `pagesPerDay: Int`, `updatedAt: Long`  
   Table: `quran_goals`

3. **`QuranBookmarkEntity.kt`** (MODIFY)  
   Remove columns `dailyGoalPages` and `pagesReadToday`. Add `@DeleteColumn` annotations to trigger auto-migration.

4. **`QuranDailyLogDao.kt`** (NEW)  
   Queries: `insertLog`, `getLogsForDate` (Flow), `getLogsBefore` (Flow, for recent logs), `getAllLoggedDates` (suspend, for streak)

5. **`QuranGoalDao.kt`** (NEW)  
   Queries: `upsertGoal` (`@Upsert`), `getGoal` (Flow returning nullable)

6. **`QuranBookmarkDao.kt`** (MODIFY)  
   Remove `resetDailyPages()` — column no longer exists.

7. **`MudawamaDatabase.kt`** (MODIFY)  
   - Bump `version = 3`  
   - Add `QuranDailyLogEntity`, `QuranGoalEntity` to `entities`  
   - Add `AutoMigration(from = 2, to = 3, spec = AutoMigration_2_3::class)`  
   - Add inner class `AutoMigration_2_3 : AutoMigrationSpec` with `@DeleteColumn` annotations  
   - Add `abstract fun quranDailyLogDao(): QuranDailyLogDao`  
   - Add `abstract fun quranGoalDao(): QuranGoalDao`

8. **`DatabaseModule.kt`** (MODIFY)  
   Register: `single<QuranDailyLogDao> { get<MudawamaDatabase>().quranDailyLogDao() }`  
   Register: `single<QuranGoalDao> { get<MudawamaDatabase>().quranGoalDao() }`

**Build system:** No new KSP config needed — `kspAndroid` and `kspIos*` entries in `shared:core:database/build.gradle.kts` already cover all targets.

---

### Phase B — Module Scaffolding

**Deliverables**: Three new Gradle modules registered and buildable (empty).

1. Add to `settings.gradle.kts`:
   ```kotlin
   include(":feature:quran:domain")
   include(":feature:quran:data")
   include(":feature:quran:presentation")
   ```

2. Create `feature/quran/domain/build.gradle.kts`:
   ```kotlin
   plugins {
       id("mudawama.kmp")
       id("mudawama.kmp.koin")
   }
   kotlin {
       android { namespace = "io.github.helmy2.mudawama.quran.domain" }
       configureIosFramework("FeatureQuranDomain")
       sourceSets {
           commonMain.dependencies {
               implementation(libs.kotlinx.coroutines.core)
               implementation(libs.kotlinx.datetime)
               implementation(projects.shared.core.time)
               api(projects.shared.core.domain)
           }
       }
   }
   ```

3. Create `feature/quran/data/build.gradle.kts`:
   ```kotlin
   plugins {
       id("mudawama.kmp")
       id("mudawama.kmp.koin")
   }
   kotlin {
       android { namespace = "io.github.helmy2.mudawama.quran.data" }
       configureIosFramework("FeatureQuranData")
       sourceSets {
           commonMain.dependencies {
               implementation(projects.feature.quran.domain)
               implementation(projects.shared.core.database)
               implementation(projects.shared.core.time)
               implementation(projects.shared.core.domain)
               implementation(libs.kotlinx.datetime)
               implementation(libs.kotlinx.coroutines.core)
           }
       }
   }
   ```

4. Create `feature/quran/presentation/build.gradle.kts`:
   ```kotlin
   plugins {
       id("mudawama.kmp.compose")
   }
   kotlin {
       android { namespace = "io.github.helmy2.mudawama.quran.presentation" }
       configureIosFramework("FeatureQuranPresentation", isStatic = true)
       sourceSets {
           commonMain.dependencies {
               implementation(projects.feature.quran.domain)
               implementation(projects.shared.core.presentation)
               implementation(projects.shared.designsystem)
               implementation(libs.bundles.compose)
               implementation(libs.bundles.lifecycle)
               implementation(libs.koin.compose.viewmodel)
               implementation(libs.kotlinx.datetime)
               implementation(libs.material.icons.extended)
           }
           androidMain.dependencies {
               implementation(libs.androidx.activity.compose)
               implementation(libs.ui.tooling)
               implementation(libs.koin.android)
           }
       }
   }
   ```

---

### Phase C — Domain Layer

**Deliverables**: All domain models, repository interfaces, use cases, error type, Koin module.

**Dependency chain inside domain**: `QuranError` → repository interfaces → use cases → `QuranDomainModule`

**Key implementation notes:**

- `SurahMetadata.kt`: declare `ALL_SURAHS` as a top-level `val` — all 114 entries hardcoded. This is the single source of truth for Surah names and Ayah counts used by both `UpdateBookmarkUseCase` (validation) and `QuranBookmarkMapper` (name lookup).

- `ObserveQuranStateUseCase`: use `combine()` on four flows:
  ```kotlin
  combine(
      goalRepo.observeGoal(),
      logRepo.observeLogsForDate(dateString),
      bookmarkRepo.observeBookmark(),
      logRepo.observeRecentLogs(beforeDate),       // 4th flow — reactive, not .first()
  ) { goal, logs, bookmark, recentRaw ->
      val pagesTotal = logs.sumOf { it.pagesRead }
      QuranScreenState(pagesTotal, goal.pagesPerDay, bookmark, buildRecentEntries(recentRaw, goal))
  }
  ```
  All four inputs are live `Flow`s — `recentLogs` reacts to new log entries automatically without a blocking `.first()` call inside the lambda.

- `ComputeStreakUseCase`: pure algorithm, no Flow:
  ```kotlin
  suspend operator fun invoke(): Int = withContext(dispatcher) {
      val dates = repo.getAllLoggedDates()  // descending list of "yyyy-MM-dd"
          .getOrNull() ?: return@withContext 0
      val yesterday = timeProvider.logicalDate().minus(DatePeriod(days = 1))
      var streak = 0
      var expected = yesterday
      for (dateStr in dates) {
          val date = LocalDate.parse(dateStr)
          if (date > yesterday) continue          // skip today (not yet closed)
          if (date == expected) { streak++; expected = expected.minus(DatePeriod(days = 1)) }
          else break                              // gap found
      }
      streak
  }
  ```

- `SetGoalUseCase` validates `pagesPerDay in 1..MAX_DAILY_GOAL_PAGES` (= 60). Values outside this range return `Failure(QuranError.InvalidGoal)`. `LogReadingUseCase` continues to validate `pages in 1..MAX_SESSION_PAGES` (= 604) — these are separate constants with separate error cases.
  ```kotlin
  override fun observeGoal(): Flow<QuranGoal> =
      dao.getGoal().map { it?.toDomain() ?: DEFAULT_DAILY_GOAL }
  ```

  `ObserveQuranStateUseCase` then aggregates these into `RecentLogEntry` with per-date sums.

---

### Phase D — Data Layer

**Deliverables**: Three repository implementations, mappers, Koin module.

**Key implementation notes:**

- All DAO writes wrapped in `safeCall { block() } { QuranError.DatabaseError }`.
- `QuranBookmarkMapper.toDomain()` resolves `surahName` from `ALL_SURAHS[entity.surah - 1].nameEn`.
- `QuranDailyLogRepositoryImpl.observeRecentLogs()`: the DAO returns all logs before `beforeDate` ordered by `date DESC, loggedAt DESC`. The repository groups by date in memory, keeps only the 3 most recent distinct dates, and sums pages per date before returning:
  ```kotlin
  override fun observeRecentLogs(beforeDate: String): Flow<List<QuranDailyLog>> =
      dao.getLogsBefore(beforeDate)
          .map { entities ->
              entities.groupBy { it.date }
                  .entries.take(3)
                  .flatMap { (_, logs) -> logs.map { it.toDomain() } }
          }
  ```
- `QuranGoalRepository.observeGoal()`: return `DEFAULT_DAILY_GOAL` when `QuranGoalEntity` row is absent:

### Phase E — Presentation Layer

**Deliverables**: `QuranViewModel`, `QuranScreen`, 5 components, 3 bottom sheets, Koin module.

**ViewModel initialization pattern** (mirrors prayer module):
```kotlin
class QuranViewModel(
    private val observeQuranStateUseCase: ObserveQuranStateUseCase,
    private val logReadingUseCase: LogReadingUseCase,
    private val setGoalUseCase: SetGoalUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val computeStreakUseCase: ComputeStreakUseCase,
    private val timeProvider: TimeProvider,
) : MviViewModel<QuranUiState, QuranUiAction, QuranUiEvent>(
    initialState = QuranUiState(
        selectedDate = today,
        today = today,
        dateStrip = generateDateStrip(today),  // [today-6 … today], today is rightmost
    )
) {
    init {
        intent {
            val streak = computeStreakUseCase()
            reduce { copy(streak = streak, isLoading = false) }

            // Date-reactive observation — mirrors PrayerViewModel pattern
            state.collectLatest { currentState ->
                observeQuranStateUseCase(currentState.selectedDate).collect { screenState ->
                    reduce {
                        copy(
                            pagesReadToday = screenState.pagesReadToday,
                            goalPages = screenState.goalPages,
                            bookmark = screenState.bookmark,
                            recentLogs = screenState.recentLogs,
                        )
                    }
                }
            }
        }
    }
```

**`onAction` handling:**
- `SelectDate`: `reduce { copy(selectedDate = action.date) }` — `collectLatest` automatically cancels old observation and re-subscribes for new date.
- `ConfirmLogReading`: `exclusiveIntent("log") { logReadingUseCase(pages, selectedDate); recomputeStreak() }` — `exclusiveIntent` prevents double-taps.
- `ConfirmSetGoal`: `intent { setGoalUseCase(pages) }` — goal Flow updates automatically.
- `ConfirmUpdatePosition`: `intent { updateBookmarkUseCase(surah, ayah); restorePreviousSheet() }`.
- `QuranGoalCard` receives `isReadOnly` and hides its internal "Log Reading" CTA when `true`. The card body tap (opens `SetGoalSheet`) always remains active.

**`UpdatePositionSheet` navigation from `LogReadingSheet`:**
- `OpenUpdatePositionFromLogSheet` action: `reduce { copy(logReadingSheetVisible = false, updatePositionSheetVisible = true, returnToLogReadingSheet = true) }`.
- `DismissUpdatePositionSheet` when `returnToLogReadingSheet == true`: `reduce { copy(updatePositionSheetVisible = false, logReadingSheetVisible = true, returnToLogReadingSheet = false) }`.

**Components delegation:**
- `QuranDateStrip` — identical layout to `PrayerDateStrip`; copy chip visual logic; replace with `QuranDateStrip` name. **Strip range is `[today-6 … today]` (past-only, no future dates)** — generate via `(-6..0).map { today.plus(DatePeriod(days = it)) }`, so today is the rightmost chip.
- `QuranProgressRing` — large `CircularProgressIndicator` (arc style, `12dp` stroke), center text, subtitle. Use `animateFloatAsState` for arc progress.
- `LogReadingSheet`, `SetGoalSheet`, `UpdatePositionSheet` — all use `ModalBottomSheet(sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true))`.
- `UpdatePositionSheet` Ayah picker — use `LazyColumn` scroll-snap (no platform `NumberPicker`) to stay in `commonMain`.

---

### Phase F — Navigation & String Resources

**Deliverables**: Navigation wired, all strings added, entry point registered.

**`MudawamaAppShell.kt`** changes:
1. Add `quranScreen: @Composable () -> Unit` parameter (after `prayerScreen`).
2. Replace `entry<QuranRoute> { QuranPlaceholderScreen() }` with `entry<QuranRoute> { quranScreen() }`.

**`strings.xml`** — add all ~35 `quran_*` keys (full list in `research.md` section 8). Add **before** writing any Composable code to avoid build violations.

**App entry** (androidApp + iOS):
1. Load `quranDataModule()`, `quranDomainModule`, `quranPresentationModule()` in Koin start block (after `coreDatabaseModule`).
2. Pass `{ QuranScreen() }` as `quranScreen` lambda to `MudawamaAppShell`.

---

## Complexity Tracking

> No constitution violations requiring justification.

The plan introduces 3 new modules — standard for the existing architecture (prayer and habits each have 3). The new entities follow the existing DB schema conventions exactly. No new convention plugins, no new library dependencies, no cross-feature dependencies.

---

## Dependencies & Prerequisites

| Prerequisite | Status |
|-------------|--------|
| `shared:core:database` Room + KSP setup | Existing |
| `shared:core:domain` — `Result`, `DomainError`, `safeCall` | Existing |
| `shared:core:presentation` — `MviViewModel` | Existing |
| `shared:designsystem` — `strings.xml`, theme, components | Existing |
| `shared:navigation` — `QuranRoute` | Existing (placeholder) |
| Koin modules load order in app entry | Requires modification |
| `MudawamaAppShell` — `quranScreen` lambda | Requires modification |

---

## Key Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| Room AutoMigration 2→3 fails if `@DeleteColumn` annotations are missing | Test migration on a device with an existing version-2 DB before merging |
| `combine()` in `ObserveQuranStateUseCase` emits stale `recentLogs` if date selection changes mid-collection | `collectLatest` in ViewModel cancels the entire outer flow on date change; inner `combine` is automatically re-subscribed |
| `UpdatePositionSheet` sheet-within-sheet navigation feels laggy | Dismiss first sheet before showing second; use `returnToLogReadingSheet` flag — both transitions complete in < 300ms per design rules |
| `ALL_SURAHS` Ayah counts are incorrect | Cross-reference against authoritative Quran metadata source; counts in `data-model.md` are from the Uthmani mushaf standard |
| Hardcoded string literals accidentally left in Composables | CI constitution check (`grep 'Text\("'`) catches violations before merge |
