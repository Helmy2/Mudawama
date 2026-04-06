package io.github.helmy2.mudawama.habits.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.helmy2.mudawama.core.presentation.util.ObserveAsEvents
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.habits.presentation.components.HabitBottomSheet
import io.github.helmy2.mudawama.habits.presentation.components.HabitCoreRitualItem
import io.github.helmy2.mudawama.habits.presentation.components.HabitPersonalItem
import io.github.helmy2.mudawama.habits.presentation.components.HabitOptionsSheet
import io.github.helmy2.mudawama.habits.presentation.model.BottomSheetMode
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiAction
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiEvent
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiState
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.action_add_new_habit
import mudawama.shared.designsystem.action_delete
import mudawama.shared.designsystem.btn_cancel
import mudawama.shared.designsystem.dialog_delete_message
import mudawama.shared.designsystem.dialog_delete_title
import mudawama.shared.designsystem.habits_screen_title
import mudawama.shared.designsystem.section_core_rituals
import mudawama.shared.designsystem.section_personal_habits
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.eventFlow) { event ->
        when (event) {
            is HabitsUiEvent.ShowSnackbar ->
                scope.launch { snackbarHostState.showSnackbar(getString(event.message)) }
        }
    }

    HabitsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitsScreenContent(
    state: HabitsUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (HabitsUiAction) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MudawamaTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.habits_screen_title),
                        style = MudawamaTheme.typography.h2,
                        color = MudawamaTheme.colors.onSurface,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MudawamaTheme.colors.background,
                ),
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MudawamaTheme.colors.primary)
                }
            }

            else -> {
                val coreHabits = state.habits.filter { it.habit.isCore }
                val personalHabits = state.habits.filter { !it.habit.isCore }

                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // ── Core Rituals section ───────────────────────────────
                    if (coreHabits.isNotEmpty()) {
                        item {
                            SectionHeader(stringResource(Res.string.section_core_rituals))
                            Spacer(Modifier.height(8.dp))
                        }
                        items(coreHabits, key = { it.habit.id }) { habitWithStatus ->
                            HabitCoreRitualItem(
                                habitWithStatus = habitWithStatus,
                                onToggle = {
                                    onAction(HabitsUiAction.ToggleCompletion(habitWithStatus.habit.id))
                                },
                                onIncrement = {
                                    onAction(HabitsUiAction.IncrementCount(habitWithStatus.habit.id))
                                },
                                modifier = Modifier.padding(bottom = 10.dp),
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }

                    // ── Personal Habits section ────────────────────────────
                    item {
                        SectionHeader(stringResource(Res.string.section_personal_habits))
                        Spacer(Modifier.height(8.dp))
                    }

                    if (personalHabits.isEmpty()) {
                        item {
                            AddNewHabitButton(
                                onClick = { onAction(HabitsUiAction.AddHabitFabClicked) },
                            )
                        }
                    } else {
                        items(personalHabits, key = { it.habit.id }) { habitWithStatus ->
                            HabitPersonalItem(
                                habitWithStatus = habitWithStatus,
                                onToggle = {
                                    onAction(HabitsUiAction.ToggleCompletion(habitWithStatus.habit.id))
                                },
                                onMoreClick = {
                                    onAction(HabitsUiAction.HabitLongPressed(habitWithStatus.habit))
                                },
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                        item {
                            Spacer(Modifier.height(4.dp))
                            AddNewHabitButton(
                                onClick = { onAction(HabitsUiAction.AddHabitFabClicked) },
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Bottom sheets & dialogs ────────────────────────────────────────────────
    when (val mode = state.bottomSheetMode) {
        is BottomSheetMode.AddHabit, is BottomSheetMode.EditHabit -> {
            HabitBottomSheet(
                mode = mode,
                onSave = { onAction(it) },
                onDismiss = { onAction(HabitsUiAction.DismissBottomSheet) },
            )
        }

        is BottomSheetMode.OptionsMenu -> {
            HabitOptionsSheet(
                habit = mode.habit,
                onEdit = { onAction(HabitsUiAction.EditHabitSelected(mode.habit)) },
                onResetToday = { onAction(HabitsUiAction.ResetTodayProgress(mode.habit.id)) },
                onDelete = { onAction(HabitsUiAction.DeleteHabitSelected(mode.habit.id)) },
                onDismiss = { onAction(HabitsUiAction.DismissBottomSheet) },
            )
        }

        is BottomSheetMode.DeleteConfirm -> {
            AlertDialog(
                onDismissRequest = { onAction(HabitsUiAction.DismissBottomSheet) },
                title = { Text(stringResource(Res.string.dialog_delete_title)) },
                text = { Text(stringResource(Res.string.dialog_delete_message)) },
                confirmButton = {
                    TextButton(
                        onClick = { onAction(HabitsUiAction.DeleteConfirmed(mode.habitId)) },
                    ) {
                        Text(stringResource(Res.string.action_delete), color = MudawamaTheme.colors.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(HabitsUiAction.DismissBottomSheet) }) {
                        Text(stringResource(Res.string.btn_cancel))
                    }
                },
            )
        }

        BottomSheetMode.Hidden -> Unit
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MudawamaTheme.typography.caption,
        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.5f),
        letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp),
    )
}

@Composable
private fun AddNewHabitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = MudawamaTheme.colors.primary.copy(alpha = 0.4f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MudawamaTheme.colors.primary,
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(
                text = stringResource(Res.string.action_add_new_habit),
                style = MudawamaTheme.typography.h5,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun HabitsScreenListPreview() {
    val coreHabit = Habit(
        id = "1", name = "Prayers", iconKey = "pray",
        type = HabitType.BOOLEAN, category = "PRAYER",
        frequencyDays = DayOfWeek.entries.toSet(),
        isCore = true, goalCount = 5, createdAt = 0L,
    )
    val personalHabit = Habit(
        id = "2", name = "Fasting Mondays", iconKey = "moon",
        type = HabitType.BOOLEAN, category = "custom",
        frequencyDays = setOf(DayOfWeek.MONDAY),
        isCore = false, goalCount = null, createdAt = 0L,
    )
    MudawamaTheme {
        HabitsScreenContent(
            state = HabitsUiState(
                isLoading = false,
                habits = listOf(
                    HabitWithStatus(coreHabit, todayLog = null, weekLogs = List(7) { null }),
                    HabitWithStatus(personalHabit, todayLog = null, weekLogs = List(7) { null }),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

