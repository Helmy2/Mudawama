import SwiftUI
import CoreLocation

struct QiblaView: View {
    @StateObject private var vm = QiblaViewModel()

    var body: some View {
        ZStack {
            Color(.systemGroupedBackground).ignoresSafeArea()

            switch vm.uiState {
            case .loading:
                loadingView
            case .permissionDenied:
                permissionDeniedView
            case .error(let msg):
                errorView(message: msg)
            case .active(let compassHeading, let qiblaAngle):
                activeView(compassHeading: compassHeading, qiblaAngle: qiblaAngle)
            }
        }
        .navigationTitle(String.loc("nav_qibla"))
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { vm.start() }
        .onDisappear { vm.stop() }
    }

    // MARK: - Loading

    private var loadingView: some View {
        VStack(spacing: MudawamaTheme.Spacing.md) {
            ProgressView()
                .tint(MudawamaTheme.Colors.primary)
            Text(String.loc("common_loading"))
                .foregroundStyle(Color.secondary)
                .font(.subheadline)
        }
    }

    // MARK: - Permission denied

    private var permissionDeniedView: some View {
        VStack(spacing: MudawamaTheme.Spacing.lg) {
            Spacer()
            Image(systemName: "location.slash.fill")
                .font(.system(size: 56))
                .foregroundStyle(Color.secondary)
            Text(String.loc("qibla_location_required"))
                .font(.headline)
            Text(String.loc("error_location"))
                .font(.subheadline)
                .foregroundStyle(Color.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, MudawamaTheme.Spacing.xl)
            Button(String.loc("qibla_go_to_settings")) {
                vm.openSystemSettings()
            }
            .buttonStyle(.borderedProminent)
            .tint(MudawamaTheme.Colors.primary)
            Spacer()
        }
    }

    // MARK: - Error

    private func errorView(message: String) -> some View {
        VStack(spacing: MudawamaTheme.Spacing.lg) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundStyle(MudawamaTheme.Colors.missed)
            Text(message)
                .foregroundStyle(Color.secondary)
                .multilineTextAlignment(.center)
            Button(String.loc("common_retry")) { vm.start() }
                .buttonStyle(.borderedProminent)
                .tint(MudawamaTheme.Colors.primary)
            Spacer()
        }
        .padding()
    }

    // MARK: - Active compass

    private func activeView(compassHeading: Double, qiblaAngle: Double) -> some View {
        // Needle rotation: rotate to point toward Qibla relative to current heading.
        // When compassHeading == qiblaAngle the offset is 0 (needle points up = Qibla).
        let needleRotation = qiblaAngle - compassHeading

        return VStack(spacing: MudawamaTheme.Spacing.xl) {
            Spacer()

            // Compass rose
            ZStack {
                // Outer ring
                Circle()
                    .stroke(Color(.systemFill), lineWidth: 2)
                    .frame(width: 260, height: 260)

                // Background fill
                Circle()
                    .fill(Color(.secondarySystemGroupedBackground))
                    .frame(width: 260, height: 260)

                // Cardinal direction ticks
                ForEach(0..<36, id: \.self) { i in
                    let angle = Double(i) * 10.0
                    let isMajor = i % 9 == 0  // N, E, S, W
                    Rectangle()
                        .fill(isMajor ? MudawamaTheme.Colors.primary.opacity(0.5) : Color(.tertiaryLabel))
                        .frame(width: isMajor ? 2 : 1, height: isMajor ? 14 : 8)
                        .offset(y: -118)
                        .rotationEffect(.degrees(angle))
                }

                // Kaaba icon / needle pointing toward Qibla
                VStack(spacing: 0) {
                    Image(systemName: "location.north.fill")
                        .font(.system(size: 52, weight: .semibold))
                        .foregroundStyle(
                            vm.isAligned
                                ? MudawamaTheme.Colors.done
                                : MudawamaTheme.Colors.primary
                        )
                    Spacer().frame(height: 52)
                }
                .frame(height: 260)
                .rotationEffect(.degrees(needleRotation))
                .animation(.interpolatingSpring(stiffness: 40, damping: 8), value: needleRotation)

                // Center dot
                Circle()
                    .fill(Color(.secondarySystemGroupedBackground))
                    .frame(width: 20, height: 20)
                    .overlay(
                        Circle()
                            .stroke(MudawamaTheme.Colors.primary.opacity(0.4), lineWidth: 1.5)
                    )
            }

            // Degree readout
            Text(String(format: "%.0f°", qiblaAngle))
                .font(.system(size: 48, weight: .bold, design: .rounded))
                .foregroundStyle(MudawamaTheme.Colors.primary)
                .monospacedDigit()
                .contentTransition(.numericText())
                .animation(.easeInOut(duration: 0.2), value: Int(qiblaAngle))

            // Status label
            if vm.isAligned {
                Label(String.loc("qibla_aligned"), systemImage: "checkmark.circle.fill")
                    .font(.headline)
                    .foregroundStyle(MudawamaTheme.Colors.done)
                    .transition(.scale.combined(with: .opacity))
            } else {
                let delta = normalizedDelta(compassHeading: compassHeading, qiblaAngle: qiblaAngle)
                let degrees = Int(abs(delta).rounded())
                if delta > 0 {
                    Text(String(format: String.loc("qibla_turn_right"), degrees))
                        .font(.subheadline)
                        .foregroundStyle(Color.secondary)
                } else {
                    Text(String(format: String.loc("qibla_turn_left"), degrees))
                        .font(.subheadline)
                        .foregroundStyle(Color.secondary)
                }
            }

            Text(String.loc("qibla_direction_hint"))
                .font(.caption)
                .foregroundStyle(Color.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, MudawamaTheme.Spacing.xl)

            Spacer()
        }
        .animation(.default, value: vm.isAligned)
    }

    // MARK: - Helpers

    /// Returns signed delta (positive = turn right, negative = turn left), range (-180, 180].
    private func normalizedDelta(compassHeading: Double, qiblaAngle: Double) -> Double {
        var delta = qiblaAngle - compassHeading
        while delta > 180  { delta -= 360 }
        while delta <= -180 { delta += 360 }
        return delta
    }
}

#Preview {
    NavigationStack { QiblaView() }
}
