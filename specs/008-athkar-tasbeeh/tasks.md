# Tasks: Athkar & Tasbeeh

**Input**: Design documents from `/specs/008-athkar-tasbeeh/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ui-contracts.md ✅
**Branch**: `008-athkar-tasbeeh`

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

---

## Phase 1: Setup (Gradle Modules) ✅ COMPLETE

**Purpose**: Create the four new Gradle modules required before any source code can compile.

- [X] T001 [P] Create `feature/athkar/domain/build.gradle.kts` — apply `mudawama.kmp` plugin; depend on `:shared:core:domain`
- [X] T002 [P] Create `feature/athkar/data/build.gradle.kts` — apply `mudawama.kmp` + `mudawama.kmp.koin` plugins; apply `kotlinxSerialization` plugin; depend on `:feature:athkar:domain`, `:shared:core:database`, `:shared:core:data`, `:shared:core:notifications`
- [X] T003 [P] Create `feature/athkar/presentation/build.gradle.kts` — apply `mudawama.kmp.compose` plugin; depend on `:feature:athkar:domain`, `:shared:core:presentation`, `:shared:designsystem`
- [X] T004 [P] Create `shared/core/notifications/build.gradle.kts` — **N/A**: `NotificationScheduler` interface placed in `shared/core/common` (same pattern as `LocationProvider`); Android/iOS impls in `shared/core/common` androidMain/iosMain; Koin wiring in `shared/core/data` android/iosMain DI modules. No separate Gradle module needed.
- [X] T005 Register all four new modules in `settings.gradle.kts` (`include(":feature:athkar:domain")`, `:feature:athkar:data`, `:feature:athkar:presentation`, `:shared:core:notifications`)
- [X] T006 Add `:feature:athkar:presentation` and `:shared:core:notifications` to `shared/umbrella-ui/build.gradle.kts` dependencies
- [X] T007 Add `kotlinx-serialization-json` dependency + `kotlinxSerialization` plugin to `shared/core/database/build.gradle.kts`

---

## Phase 2: Foundational (Blocking Prerequisites) ✅ COMPLETE

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented. Includes DB migration, new entities/DAOs, TypeConverter, DataStore keys, and Koin wiring.

### Database (shared:core:database)

- [X] T008 [P] Create `AthkarDailyLogEntity.kt` — table `athkar_daily_logs`, composite PK `["group_type","date"]`, columns: `group_type TEXT`, `date TEXT`, `counters_json TEXT`, `is_complete INTEGER`
- [X] T009 [P] Create `TasbeehGoalEntity.kt` — table `tasbeeh_goals`, singleton `id = 1`, `goal_count: Int = 100`
- [X] T010 [P] Create `TasbeehDailyTotalEntity.kt` — table `tasbeeh_daily_totals`, `date: String` PK, `total_count: Int = 0`
- [X] T011 [P] Create `AthkarDailyLogDao.kt` — `getLog`, `getCompletionStatusForDate`, `getLogsForDateRange`, `upsertLog`
- [X] T012 [P] Create `TasbeehGoalDao.kt` — `getGoal`, `upsertGoal`
- [X] T013 [P] Create `TasbeehDailyTotalDao.kt` — `getTotalForDate`, `upsertTotal`
- [X] T014 Create `AthkarCountersConverter.kt` — `@TypeConverter` `Map<String,Int> ↔ String` via kotlinx-serialization
- [X] T015 Update `MudawamaDatabase.kt` — bumped to v4, `AutoMigration(3→4)`, new entities + DAOs, `@TypeConverters(AthkarCountersConverter::class)`

### DataStore keys (shared:core:data)

- [X] T016 Add six `PreferencesKeys`: `ATHKAR_MORNING_NOTIF_ENABLED`, `_HOUR`, `_MINUTE`; `ATHKAR_EVENING_NOTIF_ENABLED`, `_HOUR`, `_MINUTE`

### Domain error type (feature:athkar:domain)

- [X] T017 Create `AthkarError.kt` — `sealed interface AthkarError : DomainError` (`DatabaseError`, `NotificationSchedulingError`, `InvalidInput`)

### String resources (shared:designsystem)

- [X] T018 All Athkar & Tasbeeh string keys added to `strings.xml` (group names, item transliterations/translations for Morning/Evening/Post-Prayer, Tasbeeh screen strings, notification strings, `nav_tab_tasbeeh`)

### Koin wiring (foundational modules)

- [X] T019 Create `AthkarDataModule.kt` — registers `AthkarRepositoryImpl` and `TasbeehRepositoryImpl`

---

## Phase 3: US1 — Morning & Evening Athkar Checklists ✅ COMPLETE

- [X] T020–T026 Domain models + static item lists (`AthkarGroupType`, `AthkarItem`, `AthkarGroup`, `AthkarDailyLog`, `AthkarNotificationIds`, `AthkarPrayerSlot`, `MorningAthkarItems`, `EveningAthkarItems`)
- [X] T027–T030 Repository interface + use cases (`AthkarRepository`, `GetAthkarGroupUseCase`, `IncrementAthkarItemUseCase`, `ObserveAthkarCompletionUseCase`, `ObserveAthkarLogUseCase`, `ResetAthkarItemUseCase`)
- [X] T031–T032 Data layer (`AthkarMappers`, `AthkarRepositoryImpl` — slotted keys for POST_PRAYER, clamping, `computeIsComplete`)
- [X] T033–T038 Presentation (`AthkarPresentationModule`, `AthkarViewModel`, `AthkarItemCard`, `AthkarGroupCard`, `AthkarGroupScreen`, `AthkarScreen`)
- [X] T039–T041 Navigation wiring — `AthkarScreen` in `MudawamaAppShell`, `TasbeehRoute` in `Routes.kt`, `tasbeehScreen` destination added
- [X] expect/actual: `AthkarBackHandler`, `RequestNotificationPermissionEffect`
- [X] `NotificationPermissionProvider` interface in `shared/core/domain`; `IosNotificationProvider.swift` (NSObject+UNUserNotificationCenterDelegate, `willPresent` returns `.banner+.sound`)
- [X] Android welcome notification on permission grant; `POST_NOTIFICATIONS` in `AndroidManifest.xml`; `NSUserNotificationsUsageDescription` in `Info.plist`

---

## Phase 4: US2 — Post-Prayer Athkar Checklist ✅ COMPLETE

- [X] T042 `PostPrayerAthkarItems.kt` — Tasbeeh Al-Fatima (SubhanAllah×33, Alhamdulillah×33, Allahu Akbar×34) + Ayat al-Kursi + post-prayer supplications
- [X] T043 `GetAthkarGroupUseCase` returns `postPrayerAthkarItems` for `POST_PRAYER`
- [X] T044 Post-Prayer card renders in `AthkarScreen`; prayer slot `FilterChip` row in `AthkarGroupScreen`; counter key format `"itemId#slotIndex"`; `AthkarPrayerSlot` (FAJR=0…ISHA=4)

---

## Phase 5: US3 — Standalone Tasbeeh Counter ✅ COMPLETE

- [X] T045 `TasbeehGoal.kt` domain model
- [X] T046 `TasbeehDailyTotal.kt` domain model
- [X] T047 `TasbeehRepository.kt` interface
- [X] T048 `ObserveTasbeehGoalUseCase.kt`
- [X] T049 `SetTasbeehGoalUseCase.kt` — validates `goalCount >= 1`
- [X] T050 `ObserveTasbeehDailyTotalUseCase.kt`
- [X] T051 `AddToTasbeehDailyUseCase.kt`
- [X] T052 `TasbeehMappers.kt` (entity → domain)
- [X] T053 `TasbeehRepositoryImpl.kt` — `addToDaily` reads current total then upserts `current + amount`; `amount = 0` is a no-op
- [X] T054 `TasbeehViewModel.kt` — overflow guard (`>= Int.MAX_VALUE - 1` drops tap); session count is in-memory; `Reset` calls `AddToTasbeehDailyUseCase` then clears session
- [X] T055 `TasbeehGoalBottomSheet.kt` — `MudawamaBottomSheet` wrapper; presets 33/100/300; custom text field; `MudawamaPrimaryButton` confirm
- [X] T056 `TasbeehScreen.kt` — large `Canvas`-arc circular tap button, stats row, haptic feedback on tap + goal-reached
- [X] T057 `TasbeehViewModel` registered in `AthkarPresentationModule`
- [X] T058 `TasbeehScreen()` wired to `TasbeehRoute` via `App.kt`

### Navigation & UI fixes (post-Phase 5)

- [X] T058a Add `TASBEEH` entry to `BottomNavItem` — `TasbeehRoute`, `Icons.Default.Loop`, `nav_tab_tasbeeh`; bottom bar now has **5 tabs**
- [X] T058b Replace `Scaffold` with `Box` overlay in `MudawamaAppShell` — `NavDisplay` fills `fillMaxSize`, `MudawamaBottomBar` aligned to `BottomCenter`; removes opaque Scaffold background behind bar

### UI Polish fixes (post-Phase 5)

- [X] T058c Reduced bottom bar tab padding for 5-item fit — active pill `horizontal 20→12dp`, inactive `horizontal 16→8dp`, icons `22→20dp`, row arrangement `SpaceAround→SpaceEvenly`
- [X] T058d Added `statusBarsPadding()` to all screens (`PrayerScreen`, `QuranScreen`, `AthkarScreen`, `AthkarGroupScreen`, `HabitsScreen`, `TasbeehScreen`) — screens were drawing behind the status bar
- [X] T058e Added `96.dp` bottom spacer/padding to all screens — content was hidden behind the floating bottom bar
- [X] T058f `TasbeehScreen` title: added explicit `color = MaterialTheme.colorScheme.onBackground` — was invisible in dark theme due to missing `LocalContentColor` propagation

---

## Phase 6: US4 — Daily Completion Tracking (Priority: P3)

**Goal**: Athkar completion data is reliably persisted per calendar date; yesterday's data is preserved read-only; today starts fresh.

- [X] T059 [US4] Verify `AthkarRepositoryImpl.observeCompletionStatus(date)` returns empty/false when no log row exists for today (fresh day)
- [X] T060 [US4] Verify `AthkarViewModel` passes today's date (from injected `TimeProvider`) to all repository calls — confirm no `Clock.System` direct calls (SC-002 compliance)
- [X] T061 [US4] Verify `TasbeehRepositoryImpl.addToDaily` correctly adds to existing total and does not reset to zero when called multiple times

**Checkpoint**: Calendar-date isolation confirmed. No date-related data bleed between days. ✅

---

## Phase 7: US5 — Configurable Notification Reminders (Priority: P4)

**Goal**: Users can enable and schedule daily Morning/Evening Athkar notifications.

### NotificationScheduler module (shared:core:notifications)

- [X] T063 [P] [US5] Create `NotificationScheduler.kt` interface — `scheduleDailyReminder(notificationId, hour, minute, title, body)`, `cancelReminder(notificationId)`
- [X] T064 [P] [US5] Create `NotificationPermissionChecker.kt` interface — `hasPermission(): Boolean`, `requestPermission(): NotificationPermissionResult`
- [X] T065 [US5] `AndroidNotificationScheduler.kt` — `AlarmManager.setExactAndAllowWhileIdle` + `BroadcastReceiver`; channel `"athkar_reminders"`
- [X] T066 [US5] `AthkarNotificationReceiver.kt` BroadcastReceiver — posts via `NotificationManagerCompat`; re-schedules next day's alarm
- [X] T067 [US5] Register receiver in `AndroidManifest.xml`; add `SCHEDULE_EXACT_ALARM` + `USE_EXACT_ALARM` permissions
- [X] T068 [US5] `AndroidNotificationPermissionChecker.kt` — `ContextCompat.checkSelfPermission(POST_NOTIFICATIONS)`
- [X] T069 [US5] `IosNotificationScheduler.kt` — `UNCalendarNotificationTrigger` (hour+minute, repeats=true)
- [X] T070 [US5] `IosNotificationPermissionChecker.kt` — `UNUserNotificationCenter.requestAuthorization`
- [X] T071 [US5] `NotificationsModule.kt` Koin modules — `androidNotificationsModule` (androidMain) + `iosNotificationsModule` (iosMain) in `shared/core/data/src/.../di/`

### Notification preference domain & data (feature:athkar)

- [X] T072 [P] [US5] `NotificationPreference.kt` — `groupType`, `enabled`, `hour`, `minute`
- [X] T073 [P] [US5] `AthkarNotificationRepository.kt` interface — `observePreference`, `savePreference(preference, title, body)`
- [X] T074 [P] [US5] `ObserveNotificationPreferenceUseCase.kt`
- [X] T075 [P] [US5] `SaveNotificationPreferenceUseCase.kt` — validates hour ∈ [0,23], minute ∈ [0,59]
- [X] T076 [US5] `AthkarNotificationRepositoryImpl.kt` — reads/writes DataStore; delegates to `NotificationScheduler`; `POST_PRAYER` is a no-op (no notification)
- [X] T076a [US5] Register `AthkarNotificationRepositoryImpl` in `AthkarDataModule`
- [X] Updated `AthkarDomainModule` — added `ObserveNotificationPreferenceUseCase` + `SaveNotificationPreferenceUseCase`

### Presentation (notification settings)

- [X] T077 [US5] `AthkarNotificationViewModel.kt` — actions carry already-resolved title/body strings; permission-checks on enable; emits `PermissionDenied` / `SaveError` events
- [X] T078 [US5] Add Morning/Evening notification `Switch` toggle rows to `AthkarOverviewContent` in `AthkarScreen.kt`
- [X] T079 [US5] Register `AthkarNotificationViewModel` in `AthkarPresentationModule`
- [X] T080 [US5] Add `androidNotificationsModule` / `iosNotificationsModule` to Koin init in `KoinInitializer.kt` (androidMain + iosMain)

**Checkpoint**: Enable Morning Athkar notification, set time, background app. Notification arrives at scheduled time. Disabling cancels it.

---

## Phase 8: Polish & Cross-Cutting Concerns ✅ COMPLETE

- [X] T081 [P] Overflow guard in `TasbeehViewModel` — taps beyond `Int.MAX_VALUE - 1` are silently dropped ✅ (implemented in T054)
- [X] T082 [P] Verify zero hardcoded strings in `athkar/presentation/` composables ✅
- [X] T083 [P] Verify no direct `Dispatchers.IO` or `Dispatchers.Main` in `feature:athkar` — all dispatchers injected ✅
- [X] T084 [P] Verify no `Clock.System` calls outside `SystemTimeProvider` in `feature:athkar` (SC-002) ✅
- [X] T085 [P] Verify domain layer purity — no Android/Ktor/Room imports in `feature/athkar/domain/` ✅
- [X] T086 Verify `AthkarRepositoryImpl.incrementItem` clamps at `targetCount` (FR-007b) ✅ — line 58 early return
- [X] T087 Verify `isComplete` is recomputed and persisted correctly when the last item reaches target ✅ — `computeIsComplete` called on every increment
- [X] T088 Verify `TasbeehRepositoryImpl.addToDaily` is a no-op for `amount = 0` ✅ — line 42 early return
- [X] T089 Verify `AthkarNotificationRepositoryImpl.savePreference` re-schedules when only time changes (FR-024) ✅ — always calls `scheduleDailyReminder` when `enabled = true`
- [X] T090 Verify notification permission denial path emits `PermissionDenied` event (FR-022) ✅ — `AthkarNotificationViewModel` emits `PermissionDenied` when `requestPermission()` returns `Denied`
- [X] T091 `androidApp:compileDebugKotlin` — BUILD SUCCESSFUL ✅
- [X] T092 `feature:athkar:domain/data/presentation:compileCommonMainKotlinMetadata` — zero errors ✅
- [X] T093 `shared:umbrella-ui:compileAndroidMain` — assembles with athkar modules ✅
- [ ] T094 Follow `specs/008-athkar-tasbeeh/quickstart.md` end-to-end verification (manual device test)
- [X] T095 Update `AGENTS.md` to record feature `008-athkar-tasbeeh` as complete ✅

---

## Dependencies & Execution Order

- **Phase 1 (Setup)**: ✅ Complete.
- **Phase 2 (Foundational)**: ✅ Complete.
- **Phase 3 (US1)**: ✅ Complete.
- **Phase 4 (US2)**: ✅ Complete.
- **Phase 5 (US3)**: ✅ Complete (including T058a/T058b nav/bar fixes).
- **Phase 6 (US4)**: ✅ Complete — verification tasks passed.
- **Phase 7 (US5)**: ✅ Complete — notification infrastructure + UI toggle rows.
- **Phase 8 (Polish)**: ✅ Complete — all static verifications + build passing. T094 (manual device test) pending.

---

## Notes

- `[P]` = different files, no dependency conflicts — safe to parallelise
- `[USN]` = maps to User Story N for traceability
- Counter key format for POST_PRAYER: `"itemId#slotIndex"` (e.g. `"post_prayer_subhanallah#2"`)
- Tasbeeh session count is **never** written to DB on each tap — only on `Reset` action
- `AthkarRepositoryImpl.incrementItem` resolves `targetCount` internally from the static items list
- Bottom bar floats via `Box` overlay in `MudawamaAppShell` (not `Scaffold.bottomBar`) — prevents opaque background behind the glassmorphism bar
- `MudawamaPrimaryButton` is the correct button component (not `PrimaryButton`)
- `TasbeehScreen` entry-point composable owns its own `koinViewModel()` call — `App.kt` calls it as `TasbeehScreen()` with no ViewModel wiring
