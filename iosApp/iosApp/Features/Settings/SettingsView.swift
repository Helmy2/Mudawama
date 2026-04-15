import SwiftUI
import MudawamaCore

struct SettingsView: View {
    @StateObject private var vm = SettingsViewModel()

    var body: some View {
        Form {
            if vm.isLoading {
                ProgressView()
            } else {
                prayerSection
                appearanceSection
                notificationsSection
            }
        }
        .navigationTitle(String.loc("nav_settings"))
        .task { vm.observe() }
    }

    // MARK: - Prayer section

    private var prayerSection: some View {
        Section(String.loc("settings_prayer_section")) {
            Picker(String.loc("settings_calculation_method"),
                   selection: Binding(
                    get: { vm.settings.calculationMethod },
                    set: { vm.setCalculationMethod($0) }
                   )) {
                ForEach(CalculationMethod.allCases, id: \.self) { method in
                    Text(method.name.replacingOccurrences(of: "_", with: " ").capitalized)
                        .tag(method)
                }
            }

            let isGps = vm.settings.locationMode is LocationMode.Gps
            Toggle(String.loc("settings_location_mode"),
                   isOn: Binding(
                    get: { isGps },
                    set: { useGps in
                        if useGps {
                            vm.setLocationMode(LocationMode.Gps())
                        } else {
                            // Preserve existing manual coords if already set, else default to Mecca
                            let current = vm.settings.locationMode as? LocationMode.Manual
                            vm.setLocationMode(LocationMode.Manual(
                                latitude: current?.latitude ?? 21.3891,
                                longitude: current?.longitude ?? 39.8579
                            ))
                        }
                    }
                   ))
        }
    }

    // MARK: - Appearance section

    private var appearanceSection: some View {
        Section(String.loc("settings_appearance_section")) {
            Picker(String.loc("settings_theme"),
                   selection: Binding(
                    get: { vm.settings.appTheme },
                    set: { vm.setTheme($0) }
                   )) {
                ForEach(AppTheme.allCases, id: \.self) { theme in
                    Text(theme.name.capitalized).tag(theme)
                }
            }

            Picker(String.loc("settings_language"),
                   selection: Binding(
                    get: { vm.settings.appLanguage },
                    set: { vm.setLanguage($0) }
                   )) {
                ForEach(AppLanguage.allCases, id: \.self) { lang in
                    Text(lang.name.capitalized).tag(lang)
                }
            }
        }
    }

    // MARK: - Notifications section

    private var notificationsSection: some View {
        Section(String.loc("settings_notifications_section")) {
            Toggle(String.loc("settings_morning_notif"),
                   isOn: Binding(
                    get: { vm.settings.morningNotificationEnabled },
                    set: { vm.setMorningNotification(enabled: $0) }
                   ))

            Toggle(String.loc("settings_evening_notif"),
                   isOn: Binding(
                    get: { vm.settings.eveningNotificationEnabled },
                    set: { vm.setEveningNotification(enabled: $0) }
                   ))
        }
    }
}

#Preview {
    NavigationStack { SettingsView() }
}
