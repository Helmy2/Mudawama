# Tasks: Full Native iOS App (SwiftUI)

**Input**: Design documents from `specs/013-ios-native-ui/`  
**Branch**: `013-ios-native-ui`  
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/ ✅ quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.  
**Tests**: Not explicitly requested — no test tasks generated.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no blocking dependency on an incomplete task)
- **[Story]**: User story this task belongs to (US1–US6)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Gradle + Xcode wiring. Must complete before any Swift screen work begins.

- [x] T001 Add `alias(libs.plugins.skie)` to `plugins {}` block in `shared/umbrella-core/build.gradle.kts`
- [x] T002 Expand `configureIosFramework("MudawamaCore", isStatic = true)` in `shared/umbrella-core/build.gradle.kts` to `export()` all feature domain + data modules (habits, prayer, quran, athkar, settings, qibla) and core modules (core.domain, core.data, core.time) per `contracts/kotlin-exports.md`
- [x] T003 Add matching `api()` declarations in `commonMain.dependencies` for every module added to `export()` in T002 — keep `implementation(projects.shared.core.database)` as-is in `shared/umbrella-core/build.gradle.kts`
- [x] T004 Verify Gradle sync succeeds: run `./gradlew :shared:umbrella-core:linkDebugFrameworkIosSimulatorArm64` — fix any "exported dependency must also be api dependency" errors
- [x] T005 In Xcode project `iosApp/iosApp.xcodeproj`: remove `MudawamaUI.framework` from Link Binary With Libraries; add `MudawamaCore.framework` built by T004
- [x] T006 Verify `./gradlew :androidApp:assembleDebug` still passes with zero changes to any Android source file

**Checkpoint**: `MudawamaCore` framework builds and links in Xcode. Android build is green.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Kotlin-side Koin helpers + iOS app entry point. MUST be complete before any feature screen can compile.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T007 Create `shared/umbrella-core/src/iosMain/kotlin/di/IosKoinHelpers.kt` with all 8 `KoinComponent` provider classes: `HomeUseCaseProvider`, `PrayerUseCaseProvider`, `QuranUseCaseProvider`, `AthkarUseCaseProvider`, `TasbeehUseCaseProvider`, `HabitsUseCaseProvider`, `SettingsUseCaseProvider`, `QiblaUseCaseProvider` — each exposing the use cases listed in `data-model.md §4`
- [x] T008 Create `shared/umbrella-core/src/iosMain/kotlin/KoinInitializer.kt` with `fun initializeKoin(iosEncryptor, iosLocationProvider, iosNotificationProvider)` — registers all feature domain + data Koin modules (see `contracts/kotlin-exports.md` iOS Koin Initialiser section); remove the `iosQiblaViewControllerProvider` parameter
- [x] T009 Run `./gradlew :shared:umbrella-core:compileKotlinIosSimulatorArm64` — must succeed with no unresolved references in T007/T008
- [x] T010 [P] Update `iosApp/iosApp/IosEncryptor.swift`: change `import MudawamaUI` → `import MudawamaCore`
- [x] T011 [P] Update `iosApp/iosApp/IosLocationProvider.swift`: change `import MudawamaUI` → `import MudawamaCore`
- [x] T012 [P] Update `iosApp/iosApp/IosNotificationProvider.swift`: change `import MudawamaUI` → `import MudawamaCore`
- [x] T013 Delete `iosApp/iosApp/IosQiblaViewControllerProvider.swift` — superseded by native QiblaView (US6); `ContentView.swift` also deleted (Compose bridge no longer needed)
- [x] T014 Replace `iosApp/iosApp/iOSApp.swift` entirely: change import to `MudawamaCore`, call the new `KoinInitializerKt.initializeKoin(iosEncryptor:iosLocationProvider:iosNotificationProvider:)` (3 params, no qibla provider), set body to `RootNavigationView()`
- [x] T015 Create `iosApp/iosApp/Navigation/RootNavigationView.swift` as a stub with 4-tab `TabView` referencing placeholder views (`HomeView`, `PrayerView`, `QuranView`, `AthkarView`) — each as an empty `struct` in the same file temporarily so Xcode compiles
- [x] T016 Create `iosApp/iosApp/Strings/LocalizedKey.swift` with the `extension String { static func loc(_ key: String) -> String }` helper
- [x] T017 Create `iosApp/iosApp/Strings/Localizable.xcstrings` (Xcode String Catalog) with navigation keys: `nav_home`, `nav_prayers`, `nav_quran`, `nav_athkar`, `nav_habits`, `nav_tasbeeh`, `nav_settings`, `nav_qibla` in English and Arabic
- [x] T018 Verify Xcode builds and launches on Simulator with 4 empty tabs and no `MudawamaUI` linkage — confirm `MudawamaCore` is the linked KMP framework (requires T005 manual Xcode step first)

