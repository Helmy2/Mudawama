# System Design Document: Mudawama (مُداوَمَة)

**Version:** 1.0.0  
**Date:** March 2026

---

## 1. Introduction

### 1.1 Purpose
This System Design Document specifies the technical architecture, data design, and component interactions for the Mudawama application. It acts as the technical blueprint for the engineering team and provides the structural context required for AI-driven development via GitHub Spec Kit to implement features correctly without violating architectural boundaries.

### 1.2 System Overview
Mudawama is an offline-first application built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP). It leverages a local Room database for data persistence, Ktor for network caching, Clean Architecture for strict module boundaries, Railway-Oriented Programming for error handling, and a custom Orbit-style MVI pattern for state management. The UI is built using Compose Multiplatform for both platforms, with a "dual-umbrella" framework design allowing a clean, future transition to native iOS SwiftUI.

---

## 2. System Architecture

### 2.1 High-Level Architecture (Clean Architecture)
The system strictly adheres to Clean Architecture principles, divided into three distinct layers, physically enforced by Gradle module boundaries:

1. **Presentation Layer:** Contains Jetpack Compose UI components and Orbit MVI ViewModels. This layer depends entirely on the Domain layer.
2. **Domain Layer:** The pure Kotlin core of the system. Contains business logic (Use Cases), Domain Models, and strict `Result<Data, Error>` wrappers. It has **zero dependencies** on the framework (Android/iOS) or data implementation.
3. **Data Layer:** Implements the Domain interfaces. Contains Ktor network clients, Room Database DAOs, Repositories, and the `safeCall` boundary that catches third-party exceptions and converts them into pure `DataError` objects.

_Dependency Rule:_ `Presentation -> Domain <- Data`

### 2.2 Multi-Module & Packaging Strategy
The repository uses a **"Packaging by Feature"** strategy to ensure horizontal scalability, prevent merge conflicts, and guarantee fast Gradle build times.

- **`build-logic`:** Houses custom Gradle convention plugins (e.g., `mudawama.kmp.library`, `mudawama.kmp.koin`, `mudawama.kmp.data`, `mudawama.kmp.presentation`) to centralize complex build configurations and enforce performance best practices (e.g., eager plugin application for Configuration Cache).
- **`shared/core`:** Contains the base `Result` classes, Error interfaces, Ktor client engines, and the `UiMessageManager` messaging queue.
- **`shared/feature/x`:** Each feature (e.g., `habits`, `prayer`) is split into independent `domain`, `data`, and `presentation` sub-modules.
- **`shared/umbrella-core`:** Aggregates all `domain` and `data` modules for iOS export (no UI). Used for future SwiftUI migration.
- **`shared/umbrella-ui`:** Aggregates all `presentation` modules and the design system for iOS export with Compose UI.
- **`androidApp` / `iosApp`:** Thin native shells hosting the application entry points.

---

## 3. Data Design (Room KMP, Ktor, & DataStore)

The application operates entirely offline using **Room for KMP** (`androidx.room` 2.7+). The schema normalises the definition of a habit (the rule) from its daily completion log (the action), and stores the user's Quran reading position as a singleton bookmark.

### 3.1 Entity: `HabitEntity` — `habits` table
| Column | Type | Notes |
|---|---|---|
| `id` | `String` (UUID) | Primary Key |
| `name` | `String` | Display name (e.g. "Fajr", "Read Quran") |
| `iconKey` | `String` | Icon identifier key |
| `type` | `String` | Enum: `BOOLEAN` or `COUNTER` |
| `category` | `String` | Enum: `PRAYER`, `QURAN`, `ATHKAR`, `CUSTOM` |
| `frequencyDays` | `String` | Comma-separated days e.g. `"MON,WED,FRI"` |
| `isCore` | `Boolean` | `true` = cannot be deleted by the user |
| `goalCount` | `Int?` | Nullable; only used when `type = COUNTER` |
| `createdAt` | `Long` | Unix timestamp millis |

### 3.2 Entity: `HabitLogEntity` — `habit_logs` table
| Column | Type | Notes |
|---|---|---|
| `id` | `String` (UUID) | Primary Key |
| `habitId` | `String` | FK → `habits.id` ON DELETE CASCADE |
| `date` | `String` | ISO date `"yyyy-MM-dd"` |
| `status` | `String` | Enum: `PENDING`, `COMPLETED`, `MISSED` |
| `completedCount` | `Int` | Repetitions for COUNTER habits; 0 for BOOLEAN |
| `loggedAt` | `Long` | Unix timestamp millis |

### 3.3 Entity: `QuranBookmarkEntity` — `quran_bookmarks` table
Singleton row (`id = 1` always).

| Column | Type | Notes |
|---|---|---|
| `id` | `Int` | Always 1 |
| `surah` | `Int` | 1–114 |
| `ayah` | `Int` | Ayah number within the surah |
| `dailyGoalPages` | `Int` | User's daily reading goal |
| `pagesReadToday` | `Int` | Resets each day via `resetDailyPages()` |
| `lastUpdated` | `Long` | Unix timestamp millis |

