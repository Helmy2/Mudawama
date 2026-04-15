import SwiftUI
import MudawamaCore

struct NewHabitSheet: View {
    @Environment(\.dismiss) private var dismiss
    let onCreate: (String, HabitType, Int?) -> Void

    @State private var name = ""
    @State private var selectedType: HabitType = .boolean
    @State private var goalText = ""

    var body: some View {
        NavigationStack {
            Form {
                Section(String.loc("habits_name_label")) {
                    TextField(String.loc("habits_name_label"), text: $name)
                }

                Section(String.loc("habits_type_label")) {
                    Picker(String.loc("habits_type_label"), selection: $selectedType) {
                        Text(String.loc("habits_type_boolean")).tag(HabitType.boolean)
                        Text(String.loc("habits_type_numeric")).tag(HabitType.numeric)
                    }
                    .pickerStyle(.segmented)
                }

                if selectedType == .numeric {
                    Section(String.loc("habits_target_label")) {
                        TextField("10", text: $goalText)
                            .keyboardType(.numberPad)
                    }
                }
            }
            .navigationTitle(String.loc("habits_new_sheet_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(String.loc("common_cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(String.loc("common_save")) {
                        let goal: Int? = selectedType == .numeric ? Int(goalText) : nil
                        onCreate(name, selectedType, goal)
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}
