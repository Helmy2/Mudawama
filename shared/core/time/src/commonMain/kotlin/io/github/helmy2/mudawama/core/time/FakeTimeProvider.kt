package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Test double for [TimeProvider].
 *
 * ⚠️ **For test use only.** Do not bind in production Koin modules.
 *
 * Ships in `commonMain` so consumer modules can use it in their own `commonTest`
 * without additional Gradle dependency wiring.
 *
 * @param fixedInstant Frozen point in time. **Mutable** (`var`) to support time-travel
 *   scenarios in tests (BC-4 in contract) — mutate between assertions to simulate the
 *   passage of time without touching the system clock.
 * @param policy Rollover policy used for [logicalDate]. Defaults to [RolloverPolicy.Standard].
 */
class FakeTimeProvider(
    var fixedInstant: Instant,
    private val policy: RolloverPolicy = RolloverPolicy.Standard,
) : TimeProvider {

    /** Returns the frozen [fixedInstant]. Never reads the system clock. */
    override fun nowInstant(): Instant = fixedInstant

    /**
     * Returns the logical date for the frozen [fixedInstant] in the given [timeZone],
     * adjusted by the active [RolloverPolicy].
     */
    override fun logicalDate(timeZone: TimeZone): LocalDate {
        val ldt = fixedInstant.toLocalDateTime(timeZone)
        return computeLogicalDate(ldt.date, ldt.hour, policy)
    }
}

