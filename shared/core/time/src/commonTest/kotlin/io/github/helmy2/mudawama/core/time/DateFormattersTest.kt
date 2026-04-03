package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DateFormattersTest {

    // ── US3-Scenario-1: LocalDate with zero-padded month and day ─────────────────────────
    @Test
    fun `US3-Scenario-1 - toIsoDateString formats LocalDate with zero-padded month and day`() {
        assertEquals("2026-01-07", toIsoDateString(LocalDate(2026, 1, 7)))
    }

    // ── LocalDate at year-end ─────────────────────────────────────────────────────────────
    @Test
    fun `toIsoDateString formats LocalDate at end of year correctly`() {
        assertEquals("2026-12-31", toIsoDateString(LocalDate(2026, 12, 31)))
    }

    // ── US3-Scenario-2: Instant in UTC → date matches UTC calendar date ──────────────────
    @Test
    fun `US3-Scenario-2 - toIsoDateString converts Instant in UTC to correct date string`() {
        assertEquals(
            "2026-12-31",
            toIsoDateString(Instant.parse("2026-12-31T23:00:00Z"), TimeZone.UTC),
        )
    }

    // ── Timezone offset pushes date into next day ─────────────────────────────────────────
    @Test
    fun `toIsoDateString converts Instant in Asia-Riyadh UTC+3 crossing midnight to next day`() {
        // 2026-12-31T23:30:00Z = 2027-01-01T02:30:00+03:00 in Asia/Riyadh
        assertEquals(
            "2027-01-01",
            toIsoDateString(
                Instant.parse("2026-12-31T23:30:00Z"),
                TimeZone.of("Asia/Riyadh"),
            ),
        )
    }
}

