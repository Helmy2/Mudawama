import SwiftUI
import UIKit
import MudawamaUI

/// Swift implementation of the KMP `QiblaViewControllerProvider` interface.
/// Creates a UIViewController wrapping the SwiftUI QiblaView.
///
/// This follows the same pattern as IosLocationProvider - implements a Kotlin
/// interface in Swift and is passed to initializeKoin() from iOSApp.swift.
@MainActor
class IosQiblaViewControllerProvider: NSObject, QiblaViewControllerProvider {
    
    /// Creates a UIViewController hosting the SwiftUI QiblaView.
    /// Retrieves the ViewModel and navigation callback from the Kotlin bridge.
    func createViewController() -> Any {
        // Get viewModel and callback from Kotlin bridge
        guard let viewModel = QiblaScreenBridge.shared.viewModel,
              let onNavigateBack = QiblaScreenBridge.shared.onNavigateBack else {
            fatalError("QiblaScreenBridge parameters not set. Make sure QiblaScreen.kt sets viewModel and onNavigateBack before calling createViewController().")
        }
        
        let qiblaView = QiblaViewWrapper(
            viewModel: viewModel,
            onNavigateBack: onNavigateBack
        )
        
        let hostingController = UIHostingController(rootView: qiblaView)
        return hostingController
    }
}

/// SwiftUI wrapper that observes Kotlin ViewModel
private struct QiblaViewWrapper: View {
    @StateObject private var observer: QiblaStateObserver
    let onNavigateBack: () -> Void
    
    init(viewModel: QiblaViewModel, onNavigateBack: @escaping () -> Void) {
        _observer = StateObject(wrappedValue: QiblaStateObserver(viewModel: viewModel))
        self.onNavigateBack = onNavigateBack
    }
    
    var body: some View {
        QiblaViewContent(
            state: observer.state,
            onAction: observer.onAction,
            onNavigateBack: onNavigateBack
        )
    }
}

/// Observable wrapper for Kotlin StateFlow
@MainActor
private class QiblaStateObserver: ObservableObject {
    @Published var state: QiblaState
    private let viewModel: QiblaViewModel
    private var timer: Timer?
    
    init(viewModel: QiblaViewModel) {
        self.viewModel = viewModel
        // Get initial state
        self.state = viewModel.state.value as! QiblaState
        
        // Start observing using a periodic timer
        // Check for state updates every 100ms (10 FPS for state sync)
        self.timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            let newState = self.viewModel.state.value as! QiblaState
            // Only update if reference changed (Kotlin creates new state objects)
            self.state = newState
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
    let onNavigateBack: () -> Void
    
    @State private var previousAligned: Bool = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                if state.isLoading && state.qiblaAngle == nil {
                    ProgressView()
                } else if let error = state.error {
                    errorContent(error: error)
                } else if state.qiblaAngle != nil {
                    compassContent()
                } else {
                    compassContent()
                }
            }
            .padding()
            .navigationTitle("Qibla")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onNavigateBack) {
                        Image(systemName: "chevron.left")
                    }
                }
            }
        }
        .onChange(of: state.isAligned) { newValue in
            if newValue && !previousAligned {
                // Trigger haptic feedback
                let generator = UIImpactFeedbackGenerator(style: .medium)
                generator.impactOccurred()
            }
            previousAligned = newValue
        }
    }
    
    @ViewBuilder
    private func errorContent(error: QiblaError) -> some View {
        VStack(spacing: 16) {
            Text(errorMessage(for: error))
                .font(.body)
                .foregroundColor(.red)
            
            Button(action: {
                onAction(.RequestLocationPermission())
            }) {
                Text("Go to Settings")
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
    }
    
    @ViewBuilder
    private func compassContent() -> some View {
        VStack(spacing: 32) {
            Text(directionText)
                .font(.title2)
                .foregroundColor(state.isAligned ? .green : .primary)
            
            Text("\(Int(state.qiblaAngle?.doubleValue ?? 0))°")
                .font(.system(size: 72, weight: .bold))
                .foregroundColor(.blue)
            
            Text("\(Int(state.currentHeading))°")
                .font(.title)
                .foregroundColor(.secondary)
            
            if shouldShowCalibrationWarning {
                calibrationWarning()
            }
        }
    }
    
    @ViewBuilder
    private func calibrationWarning() -> some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.orange)
            Text("Calibrate your compass")
                .font(.caption)
                .foregroundColor(.orange)
        }
        .padding(12)
        .background(Color.orange.opacity(0.1))
        .cornerRadius(10)
    }
    
    private var directionText: String {
        if state.isAligned {
            return "You are facing Qibla"
        }
        
        let diff = calculateTurnAngle()
        if diff >= 0 {
            return "Turn \(diff)° right"
        } else {
            return "Turn \(-diff)° left"
        }
    }
    
    private var shouldShowCalibrationWarning: Bool {
        state.accuracy == .low || state.accuracy == .unreliable
    }
    
    private func calculateTurnAngle() -> Int {
        // Convert KotlinDouble to Swift Double
        let qiblaAngle = state.qiblaAngle?.doubleValue ?? 0.0
        let currentHeading = state.currentHeading
        
        var diff = qiblaAngle - currentHeading
        if diff < 0 { diff += 360 }
        if diff > 180 { diff = 360 - diff }
        return Int(diff)
    }
    
    private func errorMessage(for error: QiblaError) -> String {
        if error is QiblaError.NoLocation {
            return "Location required"
        } else if error is QiblaError.SensorUnavailable {
            return "Compass sensor unavailable"
        } else if error is QiblaError.LocationError {
            return "Location error"
        } else {
            return "Unknown error"
        }
    }
}
