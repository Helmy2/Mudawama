# Data Model: shared:core:time

**Phase**: 1 — Design & Contracts  
**Date**: 2026-04-02  
**Feeds**: [plan.md](./plan.md), [contracts/time-provider-api.md](./contracts/time-provider-api.md)

---

## Overview

`shared:core:time` contains no persisted entities. All types below are **in-memory value
and service types** that live only in the running process. They represent the domain
vocabulary for time within Mudawama.

---

## Entity 1 — `RolloverPolicy`

**Kind**: Immutable value type (`data class`)  
**Package**: `io.github.helmy2.mudawama.core.time`  
**File**: `RolloverPolicy.kt`

### Fields

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `offsetHour` | `Int` | `0..23` | Hour (local time) at which the logical day resets. `0` = midnight = Standard policy. |

### Named Constructors / Companions

| Name | `offsetHour` | Semantics |
|------|-------------|-----------|
| `RolloverPolicy.Standard` | `0` | Logical day starts at midnight — identical to calendar day. |
| `RolloverPolicy.fixed(hour: Int)` | `hour` | Logical day starts at the given local hour. |

### Serialisation Contract (FR-006)

`RolloverPolicy` is fully represented by its `offsetHour` integer. Callers persist and
restore via:

```kotlin
val stored: Int = policy.offsetHour         // write to preferences
val restored = RolloverPolicy(stored)        // read from preferences
```

No dependency on `kotlinx-serialization` or any other serialisation library is required or introduced.

### State Transitions

None — `RolloverPolicy` is immutable. Policy changes take effect at the next call to
`TimeProvider.logicalDate()`; historical records are not retroactively modified (spec §Edge Cases).

### Rollover Algorithm (from research Decision 2)

Given `calendarDate` and `hour = instant.toLocalDateTime(tz).hour`:

```
H = policy.offsetHour

logicalDate =
  when {
    H >= 12 && hour >= H  -> calendarDate + 1   // Evening rollover (Islamic-style)
    H in 1..11 && hour < H -> calendarDate - 1  // Morning rollover (night-owl)
    else                   -> calendarDate        // Standard / within window
  }
```

---

## Entity 2 — `TimeProvider` (Interface)

**Kind**: Interface  
**Package**: `io.github.helmy2.mudawama.core.time`  
**File**: `TimeProvider.kt`

### Methods

| Method | Return type | Description |
|--------|-------------|-------------|
| `nowInstant(): Instant` | `kotlinx.datetime.Instant` | Returns the current moment in time. |
| `logicalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate` | `kotlinx.datetime.LocalDate` | Returns the *logical* date under the active `RolloverPolicy` in the given timezone. Defaults to device timezone. |

### Invariants

- Multiple calls within the same wall-clock instant MUST return consistent results for the
  same timezone (no hidden state drift).
- `logicalDate()` MUST delegate entirely to `nowInstant()` + the rollover algorithm; it
  introduces no independent clock access.

---

## Entity 3 — `SystemTimeProvider`

**Kind**: `class` implementing `TimeProvider`  
**Package**: `io.github.helmy2.mudawama.core.time`  
**File**: `SystemTimeProvider.kt`

### Constructor

| Parameter | Type | Default | Notes |
|-----------|------|---------|-------|
| `policy` | `RolloverPolicy` | — | Injected at construction time (from Koin module factory). |

### Behaviour

- `nowInstant()` → `Clock.System.now()` (the only location in the entire codebase where
  `Clock.System` is accessed — enforced by CI grep rule SC-002).
- `logicalDate(timeZone)` → applies the rollover algorithm to `nowInstant()`.

---

## Entity 4 — `FakeTimeProvider`

**Kind**: `class` implementing `TimeProvider`  
**Package**: `io.github.helmy2.mudawama.core.time`  
**File**: `FakeTimeProvider.kt`

> ⚠️ **For test use only.** Do not bind in production Koin modules.
> Ships in `commonMain` so consumer modules can reach it from `commonTest`.

### Constructor

| Parameter | Type | Default | Notes |
|-----------|------|---------|-------|
| `fixedInstant` | `Instant` | — | The frozen point in time returned by every `nowInstant()` call. |
| `policy` | `RolloverPolicy` | `RolloverPolicy.Standard` | Rollover policy used for `logicalDate()`. |

### Behaviour

- `nowInstant()` always returns `fixedInstant` — no clock access.
- `logicalDate(timeZone)` applies the rollover algorithm to `fixedInstant`.
- The fixed instant can be updated between test steps via a `var fixedInstant` property to
  allow time-travel scenarios within a single test.

---

## Entity 5 — `DateFormatters` (top-level functions)

**Kind**: Top-level extension / standalone functions  
**Package**: `io.github.helmy2.mudawama.core.time`  
**File**: `DateFormatters.kt`

### Functions

| Signature | Return type | Notes |
|-----------|-------------|-------|
| `toIsoDateString(date: LocalDate): String` | `String` | Delegates to `date.toString()` which is documented to produce `yyyy-MM-dd`. |
| `toIsoDateString(instant: Instant, timeZone: TimeZone): String` | `String` | Converts `instant` to `LocalDateTime` in `timeZone`, then calls `date.toString()`. |

### Formatting Contract

Output is always `yyyy-MM-dd` (e.g., `"2026-04-02"`), ISO-8601 compliant, lexicographically
sortable. Suitable for direct storage in the Room database as a `TEXT` column.

---

## Entity 6 — `TimeModule` (Koin module factory)

**Kind**: Top-level function  
**Package**: `io.github.helmy2.mudawama.core.time.di`  
**File**: `di/TimeModule.kt`

### Signature

```kotlin
fun timeModule(
    rolloverPolicy: RolloverPolicy = RolloverPolicy.Standard
): org.koin.core.module.Module
```

### Behaviour

- Returns a Koin `Module` that registers `TimeProvider` as a **singleton** backed by
  `SystemTimeProvider(rolloverPolicy)`.
- The policy is closed over at module creation time; no runtime `parametersOf()` calls are
  needed at injection sites.
- Consumers call `startKoin { modules(timeModule(RolloverPolicy.fixed(18))) }`.
- Default policy is `Standard` (midnight) when called with no arguments (FR-011 + US4-S2).

---

## Concept Glossary

| Term | Definition |
|------|------------|
| **Logical Date** | A `LocalDate` derived from the current `Instant` + active `RolloverPolicy` + timezone. NOT the calendar midnight date when an evening policy is active. |
| **Calendar Date** | The plain wall-clock `LocalDate` at midnight in the device timezone — the raw output of `instant.toLocalDateTime(tz).date`. |
| **Rollover Boundary** | The local time at which the logical day advances. `18:00` → Islamic Maghrib approximation. `0` → midnight = Standard. |
| **Standard Policy** | `RolloverPolicy(0)` — logical date equals calendar date. |
| **Fixed Offset Policy** | `RolloverPolicy(H)` where `H ∈ 1..23`. |

