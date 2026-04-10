# Research: Quran Tracking Feature (007)

**Branch**: `007-quran-tracking` | **Date**: 2026-04-10

---

## 1. Existing `shared:core:database` State

### Current Database (Version 2)

`MudawamaDatabase` already contains `QuranBookmarkEntity` and `QuranBookmarkDao` — they were scaffolded early but are limited:

**`QuranBookmarkEntity`** (table `quran_bookmarks`, existing):
```
id: Int = 1       surah: Int       ayah: Int
dailyGoalPages: Int    pagesReadToday: Int    lastUpdated: Long
```

**`QuranBookmarkDao`** (existing):
- `suspend fun upsertBookmark(bookmark: QuranBookmarkEntity)` — `@Upsert`
- `fun getBookmark(): Flow<QuranBookmarkEntity?>` — `WHERE id = 1`
- `suspend fun resetDailyPages()` — `UPDATE … SET pagesReadToday = 0 WHERE id = 1`

### Gap Analysis: What's Missing for the Full Feature

The existing `QuranBookmarkEntity` conflates bookmark (position) with daily tracking (`pagesReadToday`, `dailyGoalPages`). The spec requires:
- **`QuranGoalEntity`** — dedicated singleton for the daily page goal (A-008: single-row Room entity, INSERT OR REPLACE, `id = 1`).
- **`QuranDailyLogEntity`** — per-session read log keyed by date. Multiple sessions per day are additive. This is an N-row table (one row per session).
- The existing `QuranBookmarkEntity` can be **kept as-is** for position (surah + ayah) after stripping the tracking fields.

### Decision: Database Migration Strategy

**Decision:** Bump `MudawamaDatabase` to **version 3**; add `AutoMigration(from = 2, to = 3)`.

Add two new entities:
1. `QuranDailyLogEntity` — new table `quran_daily_logs`
2. `QuranGoalEntity` — new table `quran_goals`

Also **modify** `QuranBookmarkEntity` to drop the `dailyGoalPages` and `pagesReadToday` columns (they now belong to dedicated entities). Because Room auto-migrations support column removal only via `@DeleteColumn`, a `RenameAndDeleteMigration` spec annotation will handle this in the migration spec file.

**Rationale:** Separation of concerns matches the spec's A-002 and A-008. Keeping existing bookmark DAO reduces changes to prayer-adjacent infrastructure.

**Alternative considered:** Reuse the existing `QuranBookmarkEntity` columns for goal/pages. Rejected because it violates single-responsibility and would make the DAO harder to reason about.

---

## 2. Module Structure Decision

**Decision:** Create `feature/quran/domain`, `feature/quran/data`, `feature/quran/presentation` — matching the prayer module structure exactly.

| Module | Convention Plugin | Key Dependencies |
|--------|------------------|------------------|
| `:feature:quran:domain` | `mudawama.kmp` + `mudawama.kmp.koin` | `shared.core.domain` (api), `shared.core.time`, `kotlinx.datetime` |
| `:feature:quran:data` | `mudawama.kmp` + `mudawama.kmp.koin` | `feature.quran.domain`, `shared.core.database`, `shared.core.time`, `kotlinx.datetime` |
| `:feature:quran:presentation` | `mudawama.kmp.compose` | `feature.quran.domain`, `shared.core.presentation`, `shared.designsystem`, `koin.compose.viewmodel`, `kotlinx.datetime` |

All three need `include(":feature:quran:<layer>")` added to `settings.gradle.kts`.

**No Ktor dependency** in data — this feature is 100% offline (spec SC-008). No network calls at any point.

---

## 3. Surah Metadata Strategy

**Decision:** Hardcode all 114 Surahs as an in-memory `val` list in the domain layer — `SurahMetadata.kt`.

**Rationale:**
- Fully offline (spec SC-008, A-002). The 114-Surah/Ayah count data is static and will never change.
- The domain layer owns this static knowledge (it is Quran domain logic, not a DB or network concern).
- No Room entity needed; no API. Instantaneous lookup.

**Alternatives considered:**
- Bundled JSON asset: unnecessary complexity for immutable data.
- Room table pre-populated via migration: overkill; assets can't be queried at compile time.

**Data structure:**
```kotlin
// feature/quran/domain/model/SurahMetadata.kt
data class SurahMetadata(val number: Int, val nameEn: String, val ayahCount: Int)

val ALL_SURAHS: List<SurahMetadata> = listOf(
    SurahMetadata(1, "Al-Fatihah", 7),
    SurahMetadata(2, "Al-Baqarah", 286),
    SurahMetadata(3, "Aal-E-Imran", 200),
    // … 114 entries total
)
```

`UpdateBookmarkUseCase` validates `ayah <= ALL_SURAHS[surahNumber - 1].ayahCount` before persisting.

---

## 4. Use Cases — Design Decisions

