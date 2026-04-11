# Implementation Plan: Athkar & Tasbeeh

**Branch**: `008-athkar-tasbeeh` | **Date**: 2026-04-10 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/008-athkar-tasbeeh/spec.md`

## Summary

Add Morning, Evening, and Post-Prayer Athkar checklists (tap-to-count per dhikr, clamped at target), a standalone Tasbeeh counter (in-memory session + persisted daily total + configurable goal), and configurable daily notification reminders for Morning and Evening Athkar. Daily completion status for all three Athkar groups is persisted per calendar date in Room. The feature introduces `feature:athkar` (domain / data / presentation) and a new `shared:core:notifications` platform module (interface + Android/iOS implementations via Koin). Room DB bumps from schema v3 to v4.

## Technical Context

**Language/Version**: Kotlin 2.3.20 (Kotlin Multiplatform)  
**Primary Dependencies**: Compose Multiplatform 1.10.3 (UI), Room 2.8.4 (local storage), Koin 4.2.0 (DI), kotlinx-serialization-json 1.10.0 (TypeConverter for counter map), kotlinx-coroutines 1.10.2, androidx.datastore.preferences (notification prefs)  
**Storage**: Room KMP (3 new entities: `athkar_daily_logs`, `tasbeeh_goals`, `tasbeeh_daily_totals`) + DataStore (6 new notification pref keys in existing `session.preferences_pb`)  
**Testing**: Standard KMP unit tests (JVM target) for use cases and repositories; UI verified against `docs/ui/` reference screens  
**Target Platform**: Android (minSdk 30) + iOS 15+  
**Project Type**: Kotlin Multiplatform mobile app  
**Performance Goals**: Haptic feedback + counter increment latency < 100ms (SC-004); notification fires within 60s of scheduled time (SC-006)  
**Constraints**: Fully offline (SC-008); zero hardcoded strings in Composables; all text via `stringResource(Res.string.*)`  
**Scale/Scope**: ~30 dhikr items across 3 groups (static); data volume ~1KB/day; 4 new Gradle modules

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Rule | Status | Notes |
|---|---|---|
| Domain layer is 100% pure Kotlin | PASS | All domain models/interfaces in `feature:athkar:domain` have zero Android/Room/Ktor imports |
| Presentation uses Compose Multiplatform only | PASS | No Android XML or UIKit/SwiftUI in `commonMain` |
| Dependency direction: `:presentation → :domain ← :data` | PASS | Confirmed module structure |
| Feature modules do NOT depend on other feature modules | PASS | `feature:athkar` depends only on `shared:core:*` and `shared:designsystem` |
| `Result<D,E>` + `DomainError` for all public APIs | PASS | `AthkarError : DomainError`; all repo methods return `Result<T, AthkarError>` or `EmptyResult<AthkarError>` |
| `safeCall{}` wraps all DB/network calls in data layer | PASS | Repository implementations use existing `safeCall` from `shared:core:domain` |
| MVI pattern (State / Action / Event) | PASS | `AthkarViewModel`, `TasbeehViewModel`, `AthkarNotificationViewModel` all defined in contracts |
| No hardcoded strings in Composables | PASS | All labels use `Res.string.*` keys; defined in single `shared/designsystem/strings.xml` |
| `Res` imported from `mudawama.shared.designsystem` only | PASS | No per-feature `strings.xml`; no `Res as DsRes` alias |
| CoroutineDispatcher injected, no direct `Dispatchers.*` | PASS | All ViewModels and UseCases receive `CoroutineDispatcher` via Koin |
| Convention plugins single-responsibility | PASS | No new convention plugins; existing `mudawama.kmp`, `mudawama.kmp.compose`, `mudawama.kmp.koin` reused |
| UI matches `docs/ui/` reference screens | PASS | 5 reference screens identified: `daily_athkar_tracker.png`, `morning_athkar_reading.png`, `post_prayer_athkar.png`, `tasbeeh_counter.png`, `tasbeeh_goal_bottom_sheet.png` |
| Room only (no SQLDelight) | PASS | |
| Ktor only (no Retrofit) | PASS | This feature has no network calls |
| Koin only (no Dagger/Hilt) | PASS | |

**Post-design re-check**: All gates pass. The `shared:core:notifications` module uses the same interface-based platform pattern as `shared:core:domain/location` — no `expect/actual` keywords needed, just Koin-wired platform classes.

## Project Structure

### Documentation (this feature)

```text
specs/008-athkar-tasbeeh/
├── plan.md              ← this file
├── spec.md              ← feature specification
├── research.md          ← Phase 0 decisions
├── data-model.md        ← Phase 1 entities + DAOs
├── quickstart.md        ← Phase 1 developer guide
├── contracts/
│   └── ui-contracts.md  ← MVI State/Action/Event + Repository interfaces
└── checklists/
    └── requirements.md  ← spec quality checklist
