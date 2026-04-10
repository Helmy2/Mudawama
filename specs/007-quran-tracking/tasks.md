# Tasks: Quran Reading Tracker

**Input**: Design documents from `/specs/007-quran-tracking/`  
**Branch**: `007-quran-tracking`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ui-contracts.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1–US5)
- No test tasks — not requested in the spec

---

## Phase 1: Setup (Gradle Modules & Navigation Wiring)

**Purpose**: Register all new modules, add string resources, and wire navigation so the project compiles cleanly before any feature logic is written.

- [ ] T001 Add `:feature:quran:domain`, `:feature:quran:data`, `:feature:quran:presentation` to `settings.gradle.kts`
- [ ] T002 Create `feature/quran/domain/build.gradle.kts` with plugins `mudawama.kmp` + `mudawama.kmp.koin`; deps: `shared.core.domain` (api), `shared.core.time`, `kotlinx.coroutines.core`, `kotlinx.datetime`
- [ ] T003 [P] Create `feature/quran/data/build.gradle.kts` with plugins `mudawama.kmp` + `mudawama.kmp.koin`; deps: `feature.quran.domain`, `shared.core.database`, `shared.core.time`, `shared.core.domain`, `kotlinx.datetime`, `kotlinx.coroutines.core`
- [ ] T004 [P] Create `feature/quran/presentation/build.gradle.kts` with plugin `mudawama.kmp.compose`; deps: `feature.quran.domain`, `shared.core.presentation`, `shared.designsystem`, `bundles.compose`, `bundles.lifecycle`, `koin.compose.viewmodel`, `kotlinx.datetime`, `material.icons.extended`; androidMain: `activity.compose`, `ui.tooling`, `koin.android`
- [ ] T005 Add all ~35 `quran_*` string keys to `shared/designsystem/src/commonMain/composeResources/values/strings.xml` (full key list in `research.md` §8) — **must be done before any Composable is written**

**Checkpoint**: Project syncs with three empty modules; string keys compile cleanly from designsystem.

---

## Phase 2: Foundational — Database (Blocking Prerequisite)

**Purpose**: Bump `shared:core:database` to version 3 with the two new entities, the modified bookmark entity, and the new DAOs. All user stories depend on these entities being present.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T006 Create `shared/core/database/src/commonMain/kotlin/…/core/database/entity/QuranDailyLogEntity.kt` — `@Entity(tableName = "quran_daily_logs")`, fields: `id: String` (PK UUID), `date: String` ("yyyy-MM-dd"), `pagesRead: Int`, `loggedAt: Long`
- [ ] T007 [P] Create `shared/core/database/src/commonMain/kotlin/…/core/database/entity/QuranGoalEntity.kt` — `@Entity(tableName = "quran_goals")`, fields: `id: Int = 1` (PK singleton), `pagesPerDay: Int`, `updatedAt: Long`
- [ ] T008 [P] Modify `shared/core/database/src/commonMain/kotlin/…/core/database/entity/QuranBookmarkEntity.kt` — remove `dailyGoalPages` and `pagesReadToday` fields; add `@DeleteColumn` annotations on the entity to support AutoMigration spec
- [ ] T009 Create `shared/core/database/src/commonMain/kotlin/…/core/database/dao/QuranDailyLogDao.kt` — `@Insert(onConflict=REPLACE) suspend fun insertLog(…)`; `fun getLogsForDate(date: String): Flow<List<QuranDailyLogEntity>>`; `fun getLogsBefore(beforeDate: String): Flow<List<QuranDailyLogEntity>>`; `suspend fun getAllLoggedDates(): List<String>`
- [ ] T010 [P] Create `shared/core/database/src/commonMain/kotlin/…/core/database/dao/QuranGoalDao.kt` — `@Upsert suspend fun upsertGoal(…)`; `fun getGoal(): Flow<QuranGoalEntity?>`
- [ ] T011 [P] Modify `shared/core/database/src/commonMain/kotlin/…/core/database/dao/QuranBookmarkDao.kt` — remove `resetDailyPages()` (column no longer exists)
- [ ] T012 Modify `shared/core/database/src/commonMain/kotlin/…/core/database/MudawamaDatabase.kt` — bump `version = 3`; add `QuranDailyLogEntity` and `QuranGoalEntity` to `entities` list; add inner class `AutoMigration_2_3 : AutoMigrationSpec` with `@DeleteColumn(tableName="quran_bookmarks", columnName="dailyGoalPages")` and `@DeleteColumn(tableName="quran_bookmarks", columnName="pagesReadToday")`; add `AutoMigration(from=2, to=3, spec=…::class)` entry; add abstract `quranDailyLogDao()` and `quranGoalDao()` functions
- [ ] T013 Modify `shared/core/database/src/commonMain/kotlin/…/core/database/di/DatabaseModule.kt` — register `single<QuranDailyLogDao> { get<MudawamaDatabase>().quranDailyLogDao() }` and `single<QuranGoalDao> { get<MudawamaDatabase>().quranGoalDao() }`

