# Data Model: 009 Home Dashboard

No new database entities, DAOs, repositories, or network calls are introduced. This feature is
presentation-layer only.

This document describes the **new and modified in-memory data structures** (UiState, Actions,
Events) that drive the Home screen, and how they relate to existing domain models.

---

## New: `HomeUiState`

**File:** `feature/home/presentation/src/commonMain/.../home/presentation/model/HomeUiState.kt`

```
HomeUiState
├── habits: List<HabitWithStatus>               // unchanged — habits list
├── isLoading: Boolean                          // unchanged — habits loading flag
├── bottomSheetMode: BottomSheetMode            // unchanged — sheet/dialog state
├── errorMessage: String?                       // unchanged
│
├── [NEW] nextPrayer: PrayerWithStatus?         // null = loading / no pending prayer today
├── [NEW] isPrayerLoading: Boolean = true
├── [NEW] prayerTimesAvailable: Boolean = false // false → show "unavailable" fallback on card
│
├── [NEW] athkarStatus: Map<AthkarGroupType, Boolean> = emptyMap()
│         // keys: MORNING, EVENING only (POST_PRAYER excluded from summary card)
│         // true = group complete today, false = incomplete, absent = not yet started
├── [NEW] isAthkarLoading: Boolean = true
│
├── [NEW] quranPagesReadToday: Int = 0
├── [NEW] quranGoalPages: Int = 5
├── [NEW] isQuranLoading: Boolean = true
│
├── [NEW] tasbeehDailyTotal: Int = 0            // cumulative flushed count for today
├── [NEW] tasbeehGoal: Int = 100               // user's Tasbeeh target
└── [NEW] isTasbeehLoading: Boolean = true

Derived properties (no storage):
  progressFraction: Float
    = if (quranGoalPages > 0) (quranPagesReadToday / quranGoalPages.toFloat()).coerceIn(0f, 1f)
      else 0f

  tasbeehProgressFraction: Float
    = if (tasbeehGoal > 0) (tasbeehDailyTotal / tasbeehGoal.toFloat()).coerceIn(0f, 1f)
      else 0f
```

### Validation rules

| Field | Constraint |
|---|---|
| `quranGoalPages` | Set by `SetGoalUseCase` which rejects `< 1`; defensively treated as `5` (default) if `0` |
| `tasbeehGoal` | Set by `TasbeehGoalUseCase` which rejects `< 1`; default `100` |
| `athkarStatus` absent key | Treated as `NotStarted` (renders "Tap to get started") rather than `false` (incomplete) |
| `nextPrayer == null` when `isPrayerLoading == false` | All 5 prayers completed today — card shows "All prayers done" |
| `prayerTimesAvailable == false` | Card shows "Prayer times unavailable" fallback |

---

## New: `HomeUiAction`

**File:** `feature/home/presentation/src/commonMain/.../home/presentation/model/HomeUiAction.kt`

```
HomeUiAction (sealed interface)
│
├── [ALL EXISTING unchanged]
│   AddHabitFabClicked, HabitLongPressed, EditHabitSelected, DeleteHabitSelected,
│   DeleteConfirmed, SaveHabit, ToggleCompletion, IncrementCount, DecrementCount,
│   ResetTodayProgress, DismissBottomSheet, DismissError
│
└── [NEW — nested in sealed interface Navigate]
    NavigateToPrayer                    // user taps Next Prayer card
    NavigateToAthkar                    // user taps Athkar card
    NavigateToQuran                     // user taps Quran card
    NavigateToSettings                  // user taps Settings icon in top bar
    NavigateToHabits                    // user taps "View All" in Habits section
    NavigateToTasbeeh                   // user taps Tasbeeh card
```

---

## New: `HomeUiEvent`

**File:** `feature/home/presentation/src/commonMain/.../home/presentation/model/HomeUiEvent.kt`

```
HomeUiEvent (sealed interface)
│
├── ShowSnackbar(message: StringResource)     // unchanged
└── [NEW] sealed interface Navigate : HomeUiEvent
          // Consumed by HomeScreen via ObserveAsEvents
          // HomeScreen maps each subtype to the corresponding () -> Unit callback
          // NO Route types — feature:home:presentation has no shared:navigation dep
          ├── ToPrayer   : Navigate
          ├── ToAthkar   : Navigate
          ├── ToQuran    : Navigate
          ├── ToSettings : Navigate
          ├── ToHabits   : Navigate
          └── ToTasbeeh  : Navigate
```

---

## New: `SettingsRoute` (in `shared:navigation`)

**File:** `shared/navigation/src/commonMain/.../navigation/Routes.kt`

```
@Serializable data object SettingsRoute : Route
```

