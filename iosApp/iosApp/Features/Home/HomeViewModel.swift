import SwiftUI
import MudawamaCore

// MARK: - Home UI State

struct HomeUiState {
    var prayers: [PrayerWithStatus] = []
    var athkarCompletion: [AthkarGroupType: Bool] = [:]
    var quranState: QuranScreenState? = nil
    var tasbeehGoal: TasbeehGoal? = nil
    var tasbeehTotal: TasbeehDailyTotal? = nil
    var habits: [HabitWithStatus] = []
    var isLoading = true
    var errorMessage: String? = nil
}

// MARK: - Home ViewModel

@MainActor
class HomeViewModel: ObservableObject {

    @Published var state = HomeUiState()

    private let provider = HomeUseCaseProvider()
    private let locationProvider = IosLocationProvider()

    private var prayerTask: Task<Void, Never>? = nil
    private var athkarTask: Task<Void, Never>? = nil
    private var quranTask: Task<Void, Never>? = nil
    private var tasbeehGoalTask: Task<Void, Never>? = nil
    private var tasbeehTotalTask: Task<Void, Never>? = nil
    private var habitsTask: Task<Void, Never>? = nil

    private var today: Kotlinx_datetimeLocalDate {
        let c = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        return Kotlinx_datetimeLocalDate(
            year: Int32(c.year ?? 2024),
            month: Int32(c.month ?? 1),
            day: Int32(c.day ?? 1)
        )
    }

    private var todayString: String {
        let f = DateFormatter(); f.dateFormat = "yyyy-MM-dd"; return f.string(from: Date())
    }

    func observe() {
        // Seed prayer habits on first run
        Task { _ = try? await provider.seedPrayerHabitsUseCase.invoke() }

        startPrayerObserver()
        startAthkarObserver()
        startQuranObserver()
        startTasbeehObserver()
        startHabitsObserver()
    }

    private func startPrayerObserver() {
        prayerTask?.cancel()
        prayerTask = Task {
            let locationResult = try? await locationProvider.getCurrentLocation()
            let coords: Coordinates
            if let r = locationResult as? ResultSuccess<AnyObject>, let c = r.data as? Coordinates {
                coords = c
            } else {
                coords = Coordinates(latitude: 21.4225, longitude: 39.8262)
            }
            for await result in provider.observePrayersForDateUseCase.invoke(
                date: today,
                coordinates: coords,
                method: CalculationMethod.muslimWorldLeague
            ) {
                if Task.isCancelled { return }
                if let success = result as? ResultSuccess<AnyObject>,
                   let list = success.data as? [PrayerWithStatus] {
                    self.state.prayers = list
                    self.state.isLoading = false
                }
            }
        }
    }

    private func startAthkarObserver() {
        athkarTask?.cancel()
        athkarTask = Task {
            for await map in provider.observeAthkarCompletionUseCase.invoke(date: todayString) {
                if Task.isCancelled { return }
                self.state.athkarCompletion = map as? [AthkarGroupType: Bool] ?? [:]
                self.state.isLoading = false
            }
        }
    }

    private func startQuranObserver() {
        quranTask?.cancel()
        quranTask = Task {
            for await s in provider.observeQuranStateUseCase.invoke(date: today) {
                if Task.isCancelled { return }
                self.state.quranState = s
                self.state.isLoading = false
            }
        }
    }

    private func startTasbeehObserver() {
        tasbeehGoalTask?.cancel()
        tasbeehTotalTask?.cancel()

        tasbeehGoalTask = Task {
            for await g in provider.observeTasbeehGoalUseCase.invoke() {
                if Task.isCancelled { return }
                self.state.tasbeehGoal = g
            }
        }

        tasbeehTotalTask = Task {
            for await t in provider.observeTasbeehDailyTotalUseCase.invoke(date: todayString) {
                if Task.isCancelled { return }
                self.state.tasbeehTotal = t
            }
        }
    }

    private func startHabitsObserver() {
        habitsTask?.cancel()
        habitsTask = Task {
            for await list in provider.observeHabitsWithTodayStatusUseCase.invoke() {
                if Task.isCancelled { return }
                self.state.habits = list
                self.state.isLoading = false
            }
        }
    }

    deinit {
        prayerTask?.cancel()
        athkarTask?.cancel()
        quranTask?.cancel()
        tasbeehGoalTask?.cancel()
        tasbeehTotalTask?.cancel()
        habitsTask?.cancel()
    }
}
