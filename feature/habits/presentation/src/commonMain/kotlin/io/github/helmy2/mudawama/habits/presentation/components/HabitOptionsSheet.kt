package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import kotlinx.datetime.DayOfWeek
import mudawama.feature.habits.presentation.Res
import mudawama.feature.habits.presentation.action_delete
import mudawama.feature.habits.presentation.action_edit
import org.jetbrains.compose.resources.stringResource

/**
 * Options sheet shown when the user long-presses a habit card.
 * "Delete" is only rendered for non-core habits (FR-017).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitOptionsSheet(
    habit: Habit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MudawamaTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = habit.name,
                style = MudawamaTheme.typography.h4,
                color = MudawamaTheme.colors.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            HorizontalDivider()

            Text(
                text = stringResource(Res.string.action_edit),
                style = MudawamaTheme.typography.body1,
                color = MudawamaTheme.colors.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )

            // Delete is hidden for core habits (FR-017)
            if (!habit.isCore) {
                HorizontalDivider()
                Text(
                    text = stringResource(Res.string.action_delete),
                    style = MudawamaTheme.typography.body1,
                    color = MudawamaTheme.colors.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitOptionsSheetCustomPreview() {
    MudawamaTheme {
        HabitOptionsSheet(
            habit = Habit(
                id = "1", name = "Drink Water", iconKey = "water",
                type = HabitType.NUMERIC, category = "custom",
                frequencyDays = DayOfWeek.entries.toSet(),
                isCore = false, goalCount = 8, createdAt = 0L,
            ),
            onEdit = {}, onDelete = {}, onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitOptionsSheetCorePreview() {
    MudawamaTheme {
        HabitOptionsSheet(
            habit = Habit(
                id = "2", name = "Fajr Prayer", iconKey = "pray",
                type = HabitType.BOOLEAN, category = "PRAYER",
                frequencyDays = DayOfWeek.entries.toSet(),
                isCore = true, goalCount = null, createdAt = 0L,
            ),
            onEdit = {}, onDelete = {}, onDismiss = {},
        )
    }
}
