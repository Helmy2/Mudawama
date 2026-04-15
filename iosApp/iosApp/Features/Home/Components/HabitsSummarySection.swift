import SwiftUI
import MudawamaCore

struct HabitsSummarySection: View {
    let habits: [HabitWithStatus]
    @EnvironmentObject private var appSettings: AppSettingsViewModel

    private var completedCount: Int {
        habits.filter { $0.todayLog?.status == .completed }.count
    }

    var body: some View {
        MudawamaSurfaceCard {
            VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.md) {
                HStack {
                    HStack(spacing: MudawamaTheme.Spacing.sm) {
                        Image(systemName: "checklist")
                            .font(.subheadline)
                            .foregroundStyle(MudawamaTheme.Colors.primary)
                        Text(String.loc("home_habits_label"))
                            .font(.subheadline.bold())
                    }
                    Spacer()
                    Text("\(completedCount)/\(habits.count)")
                        .font(.subheadline.bold())
                        .foregroundStyle(
                            completedCount == habits.count && !habits.isEmpty
                                ? MudawamaTheme.Colors.done
                                : MudawamaTheme.Colors.primary
                        )
                }

                if habits.isEmpty {
                    Text(String.loc("habits_empty_prompt"))
                        .font(.subheadline)
                        .foregroundStyle(Color.secondary)
                } else {
                    // Show up to 4 habits with name + status
                    VStack(spacing: MudawamaTheme.Spacing.xs) {
                        ForEach(habits.prefix(4), id: \.habit.id) { hs in
                            let isDone = hs.todayLog?.status == .completed
                            HStack {
                                Image(systemName: isDone ? "checkmark.circle.fill" : "circle")
                                    .font(.callout)
                                    .foregroundStyle(isDone ? MudawamaTheme.Colors.done : Color(.tertiaryLabel))
                                Text(hs.habit.name)
                                    .font(.subheadline)
                                    .foregroundStyle(isDone ? Color.secondary : Color.primary)
                                    .lineLimit(1)
                                Spacer()
                            }
                        }
                        if habits.count > 4 {
                            Text("+\(habits.count - 4) \(String.loc("home_habits_label"))")
                                .font(.caption)
                                .foregroundStyle(Color.secondary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}
