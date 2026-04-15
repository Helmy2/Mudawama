# Research: Full Native iOS App (SwiftUI)

**Feature**: `013-ios-native-ui`  
**Date**: 2026-04-15  
**Status**: All NEEDS CLARIFICATION resolved

---

## D-001: SKIE Version and umbrella-core Integration

**Decision**: Use SKIE `0.10.11` (already pinned in `libs.versions.toml` as `skie = "0.10.11"`). Apply the plugin to `shared/umbrella-core/build.gradle.kts` via `alias(libs.plugins.skie)` — exactly as it is applied today in `umbrella-ui/build.gradle.kts`.

**Rationale**: SKIE 0.10.11 is the current stable release and explicitly supports Kotlin 2.3.20 (the project's current Kotlin version). The plugin is already versioned in the project's TOML — no new dependency declaration needed.

**How it is applied**:
```kotlin
// shared/umbrella-core/build.gradle.kts
plugins {
    id("mudawama.kmp")
    alias(libs.plugins.skie)    // ← add this line
}
```

SKIE instruments the framework at compile time — it is applied only to the module that produces the `.framework` binary. No SKIE changes are needed in any feature module.

**Alternatives considered**:
- KMMBridge + direct Obj-C headers: rejected — no async/await or Flow bridging without SKIE.
- Manual Combine bridging in Swift: rejected — verbose, error-prone, hard to maintain for 8+ StateFlows.

---

## D-002: Flow → AsyncSequence (Swift Collection Pattern)

**Decision**: Use SKIE's automatic `SkieSwiftStateFlow<T>` / `SkieSwiftFlow<T>` bridging with Swift's `for await` loop inside a `.task {}` view modifier. Use `ObservableObject` + `@Published` (iOS 15+ compatible — rules out `@Observable` which requires iOS 17+).

**Pattern** (canonical, used for all 8 feature ViewModels):
```swift
@MainActor
final class FeatureSwiftViewModel: ObservableObject {
    private let provider = FeatureUseCaseProvider()  // KoinComponent helper

    @Published private(set) var state: FeatureUiState = FeatureUiState()

    func observe() async {
        // Read initial value synchronously before entering loop
        self.state = provider.uiStateFlow.value
        for await newState in provider.uiStateFlow {
            self.state = newState   // safe: @MainActor guarantees main thread
        }
    }
}

struct FeatureScreen: View {
    @StateObject private var vm = FeatureSwiftViewModel()
    var body: some View {
        FeatureContent(state: vm.state)
            .task { await vm.observe() }  // cancels when view leaves hierarchy
    }
}
```

**Key rules**:
1. Always read `.value` synchronously for initial state to avoid blank first render.
2. `.task {}` auto-cancels the Kotlin coroutine when the view disappears — no manual cancellation needed.
3. All `@Published` mutations happen on `@MainActor` — required by SwiftUI.
4. For features without a persistent StateFlow (e.g., one-shot suspend calls), use plain `async let` or `Task { }` inside `.onAppear`.

**Alternatives considered**:
- SKIE `Observing` preview feature: rejected — still marked experimental/preview, not production-ready.
- 100ms `Timer` polling (current Qibla approach): rejected for general use — `for await` is event-driven with zero polling overhead. Timer polling kept only if a specific Kotlin Flow proves incompatible (document per feature if needed).

---

## D-003: Koin Resolution from Swift (KoinComponent Helpers)

**Decision**: Define thin `KoinComponent` provider classes in `shared/umbrella-core/src/iosMain/kotlin/di/` — one per feature. Swift ViewModels instantiate these providers directly and access use cases as properties. This is the established KMP-Koin pattern for native Swift consumers.

**Implementation**:
```kotlin
// shared/umbrella-core/src/iosMain/kotlin/di/IosKoinHelpers.kt
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PrayerUseCaseProvider : KoinComponent {
    val observePrayers: ObservePrayersForDateUseCase by inject()
    val toggleStatus: TogglePrayerStatusUseCase by inject()
    val seedHabits: SeedPrayerHabitsUseCase by inject()
}

class QuranUseCaseProvider : KoinComponent {
    val observeProgress: ObserveQuranProgressUseCase by inject()
    val logReading: LogQuranReadingUseCase by inject()
    val observeStreak: ObserveReadingStreakUseCase by inject()
}

class AthkarUseCaseProvider : KoinComponent {
    val observeGroups: ObserveAthkarGroupsUseCase by inject()
    val markGroupComplete: MarkAthkarGroupCompleteUseCase by inject()
}

class TasbeehUseCaseProvider : KoinComponent {
    val observeGoal: ObserveTasbeehGoalUseCase by inject()
    val incrementCount: IncrementTasbeehUseCase by inject()
    val observeDailyTotal: ObserveTasbeehDailyTotalUseCase by inject()
}

class HabitsUseCaseProvider : KoinComponent {
    val observeHabits: ObserveHabitsUseCase by inject()
    val toggleHabitLog: ToggleHabitLogUseCase by inject()
}

class SettingsUseCaseProvider : KoinComponent {
    val observeSettings: ObserveSettingsUseCase by inject()
    val setCalculationMethod: SetCalculationMethodUseCase by inject()
    val setLocationMode: SetLocationModeUseCase by inject()
    val setAppTheme: SetAppThemeUseCase by inject()
    val setAppLanguage: SetAppLanguageUseCase by inject()
}

class QiblaUseCaseProvider : KoinComponent {
    val calculateQiblaAngle: CalculateQiblaAngleUseCase by inject()
}

class HomeUseCaseProvider : KoinComponent {
    // Home aggregates use cases from other features — all in domain layer
    val getNextPrayer: GetNextPrayerTimeUseCase by inject()
    val observeAthkarSummary: ObserveAthkarSummaryUseCase by inject()
    val observeQuranProgress: ObserveQuranProgressUseCase by inject()
    val observeTasbeehSummary: ObserveTasbeehDailyTotalUseCase by inject()
    val observeHabitsSummary: ObserveHabitsUseCase by inject()
}
```

**Swift usage**:
```swift
@MainActor final class PrayerSwiftViewModel: ObservableObject {
    private let useCases = PrayerUseCaseProvider()
    // useCases.observePrayers is a use case resolved from Koin
}
```

**Rationale**: `KoinComponent` with `by inject()` is lazy — resolved on first access, after `initializeKoin()` completes. Safe from race conditions as long as `initializeKoin()` is called before the first ViewModel is created (enforced by `iOSApp.init()`). Avoids exposing Koin's full API to Swift.

**Alternatives considered**:
- `KoinKt.getKoin().get(objCClass:)` from Swift: rejected — couples Swift to internal Koin API; verbose.
- Returning use cases from `initializeKoin()` return value: rejected — doesn't scale; breaks when use cases have state or need re-resolution.

---

## D-004: String Strategy (iOS Localizable.strings)

**Decision**: iOS uses `Localizable.strings` (or Xcode String Catalogs — `.xcstrings`) as the single source of truth for all user-visible strings on the iOS side. The shared `strings.xml` in `shared/designsystem` is untouched and continues to serve Android/Compose.

**Rationale**:
- Compose Resources (`Res.string.*`) are generated by the Compose Multiplatform plugin and are not accessible from `MudawamaCore` (which has no Compose dependency).
- iOS String Catalogs (Xcode 15+) support Arabic plural rules (6 grammatical forms), RTL layout, and per-device string variants — superior to any manual bridging.
- Apple's standard localization tooling works only with iOS-native string files.
- The project already supports Arabic (`ar.lproj/`) — this is a first-class citizen.

**String key alignment**: While not required, string keys SHOULD match the `snake_case` scheme used in `strings.xml` (e.g., `prayer_next_title`) to make cross-platform auditing easier. This is a convention, not a build constraint.

**String Catalog format** (recommended over legacy `.strings`):
```json
// iosApp/iosApp/Localizable.xcstrings (Xcode String Catalog)
{
  "sourceLanguage": "en",
  "strings": {
    "prayer_next_title": {
      "localizations": {
        "en": { "stringUnit": { "state": "translated", "value": "Next Prayer" } },
        "ar": { "stringUnit": { "state": "translated", "value": "الصلاة القادمة" } }
      }
    }
  }
}
```

**Swift access**:
```swift
// Simple helper — wraps NSLocalizedString
extension String {
    static func loc(_ key: String) -> String {
        NSLocalizedString(key, comment: "")
    }
}
// Usage: Text(.loc("prayer_next_title"))
```

**Alternatives considered**:
- Pass resolved strings from Kotlin use cases: rejected — Kotlin has no access to iOS locale/bundle; would require an extra bridging layer and breaks Arabic plural rules.
- Duplicate all strings in both systems: rejected — maintenance nightmare; divergence guaranteed.

---

## D-005: umbrella-core Export Scope

**Decision**: `umbrella-core/build.gradle.kts` must export all feature `:domain` modules and their `:data` modules (since some ViewModels need data-layer types like `PrayerStatus`, repository results etc.). Each exported module must also be declared as `api()` in `commonMain.dependencies` — Gradle enforces this.

**Required exports** (derived from feature inventory):
```kotlin
// shared/umbrella-core/build.gradle.kts — target state
configureIosFramework("MudawamaCore", isStatic = true) {
    // Core
    export(projects.shared.core.domain)
    export(projects.shared.core.data)
    export(projects.shared.core.time)
    // Features — domain
    export(projects.feature.habits.domain)
    export(projects.feature.prayer.domain)
    export(projects.feature.quran.domain)
    export(projects.feature.athkar.domain)
    export(projects.feature.settings.domain)
    export(projects.feature.qibla.domain)
    // Features — data (needed for Koin module registration)
    export(projects.feature.habits.data)
    export(projects.feature.prayer.data)
    export(projects.feature.quran.data)
    export(projects.feature.athkar.data)
    export(projects.feature.settings.data)
    export(projects.feature.qibla.data)
}

sourceSets {
    commonMain.dependencies {
        // Must mirror every export() above as api()
        api(projects.shared.core.domain)
        api(projects.shared.core.data)
        api(projects.shared.core.time)
        api(projects.feature.habits.domain)
        api(projects.feature.prayer.domain)
        api(projects.feature.quran.domain)
        api(projects.feature.athkar.domain)
        api(projects.feature.settings.domain)
        api(projects.feature.qibla.domain)
        api(projects.feature.habits.data)
        api(projects.feature.prayer.data)
        api(projects.feature.quran.data)
        api(projects.feature.athkar.data)
        api(projects.feature.settings.data)
        api(projects.feature.qibla.data)
        // Keep existing database dep (required by data modules)
        implementation(projects.shared.core.database)
    }
}
```

**Types NOT visible to Swift even after export** (document per developer):
- `internal` Kotlin classes (e.g., internal repository implementations, internal DAOs) — stripped from ObjC headers.
- Kotlin `suspend` lambda parameters on interfaces — SKIE handles suspend *functions* but not suspend function-type *parameters*.
- `Result<D, E>` custom sealed class — exported but Swift sees it as a sealed class hierarchy; SKIE's sealed class support makes exhaustive `switch` possible in Swift.

**Note on `shared:core:database`**: exported as `implementation` (not `api`) since Swift does not need to reference Room entity types directly — they are mapped to domain models before leaving the data layer.

---

## D-006: Navigation Architecture (SwiftUI)

**Decision**: `TabView` with 4 tabs at the root. Each tab hosts a `NavigationStack`. Push destinations (Habits, Tasbeeh, Settings, Qibla) are pushed onto the appropriate tab's stack. No Kotlin navigation involved.

**Structure**:
```swift
// RootNavigationView.swift
struct RootNavigationView: View {
    var body: some View {
        TabView {
            NavigationStack { HomeView() }
                .tabItem { Label(loc("nav_home"), systemImage: "house.fill") }

            NavigationStack { PrayerView() }
                .tabItem { Label(loc("nav_prayers"), systemImage: "moon.stars.fill") }

            NavigationStack { QuranView() }
                .tabItem { Label(loc("nav_quran"), systemImage: "book.fill") }

            NavigationStack { AthkarView() }
                .tabItem { Label(loc("nav_athkar"), systemImage: "heart.fill") }
        }
    }
}
```

Push destinations are navigated via `NavigationLink` or programmatic `NavigationPath`. Tab bar is automatically hidden when pushing (standard SwiftUI `navigationBarHidden` / `toolbar(.hidden, for: .tabBar)` on iOS 16+; `toolbarBackground(.hidden, for: .tabBar)` pattern for iOS 15).

**Alternatives considered**:
- Custom tab bar matching the Compose glassmorphism design: deferred to implementation — iOS 15 `TabView` is the baseline; custom styling is an enhancement.
- `NavigationSplitView`: rejected — iPad-optimised, not appropriate for this phone-first app.

---

## D-007: iOSApp.swift and ContentView.swift Changes

**Decision**:

`iOSApp.swift` — change `import MudawamaUI` → `import MudawamaCore`. Remove `IosQiblaViewControllerProvider`. Keep all other providers unchanged (they implement interfaces from `core` layer, now exposed via `MudawamaCore`).

`ContentView.swift` — replace entirely. Remove `UIViewControllerRepresentable` wrapping `MainKt.MainViewController()`. Replace with `RootNavigationView()`.

```swift
// iOSApp.swift — target state
import SwiftUI
import MudawamaCore   // ← was MudawamaUI

@main
struct iOSApp: App {
    init() {
        KoinInitializerKt.initializeKoin(
            iosEncryptor: IosEncryptor(),
            iosLocationProvider: IosLocationProvider(),
            iosNotificationProvider: IosNotificationProvider()
            // IosQiblaViewControllerProvider removed — Qibla is now pure SwiftUI
        )
    }
    var body: some Scene {
        WindowGroup {
            RootNavigationView()
        }
    }
}
```

**Note**: `initializeKoin` signature change (removing `iosQiblaViewControllerProvider` parameter) requires a corresponding update to the Kotlin `KoinInitializer` in `umbrella-core/src/iosMain/`. The `iosQiblaPresentationModule` and `QiblaViewControllerProvider` interface are no longer needed — they live in `feature:qibla:presentation` which is untouched (Android still uses them). The iOS Koin initialiser (`umbrella-core` iosMain) is written fresh and does not call the `MudawamaUI` initialiser.

---

## D-008: Qibla Compass — Pure SwiftUI

**Decision**: The Qibla screen is a pure SwiftUI `View` + `ObservableObject` ViewModel that:
1. Calls `CalculateQiblaAngleUseCase` (via `QiblaUseCaseProvider`) for the Qibla bearing.
2. Reads device heading via `CLLocationManager` directly in Swift (already implemented in `IosLocationProvider.swift`; compass heading needs a dedicated `CLLocationManager` instance for continuous heading updates).
3. Renders the compass needle using SwiftUI `Canvas` or a rotating `Image` with `.rotationEffect`.

This replaces `IosQiblaViewControllerProvider.swift` entirely. The file is deleted.

**`IosQiblaViewControllerProvider.swift` deletion** is safe because:
- `QiblaViewControllerProvider` interface and `QiblaScreenBridge` live in `feature:qibla:presentation` which is untouched.
- Android still uses them; the iOS Koin initialiser simply does not register an implementation — and iOS no longer links `MudawamaUI` so it cannot reference `QiblaScreenBridge` anyway.
