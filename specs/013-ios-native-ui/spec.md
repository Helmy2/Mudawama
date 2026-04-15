# Feature Specification: Full Native iOS App (SwiftUI)

**Feature Branch**: `013-ios-native-ui`  
**Created**: 2026-04-15  
**Status**: Draft  
**Input**: User description: "native ui for ios use the @docs/ARCHITECTURE.md — full ui and navigation using Swift, kotlin code for data and domain only, ios consumes via shared:umbrella-core (MudawamaCore framework), KMP not CMP"

## Overview

Replace the current iOS delivery model — which renders every screen through Compose Multiplatform embedded in a `UIViewController` via `MudawamaUI` — with a **fully native SwiftUI application** on iOS. All screens, navigation, animations, and platform interactions will be written in Swift/SwiftUI. The shared Kotlin layer is narrowed to **domain logic and data only**: use cases, repositories, Room-backed local storage, Ktor networking, and time utilities — all accessed through the `MudawamaCore` (`:shared:umbrella-core`) iOS framework.

The iOS Xcode target switches from linking `MudawamaUI` to linking `MudawamaCore`. The `MudawamaUI` framework and `:shared:umbrella-ui` Gradle module are **left completely unchanged** — Android continues to depend on `:shared:umbrella-ui` exactly as today and must not be modified. No Android source files, no Android build configuration, and no shared Kotlin presentation or navigation modules are touched by this migration.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - iOS Users Experience Fully Native UI (Priority: P1)

As an iOS user, every screen in Mudawama feels native — built with SwiftUI, respecting iOS design conventions, animations, gestures, and system integrations (Dynamic Type, Dark Mode, haptics, accessibility) — while continuing to display the same data and enforce the same business rules as the Android app.

**Why this priority**: This is the primary goal of the migration. A native SwiftUI app can take full advantage of iOS platform capabilities that are unavailable or degraded in Compose Multiplatform.

**Independent Test**: Can be fully tested by installing the iOS build on a device, navigating all screens, and verifying no Compose canvas is present — the app is a pure SwiftUI hierarchy. Delivers the baseline value of the migration.

**Acceptance Scenarios**:

1. **Given** the iOS app is installed, **When** a user opens it, **Then** the first screen is a SwiftUI view (not a Compose-wrapped `UIViewController`) and the `MudawamaUI` framework is not linked in the binary.
2. **Given** any screen in the app, **When** the user interacts with it, **Then** standard iOS gestures (swipe back, long-press, pull-to-refresh) work as expected without custom workarounds.
3. **Given** the device is in Dark Mode, **When** any screen is displayed, **Then** colors and assets adapt automatically using SwiftUI's native color system.
4. **Given** Dynamic Type is increased in iOS Settings, **When** any screen is displayed, **Then** text scales correctly without clipping or overflow.

---

### User Story 2 - All Existing Features Available on iOS (Priority: P1)

As an iOS user, every feature available in the current app — Home Dashboard, Prayer Tracker, Quran Reading Tracker, Athkar / Tasbeeh, Habits, Settings, and Qibla Compass — is accessible in the native iOS app with equivalent functionality.

**Why this priority**: Feature parity is a hard requirement; the migration must not regress any user capability.

**Independent Test**: Each feature screen can be tested independently against the existing Android behavior as the reference. A feature is considered passing when its acceptance scenarios match the Android equivalents.

**Acceptance Scenarios**:

1. **Given** the Home Dashboard screen, **When** it loads, **Then** it displays the next prayer time, Athkar summary, Quran progress, Tasbeeh summary, and Habits summary — all sourced from the Kotlin domain layer via `MudawamaCore`.
2. **Given** the Prayer Tracker screen, **When** a user marks a prayer as complete, **Then** the status is persisted via the Kotlin use case and reflected immediately in the UI.
3. **Given** the Quran screen, **When** a user logs their reading, **Then** the log is saved via the Kotlin use case and the reading streak updates correctly.
4. **Given** the Athkar screen, **When** a user completes a dhikr group, **Then** the completion is recorded via the Kotlin domain layer and the progress indicator updates.
5. **Given** the Tasbeeh screen, **When** a user taps the counter, **Then** the count increments and is persisted via the Kotlin use case.
6. **Given** the Settings screen, **When** a user changes the calculation method, **Then** the setting is saved via the Kotlin use case and prayer times recalculate accordingly.
7. **Given** the Qibla screen, **When** the device compass is available, **Then** the needle points toward Mecca using the angle computed by the Kotlin use case.

