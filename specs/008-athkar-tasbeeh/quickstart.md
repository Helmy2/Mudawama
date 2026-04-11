# Quickstart: 008-athkar-tasbeeh

Developer onboarding guide for the Athkar & Tasbeeh feature.

---

## What this feature adds

- **Three Athkar checklists** (Morning, Evening, Post-Prayer): tap-to-count dhikr sessions with per-item clamping at target count, daily completion persistence in Room.
- **Tasbeeh Counter**: in-memory session count with haptic feedback, goal bottom sheet, daily total persisted in Room.
- **Notification reminders**: Morning and Evening Athkar daily reminders at user-configured times, using a new `shared:core:notifications` platform module.
- **Room DB v3 → v4**: three new tables (`athkar_daily_logs`, `tasbeeh_goals`, `tasbeeh_daily_totals`).

---

## Module map

```
feature/athkar/domain/        Pure Kotlin domain models, use cases, repository interfaces
feature/athkar/data/          Room DAOs + entities wiring, repository implementations, Koin
feature/athkar/presentation/  Compose screens, ViewModels, Koin

shared/core/notifications/    NEW — NotificationScheduler + NotificationPermissionChecker
                               expect/actual (interface + platform impl via Koin)
shared/core/database/         Updated — 3 new entities + 3 new DAOs + TypeConverter + v4
shared/core/data/             Updated — 6 new DataStore keys for notification prefs
```

---

## New Gradle modules to create

| Module path | Convention plugins |
|---|---|
| `:feature:athkar:domain` | `mudawama.kmp`, `mudawama.kmp.koin` |
| `:feature:athkar:data` | `mudawama.kmp`, `mudawama.kmp.koin` |
| `:feature:athkar:presentation` | `mudawama.kmp.compose`, `mudawama.kmp.koin` |
| `:shared:core:notifications` | `mudawama.kmp`, `mudawama.kmp.koin` |

Add all four to `settings.gradle.kts` include list and add them to the relevant umbrella modules.

---

## Build changes checklist

- [ ] `shared/core/database/build.gradle.kts` — add `alias(libs.plugins.kotlinxSerialization)` + `implementation(libs.kotlinx.serialization.json)` in `commonMain`
- [ ] `shared/core/database/` — add 3 entity classes, 3 DAO interfaces, `AthkarCountersConverter`, update `MudawamaDatabase` (version 4, new entities, new DAOs, `AutoMigration(3,4)`, `@TypeConverters`)
- [ ] `shared/core/data/` — add 6 new DataStore `PreferencesKey` constants for notification prefs
- [ ] `shared/core/notifications/` — create module; implement `NotificationScheduler` and `NotificationPermissionChecker` for Android + iOS
- [ ] `androidApp/AndroidManifest.xml` — add `POST_NOTIFICATIONS` permission (Android 13+), `RECEIVE_BOOT_COMPLETED` for reschedule-on-boot
- [ ] `iosApp/` — add `UNUserNotificationCenter` usage description to `Info.plist`
- [ ] `shared/umbrella-ui/` — add `:feature:athkar:presentation` and `:shared:core:notifications` to umbrella dependencies + Koin setup
- [ ] `shared/designsystem/strings.xml` — add all new string keys

---

## Key file locations (after implementation)

