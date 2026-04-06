package io.github.helmy2.mudawama.umbrella.ui

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.habits.presentation.HabitsScreen
import io.github.helmy2.mudawama.navigation.MudawamaAppShell

@Composable
fun App() {
    MudawamaAppShell(
        habitsScreen = { HabitsScreen() }
    )
}