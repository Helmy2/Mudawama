import SwiftUI
import CoreLocation
import MudawamaCore

// MARK: - Qibla UI State

enum QiblaUiState {
    case loading
    case active(compassHeading: Double, qiblaAngle: Double)
    case permissionDenied
    case error(String)
}

// MARK: - Qibla ViewModel
// Manages CLLocationManager for device heading + location,
// delegates Qibla angle calculation to Kotlin use case.

@MainActor
class QiblaViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {

    @Published var uiState: QiblaUiState = .loading
    @Published var isAligned = false

    private let provider = QiblaUseCaseProvider()
    private let locationManager = CLLocationManager()

    // Last known device location for Qibla angle calculation
    private var userCoordinates: Coordinates? = nil
    // Current compass heading (degrees from true north)
    private var compassHeading: Double = 0

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.headingFilter = 1  // update every 1°
    }

    func start() {
        switch locationManager.authorizationStatus {
        case .notDetermined:
            locationManager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways:
            locationManager.startUpdatingLocation()
            locationManager.startUpdatingHeading()
        case .denied, .restricted:
            uiState = .permissionDenied
        @unknown default:
            uiState = .permissionDenied
        }
    }

    func stop() {
        locationManager.stopUpdatingLocation()
        locationManager.stopUpdatingHeading()
    }

    func openSystemSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(url)
        }
    }

    // MARK: - CLLocationManagerDelegate

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            switch manager.authorizationStatus {
            case .authorizedWhenInUse, .authorizedAlways:
                manager.startUpdatingLocation()
                manager.startUpdatingHeading()
            case .denied, .restricted:
                self.uiState = .permissionDenied
            case .notDetermined:
                break
            @unknown default:
                break
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        Task { @MainActor in
            self.userCoordinates = Coordinates(
                latitude: loc.coordinate.latitude,
                longitude: loc.coordinate.longitude
            )
            self.recalculate()
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        guard newHeading.headingAccuracy >= 0 else { return }
        Task { @MainActor in
            self.compassHeading = newHeading.trueHeading >= 0
                ? newHeading.trueHeading
                : newHeading.magneticHeading
            self.recalculate()
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            self.uiState = .error(String.loc("error_location"))
        }
    }

    // MARK: - Angle calculation

    private func recalculate() {
        guard let coords = userCoordinates else { return }
        let qiblaAngle = provider.calculateQiblaAngleUseCase.invoke(origin: coords)
        let aligned = abs(compassHeading - qiblaAngle) <= 2.0
            || abs(compassHeading - qiblaAngle) >= 358.0

        if aligned != isAligned {
            isAligned = aligned
            if aligned {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            }
        }
        uiState = .active(compassHeading: compassHeading, qiblaAngle: qiblaAngle)
    }
}
