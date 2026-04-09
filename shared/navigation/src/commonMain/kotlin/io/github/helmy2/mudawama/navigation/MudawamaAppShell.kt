package io.github.helmy2.mudawama.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
 * HomeRoute is wired directly to [habitsScreen]. There is no separate HabitsRoute.
 * // FR-002: MudawamaTheme wraps all content; darkTheme derived from isSystemInDarkTheme() — never hardcoded
 */
@Composable
fun MudawamaAppShell(
    habitsScreen: @Composable () -> Unit,
    prayerScreen: @Composable () -> Unit,
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
                        // FR-012 / SC-007: single-top guard — tapping the active tab is a no-op
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
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(animationSpec = tween(150)),
                        initialContentExit = fadeOut(animationSpec = tween(150)),
                    )
                },
                popTransitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(animationSpec = tween(150)),
                        initialContentExit = fadeOut(animationSpec = tween(150)),
                    )
                },
                entryProvider = entryProvider {
                    entry<HomeRoute> {
                        habitsScreen()
                    }

                    entry<PrayerRoute> {
                        prayerScreen()
                    }

                    entry<QuranRoute> {
                        QuranPlaceholderScreen()
                    }

                    entry<AthkarRoute> {
                        AthkarPlaceholderScreen()
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun MudawamaAppShellPreview() {
    MudawamaAppShell({}, {})
}
