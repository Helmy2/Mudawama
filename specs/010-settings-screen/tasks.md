# Tasks: Settings Screen

**Feature**: 010-settings-screen  
**Generated**: 2026-04-11

## Phase 1: Setup

- [X] T001 Create settings feature module structure: `feature/settings/` with `domain/`, `data/`, `presentation/` subdirectories
- [X] T003 Add dependencies to `feature/settings/data/build.gradle.kts`: feature:settings:domain, androidx.datastore, kotlinx-serialization-json
- [X] T004 Add dependencies to `feature/settings/presentation/build.gradle.kts`: feature:settings:domain, feature:quran:domain, koin

## Phase 2: Foundational

- [X] T005 Create domain enums in `feature/settings/domain/src/commonMain/kotlin/.../CalculationMethod.kt`
- [X] T006 Create domain enums in `feature/settings/domain/src/commonMain/kotlin/.../AppTheme.kt`
- [X] T007 Create domain enums in `feature/settings/domain/src/commonMain/kotlin/.../AppLanguage.kt`
- [X] T008 Create LocationMode sealed class in `feature/settings/domain/src/commonMain/kotlin/.../LocationMode.kt`
- [X] T009 Create AppSettings data class in `feature/settings/domain/src/commonMain/kotlin/.../AppSettings.kt`
- [X] T010 Create SettingsRepository interface in `feature/settings/domain/src/commonMain/kotlin/.../SettingsRepository.kt`
- [X] T011 Create ObserveSettingsUseCase in `feature/settings/domain/src/commonMain/kotlin/.../ObserveSettingsUseCase.kt`
- [X] T012 Create SetCalculationMethodUseCase in `feature/settings/domain/src/commonMain/kotlin/.../SetCalculationMethodUseCase.kt`
- [X] T013 Create SetLocationModeUseCase in `feature/settings/domain/src/commonMain/kotlin/.../SetLocationModeUseCase.kt`
- [X] T014 Create SetAppThemeUseCase in `feature/settings/domain/src/commonMain/kotlin/.../SetAppThemeUseCase.kt`
- [X] T015 Create SetAppLanguageUseCase in `feature/settings/domain/src/commonMain/kotlin/.../SetAppLanguageUseCase.kt`
- [X] T016 Create SettingsRepositoryImpl in `feature/settings/data/src/commonMain/kotlin/.../SettingsRepositoryImpl.kt`
- [X] T017 [P] Register DataStore preferences keys in SettingsRepositoryImpl: calculation_method, location_mode_is_gps, location_mode_latitude, location_mode_longitude, app_theme, app_language
- [X] T018 [P] Create MVI State/Action/Event in `feature/settings/presentation/src/commonMain/kotlin/.../SettingsState.kt`
- [X] T019 Create SettingsViewModel in `feature/settings/presentation/src/commonMain/kotlin/.../SettingsViewModel.kt`
- [X] T020 Create SettingsScreen empty scaffold in `feature/settings/presentation/src/commonMain/kotlin/.../SettingsScreen.kt` — TopAppBar with back button, scrollable column container. Sections added by subsequent tasks.
- [X] T021 Create SettingsPresentationModule Koin module in `feature/settings/presentation/src/commonMain/kotlin/.../SettingsPresentationModule.kt`
- [X] T022 Create SettingsDataModule Koin module in `feature/settings/data/src/commonMain/kotlin/.../SettingsDataModule.kt` — binds SettingsRepositoryImpl to SettingsRepository
- [X] T023 Initialize latitudeInput and longitudeInput from saved LocationMode.Manual when SettingsViewModel loads settings

## Phase 3: US1 - Prayer Calculation Method (P1) MVP

