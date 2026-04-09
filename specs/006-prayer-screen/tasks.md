# Tasks: Prayer Tracking Screen

**Input**: Design documents from `/specs/006-prayer-screen/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/domain-api.md, contracts/presentation-composables.md, quickstart.md

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, database changes, and basic structure

- [x] T001 Update `LogStatus` enum with `MISSED` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/model/LogStatus.kt`
- [x] T002 Update exhaustive `when` statements for `LogStatus.MISSED` in existing habits domain/presentation and mappers
- [x] T003 Create `PrayerTimeCacheEntity` in `shared/core/database/src/commonMain/kotlin/io/github/helmy2/mudawama/core/database/entity/PrayerTimeCacheEntity.kt`
- [x] T004 Create `PrayerTimeCacheDao` in `shared/core/database/src/commonMain/kotlin/io/github/helmy2/mudawama/core/database/dao/PrayerTimeCacheDao.kt`
- [x] T005 Update `MudawamaDatabase` to version 2 and add AutoMigration in `shared/core/database/src/commonMain/kotlin/io/github/helmy2/mudawama/core/database/MudawamaDatabase.kt`
- [x] T006 Create new modules and include them in `settings.gradle.kts`
- [x] T007 Configure `feature/prayer/domain/build.gradle.kts` based on plan
- [x] T008 Configure `feature/prayer/data/build.gradle.kts` based on plan
- [x] T009 Configure `feature/prayer/presentation/build.gradle.kts` based on plan

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T010 [P] Create `Coordinates` in `shared/core/src/commonMain/kotlin/io/github/helmy2/mudawama/core/location/Coordinates.kt`
- [x] T011 Create `LocationProvider` interface in `shared/core/src/commonMain/kotlin/io/github/helmy2/mudawama/core/location/LocationProvider.kt`
- [x] T012 [P] Implement `AndroidLocationProvider` in `shared/core/src/androidMain/kotlin/io/github/helmy2/mudawama/core/location/AndroidLocationProvider.kt`
- [x] T013 [P] Implement `IosLocationProvider` in `shared/core/src/iosMain/kotlin/io/github/helmy2/mudawama/core/location/IosLocationProvider.kt`
- [x] T014 [P] Create `PrayerName`, `PrayerTime`, `PrayerWithStatus` models in `feature/prayer/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/domain/model/`
- [x] T015 [P] Create `AladhanTimingsDto` and DTOs in `feature/prayer/data/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/data/dto/AladhanTimingsDto.kt`
- [x] T016 Create `AladhanMapper.kt` in `feature/prayer/data/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/data/mapper/AladhanMapper.kt`
- [x] T017 Define `PrayerTimesRepository` and `PrayerHabitRepository` interfaces in `feature/prayer/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/domain/repository/`
- [x] T018 Implement `PrayerHabitRepositoryImpl` in `feature/prayer/data/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/data/repository/PrayerHabitRepositoryImpl.kt`
- [x] T019 Implement `PrayerTimesRepositoryImpl` in `feature/prayer/data/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/data/repository/PrayerTimesRepositoryImpl.kt`
- [x] T020 [P] Create `PrayerHabitIds` object in `feature/prayer/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/domain/model/PrayerHabitIds.kt` with 5 stable hardcoded habit ID constants
- [x] T021 Create `SeedPrayerHabitsUseCase` in `feature/prayer/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/domain/usecase/SeedPrayerHabitsUseCase.kt`
- [x] T022 Wire `SeedPrayerHabitsUseCase` inside Android/iOS startup path or Koin `createdAtStart`
- [x] T023 Create DI modules `PrayerDomainModule.kt` and `PrayerDataModule.kt` in their respective `di/` packages

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View and Mark Today's Prayers (Priority: P1) 🎯 MVP

**Goal**: A user opens the Prayers tab and immediately sees all 5 prayers listed in chronological order with their scheduled times and current status, and can tap to toggle PENDING/COMPLETED.

**Independent Test**: Seed 5 prayers as core habits, navigate to the Prayers tab, verify all 5 appear with times and an unchecked status, tap one — verify counter changes from 0/5 to 1/5 and the row shows the completed state.

### Implementation for User Story 1

