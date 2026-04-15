import SwiftUI
import MudawamaCore

// MARK: - Athkar ViewModel

@MainActor
class AthkarViewModel: ObservableObject {

    @Published var selectedType: AthkarGroupType = .morning
    @Published var completionMap: [AthkarGroupType: Bool] = [:]
    /// Live counters from DB: groupType → (itemId → tappedCount)
    @Published var counters: [AthkarGroupType: [String: Int]] = [:]
    @Published var isLoading = true

    private let provider = AthkarUseCaseProvider()
    private var completionTask: Task<Void, Never>? = nil
    private var logTasks: [AthkarGroupType: Task<Void, Never>] = [:]

    func observe(date: String) {
        completionTask?.cancel()
        logTasks.values.forEach { $0.cancel() }
        logTasks = [:]
        isLoading = true

        // Observe group-level completion map
        completionTask = Task {
            for await map in provider.observeAthkarCompletionUseCase.invoke(date: date) {
                if Task.isCancelled { return }
                self.completionMap = map as? [AthkarGroupType: Bool] ?? [:]
                self.isLoading = false
            }
        }

        // Observe per-item counters for each group type
        for type in [AthkarGroupType.morning, AthkarGroupType.evening, AthkarGroupType.postPrayer] {
            let task = Task {
                for await log in provider.observeAthkarLogUseCase.invoke(groupType: type, date: date) {
                    if Task.isCancelled { return }
                    // log is nullable — if nil, counters are all zero
                    let rawCounters = (log?.counters as? [String: Int]) ?? [:]
                    self.counters[type] = rawCounters
                }
            }
            logTasks[type] = task
        }
    }

    func athkarGroup(for type: AthkarGroupType) -> AthkarGroup {
        return provider.getAthkarGroupUseCase.invoke(type: type)
    }

    /// Returns the current tap count for an item from the live DB counters.
    func count(for itemId: String, groupType: AthkarGroupType) -> Int {
        return counters[groupType]?[itemId] ?? 0
    }

    func increment(groupType: AthkarGroupType, date: String, itemId: String) {
        Task {
            _ = try? await provider.incrementAthkarItemUseCase.invoke(
                groupType: groupType,
                date: date,
                itemId: itemId,
                prayerSlot: nil
            )
        }
    }

    deinit {
        completionTask?.cancel()
        logTasks.values.forEach { $0.cancel() }
    }
}

// MARK: - AthkarGroupType helpers

extension AthkarGroupType {
    var locKey: String {
        switch self {
        case .morning:    return "athkar_morning_tab"
        case .evening:    return "athkar_evening_tab"
        case .postPrayer: return "athkar_post_prayer_tab"
        }
    }
}
