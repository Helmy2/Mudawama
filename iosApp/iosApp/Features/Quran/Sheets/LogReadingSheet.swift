import SwiftUI
import MudawamaCore

struct LogReadingSheet: View {
    @Environment(\.dismiss) private var dismiss
    let date: Kotlinx_datetimeLocalDate
    let vm: QuranViewModel
    @State private var pagesText = ""

    var body: some View {
        NavigationStack {
            Form {
                Section(String.loc("quran_pages_read_label")) {
                    TextField("1", text: $pagesText)
                        .keyboardType(.numberPad)
                }
            }
            .navigationTitle(String.loc("quran_log_sheet_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(String.loc("common_cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(String.loc("common_confirm")) {
                        if let pages = Int(pagesText), pages > 0 {
                            vm.logReading(pages: pages, date: date)
                        }
                        dismiss()
                    }
                    .disabled(Int(pagesText) == nil || (Int(pagesText) ?? 0) < 1)
                }
            }
        }
        .presentationDetents([.medium])
    }
}