**Checkpoint**: App launches from `RootNavigationView` with 4 empty tabs. All Swift files import `MudawamaCore`. Kotlin compiles cleanly.

---

## Phase 3: User Story 1 — Fully Native iOS Experience (Priority: P1) 🎯 MVP

**Goal**: All 8 screens render as SwiftUI views. No Compose canvas present. Dark Mode, Dynamic Type, and native gestures all work.

**Independent Test**: Install on iOS Simulator; navigate all tabs and push destinations; verify every screen is SwiftUI (no `UIViewControllerRepresentable` wrapping Compose); toggle Dark Mode in Settings app and verify all screens adapt.

- [x] T019 [US1] Create `iosApp/iosApp/DesignSystem/MudawamaTheme.swift` — define iOS color tokens (teal primary, surface/background, adaptive dark mode via `.secondarySystemGroupedBackground`); also defines `MudawamaSurfaceCard` component
- [x] T020 [P] [US1] Create `iosApp/iosApp/Features/Home/HomeView.swift` as a full-skeleton SwiftUI `View` (scrollable VStack with placeholder cards for next prayer, athkar, quran, tasbeeh, habits sections)
- [x] T021 [P] [US1] Create `iosApp/iosApp/Features/Prayer/PrayerView.swift` as a skeleton with date strip placeholder and 5 prayer rows
- [x] T022 [P] [US1] Create `iosApp/iosApp/Features/Quran/QuranView.swift` as a skeleton with progress ring, streak, bookmark, and Log Reading button
- [x] T023 [P] [US1] Create `iosApp/iosApp/Features/Athkar/AthkarView.swift` as a skeleton with segmented picker and group list
- [x] T024 [P] [US1] Create `iosApp/iosApp/Features/Habits/HabitsView.swift` as a skeleton with habit list rows and new-habit toolbar button
- [x] T025 [P] [US1] Create `iosApp/iosApp/Features/Tasbeeh/TasbeehView.swift` as a skeleton with counter ring and tap button
- [x] T026 [P] [US1] Create `iosApp/iosApp/Features/Settings/SettingsView.swift` as a skeleton Form with prayer, appearance, and notifications sections
- [x] T027 [P] [US1] Create `iosApp/iosApp/Features/Qibla/QiblaView.swift` as a skeleton compass screen (circular placeholder, needle icon, degree label)
- [x] T028 [US1] Update `iosApp/iosApp/Navigation/RootNavigationView.swift`: wire 4 real tab views with SF Symbol icons; tint set to teal primary; Navigation links to push destinations will be added in T057–T058 (Phase 5)
- [x] T029 [US1] Add all screen-title and common UI string keys to `iosApp/iosApp/Strings/Localizable.xcstrings` — 60+ keys covering all 8 screens, common actions, errors, notifications in English and Arabic
- [x] T030 [US1] Verify on Simulator: all 8 screens are navigable, Dark Mode switches colors correctly via `MudawamaTheme`, swipe-back works on all push destinations, tab bar hidden when pushing Habits/Tasbeeh/Settings/Qibla (requires T005 Xcode manual step + T057–T058 nav links) — pending manual Xcode step

**Checkpoint**: Pure SwiftUI app navigable across all 8 screens. No Compose runtime. Dark Mode and Dynamic Type work. Swipe-back works everywhere.

---

## Phase 4: User Story 2 — All Existing Features Available on iOS (Priority: P1)

**Goal**: Every feature screen displays real data from Kotlin use cases via `MudawamaCore`. All create/update actions persist via Kotlin.

**Independent Test**: Each feature can be tested in isolation — open screen, verify data loads from Kotlin, perform an action (toggle, log, increment), close and reopen to confirm persistence.

### Prayer Feature (US2)

