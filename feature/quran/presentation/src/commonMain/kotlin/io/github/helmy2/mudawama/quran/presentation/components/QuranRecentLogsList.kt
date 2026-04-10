package io.github.helmy2.mudawama.quran.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.quran.domain.model.QuranScreenState
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.quran_log_status_hit_goal
import mudawama.shared.designsystem.quran_log_status_over_goal
import mudawama.shared.designsystem.quran_log_status_under_goal
import mudawama.shared.designsystem.quran_of_pages_format
import mudawama.shared.designsystem.quran_recent_logs_title
import mudawama.shared.designsystem.quran_recent_logs_view_all
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuranRecentLogsList(
    logs: List<QuranScreenState.RecentLogEntry>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (logs.isEmpty()) return

    MudawamaSurfaceCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.quran_recent_logs_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onViewAll) {
                    Text(
                        text = stringResource(Res.string.quran_recent_logs_view_all),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            logs.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                    )
                }
                RecentLogRow(entry = entry)
            }
        }
    }
}

@Composable
private fun RecentLogRow(
    entry: QuranScreenState.RecentLogEntry,
    modifier: Modifier = Modifier,
) {
    val (statusText, statusColor, iconVector) = when (entry.status) {
        QuranScreenState.LogStatus.OVER -> Triple(
            stringResource(Res.string.quran_log_status_over_goal),
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle,
        )
        QuranScreenState.LogStatus.HIT -> Triple(
            stringResource(Res.string.quran_log_status_hit_goal),
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle,
        )
        QuranScreenState.LogStatus.UNDER -> Triple(
            stringResource(Res.string.quran_log_status_under_goal),
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.RadioButtonUnchecked,
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.quran_of_pages_format, entry.pagesRead, entry.goalPages),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusColor.copy(alpha = 0.12f),
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Preview
@Composable
private fun QuranRecentLogsListPreview() {
    QuranRecentLogsList(
        logs = listOf(
            QuranScreenState.RecentLogEntry("2026-04-09", 8, 5),
            QuranScreenState.RecentLogEntry("2026-04-08", 5, 5),
            QuranScreenState.RecentLogEntry("2026-04-07", 3, 5),
        ),
        onViewAll = {},
    )
}
