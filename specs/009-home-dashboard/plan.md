# Implementation Plan: Home Dashboard

**Branch**: `009-home-dashboard` | **Date**: 2026-04-11 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/009-home-dashboard/spec.md`  
**Status**: ✅ Complete — see [quickstart.md](./quickstart.md) for as-built deviations

## Summary

A new `feature:home:presentation` module was created to house the Home Dashboard. It contains
`HomeScreen.kt`, `HomeViewModel.kt`, five summary card/section composables, MVI model files, and
its own Koin DI module. `feature:habits:presentation` is left entirely untouched.

`HomeScreen` shows four read-only summary cards — Next Prayer (full-width), Athkar daily status
(full-width), Quran progress ring and Tasbeeh progress ring (2-column row) — above the Habits
summary section. Cards navigate to their feature screen on tap via `HomeUiEvent.Navigate` (a nested
sealed interface with typed objects) consumed in `HomeScreen`, which invokes the corresponding
`() -> Unit` callback from `MudawamaAppShell`. A Settings icon in the `TopAppBar` trailing slot
pushes `SettingsRoute` / `SettingsScreen` placeholder. No new DB entities, no new network calls.

**Key deviation from original spec**: `feature:home:presentation` has NO dependency on
`shared:navigation`. Navigation is done via 6 plain callbacks. `HomeUiEvent` uses typed Navigate
objects rather than `NavigateTo(route: Route)`.

## Technical Context

**Language/Version**: Kotlin 2.3.20 (Kotlin Multiplatform)  
**Primary Dependencies**: Compose Multiplatform 1.10.3, Koin 4.2.0, kotlinx-coroutines 1.10.2, kotlinx-datetime 0.7.1  
**Storage**: Room 2.8.4 — no new entities (read-only; existing DAOs via domain use cases)  
**Testing**: None prescribed for this feature (UI-only, no new business logic); existing test suite must not regress  
**Target Platform**: Android (minSdk 30) + iOS 15+ via KMP  
**Project Type**: Mobile app (Compose Multiplatform)  
**Performance Goals**: Summary cards must not block the habits list; each data stream is independently asynchronous  
**Constraints**: Presentation-layer only; no new Room/Ktor/SQLDelight; no new DB schema version  
**Scale/Scope**: 1 new module, 1 new screen, 5 new composable components, 3 new navigation entries, `AppBackHandler` expect/actual

## Constitution Check

| Rule | Pre-Design | Post-Design |
|---|---|---|
| Domain layer: zero Android/Compose/Room/Ktor imports | ✓ N/A — no new domain code | ✓ |
| Presentation → Domain ← Data dependency direction | ✓ | ✓ — only `feature:*:domain` modules added to `feature:home:presentation` |
| Feature modules MUST NOT depend on other feature modules' **presentation** | ✓ | ✓ — `feature:home:presentation` depends on domain-only modules of other features — architecturally correct for a dashboard aggregator. No presentation-to-presentation dependency. |
| MVI: immutable State, Action sealed, Event sealed | ✓ | ✓ |
| No hardcoded user-facing string literals in Composables | ✓ | ✓ — 16 new keys in `strings.xml` |
| `Res` import from `mudawama.shared.designsystem` only | ✓ | ✓ |
| `CoroutineDispatcher` constructor-injected | ✓ | ✓ — `HomeViewModel` injects `dispatcher` |
| No `Dispatchers.IO` / `Dispatchers.Main` hardcoded | ✓ | ✓ |
| `@Preview` from `androidx.compose.ui.tooling.preview.Preview` | ✓ | ✓ |
| No new Room entities / DAOs / repos | ✓ | ✓ |

**No constitution violation.** `feature:home:presentation` is the dashboard aggregator — its
sole architectural purpose is to compose data from multiple features into a single unified view.
Depending on `:domain` modules (pure Kotlin, no Android/Room/Ktor) of other features is not a
violation; it is the architecturally correct design. There is no exception needed.

## Project Structure (as built)

### Documentation (this feature)

```text
specs/009-home-dashboard/
├── plan.md              ← this file
├── research.md          ← Phase 0 decisions (D-001 through D-012)
├── data-model.md        ← HomeUiState / Actions / Events / string keys (updated)
├── quickstart.md        ← as-built file-by-file change guide + verification checklist
├── spec.md              ← Status: Complete ✅
├── tasks.md             ← All tasks marked [x]
├── contracts/
│   └── ui-contracts.md  ← composable signatures, ViewModel MVI interface
└── checklists/
    └── requirements.md  ← spec quality checklist (all pass)
