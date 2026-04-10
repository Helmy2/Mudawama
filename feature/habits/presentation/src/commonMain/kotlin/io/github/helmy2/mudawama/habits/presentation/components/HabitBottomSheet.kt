package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.presentation.model.BottomSheetMode
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiAction
import io.github.helmy2.mudawama.habits.presentation.util.HABIT_ICONS
import io.github.helmy2.mudawama.habits.presentation.util.iconKeyToImageVector
import kotlinx.datetime.DayOfWeek
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.action_close
import mudawama.shared.designsystem.action_save
import mudawama.shared.designsystem.hint_daily_goal
import mudawama.shared.designsystem.hint_habit_name_placeholder
import mudawama.shared.designsystem.label_daily_reminder
import mudawama.shared.designsystem.label_daily_reminder_subtitle
import mudawama.shared.designsystem.label_frequency
import mudawama.shared.designsystem.label_goal_type
import mudawama.shared.designsystem.label_habit_name
import mudawama.shared.designsystem.label_identity_icon
import mudawama.shared.designsystem.title_edit_habit
import mudawama.shared.designsystem.title_new_habit
import mudawama.shared.designsystem.type_checkoff_subtitle
import mudawama.shared.designsystem.type_checkoff_title
import mudawama.shared.designsystem.type_counter_subtitle
import mudawama.shared.designsystem.type_counter_title
import org.jetbrains.compose.resources.stringResource

private val DAYS_ORDERED = listOf(
    DayOfWeek.MONDAY to "M",
    DayOfWeek.TUESDAY to "T",
    DayOfWeek.WEDNESDAY to "W",
    DayOfWeek.THURSDAY to "T",
    DayOfWeek.FRIDAY to "F",
    DayOfWeek.SATURDAY to "S",
    DayOfWeek.SUNDAY to "S",
)

