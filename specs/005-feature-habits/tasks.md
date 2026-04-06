# Tasks: feature:habits — Custom Habit Management & Daily Tracking

**Created**: 2026-04-05
**Feature**: `feature:habits`
**Branch**: `005-feature-habits`
**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Data Model**: [data-model.md](./data-model.md) | **Contracts**: [contracts/domain-api.md](./contracts/domain-api.md), [contracts/presentation-composables.md](./contracts/presentation-composables.md)

How to read: Tasks are ordered by dependency — earlier tasks must be complete before later ones begin.
All source paths are relative to the repository root `/Users/platinum/AndroidStudioProjects/Mudawama`.
`[P]` = safe to run in parallel with other `[P]` tasks in the same phase once that phase's sequential prerequisites are met.
`[US#]` = the user story this task primarily delivers (mapped to spec.md §User Stories).

---

## Phase 1 — Domain Module (`feature:habits:domain`)

> **Goal**: Stand up the pure-Kotlin domain module with Gradle scaffold, all domain models, repository interfaces, 7 use cases, and the Koin DI module. No implementation logic — only contracts and business rules. `:data` and `:presentation` must not be started until T002 is complete and Gradle sync succeeds.
>
> **Prerequisite**: Specs `002-add-core-database`, `003-add-core-time`, and `004-shared-navigation-shell` are all fully delivered. `shared:core:domain` exposes `DomainError`, `Result`, and `EmptyResult` in `commonMain`.
>
> **Independent test**: `./gradlew :feature:habits:domain:compileCommonMainKotlinMetadata` exits with zero errors; all 7 use case classes compile against the `HabitRepository`, `HabitLogRepository`, and `TimeProvider` interfaces.

### Gradle Wiring

- [X] T001 Register all three feature sub-modules in `settings.gradle.kts` by adding the following three `include()` calls (insert after the existing `:shared:navigation` entry): `include(":feature:habits:domain")`, `include(":feature:habits:data")`, `include(":feature:habits:presentation")`; also create the three empty directories `feature/habits/domain/`, `feature/habits/data/`, `feature/habits/presentation/` at the repository root so Gradle can resolve the module paths; run `./gradlew projects` and confirm all three appear without "Project not found" errors in `settings.gradle.kts`

- [X] T002 Create `feature/habits/domain/build.gradle.kts` with the following content: apply plugins `id("mudawama.kmp.library")` and `id("mudawama.kmp.koin")`; configure `android { namespace = "io.github.helmy2.mudawama.habits.domain" }`; call `configureIosFramework("FeatureHabitsDomain")`; declare `sourceSets { commonMain.dependencies { implementation(libs.kotlinx.coroutines.core); implementation(libs.kotlinx.datetime); api(projects.shared.core.domain) } }` — `kotlinx.datetime` is required for `DayOfWeek` and `LocalDate` (Decision 2 in research.md); `shared.core.domain` is `api()` so its `Result`/`EmptyResult`/`DomainError` types are visible to `:presentation` transitively; run `./gradlew :feature:habits:domain:help` to confirm the module resolves in `feature/habits/domain/build.gradle.kts`

### Domain Models, Errors & Repository Interfaces

- [X] T003 [P] Create the two status enum files — `HabitType.kt`: `enum class HabitType { BOOLEAN, NUMERIC }` and `LogStatus.kt`: `enum class LogStatus { PENDING, COMPLETED }` — both in package `io.github.helmy2.mudawama.habits.domain.model` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/model/HabitType.kt` and `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/model/LogStatus.kt`

- [X] T004 [P] Create `Habit.kt` as `data class Habit(val id: String, val name: String, val iconKey: String, val type: HabitType, val category: String, val frequencyDays: Set<DayOfWeek>, val isCore: Boolean, val goalCount: Int?, val createdAt: Long)` in package `io.github.helmy2.mudawama.habits.domain.model`; import `kotlinx.datetime.DayOfWeek`; add KDoc noting three domain invariants: `name.isNotBlank()` · `frequencyDays.isNotEmpty()` · `type == BOOLEAN implies goalCount == null` (these are enforced by use cases, not the data class constructor) in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/model/Habit.kt`

- [X] T005 [P] Create `HabitLog.kt` as `data class HabitLog(val id: String, val habitId: String, val date: String, val status: LogStatus, val completedCount: Int, val loggedAt: Long)` in package `io.github.helmy2.mudawama.habits.domain.model`; add KDoc: "`date` is ISO-8601 `yyyy-MM-dd` derived exclusively from `TimeProvider.logicalDate()` — direct `Clock.System` calls are forbidden (FR-010, SC-002)"; note that `(habitId, date)` is the logical composite key even though `id` (UUID) is the physical primary key in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/model/HabitLog.kt`

- [X] T006 Create `HabitWithStatus.kt` as `data class HabitWithStatus(val habit: Habit, val todayLog: HabitLog?, val weekLogs: List<HabitLog?>)` in package `io.github.helmy2.mudawama.habits.domain.model`; add KDoc: "`weekLogs` contains exactly 7 entries ordered `[today, today-1, …, today-6]`; a `null` entry means no log record exists for that day; this is a read-only projection emitted by `ObserveHabitsWithTodayStatusUseCase` and is never persisted"; also document the four derived display properties computed in composables: `isCompletedToday = todayLog?.status == LogStatus.COMPLETED`, `isDueToday = today's DayOfWeek in habit.frequencyDays`, `numericProgress = todayLog?.completedCount ?: 0`, `isNumericGoalReached = habit.goalCount != null && numericProgress >= habit.goalCount` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/model/HabitWithStatus.kt`

