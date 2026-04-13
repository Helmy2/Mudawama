package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.core.domain.model.LogStatus
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.prayer_asr
import mudawama.shared.designsystem.prayer_dhuhr
import mudawama.shared.designsystem.prayer_fajr
import mudawama.shared.designsystem.prayer_isha
import mudawama.shared.designsystem.prayer_maghrib
import org.jetbrains.compose.resources.stringResource

private data class PrayerIconStyle(
    val icon: ImageVector,
    // bgAlpha: opacity applied to primary/surfaceVariant to derive chip bg from theme colours
    val usePrimaryChip: Boolean,
)

@Composable
private fun PrayerName.displayName(): String = when (this) {
    PrayerName.FAJR -> stringResource(Res.string.prayer_fajr)
    PrayerName.DHUHR -> stringResource(Res.string.prayer_dhuhr)
    PrayerName.ASR -> stringResource(Res.string.prayer_asr)
    PrayerName.MAGHRIB -> stringResource(Res.string.prayer_maghrib)
    PrayerName.ISHA -> stringResource(Res.string.prayer_isha)
}

/**
 * Returns icon + whether to use a tinted primary chip (Fajr/Dhuhr)
 * or the neutral surfaceVariant chip (Asr/Maghrib/Isha).
 */
private fun prayerIconStyle(name: PrayerName): PrayerIconStyle = when (name) {
    PrayerName.FAJR -> PrayerIconStyle(Icons.Default.WbTwilight, usePrimaryChip = true)
    PrayerName.DHUHR -> PrayerIconStyle(Icons.Default.WbSunny, usePrimaryChip = true)
    PrayerName.ASR -> PrayerIconStyle(Icons.Default.WbSunny, usePrimaryChip = false)
    PrayerName.MAGHRIB -> PrayerIconStyle(Icons.Default.Star, usePrimaryChip = false)
    PrayerName.ISHA -> PrayerIconStyle(Icons.Default.NightsStay, usePrimaryChip = false)
}

/**
 * Prayer list row — matches daily_prayer_tracker.png design reference.
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
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Icon chip: day prayers → primary at 15% alpha bg + primary icon tint
    //            evening prayers → surfaceVariant bg + onSurfaceVariant icon tint
    val chipBg = if (style.usePrimaryChip) primary.copy(alpha = 0.12f) else surfaceVariant
    val iconTint = if (style.usePrimaryChip) primary else MaterialTheme.colorScheme.onSurfaceVariant

    MudawamaSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
