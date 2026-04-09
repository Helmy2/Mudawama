# Phase 0: Research Decisions (Prayer Tracking Screen)

**Branch**: `006-prayer-screen` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)

---

## Overview

This document captures the key architectural and design decisions made during Phase 0 planning for the Prayer Tracking feature (FR-1) of Mudawama. These decisions constrain the implementation and ensure it adheres to the project's constitution, clean architecture principles, and offline-first design goals.

## Decision Log

### D-01: Aladhan API Endpoint
**Decision:** Use `GET https://api.aladhan.com/v1/timings/{DD-MM-YYYY}?latitude=&longitude=&method=2`.
**Rationale:** The Aladhan timings endpoint is reliable, free, and returns exact times for a single day based on coordinates and calculation method. Method 2 (Islamic Society of North America) or an equivalent default is used as a standard baseline. The `{DD-MM-YYYY}` format is the required path parameter for this specific date endpoint.

### D-02: Cache Key Strategy
**Decision:** Cache prayer times in the local Room database using the ISO date string (`yyyy-MM-dd`) as the sole unique key.
**Rationale:** The user's location is assumed to be mostly static per day for the purpose of prayer times. Including location coordinates in the cache key would complicate lookups and require tight coupling with the location provider on every database read. Caching by date alone is simpler and fulfils the offline-first requirement (once fetched for today, it works all day regardless of network or location changes).

### D-03: Cache Invalidation Strategy
**Decision:** Implement an "absent-if-no-row-for-today" logic. There is no Time-To-Live (TTL) column. A new calendar day simply requires a new fetch if no row exists for that new date.
**Rationale:** Prayer times do not change intra-day. Once a day's times are fetched and cached, they are valid until midnight. This avoids complex TTL validation logic and aligns with the daily tracker nature of the app.

### D-04: Prayer Seeding Mechanism
**Decision:** Create a `SeedPrayerHabitsUseCase` executed automatically at app startup via a Koin eager singleton. It must be idempotent.
**Rationale:** The 5 obligatory prayers are universal and must always be present. Instead of requiring the user to manually add them, they are seeded automatically. The use case checks if the habit repository already contains 5 habits with `category="prayer"` and skips insertion if true, preventing duplication across app launches.

### D-05: `LogStatus.MISSED` Toggle Rules
**Decision:** A simple tap on a prayer toggles only between `PENDING` and `COMPLETED`. A tap on a `MISSED` prayer changes it to `COMPLETED`. Marking a prayer as `MISSED` is only accessible via a long-press action sheet.
**Rationale:** Tapping should optimise for the happy path (completion). Toggling through a three-state loop (`PENDING` -> `COMPLETED` -> `MISSED` -> `PENDING`) on a single tap is poor UX and prone to accidental taps. The long-press for `MISSED` is an intentional, explicit action.

### D-06: `LocationProvider` Architecture
**Decision:** Define a pure Kotlin `LocationProvider` interface in `shared:core`. Provide platform-specific implementations (`AndroidLocationProvider` using `FusedLocationProviderClient` and `IosLocationProvider` using `CLLocationManager`) in their respective source sets within `shared:core`.
**Rationale:** Avoids the `expect`/`actual` language feature which can be brittle for complex APIs. By injecting the interface via Koin, the domain layer remains pure Kotlin and testable, while platform modules supply the correct hardware implementation.

### D-07: Prayer Habit Identity
**Decision:** Use stable, hardcoded, deterministic UUID-style strings (e.g., `"habit-prayer-fajr-00000000"`) for the core prayer habit IDs.
**Rationale:** Random UUIDs on insertion would make the seeding process non-idempotent across device restores or app re-installs if the database is cleared but remote sync is eventually added. Stable IDs ensure the 5 prayers always have the exact same primary keys.

### D-08: Reusing Habit Completion Logic
**Decision:** `TogglePrayerStatusUseCase` will internally delegate to the existing `ToggleHabitCompletionUseCase` from `feature:habits:domain`.
**Rationale:** Prayers are stored as core habits (`isCore = true`, `category = "prayer"`). The logic for updating a `HabitLogEntity` status in the database is identical. This avoids duplicating core database manipulation logic and prevents drift between habit and prayer tracking.

### D-09: Dedicated Missed Logic
**Decision:** Create a dedicated `MarkPrayerMissedUseCase` rather than expanding the toggle use case to accept a target state.
**Rationale:** `ToggleHabitCompletionUseCase` flips state based on the current state (a pure toggle). Marking as missed is an explicit one-way action triggered from a specific UI gesture (long-press). A dedicated use case keeps the intent explicit and the toggle logic simple.

### D-10: `PrayerName` Enum Ordering
**Decision:** Declare the `PrayerName` enum in strict chronological order: `FAJR`, `DHUHR`, `ASR`, `MAGHRIB`, `ISHA`.
**Rationale:** This allows the ordinal value (`PrayerName.entries`) to be used directly as a sort key for UI presentation, avoiding the need for a separate mapping function to determine display order.
