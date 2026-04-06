# Feature Specification: feature:habits — Custom Habit Management & Daily Tracking

**Feature Branch**: `005-feature-habits`
**Created**: 2026-04-05
**Status**: Draft
**Reference**: SRS §FR-4 (Custom Habit Management), §FR-5 (Daily Logs and Insights)

---

## Overview

**Purpose**: Deliver the `feature:habits` module — the first real feature slice in Mudawama — replacing the `HabitsPlaceholderScreen` in `shared:navigation` with a fully functional `HabitsScreen`. This screen becomes the single unified surface where users view, create, edit, delete, and check off their custom personal habits, alongside a per-habit 7-day consistency heatmap.

**Goals**:
- Allow users to create custom habits specifying: name, icon, frequency (days of week), and type (Boolean check-off vs. numeric counter).
- Display all active habits in a clean, scrollable list on the Habits tab.
- Support full lifecycle management — create, edit, and delete custom habits — via context-preserving Modal Bottom Sheets.
- Prevent deletion of core ritual habits (those flagged as `isCore = true` in `HabitEntity`).
- Surface a 7-day streak heatmap per habit card to motivate daily consistency.
- Persist all habit definitions and daily completion logs using `HabitDao` and `HabitLogDao` from `shared:core:database`.

**Scope**: `feature/habits/` — three sub-modules:
- `feature:habits:domain` — pure Kotlin; models, repository interfaces, use cases.
- `feature:habits:data` — Room-backed repository implementations; depends on `shared:core:database` and `shared:core:time`.
- `feature:habits:presentation` — Compose Multiplatform screens and ViewModels; replaces `HabitsPlaceholderScreen` upon delivery.

All UI code lives in `commonMain`. No platform-specific source sets are introduced in `:presentation`.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Viewing the list of active habits (Priority: P1)

A user taps the "Habits" tab in the bottom navigation bar. They see a scrollable list of all their active custom habits. Each list item shows the habit name, icon, and today's completion status — a checkmark for Boolean habits and a numeric counter for Numeric habits. A 7-cell heatmap row beneath each card shows the last 7 logical days.

**Why this priority**: This is the entry point for every other habit interaction. Without the list, no subsequent story can be exercised.

**Independent Test**: Seed the database with known habits and logs; confirm all habits appear on screen with the correct completion states and heatmap cells.

**Acceptance Scenarios**:

1. **Given** the user has 3 custom habits in the database, **When** they navigate to the Habits screen, **Then** all 3 habits are visible in the scrollable list.
2. **Given** a Boolean habit was already marked complete today, **When** the Habits screen renders, **Then** that habit's completion control shows a filled/checked state and today's heatmap cell is highlighted.
3. **Given** a Numeric habit has a `goalCount` of 5 and today's `completedCount` is 3, **When** the screen renders, **Then** the habit shows "3 / 5" progress and is not visually complete.
4. **Given** the user has no habits in the database, **When** they navigate to the Habits screen, **Then** an empty-state illustration and a prompt to add the first habit are displayed.
5. **Given** the DAO's Flow emits an updated list, **When** the ViewModel receives the emission, **Then** the list updates reactively without requiring a manual screen refresh.

---

### User Story 2 — Creating a custom habit (Priority: P1)

A user taps the "+" FAB at the bottom of the Habits screen. A Modal Bottom Sheet slides up, keeping the list partially visible behind it. The user fills in a name ("Fasting Mondays"), picks an icon, selects Monday as the frequency day, and chooses "Check-off" as the type. Tapping "Save" dismisses the sheet and the new habit immediately appears in the list.

**Why this priority**: Creating a habit is the foundational action of the entire module.

**Independent Test**: Tap "Save" with valid inputs; confirm a new `HabitEntity` appears in the database and is rendered in the list within the same session.

**Acceptance Scenarios**:

1. **Given** the user taps the "Add Habit" FAB, **When** the bottom sheet opens, **Then** the existing habit list remains partially visible behind the sheet (context-preserving modal).
2. **Given** the user has entered a name, chosen an icon, selected at least one frequency day, and chosen a type, **When** they tap "Save", **Then** the habit is persisted and appears in the list.
3. **Given** the user taps "Save" with an empty name field, **Then** an inline validation message is shown on the name field and no record is saved.
4. **Given** the user taps "Save" with no frequency day selected, **Then** an inline validation message is shown on the frequency selector and no record is saved.
5. **Given** the user creates a Boolean habit, **When** the habit is saved, **Then** its `HabitEntity.type` stores `"BOOLEAN"` and `goalCount` is `null`.
6. **Given** the user creates a Numeric habit with a goal of 10, **When** saved, **Then** `HabitEntity.type` stores `"NUMERIC"` and `goalCount` is `10`.
7. **Given** the bottom sheet is open, **When** the user swipes it down or taps outside its bounds, **Then** the sheet dismisses without saving and no habit is created.