---

### User Story 3 - Native iOS Navigation (Priority: P1)

As an iOS user, navigation between screens uses SwiftUI's native navigation system — back swipe gestures, navigation stack, modal sheets — rather than a Kotlin-controlled back-stack.

**Why this priority**: Navigation is foundational; every other user story depends on it working correctly. Compose-managed navigation cannot be used since CMP is removed from iOS.

**Independent Test**: Can be fully tested by navigating to every screen and verifying swipe-back works, the navigation bar renders natively, and deep links from the Home Dashboard push the correct screen.

**Acceptance Scenarios**:

1. **Given** any pushed screen (Habits, Tasbeeh, Settings, Qibla), **When** the user swipes from the left edge, **Then** the screen is dismissed via native iOS swipe-back and the previous screen is shown.
2. **Given** the bottom tab bar, **When** a user taps a tab (Home, Prayer, Quran, Athkar), **Then** the corresponding SwiftUI view is shown instantly without a full re-render of unrelated tabs.
3. **Given** the Home Dashboard, **When** a user taps a summary card (e.g., Habits), **Then** the Habits screen is pushed onto the navigation stack with a native slide transition.
4. **Given** a pushed screen, **When** the user taps the back button, **Then** the back-stack pops to the previous screen — there is no Kotlin `goHome()` involved.

---

### User Story 4 - Kotlin Domain and Data Consumed via MudawamaCore (Priority: P1)

As a developer, all business logic and data access is provided by the `MudawamaCore` Kotlin framework — use cases, repositories, Room DAOs, Ktor API clients, and time utilities — exposed to Swift via KMP interop. No Compose or navigation Kotlin code is consumed by iOS.

**Why this priority**: This is the architectural constraint that enables the migration while preserving code sharing. Violations here would re-introduce CMP on iOS or duplicate business logic in Swift.

**Independent Test**: Can be fully tested by inspecting the iOS binary and confirming `MudawamaCore` is the only linked KMP framework (no `MudawamaUI`), and that all data displayed in the SwiftUI UI originates from Kotlin use case calls.

**Acceptance Scenarios**:

1. **Given** the iOS Xcode project, **When** the linked frameworks are inspected, **Then** `MudawamaCore` is linked and `MudawamaUI` is not.
2. **Given** a Swift view model (iOS-side), **When** it needs to load data, **Then** it calls a Kotlin use case directly (e.g., `GetNextPrayerUseCase`, `ObserveTodayAthkarUseCase`) — there is no duplicated business logic in Swift.
3. **Given** the Kotlin Room database, **When** data is written by a Kotlin use case on iOS, **Then** the data persists across app restarts and is visible to all SwiftUI screens that observe it.
4. **Given** a Kotlin use case that returns a `Result<D, E>`, **When** called from Swift, **Then** the Swift side handles both success and failure branches using idiomatic Swift error handling (enabled by SKIE or equivalent KMP interop tooling).

---

### User Story 5 - Notification and Platform Permissions (Priority: P2)

As an iOS user, Athkar notification reminders and location permission (for prayer times and Qibla) work correctly in the native iOS app, using the same Kotlin notification scheduling and permission infrastructure.

**Why this priority**: These are platform-critical integrations. Broken notifications would affect daily user engagement; broken location access would break prayer times and Qibla.

**Independent Test**: Can be tested independently by enabling Athkar notifications in Settings and verifying they fire at the configured time, and by granting location permission and verifying prayer times load.

**Acceptance Scenarios**:

1. **Given** Athkar notifications are enabled in Settings, **When** the configured hour arrives, **Then** the iOS notification fires with the correct title and body text sourced from the Kotlin layer.
2. **Given** location permission is not yet granted, **When** the user opens the Prayer or Qibla screen, **Then** a native iOS permission prompt is displayed; on grant, data loads immediately.
3. **Given** the user denies location permission, **When** they open the Prayer screen, **Then** a fallback UI is shown guiding them to iOS Settings — the app does not crash.

---

### User Story 6 - iOS-Specific UX Enhancements (Priority: P3)

As an iOS user, the native app takes advantage of iOS-only capabilities not available in Compose Multiplatform — including SF Symbols, native haptic feedback patterns, iOS system fonts, and iOS-standard sheet presentations.

**Why this priority**: These enhancements improve quality and platform fit but are additive; the app is usable without them.

