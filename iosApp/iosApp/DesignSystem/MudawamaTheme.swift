import SwiftUI

// MARK: - Mudawama iOS Design System
// Color tokens match the Compose design system (teal primary, surface/background).
// Uses adaptive colors so Dark Mode works automatically.

enum MudawamaTheme {

    // MARK: Colors

    enum Colors {
        /// Primary teal — matches MaterialTheme primary in the Compose design system.
        static let primary = Color(red: 0.0, green: 0.588, blue: 0.588)
        /// On-primary (white text / icons on teal backgrounds).
        static let onPrimary = Color.white
        /// Surface card background (white / dark-elevated).
        static let surface = Color(.secondarySystemGroupedBackground)
        /// App background (off-white / true black).
        static let background = Color(.systemGroupedBackground)
        /// Destructive / error red.
        static let error = Color.red
        /// Done / success green.
        static let done = Color.green
        /// Missed / warning orange.
        static let missed = Color.orange
    }

    // MARK: Corner Radius

    enum Radius {
        static let card: CGFloat = 16
        static let button: CGFloat = 12
        static let chip: CGFloat = 8
    }

    // MARK: Spacing

    enum Spacing {
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 16
        static let lg: CGFloat = 24
        static let xl: CGFloat = 32
    }
}

// MARK: - MudawamaSurfaceCard

/// App-wide card surface matching `MudawamaSurfaceCard` from the Compose design system.
struct MudawamaSurfaceCard<Content: View>: View {
    var cornerRadius: CGFloat = MudawamaTheme.Radius.card
    var padding: EdgeInsets = EdgeInsets(
        top: MudawamaTheme.Spacing.md,
        leading: MudawamaTheme.Spacing.md,
        bottom: MudawamaTheme.Spacing.md,
        trailing: MudawamaTheme.Spacing.md
    )
    var action: (() -> Void)? = nil
    @ViewBuilder let content: () -> Content

    var body: some View {
        Group {
            if let action {
                Button(action: action) { cardContent }
                    .buttonStyle(.plain)
            } else {
                cardContent
            }
        }
    }

    private var cardContent: some View {
        content()
            .padding(padding)
            .background(Color(.secondarySystemGroupedBackground))
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .shadow(color: .black.opacity(0.06), radius: 2, x: 0, y: 1)
    }
}
