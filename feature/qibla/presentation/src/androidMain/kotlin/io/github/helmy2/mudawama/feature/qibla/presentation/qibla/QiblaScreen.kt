package io.github.helmy2.mudawama.feature.qibla.presentation.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.components.MudawamaTopAppBar
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaAction
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaError
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaState
import io.github.helmy2.mudawama.feature.qibla.presentation.viewmodel.QiblaViewModel
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.qibla_aligned
import mudawama.shared.designsystem.qibla_calibration_warning
import mudawama.shared.designsystem.qibla_go_to_settings
import mudawama.shared.designsystem.qibla_no_location
import mudawama.shared.designsystem.qibla_title
import mudawama.shared.designsystem.qibla_turn_left
import mudawama.shared.designsystem.qibla_turn_right
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun QiblaScreen(
    onNavigateBack: () -> Unit,
    viewModel: QiblaViewModel
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val previousAligned = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(QiblaAction.StartCompass)
    }

    LaunchedEffect(state.isAligned) {
        if (state.isAligned && !previousAligned.value) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        previousAligned.value = state.isAligned
    }

    QiblaScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QiblaScreenContent(
    state: QiblaState,
    onAction: (QiblaAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            MudawamaTopAppBar(
                title = { Text(stringResource(Res.string.qibla_title)) },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack,
                containerColor = MaterialTheme.colorScheme.surface,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        onAction = onAction
                    )
                }
                state.isLoading && state.qiblaAngle == null -> {
                    CircularProgressIndicator()
                }
                else -> {
                    CompassContent(state = state)
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: QiblaError,
    onAction: (QiblaAction) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val message = when (error) {
            is QiblaError.NoLocation -> stringResource(Res.string.qibla_no_location)
            is QiblaError.SensorUnavailable -> stringResource(Res.string.qibla_calibration_warning)
            is QiblaError.LocationError -> stringResource(Res.string.qibla_no_location)
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onAction(QiblaAction.RequestLocationPermission) }) {
            Text(stringResource(Res.string.qibla_go_to_settings))
        }
    }
}

@Composable
private fun CompassContent(
    state: QiblaState,
) {
    // Rotate to point to Qibla: (Current Heading + Qibla Angle)
    val rotation by animateFloatAsState(
        targetValue = (state.qiblaAngle?.toFloat() ?: 0f) - state.currentHeading.toFloat(),
        animationSpec = tween(durationMillis = 300),
        label = "compassRotation"
    )

    val directionText = if (state.isAligned) {
        stringResource(Res.string.qibla_aligned)
    } else {
        val diff = calculateTurnAngle(state.currentHeading, state.qiblaAngle ?: 0.0)
        if (diff >= 0) {
            stringResource(Res.string.qibla_turn_right, diff)
        } else {
            stringResource(Res.string.qibla_turn_left, -diff)
        }
    }

    val directionColor = if (state.isAligned) {
        Color(0xFF4CAF50) // Green when aligned
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Arrow pointing to Qibla
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .rotate(rotation),
            tint = directionColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = directionText,
            style = MaterialTheme.typography.headlineLarge,
            color = directionColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${state.qiblaAngle?.toInt() ?: 0}°",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        if (state.accuracy == io.github.helmy2.mudawama.feature.qibla.domain.model.CompassAccuracy.LOW ||
            state.accuracy == io.github.helmy2.mudawama.feature.qibla.domain.model.CompassAccuracy.UNRELIABLE) {
            Spacer(modifier = Modifier.height(16.dp))
            CalibrationWarning()
        }
    }
}

@Composable
private fun CalibrationWarning() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.qibla_calibration_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

private fun calculateTurnAngle(currentHeading: Double, qiblaAngle: Double): Int {
    var diff = qiblaAngle - currentHeading
    if (diff < 0) diff += 360
    if (diff > 180) diff = 360 - diff
    return diff.toInt()
}