**Checkpoint**: Project builds; Room schema version 3 generated under `shared/core/database/schemas/`; existing data unaffected.

---

## Phase 3: User Story 1 — Log Today's Quran Reading (Priority: P1) 🎯 MVP

**Goal**: A user opens the Quran tab, taps "Log Reading", adjusts a page counter using the stepper and quick-add chips, taps "Done", and the main screen immediately reflects the additive new total on the progress ring.

**Independent Test**: Install on device with a fresh DB → navigate to Quran tab → tap "Log Reading" → increment to 5 pages → "Done" → ring shows 5 pages; tap again → log 3 more → ring shows 8.

### Domain

- [ ] T014 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/error/QuranError.kt` — `sealed interface QuranError : DomainError` with cases `DatabaseError`, `InvalidPageCount`, `InvalidGoal`, `InvalidSurah`, `InvalidAyah`, `GenericError`; add top-level constants `MAX_SESSION_PAGES = 604`, `MAX_DAILY_GOAL_PAGES = 60`, `DEFAULT_DAILY_GOAL_PAGES = 5`
- [ ] T015 [P] Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/model/QuranDailyLog.kt` — `data class QuranDailyLog(id, date, pagesRead, loggedAt)`
- [ ] T016 [P] Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/model/QuranGoal.kt` — `data class QuranGoal(pagesPerDay, updatedAt)`; add `val DEFAULT_DAILY_GOAL = QuranGoal(pagesPerDay = DEFAULT_DAILY_GOAL_PAGES, updatedAt = 0L)`
- [ ] T017 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/repository/QuranDailyLogRepository.kt` — interface with: `fun observeLogsForDate(date: String): Flow<List<QuranDailyLog>>`; `fun observeRecentLogs(beforeDate: String): Flow<List<QuranDailyLog>>`; `suspend fun insertLog(log: QuranDailyLog): EmptyResult<QuranError>`; `suspend fun getAllLoggedDates(): Result<List<String>, QuranError>`
- [ ] T018 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/usecase/LogReadingUseCase.kt` — constructor: `(repo: QuranDailyLogRepository, timeProvider: TimeProvider, dispatcher: CoroutineDispatcher)`; `suspend operator fun invoke(pages: Int, date: LocalDate): EmptyResult<QuranError>`; validate `pages in 1..MAX_SESSION_PAGES`; create a `QuranDailyLog` with UUID id and `repo.insertLog()`

### Data

- [ ] T019 Create `feature/quran/data/src/commonMain/kotlin/…/quran/data/mapper/QuranDailyLogMapper.kt` — `fun QuranDailyLogEntity.toDomain(): QuranDailyLog` and `fun QuranDailyLog.toEntity(): QuranDailyLogEntity`
- [ ] T020 Create `feature/quran/data/src/commonMain/kotlin/…/quran/data/repository/QuranDailyLogRepositoryImpl.kt` — `internal class` implementing `QuranDailyLogRepository`; `insertLog` uses `safeCall { dao.insertLog(log.toEntity()) } { QuranError.DatabaseError }`; `observeLogsForDate` maps Flow via `toDomain()`; `observeRecentLogs` uses `dao.getLogsBefore()`, groups by date in-memory, keeps 3 most recent distinct dates; `getAllLoggedDates` wraps in `safeCall`

### Presentation (US1 slice — progress ring + Log Reading sheet)

- [ ] T021 Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/model/QuranUiState.kt` — `data class QuranUiState(selectedDate, dateStrip, today, pagesReadToday=0, goalPages=5, bookmark=null, recentLogs=emptyList(), streak=0, isLoading=true, logReadingSheetVisible=false, setGoalSheetVisible=false, updatePositionSheetVisible=false, logReadingPageInput=0)` **(no `returnToLogReadingSheet` field)**; computed `val isReadOnly`, `val progressFraction`; `sealed interface QuranUiAction` (all actions from data-model.md §9 — **no `OpenUpdatePositionFromLogSheet`**); `sealed interface QuranUiEvent { data class ShowSnackbar(…) }`
- [ ] T022 Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/components/QuranProgressRing.kt` — stateless `@Composable` accepting `pagesRead: Int`, `goalPages: Int`, `progressFraction: Float`, `isLoading: Boolean`; large `CircularProgressIndicator` arc (12dp stroke, `secondary` color); center text via `quran_of_pages_format`; subtitle from `quran_daily_progress_subtitle_in_progress` / `quran_daily_progress_subtitle_complete` based on `progressFraction >= 1f`; include `@Preview`
- [ ] T023 Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/sheets/LogReadingSheet.kt` — `ModalBottomSheet(skipPartiallyExpanded=true)`; stepper (−/+) clamped `0..MAX_SESSION_PAGES`; quick-add chips "+1 Page" (+1), "+5 Pages" (+5), "1 Juz" (+20); "PAGES READ THIS SESSION" label; arc ring showing `pageInput / goalPages`; "Done" button → `onConfirm(pageInput)` no-op if `pageInput == 0`; **no "Current Position" row** — bookmark auto-advances silently after confirm; all strings via `stringResource(Res.string.quran_*)`; include `@Preview`
- [ ] T024 [US1] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/QuranViewModel.kt` — extends `MviViewModel<QuranUiState, QuranUiAction, QuranUiEvent>`; constructor: `(observeQuranStateUseCase, logReadingUseCase, setGoalUseCase, updateBookmarkUseCase, computeStreakUseCase, advanceBookmarkUseCase, timeProvider)`; `init` block: compute streak then `state.collectLatest { currentState → observeQuranStateUseCase(selectedDate).collect { … } }`; `generateDateStrip(today)` generates `(-6..0).map { today.plus(DatePeriod(days=it)) }` (today is rightmost); `onAction` handles all `QuranUiAction` cases: `SelectDate` → `reduce { copy(selectedDate=…) }`; `ConfirmLogReading` → `exclusiveIntent("log") { logReadingUseCase(pages, selectedDate); advanceBookmarkUseCase(currentBookmark, pages) /* errors silently ignored */; recomputeStreak() }`; sheet open/dismiss → `reduce { copy(…=true/false) }`; **no `OpenUpdatePositionFromLogSheet` handler; no `returnToLogReadingSheet` state**
- [ ] T025 [US1] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/QuranScreen.kt` — `QuranScreen()` entry point using `koinViewModel<QuranViewModel>()`; `QuranScreenContent(state, onAction)` stateless inner composable; renders `QuranProgressRing`, `QuranGoalCard`, `QuranResumeReadingCard`, `QuranRecentLogsList`; shows `LogReadingSheet` when `state.logReadingSheetVisible`; "Log Reading" button on screen body (hidden when `state.isReadOnly`); all strings via `stringResource`; include `@Preview`

