import SwiftUI
import MudawamaCore

// MARK: - AthkarGroupView
// Tap-to-count reading session for a single Athkar group.
// Counts are driven by the live DB flow via AthkarViewModel — no local state.

struct AthkarGroupView: View {
    let groupType: AthkarGroupType
    let group: AthkarGroup
    let date: String
    @ObservedObject var vm: AthkarViewModel
    @EnvironmentObject private var appSettings: AppSettingsViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: MudawamaTheme.Spacing.md) {
                    ForEach(group.items, id: \.id) { item in
                        itemCard(item: item)
                    }
                }
                .padding(MudawamaTheme.Spacing.md)
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle(groupTitle)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(String.loc("common_done")) { dismiss() }
                        .tint(MudawamaTheme.Colors.primary)
                }
            }
        }
    }

    // MARK: - Item Card

    @ViewBuilder
    private func itemCard(item: AthkarItem) -> some View {
        let tapped    = vm.count(for: item.id, groupType: groupType)
        let target    = Int(item.targetCount)
        let remaining = max(target - tapped, 0)
        let isDone    = remaining == 0

        MudawamaSurfaceCard(action: isDone ? nil : {
            vm.increment(groupType: groupType, date: date, itemId: item.id)
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
        }) {
            HStack(spacing: MudawamaTheme.Spacing.md) {
                // Completion indicator
                ZStack {
                    Circle()
                        .fill(isDone
                              ? MudawamaTheme.Colors.done
                              : MudawamaTheme.Colors.primary.opacity(0.12))
                        .frame(width: 44, height: 44)
                    if isDone {
                        Image(systemName: "checkmark")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(MudawamaTheme.Colors.onPrimary)
                    } else {
                        Text("\(remaining)")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(MudawamaTheme.Colors.primary)
                    }
                }

                VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.xs) {
                    Text(String.loc(item.id))
                        .font(.body)
                        .foregroundStyle(isDone ? Color.secondary : Color.primary)
                        .lineLimit(3)
                    Text(
                        isDone
                        ? String.loc("athkar_item_done")
                        : String(format: String.loc("athkar_item_count_format"), remaining, target)
                    )
                    .font(.caption)
                    .foregroundStyle(isDone ? MudawamaTheme.Colors.done : Color.secondary)
                }

                Spacer()

                if !isDone {
                    Image(systemName: "hand.tap.fill")
                        .foregroundStyle(MudawamaTheme.Colors.primary.opacity(0.5))
                        .font(.title3)
                }
            }
        }
        .opacity(isDone ? 0.6 : 1.0)
        .animation(.easeInOut(duration: 0.2), value: isDone)
    }

    // MARK: - Helpers

    private var groupTitle: String {
        switch groupType {
        case .morning:    return String.loc("athkar_morning_tab")
        case .evening:    return String.loc("athkar_evening_tab")
        case .postPrayer: return String.loc("athkar_post_prayer_tab")
        }
    }
}

#Preview {
    Text("AthkarGroupView Preview")
}
