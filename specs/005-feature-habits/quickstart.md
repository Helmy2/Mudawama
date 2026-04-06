# Quickstart: feature:habits

**Date**: 2026-04-05
**Branch**: `005-feature-habits`

---

## Prerequisites

- Spec `002-add-core-database` fully delivered (HabitDao, HabitLogDao, HabitEntity, HabitLogEntity present).
- Spec `003-add-core-time` fully delivered (TimeProvider, DateFormatters, FakeTimeProvider in commonMain).
- Spec `004-shared-navigation-shell` fully delivered (MudawamaAppShell, HabitsRoute, HabitsPlaceholderScreen present).
- Android Studio Meerkat (2024.3) or later with KMP plugin.
- Xcode 16+ for iOS targets.

---

## Step 1 — Create Gradle modules

Add the following three directories with `build.gradle.kts` files:

```
feature/
  habits/
    domain/   build.gradle.kts
    data/     build.gradle.kts
    presentation/ build.gradle.kts
```

Register in `settings.gradle.kts`:

```kotlin
include(":feature:habits:domain")
include(":feature:habits:data")
include(":feature:habits:presentation")
```

Verify Gradle sync completes with no errors before adding any source files.

---

## Step 2 — Implement domain layer

Build order inside `feature:habits:domain`:

1. `model/` — `HabitType`, `LogStatus`, `Habit`, `HabitLog`, `HabitWithStatus`
2. `error/HabitError.kt`
3. `repository/` — `HabitRepository`, `HabitLogRepository`
4. `util/IdGenerator.kt` — `@OptIn(ExperimentalUuidApi::class) internal fun generateId()`
5. `usecase/` — all 7 use cases (see contracts/domain-api.md for signatures)
6. `di/HabitsDomainModule.kt`

Run domain unit tests:

```bash
./gradlew :feature:habits:domain:testDebugUnitTest
```

Expected: 14+ tests pass (2 per use case: success + failure/edge-case path).

---

## Step 3 — Implement data layer

Inside `feature:habits:data`:

1. `mapper/HabitMapper.kt` — `HabitEntity.toDomain()` and `Habit.toEntity()` extension functions
2. `mapper/HabitLogMapper.kt` — `HabitLogEntity.toDomain()` and `HabitLog.toEntity()`
3. `repository/HabitRepositoryImpl.kt` — implements `HabitRepository` using `HabitDao`
4. `repository/HabitLogRepositoryImpl.kt` — implements `HabitLogRepository` using `HabitLogDao`
5. `di/HabitsDataModule.kt` — `fun habitsDataModule()`

Integration test (Room in-memory):

```bash
./gradlew :feature:habits:data:connectedDebugAndroidTest
```

Expected: cascade-delete test passes (SC-008); mapper round-trip tests pass.

---

## Step 4 — Implement presentation layer

Inside `feature:habits:presentation`:

1. `model/` — all state/action/event/mode classes
2. `HabitsViewModel.kt` — extends `MviViewModel<HabitsUiState, HabitsUiAction, HabitsUiEvent>`
3. `components/` — `HabitHeatmapRow`, `HabitListItem`, `HabitBottomSheet`, `HabitOptionsSheet`
4. `HabitsScreen.kt` — root composable
5. `di/HabitsPresentationModule.kt`

Verify Compose Previews render:

```bash
./gradlew :feature:habits:presentation:assembleDebug
```

Check Android Studio Preview panel — all @Preview functions should render without error.

---

## Step 5 — Wire navigation

1. Update `MudawamaAppShell` signature in `shared:navigation` to accept `habitsScreen` lambda.
2. Create `MudawamaApp.kt` in `shared:umbrella-ui` passing `habitsScreen = { HabitsScreen() }`.
3. Update `androidApp/MainActivity` and `iosApp/ContentView` to call `MudawamaApp()`.
4. Add module dependencies to `shared/umbrella-ui/build.gradle.kts`:
   ```kotlin
   implementation(projects.feature.habits.domain)
   implementation(projects.feature.habits.data)
   implementation(projects.feature.habits.presentation)
   ```

Verify full build:

```bash
./gradlew assembleDebug          # Android
./gradlew iosApp:linkDebugFrameworkIosSimulatorArm64  # iOS
```

---

## Step 6 — Register Koin modules

In the app-level Koin startup (wherever `startKoin { modules(...) }` lives), add:

```kotlin
startKoin {
    modules(
        // existing modules ...
        habitsDomainModule(),
        habitsDataModule(),
        habitsPresentationModule(),
    )
}
```

---

## Smoke Test Checklist

- [ ] Launch app on Android emulator; tap Habits tab → sees empty-state or pre-seeded habits list.
- [ ] Tap "+" FAB → `HabitBottomSheet` slides up; list partially visible behind it.
- [ ] Fill in name, icon, days, type = Boolean; tap Save → habit appears in list (SC-001).
- [ ] Tap the Boolean habit check-off → UI updates within 300 ms (SC-002).
- [ ] Verify 7-day heatmap cells update on check-off (SC-003).
- [ ] Long-press a custom habit → OptionsMenu appears with Edit and Delete.
- [ ] Long-press a core habit → OptionsMenu appears WITHOUT Delete option (FR-017).
- [ ] Delete a custom habit → habit and its logs removed from DB (SC-008).
- [ ] Build on iOS simulator; verify identical behaviour on both platforms (SC-009).
