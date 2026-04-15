# iOS Native UI — Mudawama

**Spec**: 013-ios-native-ui  
**Status**: Complete (T001–T082 done; T083 manual simulator verification pending)  
**Platform**: iOS 15+ · Swift 5.10 · SwiftUI  
**KMP framework**: `MudawamaCore` (domain + data only) via SKIE 0.10.11

---

## 1. Overview

Spec 013 replaced the previous Compose Multiplatform iOS canvas with a fully native SwiftUI UI layer. The iOS app now:

- Contains **zero Compose / JetBrains runtime** — the `MudawamaUI` framework is Android-only and is never linked on iOS.
- Drives all UI with SwiftUI `View` + `ObservableObject` ViewModels.
- Calls Kotlin domain use cases through the `MudawamaCore` XCFramework (exported by `:shared:umbrella-core`), bridged by **SKIE** so Kotlin `Flow<T>` becomes Swift `AsyncSequence` and `suspend` becomes `async throws`.
- All business logic (prayer times, streak calculation, Qibla angle, etc.) remains in Kotlin — Swift ViewModels are thin orchestrators.

### Architecture layers

```
Swift (iosApp/)
  Views (SwiftUI)  ←  ViewModels (@MainActor ObservableObject)
                            ↓  calls
  KoinComponent Providers (IosKoinHelpers.kt — compiled into MudawamaCore)
                            ↓  injects
  Kotlin Use Cases  ←  Kotlin Repositories  ←  Room / Ktor / DataStore
```

---

## 2. Framework Setup

### 2.1 MudawamaCore XCFramework

Built by `:shared:umbrella-core`. Key Gradle config (`shared/umbrella-core/build.gradle.kts`):

```kotlin
plugins {
    alias(libs.plugins.skie)          // SKIE 0.10.11
}

kotlin {
    configureIosFramework("MudawamaCore", isStatic = true) {
        export(projects.feature.prayer.domain)
        export(projects.feature.prayer.data)
        export(projects.feature.quran.domain)
        export(projects.feature.quran.data)
        export(projects.feature.athkar.domain)
        export(projects.feature.athkar.data)
        export(projects.feature.tasbeeh.domain)
        export(projects.feature.tasbeeh.data)
        export(projects.feature.habits.domain)
        export(projects.feature.habits.data)
        export(projects.feature.settings.domain)
        export(projects.feature.settings.data)
        export(projects.feature.qibla.domain)
        export(projects.feature.qibla.data)
        export(projects.shared.core.domain)
        export(projects.shared.core.data)
        export(projects.shared.core.time)
        // Note: shared:core:database stays implementation() — not exported to Swift
    }
}
```

Build commands:
```bash
# Debug Simulator
./gradlew :shared:umbrella-core:linkDebugFrameworkIosSimulatorArm64

# Release device
./gradlew :shared:umbrella-core:linkReleaseFrameworkIosArm64
```

### 2.2 Koin initialisation

`KoinInitializer.kt` (`shared/umbrella-core/src/iosMain/kotlin/`) is called once from `iOSApp.swift`:

```swift
KoinInitializerKt.initializeKoin(
    iosEncryptor: IosEncryptor(),
    iosLocationProvider: IosLocationProvider(),
    iosNotificationProvider: IosNotificationProvider()
)
```

The three provider parameters are Swift classes that conform to Kotlin interfaces (`EncryptionProvider`, `LocationProvider`, `NotificationPermissionProvider`).

### 2.3 KoinComponent Providers

`IosKoinHelpers.kt` defines one `KoinComponent` class per feature. Each class exposes the use cases needed by that feature's Swift ViewModel:

