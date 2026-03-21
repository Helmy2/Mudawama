import SwiftUI
import MudawamaUI

@main
struct iOSApp: App {
    init() {
        let swiftEncryptor = IosEncryptor()
        KoinInitializerKt.initializeKoin(iosEncryptor: swiftEncryptor)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