### `LogReadingUseCase`
- **Input:** `pages: Int, date: LocalDate`
- **Output:** `EmptyResult<QuranError>`
- **Behaviour:** Creates a new `QuranDailyLog` session row (additive). Pages = 0 → no-op, returns `Success(Unit)`. Pages > 604 → `Failure(QuranError.InvalidPageCount)`.
- **Does NOT replace** prior sessions — all are stored separately; daily total is always computed by summing all sessions for a date.

### `ObserveQuranStateUseCase`
- **Input:** `date: LocalDate`
- **Output:** `Flow<QuranScreenState>` (domain model, not UI state)
- **Behaviour:** Combines 3 `Flow`s: `QuranGoalRepository.observeGoal()`, `QuranDailyLogRepository.observeLogsForDate(date)`, `QuranBookmarkRepository.observeBookmark()` → emits a merged `QuranScreenState(pagesReadToday, goalPages, bookmark, recentLogs)` on any change.
- **Pattern:** mirrors `PrayerHabitRepositoryImpl`'s `combine(habitFlow, logFlow)` pattern.

### `UpdateBookmarkUseCase`
- **Input:** `surahNumber: Int, ayahNumber: Int`
- **Output:** `EmptyResult<QuranError>`
- **Validates:** `surahNumber in 1..114`, `ayahNumber in 1..ALL_SURAHS[surahNumber-1].ayahCount`.

### `SetGoalUseCase`
- **Input:** `pagesPerDay: Int`
- **Output:** `EmptyResult<QuranError>`
- **Validates:** `pagesPerDay in 1..604`.

### `ComputeStreakUseCase`
- **Input:** `today: LocalDate`
- **Output:** `suspend fun invoke(): Int` (not a Flow — streak is computed once on demand, not observed live; the ViewModel calls it on init and after each log)
- **Algorithm:**
  1. Fetch all distinct dates where at least one log exists, ordered descending.
  2. Walk backwards from `yesterday` (not today — spec FR-013): count consecutive days with `sum(pages) >= 1`.
  3. Stop at first gap.
  - Today is excluded from the count until the day closes (spec clarification).

**Rationale for `suspend` not `Flow`:** Streak is a computed aggregate, not a reactive entity. Recomputing on every DB change would be expensive. The ViewModel recomputes after `LogReadingUseCase` succeeds.

---

## 5. ViewModel Pattern Decision

**Decision:** Use the existing `MviViewModel<S, A, E>` base class from `shared:core:presentation`.

**Rationale:** The habits module uses it; it provides `reduce {}`, `intent {}`, `exclusiveIntent {}`, `emitEvent()` out of the box. The prayer module skips it (manual `MutableStateFlow`) — that's an older pattern. The quran module should follow the habits module's more idiomatic pattern.

**Date-reactive observation:** Adopt the prayer module's `state.collectLatest { currentState -> useCase(currentState.selectedDate).collect { } }` pattern inside `init`. This automatically re-subscribes `ObserveQuranStateUseCase` when the date strip selection changes — without additional wiring in `onAction`.

**State shape:**
```kotlin
data class QuranUiState(
    val selectedDate: LocalDate = /* today */,
    val dateStrip: List<LocalDate> = emptyList(),   // 7 days [today-6 … today]; today is rightmost; no future dates
    val today: LocalDate = selectedDate,
    val pagesReadToday: Int = 0,
    val goalPages: Int = 5,
    val bookmark: QuranBookmark? = null,
    val recentLogs: List<QuranLogUiModel> = emptyList(),
    val streak: Int = 0,
    val isLoading: Boolean = false,
    val logReadingSheetVisible: Boolean = false,
    val setGoalSheetVisible: Boolean = false,
    val updatePositionSheetVisible: Boolean = false,
    val logReadingPageInput: Int = 0,
) {
    val isReadOnly: Boolean get() = selectedDate != today
    val progressFraction: Float get() = if (goalPages > 0) (pagesReadToday / goalPages.toFloat()).coerceIn(0f, 1f) else 0f
}
```

---

## 6. Bottom Sheet Strategy

All three sheets follow the existing `MarkMissedBottomSheet` pattern: visibility is driven by a Boolean field in `QuranUiState`; show/hide via `Action` dispatch.

| Sheet | Trigger Action | Dismiss Action | Save Action |
|-------|----------------|----------------|-------------|
| `LogReadingSheet` | `OpenLogReadingSheet` | `DismissLogReadingSheet` | `ConfirmLogReading(pages: Int)` |
| `SetGoalSheet` | `OpenSetGoalSheet` | `DismissSetGoalSheet` | `ConfirmSetGoal(pages: Int)` |
| `UpdatePositionSheet` | `OpenUpdatePositionSheet` | `DismissUpdatePositionSheet` | `ConfirmUpdatePosition(surah: Int, ayah: Int)` |

The `UpdatePositionSheet` opens from two locations: the "Resume Reading" card on the main screen AND the "Current Position" row inside `LogReadingSheet`. Both dispatch `OpenUpdatePositionSheet`. When returning from `UpdatePositionSheet` the previous sheet (if any) should be re-opened. This is handled by adding an `originSheet: SheetOrigin` to the state (either `MAIN` or `LOG_READING`).

