# UI Contracts: Quran Tracking Feature (007)

**Branch**: `007-quran-tracking` | **Date**: 2026-04-10

This document defines the contracts between the Presentation layer and the UI composables — what state each component expects, what actions it emits, and what invariants hold. These contracts are technology-agnostic at the spec level but map directly to Compose Multiplatform `@Composable` function signatures.

---

## Screen: `QuranScreen`

**Route:** `QuranRoute` (Navigation3)  
**Reference UI:** `docs/ui/quran_daily_reading_tracker.png`

### Inputs (from ViewModel state)

| Field | Type | Invariant |
|-------|------|-----------|
| `selectedDate` | `LocalDate` | Any date in `dateStrip`; today by default |
| `dateStrip` | `List<LocalDate>` | Exactly 7 entries: `[today-6 … today]`; today is always the rightmost chip; no future dates |
| `today` | `LocalDate` | Device local date at ViewModel init; never changes during session |
| `pagesReadToday` | `Int` | `>= 0`; sum of all log sessions for `selectedDate` |
| `goalPages` | `Int` | `>= 1`; default 5 if no goal set |
| `progressFraction` | `Float` | `0f..1f`; computed `pagesReadToday / goalPages`, clamped |
| `bookmark` | `QuranBookmark?` | `null` = no position saved yet |
| `recentLogs` | `List<RecentLogEntry>` | Max 3 entries; most recent first; excludes `selectedDate` if past |
| `streak` | `Int` | `>= 0`; consecutive days with at least 1 page logged |
| `isReadOnly` | `Boolean` | `true` when `selectedDate != today` |
| `isLoading` | `Boolean` | Loading spinner replaces ring when `true` |
| `logReadingSheetVisible` | `Boolean` | Controls `LogReadingSheet` visibility |
| `setGoalSheetVisible` | `Boolean` | Controls `SetGoalSheet` visibility |
| `updatePositionSheetVisible` | `Boolean` | Controls `UpdatePositionSheet` visibility |

### Outputs (user actions → ViewModel)

| User Gesture | Action Dispatched |
|-------------|-------------------|
| Tap date chip in strip | `SelectDate(date)` |
| Tap "Log Reading" button (visible only when `!isReadOnly`) | `OpenLogReadingSheet` |
| Tap Goal card | `OpenSetGoalSheet` |
| Tap "Resume Reading" card | `OpenUpdatePositionSheet` |
| Confirm log in `LogReadingSheet` | `ConfirmLogReading(pages: Int)` |
| Dismiss `LogReadingSheet` | `DismissLogReadingSheet` |
| Save goal in `SetGoalSheet` | `ConfirmSetGoal(pages: Int)` |
| Dismiss `SetGoalSheet` | `DismissSetGoalSheet` |
| Confirm position in `UpdatePositionSheet` | `ConfirmUpdatePosition(surah: Int, ayah: Int)` |
| Dismiss `UpdatePositionSheet` | `DismissUpdatePositionSheet` |

### Read-Only Enforcement

When `isReadOnly == true`:
- "Log Reading" button is **hidden** (not just disabled)
- Goal card remains tappable (goal setting is not date-scoped)
- "Resume Reading" card remains tappable (bookmark is not date-scoped)
- `recentLogs` reflects data for `selectedDate` only

---

## Component: `QuranDateStrip`

**Reference:** `docs/ui/daily_prayer_tracker.png` (same pattern as prayer screen)

### Inputs

| Prop | Type | Invariant |
|------|------|-----------|
| `dates` | `List<LocalDate>` | Exactly 7 items: `[today-6 … today]`; today is rightmost; no future dates |
| `selectedDate` | `LocalDate` | Must be in `dates` |
| `today` | `LocalDate` | Must be in `dates` |
| `onDateSelected` | `(LocalDate) -> Unit` | Never null |

### Visual States per Chip

| Condition | Background | Text | Indicator dot |
|-----------|-----------|------|---------------|
| `date == selectedDate` | Primary fill | `onPrimary` | Below chip, `onPrimary` tint |
| `date == today` (not selected) | `surfaceContainer` | `onSurface` | Below chip, `primary` tint |
| Other | `surfaceContainer` | `onSurfaceVariant` | None |

