# Implementation Plan: Qibla Compass

**Branch**: `[011-qibla-compass]` | **Date**: 2026-04-12 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/011-qibla-compass/spec.md`

## Summary

Qibla Compass feature providing a buttery-smooth 60-120fps compass that points Muslims toward Mecca. Uses Kotlin ↔ Swift bridge architecture: iOS renders with native SwiftUI for performance, Android uses Compose. Shared Kotlin provides compass sensor (expect/actual), Qibla angle calculation (Haversine), and ViewModel state.

## Technical Context

**Language/Version**: Kotlin 2.x (KMP), Swift 5.9 (iOS native), Compose Multiplatform 1.10.3  
**Primary Dependencies**: Koin 4.2.0, kotlinx-coroutines 1.10.2, Android Sensor API, iOS CoreLocation  
**Storage**: DataStore (settings location), no new DB entities required  
**Testing**: Platform-specific (XCTest for iOS, JUnit for Kotlin)  
**Target Platform**: Android (minSdk 30), iOS 15+  
**Performance Goals**: 60-120fps compass dial rotation, <16ms frame time  
**Constraints**: KMP ↔ SwiftUI interop, Clean Architecture (domain/data/presentation split), MVI pattern  
**Scale**: Single screen feature, 5 user stories, 18 functional requirements

## Constitution Check

| Gate | Status | Notes |
|------|--------|-------|
| Domain layer pure Kotlin | ✅ Pass | CalculateQiblaAngleUseCase uses pure Kotlin math |
| Presentation layer MVI | ✅ Pass | QiblaViewModel uses State/Action/Event pattern |
| No hardcoded strings | ✅ Pass | Strings via shared/designsystem/strings.xml |
| Result<D,E> pattern | ✅ Pass | Domain returns results, no exceptions thrown |
| Koin DI only | ✅ Pass | All DI via Koin modules |
| Dispatcher injection | ✅ Pass | CoroutineDispatcher injected, no hardcoded |

**Note**: iOS uses SwiftUI (not Compose) for the compass UI per FR-018. This is standard KMP architecture - the domain layer remains pure Kotlin, and the ViewModel follows MVI pattern.

## Project Structure

### Documentation (this feature)

```text
specs/011-qibla-compass/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (navigation interface contracts)
└── tasks.md             # Phase 2 output (/speckit.tasks - NOT created here)
```

### Source Code (repository root)

```text
feature/qibla/
├── domain/              # Kotlin domain layer (pure)
│   ├── model/           # CompassHeading, QiblaState, CompassAccuracy
│   ├── usecase/         # CalculateQiblaAngleUseCase
│   └── repository/      # QiblaRepository interface
├── data/                # Kotlin data layer
│   ├── sensor/          # CompassSensorManager expect + androidMain/iosMain actual
│   └── repository/      # QiblaRepositoryImpl
└── presentation/       # Kotlin presentation layer
    ├── viewmodel/       # QiblaViewModel (MVI)
    └── navigation/      # NativeNavigationController interface

iosApp/iosApp/
├── QiblaView.swift      # Native SwiftUI compass UI
└── NavigationBridge.swift # NativeNavigationController Swift impl

# Home screen integration
feature/home/presentation/  # Add Qibla navigation via HomeUiEvent
shared/navigation/           # Add QiblaRoute (existing Routes.kt pattern)
```

**Structure Decision**: New `feature:qibla` module follows existing feature module pattern (domain/data/presentation). Native SwiftUI files live in iosApp alongside other iOS code. Navigation bridge interface in shared Kotlin, implemented in Swift.

## Phase 0: Research

### Research Tasks

1. **KMP SwiftUI interop**: Research best practices for exposing Kotlin StateFlow to SwiftUI
2. **Haversine Qibla formula**: Verify mathematical correctness for Qibla bearing calculation
3. **Android TYPE_ROTATION_VECTOR**: Research sensor fusion API usage
4. **iOS CLLocationManager heading**: Research magnetic heading accuracy and calibration events

### Unknowns to Resolve

- LocationProvider existence check in shared:core:location ✅ Resolved
- Haptic API availability on Android (Vibrator service vs VibrationEffect) ✅ Resolved

**Output**: [research.md](./research.md) - all unknowns resolved

## Phase 1: Design & Contracts

### Data Model

Output: [data-model.md](./data-model.md)
- Entities: CompassHeading, QiblaState, QiblaAction, QiblaEvent, QiblaError
- Validation rules: heading 0-360°, alignment threshold ±2°
- State transitions documented

### Contracts

Output: [contracts/navigation-interface.md](./contracts/navigation-interface.md)
- NativeNavigationController interface
- CompassSensorManager expect/actual
- QiblaViewModel contract
- Kotlin ↔ Swift interop summary

### Quickstart

Output: [quickstart.md](./quickstart.md)
- 8-step implementation order
- Detailed Koin module creation
- SwiftUI StateFlow wrapper pattern

---

## Phase 2: Tasks

Not created by `/speckit.plan`. Use `/speckit.tasks` to generate task breakdown.

---

## Status

- ✅ Phase 0: Research complete
- ✅ Phase 1: Design complete
- ⏳ Phase 2: Awaiting `/speckit.tasks`