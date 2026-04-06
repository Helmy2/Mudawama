# Implementation Plan: feature:habits — Custom Habit Management & Daily Tracking

**Branch**: `005-feature-habits` | **Date**: 2026-04-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-feature-habits/spec.md`

---

## Summary

Deliver the `feature:habits` vertical slice as **three Gradle sub-modules**:

| Module | Plugin | Role |
|--------|--------|------|
| `feature:habits:domain` | `mudawama.kmp.library` + `mudawama.kmp.koin` | Pure-Kotlin models, repository interfaces, 7 use cases |
| `feature:habits:data` | `mudawama.kmp.data` | Room-backed repository implementations; entity ↔ domain mappers |
| `feature:habits:presentation` | `mudawama.kmp.presentation` | Compose Multiplatform screen, ViewModel, bottom-sheet components |

Upon delivery, `HabitsPlaceholderScreen` in `shared:navigation` is replaced by the real `HabitsScreen`
via a content-slot refactor of `MudawamaAppShell` (see §Phase 1 §5 — Navigation Wiring).
All UI code lives in `commonMain`. The feature is entirely offline-first — no network calls.

---

## Technical Context

**Language/Version**: Kotlin 2.3.20 (KMP — `commonMain` only for `:domain` and `:presentation`;
`commonMain` + platform source sets handled transparently by `mudawama.kmp.data` for `:data`)
**Primary Dependencies**:
- `shared:core:database` — `HabitDao`, `HabitLogDao`, `MudawamaDatabase` (Room 2.8.4)
- `shared:core:time` — `TimeProvider`, `DateFormatters`, `FakeTimeProvider`
- `shared:core:domain` — `DomainError`, `Result`, `EmptyResult`
- `shared:core:presentation` — `MviViewModel` (base class for `HabitsViewModel`)
- `shared:designsystem` — colour, typography, shape tokens; predefined icon set
- `shared:navigation` — `HabitsRoute` (existing), `MudawamaAppShell` (refactored)
- `kotlinx.datetime 0.7.1` — `LocalDate`, `DayOfWeek`, `DatePeriod`
- `koin-core 4.2.0` (BOM) — dependency injection for all three modules
**Storage**: Room DB via `shared:core:database` (no schema migration required)
**Testing**: `kotlin.test` in `commonTest`; Room in-memory DB for integration tests
**Target Platform**: Android (minSdk 30) + iOS (arm64, x64, simulatorArm64) — shared `commonMain`
**Performance Goals**: Habits list (≤ 50 items) renders in < 1 s from screen entry (SC-006);
Boolean completion control responds within 300 ms (SC-002)
**Constraints**: No `androidMain`/`iosMain` in `:presentation` (FR-018); no direct DB dependency
in `:presentation` (FR-019); all date strings from `TimeProvider` only (FR-010)
**Scale/Scope**: ~25 source files across 3 modules; ~14 unit tests in `:domain`

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| # | Rule | Status | Notes |
|---|------|--------|-------|
| 1 | Presentation layer — Compose MPP only; no XML, no Context, no UIKit | ✅ PASS | 100 % `commonMain`; `MviViewModel` extends AndroidX `ViewModel` via JetBrains lifecycle KMP |
| 2 | Dependency direction — no feature→feature imports | ✅ PASS | `feature:habits:*` depends only on `shared:*` modules; no cross-feature imports |
| 3 | DI uses Koin only | ✅ PASS | All three modules expose `fun habits*Module()` factory functions; no Dagger/Hilt |
| 4 | No `Dispatchers.IO` / `Dispatchers.Main` hardcoded | ✅ PASS | `MviViewModel.intent { }` uses `viewModelScope`; Room Flow automatically dispatches on a background thread |
| 5 | No SQLDelight, no Retrofit | ✅ PASS | Uses Room (`shared:core:database`); no Retrofit (offline-first) |
| 6 | No hardcoded colors or dimension literals | ✅ PASS | All tokens from `MudawamaTheme`; spacing as `dp` local vals |
| 7 | UI components MUST provide `@Preview` wrappers | ✅ PASS | `HabitsScreen`, `HabitListItem`, `HabitHeatmapRow`, `HabitBottomSheet`, `HabitOptionsSheet` each have `@Preview` companions |
| 8 | Code style — idiomatic Kotlin 2.x, composition over inheritance | ✅ PASS | `sealed interface` for errors, actions, events, and sheet mode; `data class` for state; `data object` singletons |

**Post-design re-check**: All checks remain green. `kotlin.uuid.Uuid.random()` (`@ExperimentalUuidApi`)
requires an opt-in annotation in `CreateHabitUseCase` and the log-creation use cases — acceptable per
Kotlin 2.x experimental API policy.

---

## Project Structure

### Documentation (this feature)

```
specs/005-feature-habits/
├── plan.md                             # This file
├── spec.md                             # Approved feature specification
├── research.md                         # Phase 0 output
├── data-model.md                       # Phase 1 output
├── quickstart.md                       # Phase 1 output
├── contracts/
│   ├── domain-api.md                   # Repository + use case API contract
│   └── presentation-composables.md    # Composable API contract
└── tasks.md                            # Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```
feature/
└── habits/
    ├── domain/
    │   ├── build.gradle.kts
    │   └── src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/
    │       ├── model/
    │       │   ├── Habit.kt
    │       │   ├── HabitLog.kt
    │       │   ├── HabitType.kt
    │       │   ├── LogStatus.kt
    │       │   └── HabitWithStatus.kt
    │       ├── error/
    │       │   └── HabitError.kt
    │       ├── repository/
    │       │   ├── HabitRepository.kt
    │       │   └── HabitLogRepository.kt
    │       ├── usecase/
    │       │   ├── ObserveHabitsWithTodayStatusUseCase.kt
    │       │   ├── CreateHabitUseCase.kt
    │       │   ├── UpdateHabitUseCase.kt
    │       │   ├── DeleteHabitUseCase.kt
    │       │   ├── ToggleHabitCompletionUseCase.kt
    │       │   ├── IncrementHabitCountUseCase.kt
    │       │   └── ObserveWeeklyHeatmapUseCase.kt
    │       ├── util/
    │       │   └── IdGenerator.kt
    │       └── di/
    │           └── HabitsDomainModule.kt
    │
    ├── data/
    │   ├── build.gradle.kts
    │   └── src/commonMain/kotlin/io/github/helmy2/mudawama/habits/data/
    │       ├── mapper/
    │       │   ├── HabitMapper.kt
    │       │   └── HabitLogMapper.kt
    │       ├── repository/
    │       │   ├── HabitRepositoryImpl.kt
    │       │   └── HabitLogRepositoryImpl.kt
    │       └── di/
    │           └── HabitsDataModule.kt
    │
    └── presentation/
        ├── build.gradle.kts
        └── src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/
            ├── model/
            │   ├── HabitsUiState.kt
            │   ├── HabitsUiAction.kt
            │   ├── HabitsUiEvent.kt
            │   └── BottomSheetMode.kt
            ├── components/
            │   ├── HabitListItem.kt
            │   ├── HabitHeatmapRow.kt
            │   ├── HabitBottomSheet.kt
            │   └── HabitOptionsSheet.kt
            ├── HabitsViewModel.kt
            ├── HabitsScreen.kt
            └── di/
                └── HabitsPresentationModule.kt