- [x] T031 [US2] Create `iosApp/iosApp/Features/Prayer/PrayerViewModel.swift`: `@MainActor ObservableObject` using `PrayerUseCaseProvider`; `observe(date:)` collects `ObservePrayersForDateUseCase` flow via `for await`; `toggleStatus(habitId:date:)` calls `TogglePrayerStatusUseCase`; `@Published prayers: [PrayerWithStatus]`, `isLoading: Bool`, `error: String?`
- [x] T032 [US2] Update `iosApp/iosApp/Features/Prayer/PrayerView.swift`: connect to `PrayerViewModel` via `@StateObject`; render prayer rows with name, time, `LogStatus` icon (SF Symbols: `checkmark.circle.fill`, `xmark.circle.fill`, `circle`); tap row calls `vm.toggleStatus`; `.task { await vm.observe(date: .today) }`; add Arabic prayer name string keys to `Localizable.xcstrings`

### Quran Feature (US2)

- [x] T033 [P] [US2] Create `iosApp/iosApp/Features/Quran/QuranViewModel.swift`: `@MainActor ObservableObject` using `QuranUseCaseProvider`; observes `ObserveQuranProgressUseCase` flow; `logReading(pages:date:)` calls `LogQuranReadingUseCase`; `@Published` progress, streak, bookmark state
- [x] T034 [P] [US2] Update `iosApp/iosApp/Features/Quran/QuranView.swift`: progress ring (pages today / goal), streak badge, bookmark position display, "Log Reading" button presenting `LogReadingSheet`; `.task` collection
- [x] T035 [P] [US2] Create `iosApp/iosApp/Features/Quran/Sheets/LogReadingSheet.swift`: sheet with page-count input, confirm button calling `vm.logReading`; add string keys to `Localizable.xcstrings`
- [x] T036 [P] [US2] Create `iosApp/iosApp/Features/Quran/Sheets/QuranGoalSheet.swift`: sheet to set daily page goal; add string keys to `Localizable.xcstrings`
- [x] T037 [P] [US2] Create `iosApp/iosApp/Features/Quran/Sheets/QuranPositionSheet.swift`: sheet to update Surah/Ayah bookmark position; add string keys to `Localizable.xcstrings`

### Athkar Feature (US2)

- [x] T038 [P] [US2] Create `iosApp/iosApp/Features/Athkar/AthkarViewModel.swift`: observes `ObserveAthkarGroupsUseCase` for MORNING, EVENING, POST_PRAYER types; `markComplete(groupId:date:)` action; segmented picker state for type selection
- [x] T039 [P] [US2] Update `iosApp/iosApp/Features/Athkar/AthkarView.swift`: segmented control (Morning / Evening / Post-Prayer tabs); group list with completion indicator; tap to enter group reading session (`AthkarGroupView`); `.task` collection
- [x] T040 [P] [US2] Create `iosApp/iosApp/Features/Athkar/AthkarGroupView.swift`: tap-to-count individual dhikr items matching `docs/ui/morning_athkar_reading.png` and `docs/ui/post_prayer_athkar.png`; add string keys to `Localizable.xcstrings`

### Tasbeeh Feature (US2)

- [x] T041 [P] [US2] Create `iosApp/iosApp/Features/Tasbeeh/TasbeehViewModel.swift`: observes `ObserveTasbeehGoalUseCase` + `ObserveTasbeehDailyTotalUseCase`; `increment(date:)` action with haptic feedback (`UIImpactFeedbackGenerator`); `setGoal(count:)` action
- [x] T042 [P] [US2] Update `iosApp/iosApp/Features/Tasbeeh/TasbeehView.swift`: large counter button, daily total / goal display, goal ring; presents `TasbeehGoalSheet`; `.task` collection
- [x] T043 [P] [US2] Create `iosApp/iosApp/Features/Tasbeeh/TasbeehGoalSheet.swift`: numeric input for daily goal; matches `docs/ui/tasbeeh_goal_bottom_sheet.png`; add string keys to `Localizable.xcstrings`

### Habits Feature (US2)

