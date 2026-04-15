import SwiftUI
import MudawamaCore

struct TasbeehSummaryCard: View {
    let goal: TasbeehGoal?
    let total: TasbeehDailyTotal?
    @EnvironmentObject private var appSettings: AppSettingsViewModel

    private var totalCount: Int { Int(total?.totalCount ?? 0) }
    private var goalCount: Int  { Int(goal?.goalCount ?? 100) }

    private var progress: CGFloat {
        goalCount > 0 ? min(CGFloat(totalCount) / CGFloat(goalCount), 1.0) : 0
    }

    var body: some View {
        MudawamaSurfaceCard {
            VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.sm) {
                HStack(spacing: MudawamaTheme.Spacing.sm) {
                    Image(systemName: "circle.dotted")
                        .font(.subheadline)
                        .foregroundStyle(MudawamaTheme.Colors.primary)
                    Text(String.loc("home_tasbeeh_label"))
                        .font(.subheadline.bold())
                }

                Spacer()

                HStack(alignment: .bottom) {
                    Text("\(totalCount)")
                        .font(.title2.bold())
                        .foregroundStyle(MudawamaTheme.Colors.primary)
                        .monospacedDigit()
                    Text("/ \(goalCount)")
                        .font(.caption)
                        .foregroundStyle(Color.secondary)
                        .padding(.bottom, 3)
                }

                // Mini progress bar
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule().fill(Color(.systemFill))
                        Capsule()
                            .fill(MudawamaTheme.Colors.primary)
                            .frame(width: geo.size.width * progress)
                    }
                }
                .frame(height: 4)
            }
            .frame(maxWidth: .infinity, minHeight: 100, alignment: .leading)
        }
    }
}