### DI + Wiring

- [ ] T026 [US1] Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/di/QuranDomainModule.kt` — top-level `val quranDomainModule = module { factory<CoroutineDispatcher> { Dispatchers.Default }; factoryOf(::LogReadingUseCase) }` (remaining use cases added in later phases)
- [ ] T027 [US1] Create `feature/quran/data/src/commonMain/kotlin/…/quran/data/di/QuranDataModule.kt` — `fun quranDataModule() = module { single<QuranDailyLogRepository> { QuranDailyLogRepositoryImpl(get()) } }`
- [ ] T028 [US1] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/di/QuranPresentationModule.kt` — `fun quranPresentationModule() = module { viewModelOf(::QuranViewModel) }`
- [ ] T029 [US1] Wire Koin in `androidApp` entry (and iOS entry): load `quranDataModule()`, `quranDomainModule`, `quranPresentationModule()` after `coreDatabaseModule`
- [ ] T030 [US1] Modify `shared/navigation/src/commonMain/kotlin/…/navigation/MudawamaAppShell.kt` — add `quranScreen: @Composable () -> Unit` parameter; replace `entry<QuranRoute> { QuranPlaceholderScreen() }` with `entry<QuranRoute> { quranScreen() }`; update call-site in `androidApp` to pass `{ QuranScreen() }`

**Checkpoint**: Quran tab loads; log reading sheet opens; page counter works; "Done" saves; progress ring updates reactively. US1 fully functional.