- [X] T007 [P] Create `HabitError.kt` as `sealed interface HabitError : DomainError` with four cases: `data object CoreHabitCannotBeDeleted`, `data object EmptyHabitName`, `data object NoFrequencyDaySelected`, `data class HabitNotFound(val habitId: String)`; import `io.github.helmy2.mudawama.core.domain.DomainError` in package `io.github.helmy2.mudawama.habits.domain.error` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/error/HabitError.kt`

- [X] T008 [P] Create `HabitRepository.kt` as `interface HabitRepository` with four method signatures: `fun observeAllHabits(): Flow<List<Habit>>`, `suspend fun upsertHabit(habit: Habit)`, `suspend fun deleteHabit(habitId: String)`, `suspend fun getHabitById(habitId: String): Habit?`; add KDoc on `upsertHabit`: "Acts as both create and update — the data layer implementation uses `OnConflictStrategy.REPLACE` on the UUID primary key"; import `kotlinx.coroutines.flow.Flow` in package `io.github.helmy2.mudawama.habits.domain.repository` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/repository/HabitRepository.kt`

- [X] T009 [P] Create `HabitLogRepository.kt` as `interface HabitLogRepository` with three method signatures: `fun observeLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLog>>`, `suspend fun upsertLog(log: HabitLog)`, `suspend fun getLogForHabitOnDate(habitId: String, date: String): HabitLog?`; add KDoc on `observeLogsForDateRange`: "Both `startDate` and `endDate` must be ISO-8601 `yyyy-MM-dd` strings produced by `toIsoDateString(TimeProvider.logicalDate())`; lexicographic ordering equals chronological ordering for this format (Decision 5 in research.md)"; import `kotlinx.coroutines.flow.Flow` in package `io.github.helmy2.mudawama.habits.domain.repository` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/repository/HabitLogRepository.kt`

- [X] T010 [P] Create `IdGenerator.kt` — `@OptIn(ExperimentalUuidApi::class) internal fun generateId(): String = Uuid.random().toString()` — prepend `@file:OptIn(ExperimentalUuidApi::class)` at the top of the file; add KDoc: "Generates a random UUID v4 string via `kotlin.uuid.Uuid.random()` (Kotlin 2.0+ stdlib — no external UUID dependency needed, Decision 1 in research.md). Marked `internal` so callers outside `:domain` cannot generate IDs directly. Used by `CreateHabitUseCase`, `ToggleHabitCompletionUseCase`, and `IncrementHabitCountUseCase`."; imports: `kotlin.uuid.ExperimentalUuidApi`, `kotlin.uuid.Uuid` in package `io.github.helmy2.mudawama.habits.domain.util` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/util/IdGenerator.kt`

### Use Cases

- [X] T011 [P] [US1] Create `ObserveHabitsWithTodayStatusUseCase.kt` — `class ObserveHabitsWithTodayStatusUseCase(private val habitRepository: HabitRepository, private val habitLogRepository: HabitLogRepository, private val timeProvider: TimeProvider)` with `operator fun invoke(): Flow<List<HabitWithStatus>>`; at `invoke()` call time capture `val today: LocalDate = timeProvider.logicalDate()`, `val endDate = toIsoDateString(today)`, `val startDate = toIsoDateString(today - DatePeriod(days = 6))`; return `combine(habitRepository.observeAllHabits(), habitLogRepository.observeLogsForDateRange(startDate, endDate)) { habits, logs -> … }` — inside the lambda build `val logsByHabitByDate: Map<String, Map<String, HabitLog>> = logs.groupBy { it.habitId }.mapValues { (_, l) -> l.associateBy { it.date } }` and `val last7Dates: List<String> = (0..6).map { toIsoDateString(today - DatePeriod(days = it)) }`; map each habit to `HabitWithStatus(habit, todayLog = logsByHabitByDate[habit.id]?.get(endDate), weekLogs = last7Dates.map { logsByHabitByDate[habit.id]?.get(it) })`; add KDoc explaining why `combine` is used over `flatMapLatest` (Decision 4 in research.md: date window is captured once at ViewModel init — midnight-rollover is a documented v1 limitation); imports: `kotlinx.datetime.DatePeriod`, `kotlinx.datetime.LocalDate`, `kotlinx.coroutines.flow.combine`, `io.github.helmy2.mudawama.core.time.TimeProvider`, `io.github.helmy2.mudawama.core.time.toIsoDateString` in package `io.github.helmy2.mudawama.habits.domain.usecase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/ObserveHabitsWithTodayStatusUseCase.kt`

- [X] T012 [P] [US2] Create `CreateHabitUseCase.kt` — `class CreateHabitUseCase(private val habitRepository: HabitRepository, private val timeProvider: TimeProvider)` with `@OptIn(ExperimentalUuidApi::class) suspend operator fun invoke(name: String, iconKey: String, frequencyDays: Set<DayOfWeek>, type: HabitType, goalCount: Int?, category: String = "custom"): EmptyResult<HabitError>`; validate `name.isBlank()` → `Result.Failure(HabitError.EmptyHabitName)` and `frequencyDays.isEmpty()` → `Result.Failure(HabitError.NoFrequencyDaySelected)` before constructing the habit; construct `Habit(id = generateId(), name = name.trim(), iconKey, type, category, frequencyDays, isCore = false, goalCount = if (type == HabitType.NUMERIC) goalCount else null, createdAt = timeProvider.nowInstant().toEpochMilliseconds())`; call `habitRepository.upsertHabit(habit)`; return `Result.Success(Unit)`; import `kotlinx.datetime.DayOfWeek` in package `io.github.helmy2.mudawama.habits.domain.usecase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/CreateHabitUseCase.kt`

- [X] T013 [P] [US4] Create `UpdateHabitUseCase.kt` — `class UpdateHabitUseCase(private val habitRepository: HabitRepository)` with `suspend operator fun invoke(habit: Habit): EmptyResult<HabitError>`; apply the same two validations as `CreateHabitUseCase`: `habit.name.isBlank()` → `Result.Failure(HabitError.EmptyHabitName)`, `habit.frequencyDays.isEmpty()` → `Result.Failure(HabitError.NoFrequencyDaySelected)`; on success call `habitRepository.upsertHabit(habit)` and return `Result.Success(Unit)`; add KDoc: "Caller is responsible for constructing the updated `Habit` instance via `existing.copy(…)` before passing it here" in package `io.github.helmy2.mudawama.habits.domain.usecase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/UpdateHabitUseCase.kt`

