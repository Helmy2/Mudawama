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
│   │   ├── data/               # Ktor client setup, Room DB builders, Tink/Platform Session Encryptors
│   │   └── presentation/       # Custom MVI BaseViewModels, UiMessageManager, Permission State Composables
│   │
│   ├── designsystem/           # Compose themes, typography, localized strings, icons
│   │
│   ├── umbrella-core/          # iOS Export: Pure Business Logic (.framework)
│   └── umbrella-ui/            # iOS Export: Compose UI & NavGraph (.framework)
│
└── feature/                    # ALL product features live here
    ├── habits/
    │   ├── domain/             # Models, UseCases, Repository Interfaces
    │   ├── data/               # Room Entities, DAOs, API Calls
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
* **`:presentation` (The Screens):** Depends on `:domain`. Contains Orbit MVI ViewModels and Jetpack Compose screens.

### 2. The `shared:core` Split
To prevent feature modules from importing heavy libraries they don't need, the core infrastructure is aggressively split:
* A `:feature:x:domain` module only needs `Result` classes, so it depends purely on `shared:core:domain`.
* It will never accidentally pull in Ktor or Jetpack Compose because those live in `core:data` and `core:presentation`.

### 3. The `shared:designsystem`
Contains all static resources via JetBrains Compose Resources (`strings.xml`, `.ttf` fonts, `.svg` icons) and the global `MudawamaTheme`. Every feature's `:presentation` module depends on this to ensure visual consistency.

---

## ☂️ The Dual-Umbrella Strategy (iOS Exports)

Because Xcode requires a single compiled `.framework` to link against, we use "Umbrella" modules to aggregate our KMP code. We maintain two distinct umbrellas to support the project's evolution.

### Phase 1: `shared:umbrella-ui`
* **What it is:** The complete cross-platform application.
* **Dependencies:** Aggregates all `feature:x:presentation` modules, the NavHost, and the `designsystem`.
* **Usage:** Used to launch the MVP quickly. The iOS app simply instantiates a `ComposeUIViewController` to render the app.

### Phase 2: `shared:umbrella-core`
* **What it is:** The pure brain of the app (Zero UI).
* **Dependencies:** Aggregates *only* the `:domain` and `:data` modules. It explicitly hides Compose Multiplatform from the Swift compiler.
* **Usage:** When the team is ready to rewrite the UI in native SwiftUI for maximum performance, they simply swap their Xcode import from `MudawamaUI` to `MudawamaCore`. They immediately get access to all local databases and Use Cases, without inflating the iOS binary with the Compose runtime.

---

## 🚀 Dependency Rules (The Golden Path)

To prevent breaking the architecture, follow these strict dependency rules when writing `build.gradle.kts` files:

1. `domain` modules may **only** depend on `shared:core:domain`.
2. `data` modules must depend on their own `:domain`, plus `shared:core:data`.
3. `presentation` modules must depend on their own `:domain`, `shared:core:presentation`, and `shared:designsystem`.
4. Feature modules may **never** depend on other feature modules. (If features must communicate, they do so via deep-linking in the `umbrella-ui` NavHost or via shared IDs).

---

## 💉 Dependency Injection (Koin)

Our Koin architecture follows the **Composition Root** pattern, ensuring that dependency injection is initialized at the highest level of the application, keeping lower layers decoupled from the DI lifecycle.

### The Flow
1. **Module Composition:** Each layer provides its own Koin definitions (e.g., `coreDataModule`). Platform-specific implementations (like `DataStore` or native encryptors) are provided via `androidCoreDataModule` and `iosCoreDataModule`, which internally use `includes(coreDataModule)` to cleanly bundle the common logic.
2. **Umbrella Initialization:** The `umbrella-ui` module serves as the primary KMP composition root. It aggregates the data modules and prepares the DI container for any future UI-level elements like ViewModels.
3. **Native Launch:**
   - **Android:** The `MudawamaApplication` class safely calls `startKoin { setupModules() }`.
   - **iOS:** The native `iOSApp.swift` instantiates the Swift-specific `IosEncryptor` and passes it into KMP via `KoinInitializerKt.initializeKoin(iosEncryptor: swiftEncryptor)`, allowing `umbrella-ui` to configure and execute `startKoin`.

By restricting `startKoin` to the top-level composition root (the umbrella module or native apps), we ensure that feature modules can easily register their dependencies (such as Use Cases or ViewModels) into the DI graph without encountering race conditions or initialization limitations.