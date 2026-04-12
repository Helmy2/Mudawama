package io.github.helmy2.mudawama.prayer.domain.model

import io.github.helmy2.mudawama.core.domain.model.LogStatus

data class PrayerWithStatus(
    val habitId: String,
    val name: PrayerName,
    val timeString: String, // "HH:mm" from cache, or "—" if unavailable
    val status: LogStatus
)
