package io.github.helmy2.mudawama.home.presentation.model

import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName

data class HomeUiState(
    // ── Habits (read-only list; add/edit/delete managed in Habits tab) ────────
    val habits: List<HabitWithStatus> = emptyList(),
    val isHabitsLoading: Boolean = true,

    // ── Next Prayer card ─────────────────────────────────────────────────────
    val nextPrayerName: PrayerName? = null,
    val nextPrayerTime: String = "",
    val isPrayerLoading: Boolean = true,
    val prayerTimesAvailable: Boolean = false,
    val allPrayersDone: Boolean = false,

    // ── Athkar summary card ──────────────────────────────────────────────────
    /** Keys: MORNING, EVENING only (POST_PRAYER excluded from summary). */
    val athkarStatus: Map<AthkarGroupType, Boolean> = emptyMap(),
    val isAthkarLoading: Boolean = true,

    // ── Quran progress card ──────────────────────────────────────────────────
    val quranPagesReadToday: Int = 0,
    val quranGoalPages: Int = 0,
    val isQuranLoading: Boolean = true,

    // ── Tasbeeh summary card ─────────────────────────────────────────────────
    val tasbeehDailyTotal: Int = 0,
    val tasbeehGoal: Int = 0,
    val isTasbeehLoading: Boolean = true,
) {
    /** 0f..1f clamped progress fraction for the Quran ring. */
    val progressFraction: Float
        get() = if (quranGoalPages > 0) {
            (quranPagesReadToday.toFloat() / quranGoalPages).coerceIn(0f, 1f)
        } else 0f

    /** 0f..1f clamped progress fraction for the Tasbeeh ring. */
    val tasbeehProgressFraction: Float
        get() = if (tasbeehGoal > 0) {
            (tasbeehDailyTotal.toFloat() / tasbeehGoal).coerceIn(0f, 1f)
        } else 0f

    /** True when both Morning and Evening athkar are not yet started (both false). */
    val athkarNotStarted: Boolean
        get() = !isAthkarLoading &&
                athkarStatus[AthkarGroupType.MORNING] == false &&
                athkarStatus[AthkarGroupType.EVENING] == false
}
