package io.github.helmy2.mudawama.feature.qibla.presentation.qibla

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.UIKitViewController
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaAction
import io.github.helmy2.mudawama.feature.qibla.domain.ui.QiblaViewControllerProvider
import io.github.helmy2.mudawama.feature.qibla.presentation.viewmodel.QiblaViewModel
import io.github.helmy2.mudawama.settings.domain.AppSettings
import io.github.helmy2.mudawama.settings.domain.AppTheme
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import kotlinx.cinterop.ExperimentalForeignApi
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.qibla_aligned
import mudawama.shared.designsystem.qibla_calibration_warning
import mudawama.shared.designsystem.qibla_go_to_settings
import mudawama.shared.designsystem.qibla_no_location
import mudawama.shared.designsystem.qibla_title
import mudawama.shared.designsystem.qibla_turn_left
import mudawama.shared.designsystem.qibla_turn_right
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

data class QiblaStrings(
    val title: String,
    val goToSettings: String,
    val calibrationWarning: String,
    val aligned: String,
    val turnRight: String,
    val turnLeft: String,
    val locationRequired: String
)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QiblaScreen(
    onNavigateBack: () -> Unit,
    viewModel: QiblaViewModel
) {
    val viewControllerProvider = koinInject<QiblaViewControllerProvider>()
    val observeSettingsUseCase: ObserveSettingsUseCase = koinInject()
    val settings by observeSettingsUseCase().collectAsState(
        initial = AppSettings.DEFAULT
    )
    
    // Fetch strings reactively
    var strings by remember { mutableStateOf<QiblaStrings?>(null) }
    LaunchedEffect(settings.appLanguage) {
        strings = QiblaStrings(
            title = getString(Res.string.qibla_title),
            goToSettings = getString(Res.string.qibla_go_to_settings),
            calibrationWarning = getString(Res.string.qibla_calibration_warning),
            aligned = getString(Res.string.qibla_aligned),
            turnRight = getString(Res.string.qibla_turn_right),
            turnLeft = getString(Res.string.qibla_turn_left),
            locationRequired = getString(Res.string.qibla_no_location)
        )
    }

    QiblaScreenBridge.viewModel = viewModel
    QiblaScreenBridge.onNavigateBack = onNavigateBack
    QiblaScreenBridge.isDarkTheme = when (settings.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
    QiblaScreenBridge.isRTL = settings.appLanguage.isRtl
    QiblaScreenBridge.strings = strings
    
    DisposableEffect(Unit) {
        viewModel.onAction(QiblaAction.StartCompass)
        onDispose {
            QiblaScreenBridge.viewModel = null
            QiblaScreenBridge.onNavigateBack = null
        }
    }
    
    UIKitViewController(
        factory = { viewControllerProvider.createViewController() as UIViewController }
    )
}

object QiblaScreenBridge {
    var viewModel: QiblaViewModel? = null
    var onNavigateBack: (() -> Unit)? = null
    var isDarkTheme: Boolean = false
    var isRTL: Boolean = false
    var strings: QiblaStrings? = null
}
