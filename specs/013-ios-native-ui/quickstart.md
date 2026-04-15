# Quickstart: Full Native iOS App (SwiftUI)

**Feature**: `013-ios-native-ui`  
**Branch**: `013-ios-native-ui`  
**Date**: 2026-04-15

This guide explains how to implement and verify each piece of the migration. Follow sections in order — the Kotlin umbrella change must be built before the iOS Xcode target can compile.

---

## Prerequisites

- Xcode 15+ (for String Catalogs; Xcode 14 minimum for SwiftUI but 15 recommended)
- Kotlin Multiplatform toolchain configured (Android Studio / IntelliJ + KMP plugin)
- `./gradlew` runs without error on `main` before starting

---

## Step 1 — Update `umbrella-core/build.gradle.kts`

This is the only Kotlin-side Gradle change. Open `shared/umbrella-core/build.gradle.kts` and apply the target state from `contracts/kotlin-exports.md`.

Key changes:
1. Add `alias(libs.plugins.skie)` to `plugins {}`.
2. Expand `configureIosFramework("MudawamaCore", isStatic = true)` with `export(...)` for all feature domain + data modules.
3. Add matching `api(...)` declarations in `commonMain.dependencies`.
4. Keep `implementation(projects.shared.core.database)` as-is.

**Verify**: Run `./gradlew :shared:umbrella-core:linkDebugFrameworkIosSimulatorArm64` — it must succeed and produce a `MudawamaCore.framework` that replaces the old minimal version.

---

## Step 2 — Add `IosKoinHelpers.kt` to `umbrella-core` iosMain

Create `shared/umbrella-core/src/iosMain/kotlin/di/IosKoinHelpers.kt` using the content from `contracts/kotlin-exports.md` (the `KoinComponent` provider classes section).

Also create `shared/umbrella-core/src/iosMain/kotlin/KoinInitializer.kt` — the iOS-facing `initializeKoin()` function. This replaces the one currently in `umbrella-ui`. The signature changes:
- Remove `iosQiblaViewControllerProvider` parameter (Qibla is now pure SwiftUI).

**Verify**: `./gradlew :shared:umbrella-core:compileKotlinIosSimulatorArm64` — must succeed with no unresolved references.

---

## Step 3 — Update Xcode Project to Link MudawamaCore

In Xcode:
1. Open `iosApp/iosApp.xcodeproj`.
2. In Build Phases → Link Binary With Libraries: remove `MudawamaUI.framework`, add `MudawamaCore.framework`.
3. In Build Settings → Framework Search Paths: verify path points to the `umbrella-core` build output (it follows the same pattern as the existing `umbrella-ui` path).
4. Update `Configuration/Config.xcconfig` if it references the framework name explicitly.

**Note**: After this step the project will not compile until Steps 4–6 are complete (Swift files still import `MudawamaUI`).

---

## Step 4 — Update Existing Swift Provider Files

Change `import MudawamaUI` → `import MudawamaCore` in these three files:
- `iosApp/iosApp/IosEncryptor.swift`
- `iosApp/iosApp/IosLocationProvider.swift`
- `iosApp/iosApp/IosNotificationProvider.swift`

No logic changes — only the import line changes. The interfaces (`Encryptor`, `LocationProvider`, `NotificationPermissionProvider`) are now exported from `MudawamaCore` instead of `MudawamaUI`.

Delete `iosApp/iosApp/IosQiblaViewControllerProvider.swift` — it is superseded by the native `QiblaView.swift`.

---

## Step 5 — Replace iOSApp.swift and ContentView.swift

**`iOSApp.swift`** — new content:
```swift
import SwiftUI
import MudawamaCore

@main
struct iOSApp: App {
    init() {
        KoinInitializerKt.initializeKoin(
            iosEncryptor: IosEncryptor(),
            iosLocationProvider: IosLocationProvider(),
            iosNotificationProvider: IosNotificationProvider()
        )
    }

    var body: some Scene {
        WindowGroup {
            RootNavigationView()
        }
    }
}
```

**`ContentView.swift`** — delete or repurpose as `RootNavigationView.swift`:
```swift
import SwiftUI

struct RootNavigationView: View {
    var body: some View {
        TabView {
            NavigationStack { HomeView() }
                .tabItem { Label(String.loc("nav_home"), systemImage: "house.fill") }

            NavigationStack { PrayerView() }
                .tabItem { Label(String.loc("nav_prayers"), systemImage: "moon.stars.fill") }

            NavigationStack { QuranView() }
                .tabItem { Label(String.loc("nav_quran"), systemImage: "book.fill") }

            NavigationStack { AthkarView() }
                .tabItem { Label(String.loc("nav_athkar"), systemImage: "heart.fill") }
        }
    }
}
```

---

## Step 6 — Implement Feature ViewModels and Views

For each feature, follow this template (see `contracts/swift-viewmodel.md` for per-feature input/output contracts):