---

### User Story 3 — Checking off / incrementing a habit for today (Priority: P1)

A user sees the habit "Read Hadith" in the list and taps its check-off button. The control transitions to a completed visual state and today's heatmap cell lights up. For a Numeric habit "Drink Water (8 glasses)", tapping "+" increments the count display by 1.

**Why this priority**: This is the primary daily action; most users open the Habits screen solely for this purpose.

**Independent Test**: Tap the check-off; confirm a `HabitLogEntity` for today's logical date exists in the database with `status = "COMPLETED"` and the UI reflects it immediately.

**Acceptance Scenarios**:

1. **Given** a Boolean habit has no log for today, **When** the user taps the check-off, **Then** a `HabitLogEntity` is created with `status = "COMPLETED"` for today's logical date, and the UI updates immediately.
2. **Given** a Boolean habit is already marked complete today, **When** the user taps the check-off again, **Then** the log's status reverts to `"PENDING"` and the UI shows the uncompleted state.
3. **Given** a Numeric habit at 3 / 5, **When** the user taps "+", **Then** `completedCount` becomes 4 and the UI shows "4 / 5".
4. **Given** a Numeric habit reaches its `goalCount` on the final "+", **Then** the habit card visually enters the fully-completed state (same filled styling as a Boolean completion).
5. **Given** a habit's `frequencyDays` does not include today's day-of-week, **When** the screen renders, **Then** the habit is shown in a "not due today" visual state and its completion control is disabled.

---

### User Story 4 — Editing a custom habit (Priority: P2)

A user long-presses a custom habit card. An options bottom sheet appears offering "Edit" and "Delete". Selecting "Edit" opens the same bottom sheet pre-populated with the habit's current values. The user changes the frequency to "Mon & Wed" and taps "Save". The list item updates immediately.

**Why this priority**: Habits evolve over time; without editing, users abandon stale habits rather than adapt them.

**Independent Test**: Open the edit sheet, change the habit name, save, and confirm the updated `HabitEntity.name` persists in the database.

**Acceptance Scenarios**:

1. **Given** the user long-presses a custom habit, **When** the options appear, **Then** both "Edit" and "Delete" are available.
2. **Given** the user selects "Edit", **When** the bottom sheet opens, **Then** all fields are pre-populated with the habit's current `name`, `iconKey`, `frequencyDays`, and `type`.
3. **Given** the user changes the habit name and taps "Save", **When** the sheet dismisses, **Then** the list item shows the updated name and `HabitEntity.name` is updated in the database.
4. **Given** the user opens the Edit sheet and taps the dismiss control without saving, **Then** no changes are persisted to the database.

---

### User Story 5 — Deleting a custom habit (Priority: P2)

From the options bottom sheet, the user selects "Delete" for a custom habit. A confirmation dialog appears. On confirm, the habit and all its logs are removed from the database and the list item disappears.

**Why this priority**: Without deletion, stale habits accumulate and clutter the experience.

**Independent Test**: Confirm deletion; verify that neither the `HabitEntity` nor any associated `HabitLogEntity` rows for that habit remain in the database.

**Acceptance Scenarios**:

1. **Given** the user selects "Delete", **When** the confirmation dialog is shown, **Then** the user must tap "Confirm Delete" to proceed; tapping "Cancel" leaves the habit intact.
2. **Given** the user confirms deletion, **When** the operation completes, **Then** the habit disappears from the list and all associated database records are removed (cascade delete).
3. **Given** the user long-presses a habit where `isCore = true`, **When** the options appear, **Then** the "Delete" option is absent; the habit cannot be deleted from this screen.

---

### User Story 6 — Viewing the 7-day consistency heatmap (Priority: P2)

Beneath each habit card, a compact row of 7 day-cells represents the last 7 logical dates. Completed days are highlighted in the primary teal color; pending/missed days appear in a muted surface color; days the habit was not scheduled appear in a ghost/inactive variant.

**Why this priority**: The heatmap delivers the visual motivation loop that sustains long-term habit consistency — a direct product goal per PRD §3.3 and SRS §FR-5.2.

