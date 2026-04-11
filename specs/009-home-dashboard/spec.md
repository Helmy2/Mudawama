# Feature Specification: Home Dashboard

**Feature Branch**: `009-home-dashboard`  
**Created**: 2026-04-11  
**Status**: Complete ✅  
**Input**: User description: "Enhance the existing Home screen to display summary cards for all completed features alongside the existing Habits list."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — At-a-Glance Daily Overview (Priority: P1)

A user opens the app and lands on the Home screen. Without switching tabs, they can immediately see their next prayer time, whether they've completed morning/evening Athkar, their Quran reading progress for today, and their habits list — all in one scrollable view.

**Why this priority**: This is the core value of the feature. Consolidating cross-feature status on the Home screen reduces tab-switching friction and makes the app's daily-use loop immediately visible.

**Independent Test**: Open the Home screen and verify that all four sections (next prayer, Athkar status, Quran ring, habits) appear. No navigation to other tabs required. Delivering just this read-only layout provides immediate value.

**Acceptance Scenarios**:

1. **Given** the user has location permission granted and prayer times loaded, **When** they open the Home screen, **Then** a "Next Prayer" card displays the name and scheduled time of the upcoming prayer.
2. **Given** the user has completed morning Athkar today, **When** they view the Home screen, **Then** the Athkar card shows Morning as completed and Evening as pending (or vice versa based on actual status).
3. **Given** the user has read 3 out of 5 goal pages today, **When** they view the Home screen, **Then** the Quran card shows a progress ring at 60% with the page count.
4. **Given** no data has been loaded yet (loading state), **When** the Home screen opens, **Then** each summary card shows a skeleton/placeholder state rather than blank or crashing.

---

### User Story 2 — Tap-to-Navigate from Summary Cards (Priority: P2)

The user taps any summary card and is taken directly to the corresponding feature tab (Prayer, Athkar, or Quran). The habits section remains in place and is not tappable as a "card" (it is the native home content).

**Why this priority**: Cards that are read-only reduce value if the user cannot act on them. Quick navigation closes the loop between awareness and action.

**Independent Test**: Tap each of the three summary cards in isolation and confirm navigation to the correct tab. Can be tested even before the full data binding is complete.

**Acceptance Scenarios**:

1. **Given** the user is on the Home screen, **When** they tap the Next Prayer card, **Then** the app navigates to the Prayer tab.
2. **Given** the user is on the Home screen, **When** they tap the Athkar card, **Then** the app navigates to the Athkar tab.
3. **Given** the user is on the Home screen, **When** they tap the Quran card, **Then** the app navigates to the Quran tab.

---

### User Story 3 — Settings Navigation (Priority: P3)

A Settings icon button appears in the Home screen top app bar. Tapping it navigates the user to a Settings placeholder screen. The placeholder communicates that full settings are coming soon.

**Why this priority**: Provides the route foundation for the future Settings feature without blocking the core dashboard work.

**Independent Test**: Tap the Settings icon and verify the placeholder screen appears and the user can navigate back. Can be tested independently of all summary card work.

**Acceptance Scenarios**:

1. **Given** the user is on the Home screen, **When** they tap the Settings icon in the top bar, **Then** a Settings screen (placeholder) is displayed.
2. **Given** the user is on the Settings placeholder screen, **When** they press the system back button, **Then** they return to the Home screen.

---

### Edge Cases

