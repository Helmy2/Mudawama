package io.github.helmy2.mudawama.home.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.designsystem.components.Skeleton
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.home_tasbeeh_count_progress
import mudawama.shared.designsystem.home_tasbeeh_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasbeehSummaryCard(
    dailyTotal: Int,
    goal: Int,
    progressFraction: Float,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MudawamaSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.home_tasbeeh_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Skeleton(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.medium),
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.size(56.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        strokeWidth = 5.dp,
                    )
                    Text(
                        text = "$dailyTotal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        Res.string.home_tasbeeh_count_progress,
                        dailyTotal,
                        goal,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun TasbeehSummaryCardPreview() {
    MudawamaTheme(darkTheme = false) {
        TasbeehSummaryCard(
            dailyTotal = 67,
            goal = 100,
            progressFraction = 0.67f,
            isLoading = false,
            onClick = {},
        )
    }
}
