# System Design Document: Mudawama (مُداوَمَة)

**Version:** 1.0.0  
**Date:** March 2026

---

## 1. Introduction

### 1.1 Purpose
This System Design Document specifies the technical architecture, data design, and component interactions for the Mudawama application. It acts as the technical blueprint for the engineering team and provides the structural context required for AI-driven development via GitHub Spec Kit to implement features correctly without violating architectural boundaries.

### 1.2 System Overview
Mudawama is an offline-first application built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP). It leverages a local Room database for data persistence, Ktor for network caching, Clean Architecture for strict module boundaries, Railway-Oriented Programming for error handling, and an Orbit-style MVI pattern for state management. The UI is built using Compose Multiplatform for both platforms, with a "dual-umbrella" framework design allowing a clean, future transition to native iOS SwiftUI.

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

- **`build-logic`:** Houses custom Gradle convention plugins (e.g., `mudawama.kmp.compose`, `mudawama.kmp.room`) to centralize complex build configurations.
- **`shared/core`:** Contains the base `Result` classes, Error interfaces, Ktor client engines, and the `UiMessageManager` messaging queue.
- **`shared/feature/x`:** Each feature (e.g., `habits`, `prayer`) is split into independent `domain`, `data`, and `presentation` sub-modules.
- **`shared/umbrella-core`:** Aggregates all `domain` and `data` modules for iOS export (no UI). Used for future SwiftUI migration.
- **`shared/umbrella-ui`:** Aggregates all `presentation` modules and the design system for iOS export with Compose UI.
- **`androidApp` / `iosApp`:** Thin native shells hosting the application entry points.

---

## 3. Data Design (Room, Ktor, & DataStore)

The application operates primarily offline using KMP-compatible Room. The database normalizes the definition of a habit (the rule) from the daily completion log (the action), while also caching external API data.

### 3.1 Entity: `HabitEntity`
- `id` (String, UUID, Primary Key)
- `title` (String) - e.g., "Fajr", "Read Quran", "Duha"
- `type` (Enum) - `PRAYER`, `QURAN`, `FASTING`, `CUSTOM`
- `frequencyType` (Enum) - `DAILY`, `SPECIFIC_DAYS`
- `frequencyValue` (String) - e.g., "1,4" for Monday/Thursday
- `createdAt` (Long, Timestamp)

### 3.2 Entity: `DailyLogEntity`
- `id` (String, UUID, Primary Key)
- `habitId` (String, UUID, Foreign Key referencing `HabitEntity`)
- `date` (String) - ISO-8601 date string (e.g., "2026-03-18")
- `status` (Enum) - `PENDING`, `COMPLETED`, `COMPLETED_JAMAAH`, `MISSED`
- `metricValue` (Int, Nullable) - Used for variable tracking (e.g., number of Quran pages read)
- `updatedAt` (Long, Timestamp)

### 3.3 Entity: `PrayerTimeCacheEntity`
- `date` (String, Primary Key) - Format: "DD-MM-YYYY"
- `fajr` (String)
- `dhuhr` (String)
- `asr` (String)
- `maghrib` (String)
- `isha` (String)

### 3.4 Data Flow & Caching Strategy
The Data Repository acts as the single source of truth. When the `GetPrayerTimesUseCase` requests times, the repository queries `PrayerTimeCacheEntity`. If the data exists, it returns immediately. If missing, it utilizes the `Ktor` client to fetch a full month from `http://api.aladhan.com/v1/calendarByCity`, saves it to the Room database, and returns the requested date.

### 3.5 DataStore Preferences (`shared/core/datastore`)
- `theme_preference` (String) - LIGHT, DARK, SYSTEM
- `language_preference` (String) - EN, AR
- `calculation_method_id` (Int) - API specific ID (e.g., 5 for Egyptian General Authority of Survey)
- `daily_reset_preference` (String) - MIDNIGHT, MAGHRIB

---

## 4. Component Design & State Management

### 4.1 Orbit-Style MVI (Presentation Layer)
ViewModels are platform-agnostic (residing in `shared/feature/x/presentation`) and utilize an Orbit-style MVI flow.
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

---

## 5. Security & Privacy Design
- **Local Sandboxing:** Room database files (`mudawama.db`) and DataStore preferences are stored in the platform-specific secure app sandbox (`Context.filesDir` on Android, `NSDocumentDirectory` on iOS).
- **Network Scoping:** Network requests are strictly limited to the Aladhan API domain for prayer times. No analytic SDKs or third-party trackers are integrated, physically preventing data exfiltration and guaranteeing privacy.