- What happens when prayer times are unavailable (no location permission or network error)? The Next Prayer card must show a graceful fallback state (e.g., "Prayer times unavailable") rather than crash or show stale data.
- What happens when all prayers for today have been completed? The card should indicate no remaining prayers today or show the first prayer of tomorrow.
- What happens when the Quran goal is set to zero or the user has no goal configured? The progress ring must handle a missing or zero goal gracefully and show a neutral state.
- What happens when the device is offline and summary data cannot be refreshed? Cards display the last known cached state without crashing.
- What happens when the user has never logged any Quran pages? The ring shows 0% with "0 / goal pages" rather than an error.
- What happens when the user has never opened the Athkar tab or logged any Athkar? The Athkar card MUST show a neutral "Tap to get started" state for both Morning and Evening groups rather than showing 0% or crashing.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Home screen MUST display a "Next Prayer" summary card showing the name and scheduled time of the next upcoming daily prayer.
- **FR-002**: The Home screen MUST display an "Athkar" summary card showing the completion status (done / pending) for Morning and Evening Athkar groups for the current day.
- **FR-003**: The Home screen MUST display a "Quran" summary card showing a circular progress indicator representing today's reading progress relative to the daily goal, along with the numeric pages read and goal.
- **FR-004**: The Habits section on the Home screen MUST remain unchanged — no existing habits UI, state, or behavior may be removed or altered.
- **FR-005**: Tapping the Next Prayer card MUST navigate the user to the Prayer tab.
- **FR-006**: Tapping the Athkar card MUST navigate the user to the Athkar tab.
- **FR-007**: Tapping the Quran card MUST navigate the user to the Quran tab.
- **FR-008**: The Home screen top app bar MUST include a Settings icon button in the **trailing (actions) slot** of the `TopAppBar`. The leading slot is reserved for back navigation and MUST NOT be used on root tab screens.
- **FR-009**: Tapping the Settings icon button MUST navigate the user to a new Settings placeholder screen.
- **FR-010**: A `SettingsRoute` MUST be added to the shared navigation module so other parts of the app can reference it.
- **FR-011**: The Settings placeholder screen MUST display a minimal UI indicating it is a future feature; no functional settings are required.
- **FR-012**: Each summary card MUST handle a loading state gracefully, displaying a placeholder or skeleton rather than empty content or a crash.
- **FR-013**: Each summary card MUST handle an error/unavailable state gracefully with a user-readable fallback message.
- **FR-014**: All Home Dashboard presentation code MUST live in the new `feature:home:presentation` module. `feature:habits:presentation` MUST NOT be modified by this feature. The new module's `build.gradle.kts` MUST declare `implementation` dependencies on `projects.feature.prayer.domain`, `projects.feature.athkar.domain`, `projects.feature.quran.domain`, and `projects.feature.habits.domain` in `commonMain`. **`projects.shared.navigation` is NOT a dependency** — navigation is done via plain `() -> Unit` callbacks passed from `MudawamaAppShell`. `HomeUiEvent` uses a nested `sealed interface Navigate` with typed objects (`ToPrayer`, `ToAthkar`, `ToQuran`, `ToSettings`, `ToHabits`, `ToTasbeeh`) rather than `Route` types.
- **FR-014b**: A `TasbeehSummaryCard` MUST be displayed alongside `QuranProgressCard` in a 2-column row, showing the user's Tasbeeh daily total vs. goal with a circular progress ring. Tapping the card navigates to `TasbeehRoute` (push destination, no bottom bar).
- **FR-015**: Summary cards are read-only — they display data but do not allow editing or logging from the Home screen.
- **FR-016**: Tab navigation triggered from a summary card tap MUST be implemented via `HomeUiEvent.Navigate` (a nested sealed interface with typed objects: `ToPrayer`, `ToAthkar`, `ToQuran`, `ToSettings`, `ToHabits`, `ToTasbeeh`) emitted from `HomeViewModel` and consumed by `HomeScreen` via `ObserveAsEvents`. `HomeScreen` invokes the corresponding `() -> Unit` callback received from `MudawamaAppShell`. Direct `navController` access from the ViewModel is forbidden. No `Route` types exist in `feature:home:presentation`.

### Key Entities

- **Next Prayer Summary**: The first upcoming prayer that has not yet occurred today, identified by name and scheduled time string.
- **Athkar Daily Status**: A per-group (Morning, Evening) boolean indicating whether all items in that group have been completed today.
- **Quran Daily Progress**: The count of pages read today, the daily goal, and the derived fraction (0.0–1.0) used for the progress ring display.
- **SettingsRoute**: A navigation destination registered in the shared navigation graph that leads to the Settings placeholder composable.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can view their next prayer time, Athkar completion status, and Quran progress without leaving the Home screen — verified by completing this review in a single-screen interaction with zero tab switches.
- **SC-002**: Tapping any summary card navigates to the correct feature tab within one user action (single tap), with no intermediate screens or delays beyond standard navigation transitions.
- **SC-003**: The Settings placeholder screen is reachable from the Home screen top bar in exactly one tap and dismissible via the system back gesture.
- **SC-004**: All three summary cards display a visible, non-crashing fallback state when their respective data sources are unavailable or still loading.
- **SC-005**: The existing Habits section on the Home screen passes all pre-existing acceptance scenarios without modification — zero regression in habits functionality.
- **SC-006**: The Home screen layout accommodates all four sections (three summary cards + habits list) in a single scrollable container without layout overflow, clipping, or accessibility violations.

## Assumptions

- The Home screen is implemented in a new `feature:home:presentation` module containing `HomeScreen.kt`, `HomeViewModel.kt`, and associated MVI model files. `feature:habits:presentation` is not modified.
- The Prayer summary card derives "next prayer" from the existing `PrayerWithStatus` list by selecting the first prayer with a pending status relative to the current time; no new use case is required.
- The Athkar card reads completion status from state already produced by the Athkar feature's presentation layer, accessed via a lightweight read-only injection; no new domain use cases are required.
- The Quran card reads progress fraction, pages read, and goal from state already produced by the Quran feature's presentation layer; no new domain use cases are required.
- Because this is presentation-layer only, no new Room entities, DAOs, repositories, or network calls are introduced.
- Summary card visual design uses `MudawamaSurfaceCard` from the shared design system for consistency with the rest of the app.
- The `SettingsRoute` follows the same `@Serializable data object` pattern as all other routes.
- The Settings placeholder composable does not require its own module and lives within an existing module.
- The layout order on the Home screen (top-to-bottom) places summary cards before the existing habits list, though the exact visual arrangement is an implementation detail.
- A new `feature:home:presentation` Gradle module is created. It is the aggregating dashboard and explicitly depends on the domain-only modules of other features — this is architecturally correct.
- A new `HomeViewModel` owns all four data streams: habits list, next prayer summary, Athkar daily status, and Quran daily progress. No separate ViewModel is introduced for the summary cards.
- The Settings placeholder composable (`SettingsScreen.kt`) and its route (`SettingsRoute`) are defined inside `shared:navigation`, consistent with how other placeholder screens are handled in the project. No new module is created for Settings.
- Tab-switching navigation (Prayer, Athkar, Quran tabs) is driven by `HomeUiEvent.NavigateTo(route)` consumed at the `MudawamaAppShell` level, not by direct `navController` injection into the ViewModel.
