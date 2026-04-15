import SwiftUI
import MudawamaCore

struct NextPrayerCard: View {
    let prayers: [PrayerWithStatus]
    @EnvironmentObject private var appSettings: AppSettingsViewModel

    private var nextPrayer: PrayerWithStatus? {
        prayers.first(where: { $0.status == .pending })
    }

    var body: some View {
        MudawamaSurfaceCard {
            HStack(alignment: .center, spacing: MudawamaTheme.Spacing.md) {
                // Icon
                ZStack {
                    Circle()
                        .fill(MudawamaTheme.Colors.primary.opacity(0.12))
                        .frame(width: 56, height: 56)
                    Image(systemName: "moon.stars.fill")
                        .font(.title2)
                        .foregroundStyle(MudawamaTheme.Colors.primary)
                }

                VStack(alignment: .leading, spacing: MudawamaTheme.Spacing.xs) {
                    Text(String.loc("home_next_prayer_label"))
                        .font(.caption)
                        .foregroundStyle(Color.secondary)
                    if let prayer = nextPrayer {
                        Text(String.loc(prayer.name.localisedKey))
                            .font(.title2.bold())
                        Text(prayer.timeString)
                            .font(.subheadline)
                            .foregroundStyle(Color.secondary)
                    } else {
                        Text("—")
                            .font(.title2.bold())
                            .foregroundStyle(Color.secondary)
                    }
                }
                Spacer()

                // All prayers status dots
                if !prayers.isEmpty {
                    VStack(alignment: .trailing, spacing: MudawamaTheme.Spacing.xs) {
                        ForEach(prayers, id: \.habitId) { prayer in
                            HStack(spacing: MudawamaTheme.Spacing.xs) {
                                Text(String.loc(prayer.name.localisedKey))
                                    .font(.caption2)
                                    .foregroundStyle(Color.secondary)
                                Image(systemName: prayer.status.symbolName)
                                    .font(.caption)
                                    .foregroundStyle(prayer.status.symbolColor)
                            }
                        }
                    }
                }
            }
        }
    }
}
