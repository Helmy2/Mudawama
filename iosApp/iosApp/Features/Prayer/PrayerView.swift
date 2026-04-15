import SwiftUI
import MudawamaCore

struct PrayerView: View {
    @StateObject private var vm = PrayerViewModel()
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

            if vm.isLoading {
                ProgressView().padding(.top, 40)
                Spacer()
            } else if let error = vm.errorMessage {
                errorView(message: error)
            } else if vm.prayers.isEmpty {
                emptyView
            } else {
                List(vm.prayers, id: \.habitId) { prayer in
                    prayerRow(prayer)
                }
                .listStyle(.insetGrouped)
                .refreshable { vm.observe(date: kotlinDate) }
            }
        }
        .navigationTitle(String.loc("nav_prayers"))
        .background(Color(.systemGroupedBackground))
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

    // MARK: - Prayer row

    private func prayerRow(_ prayer: PrayerWithStatus) -> some View {
        let isPast = !Calendar.current.isDateInToday(selectedDate)
        return HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(String.loc(prayer.name.localisedKey))
                    .font(.body.bold())
                Text(prayer.timeString)
                    .font(.caption)
                    .foregroundStyle(Color.secondary)
            }
            Spacer()
            Image(systemName: prayer.status.symbolName)
                .foregroundStyle(prayer.status.symbolColor)
                .font(.title2)
        }
        .padding(.vertical, MudawamaTheme.Spacing.xs)
        .contentShape(Rectangle())
        .onTapGesture {
            guard !isPast else { return }
            vm.toggleStatus(habitId: prayer.habitId, date: kotlinDate)
        }
    }

    // MARK: - States

    private var emptyView: some View {
        VStack(spacing: MudawamaTheme.Spacing.lg) {
            Spacer()
            Image(systemName: "moon.stars")
                .font(.system(size: 48))
                .foregroundStyle(Color.secondary)
            Text(String.loc("prayer_empty_state"))
                .foregroundStyle(Color.secondary)
                .multilineTextAlignment(.center)
            Spacer()
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
            Spacer()
        }
        .padding()
    }
}

#Preview {
    NavigationStack { PrayerView() }
}
