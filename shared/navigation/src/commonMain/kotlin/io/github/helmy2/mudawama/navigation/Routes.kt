package io.github.helmy2.mudawama.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.nav_tab_athkar
import mudawama.shared.designsystem.nav_tab_home
import mudawama.shared.designsystem.nav_tab_prayers
import mudawama.shared.designsystem.nav_tab_quran
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

/**
 * Type-safe route hierarchy for shared:navigation.
 * Sealed interface makes `when(route)` in NavDisplay exhaustive at compile time.
 */
@Serializable
sealed interface Route : NavKey

@Serializable data object HomeRoute   : Route
@Serializable data object PrayerRoute : Route
@Serializable data object QuranRoute  : Route
@Serializable data object AthkarRoute : Route
@Serializable data object HabitsRoute : Route

/**
 * Enum binding each bottom-nav [Route] to its icon and string resource label.
 * Labels are [StringResource] — never hardcoded string literals.
 */
enum class BottomNavItem(
    val route: Route,
    val icon: ImageVector,
    val labelRes: StringResource,
) {
    HOME(HomeRoute,     Icons.Default.Home,            Res.string.nav_tab_home),
    PRAYER(PrayerRoute, Icons.Default.Star,            Res.string.nav_tab_prayers),
    QURAN(QuranRoute,   Icons.Default.MenuBook,        Res.string.nav_tab_quran),
    ATHKAR(AthkarRoute, Icons.Default.SelfImprovement, Res.string.nav_tab_athkar),
}