**Independent Test**: Seed 7 known `HabitLogEntity` records with alternating statuses; confirm each of the 7 cells renders the correct filled/empty/inactive state.

**Acceptance Scenarios**:

1. **Given** a habit has log entries for each of the last 7 days, **When** the heatmap row renders, **Then** each cell accurately reflects that day's log status (`COMPLETED` → filled, `PENDING` → muted).
2. **Given** today's log does not yet exist for a habit, **When** today's cell renders, **Then** it displays as pending/empty (not filled).
3. **Given** a habit has no log record for a specific day within the last 7 (missed), **When** the heatmap renders that cell, **Then** it shows the missed/muted visual.
4. **Given** a habit is only scheduled on certain days (e.g., Monday-only), **When** the heatmap renders a non-scheduled day, **Then** that cell uses the "not scheduled" ghost variant — visually distinct from a missed day.
5. **Given** the user just checked off a habit, **When** the ViewModel state updates, **Then** today's heatmap cell transitions to the filled state within the same render cycle (no manual refresh).

---

### Edge Cases

- **Incomplete write on app kill**: `HabitDao.insertHabit()` is a `suspend` function; if its coroutine is cancelled before the database write commits, no partial record is stored. On next launch, no phantom habit appears.
- **Duplicate habit names**: Duplicate names are permitted (no `UNIQUE` constraint on `name`). The `id` (UUID) is the canonical identifier.
- **Day rollover while screen is open**: The ViewModel observes habit-log Flows; when the daily rollover job creates fresh empty logs for the new logical date, the UI updates reactively to reflect uncompleted state.
- **Null `goalCount` for a Numeric habit**: The UI renders the count without a denominator (e.g., "4") rather than crashing, treating a null goal as "no goal set".
- **Missing icon key**: If `iconKey` no longer maps to any known icon asset, a default placeholder icon is rendered rather than crashing or showing an empty space.
- **Concurrent taps on the same completion control**: Rapid double-taps are debounced in the ViewModel to prevent duplicate log writes.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Domain Layer (`feature:habits:domain`)

- **FR-001**: The module MUST define a `Habit` domain model with fields: `id: String`, `name: String`, `iconKey: String`, `type: HabitType` (enum: `BOOLEAN`, `NUMERIC`), `category: String`, `frequencyDays: Set<DayOfWeek>`, `isCore: Boolean`, `goalCount: Int?`, `createdAt: Long`.
- **FR-002**: The module MUST define a `HabitLog` domain model with fields: `id: String`, `habitId: String`, `date: String` (ISO `yyyy-MM-dd`), `status: LogStatus` (enum: `PENDING`, `COMPLETED`), `completedCount: Int`, `loggedAt: Long`.
- **FR-003**: The module MUST define a `HabitRepository` interface exposing:
  - `fun observeAllHabits(): Flow<List<Habit>>`
  - `suspend fun upsertHabit(habit: Habit)`
  - `suspend fun deleteHabit(habitId: String)`
  - `suspend fun getHabitById(habitId: String): Habit?`
- **FR-004**: The module MUST define a `HabitLogRepository` interface exposing:
  - `fun observeLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLog>>`
  - `suspend fun upsertLog(log: HabitLog)`
  - `suspend fun getLogForHabitOnDate(habitId: String, date: String): HabitLog?`
- **FR-005**: The module MUST provide the following Use Cases, each as a single-responsibility class injected via Koin:
  - `ObserveHabitsWithTodayStatusUseCase` — combines `observeAllHabits()` with today's logs to emit `List<HabitWithStatus>`.
  - `CreateHabitUseCase` — validates non-empty name and at least one frequency day, then calls `HabitRepository.upsertHabit()`.
  - `UpdateHabitUseCase` — validates the same constraints as `CreateHabitUseCase`, then calls `upsertHabit()`.
  - `DeleteHabitUseCase` — asserts `habit.isCore == false` before calling `deleteHabit()`; returns a domain error if the habit is a core ritual.
  - `ToggleHabitCompletionUseCase` — flips a Boolean habit log between `COMPLETED` and `PENDING` for the current logical date, creating the log if it does not exist.
  - `IncrementHabitCountUseCase` — increments `completedCount` on the Numeric habit log for the current logical date, creating the log if it does not exist.
  - `ObserveWeeklyHeatmapUseCase` — returns a Flow of the last 7 logical days of `HabitLog` entries for a single habit.

#### Data Layer (`feature:habits:data`)

