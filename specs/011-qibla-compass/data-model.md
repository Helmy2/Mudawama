# Data Model: Qibla Compass

## Domain Entities

### CompassHeading

```kotlin
data class CompassHeading(
    val heading: Double,           // 0.0 to 360.0 degrees
    val accuracy: CompassAccuracy   // Sensor accuracy level
)

enum class CompassAccuracy {
    HIGH,      // ±1-5 degrees
    MEDIUM,    // ±5-15 degrees
    LOW,       // ±15-25 degrees
    UNRELIABLE // >25 degrees or unavailable
}
```

### QiblaState (ViewModel State)

```kotlin
data class QiblaState(
    val currentHeading: Double = 0.0,          // Device heading (0-360)
    val qiblaAngle: Double? = null,            // Calculated Qibla direction
    val accuracy: CompassAccuracy = CompassAccuracy.UNRELIABLE,
    val isAligned: Boolean = false,             // Within ±2° of Qibla
    val hasLocation: Boolean = false,          // Location available
    val isLoading: Boolean = true,              // Initial loading
    val error: QiblaError? = null               // Error state
)

sealed class QiblaError {
    object NoLocation           // No GPS permission and no manual coordinates
    object SensorUnavailable    // Device lacks magnetometer
    object LocationError        // Failed to get location
}
```

### QiblaAction (ViewModel Actions)

```kotlin
sealed class QiblaAction {
    object StartCompass          // Initialize compass sensor
    object StopCompass           // Stop sensor updates
    object NavigateToSettings    // User taps "Go to Settings"
    object RequestLocationPermission  // Request GPS permission (if in GPS mode but denied)
}
```

### QiblaEvent (One-shot events)

```kotlin
sealed class QiblaEvent {
    object NavigateToSettings    // Navigate to Settings screen
}
```

### Coordinates (existing, from shared:core:domain)

```kotlin
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
```

---

## Key Relationships

| Entity | Source | Description |
|--------|--------|-------------|
| `QiblaViewModel` | feature:qibla:presentation | Exposes `StateFlow<QiblaState>` |
| `CalculateQiblaAngleUseCase` | feature:qibla:domain | Takes `Coordinates` → returns `Double` (bearing) |
| `CompassSensorManager` | feature:qibla:data (expect/actual) | Emits `Flow<CompassHeading>` |
| `NativeNavigationController` | feature:qibla:presentation | Interface for navigation |
| `LocationProvider` | shared:core:domain | Gets live GPS coordinates |
| `ObserveSettingsUseCase` | feature:settings:domain | Gets `LocationMode` (GPS/Manual) |

---

## State Transitions

```
                    ┌─────────────────┐
                    │  Initializing   │
                    │  isLoading=true │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
       ┌──────────┐   ┌──────────────┐  ┌──────────┐
       │ Has Data │   │ No Location  │  │  Error   │
       │ isLoading=false    │  hasLocation=false │  error!=null
       │ qiblaAngle!=null   │  error=NoLocation  │          │
       └──────────┘   └──────────────┘  └──────────┘
              │
              ▼ (heading changes)
       ┌──────────────────┐
       │ isAligned check  │
       │ abs(heading -   │
       │ qiblaAngle) <= 2 │
       └────────┬─────────┘
                │
       ┌────────┴────────┐
       ▼                 ▼
┌─────────────┐   ┌─────────────┐
│ isAligned   │   │ !isAligned  │
│ = true      │   │ = false     │
│ (haptic)    │   │             │
└─────────────┘   └─────────────┘
```

---

## Validation Rules

1. **Heading**: 0.0 ≤ heading ≤ 360.0 (wrap-around at 360)
2. **Qibla angle**: 0.0 ≤ qiblaAngle ≤ 360.0
3. **Alignment threshold**: ±2 degrees (FR-012)
4. **Coordinates**: Valid lat/long per `LocationProvider` constraints

---

## No New Database Entities

This feature does not require new Room entities. Qibla direction is calculated on-the-fly from user location; no persistence needed.