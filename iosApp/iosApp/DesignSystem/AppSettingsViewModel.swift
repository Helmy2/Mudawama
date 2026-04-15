import SwiftUI
import MudawamaCore

// MARK: - App-level settings observer
// Owned by iOSApp / RootNavigationView so it lives for the full app lifetime.
// Drives .preferredColorScheme, layoutDirection, and locale for the whole view hierarchy.

@MainActor
class AppSettingsViewModel: ObservableObject {

    @Published var colorScheme: ColorScheme? = nil          // nil = follow system
    @Published var layoutDirection: LayoutDirection = .leftToRight
    @Published var locale: Locale = .current

    private let provider = SettingsUseCaseProvider()
    private var task: Task<Void, Never>? = nil

    func observe() {
        task?.cancel()
        task = Task {
            for await settings in provider.observeSettingsUseCase.invoke() {
                if Task.isCancelled { return }

                // Map AppTheme → SwiftUI ColorScheme?
                switch settings.appTheme {
                case .system: self.colorScheme = nil
                case .light:  self.colorScheme = .light
                case .dark:   self.colorScheme = .dark
                }

                // Map AppLanguage → LayoutDirection + Locale + Bundle
                let code = settings.appLanguage.code  // "en" or "ar"
                self.layoutDirection = settings.appLanguage.isRtl
                    ? .rightToLeft
                    : .leftToRight
                self.locale = Locale(identifier: code)
                CurrentBundle.shared.setLanguage(code)
            }
        }
    }

    deinit {
        task?.cancel()
    }
}
