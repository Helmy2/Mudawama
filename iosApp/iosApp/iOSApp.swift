import SwiftUI
import MudawamaUI

@main
struct iOSApp: App {
    init() {
        let swiftEncryptor = IosEncryptor()
        let swiftLocationProvider = IosLocationProvider()
        KoinInitializerKt.initializeKoin(
            iosEncryptor: swiftEncryptor,
            iosLocationProvider: swiftLocationProvider
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}
