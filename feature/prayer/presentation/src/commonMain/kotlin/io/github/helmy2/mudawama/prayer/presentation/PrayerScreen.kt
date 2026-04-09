package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.prayer.presentation.components.MarkMissedBottomSheet
import io.github.helmy2.mudawama.prayer.presentation.components.PrayerCompletionHero
import io.github.helmy2.mudawama.prayer.presentation.components.PrayerDateStrip
import io.github.helmy2.mudawama.prayer.presentation.components.PrayerRowItem
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiState
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.error_times_unavailable
import mudawama.shared.designsystem.prayer_location_fallback
import mudawama.shared.designsystem.prayer_location_service_disabled
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val openLocationSettings = rememberOpenLocationSettings()

    LocationPermissionEffect(onAction = viewModel::onAction)

    LocationSettingsEffect(
        locationServiceDisabled = state.locationServiceDisabled,
        onAction = viewModel::onAction
    )

    PrayerScreenContent(
        state = state,
        onAction = { action ->
            if (action is PrayerUiAction.OpenLocationSettings) {
                openLocationSettings()
            } else {
                viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrayerScreenContent(
    state: PrayerUiState,
    onAction: (PrayerUiAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 7-day date strip
        PrayerDateStrip(
            dates = state.dateStrip,
            selectedDate = state.selectedDate,
            today = state.today,
            onDateSelected = { onAction(PrayerUiAction.SelectDate(it)) },
        )

        // Banner: location service is off
        if (state.locationServiceDisabled) {
            LocationServiceDisabledBanner(
                onClick = { onAction(PrayerUiAction.OpenLocationSettings) }
            )
        }

        // Error: times unavailable
        if (!state.timesAvailable && state.prayers.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.error_times_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        // Fallback location notice
        if (state.usingFallbackLocation) {
            Text(
                text = stringResource(Res.string.prayer_location_fallback),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        // Hero completion card
        PrayerCompletionHero(prayers = state.prayers)

        Spacer(Modifier.height(16.dp))

        // Prayer list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.prayers) { prayer ->
                PrayerRowItem(
                    prayer = prayer,
                    onToggle = if (!state.isReadOnly) {
                        { onAction(PrayerUiAction.TogglePrayer(prayer.habitId)) }
                    } else {
                        {}
                    },
                    onLongPress = if (!state.isReadOnly) {
                        { onAction(PrayerUiAction.MarkMissedRequested(prayer)) }
                    } else {
                        {}
                    },
                    enabled = !state.isReadOnly,
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    // Mark as Missed bottom sheet
    val sheetPrayer = state.missedSheetPrayer
    if (sheetPrayer != null) {
        MarkMissedBottomSheet(
            prayer = sheetPrayer,
            onMarkMissed = { onAction(PrayerUiAction.ConfirmMarkMissed(sheetPrayer.habitId)) },
            onMarkPending = { onAction(PrayerUiAction.ConfirmMarkPending(sheetPrayer.habitId)) },
            onDismiss = { onAction(PrayerUiAction.DismissMissedSheet) }
        )
    }
}

@Composable
private fun LocationServiceDisabledBanner(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.prayer_location_service_disabled),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}