- [x] T044 [P] [US2] Create `iosApp/iosApp/Features/Habits/HabitsViewModel.swift`: observes `ObserveHabitsWithLogsUseCase`; `toggleLog(habitId:date:)` action; `addHabit(...)` and `deleteHabit(...)` actions
- [x] T045 [P] [US2] Update `iosApp/iosApp/Features/Habits/HabitsView.swift`: habit list with toggle (BOOLEAN) or stepper (NUMERIC); swipe-to-delete; add habit FAB presenting `NewHabitSheet`; `.task` collection; matches `docs/ui/daily_habits.png`
- [x] T046 [P] [US2] Create `iosApp/iosApp/Features/Habits/NewHabitSheet.swift`: name field, type picker (Boolean/Numeric), target count field for Numeric type; matches `docs/ui/new_habit_bottom_sheet.png`; add string keys to `Localizable.xcstrings`
- [x] T047 [P] [US2] Create `iosApp/iosApp/Features/Habits/ManageHabitSheet.swift`: edit/delete existing habit; matches `docs/ui/manage_habit_bottom_sheet.png`; add string keys to `Localizable.xcstrings`

### Settings Feature (US2)

- [x] T048 [P] [US2] Create `iosApp/iosApp/Features/Settings/SettingsViewModel.swift`: observes `ObserveSettingsUseCase`; exposes setter actions for all `AppSettings` fields; `@Published settings: AppSettings`
- [x] T049 [P] [US2] Update `iosApp/iosApp/Features/Settings/SettingsView.swift`: calculation method picker, location mode toggle, theme picker, language picker, morning/evening notification toggles with time pickers; matches `docs/ui/settings.png`; all actions call `vm.setXxx` suspend use cases; add string keys to `Localizable.xcstrings`

### Home Dashboard (US2)

- [x] T050 [US2] Create `iosApp/iosApp/Features/Home/HomeViewModel.swift`: `@MainActor ObservableObject` using `HomeUseCaseProvider`; combines 5 parallel flows (next prayer, athkar summary, quran progress, tasbeeh total, habits summary) via `async let` or `TaskGroup`; `@Published homeState: HomeUiState`
- [x] T051 [US2] Update `iosApp/iosApp/Features/Home/HomeView.swift`: `NextPrayerCard` (full-width), `AthkarSummaryCard`, `QuranProgressCard`, `TasbeehSummaryCard` (2-column row), `HabitsSummarySection`; each card taps to navigate to its feature; `.task { await vm.observe() }`; matches `docs/ui/home_dashboard.png`
- [x] T052 [US2] Create `iosApp/iosApp/Features/Home/Components/NextPrayerCard.swift` — full-width card showing next prayer name + time + countdown; add string keys to `Localizable.xcstrings`
- [x] T053 [P] [US2] Create `iosApp/iosApp/Features/Home/Components/AthkarSummaryCard.swift` — morning/evening completion dots
- [x] T054 [P] [US2] Create `iosApp/iosApp/Features/Home/Components/QuranProgressCard.swift` — pages today vs goal mini ring
- [x] T055 [P] [US2] Create `iosApp/iosApp/Features/Home/Components/TasbeehSummaryCard.swift` — count today vs goal
- [x] T056 [P] [US2] Create `iosApp/iosApp/Features/Home/Components/HabitsSummarySection.swift` — compact habit completion row

**Checkpoint**: Every feature screen displays real Kotlin data. Actions persist across app restarts. Feature parity with Android confirmed screen-by-screen against `docs/ui/` references.

---

## Phase 5: User Story 3 — Native iOS Navigation (Priority: P1)

**Goal**: SwiftUI navigation is fully native — tab switching, swipe-back, `NavigationStack`, sheets. No Kotlin back-stack.

**Independent Test**: Navigate to every screen; swipe-back from every push destination; switch tabs rapidly; open and dismiss sheets. All transitions are native iOS animations with no jank.

