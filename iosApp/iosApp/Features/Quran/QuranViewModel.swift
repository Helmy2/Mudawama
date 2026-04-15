import SwiftUI
import MudawamaCore

// MARK: - Quran ViewModel

@MainActor
class QuranViewModel: ObservableObject {

    @Published var state: QuranScreenState? = nil
    @Published var isLoading = true
    @Published var errorMessage: String? = nil
    @Published var showLogSheet = false
    @Published var showGoalSheet = false
    @Published var showPositionSheet = false

    private let provider = QuranUseCaseProvider()
    private var observeTask: Task<Void, Never>? = nil

    func observe(date: Kotlinx_datetimeLocalDate) {
        observeTask?.cancel()
        isLoading = true
        observeTask = Task {
            for await screenState in provider.observeQuranStateUseCase.invoke(date: date) {
                if Task.isCancelled { return }
                self.state = screenState
                self.isLoading = false
            }
        }
    }

    func logReading(pages: Int, date: Kotlinx_datetimeLocalDate) {
        Task {
            _ = try? await provider.logReadingUseCase.invoke(pages: Int32(pages), date: date)
        }
    }

    func setGoal(pages: Int) {
        Task {
            _ = try? await provider.setGoalUseCase.invoke(pagesPerDay: Int32(pages))
        }
    }

    func updateBookmark(surah: Int, ayah: Int) {
        Task {
            _ = try? await provider.updateBookmarkUseCase.invoke(
                surahNumber: Int32(surah),
                ayahNumber: Int32(ayah)
            )
        }
    }

    deinit {
        observeTask?.cancel()
    }
}