- [X] T014 [P] [US5] Create `DeleteHabitUseCase.kt` — `class DeleteHabitUseCase(private val habitRepository: HabitRepository)` with `suspend operator fun invoke(habitId: String): EmptyResult<HabitError>`; load the habit via `habitRepository.getHabitById(habitId)` — return `Result.Failure(HabitError.HabitNotFound(habitId))` if `null`; return `Result.Failure(HabitError.CoreHabitCannotBeDeleted)` if `habit.isCore == true` (SC-004); on success call `habitRepository.deleteHabit(habitId)` and return `Result.Success(Unit)`; add KDoc: "Room `ForeignKey.CASCADE` on `HabitLogEntity.habitId` ensures all associated log rows are deleted atomically by the database — no explicit log deletion is required in this use case" in package `io.github.helmy2.mudawama.habits.domain.usecase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/DeleteHabitUseCase.kt`

- [X] T015 [P] [US3] Create `ToggleHabitCompletionUseCase.kt` — `class ToggleHabitCompletionUseCase(private val habitLogRepository: HabitLogRepository, private val timeProvider: TimeProvider)` with `@OptIn(ExperimentalUuidApi::class) suspend operator fun invoke(habitId: String): EmptyResult<HabitError>`; compute `val today = toIsoDateString(timeProvider.logicalDate())` and `val now = timeProvider.nowInstant().toEpochMilliseconds()`; call `habitLogRepository.getLogForHabitOnDate(habitId, today)`; if `null` create `HabitLog(id = generateId(), habitId, date = today, status = LogStatus.COMPLETED, completedCount = 0, loggedAt = now)`; if exists create a copy with toggled `status` (`COMPLETED` → `PENDING`, `PENDING` → `COMPLETED`) and updated `loggedAt = now`; call `habitLogRepository.upsertLog(updated)`; return `Result.Success(Unit)`; add KDoc citing Decision 6 (research.md): "get-then-branch keeps the toggle logic in the domain layer — the repository's `upsertLog` is a pure write" in package `io.github.helmy2.mudawama.habits.domain.usecase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/ToggleHabitCompletionUseCase.kt`

- [X] T016 [P] [US3] Create `IncrementHabitCountUseCase.kt` — `class IncrementHabitCountUseCase(private val habitLogRepository: HabitLogRepository, private val timeProvider: TimeProvider)` with `@OptIn(ExperimentalUuidApi::class) suspend operator fun invoke(habitId: String): EmptyResult<HabitError>`; same `today`/`now` setup as T015; if no existing log for today create `HabitLog(id = generateId(), habitId, date = today, status = LogStatus.PENDING, completedCount = 1, loggedAt = now)`; if exists create a copy with `completedCount = existing.completedCount + 1` and `loggedAt = now`; call `habitLogRepository.upsertLog(updated)`; return `Result.Success(Unit)` in package `io.github.helmy2.mudawama.habits.domain.usecase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/IncrementHabitCountUseCase.kt`

- [X] T017 [P] [US6] Create `ObserveWeeklyHeatmapUseCase.kt` — `class ObserveWeeklyHeatmapUseCase(private val habitLogRepository: HabitLogRepository, private val timeProvider: TimeProvider)` with `operator fun invoke(habitId: String): Flow<List<HabitLog?>>`; at `invoke()` time capture `val today = timeProvider.logicalDate()`, `val endDate = toIsoDateString(today)`, `val startDate = toIsoDateString(today - DatePeriod(days = 6))`; return `habitLogRepository.observeLogsForDateRange(startDate, endDate).map { logs -> val logsByDate = logs.filter { it.habitId == habitId }.associateBy { it.date }; (0..6).map { daysAgo -> logsByDate[toIsoDateString(today - DatePeriod(days = daysAgo))] } }` producing exactly 7 nullable entries where index 0 = today; add KDoc: "Emits 7 entries; `null` at an index means no log record exists for that day. Used by the presentation layer for per-habit heatmap display (US6, SC-003)" in package `io.github.helmy2.mudawama.habits.domain.usecase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/ObserveWeeklyHeatmapUseCase.kt`

### Koin DI

- [X] T018 Create `HabitsDomainModule.kt` — top-level function `fun habitsDomainModule() = module { factory { ObserveHabitsWithTodayStatusUseCase(get(), get(), get()) }; factory { CreateHabitUseCase(get(), get()) }; factory { UpdateHabitUseCase(get()) }; factory { DeleteHabitUseCase(get()) }; factory { ToggleHabitCompletionUseCase(get(), get()) }; factory { IncrementHabitCountUseCase(get(), get()) }; factory { ObserveWeeklyHeatmapUseCase(get(), get()) } }`; add KDoc listing the three required upstream bindings: "`HabitRepository` (provided by `habitsDataModule()`), `HabitLogRepository` (provided by `habitsDataModule()`), `TimeProvider` (provided by `timeModule(policy)`) — these must be registered in the Koin graph before `habitsDomainModule()` is loaded" in package `io.github.helmy2.mudawama.habits.domain.di` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/di/HabitsDomainModule.kt`

---

## Phase 2 — Data Module (`feature:habits:data`)

> **Goal**: Implement Room-backed repository implementations and bidirectional entity ↔ domain mappers. Wire them into a Koin module that binds the `:domain` repository interfaces. `:presentation` must NOT depend on this module directly (FR-019).
>
> **Prerequisite**: Phase 1 complete — `feature:habits:domain` compiles and Gradle sync succeeds with the `:feature:habits:domain` project accessor available.
>
> **Independent test**: `./gradlew :feature:habits:data:compileCommonMainKotlinMetadata` exits with zero errors; `HabitRepositoryImpl` and `HabitLogRepositoryImpl` satisfy their respective interfaces at compile time.

- [X] T019 Create `feature/habits/data/build.gradle.kts` applying plugin `id("mudawama.kmp.data")`; set `android { namespace = "io.github.helmy2.mudawama.habits.data" }`; call `configureIosFramework("FeatureHabitsData")`; declare `sourceSets { commonMain.dependencies { implementation(projects.feature.habits.domain); implementation(projects.shared.core.database); implementation(projects.shared.core.time) } }`; add a comment block: `// Note: mudawama.kmp.data adds Ktor/Kermit transitively; unused here (offline-first). See plan.md §1.` in `feature/habits/data/build.gradle.kts`

