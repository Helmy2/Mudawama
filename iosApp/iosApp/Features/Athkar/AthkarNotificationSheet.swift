import SwiftUI
import MudawamaCore

// MARK: - AthkarNotificationSheet
// Quick-access notification toggles for morning/evening Athkar reminders.
// Mirrors the notification section in SettingsView for convenience from AthkarView toolbar.

struct AthkarNotificationSheet: View {
    @StateObject private var vm = SettingsViewModel()
    @EnvironmentObject private var appSettings: AppSettingsViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section(String.loc("athkar_notification_sheet_title")) {
                    if vm.isLoading {
                        ProgressView()
                    } else {
                        Toggle(
                            String.loc("settings_morning_notif"),
                            isOn: Binding(
                                get: { vm.settings.morningNotificationEnabled },
                                set: { vm.setMorningNotification(enabled: $0) }
                            )
                        )
                        Toggle(
                            String.loc("settings_evening_notif"),
                            isOn: Binding(
                                get: { vm.settings.eveningNotificationEnabled },
                                set: { vm.setEveningNotification(enabled: $0) }
                            )
                        )
                    }
                }
            }
            .navigationTitle(String.loc("athkar_notification_sheet_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(String.loc("common_done")) { dismiss() }
                        .tint(MudawamaTheme.Colors.primary)
                }
            }
            .task { vm.observe() }
        }
    }
}

#Preview {
    AthkarNotificationSheet()
}
