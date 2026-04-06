package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.presentation.util.iconKeyToImageVector
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mudawama.feature.habits.presentation.Res
import mudawama.feature.habits.presentation.cd_increment_count
import mudawama.feature.habits.presentation.cd_mark_complete
import mudawama.feature.habits.presentation.cd_mark_incomplete
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun HabitListItem(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithStatus.habit
    val todayLog = habitWithStatus.todayLog

    // Derived display properties (never stored — spec §HabitWithStatus KDoc)
    val isCompletedToday = todayLog?.status == LogStatus.COMPLETED
    val todayDayOfWeek = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek }
    val isDueToday = todayDayOfWeek in habit.frequencyDays
    val numericProgress = todayLog?.completedCount ?: 0
    val isNumericGoalReached = habit.goalCount != null && numericProgress >= habit.goalCount!!

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MudawamaTheme.colors.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(onLongPress) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Habit icon
                Icon(
                    imageVector = iconKeyToImageVector(habit.iconKey),
                    contentDescription = null,
                    tint = if (isDueToday) MudawamaTheme.colors.primary
                    else MudawamaTheme.colors.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Name
                Text(
                    text = habit.name,
                    style = MudawamaTheme.typography.h4,
                    color = if (isDueToday) MudawamaTheme.colors.onSurface
                    else MudawamaTheme.colors.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Completion control
                when (habit.type) {
                    HabitType.BOOLEAN -> {
                        IconButton(onClick = onToggle) {
                            Icon(
                                imageVector = if (isCompletedToday) Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isCompletedToday)
                                    stringResource(Res.string.cd_mark_incomplete)
                                else
                                    stringResource(Res.string.cd_mark_complete),
                                tint = if (isCompletedToday) MudawamaTheme.colors.primary
                                else MudawamaTheme.colors.onSurface.copy(alpha = 0.4f),
                            )
                        }
                    }

                    HabitType.NUMERIC -> {
                        val progressLabel = if (habit.goalCount != null)
                            "$numericProgress / ${habit.goalCount}"
                        else
                            "$numericProgress"

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = progressLabel,
                                style = MudawamaTheme.typography.body2,
                                color = if (isNumericGoalReached) MudawamaTheme.colors.primary
                                else MudawamaTheme.colors.onSurface,
                            )
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = onIncrement) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = stringResource(Res.string.cd_increment_count),
                                    tint = if (isNumericGoalReached) MudawamaTheme.colors.primary
                                    else MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HabitHeatmapRow(
                weekLogs = habitWithStatus.weekLogs,
                frequencyDays = habit.frequencyDays,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitListItemPreview() {
    MudawamaTheme {
        HabitListItem(
            habitWithStatus = HabitWithStatus(
                habit = Habit(
                    id = "1",
                    name = "Read Quran",
                    iconKey = "book",
                    type = HabitType.BOOLEAN,
                    category = "custom",
                    frequencyDays = DayOfWeek.entries.toSet(),
                    isCore = false,
                    goalCount = null,
                    createdAt = 0L,
                ),
                todayLog = null,
                weekLogs = List(7) { null },
            ),
            onToggle = {},
            onIncrement = {},
            onLongPress = {},
        )
    }
}