Added alongside the existing five routes. NOT added to `BottomNavItem` (Settings is not a tab).
Must be registered in `MudawamaAppShell`'s `SerializersModule` and `entryProvider`.

---

## Domain models consumed (read-only, no modification)

| Domain Model | Source Module | Used by `HomeUiState` field |
|---|---|---|
| `PrayerWithStatus` | `feature:prayer:domain` | `nextPrayer` |
| `PrayerName` (enum) | `feature:prayer:domain` | via `PrayerWithStatus.name` |
| `LogStatus` (enum) | `feature:habits:domain` | via `PrayerWithStatus.status` |
| `AthkarGroupType` (enum) | `feature:athkar:domain` | `athkarStatus` map key |
| `TasbeehGoal` | `feature:athkar:domain` | `tasbeehGoal` |
| `TasbeehDailyTotal` | `feature:athkar:domain` | `tasbeehDailyTotal` |
| `Coordinates` | `shared:core:domain` | `HomeViewModel` location resolution |

---

## State transitions

```
App launch
  └─► HomeViewModel.init()
        ├─► observeHabitsUseCase().collect         → reduce(habits, isLoading=false)
        ├─► resolveLocation()
        │     └─► locationProvider.getCurrentLocation()
        │           ├─ Success → currentCoordinates set
        │           └─ Failure → fallback to Mecca (21.3891, 39.8579)
        ├─► observePrayersForDateUseCase(today, coords).collect
        │     ├─ Success → nextPendingFromNow(prayers) → reduce(nextPrayer, prayerTimesAvailable=true, isPrayerLoading=false)
        │     └─ Failure → reduce(prayerTimesAvailable=false, isPrayerLoading=false)
        ├─► observeAthkarCompletionUseCase(today.toString()).collect  // String date
        │     └─► reduce(athkarStatus = map[MORNING, EVENING], isAthkarLoading=false)
        ├─► observeQuranStateUseCase(today).collect
        │     └─► reduce(quranPagesReadToday, quranGoalPages, isQuranLoading=false)
        ├─► observeTasbeehGoalUseCase().collect
        │     └─► reduce(tasbeehGoal, isTasbeehLoading=false)
        └─► observeTasbeehDailyTotalUseCase(today.toString()).collect
              └─► reduce(tasbeehDailyTotal)

User taps Next Prayer card
  └─► onAction(HomeUiAction.NavigateToPrayer)
        └─► emitEvent(HomeUiEvent.Navigate.ToPrayer)
              └─► HomeScreen: onNavigateToPrayer()
                    └─► MudawamaAppShell: backStack.clear(); backStack.add(PrayerRoute)

User taps Tasbeeh card
  └─► onAction(HomeUiAction.NavigateToTasbeeh)
        └─► emitEvent(HomeUiEvent.Navigate.ToTasbeeh)
              └─► HomeScreen: onNavigateToTasbeeh()
                    └─► MudawamaAppShell: backStack.add(TasbeehRoute)  [push — no clear]

User taps Settings icon
  └─► onAction(HomeUiAction.NavigateToSettings)
        └─► emitEvent(HomeUiEvent.Navigate.ToSettings)
              └─► HomeScreen: onNavigateToSettings()
                    └─► MudawamaAppShell: backStack.add(SettingsRoute)  [push — no clear]

User presses back from Settings / Tasbeeh / Habits
  └─► AppBackHandler triggers goHome()
        └─► backStack.clear(); backStack.add(HomeRoute)
```

---

## String keys (new, added to `shared/designsystem/.../strings.xml`)

All new keys follow `snake_case` scoped by screen/component:

| Key | Usage |
|---|---|
| `home_next_prayer_label` | "NEXT PRAYER" section label on the card |
| `home_next_prayer_unavailable` | Fallback when prayer times cannot be loaded |
| `home_all_prayers_done` | Shown when all 5 prayers are complete for today |
| `home_athkar_morning_label` | "Morning" label in Athkar card |
| `home_athkar_evening_label` | "Evening" label in Athkar card |
| `home_athkar_not_started` | "Tap to get started" for never-opened Athkar |
| `home_athkar_done` | "Done" status label |
| `home_athkar_pending` | "Pending" status label |
| `home_quran_pages_progress` | "%1$d / %2$d pages" format string |
| `home_quran_label` | "Quran" card label |
| `home_tasbeeh_label` | "TASBEEH" card label (NEW) |
| `home_tasbeeh_count_progress` | "%1$d / %2$d" format string for Tasbeeh daily/goal (NEW) |
| `home_daily_rituals_label` | "Daily Rituals" section header |
| `home_settings_icon_description` | Content description for settings icon button |
| `settings_placeholder_title` | "Settings" title on placeholder screen |
| `settings_placeholder_coming_soon` | "Coming soon" body text on placeholder |