| Provider class | Feature | Key use cases exposed |
|---|---|---|
| `HomeUseCaseProvider` | Home | `observePrayersForDateUseCase`, `observeAthkarCompletionUseCase`, `observeQuranStateUseCase`, `observeTasbeehGoalUseCase`, `observeTasbeehDailyTotalUseCase`, `observeHabitsWithTodayStatusUseCase`, `seedPrayerHabitsUseCase` |
| `PrayerUseCaseProvider` | Prayer | `observePrayersForDateUseCase`, `togglePrayerStatusUseCase`, `seedPrayerHabitsUseCase` |
| `QuranUseCaseProvider` | Quran | `observeQuranStateUseCase`, `logQuranReadingUseCase`, `setQuranGoalUseCase`, `updateQuranPositionUseCase` |
| `AthkarUseCaseProvider` | Athkar | `observeAthkarCompletionUseCase`, `observeAthkarLogUseCase`, `incrementAthkarItemUseCase`, `getAthkarGroupUseCase` |
| `TasbeehUseCaseProvider` | Tasbeeh | `observeTasbeehGoalUseCase`, `observeTasbeehDailyTotalUseCase`, `incrementTasbeehUseCase`, `setTasbeehGoalUseCase` |
| `HabitsUseCaseProvider` | Habits | `observeHabitsWithLogsUseCase`, `toggleHabitLogUseCase`, `addHabitUseCase`, `deleteHabitUseCase` |
| `SettingsUseCaseProvider` | Settings | `observeSettingsUseCase`, `setCalculationMethodUseCase`, `setLocationModeUseCase`, `setAppThemeUseCase`, `setAppLanguageUseCase`, `setMorningNotificationUseCase`, `setEveningNotificationUseCase` |
| `QiblaUseCaseProvider` | Qibla | `calculateQiblaAngleUseCase` |

Each provider is instantiated directly in the Swift ViewModel: `private let provider = PrayerUseCaseProvider()`.

---

## 3. Screen Inventory

| Screen | File | ViewModel | Tab / Push | Key published state |
|---|---|---|---|---|
| Home | `Features/Home/HomeView.swift` | `HomeViewModel` | Tab | `HomeUiState` (prayers, athkarCompletion, quranState, tasbeehGoal/total, habits, isLoading, errorMessage) |
| Prayer | `Features/Prayer/PrayerView.swift` | `PrayerViewModel` | Tab | `[PrayerWithStatus]`, `isLoading`, `errorMessage` |
| Quran | `Features/Quran/QuranView.swift` | `QuranViewModel` | Tab | `QuranScreenState?`, `isLoading`, `errorMessage` |
| Athkar | `Features/Athkar/AthkarView.swift` | `AthkarViewModel` | Tab | `selectedType`, `completionMap`, `counters`, `isLoading` |
| Habits | `Features/Habits/HabitsView.swift` | `HabitsViewModel` | Push | `[HabitWithLogs]`, `isLoading` |
| Tasbeeh | `Features/Tasbeeh/TasbeehView.swift` | `TasbeehViewModel` | Push | `goal`, `dailyTotal`, `isLoading` |
| Settings | `Features/Settings/SettingsView.swift` | `SettingsViewModel` | Push | `AppSettings`, `isLoading` |
| Qibla | `Features/Qibla/QiblaView.swift` | `QiblaViewModel` | Push | `QiblaUiState` (loading / active / permissionDenied / error), `isAligned` |

### Sheets

| Sheet | File | Presented from |
|---|---|---|
| Log Reading | `Features/Quran/Sheets/LogReadingSheet.swift` | `QuranView` |
| Quran Goal | `Features/Quran/Sheets/QuranGoalSheet.swift` | `QuranView` |
| Quran Position | `Features/Quran/Sheets/QuranPositionSheet.swift` | `QuranView` |
| New Habit | `Features/Habits/NewHabitSheet.swift` | `HabitsView` |
| Manage Habit | `Features/Habits/ManageHabitSheet.swift` | `HabitsView` (long-press / swipe) |
| Tasbeeh Goal | `Features/Tasbeeh/TasbeehGoalSheet.swift` | `TasbeehView` |
| Athkar Notifications | `Features/Athkar/AthkarNotificationSheet.swift` | `AthkarView` toolbar bell |

---

## 4. Navigation Architecture

### 4.1 Structure

