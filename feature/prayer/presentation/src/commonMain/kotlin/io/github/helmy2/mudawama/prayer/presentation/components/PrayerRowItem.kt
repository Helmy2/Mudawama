package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus

private val CardShape = RoundedCornerShape(20.dp)

private data class PrayerIconStyle(
    val icon: ImageVector,
    // bgAlpha: opacity applied to primary/surfaceVariant to derive chip bg from theme colours
    val usePrimaryChip: Boolean,
)

private fun PrayerName.displayName(): String = when (this) {
    PrayerName.FAJR    -> "Fajr"
    PrayerName.DHUHR   -> "Dhuhr"
    PrayerName.ASR     -> "Asr"
    PrayerName.MAGHRIB -> "Maghrib"
    PrayerName.ISHA    -> "Isha"
}

/**
 * Returns icon + whether to use a tinted primary chip (Fajr/Dhuhr)
 * or the neutral surfaceVariant chip (Asr/Maghrib/Isha).
 */
private fun prayerIconStyle(name: PrayerName): PrayerIconStyle = when (name) {
    PrayerName.FAJR    -> PrayerIconStyle(Icons.Default.WbTwilight, usePrimaryChip = true)
    PrayerName.DHUHR   -> PrayerIconStyle(Icons.Default.WbSunny,    usePrimaryChip = true)
    PrayerName.ASR     -> PrayerIconStyle(Icons.Default.WbSunny,    usePrimaryChip = false)
    PrayerName.MAGHRIB -> PrayerIconStyle(Icons.Default.Star,        usePrimaryChip = false)
    PrayerName.ISHA    -> PrayerIconStyle(Icons.Default.NightsStay,  usePrimaryChip = false)
}

/**
 * Prayer list row — matches daily_prayer_tracker.png design reference.
 *
 * All colours come from MudawamaTheme / MaterialTheme — no hardcoded hex literals.
 *   - White [surface] card, xl corner radius
 *   - Left: circular icon chip — tinted [primary] bg for day prayers, [surfaceVariant] for evening
 *   - Centre: prayer name (bodyLarge SemiBold) + time (bodySmall onSurfaceVariant)
 *   - Right: circular toggle — [primary] filled check (completed), error (missed), outlined (pending)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrayerRowItem(
    prayer: PrayerWithStatus,
    onToggle: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val style = prayerIconStyle(prayer.name)
    val primary        = MudawamaTheme.colors.primary
    val onPrimary      = MudawamaTheme.colors.onPrimary
    val surfaceVariant = MudawamaTheme.colors.surfaceVariant

    // Icon chip: day prayers → primary at 15% alpha bg + primary icon tint
    //            evening prayers → surfaceVariant bg + onSurfaceVariant icon tint
    val chipBg   = if (style.usePrimaryChip) primary.copy(alpha = 0.12f) else surfaceVariant
    val iconTint = if (style.usePrimaryChip) primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    enabled = enabled,
                    onClick = onToggle,
                    onLongClick = onLongPress,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Circular icon chip
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(chipBg),
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Prayer name + time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prayer.name.displayName(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = prayer.timeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Circular status toggle
            when (prayer.status) {
                LogStatus.COMPLETED -> {
                    // Solid primary circle with white check
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primary),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                LogStatus.MISSED -> {
                    // Error container circle with × icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                LogStatus.PENDING -> {
                    // Outline-only circle using onSurfaceVariant at low opacity
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(34.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        trackColor = Color.Transparent,
                    )
                }
            }
        }
    }
}