---

## 7. Navigation Wiring Decision

**Decision:** Add `quranScreen: @Composable () -> Unit` lambda to `MudawamaAppShell`, replacing `entry<QuranRoute> { QuranPlaceholderScreen() }` with `entry<QuranRoute> { quranScreen() }` — identical to how `prayerScreen` and `habitsScreen` are wired.

**Caller:** `androidApp/MainActivity.kt` (or equivalent iOS entry) will pass `{ QuranScreen() }` as the lambda.

---

## 8. String Resources

All new strings for the Quran feature go in the **single** `shared/designsystem/src/commonMain/composeResources/values/strings.xml`.

Key string keys to add (naming convention: `quran_<element>_<type>`):

| Key | Value (EN) |
|-----|-----------|
| `quran_screen_title` | `"Quran Reading"` |
| `quran_daily_progress_label` | `"Daily Progress"` |
| `quran_daily_progress_subtitle_in_progress` | `"Almost there, stay focused."` |
| `quran_daily_progress_subtitle_complete` | `"Goal reached! MashaAllah."` |
| `quran_of_pages_format` | `"%1$d OF %2$d PAGES"` |
| `quran_goal_card_badge` | `"ACTIVE GOAL"` |
| `quran_goal_card_title_format` | `"Goal: %1$d Pages"` |
| `quran_goal_card_subtitle` | `"Establish a consistent connection with the Word of Allah today."` |
| `quran_log_reading_button` | `"Log Reading"` |
| `quran_resume_reading_label` | `"RESUME READING"` |
| `quran_resume_reading_no_position` | `"Tap to set your reading position"` |
| `quran_resume_reading_ayah_format` | `"Ayah %1$d"` |
| `quran_recent_logs_title` | `"Recent Logs"` |
| `quran_recent_logs_view_all` | `"VIEW ALL"` |
| `quran_log_status_over_goal` | `"OVER GOAL"` |
| `quran_log_status_under_goal` | `"UNDER GOAL"` |
| `quran_log_status_hit_goal` | `"HIT GOAL"` |
| `quran_log_reading_sheet_title` | `"Log Reading"` |
| `quran_log_reading_session_label` | `"PAGES READ THIS SESSION"` |
| `quran_log_reading_done_button` | `"Done"` |
| `quran_log_reading_chip_one_page` | `"+1 Page"` |
| `quran_log_reading_chip_five_pages` | `"+5 Pages"` |
| `quran_log_reading_chip_one_juz` | `"1 Juz"` |
| `quran_log_reading_current_position_label` | `"CURRENT POSITION"` |
| `quran_goal_sheet_title` | `"Daily Quran Goal"` |
| `quran_goal_sheet_pages_per_day` | `"PAGES PER DAY"` |
| `quran_goal_sheet_hint` | `"Setting a sustainable goal helps build a lifelong habit with the Quran. Most people start with 5 pages."` |
| `quran_goal_sheet_popular_goals` | `"POPULAR GOALS"` |
| `quran_goal_sheet_one_page` | `"1 Page"` |
| `quran_goal_sheet_five_pages` | `"5 Pages"` |
| `quran_goal_sheet_ten_pages` | `"10 Pages"` |
| `quran_goal_sheet_one_juz` | `"1 Juz"` |
| `quran_goal_sheet_save_button` | `"Save"` |
| `quran_position_sheet_title` | `"Update Position"` |
| `quran_position_sheet_done_button` | `"Done"` |
| `quran_position_sheet_search_hint` | `"Search Surah"` |
| `quran_position_sheet_verse_label` | `"VERSE (AYAH)"` |

---

## 9. Resolved Unknowns Summary

| Unknown | Resolution |
|---------|------------|
| Existing DB entities for Quran | `QuranBookmarkEntity` + `QuranBookmarkDao` exist (version 2). Scope: keep bookmark columns, add `QuranGoalEntity` + `QuranDailyLogEntity`, bump to version 3. |
| `QuranGoalEntity` storage (spec A-008) | New Room table `quran_goals`, singleton row `id=1`, `INSERT OR REPLACE` via `@Upsert`. |
| `QuranBookmark` singleton upsert (spec A-009) | `@Upsert` on existing `QuranBookmarkDao` — already implemented. |
| Module naming / build.gradle.kts pattern | Mirror `:feature:prayer:*` exactly. |
| MVI base class | Use `MviViewModel` (habits pattern), not manual `MutableStateFlow` (prayer pattern). |
| Date strip range | 7 days `(-3..3)` from today, matching prayer. Future dates (+1..+3) are also read-only. |
| Streak "today" boundary | Streak walks from `yesterday` backward. Today excluded until midnight (spec FR-013). |
| `safeCall` usage | Use in data layer for all DB writes (consistent with constitution). |
| Navigation injection point | `MudawamaAppShell` — add `quranScreen` lambda, replace `QuranPlaceholderScreen`. |
| String resource location | Single `strings.xml` in `shared:designsystem`. |
| Juz = pages | 1 Juz = 20 pages (spec A-001). |