```

**Files modified in existing modules**:

```
settings.gradle.kts                             # +3 include() entries
shared/navigation/
  src/commonMain/.../MudawamaAppShell.kt        # add habitsScreen content-slot param
shared/umbrella-ui/
  build.gradle.kts                              # +3 feature module deps
  src/commonMain/.../MudawamaApp.kt             # NEW — real-screen composition root
androidApp/
  src/main/.../MainActivity.kt                  # call MudawamaApp() instead of MudawamaAppShell()
iosApp/
  iosApp/ContentView.swift                      # (or wrapper) call MudawamaApp()
```

---

## Phase 0: Research Findings (resolved)

All unknowns resolved in `research.md`. Summary:

| Item | Decision |
|------|----------|
| UUID generation | `kotlin.uuid.Uuid.random()` (Kotlin 2.0+ stdlib, `@ExperimentalUuidApi`) |
| `DayOfWeek` source | `kotlinx.datetime.DayOfWeek`; add `kotlinx-datetime` dep to `:domain` |
| Navigation wiring | Content-slot lambda on `MudawamaAppShell`; real screens in `umbrella-ui` |
| Date window on rollover | Static window at init; v1 limitation documented; dynamic ticker deferred |
| ISO date sort correctness | Lexicographic = chronological for `yyyy-MM-dd` — DAO query unchanged |
| Log upsert strategy | Domain get-then-branch + `insertLog(REPLACE)` in data layer |
| Rapid-tap debounce | `exclusiveIntent(key = "toggle_$habitId")` in ViewModel is sufficient |

---

## Phase 1: Design Decisions

### 1. Gradle Module Setup

#### `feature/habits/domain/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp.library")
    id("mudawama.kmp.koin")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.habits.domain"
    }
    configureIosFramework("FeatureHabitsDomain")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)       // for DayOfWeek, LocalDate, DatePeriod
            api(projects.shared.core.domain)            // DomainError, Result, EmptyResult
        }
    }
}
```

#### `feature/habits/data/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp.data")
    // mudawama.kmp.data applies: mudawama.kmp.library, mudawama.kmp.koin,
    //   kotlinx.serialization plugin, io.insert-koin.compiler.plugin
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.habits.data"
    }
    configureIosFramework("FeatureHabitsData")

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.habits.domain)   // repository interfaces + models
            implementation(projects.shared.core.database)    // HabitDao, HabitLogDao, entities
            implementation(projects.shared.core.time)        // TimeProvider, toIsoDateString
        }
        // Note: mudawama.kmp.data adds ktor/kermit — unused here but not harmful.
        // Ktor client is only pulled into the androidApp/iosApp binary if actually called.
    }
}
```

#### `feature/habits/presentation/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp.presentation")
    // mudawama.kmp.presentation applies: mudawama.kmp.library, mudawama.kmp.koin,
    //   compose plugin, compose compiler, io.insert-koin.compiler.plugin
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.habits.presentation"
    }
    configureIosFramework("FeatureHabitsPresentation", isStatic = true)

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.habits.domain)      // use cases + models
            implementation(projects.shared.core.presentation)   // MviViewModel (BaseViewModel)
            implementation(projects.shared.designsystem)        // color/typography/icon tokens
        }
        // NO dependency on feature:habits:data (FR-019)
        // NO dependency on shared:core:database (FR-019)
    }
}
```

#### `settings.gradle.kts` additions

```kotlin
include(":feature:habits:domain")
include(":feature:habits:data")
include(":feature:habits:presentation")
```

Gradle path → directory mapping (with `TYPESAFE_PROJECT_ACCESSORS` enabled):
- `:feature:habits:domain` → `feature/habits/domain/`
- `:feature:habits:data` → `feature/habits/data/`
- `:feature:habits:presentation` → `feature/habits/presentation/`
- Accessor: `projects.feature.habits.domain`, `projects.feature.habits.data`, `projects.feature.habits.presentation`

#### `shared/umbrella-ui/build.gradle.kts` additions

```kotlin
sourceSets {
    commonMain.dependencies {
        // ... existing deps ...
        implementation(projects.feature.habits.domain)
        implementation(projects.feature.habits.data)
        implementation(projects.feature.habits.presentation)
    }
}
```

`umbrella-ui` becomes the composition root that knows about all three feature sub-modules.
The Koin DI graph for habits is assembled here (all three `habits*Module()` registered at startup).

---

### 2. Domain Layer

#### `HabitRepository` and `HabitLogRepository`

Interfaces defined in `feature:habits:domain/repository/`. No implementation logic here.

```kotlin
// HabitRepository.kt
interface HabitRepository {
    fun observeAllHabits(): Flow<List<Habit>>
    suspend fun upsertHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    suspend fun getHabitById(habitId: String): Habit?
}