---

## Phase 4: User Story 2 — View Daily Progress & Goal (Priority: P2)

**Goal**: The user sees a progress ring with "N OF G PAGES", a Goal card with the active daily target, can tap the Goal card to open the Daily Quran Goal sheet, and changing the goal immediately re-calculates the ring.

**Independent Test**: Set goal to 10 via sheet → log 6 pages → ring shows ~60%; change goal to 5 → ring shows 100% (clamped). Default of 5 pages applies with no prior goal set.

### Domain

- [ ] T031 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/repository/QuranGoalRepository.kt` — interface: `fun observeGoal(): Flow<QuranGoal>`; `suspend fun setGoal(goal: QuranGoal): EmptyResult<QuranError>`
- [ ] T032 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/model/QuranBookmark.kt` — `data class QuranBookmark(surah: Int, surahName: String, ayah: Int)`
- [ ] T033 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/model/QuranScreenState.kt` — `data class QuranScreenState(pagesReadToday, goalPages, bookmark, recentLogs)`; nested `data class RecentLogEntry(date, pagesRead, goalPages)` with computed `val status: LogStatus`; nested `enum class LogStatus { OVER, HIT, UNDER }`
- [ ] T034 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/repository/QuranBookmarkRepository.kt` — interface: `fun observeBookmark(): Flow<QuranBookmark?>`; `suspend fun upsertBookmark(bookmark: QuranBookmark): EmptyResult<QuranError>`
- [ ] T035 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/usecase/ObserveQuranStateUseCase.kt` — constructor: `(logRepo, goalRepo, bookmarkRepo)`; `operator fun invoke(date: LocalDate): Flow<QuranScreenState>` using `combine(goalRepo.observeGoal(), logRepo.observeLogsForDate(dateStr), bookmarkRepo.observeBookmark(), logRepo.observeRecentLogs(todayStr)) { goal, logs, bookmark, recentRaw → QuranScreenState(…) }`; `buildRecentEntries` groups `recentRaw` by date, sums pages, maps to `RecentLogEntry` list (max 3)
- [ ] T036 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/usecase/SetGoalUseCase.kt` — constructor: `(repo: QuranGoalRepository, timeProvider: TimeProvider, dispatcher: CoroutineDispatcher)`; validate `pagesPerDay in 1..MAX_DAILY_GOAL_PAGES`; on failure return `Failure(QuranError.InvalidGoal)`; on success call `repo.setGoal(QuranGoal(pagesPerDay, nowMs))`

### Data

- [ ] T037 [P] Create `feature/quran/data/src/commonMain/kotlin/…/quran/data/mapper/QuranGoalMapper.kt` — `fun QuranGoalEntity.toDomain(): QuranGoal` and `fun QuranGoal.toEntity(): QuranGoalEntity`
- [ ] T038 [P] Create `feature/quran/data/src/commonMain/kotlin/…/quran/data/mapper/QuranBookmarkMapper.kt` — `fun QuranBookmarkEntity.toDomain(): QuranBookmark` deriving `surahName = ALL_SURAHS[entity.surah - 1].nameEn`; `fun QuranBookmark.toEntity(): QuranBookmarkEntity`
- [ ] T039 Create `feature/quran/data/src/commonMain/kotlin/…/quran/data/repository/QuranGoalRepositoryImpl.kt` — `internal class`; `observeGoal()` maps Flow: `it?.toDomain() ?: DEFAULT_DAILY_GOAL`; `setGoal` uses `safeCall { dao.upsertGoal(goal.toEntity()) } { QuranError.DatabaseError }`
- [ ] T040 [P] Create `feature/quran/data/src/commonMain/kotlin/…/quran/data/repository/QuranBookmarkRepositoryImpl.kt` — `observeBookmark()` maps Flow: `it?.toDomain()`; `upsertBookmark` uses `safeCall { dao.upsertBookmark(bookmark.toEntity()) } { QuranError.DatabaseError }`

### Presentation (US2 slice — Goal card + SetGoalSheet)

