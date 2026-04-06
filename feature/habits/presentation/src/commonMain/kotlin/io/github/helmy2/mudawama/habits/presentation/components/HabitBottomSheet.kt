package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.presentation.model.BottomSheetMode
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiAction
import io.github.helmy2.mudawama.habits.presentation.util.HABIT_ICONS
import kotlinx.datetime.DayOfWeek
import mudawama.feature.habits.presentation.Res
import mudawama.feature.habits.presentation.btn_cancel
import mudawama.feature.habits.presentation.btn_save
import mudawama.feature.habits.presentation.day_fri
import mudawama.feature.habits.presentation.day_mon
import mudawama.feature.habits.presentation.day_sat
import mudawama.feature.habits.presentation.day_sun
import mudawama.feature.habits.presentation.day_thu
import mudawama.feature.habits.presentation.day_tue
import mudawama.feature.habits.presentation.day_wed
import mudawama.feature.habits.presentation.error_name_empty
import mudawama.feature.habits.presentation.error_select_day
import mudawama.feature.habits.presentation.hint_daily_goal
import mudawama.feature.habits.presentation.hint_habit_name
import mudawama.feature.habits.presentation.label_days
import mudawama.feature.habits.presentation.label_icon
import mudawama.feature.habits.presentation.label_type
import mudawama.feature.habits.presentation.title_edit_habit
import mudawama.feature.habits.presentation.title_new_habit
import mudawama.feature.habits.presentation.type_boolean
import mudawama.feature.habits.presentation.type_numeric
import org.jetbrains.compose.resources.stringResource

private val DAYS_ORDERED = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
)


/**
 * Shared bottom sheet used for both Add and Edit habit flows (FR-016).
 * Pre-populates fields when [mode] is [BottomSheetMode.EditHabit].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitBottomSheet(
    mode: BottomSheetMode,
    onSave: (HabitsUiAction.SaveHabit) -> Unit,
    onDismiss: () -> Unit,
) {
    val existingHabit = (mode as? BottomSheetMode.EditHabit)?.habit

    var nameInput by remember { mutableStateOf(existingHabit?.name ?: "") }
    var iconKey by remember { mutableStateOf(existingHabit?.iconKey ?: HABIT_ICONS.first().first) }
    var selectedDays by remember {
        mutableStateOf(existingHabit?.frequencyDays ?: emptySet<DayOfWeek>())
    }
    var habitType by remember { mutableStateOf(existingHabit?.type ?: HabitType.BOOLEAN) }
    var goalInput by remember { mutableStateOf(existingHabit?.goalCount?.toString() ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var daysError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MudawamaTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (existingHabit != null) stringResource(Res.string.title_edit_habit)
                       else stringResource(Res.string.title_new_habit),
                style = MudawamaTheme.typography.h3,
                color = MudawamaTheme.colors.onSurface,
            )

            // ── Name ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    nameError = false
                },
                label = { Text(stringResource(Res.string.hint_habit_name)) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(Res.string.error_name_empty), color = MudawamaTheme.colors.error) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Icon picker ───────────────────────────────────────────────────
            Text(stringResource(Res.string.label_icon), style = MudawamaTheme.typography.h5, color = MudawamaTheme.colors.onSurface)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HABIT_ICONS) { (key, vector) ->
                    FilterChip(
                        selected = key == iconKey,
                        onClick = { iconKey = key },
                        label = {
                            Icon(
                                imageVector = vector,
                                contentDescription = key,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MudawamaTheme.colors.primary,
                            selectedLabelColor = MudawamaTheme.colors.onPrimary,
                            labelColor = MudawamaTheme.colors.onSurface,
                        ),
                    )
                }
            }

            // ── Frequency days ────────────────────────────────────────────────
            Column {
                Text(stringResource(Res.string.label_days), style = MudawamaTheme.typography.h5, color = MudawamaTheme.colors.onSurface)
                Spacer(Modifier.height(4.dp))
                val dayLabels = listOf(
                    stringResource(Res.string.day_mon),
                    stringResource(Res.string.day_tue),
                    stringResource(Res.string.day_wed),
                    stringResource(Res.string.day_thu),
                    stringResource(Res.string.day_fri),
                    stringResource(Res.string.day_sat),
                    stringResource(Res.string.day_sun),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DAYS_ORDERED.forEachIndexed { index, day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays)
                                    selectedDays - day else selectedDays + day
                                daysError = false
                            },
                            label = { Text(dayLabels[index]) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MudawamaTheme.colors.primary,
                                selectedLabelColor = MudawamaTheme.colors.onPrimary,
                            ),
                        )
                    }
                }
                if (daysError) {
                    Text(
                        stringResource(Res.string.error_select_day),
                        style = MudawamaTheme.typography.caption,
                        color = MudawamaTheme.colors.error,
                    )
                }
            }

            // ── Type selector ─────────────────────────────────────────────────
            Column {
                Text(stringResource(Res.string.label_type), style = MudawamaTheme.typography.h5, color = MudawamaTheme.colors.onSurface)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HabitType.entries.forEach { type ->
                        FilterChip(
                            selected = habitType == type,
                            onClick = { habitType = type },
                            label = {
                                Text(
                                    if (type == HabitType.BOOLEAN) stringResource(Res.string.type_boolean)
                                    else stringResource(Res.string.type_numeric)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MudawamaTheme.colors.primary,
                                selectedLabelColor = MudawamaTheme.colors.onPrimary,
                            ),
                        )
                    }
                }
            }

            // ── Goal count (Numeric only) ─────────────────────────────────────
            if (habitType == HabitType.NUMERIC) {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(Res.string.hint_daily_goal)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Actions ───────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.btn_cancel))
                }
                Button(
                    onClick = {
                        nameError = nameInput.isBlank()
                        daysError = selectedDays.isEmpty()
                        if (!nameError && !daysError) {
                            onSave(
                                HabitsUiAction.SaveHabit(
                                    name = nameInput.trim(),
                                    iconKey = iconKey,
                                    frequencyDays = selectedDays,
                                    type = habitType,
                                    goalCount = goalInput.toIntOrNull(),
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MudawamaTheme.colors.primary,
                        contentColor = MudawamaTheme.colors.onPrimary,
                    ),
                ) {
                    Text(stringResource(Res.string.btn_save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HabitBottomSheetAddPreview() {
    MudawamaTheme {
        HabitBottomSheet(
            mode = BottomSheetMode.AddHabit,
            onSave = {},
            onDismiss = {},
        )
    }
}
