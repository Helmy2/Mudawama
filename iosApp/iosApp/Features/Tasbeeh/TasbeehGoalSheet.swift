import SwiftUI

struct TasbeehGoalSheet: View {
    @Environment(\.dismiss) private var dismiss
    let currentGoal: Int
    let onSave: (Int) -> Void
    @State private var goalText: String

    init(currentGoal: Int, onSave: @escaping (Int) -> Void) {
        self.currentGoal = currentGoal
        self.onSave = onSave
        _goalText = State(initialValue: "\(currentGoal)")
    }

    var body: some View {
        NavigationStack {
            Form {
                Section(String.loc("tasbeeh_daily_total_label")) {
                    TextField("100", text: $goalText)
                        .keyboardType(.numberPad)
                }
            }
            .navigationTitle(String.loc("tasbeeh_goal_sheet_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(String.loc("common_cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(String.loc("common_save")) {
                        if let count = Int(goalText), count > 0 {
                            onSave(count)
                        }
                        dismiss()
                    }
                    .disabled(Int(goalText) == nil || (Int(goalText) ?? 0) < 1)
                }
            }
        }
        .presentationDetents([.medium])
    }
}
