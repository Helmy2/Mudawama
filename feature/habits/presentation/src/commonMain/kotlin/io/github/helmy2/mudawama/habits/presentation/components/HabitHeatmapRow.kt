package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.HabitLog
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * A row of 7 small cells representing the last 7 logical days for a habit.
 *
 * Cell colour key:
 *  - Not scheduled on that day  → ghost (low-opacity outline)
 *  - Scheduled + COMPLETED      → primary (teal fill)
 *  - Scheduled + PENDING/null   → surfaceVariant (muted)
 *
 * `Clock.System.todayIn` is used here intentionally — this is a display-layer
 * day-of-week lookup only, not a business-logic date decision (see plan.md §4).
 */
@Composable
fun HabitHeatmapRow(
    weekLogs: List<HabitLog?>,
    frequencyDays: Set<DayOfWeek>,
    modifier: Modifier = Modifier,
) {
    // Display-layer date reference — acceptable per plan.md §4 note
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (i in 0..6) {
            val date = remember(i) { today.minus(DatePeriod(days = i)) }
            val isScheduled = date.dayOfWeek in frequencyDays
            val isCompleted = weekLogs.getOrNull(i)?.status == LogStatus.COMPLETED

            val cellColor = when {
                !isScheduled -> MudawamaTheme.colors.onSurface.copy(alpha = 0.12f)
                isCompleted  -> MudawamaTheme.colors.primary
                else         -> MudawamaTheme.colors.onSurface.copy(alpha = 0.25f)
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(cellColor, RoundedCornerShape(4.dp)),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitHeatmapRowPreview() {
    MudawamaTheme {
        HabitHeatmapRow(
            weekLogs = listOf(null, null, null, null, null, null, null),
            frequencyDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
            ),
        )
    }
}

