package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.cd_mark_complete
import mudawama.shared.designsystem.cd_mark_incomplete
import mudawama.shared.designsystem.habit_progress_count
import mudawama.shared.designsystem.habit_progress_fraction
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.presentation.util.iconKeyToImageVector
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/**
 * Row for a CORE RITUAL habit (isCore = true).
 *
 * Layout (matches daily_habits.png CORE RITUALS section):
 *   [circular progress ring] | habit name (bold) + subtitle | [chevron >]
 *
 * The ring shows numeric progress (completedCount / goalCount) for NUMERIC habits,
 * or a full green ring with a check icon for completed BOOLEAN habits.
 */
@Composable
fun HabitCoreRitualItem(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithStatus.habit
    val todayLog = habitWithStatus.todayLog
    val isCompleted = todayLog?.status == LogStatus.COMPLETED
    val numericProgress = todayLog?.completedCount ?: 0
    val goal = habit.goalCount ?: 1

    val progressFraction = when (habit.type) {
        HabitType.BOOLEAN -> if (isCompleted) 1f else 0f
        HabitType.NUMERIC -> if (goal > 0) (numericProgress.toFloat() / goal).coerceIn(0f, 1f) else 0f
    }

    val progressLabel = when (habit.type) {
        HabitType.BOOLEAN -> if (isCompleted) "1/1" else "0/1"
        HabitType.NUMERIC -> stringResource(Res.string.habit_progress_fraction, numericProgress, goal)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MudawamaTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Circular progress ring ─────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp),
            ) {
                // Track (background arc)
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(56.dp),
                    color = MudawamaTheme.colors.primary.copy(alpha = 0.12f),
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round,
                )
                // Filled arc
                CircularProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.size(56.dp),
                    color = MudawamaTheme.colors.primary,
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round,
                )
                // Center label
                Text(
                    text = progressLabel,
                    style = MudawamaTheme.typography.caption,
                    color = MudawamaTheme.colors.onSurface,
                )
            }

            Spacer(Modifier.width(16.dp))

            // ── Name + subtitle ────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MudawamaTheme.typography.h4,
                    color = MudawamaTheme.colors.onSurface,
                )
                if (isCompleted) {
                    Text(
                        text = stringResource(Res.string.cd_mark_incomplete),
                        style = MudawamaTheme.typography.body2,
                        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.5f),
                    )
                }
            }

            // ── Chevron ────────────────────────────────────────────────────
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MudawamaTheme.colors.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Row for a PERSONAL HABIT (isCore = false).
 *
 * Layout (matches daily_habits.png PERSONAL HABITS section):
 *   [icon chip — teal circle] | habit name | [MoreVert ⋮]
 */
@Composable
fun HabitPersonalItem(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithStatus.habit
    val todayLog = habitWithStatus.todayLog
    val todayDayOfWeek = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek }
    val isDueToday = todayDayOfWeek in habit.frequencyDays

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MudawamaTheme.colors.surface.copy(alpha = if (isDueToday) 1f else 0.6f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Icon chip ──────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MudawamaTheme.colors.surface),
            ) {
                Icon(
                    imageVector = iconKeyToImageVector(habit.iconKey),
                    contentDescription = null,
                    tint = MudawamaTheme.colors.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }

            // ── Habit name ─────────────────────────────────────────────────
            Text(
                text = habit.name,
                style = MudawamaTheme.typography.h4,
                color = MudawamaTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )

            // ── More (⋮) button ────────────────────────────────────────────
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(Res.string.cd_mark_complete),
                    tint = MudawamaTheme.colors.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HabitCoreRitualItemPreview() {
    MudawamaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            HabitCoreRitualItem(
                habitWithStatus = HabitWithStatus(
                    habit = Habit(
                        id = "1", name = "Prayers", iconKey = "pray",
                        type = HabitType.NUMERIC, category = "PRAYER",
                        frequencyDays = DayOfWeek.entries.toSet(),
                        isCore = true, goalCount = 5, createdAt = 0L,
                    ),
                    todayLog = null,
                    weekLogs = List(7) { null },
                ),
                onToggle = {},
                onIncrement = {},
            )
            HabitCoreRitualItem(
                habitWithStatus = HabitWithStatus(
                    habit = Habit(
                        id = "2", name = "Athkar", iconKey = "star",
                        type = HabitType.BOOLEAN, category = "ATHKAR",
                        frequencyDays = DayOfWeek.entries.toSet(),
                        isCore = true, goalCount = null, createdAt = 0L,
                    ),
                    todayLog = null,
                    weekLogs = List(7) { null },
                ),
                onToggle = {},
                onIncrement = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitPersonalItemPreview() {
    MudawamaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            HabitPersonalItem(
                habitWithStatus = HabitWithStatus(
                    habit = Habit(
                        id = "3", name = "Fasting Mondays", iconKey = "moon",
                        type = HabitType.BOOLEAN, category = "custom",
                        frequencyDays = setOf(DayOfWeek.MONDAY),
                        isCore = false, goalCount = null, createdAt = 0L,
                    ),
                    todayLog = null,
                    weekLogs = List(7) { null },
                ),
                onToggle = {},
                onMoreClick = {},
            )
            HabitPersonalItem(
                habitWithStatus = HabitWithStatus(
                    habit = Habit(
                        id = "4", name = "Give Sadaqah", iconKey = "heart",
                        type = HabitType.BOOLEAN, category = "custom",
                        frequencyDays = DayOfWeek.entries.toSet(),
                        isCore = false, goalCount = null, createdAt = 0L,
                    ),
                    todayLog = null,
                    weekLogs = List(7) { null },
                ),
                onToggle = {},
                onMoreClick = {},
            )
        }
    }
}