```
RootNavigationView  (TabView — 4 tabs)
├── NavigationStack  →  HomeView
│     └── .navigationDestination  →  HabitsView, TasbeehView, SettingsView, QiblaView
├── NavigationStack  →  PrayerView
│     └── .navigationDestination  →  SettingsView, QiblaView (via toolbar)
├── NavigationStack  →  QuranView
│     └── .navigationDestination  →  SettingsView
└── NavigationStack  →  AthkarView
      └── .navigationDestination  →  SettingsView
```

Each tab has its own `NavigationPath` (`@State private var homePath = NavigationPath()` etc.) so each tab maintains an independent back-stack. Switching tabs does not reset navigation state.

### 4.2 Route enum

```swift
enum AppRoute: Hashable {
    case habits
    case tasbeeh
    case settings
    case qibla
}
```

Push destinations are triggered by `path.append(AppRoute.xxx)`. The `pushDestination(_:)` builder in `RootNavigationView` maps each case to its `View` and applies `.toolbar(.hidden, for: .tabBar)`.

### 4.3 Tab bar visibility

The tab bar is **hidden** on all four push destinations (`HabitsView`, `TasbeehView`, `SettingsView`, `QiblaView`) via `.toolbar(.hidden, for: .tabBar)` (iOS 16+).

### 4.4 Settings access

Every top-level tab (`PrayerView`, `QuranView`, `AthkarView`) has a trailing `gearshape.fill` toolbar button that pushes `AppRoute.settings`. `HomeView` uses a plain callback `onGoToSettings` which the shell wires to `homePath.append(.settings)`.

### 4.5 Home navigation

`HomeView` receives 7 plain `() -> Void` callbacks for navigation — it has **no dependency on any navigation framework**:

```swift
HomeView(
    onGoToPrayer:   { selectedTab = .prayer },
    onGoToAthkar:   { selectedTab = .athkar },
    onGoToQuran:    { selectedTab = .quran },
    onGoToHabits:   { homePath.append(AppRoute.habits) },
    onGoToTasbeeh:  { homePath.append(AppRoute.tasbeeh) },
    onGoToSettings: { homePath.append(AppRoute.settings) },
    onGoToQibla:    { homePath.append(AppRoute.qibla) }
)
```

---

## 5. Design System

Defined in `DesignSystem/MudawamaTheme.swift`.

### 5.1 Color tokens

| Token | Swift accessor | Value / adaptive |
|---|---|---|
| Primary teal | `MudawamaTheme.Colors.primary` | `Color(red: 0.0, green: 0.588, blue: 0.588)` |
| On-primary | `MudawamaTheme.Colors.onPrimary` | `.white` |
| Surface | `MudawamaTheme.Colors.surface` | `.secondarySystemGroupedBackground` (adaptive) |
| Background | `MudawamaTheme.Colors.background` | `.systemGroupedBackground` (adaptive) |
| Error | `MudawamaTheme.Colors.error` | `.red` |
| Done / success | `MudawamaTheme.Colors.done` | `.green` |
| Missed / warning | `MudawamaTheme.Colors.missed` | `.orange` |

Dark Mode is automatic — `.secondarySystemGroupedBackground` and `.systemGroupedBackground` are Apple semantic colors that adapt without any extra code.

### 5.2 Spacing tokens

| Token | Value |
|---|---|
| `MudawamaTheme.Spacing.xs` | 4 pt |
| `MudawamaTheme.Spacing.sm` | 8 pt |
| `MudawamaTheme.Spacing.md` | 16 pt |
| `MudawamaTheme.Spacing.lg` | 24 pt |
| `MudawamaTheme.Spacing.xl` | 32 pt |

### 5.3 Corner radius tokens

| Token | Value |
|---|---|
| `MudawamaTheme.Radius.card` | 16 pt |
| `MudawamaTheme.Radius.button` | 12 pt |
| `MudawamaTheme.Radius.chip` | 8 pt |

### 5.4 MudawamaSurfaceCard

Generic card container in `MudawamaTheme.swift`:

```swift
MudawamaSurfaceCard(action: { /* optional tap */ }) {
    // content
}
```

- Background: `.secondarySystemGroupedBackground`
- Corner radius: `MudawamaTheme.Radius.card` (16 pt)
- Shadow: `black.opacity(0.06)`, radius 2, y offset 1
- Default padding: 16 pt on all edges
- When `action` is non-nil, wraps content in a `Button` with `.plain` style

