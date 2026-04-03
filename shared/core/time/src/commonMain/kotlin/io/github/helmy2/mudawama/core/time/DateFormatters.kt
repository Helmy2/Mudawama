package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Formats a [LocalDate] as an ISO-8601 string (`yyyy-MM-dd`).
 *
 * Output is always `yyyy-MM-dd`, ISO-8601 compliant, lexicographically sortable,
 * and suitable for Room TEXT columns. Delegates to [LocalDate.toString] which is
 * documented by kotlinx-datetime to always produce the ISO-8601 format.
 *
 * Example: `LocalDate(2026, 1, 7)` → `"2026-01-07"`
 */
fun toIsoDateString(date: LocalDate): String = date.toString()

/**
 * Converts an [Instant] to a `yyyy-MM-dd` string in the specified [timeZone].
 *
 * The [instant] is first converted to a [kotlinx.datetime.LocalDateTime] using the
 * provided [timeZone] (applying IANA timezone rules including DST), then the date
 * component is formatted as `yyyy-MM-dd`.
 *
 * Example: `Instant.parse("2026-12-31T23:30:00Z")` in `"Asia/Riyadh"` (UTC+3)
 * → `"2027-01-01"` (the instant falls on Jan 1 in Riyadh local time).
 *
 * @param instant   The moment to convert.
 * @param timeZone  The timezone for the local date resolution.
 */
fun toIsoDateString(instant: Instant, timeZone: TimeZone): String =
    instant.toLocalDateTime(timeZone).date.toString()