```

### Source Code (as built)

```text
feature/
  home/
    presentation/                        ← NEW MODULE
      build.gradle.kts                   ← NEW — 5 deps (no shared:navigation)
      src/commonMain/kotlin/.../home/presentation/
        HomeScreen.kt                    ← NEW — 6 plain () -> Unit callbacks
        HomeViewModel.kt                 ← NEW — 5 observation flows (incl. Tasbeeh)
        components/
          NextPrayerCard.kt              ← NEW — full-width primary fill
          AthkarSummaryCard.kt           ← NEW — full-width
          QuranProgressCard.kt           ← NEW — half-width (left col)
          TasbeehSummaryCard.kt          ← NEW — half-width (right col)
          HabitsSummarySection.kt        ← NEW
          SkeletonBlock.kt               ← NEW — reusable skeleton
        model/
          HomeUiState.kt                 ← NEW — includes Tasbeeh fields
          HomeUiAction.kt                ← NEW — includes Navigate sealed subinterface
          HomeUiEvent.kt                 ← NEW — Navigate nested sealed interface (no Route types)
        di/
          HomePresentationModule.kt      ← NEW

  habits/
    presentation/                        (UNCHANGED — not modified by this feature)

shared/
  navigation/
    src/commonMain/kotlin/.../navigation/
      Routes.kt                          ← HabitsRoute added; TASBEEH removed from BottomNavItem (4 tabs)
      Placeholders.kt                    ← SettingsScreen added
      MudawamaAppShell.kt                ← 6-callback homeScreen; AppBackHandler; goHome(); isTopLevel bar
      AppBackHandler.kt                  ← NEW expect declaration
    src/androidMain/kotlin/.../navigation/
      AppBackHandler.kt                  ← NEW actual (BackHandler)
    src/iosMain/kotlin/.../navigation/
      AppBackHandler.kt                  ← NEW actual (no-op)
    build.gradle.kts                     ← androidMain + iosMain source sets added
  designsystem/
    src/commonMain/composeResources/values/
      strings.xml                        ← 16 new string keys (14 planned + 2 Tasbeeh)
  umbrella-ui/
    src/commonMain/.../App.kt            ← 6 callbacks; HabitsScreen import
    src/androidMain/.../KoinInitializer.kt ← homePresentationModule()
    src/iosMain/.../KoinInitializer.kt   ← homePresentationModule()
    build.gradle.kts                     ← feature:home:presentation dep
```

**Structure Decision**: A new `feature:home:presentation` Gradle module is created. This is
the architecturally correct placement for a cross-feature dashboard aggregator. It keeps
`feature:habits:presentation` clean and avoids an exception to the architecture rules.

## Complexity Tracking

No constitution violations. No exceptions required.

| Dependency | Justification |
|---|---|
| `feature:home:presentation` → `feature:habits:domain` | Home shows the habits summary — needs `HabitWithStatus`, use cases |
| `feature:home:presentation` → `feature:prayer:domain` | Next Prayer card needs `PrayerWithStatus`, `ObservePrayersForDateUseCase` |
| `feature:home:presentation` → `feature:athkar:domain` | Athkar card needs `AthkarGroupType`, `ObserveAthkarCompletionUseCase`; Tasbeeh card needs `ObserveTasbeehGoalUseCase`, `ObserveTasbeehDailyTotalUseCase` |
| `feature:home:presentation` → `feature:quran:domain` | Quran card needs `QuranScreenState`, `ObserveQuranStateUseCase` |
