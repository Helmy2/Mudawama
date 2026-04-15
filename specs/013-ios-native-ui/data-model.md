# Data Model: Full Native iOS App (SwiftUI)

**Feature**: `013-ios-native-ui`  
**Date**: 2026-04-15

This document captures the entities, state shapes, and data flows that cross the Kotlin→Swift boundary. No new Kotlin entities or Room entities are introduced by this migration. All Kotlin domain models are consumed read-only by SwiftUI ViewModels.

---

## 1. Kotlin Domain Models Consumed by Swift (per feature)

### 1.1 Prayer

| Kotlin type | Package | Fields | Swift ViewModel uses |
|---|---|---|---|
| `PrayerWithStatus` | `prayer.domain.model` | `prayerName: PrayerName`, `habitId: String`, `status: LogStatus`, `prayerTime: PrayerTime?` | `PrayerSwiftViewModel.state` |
| `PrayerName` | `prayer.domain.model` | enum: FAJR, DHUHR, ASR, MAGHRIB, ISHA | Displayed as localised string via key |
| `PrayerTime` | `prayer.domain.model` | `hour: Int`, `minute: Int` | Formatted time string |
| `LogStatus` | `core.domain.model` | enum: PENDING, DONE, MISSED | Maps to iOS UI icon/colour |
| `PrayerError` | `prayer.domain.error` | sealed: NetworkError, LocationError, DatabaseError, GenericError | Maps to iOS error message key |

**StateFlow shape consumed**:
```
ObservePrayersForDateUseCase → Flow<List<PrayerWithStatus>>
```

---

### 1.2 Quran

| Kotlin type | Package | Fields | Swift ViewModel uses |
|---|---|---|---|
| `QuranDailyLog` | `quran.domain` | `date: LocalDate`, `pagesRead: Int` | Progress display |
| `QuranGoal` | `quran.domain` | `dailyGoalPages: Int` | Goal ring / progress |
| `QuranBookmark` | `quran.domain` | `surahNumber: Int`, `ayahNumber: Int`, `surahName: String` | Current position display |
| `ReadingStreak` | `quran.domain` | `currentStreak: Int`, `longestStreak: Int` | Streak display |

**Flows consumed**:
```
ObserveQuranProgressUseCase → Flow<QuranProgress>  (aggregates log + goal + streak)
```

---

### 1.3 Athkar

| Kotlin type | Package | Fields | Swift ViewModel uses |
|---|---|---|---|
| `AthkarGroup` | `athkar.domain` | `id: String`, `title: String`, `type: AthkarType`, `items: List<AthkarItem>`, `isCompleted: Boolean` | Group list display |
| `AthkarItem` | `athkar.domain` | `id: String`, `arabic: String`, `translation: String`, `count: Int`, `currentCount: Int` | Individual dhikr view |
| `AthkarType` | `athkar.domain` | enum: MORNING, EVENING, POST_PRAYER | Tab/section selector |
| `AthkarDailyLog` | `athkar.domain` | `date: LocalDate`, `groupId: String`, `completed: Boolean` | Completion state |

**Flows consumed**:
```
ObserveAthkarGroupsUseCase(type, date) → Flow<List<AthkarGroup>>
```

---

### 1.4 Tasbeeh

| Kotlin type | Package | Fields | Swift ViewModel uses |
|---|---|---|---|
| `TasbeehGoal` | `athkar.domain` | `dailyGoal: Int` | Goal ring |
| `TasbeehDailyTotal` | `athkar.domain` | `date: LocalDate`, `total: Int` | Counter display |

**Flows consumed**:
```
ObserveTasbeehGoalUseCase     → Flow<TasbeehGoal?>
ObserveTasbeehDailyTotalUseCase(date) → Flow<TasbeehDailyTotal?>
```

---

### 1.5 Habits

| Kotlin type | Package | Fields | Swift ViewModel uses |
|---|---|---|---|
| `Habit` | `habits.domain` | `id: String`, `name: String`, `type: HabitType`, `targetCount: Int?` | Habit list row |
| `HabitLog` | `habits.domain` | `habitId: String`, `date: LocalDate`, `status: LogStatus`, `count: Int?` | Completion toggle state |
| `HabitType` | `habits.domain` | enum: BOOLEAN, NUMERIC | Toggle vs. counter UI |

**Flows consumed**:
```
ObserveHabitsWithLogsUseCase(date) → Flow<List<HabitWithLog>>
```

---

### 1.6 Settings

