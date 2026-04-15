# Contract: Kotlin Exports from MudawamaCore

**Feature**: `013-ios-native-ui`  
**Scope**: What `shared/umbrella-core` makes visible to Swift via the `MudawamaCore` iOS framework  
**Date**: 2026-04-15

---

## Overview

The `MudawamaCore` static framework is the sole KMP dependency linked by the iOS Xcode target. Every Kotlin type, use case, or interface that Swift needs to reference must be explicitly exported in `umbrella-core/build.gradle.kts`.

The rule is: **`export(X)` + `api(X)` must appear together** for every module whose public API Swift consumes. Failing to add `api()` causes a Gradle build error; failing to add `export()` compiles but strips headers so Swift cannot see the types.

---

## umbrella-core/build.gradle.kts — Target State

```kotlin
plugins {
    id("mudawama.kmp")
    alias(libs.plugins.skie)          // ← ADD: enables Flow→AsyncSequence, suspend→async
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella.core"
    }

    configureIosFramework("MudawamaCore", isStatic = true) {
        // Core infrastructure
        export(projects.shared.core.domain)
        export(projects.shared.core.data)
        export(projects.shared.core.time)
        // Feature domain layers
        export(projects.feature.habits.domain)
        export(projects.feature.prayer.domain)
        export(projects.feature.quran.domain)
        export(projects.feature.athkar.domain)
        export(projects.feature.settings.domain)
        export(projects.feature.qibla.domain)
        // Feature data layers (Koin modules live here)
        export(projects.feature.habits.data)
        export(projects.feature.prayer.data)
        export(projects.feature.quran.data)
        export(projects.feature.athkar.data)
        export(projects.feature.settings.data)
        export(projects.feature.qibla.data)
    }

    sourceSets {
        commonMain {
            dependencies {
                // Must mirror every export() — Gradle enforces this
                api(projects.shared.core.domain)
                api(projects.shared.core.data)
                api(projects.shared.core.time)
                api(projects.feature.habits.domain)
                api(projects.feature.prayer.domain)
                api(projects.feature.quran.domain)
                api(projects.feature.athkar.domain)
                api(projects.feature.settings.domain)
                api(projects.feature.qibla.domain)
                api(projects.feature.habits.data)
                api(projects.feature.prayer.data)
                api(projects.feature.quran.data)
                api(projects.feature.athkar.data)
                api(projects.feature.settings.data)
                api(projects.feature.qibla.data)
                // Database: implementation only — Swift does not reference Room entities directly
                implementation(projects.shared.core.database)
            }
        }
    }
}
```

---

## iosMain KoinComponent Providers

These Kotlin classes live in `shared/umbrella-core/src/iosMain/kotlin/di/IosKoinHelpers.kt`. They implement `KoinComponent` and expose feature use cases to Swift via lazy Koin injection. This is the **only** mechanism Swift uses to resolve dependencies — Swift does not interact with Koin directly.

