import SwiftUI
import MudawamaCore

// MARK: - Settings ViewModel

@MainActor
class SettingsViewModel: ObservableObject {

    @Published var settings: AppSettings = AppSettings.companion.DEFAULT
    @Published var isLoading = true

    private let provider = SettingsUseCaseProvider()
    private var observeTask: Task<Void, Never>? = nil

    func observe() {
        observeTask?.cancel()
        isLoading = true
        observeTask = Task {
            for await s in provider.observeSettingsUseCase.invoke() {
                if Task.isCancelled { return }
                self.settings = s
                self.isLoading = false
            }
        }
    }

    func setCalculationMethod(_ method: CalculationMethod) {
        Task { _ = try? await provider.setCalculationMethodUseCase.invoke(method: method) }
    }

    func setLocationMode(_ mode: LocationMode) {
        Task { _ = try? await provider.setLocationModeUseCase.invoke(mode: mode) }
    }

    func setTheme(_ theme: AppTheme) {
        Task { _ = try? await provider.setAppThemeUseCase.invoke(theme: theme) }
    }

    func setLanguage(_ language: AppLanguage) {
        Task { _ = try? await provider.setAppLanguageUseCase.invoke(language: language) }
    }

    func setMorningNotification(enabled: Bool) {
        Task {
            _ = try? await provider.setMorningNotificationUseCase.invoke(enabled: enabled)
        }
    }

    func setEveningNotification(enabled: Bool) {
        Task {
            _ = try? await provider.setEveningNotificationUseCase.invoke(enabled: enabled)
        }
    }

    deinit {
        observeTask?.cancel()
    }
}