### 5.5 Tab bar SF Symbols

| Tab | SF Symbol |
|---|---|
| Home | `house.fill` |
| Prayers | `moon.stars.fill` |
| Quran | `book.fill` |
| Athkar | `heart.fill` |

Tab bar tint: `MudawamaTheme.Colors.primary`.

---

## 6. ViewModel Pattern

All ViewModels follow this canonical template:

```swift
@MainActor                                      // required — prevents @Published mutation crashes on iOS 17+
class ExampleViewModel: ObservableObject {

    @Published var items: [SomeType] = []
    @Published var isLoading = true
    @Published var errorMessage: String? = nil

    private let provider = ExampleUseCaseProvider()   // KoinComponent — injects use cases
    private var observeTask: Task<Void, Never>? = nil

    func observe() {
        observeTask?.cancel()
        isLoading = true
        errorMessage = nil
        observeTask = Task {
            for await result in provider.observeSomethingUseCase.invoke(...) {
                if Task.isCancelled { return }
                if let success = result as? ResultSuccess<AnyObject>,
                   let data = success.data as? [SomeType] {
                    self.items = data
                    self.isLoading = false
                } else if result is ResultFailure<AnyObject> {
                    self.errorMessage = String.loc("common_error_generic")
                    self.isLoading = false
                }
            }
        }
    }

    func doAction() {
        Task { _ = try? await provider.someActionUseCase.invoke(...) }
    }

    deinit { observeTask?.cancel() }
}
```

Key rules:
- `@MainActor` on every ViewModel class — mandatory.
- `for await` on SKIE-bridged Flows is **non-throwing** — never use `for try await`.
- Kotlin `Result<D,E>` must be cast as `ResultSuccess<AnyObject>` / `ResultFailure<AnyObject>` — the `<AnyObject>` annotation is required.
- Kotlin `suspend` functions bridged by SKIE may throw — always call via `try? await` or a `do/catch`.
- Kotlin default parameters are **not bridged** — pass all parameters explicitly.
- One `Task` per observable flow; cancel in `deinit`.

---

## 7. String System

### 7.1 Localizable.xcstrings

All user-visible strings are stored in `iosApp/iosApp/Strings/Localizable.xcstrings` (Xcode String Catalog format). The catalog contains **64 keys** in English (en) and Arabic (ar).

Key categories:

| Prefix | Purpose |
|---|---|
| `nav_*` | Tab and screen navigation titles |
| `prayer_*` | Prayer names and states |
| `quran_*` | Quran screen labels |
| `athkar_*` | Athkar group names, tabs |
| `tasbeeh_*` | Tasbeeh counter labels |
| `habits_*` | Habit list and sheets |
| `settings_*` | Settings form labels |
| `qibla_*` | Qibla compass labels |
| `home_*` | Home dashboard cards |
| `common_*` | Shared labels (loading, error, save, cancel, etc.) |
| `error_*` | Error messages mapped from `DomainError` subtypes |
| `notification_*` | Notification permission and Athkar reminder strings |

### 7.2 String.loc helper

Defined in `Strings/LocalizedKey.swift`:

```swift
extension String {
    static func loc(_ key: String) -> String {
        NSLocalizedString(key, bundle: CurrentBundle.shared.bundle, comment: "")
    }
}
```

Usage in views: `Text(String.loc("prayer_fajr"))`.

Falls back to the key name itself if the key is missing, making missing keys immediately visible during development.

### 7.3 CurrentBundle singleton

`CurrentBundle` holds the active `Bundle` for localisation. `AppSettingsViewModel` calls `CurrentBundle.shared.setLanguage("ar")` when the user changes language in Settings, causing all `String.loc()` calls to return Arabic strings immediately without an app restart.

### 7.4 ErrorKeys

`Strings/ErrorKeys.swift` maps Kotlin `DomainError` subtypes to string keys:

