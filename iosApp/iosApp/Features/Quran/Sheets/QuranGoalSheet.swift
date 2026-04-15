import SwiftUI
import MudawamaCore

struct QuranGoalSheet: View {
    @Environment(\.dismiss) private var dismiss
    let vm: QuranViewModel
    @State private var goalText = ""

    var body: some View {
        NavigationStack {
            Form {
                Section(String.loc("quran_goal_label")) {
                    TextField("10", text: $goalText)
                        .keyboardType(.numberPad)
                }
            }
            .navigationTitle(String.loc("quran_goal_sheet_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(String.loc("common_cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(String.loc("common_save")) {
                        if let pages = Int(goalText), pages > 0 {
                            vm.setGoal(pages: pages)
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
