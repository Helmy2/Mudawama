# Feature Specification: shared:core:time — Centralised Time & Logical Date Module

**Feature Branch**: `001-add-core-time`  
**Created**: 2026-04-02  
**Status**: Draft  
**Input**: User description: "Build shared:core:time module — single source of truth for date/time operations with Islamic logical-date rollover support"

---

## Overview

**Purpose**: Provide a single, injectable time-provider abstraction and a set of logical-date helpers that every feature module in Mudawama uses when it needs to know "what day is it?". Because Islamic tradition considers the new day to begin at Maghrib (sunset), habit logs must be stamped against the *logical* date rather than the calendar midnight date. This module owns that determination and exposes it as a replaceable interface so time can be frozen in tests.

**Goals**:
- Eliminate direct system-clock calls scattered across feature modules.
- Implement configurable rollover policies so the logical date calculation adapts to the user's preference (midnight or a fixed hour such as Maghrib time).
- Provide reliable ISO-8601 date-string formatting helpers consumed by the database module.
- Wire everything into Koin so consumers receive the service through dependency injection.

**Scope**: `shared/core/time/` — 100 % shared (commonMain) code. No platform-specific source sets. No UI.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Habit logging records the correct logical date (Priority: P1)

A user completes their evening Wird at 9 PM. Because the rollover is configured to 18:00 (Maghrib approximation), the app considers 9 PM to already be *the next calendar day* in Islamic reckoning. The habit log is therefore stamped with tomorrow's calendar date, which is the correct logical date for that session.

**Why this priority**: Incorrect date stamping corrupts every streak, heatmap, and history view downstream. This is the primary reason the module exists.

**Independent Test**: Can be fully tested by asking the time module "what is the logical date at 21:00 when the rollover offset is 18:00?" and verifying the answer is tomorrow's calendar date.

**Acceptance Scenarios**:

1. **Given** the rollover policy is *Fixed Offset at 18:00*, **When** the current time is 21:00 on Monday the 1st, **Then** the logical date is Tuesday the 2nd.
2. **Given** the rollover policy is *Fixed Offset at 18:00*, **When** the current time is 17:59 on Monday the 1st, **Then** the logical date is Monday the 1st.
3. **Given** the rollover policy is *Standard (midnight)*, **When** the current time is 23:59 on Monday the 1st, **Then** the logical date is Monday the 1st.
4. **Given** the rollover policy is *Standard (midnight)*, **When** the current time is 00:01 on Tuesday the 2nd, **Then** the logical date is Tuesday the 2nd.
5. **Given** the rollover policy is *Fixed Offset at 03:00* (night-owl mode), **When** the current time is 02:00, **Then** the logical date is still the previous calendar day.

---

### User Story 2 — Unit tests freeze time to verify business logic (Priority: P2)

A developer writing tests for the Habits feature needs to simulate "it is 11:50 PM on a Friday" without relying on the actual device clock. They inject a test-double time provider seeded with a fixed instant and run assertions against rollover-sensitive logic.

**Why this priority**: Without a replaceable time source the entire test suite for every feature that touches dates becomes unreliable and non-deterministic.

**Independent Test**: Can be tested by injecting a fixed-time double, calling `getLogicalDate()`, and asserting the result matches the pre-computed expected date.

**Acceptance Scenarios**:

1. **Given** a test-double time provider is seeded with a specific instant, **When** any feature calls `getLogicalDate()`, **Then** it receives the deterministic date corresponding to that instant and the configured policy.
2. **Given** the test-double is seeded, **When** the same instant is queried multiple times, **Then** the result is identical every time (no hidden wall-clock drift).

---

### User Story 3 — Database layer formats dates consistently (Priority: P3)

A developer writing a DAO query needs to persist a `LocalDate` as a string in the Room database. They call the module's formatter and receive a well-formed `yyyy-MM-dd` string that is both human-readable and lexicographically sortable.

**Why this priority**: Consistent string formatting prevents subtle bugs where dates written by one module cannot be parsed by another.

**Independent Test**: Can be tested by passing a known `LocalDate` to the formatter and asserting the output string equals the expected ISO value.

**Acceptance Scenarios**:

1. **Given** a `LocalDate` of 2026-01-07, **When** the formatter is called, **Then** the output string is `"2026-01-07"`.
2. **Given** an `Instant` that corresponds to 2026-12-31 at 23:00 UTC, **When** the instant-to-date formatter is called with timezone UTC, **Then** the output string is `"2026-12-31"`.

---

### User Story 4 — Feature modules receive the time service via dependency injection (Priority: P4)

A developer adds the time module to their feature's Gradle dependencies and simply requests a `TimeProvider` from Koin. No manual wiring, no platform-specific factory calls.

**Why this priority**: Consistent DI patterns are a core architectural requirement per the project constitution. Deviating from this would break conventions across the codebase.

**Independent Test**: Can be tested by loading the Koin module in a test and resolving the `TimeProvider`; it must succeed without errors.

**Acceptance Scenarios**:

1. **Given** the Koin module for `shared:core:time` is loaded, **When** a consumer requests `TimeProvider`, **Then** the same singleton instance is returned every time.
2. **Given** no additional configuration, **When** the module is loaded with no rollover preference supplied, **Then** it defaults to Standard (midnight) rollover.

---

### Edge Cases

