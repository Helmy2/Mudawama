package io.github.helmy2.mudawama.athkar.domain.model

import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType

/**
 * Represents a user's saved notification preference for a single Athkar group.
 *
 * @param groupType  Which Athkar group this preference applies to (MORNING or EVENING).
 * @param enabled    Whether the reminder is active.
 * @param hour       Hour-of-day in 24-hour format (0–23).
 * @param minute     Minute (0–59).
 */
data class NotificationPreference(
    val groupType: AthkarGroupType,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
)
