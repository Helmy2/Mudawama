package io.github.helmy2.mudawama.athkar.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.athkar_evening_title
import mudawama.shared.designsystem.athkar_group_complete
import mudawama.shared.designsystem.athkar_morning_title
import mudawama.shared.designsystem.athkar_post_prayer_title
import org.jetbrains.compose.resources.stringResource

/**
 * Overview card for a single Athkar group.
 * Shows group title, completion badge with progress, and navigates into [AthkarGroupScreen] on tap.
 */
@Composable
fun AthkarGroupCard(
    type: AthkarGroupType,
    isComplete: Boolean,
    completedCount: Int,
    totalCount: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MudawamaSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onTap,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(
                        when (type) {
                            AthkarGroupType.MORNING -> Res.string.athkar_morning_title
                            AthkarGroupType.EVENING -> Res.string.athkar_evening_title
                            AthkarGroupType.POST_PRAYER -> Res.string.athkar_post_prayer_title
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isComplete)
                        stringResource(Res.string.athkar_group_complete)
                    else
                        "$completedCount / $totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isComplete)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = if (isComplete)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isComplete)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview
@Composable
private fun AthkarGroupCardPreview() {
    // Minimal preview stub
}