**Independent Test**: Can be tested independently per enhancement (e.g., verify haptics fire on Tasbeeh tap, verify SF Symbols are used for icons).

**Acceptance Scenarios**:

1. **Given** the Tasbeeh counter, **When** the user taps the counter button, **Then** a haptic feedback pulse fires using the iOS haptic engine.
2. **Given** any bottom sheet (Log Reading, Set Goal, Athkar notification settings), **When** it is presented, **Then** it uses a native `sheet` or `fullScreenCover` presentation, not a custom overlay.
3. **Given** any icon in the app, **When** rendered on iOS, **Then** it uses an SF Symbol or a shared asset — no Compose-generated icon drawing code is present.

---

### Edge Cases

- What happens when a Kotlin use case returns an error (e.g., network failure for prayer times) — how is it surfaced in the SwiftUI layer?
- How does the app behave if `MudawamaCore` Koin initialisation has not completed before the first SwiftUI view attempts to call a use case?
- How are Kotlin `Flow` / `StateFlow` emissions observed in SwiftUI — polling, SKIE async streams, or Combine bridging?
- What happens if the Room database schema migration fails on app update — is the user notified or does the app silently reset data?
- How does the app handle the iOS app lifecycle (backgrounding, termination) in relation to in-flight Kotlin coroutines?
- What happens on first launch when no data exists yet (empty state for every screen)?
- How are localised strings handled — do they come from the Kotlin shared string resources or from iOS `Localizable.strings`?
- What happens when a Kotlin `suspend` function is called from the Swift main thread — are threading rules enforced by SKIE or manually?

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The iOS app MUST be a fully native SwiftUI application; the `MudawamaUI` Kotlin framework MUST NOT be linked to the iOS target after migration.
- **FR-002**: The iOS app MUST link only `MudawamaCore` as its KMP framework, consuming domain use cases, repository interfaces, and data layer implementations.
- **FR-003**: ALL screens currently available in the app MUST be re-implemented in SwiftUI: Home Dashboard, Prayer Tracker, Quran Reading Tracker, Athkar, Tasbeeh, Habits, Settings, and Qibla Compass.
- **FR-004**: App navigation MUST use SwiftUI's native navigation system (`NavigationStack`, `TabView`, `sheet`, `fullScreenCover`) — no Kotlin navigation or back-stack management.
- **FR-005**: The bottom tab bar MUST be a native SwiftUI `TabView` with four tabs: Home, Prayers, Quran, Athkar; push destinations (Habits, Tasbeeh, Settings, Qibla) MUST hide the tab bar when pushed.
- **FR-006**: Every data-fetching operation in SwiftUI view models MUST call the corresponding Kotlin use case via `MudawamaCore` — no business logic MUST be duplicated in Swift.
- **FR-007**: Kotlin `StateFlow` / `Flow` values MUST be observed in SwiftUI in a way that drives UI re-renders when new values are emitted (via SKIE async streams, Combine, or equivalent).
- **FR-008**: Koin initialisation (`KoinInitializerKt.initializeKoin(...)`) MUST complete before any SwiftUI view attempts to resolve a use case; the app MUST show a loading or splash state until initialisation is complete.
- **FR-009**: The `MudawamaCore` framework MUST export all use cases, repository interfaces, and models needed by each feature screen — the `umbrella-core/build.gradle.kts` MUST be updated to `export` the required feature `:domain` and `:data` modules.
- **FR-010**: Platform-native Swift implementations (Encryptor, LocationProvider, NotificationPermissionProvider) MUST continue to be passed into `initializeKoin()` as today.
- **FR-011**: All user-visible strings MUST be managed in a single source of truth; strings MUST NOT be duplicated between the Kotlin shared layer and iOS `Localizable.strings` — a clear ownership decision MUST be made and documented.
- **FR-012**: The `:shared:umbrella-ui` Gradle module and the `MudawamaUI` iOS framework MUST be left completely unchanged — no files inside it are modified. The Android app MUST remain fully compilable and functional, continuing to depend on `:shared:umbrella-ui` exactly as today.
- **FR-013**: The iOS app MUST support the same minimum iOS version as the current project (iOS 15+).
- **FR-014**: Notification scheduling (Athkar daily reminders) MUST continue to work via the Kotlin `NotificationScheduler` interface implementation in Swift, registered through `MudawamaCore`.
- **FR-015**: The Qibla screen MUST use native SwiftUI with compass data (heading, Qibla angle) computed by the Kotlin `CalculateQiblaAngleUseCase` and sensor readings from the iOS `CompassSensorManager`.

