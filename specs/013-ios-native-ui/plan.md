# Implementation Plan: Full Native iOS App (SwiftUI)

**Branch**: `013-ios-native-ui` | **Date**: 2026-04-15 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `specs/013-ios-native-ui/spec.md`

## Summary

Migrate the iOS delivery model from Compose Multiplatform (via `MudawamaUI` framework) to a fully native SwiftUI application. The iOS Xcode target will link `MudawamaCore` (`:shared:umbrella-core`) only — containing all feature `:domain` + `:data` modules exported via KMP. All screens, navigation (SwiftUI `TabView` + `NavigationStack`), and platform integrations are rewritten in Swift. Android is entirely unaffected; `:shared:umbrella-ui` and all KMP presentation/navigation modules are left untouched.

## Technical Context

**Language/Version**: Kotlin 2.3.20 (KMP — domain/data only) + Swift 5.10 / SwiftUI (iOS UI layer)  
**Primary Dependencies**:
- Kotlin side: Room 2.8.4, Ktor 3.4.1, Koin 4.2.0, kotlinx-coroutines 1.10.2, kotlinx-datetime 0.7.1
- iOS side: SwiftUI, Combine (for ObservableObject), SKIE 0.10.11 (Flow→AsyncSequence bridge)
- Interop: SKIE `co.touchlab.skie:0.10.11` applied to `umbrella-core` only

**Storage**: Room KMP (SQLite) via `shared:core:database` — unchanged  
**Testing**: XCTest (iOS SwiftUI ViewModels), existing Kotlin unit tests unchanged  
**Target Platform**: iOS 15+ (existing minimum); Android unchanged  
**Project Type**: KMP mobile app — Kotlin for domain/data, native SwiftUI for iOS UI  
**Performance Goals**: 60fps navigation transitions; StateFlow emissions visible in SwiftUI ≤200ms  
**Constraints**: iOS 15+ compatibility (rules out `@Observable` macro — iOS 17+ only; use `ObservableObject` pattern); `MudawamaUI` and all Android modules must remain unmodified  
**Scale/Scope**: 8 feature screens + root navigation; ~8 Swift ViewModels + ~8 SwiftUI screen files; 1 umbrella-core Gradle change

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Rule | Status | Notes |
|---|---|---|
| Domain layer: pure Kotlin, no Android/iOS/Compose imports | PASS | No changes to domain modules — they remain untouched |
| Presentation layer: Compose Multiplatform only | **SCOPED EXCEPTION** | The constitution's "Presentation MUST use Compose Multiplatform" applies to the KMP `:presentation` modules. Those modules are entirely untouched. The iOS native SwiftUI layer lives exclusively in `iosApp/` — outside any KMP module boundary. This is architecturally the Phase 2 umbrella-core strategy described in `docs/ARCHITECTURE.md`. |
| Dependency direction: presentation → domain ← data | PASS | SwiftUI ViewModels → Kotlin domain use cases ← Kotlin data implementations. Direction preserved. |
| No feature module cross-dependencies | PASS | No KMP module changes except `umbrella-core/build.gradle.kts` aggregation |
| Error handling: Result<D,E> / safeCall | PASS | Unchanged in Kotlin layer; Swift side handles via try/catch on SKIE-bridged async calls |
| MVI in presentation | N/A | MVI applies to KMP `:presentation` modules which are untouched. iOS ViewModels use `ObservableObject` + `@Published` state (SwiftUI-idiomatic equivalent) |
| Single strings.xml in shared:designsystem | SCOPED — iOS exception | The shared `strings.xml` serves Compose UI (Android). iOS native SwiftUI uses `Localizable.strings` in `iosApp/` — see research.md Decision D-004. Android strings are untouched. |
| No hardcoded strings in Composables | PASS | No Composable code is changed |
| DI: Koin only | PASS | Koin initialised in `iOSApp.swift` via `KoinInitializerKt.initializeKoin()`; Swift-side ViewModels resolve use cases via `KoinComponent` helpers in `umbrella-core`'s `iosMain` |
| Room for DB, Ktor for network, no Retrofit/SQLDelight | PASS | No data module changes |
| CoroutineDispatcher injected, no Dispatchers.IO/Main hardcoded | PASS | No change to Kotlin coroutine usage |
| Convention plugins single-responsibility | PASS | Only adding `alias(libs.plugins.skie)` to `umbrella-core/build.gradle.kts` — same pattern as `umbrella-ui` |
| UI matches docs/ui/ reference screens | SCOPED — iOS re-implementation | SwiftUI screens must match the same `docs/ui/` reference images as the Compose counterparts |

