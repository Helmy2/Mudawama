package io.github.helmy2.mudawama.umbrella.ui

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.habits.presentation.HabitsScreen
import io.github.helmy2.mudawama.navigation.MudawamaAppShell
import io.github.helmy2.mudawama.prayer.presentation.PrayerScreen

@Composable
fun App() {
    MudawamaAppShell(
        habitsScreen = { HabitsScreen() },
        prayerScreen = { PrayerScreen() }
    )
}