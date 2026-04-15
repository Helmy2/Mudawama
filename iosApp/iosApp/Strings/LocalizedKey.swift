import Foundation

// MARK: - CurrentBundle
// Holds the active language bundle so that String.loc() always returns strings
// in the language chosen inside the app (AppLanguage), regardless of the iOS
// system locale.  AppSettingsViewModel updates this whenever AppLanguage changes.

final class CurrentBundle {
    static let shared = CurrentBundle()
    private init() {}

    private(set) var bundle: Bundle = .main

    /// Call this whenever the in-app language changes.
    func setLanguage(_ languageCode: String) {
        // Look for a .lproj folder in the main bundle for the requested language.
        // Falls back to .main if the language is not found.
        if let path = Bundle.main.path(forResource: languageCode, ofType: "lproj"),
           let b = Bundle(path: path) {
            bundle = b
        } else {
            bundle = .main
        }
    }
}

// MARK: - String.loc

extension String {
    /// Returns the localized string for the given key using the app's current
    /// language bundle (set by AppSettingsViewModel via CurrentBundle).
    /// Falls back to the key name itself so missing keys are easy to spot.
    static func loc(_ key: String) -> String {
        NSLocalizedString(key, bundle: CurrentBundle.shared.bundle, comment: "")
    }
}
