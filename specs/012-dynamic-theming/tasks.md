# Task List: Android Dynamic Theming (012-dynamic-theming)

## Phase 1: Setup (Project Initialization)
- [X] T001 Add `use_dynamic_theme` preference key to `AppPreferencesKeys` (shared/core/data)
- [X] T002 [P] Create `expect` theme bridge interface shared/designsystem/src/commonMain/kotlin/mudawama/shared/designsystem/theme/ThemeBridge.kt

## Phase 2: Foundational (Blocking)
- [X] T003 Update `SettingsRepository` to expose `useDynamicTheme` flow feature/settings/data/src/commonMain/kotlin/mudawama/feature/settings/data/SettingsRepository.kt

## Phase 3: User Story 1 - Dynamic Theming on Android 12+ [US1]
- [X] T004 [US1] Implement `actual` theme provider for Android (API 31+) shared/designsystem/src/androidMain/kotlin/mudawama/shared/designsystem/theme/ThemeBridge.kt
- [X] T005 [US1] Implement `actual` theme provider for iOS (fallback) shared/designsystem/src/iosMain/kotlin/mudawama/shared/designsystem/theme/ThemeBridge.kt
- [X] T006 [US1] Update `MudawamaTheme` to accept `useDynamicTheme` and apply dynamic colors shared/designsystem/src/commonMain/kotlin/mudawama/shared/designsystem/theme/MudawamaTheme.kt
- [X] T006b [US1] Update `MudawamaAppShell` to collect dynamic theme preference and pass to `MudawamaTheme` (shared/navigation or root)

## Phase 4: User Story 2 - Toggle Dynamic Theming in Settings [US2]
- [X] T007 [US2] Update `SettingsViewModel` to handle dynamic theme toggle actions feature/settings/presentation/src/commonMain/kotlin/mudawama/feature/settings/presentation/SettingsViewModel.kt
- [X] T008 [US2] Add dynamic theme toggle switch to `SettingsScreen` (conditionally hidden) feature/settings/presentation/src/commonMain/kotlin/mudawama/feature/settings/presentation/SettingsScreen.kt

## Phase 5: User Story 3 - Fallback Verification [US3]
- [X] T009 [US3] Verify graceful fallback to brand colors on iOS and older Android

---

## Dependencies
- US1 (Theme Integration) depends on Setup (T001, T002) and Foundational (T003, T004).
- US2 (Settings Toggle) depends on US1 and T003 (Repository update).
- US3 (Fallback Verification) depends on US1.

## Parallel Execution Examples
- **Phase 1**: Setup DataStore keys (T001) and Theme interface (T002) can run in parallel.
- **Phase 3**: Android (T004) and iOS (T005) implementations can run in parallel.

## Independent Test Criteria
- US1: Launch app on Android 12+ (dynamic) and iOS (brand).
- US2: Toggle setting on Android 12+, verify color change.
- US3: Launch on Android 10, verify Settings toggle is hidden and colors are static brand.

## Implementation Strategy
- Focus on implementing the `expect/actual` bridge first as it unblocks UI integration.
- MVP scope: US1 (Dynamic Theming on Android 12+) and initial repository wiring (T006, T006b). Settings UI (US2) can follow.
