import SwiftUI
import MudawamaCore

struct QuranPositionSheet: View {
    @Environment(\.dismiss) private var dismiss
    let vm: QuranViewModel
    @State private var surahText = ""
    @State private var ayahText = ""

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("1", text: $surahText)
                        .keyboardType(.numberPad)
                } header: { Text("Surah (1–114)") }
                Section {
                    TextField("1", text: $ayahText)
                        .keyboardType(.numberPad)
                } header: { Text("Ayah") }
            }
            .navigationTitle(String.loc("quran_position_sheet_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(String.loc("common_cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(String.loc("common_save")) {
                        if let surah = Int(surahText), let ayah = Int(ayahText),
                           surah >= 1, surah <= 114, ayah >= 1 {
                            vm.updateBookmark(surah: surah, ayah: ayah)
                        }
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
