import SwiftUI
import MudawamaCore

struct ManageHabitSheet: View {
    @Environment(\.dismiss) private var dismiss
    let habit: Habit
    let onDelete: () -> Void

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text(habit.name).font(.headline)
                    Text(habit.type.displayName).foregroundStyle(Color.secondary)
                }

                if !habit.isCore {
                    Section {
                        Button(role: .destructive) {
                            onDelete()
                            dismiss()
                        } label: {
                            Label(String.loc("habits_delete_label"), systemImage: "trash")
                        }
                    }
                }
            }
            .navigationTitle(String.loc("habits_manage_sheet_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(String.loc("common_cancel")) { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
