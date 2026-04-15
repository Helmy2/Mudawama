import SwiftUI
import MudawamaCore

struct AthkarSummaryCard: View {
    let completionMap: [AthkarGroupType: Bool]
    @EnvironmentObject private var appSettings: AppSettingsViewModel

    var body: some View {
        MudawamaSurfaceCard {
            VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.sm) {
                HStack(spacing: MudawamaTheme.Spacing.sm) {
                    Image(systemName: "heart.fill")
                        .font(.subheadline)
                        .foregroundStyle(MudawamaTheme.Colors.primary)
                    Text(String.loc("home_athkar_label"))
                        .font(.subheadline.bold())
                }

                Spacer()

                VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.xs) {
                    dotRow(type: .morning,    labelKey: "athkar_morning_tab",     icon: "sun.max.fill")
                    dotRow(type: .evening,    labelKey: "athkar_evening_tab",     icon: "moon.fill")
                    dotRow(type: .postPrayer, labelKey: "athkar_post_prayer_tab", icon: "hands.sparkles.fill")
                }
            }
            .frame(maxWidth: .infinity, minHeight: 100, alignment: .leading)
        }
    }

    @ViewBuilder
    private func dotRow(type: AthkarGroupType, labelKey: String, icon: String) -> some View {
        let isDone = completionMap[type] ?? false
        HStack(spacing: MudawamaTheme.Spacing.xs) {
            Image(systemName: isDone ? "checkmark.circle.fill" : "circle")
                .font(.caption)
                .foregroundStyle(isDone ? MudawamaTheme.Colors.done : Color(.tertiaryLabel))
            Text(String.loc(labelKey))
                .font(.caption)
                .foregroundStyle(isDone ? Color.primary : Color.secondary)
        }
    }
}
