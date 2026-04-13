package io.github.helmy2.mudawama.quran.presentation

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.components.CircularProgressIndicator
import io.github.helmy2.mudawama.designsystem.components.MudawamaDateStrip
import io.github.helmy2.mudawama.quran.presentation.components.QuranGoalCard
import io.github.helmy2.mudawama.quran.presentation.components.QuranRecentLogsList
import io.github.helmy2.mudawama.quran.presentation.components.QuranResumeReadingCard
import io.github.helmy2.mudawama.quran.presentation.model.QuranUiAction
import io.github.helmy2.mudawama.quran.presentation.model.QuranUiState
import io.github.helmy2.mudawama.quran.presentation.sheets.LogReadingSheet
import io.github.helmy2.mudawama.quran.presentation.sheets.SetGoalSheet
import io.github.helmy2.mudawama.quran.presentation.sheets.UpdatePositionSheet
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.quran_daily_progress_subtitle_complete
import mudawama.shared.designsystem.quran_daily_progress_subtitle_in_progress
import mudawama.shared.designsystem.quran_log_reading_button
import mudawama.shared.designsystem.quran_of_pages_format
import mudawama.shared.designsystem.quran_streak_days_format
import mudawama.shared.designsystem.quran_streak_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuranScreen(
    viewModel: QuranViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    QuranScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
internal fun QuranScreenContent(
    state: QuranUiState,
    onAction: (QuranUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // Date strip
        MudawamaDateStrip(
            dates = state.dateStrip,
            selectedDate = state.selectedDate,
            today = state.today,
            onDateSelected = { onAction(QuranUiAction.SelectDate(it)) },
        )

        // Progress ring
        CircularProgressIndicator(
            currentValue = state.pagesReadToday,
            progressFraction = state.progressFraction,
            isLoading = state.isLoading,
            centerLabel = stringResource(Res.string.quran_of_pages_format, state.pagesReadToday, state.goalPages)
                .substringAfter("OF "),
            subtitle = if (state.progressFraction >= 1f) {
                stringResource(Res.string.quran_daily_progress_subtitle_complete)
            } else {
                stringResource(Res.string.quran_daily_progress_subtitle_in_progress)
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        // Streak row — only show when streak > 0
        if (state.streak > 0) {
            QuranStreakRow(
                streak = state.streak,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Goal card (tap to edit goal in non-read-only mode)
            QuranGoalCard(
                goalPages = state.goalPages,
                isReadOnly = state.isReadOnly,
                onTap = { onAction(QuranUiAction.OpenSetGoalSheet) },
                onLogReadingClick = { onAction(QuranUiAction.OpenLogReadingSheet) },
            )

            // Resume Reading card
            QuranResumeReadingCard(
                bookmark = state.bookmark,
                onTap = { onAction(QuranUiAction.OpenUpdatePositionSheet) },
            )

            // Log Reading button — hidden in read-only mode
            if (!state.isReadOnly) {
                Button(
                    onClick = { onAction(QuranUiAction.OpenLogReadingSheet) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = stringResource(Res.string.quran_log_reading_button),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Recent logs
            QuranRecentLogsList(
                logs = state.recentLogs,
                onViewAll = { /* future: navigate to full history */ },
            )

            Spacer(Modifier.height(96.dp))
        }
    }

    // Sheets
    if (state.logReadingSheetVisible) {
        LogReadingSheet(
            pageInput = state.logReadingPageInput,
            goalPages = state.goalPages,
            onPageInputChange = { onAction(QuranUiAction.UpdateLogPageInput(it)) },
            onConfirm = { onAction(QuranUiAction.ConfirmLogReading(it)) },
            onDismiss = { onAction(QuranUiAction.DismissLogReadingSheet) },
        )
    }

    if (state.setGoalSheetVisible) {
        SetGoalSheet(
            currentGoal = state.goalPages,
            onSave = { onAction(QuranUiAction.ConfirmSetGoal(it)) },
            onDismiss = { onAction(QuranUiAction.DismissSetGoalSheet) },
        )
    }

    if (state.updatePositionSheetVisible) {
        UpdatePositionSheet(
            initialSurah = state.bookmark?.surah ?: 1,
            initialAyah = state.bookmark?.ayah ?: 1,
            onDone = { surah, ayah -> onAction(QuranUiAction.ConfirmUpdatePosition(surah, ayah)) },
            onDismiss = { onAction(QuranUiAction.DismissUpdatePositionSheet) },
        )
    }
}

@Composable
private fun QuranStreakRow(
    streak: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(Res.string.quran_streak_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.quran_streak_days_format, streak),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview
@Composable
private fun QuranScreenContentPreview() {
    // Minimal preview — no data
}