- [ ] T041 [US2] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/components/QuranGoalCard.kt` — stateless `@Composable(goalPages: Int, isReadOnly: Boolean, onTap: () → Unit, onLogReadingClick: () → Unit)`; "ACTIVE GOAL" badge always shown; title `quran_goal_card_title_format`; subtitle `quran_goal_card_subtitle`; "Log Reading" CTA button hidden when `isReadOnly`; include `@Preview`
- [ ] T042 [US2] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/sheets/SetGoalSheet.kt` — `ModalBottomSheet(skipPartiallyExpanded=true)`; stepper clamped `1..MAX_DAILY_GOAL_PAGES` (60); popular goal chips (1 Page=1, 5 Pages=5, 10 Pages=10, 1 Juz=20) — selected chip highlighted primary fill; "Save" → `onSave(goal)`; `×` → `onDismiss()`; all strings via `stringResource`; include `@Preview`
- [ ] T043 [US2] Update `QuranViewModel` (`feature/quran/presentation/…/QuranViewModel.kt`) — wire `ObserveQuranStateUseCase` into the `state.collectLatest` loop (replacing any stub); add `ConfirmSetGoal` handler: `intent { setGoalUseCase(pages).onSuccess { reduce { copy(setGoalSheetVisible=false) } }.onFailure { emitEvent(ShowSnackbar(…)) } }`
- [ ] T044 [US2] Update `QuranScreen` (`feature/quran/presentation/…/QuranScreen.kt`) — pass `QuranGoalCard(goalPages, isReadOnly, onTap={ onAction(OpenSetGoalSheet) }, onLogReadingClick={ onAction(OpenLogReadingSheet) })`; show `SetGoalSheet` when `state.setGoalSheetVisible`; wire `ConfirmSetGoal` and `DismissSetGoalSheet` actions

### DI

- [ ] T045 [US2] Update `QuranDomainModule.kt` — add `factoryOf(::ObserveQuranStateUseCase)` and `factoryOf(::SetGoalUseCase)`
- [ ] T046 [US2] Update `QuranDataModule.kt` — add `single<QuranGoalRepository> { QuranGoalRepositoryImpl(get()) }` and `single<QuranBookmarkRepository> { QuranBookmarkRepositoryImpl(get()) }`

**Checkpoint**: Goal card shows active goal; Daily Quran Goal sheet opens; saving a new goal re-calculates progress ring immediately. US1 + US2 both functional.

---

## Phase 5: User Story 3 — Update Reading Position / Bookmark (Priority: P3)

**Goal**: The user taps the "Resume Reading" card (or the chevron inside the Log Reading sheet), picks a Surah from a searchable 114-item list, selects an Ayah, taps "Done", and the card updates to show the new position.

**Independent Test**: Open Quran tab → tap "Resume Reading" card → search "imran" → select Aal-E-Imran → set Ayah 50 → Done → card shows "Surah Aal-E-Imran / Ayah 50". Open Log Reading sheet → tap "Current Position" row → search "baqarah" → select → Ayah 142 → Done → position row in sheet updates.

### Domain

