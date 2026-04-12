# 🏛 Mudawama Architecture & Module Structure

Mudawama is built using a highly scalable, enterprise-grade Kotlin Multiplatform (KMP) architecture. It strictly enforces **Clean Architecture** via physical Gradle module boundaries and uses a **"Packaging by Feature"** strategy to ensure fast build times and zero circular dependencies.

## 📂 The Directory Blueprint

The repository is flattened into distinct top-level directories to clearly separate native platform shells, shared infrastructure, and actual business features.

```text
mudawama/
├── androidApp/                 # Native Android shell
├── iosApp/                     # Native iOS shell
│
├── shared/                     # Foundational infrastructure (Not feature-specific)
│   ├── core/
│   │   ├── domain/             # Result wrappers, DataError interfaces, MudawamaLogger, ConnectivityObserver
│   │   ├── data/               # Ktor client setup, Tink/Platform Session Encryptors, DataStore
│   │   ├── database/           # Room KMP database: entities, DAOs, MudawamaDatabase, Koin DI
│   │   ├── time/               # TimeProvider interface, RolloverPolicy, logical date calculation, Koin DI
│   │   └── presentation/       # Custom MVI BaseViewModels, UiMessageManager, Permission State Composables
│   │
│   ├── build-logic/            # Custom Gradle Convention Plugins (shared build logic)
│   │
│   ├── designsystem/           # Compose themes, typography, localized strings, icons
│   ├── navigation/             # App Shell, floating bottom nav bar, Navigation 3 routing graph
│   │
│   ├── umbrella-core/          # iOS Export: Pure Business Logic (.framework)
│   └── umbrella-ui/            # iOS Export: Compose UI & NavGraph (.framework)
│
└── feature/                    # ALL product features live here
    ├── home/
    │   └── presentation/       # HomeScreen, HomeViewModel, summary cards (NextPrayerCard,
    │                           # AthkarSummaryCard, QuranProgressCard, TasbeehSummaryCard,
    │                           # HabitsSummarySection), MVI model files, Koin DI module
    │                           # Aggregates domain-only deps from prayer, athkar, quran, habits
    │
    ├── habits/
    │   ├── domain/             # Models, UseCases, Repository Interfaces
    │   ├── data/               # DAOs via shared:core:database, API Calls
    │   └── presentation/       # Jetpack Compose UI, ViewModels
    │
    ├── prayer/
    │   ├── domain/
    │   ├── data/               # Aladhan API via named Ktor HttpClient
    │   └── presentation/
    │
    ├── quran/
    │   ├── domain/             # ReadingStreak, LogReading, AdvanceBookmark UseCases, etc.
    │   ├── data/               # alquran.cloud API via named Ktor HttpClient, Room DAOs
    │   └── presentation/       # QuranScreen, Log/Goal/Position bottom sheets
    │
    └── athkar/
        ├── domain/             # AthkarItem, AthkarGroup, AthkarDailyLog, TasbeehGoal, TasbeehDailyTotal models;
        │                       # AthkarRepository, TasbeehRepository interfaces; 9 use cases
        ├── data/               # AthkarRepositoryImpl, TasbeehRepositoryImpl, mappers, Koin DI
        └── presentation/       # AthkarScreen, AthkarGroupScreen, TasbeehScreen, TasbeehGoalBottomSheet,
                                # AthkarViewModel, TasbeehViewModel; expect/actual BackHandler + Permission

    │
    └── settings/
        ├── domain/             # AppSettings, CalculationMethod, LocationMode, AppTheme, AppLanguage models;
        │                       # SettingsRepository interface, ObserveSettingsUseCase, SetXxxUseCases
        ├── data/               # SettingsRepositoryImpl using DataStore Preferences, Koin DI
        └── presentation/       # SettingsScreen, SettingsViewModel (MVI), Notifications section
```

---

## 🧩 Module Deep Dive

### 1. The `feature/` Directory
All actual product value lives here. Every new feature is physically sliced into three sub-modules:
* **`:domain` (The Rules):** Pure Kotlin. Cannot import Compose, Android, iOS, or Ktor.
* **`:data` (The Implementation):** Depends on `:domain`. Handles Room SQLite, APIs, and maps external exceptions into pure `DataError` objects via a `safeCall` wrapper.
* **`:presentation` (The Screens):** Depends on `:domain`. Contains Orbit-style MVI ViewModels and Jetpack Compose screens.

### 2. The `shared:core` Split
To prevent feature modules from importing heavy libraries they don't need, the core infrastructure is aggressively split:
* A `:feature:x:domain` module only needs `Result` classes, so it depends purely on `shared:core:domain`.
* A `:feature:x:data` module depends on its own `:domain`, `shared:core:data` (for Ktor/DataStore), and `shared:core:database` (for Room entities and DAOs).
* It will never accidentally pull in Compose UI because that lives in `core:presentation`.

