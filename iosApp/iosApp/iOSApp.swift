import SwiftUI
import MudawamaUI

@main
struct iOSApp: App {
    init() {
        let swiftEncryptor = IosEncryptor()
        let swiftLocationProvider = IosLocationProvider()
        let swiftNotificationProvider = IosNotificationProvider()
        KoinInitializerKt.initializeKoin(
            iosEncryptor: swiftEncryptor,
            iosLocationProvider: swiftLocationProvider,
            iosNotificationProvider: swiftNotificationProvider
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}
