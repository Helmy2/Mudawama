# Feature Specification: shared:navigation — App Shell, Bottom Navigation Bar & Routing Graph

**Feature Branch**: `004-shared-navigation-shell`
**Created**: 2026-04-04
**Status**: Draft
**Input**: User description: "Build shared:navigation module — App Shell, bottom navigation bar, and routing graph with Navigation 3"

---

## Overview

**Purpose**: Introduce the `shared:navigation` module — the entry point that wraps the entire app in the Mudawama theme, hosts the main navigation graph, and renders the floating glassmorphism bottom navigation bar. This module is the structural skeleton every screen plugs into; it must be working and visually correct before any real feature screen is connected.

**Goals**:
- Provide a single `MudawamaAppShell` composable that the platform entry points (`androidApp`, `iosApp`) call to launch the app.
- Define a type-safe routing graph using Navigation 3 with serialisable route objects.
- Implement the custom floating bottom navigation bar from the Design System specification with automatic active-state tracking.
- Supply placeholder screen composables (Prayer, Athkar, Quran) so the graph and navigation bar can be exercised end-to-end immediately; the Home tab renders `HabitsScreen` directly (no placeholder).

**Scope**: `shared/navigation/` — 100% `commonMain` code. No platform-specific source sets. No business logic; UI shell only.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — App launches and the bottom navigation bar is visible (Priority: P1)

A developer runs the app on Android or iOS immediately after this module is wired up. They see the themed app shell with the floating glassmorphism bottom navigation bar at the bottom of the screen. Four tab items are visible. The first tab (Home) is shown in its selected (active) state.

**Why this priority**: The app shell is the first visual artefact every developer and tester interacts with. Without it, no feature screen can be shown in context.

**Independent Test**: Can be fully tested by running the app and confirming the bottom bar renders with four items, the correct icon/label for each, and the Home item selected by default.

**Acceptance Scenarios**:

1. **Given** the app is freshly launched, **When** the main composable renders, **Then** the bottom navigation bar is visible with four items: Home, Prayer, Quran, and Athkar.
2. **Given** the bottom bar is displayed, **When** no tab has been tapped yet, **Then** the Home tab is in the selected visual state and the other three are unselected.
3. **Given** the bottom bar is displayed, **When** a user looks at the bar, **Then** the bar appears "floating" — it does not span edge-to-edge but has visible spacing around it, and has a translucent glassmorphism appearance.

---

### User Story 2 — Tapping a tab navigates to the corresponding placeholder screen (Priority: P1)

A developer taps the "Prayer" tab. The visible content area switches to the Prayer placeholder screen and the Prayer tab item changes to its selected visual state while the previously selected tab reverts to unselected.

**Why this priority**: Core navigation correctness. If tab switching does not work the entire routing graph is broken.

**Independent Test**: Can be tested by tapping each of the four tabs in sequence and verifying that: (a) the content area changes, and (b) only the tapped tab shows as selected.

**Acceptance Scenarios**:

1. **Given** the Home screen is displayed and Home tab is selected, **When** the user taps the Prayer tab, **Then** the Prayer placeholder screen is shown and the Prayer tab becomes selected.
2. **Given** any tab is selected, **When** the user taps a different tab, **Then** the previously selected tab reverts to the unselected visual state.
3. **Given** any tab is selected, **When** the user taps the already-selected tab, **Then** no navigation occurs and the current screen remains visible (no duplicate back-stack entries).

---

### User Story 3 — Selected tab state is driven by the navigation backstack, not local state (Priority: P2)

A developer or QA engineer navigates forward via code (e.g., in a test or via deep-link). The bottom navigation bar's selected item updates automatically to reflect the current destination — it is not controlled by a remembered local variable that could diverge from the actual route.

**Why this priority**: If the selected tab diverges from the real backstack the UI becomes misleading and deeper navigation bugs become very hard to diagnose.

**Independent Test**: Can be tested by programmatically navigating to a specific route and asserting that the corresponding tab in the bottom bar shows as selected without any explicit tab-selection call.

**Acceptance Scenarios**:

