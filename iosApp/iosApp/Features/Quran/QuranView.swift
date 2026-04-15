import SwiftUI
import MudawamaCore

struct QuranView: View {
    @StateObject private var vm = QuranViewModel()
    @State private var selectedDate = Date()

    private var kotlinDate: Kotlinx_datetimeLocalDate {
        let cal = Calendar.current
        let c = cal.dateComponents([.year, .month, .day], from: selectedDate)
        return Kotlinx_datetimeLocalDate(
            year: Int32(c.year ?? 2024),
            month: Int32(c.month ?? 1),
            day: Int32(c.day ?? 1)
        )
    }

    var body: some View {
        VStack(spacing: 0) {
            dateStrip

            ScrollView {
                VStack(spacing: MudawamaTheme.Spacing.md) {
                    if vm.isLoading {
                        ProgressView().tint(MudawamaTheme.Colors.primary).padding(.top, 40)
                    } else if let errorMsg = vm.errorMessage {
                        errorView(message: errorMsg)
                    } else if let state = vm.state {
                        progressCard(state)
                        let isPast = !Calendar.current.isDateInToday(selectedDate)
                        if !isPast { logReadingButton }
                        if !state.recentLogs.isEmpty {
                            recentLogsSection(state.recentLogs)
                        }
                    } else {
                        // No data yet — prompt to log first reading
                        VStack(spacing: MudawamaTheme.Spacing.lg) {
                            Image(systemName: "book.closed")
                                .font(.system(size: 48))
                                .foregroundStyle(Color.secondary)
                            Text(String.loc("quran_log_reading_button"))
                                .foregroundStyle(Color.secondary)
                            logReadingButton
                        }
                        .padding(.top, MudawamaTheme.Spacing.xl)
                    }
                }
                .padding(MudawamaTheme.Spacing.md)
            }
            .refreshable { vm.observe(date: kotlinDate) }
        }
        .navigationTitle(String.loc("nav_quran"))
        .background(Color(.systemGroupedBackground))
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button(String.loc("quran_goal_sheet_title")) { vm.showGoalSheet = true }
                    Button(String.loc("quran_position_sheet_title")) { vm.showPositionSheet = true }
                } label: {
                    Image(systemName: "ellipsis.circle")
                }
            }
        }
        .sheet(isPresented: $vm.showLogSheet) {
            LogReadingSheet(date: kotlinDate, vm: vm)
        }
        .sheet(isPresented: $vm.showGoalSheet) {
            QuranGoalSheet(vm: vm)
        }
        .sheet(isPresented: $vm.showPositionSheet) {
            QuranPositionSheet(vm: vm)
        }
        .task(id: kotlinDate.description()) {
            vm.observe(date: kotlinDate)
        }
    }

    // MARK: - Date strip

    private var dateStrip: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: MudawamaTheme.Spacing.sm) {
                ForEach(-3...3, id: \.self) { offset in
                    let chipDate = Calendar.current.date(byAdding: .day, value: offset, to: Date()) ?? Date()
                    let isSelected = Calendar.current.isDate(chipDate, inSameDayAs: selectedDate)
                    let formatter: DateFormatter = {
                        let f = DateFormatter()
                        f.dateFormat = "d"
                        return f
                    }()
                    Button(action: { selectedDate = chipDate }) {
                        Text(formatter.string(from: chipDate))
                            .font(.caption.bold())
                            .padding(.horizontal, MudawamaTheme.Spacing.md)
                            .padding(.vertical, MudawamaTheme.Spacing.sm)
                            .background(isSelected ? MudawamaTheme.Colors.primary : Color(.secondarySystemGroupedBackground))
                            .foregroundStyle(isSelected ? Color.white : Color.primary)
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, MudawamaTheme.Spacing.md)
            .padding(.vertical, MudawamaTheme.Spacing.sm)
        }
    }

    // MARK: - Progress card

    private func progressCard(_ state: QuranScreenState) -> some View {
        MudawamaSurfaceCard {
            HStack(spacing: MudawamaTheme.Spacing.lg) {
                ZStack {
                    Circle()
                        .stroke(Color(.systemFill), lineWidth: 8)
                    let progress = state.goalPages > 0
                        ? min(CGFloat(state.pagesReadToday) / CGFloat(state.goalPages), 1.0)
                        : 0
                    Circle()
                        .trim(from: 0, to: progress)
                        .stroke(MudawamaTheme.Colors.primary,
                                style: StrokeStyle(lineWidth: 8, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                    VStack(spacing: 2) {
                        Text("\(state.pagesReadToday)")
                            .font(.title3.bold())
                        Text(String.loc("quran_pages_label"))
                            .font(.caption2)
                            .foregroundStyle(Color.secondary)
                    }
                }
                .frame(width: 80, height: 80)

                VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.sm) {
                    Text("\(state.goalPages) \(String.loc("quran_pages_label"))")
                        .font(.caption)
                        .foregroundStyle(Color.secondary)

                    if let bookmark = state.bookmark {
                        Text(String.loc("quran_bookmark_label") + ": \(bookmark.surahName) \(bookmark.ayah)")
                            .font(.caption)
                            .foregroundStyle(Color.secondary)
                    }
                }
                Spacer()
            }
        }
    }

    private var logReadingButton: some View {
        Button(action: { vm.showLogSheet = true }) {
            Label(String.loc("quran_log_reading_button"), systemImage: "plus.circle.fill")
                .frame(maxWidth: .infinity)
                .padding(MudawamaTheme.Spacing.md)
                .background(MudawamaTheme.Colors.primary)
                .foregroundStyle(.white)
                .clipShape(RoundedRectangle(cornerRadius: MudawamaTheme.Radius.button, style: .continuous))
        }
    }

    private func errorView(message: String) -> some View {
        VStack(spacing: MudawamaTheme.Spacing.lg) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundStyle(MudawamaTheme.Colors.missed)
            Text(message)
                .foregroundStyle(Color.secondary)
                .multilineTextAlignment(.center)
            Button(String.loc("common_retry")) {
                vm.observe(date: kotlinDate)
            }
            .buttonStyle(.borderedProminent)
            .tint(MudawamaTheme.Colors.primary)
            Spacer()
        }
        .padding()
    }

    private func recentLogsSection(_ logs: [QuranScreenState.RecentLogEntry]) -> some View {
        VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.sm) {
            Text(String.loc("quran_pages_read_label"))
                .font(.caption.bold())
                .foregroundStyle(Color.secondary)
                .padding(.horizontal, MudawamaTheme.Spacing.xs)
            ForEach(logs, id: \.date) { entry in
                MudawamaSurfaceCard {
                    HStack {
                        Text(entry.date).font(.caption).foregroundStyle(Color.secondary)
                        Spacer()
                        Text("\(entry.pagesRead)").font(.body.bold())
                    }
                }
            }
        }
    }
}

#Preview {
    NavigationStack { QuranView() }
}