- [ ] T047 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/model/SurahMetadata.kt` — `data class SurahMetadata(number: Int, nameEn: String, ayahCount: Int, startPage: Int)`; `val ALL_SURAHS: List<SurahMetadata>` with all 114 entries hardcoded including Madinah Mushaf `startPage` values (use the complete list from `data-model.md` §3); add `fun surahForPage(page: Int): SurahMetadata` helper that returns the last surah whose `startPage ≤ page` (clamped to 1–604)
- [ ] T048 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/usecase/UpdateBookmarkUseCase.kt` — constructor: `(repo: QuranBookmarkRepository, dispatcher: CoroutineDispatcher)`; validate `surahNumber in 1..114`; validate `ayahNumber in 1..ALL_SURAHS[surahNumber-1].ayahCount`; build `QuranBookmark(surah, ALL_SURAHS[surahNumber-1].nameEn, ayah)`; call `repo.upsertBookmark(…)` wrapped in `withContext(dispatcher)`
- [ ] T048b Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/usecase/AdvanceBookmarkUseCase.kt` — constructor: `(repo: QuranBookmarkRepository, dispatcher: CoroutineDispatcher)`; `suspend operator fun invoke(currentBookmark: QuranBookmark?, pagesRead: Int): EmptyResult<QuranError>`; compute `startPage` from current bookmark's surah or 1 if null; `targetPage = (startPage + pagesRead).coerceAtMost(604)`; resolve new surah via `surahForPage(targetPage)`; upsert `QuranBookmark(newSurah.number, newSurah.nameEn, ayah=1)`; return `Failure(InvalidPageCount)` if `pagesRead < 1`

### Presentation

- [ ] T049 [US3] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/components/QuranResumeReadingCard.kt` — stateless `@Composable(bookmark: QuranBookmark?, onTap: () → Unit)`; when `bookmark != null`: label "RESUME READING", title = `bookmark.surahName`, subtitle `quran_resume_reading_ayah_format`; when `null`: title `quran_resume_reading_no_position`; include `@Preview`
- [ ] T050 [US3] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/sheets/UpdatePositionSheet.kt` — `ModalBottomSheet(skipPartiallyExpanded=true)`; search field filters `ALL_SURAHS` by name (case-insensitive); left column: `LazyColumn` of 114 Surah items — selected item highlighted with checkmark + `surfaceContainerLow` bg; right column: Ayah number `LazyColumn` scroll-picker bounded `1..selectedSurah.ayahCount` with "of N" indicator; Ayah clamped to new Surah max on Surah change; "Done" → `onDone(surah, ayah)`; all strings via `stringResource`; include `@Preview`
- [ ] T051 [US3] Update `QuranViewModel` (`feature/quran/presentation/…/QuranViewModel.kt`) — add `ConfirmUpdatePosition` handler: `intent { updateBookmarkUseCase(surah, ayah).onSuccess { reduce { copy(updatePositionSheetVisible=false) } }.onFailure { emitEvent(ShowSnackbar(…)) } }`
- [ ] T052 [US3] Update `QuranScreen` (`feature/quran/presentation/…/QuranScreen.kt`) — pass `QuranResumeReadingCard(bookmark, onTap={ onAction(OpenUpdatePositionSheet) })`; show `UpdatePositionSheet` when `state.updatePositionSheetVisible`; pass `allSurahs=ALL_SURAHS`; wire `ConfirmUpdatePosition` and `DismissUpdatePositionSheet` actions; **`LogReadingSheet` no longer receives `bookmark` or `onUpdatePosition` params**

### DI

- [ ] T053 [US3] Update `QuranDomainModule.kt` — add `factoryOf(::UpdateBookmarkUseCase)` and `factoryOf(::AdvanceBookmarkUseCase)`

**Checkpoint**: Resume Reading card tappable; Surah search works; Ayah picker bounded correctly; bookmark persists and re-displays after restart. US1 + US2 + US3 all functional.

---

## Phase 6: User Story 4 — Reading Streak & Recent Logs (Priority: P4)

**Goal**: The screen shows a streak count (consecutive days with ≥1 page logged) that increments on each logged day, resets if a past day has zero pages, and a "Recent Logs" list with OVER/UNDER/HIT GOAL labels per entry.

**Independent Test**: Log pages on 3 consecutive days → streak = 3. Skip a day (yesterday = 0) → open app today → streak = 0. Log 12 pages against goal of 10 → "Recent Logs" row shows "12 Pages / OVER GOAL".

### Domain

- [ ] T054 Create `feature/quran/domain/src/commonMain/kotlin/…/quran/domain/usecase/ComputeStreakUseCase.kt` — constructor: `(repo: QuranDailyLogRepository, timeProvider: TimeProvider, dispatcher: CoroutineDispatcher)`; `suspend operator fun invoke(): Int`; fetch `repo.getAllLoggedDates()` (descending); walk backward from `yesterday`; skip dates `> yesterday` (today excluded until closed, per FR-013); count consecutive days until first gap; return streak count

### Presentation

- [ ] T055 [US4] Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/components/QuranRecentLogsList.kt` — stateless `@Composable(logs: List<QuranScreenState.RecentLogEntry>, onViewAll: () → Unit)`; "Recent Logs" header with "VIEW ALL" link; per-row: date string, `"{N} Pages"`, status chip (`OVER GOAL` = primary, `HIT GOAL` = secondary, `UNDER GOAL` = onSurfaceVariant); filled checkmark circle icon for OVER/HIT, grey for UNDER; `quran_recent_logs_title`, `quran_recent_logs_view_all`, `quran_log_status_*` string keys; include `@Preview`
- [ ] T056 [US4] Update `QuranViewModel` (`feature/quran/presentation/…/QuranViewModel.kt`) — add `recomputeStreak()` private helper calling `computeStreakUseCase()` and `reduce { copy(streak=it) }`; call `recomputeStreak()` in `init` after initial state setup and inside `ConfirmLogReading` handler after `logReadingUseCase` succeeds
- [ ] T057 [US4] Update `QuranScreen` (`feature/quran/presentation/…/QuranScreen.kt`) — wire `QuranRecentLogsList(state.recentLogs, onViewAll={})` into `QuranScreenContent`; streak count display (position TBD per UI reference — render near the progress ring card or as a separate row above Recent Logs)

