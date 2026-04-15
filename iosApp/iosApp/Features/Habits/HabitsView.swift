import SwiftUI
import MudawamaCore

struct HabitsView: View {
    @StateObject private var vm = HabitsViewModel()
    @State private var showNewHabitSheet = false

    var body: some View {
        Group {
            if vm.isLoading {
                ProgressView()
            } else if vm.habits.isEmpty {
                emptyState
            } else {
                List {
                    ForEach(vm.habits, id: \.habit.id) { habitStatus in
                        habitRow(habitStatus)
                    }
                    .onDelete { indexSet in
                        for index in indexSet {
                            let id = vm.habits[index].habit.id
                            vm.delete(habitId: id)
                        }
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
        .navigationTitle(String.loc("nav_habits"))
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: { showNewHabitSheet = true }) {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showNewHabitSheet) {
            NewHabitSheet { name, type, goal in
                vm.create(name: name, type: type, goalCount: goal)
            }
        }
        .task {
            vm.observe()
        }
    }

    private func habitRow(_ habitStatus: HabitWithStatus) -> some View {
        let habit = habitStatus.habit
        let isCompleted = habitStatus.todayLog?.status == .completed

        return HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(habit.name)
                    .font(.body.bold())
                if let goal = habit.goalCount {
                    Text("\(habitStatus.todayLog?.completedCount ?? 0) / \(goal)")
                        .font(.caption)
                        .foregroundStyle(Color.secondary)
                }
            }
            Spacer()
            if habit.type == .boolean {
                Image(systemName: isCompleted ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(isCompleted ? MudawamaTheme.Colors.done : Color.secondary)
                    .font(.title2)
                    .contentShape(Rectangle())
                    .onTapGesture { vm.toggle(habitId: habit.id) }
            } else {
                Toggle("", isOn: Binding(
                    get: { isCompleted },
                    set: { _ in vm.toggle(habitId: habit.id) }
                ))
                .labelsHidden()
            }
        }
        .padding(.vertical, MudawamaTheme.Spacing.xs)
    }

    private var emptyState: some View {
        VStack(spacing: MudawamaTheme.Spacing.lg) {
            Spacer()
            Image(systemName: "checklist").font(.system(size: 48)).foregroundStyle(Color.secondary)
            Text(String.loc("habits_empty_prompt")).foregroundStyle(Color.secondary)
            Button(action: { showNewHabitSheet = true }) {
                Label(String.loc("habits_new_sheet_title"), systemImage: "plus")
            }
            .buttonStyle(.borderedProminent)
            Spacer()
        }
    }
}

#Preview {
    NavigationStack { HabitsView() }
}
