package io.github.helmy2.mudawama.athkar.domain.model

/**
 * A single Dhikr item within an Athkar group.
 *
 * @param id Stable identifier used as the key in the counters map.
 * @param targetCount Number of times this item must be counted to mark it complete.
 */
data class AthkarItem(
    val id: String,
    val targetCount: Int,
)