1. **Given** the app is showing the Home screen, **When** the backstack entry changes to the Athkar route (e.g., via a programmatic call), **Then** the Athkar tab in the bottom bar automatically shows as selected.
2. **Given** the backstack entry is observed by the bottom bar, **When** the current destination does not match any of the four top-level routes, **Then** no tab is shown as selected (graceful fallback).

---

### User Story 4 — The app shell is wrapped in MudawamaTheme (Priority: P2)

Any screen rendered inside `MudawamaAppShell` automatically inherits the Mudawama design tokens — the primary teal color, surface colors, typography, and shapes — without each screen needing to declare the theme independently.

**Why this priority**: The theme wrapper prevents screens from rendering with default Material 3 colors, which would look inconsistent and incorrect.

**Independent Test**: Can be tested by reading `MudawamaTheme.colors.primary` inside any placeholder screen and asserting it equals the expected `#004f45` teal value.

**Acceptance Scenarios**:

1. **Given** `MudawamaAppShell` is rendered, **When** any child composable accesses `MudawamaTheme.colors.primary`, **Then** it receives the project's defined primary teal color, not the default Material 3 purple.
2. **Given** the system is in dark mode, **When** `MudawamaAppShell` is rendered, **Then** child composables receive the dark-mode color tokens.

---

### Edge Cases

- What happens when the device has a large display cutout or system gesture bar that overlaps the floating bottom bar?
  → The bottom bar should respect the window's `WindowInsets` bottom padding so system UI does not obscure the bar.
- What happens when the backstack is popped all the way to empty (e.g., all screens popped)?
  → The bottom bar gracefully shows no tab selected; the app does not crash.
- What if the user rapidly taps two different tabs in quick succession?
  → Navigation must be idempotent for same-destination taps; rapid multi-destination taps may produce intermediate states but must not corrupt the backstack.
- What if one of the four placeholder screens takes too long to compose on a very slow device?
  → Placeholder screens are intentionally trivial and must compose synchronously with no state loading.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The module MUST expose a `MudawamaAppShell` composable that is the single entry point called by platform host composables (Android `MainActivity`, iOS `ContentView`).
- **FR-002**: `MudawamaAppShell` MUST wrap all content inside `MudawamaTheme` from `shared:designsystem`, passing `darkTheme` derived from `isSystemInDarkTheme()`.
- **FR-003**: The routing graph MUST use the KMP port of Google Navigation 3 (`androidx.navigation3` / `org.jetbrains.compose.navigation:navigation-compose`) with type-safe routes defined as `@Serializable` Kotlin objects or data classes via `kotlinx-serialization`.
- **FR-004**: The module MUST define exactly four top-level route objects: `HomeRoute`, `PrayerRoute`, `AthkarRoute`, and `QuranRoute`. There is no `HabitsRoute` — Daily Habits is served by `HomeRoute`.
- **FR-005**: The routing graph MUST include a `NavDisplay` with one entry per top-level route. The `HomeRoute` branch MUST render the real `HabitsScreen` composable (passed in as a lambda parameter); other unimplemented routes render placeholder composables.
- **FR-006**: The module MUST provide placeholder screen composables (`QuranPlaceholderScreen`, `AthkarPlaceholderScreen`) for routes whose real screens are not yet implemented. Each MUST display at minimum the screen's name as a centred text label, rendered using `MudawamaTheme` typography and color tokens. `HomePlaceholderScreen` and `HabitsPlaceholderScreen` have been removed — they are no longer needed.
- **FR-007**: The module MUST implement a `MudawamaBottomBar` composable that renders four navigation items (Home, Prayer, Quran, Athkar), each with an icon and a label.
- **FR-008**: `MudawamaBottomBar` MUST derive the selected tab solely from the current `NavBackStackEntry` (or equivalent Navigation 3 backstack state) — no separate remembered local variable for the active tab index.
- **FR-009**: `MudawamaBottomBar` MUST apply glassmorphism styling: the bar surface MUST render at 80% opacity (α = 0.80) with a visible background-blur effect where platform APIs permit, following DESIGN.md §2 "Floating Navigation".
- **FR-010**: `MudawamaBottomBar` MUST appear "floating" — it MUST have horizontal padding and a rounded shape so it does not span edge-to-edge; its outer edges must not touch the screen sides.
- **FR-011**: `MudawamaBottomBar` MUST respect bottom window insets so the bar is not obscured by the system navigation bar or home indicator.
- **FR-012**: Tapping a tab that is already the current destination MUST NOT push a duplicate entry onto the backstack; navigation to the same route must use `launchSingleTop` semantics or equivalent.
- **FR-013**: All source code MUST reside in `commonMain`; no `androidMain` or `iosMain` source sets are permitted in this module.
- **FR-014**: The module MUST depend on `shared:designsystem` for all color, typography, and shape tokens — no hardcoded color or dimension literals in any composable.
- **FR-015**: Each public composable MUST have a companion `@Preview`-annotated preview function following the project convention (preview wrapper pattern from the constitution).