### DI

- [ ] T058 [US4] Update `QuranDomainModule.kt` — add `factoryOf(::ComputeStreakUseCase)`

**Checkpoint**: Streak count visible and accurate; Recent Logs list shows last 3 entries with correct status labels. US1–US4 all functional.

---

## Phase 7: User Story 5 — Navigate Past Days (Read-Only) (Priority: P5)

**Goal**: A horizontal date strip shows [today-6 … today] (today rightmost). Tapping any past chip reloads the screen with that day's data in read-only mode — "Log Reading" button hidden, Goal card CTA hidden. Tapping today's chip restores full interactive mode.

**Independent Test**: Log 5 pages today → tap yesterday chip → ring shows 0% (no log yesterday) → "Log Reading" button absent → tap today → button reappears; yesterday that had logs shows correct page total.

### Presentation

- [ ] T059 Create `feature/quran/presentation/src/commonMain/kotlin/…/quran/presentation/components/QuranDateStrip.kt` — stateless `@Composable(dates: List<LocalDate>, selectedDate: LocalDate, today: LocalDate, onDateSelected: (LocalDate) → Unit)`; `LazyRow` of 7 pill chips; chip width 52dp, cornerRadius 26dp; selected chip: primary fill + onPrimary text + onPrimary dot below; today chip (unselected): surfaceContainer fill + onSurface text + primary dot; other: surfaceContainer + onSurfaceVariant, no dot; include `@Preview`
- [ ] T060 [US5] Update `QuranScreen` (`feature/quran/presentation/…/QuranScreen.kt`) — place `QuranDateStrip` at top of `QuranScreenContent`; wire `onDateSelected = { onAction(SelectDate(it)) }`; enforce read-only: "Log Reading" button visibility = `!state.isReadOnly`; pass `isReadOnly = state.isReadOnly` to `QuranGoalCard` (CTA hidden when past day)

**Checkpoint**: Date strip renders correctly; past-day selection switches to read-only; today's chip re-enables all interactions. All 5 user stories functional end-to-end.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Completeness, edge cases, and constitution compliance across all stories.

- [ ] T061 Verify zero-state renders correctly in `QuranScreen` — no logs, no goal ever set, no bookmark: ring shows 0/5, Goal card shows default, Resume Reading shows "Tap to set your reading position", Recent Logs empty; no crash or blank layout
- [ ] T062 [P] Verify string resource compliance — `grep -rn 'Text("' feature/quran/presentation/` must return zero results; all user-visible text uses `stringResource(Res.string.quran_*)`; import is `mudawama.shared.designsystem.Res`
- [ ] T063 [P] Verify no forbidden imports in domain — `grep -rn 'import android\.\|import androidx\.\|import io\.ktor\.' feature/quran/domain/` must return zero results
- [ ] T064 [P] Verify dependency direction — `feature/quran/presentation/build.gradle.kts` must not reference `projects.feature.quran.data` or `projects.shared.core.database`
- [ ] T065 Verify `LogReadingSheet` page stepper bounds — manually test: tap "−" at 0 stays at 0; enter 604 pages → "+" stays at 604; "1 Juz" chip at 590 clamps to 604
- [ ] T066 [P] Verify `SetGoalSheet` stepper bounds — tap "−" at 1 stays at 1; tap "+" at 60 stays at 60; "1 Juz" chip sets to 20; selected chip highlighted
- [ ] T067 Verify `UpdatePositionSheet` Ayah clamping — select Al-Fatihah (7 ayahs) when prior selection had Ayah 100 → Ayah resets to 7 (or 1); picker max shows "of 7"
- [ ] T068 [P] Verify Room AutoMigration 2→3 — install a build with DB version 2 (existing app data); upgrade to version 3 build; confirm: `quran_bookmarks` table no longer has `dailyGoalPages`/`pagesReadToday` columns; existing bookmark row (if present) is intact; no crash on first launch after upgrade
- [ ] T069 Verify streak boundary — log pages today only (no historical data) → streak = 0 (today excluded until midnight per FR-013); yesterday with no log → streak = 0; log yesterday + today → streak = 1 (yesterday counts)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately; T002/T003/T004 parallelizable after T001
- **Phase 2 (Database)**: Requires Phase 1 complete; T006/T007/T008/T010/T011 parallelizable; T009 after T006; T012 after T006–T011; T013 after T012
- **Phase 3 (US1)**: Requires Phase 2 complete; T014–T016 parallelizable; T017 after T014; T018 after T015+T017; T019 after T015; T020 after T019; T021 after T014; T022–T023 after T021; T024 after T021+T018+T020; T025 after T022+T024; T026–T028 after T018+T020; T029–T030 after T028
- **Phase 4 (US2)**: Requires Phase 3 complete; T031–T034 parallelizable; T035 after T031+T033+T034; T036 after T031; T037–T038 parallelizable; T039 after T037; T040 after T038; T041–T042 after T021; T043–T044 after T041+T042; T045–T046 after T035+T036+T039+T040
- **Phase 5 (US3)**: Requires Phase 4 complete; T047 before T048; T049–T050 after T021+T047; T051–T052 after T050; T053 after T048
- **Phase 6 (US4)**: Requires Phase 5 complete; T054 after T017; T055–T057 after T021+T054; T058 after T054
- **Phase 7 (US5)**: Requires Phase 6 complete; T059 independent; T060 after T059
- **Phase 8 (Polish)**: Requires all prior phases complete; all [P] tasks run in parallel