```swift
enum ErrorKeys {
    static func key(for error: any DomainError) -> String {
        switch error {
        case is LocationError:  return "error_location"
        case is PrayerError:    return "error_prayer"
        case is QuranError:     return "error_quran"
        case is HabitError:     return "error_habit"
        case is AthkarError:    return "error_athkar"
        default:                return "common_error_generic"
        }
    }
}
```

---

## 8. Language and Theme System

`AppSettingsViewModel` (`DesignSystem/AppSettingsViewModel.swift`) is owned by `RootNavigationView` as a `@StateObject` and lives for the full app lifetime. It observes `ObserveSettingsUseCase` (a Kotlin Flow) and publishes:

| Property | SwiftUI environment key | Effect |
|---|---|---|
| `colorScheme: ColorScheme?` | `.preferredColorScheme` | `nil` = follow system; `.light` / `.dark` = forced |
| `layoutDirection: LayoutDirection` | `.layoutDirection` | `.leftToRight` (EN) or `.rightToLeft` (AR) |
| `locale: Locale` | `.locale` | Used by `Text` formatters |

All three are injected at the root:

```swift
TabView { ... }
    .preferredColorScheme(appSettings.colorScheme)
    .environment(\.layoutDirection, appSettings.layoutDirection)
    .environment(\.locale, appSettings.locale)
    .environmentObject(appSettings)
```

`AppLanguage.isRtl` (Kotlin computed property bridged by SKIE) drives the `layoutDirection`. `CurrentBundle.shared.setLanguage(code)` is called in the same observer to update `String.loc()`.

---

## 9. Screen State Pattern

All 8 feature screens follow the same three-state pattern:

### Loading state
```swift
if vm.isLoading {
    ProgressView()
        .tint(MudawamaTheme.Colors.primary)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
}
```

### Error state
```swift
else if let msg = vm.errorMessage {
    VStack(spacing: MudawamaTheme.Spacing.md) {
        Text(msg).foregroundStyle(MudawamaTheme.Colors.error)
        Button(String.loc("common_retry")) { vm.observe(...) }
    }
}
```

### Empty state
Shown inline when the data collection is empty (e.g., no habits yet, no Quran log). Each screen has a feature-specific empty state prompt.

### TasbeehView loading gate
`TasbeehView` uses a `Group`-based loading gate with `mainContent` as a computed property. All `.navigationTitle`, `.toolbar`, `.sheet`, and `.task` modifiers are applied to the `Group` in `body`, **not** on `mainContent`.

---

## 10. Athkar Screen Design

`AthkarView` renders dhikr item cards **directly inline** in a `ScrollView` — no intermediate sheet presentation. The flow:

1. Segmented control at top (Morning / Evening / Post-Prayer) — updates `vm.selectedType`.
2. Completion header shown inline when the group is fully done.
3. `itemCard(item:groupType:)` renders each `AthkarItem` as a tappable card showing:
   - Arabic text + transliteration
   - Current count vs required count (from `vm.count(for: item.id, groupType:)`)
   - Progress indicator (done / remaining)
4. Tapping calls `vm.increment(groupType:date:itemId:)`.

Live counters are driven by `ObserveAthkarLogUseCase` (one Flow per group type). `AthkarViewModel.counters` is a `[AthkarGroupType: [String: Int]]` map updated in real time.

`AthkarGroupView.swift` still exists as a standalone file but is no longer presented from `AthkarView`.

Bell toolbar button in `AthkarView` presents `AthkarNotificationSheet` for quick access to morning/evening notification toggles.

---

## 11. Qibla Compass

`QiblaViewModel` (`Features/Qibla/QiblaViewModel.swift`) is an `NSObject` subclass that conforms to `CLLocationManagerDelegate` directly — this is allowed in Swift (unlike in Kotlin/Native where NSObject subclassing is restricted).

### State machine

```swift
enum QiblaUiState {
    case loading
    case active(compassHeading: Double, qiblaAngle: Double)
    case permissionDenied
    case error(String)
}
```

### Data flow

1. `vm.start()` → checks `CLLocationManager.authorizationStatus`
2. If `.notDetermined` → `requestWhenInUseAuthorization()` → delegate callback → start updates
3. `didUpdateLocations` → stores `userCoordinates` → calls `recalculate()`
4. `didUpdateHeading` → updates `compassHeading` → calls `recalculate()`
5. `recalculate()` → calls `provider.calculateQiblaAngleUseCase.invoke(origin: coords)` → publishes `.active(compassHeading:qiblaAngle:)`

