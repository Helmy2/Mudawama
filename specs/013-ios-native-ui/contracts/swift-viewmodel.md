# Contract: Swift ViewModel ↔ Kotlin Use Case

**Feature**: `013-ios-native-ui`  
**Scope**: The interface contract each Swift ViewModel must satisfy when consuming Kotlin use cases  
**Date**: 2026-04-15

---

## Canonical ViewModel Contract

Every SwiftUI ViewModel in the iOS app must follow this contract:

```
ViewModel
  ├── Instantiates: one XxxUseCaseProvider (KoinComponent — lazy Koin resolution)
  ├── Publishes:    @Published state (single value, starts in loading state)
  ├── Activates:    func observe() async  — called from .task{} in View
  ├── Actions:      func doXxx() async    — one func per user action (calls suspend use case)
  └── Teardown:     automatic — .task{} cancels Kotlin coroutines on view disappear
```

---

## Per-Feature Contracts

### Prayer

```
Input:    date: LocalDate (today by default)
Observe:  ObservePrayersForDateUseCase(date) → Flow<List<PrayerWithStatus>>
Actions:
  - togglePrayerStatus(habitId: String, date: LocalDate)  → TogglePrayerStatusUseCase
  - seedPrayerHabits()                                    → SeedPrayerHabitsUseCase
State:    [loading | List<PrayerWithStatus> | error(key: String)]
```

### Quran

```
Input:    date: LocalDate (today by default)
Observe:  ObserveQuranProgressUseCase(date) → Flow<QuranProgress>
Actions:
  - logReading(pages: Int, date: LocalDate)      → LogQuranReadingUseCase
  - observeStreak()                              → ObserveReadingStreakUseCase (secondary flow)
State:    [loading | QuranProgress | error(key: String)]
```

### Athkar

```
Input:    type: AthkarType, date: LocalDate
Observe:  ObserveAthkarGroupsUseCase(type, date) → Flow<List<AthkarGroup>>
Actions:
  - markGroupComplete(groupId: String, date: LocalDate) → MarkAthkarGroupCompleteUseCase
State:    [loading | List<AthkarGroup> | error(key: String)]
```

### Tasbeeh

```
Observe:  ObserveTasbeehGoalUseCase()        → Flow<TasbeehGoal?>
          ObserveTasbeehDailyTotalUseCase(date) → Flow<TasbeehDailyTotal?>
Actions:
  - increment(date: LocalDate)               → IncrementTasbeehUseCase
  - setGoal(count: Int)                      → SetTasbeehGoalUseCase
State:    [loading | TasbeehUiState(goal, total) | error(key: String)]
```

### Habits

```
Input:    date: LocalDate
Observe:  ObserveHabitsWithLogsUseCase(date) → Flow<List<HabitWithLog>>
Actions:
  - toggleLog(habitId: String, date: LocalDate) → ToggleHabitLogUseCase
State:    [loading | List<HabitWithLog> | error(key: String)]
```

### Settings

```
Observe:  ObserveSettingsUseCase() → Flow<AppSettings>
Actions:  (all suspend, no return value)
  - setCalculationMethod(CalculationMethod)
  - setLocationMode(LocationMode)
  - setAppTheme(AppTheme)
  - setAppLanguage(AppLanguage)
  - setMorningNotification(enabled: Bool, hour: Int, minute: Int)
  - setEveningNotification(enabled: Bool, hour: Int, minute: Int)
State:    [loading | AppSettings | error(key: String)]
```

### Qibla

```
Observe:  CLLocationManager heading (Swift — continuous updates from IosCompassManager)
One-shot: CalculateQiblaAngleUseCase.invoke(lat, lng) → Double  (called once on location fix)
State:    [requestingPermission | calibrating | active(compassDegrees, qiblaDegrees, accuracy) | error(key)]
```

### Home

```
Observe (parallel, zipped or combined in Swift):
  - GetNextPrayerTimeUseCase()                     → suspend → NextPrayerInfo
  - ObserveAthkarGroupsUseCase(MORNING+EVENING, today) → Flow<List<AthkarGroup>>
  - ObserveQuranProgressUseCase(today)             → Flow<QuranProgress>
  - ObserveTasbeehDailyTotalUseCase(today)         → Flow<TasbeehDailyTotal?>
  - ObserveHabitsUseCase(today)                    → Flow<List<HabitWithLog>>
State:    HomeUiState { nextPrayer, athkarSummary, quranProgress, tasbeehTotal, habitsSummary }
```

---

## Error Handling Contract

All Kotlin use cases return `Result<D, E>` (custom sealed class). SKIE exposes this as a sealed class hierarchy. Swift handles it as:

```swift
// Pattern for suspend use cases returning Result<D,E>
func doAction() async {
    let result = try await useCases.someUseCase.invoke(...)
    if let success = result as? ResultSuccess<SomeData, SomeError> {
        self.state = .success(success.data)
    } else if let failure = result as? ResultFailure<SomeData, SomeError> {
        self.state = .error(errorKey(failure.error))
    }
}

// Helper — maps DomainError to a localised string key
private func errorKey(_ error: DomainError) -> String {
    switch error {
    case is PrayerError.NetworkError: return "error_network"
    case is PrayerError.LocationError: return "error_location"
    default: return "error_generic"
    }
}
```

---

## Navigation Contract

Navigation is entirely managed by Swift. There is no Kotlin navigation involvement.

```
TabView tabs (always visible on top-level screens):
  Tab 0: Home     → HomeView      (NavigationStack root)
  Tab 1: Prayers  → PrayerView    (NavigationStack root)
  Tab 2: Quran    → QuranView     (NavigationStack root)
  Tab 3: Athkar   → AthkarView    (NavigationStack root)

Push destinations (tab bar hidden):
  HomeView  → NavigationLink → HabitsView
  HomeView  → NavigationLink → TasbeehView
  HomeView  → NavigationLink → QiblaView
  Any tab   → NavigationLink → SettingsView

Sheet presentations:
  PrayerView    → sheet → (no bottom sheets defined for prayer yet)
  QuranView     → sheet → LogReadingSheet, GoalSheet, PositionSheet
  AthkarView    → sheet → NotificationSettingsSheet
  TasbeehView   → sheet → TasbeehGoalSheet
  HabitsView    → sheet → NewHabitSheet, ManageHabitSheet
```