### Key Entities

- **MudawamaCore Framework**: The single KMP static framework linked by the iOS target after this migration. Aggregates and exports all feature `:domain` modules and their `:data` implementations. The iOS Xcode target switches its framework link from `MudawamaUI` to `MudawamaCore`; `MudawamaUI` itself is left untouched and continues to be used by Android.
- **SwiftUI View Model (per feature)**: An iOS-side `ObservableObject` (or `@Observable`) class responsible for calling Kotlin use cases, collecting `Flow` emissions, and exposing state to SwiftUI views. One per feature screen.
- **SwiftUI Screen (per feature)**: A native SwiftUI `View` struct for each app screen. Consumes its corresponding Swift view model. Contains no business logic.
- **iOS Navigation Root**: A SwiftUI `TabView` at the root, hosting `NavigationStack` instances per tab. Replaces `MudawamaAppShell` and the Kotlin navigation back-stack.
- **Koin Initialisation Bridge**: The existing `KoinInitializerKt.initializeKoin(...)` call in `iOSApp.swift`, extended to pass any additional platform providers required by the expanded `MudawamaCore` exports.
- **SKIE (or equivalent interop layer)**: The Kotlin/Swift interop tool that makes Kotlin `suspend` functions callable as `async` Swift functions and `Flow` collectible as `AsyncSequence` — critical for idiomatic Swift consumption of the Kotlin layer.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The iOS binary does not contain the Compose Multiplatform runtime — binary size decreases compared to the `MudawamaUI`-based build.
- **SC-002**: All 8 feature screens are available and functionally equivalent to their Android counterparts, verified by manual testing against a shared test matrix.
- **SC-003**: Native iOS navigation (swipe-back, tab switching) responds within one frame (< 16ms) — no dropped frames on navigation transitions on an iPhone running iOS 15+.
- **SC-004**: Users can complete any primary task (mark a prayer, log Quran reading, increment Tasbeeh) within the same number of taps as the Android app — no regression in task efficiency.
- **SC-005**: Athkar notifications fire within 60 seconds of the scheduled time on a real iOS device with the app in the background.
- **SC-006**: The Android build continues to compile and pass its existing test suite with zero modifications — `:shared:umbrella-ui` and all Android source files are untouched.
- **SC-007**: Zero Kotlin use case logic is duplicated in Swift — all business rules (streak calculation, prayer time fetching, Qibla angle formula) remain exclusively in the Kotlin domain layer.
- **SC-008**: App launch to first interactive screen takes no longer than 3 seconds on an iPhone SE (lowest supported hardware tier), measured from cold start.

---

## Assumptions

- The `MudawamaCore` framework will need its `build.gradle.kts` updated to `export` all feature `:domain` modules (habits, prayer, quran, athkar, settings, qibla) and their `:data` modules, in addition to the current `core:domain` and `core:time` exports. This is a Gradle change only — no Kotlin source changes are needed, and `:shared:umbrella-ui` is not modified.
- SKIE (already present in `umbrella-ui/build.gradle.kts`) will be added to `umbrella-core/build.gradle.kts` to provide idiomatic Swift interop (async/await for suspend functions, AsyncSequence for Flow). The `umbrella-ui/build.gradle.kts` SKIE configuration is left as-is.
- The existing Swift platform provider implementations (`IosEncryptor`, `IosLocationProvider`, `IosNotificationProvider`) are reused without modification.
- String ownership: iOS `Localizable.strings` will be the single source of truth for user-visible strings on iOS (since the Kotlin `Res` string system is part of Compose Resources, which lives in `MudawamaUI` — not `MudawamaCore`). This is a deliberate departure from the current shared-string approach and applies to iOS only; the Android shared-string approach is unchanged.
- The `IosQiblaViewControllerProvider` pattern is superseded — the Qibla screen will be a pure SwiftUI view consuming Kotlin sensor data and use case results directly.
- All Android modules — `feature:x:presentation`, `shared:navigation`, `shared:designsystem`, `shared:umbrella-ui` — are entirely untouched by this migration.
- The existing `ContentView.swift` (which wraps `MainKt.MainViewController()`) will be replaced by a SwiftUI root view — `iOSApp.swift` entry point remains but its `body` changes to render native SwiftUI.
- The migration is a full replacement, not an incremental screen-by-screen approach; the iOS app will not be in a hybrid state (some screens Compose, some SwiftUI) at any point during development on the feature branch.