### 3. The `shared:core:database`
The offline-first persistence layer for the entire app. Built with **Room for KMP** (`androidx.room`), it provides:
* **Eight Room entities:** `HabitEntity`, `HabitLogEntity`, `QuranBookmarkEntity`, `QuranDailyLogEntity`, `QuranGoalEntity`, `AthkarDailyLogEntity`, `TasbeehGoalEntity`, `TasbeehDailyTotalEntity`
* **Eight DAOs:** `HabitDao`, `HabitLogDao`, `QuranBookmarkDao`, `QuranDailyLogDao`, `QuranGoalDao`, `AthkarDailyLogDao`, `TasbeehGoalDao`, `TasbeehDailyTotalDao`
* A single `MudawamaDatabase` (current schema version **4**) with an `expect/actual` constructor pattern so Room KSP generates platform bridges automatically
* AutoMigration 2→3: removes `dailyGoalPages` and `pagesReadToday` from `quran_bookmarks`; adds `quran_daily_logs` and `quran_goals` tables
* AutoMigration 3→4: adds `athkar_daily_logs`, `tasbeeh_goals`, and `tasbeeh_daily_totals` tables (pure additions — no spec class needed)
* `AthkarCountersConverter` TypeConverter using `kotlinx-serialization-json` to serialize `Map<String, Int>` counter maps
* Platform-specific `getDatabaseBuilder()` functions (Android uses `Context`, iOS uses `NSHomeDirectory`)
* A Koin module per platform: `androidCoreDatabaseModule` / `iosCoreDatabaseModule()`

### 4. The `shared:core:time`
The single source of truth for all date and time operations across the app. **Features must never call `Clock.System.now()` directly** (SC-002). It provides:
* **`TimeProvider`** — Interface exposing `nowInstant()` and `logicalDate()`. Injected via Koin everywhere time is needed.
* **`RolloverPolicy`** — Data class encoding when the logical day resets: `Standard` (midnight, `offsetHour = 0`) or `fixed(hour)` for Islamic evening rollover (e.g., `fixed(18)` for Maghrib) or night-owl morning offset.
* **`SystemTimeProvider`** — The **only** production call site for `Clock.System.now()` in the entire codebase.
* **`FakeTimeProvider`** — Test double with a mutable `fixedInstant`, living in `commonMain` so any module's unit tests can freeze time deterministically.
* **`DateFormatters`** — Top-level helpers converting `Instant`/`LocalDate` to `"yyyy-MM-dd"` ISO strings for database storage.
* A Koin factory `timeModule(rolloverPolicy)` (defaults to `Standard`).

### 5. The `shared:navigation`
The structural skeleton of the app. Provides a single `MudawamaAppShell` composable — the only entry point platform shells (`androidApp`, `iosApp`) need to call. It delivers:
* **Type-safe routing** using JetBrains Navigation 3 (`navigation3-ui:1.0.0-alpha06`). Routes are `@Serializable data object` instances implementing a `sealed interface Route : NavKey`, making `when(route)` exhaustive at compile time.
* **`MudawamaBottomBar`** — a floating glassmorphism navigation bar with **4 tabs: Home, Prayers, Quran, Athkar** (80% opacity, 20dp blur, 28dp corner radius, 16dp horizontal float margin). Active tab is a rounded-square deep teal container with white icon + label; inactive tabs use `on-surface-variant`. Active tab is derived exclusively from `backStack.lastOrNull()` — no separate remembered state variable. The bottom bar is visible only on top-level routes (`HomeRoute`, `PrayerRoute`, `QuranRoute`, `AthkarRoute`); it is hidden on push destinations (`HabitsRoute`, `TasbeehRoute`, `SettingsRoute`).
* **`NavDisplay` + `entryProvider`** (Navigation 3) replacing the old `NavHost` / `NavController` pattern entirely.
* **Box overlay layout** — `MudawamaAppShell` uses a plain `Box` (not `Scaffold`) so the bottom bar floats over content. This prevents an opaque scaffold background from showing behind the glassmorphism bar. Feature screens add their own `statusBarsPadding()` and `96.dp` bottom spacer to account for the floating bar.
* **Back → Home pattern** — `AppBackHandler` (an `expect/actual` composable) is placed inside every non-Home `entryProvider` branch. On Android it wraps `androidx.activity.compose.BackHandler`; on iOS it is a no-op. When triggered, it calls `goHome()` which clears the back-stack and adds `HomeRoute`, ensuring any push destination (Settings, Habits, Tasbeeh) returns the user to the Home Dashboard.
* **`AppBackHandler` expect/actual** — declared in `shared/navigation/src/commonMain/` with platform implementations in `androidMain/` (wraps `BackHandler`) and `iosMain/` (no-op).
* **Full screen inventory** — all routes listed below correspond to reference UI screens in `docs/ui/`:

