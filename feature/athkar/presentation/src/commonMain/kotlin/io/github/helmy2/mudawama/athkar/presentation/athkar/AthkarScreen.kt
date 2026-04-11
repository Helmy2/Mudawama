package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.presentation.component.AthkarGroupCard
import io.github.helmy2.mudawama.athkar.presentation.component.displayItemsFor
import io.github.helmy2.mudawama.athkar.presentation.notification.AthkarNotificationUiAction
import io.github.helmy2.mudawama.athkar.presentation.notification.AthkarNotificationViewModel
import io.github.helmy2.mudawama.core.presentation.util.ObserveAsEvents
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.athkar_screen_title
import mudawama.shared.designsystem.notification_athkar_evening_body
import mudawama.shared.designsystem.notification_athkar_evening_title
import mudawama.shared.designsystem.notification_athkar_morning_body
import mudawama.shared.designsystem.notification_athkar_morning_title
import mudawama.shared.designsystem.notification_settings_evening_label
import mudawama.shared.designsystem.notification_settings_morning_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AthkarScreen(
    viewModel: AthkarViewModel = koinViewModel(),
    notificationViewModel: AthkarNotificationViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val notifState by notificationViewModel.state.collectAsState()

    ObserveAsEvents(viewModel.eventFlow) { /* GroupCompleted handled via state */ }

    // Notification permission request — fires once when the screen is first composed.
    // Implemented per-platform in androidMain / iosMain expect/actual.
    RequestNotificationPermissionEffect()

    if (state.activeGroup != null) {
        AthkarGroupScreen(
            state = state,
            onAction = viewModel::onAction,
        )
    } else {
        val morningTitle  = stringResource(Res.string.notification_athkar_morning_title)
        val morningBody   = stringResource(Res.string.notification_athkar_morning_body)
        val eveningTitle  = stringResource(Res.string.notification_athkar_evening_title)
        val eveningBody   = stringResource(Res.string.notification_athkar_evening_body)

        AthkarOverviewContent(
            state = state,
            onAction = viewModel::onAction,
            morningNotifEnabled = notifState.morningPreference.enabled,
            eveningNotifEnabled = notifState.eveningPreference.enabled,
            onToggleMorning = { enabled ->
                notificationViewModel.onAction(
                    AthkarNotificationUiAction.ToggleMorning(enabled, morningTitle, morningBody)
                )
            },
            onToggleEvening = { enabled ->
                notificationViewModel.onAction(
                    AthkarNotificationUiAction.ToggleEvening(enabled, eveningTitle, eveningBody)
                )
            },
        )
    }
}

@Composable
internal fun AthkarOverviewContent(
    state: AthkarUiState,
    onAction: (AthkarUiAction) -> Unit,
    morningNotifEnabled: Boolean = false,
    eveningNotifEnabled: Boolean = false,
    onToggleMorning: (Boolean) -> Unit = {},
    onToggleEvening: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.athkar_screen_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.today,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Group cards + Notification settings ───────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AthkarGroupType.entries.forEach { type ->
                val displayItems = displayItemsFor(type)
                val domainItems = displayItems.map { it.item }
                val totalCount = if (type == AthkarGroupType.POST_PRAYER) 5 else domainItems.size
                AthkarGroupCard(
                    type = type,
                    isComplete = state.completionStatus[type] ?: false,
                    completedCount = state.completedCountFor(type, domainItems),
                    totalCount = totalCount,
                    onTap = { onAction(AthkarUiAction.OpenGroup(type)) },
                )
            }

            // ── Notification Settings Section ──────────────────────────────
            Spacer(Modifier.height(4.dp))
            NotificationToggleRow(
                label = stringResource(Res.string.notification_settings_morning_label),
                enabled = morningNotifEnabled,
                onToggle = onToggleMorning,
            )
            NotificationToggleRow(
                label = stringResource(Res.string.notification_settings_evening_label),
                enabled = eveningNotifEnabled,
                onToggle = onToggleEvening,
            )

            Spacer(Modifier.height(96.dp))
        }
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
        )
    }
}

@Preview
@Composable
private fun AthkarOverviewContentPreview() {
    // Minimal preview stub
}