### Key Entities

- **Route**: A `@Serializable` Kotlin object or data class that uniquely identifies a navigation destination. There are four top-level routes (`HomeRoute`, `PrayerRoute`, `QuranRoute`, `AthkarRoute`). Routes carry no mutable state.
- **MudawamaAppShell**: The root composable that owns the `NavDisplay` and `Scaffold`, wraps children in `MudawamaTheme`, and passes the bottom bar as the `bottomBar` slot. It accepts `habitsScreen` and `prayerScreen` composable lambdas so real feature screens can be injected without creating a direct dependency on feature modules.
- **MudawamaBottomBar**: A stateless composable that receives the current destination and a navigate callback; it renders the four tab items with active-state tracking and highlights the active tab.
- **Placeholder Screen**: A trivial composable that renders a centred text label. Exists solely to verify the routing graph before real feature screens are connected. Only `QuranPlaceholderScreen` and `AthkarPlaceholderScreen` remain; Home and Prayer are served by real screens.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All four tabs are tappable and navigate to distinct screens within a single run of the app on both Android and iOS — verified by manual smoke test. Home tab shows Daily Habits; Prayer tab shows Today's Prayers; Quran and Athkar show placeholders.
- **SC-002**: The selected tab in the bottom bar always reflects the current navigation destination; no visible desync is observed during tap tests covering all four tabs in every order.
- **SC-003**: The bottom bar is visually "floating" — its horizontal edges have at least 16 dp of clear space from the screen boundary on both Android and iOS; confirmed by visual inspection.
- **SC-004**: The glassmorphism surface renders at the specified 80% opacity; confirmed by capturing the composable in a screenshot preview and inspecting its alpha value.
- **SC-005**: The module compiles and all Compose previews render without errors on both Android and iOS targets.
- **SC-006**: The entire `shared:navigation` module contains no more than 10 source files in `commonMain`, keeping the shell minimal and focused.
- **SC-007**: Back-stack correctness — tapping a tab that is already active does not increase the back-stack depth; confirmed by asserting the backstack size before and after a duplicate-tap.

---

## Assumptions

- Navigation 3 (`androidx.navigation3` or the equivalent Compose Multiplatform navigation artifact) is available or will be added to the version catalog as part of implementing this spec; no other navigation library is introduced.
- `kotlinx-serialization` is already available in the project (listed in `libs.versions.toml` as `kotlinx-serialization-json`); the `kotlinx.serialization` Gradle plugin will be applied to `shared:navigation` so that `@Serializable` routes compile.
- Platform entry points (`androidApp` and `iosApp`) currently call `App()` from `shared:umbrella-ui`; after delivery they will call `MudawamaAppShell()` from `shared:navigation` directly (or via the umbrella module). Migration of call sites is out of scope for this spec.
- The backdrop-blur portion of the glassmorphism effect may render only on platforms whose Compose runtime supports native blur APIs. On platforms without hardware blur support, a solid semi-transparent surface at 80% opacity is the accepted fallback.
- Icon assets for the four navigation tabs (Home, Prayer, Athkar, Habits) are assumed to exist in `shared:designsystem` resources. If absent, Material3 icon alternatives from the Compose Material3 icons bundle are an acceptable temporary substitute.
- There is no deep-linking requirement for this iteration; the `NavHost` uses in-memory backstack management only.
- The module uses the `mudawama.kmp.presentation` convention plugin (same as `shared:designsystem`) and is registered in `settings.gradle.kts` as `:shared:navigation`.