| Kotlin type | Package | Fields | Swift ViewModel uses |
|---|---|---|---|
| `AppSettings` | `settings.domain` | `calculationMethod: CalculationMethod`, `locationMode: LocationMode`, `appTheme: AppTheme`, `appLanguage: AppLanguage`, `morningNotifEnabled: Boolean`, `eveningNotifEnabled: Boolean`, `morningNotifHour: Int`, `morningNotifMinute: Int`, `eveningNotifHour: Int`, `eveningNotifMinute: Int`, `dynamicThemeEnabled: Boolean` | Full settings screen |
| `CalculationMethod` | `settings.domain` | enum (MWL, ISNA, Egypt, Makkah, Karachi, Tehran, Jafari) | Picker |
| `LocationMode` | `settings.domain` | enum: AUTO, MANUAL | Toggle |
| `AppTheme` | `settings.domain` | enum: SYSTEM, LIGHT, DARK | Picker |
| `AppLanguage` | `settings.domain` | enum: SYSTEM, ENGLISH, ARABIC | Picker |

**Flows consumed**:
```
ObserveSettingsUseCase → Flow<AppSettings>
```

---

### 1.7 Qibla

| Kotlin type | Package | Fields | Swift ViewModel uses |
|---|---|---|---|
| `QiblaState` | `qibla.domain` | `qiblaAngle: Double`, `compassHeading: Double`, `accuracy: CompassAccuracy` | Compass rendering |
| `CompassAccuracy` | `qibla.domain` | enum: HIGH, MEDIUM, LOW, UNRELIABLE | Calibration warning |
| `QiblaError` | `qibla.domain` | sealed: LocationPermissionDenied, LocationUnavailable, SensorUnavailable | Error state |
| `CalculateQiblaAngleUseCase` | `qibla.domain` | `invoke(lat, lng): Double` | One-shot computation |

**Data flow**: `CLLocationManager` heading → Swift ViewModel → combined with `CalculateQiblaAngleUseCase` result → `QiblaUiState`.

---

### 1.8 Home Dashboard (aggregated)

The Home screen aggregates from multiple features. The Swift `HomeSwiftViewModel` composes multiple flows:

| Source | Data shown |
|---|---|
| `GetNextPrayerTimeUseCase` | Next prayer name + time |
| `ObserveTasbeehDailyTotalUseCase` | Tasbeeh count today vs. goal |
| `ObserveQuranProgressUseCase` | Pages read today vs. goal |
| `ObserveAthkarGroupsUseCase` | Morning/Evening completion status |
| `ObserveHabitsUseCase` | Habits done today / total |

---

## 2. iOS-Side State Shapes (Swift)

Each SwiftUI ViewModel exposes a single `@Published` state value. State shapes mirror the Kotlin domain models but are Swift structs (decoded from Kotlin types via SKIE interop).

### Generic ViewModel State Pattern

```swift
// Pattern used across all 8 ViewModels
enum LoadState<T> {
    case loading
    case success(T)
    case failure(String)   // localised error message key
}
```

No new persistent entities are introduced on the iOS side — all persistence goes through Kotlin Room via use cases.

---

## 3. Entity Relationships (Unchanged)

The entity-relationship graph is defined entirely in the Kotlin domain and data layers. This migration does not add, remove, or alter any entity or relationship. See `docs/ARCHITECTURE.md` §3 and the Room schema at version 4 for the canonical source.

---

## 4. KoinComponent Provider → Use Case Mapping

| Provider class (iosMain) | Use cases exposed | Feature |
|---|---|---|
| `HomeUseCaseProvider` | GetNextPrayerTimeUseCase, ObserveAthkarSummaryUseCase, ObserveQuranProgressUseCase, ObserveTasbeehDailyTotalUseCase, ObserveHabitsUseCase | Home |
| `PrayerUseCaseProvider` | ObservePrayersForDateUseCase, TogglePrayerStatusUseCase, SeedPrayerHabitsUseCase | Prayer |
| `QuranUseCaseProvider` | ObserveQuranProgressUseCase, LogQuranReadingUseCase, ObserveReadingStreakUseCase | Quran |
| `AthkarUseCaseProvider` | ObserveAthkarGroupsUseCase, MarkAthkarGroupCompleteUseCase | Athkar |
| `TasbeehUseCaseProvider` | ObserveTasbeehGoalUseCase, IncrementTasbeehUseCase, ObserveTasbeehDailyTotalUseCase | Tasbeeh |
| `HabitsUseCaseProvider` | ObserveHabitsWithLogsUseCase, ToggleHabitLogUseCase | Habits |
| `SettingsUseCaseProvider` | ObserveSettingsUseCase, SetCalculationMethodUseCase, SetLocationModeUseCase, SetAppThemeUseCase, SetAppLanguageUseCase | Settings |
| `QiblaUseCaseProvider` | CalculateQiblaAngleUseCase | Qibla |
