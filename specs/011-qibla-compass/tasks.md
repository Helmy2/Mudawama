# Tasks: Qibla Compass

**Feature**: Qibla Compass  
**Branch**: `011-qibla-compass`  
**Generated**: 2026-04-12

## Summary

- **Total Tasks**: 49
- **MVP Scope**: User Stories 1 & 2 (P1) - 32 tasks
- **Independent Testable Stories**: 5

## Phase 1: Setup (Project Initialization)

- [X] T001 Create feature/qibla/ directory structure following existing feature pattern
- [X] T002 Create feature/qibla/domain/build.gradle.kts with KMP configuration
- [X] T003 Create feature/qibla/data/build.gradle.kts with KMP configuration
- [X] T004 Create feature/qibla/presentation/build.gradle.kts with KMP configuration
- [X] T005 Add feature/qibla modules to settings.gradle.kts

## Phase 2: Domain & Data Foundation (Blocking Prerequisites)

- [X] T006 [P] Create CompassHeading data class in feature/qibla/domain/model/CompassHeading.kt
- [X] T007 [P] Create CompassAccuracy enum in feature/qibla/domain/model/CompassAccuracy.kt
- [X] T008 [P] Create QiblaState data class in feature/qibla/domain/model/QiblaState.kt
- [X] T009 [P] Create QiblaAction sealed class in feature/qibla/domain/model/QiblaAction.kt
- [X] T010 [P] Create QiblaEvent sealed class in feature/qibla/domain/model/QiblaEvent.kt
- [X] T011 [P] Create QiblaError sealed class in feature/qibla/domain/model/QiblaError.kt
- [X] T012 Implement CalculateQiblaAngleUseCase in feature/qibla/domain/usecase/CalculateQiblaAngleUseCase.kt using Haversine formula with MeccaCoordinates (21.4225° N, 39.8262° E)
- [X] T013 [P] Create expect class CompassSensorManager in feature/qibla/data/sensor/CompassSensorManager.kt
- [X] T014 Implement Android actual CompassSensorManager in feature/qibla/data/sensor/androidMain/ using TYPE_ROTATION_VECTOR
- [X] T015 Implement iOS actual CompassSensorManager in feature/qibla/data/sensor/iosMain/ using CLLocationManager
- [X] T016 Add string resources to shared/designsystem/src/commonMain/composeResources/values/strings.xml
  - qibla_title ("Qibla Compass"), qibla_calibration_warning ("Calibrate your compass"), qibla_no_location ("Location required"), qibla_go_to_settings ("Go to Settings"), qibla_aligned ("You are facing Qibla"), qibla_turn_right ("Turn %1$d° right"), qibla_turn_left ("Turn %1$d° left")

## Phase 3: US1 - Native Navigation (P1) 🎯 MVP

**Story Goal**: User taps Qibla card on Home screen → dedicated compass screen opens

**Independent Test**: Tap Qibla card → compass screen appears (iOS: SwiftUI, Android: Compose)

- [X] T017 [P] Define NativeNavigationController interface in feature/qibla/presentation/navigation/NativeNavigationController.kt
- [X] T018 Implement QiblaNavigationBridge in iosApp/iosApp/NavigationBridge.swift implementing NativeNavigationController
- [X] T019 Create QiblaDataModule in feature/qibla/data/di/QiblaDataModule.kt with CompassSensorManager singleton
- [X] T020 Create QiblaPresentationModule in feature/qibla/presentation/di/QiblaPresentationModule.kt with NativeNavigationController (ViewModel added in T026)
- [X] T021 Register Qibla modules in main App Koin setup (AppModule.kt or MainKoinContext.kt)
- [X] T022 Add NavigateToQibla case to HomeUiEvent.Navigate in feature/home/presentation/
- [X] T023 Add onNavigateToQibla: () -> Unit parameter to HomeScreen composable
- [X] T024 Pass callback from MudawamaAppShell to HomeScreen and wire to NativeNavigationController.navigateToQibla()

## Phase 4: US2 - Real-time Compass Rotation (P1) 🎯 MVP

**Story Goal**: Compass dial rotates smoothly at 60-120fps as user physically turns phone

**Independent Test**: Rotate phone → compass needle updates smoothly, points to Mecca direction

- [X] T025 Implement QiblaViewModel in feature/qibla/presentation/viewmodel/QiblaViewModel.kt: combine ObserveSettingsUseCase (to check GPS/Manual mode) and LocationProvider (to get coordinates), then call CalculateQiblaAngleUseCase
- [X] T026 Wire QiblaViewModel in QiblaPresentationModule with all dependencies
- [X] T027 [P] Create QiblaStateObservable wrapper in iosApp/iosApp/QiblaStateObservable.swift for SwiftUI observation of Kotlin StateFlow
- [X] T028 Implement QiblaView SwiftUI in iosApp/iosApp/QiblaView.swift with CompassDial and QiblaNeedle components
- [X] T029 Update NavigationBridge to retrieve QiblaViewModel from Koin (via helper function or direct Koin.get()) and pass to QiblaView
- [X] T030 Implement QiblaScreen Compose UI in feature/qibla/presentation/screen/QiblaScreen.kt with rotating dial and needle observing QiblaViewModel

