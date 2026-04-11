package io.github.helmy2.mudawama.athkar.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.athkar_item_count_format
import mudawama.shared.designsystem.athkar_tap_to_count
import org.jetbrains.compose.resources.stringResource

/**
 * Displays a single Athkar item with its transliteration, count, and completion state.
 * - Tap: increments the counter (no-op when already complete).
 * - Long-press when complete: resets the counter back to 0.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AthkarItemCard(
    displayItem: AthkarDisplayItem,
    currentCount: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isComplete = currentCount >= displayItem.item.targetCount

    MudawamaSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        onClick = null, // we handle clicks via combinedClickable on the inner row
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { if (!isComplete) onTap() },
                    onLongClick = { if (isComplete) onLongPress() },
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(displayItem.transliteration),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(displayItem.translation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!isComplete) {
                    Text(
                        text = stringResource(Res.string.athkar_tap_to_count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = if (isComplete)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(52.dp),
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (isComplete) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Text(
                            text = stringResource(
                                Res.string.athkar_item_count_format,
                                currentCount,
                                displayItem.item.targetCount,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AthkarItemCardPreview() {
    // Minimal preview stub
}

