# Research: Qibla Compass

## Qibla Direction Calculation

### Decision: Haversine Formula for Bearing

**Rationale**: The great-circle bearing formula calculates the initial bearing from point A (user) to point B (Mecca). This is the standard algorithm used by Islamic prayer apps.

**Formula**:
```
θ = atan2(sin(Δλ) * cos(φ₂), cos(φ₁) * sin(φ₂) − sin(φ₁) * cos(φ₂) * cos(Δλ))
```
Where:
- φ₁, φ₂ = latitude of origin and destination (radians)
- Δλ = longitude difference (radians)
- θ = bearing in degrees (0-360)

**Mecca Coordinates**: 21.4225° N, 39.8262° E (fixed constant)

**Alternative considered**: Spherical law of cosines - numerically less stable for small distances, rejected.

---

## KMP SwiftUI Interop

### Decision: Manual ObservableObject wrapper using Combine

**Rationale**: Swift cannot natively observe Kotlin StateFlow. A manual wrapper class using Combine's `@Published` property is required. This is the standard approach unless using a third-party library like SKIE.

**Implementation pattern**:

```swift
@MainActor
class QiblaStateObservable: ObservableObject {
    @Published var state: QiblaState
    
    private var cancellables = Set<AnyCancellable>()
    
    init(viewModel: QiblaViewModel) {
        // Collect Kotlin StateFlow and publish to SwiftUI
        viewModel.state.asDriver()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                self?.state = state
            }
            .store(in: &cancellables)
    }
}
```

**Alternative considered**: SKIE library - provides automatic Swift bindings for Kotlin flows. Rejected for this feature as it adds a dependency. The manual wrapper is sufficient.

**Reference**: Kotlin Multiplatform documentation on Swift interop - manual observation required.
```swift
// Swift side
@ObservedObject private var viewModelWrapper: KmpStateFlowWrapper<QiblaState>

// Observation
viewModelWrapper.state.qiblaAngle
```

**Alternative considered**: Using a custom delegate - rejected as unnecessary complexity.

**Reference**: Kotlin Multiplatform documentation on Swift integration.

---

## Android Sensor: TYPE_ROTATION_VECTOR

### Decision: Use `SensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)`

**Rationale**: `TYPE_ROTATION_VECTOR` uses hardware sensor fusion (accelerometer + magnetometer + gyroscope if available) to compute device orientation. This provides accurate heading regardless of phone tilt, which is essential for a compass held naturally.

**Fallback**: If `TYPE_ROTATION_VECTOR` unavailable, fall back to `TYPE_MAGNETIC_FIELD` + `TYPE_ACCELEROMETER` manual fusion.

**Reference**: Android Sensor API documentation - `SensorManager` + `SensorEvent` for rotation vector.

---

## iOS Heading: CLLocationManager

### Decision: Use `CLLocationManager.headingUpdates`

**Rationale**: `CLLocationManager` provides magnetic heading via `startUpdatingHeading()`. It also reports heading accuracy, which maps directly to our `CompassAccuracy` enum (High/Medium/Low/Unreliable).

**Key APIs**:
- `CLLocationManager.delegate` - receives heading updates
- `CLLocationManager.heading` - magnetic heading in degrees
- `CLLocationManager.headingAccuracy` - accuracy value (-1 = unreliable)

**Reference**: Apple Core Location documentation.

---

## Location Provider Integration

### Decision: Use existing `LocationProvider` from `shared:core:domain`

**Rationale**: The project already has `LocationProvider` interface in `shared/core/domain`:
- `hasPermission(): Boolean`
- `getCurrentLocation(): Result<Coordinates, LocationError>`

Combined with `ObserveSettingsUseCase` from `feature:settings:domain` to get `LocationMode` (GPS vs Manual), this provides the origin point for Qibla calculation.

**Verification**: Confirmed `LocationProvider` exists in `shared/core/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/core/location/LocationProvider.kt`

---

## Haptic Feedback

### Decision: Native UI layers trigger haptics directly

**Rationale**: The domain layer (pure Kotlin) must remain platform-agnostic. The `QiblaViewModel` exposes `isAligned: Boolean`. The native UI layers (SwiftUI on iOS, Compose on Android) observe this and trigger platform-specific haptics:

- **iOS**: `UIImpactFeedbackGenerator` with `.success` style
- **Android**: `Vibrator` service or `VibrationEffect.createOneShot()`

This keeps domain pure while enabling native haptics.

**Trigger condition**: `isAligned` transitions from `false` to `true` (single trigger per alignment entry, as per FR-012).

---

## Summary

| Decision | Status |
|----------|--------|
| Haversine formula for Qibla bearing | ✅ Resolved |
| KMP SwiftUI interop via StateFlow.collectAsState() | ✅ Resolved |
| Android TYPE_ROTATION_VECTOR sensor | ✅ Resolved |
| iOS CLLocationManager heading | ✅ Resolved |
| Use existing LocationProvider + ObserveSettingsUseCase | ✅ Resolved |
| Haptics triggered by native UI layer | ✅ Resolved |

All unknowns resolved. Proceed to Phase 1.