/**
 * Add / Edit habit bottom sheet — matches new_habit_bottom_sheet.png exactly.
 *
 * Structure:
 *   Header: × close | title | [Save] pill button
 *   HABIT NAME label + filled rounded TextField
 *   IDENTITY ICON label + horizontal LazyRow of circular icon chips
 *   FREQUENCY label + 7 circular day chips
 *   GOAL TYPE label + two side-by-side goal type cards
 *   Daily Reminder row + Switch
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
    var goalCountInput by remember {
        mutableStateOf(existingHabit?.goalCount?.toString() ?: "")
    }
    var reminderEnabled by remember { mutableStateOf(false) }

    // Inline validation state
    var nameError by remember { mutableStateOf(false) }
    var daysError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MudawamaTheme.colors.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.action_close),
                        tint = MudawamaTheme.colors.onSurface,
                    )
                }
                Text(
                    text = if (existingHabit != null) stringResource(Res.string.title_edit_habit)
                    else stringResource(Res.string.title_new_habit),
                    style = MudawamaTheme.typography.h3,
                    fontWeight = FontWeight.Bold,
                    color = MudawamaTheme.colors.onSurface,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
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
                                    goalCount = if (habitType == HabitType.NUMERIC)
                                        goalCountInput.toIntOrNull() else null,
                                )
                            )
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MudawamaTheme.colors.primary,
                        contentColor = MudawamaTheme.colors.onPrimary,
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp, vertical = 10.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(Res.string.action_save),
                        style = MudawamaTheme.typography.h5,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // ── HABIT NAME ────────────────────────────────────────────────────
            SheetSectionLabel(stringResource(Res.string.label_habit_name))
            TextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    nameError = false
                },
                placeholder = {
                    Text(
                        text = stringResource(Res.string.hint_habit_name_placeholder),
                        style = MudawamaTheme.typography.body1,
                        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.35f),
                    )
                },
                isError = nameError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MudawamaTheme.colors.surface,
                    unfocusedContainerColor = MudawamaTheme.colors.surface,
                    errorContainerColor = MudawamaTheme.colors.error.copy(alpha = 0.08f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedTextColor = MudawamaTheme.colors.onSurface,
                    unfocusedTextColor = MudawamaTheme.colors.onSurface,
                ),
            )

            // ── IDENTITY ICON ─────────────────────────────────────────────────
            SheetSectionLabel(stringResource(Res.string.label_identity_icon))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MudawamaTheme.colors.surface)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(HABIT_ICONS) { (key, vector) ->
                        val selected = key == iconKey
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) MudawamaTheme.colors.primary
                                    else Color.Transparent
                                )
                                .clickable { iconKey = key }
                                .padding(4.dp),
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = key,
                                tint = if (selected) MudawamaTheme.colors.onPrimary
                                else MudawamaTheme.colors.onSurface,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                }
            }

            // ── FREQUENCY ─────────────────────────────────────────────────────
            SheetSectionLabel(stringResource(Res.string.label_frequency))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DAYS_ORDERED.forEach { (day, abbrev) ->
                    val selected = day in selectedDays
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MudawamaTheme.colors.primary
                                else MudawamaTheme.colors.surface
                            )
                            .then(
                                if (daysError && !selected)
                                    Modifier.border(1.dp, MudawamaTheme.colors.error, CircleShape)
                                else Modifier
                            )
                            .clickable {
                                selectedDays = if (day in selectedDays)
                                    selectedDays - day else selectedDays + day
                                daysError = false
                            },
                    ) {
                        Text(
                            text = abbrev,
                            style = MudawamaTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) MudawamaTheme.colors.onPrimary
                            else MudawamaTheme.colors.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            // ── GOAL TYPE ─────────────────────────────────────────────────────
            SheetSectionLabel(stringResource(Res.string.label_goal_type))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GoalTypeCard(
                    title = stringResource(Res.string.type_checkoff_title),
                    subtitle = stringResource(Res.string.type_checkoff_subtitle),
                    isSelected = habitType == HabitType.BOOLEAN,
                    iconContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MudawamaTheme.colors.primary.copy(alpha = 0.12f)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MudawamaTheme.colors.primary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    },
                    onClick = { habitType = HabitType.BOOLEAN; goalCountInput = "" },
                    modifier = Modifier.weight(1f),
                )
                GoalTypeCard(
                    title = stringResource(Res.string.type_counter_title),
                    subtitle = stringResource(Res.string.type_counter_subtitle),
                    isSelected = habitType == HabitType.NUMERIC,
                    iconContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MudawamaTheme.colors.onSurface.copy(alpha = 0.08f)),
                        ) {
                            Text(
                                text = "123",
                                style = MudawamaTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                color = MudawamaTheme.colors.onSurface,
                            )
                        }
                    },
                    onClick = { habitType = HabitType.NUMERIC },
                    modifier = Modifier.weight(1f),
                )
            }

            // ── DAILY GOAL (Counter only) ──────────────────────────────────────
            if (habitType == HabitType.NUMERIC) {
                TextField(
                    value = goalCountInput,
                    onValueChange = { new ->
                        // Allow only digits, strip leading zeros
                        if (new.all { it.isDigit() }) goalCountInput = new
                    },
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.hint_daily_goal),
                            style = MudawamaTheme.typography.body1,
                            color = MudawamaTheme.colors.onSurface.copy(alpha = 0.35f),
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MudawamaTheme.colors.surface,
                        unfocusedContainerColor = MudawamaTheme.colors.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedTextColor = MudawamaTheme.colors.onSurface,
                        unfocusedTextColor = MudawamaTheme.colors.onSurface,
                    ),
                )
            }

            // ── Daily Reminder ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MudawamaTheme.colors.surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MudawamaTheme.colors.onSurface.copy(alpha = 0.08f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MudawamaTheme.colors.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.label_daily_reminder),
                        style = MudawamaTheme.typography.h4,
                        fontWeight = FontWeight.SemiBold,
                        color = MudawamaTheme.colors.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.label_daily_reminder_subtitle),
                        style = MudawamaTheme.typography.body2,
                        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.5f),
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MudawamaTheme.colors.onPrimary,
                        checkedTrackColor = MudawamaTheme.colors.primary,
                        uncheckedThumbColor = MudawamaTheme.colors.onSurface.copy(alpha = 0.4f),
                        uncheckedTrackColor = MudawamaTheme.colors.onSurface.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        style = MudawamaTheme.typography.caption,
        fontWeight = FontWeight.Bold,
        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.5f),
        letterSpacing = androidx.compose.ui.unit.TextUnit(
            1.2f, androidx.compose.ui.unit.TextUnitType.Sp,
        ),
    )
}

@Composable
private fun GoalTypeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) MudawamaTheme.colors.primary
    else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MudawamaTheme.colors.surface)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Selected indicator (top-right checkmark badge)
        Box(modifier = Modifier.fillMaxWidth()) {
            iconContent()
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MudawamaTheme.colors.primary)
                        .align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MudawamaTheme.colors.onPrimary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MudawamaTheme.typography.h4,
            fontWeight = FontWeight.SemiBold,
            color = MudawamaTheme.colors.onSurface,
        )
        Text(
            text = subtitle,
            style = MudawamaTheme.typography.body2,
            color = MudawamaTheme.colors.onSurface.copy(alpha = 0.5f),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun HabitBottomSheetAddPreview() {
    MudawamaTheme {
        HabitBottomSheet(
            mode = BottomSheetMode.AddHabit,
            onSave = {},
            onDismiss = {},
        )
    }
}
