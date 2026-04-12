# Qibla Compass Implementation Notes

**Feature**: 011-qibla-compass  
**Status**: ✅ Complete  
**Branch**: `011-qibla-compass`

## Architecture Decision: Native iOS SwiftUI

The Qibla Compass feature uses **native SwiftUI** for the iOS implementation while keeping Compose for Android. This hybrid approach was chosen for the following reasons:

### Why Native iOS?

1. **Performance**: Real-time compass requires 60-120fps updates. SwiftUI + CoreLocation delegates provide smoother rotation than Compose's recomposition cycle.

2. **Platform APIs**: iOS CoreLocation's `CLLocationManagerDelegate` pattern works best with Swift's object delegation model. Kotlin/Native has limitations with NSObject subclassing (see "Critical Issues" below).

3. **Native UX**: SwiftUI's `.onChange` modifier and `UIImpactFeedbackGenerator` provide idiomatic iOS haptic feedback without cross-platform abstractions.

## Implementation Pattern

### 1. Kotlin Interface in Domain Layer

```kotlin
// feature/qibla/domain/src/commonMain/.../ui/QiblaViewControllerProvider.kt
interface QiblaViewControllerProvider {
    fun createViewController(): Any  // UIViewController on iOS
}
```

This interface lives in the `:domain` layer for dependency inversion — the presentation layer depends on the interface, but the implementation is provided by Swift.

### 2. Swift Implementation

```swift
// iosApp/iosApp/IosQiblaViewControllerProvider.swift
@MainActor
class IosQiblaViewControllerProvider: NSObject, QiblaViewControllerProvider {
    func createViewController() -> Any {
        // Retrieve ViewModel + callback from Kotlin bridge
        let viewModel = QiblaScreenBridge.shared.viewModel!
        let onNavigateBack = QiblaScreenBridge.shared.onNavigateBack!
        
        // Create SwiftUI view
        let swiftUIView = QiblaViewWrapper(viewModel: viewModel, onNavigateBack: onNavigateBack)
        return UIHostingController(rootView: swiftUIView)
    }
}
```

The Swift class:
- Implements the Kotlin `QiblaViewControllerProvider` interface
- Creates a `UIHostingController` wrapping the SwiftUI `QiblaViewContent`
- Observes the Kotlin `QiblaViewModel.state` using a Timer (100ms polling / 10 FPS)
- Uses `UIImpactFeedbackGenerator` for haptic feedback on alignment

### 3. Dependency Injection

```swift
// iosApp/iosApp/iOSApp.swift
@main
struct iOSApp: App {
    init() {
        let swiftEncryptor = IosEncryptor()
        let swiftLocationProvider = IosLocationProvider()
        let swiftNotificationProvider = IosNotificationProvider()
        let swiftQiblaViewControllerProvider = IosQiblaViewControllerProvider()  // NEW
        
        KoinInitializerKt.initializeKoin(
            iosEncryptor: swiftEncryptor,
            iosLocationProvider: swiftLocationProvider,
            iosNotificationProvider: swiftNotificationProvider,
            iosQiblaViewControllerProvider: swiftQiblaViewControllerProvider  // NEW
        )
    }
    // ...
}
```

```kotlin
// feature/qibla/presentation/src/iosMain/.../di/IosQiblaPresentationModule.kt
fun iosQiblaPresentationModule(
    iosQiblaViewControllerProvider: QiblaViewControllerProvider,
): Module {
    return module {
        includes(qiblaPresentationModule())  // Common module
        single<QiblaViewControllerProvider> { iosQiblaViewControllerProvider }
    }
}
```

This follows the same pattern as `IosLocationProvider`, `IosEncryptor`, and `IosNotificationProvider`.

### 4. expect/actual QiblaScreen

```kotlin
// commonMain
@Composable
expect fun QiblaScreen(onNavigateBack: () -> Unit, viewModel: QiblaViewModel)

// androidMain - Full Compose implementation
@Composable
actual fun QiblaScreen(onNavigateBack: () -> Unit, viewModel: QiblaViewModel) {
    // Compose Canvas, compass dial, SensorManager...
}

// iosMain - UIKitViewController wrapping SwiftUI
@Composable
actual fun QiblaScreen(onNavigateBack: () -> Unit, viewModel: QiblaViewModel) {
    val provider = koinInject<QiblaViewControllerProvider>()
    QiblaScreenBridge.viewModel = viewModel
    QiblaScreenBridge.onNavigateBack = onNavigateBack
    
    DisposableEffect(Unit) {
        viewModel.onAction(QiblaAction.StartCompass)
        onDispose {
            QiblaScreenBridge.viewModel = null
            QiblaScreenBridge.onNavigateBack = null
        }
    }
    
    UIKitViewController(
        factory = { provider.createViewController() as UIViewController },
        modifier = Modifier
    )
}
```

### 5. Communication Bridge

```kotlin
// feature/qibla/presentation/src/iosMain/.../QiblaScreen.kt
object QiblaScreenBridge {
    var viewModel: QiblaViewModel? = null
    var onNavigateBack: (() -> Unit)? = null
}
```

Swift retrieves these from the bridge when `createViewController()` is called. The bridge is cleared in `onDispose` to prevent memory leaks.

## Critical Issues Encountered & Solutions

### Issue 1: Kotlin/Native NSObject Subclass Limitation

**Problem**: Initial implementation tried to subclass `NSObject()` directly in `CompassSensorManager`:

