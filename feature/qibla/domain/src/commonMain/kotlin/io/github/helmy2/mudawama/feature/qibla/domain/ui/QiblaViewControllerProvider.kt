package io.github.helmy2.mudawama.feature.qibla.domain.ui

/**
 * Platform-specific provider for creating Qibla UI.
 * 
 * On iOS, this is implemented in Swift to provide a native SwiftUI view.
 * On Android, this interface is not needed (Compose is used directly).
 */
interface QiblaViewControllerProvider {
    /**
     * Creates a platform-specific view controller for the Qibla screen.
     * 
     * This method is called from iOS-side Compose code to get a UIViewController
     * that wraps the SwiftUI QiblaView.
     * 
     * @return A platform-specific view controller (UIViewController on iOS)
     */
    fun createViewController(): Any
}
