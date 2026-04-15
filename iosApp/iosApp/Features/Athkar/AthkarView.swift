import SwiftUI
import MudawamaCore

struct AthkarView: View {
    @StateObject private var vm = AthkarViewModel()
    @EnvironmentObject private var appSettings: AppSettingsViewModel
    @State private var todayDateString: String = {
        let f = DateFormatter(); f.dateFormat = "yyyy-MM-dd"; return f.string(from: Date())
    }()
    @State private var showNotificationSheet = false

    var body: some View {
        VStack(spacing: 0) {
            Picker(String.loc("athkar_type_picker"), selection: $vm.selectedType) {
                Text(String.loc("athkar_morning_tab")).tag(AthkarGroupType.morning)
                Text(String.loc("athkar_evening_tab")).tag(AthkarGroupType.evening)
                Text(String.loc("athkar_post_prayer_tab")).tag(AthkarGroupType.postPrayer)
            }
            .pickerStyle(.segmented)
            .padding(MudawamaTheme.Spacing.md)

            if vm.isLoading {
                ProgressView().tint(MudawamaTheme.Colors.primary).padding(.top, 40)
                Spacer()
            } else {
                let group = vm.athkarGroup(for: vm.selectedType)
                let isCompleted = vm.completionMap[vm.selectedType] ?? false

                ScrollView {
                    VStack(spacing: MudawamaTheme.Spacing.md) {
                        if isCompleted {
                            Label(String.loc(vm.selectedType.locKey), systemImage: "checkmark.seal.fill")
                                .font(.subheadline.bold())
                                .foregroundStyle(MudawamaTheme.Colors.done)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, MudawamaTheme.Spacing.xs)
                        }

                        ForEach(group.items, id: \.id) { item in
                            itemCard(item: item, groupType: vm.selectedType)
                        }
                    }
                    .padding(MudawamaTheme.Spacing.md)
                }
                .background(Color(.systemGroupedBackground))
            }
        }
        .navigationTitle(String.loc("nav_athkar"))
        .background(Color(.systemGroupedBackground))
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    showNotificationSheet = true
                } label: {
                    Image(systemName: "bell.fill")
                        .foregroundStyle(MudawamaTheme.Colors.primary)
                }
            }
        }
        .sheet(isPresented: $showNotificationSheet) {
            AthkarNotificationSheet()
        }
        .task {
            vm.observe(date: todayDateString)
        }
    }

    // MARK: - Item card (inline — matches AthkarGroupView.itemCard)

    @ViewBuilder
    private func itemCard(item: AthkarItem, groupType: AthkarGroupType) -> some View {
        let tapped    = vm.count(for: item.id, groupType: groupType)
        let target    = Int(item.targetCount)
        let remaining = max(target - tapped, 0)
        let isDone    = remaining == 0

        MudawamaSurfaceCard(action: isDone ? nil : {
            vm.increment(groupType: groupType, date: todayDateString, itemId: item.id)
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
        }) {
            HStack(spacing: MudawamaTheme.Spacing.md) {
                // Completion indicator circle
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
}

#Preview {
    NavigationStack { AthkarView() }
}
