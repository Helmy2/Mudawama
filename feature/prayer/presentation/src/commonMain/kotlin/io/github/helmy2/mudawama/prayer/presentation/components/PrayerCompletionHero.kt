package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.prayer_completion_fraction
import mudawama.shared.designsystem.prayer_daily_completion_label
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import org.jetbrains.compose.resources.stringResource

private val HeroCardShape = RoundedCornerShape(24.dp)

/**
 * Full-width hero card matching the design reference (daily_prayer_tracker.png):
 *   - Primary (deep teal) fill, rounded-xl corners
 *   - Left: "DAILY COMPLETION" label + large fraction (e.g. "2 / 5")
 *   - Right: circular progress ring (onPrimary arc on semi-transparent track)
 *
 * All colours come from MudawamaTheme / MaterialTheme — no hardcoded hex literals.
 */
@Composable
fun PrayerCompletionHero(
    prayers: List<PrayerWithStatus>,
    modifier: Modifier = Modifier,
) {
    val completedCount = prayers.count { it.status == LogStatus.COMPLETED }
    val total = prayers.size
    val progress = if (total > 0) completedCount.toFloat() / total else 0f

    val cardColor   = MudawamaTheme.colors.primary
    val onCardColor = MudawamaTheme.colors.onPrimary
    val trackColor  = onCardColor.copy(alpha = 0.25f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = HeroCardShape,
        color = cardColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: label + fraction
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.prayer_daily_completion_label),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        color = onCardColor.copy(alpha = 0.75f),
                    ),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.prayer_completion_fraction, completedCount, total),
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = onCardColor,
                    ),
                )
            }

            // Right: circular progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp),
            ) {
                // Track (background ring)
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 6.dp,
                    color = trackColor,
                    trackColor = Color.Transparent,
                )
                // Progress arc
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 6.dp,
                    color = onCardColor,
                    trackColor = Color.Transparent,
                )
            }
        }
    }
}