- **FR-006**: The module MUST implement `HabitRepository` using `HabitDao` from `shared:core:database`.
- **FR-007**: The module MUST implement `HabitLogRepository` using `HabitLogDao` from `shared:core:database`.
- **FR-008**: The module MUST provide bidirectional mapper functions: `HabitEntity ↔ Habit` and `HabitLogEntity ↔ HabitLog`.
- **FR-009**: The `frequencyDays` field MUST be serialised as a comma-separated string of day-of-week ordinal indices (e.g., `"1,3"` for Monday and Wednesday) when stored in `HabitEntity.frequencyDays`, and deserialised back to `Set<DayOfWeek>` in the entity-to-domain mapper.
- **FR-010**: All date strings passed to DAO queries MUST be derived from `TimeProvider.logicalDate()` formatted via `DateFormatters.toIsoDateString()` from `shared:core:time` — direct system-clock calls are forbidden (per project constraint SC-002 in spec `003-add-core-time`).
- **FR-011**: The module MUST register `HabitRepository` and `HabitLogRepository` implementations in a Koin module under `feature:habits:data`.

#### Presentation Layer (`feature:habits:presentation`)

- **FR-012**: The module MUST provide a `HabitsViewModel` extending the project's `BaseViewModel`. It MUST hold a `HabitsUiState` sealed/data class and handle `HabitsUiAction` sealed class events.
- **FR-013**: `HabitsUiState` MUST contain at minimum:
  - `habits: List<HabitWithStatus>` — combined list of habits with today's status and last-7-days logs.
  - `isLoading: Boolean`
  - `bottomSheetMode: BottomSheetMode` — one of: `Hidden`, `AddHabit`, `EditHabit(habit: Habit)`, `DeleteConfirm(habitId: String)`.
  - `errorMessage: String?`
- **FR-014**: The module MUST provide a `HabitsScreen` composable that:
  - Renders the list of habits via `LazyColumn`.
  - Shows an empty-state view when `habits` is empty.
  - Renders a floating action button ("+" / "Add Habit") anchored to the bottom-right of the screen.
  - Shows `HabitBottomSheet` when `bottomSheetMode` is not `Hidden`.
- **FR-015**: Each habit list item MUST display:
  - The habit icon (resolved from `iconKey`).
  - The habit name.
  - A completion control: a toggle checkable icon for `BOOLEAN` habits; a "+" button and `"count / goal"` label for `NUMERIC` habits.
  - A 7-cell heatmap row representing the last 7 logical dates (filled, muted, or ghost variants per day status).
- **FR-016**: The module MUST provide a `HabitBottomSheet` composable (`ModalBottomSheet`) shared by both Add and Edit flows, containing:
  - A text input field for the habit name with inline empty-state validation.
  - A horizontally scrollable icon picker row (predefined icon set from `shared:designsystem`).
  - A 7-chip day-of-week frequency selector (Mon–Sun toggles) with inline validation requiring at least one selection.
  - A two-option type selector ("Check-off" | "Numeric Counter").
  - A "Goal" numeric input that appears only when "Numeric Counter" is selected.
  - "Save" (primary) and "Cancel" (tertiary) action buttons.
- **FR-017**: The "Delete" option in the options bottom sheet MUST be conditionally rendered; it MUST be hidden for any habit where `isCore == true`.
- **FR-018**: All source code in `feature:habits:presentation` MUST reside in `commonMain`; no `androidMain` or `iosMain` source sets are permitted in this sub-module.
- **FR-019**: The `:presentation` module MUST declare dependencies on `feature:habits:domain`, `shared:core:presentation` (for `BaseViewModel`), and `shared:designsystem` (for all color, typography, and icon tokens). It MUST NOT depend on `shared:core:database` directly.
- **FR-020**: Upon delivery, the `NavDisplay` entry for `HabitsRoute` in `shared:navigation` (or the umbrella composition root) MUST be updated to render `HabitsScreen` instead of `HabitsPlaceholderScreen`. The module containing `HabitsPlaceholderScreen` MUST retain it (or remove it) as a separate clean-up task to avoid breaking the navigation graph mid-delivery.

---

## Key Entities

