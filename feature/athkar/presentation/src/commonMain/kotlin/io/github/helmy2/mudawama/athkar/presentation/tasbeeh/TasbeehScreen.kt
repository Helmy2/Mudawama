package io.github.helmy2.mudawama.athkar.presentation.tasbeeh

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.tasbeeh_cd_tap_button
import mudawama.shared.designsystem.tasbeeh_daily_total_label
import mudawama.shared.designsystem.tasbeeh_goal_button
import mudawama.shared.designsystem.tasbeeh_goal_reached_label
import mudawama.shared.designsystem.tasbeeh_reset_button
import mudawama.shared.designsystem.tasbeeh_screen_title
import mudawama.shared.designsystem.tasbeeh_session_count_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry-point composable — owns the ViewModel and delegates to [TasbeehContent].
 */
@Composable
fun TasbeehScreen(
    viewModel: TasbeehViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    TasbeehContent(
        state = state,
        onAction = viewModel::onAction,
        eventFlow = viewModel.eventFlow,
    )
}

@Composable
internal fun TasbeehContent(
    state: TasbeehUiState,
    onAction: (TasbeehUiAction) -> Unit,
    eventFlow: kotlinx.coroutines.flow.Flow<TasbeehUiEvent>,
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        eventFlow.collect { event ->
            when (event) {
                TasbeehUiEvent.TapHaptic -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                TasbeehUiEvent.GoalReached -> {
                    repeat(3) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.tasbeeh_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(40.dp))

        TasbeehTapButton(
            sessionCount = state.sessionCount,
            goalCount = state.goalCount,
            progress = state.goalProgress,
            isGoalReached = state.isGoalReached,
            contentDescription = stringResource(Res.string.tasbeeh_cd_tap_button),
            onClick = { onAction(TasbeehUiAction.Tap) },
        )

        Spacer(Modifier.height(40.dp))

        if (state.isGoalReached) {
            Text(
                text = stringResource(Res.string.tasbeeh_goal_reached_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatColumn(
                label = stringResource(Res.string.tasbeeh_session_count_label),
                value = state.sessionCount.toString(),
            )
            StatColumn(
                label = stringResource(Res.string.tasbeeh_daily_total_label),
                value = state.dailyTotal.toString(),
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(onClick = { onAction(TasbeehUiAction.Reset) }) {
                Text(
                    text = stringResource(Res.string.tasbeeh_reset_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = { onAction(TasbeehUiAction.OpenGoalSheet) }) {
                Text(
                    text = stringResource(Res.string.tasbeeh_goal_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(96.dp))
    }

    if (state.isGoalSheetVisible) {
        TasbeehGoalBottomSheet(state = state, onAction = onAction)
    }
}

@Composable
private fun TasbeehTapButton(
    sessionCount: Int,
    goalCount: Int,
    progress: Float,
    isGoalReached: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = surfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            if (progress > 0f) {
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = if (isGoalReached) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(196.dp)
                .clip(CircleShape)
                .clickable(onClickLabel = contentDescription, onClick = onClick),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = sessionCount.toString(),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "/ $goalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun TasbeehScreenPreview() {
    // Minimal preview stub
}