- [X] T020 [P] Create `HabitMapper.kt` with two top-level extension functions in package `io.github.helmy2.mudawama.habits.data.mapper` — `fun HabitEntity.toDomain(): Habit`: convert `type: String` via `HabitType.valueOf(type)`, convert `frequencyDays: String` via `split(",").filter { it.isNotBlank() }.map { DayOfWeek(it.trim().toInt()) }.toSet()` (ISO day number 1 = Monday, 7 = Sunday per Decision 2 in research.md), all other fields identity-mapped; `fun Habit.toEntity(): HabitEntity`: serialise `type` as `type.name`, serialise `frequencyDays` as `frequencyDays.joinToString(",") { it.isoDayNumber.toString() }` (FR-009: comma-separated ordinal string), all other fields identity-mapped; imports: `HabitEntity` from `io.github.helmy2.mudawama.core.database`, `DayOfWeek` from `kotlinx.datetime`, domain model imports in `feature/habits/data/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/data/mapper/HabitMapper.kt`

- [X] T021 [P] Create `HabitLogMapper.kt` with two top-level extension functions in package `io.github.helmy2.mudawama.habits.data.mapper` — `fun HabitLogEntity.toDomain(): HabitLog`: convert `status: String` via `LogStatus.valueOf(status)`, all other fields identity-mapped; `fun HabitLog.toEntity(): HabitLogEntity`: serialise `status` as `status.name`, all other fields identity-mapped; imports: `HabitLogEntity` from `io.github.helmy2.mudawama.core.database`, domain model imports in `feature/habits/data/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/data/mapper/HabitLogMapper.kt`

- [X] T022 Create `HabitRepositoryImpl.kt` — `internal class HabitRepositoryImpl(private val dao: HabitDao, private val timeProvider: TimeProvider) : HabitRepository`; implement `observeAllHabits()` as `dao.getAllHabits().map { it.map { entity -> entity.toDomain() } }`; implement `upsertHabit(habit)` as `dao.insertHabit(habit.toEntity())` — add comment: `// insertHabit uses OnConflictStrategy.REPLACE; inserting an entity with an existing UUID id performs an in-place replacement (upsert)`; implement `deleteHabit(habitId)` as `dao.deleteHabit(habitId)`; implement `getHabitById(habitId)` as `dao.getHabitById(habitId)?.toDomain()`; imports: `HabitDao` from `shared.core.database`, `TimeProvider` from `shared.core.time`, mapper extensions from the local `mapper` package in package `io.github.helmy2.mudawama.habits.data.repository` in `feature/habits/data/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/data/repository/HabitRepositoryImpl.kt`

- [X] T023 Create `HabitLogRepositoryImpl.kt` — `internal class HabitLogRepositoryImpl(private val dao: HabitLogDao) : HabitLogRepository`; implement `observeLogsForDateRange(startDate, endDate)` as `dao.getLogsForDateRange(startDate, endDate).map { it.map { entity -> entity.toDomain() } }`; implement `upsertLog(log)` as `dao.insertLog(log.toEntity())` — add comment: `// insertLog uses OnConflictStrategy.REPLACE on HabitLogEntity.id — same UUID reuses the upsert path`; implement `getLogForHabitOnDate(habitId, date)` as `dao.getLogForHabitOnDate(habitId, date)?.toDomain()`; imports: `HabitLogDao` from `shared.core.database` in package `io.github.helmy2.mudawama.habits.data.repository` in `feature/habits/data/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/data/repository/HabitLogRepositoryImpl.kt`

- [X] T024 Create `HabitsDataModule.kt` — top-level function `fun habitsDataModule() = module { single<HabitRepository> { HabitRepositoryImpl(dao = get<HabitDao>(), timeProvider = get()) }; single<HabitLogRepository> { HabitLogRepositoryImpl(dao = get<HabitLogDao>()) } }`; add KDoc listing Koin prerequisite bindings: "`HabitDao` and `HabitLogDao` provided by `coreDatabaseModule()` (or `androidCoreDatabaseModule` / `iosCoreDatabaseModule`); `TimeProvider` provided by `timeModule(policy)` — these must be registered before this module loads" in package `io.github.helmy2.mudawama.habits.data.di` in `feature/habits/data/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/data/di/HabitsDataModule.kt`

---

## Phase 3 — Presentation Module (`feature:habits:presentation`)

> **Goal**: Implement the Compose Multiplatform screen, ViewModel, all sub-components, the Koin presentation module, and wire `HabitsScreen` into the app shell via `shared:umbrella-ui`. All code resides in `commonMain` (FR-018). No dependency on `:data` or `shared:core:database` (FR-019).
>
> **Prerequisite**: Phase 1 complete (`feature:habits:domain` compiles). Phase 2 MAY be developed in parallel — `:presentation` does not depend on `:data` at compile time; both modules are wired together at runtime via Koin.
>
> **Independent test**: `./gradlew :feature:habits:presentation:assembleDebug` succeeds with zero errors; all `@Preview` composables render in the Android Studio Preview panel without errors (SC-005).

### Gradle Wiring

- [X] T025 Create `feature/habits/presentation/build.gradle.kts` applying plugin `id("mudawama.kmp.presentation")`; set `android { namespace = "io.github.helmy2.mudawama.habits.presentation" }`; call `configureIosFramework("FeatureHabitsPresentation", isStatic = true)`; declare `sourceSets { commonMain.dependencies { implementation(projects.feature.habits.domain); implementation(projects.shared.core.presentation); implementation(projects.shared.designsystem) } }`; add a comment block: `// MUST NOT depend on feature:habits:data or shared:core:database (FR-019)` in `feature/habits/presentation/build.gradle.kts`

### Presentation Models

