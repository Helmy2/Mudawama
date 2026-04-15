import SwiftUI
import MudawamaCore
import UIKit

// MARK: - Tasbeeh ViewModel

@MainActor
class TasbeehViewModel: ObservableObject {

    @Published var goal: TasbeehGoal? = nil
    @Published var dailyTotal: TasbeehDailyTotal? = nil
    @Published var isLoading = true

    private let provider = TasbeehUseCaseProvider()
    private var goalTask: Task<Void, Never>? = nil
    private var totalTask: Task<Void, Never>? = nil
    private let haptics = UIImpactFeedbackGenerator(style: .medium)

    func observe(date: String) {
        isLoading = true
        goalTask?.cancel()
        totalTask?.cancel()

        goalTask = Task {
            for await g in provider.observeTasbeehGoalUseCase.invoke() {
                if Task.isCancelled { return }
                self.goal = g
                self.isLoading = false
            }
        }

        totalTask = Task {
            for await t in provider.observeTasbeehDailyTotalUseCase.invoke(date: date) {
                if Task.isCancelled { return }
                self.dailyTotal = t
            }
        }
    }

    func increment(date: String) {
        haptics.impactOccurred()
        Task {
            _ = try? await provider.addToTasbeehDailyUseCase.invoke(date: date, amount: 1)
        }
    }

    func setGoal(count: Int) {
        Task {
            _ = try? await provider.setTasbeehGoalUseCase.invoke(goalCount: Int32(count))
        }
    }

    deinit {
        goalTask?.cancel()
        totalTask?.cancel()
    }
}
