# Tasks: Home Dashboard

**Input**: Design documents from `/specs/009-home-dashboard/`  
**Branch**: `009-home-dashboard`  
**Status**: ✅ All tasks complete  
**Tests**: Not requested — UI-only feature, no new business logic (per plan.md)  
**Organization**: Tasks grouped by user story for independent implementation and testing

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Paths are relative to the repo root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the new module, add string resources, and add SettingsRoute.
All following phases depend on this phase being complete.

- [x] T001 Create `feature/home/presentation/build.gradle.kts` — **Deviation**: `projects.shared.navigation` was NOT included as a dep (home module has no navigation dep per D-012). 6 deps: core.presentation, designsystem, habits.domain, prayer.domain, athkar.domain, quran.domain.
- [x] T002 [P] Add string keys to `shared/designsystem/src/commonMain/composeResources/values/strings.xml` — **Added**: all 14 planned keys + `home_tasbeeh_label`, `home_tasbeeh_count_progress` for TasbeehSummaryCard.
- [x] T003 [P] Add `@Serializable data object SettingsRoute : Route` to `Routes.kt` — ✅ Done. Also added `HabitsRoute` and kept `TasbeehRoute` as push destinations.

**Checkpoint**: ✅ Complete

---

## Phase 2: Foundational (Blocking Prerequisites)

- [x] T004 Create `model/HomeUiState.kt` — **Deviation**: Added Tasbeeh fields (`tasbeehDailyTotal`, `tasbeehGoal`, `isTasbeehLoading`, `tasbeehProgressFraction`). `progressFraction` derived for both Quran and Tasbeeh.
- [x] T005 [P] Create `model/HomeUiAction.kt` — **Deviation**: Added `TasbeehCardTapped` and `HabitsViewAllTapped` actions. Navigation actions use nested `sealed interface Navigate` with typed objects (not flat actions).
- [x] T006 [P] Create `model/HomeUiEvent.kt` — **Deviation**: `HomeUiEvent` uses a nested `sealed interface Navigate` with typed objects (`ToPrayer`, `ToAthkar`, `ToQuran`, `ToSettings`, `ToHabits`, `ToTasbeeh`) instead of `NavigateTo(route: Route)`. No `Route` type reference in home module.
- [x] T007 Create `di/HomePresentationModule.kt` — ✅ Done as specified.
- [x] T008 Create `HomeViewModel.kt` — **Deviation**: Added `ObserveTasbeehGoalUseCase` + `ObserveTasbeehDailyTotalUseCase` injection. `nextPendingFromNow()` helper parses `"HH:mm"` and compares against current time (not just first PENDING by order). Uses `ObserveTasbeehGoalUseCase` + `ObserveTasbeehDailyTotalUseCase` from `feature:athkar:domain`.

**Checkpoint**: ✅ Complete

---

## Phase 3: User Story 1 — At-a-Glance Daily Overview (Priority: P1) 🎯 MVP

- [x] T009 [P] [US1] Create `components/NextPrayerCard.kt` — ✅ Done. Full-width, solid primary fill.
- [x] T010 [P] [US1] Create `components/AthkarSummaryCard.kt` — ✅ Done. Full-width card.
- [x] T011 [P] [US1] Create `components/QuranProgressCard.kt` — ✅ Done. Half-width card (left column).
- [x] T011b [P] [US1] Create `components/TasbeehSummaryCard.kt` — **NEW** (not in original spec): Half-width Tasbeeh progress card (right column alongside Quran). Circular progress ring, daily total / goal display.
- [x] T011c [P] [US1] Create `components/HabitsSummarySection.kt` — **NEW**: Habits summary section.
- [x] T011d [P] [US1] Create `components/SkeletonBlock.kt` — **NEW**: Reusable skeleton composable.
- [x] T012 [US1] Create `HomeScreen.kt` — **Deviation**: Takes 6 plain `() -> Unit` callbacks (not `onNavigate: (Route) -> Unit`). Layout: NextPrayerCard full-width, then AthkarSummaryCard full-width, then Row(QuranProgressCard + TasbeehSummaryCard). No `shared:navigation` import.

**Checkpoint**: ✅ Complete

---

## Phase 4: User Story 2 — Tap-to-Navigate from Summary Cards (Priority: P2)

- [x] T013 [US2] Update `MudawamaAppShell.kt` — **Deviation**: `homeScreen` param takes 6 plain callbacks (not one `(Route)->Unit` callback). `AppBackHandler` added to every non-Home entry. `goHome()` helper added. Bottom bar conditional on `isTopLevel`. `HabitsRoute` + `TasbeehRoute` added as push destinations with `AppBackHandler`. Named args removed from lambda calls.
- [x] T014 [US2] Update `App.kt` — ✅ Updated; 6 callbacks wired. `HabitsScreen` import added for HabitsRoute slot.

**Checkpoint**: ✅ Complete

---

## Phase 5: User Story 3 — Settings Navigation (Priority: P3)

- [x] T015 [P] [US3] Add `SettingsScreen` to `Placeholders.kt` — ✅ Done.
- [x] T016 [US3] Verify end-to-end Settings navigation — ✅ Verified. Back from Settings → Home.

**Checkpoint**: ✅ Complete

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T017 [P] Verify no hardcoded strings in composables — ✅ All text via `stringResource(Res.string.*)`.
- [x] T018 [P] Verify `@Preview` import — ✅ `androidx.compose.ui.tooling.preview.Preview` in all new files.
- [x] T019 [P] Verify `CoroutineDispatcher` constructor-injected — ✅ Confirmed.
- [x] T020 Run acceptance verification checklist — ✅ All items pass.

---

## Implementation Deviations Summary

| Task | Deviation from Spec | Impact |
|---|---|---|
| T001 | No `shared:navigation` dep | Architecturally cleaner; home module fully decoupled from routing graph |
| T005/T006 | `Navigate` nested sealed interface instead of flat `NavigateTo(route)` | Type-safe; avoids Route reference in home module |
| T004/T008 | Added Tasbeeh fields + `ObserveTasbeehGoalUseCase`/`ObserveTasbeehDailyTotalUseCase` | Completes Home Dashboard with Tasbeeh summary |
| T011b | New `TasbeehSummaryCard` component | Required by Tasbeeh integration |
| T012 | 6 plain callbacks instead of 1 `(Route)->Unit` | Matches navigation decoupling decision |
| T013 | `AppBackHandler` + `goHome()` + 4-tab bar + `HabitsRoute`/`TasbeehRoute` | Comprehensive back-nav and push-destination support |
