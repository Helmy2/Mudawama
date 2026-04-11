package io.github.helmy2.mudawama.athkar.domain.repository

import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.model.AthkarDailyLog
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.AthkarPrayerSlot
import io.github.helmy2.mudawama.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface AthkarRepository {

    /**
     * Observe the daily log for a specific group and date.
     * Emits null when no row exists yet (fresh day or first use).
     */
    fun observeLog(groupType: AthkarGroupType, date: String): Flow<AthkarDailyLog?>

    /**
     * Observe completion status for all groups on a given date.
     * Returns a map of [AthkarGroupType] → isComplete (false when no log row exists).
     */
    fun observeCompletionStatus(date: String): Flow<Map<AthkarGroupType, Boolean>>

    /**
     * Increment the counter for [itemId] within [groupType] on [date].
     * For [AthkarGroupType.POST_PRAYER], [prayerSlot] selects which prayer's counter to increment.
     * Clamped at the item's targetCount — tapping beyond it is a no-op.
     * Recomputes and persists [AthkarDailyLog.isComplete] after each increment.
     */
    suspend fun incrementItem(
        groupType: AthkarGroupType,
        date: String,
        itemId: String,
        prayerSlot: AthkarPrayerSlot? = null,
    ): EmptyResult<AthkarError>

    /**
     * Reset the counter for [itemId] within [groupType] on [date] back to zero.
     * For [AthkarGroupType.POST_PRAYER], [prayerSlot] selects which prayer's counter to reset.
     * Recomputes and persists [AthkarDailyLog.isComplete] after the reset.
     */
    suspend fun resetItem(
        groupType: AthkarGroupType,
        date: String,
        itemId: String,
        prayerSlot: AthkarPrayerSlot? = null,
    ): EmptyResult<AthkarError>
}