---

## Component: `QuranProgressRing`

**Reference:** `docs/ui/quran_daily_reading_tracker.png` — large center card

### Inputs

| Prop | Type | Invariant |
|------|------|-----------|
| `pagesRead` | `Int` | `>= 0` |
| `goalPages` | `Int` | `>= 1` |
| `progressFraction` | `Float` | `0f..1f` |
| `isLoading` | `Boolean` | Replaces ring with spinner |

### Visual States

| Condition | Ring | Center Text | Subtitle |
|-----------|------|-------------|---------|
| `progressFraction < 1f` | Partial green arc | `"{N} OF {G} PAGES"` | Motivational (in-progress) |
| `progressFraction >= 1f` | Full green arc | `"{N} OF {G} PAGES"` | `"Goal reached! MashaAllah."` |
| `isLoading` | Spinner | — | — |

String keys: `quran_of_pages_format`, `quran_daily_progress_subtitle_in_progress`, `quran_daily_progress_subtitle_complete`

---

## Component: `QuranGoalCard`

**Reference:** `docs/ui/quran_daily_reading_tracker.png` — second card

### Inputs

| Prop | Type | Notes |
|------|------|-------|
| `goalPages` | `Int` | |
| `isReadOnly` | `Boolean` | When `true`, the "Log Reading" CTA inside the card is hidden |
| `onTap` | `() -> Unit` | Tapping the card body → opens `SetGoalSheet`; always active regardless of `isReadOnly` |
| `onLogReadingClick` | `() -> Unit` | Tapping the "Log Reading" CTA → dispatches `OpenLogReadingSheet`; only wired when `!isReadOnly` |

### Visual States

- Badge: `"ACTIVE GOAL"` always shown
- Title: `"Goal: {N} Pages"` (`quran_goal_card_title_format`)
- Subtitle: `"Establish a consistent connection…"` (`quran_goal_card_subtitle`)
- CTA button: `"Log Reading"` — **hidden** when `isReadOnly == true`; visible and tappable when `isReadOnly == false`

---

## Component: `QuranResumeReadingCard`

**Reference:** `docs/ui/quran_daily_reading_tracker.png` — third card

### Inputs

| Prop | Type | Invariant |
|------|------|-----------|
| `bookmark` | `QuranBookmark?` | `null` = no position set |
| `onTap` | `() -> Unit` | Opens `UpdatePositionSheet` |

### Visual States

| Condition | Content |
|-----------|---------|
| `bookmark != null` | Label `"RESUME READING"`, title: Surah name, subtitle: `"Ayah {N}"`, bookmark icon button |
| `bookmark == null` | Label `"RESUME READING"`, title: `quran_resume_reading_no_position`, no subtitle |

---

## Component: `QuranRecentLogsList`

**Reference:** `docs/ui/quran_daily_reading_tracker.png` — bottom section

### Inputs

| Prop | Type |
|------|------|
| `logs` | `List<RecentLogEntry>` (max 3) |
| `onViewAll` | `() -> Unit` |

### Per-Row State

| `LogStatus` | Status label | Status color |
|-------------|-------------|-------------|
| `OVER` | `"OVER GOAL"` | `primary` (teal) |
| `HIT` | `"HIT GOAL"` | `secondary` (success green) |
| `UNDER` | `"UNDER GOAL"` | `onSurfaceVariant` (neutral) |

Icon: filled checkmark circle for `OVER`/`HIT`; grey checkmark for `UNDER`.

---

## Bottom Sheet: `LogReadingSheet`

**Reference:** `docs/ui/log_reading_bottom_sheet.png`

### Inputs

| Prop | Type | Invariant |
|------|------|-----------|
| `pageInput` | `Int` | `0..604` |
| `goalPages` | `Int` | `>= 1` — for ring arc display |
| `bookmark` | `QuranBookmark?` | Shown in "Current Position" row |
| `onDismiss` | `() -> Unit` | |
| `onConfirm` | `(Int) -> Unit` | Called with final page count |
| `onUpdatePosition` | `() -> Unit` | Opens `UpdatePositionSheet` |
| `onPageChange` | `(Int) -> Unit` | Stepper change |

### Controls

