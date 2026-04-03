package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Production implementation of [TimeProvider] backed by the device system clock.
 *
 * Registered as a Koin singleton by [io.github.helmy2.mudawama.core.time.di.timeModule].
 * Do not construct manually in production code — use Koin injection.
 *
 * **SC-002**: This class is the **sole** permitted call site for [Clock.System] in the
 * entire codebase. All other modules must obtain the current time via the [TimeProvider]
 * interface. Verified at CI time with:
 * ```
 * git grep -rn "Clock\.System" --include="*.kt" -- shared/
 * ```
 *
 * @param policy Rollover policy supplied by the app's DI setup via
 *   [io.github.helmy2.mudawama.core.time.di.timeModule].
 */
class SystemTimeProvider(private val policy: RolloverPolicy) : TimeProvider {

    /**
     * Returns the current moment from [Clock.System].
     *
     * **This is the only permitted [Clock.System] access in the codebase (SC-002).**
     */
    override fun nowInstant(): Instant = Clock.System.now()

    /**
     * Returns the logical date for the current moment in the given [timeZone],
     * adjusted by the active [RolloverPolicy].
     */
    override fun logicalDate(timeZone: TimeZone): LocalDate {
        val ldt = nowInstant().toLocalDateTime(timeZone)
        return computeLogicalDate(ldt.date, ldt.hour, policy)
    }
}