**Constitution gate result: PASS WITH DOCUMENTED SCOPE.** The two exceptions (SwiftUI in `iosApp/`, `Localizable.strings`) are explicitly sanctioned by the Phase 2 umbrella-core strategy in `docs/ARCHITECTURE.md` and do not violate any KMP module boundaries.

## Project Structure

### Documentation (this feature)

```text
specs/013-ios-native-ui/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── kotlin-exports.md      # What umbrella-core exports to Swift
│   └── swift-viewmodel.md     # Swift ViewModel ↔ Kotlin use case contract
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code Changes

```text
# Kotlin — ONLY change is umbrella-core
shared/umbrella-core/
├── build.gradle.kts           ← ADD: skie plugin, export all feature domain+data modules
└── src/iosMain/kotlin/
    └── di/
        └── IosKoinHelpers.kt  ← NEW: KoinComponent provider classes for each feature

# iOS — all new or replaced Swift files
iosApp/iosApp/
├── iOSApp.swift               ← MODIFY: remove MudawamaUI import, remove IosQiblaViewControllerProvider
├── ContentView.swift          ← REPLACE: remove UIViewControllerRepresentable, add SwiftUI root
│
├── Navigation/
│   └── RootNavigationView.swift   ← NEW: TabView with NavigationStack per tab
│
├── Features/
│   ├── Home/
│   │   ├── HomeViewModel.swift
│   │   └── HomeView.swift
│   ├── Prayer/
│   │   ├── PrayerViewModel.swift
│   │   └── PrayerView.swift
│   ├── Quran/
│   │   ├── QuranViewModel.swift
│   │   └── QuranView.swift
│   ├── Athkar/
│   │   ├── AthkarViewModel.swift
│   │   └── AthkarView.swift
│   ├── Tasbeeh/
│   │   ├── TasbeehViewModel.swift
│   │   └── TasbeehView.swift
│   ├── Habits/
│   │   ├── HabitsViewModel.swift
│   │   └── HabitsView.swift
│   ├── Settings/
│   │   ├── SettingsViewModel.swift
│   │   └── SettingsView.swift
│   └── Qibla/
│       ├── QiblaViewModel.swift
│       └── QiblaView.swift
│
├── DesignSystem/
│   └── MudawamaTheme.swift     ← NEW: iOS color/typography tokens matching docs/ui/
│
├── Strings/
│   └── (managed via Xcode String Catalogs or Localizable.strings)
│
├── IosEncryptor.swift          ← MODIFY: change import MudawamaUI → import MudawamaCore
├── IosLocationProvider.swift   ← MODIFY: change import MudawamaUI → import MudawamaCore
├── IosNotificationProvider.swift ← MODIFY: change import MudawamaUI → import MudawamaCore
└── IosQiblaViewControllerProvider.swift ← DELETE: superseded by native QiblaView
```

**Structure Decision**: iOS-side code follows feature-folder organisation matching the Kotlin module layout. Each feature gets a ViewModel (ObservableObject) and a SwiftUI View. Navigation lives in a dedicated `Navigation/` folder. Shared design tokens in `DesignSystem/`. The Kotlin side change is minimal — only `umbrella-core`.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| SwiftUI in `iosApp/` (outside KMP modules) | Full native iOS experience is the feature goal | Keeping Compose on iOS defeats the entire purpose of the migration |
| `Localizable.strings` on iOS alongside shared `strings.xml` | Compose Resources (shared strings.xml) are part of MudawamaUI/CMP runtime, not available in MudawamaCore | Cannot use Kotlin `Res` without linking Compose runtime — which is the thing being removed |