```

### Source Code (repository root)

```text
feature/
  athkar/
    domain/
      src/commonMain/kotlin/.../athkar/domain/
        model/       AthkarGroupType.kt, AthkarItem.kt, AthkarGroup.kt,
                     AthkarDailyLog.kt, TasbeehGoal.kt, TasbeehDailyTotal.kt,
                     NotificationPreference.kt, AthkarNotificationIds.kt
        error/       AthkarError.kt
        repository/  AthkarRepository.kt, TasbeehRepository.kt,
                     AthkarNotificationRepository.kt
        usecase/     GetAthkarGroupUseCase.kt, IncrementAthkarItemUseCase.kt,
                     ObserveAthkarCompletionUseCase.kt,
                     ObserveTasbeehGoalUseCase.kt, SetTasbeehGoalUseCase.kt,
                     ObserveTasbeehDailyTotalUseCase.kt, AddToTasbeehDailyUseCase.kt,
                     ObserveNotificationPreferenceUseCase.kt,
                     SaveNotificationPreferenceUseCase.kt
        items/       MorningAthkarItems.kt, EveningAthkarItems.kt,
                     PostPrayerAthkarItems.kt
    data/
      src/commonMain/kotlin/.../athkar/data/
        repository/  AthkarRepositoryImpl.kt, TasbeehRepositoryImpl.kt,
                     AthkarNotificationRepositoryImpl.kt
        mapper/      AthkarMappers.kt, TasbeehMappers.kt
        di/          AthkarDataModule.kt
    presentation/
      src/commonMain/kotlin/.../athkar/presentation/
        athkar/      AthkarScreen.kt, AthkarGroupScreen.kt, AthkarViewModel.kt
        tasbeeh/     TasbeehScreen.kt, TasbeehGoalBottomSheet.kt, TasbeehViewModel.kt
        notification/ AthkarNotificationViewModel.kt
        component/   AthkarGroupCard.kt, AthkarItemCard.kt
        di/          AthkarPresentationModule.kt

shared/
  core/
    notifications/                  ← NEW MODULE
      src/
        commonMain/.../core/notifications/
          NotificationScheduler.kt
          NotificationPermissionChecker.kt
          di/NotificationsModule.kt
        androidMain/.../core/notifications/
          AndroidNotificationScheduler.kt
          AndroidNotificationPermissionChecker.kt
        iosMain/.../core/notifications/
          IosNotificationScheduler.kt
          IosNotificationPermissionChecker.kt
    database/                       ← UPDATED
      entity/  AthkarDailyLogEntity.kt, TasbeehGoalEntity.kt,
               TasbeehDailyTotalEntity.kt
      dao/     AthkarDailyLogDao.kt, TasbeehGoalDao.kt,
               TasbeehDailyTotalDao.kt
      converter/ AthkarCountersConverter.kt
      MudawamaDatabase.kt           ← version 4, new entities/DAOs/TypeConverters
    data/                           ← UPDATED
      DataStore keys for athkar notification prefs
  designsystem/
    strings.xml                     ← new athkar_* + tasbeeh_* + notification_athkar_* keys
  navigation/
    Routes.kt                       ← AthkarRoute already exists; TasbeehRoute already exists
  umbrella-ui/
    build.gradle.kts                ← add :feature:athkar:presentation, :shared:core:notifications
```

**Structure Decision**: Option 3 (KMP mobile app). Feature modules under `feature/`, shared infrastructure under `shared/core/`. Follows identical layout to `feature/quran/` and `feature/habits/`.

## Complexity Tracking

No constitution violations. No complexity justification required.
