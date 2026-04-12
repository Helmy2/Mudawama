package io.github.helmy2.mudawama.home.presentation.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.prayer_asr
import mudawama.shared.designsystem.prayer_dhuhr
import mudawama.shared.designsystem.prayer_fajr
import mudawama.shared.designsystem.prayer_isha
import mudawama.shared.designsystem.prayer_maghrib
import org.jetbrains.compose.resources.stringResource

/**
 * A read-only summary section showing today's habits with toggle/counter interactions.
 * Add/Edit/Delete operations are intentionally omitted here — handled in the Habits tab.
 */
@Composable
internal fun HabitsSummarySection(
    habits: List<HabitWithStatus>,
    isLoading: Boolean,
    onToggle: (String) -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    MudawamaSurfaceCard(modifier = modifier.fillMaxWidth()) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(3) {
                        SkeletonBlock(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(MaterialTheme.shapes.medium),
                        )
                    }
                }
            }

            habits.isEmpty() -> {
                // No habits yet — no action needed here, user manages in Habits tab
                Spacer(Modifier.height(24.dp))
            }

            else -> {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    habits.forEach { habitWithStatus ->
                        HabitSummaryRow(
                            habitWithStatus = habitWithStatus,
                            onToggle = { onToggle(habitWithStatus.habit.id) },
                            onIncrement = { onIncrement(habitWithStatus.habit.id) },
                            onDecrement = { onDecrement(habitWithStatus.habit.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getLocalizedHabitName(name: String, category: String): String {
    return when (category.lowercase()) {
        "prayer" -> {
            when (name.lowercase()) {
                "fajr" -> stringResource(Res.string.prayer_fajr)
                "dhuhr" -> stringResource(Res.string.prayer_dhuhr)
                "asr" -> stringResource(Res.string.prayer_asr)
                "maghrib" -> stringResource(Res.string.prayer_maghrib)
                "isha" -> stringResource(Res.string.prayer_isha)
                else -> name
            }
        }

        else -> name
    }
}

@Composable
private fun HabitSummaryRow(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithStatus.habit
    val todayLog = habitWithStatus.todayLog
    val isCompleted = todayLog?.status == LogStatus.COMPLETED
    val count = todayLog?.completedCount ?: 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = getLocalizedHabitName(habit.name, habit.category),
            style = MudawamaTheme.typography.body1,
            color = MudawamaTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        when (habit.type) {
            HabitType.BOOLEAN -> {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isCompleted) MudawamaTheme.colors.primary
                        else MudawamaTheme.colors.onSurface.copy(alpha = 0.3f),
                    )
                }
            }

            HabitType.NUMERIC -> {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = MudawamaTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = if (habit.goalCount != null) "$count / ${habit.goalCount}"
                    else "$count",
                    style = MudawamaTheme.typography.caption,
                    color = if (isCompleted) MudawamaTheme.colors.primary
                    else MudawamaTheme.colors.onSurface,
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MudawamaTheme.colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
