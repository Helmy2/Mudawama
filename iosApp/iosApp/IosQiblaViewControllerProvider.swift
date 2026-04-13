import SwiftUI
import UIKit
import MudawamaUI

/// Swift implementation of the KMP `QiblaViewControllerProvider` interface.
@MainActor
class IosQiblaViewControllerProvider: NSObject, QiblaViewControllerProvider {
    
    nonisolated func createViewController() -> Any {
        return MainActor.assumeIsolated {
            guard let viewModel = QiblaScreenBridge.shared.viewModel,
                  let onNavigateBack = QiblaScreenBridge.shared.onNavigateBack else {
                fatalError("QiblaScreenBridge parameters not set.")
            }
            
            let qiblaView = QiblaViewWrapper(
                viewModel: viewModel,
                onNavigateBack: onNavigateBack
            )
            
            let hostingController = UIHostingController(rootView: qiblaView)
            return hostingController
        }
    }
}

/// SwiftUI wrapper that observes Kotlin ViewModel and handles Theme/RTL
private struct QiblaViewWrapper: View {
    @StateObject private var observer: QiblaStateObserver
    let onNavigateBack: () -> Void
    
    init(viewModel: QiblaViewModel, onNavigateBack: @escaping () -> Void) {
        _observer = StateObject(wrappedValue: QiblaStateObserver(viewModel: viewModel))
        self.onNavigateBack = onNavigateBack
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                QiblaViewContent(
                    state: observer.state,
                    onAction: observer.onAction,
                    strings: observer.strings
                )
            }
            .navigationTitle(observer.strings?.title ?? "Qibla")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onNavigateBack) {
                        Image(systemName: "chevron.backward")
                            .foregroundColor(Color(uiColor: .label))
                    }
                }
            }
        }
        .preferredColorScheme(observer.isDarkTheme ? .dark : .light)
        .environment(\.layoutDirection, observer.isRTL ? .rightToLeft : .leftToRight)
        .id(observer.isRTL)
    }
}

/// Observable wrapper for Kotlin StateFlow
@MainActor
private class QiblaStateObserver: ObservableObject {
    @Published var state: QiblaState
    @Published var isDarkTheme: Bool = false
    @Published var isRTL: Bool = false
    @Published var strings: QiblaStrings? = nil
    
    private let viewModel: QiblaViewModel
    private var timer: Timer?
    
    init(viewModel: QiblaViewModel) {
        self.viewModel = viewModel
        self.state = viewModel.state.value!
        
        // Poll for state, theme, RTL, and string updates
        self.timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
            Task { @MainActor in
                guard let self = self else { return }
                self.state = self.viewModel.state.value!
                self.isDarkTheme = QiblaScreenBridge.shared.isDarkTheme
                self.isRTL = QiblaScreenBridge.shared.isRTL
                self.strings = QiblaScreenBridge.shared.strings
            }
        }
    }
    
    func onAction(_ action: QiblaAction) {
        viewModel.onAction(action: action)
    }
    
    deinit {
        timer?.invalidate()
    }
}

/// The actual SwiftUI view content
private struct QiblaViewContent: View {
    let state: QiblaState
    let onAction: (QiblaAction) -> Void
    let strings: QiblaStrings?
    
    @State private var previousAligned: Bool = false
    
    var body: some View {
        VStack(spacing: 24) {
            if state.isLoading && state.qiblaAngle == nil {
                ProgressView()
            } else if let error = state.error {
                errorContent(error: error)
            } else {
                compassContent()
            }
        }
        .padding()
        .onChange(of: state.isAligned) { oldValue, newValue in
            if newValue && !previousAligned {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            }
            previousAligned = newValue
        }
    }
    
    @ViewBuilder
    private func errorContent(error: QiblaError) -> some View {
        VStack(spacing: 16) {
            Text(errorMessage(for: error))
                .font(.body)
                .foregroundColor(Color(uiColor: .systemRed))
            
            Button(action: { onAction(.RequestLocationPermission()) }) {
                Text(strings?.goToSettings ?? "Go to Settings")
                    .padding()
                    .background(Color(uiColor: .systemBlue))
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
    }
    
    @ViewBuilder
    private func compassContent() -> some View {
        VStack(spacing: 32) {
            Image(systemName: "location.north.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 150, height: 150)
                .rotationEffect(.degrees((Double(truncating: state.qiblaAngle ?? 0.0)) - state.currentHeading))
                .foregroundColor(state.isAligned ? Color(uiColor: .systemGreen) : Color(uiColor: .systemBlue))
                .flipsForRightToLeftLayoutDirection(false)
            
            Text(directionText)
                .font(.title2)
                .foregroundColor(state.isAligned ? Color(uiColor: .systemGreen) : Color(uiColor: .label))
            
            Text("\(Int(Double(truncating:state.qiblaAngle ?? 0.0)))°")
                .font(.system(size: 72, weight: .bold))
                .foregroundColor(Color(uiColor: .systemBlue))
            
            Text("\(Int(state.currentHeading))°")
                .font(.title)
                .foregroundColor(Color(uiColor: .secondaryLabel))
            
            if shouldShowCalibrationWarning {
                calibrationWarning()
            }
        }
    }
    
    @ViewBuilder
    private func calibrationWarning() -> some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(Color(uiColor: .systemOrange))
            Text(strings?.calibrationWarning ?? "Calibrate your compass")
                .font(.caption)
                .foregroundColor(Color(uiColor: .systemOrange))
        }
        .padding(12)
        .background(Color(uiColor: .systemOrange).opacity(0.1))
        .cornerRadius(10)
    }
    
    private var directionText: String {
        if state.isAligned {
            return strings?.aligned ?? "You are facing Qibla"
        }
        
        let diff = calculateTurnAngle()
        if diff >= 0 {
            let format = strings?.turnRight ?? "Turn %d° right"
            return String(format: format, diff)
        } else {
            let format = strings?.turnLeft ?? "Turn %d° left"
            return String(format: format, -diff)
        }
    }
    
    private var shouldShowCalibrationWarning: Bool {
        state.accuracy == .low || state.accuracy == .unreliable
    }
    
    private func calculateTurnAngle() -> Int {
        let qiblaAngle = Double(truncating:state.qiblaAngle ?? 0.0)
        let currentHeading = state.currentHeading
        
        var diff = qiblaAngle - currentHeading
        if diff < 0 { diff += 360 }
        if diff > 180 { diff = 360 - diff }
        return Int(diff)
    }
    
    private func errorMessage(for error: QiblaError) -> String {
        if error is QiblaError.NoLocation {
            return strings?.locationRequired ?? "Location required"
        } else if error is QiblaError.SensorUnavailable {
            return strings?.calibrationWarning ?? "Compass sensor unavailable"
        } else if error is QiblaError.LocationError {
            return strings?.locationRequired ?? "Location error"
        } else {
            return "Unknown error"
        }
    }
}
