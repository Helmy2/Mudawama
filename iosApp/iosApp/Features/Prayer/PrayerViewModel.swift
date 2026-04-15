import SwiftUI
import MudawamaCore

// MARK: - Prayer ViewModel

@MainActor
class PrayerViewModel: ObservableObject {

    // MARK: Published State

    @Published var prayers: [PrayerWithStatus] = []
    @Published var isLoading = true
    @Published var errorMessage: String? = nil

    // MARK: Private

    private let provider = PrayerUseCaseProvider()
    private let locationProvider = IosLocationProvider()
    private var observeTask: Task<Void, Never>? = nil

    // MARK: Lifecycle

    func observe(date: Kotlinx_datetimeLocalDate) {
        observeTask?.cancel()
        isLoading = true
        errorMessage = nil
        observeTask = Task {
            // Seed prayer habits on first run
            _ = try? await provider.seedPrayerHabitsUseCase.invoke()

            // Fetch current location for prayer times
            let locationResult = try? await locationProvider.getCurrentLocation()
            let coords: Coordinates?
            if let result = locationResult as? ResultSuccess<AnyObject>,
               let c = result.data as? Coordinates {
                coords = c
            } else {
                coords = nil
            }

            let useCoords = coords ?? Coordinates(latitude: 21.4225, longitude: 39.8262)

            for await result in provider.observePrayersForDateUseCase.invoke(
                    date: date,
                    coordinates: useCoords,
                    method: CalculationMethod.muslimWorldLeague
                ) {
                    if Task.isCancelled { return }
                    if let success = result as? ResultSuccess<AnyObject>,
                       let list = success.data as? [PrayerWithStatus] {
                        self.prayers = list
                        self.isLoading = false
                    } else if result is ResultFailure<AnyObject> {
                        self.errorMessage = String.loc("common_error_generic")
                        self.isLoading = false
                    }
                }
        }
    }

    func toggleStatus(habitId: String, date: Kotlinx_datetimeLocalDate) {
        Task {
            _ = try? await provider.togglePrayerStatusUseCase.invoke(
                prayerHabitId: habitId,
                date: date
            )
        }
    }

    deinit {
        observeTask?.cancel()
    }
}

// MARK: - LogStatus SF Symbol helper

extension LogStatus {
    var symbolName: String {
        switch self {
        case .completed: return "checkmark.circle.fill"
        case .missed:    return "xmark.circle.fill"
        case .pending:   return "circle"
        }
    }

    var symbolColor: Color {
        switch self {
        case .completed: return MudawamaTheme.Colors.done
        case .missed:    return MudawamaTheme.Colors.missed
        case .pending:   return Color.secondary
        }
    }
}

// MARK: - PrayerName localisation helper

extension PrayerName {
    var localisedKey: String {
        switch self {
        case .fajr:    return "prayer_fajr"
        case .dhuhr:   return "prayer_dhuhr"
        case .asr:     return "prayer_asr"
        case .maghrib: return "prayer_maghrib"
        case .isha:    return "prayer_isha"
        }
    }
}
