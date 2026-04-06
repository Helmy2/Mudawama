# Contract: feature:habits — Presentation Layer Composable API

**Module**: `feature:habits:presentation`
**Date**: 2026-04-05
**Consumer**: `shared:umbrella-ui` (wires `HabitsScreen` into `MudawamaAppShell`)

---

## `HabitsScreen`

Public entry point. One composable per navigation destination.

```kotlin
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = koinViewModel()
)
```

**Responsibilities**:
- Collects `viewModel.state` as Compose `State`.
- Renders a `Scaffold` with a `FloatingActionButton` anchored bottom-end.
- Shows a `LazyColumn` of `HabitListItem` composables when `habits` is non-empty.
- Shows `HabitsEmptyState` when `habits` is empty and `isLoading == false`.
- Renders `HabitBottomSheet` when `bottomSheetMode != Hidden`.
- Renders `HabitOptionsSheet` when `bottomSheetMode == OptionsMenu`.
- Renders `DeleteConfirmDialog` when `bottomSheetMode == DeleteConfirm`.
- Observes `viewModel.eventFlow` via `ObserveAsEvents` for `ShowSnackbar` events.

**Preview**:
```kotlin
@Preview @Composable
fun HabitsScreenPreview() { /* seeded HabitsUiState */ }
```

---

## `HabitListItem`

```kotlin
@Composable
fun HabitListItem(
    habitWithStatus : HabitWithStatus,
    onToggle        : () -> Unit,     // BOOLEAN check-off
    onIncrement     : () -> Unit,     // NUMERIC "+1"
    onLongPress     : () -> Unit,
    modifier        : Modifier = Modifier,
)
```

Renders:
1. **Icon** resolved from `habitWithStatus.habit.iconKey`. Placeholder on unknown key.
2. **Name** (`MudawamaTheme.typography.bodyLarge`).
3. **Completion control**: `Checkbox` for BOOLEAN; `"count / goal"` label + `IconButton(+)` for NUMERIC.
   Disabled if `isDueToday == false`. Absent denominator when `goalCount == null`.
4. **`HabitHeatmapRow`** below the main row.

---

## `HabitHeatmapRow`

```kotlin
@Composable
fun HabitHeatmapRow(
    weekLogs        : List<HabitLog?>,    // exactly 7 entries, index 0 = today
    frequencyDays   : Set<DayOfWeek>,
    modifier        : Modifier = Modifier,
)
```

Renders 7 day-cells in a `Row`. Cell visual state:

| Condition | Visual |
|-----------|--------|
| Log exists and `status == COMPLETED` | Filled: `MudawamaTheme.colors.primary` |
| Log exists and `status == PENDING` | Muted: `MudawamaTheme.colors.surfaceVariant` |
| Log is `null` (missed) | Muted (same as PENDING) |
| Day not in `frequencyDays` | Ghost: `MudawamaTheme.colors.outline.copy(alpha = 0.3f)` |

---

## `HabitBottomSheet`

Shared by Add and Edit flows. Driven by `BottomSheetMode.AddHabit` or `BottomSheetMode.EditHabit`.

```kotlin
@Composable
fun HabitBottomSheet(
    mode        : BottomSheetMode,      // AddHabit or EditHabit(habit)
    onSave      : (HabitsUiAction.SaveHabit) -> Unit,
    onDismiss   : () -> Unit,
)
```

Contains (per FR-016):
- `OutlinedTextField` for name; inline error when blank.
- Horizontally scrollable icon picker (`LazyRow` of predefined icons from `shared:designsystem`).
- 7-chip `FlowRow` day-of-week frequency selector; inline error when none selected.
- `SegmentedButton` (or two `FilterChip`s) for BOOLEAN / NUMERIC type.
- `OutlinedTextField` for goal count; visible only when NUMERIC selected.
- "Save" `Button` (primary) + "Cancel" `TextButton` (tertiary).

Pre-population: when `mode == EditHabit(habit)`, all fields initialised from `habit`.

---

## `HabitOptionsSheet`

```kotlin
@Composable
fun HabitOptionsSheet(
    habit       : Habit,
    onEdit      : () -> Unit,
    onDelete    : () -> Unit,   // only rendered if habit.isCore == false
    onDismiss   : () -> Unit,
)
```

Renders as a `ModalBottomSheet`. "Delete" option is hidden when `habit.isCore == true` (FR-017).

---

## Navigation Wiring (FR-020)

`HabitsScreen` replaces `HabitsPlaceholderScreen` in the `NavDisplay` routing graph via a content-slot
lambda on `MudawamaAppShell`:

```kotlin
// shared/navigation — MudawamaAppShell.kt (updated signature)
@Composable
fun MudawamaAppShell(
    habitsScreen: @Composable () -> Unit = { HabitsPlaceholderScreen() },
)

// shared/umbrella-ui — MudawamaApp.kt (new file)
@Composable
fun MudawamaApp() {
    MudawamaAppShell(
        habitsScreen = { HabitsScreen() }
    )
}
```

Platform hosts (`androidApp/MainActivity`, `iosApp/ContentView`) call `MudawamaApp()`.
`shared:umbrella-ui/build.gradle.kts` adds `implementation(projects.feature.habits.presentation)`.
