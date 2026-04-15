import SwiftUI
import MudawamaCore

@main
struct iOSApp: App {

    private let notificationProvider = IosNotificationProvider()

    init() {
        let swiftEncryptor = IosEncryptor()
        let swiftLocationProvider = IosLocationProvider()
        KoinInitializerKt.initializeKoin(
            iosEncryptor: swiftEncryptor,
            iosLocationProvider: swiftLocationProvider,
            iosNotificationProvider: notificationProvider
        )
    }

    var body: some Scene {
        WindowGroup {
            RootNavigationView()
                .task {
                    // Request notification permission on first launch.
                    // IosNotificationProvider shows a welcome notification when the user accepts.
                    await notificationProvider.requestPermissionAndNotifyIfGranted()
                }
        }
    }
}
