package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.helmy2.mudawama.core.domain.model.LogStatus
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.prayer_completion_fraction
import mudawama.shared.designsystem.prayer_daily_completion_label
import org.jetbrains.compose.resources.stringResource

private val HeroCardShape = RoundedCornerShape(24.dp)

@Composable
fun PrayerCompletionHero(
    prayers: List<PrayerWithStatus>,
    modifier: Modifier = Modifier,
) {
    val completedCount = prayers.count { it.status == LogStatus.COMPLETED }
    val total = prayers.size
    val targetProgress = if (total > 0) completedCount.toFloat() / total else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    MudawamaSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = HeroCardShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.prayer_daily_completion_label),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        color = onPrimaryContainer.copy(alpha = 0.7f),
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(Res.string.prayer_completion_fraction, completedCount, total),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryContainer,
                        ),
                    )
                    if (completedCount == total && total > 0) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = onPrimaryContainer,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp),
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = primary.copy(alpha = 0.2f),
                    trackColor = Color.Transparent,
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = primary,
                    trackColor = Color.Transparent,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(primary),
                ) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = onPrimary,
                        ),
                    )
                }
            }
        }
    }
}