- [x] T057 [US3] Refine `iosApp/iosApp/Navigation/RootNavigationView.swift`: add `NavigationPath` state per tab for programmatic navigation; ensure tab bar is hidden when inside `HabitsView`, `TasbeehView`, `QiblaView`, `SettingsView` using `.toolbar(.hidden, for: .tabBar)` (iOS 16+) with iOS 15 fallback
- [x] T058 [US3] Add `NavigationLink` entries from `HomeView` → `HabitsView`, `TasbeehView`, `QiblaView`; add Settings `NavigationLink` in toolbar of each top-level tab; verify swipe-back dismisses each push destination correctly
- [x] T059 [US3] Ensure all sheet presentations (`LogReadingSheet`, `NewHabitSheet`, `ManageHabitSheet`, `TasbeehGoalSheet`, `QuranGoalSheet`, `QuranPositionSheet`) use SwiftUI `.sheet()` modifier — not custom overlays
- [x] T060 [US3] Verify native swipe-back gesture works on all push destinations and does not trigger any Kotlin `goHome()` call — confirm `AppBackHandler` in KMP modules has no iOS effect (it is a no-op actual)
- [x] T061 [US3] Add date navigation strip to `PrayerView` and `QuranView` using a native SwiftUI horizontal `ScrollView` of date chips (matching `DateStrip` behavior from the Compose implementation); past-date navigation shows read-only state

**Checkpoint**: All navigation is native SwiftUI. Swipe-back, tab switching, and sheet presentations work with zero Kotlin involvement.

---

## Phase 6: User Story 4 — Kotlin Domain via MudawamaCore (Priority: P1)

**Goal**: Confirm the architectural constraint — only `MudawamaCore` is linked; no business logic in Swift.

**Independent Test**: Inspect the linked binary (`otool -L` on the built `.app`); confirm no `MudawamaUI` framework present; audit all Swift ViewModels to ensure zero business logic (all calculations delegate to Kotlin use cases).

- [x] T062 [US4] Run `otool -L iosApp/build/Products/Debug-iphonesimulator/iosApp.app/iosApp` and confirm `MudawamaCore` is present and `MudawamaUI` is absent — document result in a one-line comment at top of `iosApp/iosApp/iOSApp.swift` (requires T005 manual Xcode step; binary not yet built)
- [x] T063 [US4] Audit all `*ViewModel.swift` files: verify no business logic (no date arithmetic, no streak calculation, no prayer-time math, no angle trigonometry) — all delegated to Kotlin use cases; fix any violations found
- [x] T064 [US4] Verify that Kotlin `Result<D,E>` success/failure pattern matching works correctly in at least 3 ViewModels (Prayer, Quran, Settings) — add `errorKey(_ error: DomainError) -> String` helper to `iosApp/iosApp/Strings/ErrorKeys.swift` mapping domain errors to `Localizable.xcstrings` keys
- [x] T065 [US4] Verify Room database persistence: kill app, relaunch, confirm Quran log / Tasbeeh total / prayer status are intact — data survives through Kotlin Room (no Swift-side caching) — pending manual device test

**Checkpoint**: Binary audit passes. Zero business logic in Swift. Room persistence confirmed across cold starts.

---

## Phase 7: User Story 5 — Notifications and Platform Permissions (Priority: P2)

**Goal**: Athkar notifications fire at scheduled times. Location permission flows work correctly for prayer times and Qibla.

**Independent Test**: Enable Athkar morning notification in Settings, background the app, wait for scheduled time; verify notification fires. Grant/deny location permission; verify prayer screen handles both states.