```kotlin
// umbrella-core/src/iosMain/kotlin/di/IosKoinHelpers.kt
package io.github.helmy2.mudawama.umbrella.core.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
// (use case imports per feature)

class HomeUseCaseProvider : KoinComponent {
    val getNextPrayerTime: GetNextPrayerTimeUseCase by inject()
    val observeAthkarSummary: ObserveAthkarGroupsUseCase by inject()
    val observeQuranProgress: ObserveQuranProgressUseCase by inject()
    val observeTasbeehTotal: ObserveTasbeehDailyTotalUseCase by inject()
    val observeHabits: ObserveHabitsUseCase by inject()
}

class PrayerUseCaseProvider : KoinComponent {
    val observePrayers: ObservePrayersForDateUseCase by inject()
    val toggleStatus: TogglePrayerStatusUseCase by inject()
    val seedHabits: SeedPrayerHabitsUseCase by inject()
}

class QuranUseCaseProvider : KoinComponent {
    val observeProgress: ObserveQuranProgressUseCase by inject()
    val logReading: LogQuranReadingUseCase by inject()
    val observeStreak: ObserveReadingStreakUseCase by inject()
}

class AthkarUseCaseProvider : KoinComponent {
    val observeGroups: ObserveAthkarGroupsUseCase by inject()
    val markComplete: MarkAthkarGroupCompleteUseCase by inject()
}

class TasbeehUseCaseProvider : KoinComponent {
    val observeGoal: ObserveTasbeehGoalUseCase by inject()
    val increment: IncrementTasbeehUseCase by inject()
    val observeTotal: ObserveTasbeehDailyTotalUseCase by inject()
}

class HabitsUseCaseProvider : KoinComponent {
    val observeHabitsWithLogs: ObserveHabitsWithLogsUseCase by inject()
    val toggleLog: ToggleHabitLogUseCase by inject()
}

class SettingsUseCaseProvider : KoinComponent {
    val observeSettings: ObserveSettingsUseCase by inject()
    val setCalculationMethod: SetCalculationMethodUseCase by inject()
    val setLocationMode: SetLocationModeUseCase by inject()
    val setAppTheme: SetAppThemeUseCase by inject()
    val setAppLanguage: SetAppLanguageUseCase by inject()
    val setMorningNotif: SetMorningNotificationUseCase by inject()
    val setEveningNotif: SetEveningNotificationUseCase by inject()
}

class QiblaUseCaseProvider : KoinComponent {
    val calculateAngle: CalculateQiblaAngleUseCase by inject()
}
```

---

## iOS Koin Initialiser (iosMain)

The iOS Koin initialiser lives in `umbrella-core/src/iosMain/` and is the entry point called from `iOSApp.swift`. It registers all platform modules and all feature Koin modules. It does NOT register `iosQiblaPresentationModule` (that was a MudawamaUI concern).

```kotlin
// umbrella-core/src/iosMain/kotlin/KoinInitializer.kt
fun initializeKoin(
    iosEncryptor: Encryptor,
    iosLocationProvider: LocationProvider,
    iosNotificationProvider: NotificationPermissionProvider,
) {
    startKoin {
        modules(
            // Core platform modules
            iosCoreDataModule(iosEncryptor),
            iosCoreDatabaseModule(),
            timeModule(),
            // Notification
            iosNotificationsModule(iosNotificationProvider),
            // Location
            iosLocationModule(iosLocationProvider),
            // Feature modules (domain + data Koin modules)
            prayerDomainModule,
            prayerDataModule,
            quranDomainModule,
            quranDataModule,
            athkarDomainModule,
            athkarDataModule,
            habitsDomainModule,
            habitsDataModule,
            settingsDomainModule,
            settingsDataModule,
            qiblaDomainModule,
            qiblaDataModule,
        )
    }
}
```

---

## Types Visible to Swift (after export)

| Category | Example | Swift name |
|---|---|---|
| Domain models | `PrayerWithStatus`, `LogStatus`, `AthkarGroup` | Same name (SKIE preserves) |
| Use cases | `ObservePrayersForDateUseCase` | Same name |
| Repository interfaces | `PrayerTimesRepository` | Same name (if public) |
| Enums | `PrayerName`, `HabitType`, `AppTheme` | Swift `enum` via SKIE sealed class support |
| `Result<D,E>` | Custom sealed class | Swift sealed class hierarchy |
| `Flow<T>` / `StateFlow<T>` | Any exported use case returning flow | `SkieSwiftFlow<T>` / `SkieSwiftStateFlow<T>` |
| `suspend` functions | Any exported use case `invoke()` | Swift `async throws` function |

## Types NOT Visible to Swift

| Category | Reason |
|---|---|
| `internal` classes (e.g., `PrayerRepositoryImpl`) | Kotlin `internal` visibility — stripped from ObjC headers |
| Room entity classes | Declared as `internal` in `shared:core:database`; not exported |
| DAO interfaces | `internal` — never exposed outside data layer |
| `suspend` function-type parameters | ObjC cannot represent them; SKIE handles suspend *methods*, not *function-type* params |
| `MudawamaUI` types | iOS no longer links `MudawamaUI` — completely unavailable |