- [X] T024 [US1] Create calculation method dropdown/picker UI in SettingsScreen (section: settings_section_calculation_method)
- [X] T026 [US1] Handle SetCalculationMethod action in SettingsViewModel
- [X] T027 [US1] Connect SettingsRepository setCalculationMethod in SettingsViewModel
- [X] T028 [US2] Create location mode toggle (GPS/Manual) UI in SettingsScreen
- [X] T029 [US2] Add manual coordinate input fields with validation in SettingsScreen
- [X] T031 [US2] Handle UpdateLatitudeInput, UpdateLongitudeInput actions in SettingsViewModel
- [X] T032 [US2] Implement coordinate validation: latitude [-90, 90], longitude [-180, 180]
- [X] T033 [US2] Handle SaveManualLocation action in SettingsViewModel
- [X] T034 [US2] Handle SetLocationMode(Gps) action in SettingsViewModel
- [X] T036 [US3] Handle UpdateGoalInput action in SettingsViewModel
- [X] T037 [US3] Implement goal validation: minimum 1 page
- [X] T038 [US3] Invoke SetGoalUseCase from feature:quran:domain on SaveGoal action
- [X] T040 [US4] Create theme selector (System/Light/Dark) UI in SettingsScreen
- [X] T042 [US4] Handle SetAppTheme action in SettingsViewModel
- [X] T043 [US5] Create language selector (English/Arabic) UI in SettingsScreen
- [X] T045 [US5] Handle SetAppLanguage action in SettingsViewModel

## Phase 8: Integration

- [X] T046 Inject ObserveSettingsUseCase at MudawamaAppShell level and collect settings as State — shared prerequisite for theme and language integration (both US4 and US5)
- [ ] T047 Apply LayoutDirection.Rtl in App.kt when Arabic selected — requires T046 first
- [ ] T048 Integrate with App.kt / MudawamaAppShell for immediate theme application — requires T046 first
- [X] T049 Add feature:settings:domain as dependency to feature:prayer:presentation
- [X] T050 Add feature:settings:domain as dependency to feature:home:presentation
- [X] T051 Inject ObserveSettingsUseCase in PrayerViewModel
- [ ] T054 Use persisted CalculationMethod and LocationMode in PrayerViewModel (replace hardcoded) — requires T053 first
- [X] T053 Inject ObserveSettingsUseCase in HomeViewModel
- [ ] T056 Use persisted CalculationMethod and LocationMode in HomeViewModel (replace hardcoded)
- [X] T057 Replace SettingsScreen placeholder in shared/navigation/Placeholders.kt
- [X] T058 Register SettingsDataModule and SettingsPresentationModule in the app's Koin modules list (App.kt or KoinSetup.kt)

## Phase 9: String Resources

- [X] T059 [P] Add all English strings to shared/designsystem/src/commonMain/composeResources/values/strings.xml
- [X] T060 [P] Create Arabic translations in shared/designsystem/src/commonMain/composeResources/values-ar/strings.xml
- [ ] T061 [P] Verify ALL string keys from spec Arabic Translations Reference section

## Phase 10: Polish & Cross-Cutting

- [ ] T062 Verify Mecca fallback: (21.3891, 39.8579) only when GPS mode + GPS failure (per FR-006)
- [ ] T063 Verify Settings persist after app restart
- [ ] T064 Verify zero regression: Prayer, Athkar, Quran, Habits unchanged

---

## Summary

| Metric | Count |
|--------|-------|
| Total Tasks | 64 |
| Setup | 3 |
| Foundational | 19 |
| US1 - Calculation Method | 4 |
| US2 - Location Mode | 7 |
| US3 - Quran Goal | 5 |
| US4 - App Theme | 3 |
| US5 - Language | 3 |
| Integration | 13 |
| String Resources | 3 |
| Polish | 3 |

## MVP Scope

MVP includes US1 (Calculation Method) + US2 (Location Mode) + String Resources — tasks T001-T034 + T059-T061 form the MVP deliverable.

## Independent Test Criteria

- **US1**: Change calculation method → Prayer screen shows updated prayer times
- **US2**: Enter manual coordinates → Prayer uses those coordinates (not GPS)
- **US3**: Change Quran goal → Home screen ring shows new denominator
- **US4**: Change theme → App palette updates immediately
- **US5**: Select Arabic → UI shows Arabic strings with RTL layout

## Parallel Opportunities

- T017 (DataStore keys) can execute in parallel with T018 (MVI State/Action)
- T059 (EN strings) can execute in parallel with T060 (Arabic strings)
- T053 is a prerequisite for T054 — NOT parallel (mapper required before use)
- T046 is a shared prerequisite for both T047 and T048