# Public API Contract: shared:core:time

**Phase**: 1 — Design & Contracts  
**Date**: 2026-04-02  
**Module**: `:shared:core:time`  
**Consumer entry point**: `shared/umbrella-core` (re-exported via `api()`)

---

## Overview

This document defines the stable public surface of the `shared:core:time` library.
All types listed here are exported via `api()` from `shared:umbrella-core` and can be
used by any feature module without declaring a direct dependency on `:shared:core:time`.

Types marked **⚠️ TEST ONLY** ship in `commonMain` but MUST NOT be bound in production
Koin modules.

---

## 1. `RolloverPolicy`

```kotlin
package io.github.helmy2.mudawama.core.time

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
         * @param hour Local hour (1–23).
         */
        fun fixed(hour: Int): RolloverPolicy = RolloverPolicy(hour)
    }
}
```

**Serialisation surface**: `policy.offsetHour: Int` ↔ `RolloverPolicy(storedInt)`.  
No serialisation framework dependency.

---

## 2. `TimeProvider`

```kotlin
package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Single source of truth for the current moment and logical date.
 *
 * Inject via Koin — never construct directly in production code.
 */
interface TimeProvider {

    /**
     * Returns the current moment.
     *
     * Implementations MUST be the ONLY location in the codebase where the
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
```

**DI binding**: `single<TimeProvider> { SystemTimeProvider(policy) }` inside `timeModule()`.

---

## 3. `SystemTimeProvider`

```kotlin
package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.*

/**
 * Production implementation of [TimeProvider] backed by the device system clock.
 *
 * Registered as a Koin singleton by [timeModule]. Do not construct manually.
 *
 * @param policy Rollover policy supplied by the app's DI setup.
 */
class SystemTimeProvider(private val policy: RolloverPolicy) : TimeProvider {

    override fun nowInstant(): Instant = Clock.System.now()

    override fun logicalDate(timeZone: TimeZone): LocalDate {
        val instant = nowInstant()
        val localDateTime = instant.toLocalDateTime(timeZone)
        return computeLogicalDate(localDateTime.date, localDateTime.hour, policy)
    }
}
```

---

## 4. `FakeTimeProvider` ⚠️ TEST ONLY

```kotlin
package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.*

/**
 * Test double for [TimeProvider].
 *
 * ⚠️ **For test use only.** Do not bind in production Koin modules.
 *
 * Ships in `commonMain` so consumer modules can use it in their own `commonTest`
 * without additional Gradle dependency wiring.
 *
 * @param fixedInstant Frozen point in time. Mutable for time-travel test scenarios.
 * @param policy Rollover policy used for [logicalDate].
 */
class FakeTimeProvider(
    var fixedInstant: Instant,
    private val policy: RolloverPolicy = RolloverPolicy.Standard,
) : TimeProvider {

    override fun nowInstant(): Instant = fixedInstant

    override fun logicalDate(timeZone: TimeZone): LocalDate {
        val localDateTime = fixedInstant.toLocalDateTime(timeZone)
        return computeLogicalDate(localDateTime.date, localDateTime.hour, policy)
    }
}
```

---

## 5. Date Formatting Helpers

```kotlin
package io.github.helmy2.mudawama.core.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Formats a [LocalDate] as an ISO-8601 string (`yyyy-MM-dd`).
 *
 * Output is lexicographically sortable and suitable for Room TEXT columns.
 */
fun toIsoDateString(date: LocalDate): String = date.toString()

/**
 * Converts an [Instant] to a `yyyy-MM-dd` string in the specified [timeZone].
 */
fun toIsoDateString(instant: Instant, timeZone: TimeZone): String =
    instant.toLocalDateTime(timeZone).date.toString()
```

---

## 6. Koin Module Factory

```kotlin
package io.github.helmy2.mudawama.core.time.di

import io.github.helmy2.mudawama.core.time.RolloverPolicy
import io.github.helmy2.mudawama.core.time.SystemTimeProvider
import io.github.helmy2.mudawama.core.time.TimeProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates the Koin module for [TimeProvider].
 *
 * Call this once in your app-level Koin setup:
 * ```kotlin
 * startKoin {
 *     modules(
 *         timeModule(rolloverPolicy = RolloverPolicy.fixed(18)),
 *         // other modules …
 *     )
 * }
 * ```
 *
 * @param rolloverPolicy Policy to use. Defaults to [RolloverPolicy.Standard] (midnight).
 */
fun timeModule(rolloverPolicy: RolloverPolicy = RolloverPolicy.Standard): Module = module {
    single<TimeProvider> { SystemTimeProvider(rolloverPolicy) }
}
```

---

## 7. Internal Helper (non-public)

```kotlin
// Internal — NOT part of the public API surface.
// Located in the same package so implementations can call it.

internal fun computeLogicalDate(
    calendarDate: kotlinx.datetime.LocalDate,
    hour: Int,
    policy: RolloverPolicy,
): kotlinx.datetime.LocalDate {
    val h = policy.offsetHour
    return when {
        h >= 12 && hour >= h -> calendarDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
        h in 1..11 && hour < h -> calendarDate.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
        else -> calendarDate
    }
}
```

---

## Acceptance / Breaking-Change Rules

| # | Rule |
|---|------|
| BC-1 | Adding new methods to `TimeProvider` is a **breaking change** — all implementors (including `FakeTimeProvider`) must be updated simultaneously. |
| BC-2 | `RolloverPolicy.offsetHour` is part of the serialisation contract (FR-006). Changing its type or meaning is a **breaking change**. |
| BC-3 | The `timeModule` function signature (parameter name `rolloverPolicy`) is public API. Renaming the parameter is a **source-incompatible change**. |
| BC-4 | `FakeTimeProvider.fixedInstant` is `var` by design. Making it `val` is a **breaking change** for time-travel test scenarios. |
| NC-1 | Adding new companion members to `RolloverPolicy` is **non-breaking**. |
| NC-2 | Adding new overloads to `toIsoDateString` is **non-breaking**. |