| Route | Type | `docs/ui/` reference |
|---|---|---|
| `OnboardingRoute` | Tab / initial | `welcome_to_mudawama.png` |
| `HomeRoute` | Tab (bottom bar) | `home_dashboard.png` |
| `PrayerRoute` | Tab (bottom bar) | `daily_prayer_tracker.png` |
| `QuranRoute` | Tab (bottom bar) | `quran_daily_reading_tracker.png` |
| `AthkarRoute` | Tab (bottom bar) | `daily_athkar_tracker.png` |
| `HabitsRoute` | Push destination (no bar) | `daily_habits.png` |
| `TasbeehRoute` | Push destination (no bar) | `tasbeeh_counter.png` |
| `SettingsRoute` | Push destination (no bar) | `settings.png` |

> **Navigation design**: `HomeRoute` renders `HomeScreen` (the Home Dashboard from `feature:home:presentation`). `HabitsRoute` and `TasbeehRoute` are push destinations navigated to from cards on the Home Dashboard — they are not bottom-bar tabs. `feature:home:presentation` has **no dependency on `shared:navigation`**; navigation is done via plain `() -> Unit` callbacks passed from `MudawamaAppShell`. `HomeUiEvent` uses a nested `sealed interface Navigate` with typed objects (`ToPrayer`, `ToAthkar`, `ToQuran`, `ToSettings`, `ToHabits`, `ToTasbeeh`).

* Uses `commonMain`, `androidMain`, and `iosMain` source sets (for `AppBackHandler` expect/actual).
* Depends on `shared:designsystem` via `api(…)` so consumers inherit `MudawamaTheme` tokens transitively.

### 6. The `shared:designsystem`
Contains all static resources via JetBrains Compose Resources (`strings.xml`, `.ttf` fonts, `.svg` icons) and the global `MudawamaTheme`. Every feature's `:presentation` module depends on this to ensure visual consistency.

#### Shared UI Components

Reusable Composables that live in `shared/designsystem/src/commonMain/kotlin/.../designsystem/components/` and are shared across feature modules:

| Component | File | Notes |
|---|---|---|
| `MudawamaSurfaceCard` | `SurfaceCard.kt` | Layout-agnostic card surface. `color = MaterialTheme.colorScheme.surface`, `shadowElevation = 1.dp`, `tonalElevation = 0.dp`. Accepts `shape` param (default `RoundedCornerShape(16.dp)`) and optional `onClick`. No forced inner padding. |
| `MudawamaBottomSheet` | `BottomSheet.kt` | App-wide bottom sheet wrapper. `containerColor = MudawamaTheme.colors.background`, `shape = RoundedCornerShape(topStart/End = 24.dp)`, `dragHandle = null`, `skipPartiallyExpanded = true`, 20dp top padding applied to content. Use for all feature bottom sheets. |
| `DateStrip` | `DateStrip.kt` | Horizontal 7-day date chip row. Shared across Prayer and Quran screens. |
| `PrimaryButton` | `PrimaryButton.kt` | Full-width primary CTA button. |
| `GhostButton` | `GhostButton.kt` | Outlined secondary button. |

#### Single source of truth for strings

There is exactly **one** `strings.xml` in the entire project:

```
shared/designsystem/src/commonMain/composeResources/values/strings.xml
```

All user-visible strings — nav tab labels, screen titles, section headers, button labels, error messages, content descriptions, format strings — live here. Feature modules MUST NOT create their own `strings.xml` or `composeResources/values/` directory.

#### Correct `Res` import path

The `mudawama.kmp.compose` convention plugin sets `packageOfResClass` to:

```
"mudawama." + gradlePath.trimStart(':').replace(':', '.')
```

For `:shared:designsystem` this yields `mudawama.shared.designsystem`. The correct import in every `.kt` file is therefore:

```kotlin
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.<string_key>   // individual key (optional, for IDE auto-import)
```

The legacy package `io.github.helmy2.mudawama.designsystem.generated.resources.Res` does **not** exist and will fail to compile. A `Res as DsRes` alias is also forbidden — there is only one `Res` in the project, so no alias is needed.

**`publicResClass = true`.** Compose Resources generates `Res` as `internal` by default. Because other modules import this `Res`, `shared/designsystem/build.gradle.kts` sets:

```kotlin
compose.resources {
    publicResClass = true
}
```

Any module whose `Res` is consumed outside its own compilation unit must do the same.

---

## ☂️ The Dual-Umbrella Strategy (iOS Exports)

Because Xcode requires a single compiled `.framework` to link against, we use "Umbrella" modules to aggregate our KMP code. We maintain two distinct umbrellas to support the project's evolution.

