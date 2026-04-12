# Contracts: Qibla Compass

## Navigation Contract

### NativeNavigationController Interface

**Location**: `feature/qibla/presentation/navigation/NativeNavigationController.kt`

```kotlin
package io.github.helmy2.mudawama.feature.qibla.presentation.navigation

interface NativeNavigationController {
    /**
     * Navigate to the Qibla Compass screen.
     * - Android: Pushes a Compose screen via navigation system
     * - iOS: Presents a native SwiftUI view via UIHostingController
     */
    fun navigateToQibla()
}
```

### iOS Implementation Contract

**Location**: `iosApp/iosApp/NavigationBridge.kt`

```swift
class QiblaNavigationBridge: NativeNavigationController {
    private let window: UIWindow
    
    init(window: UIWindow) {
        self.window = window
    }
    
    func navigateToQibla() {
        let qiblaView = QiblaView(viewModel: /* Kotlin QiblaViewModel */)
        let hostingController = UIHostingController(rootView: qiblaView)
        window.rootViewController?.present(hostingController, animated: true)
    }
}
```

### Home Screen Integration Contract

**Location**: `feature/home/presentation/` (existing HomeScreen)

The Home screen uses the established `HomeUiEvent` architecture from feature 009. The Qibla navigation must follow this pattern:

1. **HomeUiEvent.Navigate**: Add a new case for Qibla navigation
   ```kotlin
   sealed class HomeUiEvent {
       sealed class Navigate : HomeUiEvent() {
           object ToPrayer : Navigate()
           object ToAthkar : Navigate()
           object ToQuran : Navigate()
           object ToSettings : Navigate()
           object ToHabits : Navigate()
           object ToTasbeeh : Navigate()
           object ToQibla : Navigate()  // NEW
       }
   }
   ```

2. **HomeViewModel**: Emit `HomeUiEvent.Navigate.ToQibla` when user taps Qibla card
   ```kotlin
   // In handleAction() for action that triggers navigation
   _events.add(HomeUiEvent.Navigate.ToQibla)
   ```

3. **MudawamaAppShell**: Handle the event and call NativeNavigationController
   ```kotlin
   // Collect HomeViewModel events
   LaunchedEffect(homeViewModel) {
       homeViewModel.events.collect { event ->
           when (event) {
               is HomeUiEvent.Navigate.ToQibla -> nativeNavController.navigateToQibla()
               // ... other navigation events
           }
       }
   }
   ```

4. **NativeNavigationController**: Must be available in AppShell scope via Koin
   ```kotlin
   val nativeNavController: NativeNavigationController = get()
   ```

---

## Sensor Contract

### CompassSensorManager (expect)

**Location**: `feature/qibla/data/sensor/CompassSensorManager.kt`

```kotlin
expect class CompassSensorManager {
    /**
     * Observe compass heading updates.
     * Emits CompassHeading with heading (0-360) and accuracy level.
     * Completes when scope is cancelled.
     */
    fun observeHeading(): Flow<CompassHeading>
    
    fun start()
    fun stop()
}
```

### Android Implementation Contract

```kotlin
actual class CompassSensorManager {
    private val sensorManager: SensorManager
    private val rotationSensor: Sensor
    
    // Uses TYPE_ROTATION_VECTOR with SENSOR_DELAY_UI
    // Converts rotation matrix to azimuth (heading)
    // Reports accuracy from magnetic field sensor
}
```

### iOS Implementation Contract

```swift
class CompassSensorManager {
    private let locationManager: CLLocationManager
    
    // Uses CLLocationManager.headingUpdates
    // Reports magnetic heading and accuracy
}
```

---

## QiblaViewModel Contract

**Location**: `feature/qibla/presentation/viewmodel/QiblaViewModel.kt`

```kotlin
class QiblaViewModel(
    private val calculateQiblaAngleUseCase: CalculateQiblaAngleUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val locationProvider: LocationProvider,
    private val compassSensorManager: CompassSensorManager,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    
    val state: StateFlow<QiblaState>
    
    fun onAction(action: QiblaAction)
    
    // Key state properties exposed to SwiftUI:
    // - state.value.currentHeading
    // - state.value.qiblaAngle  
    // - state.value.isAligned
    // - state.value.accuracy
    // - state.value.hasLocation
}
```

---

## Kotlin ↔ Swift Interop Summary

| Kotlin Type | Swift Observable | Notes |
|-------------|------------------|-------|
| `StateFlow<QiblaState>` | `ObservableObject` wrapper | Manual wrapper using Combine (see quickstart.md Step 5) |
| `CompassHeading` | Swift struct with same fields | Auto-mapped |
| `QiblaState` | Swift struct with same fields | Auto-mapped |
| `NativeNavigationController` | Concrete Swift implementation | Passed to Kotlin at init |

No additional data transfer objects required; KMP handles type mapping.