```swift
// Features/Prayer/PrayerViewModel.swift
import Foundation
import MudawamaCore

@MainActor
final class PrayerSwiftViewModel: ObservableObject {
    private let useCases = PrayerUseCaseProvider()

    @Published private(set) var prayers: [PrayerWithStatus] = []
    @Published private(set) var error: String?
    @Published private(set) var isLoading = true

    func observe(date: Kotlinx_datetimeLocalDate) async {
        isLoading = true
        let flow = useCases.observePrayers.invoke(date: date)
        // Read initial value
        if let initial = flow.value as? [PrayerWithStatus] {
            self.prayers = initial
            self.isLoading = false
        }
        for await list in flow {
            self.prayers = list as! [PrayerWithStatus]
            self.isLoading = false
        }
    }

    func toggleStatus(habitId: String, date: Kotlinx_datetimeLocalDate) async {
        _ = try? await useCases.toggleStatus.invoke(habitId: habitId, date: date)
    }
}
```

```swift
// Features/Prayer/PrayerView.swift
import SwiftUI

struct PrayerView: View {
    @StateObject private var vm = PrayerSwiftViewModel()

    var body: some View {
        Group {
            if vm.isLoading {
                ProgressView()
            } else {
                List(vm.prayers, id: \.habitId) { prayer in
                    PrayerRowView(prayer: prayer) {
                        Task { await vm.toggleStatus(habitId: prayer.habitId, date: .today) }
                    }
                }
                .navigationTitle(String.loc("prayer_screen_title"))
            }
        }
        .task { await vm.observe(date: .today) }
    }
}
```

Repeat this pattern for all 8 features. Reference `docs/ui/` PNG files for each screen's layout.

---

## Step 7 — Add Localizable.xcstrings

Create `iosApp/iosApp/Localizable.xcstrings` (Xcode String Catalog). Populate with all user-visible string keys — they should match the snake_case keys in `shared/designsystem/.../strings.xml` for consistency.

Minimum keys needed for navigation:
```
nav_home, nav_prayers, nav_quran, nav_athkar
```

Plus all screen titles, labels, button text, and error messages for each feature. Reference `strings.xml` for the English values and the Arabic translations.

Add a Swift extension for ergonomic access:
```swift
// Strings/LocalizedKey.swift
extension String {
    static func loc(_ key: String) -> String {
        NSLocalizedString(key, comment: "")
    }
}
```

---

## Step 8 — Verify Full Build

```bash
# Kotlin side — ensure umbrella-core still builds
./gradlew :shared:umbrella-core:linkReleaseFrameworkIosArm64

# Android side — must be unaffected
./gradlew :androidApp:assembleDebug

# iOS side — via Xcode or xcodebuild
xcodebuild -project iosApp/iosApp.xcodeproj \
           -scheme iosApp \
           -destination 'platform=iOS Simulator,name=iPhone 16' \
           build
```

All three must succeed before marking the implementation complete.

---

## Step 9 — UI Verification Checklist

For each screen, verify against the reference PNG in `docs/ui/`:

| Screen | Reference file | Key checks |
|---|---|---|
| Home | `home_dashboard.png` | Next prayer card, Athkar summary, Quran progress, Tasbeeh summary, Habits section |
| Prayer | `daily_prayer_tracker.png` | 5 prayers listed, status icons, time display |
| Quran | `quran_daily_reading_tracker.png` | Progress ring, streak, date strip, log button |
| Athkar | `daily_athkar_tracker.png` | Morning/Evening/Post-Prayer tabs, completion state |
| Tasbeeh | `tasbeeh_counter.png` | Counter button, daily total, goal ring |
| Habits | `daily_habits.png` | Habit list, toggle/count, add habit sheet |
| Settings | `settings.png` | All setting rows, notification section |
| Qibla | (native compass) | Needle rotation, degree readout, calibration warning |

---

## Common Pitfalls

| Pitfall | Fix |
|---|---|
| Swift sees `Optional<KotlinX>` instead of native type | Use SKIE's type-safe wrappers; cast via `as? T` |
| `@Published` mutation on background thread | Always mark ViewModel `@MainActor` |
| `for await` loop never ends on screen re-entry | `.task{}` lifecycle handles this — don't call `observe()` from `onAppear` (use `.task` instead) |
| Koin use case is null on first access | Ensure `initializeKoin()` is called in `iOSApp.init()` before any View body executes |
| `MudawamaCore` types not visible in Swift | Check that `export()` + `api()` both appear for that module in `umbrella-core/build.gradle.kts` |
| `Result<D,E>` pattern match fails | SKIE generates `ResultSuccess` and `ResultFailure` subclasses — match with `as? ResultSuccess` |
| Arabic text appears LTR | Set `environment(\.layoutDirection, .rightToLeft)` for Arabic locale — SwiftUI does this automatically if system language is Arabic |