### Phase 1: `shared:umbrella-ui`
* **What it is:** The complete cross-platform application composition root.
* **Dependencies:** Aggregates all `feature:x:presentation` modules, depends on `shared:navigation` (which owns the routing graph), and the `designsystem`.
* **Usage:** Used to launch the MVP quickly. The iOS app simply instantiates a `ComposeUIViewController` to render the app via `MudawamaAppShell`.

### Phase 2: `shared:umbrella-core`
* **What it is:** The pure brain of the app (Zero UI).
* **Dependencies:** Aggregates *only* the `:domain` and `:data` modules. It explicitly hides Compose Multiplatform from the Swift compiler.
* **Usage:** When the team is ready to rewrite the UI in native SwiftUI for maximum performance, they simply swap their Xcode import from `MudawamaUI` to `MudawamaCore`. They immediately get access to all local databases and Use Cases, without inflating the iOS binary with the Compose runtime.

---

## 🚀 Dependency Rules (The Golden Path)

To prevent breaking the architecture, follow these strict dependency rules when writing `build.gradle.kts` files:

1. `domain` modules may **only** depend on `shared:core:domain`.
2. `data` modules must depend on their own `:domain`, `shared:core:data`, `shared:core:database`, and `shared:core:time` (for logical date stamping).
3. `presentation` modules must depend on their own `:domain`, `shared:core:presentation`, and `shared:designsystem`.
4. Feature modules may **never** depend on other feature modules' `:presentation` layers. `feature:home:presentation` is the sole **aggregator exception** — it explicitly depends on the `:domain`-only modules of `habits`, `prayer`, `athkar`, and `quran`. Depending on pure-Kotlin `:domain` modules is not a violation.
5. `shared:navigation` may only depend on `shared:designsystem` (via `api`) — it must never depend on feature modules or core infrastructure directly.
6. `feature:home:presentation` has **no dependency on `shared:navigation`** — navigation is performed via plain `() -> Unit` callbacks passed in from `MudawamaAppShell`, keeping the module decoupled from the routing graph.

---

## 🛠 Build System & Convention Plugins

Mudawama uses **Gradle Convention Plugins** located in the `build-logic` module to centralize build configuration. Each plugin has a single responsibility: applying and configuring Gradle plugins only. All dependencies are declared explicitly in each module's own `build.gradle.kts`.

### Convention Plugins:
* **`mudawama.kmp`**: Configures the base KMP toolchain — `kotlin.multiplatform`, `com.android.kotlin.multiplatform.library`, JVM 17, SDK versions, and iOS targets. No dependencies are injected.
* **`mudawama.kmp.compose`**: Extends `mudawama.kmp` with Compose Multiplatform — applies `org.jetbrains.compose` + `org.jetbrains.kotlin.plugin.compose`, sets the per-module `packageOfResClass`, and enables `androidResources` for `.cvr` asset packaging. No dependencies are injected.
* **`mudawama.kmp.koin`**: Dependency-shorthand plugin. Applies no Gradle plugins; injects `koin.bom` (platform), `bundles.koin`, and `koin.android` into every KMP module that uses Koin. Justified because all three declarations always travel together across 8+ modules.

> **Single-responsibility rule:** Convention plugins only apply/configure Gradle plugins. All other dependencies are declared explicitly in each module's own `build.gradle.kts`. The `mudawama.kmp.koin` exception is the only permitted dependency-injection shorthand.

---

## 💉 Dependency Injection (Koin)

Our Koin architecture follows the **Composition Root** pattern, ensuring that dependency injection is initialized at the highest level of the application, keeping lower layers decoupled from the DI lifecycle.

### The Flow
1. **Module Composition:** Each layer provides its own Koin definitions. Platform-specific implementations are provided via platform-specific modules:
   - `androidCoreDataModule` / `iosCoreDataModule(iosEncryptor)` — Ktor, DataStore, Encryptor, ConnectivityObserver
   - `androidCoreDatabaseModule` / `iosCoreDatabaseModule()` — `MudawamaDatabase` + all eight DAOs
   - `timeModule(rolloverPolicy)` — `TimeProvider` singleton (platform-agnostic; `commonMain` only)
2. **Umbrella Initialization:** The `umbrella-ui` module is the KMP composition root. It aggregates all module DI registrations and boots Koin.
3. **Native Launch:**
   - **Android:** `MudawamaApplication.onCreate()` calls `startKoin { androidContext(...); setupModules() }`, which registers the data, database, and time modules.
   - **iOS:** `iOSApp.swift` instantiates a Swift `IosEncryptor` and passes it into KMP via `KoinInitializerKt.initializeKoin(iosEncryptor:)`, which registers all three platform modules.

By restricting `startKoin` to the top-level composition root (the umbrella module or native apps), we ensure that feature modules can easily register their dependencies (such as Use Cases or ViewModels) into the DI graph without encountering race conditions or initialization limitations.