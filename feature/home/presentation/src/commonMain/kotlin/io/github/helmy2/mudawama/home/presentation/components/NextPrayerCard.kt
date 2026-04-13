package io.github.helmy2.mudawama.home.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.designsystem.components.Skeleton
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName
import mudawama.shared.designsystem.prayer_asr
import mudawama.shared.designsystem.prayer_dhuhr
import mudawama.shared.designsystem.prayer_fajr
import mudawama.shared.designsystem.prayer_isha
import mudawama.shared.designsystem.prayer_maghrib
import org.jetbrains.compose.resources.stringResource
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.home_all_prayers_done
import mudawama.shared.designsystem.home_next_prayer_label
import mudawama.shared.designsystem.home_next_prayer_unavailable

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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(6.dp))

            when {
                isPrayerLoading -> {
                    Skeleton(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(24.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }
                allPrayersDone -> {
                    Text(
                        text = stringResource(Res.string.home_all_prayers_done),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                nextPrayerName != null && prayerTimesAvailable -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nextPrayerName.displayName(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = nextPrayerTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
                else -> {
                    Text(
                        text = stringResource(Res.string.home_next_prayer_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerName.displayName(): String = when (this) {
    PrayerName.FAJR -> stringResource(Res.string.prayer_fajr)
    PrayerName.DHUHR -> stringResource(Res.string.prayer_dhuhr)
    PrayerName.ASR -> stringResource(Res.string.prayer_asr)
    PrayerName.MAGHRIB -> stringResource(Res.string.prayer_maghrib)
    PrayerName.ISHA -> stringResource(Res.string.prayer_isha)
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
