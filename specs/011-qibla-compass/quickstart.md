# Quickstart: Qibla Compass

## Prerequisites

- Android Studio with Kotlin 2.x support
- Xcode 15+ for iOS development
- Project builds successfully before starting

## Implementation Order

### Step 1: Domain Layer (Priority: P1)

1. Create `feature/qibla/domain/` module structure:
   - `feature/qibla/domain/src/commonMain/kotlin/`
   - Add `build.gradle.kts` following existing feature pattern

2. Implement domain entities:
   - `CompassHeading.kt` - heading + accuracy
   - `CompassAccuracy.kt` - enum
   - `QiblaState.kt` - ViewModel state
   - `QiblaAction.kt` - user actions
   - `QiblaError.kt` - error states

3. Implement `CalculateQiblaAngleUseCase`:
   ```kotlin
   class CalculateQiblaAngleUseCase {
       operator fun invoke(origin: Coordinates): Double
   }
   ```
   - Use Haversine formula with Mecca coordinates (21.4225° N, 39.8262° E)
   - Pure Kotlin, no platform imports

### Step 2: Data Layer - Sensor (Priority: P1)

1. Create `feature/qibla/data/sensor/`

2. Implement `CompassSensorManager.kt` (expect):
   ```kotlin
   expect class CompassSensorManager {
       fun observeHeading(): Flow<CompassHeading>
       fun start()
       fun stop()
   }
   ```

3. Implement Android actual in `androidMain/`:
   - Use `SensorManager.TYPE_ROTATION_VECTOR`
   - Convert rotation vector to azimuth (heading)
   - Report accuracy from sensor

4. Implement iOS actual in `iosMain/`:
   - Use `CLLocationManager.headingUpdates`
   - Report magnetic heading and accuracy

### Step 3: Navigation Bridge (Priority: P1)

1. Create `feature/qibla/presentation/navigation/`

2. Define `NativeNavigationController` interface:
   ```kotlin
   interface NativeNavigationController {
       fun navigateToQibla()
   }
   ```

3. iOS implementation: Create `iosApp/iosApp/NavigationBridge.swift`
   - Implements `NativeNavigationController`
   - Creates `UIHostingController` wrapping SwiftUI `QiblaView`

### Step 4: Presentation - ViewModel (Priority: P1)

1. Create `feature/qibla/presentation/viewmodel/`

2. Implement `QiblaViewModel`:
   - Inject: `CalculateQiblaAngleUseCase`, `ObserveSettingsUseCase`, `LocationProvider`, `CompassSensorManager`, `CoroutineDispatcher`
   - Expose: `StateFlow<QiblaState>`
   - Handle actions: `StartCompass`, `StopCompass`, `NavigateToSettings`
   - Logic:
     - On start: observe compass heading, get location, calculate Qibla angle
     - On heading update: compute `isAligned = abs(heading - qiblaAngle) <= 2`
     - On location unavailable: set error state

### Step 5: iOS SwiftUI (Priority: P1)

1. Create `iosApp/iosApp/QiblaView.swift`

2. **CRITICAL**: Create a Swift wrapper to observe Kotlin StateFlow:
   ```swift
   import SwiftUI
   import Combine
   
   // Kotlin StateFlow wrapper for SwiftUI observation
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
   - Swift cannot natively observe Kotlin StateFlow without a wrapper
   - Use KMP's built-in `.asDriver()` or collect in a coroutine and publish to `@Published`

3. Implement compass UI:
   - `QiblaView` struct using `QiblaStateObservable`
   - `CompassDial` - rotating compass circle
   - `QiblaNeedle` - pointing to Mecca
   - `AccuracyWarning` - calibration prompt
   - `EmptyState` - "Go to Settings" button
   - Haptic: `UIImpactFeedbackGenerator` triggered on `isAligned` transition

### Step 6: Android Compose (Priority: P1)

1. Create `feature/qibla/presentation/screen/`

2. Implement `QiblaScreen` Composable:
   - Reuse same `QiblaViewModel` as iOS
   - Compose UI matching SwiftUI design (optional for MVP)

### Step 7: Integration (Priority: P1)

1. **Home screen**: Add Qibla card to `HomeScreen`
   - Add `onNavigateToQibla: () -> Unit` parameter to `HomeScreen`
   - Pass to `QiblaSummaryCard`

2. **HomeViewModel**: Add navigation event
   - Add `NavigateToQibla` case to `HomeUiEvent.Navigate`
   - Emit event when user taps Qibla card

3. **MudawamaAppShell**: Wire up navigation via events
   ```kotlin
   val nativeNavController: NativeNavigationController = // via Koin
   
   // Collect HomeViewModel events
   LaunchedEffect(homeState) {
       homeState.event?.let { event ->
           when (event) {
               is HomeUiEvent.NavigateToQibla -> nativeNavController.navigateToQibla()
               // ... other events
           }
       }
   }
   ```

4. **Koin modules**: Create and register Qibla modules
   - Create `QiblaDataModule` in `feature/qibla/data/di/`
     ```kotlin
     val qiblaDataModule = module {
         single { CompassSensorManager() }
     }
     ```
   - Create `QiblaPresentationModule` in `feature/qibla/presentation/di/`
     ```kotlin
     val qiblaPresentationModule = module {
         viewModel { QiblaViewModel(get(), get(), get(), get(), get()) }
         single<NativeNavigationController> { QiblaNavigationBridge() }
     }
     ```
   - Register both in main App Koin setup (e.g., `AppModule.kt` or `MainKoinContext.kt`)

### Step 8: Strings (Priority: P2)

Add to `shared/designsystem/src/commonMain/composeResources/values/strings.xml`:
- `qibla_title` - "Qibla Compass"
- `qibla_calibration_warning` - "Calibrate your compass"
- `qibla_no_location` - "Location required"
- `qibla_go_to_settings` - "Go to Settings"
- `qibla_aligned` - "You are facing Qibla"
- `qibla_turn_right` - "Turn %1$d° right"
- `qibla_turn_left` - "Turn %1$d° left"

---

## Testing Checklist

- [ ] Compass needle rotates smoothly (60-120fps target)
- [ ] Qibla angle calculated correctly for various locations
- [ ] Haptic triggers when aligned (±2°)
- [ ] Calibration warning shows when accuracy Low/Unreliable
- [ ] Empty state shows when no location available
- [ ] Navigation works from Home screen
- [ ] iOS SwiftUI and Android Compose both functional

---

## Common Issues

1. **Heading jumps**: Ensure TYPE_ROTATION_VECTOR not raw magnetometer
2. **Qibla angle wrong**: Verify location mode (GPS vs Manual) combined correctly
3. **Haptic fires repeatedly**: Check `isAligned` transition detection (false→true only)
4. **KMP Swift interop fails**: Ensure `publicResClass = true` in gradle