// HabitLogRepository.kt
interface HabitLogRepository {
    fun observeLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLog>>
    suspend fun upsertLog(log: HabitLog)
    suspend fun getLogForHabitOnDate(habitId: String, date: String): HabitLog?
}
```

#### `ObserveHabitsWithTodayStatusUseCase` — the key Flow combiner

This use case is central to the feature. It merges two live Room Flows into a
`Flow<List<HabitWithStatus>>` that the ViewModel collects once at init.

```kotlin
class ObserveHabitsWithTodayStatusUseCase(
    private val habitRepository   : HabitRepository,
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider      : TimeProvider,
) {
    operator fun invoke(): Flow<List<HabitWithStatus>> {
        val today     : LocalDate = timeProvider.logicalDate()
        val endDate   : String    = toIsoDateString(today)
        val startDate : String    = toIsoDateString(today - DatePeriod(days = 6))

        return combine(
            habitRepository.observeAllHabits(),
            habitLogRepository.observeLogsForDateRange(startDate, endDate),
        ) { habits, logs ->
            // Build a two-level map: habitId -> (date -> HabitLog)
            val logsByHabitByDate: Map<String, Map<String, HabitLog>> =
                logs.groupBy { it.habitId }
                    .mapValues { (_, l) -> l.associateBy { it.date } }

            // Fixed ordered list of the last 7 date strings: index 0 = today
            val last7Dates: List<String> = (0..6).map { daysAgo ->
                toIsoDateString(today - DatePeriod(days = daysAgo))
            }

            habits.map { habit ->
                val logMap = logsByHabitByDate[habit.id] ?: emptyMap()
                HabitWithStatus(
                    habit    = habit,
                    todayLog = logMap[endDate],
                    weekLogs = last7Dates.map { date -> logMap[date] },
                )
            }
        }
    }
}
```

**Why `combine` and not `flatMapLatest`**: Both habit definitions and daily logs can change
independently. `combine` emits a new merged list whenever *either* upstream Flow emits,
guaranteeing the UI is always consistent with the latest database state. `flatMapLatest`
would re-subscribe the logs Flow on every habits emission, creating unnecessary overhead.

**Date window capture**: `today`, `startDate`, and `endDate` are captured at `invoke()` call time
(ViewModel init). The Room Flow emits reactively for all changes within the fixed window. This is
correct for the primary use case (same-day session). See `research.md` Decision 4 for the
midnight-rollover edge case and the deferred dynamic-window enhancement.

#### `CreateHabitUseCase`

```kotlin
class CreateHabitUseCase(private val habitRepository: HabitRepository) {
    suspend operator fun invoke(
        name          : String,
        iconKey       : String,
        frequencyDays : Set<DayOfWeek>,
        type          : HabitType,
        goalCount     : Int?,
        category      : String = "custom",
    ): EmptyResult<HabitError> {
        if (name.isBlank())           return Result.Failure(HabitError.EmptyHabitName)
        if (frequencyDays.isEmpty())  return Result.Failure(HabitError.NoFrequencyDaySelected)

        val habit = Habit(
            id            = generateId(),          // kotlin.uuid.Uuid.random().toString()
            name          = name.trim(),
            iconKey       = iconKey,
            type          = type,
            category      = category,
            frequencyDays = frequencyDays,
            isCore        = false,                 // user-created habits are never core
            goalCount     = if (type == HabitType.NUMERIC) goalCount else null,
            createdAt     = /* TimeProvider.nowInstant().toEpochMilliseconds() injected */ 0L,
        )
        habitRepository.upsertHabit(habit)
        return Result.Success(Unit)
    }
}
```

**Note**: `createdAt` requires `TimeProvider`. In the full implementation, inject
`TimeProvider` into `CreateHabitUseCase` (same pattern as `ToggleHabitCompletionUseCase`).

#### `DeleteHabitUseCase`

```kotlin
class DeleteHabitUseCase(
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habitId: String): EmptyResult<HabitError> {
        val habit = habitRepository.getHabitById(habitId)
            ?: return Result.Failure(HabitError.HabitNotFound(habitId))

        if (habit.isCore)
            return Result.Failure(HabitError.CoreHabitCannotBeDeleted)

        habitRepository.deleteHabit(habitId)
        // Cascade delete of HabitLogEntity rows is handled by Room ForeignKey.CASCADE
        return Result.Success(Unit)
    }
}
```

#### `ToggleHabitCompletionUseCase`

```kotlin
class ToggleHabitCompletionUseCase(
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider      : TimeProvider,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(habitId: String): EmptyResult<HabitError> {
        val today   = toIsoDateString(timeProvider.logicalDate())
        val now     = timeProvider.nowInstant().toEpochMilliseconds()
        val existing = habitLogRepository.getLogForHabitOnDate(habitId, today)

        val updated = if (existing == null) {
            HabitLog(
                id             = Uuid.random().toString(),
                habitId        = habitId,
                date           = today,
                status         = LogStatus.COMPLETED,
                completedCount = 0,
                loggedAt       = now,
            )
        } else {
            val newStatus = if (existing.status == LogStatus.COMPLETED)
                LogStatus.PENDING else LogStatus.COMPLETED
            existing.copy(status = newStatus, loggedAt = now)
        }
        habitLogRepository.upsertLog(updated)
        return Result.Success(Unit)
    }
}
```

#### `IncrementHabitCountUseCase`

```kotlin
class IncrementHabitCountUseCase(
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider      : TimeProvider,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(habitId: String): EmptyResult<HabitError> {
        val today   = toIsoDateString(timeProvider.logicalDate())
        val now     = timeProvider.nowInstant().toEpochMilliseconds()
        val existing = habitLogRepository.getLogForHabitOnDate(habitId, today)

        val updated = if (existing == null) {
            HabitLog(
                id             = Uuid.random().toString(),
                habitId        = habitId,
                date           = today,
                status         = LogStatus.PENDING,
                completedCount = 1,
                loggedAt       = now,
            )
        } else {
            existing.copy(completedCount = existing.completedCount + 1, loggedAt = now)
        }
        habitLogRepository.upsertLog(updated)
        return Result.Success(Unit)
    }
}
```

#### `HabitsDomainModule.kt`

```kotlin
fun habitsDomainModule() = module {
    factory { ObserveHabitsWithTodayStatusUseCase(get(), get(), get()) }
    factory { CreateHabitUseCase(get(), get()) }      // HabitRepository + TimeProvider
    factory { UpdateHabitUseCase(get()) }
    factory { DeleteHabitUseCase(get()) }
    factory { ToggleHabitCompletionUseCase(get(), get()) }
    factory { IncrementHabitCountUseCase(get(), get()) }
    factory { ObserveWeeklyHeatmapUseCase(get(), get()) }
}
```

---

### 3. Data Layer

#### `HabitMapper.kt`

```kotlin
// Extension functions — no singleton/object; mappers are pure top-level functions

