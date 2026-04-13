package io.github.helmy2.mudawama.home.presentation

// import io.github.helmy2.mudawama.designsystem.MudawamaAppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.helmy2.mudawama.core.presentation.util.ObserveAsEvents
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.designsystem.components.MudawamaTopAppBar
import io.github.helmy2.mudawama.home.presentation.components.AthkarSummaryCard
import io.github.helmy2.mudawama.home.presentation.components.HabitsSummarySection
import io.github.helmy2.mudawama.home.presentation.components.NextPrayerCard
import io.github.helmy2.mudawama.home.presentation.components.QuranProgressCard
import io.github.helmy2.mudawama.home.presentation.components.TasbeehSummaryCard
import io.github.helmy2.mudawama.home.presentation.model.HomeUiAction
import io.github.helmy2.mudawama.home.presentation.model.HomeUiEvent
import io.github.helmy2.mudawama.home.presentation.model.HomeUiState
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.action_view_all
import mudawama.shared.designsystem.home_daily_habits_button
import mudawama.shared.designsystem.home_daily_rituals_label
import mudawama.shared.designsystem.home_settings_icon_description
import mudawama.shared.designsystem.qibla_subtitle
import mudawama.shared.designsystem.qibla_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToPrayer: () -> Unit,
    onNavigateToAthkar: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToQibla: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(flow = viewModel.eventFlow) { event ->
        when (event) {
            is HomeUiEvent.Navigate.ToPrayer -> onNavigateToPrayer()
            is HomeUiEvent.Navigate.ToAthkar -> onNavigateToAthkar()
            is HomeUiEvent.Navigate.ToQuran -> onNavigateToQuran()
            is HomeUiEvent.Navigate.ToSettings -> onNavigateToSettings()
            is HomeUiEvent.Navigate.ToHabits -> onNavigateToHabits()
            is HomeUiEvent.Navigate.ToTasbeeh -> onNavigateToTasbeeh()
            is HomeUiEvent.Navigate.ToQibla -> onNavigateToQibla()
            is HomeUiEvent.ShowSnackbar -> { /* reserved for future use */
            }
        }
    }

    HomeScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenContent(
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        MudawamaTopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.home_daily_habits_button),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            actions = {
                IconButton(onClick = { onAction(HomeUiAction.SettingsIconTapped) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(Res.string.home_settings_icon_description),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Next Prayer (full-width) ──────────────────────────────────────
            NextPrayerCard(
                nextPrayerName = state.nextPrayerName,
                nextPrayerTime = state.nextPrayerTime,
                isPrayerLoading = state.isPrayerLoading,
                prayerTimesAvailable = state.prayerTimesAvailable,
                allPrayersDone = state.allPrayersDone,
                onClick = { onAction(HomeUiAction.PrayerCardTapped) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // ── Athkar (full-width) ───────────────────────────────────────────
            AthkarSummaryCard(
                athkarStatus = state.athkarStatus,
                isAthkarLoading = state.isAthkarLoading,
                athkarNotStarted = state.athkarNotStarted,
                onClick = { onAction(HomeUiAction.AthkarCardTapped) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // ── Quran + Tasbeeh (2-column row) ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuranProgressCard(
                    pagesReadToday = state.quranPagesReadToday,
                    goalPages = state.quranGoalPages,
                    progressFraction = state.progressFraction,
                    isQuranLoading = state.isQuranLoading,
                    onClick = { onAction(HomeUiAction.QuranCardTapped) },
                    modifier = Modifier.weight(1f),
                )
                TasbeehSummaryCard(
                    dailyTotal = state.tasbeehDailyTotal,
                    goal = state.tasbeehGoal,
                    progressFraction = state.tasbeehProgressFraction,
                    isLoading = state.isTasbeehLoading,
                    onClick = { onAction(HomeUiAction.TasbeehCardTapped) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Qibla Compass (full-width) ───────────────────────────────────────
            MudawamaSurfaceCard(
                onClick = { onAction(HomeUiAction.QiblaCardTapped) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.qibla_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(Res.string.qibla_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Daily Rituals section header ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.home_daily_rituals_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onAction(HomeUiAction.HabitsViewAllTapped) }) {
                    Text(
                        text = stringResource(Res.string.action_view_all),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Habits summary section ────────────────────────────────────────
            HabitsSummarySection(
                habits = state.habits,
                onToggle = { onAction(HomeUiAction.ToggleCompletion(it)) },
                onIncrement = { onAction(HomeUiAction.IncrementCount(it)) },
                onDecrement = { onAction(HomeUiAction.DecrementCount(it)) },
            )

            // Bottom padding for floating bottom bar
            Spacer(Modifier.height(96.dp))
        }
    }
}

@Preview
@Composable
private fun HomeScreenContentPreview() {
    MudawamaTheme(darkTheme = true) {
        HomeScreenContent(
            state = HomeUiState(),
            onAction = {},
        )
    }
}