- [X] T026 [P] Create `HabitsUiState.kt` — `data class HabitsUiState(val habits: List<HabitWithStatus> = emptyList(), val isLoading: Boolean = true, val bottomSheetMode: BottomSheetMode = BottomSheetMode.Hidden, val errorMessage: String? = null)` in package `io.github.helmy2.mudawama.habits.presentation.model` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/model/HabitsUiState.kt` — and create `BottomSheetMode.kt` as `sealed interface BottomSheetMode` with five cases: `data object Hidden`, `data object AddHabit`, `data class OptionsMenu(val habit: Habit)`, `data class EditHabit(val habit: Habit)`, `data class DeleteConfirm(val habitId: String)` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/model/BottomSheetMode.kt`; add KDoc on `BottomSheetMode` documenting the state transition table from data-model.md §BottomSheetMode

- [X] T027 [P] Create `HabitsUiAction.kt` as `sealed interface HabitsUiAction` with ten subtypes: `data object AddHabitFabClicked`, `data class HabitLongPressed(val habit: Habit)`, `data class EditHabitSelected(val habit: Habit)`, `data class DeleteHabitSelected(val habitId: String)`, `data class DeleteConfirmed(val habitId: String)`, `data class SaveHabit(val name: String, val iconKey: String, val frequencyDays: Set<DayOfWeek>, val type: HabitType, val goalCount: Int?)`, `data class ToggleCompletion(val habitId: String)`, `data class IncrementCount(val habitId: String)`, `data object DismissBottomSheet`, `data object DismissError`; and create `HabitsUiEvent.kt` as `sealed interface HabitsUiEvent` with one subtype `data class ShowSnackbar(val message: String)`; both in package `io.github.helmy2.mudawama.habits.presentation.model` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/model/HabitsUiAction.kt` and `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/model/HabitsUiEvent.kt`

### ViewModel

- [X] T028 [US1] Create `HabitsViewModel.kt` — `class HabitsViewModel(private val observeHabitsUseCase: ObserveHabitsWithTodayStatusUseCase, private val createHabitUseCase: CreateHabitUseCase, private val updateHabitUseCase: UpdateHabitUseCase, private val deleteHabitUseCase: DeleteHabitUseCase, private val toggleCompletionUseCase: ToggleHabitCompletionUseCase, private val incrementCountUseCase: IncrementHabitCountUseCase) : MviViewModel<HabitsUiState, HabitsUiAction, HabitsUiEvent>(HabitsUiState())`; in `init { intent { observeHabitsUseCase().collect { habitsWithStatus -> reduce { copy(habits = habitsWithStatus, isLoading = false) } } } }` — this single subscription lives for the ViewModel's lifetime (cancels on `onCleared()` via `viewModelScope`); implement `override fun onAction(action: HabitsUiAction)` dispatching all ten action subtypes — `ToggleCompletion` and `IncrementCount` use `exclusiveIntent("toggle_${action.habitId}")` and `exclusiveIntent("increment_${action.habitId}")` respectively (Decision 7 in research.md: cancel in-flight job for same habitId on rapid re-tap); `SaveHabit` delegates to a private `handleSaveHabit(action)` function that checks `state.value.bottomSheetMode` — `AddHabit` → `createHabitUseCase(…)`, `EditHabit(habit)` → `updateHabitUseCase(habit.copy(…))`; on `EmptyHabitName` or `NoFrequencyDaySelected` errors set `errorMessage` in state (inline validation, not snackbar); on other failures emit `HabitsUiEvent.ShowSnackbar(…)`; see plan.md §4 for the complete `onAction` and `handleSaveHabit` implementations in package `io.github.helmy2.mudawama.habits.presentation` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/HabitsViewModel.kt`

### UI Components

- [X] T029 [P] [US6] Create `HabitHeatmapRow.kt` — `@Composable fun HabitHeatmapRow(weekLogs: List<HabitLog?>, frequencyDays: Set<DayOfWeek>, modifier: Modifier = Modifier)`; use `val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }` for display-layer date reference (documented as acceptable in plan.md §4 — this is view computation, not business logic); render a `Row` with `Arrangement.spacedBy(4.dp)` containing 7 `Box` cells each `Modifier.size(20.dp).background(cellColor, RoundedCornerShape(4.dp))`; cell color per cell index `i`: derive `val date = today.minus(DatePeriod(days = i))`, check `date.dayOfWeek in frequencyDays` — if not scheduled → `MudawamaTheme.colors.outline.copy(alpha = 0.3f)` (ghost); else if `weekLogs[i]?.status == LogStatus.COMPLETED` → `MudawamaTheme.colors.primary` (filled); else → `MudawamaTheme.colors.surfaceVariant` (muted); add `@Preview @Composable fun HabitHeatmapRowPreview()` seeded with sample data in package `io.github.helmy2.mudawama.habits.presentation.components` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/components/HabitHeatmapRow.kt`

- [X] T030 [P] [US1] Create `HabitListItem.kt` — `@Composable fun HabitListItem(habitWithStatus: HabitWithStatus, onToggle: () -> Unit, onIncrement: () -> Unit, onLongPress: () -> Unit, modifier: Modifier = Modifier)`; render using `combinedClickable(onLongClick = onLongPress)`; layout: habit icon resolved from `habit.iconKey` against the designsystem icon set (render a placeholder icon on unknown key per spec edge case); habit name in `MudawamaTheme.typography.bodyLarge`; completion control: for `BOOLEAN` habits a `Checkbox(checked = isCompletedToday, enabled = isDueToday, onCheckedChange = { onToggle() })`; for `NUMERIC` habits a `"${numericProgress}${if (goalCount != null) " / $goalCount" else ""}"` text label and an `IconButton(onClick = onIncrement, enabled = isDueToday)` with a "+" icon; below the main row render `HabitHeatmapRow(habitWithStatus.weekLogs, habit.frequencyDays)`; compute `isCompletedToday`, `isDueToday`, `numericProgress` inline per the derived expressions in data-model.md; add `@Preview @Composable fun HabitListItemPreview()` in package `io.github.helmy2.mudawama.habits.presentation.components` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/components/HabitListItem.kt`