### 3.4 Database: `MudawamaDatabase`
- Single `RoomDatabase` class with `@ConstructedBy(MudawamaDatabaseConstructor::class)`
- Room KSP auto-generates the `actual object MudawamaDatabaseConstructor` for each platform
- Android builder uses `getDatabaseBuilder(Context)` with `getDatabasePath("mudawama.db")`
- iOS builder uses `getDatabaseBuilder()` with `NSHomeDirectory() + "/mudawama.db"`
- All DAOs use `suspend` functions and `Flow<>` for reactive reads

### 3.5 Data Flow & Caching Strategy
The Data Repository acts as the single source of truth. When a feature module's Use Case requests data, its repository queries the relevant DAO from `MudawamaDatabase`. If the data requires network enrichment (e.g. prayer times), the repository utilises the Ktor client to fetch from the remote API, persists the result locally, and returns it.

### 3.6 DataStore Preferences (`shared/core/data/session`)
- `theme_preference` (String) — LIGHT, DARK, SYSTEM
- `language_preference` (String) — EN, AR
- `calculation_method_id` (Int) — API-specific ID
- `daily_reset_preference` (String) — MIDNIGHT, MAGHRIB

### 3.7 Session & Cryptography (`shared/core/data/session`)
- Authentication tokens and sensitive user session data are stored securely using platform-specific cryptography. 
- **Android:** Leverages Google's Tink library (`TinkEncryptor`) coupled with Android Keystore (`AES256_GCM`) to encrypt data before persistence.
- **iOS:** Uses a custom `IosEncryptor` to encrypt data securely across the Swift/Kotlin boundary.

### 3.8 Network Connectivity Monitoring
- The `ConnectivityObserver` domain interface provides a `Flow<ConnectivityStatus>` to monitor network availability.
- **Android:** Uses `AndroidConnectivityObserver` powered by `ConnectivityManager.NetworkCallback`.
- **iOS:** Uses `IosConnectivityObserver` powered by Darwin's `NWPathMonitor`.

### 3.9 Time Provider & Logical Date (`shared/core/time`)
All date and time operations are centralized in this module. **No feature or data module may call `Clock.System.now()` directly** (SC-002 — enforced by a CI grep check). All time reads flow through the injected `TimeProvider`.

#### 3.9.1 `TimeProvider` Interface
```kotlin
interface TimeProvider {
    fun nowInstant(): Instant
    fun logicalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate
}
```
- `SystemTimeProvider` — the sole production `Clock.System` call site.
- `FakeTimeProvider(fixedInstant)` — ships in `commonMain`; any test can freeze time deterministically without platform setup.

#### 3.9.2 `RolloverPolicy`
Encodes when the *logical day* resets, satisfying FR-5.1 (configurable Islamic day boundary):

| `offsetHour` | Behaviour |
|---|---|
| `0` (`Standard`) | Logical day = calendar day. Day rolls at midnight. |
| `1–11` (morning offset) | Hours before `offsetHour` still belong to the *previous* logical day (night-owl mode). |
| `12–23` (evening offset) | Hours at or after `offsetHour` already belong to the *next* logical day (Islamic Maghrib-style). |

The `computeLogicalDate(calendarDate, hour, policy)` internal function implements the branching algorithm. The default policy is `RolloverPolicy.Standard`; callers can supply `RolloverPolicy.fixed(18)` for an 18:00 Maghrib rollover.

#### 3.9.3 `DateFormatters`
Top-level helpers used whenever an `Instant` or `LocalDate` is serialized to the database:
- `Instant.toIsoDateString(timeZone): String` → `"yyyy-MM-dd"`
- `LocalDate.toIsoString(): String` → `"yyyy-MM-dd"`

#### 3.9.4 DI
`timeModule(rolloverPolicy: RolloverPolicy = RolloverPolicy.Standard): Module` — registered at the composition root in `umbrella-ui`'s `KoinInitializer`. It is 100% `commonMain`; no platform-specific source sets are needed.

### 3.10 Navigation Shell (`shared/navigation`)
The single structural entry point for the entire application UI. Platform shells (`androidApp` → `MainActivity`, `iosApp` → `ContentView`) call exactly one composable: `MudawamaAppShell()`.

#### 3.10.1 Routing Model
Routes are defined as `@Serializable data object` instances implementing `sealed interface Route : NavKey` from JetBrains Navigation 3 (`navigation3-ui:1.0.0-alpha06`). The sealed hierarchy makes every `when(route)` in the rendering block exhaustive at compile time — adding a new route without handling it is a compiler error, not a runtime crash.

