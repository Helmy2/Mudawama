import SwiftUI
import MudawamaCore

struct HomeView: View {
    @StateObject private var vm = HomeViewModel()
    @EnvironmentObject private var appSettings: AppSettingsViewModel

    // Navigation callbacks (set by RootNavigationView)
    var onGoToPrayer: (() -> Void)? = nil
    var onGoToAthkar: (() -> Void)? = nil
    var onGoToQuran: (() -> Void)? = nil
    var onGoToHabits: (() -> Void)? = nil
    var onGoToTasbeeh: (() -> Void)? = nil
    var onGoToSettings: (() -> Void)? = nil
    var onGoToQibla: (() -> Void)? = nil

    var body: some View {
        ScrollView {
            if vm.state.isLoading {
                VStack(spacing: MudawamaTheme.Spacing.lg) {
                    ProgressView()
                        .tint(MudawamaTheme.Colors.primary)
                        .padding(.top, MudawamaTheme.Spacing.xl)
                    Text(String.loc("common_loading"))
                        .font(.caption)
                        .foregroundStyle(Color.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.top, MudawamaTheme.Spacing.xl)
            } else if let errorMsg = vm.state.errorMessage {
                VStack(spacing: MudawamaTheme.Spacing.lg) {
                    Spacer()
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundStyle(MudawamaTheme.Colors.missed)
                    Text(errorMsg)
                        .foregroundStyle(Color.secondary)
                        .multilineTextAlignment(.center)
                    Button(String.loc("common_retry")) { vm.observe() }
                        .buttonStyle(.borderedProminent)
                        .tint(MudawamaTheme.Colors.primary)
                    Spacer()
                }
                .frame(maxWidth: .infinity)
                .padding()
            } else {
                LazyVStack(spacing: MudawamaTheme.Spacing.md) {

                    // MARK: Next Prayer — full width, prominent
                    NextPrayerCard(prayers: vm.state.prayers)
                        .onTapGesture { onGoToPrayer?() }

                    // MARK: Athkar + Quran — equal halves
                    HStack(alignment: .top, spacing: MudawamaTheme.Spacing.md) {
                        AthkarSummaryCard(completionMap: vm.state.athkarCompletion)
                            .onTapGesture { onGoToAthkar?() }
                            .frame(maxWidth: .infinity)

                        QuranProgressCard(quranState: vm.state.quranState)
                            .onTapGesture { onGoToQuran?() }
                            .frame(maxWidth: .infinity)
                    }

                    // MARK: Tasbeeh + Qibla — equal halves
                    HStack(alignment: .top, spacing: MudawamaTheme.Spacing.md) {
                        TasbeehSummaryCard(
                            goal: vm.state.tasbeehGoal,
                            total: vm.state.tasbeehTotal
                        )
                        .onTapGesture { onGoToTasbeeh?() }
                        .frame(maxWidth: .infinity)

                        MudawamaSurfaceCard(action: onGoToQibla) {
                            VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.sm) {
                                Image(systemName: "location.north.fill")
                                    .font(.title2)
                                    .foregroundStyle(MudawamaTheme.Colors.primary)
                                Spacer()
                                Text(String.loc("nav_qibla"))
                                    .font(.subheadline.bold())
                                Text(String.loc("qibla_direction_hint"))
                                    .font(.caption2)
                                    .foregroundStyle(Color.secondary)
                                    .lineLimit(2)
                            }
                            .frame(maxWidth: .infinity, minHeight: 80, alignment: .leading)
                        }
                        .frame(maxWidth: .infinity)
                    }

                    // MARK: Habits — full width
                    HabitsSummarySection(habits: vm.state.habits)
                        .onTapGesture { onGoToHabits?() }
                }
                .padding(MudawamaTheme.Spacing.md)
            }
        }
        .navigationTitle(String.loc("nav_home"))
        .background(Color(.systemGroupedBackground))
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { onGoToSettings?() } label: {
                    Image(systemName: "gearshape.fill")
                        .foregroundStyle(MudawamaTheme.Colors.primary)
                }
            }
        }
        .task { vm.observe() }
    }
}

#Preview {
    NavigationStack { HomeView() }
}