## Phase 5: US4 - Sensor Calibration Warning (P1)

**Story Goal**: Calibration warning appears when sensor accuracy is Low or Unreliable

**Independent Test**: Simulate low accuracy → warning UI displayed

- [X] T031 [P] Add accuracy field observation in QiblaViewModel state (already included in QiblaState)
- [X] T032 Add AccuracyWarning UI component in iosApp/iosApp/QiblaView.swift
- [X] T033 Add calibration warning UI in Android QiblaScreen.kt

## Phase 6: US3 - Haptic Feedback on Alignment (P2)

**Story Goal**: Haptic triggers when compass aligns with Qibla (±2°)

**Independent Test**: Rotate to align → feel vibration, see green highlight

- [X] T034 [P] Implement isAligned calculation in QiblaViewModel: abs(heading - qiblaAngle) <= 2 (track previous state for transition detection)
- [X] T035 Add UIImpactFeedbackGenerator in QiblaView SwiftUI, triggered on isAligned transition false→true
- [X] T036 Add green highlight to QiblaNeedle in SwiftUI when isAligned is true
- [X] T037 Add HapticFeedback in Android QiblaScreen.kt triggered on isAligned transition false→true (use LocalHapticFeedback or Vibrator)

## Phase 7: US5 - Location Fallback (P2)

**Story Goal**: Empty state with "Go to Settings" button when no location available

**Independent Test**: Clear location → open compass → see empty state with button

- [X] T038 [P] Add hasLocation field to QiblaState (already included)
- [X] T039 Add location check logic in QiblaViewModel on start: check LocationProvider.hasPermission() and observe LocationMode from settings
- [X] T040 Add EmptyState UI in iosApp/iosApp/QiblaView.swift with "Go to Settings" button
- [X] T041 Add EmptyState UI in Android QiblaScreen.kt with "Go to Settings" button
- [X] T042 Wire "Go to Settings" button to emit QiblaEvent.NavigateToSettings from QiblaViewModel. Handle in QiblaView/QiblaScreen by navigating to Settings via NativeNavigationController or direct navigation.

## Phase 8: Polish & Cross-Cutting

- [X] T043 [P] Verify all strings use stringResource(Res.string.*) in Compose code
- [X] T044 Verify Android QiblaScreen mirrors iOS QiblaView functionality (needle, alignment, haptics)
- [ ] T045 Test compass rotation smoothness target (60-120fps)

---

## Dependencies

```
Phase 1 (Setup)
    ↓
Phase 2 (Domain & Data Foundation) ← requires Phase 1
    ↓
Phase 3 (US1 - Native Navigation) ← requires Phase 2
    ↓
Phase 4 (US2 - Real-time Compass UI) ← requires Phase 3
    ↓
Phase 5 (US4 - Calibration) ← requires Phase 4 (uses ViewModel)
    ↓
Phase 6 (US3 - Haptics) ← requires Phase 4 (uses isAligned)
    ↓
Phase 7 (US5 - Location Fallback) ← requires Phase 4 (uses QiblaState)
    ↓
Phase 8 (Polish)
```

## Parallel Opportunities

| Task IDs | Reason |
|----------|--------|
| T006-T011 | Domain model classes (no inter-deps) |
| T013-T015 | CompassSensorManager expect + Android + iOS |
| T017-T018 | NativeNavigationController interface + iOS impl |
| T027-T029 | iOS UI components |
| T031-T033 | Calibration warning (US4) |
| T034-T037 | Haptic feedback (US3, iOS + Android) |
| T038-T042 | Location fallback (US5) |
| T043-T045 | Polish tasks |

## Implementation Strategy

**MVP First** (US1 + US2 - P1 stories): Tasks T001-T030
- Complete Phase 1: Setup
- Complete Phase 2: Domain & Data Foundation
- Complete Phase 3: Native Navigation (US1)
- Complete Phase 4: Real-time Compass (US2) - iOS SwiftUI + Android Compose

**Incremental Delivery**:
- After MVP: Phase 5 (US4 - Calibration Warning)
- After US4: Phase 6 (US3 - Haptic Feedback) 
- After US3: Phase 7 (US5 - Location Fallback)
- Final: Phase 8 (Polish)

## Independent Test Criteria

| User Story | Test Criteria |
|------------|---------------|
| US1 - Native Navigation | Tap Qibla card → compass screen opens (iOS: SwiftUI, Android: Compose) |
| US2 - Real-time Compass | Rotate phone → compass needle updates at 60-120fps, points to Mecca |
| US3 - Haptic Feedback | Align within ±2° → vibration triggers on both platforms, green highlight appears |
| US4 - Calibration Warning | Sensor accuracy Low/Unreliable → warning UI displayed on both platforms |
| US5 - Location Fallback | No location → empty state with "Go to Settings" button on both platforms |