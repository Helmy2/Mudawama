package io.github.helmy2.mudawama.umbrella.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.LayoutDirection
import io.github.helmy2.mudawama.athkar.presentation.athkar.AthkarScreen
import io.github.helmy2.mudawama.athkar.presentation.tasbeeh.TasbeehScreen
import io.github.helmy2.mudawama.feature.qibla.presentation.qibla.QiblaScreen
import io.github.helmy2.mudawama.habits.presentation.HabitsScreen
import io.github.helmy2.mudawama.home.presentation.HomeScreen
import io.github.helmy2.mudawama.navigation.MudawamaAppShell
import io.github.helmy2.mudawama.prayer.presentation.PrayerScreen
import io.github.helmy2.mudawama.quran.presentation.QuranScreen
import io.github.helmy2.mudawama.settings.domain.AppSettings
import io.github.helmy2.mudawama.settings.domain.AppTheme
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import io.github.helmy2.mudawama.settings.presentation.SettingsScreen
import org.koin.compose.koinInject

@Composable
fun App() {
    val observeSettingsUseCase: ObserveSettingsUseCase = koinInject()
    val settings by observeSettingsUseCase().collectAsState(
        initial = AppSettings.DEFAULT
    )

    val isDarkTheme = when (settings.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val layoutDirection = if (settings.appLanguage.isRtl) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    MudawamaAppShell(
        darkTheme = isDarkTheme,
        layoutDirection = layoutDirection,
        homeScreen = { onNavigateToPrayer, onNavigateToAthkar,
                       onNavigateToQuran, onNavigateToSettings,
                       onNavigateToHabits, onNavigateToTasbeeh, onNavigateToQibla ->
            HomeScreen(
                onNavigateToPrayer = onNavigateToPrayer,
                onNavigateToAthkar = onNavigateToAthkar,
                onNavigateToQuran = onNavigateToQuran,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToHabits = onNavigateToHabits,
                onNavigateToTasbeeh = onNavigateToTasbeeh,
                onNavigateToQibla = onNavigateToQibla
            )
        },
        prayerScreen = { PrayerScreen() },
        quranScreen = { QuranScreen() },
        athkarScreen = { AthkarScreen() },
        qiblaScreen = { onBack -> QiblaScreen(onNavigateBack = onBack) },
        tasbeehScreen = { onBack -> TasbeehScreen(onBack) },
        habitsScreen = { onBack -> HabitsScreen(onBack = onBack) },
        settingsScreen = { onBack -> SettingsScreen(onNavigateBack = onBack) },
    )
}