- [x] T024 [P] [US1] Create `ObservePrayersForDateUseCase` in `feature/prayer/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/domain/usecase/ObservePrayersForDateUseCase.kt`
- [x] T025 [P] [US1] Create `TogglePrayerStatusUseCase` in `feature/prayer/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/domain/usecase/TogglePrayerStatusUseCase.kt`
- [x] T026 [P] [US1] Create UI models `PrayerUiState`, `PrayerUiAction`, `PrayerUiEvent` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/model/PrayerUiState.kt`
- [x] T027 [US1] Implement `PrayerViewModel` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/PrayerViewModel.kt`
- [x] T028 [P] [US1] Implement `PrayerCompletionHero.kt` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/components/PrayerCompletionHero.kt`
- [x] T029 [P] [US1] Implement `PrayerRowItem.kt` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/components/PrayerRowItem.kt`
- [x] T030 [US1] Implement `PrayerScreen.kt` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/PrayerScreen.kt`
- [x] T031 [US1] Add localized strings to `shared/designsystem/src/commonMain/composeResources/values/strings.xml`
- [x] T032 [US1] Create `PrayerPresentationModule.kt` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/di/PrayerPresentationModule.kt`
- [x] T033 [US1] Wire `PrayerScreen` into the navigation in `shared/navigation/src/commonMain/kotlin/io/github/helmy2/mudawama/navigation/MudawamaAppShell.kt`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Mark a Prayer as Missed (Priority: P2)

**Goal**: Users can long-press a prayer row and mark it as "Missed", visibly distinguishing it from completed or pending prayers.

**Independent Test**: Long-press a prayer row, select "Mark as Missed" from the action sheet, verify the row shows a missed indicator distinct from the pending empty circle and the completed teal check.

### Implementation for User Story 2

- [x] T034 [P] [US2] Create `SetHabitLogStatusUseCase` in `feature/habits/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/habits/domain/usecase/SetHabitLogStatusUseCase.kt`
- [x] T035 [US2] Create `MarkPrayerMissedUseCase` in `feature/prayer/domain/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/domain/usecase/MarkPrayerMissedUseCase.kt`
- [x] T036 [P] [US2] Implement `MarkMissedBottomSheet.kt` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/components/MarkMissedBottomSheet.kt`
- [x] T037 [US2] Update `PrayerViewModel.kt` and `PrayerScreen.kt` to handle the `MarkMissedRequested` action and show the bottom sheet

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Browse Past Days (Priority: P2)

**Goal**: Users can check past days using a 7-day horizontal date strip at the top of the screen. Non-today dates are read-only.

**Independent Test**: Navigate to yesterday in the date strip, verify the prayer rows show whatever statuses were logged for that date, navigate back to today and verify today's state is restored.

### Implementation for User Story 3

- [x] T038 [P] [US3] Implement `PrayerDateStrip.kt` in `feature/prayer/presentation/src/commonMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/components/PrayerDateStrip.kt`
- [x] T039 [US3] Update `PrayerScreen.kt` to include `PrayerDateStrip` and pass the selected date
- [x] T040 [US3] Update `PrayerViewModel.kt` to enforce read-only status for non-today dates
- [x] T041 [US3] Update `PrayerRowItem.kt` to disable toggles if the screen is in read-only mode

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: User Story 4 - Offline-First Prayer Times (Priority: P3)

**Goal**: Prayer times are cached and available offline with no visible error states on startup.

**Independent Test**: Fetch times once with network available. Disable the network. Restart the app. Confirm prayer times are displayed without a network call.

### Implementation for User Story 4

- [x] T042 [US4] Update `PrayerTimesRepositoryImpl.kt` to gracefully catch network/location errors and fallback to cached data or return null
- [x] T043 [US4] Update `PrayerViewModel.kt` to set `timesAvailable = false` when `PrayerTimesRepository` returns an error and no cache row exists for today
- [x] T044 [US4] Update `PrayerViewModel.kt` to set `usingFallbackLocation = true` when `LocationProvider` returns permission-denied, and silently use Mecca fallback coordinates
- [x] T045 [US4] Update `PrayerRowItem.kt` to display placeholder times (e.g., "—") if `timesAvailable` is false

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T046 Update/create preview functions in `feature/prayer/presentation/src/androidMain/kotlin/io/github/helmy2/mudawama/prayer/presentation/PrayerScreenPreview.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Integrates with US1's ViewModel/Screen
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Integrates with US1's ViewModel/Screen
- **User Story 4 (P3)**: Can start after Foundational (Phase 2) - Modifies US1 implementations

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel
- Models and components within a story marked [P] can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch UI components for User Story 1 together:
Task: "Implement PrayerCompletionHero.kt in feature/prayer/presentation/.../components/PrayerCompletionHero.kt"
Task: "Implement PrayerRowItem.kt in feature/prayer/presentation/.../components/PrayerRowItem.kt"

# Launch UseCases for User Story 1 together:
Task: "Create ObservePrayersForDateUseCase in feature/prayer/domain/.../usecase/ObservePrayersForDateUseCase.kt"
Task: "Create TogglePrayerStatusUseCase in feature/prayer/domain/.../usecase/TogglePrayerStatusUseCase.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Add User Story 4 → Test independently → Deploy/Demo
6. Each story adds value without breaking previous stories
