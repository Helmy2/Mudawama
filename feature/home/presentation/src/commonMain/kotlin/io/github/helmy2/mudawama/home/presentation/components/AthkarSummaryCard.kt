package io.github.helmy2.mudawama.home.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.designsystem.components.Skeleton
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.home_athkar_done
import mudawama.shared.designsystem.home_athkar_evening_label
import mudawama.shared.designsystem.home_athkar_morning_label
import mudawama.shared.designsystem.home_athkar_not_started
import mudawama.shared.designsystem.home_athkar_pending
import org.jetbrains.compose.resources.stringResource

@Composable
fun AthkarSummaryCard(
    athkarStatus: Map<AthkarGroupType, Boolean>,
    isAthkarLoading: Boolean,
    athkarNotStarted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MudawamaSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when {
                isAthkarLoading -> {
                    Skeleton(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                    Spacer(Modifier.height(8.dp))
                    Skeleton(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }
                athkarNotStarted -> {
                    AthkarRow(
                        label = stringResource(Res.string.home_athkar_morning_label),
                        statusText = stringResource(Res.string.home_athkar_not_started),
                        isDone = false,
                    )
                    Spacer(Modifier.height(6.dp))
                    AthkarRow(
                        label = stringResource(Res.string.home_athkar_evening_label),
                        statusText = stringResource(Res.string.home_athkar_not_started),
                        isDone = false,
                    )
                }
                else -> {
                    val morningDone = athkarStatus[AthkarGroupType.MORNING] == true
                    val eveningDone = athkarStatus[AthkarGroupType.EVENING] == true
                    AthkarRow(
                        label = stringResource(Res.string.home_athkar_morning_label),
                        statusText = if (morningDone) stringResource(Res.string.home_athkar_done)
                        else stringResource(Res.string.home_athkar_pending),
                        isDone = morningDone,
                    )
                    Spacer(Modifier.height(6.dp))
                    AthkarRow(
                        label = stringResource(Res.string.home_athkar_evening_label),
                        statusText = if (eveningDone) stringResource(Res.string.home_athkar_done)
                        else stringResource(Res.string.home_athkar_pending),
                        isDone = eveningDone,
                    )
                }
            }
        }
    }
}

@Composable
private fun AthkarRow(
    label: String,
    statusText: String,
    isDone: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isDone) Icons.Default.Check else Icons.Default.Circle,
            contentDescription = null,
            tint = if (isDone) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.height(14.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = 6.dp)
                .weight(1f),
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = if (isDone) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

@Preview
@Composable
private fun AthkarSummaryCardPreview() {
    MudawamaTheme(darkTheme = true) {
        AthkarSummaryCard(
            athkarStatus = mapOf(
                AthkarGroupType.MORNING to true,
                AthkarGroupType.EVENING to false,
            ),
            isAthkarLoading = false,
            athkarNotStarted = false,
            onClick = {},
        )
    }
}
