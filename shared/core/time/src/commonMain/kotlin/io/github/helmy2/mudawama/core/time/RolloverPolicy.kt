package io.github.helmy2.mudawama.core.time

import io.github.helmy2.mudawama.core.time.RolloverPolicy.Companion.Standard
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Describes when the *logical day* resets relative to the device's local clock.
 *
 * @param offsetHour Local hour (0–23) at which the new logical day begins.
 *   - `0` = midnight = [Standard] policy.
 *   - `1..11` = morning offset (night-owl mode): hours before [offsetHour] still
 *     belong to the previous logical day.
 *   - `12..23` = evening offset (Islamic-style): hours at or after [offsetHour]
 *     already belong to the next logical day.
 */
data class RolloverPolicy(val offsetHour: Int) {

    init {
        require(offsetHour in 0..23) { "offsetHour must be in 0..23" }
    }

    companion object {
        /** Logical day starts at midnight — identical to the calendar day. */
        val Standard: RolloverPolicy = RolloverPolicy(0)

        /**
         * Logical day starts at [hour]:00 local time.
         *
         * @param hour Local hour (0–23).
         */
        fun fixed(hour: Int): RolloverPolicy = RolloverPolicy(hour)
    }
}

/**
 * Computes the *logical date* by applying [policy] to a known [calendarDate] and local [hour].
 *
 * The algorithm branches on the *hemisphere* of [RolloverPolicy.offsetHour]:
 *
 * - **Evening offset** (`offsetHour ≥ 12`): If [hour] ≥ [RolloverPolicy.offsetHour], the current
 *   wall-clock moment is already part of the **next** logical day, so the calendar date is
 *   advanced by one day.
 * - **Morning offset** (`offsetHour in 1..11`): If [hour] < [RolloverPolicy.offsetHour], the
 *   current moment still belongs to the **previous** logical day, so the calendar date is
 *   retreated by one day.
 * - **Standard** (`offsetHour = 0`) or within the normal window: returns [calendarDate] unchanged.
 *
 * @param calendarDate The wall-clock calendar date derived from the current [Instant] + timezone.
 * @param hour         The local hour (0–23) of the current moment.
 * @param policy       The active rollover policy.
 * @return The adjusted *logical date*.
 */
internal fun computeLogicalDate(
    calendarDate: LocalDate,
    hour: Int,
    policy: RolloverPolicy,
): LocalDate {
    val h = policy.offsetHour
    return when {
        h in 12..hour -> calendarDate.plus(1, DateTimeUnit.DAY)
        h in 1..11 && hour < h -> calendarDate.minus(1, DateTimeUnit.DAY)
        else -> calendarDate
    }
}

