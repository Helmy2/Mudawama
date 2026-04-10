package io.github.helmy2.mudawama.umbrella.ui

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.habits.presentation.HabitsScreen
import io.github.helmy2.mudawama.navigation.MudawamaAppShell
import io.github.helmy2.mudawama.prayer.presentation.PrayerScreen
import io.github.helmy2.mudawama.quran.presentation.QuranScreen

@Composable
fun App() {
    MudawamaAppShell(
        habitsScreen = { HabitsScreen() },
        prayerScreen = { PrayerScreen() },
        quranScreen = { QuranScreen() },
    )
}
