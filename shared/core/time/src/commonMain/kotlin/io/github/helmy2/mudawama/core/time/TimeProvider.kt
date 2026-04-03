package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/**
 * Single source of truth for the current moment and logical date.
 *
 * Inject via Koin — never construct directly in production code.
 *
 * Implementations MUST be the **only** location in the codebase where the system clock is
 * read (SC-002). All other modules must obtain time information exclusively through this
 * interface, injected via [io.github.helmy2.mudawama.core.time.di.timeModule].
 *
 * @see SystemTimeProvider Production implementation backed by [kotlinx.datetime.Clock.System].
 * @see FakeTimeProvider Test double for deterministic unit tests.
 */
interface TimeProvider {

    /**
     * Returns the current moment.
     *
     * Implementations MUST be the **only** location in the codebase where the
     * system clock is read (SC-002).
     */
    fun nowInstant(): Instant

    /**
     * Returns the current *logical date* under the module's active [RolloverPolicy].
     *
     * The logical date differs from the calendar date when a non-standard rollover
     * policy is in effect (e.g., 21:00 under an 18:00 policy → next calendar date).
     *
     * @param timeZone Timezone used to convert the current [Instant] to local time.
     *   Defaults to the device's current system timezone.
     */
    fun logicalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate
}

