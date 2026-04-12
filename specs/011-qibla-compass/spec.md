# Feature Specification: Qibla Compass

**Feature Branch**: `[011-qibla-compass]`  
**Created**: 2026-04-12  
**Status**: Draft  
**Input**: User description: "Qibla Compass feature with Kotlin ↔ Swift bridge architecture for native iOS SwiftUI compass with smooth rotation"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Native Navigation (Priority: P1) 🎯 MVP

As a user, when I tap the Qibla card on the Home screen, I want to open a dedicated compass screen.

**Why this priority**: This is the entry point for the entire feature. Without navigation, users cannot access the compass functionality.

**Independent Test**: Can be tested by tapping the Qibla card on the Home screen and verifying the compass screen opens (Android: Compose screen, iOS: native SwiftUI view).

**Acceptance Scenarios**:

1. **Given** user is on Home screen, **When** they tap the Qibla card, **Then** the Qibla Compass screen opens
2. **Given** Qibla Compass screen is open, **When** user taps back, **Then** they return to the Home screen
3. **Given** user on iOS, **When** they tap the Qibla card, **Then** a native SwiftUI view is presented (not a Compose screen)

---

### User Story 2 - Real-time Compass Rotation (Priority: P1) 🎯 MVP

As a user, I want the compass dial to rotate smoothly as I physically turn my phone so I can accurately find the Qibla direction.

**Why this priority**: This is the core functionality of the feature. Users rely on the compass to determine the correct prayer direction.

**Independent Test**: Can be tested by rotating the phone and observing the compass needle update smoothly in real-time.

**Acceptance Scenarios**:

1. **Given** the compass screen is open, **When** the user rotates the phone, **Then** the compass dial rotates smoothly at 60-120fps
2. **Given** the Qibla angle is calculated (e.g., 120° from North), **When** the phone is rotated to face that direction, **Then** the needle points directly to Mecca indicator
3. **Given** the phone is held steady, **Then** the compass needle remains stable without jitter

---

### User Story 3 - Haptic Feedback on Alignment (Priority: P2)

As a user, I want my phone to vibrate when the compass aligns with the Qibla so I don't have to stare at the screen.

**Why this priority**: Haptic feedback provides confirmation without requiring visual attention, which is especially useful during prayer.

**Independent Test**: Can be tested by rotating the phone until aligned and feeling the vibration.

**Acceptance Scenarios**:

1. **Given** the phone is within ±2 degrees of the Qibla direction, **Then** a success haptic is triggered
2. **Given** the phone is aligned and haptic fires, **Then** the UI highlights in green to confirm alignment
3. **Given** the phone moves out of alignment range, **Then** the green highlight is removed

---

### User Story 4 - Sensor Calibration Warning (Priority: P1) 🎯 MVP

As a user, I want to know if my phone's compass needs calibration so I don't pray in the wrong direction.

**Why this priority**: Inaccurate compass readings lead to praying in the wrong direction, which is a significant issue for users.

**Independent Test**: Can be tested by observing the calibration warning when sensor accuracy is low.

**Acceptance Scenarios**:

1. **Given** sensor accuracy is "Low" or "Unreliable", **When** the compass screen is open, **Then** a warning UI is displayed prompting calibration
2. **Given** the calibration warning is shown, **When** the user calibrates the device, **Then** the warning is dismissed
3. **Given** sensor accuracy is "High" or "Medium", **Then** no calibration warning is shown

---

### User Story 5 - Location Fallback (Priority: P2)

As a user, if I haven't granted location permissions or set a manual location, I want to be prompted to do so before using the compass.

**Why this priority**: The compass requires location data to calculate the Qibla direction. Without it, the feature cannot function.

**Independent Test**: Can be tested by clearing location settings and opening the compass.

**Acceptance Scenarios**:

1. **Given** no location is available (no GPS permission and no manual coordinates set), **When** the user opens the compass, **Then** an empty state is displayed with a "Go to Settings" button
2. **Given** the empty state is shown, **When** user taps "Go to Settings", **Then** they are navigated to the Settings screen
3. **Given** location is available (from GPS or manual entry), **When** the compass opens, **Then** the Qibla angle is calculated and displayed

---

### Edge Cases

