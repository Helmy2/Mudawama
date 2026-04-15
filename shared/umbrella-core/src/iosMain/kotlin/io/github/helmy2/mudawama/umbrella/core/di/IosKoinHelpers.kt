package io.github.helmy2.mudawama.umbrella.core.di

import io.github.helmy2.mudawama.athkar.domain.usecase.AddToTasbeehDailyUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.GetAthkarGroupUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.IncrementAthkarItemUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveAthkarCompletionUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveAthkarLogUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehDailyTotalUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehGoalUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.SetTasbeehGoalUseCase
import io.github.helmy2.mudawama.feature.qibla.domain.usecase.CalculateQiblaAngleUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.CreateHabitUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.DeleteHabitUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.ObserveHabitsWithTodayStatusUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.ToggleHabitCompletionUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.ObservePrayersForDateUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.SeedPrayerHabitsUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.TogglePrayerStatusUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.LogReadingUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.ObserveQuranStateUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.SetGoalUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.UpdateBookmarkUseCase
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppLanguageUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppThemeUseCase
import io.github.helmy2.mudawama.settings.domain.SetCalculationMethodUseCase
import io.github.helmy2.mudawama.settings.domain.SetEveningNotificationUseCase
import io.github.helmy2.mudawama.settings.domain.SetLocationModeUseCase
import io.github.helmy2.mudawama.settings.domain.SetMorningNotificationUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// ---------------------------------------------------------------------------
// Home
// ---------------------------------------------------------------------------

class HomeUseCaseProvider : KoinComponent {
    val observePrayersForDateUseCase: ObservePrayersForDateUseCase by inject()
    val seedPrayerHabitsUseCase: SeedPrayerHabitsUseCase by inject()
    val observeAthkarCompletionUseCase: ObserveAthkarCompletionUseCase by inject()
    val observeQuranStateUseCase: ObserveQuranStateUseCase by inject()
    val observeTasbeehGoalUseCase: ObserveTasbeehGoalUseCase by inject()
    val observeTasbeehDailyTotalUseCase: ObserveTasbeehDailyTotalUseCase by inject()
    val observeHabitsWithTodayStatusUseCase: ObserveHabitsWithTodayStatusUseCase by inject()
    val observeSettingsUseCase: ObserveSettingsUseCase by inject()
}

// ---------------------------------------------------------------------------
// Prayer
// ---------------------------------------------------------------------------

class PrayerUseCaseProvider : KoinComponent {
    val observePrayersForDateUseCase: ObservePrayersForDateUseCase by inject()
    val togglePrayerStatusUseCase: TogglePrayerStatusUseCase by inject()
    val seedPrayerHabitsUseCase: SeedPrayerHabitsUseCase by inject()
}

// ---------------------------------------------------------------------------
// Quran
// ---------------------------------------------------------------------------

class QuranUseCaseProvider : KoinComponent {
    val observeQuranStateUseCase: ObserveQuranStateUseCase by inject()
    val logReadingUseCase: LogReadingUseCase by inject()
    val setGoalUseCase: SetGoalUseCase by inject()
    val updateBookmarkUseCase: UpdateBookmarkUseCase by inject()
}

// ---------------------------------------------------------------------------
// Athkar
// ---------------------------------------------------------------------------

class AthkarUseCaseProvider : KoinComponent {
    val getAthkarGroupUseCase: GetAthkarGroupUseCase by inject()
    val incrementAthkarItemUseCase: IncrementAthkarItemUseCase by inject()
    val observeAthkarCompletionUseCase: ObserveAthkarCompletionUseCase by inject()
    val observeAthkarLogUseCase: ObserveAthkarLogUseCase by inject()
}

// ---------------------------------------------------------------------------
// Tasbeeh
// ---------------------------------------------------------------------------

class TasbeehUseCaseProvider : KoinComponent {
    val observeTasbeehGoalUseCase: ObserveTasbeehGoalUseCase by inject()
    val observeTasbeehDailyTotalUseCase: ObserveTasbeehDailyTotalUseCase by inject()
    val addToTasbeehDailyUseCase: AddToTasbeehDailyUseCase by inject()
    val setTasbeehGoalUseCase: SetTasbeehGoalUseCase by inject()
}

// ---------------------------------------------------------------------------
// Habits
// ---------------------------------------------------------------------------

class HabitsUseCaseProvider : KoinComponent {
    val observeHabitsWithTodayStatusUseCase: ObserveHabitsWithTodayStatusUseCase by inject()
    val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase by inject()
    val createHabitUseCase: CreateHabitUseCase by inject()
    val deleteHabitUseCase: DeleteHabitUseCase by inject()
}

// ---------------------------------------------------------------------------
// Settings
// ---------------------------------------------------------------------------

class SettingsUseCaseProvider : KoinComponent {
    val observeSettingsUseCase: ObserveSettingsUseCase by inject()
    val setCalculationMethodUseCase: SetCalculationMethodUseCase by inject()
    val setLocationModeUseCase: SetLocationModeUseCase by inject()
    val setAppThemeUseCase: SetAppThemeUseCase by inject()
    val setAppLanguageUseCase: SetAppLanguageUseCase by inject()
    val setMorningNotificationUseCase: SetMorningNotificationUseCase by inject()
    val setEveningNotificationUseCase: SetEveningNotificationUseCase by inject()
}

// ---------------------------------------------------------------------------
// Qibla
// ---------------------------------------------------------------------------

class QiblaUseCaseProvider : KoinComponent {
    val calculateQiblaAngleUseCase: CalculateQiblaAngleUseCase by inject()
}
