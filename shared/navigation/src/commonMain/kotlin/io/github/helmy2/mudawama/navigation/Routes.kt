package io.github.helmy2.mudawama.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe route hierarchy for shared:navigation.
 * Sealed interface makes `when(route)` in NavDisplay exhaustive at compile time (FR-003, FR-004).
 */
@Serializable
sealed interface Route : NavKey

@Serializable
data object HomeRoute : Route

@Serializable
data object PrayerRoute : Route

@Serializable
data object AthkarRoute : Route

@Serializable
data object HabitsRoute : Route

/**
 * Enum binding each [Route] to its icon and label string-resource key.
 * [route] is typed as [Route] (not [Any]) for compile-time safety (contract §5).
 */
enum class BottomNavItem(val route: Route, val icon: ImageVector, val labelKey: String) {
    HOME(HomeRoute, Icons.Default.Home, "home"),
    PRAYER(PrayerRoute, Icons.Default.Star, "prayer"),
    ATHKAR(AthkarRoute, Icons.Default.FavoriteBorder, "athkar"),
    HABITS(HabitsRoute, Icons.Default.CheckCircle, "habits"),
}