- [X] T031 [US2] Create `HabitBottomSheet.kt` — `@Composable fun HabitBottomSheet(mode: BottomSheetMode, onSave: (HabitsUiAction.SaveHabit) -> Unit, onDismiss: () -> Unit)` implemented as a `ModalBottomSheet(onDismissRequest = onDismiss)`; declare `remember { mutableStateOf(…) }` for each local field: `nameInput` (pre-populated from `(mode as? EditHabit)?.habit?.name ?: ""`), `iconKey` (from `habit.iconKey` or designsystem default), `selectedDays: Set<DayOfWeek>` (from `habit.frequencyDays` or empty), `habitType: HabitType` (from `habit.type` or `BOOLEAN`), `goalInput: String` (from `habit.goalCount?.toString() ?: ""`); sheet contents per FR-016: `OutlinedTextField` for name with `isError = nameError != null` and inline error text; `LazyRow` icon picker from designsystem icon token keys; `FlowRow` of 7 day-of-week `FilterChip` toggles (Mon–Sun) with `isError = daysError != null` and inline error text; `SegmentedButton` for BOOLEAN / NUMERIC type selection; `OutlinedTextField` for goal count `visible = habitType == NUMERIC`; bottom row with `Button("Save")` that emits `onSave(HabitsUiAction.SaveHabit(nameInput, iconKey, selectedDays, habitType, goalInput.toIntOrNull()))` and `TextButton("Cancel")` that calls `onDismiss()`; add `@Preview @Composable fun HabitBottomSheetAddPreview()` and `@Preview @Composable fun HabitBottomSheetEditPreview()` in package `io.github.helmy2.mudawama.habits.presentation.components` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/components/HabitBottomSheet.kt`

- [X] T032 [US4] Create `HabitOptionsSheet.kt` — `@Composable fun HabitOptionsSheet(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit)` implemented as a `ModalBottomSheet(onDismissRequest = onDismiss)`; render an "Edit" clickable list item (always visible) that calls `onEdit()` then `onDismiss()`; render a "Delete" clickable list item styled in error/destructive color that calls `onDelete()` then `onDismiss()` — render this item ONLY when `habit.isCore == false` (FR-017); add `@Preview @Composable fun HabitOptionsSheetCustomPreview()` (with `isCore = false`, showing Delete) and `@Preview @Composable fun HabitOptionsSheetCorePreview()` (with `isCore = true`, Delete hidden) in package `io.github.helmy2.mudawama.habits.presentation.components` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/components/HabitOptionsSheet.kt`

### Screen & DI