- [x] T066 [US5] Verify `IosNotificationProvider` compiles and functions against `MudawamaCore` (import changed in T012) — trigger a test notification from Settings screen to confirm the Kotlin `NotificationScheduler` → Swift `UNCalendarNotificationTrigger` pipeline works
- [x] T067 [US5] Add Athkar notification settings UI to `iosApp/iosApp/Features/Settings/SettingsView.swift`: morning/evening enabled toggles + time pickers (hour/minute) calling `vm.setMorningNotif` / `vm.setEveningNotif` — verify persisted via Kotlin DataStore
- [x] T068 [US5] Add location permission request flow to `PrayerView`: if location permission not granted, show permission prompt view with "Enable Location" button calling `IosLocationProvider.hasPermission()` / request flow; on grant, trigger prayer time load; on deny, show fallback error UI using `error_location` string key
- [x] T069 [US5] Add location permission request flow to `QiblaView` (same pattern as T068) — permission denial shows "Go to Settings" button opening `UIApplication.shared.open(settingsUrl)`
- [x] T070 [US5] Add `NSLocationWhenInUseUsageDescription` to `iosApp/iosApp/Info.plist` (verify already present; update copy to match app's Arabic/English context if needed)

**Checkpoint**: Notifications fire on device. Location permission grant/deny both have correct UI flows in Prayer and Qibla screens.

---

## Phase 8: User Story 6 — iOS-Specific UX Enhancements (Priority: P3)

**Goal**: Native haptics, SF Symbols, native sheet presentations, and Arabic RTL layout.

**Independent Test**: Test each enhancement in isolation on a physical device (haptics) or Simulator (SF Symbols, RTL).

- [x] T071 [US6] Implement full native Qibla compass in `iosApp/iosApp/Features/Qibla/QiblaView.swift`: `CLLocationManager` continuous heading updates via `CLLocationManagerDelegate` (object delegation pattern, not NSObject subclass in Kotlin); calculate bearing using `QiblaUseCaseProvider.calculateAngle`; animate compass needle with `.rotationEffect`; haptic feedback (`UIImpactFeedbackGenerator(.medium)`) when aligned within ±2°; calibration warning for LOW/UNRELIABLE accuracy
- [x] T072 [US6] Create `iosApp/iosApp/Features/Qibla/QiblaViewModel.swift`: manages `CLLocationManager`, calls `CalculateQiblaAngleUseCase`, publishes `QiblaUiState` (loading, active with degrees, error)
- [x] T073 [US6] Verify all icons in the app use SF Symbols — audit `HomeView`, `PrayerView`, `AthkarView`, `TasbeehView`, navigation tab items; replace any placeholder text icons with appropriate SF Symbol names
- [x] T074 [US6] Verify Arabic RTL layout: switch iOS Simulator to Arabic locale; confirm `Text` views, `HStack` directions, and list rows all mirror correctly (SwiftUI handles RTL automatically; fix any `HStack` that uses explicit `leading`/`trailing` instead of `leading` edge-relative)
- [x] T075 [P] [US6] Create `iosApp/iosApp/Features/Athkar/AthkarNotificationSheet.swift`: notification settings for morning/evening Athkar reminders accessible from `AthkarView` toolbar; duplicates Settings notification section for quick access; add string keys to `Localizable.xcstrings`

**Checkpoint**: Qibla compass rotates smoothly with haptics. All icons are SF Symbols. Arabic locale displays correctly RTL.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Error states, loading states, empty states, and final UI polish across all screens.

- [x] T076 [P] Add loading `ProgressView` placeholder to all 8 feature screens for the initial load state (when `isLoading == true`) — consistent with `MudawamaTheme` teal tint
- [x] T077 [P] Add error state views to all 8 feature screens: display localised error message from `ErrorKeys.swift`; include retry button that re-triggers the observe flow
- [x] T078 [P] Add empty state views: Prayer (no prayers seeded yet — trigger `seedPrayerHabits()`), Habits (no habits yet — show add-habit prompt), Quran (no log yet — show log-reading prompt)
- [x] T079 Add pull-to-refresh to `PrayerView` (refetches prayer times from Ktor API) and `QuranView` (refetches surah data if needed) using SwiftUI `.refreshable {}`
- [x] T080 Ensure all `NavigationStack` titles use `navigationTitle(String.loc(...))` — no hardcoded English strings anywhere in Swift source
- [x] T081 Complete `iosApp/iosApp/Localizable.xcstrings`: audit all `String.loc(...)` call sites across all Swift files; add any missing keys with English and Arabic translations
- [x] T082 Run full quickstart.md Step 8 verification: `./gradlew :shared:umbrella-core:linkReleaseFrameworkIosArm64`, `./gradlew :androidApp:assembleDebug`, and `xcodebuild` release build — all three must succeed
- [ ] T083 Manual UI verification against every `docs/ui/` reference PNG: confirm layout, labels, and copy match across all 8 screens in both English and Arabic locales

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 completion — **BLOCKS all user story phases**
- **Phase 3 (US1 — Native UI)**: Depends on Phase 2 — can start once Foundational completes
- **Phase 4 (US2 — Feature Data)**: Depends on Phase 3 (screens must exist as skeletons before wiring data)
- **Phase 5 (US3 — Navigation)**: Can run in parallel with Phase 4 (different files)
- **Phase 6 (US4 — Architecture Audit)**: Depends on Phase 4 (all ViewModels must exist)
- **Phase 7 (US5 — Notifications/Permissions)**: Depends on Phase 2; can run in parallel with Phases 4–5
- **Phase 8 (US6 — UX Enhancements)**: Depends on Phase 3 (screen skeletons); Qibla (T071-T072) depends on Phase 4
- **Phase 9 (Polish)**: Depends on all Phases 3–8 being complete

### User Story Dependencies

| Story | Depends On | Can Parallelize With |
|---|---|---|
| US1 (Native UI) | Phase 2 complete | — |
| US2 (Feature Data) | US1 screen skeletons | US3, US5 |
| US3 (Navigation) | US1 screen skeletons | US2, US5 |
| US4 (Architecture) | US2 (all ViewModels exist) | US5 |
| US5 (Notifications) | Phase 2 (Koin ready) | US2, US3 |
| US6 (UX Polish) | US1 (screens exist) | US2, US3, US5 |

### Within Each Phase (Parallel Opportunities)

- **Phase 1**: T001–T003 parallel (different files); T004 sequential; T005–T006 parallel
- **Phase 2**: T010, T011, T012, T013 all parallel; T007–T009 sequential first
- **Phase 3**: T020–T027 (all 8 screen skeletons) fully parallel
- **Phase 4**: Prayer (T031–T032), Quran (T033–T037), Athkar (T038–T040), Tasbeeh (T041–T043), Habits (T044–T047), Settings (T048–T049) all parallel; Home (T050–T056) after others
- **Phase 8**: T071–T072 (Qibla) sequential; T073–T075 parallel

---

## Parallel Example: Phase 3 (Screen Skeletons)

All 8 screen skeleton tasks can be launched simultaneously by different agents:

```
Task A: T020 — HomeView.swift skeleton
Task B: T021 — PrayerView.swift skeleton
Task C: T022 — QuranView.swift skeleton
Task D: T023 — AthkarView.swift skeleton
Task E: T024 — HabitsView.swift skeleton
Task F: T025 — TasbeehView.swift skeleton
Task G: T026 — SettingsView.swift skeleton
Task H: T027 — QiblaView.swift skeleton
```

## Parallel Example: Phase 4 (Feature ViewModels)

Six feature ViewModel+View pairs can be implemented simultaneously:

```
Task A: T031–T032 — Prayer ViewModel + View
Task B: T033–T037 — Quran ViewModel + View + 3 sheets
Task C: T038–T040 — Athkar ViewModel + View + GroupView
Task D: T041–T043 — Tasbeeh ViewModel + View + GoalSheet
Task E: T044–T047 — Habits ViewModel + View + 2 sheets
Task F: T048–T049 — Settings ViewModel + View
# Home (T050–T056) after others — aggregates all features
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup (Gradle + Xcode wiring)
2. Complete Phase 2: Foundational (Kotlin helpers + iOS entry point)
3. Complete Phase 3: US1 — native screen skeletons, navigation shell
4. Complete Phase 4: US2 — real data in all screens
5. **STOP and VALIDATE**: App is fully functional with real data on iOS
6. Demo/release if ready — this is a shippable native iOS app

### Full Delivery (All Stories)

1. Phases 1–4 → functional native app
2. Phase 5 (US3) → navigation polish (can overlap with Phase 4)
3. Phase 6 (US4) → architecture audit
4. Phase 7 (US5) → notifications + permissions
5. Phase 8 (US6) → Qibla + RTL + haptics
6. Phase 9 → Polish + final verification

### Parallel Team Strategy

With 2+ developers after Phase 2 completes:

- **Dev A**: Phase 3 screen skeletons → Phase 4 Prayer + Quran + Home
- **Dev B**: Phase 4 Athkar + Tasbeeh + Habits + Settings → Phase 5 navigation polish
- **Dev C**: Phase 7 notifications + permissions → Phase 8 Qibla

---

## Notes

- All `String.loc(...)` call sites must have a matching key in `Localizable.xcstrings` before PR — missing keys silently return the key name as fallback
- `SkieSwiftStateFlow.value` reads the current Kotlin StateFlow value synchronously — always read it before entering `for await` to avoid a blank first render frame
- `@MainActor` on every ViewModel class is required — `@Published` mutations from background threads will crash on iOS 17+
- Kotlin `suspend` functions bridged by SKIE may throw; always call them inside `try? await` or `do { try await ... } catch { }` in Swift
- The `MudawamaUI` framework and `:shared:umbrella-ui` Gradle module are never touched — Android must remain green after every commit on this branch