- **`Habit`**: Immutable domain model representing a habit's definition. Mutations are expressed by creating a new `Habit` instance and calling `upsertHabit`.
- **`HabitLog`**: Domain model representing a single day's tracking record for one habit, linked by `habitId` and `date`.
- **`HabitWithStatus`**: A read-only projection combining a `Habit`, its optional `HabitLog` for today, and the ordered list of logs for the last 7 logical days. Used exclusively by the presentation layer.
- **`HabitsViewModel`**: The single ViewModel for the Habits screen. Subscribes to domain layer Flows and maps them into a single reactive `HabitsUiState`.
- **`HabitBottomSheet`**: A context-preserving `ModalBottomSheet` composable used for both creating and editing habits. Receives pre-populated state when in Edit mode; empty state in Add mode.
- **`HabitType`**: An enum (`BOOLEAN`, `NUMERIC`) determining the completion interaction model for a habit.
- **`LogStatus`**: An enum (`PENDING`, `COMPLETED`) representing a given day's completion state in a `HabitLog`.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can create a new custom habit (all required fields) and see it appear in the list within the same session on both Android and iOS — verified by manual smoke test.
- **SC-002**: Tapping the completion control on a Boolean habit produces a visible UI state change within 300 ms on a mid-range reference device — verified by observation.
- **SC-003**: The 7-day heatmap for each habit accurately reflects the last 7 days of log data — verified by seeding 7 known `HabitLogEntity` records with alternating statuses and comparing all 7 cell states.
- **SC-004**: Attempting to delete a core ritual habit (`isCore = true`) via `DeleteHabitUseCase` produces a domain error and no database change — verified by a unit test.
- **SC-005**: `HabitsScreen` and `HabitBottomSheet` compile and all Compose Previews render without errors on both Android and iOS targets.
- **SC-006**: The Habits list loads and displays up to 50 habits in under 1 second from screen entry on a low-end reference device — verified by profiling.
- **SC-007**: All 7 Use Cases in `feature:habits:domain` have unit tests covering at least one success path and one failure/edge-case path — verified by test report.
- **SC-008**: After a confirmed deletion, no `HabitEntity` or associated `HabitLogEntity` rows for the deleted habit exist in the database — verified by an integration test against an in-memory Room instance.
- **SC-009**: Replacing `HabitsPlaceholderScreen` with `HabitsScreen` in the routing graph results in a clean build with zero compilation errors on both Android and iOS targets.

---

## Assumptions

- `shared:core:database` is fully delivered per spec `002-add-core-database`. `HabitDao` and `HabitLogDao` expose the exact method signatures currently present in source: `getAllHabits(): Flow<List<HabitEntity>>`, `insertHabit()`, `updateHabit()`, `deleteHabit()`, `getLogsForDateRange()`, `getLogForHabitOnDate()`, etc. No schema migrations are required for this feature.
- `shared:core:time` is fully delivered per spec `003-add-core-time`. `TimeProvider.logicalDate()` returns a `LocalDate` and `DateFormatters.toIsoDateString(LocalDate)` returns an ISO `yyyy-MM-dd` string. The `FakeTimeProvider` is accessible from `commonMain` for use in unit tests.
- `shared:designsystem` provides a pre-built `ModalBottomSheet` wrapper that follows the project's context-preserving modal pattern described in `DESIGN.md §5`. A finite, predefined icon set is available as composable/drawable resources; no new icons need to be created for this spec.
- Daily log generation at day rollover (SRS §FR-5.1) — creating empty `HabitLogEntity` rows for all active habits at the start of a new logical day — is a background system task outside the scope of `feature:habits`. The Habits screen reads and displays whatever logs already exist; it does not trigger the rollover job.
- Core ritual habits (Prayers, Quran, Athkar) are seeded into the `habits` table by their respective feature modules or via a first-run database seed. The `feature:habits` module treats any `HabitEntity` with `isCore = true` as read-only for deletion purposes. Editing of core ritual details is owned by the relevant feature spec, not by this one.
- `HabitsRoute` already exists in `shared:navigation/Routes.kt` as a `@Serializable data object`. Wiring `HabitsScreen` requires adding `feature:habits:presentation` as a dependency to `shared:umbrella-ui` (or the equivalent composition root) — this wiring is in scope for this feature's delivery checklist.
- `HabitEntity.frequencyDays` is stored as a `String` column (current schema). The comma-separated day-of-week ordinal format (e.g., `"1,3"`) is compatible with this column type and is the canonical serialisation format for this feature.
- No network calls are made anywhere in `feature:habits`. The module is entirely offline-first.
- The `mudawama.kmp.presentation` Gradle convention plugin is used for `feature:habits:presentation`, `mudawama.kmp.library` + `mudawama.kmp.koin` for `:domain`, and `mudawama.kmp.data` for `:data`.