| Control | Behaviour |
|---------|-----------|
| `−` button | `pageInput - 1`, floor 0 |
| `+` button | `pageInput + 1`, ceil 604 |
| `+1 Page` chip | `pageInput + 1` |
| `+5 Pages` chip | `pageInput + 5` |
| `1 Juz` chip | `pageInput + 20` |
| "Done" text button | Calls `onConfirm(pageInput)`; no-op if `pageInput == 0` |
| "Current Position" row | Shows bookmark; tap → `onUpdatePosition()` |

### Invariants

- Page counter never goes below 0
- Page counter never exceeds 604
- "Done" with `pageInput == 0` → dismiss without creating log

---

## Bottom Sheet: `SetGoalSheet`

**Reference:** `docs/ui/quran_reading_goal_bottom_sheet.png`

### Inputs

| Prop | Type |
|------|------|
| `currentGoal` | `Int` |
| `onDismiss` | `() -> Unit` |
| `onSave` | `(Int) -> Unit` |

### Controls

| Control | Behaviour |
|---------|-----------|
| `−` circle | `goal - 1`, floor 1 |
| `+` filled circle | `goal + 1`, ceil `MAX_DAILY_GOAL_PAGES` (= 60) |
| `"1 Page"` chip | Sets `goal = 1` |
| `"5 Pages"` chip (highlighted if current=5) | Sets `goal = 5` |
| `"10 Pages"` chip | Sets `goal = 10` |
| `"1 Juz"` chip | Sets `goal = 20` |
| "Save" text button | Calls `onSave(goal)` |
| `×` icon | Calls `onDismiss()` |

### Visual State

- Selected popular goal chip → Primary fill + white text
- All others → `surfaceContainerHigh` + `primary` text

---

## Bottom Sheet: `UpdatePositionSheet`

**Reference:** `docs/ui/select_surah_ayah_bottom_sheet.png`

### Inputs

| Prop | Type |
|------|------|
| `currentSurah` | `Int` (1..114) — pre-selected |
| `currentAyah` | `Int` |
| `allSurahs` | `List<SurahMetadata>` — always 114 items |
| `onDismiss` | `() -> Unit` |
| `onDone` | `(surah: Int, ayah: Int) -> Unit` |

### Controls

| Control | Behaviour |
|---------|-----------|
| Search field | Filters `allSurahs` by name (case-insensitive prefix/substring match) |
| Surah list item tap | Selects that Surah; resets Ayah to 1 if current Ayah > new Surah's max |
| Ayah number picker | Scroll picker, bounded 1..`selectedSurah.ayahCount`; shows total (`"of {N}"`) |
| "Done" text button | Calls `onDone(selectedSurah, selectedAyah)` |

### Invariants

- Selected Surah always highlighted with checkmark and `surfaceContainerLow` background
- Ayah picker maximum auto-updates on Surah change
- Ayah is clamped to `1..ayahCount` if the new Surah has fewer ayahs than current ayah value

---

## Koin DI Module Contracts

### `quranDomainModule` (top-level `val`, `feature:quran:domain`)
```
factory<CoroutineDispatcher> { Dispatchers.Default }
factoryOf(::LogReadingUseCase)
factoryOf(::ObserveQuranStateUseCase)
factoryOf(::UpdateBookmarkUseCase)
factoryOf(::SetGoalUseCase)
factoryOf(::ComputeStreakUseCase)
```

### `quranDataModule()` (function, `feature:quran:data`)
```
single<QuranDailyLogRepository> { QuranDailyLogRepositoryImpl(get()) }
single<QuranGoalRepository>     { QuranGoalRepositoryImpl(get()) }
single<QuranBookmarkRepository> { QuranBookmarkRepositoryImpl(get()) }
```
DAOs are resolved from `MudawamaDatabase` via existing `DatabaseModule` bindings:
`single<QuranDailyLogDao> { get<MudawamaDatabase>().quranDailyLogDao() }` etc.

### `quranPresentationModule()` (function, `feature:quran:presentation`)
```
viewModelOf(::QuranViewModel)
```

### Load Order (in Application)
```
coreDatabaseModule → quranDataModule() → quranDomainModule → quranPresentationModule()
```