```
feature/athkar/
  domain/src/commonMain/kotlin/.../athkar/domain/
    model/           AthkarGroupType.kt, AthkarItem.kt, AthkarGroup.kt,
                     AthkarDailyLog.kt, TasbeehGoal.kt, TasbeehDailyTotal.kt,
                     NotificationPreference.kt, AthkarNotificationIds.kt
    error/           AthkarError.kt
    repository/      AthkarRepository.kt, TasbeehRepository.kt,
                     AthkarNotificationRepository.kt
    usecase/         GetAthkarGroupUseCase.kt, IncrementAthkarItemUseCase.kt,
                     ObserveAthkarCompletionUseCase.kt,
                     ObserveTasbeehGoalUseCase.kt, SetTasbeehGoalUseCase.kt,
                     ObserveTasbeehDailyTotalUseCase.kt, AddToTasbeehDailyUseCase.kt,
                     ObserveNotificationPreferenceUseCase.kt,
                     SaveNotificationPreferenceUseCase.kt
    items/           MorningAthkarItems.kt, EveningAthkarItems.kt,
                     PostPrayerAthkarItems.kt

  data/src/commonMain/kotlin/.../athkar/data/
    repository/      AthkarRepositoryImpl.kt, TasbeehRepositoryImpl.kt,
                     AthkarNotificationRepositoryImpl.kt
    mapper/          AthkarMappers.kt, TasbeehMappers.kt
    di/              AthkarDataModule.kt

  presentation/src/commonMain/kotlin/.../athkar/presentation/
    athkar/          AthkarScreen.kt, AthkarGroupScreen.kt, AthkarViewModel.kt
    tasbeeh/         TasbeehScreen.kt, TasbeehGoalBottomSheet.kt, TasbeehViewModel.kt
    notification/    AthkarNotificationViewModel.kt
    component/       AthkarGroupCard.kt, AthkarItemCard.kt
    di/              AthkarPresentationModule.kt

shared/core/notifications/src/
  commonMain/.../core/notifications/
    NotificationScheduler.kt
    NotificationPermissionChecker.kt
    di/   NotificationsModule.kt
  androidMain/.../core/notifications/
    AndroidNotificationScheduler.kt
    AndroidNotificationPermissionChecker.kt
  iosMain/.../core/notifications/
    IosNotificationScheduler.kt
    IosNotificationPermissionChecker.kt

shared/core/database/src/commonMain/.../core/database/
  entity/   AthkarDailyLogEntity.kt, TasbeehGoalEntity.kt,
            TasbeehDailyTotalEntity.kt
  dao/      AthkarDailyLogDao.kt, TasbeehGoalDao.kt,
            TasbeehDailyTotalDao.kt
  converter/ AthkarCountersConverter.kt
```

---

## UI reference screens

All Composables MUST match:

| File | Screen |
|---|---|
| `docs/ui/daily_athkar_tracker.png` | `AthkarScreen` (overview with 3 group cards) |
| `docs/ui/morning_athkar_reading.png` | `AthkarGroupScreen` (tap-to-count session) |
| `docs/ui/post_prayer_athkar.png` | `AthkarGroupScreen` (Post-Prayer variant) |
| `docs/ui/tasbeeh_counter.png` | `TasbeehScreen` |
| `docs/ui/tasbeeh_goal_bottom_sheet.png` | `TasbeehGoalBottomSheet` |

---

## String key naming convention

All new keys must be added to `shared/designsystem/src/commonMain/composeResources/values/strings.xml`.

Prefix conventions for this feature:

| Prefix | Usage |
|---|---|
| `athkar_` | Athkar overview screen labels |
| `athkar_morning_` | Morning group labels |
| `athkar_evening_` | Evening group labels |
| `athkar_post_prayer_` | Post-Prayer group labels |
| `tasbeeh_` | Tasbeeh screen labels |
| `notification_athkar_` | Notification titles and body text |

---

## Verification steps

After implementation, verify:

1. `./gradlew :shared:core:database:kspCommonMainKotlinMetadata` — Room schema generates without errors; `schemas/4.json` created.
2. `./gradlew :feature:athkar:domain:compileCommonMainKotlinMetadata` — zero Android/Ktor/Room imports.
3. `./gradlew :feature:athkar:presentation:compileCommonMainKotlinMetadata` — zero hardcoded string literals in Composables.
4. Install on Android device, navigate to Athkar tab — three group cards visible, tap Morning Athkar, increment all items, verify group completion badge appears.
5. Navigate to Tasbeeh, tap 33 times (with goal=33), verify completion haptic and arc fill; reset, verify daily total updates.
6. Enable Morning Athkar notification in Settings, set time 1 minute ahead, lock screen — verify notification fires.
