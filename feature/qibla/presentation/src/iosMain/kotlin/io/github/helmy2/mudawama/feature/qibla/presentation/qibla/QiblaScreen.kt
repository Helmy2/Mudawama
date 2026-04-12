package io.github.helmy2.mudawama.feature.qibla.presentation.qibla

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitViewController
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaAction
import io.github.helmy2.mudawama.feature.qibla.domain.ui.QiblaViewControllerProvider
import io.github.helmy2.mudawama.feature.qibla.presentation.viewmodel.QiblaViewModel
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QiblaScreen(
    onNavigateBack: () -> Unit,
    viewModel: QiblaViewModel
) {
    // Get the Swift-implemented provider via Koin
    val viewControllerProvider = koinInject<QiblaViewControllerProvider>()
    
    // Store current parameters for Swift to access
    QiblaScreenBridge.viewModel = viewModel
    QiblaScreenBridge.onNavigateBack = onNavigateBack
    
    // Use DisposableEffect to start/stop compass when screen appears/disappears
    DisposableEffect(Unit) {
        // Start compass when screen appears
        viewModel.onAction(QiblaAction.StartCompass)
        
        onDispose {
            // Clear bridge references
            QiblaScreenBridge.viewModel = null
            QiblaScreenBridge.onNavigateBack = null
        }
    }
    
    UIKitViewController(
        factory = {
            // Call the Swift provider to create UIHostingController with SwiftUI view
            viewControllerProvider.createViewController() as UIViewController
        },
        update = { },
        modifier = Modifier
    )
}

/**
 * Bridge object that Swift can access to get ViewModel and callback
 */
object QiblaScreenBridge {
    var viewModel: QiblaViewModel? = null
    var onNavigateBack: (() -> Unit)? = null
}
