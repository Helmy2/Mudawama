package io.github.helmy2.mudawama.habits.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.helmy2.mudawama.core.presentation.util.ObserveAsEvents
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.habits.presentation.components.HabitBottomSheet
import io.github.helmy2.mudawama.habits.presentation.components.HabitListItem
import io.github.helmy2.mudawama.habits.presentation.components.HabitOptionsSheet
import io.github.helmy2.mudawama.habits.presentation.components.HabitsEmptyState
import io.github.helmy2.mudawama.habits.presentation.model.BottomSheetMode
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiAction
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiEvent
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiState
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import mudawama.feature.habits.presentation.Res
import mudawama.feature.habits.presentation.action_delete
import mudawama.feature.habits.presentation.btn_cancel
import mudawama.feature.habits.presentation.cd_add_habit
import mudawama.feature.habits.presentation.dialog_delete_message
import mudawama.feature.habits.presentation.dialog_delete_title
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(HabitsUiAction.AddHabitFabClicked) },
                containerColor = MudawamaTheme.colors.primary,
                contentColor = MudawamaTheme.colors.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.cd_add_habit))
            }
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

            state.habits.isEmpty() -> {
                HabitsEmptyState(modifier = Modifier.padding(innerPadding))
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + 80.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.habits, key = { it.habit.id }) { habitWithStatus ->
                        HabitListItem(
                            habitWithStatus = habitWithStatus,
                            onToggle = {
                                onAction(HabitsUiAction.ToggleCompletion(habitWithStatus.habit.id))
                            },
                            onIncrement = {
                                onAction(HabitsUiAction.IncrementCount(habitWithStatus.habit.id))
                            },
                            onLongPress = {
                                onAction(HabitsUiAction.HabitLongPressed(habitWithStatus.habit))
                            },
                        )
                    }
                }
            }
        }
    }

    // ── Bottom sheets & dialogs rendered outside Scaffold ─────────────────────
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun HabitsScreenEmptyPreview() {
    MudawamaTheme {
        HabitsScreenContent(
            state = HabitsUiState(isLoading = false, habits = emptyList()),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitsScreenListPreview() {
    val sampleHabit = Habit(
        id = "1", name = "Read Quran", iconKey = "book",
        type = HabitType.BOOLEAN, category = "custom",
        frequencyDays = DayOfWeek.entries.toSet(),
        isCore = false, goalCount = null, createdAt = 0L,
    )
    MudawamaTheme {
        HabitsScreenContent(
            state = HabitsUiState(
                isLoading = false,
                habits = listOf(
                    HabitWithStatus(sampleHabit, todayLog = null, weekLogs = List(7) { null }),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
