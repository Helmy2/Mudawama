import SwiftUI
import MudawamaCore

// MARK: - Habits ViewModel

@MainActor
class HabitsViewModel: ObservableObject {

    @Published var habits: [HabitWithStatus] = []
    @Published var isLoading = true

    private let provider = HabitsUseCaseProvider()
    private var observeTask: Task<Void, Never>? = nil

    func observe() {
        observeTask?.cancel()
        isLoading = true
        observeTask = Task {
            for await list in provider.observeHabitsWithTodayStatusUseCase.invoke() {
                if Task.isCancelled { return }
                self.habits = list
                self.isLoading = false
            }
        }
    }

    func toggle(habitId: String) {
        Task {
            _ = try? await provider.toggleHabitCompletionUseCase.invoke(habitId: habitId)
        }
    }

    func create(name: String, type: HabitType, goalCount: Int?) {
        Task {
            _ = try? await provider.createHabitUseCase.invoke(
                name: name,
                iconKey: "star",
                frequencyDays: Set(Kotlinx_datetimeDayOfWeek.allCases),
                type: type,
                goalCount: goalCount.map { KotlinInt(value: Int32($0)) },
                category: "custom"
            )
        }
    }

    func delete(habitId: String) {
        Task {
            _ = try? await provider.deleteHabitUseCase.invoke(habitId: habitId)
        }
    }

    deinit {
        observeTask?.cancel()
    }
}

// MARK: - HabitType helper

extension HabitType {
    var displayName: String {
        switch self {
        case .boolean: return String.loc("habits_type_boolean")
        case .numeric: return String.loc("habits_type_numeric")
        }
    }
}