1. **No location set**: User has not granted GPS permission and has not entered manual coordinates → show empty state with Settings button
2. **Sensor not available**: Device lacks magnetometer → display error "Compass not available on this device"
3. **User moves while app in background**: Location changes while app is backgrounded → recalculate Qibla angle on foreground return
4. **Rapid rotation**: User spins phone quickly → compass updates smoothly without lag or missed readings
5. **Alignment at exactly 0/360 degrees**: Qibla is exactly North → needle correctly points to 0° without boundary issues
6. **Haptic already triggered**: User stays aligned for multiple seconds → haptic does not re-trigger repeatedly (single trigger per alignment entry)
7. **Compass screen re-opened after calibration**: User returns to compass after calibrating → warning dismissed, accurate readings shown

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a `NativeNavigationController` interface in Kotlin that shared UI can call to open the Qibla screen
- **FR-002**: iOS implementation of `NativeNavigationController` MUST wrap a native SwiftUI view in `UIHostingController` when `navigateToQibla()` is called
- **FR-003**: System MUST provide an `expect/actual` `CompassSensorManager` that emits a `Flow<CompassHeading>` containing heading (degrees) and accuracy level
- **FR-004**: iOS implementation of `CompassSensorManager` MUST use `CLLocationManager` to obtain magnetic heading
- **FR-005**: Android implementation of `CompassSensorManager` MUST use `android.hardware.SensorManager` with `TYPE_ROTATION_VECTOR` sensor (hardware sensor fusion) to obtain accurate heading regardless of phone tilt
- **FR-006**: System MUST provide a `CalculateQiblaAngleUseCase` in Kotlin that computes the Qibla direction using the Haversine formula
- **FR-007**: The Qibla angle calculation MUST use Mecca coordinates (21.4225° N, 39.8262° E) as the destination
- **FR-008**: The Qibla angle calculation MUST use the user's location as the origin point by combining `ObserveSettingsUseCase` (to get LocationMode) with the app's existing `LocationProvider` (to get live GPS coordinates when mode is GPS, or stored manual coordinates when mode is Manual)
- **FR-009**: System MUST provide a `QiblaViewModel` that exposes a `StateFlow<QiblaState>` containing: current heading, Qibla angle, accuracy status, alignment state
- **FR-010**: iOS SwiftUI view MUST observe the Kotlin `StateFlow<QiblaState>` via Kotlin flows exposed to Swift
- **FR-011**: Compass dial rotation MUST update at 60-120fps to ensure smooth visual experience
- **FR-012**: The QiblaViewModel MUST expose `isAligned` boolean in `QiblaState`; the native UI layers (SwiftUI on iOS, Compose on Android) observe this property and trigger native haptic feedback directly when it transitions from false to true (single trigger per alignment entry)
- **FR-013**: System MUST display a calibration warning when sensor accuracy is "Low" or "Unreliable"
- **FR-014**: System MUST show an empty state with "Go to Settings" button when no location is available from settings
- **FR-015**: System MUST navigate to Settings screen when "Go to Settings" button is tapped from the empty state
- **FR-016**: The needle indicator MUST display the angle delta between device heading and Qibla direction (e.g., "Turn 15° right")
- **FR-017**: All Kotlin logic (navigation interface, sensor manager expect/actual, Qibla angle calculation, ViewModel) MUST be implemented in the shared Kotlin module — SwiftUI only handles rendering, not business logic
- **FR-018**: Android MUST use standard Compose for the Qibla screen UI, while iOS MUST use native SwiftUI

### Key Entities *(include if feature involves data)*

- **NativeNavigationController**: Interface with `navigateToQibla()` method, implemented in Swift for iOS
- **CompassHeading**: Data class with `heading: Double` (0-360 degrees) and `accuracy: CompassAccuracy` (High, Medium, Low, Unreliable)
- **CompassSensorManager**: Expect/actual class emitting `Flow<CompassHeading>` updates
- **CalculateQiblaAngleUseCase**: Use case that takes user latitude/longitude and returns Qibla bearing in degrees
- **QiblaViewModel**: ViewModel exposing `StateFlow<QiblaState>` with heading, qiblaAngle, accuracy, isAligned properties
- **QiblaState**: State data class containing all compass UI state
- **MeccaCoordinates**: Constants (21.4225° N, 39.8262° E) for Qibla calculation

### Default Values

| Setting | Default |
|---------|---------|
| Qibla angle calculation | Based on user's saved location from Settings |
| Compass accuracy | Unknown (until first sensor reading) |
| Alignment threshold | ±2 degrees |
| Haptic trigger | Single fire on entering alignment zone |

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: User can open the Qibla Compass screen from the Home screen with a single tap
- **SC-002**: Compass needle rotates smoothly at 60-120fps as the user physically rotates their phone
- **SC-003**: Qibla direction is correctly calculated based on user's saved location in Settings
- **SC-004**: Haptic feedback triggers when the device heading aligns with Qibla (±2 degrees)
- **SC-005**: Calibration warning appears when sensor accuracy is Low or Unreliable
- **SC-006**: Empty state with "Go to Settings" button appears when no location is available
- **SC-007**: iOS renders the compass using native SwiftUI while Android uses Compose, both achieving smooth 60-120fps rotation

---

## Assumptions

- The project already has `feature:settings:domain` with location mode (GPS/Manual) and stored coordinates available for use
- The app already has a `LocationProvider` (or equivalent) that handles live GPS fetching — this must be combined with `ObserveSettingsUseCase` to get the origin point for Qibla calculation
- Kotlin multiplatform can expose `StateFlow` to Swift via the KMP NSExpectFor implementation pattern
- iOS SwiftUI view can observe Kotlin flows using the standard KMP Swift interop mechanism
- Android Compose and iOS SwiftUI implementations share the same Kotlin ViewModel and Use Cases (single source of truth)
- Haptic feedback is triggered by the native UI layer (not the shared Kotlin code) — SwiftUI uses `UIImpactFeedbackGenerator`, Android Compose uses `Vibrator` or `rememberVibrator()` when `QiblaState.isAligned` transitions from false to true
- The Haversine formula implementation in Kotlin handles edge cases like antipodal points
- The specification follows the same format as feature 010-settings-screen for consistency