### User Story Dependencies

- **US1 (P1)**: Depends on Phase 2 (DB) only — no other story dependency
- **US2 (P2)**: Depends on US1 complete (ViewModel and screen scaffolding reused)
- **US3 (P3)**: Depends on US2 complete (bookmark repo + screen already wired)
- **US4 (P4)**: Depends on US1 complete (logReadingUseCase + ViewModel pattern established)
- **US5 (P5)**: Depends on US1 complete (QuranScreen scaffolding); self-contained date strip component

---

## Parallel Execution Examples

### Phase 2 — Database (run together after T005)

```
T006 QuranDailyLogEntity.kt
T007 QuranGoalEntity.kt
T008 QuranBookmarkEntity.kt (modify)
T010 QuranGoalDao.kt
T011 QuranBookmarkDao.kt (modify)
```

### Phase 3 — US1 Domain Models (run together after T014)

```
T015 QuranDailyLog.kt
T016 QuranGoal.kt
T019 QuranDailyLogMapper.kt
```

### Phase 4 — US2 Mappers (run together after T032+T034)

```
T037 QuranGoalMapper.kt
T038 QuranBookmarkMapper.kt
T039 QuranGoalRepositoryImpl.kt
T040 QuranBookmarkRepositoryImpl.kt
```

### Phase 8 — Polish compliance checks (run all together)

```
T062 String resource compliance grep
T063 Forbidden imports grep (domain)
T064 Dependency direction check
T066 SetGoalSheet stepper bounds
T068 Room AutoMigration smoke test
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T005)
2. Complete Phase 2: Database (T006–T013) — CRITICAL, blocks all stories
3. Complete Phase 3: User Story 1 (T014–T030)
4. **STOP and VALIDATE**: Log Reading sheet opens → pages saved → ring updates → data survives restart
5. Demo the Quran tab as a functional MVP

### Incremental Delivery

1. Phase 1 + Phase 2 → foundation ready
2. Phase 3 (US1) → log reading works → MVP demo
3. Phase 4 (US2) → goal setting works → goal card interactive
4. Phase 5 (US3) → bookmark/position works → Resume Reading card functional
5. Phase 6 (US4) → streak + recent logs visible → motivational layer complete
6. Phase 7 (US5) → date strip read-only → full parity with prayer screen
7. Phase 8 → polish, constitution compliance, migration test

---

## Notes

- [P] tasks = different files, no incomplete-task dependencies — safe to run in parallel
- [USN] label maps each task to its user story for independent traceability
- All string keys must be added (T005) before any `@Composable` is written — constitution violation otherwise
- `feature/quran/presentation` must never import `feature/quran/data` or `shared/core/database` — verify with T064
- `ALL_SURAHS` list in `SurahMetadata.kt` (T047) is the single source of truth used by both `UpdateBookmarkUseCase` (validation) and `QuranBookmarkMapper.toDomain()` (name lookup) — implement domain model before data mapper
- Room schema JSON for version 3 is auto-generated by KSP on first successful build after T012 — commit it alongside the migration
