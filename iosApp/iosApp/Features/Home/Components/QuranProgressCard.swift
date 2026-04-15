import SwiftUI
import MudawamaCore

struct QuranProgressCard: View {
    let quranState: QuranScreenState?
    @EnvironmentObject private var appSettings: AppSettingsViewModel

    var body: some View {
        MudawamaSurfaceCard {
            VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.sm) {
                HStack(spacing: MudawamaTheme.Spacing.sm) {
                    Image(systemName: "book.fill")
                        .font(.subheadline)
                        .foregroundStyle(MudawamaTheme.Colors.primary)
                    Text(String.loc("home_quran_label"))
                        .font(.subheadline.bold())
                }

                Spacer()

                if let state = quranState {
                    let progress: CGFloat = state.goalPages > 0
                        ? min(CGFloat(state.pagesReadToday) / CGFloat(state.goalPages), 1.0)
                        : 0
                    HStack(spacing: MudawamaTheme.Spacing.md) {
                        ZStack {
                            Circle()
                                .stroke(Color(.systemFill), lineWidth: 6)
                            Circle()
                                .trim(from: 0, to: progress)
                                .stroke(MudawamaTheme.Colors.primary,
                                        style: StrokeStyle(lineWidth: 6, lineCap: .round))
                                .rotationEffect(.degrees(-90))
                            VStack(spacing: 0) {
                                Text("\(state.pagesReadToday)")
                                    .font(.caption.bold())
                                Text(String.loc("quran_pages_label"))
                                    .font(.caption2)
                                    .foregroundStyle(Color.secondary)
                            }
                        }
                        .frame(width: 52, height: 52)

                        VStack(alignment: .leading, spacing: 2) {
                            Text("/ \(state.goalPages)")
                                .font(.caption)
                                .foregroundStyle(Color.secondary)
                            let streak = state.recentLogs.filter { $0.pagesRead >= $0.goalPages }.count
                            if streak > 0 {
                                Text("\(streak) \(String.loc("quran_streak_label"))")
                                    .font(.caption.bold())
                                    .foregroundStyle(MudawamaTheme.Colors.primary)
                            }
                        }
                    }
                } else {
                    Text("—")
                        .font(.title3.bold())
                        .foregroundStyle(Color.secondary)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 100, alignment: .leading)
        }
    }
}