fun HabitEntity.toDomain(): Habit = Habit(
    id            = id,
    name          = name,
    iconKey       = iconKey,
    type          = HabitType.valueOf(type),
    category      = category,
    frequencyDays = frequencyDays
        .split(",")
        .filter { it.isNotBlank() }
        .map { DayOfWeek(it.trim().toInt()) }
        .toSet(),
    isCore        = isCore,
    goalCount     = goalCount,
    createdAt     = createdAt,
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id            = id,
    name          = name,
    iconKey       = iconKey,
    type          = type.name,
    category      = category,
    frequencyDays = frequencyDays.joinToString(",") { it.isoDayNumber.toString() },
    isCore        = isCore,
    goalCount     = goalCount,
    createdAt     = createdAt,
)
```

`DayOfWeek(isoDayNumber: Int)` constructs a `kotlinx.datetime.DayOfWeek` from its ISO day number
(1 = Monday, 7 = Sunday). This is the `kotlinx.datetime` constructor overload.

#### `HabitLogMapper.kt`

```kotlin
fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    id             = id,
    habitId        = habitId,
    date           = date,
    status         = LogStatus.valueOf(status),
    completedCount = completedCount,
    loggedAt       = loggedAt,
)

fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    id             = id,
    habitId        = habitId,
    date           = date,
    status         = status.name,
    completedCount = completedCount,
    loggedAt       = loggedAt,
)
```

#### `HabitRepositoryImpl.kt`

```kotlin
internal class HabitRepositoryImpl(
    private val dao         : HabitDao,
    private val timeProvider: TimeProvider,   // unused for read ops; present for future audit logging
) : HabitRepository {

    override fun observeAllHabits(): Flow<List<Habit>> =
        dao.getAllHabits().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertHabit(habit: Habit) =
        dao.insertHabit(habit.toEntity())   // insertHabit uses OnConflictStrategy.REPLACE

    override suspend fun deleteHabit(habitId: String) =
        dao.deleteHabit(habitId)

    override suspend fun getHabitById(habitId: String): Habit? =
        dao.getHabitById(habitId)?.toDomain()
}
```

**`insertHabit` as upsert**: `HabitDao.insertHabit` has `@Insert(onConflict = OnConflictStrategy.REPLACE)`.
Since `HabitEntity.id` (UUID) is the `@PrimaryKey`, inserting an entity with an existing `id`
performs an in-place replacement (upsert). This is correct for both create and update operations.

#### `HabitLogRepositoryImpl.kt`

```kotlin
internal class HabitLogRepositoryImpl(
    private val dao: HabitLogDao,
) : HabitLogRepository {

    override fun observeLogsForDateRange(
        startDate: String,
        endDate  : String,
    ): Flow<List<HabitLog>> =
        dao.getLogsForDateRange(startDate, endDate)
           .map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertLog(log: HabitLog) =
        dao.insertLog(log.toEntity())   // OnConflictStrategy.REPLACE on HabitLogEntity.id

    override suspend fun getLogForHabitOnDate(
        habitId: String,
        date   : String,
    ): HabitLog? =
        dao.getLogForHabitOnDate(habitId, date)?.toDomain()
}
```

#### `HabitsDataModule.kt`

```kotlin
fun habitsDataModule() = module {
    single<HabitRepository> {
        HabitRepositoryImpl(
            dao          = get<HabitDao>(),      // provided by coreDatabaseModule
            timeProvider = get(),                // provided by timeModule(policy)
        )
    }
    single<HabitLogRepository> {
        HabitLogRepositoryImpl(
            dao = get<HabitLogDao>(),             // provided by coreDatabaseModule
        )
    }
}
```

**Koin prerequisite bindings** (must be in the Koin graph before `habitsDataModule()` loads):
- `HabitDao` — provided by `coreDatabaseModule` in `shared:core:database`
- `HabitLogDao` — provided by `coreDatabaseModule`
- `TimeProvider` — provided by `timeModule(policy)` in `shared:core:time`

---

### 4. Presentation Layer

#### `HabitsViewModel`

Extends `MviViewModel<HabitsUiState, HabitsUiAction, HabitsUiEvent>` (the project's BaseViewModel).

```kotlin
class HabitsViewModel(
    private val observeHabitsUseCase  : ObserveHabitsWithTodayStatusUseCase,
    private val createHabitUseCase    : CreateHabitUseCase,
    private val updateHabitUseCase    : UpdateHabitUseCase,
    private val deleteHabitUseCase    : DeleteHabitUseCase,
    private val toggleCompletionUseCase : ToggleHabitCompletionUseCase,
    private val incrementCountUseCase : IncrementHabitCountUseCase,
) : MviViewModel<HabitsUiState, HabitsUiAction, HabitsUiEvent>(HabitsUiState()) {

    init {
        // Subscribe to the live habits+logs Flow once at ViewModel creation.
        // Cancels automatically when ViewModel is cleared (viewModelScope lifecycle).
        intent {
            observeHabitsUseCase()
                .collect { habitsWithStatus ->
                    reduce { copy(habits = habitsWithStatus, isLoading = false) }
                }
        }
    }

    override fun onAction(action: HabitsUiAction) {
        when (action) {
            is HabitsUiAction.AddHabitFabClicked ->
                reduce { copy(bottomSheetMode = BottomSheetMode.AddHabit) }

            is HabitsUiAction.HabitLongPressed ->
                reduce { copy(bottomSheetMode = BottomSheetMode.OptionsMenu(action.habit)) }

            is HabitsUiAction.EditHabitSelected ->
                reduce { copy(bottomSheetMode = BottomSheetMode.EditHabit(action.habit)) }

            is HabitsUiAction.DeleteHabitSelected ->
                reduce { copy(bottomSheetMode = BottomSheetMode.DeleteConfirm(action.habitId)) }

            is HabitsUiAction.SaveHabit -> handleSaveHabit(action)

            is HabitsUiAction.DeleteConfirmed -> exclusiveIntent("delete_${action.habitId}") {
                val result = deleteHabitUseCase(action.habitId)
                reduce { copy(bottomSheetMode = BottomSheetMode.Hidden) }
                if (result is Result.Failure) emitEvent(
                    HabitsUiEvent.ShowSnackbar("Could not delete habit: ${result.error}")
                )
            }

            is HabitsUiAction.ToggleCompletion ->
                // exclusiveIntent: cancels any in-flight toggle for the same habitId,
                // preventing duplicate log writes on rapid double-tap (spec edge case).
                exclusiveIntent("toggle_${action.habitId}") {
                    toggleCompletionUseCase(action.habitId)
                }

            is HabitsUiAction.IncrementCount ->
                exclusiveIntent("increment_${action.habitId}") {
                    incrementCountUseCase(action.habitId)
                }

            is HabitsUiAction.DismissBottomSheet ->
                reduce { copy(bottomSheetMode = BottomSheetMode.Hidden) }

            is HabitsUiAction.DismissError ->
                reduce { copy(errorMessage = null) }
        }
    }

    private fun handleSaveHabit(action: HabitsUiAction.SaveHabit) = intent {
        val mode = state.value.bottomSheetMode
        val result: EmptyResult<HabitError> = when (mode) {
            is BottomSheetMode.AddHabit  ->
                createHabitUseCase(
                    name          = action.name,
                    iconKey       = action.iconKey,
                    frequencyDays = action.frequencyDays,
                    type          = action.type,
                    goalCount     = action.goalCount,
                )
            is BottomSheetMode.EditHabit ->
                updateHabitUseCase(
                    mode.habit.copy(
                        name          = action.name,
                        iconKey       = action.iconKey,
                        frequencyDays = action.frequencyDays,
                        type          = action.type,
                        goalCount     = action.goalCount,
                    )
                )
            else -> return@intent   // SaveHabit in unexpected mode; ignore
        }

        when (result) {
            is Result.Success -> reduce { copy(bottomSheetMode = BottomSheetMode.Hidden) }
            is Result.Failure -> when (result.error) {
                HabitError.EmptyHabitName         -> reduce { copy(errorMessage = "Name cannot be empty") }
                HabitError.NoFrequencyDaySelected -> reduce { copy(errorMessage = "Select at least one day") }
                else                              -> emitEvent(HabitsUiEvent.ShowSnackbar("Save failed"))
            }
        }
    }
}
```

**Key design notes**:
- `init { intent { observeHabitsUseCase().collect { ... } } }` — one subscription for the
  lifetime of the ViewModel. The `intent { }` block launches in `viewModelScope`; cancels on
  `onCleared()`. No manual `Job` management needed.
- Validation errors (`EmptyHabitName`, `NoFrequencyDaySelected`) are surfaced as `errorMessage` in
  state (inline form feedback) rather than as `HabitsUiEvent` (which would show a snackbar).
- `exclusiveIntent("toggle_$habitId")` — per `research.md` Decision 7, this provides the debouncing
  guarantee for rapid taps without introducing `debounce` latency.

#### `HabitsScreen.kt` — skeleton

```kotlin
@Composable
fun HabitsScreen(viewModel: HabitsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.eventFlow) { event ->
        when (event) {
            is HabitsUiEvent.ShowSnackbar -> { /* show SnackbarHostState message */ }
        }
    }

    val sheetMode = state.bottomSheetMode

    Scaffold(
        floatingActionButton = {
            if (sheetMode == BottomSheetMode.Hidden) {
                FloatingActionButton(onClick = {
                    viewModel.onAction(HabitsUiAction.AddHabitFabClicked)
                }) { Icon(Icons.Default.Add, contentDescription = "Add Habit") }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.padding(padding))
            state.habits.isEmpty() -> HabitsEmptyState(Modifier.padding(padding))
            else -> LazyColumn(contentPadding = padding) {
                items(state.habits, key = { it.habit.id }) { habitWithStatus ->
                    HabitListItem(
                        habitWithStatus = habitWithStatus,
                        onToggle    = { viewModel.onAction(HabitsUiAction.ToggleCompletion(habitWithStatus.habit.id)) },
                        onIncrement = { viewModel.onAction(HabitsUiAction.IncrementCount(habitWithStatus.habit.id)) },
                        onLongPress = { viewModel.onAction(HabitsUiAction.HabitLongPressed(habitWithStatus.habit)) },
                    )
                }
            }
        }
    }

    // Bottom sheet / dialog rendering driven purely by state
    when (val mode = sheetMode) {
        is BottomSheetMode.AddHabit ->
            HabitBottomSheet(mode = mode,
                onSave    = { viewModel.onAction(it) },
                onDismiss = { viewModel.onAction(HabitsUiAction.DismissBottomSheet) })
        is BottomSheetMode.EditHabit ->
            HabitBottomSheet(mode = mode,
                onSave    = { viewModel.onAction(it) },
                onDismiss = { viewModel.onAction(HabitsUiAction.DismissBottomSheet) })
        is BottomSheetMode.OptionsMenu ->
            HabitOptionsSheet(habit = mode.habit,
                onEdit    = { viewModel.onAction(HabitsUiAction.EditHabitSelected(mode.habit)) },
                onDelete  = { viewModel.onAction(HabitsUiAction.DeleteHabitSelected(mode.habit.id)) },
                onDismiss = { viewModel.onAction(HabitsUiAction.DismissBottomSheet) })
        is BottomSheetMode.DeleteConfirm ->
            DeleteConfirmDialog(habitId = mode.habitId,
                onConfirm = { viewModel.onAction(HabitsUiAction.DeleteConfirmed(mode.habitId)) },
                onDismiss = { viewModel.onAction(HabitsUiAction.DismissBottomSheet) })
        BottomSheetMode.Hidden -> {}
    }
}
```

**Stateless bottom sheets**: All bottom-sheet and dialog composables receive their data as parameters
and emit actions upward. They hold no local state about the habit list. The ViewModel is the single
source of truth.

#### `HabitHeatmapRow.kt` — cell logic

```kotlin
@Composable
fun HabitHeatmapRow(
    weekLogs      : List<HabitLog?>,    // 7 entries; index 0 = today
    frequencyDays : Set<DayOfWeek>,
    modifier      : Modifier = Modifier,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weekLogs.forEachIndexed { index, log ->
            val date      = today.minus(DatePeriod(days = index))
            val dayOfWeek = date.dayOfWeek
            val isScheduled = dayOfWeek in frequencyDays

            val cellColor = when {
                !isScheduled                               -> MudawamaTheme.colors.outline.copy(alpha = 0.3f)
                log?.status == LogStatus.COMPLETED         -> MudawamaTheme.colors.primary
                else                                       -> MudawamaTheme.colors.surfaceVariant
            }
            Box(
                Modifier
                    .size(20.dp)
                    .background(cellColor, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}
```

**Note on `Clock.System` in the composable**: `HabitHeatmapRow` needs the current `DayOfWeek` to
determine cell scheduling. Using `Clock.System` directly in a composable is acceptable here since
it is a *display-layer* computation (not a business logic rule). The spec constraint SC-002 applies
to the data and domain layers. `remember { }` ensures the calculation runs once per recomposition
entry, not on every frame.

#### `HabitsPresentationModule.kt`

```kotlin
fun habitsPresentationModule() = module {
    viewModel {
        HabitsViewModel(
            observeHabitsUseCase    = get(),
            createHabitUseCase      = get(),
            updateHabitUseCase      = get(),
            deleteHabitUseCase      = get(),
            toggleCompletionUseCase = get(),
            incrementCountUseCase   = get(),
        )
    }
}
```

---

### 5. Navigation Wiring (FR-020)

#### Update `MudawamaAppShell.kt` in `shared:navigation`

Add a content-slot parameter for the Habits destination, defaulting to the existing placeholder:

```kotlin
@Composable
fun MudawamaAppShell(
    habitsScreen: @Composable () -> Unit = { HabitsPlaceholderScreen() },
) {
    // ... existing rememberNavBackStack, MudawamaTheme, Scaffold setup ...

    NavDisplay(backStack) { route ->
        when (route) {
            is HomeRoute    -> HomePlaceholderScreen()
            is PrayerRoute  -> PrayerPlaceholderScreen()
            is AthkarRoute  -> AthkarPlaceholderScreen()
            is HabitsRoute  -> habitsScreen()          // ← calls the injected slot
        }
    }
}
```

This change is backward-compatible: callers that do not pass `habitsScreen` still get the placeholder.
No new Gradle dependency is added to `shared:navigation`.

#### Create `MudawamaApp.kt` in `shared:umbrella-ui`

```kotlin
// shared/umbrella-ui/src/commonMain/kotlin/.../MudawamaApp.kt
@Composable
fun MudawamaApp() {
    MudawamaAppShell(
        habitsScreen = { HabitsScreen() }
    )
}
```

#### Update platform hosts

```kotlin
// androidApp/src/main/.../MainActivity.kt  (setContent block)
setContent {
    MudawamaApp()   // was: MudawamaAppShell()
}

// iosApp/iosApp/ContentView.swift  (or iOSApp.swift)
// ComposeUIViewController { MudawamaApp_() }   // generated KMP accessor
```

---

### 6. Unit Testing Strategy

All 7 use cases in `feature:habits:domain` require a success path and a failure/edge-case path (SC-007).

| Use Case | Success test | Failure/edge test |
|----------|-------------|------------------|
| `ObserveHabitsWithTodayStatusUseCase` | Emits combined list with today's log | `null` log for habit without today's entry |
| `CreateHabitUseCase` | Habit persisted with correct fields | Blank name → `EmptyHabitName`; empty days → `NoFrequencyDaySelected` |
| `UpdateHabitUseCase` | Updated habit persisted | Blank name → `EmptyHabitName` |
| `DeleteHabitUseCase` | Custom habit deleted | Core habit → `CoreHabitCannotBeDeleted` (SC-004) |
| `ToggleHabitCompletionUseCase` | New log created as COMPLETED | Existing COMPLETED log reverts to PENDING |
| `IncrementHabitCountUseCase` | New log created with count=1 | Existing log count incremented by 1 |
| `ObserveWeeklyHeatmapUseCase` | Emits 7 entries for 7-day range | Habit with no logs → 7 `null` entries |

Test infrastructure: `FakeTimeProvider` (from `shared:core:time` commonMain) + in-memory stubs
implementing `HabitRepository` and `HabitLogRepository` (hand-written fakes in `commonTest`).

**Integration test** (`feature:habits:data:connectedDebugAndroidTest`):
- Cascade delete: insert habit + 3 logs; delete habit; assert both `habits` and `habit_logs` tables
  are empty for that habit (SC-008).
- Mapper round-trip: entity → domain → entity; assert all fields preserved, including
  `frequencyDays` serialisation (comma-separated ordinal string).

---

## Complexity Tracking

> No constitution violations — section intentionally empty.

---

## Known Limitations and Deferred Work

| Item | Description | Deferred to |
|------|-------------|-------------|
| Midnight rollover refresh | The 7-day window is fixed at ViewModel init; new-day logs appear only after ViewModel re-creation | Follow-up spec introducing `TimeProvider.observeLogicalDateChanges()` |
| Numeric habit goal editing | Changing `goalCount` after creation is supported by `UpdateHabitUseCase` but the UI flow is not tested separately | `tasks.md` manual test checklist |
| Ktor deps in `:data` module | `mudawama.kmp.data` plugin adds Ktor/Kermit transitively; unused for offline-first habits | Considered acceptable; no binary size impact unless called |
| `HabitsPlaceholderScreen` cleanup | `Placeholders.kt` in `shared:navigation` retains `HabitsPlaceholderScreen`; used only as default arg | Separate clean-up task in `tasks.md` |
