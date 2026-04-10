package io.github.helmy2.mudawama.quran.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.quran_daily_progress_subtitle_complete
import mudawama.shared.designsystem.quran_daily_progress_subtitle_in_progress
import mudawama.shared.designsystem.quran_of_pages_format
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuranProgressRing(
    pagesRead: Int,
    goalPages: Int,
    progressFraction: Float,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isLoading) 0f else progressFraction,
        animationSpec = tween(durationMillis = 700),
        label = "quranProgressRingAnimation",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Background track — primary color at low alpha
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(180.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                strokeWidth = 14.dp,
            )
            // Foreground arc — primary color
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(180.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 14.dp,
            )
            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = pagesRead.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.quran_of_pages_format, pagesRead, goalPages)
                        .substringAfter("OF "),   // "N PAGES" portion
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Text(
            text = if (progressFraction >= 1f) {
                stringResource(Res.string.quran_daily_progress_subtitle_complete)
            } else {
                stringResource(Res.string.quran_daily_progress_subtitle_in_progress)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (progressFraction >= 1f)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = if (progressFraction >= 1f) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Preview
@Composable
private fun QuranProgressRingPreview() {
    QuranProgressRing(
        pagesRead = 3,
        goalPages = 5,
        progressFraction = 0.6f,
        isLoading = false,
    )
}
