import SwiftUI
import MudawamaCore

struct TasbeehView: View {
    @StateObject private var vm = TasbeehViewModel()
    @State private var showGoalSheet = false

    private var todayDateString: String {
        let f = DateFormatter(); f.dateFormat = "yyyy-MM-dd"; return f.string(from: Date())
    }

    private var totalCount: Int {
        Int(vm.dailyTotal?.totalCount ?? 0)
    }

    private var goalCount: Int {
        Int(vm.goal?.goalCount ?? 100)
    }

    private var progress: CGFloat {
        guard goalCount > 0 else { return 0 }
        return min(CGFloat(totalCount) / CGFloat(goalCount), 1.0)
    }

    var body: some View {
        Group {
            if vm.isLoading {
                VStack(spacing: MudawamaTheme.Spacing.md) {
                    Spacer()
                    ProgressView().tint(MudawamaTheme.Colors.primary)
                    Text(String.loc("common_loading"))
                        .font(.caption)
                        .foregroundStyle(Color.secondary)
                    Spacer()
                }
                .frame(maxWidth: .infinity)
            } else {
                mainContent
            }
        }
        .navigationTitle(String.loc("nav_tasbeeh"))
        .background(Color(.systemGroupedBackground))
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: { showGoalSheet = true }) {
                    Image(systemName: "target")
                }
            }
        }
        .sheet(isPresented: $showGoalSheet) {
            TasbeehGoalSheet(currentGoal: goalCount) { newGoal in
                vm.setGoal(count: newGoal)
            }
        }
        .task {
            vm.observe(date: todayDateString)
        }
    }

    private var mainContent: some View {
        VStack(spacing: MudawamaTheme.Spacing.xl) {
            Spacer()

            ZStack {
                Circle()
                    .stroke(Color(.systemFill), lineWidth: 12)
                Circle()
                    .trim(from: 0, to: progress)
                    .stroke(MudawamaTheme.Colors.primary,
                            style: StrokeStyle(lineWidth: 12, lineCap: .round))
                    .rotationEffect(.degrees(-90))
                    .animation(.easeOut(duration: 0.3), value: progress)
                VStack(spacing: MudawamaTheme.Spacing.xs) {
                    Text("\(totalCount)")
                        .font(.system(size: 64, weight: .bold, design: .rounded))
                    Text("/ \(goalCount)")
                        .font(.subheadline)
                        .foregroundStyle(Color.secondary)
                }
            }
            .frame(width: 220, height: 220)

            Button(action: { vm.increment(date: todayDateString) }) {
                Circle()
                    .fill(MudawamaTheme.Colors.primary)
                    .frame(width: 80, height: 80)
                    .overlay(
                        Image(systemName: "hand.tap.fill")
                            .font(.title)
                            .foregroundStyle(.white)
                    )
            }
            .buttonStyle(.plain)

            Text(String.loc("tasbeeh_tap_hint"))
                .font(.caption)
                .foregroundStyle(Color.secondary)

            Spacer()
        }
    }
}

#Preview {
    NavigationStack { TasbeehView() }
}
