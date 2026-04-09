package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiState
import kotlinx.datetime.LocalDate

private val previewToday = LocalDate(2026, 4, 8)
private val previewStrip = (-3..3).map { offset ->
    LocalDate(2026, 4, 8 + offset)
}
private val previewPrayers = listOf(
    PrayerWithStatus("1", PrayerName.FAJR, "05:12", LogStatus.COMPLETED),
    PrayerWithStatus("2", PrayerName.DHUHR, "12:34", LogStatus.PENDING),
    PrayerWithStatus("3", PrayerName.ASR, "15:47", LogStatus.MISSED),
    PrayerWithStatus("4", PrayerName.MAGHRIB, "18:21", LogStatus.PENDING),
    PrayerWithStatus("5", PrayerName.ISHA, "19:45", LogStatus.PENDING),
)

@Preview(showBackground = true)
@Composable
private fun PrayerScreenPreview() {
    PrayerScreenContent(
        state = PrayerUiState(
            selectedDate = previewToday,
            today = previewToday,
            dateStrip = previewStrip,
            prayers = previewPrayers,
            timesAvailable = true,
        ),
        onAction = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun PrayerScreenOfflinePreview() {
    PrayerScreenContent(
        state = PrayerUiState(
            selectedDate = previewToday,
            today = previewToday,
            dateStrip = previewStrip,
            prayers = previewPrayers.map { it.copy(timeString = "—") },
            timesAvailable = false,
            usingFallbackLocation = true,
        ),
        onAction = {},
    )
}
