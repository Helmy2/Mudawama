package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.action_close
import mudawama.shared.designsystem.prayer_mark_missed_action
import mudawama.shared.designsystem.prayer_mark_missed_title
import mudawama.shared.designsystem.prayer_mark_pending_action
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom action sheet shown when the user long-presses a prayer row.
 * Offers "Mark as Missed" and "Undo / Mark Pending" actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkMissedBottomSheet(
    prayer: PrayerWithStatus,
    onMarkMissed: () -> Unit,
    onMarkPending: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.prayer_mark_missed_title) + " — ${prayer.name.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.action_close),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mark as Missed
            TextButton(
                onClick = onMarkMissed,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.prayer_mark_missed_action))
            }

            // Undo / Mark Pending
            TextButton(
                onClick = onMarkPending,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.prayer_mark_pending_action))
            }
        }
    }
}
