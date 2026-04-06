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
    ├── habits/
    │   ├── domain/             # Models, UseCases, Repository Interfaces
    │   ├── data/               # DAOs via shared:core:database, API Calls
    │   └── presentation/       # Jetpack Compose UI, ViewModels
    │
    └── prayer/
        ├── domain/
        ├── data/
        └── presentation/
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
* Three Room entities: `HabitEntity`, `HabitLogEntity`, `QuranBookmarkEntity`
* Three DAOs: `HabitDao`, `HabitLogDao`, `QuranBookmarkDao`
* A single `MudawamaDatabase` with an `expect/actual` constructor pattern so Room KSP generates platform bridges automatically
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
* **`MudawamaBottomBar`** — a floating glassmorphism navigation bar with **4 tabs: Home, Prayers, Quran, Athkar** (80% opacity, 20dp blur, 28dp corner radius, 16dp horizontal float margin). Active tab is a rounded-square deep teal container with white icon + label; inactive tabs use `on-surface-variant`. Active tab is derived exclusively from `backStack.lastOrNull()` — no separate remembered state variable.
* **`NavDisplay` + `entryProvider`** (Navigation 3) replacing the old `NavHost` / `NavController` pattern entirely.
* **Full screen inventory** — all routes listed below correspond to reference UI screens in `docs/ui/`:

| Route | Screen | `docs/ui/` reference |
|---|---|---|
| `OnboardingRoute` | Welcome / Onboarding | `welcome_to_mudawama.png` |
| `HomeRoute` | Home Dashboard | `home_dashboard.png` |
| `PrayerRoute` | Today's Prayers | `daily_prayer_tracker.png` |
| `QuranRoute` | Quran Reading Tracker | `quran_daily_reading_tracker.png` |
| `AthkarRoute` | Daily Athkar | `daily_athkar_tracker.png` |
| `HabitsRoute` | Daily Habits | `daily_habits.png` |
| `TasbeehRoute` | Tasbeeh Counter | `tasbeeh_counter.png` |
| `InsightsRoute` | Insights / Progress | `insights_progress.png` |
| `SettingsRoute` | Settings | `settings.png` |

* 100% `commonMain` code — no `androidMain` or `iosMain` source sets.
* Depends on `shared:designsystem` via `api(…)` so consumers inherit `MudawamaTheme` tokens transitively.

### 6. The `shared:designsystem`
Contains all static resources via JetBrains Compose Resources (`strings.xml`, `.ttf` fonts, `.svg` icons) and the global `MudawamaTheme`. Every feature's `:presentation` module depends on this to ensure visual consistency.

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
4. Feature modules may **never** depend on other feature modules. (If features must communicate, they do so via deep-linking in the `shared:navigation` routing graph or via shared IDs).
5. `shared:navigation` may only depend on `shared:designsystem` (via `api`) — it must never depend on feature modules or core infrastructure directly.

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
   - `androidCoreDatabaseModule` / `iosCoreDatabaseModule()` — `MudawamaDatabase` + all three DAOs
   - `timeModule(rolloverPolicy)` — `TimeProvider` singleton (platform-agnostic; `commonMain` only)
2. **Umbrella Initialization:** The `umbrella-ui` module is the KMP composition root. It aggregates all module DI registrations and boots Koin.
3. **Native Launch:**
   - **Android:** `MudawamaApplication.onCreate()` calls `startKoin { androidContext(...); setupModules() }`, which registers the data, database, and time modules.
   - **iOS:** `iOSApp.swift` instantiates a Swift `IosEncryptor` and passes it into KMP via `KoinInitializerKt.initializeKoin(iosEncryptor:)`, which registers all three platform modules.

By restricting `startKoin` to the top-level composition root (the umbrella module or native apps), we ensure that feature modules can easily register their dependencies (such as Use Cases or ViewModels) into the DI graph without encountering race conditions or initialization limitations.