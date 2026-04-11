package io.github.helmy2.mudawama.home.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.home_all_prayers_done
import mudawama.shared.designsystem.home_next_prayer_label
import mudawama.shared.designsystem.home_next_prayer_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
fun NextPrayerCard(
    nextPrayerName: PrayerName?,
    nextPrayerTime: String,
    isPrayerLoading: Boolean,
    prayerTimesAvailable: Boolean,
    allPrayersDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MudawamaSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.home_next_prayer_label),
                style = MudawamaTheme.typography.caption,
                color = MudawamaTheme.colors.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(6.dp))

            when {
                isPrayerLoading -> {
                    // Skeleton loading state
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(24.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }
                allPrayersDone -> {
                    Text(
                        text = stringResource(Res.string.home_all_prayers_done),
                        style = MudawamaTheme.typography.h2,
                        color = MudawamaTheme.colors.primary,
                    )
                }
                nextPrayerName != null && prayerTimesAvailable -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nextPrayerName.displayName(),
                            style = MudawamaTheme.typography.h2,
                            color = MudawamaTheme.colors.onSurface,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = nextPrayerTime,
                            style = MudawamaTheme.typography.body2,
                            color = MudawamaTheme.colors.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
                else -> {
                    Text(
                        text = stringResource(Res.string.home_next_prayer_unavailable),
                        style = MudawamaTheme.typography.body2,
                        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

private fun PrayerName.displayName(): String = when (this) {
    PrayerName.FAJR -> "Fajr"
    PrayerName.DHUHR -> "Dhuhr"
    PrayerName.ASR -> "Asr"
    PrayerName.MAGHRIB -> "Maghrib"
    PrayerName.ISHA -> "Isha"
}

@Preview
@Composable
private fun NextPrayerCardPreview() {
    MudawamaTheme(darkTheme = true) {
        NextPrayerCard(
            nextPrayerName = PrayerName.ASR,
            nextPrayerTime = "15:30",
            isPrayerLoading = false,
            prayerTimesAvailable = true,
            allPrayersDone = false,
            onClick = {},
        )
    }
}