- [X] T033 [US1] Create `HabitsScreen.kt` — `@Composable fun HabitsScreen(viewModel: HabitsViewModel = koinViewModel())`; collect `val state by viewModel.state.collectAsStateWithLifecycle()`; observe `viewModel.eventFlow` via `ObserveAsEvents { event -> when (event) { is ShowSnackbar -> snackbarHostState.showSnackbar(event.message) } }` (declare a `SnackbarHostState` with `remember`); render `Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, floatingActionButton = { if (state.bottomSheetMode == BottomSheetMode.Hidden) FloatingActionButton(onClick = { viewModel.onAction(HabitsUiAction.AddHabitFabClicked) }) { Icon(…, contentDescription = "Add Habit") } }) { padding -> when { state.isLoading -> Box(Modifier.fillMaxSize().padding(padding)) { CircularProgressIndicator(Modifier.align(Center)) }; state.habits.isEmpty() -> HabitsEmptyState(Modifier.padding(padding)); else -> LazyColumn(contentPadding = padding) { items(state.habits, key = { it.habit.id }) { HabitListItem(it, onToggle = { viewModel.onAction(ToggleCompletion(it.habit.id)) }, onIncrement = { viewModel.onAction(IncrementCount(it.habit.id)) }, onLongPress = { viewModel.onAction(HabitLongPressed(it.habit)) }) } } } }`; outside the Scaffold render bottom-sheet/dialog by `when (val mode = state.bottomSheetMode)`: `AddHabit` or `EditHabit` → `HabitBottomSheet(mode, onSave = { viewModel.onAction(it) }, onDismiss = { viewModel.onAction(DismissBottomSheet) })`; `OptionsMenu` → `HabitOptionsSheet(mode.habit, onEdit = { viewModel.onAction(EditHabitSelected(mode.habit)) }, onDelete = { viewModel.onAction(DeleteHabitSelected(mode.habit.id)) }, onDismiss = { viewModel.onAction(DismissBottomSheet) })`; `DeleteConfirm` → `AlertDialog` with "Confirm Delete" / "Cancel" buttons dispatching `DeleteConfirmed` / `DismissBottomSheet`; `Hidden` → nothing; also render inline error `state.errorMessage` as a dismissible `Snackbar` or `Text` below the FAB, dismissed via `viewModel.onAction(DismissError)`; add `@Preview @Composable fun HabitsScreenPreview()` seeded with a non-empty `HabitsUiState` in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/HabitsScreen.kt`

- [X] T034 Create `HabitsPresentationModule.kt` — `fun habitsPresentationModule() = module { viewModel { HabitsViewModel(observeHabitsUseCase = get(), createHabitUseCase = get(), updateHabitUseCase = get(), deleteHabitUseCase = get(), toggleCompletionUseCase = get(), incrementCountUseCase = get()) } }` in package `io.github.helmy2.mudawama.habits.presentation.di`; add KDoc: "Requires `habitsDomainModule()` to be loaded first so all 6 use case factory bindings are available when the ViewModel is first resolved" in `feature/habits/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/presentation/di/HabitsPresentationModule.kt`

### Navigation Wiring (FR-020)

- [X] T035 Update `MudawamaAppShell.kt` in `shared:navigation` — add a `habitsScreen: @Composable () -> Unit = { HabitsPlaceholderScreen() }` parameter to the `MudawamaAppShell` composable (before the existing function body); inside the `entryProvider` block replace the hardcoded `HabitsPlaceholderScreen()` call in the `entry<HabitsRoute>` lambda with `habitsScreen()` — the default argument preserves backward compatibility so existing call sites without the parameter still render the placeholder (Decision 3 in research.md); no new `import` or Gradle dependency is added to `shared:navigation` in `shared/navigation/src/commonMain/kotlin/io/github/helmy2/mudawama/navigation/MudawamaAppShell.kt`

- [X] T036 Update `App.kt` in `shared:umbrella-ui` — change `MudawamaAppShell()` to `MudawamaAppShell(habitsScreen = { HabitsScreen() })`; add `import io.github.helmy2.mudawama.habits.presentation.HabitsScreen`; `App()` is already the entry point called by both `MainActivity` and `MainViewController()` — no changes to those callers are needed (SC-009) in `shared/umbrella-ui/src/commonMain/kotlin/io/github/helmy2/mudawama/umbrella/ui/App.kt`

- [X] T037 Update `shared/umbrella-ui/build.gradle.kts` — add three `implementation` dependencies inside the existing `commonMain.dependencies { … }` block: `implementation(projects.feature.habits.domain)`, `implementation(projects.feature.habits.data)`, `implementation(projects.feature.habits.presentation)`; `umbrella-ui` is the composition root that aggregates all three habits sub-modules; this makes `HabitsScreen`, the Koin module factories, and the domain types visible to `App.kt` and the platform `KoinInitializer` files in `shared/umbrella-ui/build.gradle.kts`

- [X] T038 Update `shared/umbrella-ui/src/androidMain/kotlin/io/github/helmy2/mudawama/umbrella/ui/KoinInitializer.kt` — add `habitsDomainModule()`, `habitsDataModule()`, `habitsPresentationModule()` to the `modules(…)` list in `setupModules()`, placed after the existing `timeModule()` call (ensures `TimeProvider` is bound before `habitsDataModule()` and `habitsDomainModule()` resolve their dependencies); add imports: `io.github.helmy2.mudawama.habits.domain.di.habitsDomainModule`, `io.github.helmy2.mudawama.habits.data.di.habitsDataModule`, `io.github.helmy2.mudawama.habits.presentation.di.habitsPresentationModule` in `shared/umbrella-ui/src/androidMain/kotlin/io/github/helmy2/mudawama/umbrella/ui/KoinInitializer.kt`

- [X] T039 Update `shared/umbrella-ui/src/iosMain/kotlin/io/github/helmy2/mudawama/umbrella/ui/KoinInitializer.kt` — add `habitsDomainModule()`, `habitsDataModule()`, `habitsPresentationModule()` to the `modules(…)` list in `initializeKoin(iosEncryptor)`, placed after the existing `timeModule()` call; same imports as T038; this ensures the iOS Koin graph includes all three habits modules before `HabitsScreen` is first displayed in `shared/umbrella-ui/src/iosMain/kotlin/io/github/helmy2/mudawama/umbrella/ui/KoinInitializer.kt`

---

## Final Phase — Polish & Build Verification

> **Goal**: Confirm all success criteria (SC-001 through SC-009) pass before the branch is merged. Run once all 39 prior tasks are complete.
>
> **Prerequisite**: T001–T039 complete.

- [X] T040 [P] Verify SC-002 compliance — run `git grep -rn "Clock\.System" --include="*.kt" -- feature/` from the repository root; the only permitted match is `HabitHeatmapRow.kt` where `remember { Clock.System.todayIn(…) }` is used for display-layer day-of-week lookup (explicitly documented as acceptable in plan.md §4); confirm zero `Clock.System` calls exist in any file under `feature/habits/domain/` or `feature/habits/data/`; if a violation is found, refactor that call site to receive `TimeProvider` via constructor injection and re-run the grep in `feature/habits/domain/src/` and `feature/habits/data/src/`

- [X] T041 Run the full multi-module compile and Android build — execute `./gradlew :feature:habits:domain:compileCommonMainKotlinMetadata :feature:habits:data:compileCommonMainKotlinMetadata :feature:habits:presentation:compileCommonMainKotlinMetadata` to confirm all three modules compile independently with zero errors; then run `./gradlew assembleDebug` to confirm the Android binary builds with all modules linked; fix any import, namespace, or project accessor errors before proceeding to T042 (SC-009)

- [X] T042 [P] Verify iOS build and run smoke-test checklist — execute `./gradlew :shared:umbrella-ui:linkDebugFrameworkIosSimulatorArm64` (or the equivalent `linkDebug*` task for the active simulator architecture) and confirm zero linker errors; then run the smoke-test checklist in `specs/005-feature-habits/quickstart.md §Smoke Test Checklist` on an iOS Simulator to verify SC-001 (habit create), SC-002 (≤300 ms toggle response), SC-003 (heatmap accuracy), SC-008 (cascade delete), and SC-009 (clean build on both platforms)

---

## Dependencies (Completion Order)

```
T001 ──→ T002 ──→ T003 [P] ──┐
                  T004 [P] ──┤
                  T005 [P] ──┤──→ T006 ──→ T011 [P][US1] ──┐
                  T007 [P] ──┤    T012 [P][US2] ────────────┤
                  T008 [P] ──┤    T013 [P][US4] ────────────┤──→ T018
                  T009 [P] ──┤    T014 [P][US5] ────────────┤
                  T010 [P] ──┘    T015 [P][US3] ────────────┤
                                  T016 [P][US3] ────────────┤
                                  T017 [P][US6] ────────────┘

T018 ──→ T019 ──→ T020 [P] ──→ T022 ──┐
                  T021 [P] ──→ T023 ──┴──→ T024

T024 (or T018) ──→ T025 ──→ T026 [P] ──┐
                              T027 [P] ──┴──→ T028 [US1] ──→ T033 [US1]
                                              T029 [P][US6] ──┘
                                              T030 [P][US1] ──┘
                                              T031 [US2] ─────┘
                                              T032 [US4] ─────┘
                                        ──→ T034 ──→ T035 ──→ T036 ──→ T037
                                                               ──→ T038
                                                               ──→ T039

T037 + T038 + T039 ──→ T040 [P] ──┐
                        T041       ├──→ T042 [P]
