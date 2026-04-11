package io.github.helmy2.mudawama.athkar.domain.model

/**
 * Persisted daily progress for a single [AthkarGroupType] on a given calendar date.
 *
 * @param groupType The group this log belongs to.
 * @param date ISO-8601 date string (yyyy-MM-dd).
 * @param counters Map of item ID → current count. Items with no entry have count 0.
 * @param isComplete True when every item in the group has reached its [AthkarItem.targetCount].
 */
data class AthkarDailyLog(
    val groupType: AthkarGroupType,
    val date: String,
    val counters: Map<String, Int>,
    val isComplete: Boolean,
)
