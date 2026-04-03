package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Instant

class LogicalDateCalculatorTest {

    // ── US1-Scenario-1: Evening rollover — hour AFTER offset → +1 day ───────────────────
    @Test
    fun `US1-Scenario-1 - evening offset 18 hour 21 returns next day`() {
        // 2026-04-06T21:00:00Z → calendar date = 2026-04-06, hour = 21 ≥ 18 → +1 = 2026-04-07
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-06T21:00:00Z"),
            policy = RolloverPolicy.fixed(18),
        )
        assertEquals(LocalDate(2026, 4, 7), fake.logicalDate(TimeZone.UTC))
    }

    // ── US1-Scenario-2: Evening rollover — hour BEFORE offset → same day ────────────────
    @Test
    fun `US1-Scenario-2 - evening offset 18 hour 17 returns same day`() {
        // 2026-04-06T17:59:00Z → calendar date = 2026-04-06, hour = 17 < 18 → same day
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-06T17:59:00Z"),
            policy = RolloverPolicy.fixed(18),
        )
        assertEquals(LocalDate(2026, 4, 6), fake.logicalDate(TimeZone.UTC))
    }

    // ── US1-Scenario-3: Standard policy (H=0), late night → same day ────────────────────
    @Test
    fun `US1-Scenario-3 - standard policy H=0 at 23h59 returns same calendar day`() {
        // 2026-04-06T23:59:00Z → calendar date = 2026-04-06, H=0 → else branch → same day
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-06T23:59:00Z"),
            policy = RolloverPolicy.Standard,
        )
        assertEquals(LocalDate(2026, 4, 6), fake.logicalDate(TimeZone.UTC))
    }

    // ── US1-Scenario-4: Standard policy (H=0), just past midnight → calendar date ───────
    @Test
    fun `US1-Scenario-4 - standard policy H=0 at 00h01 returns new calendar day`() {
        // 2026-04-07T00:01:00Z → calendar date = 2026-04-07, H=0 → else branch → same day
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-07T00:01:00Z"),
            policy = RolloverPolicy.Standard,
        )
        assertEquals(LocalDate(2026, 4, 7), fake.logicalDate(TimeZone.UTC))
    }

    // ── US1-Scenario-5: Morning rollover — hour BEFORE morning offset → -1 day ──────────
    @Test
    fun `US1-Scenario-5 - morning offset H=3 hour 2 returns previous day`() {
        // 2026-04-07T02:00:00Z → calendar date = 2026-04-07, hour = 2 < 3 → -1 = 2026-04-06
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-07T02:00:00Z"),
            policy = RolloverPolicy.fixed(3),
        )
        assertEquals(LocalDate(2026, 4, 6), fake.logicalDate(TimeZone.UTC))
    }

    // ── Exact boundary: H=18, 18:00:00 exactly → +1 day (inclusive boundary) ────────────
    @Test
    fun `boundary - evening offset 18 at exactly 18h00 returns next day inclusive`() {
        // 2026-04-06T18:00:00Z → calendar date = 2026-04-06, hour = 18 ≥ 18 → +1 = 2026-04-07
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-06T18:00:00Z"),
            policy = RolloverPolicy.fixed(18),
        )
        assertEquals(LocalDate(2026, 4, 7), fake.logicalDate(TimeZone.UTC))
    }

    // ── Standard equals H=0 — RolloverPolicy.Standard and RolloverPolicy(0) are identical ─
    @Test
    fun `standard policy equals RolloverPolicy0 and produces identical results`() {
        val instant = Instant.parse("2026-04-06T21:30:00Z")
        val withStandard = FakeTimeProvider(instant, RolloverPolicy.Standard).logicalDate(TimeZone.UTC)
        val withZero = FakeTimeProvider(instant, RolloverPolicy(0)).logicalDate(TimeZone.UTC)
        assertEquals(withZero, withStandard)
        assertEquals(LocalDate(2026, 4, 6), withStandard) // H=0, no shift
    }

    // ── Morning rollover edge cases (H=6) ────────────────────────────────────────────────
    @Test
    fun `morning offset H=6 hour 10 after offset returns same day`() {
        // 2026-04-07T10:00:00Z → calendar date = 2026-04-07, hour = 10, H=6 in 1..11 but 10 ≥ 6 → else → same
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-07T10:00:00Z"),
            policy = RolloverPolicy.fixed(6),
        )
        assertEquals(LocalDate(2026, 4, 7), fake.logicalDate(TimeZone.UTC))
    }

    @Test
    fun `morning offset H=6 hour 5 before offset returns previous day`() {
        // 2026-04-07T05:00:00Z → calendar date = 2026-04-07, hour = 5 < 6 → -1 = 2026-04-06
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-07T05:00:00Z"),
            policy = RolloverPolicy.fixed(6),
        )
        assertEquals(LocalDate(2026, 4, 6), fake.logicalDate(TimeZone.UTC))
    }

    // ── Time-travel: mutate FakeTimeProvider.fixedInstant mid-test ───────────────────────
    @Test
    fun `time-travel - mutating fixedInstant changes logicalDate deterministically`() {
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-04-06T17:00:00Z"), // before offset
            policy = RolloverPolicy.fixed(18),
        )
        assertEquals(LocalDate(2026, 4, 6), fake.logicalDate(TimeZone.UTC))

        // Travel forward past the rollover threshold
        fake.fixedInstant = Instant.parse("2026-04-06T18:00:00Z")
        assertEquals(LocalDate(2026, 4, 7), fake.logicalDate(TimeZone.UTC))

        // Travel forward to next day
        fake.fixedInstant = Instant.parse("2026-04-07T10:00:00Z")
        assertEquals(LocalDate(2026, 4, 7), fake.logicalDate(TimeZone.UTC))
    }

    // ── DST edge case: Europe/London spring-forward 2026-03-29 ───────────────────────────
    @Test
    fun `DST edge case - Europe-London spring-forward instant does not throw`() {
        // UK clocks spring forward on 2026-03-29 at 01:00 UTC (GMT → BST, UTC+1).
        // 01:30 UTC would be within the skipped hour in local time.
        val fake = FakeTimeProvider(
            fixedInstant = Instant.parse("2026-03-29T01:30:00Z"),
            policy = RolloverPolicy.Standard,
        )
        val tz = TimeZone.of("Europe/London")
        val result = fake.logicalDate(tz)
        assertNotNull(result)
        // At 01:30 UTC on 2026-03-29, BST makes it 02:30 local — calendar date stays 2026-03-29
        assertEquals(LocalDate(2026, 3, 29), result)
    }
}