### Alignment + haptics

```swift
let aligned = abs(compassHeading - qiblaAngle) <= 2.0
           || abs(compassHeading - qiblaAngle) >= 358.0
if aligned && !wasAligned {
    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
}
```

`QiblaView` animates the needle with `.rotationEffect(Angle(degrees: vm.compassHeading - vm.qiblaAngle))`.

### Permission denial

When `uiState == .permissionDenied`, `QiblaView` shows a "Go to Settings" button:

```swift
Button(String.loc("qibla_go_to_settings")) {
    vm.openSystemSettings()  // UIApplication.shared.open(settingsUrl)
}
```

---

## 12. Notification System

### Architecture

```
IosNotificationProvider (Swift)
    implements NotificationPermissionProvider (Kotlin interface)
    registered via initializeKoin(iosNotificationProvider:)
    ↓
IosNotificationScheduler (Kotlin, iosMain)
    UNCalendarNotificationTrigger, repeats = true
    Scheduled by AthkarNotificationRepositoryImpl via Kotlin DataStore prefs
```

### First-launch permission

`iOSApp.body` runs `.task { await notificationProvider.requestPermissionAndNotifyIfGranted() }` once on launch. If the user grants permission, a welcome notification fires 1 second later (shown in-app via `UNUserNotificationCenterDelegate`).

### Athkar reminders

Morning and evening notification toggles + time pickers live in both:
- `SettingsView` → `SettingsViewModel.setMorningNotification(enabled:)` / `setEveningNotification(enabled:)`
- `AthkarNotificationSheet` → quick-access from the Athkar tab toolbar

Notification IDs: `AthkarNotificationIds.MORNING = 1001`, `AthkarNotificationIds.EVENING = 1002` (stable Kotlin constants).

---

## 13. Swift/Kotlin Interop Rules

These rules apply to every Swift file in `iosApp/iosApp/`:

| Rule | Detail |
|---|---|
| **SKIE Flows are non-throwing** | Use `for await x in flow { }`, never `for try await` |
| **No Kotlin default parameters in Swift** | Always pass all parameters explicitly |
| **Result casts require `<AnyObject>`** | `result as? ResultSuccess<AnyObject>`, not `ResultSuccess<SomeType>` |
| **`suspend` functions may throw** | Always call via `try? await` or `do { try await } catch {}` |
| **`@MainActor` on every ViewModel** | `@Published` mutations from background threads crash on iOS 17+ |
| **`AthkarGroupType` enum cases** | `.morning`, `.evening`, `.postPrayer` (camelCase, SKIE-bridged from Kotlin ALL_CAPS) |
| **`AthkarDailyLog.counters`** | Bridges as `[String: KotlinInt]`; cast with `as? [String: Int]` |
| **LSP errors for MudawamaCore** | Known Xcode cache artifacts — not real errors; run a clean build to verify |
| **`SkieSwiftStateFlow.value`** | Read synchronously before entering `for await` to avoid blank first frame |

---

## 14. Platform Provider Classes

Three Swift classes bridge Kotlin interfaces to native iOS APIs:

### IosEncryptor (`IosEncryptor.swift`)
- Implements: Kotlin `EncryptionProvider`
- Uses: iOS Keychain / `CryptoKit`
- Passed to: `initializeKoin(iosEncryptor:)`

### IosLocationProvider (`IosLocationProvider.swift`)
- Implements: Kotlin `LocationProvider`
- Uses: `CLLocationManager`
- Passed to: `initializeKoin(iosLocationProvider:)`
- Also used directly by `PrayerViewModel` and `HomeViewModel` for current coordinates

### IosNotificationProvider (`IosNotificationProvider.swift`)
- Implements: Kotlin `NotificationPermissionProvider`
- Conforms to: `UNUserNotificationCenterDelegate`
- Uses: `UNUserNotificationCenter`
- Passed to: `initializeKoin(iosNotificationProvider:)`
- Stored as `private let` in `iOSApp` so it lives for the full app lifetime

