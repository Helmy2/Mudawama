import MudawamaCore

// MARK: - ErrorKeys
// Maps Kotlin DomainError subtypes to Localizable.xcstrings keys.

enum ErrorKeys {
    static func key(for error: any DomainError) -> String {
        switch error {
        case is LocationError:       return "error_location"
        case is PrayerError:         return "error_prayer"
        case is QuranError:          return "error_quran"
        case is HabitError:          return "error_habit"
        case is AthkarError:         return "error_athkar"
        default:                     return "common_error_generic"
        }
    }
}
