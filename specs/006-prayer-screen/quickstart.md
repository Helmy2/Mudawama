# Phase 1: Quickstart Guide (Prayer Tracking Screen)

**Branch**: `006-prayer-screen` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)

---

## Overview

This document provides a step-by-step implementation guide for building the Prayer Tracking feature.

## Step 1: Core Updates (`shared:core` and `feature:habits:domain`)

1. **Update `LogStatus.kt`**: Add `MISSED` to `feature/habits/domain/.../model/LogStatus.kt`.
2. **Audit `when` statements**:
   - `ToggleHabitCompletionUseCase.kt`: `MISSED -> LogStatus.COMPLETED`.
   - `DecrementHabitCountUseCase.kt`: Add `MISSED` branch.
   - `HabitListItem.kt`: Treat `MISSED` like `PENDING` visually for non-prayer habits.
   - `HabitsViewModel.kt`: Update any exhaustive `when(status)`.
3. **New Use Case**: Create `SetHabitLogStatusUseCase` in `feature:habits:domain` to allow setting an explicit status (needed for MISSED).
4. **Database Migration**:
   - Create `PrayerTimeCacheEntity` and `PrayerTimeCacheDao` in `shared/core/database/`.
   - Update `MudawamaDatabase` version to `2` and add `@AutoMigration(from = 1, to = 2)`.
   - Add `fallback` logic in mappers for unknown string statuses -> `PENDING`.
5. **Location Provider**:
   - Create `LocationProvider` interface in `shared/core`.
   - Implement `AndroidLocationProvider` (requires `play-services-location` dependency).
   - Implement `IosLocationProvider`.

## Step 2: Feature Module Setup (`feature:prayer`)

1. **Create Modules**: Create `domain`, `data`, and `presentation` modules under `feature/prayer/`.
2. **Build Scripts**: Add `build.gradle.kts` to each module using the sketches in `plan.md`.
3. **Settings**: Include the new modules in `settings.gradle.kts`.
4. **Sync**: Run a Gradle sync to ensure the build is successful.
5. **Dependencies**: Add `play-services-location` to `gradle/libs.versions.toml` and the Android app module dependencies if not already present.

## Step 3: Domain Layer (`feature:prayer:domain`)

1. **Models**: Add `PrayerName`, `PrayerTime`, `PrayerWithStatus`.
2. **Repositories**: Define `PrayerTimesRepository` and `PrayerHabitRepository` interfaces.
3. **Use Cases**:
   - `ObservePrayersForDateUseCase`
   - `TogglePrayerStatusUseCase` (delegating to `ToggleHabitCompletionUseCase`)
   - `MarkPrayerMissedUseCase`
   - `SeedPrayerHabitsUseCase`
4. **DI**: Create `PrayerDomainModule` and register the use cases via `factoryOf`.

## Step 4: Data Layer (`feature:prayer:data`)

1. **DTOs and Mappers**: Create `AladhanTimingsDto` and `AladhanMapper`.
2. **API Client**: Set up Ktor client to fetch from `https://api.aladhan.com/v1/timings/{DD-MM-YYYY}`.
3. **Repositories**: Implement `PrayerTimesRepositoryImpl` (fetching + caching) and `PrayerHabitRepositoryImpl` (querying habits).
4. **DI**: Create `PrayerDataModule` and register the repositories via `single<Interface>`.

## Step 5: Presentation Layer (`feature:prayer:presentation`)

1. **ViewModel**: Implement `PrayerViewModel` managing `PrayerUiState`, `PrayerUiAction`, `PrayerUiEvent`.
2. **Composables**:
   - `PrayerScreen` (root UI)
   - `PrayerDateStrip`
   - `PrayerCompletionHero`
   - `PrayerRowItem`
   - `MarkMissedBottomSheet`
3. **Strings**: Add required strings to `shared/designsystem/src/commonMain/composeResources/values/strings.xml`.
4. **DI**: Create `PrayerPresentationModule` and register the ViewModel.

## Step 6: App Wiring

1. **MudawamaAppShell**: Update `shared/navigation/.../MudawamaAppShell.kt` to swap `PrayerPlaceholderScreen` with `PrayerScreen`.
2. **Seeding**: Ensure `SeedPrayerHabitsUseCase` is called at app startup (e.g., in a Koin `createdAtStart` or `onStart` block in the Android/iOS application class).

## Step 7: Verification

1. Run the app on Android or iOS.
2. Verify the 5 prayers appear.
3. Verify the date strip works and changes the displayed times.
4. Verify tapping a prayer toggles its status.
5. Verify long-pressing a prayer allows marking it as MISSED.
6. Verify that with no network connection, prayer times still display from cache (offline-first test).
7. Deny location permission → verify the "Using default location" notice appears and Mecca coordinates are used silently.