---

## 15. Complete File Tree

```
iosApp/iosApp/
├── iOSApp.swift                              App entry point, Koin init, notification permission
├── IosEncryptor.swift                        EncryptionProvider bridge
├── IosLocationProvider.swift                 LocationProvider bridge
├── IosNotificationProvider.swift             NotificationPermissionProvider bridge
│
├── DesignSystem/
│   ├── MudawamaTheme.swift                   Color, Spacing, Radius tokens + MudawamaSurfaceCard
│   └── AppSettingsViewModel.swift            App-wide theme / language / RTL observer
│
├── Navigation/
│   └── RootNavigationView.swift              TabView shell, NavigationPath per tab, AppRoute enum
│
├── Strings/
│   ├── LocalizedKey.swift                    CurrentBundle + String.loc() helper
│   ├── Localizable.xcstrings                 64 keys, English + Arabic
│   └── ErrorKeys.swift                       DomainError → string key mapper
│
├── Features/
│   ├── Home/
│   │   ├── HomeViewModel.swift               Aggregates 5 parallel Kotlin flows
│   │   ├── HomeView.swift                    Scrollable dashboard with 5 summary sections
│   │   └── Components/
│   │       ├── NextPrayerCard.swift          Full-width next prayer + countdown
│   │       ├── AthkarSummaryCard.swift       Morning/Evening completion dots
│   │       ├── QuranProgressCard.swift       Pages today vs goal mini ring
│   │       ├── TasbeehSummaryCard.swift      Count today vs goal
│   │       └── HabitsSummarySection.swift    Compact habit completion row
│   │
│   ├── Prayer/
│   │   ├── PrayerViewModel.swift             Observes prayers flow; LogStatus/PrayerName extensions
│   │   └── PrayerView.swift                  Date strip, prayer rows, toggle, pull-to-refresh
│   │
│   ├── Quran/
│   │   ├── QuranViewModel.swift              Observes quran state flow
│   │   ├── QuranView.swift                   Progress ring, streak, bookmark, pull-to-refresh
│   │   └── Sheets/
│   │       ├── LogReadingSheet.swift         Page-count input
│   │       ├── QuranGoalSheet.swift          Daily page goal setter
│   │       └── QuranPositionSheet.swift      Surah / Ayah bookmark updater
│   │
│   ├── Athkar/
│   │   ├── AthkarViewModel.swift             Live counters via ObserveAthkarLogUseCase (3 flows)
│   │   ├── AthkarView.swift                  Segmented picker + inline item cards
│   │   ├── AthkarGroupView.swift             Standalone (not currently presented from AthkarView)
│   │   └── AthkarNotificationSheet.swift     Quick morning/evening notification toggles
│   │
│   ├── Habits/
│   │   ├── HabitsViewModel.swift             Observes habits + logs flow
│   │   ├── HabitsView.swift                  List with toggle/stepper, swipe-to-delete
│   │   ├── NewHabitSheet.swift               Name, type, optional goal count
│   │   └── ManageHabitSheet.swift            Edit / delete existing habit
│   │
│   ├── Tasbeeh/
│   │   ├── TasbeehViewModel.swift            Goal + daily total flows, increment action
│   │   ├── TasbeehView.swift                 Counter ring, tap button, loading gate pattern
│   │   └── TasbeehGoalSheet.swift            Numeric goal input
│   │
│   ├── Settings/
│   │   ├── SettingsViewModel.swift           Observes + mutates AppSettings
│   │   └── SettingsView.swift                Form: calculation method, location, theme, language, notifications
│   │
│   └── Qibla/
│       ├── QiblaViewModel.swift              CLLocationManagerDelegate, QiblaUiState, haptics
│       └── QiblaView.swift                   Animated compass needle, permission fallback
```

---

## 16. Remaining Tasks

| ID | Task | Owner |
|---|---|---|
| T083 | Manual UI verification against every `docs/ui/` reference PNG — confirm layout, labels, and copy match across all 8 screens in English and Arabic | Human (requires Simulator) |
