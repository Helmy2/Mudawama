package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Root application shell — single public entry point for platform hosts (FR-001).
 *
 * // FR-002: MudawamaTheme wraps all content; darkTheme derived from isSystemInDarkTheme() — never hardcoded
 */
@Composable
fun MudawamaAppShell(
    habitsScreen: @Composable () -> Unit
) {
    MudawamaTheme(darkTheme = isSystemInDarkTheme()) {
        val backStack = rememberNavBackStack(
            SavedStateConfiguration {
                serializersModule = SerializersModule {
                    polymorphic(NavKey::class) {
                        subclass(HomeRoute::class)
                        subclass(PrayerRoute::class)
                        subclass(QuranRoute::class)
                        subclass(AthkarRoute::class)
                        subclass(HabitsRoute::class)
                    }
                }
            },
            HomeRoute
        )

        Scaffold(
            bottomBar = {
                MudawamaBottomBar(
                    // FR-008: backStack.lastOrNull() is the ONLY source of truth — no local var
                    currentRoute = backStack.lastOrNull(),
                    onNavigate = { route ->
                        // FR-012 / SC-007: single-top guard — tapping the active tab is a no-op at the backstack level
                        if (backStack.lastOrNull() != route) {
                            backStack.clear()
                            backStack.add(route)
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
                entryProvider = entryProvider {
                    entry<HomeRoute> {
                        HomePlaceholderScreen()
                    }

                    entry<PrayerRoute> {
                        PrayerPlaceholderScreen()
                    }

                    entry<QuranRoute> {
                        QuranPlaceholderScreen()
                    }

                    entry<AthkarRoute> {
                        AthkarPlaceholderScreen()
                    }

                    entry<HabitsRoute> {
                        habitsScreen()
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun MudawamaAppShellPreview() {
    MudawamaAppShell({})
}

