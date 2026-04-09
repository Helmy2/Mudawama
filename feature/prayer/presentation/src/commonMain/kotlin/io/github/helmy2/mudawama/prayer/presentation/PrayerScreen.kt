package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.prayer.presentation.components.PrayerCompletionHero
import io.github.helmy2.mudawama.prayer.presentation.components.PrayerRowItem
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    PrayerScreenContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
internal fun PrayerScreenContent(
    state: io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiState,
    onAction: (PrayerUiAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.usingFallbackLocation) {
            Text(
                text = "Using default location (Mecca)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        PrayerCompletionHero(prayers = state.prayers)
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.prayers) { prayer ->
                PrayerRowItem(
                    prayer = prayer,
                    onToggle = { onAction(PrayerUiAction.TogglePrayer(prayer.habitId)) },
                    onLongPress = { onAction(PrayerUiAction.MarkMissedRequested(prayer)) }
                )
            }
        }
    }
}
