import SwiftUI
import MudawamaUI

@main
struct iOSApp: App {
    init() {
        let swiftEncryptor = IosEncryptor()
        let swiftLocationProvider = IosLocationProvider()
        let swiftNotificationProvider = IosNotificationProvider()
        let swiftQiblaViewControllerProvider = IosQiblaViewControllerProvider()
        KoinInitializerKt.initializeKoin(
            iosEncryptor: swiftEncryptor,
            iosLocationProvider: swiftLocationProvider,
            iosNotificationProvider: swiftNotificationProvider,
            iosQiblaViewControllerProvider: swiftQiblaViewControllerProvider
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}