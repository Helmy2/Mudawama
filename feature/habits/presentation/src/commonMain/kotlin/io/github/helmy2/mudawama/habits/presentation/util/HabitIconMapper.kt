package io.github.helmy2.mudawama.habits.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** Ordered list of (key → icon) pairs — drives both the picker and display. */
val HABIT_ICONS: List<Pair<String, ImageVector>> = listOf(
    "book"  to Icons.Default.AutoStories,
    "star"  to Icons.Default.Star,
    "water" to Icons.Default.WaterDrop,
    "heart" to Icons.Default.Favorite,
    "run"   to Icons.AutoMirrored.Filled.DirectionsWalk,
    "pray"  to Icons.Default.SelfImprovement,
    "moon"  to Icons.Default.DarkMode,
    "sun"   to Icons.Default.WbSunny,
    "fire"  to Icons.Default.LocalFireDepartment,
    "leaf"  to Icons.Default.Eco,
    "pen"   to Icons.Default.Edit,
    "music" to Icons.Default.MusicNote,
    "gym"   to Icons.Default.FitnessCenter,
    "food"  to Icons.Default.Restaurant,
)

private val iconMap: Map<String, ImageVector> = HABIT_ICONS.toMap()

/** Returns the ImageVector for the given icon key, falling back to a star icon. */
fun iconKeyToImageVector(key: String): ImageVector =
    iconMap[key] ?: Icons.Default.Star