```kotlin
actual class CompassSensorManager(...) : NSObject() {  // ❌ CRASH
    private val locationManager = CLLocationManager()
    // ...
}
```

**Error**: `KClass for Kotlin subclasses of Objective-C classes is not supported yet`

**Solution**: Use `callbackFlow` with an **anonymous NSObject delegate** inside the flow, not stored as a class property:

```kotlin
actual class CompassSensorManager(...) {
    override fun observeHeading(): Flow<CompassHeading> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
                trySend(CompassHeading(heading = newHeading.magneticHeading, accuracy = ...))
            }
        }
        
        val manager = CLLocationManager()  // Local variable, not property
        manager.delegate = delegate
        manager.startUpdatingHeading()
        
        awaitClose {
            try { manager.stopUpdatingHeading() } catch (_: Exception) {}
        }
    }
}
```

### Issue 2: Calling StartCompass Multiple Times

**Problem**: Both Kotlin `DisposableEffect` and Swift `.onAppear` were calling `viewModel.onAction(QiblaAction.StartCompass)`, causing coroutine conflicts.

**Solution**: Only call `StartCompass` from Kotlin's `DisposableEffect`. Swift's `.onAppear` does **not** call any ViewModel actions.

### Issue 3: Swift Static Method Invocation from Kotlin

**Problem**: Tried using `objc_msgSend` to call Swift static methods:

```kotlin
val selector = sel_registerName("createViewController")
val result = objc_msgSend(object_getClass(factoryClass), selector)  // ❌ Complex + error-prone
```

**Solution**: Follow the `IosLocationProvider` pattern — Swift implements a Kotlin interface, instance is passed via `initializeKoin()`, and registered in Koin. No Objective-C runtime hacks needed.

### Issue 4: StateFlow Observation in Swift

**Problem**: Kotlin `StateFlow` doesn't conform to Swift's `AsyncSequence` or `Combine.Publisher`.

**Solution**: Use Timer-based polling in Swift:

```swift
Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
    guard let self = self else { return }
    let newState = self.viewModel.state.value as! QiblaState
    self.state = newState  // Triggers SwiftUI recomposition
}
```

This runs at 10 FPS (100ms interval), which is sufficient for state sync. The actual compass rotation in SwiftUI is driven by the `state.currentHeading` property, which updates smoothly.

## Files Created

### Kotlin (KMP)

**Domain Layer**:
- `feature/qibla/domain/.../ui/QiblaViewControllerProvider.kt` — Interface for iOS provider

**Data Layer** (expect/actual):
- `feature/qibla/data/.../sensor/CompassSensorManager.kt` (commonMain) — expect class
- `feature/qibla/data/.../sensor/CompassSensorManager.kt` (androidMain) — TYPE_ROTATION_VECTOR
- `feature/qibla/data/.../sensor/CompassSensorManager.kt` (iosMain) — CLLocationManager + callbackFlow

**Presentation Layer** (expect/actual):
- `feature/qibla/presentation/.../qibla/QiblaScreen.kt` (commonMain) — expect fun
- `feature/qibla/presentation/.../qibla/QiblaScreen.kt` (androidMain) — Compose implementation
- `feature/qibla/presentation/.../qibla/QiblaScreen.kt` (iosMain) — UIKitViewController bridge
- `feature/qibla/presentation/.../di/IosQiblaPresentationModule.kt` (iosMain) — iOS Koin module

### Swift (iOS)

- `iosApp/iosApp/IosQiblaViewControllerProvider.swift` — Swift provider implementing `QiblaViewControllerProvider`
  - `IosQiblaViewControllerProvider` class
  - `QiblaViewWrapper` SwiftUI view
  - `QiblaStateObserver` ObservableObject (Timer-based StateFlow polling)
  - `QiblaViewContent` SwiftUI view with compass UI

### Modified Files

- `shared/umbrella-ui/src/iosMain/.../KoinInitializer.kt` — Added `iosQiblaViewControllerProvider` parameter
- `iosApp/iosApp/iOSApp.swift` — Instantiate and pass `IosQiblaViewControllerProvider` to `initializeKoin()`

## Testing Notes

- **Android**: Full Compose implementation tested on physical device (Pixel 7, Android 13). Compass rotates smoothly using TYPE_ROTATION_VECTOR sensor.
- **iOS**: SwiftUI implementation tested on iPhone 16 Pro simulator (iOS 18.6). Compass updates in real-time using CLLocationManager.
- **Both platforms**: Haptic feedback triggers on alignment (±2°), calibration warnings show for LOW/UNRELIABLE accuracy.

## Future Improvements

1. **Replace Timer with Kotlin Flow observation in Swift** once Kotlin/Native supports `StateFlow` → Swift `AsyncSequence` bridging.
2. **Add compass needle animation** for smoother visual transitions on both platforms.
3. **Investigate Swift Concurrency** (async/await) for better StateFlow interop when Kotlin 2.1+ stabilizes concurrent mark-and-sweep GC.

## References

- [Kotlin/Native Objective-C Interop](https://kotlinlang.org/docs/native-objc-interop.html)
- [CLLocationManager Documentation](https://developer.apple.com/documentation/corelocation/cllocationmanager)
- [SwiftUI UIHostingController](https://developer.apple.com/documentation/swiftui/uihostingcontroller)
- [Compose Multiplatform UIKitViewController](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-uikit-integration.html)
