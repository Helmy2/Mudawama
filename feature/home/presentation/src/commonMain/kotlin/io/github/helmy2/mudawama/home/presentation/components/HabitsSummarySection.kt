package io.github.helmy2.mudawama.home.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.core.domain.model.LogStatus
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.prayer_asr
import mudawama.shared.designsystem.prayer_dhuhr
import mudawama.shared.designsystem.prayer_fajr
import mudawama.shared.designsystem.prayer_isha
import mudawama.shared.designsystem.prayer_maghrib
import org.jetbrains.compose.resources.stringResource

private val habitIcons = mapOf(
    "pray" to Icons.Default.Check,
    "quran" to Icons.AutoMirrored.Filled.MenuBook,
    "fast" to Icons.Default.Remove,
    "moon" to Icons.Default.NightsStay,
)

@Composable
private fun getHabitIcon(category: String): ImageVector {
    return habitIcons[category.lowercase()] ?: Icons.Default.Check
}

@Composable
internal fun HabitsSummarySection(
    habits: List<HabitWithStatus>,
    onToggle: (String) -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.animateContentSize()) {
        when {
            habits.isEmpty() -> {
                Spacer(Modifier.height(24.dp))
            }

            else -> {
                Column(
                    modifier = Modifier.padding(vertical = 0.dp),
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

@OptIn(ExperimentalFoundationApi::class)
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
    val primary = MaterialTheme.colorScheme.primary

    MudawamaSurfaceCard(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.12f)),
            ) {
                Icon(
                    imageVector = getHabitIcon(habit.category),
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLocalizedHabitName(habit.name, habit.category),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                when (habit.type) {
                    HabitType.BOOLEAN -> {
                        Text(
                            text = if (isCompleted) "Completed" else "Pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HabitType.NUMERIC -> {
                        Text(
                            text = if (habit.goalCount != null) "$count / ${habit.goalCount}" else "$count",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            when (habit.type) {
                HabitType.BOOLEAN -> {
                    when {
                        isCompleted -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(primary),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        else -> {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(34.dp),
                                strokeWidth = 1.5.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                trackColor = Color.Transparent,
                            )
                        }
                    }
                }
                HabitType.NUMERIC -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}