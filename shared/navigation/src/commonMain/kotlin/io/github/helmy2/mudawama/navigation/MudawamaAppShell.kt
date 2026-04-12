package io.github.helmy2.mudawama.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
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
 * FR-002: MudawamaTheme wraps all content; darkTheme derived from isSystemInDarkTheme() — never hardcoded.
 * FR-009/FR-010: Bottom bar floats over content using Box overlay (no Scaffold bottomBar slot,
 * which would add an opaque background behind the bar).
 *
 * @param homeScreen Composable for [HomeRoute]. Receives individual navigation callbacks so that
 *   the Home feature module has no dependency on navigation3 or Route types.
 * @param habitsScreen Composable for [HabitsRoute]. Receives an [onBack] lambda wired to
 *   [backStack.removeLastOrNull()].
 * @param settingsScreen Composable for [SettingsRoute]. Receives an [onBack] lambda wired to
 *   [backStack.removeLastOrNull()].
 */
@Composable
fun MudawamaAppShell(
    darkTheme: Boolean = isSystemInDarkTheme(),
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    homeScreen: @Composable (
        onNavigateToPrayer: () -> Unit,
        onNavigateToAthkar: () -> Unit,
        onNavigateToQuran: () -> Unit,
        onNavigateToSettings: () -> Unit,
        onNavigateToHabits: () -> Unit,
        onNavigateToTasbeeh: () -> Unit,
        onNavigateToQibla: () -> Unit,
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    prayerScreen: @Composable () -> Unit = {},
    quranScreen: @Composable () -> Unit = {},
    athkarScreen: @Composable () -> Unit = {},
    qiblaScreen: @Composable (onBack: () -> Unit) -> Unit = { _ -> },
    tasbeehScreen: @Composable () -> Unit = {},
    habitsScreen: @Composable (onBack: () -> Unit) -> Unit = { _ -> },
    settingsScreen: @Composable (onBack: () -> Unit) -> Unit = { _ -> },
) {
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val backStack = rememberNavBackStack(
            SavedStateConfiguration {
                serializersModule = SerializersModule {
                    polymorphic(NavKey::class) {
                        subclass(HomeRoute::class)
                        subclass(PrayerRoute::class)
                        subclass(QuranRoute::class)
                        subclass(AthkarRoute::class)
                        subclass(TasbeehRoute::class)
                        subclass(SettingsRoute::class)
                        subclass(HabitsRoute::class)
                        subclass(QiblaRoute::class)
                    }
                }
            },
            HomeRoute
        )

        fun goHome() {
            backStack.clear()
            backStack.add(HomeRoute)
        }

        fun switchTab(route: Route) {
            if (backStack.lastOrNull()?.let { it::class } != route::class) {
                backStack.clear()
                backStack.add(route)
            }
        }

        // Top-level routes are those that appear in the bottom nav bar.
        val topLevelRoutes = BottomNavItem.entries.map { it.route::class }.toSet()
        val currentRoute = backStack.lastOrNull()
        val isTopLevel = currentRoute?.let { it::class in topLevelRoutes } ?: true

        MudawamaTheme(darkTheme = darkTheme) {
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
                        entry<HomeRoute> {
                            homeScreen(
                                { switchTab(PrayerRoute) },
                                { switchTab(AthkarRoute) },
                                { switchTab(QuranRoute) },
                                { backStack.add(SettingsRoute) },
                                { backStack.add(HabitsRoute) },
                                { backStack.add(TasbeehRoute) },
                                { backStack.add(QiblaRoute) },
                            )
                        }
                        entry<PrayerRoute> {
                            AppBackHandler { goHome() }
                            prayerScreen()
                        }
                        entry<QuranRoute> {
                            AppBackHandler { goHome() }
                            quranScreen()
                        }
                        entry<AthkarRoute> {
                            AppBackHandler { goHome() }
                            athkarScreen()
                        }
                        entry<QiblaRoute> {
                            AppBackHandler { goHome() }
                            qiblaScreen { goHome() }
                        }
                        entry<TasbeehRoute> {
                            AppBackHandler { goHome() }
                            tasbeehScreen()
                        }
                        entry<HabitsRoute> {
                            AppBackHandler { goHome() }
                            habitsScreen { goHome() }
                        }
                        entry<SettingsRoute> {
                            AppBackHandler { goHome() }
                            settingsScreen { goHome() }
                        }
                    }
                )

                // ── Floating bar overlaid at the bottom (top-level routes only) ──
                if (isTopLevel) {
                    MudawamaBottomBar(
                        currentRoute = currentRoute,
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
    }
}

@Preview
@Composable
fun MudawamaAppShellPreview() {
    MudawamaAppShell()
}