| Route | Destination |
|---|---|
| `HomeRoute` | Home placeholder → future Home feature screen |
| `PrayerRoute` | Prayer placeholder → future Prayer feature screen |
| `AthkarRoute` | Athkar placeholder → future Athkar feature screen |
| `HabitsRoute` | Habits placeholder → future Habits feature screen |

#### 3.10.2 Backstack Management
Navigation 3 `rememberNavBackStack` owns a `SnapshotStateList<NavKey>`. Tab switching uses a single-top guard:
```kotlin
if (backStack.lastOrNull() != route) { backStack.clear(); backStack.add(route) }
```
No `NavController`, `NavOptions`, or `launchSingleTop` are used. The `SavedStateConfiguration` with a polymorphic `SerializersModule` enables Compose's saved-state mechanism to survive process death.

#### 3.10.3 Floating Bottom Navigation Bar
`MudawamaBottomBar` derives the active tab solely from `backStack.lastOrNull()` (passed as `currentRoute: NavKey?`). There is no local `remember { mutableStateOf }` for tab selection — it is impossible for the UI indicator to desync from the real backstack.

Glassmorphism implementation: a layered `Box` where the background sub-layer applies `.background(surface.copy(alpha = 0.80f)).blur(20.dp)` and the foreground `NavigationBar` renders with `containerColor = Transparent`. The bar is "floating" via `padding(horizontal = 16.dp)` + `clip(RoundedCornerShape(28.dp))` and inset-safe via `windowInsetsPadding(WindowInsets.navigationBars)`.

#### 3.10.4 DI
No Koin module — `shared:navigation` is purely a UI shell with no injected services. DI is handled by the modules that own real feature ViewModels.

---

## 4. Component Design & State Management

### 4.1 Custom Orbit-style MVI (Presentation Layer)
ViewModels are platform-agnostic (residing in `shared/feature/x/presentation`) and utilize an Orbit-style MVI flow implemented via standard StateFlow and SharedFlow (no external Orbit library dependency).
- **`State`:** A single immutable data class representing the UI.
- **`Action`:** A sealed interface representing user intentions (e.g., `Action.ToggleHabit`).
- **`Event`:** A sealed interface for one-shot UI side effects (e.g., `Event.ShowConfetti`, `Event.NavigateBack`).
- **Flow:** The UI triggers an `Action`. The ViewModel's `onAction()` routes to an `intent { }` coroutine block. The block executes a Domain Use Case, mutates state synchronously using `reduce { copy(...) }`, and triggers side effects via `emitEvent()`.
- **Concurrency:** Frequent actions (e.g., rapidly toggling a habit checkbox) utilize `exclusiveIntent` to automatically cancel previous in-flight database write coroutines.

### 4.2 Railway-Oriented Error Handling (Domain Layer)
Exceptions are strictly forbidden in the Domain and Presentation layers.
- **`Result<D, E>`:** All Use Cases return either a `Success` containing data or a `Failure` containing a strongly typed error.
- **Exception Boundary (`safeCall`):** Room database and Ktor operations are wrapped in a `safeCall { }` block in the Data layer. Any `SQLiteException` or `IOException` is caught and mapped to a `DataError.Local` or `DataError.Remote` enum.

### 4.3 UI Messaging System (Core Layer)
Error messaging is decoupled from individual ViewModels via a Chain of Responsibility pattern.
- **`ErrorMapper`:** Each feature implements an `ErrorMapper` to translate its specific `BusinessError`s into localized `UiText`.
- **`UiErrorAggregator`:** A singleton that coordinates feature mappers and provides fallback translations for generic `DataError`s.
- **`UiMessageManager`:** A global singleton message queue containing `StateFlow<List<UiMessage>>`. When a ViewModel receives a `Failure` result, it maps the error and enqueues a `Snackbar`, `Toast`, or `Banner`. The global `UiMessageHost` composable observes this queue and renders the messages overlaying the app.

### 4.4 Logging
- **`MudawamaLogger`:** A unified multiplatform logging interface residing in the Domain layer to prevent platform logging imports coupling business logic.
- **Implementation:** `KermitLogger` inside the Data layer wraps Touchlab's Kermit to dispatch logs across all native platforms safely.

### 4.5 Permissions Management
- Centralized utilities (`rememberPermissionState` & `PermissionState`) live in the `core:presentation` Android specific source set. They cleanly map traditional `ActivityResultContracts.RequestPermission` outcomes into readable state machines (`Rationale`, `PermanentlyDenied`) for use directly inside Jetpack Compose screens.

---

## 5. Security & Privacy Design
- **Local Sandboxing:** Room database files (`mudawama.db`) and DataStore preferences are stored in the platform-specific secure app sandbox (`Context.filesDir` on Android, `NSDocumentDirectory` on iOS).
- **Network Scoping:** Network requests are strictly limited to the Aladhan API domain for prayer times. No analytic SDKs or third-party trackers are integrated, physically preventing data exfiltration and guaranteeing privacy.
