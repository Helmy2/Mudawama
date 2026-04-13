import Foundation
import CoreLocation
import MudawamaUI

/// Swift implementation of the KMP `LocationProvider` interface.
/// Uses CLLocationManager natively on the main thread, avoiding all
/// Kotlin/Native threading issues with CoreLocation.
///
/// ### Continuation-safety notes
/// Both `ensurePermission()` and `fetchOneLocation()` store a single
/// `CheckedContinuation` while they are suspended.  Two races are
/// guarded against:
///
/// 1. **Synchronous-delegate race** — `CLLocationManager` may call its
///    delegate synchronously inside `requestWhenInUseAuthorization()` /
///    `requestLocation()` (common in the Simulator and on iOS 17+ when
///    the answer is already cached).  We therefore check whether the
///    continuation was already resumed *after* the request call and, if
///    so, resume it immediately from inside the `withCheckedContinuation`
///    closure.
///
/// 2. **Re-entrant call** — if `getCurrentLocation()` is called while a
///    previous call is still in-flight we return `.locationUnavailable`
///    immediately rather than overwriting the in-flight continuation and
///    leaking it.
@MainActor
class IosLocationProvider: NSObject, LocationProvider, CLLocationManagerDelegate {

    private let manager = CLLocationManager()

    // Continuations are set/cleared on the main thread only.
    private var permissionContinuation: CheckedContinuation<CLAuthorizationStatus, Never>?
    private var locationContinuation: CheckedContinuation<CLLocation?, Never>?

    // Flag set by the delegate *before* the continuation is stored,
    // used to handle the synchronous-delegate race in ensurePermission().
    private var pendingAuthStatus: CLAuthorizationStatus? = nil
    // Flag set by the delegate before the continuation is stored,
    // used to handle the synchronous-delegate race in fetchOneLocation().
    private var pendingLocation: CLLocation?? = nil  // nil = "not yet fired", .some(nil) = "fired with no location"

    private var isResolvingLocation = false

    override init() {
        super.init()
        manager.delegate = self
    }

    // MARK: - LocationProvider

    nonisolated func hasPermission() -> Bool {
        let status = CLLocationManager().authorizationStatus
        return status == .authorizedAlways || status == .authorizedWhenInUse
    }

    nonisolated func __getCurrentLocation(completionHandler: @escaping @Sendable ((any Result)?, (any Error)?) -> Void) {
        Task { @MainActor in
            let result = await self.resolveLocation()
            completionHandler(result, nil)
        }
    }

    // Convenience wrapper matching the Kotlin suspend function
    func getCurrentLocation() async throws -> any Result {
        return await resolveLocation()
    }

    // MARK: - Internal async logic (always runs on MainActor)

    private func resolveLocation() async -> Result {
        // Guard against re-entrant calls to avoid continuation leaks.
        guard !isResolvingLocation else {
            return ResultFailure(error: LocationErrorLocationUnavailable.shared)
        }
        isResolvingLocation = true
        defer { isResolvingLocation = false }

        // Step 1: ensure permission
        let status = await ensurePermission()

        guard status == .authorizedWhenInUse || status == .authorizedAlways else {
            return ResultFailure(error: LocationErrorPermissionDenied.shared)
        }

        // Step 2: fetch one location fix
        guard let location = await fetchOneLocation() else {
            return ResultFailure(error: LocationErrorLocationUnavailable.shared)
        }

        let coords = Coordinates(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        )
        return ResultSuccess(data: coords)
    }

    private func ensurePermission() async -> CLAuthorizationStatus {
        let current = manager.authorizationStatus
        guard current == .notDetermined else { return current }

        // Clear any stale synchronous-race flag before making the request.
        pendingAuthStatus = nil

        return await withCheckedContinuation { continuation in
            // If the delegate already fired synchronously (before this closure
            // ran), consume the cached status and resume immediately.
            if let alreadyResolved = self.pendingAuthStatus {
                self.pendingAuthStatus = nil
                continuation.resume(returning: alreadyResolved)
                return
            }
            // Normal path: store continuation, wait for delegate.
            self.permissionContinuation = continuation
            self.manager.requestWhenInUseAuthorization()
        }
    }

    private func fetchOneLocation() async -> CLLocation? {
        // Clear any stale synchronous-race flag before making the request.
        pendingLocation = nil

        return await withCheckedContinuation { continuation in
            // If the delegate already fired synchronously, consume and resume.
            if let alreadyResolved = self.pendingLocation {
                self.pendingLocation = nil
                continuation.resume(returning: alreadyResolved)
                return
            }
            // Normal path: store continuation, request a one-shot fix.
            self.locationContinuation = continuation
            self.manager.requestLocation()
        }
    }

    // MARK: - CLLocationManagerDelegate

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            self.handleAuthorizationChange(manager: manager)
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        Task { @MainActor in
            self.handleLocationUpdate(locations: locations)
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            self.handleLocationError(error: error)
        }
    }

    // MARK: - Internal handlers (MainActor)

    private func handleAuthorizationChange(manager: CLLocationManager) {
        let status = manager.authorizationStatus
        guard status != .notDetermined else { return }

        if let continuation = permissionContinuation {
            permissionContinuation = nil
            continuation.resume(returning: status)
        } else {
            pendingAuthStatus = status
        }
    }

    private func handleLocationUpdate(locations: [CLLocation]) {
        let location = locations.last

        if let continuation = locationContinuation {
            locationContinuation = nil
            continuation.resume(returning: location)
        } else {
            pendingLocation = .some(location)
        }
    }

    private func handleLocationError(error: Error) {
        if let continuation = locationContinuation {
            locationContinuation = nil
            continuation.resume(returning: nil)
        } else {
            pendingLocation = .some(nil)
        }
    }
}