- What is the logical date when the current time is exactly at the rollover boundary (e.g., exactly 18:00:00)?
  → The rollover is inclusive of the boundary second: 18:00:00 is already "the next logical day".
- What happens when the rollover offset is `0` (i.e., midnight)?
  → Behaviour must be identical to the Standard policy — no ambiguity at midnight.
- How does the system handle users who change the rollover policy mid-day?
  → The logical date is always computed from the *current* policy at query time; historical logs are not retroactively re-stamped (that is the responsibility of the caller).
- What if the user's device is in a timezone far from UTC (e.g., UTC+12)?
  → The module must use the device's local timezone when converting instants to local time for logical-date calculation.
- What happens on days with a Daylight Saving Time transition where a local hour repeats or is skipped?
  → The module delegates to `kotlinx-datetime` for timezone arithmetic; ambiguous or gap instants resolve per that library's documented rules, which must be covered by a test.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The module MUST expose a `TimeProvider` interface with at minimum a method to obtain the current `Instant` and a method to obtain the current `LogicalDate` (a `LocalDate` derived from the current instant and the active rollover policy).
- **FR-002**: The `TimeProvider` interface MUST be injectable via Koin and provided as a singleton.
- **FR-003**: The module MUST include a `SystemTimeProvider` implementation that reads the current time from the device system clock.
- **FR-004**: The module MUST include a `FakeTimeProvider` (or equivalent test double) that accepts a fixed `Instant` and returns deterministic values — this double MUST be accessible from test source sets of consumer modules.
- **FR-005**: The module MUST support at least two rollover policies:
  - **Standard**: logical day starts at 00:00 local time (midnight).
  - **Fixed Offset**: logical day starts at a configurable hour (0–23) in local time.
- **FR-006**: The rollover policy MUST be represented as a value type that is serialisable to/from a plain integer (the offset hour) so it can be stored in user preferences without coupling to this module's internal types.
- **FR-007**: The module MUST provide a `toIsoDateString(LocalDate): String` formatting helper that returns a `yyyy-MM-dd` string.
- **FR-008**: The module MUST provide a `toIsoDateString(Instant, TimeZone): String` helper that converts an `Instant` to a `yyyy-MM-dd` string in the specified timezone.
- **FR-009**: All source code MUST reside in `commonMain`; there must be no `androidMain`, `iosMain`, or any other platform-specific source sets in this module.
- **FR-010**: The module MUST include unit tests that cover:
  - Logical date at times before and after the rollover boundary for both rollover policies.
  - The exact rollover boundary second (inclusive).
  - The Standard policy being equivalent to Fixed Offset at hour 0.
  - Date formatting for a representative set of `LocalDate` and `Instant` values.
- **FR-011**: The Koin DI module MUST accept the rollover policy as an external parameter (provided by the app-level DI setup) so it is not hard-coded inside the time module.

### Key Entities

- **TimeProvider**: The central interface; represents the capability to observe the current moment and determine the current logical date under a given policy. Does not hold mutable state itself — it delegates to the clock and policy.
- **RolloverPolicy**: A value describing when the logical day resets. Carries a single `offsetHour: Int` (0 = midnight / Standard; 18 = Maghrib approximation). The two named cases (Standard, Fixed Offset) are instances of this type.
- **LogicalDate**: A `LocalDate` produced by applying a `RolloverPolicy` to a raw `Instant` in a specific `TimeZone`. Not a separate type — it is a plain `LocalDate` returned by the provider, but the concept is documented as a named term to avoid confusion with calendar dates.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every unit test in the module passes on all supported platforms (Android JVM, iOS Simulator) in under 5 seconds total test-suite execution time.
- **SC-002**: Zero direct calls to the system clock (or equivalent platform APIs) exist anywhere in `shared/core` or `feature/*/domain` — verified by a static grep check in CI.
- **SC-003**: The Koin module resolves `TimeProvider` in under 50 ms during app startup on a low-end reference device.
- **SC-004**: Rollover logic test coverage reaches 100 % branch coverage for the core calculation function (before-boundary, at-boundary, after-boundary, both policies).
- **SC-005**: Any feature module that previously called the system clock directly can be updated to use `TimeProvider` with a diff of no more than 10 lines of changed code per call site.
- **SC-006**: The formatting helpers produce output that round-trips through the Room database and back without data loss (verified by an integration test in the database module that uses the formatter).

---

## Assumptions

- `kotlinx-datetime` is available in the version catalog (or will be added as part of implementing this spec) — it is the only date/time dependency; no third-party date libraries are introduced.
- The device timezone is obtained via `kotlinx-datetime`'s `TimeZone.currentSystemDefault()` within the `SystemTimeProvider`; this is the standard KMP approach and requires no platform-specific code.
- User preferences (which rollover policy to use) are stored and retrieved by the Settings/Preferences layer (out of scope for this module); this module consumes the policy value as an injected parameter.
- The `FakeTimeProvider` is shipped in the main `commonMain` source set (not a separate test artifact) so consumer modules can use it in their own tests without complex dependency gymnastics. It is clearly annotated/documented as a test helper.
- DST (Daylight Saving Time) edge cases are delegated entirely to `kotlinx-datetime`; no custom DST handling is needed.
- The module does NOT own the Islamic prayer-times calculation (that is a separate future module); it only provides the configurable rollover boundary as a static hour value.
