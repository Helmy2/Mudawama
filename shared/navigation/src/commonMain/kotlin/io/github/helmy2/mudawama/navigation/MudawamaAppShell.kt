package io.github.helmy2.mudawama.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
 * FR-002: MudawamaTheme wraps all content; darkTheme derived from isSystemInDarkTheme() — never hardcoded.
 * FR-009/FR-010: Bottom bar floats over content using Box overlay (no Scaffold bottomBar slot,
 * which would add an opaque background behind the bar).
 */
@Composable
fun MudawamaAppShell(
    habitsScreen: @Composable () -> Unit = {},
    prayerScreen: @Composable () -> Unit = {},
    quranScreen: @Composable () -> Unit = {},
    athkarScreen: @Composable () -> Unit = {},
    tasbeehScreen: @Composable () -> Unit = {},
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
                        subclass(TasbeehRoute::class)
                    }
                }
            },
            HomeRoute
        )

        Box(modifier = Modifier.fillMaxSize()) {
            // ── Content fills the entire screen (bar floats on top) ───────────
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.fillMaxSize(),
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
                    entry<HomeRoute> { habitsScreen() }
                    entry<PrayerRoute> { prayerScreen() }
                    entry<QuranRoute> { quranScreen() }
                    entry<AthkarRoute> { athkarScreen() }
                    entry<TasbeehRoute> { tasbeehScreen() }
                }
            )

            // ── Floating bar overlaid at the bottom ───────────────────────────
            MudawamaBottomBar(
                currentRoute = backStack.lastOrNull(),
                onNavigate = { route ->
                    // SC-007: single-top guard — tapping the active tab is a no-op
                    if (backStack.lastOrNull() != route) {
                        backStack.clear()
                        backStack.add(route)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Preview
@Composable
fun MudawamaAppShellPreview() {
    MudawamaAppShell()
}