```

**Critical path** (longest blocking chain):
`T001 → T002 → T005 → T006 → T011 → T018 → T019 → T022 → T024 → T025 → T027 → T028 → T033 → T034 → T035 → T036 → T037 → T038 → T041`

---

## Parallel Execution Examples

**After T002 completes** (domain module registered + Gradle sync done):
- T003 (HabitType, LogStatus), T004 (Habit), T005 (HabitLog), T007 (HabitError), T008 (HabitRepository), T009 (HabitLogRepository), T010 (IdGenerator) can all be developed simultaneously in separate files.
- T006 (HabitWithStatus) follows as soon as T004 + T005 are done.

**After T003–T010 complete** (all domain models, errors, interfaces, and utilities ready):
- T011–T017 (all 7 use cases) can be written in parallel across 7 independent files. T011 additionally requires T006 (HabitWithStatus type).

**After T019 completes** (data build.gradle ready):
- T020 (HabitMapper) and T021 (HabitLogMapper) can be developed simultaneously.

**After T020 + T021 complete**:
- T022 (HabitRepositoryImpl) and T023 (HabitLogRepositoryImpl) can proceed independently (each depends only on its own mapper).

**After T025 completes** (presentation module scaffolded):
- T026 (state/mode models) and T027 (action/event models) can be developed in parallel.

**After T026 + T027 complete** (all presentation model types available):
- T028 (HabitsViewModel), T029 (HabitHeatmapRow), T030 (HabitListItem), T031 (HabitBottomSheet), T032 (HabitOptionsSheet) can all be developed in parallel across independent files.

**After T037 + T038 + T039 complete** (umbrella-ui wired up):
- T040 (SC-002 grep) and T041 (full Android build) can run simultaneously.

---

## Implementation Strategy

**MVP Scope** — delivers all three P1 stories (US1 view, US2 create, US3 check-off/increment):
`T001 → T002 → T003–T010 → T011 + T012 + T015 + T016 + T018 → T019 → T020–T024 → T025 → T026–T030 → T033 + T034 → T035–T039`

After this set, the app navigates to a functional `HabitsScreen`, displays all habits with today's status and 7-day heatmap, allows creating new habits via the bottom sheet, and supports toggling boolean completion and incrementing numeric habits.

**Incremental Delivery Order**:

| Sprint | Tasks | Deliverable |
|--------|-------|-------------|
| 1 — Domain Foundation | T001–T010 | Gradle modules registered; all domain models, interfaces, and utilities compile — unblocks all parallel work |
| 2 — Domain Use Cases | T011–T018 | All 7 use cases and Koin DI module; `:domain` fully self-contained and independently testable |
| 3 — Data Layer | T019–T024 | Room-backed repositories and bidirectional mappers; `:data` satisfies all `:domain` interface contracts |
| 4 — Presentation Foundation | T025–T030 | Presentation models, ViewModel (US1), heatmap row (US6), and list-item component (US1) |
| 5 — Presentation Sheets & Screen | T031–T034 | Add/Edit bottom sheet (US2 + US4), Options sheet (US4 + US5), full `HabitsScreen`, DI module |
| 6 — Integration & Verification | T035–T042 | Navigation wiring, Koin registration, SC-002 check, full build and smoke test |

---

## Output Summary

| Metric | Value |
|--------|-------|
| **Total tasks** | 42 (T001–T042) |
| Phase 1 — Domain Module | 18 tasks (T001–T018) |
| Phase 2 — Data Module | 6 tasks (T019–T024) |
| Phase 3 — Presentation Module | 15 tasks (T025–T039) |
| Final Phase — Polish | 3 tasks (T040–T042) |
| **Parallelisable tasks** | 19 `[P]` tasks: T003–T005, T007–T011, T013–T017, T020–T021, T026–T027, T029–T030, T040, T042 |
| **Parallel opportunities** | 6 identified (post-T002, post-T010, post-T019, post-T021, post-T025, post-T039) |
| **MVP scope** | P1 stories only (US1 + US2 + US3): T001–T016, T018–T030, T033–T039 |

**Independent test criteria per story**:

| Story | Criterion |
|-------|-----------|
| US1 (P1) — View habits list | Seed 3 `HabitEntity` + log rows; navigate to `HabitsScreen`; confirm all 3 items appear with correct completion state and 7-cell heatmap (spec §US1 acceptance scenarios 1–5) |
| US2 (P1) — Create a habit | Tap "+" FAB; fill valid name + icon + days + type; tap Save; confirm new `HabitEntity` row in DB and item visible in list within the same session (SC-001) |
| US3 (P1) — Check off / increment | Tap Boolean check-off; confirm `HabitLogEntity` with `status = "COMPLETED"` for today's ISO date exists in DB and UI updates within 300 ms (SC-002) |
| US4 (P2) — Edit a habit | Long-press → Edit; change name; tap Save; confirm `HabitEntity.name` updated in DB and list item reflects new name |
| US5 (P2) — Delete a habit | Long-press → Delete → Confirm Delete; confirm neither `HabitEntity` nor any `HabitLogEntity` for that habit ID remains in DB (SC-008) |
| US6 (P2) — 7-day heatmap | Seed 7 `HabitLogEntity` rows with alternating COMPLETED/PENDING; confirm all 7 heatmap cells render the correct filled/muted/ghost state (SC-003) |

---

## Format Validation

All 42 tasks follow the required checklist format:
- ✅ Checkbox (`- [ ]`) on every task
- ✅ Sequential Task ID (`T001`–`T042` in execution order)
- ✅ `[P]` marker on all 19 parallelisable tasks (T003–T005, T007–T011, T013–T017, T020–T021, T026–T027, T029–T030, T040, T042)
- ✅ `[US#]` label on every user-story-phase task: T011→US1, T012→US2, T013→US4, T014→US5, T015–T016→US3, T017→US6, T028→US1, T029→US6, T030→US1, T031→US2, T032→US4, T033→US1
- ✅ Explicit file path in every task description
- ✅ Gradle wiring tasks (T001–T002, T019, T025), DI module tasks (T018, T024, T034), navigation wiring tasks (T035–T039), and polish tasks (T040–T042) carry no `[US#]` story label (infrastructure / cross-cutting nature)

