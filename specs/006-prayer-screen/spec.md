# Feature Specification: Prayer Tracking Screen

**Feature Branch**: `006-prayer-screen`
**Created**: 2026-04-08
**Status**: Draft
**SRS Reference**: FR-1 (Prayer Tracking System)

---

## Overview

Replace the Prayer placeholder screen with a fully functional prayer tracking experience. The screen shows today's 5 obligatory prayers in chronological order alongside their scheduled times (fetched from a remote prayer-times service and cached locally). Users can mark each prayer as completed or missed. A daily completion summary is shown at the top. A 7-day date strip lets users review past days. Prayer time data is always available offline once it has been fetched once.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — View and Mark Today's Prayers (Priority: P1)

A user opens the Prayers tab and immediately sees all 5 prayers listed in chronological order (Fajr → Dhuhr → Asr → Maghrib → Isha) with their scheduled times and current status. They tap the check button next to a prayer to mark it completed. The daily completion counter updates in real time (e.g., "2 / 5").

**Why this priority**: This is the core value proposition of the prayer feature. Without it no other story delivers meaningful value.

**Independent Test**: Seed 5 prayers as core habits, navigate to the Prayers tab, verify all 5 appear with times and an unchecked status, tap one — verify counter changes from 0/5 to 1/5 and the row shows the completed state.

**Acceptance Scenarios**:

1. **Given** the app is open on today's date, **When** the user taps Prayers in the bottom nav, **Then** all 5 prayers are listed in chronological order with their scheduled times and status icons.
2. **Given** a prayer is in PENDING status, **When** the user taps its check button, **Then** its status changes to COMPLETED, the icon fills with the teal check mark, and the "X / 5" counter increments.
3. **Given** a prayer is already COMPLETED, **When** the user taps its check button again, **Then** its status reverts to PENDING (toggle behaviour).
4. **Given** the screen is loaded, **Then** the daily completion hero card shows the correct count of COMPLETED prayers out of 5.

---

### User Story 2 — Mark a Prayer as Missed (Priority: P2)

A user realises they missed Fajr earlier in the day. They long-press (or use a secondary action) on the Fajr row and mark it as "Missed". The row visually distinguishes missed prayers from pending and completed ones so the user has an honest record.

**Why this priority**: FR-1.2 explicitly requires MISSED as a status. Without it users cannot distinguish "not done yet" from "definitely missed", undermining the accountability goal.

**Independent Test**: Long-press a prayer row, select "Mark as Missed" from the action sheet, verify the row shows a missed indicator distinct from the pending empty circle and the completed teal check.

**Acceptance Scenarios**:

1. **Given** a prayer is PENDING, **When** the user long-presses it and selects "Mark as Missed", **Then** the row shows a missed indicator (e.g., a greyed-out or red-tinted X icon) and the completion counter does not increment.
2. **Given** a prayer is MISSED, **When** the user taps the check button, **Then** its status changes to COMPLETED (recovery path).
3. **Given** a prayer is MISSED, **When** the user long-presses and selects "Undo / Mark Pending", **Then** its status reverts to PENDING.

---

### User Story 3 — Browse Past Days (Priority: P2)

A user wants to check whether they prayed yesterday. They tap a previous date on the 7-day horizontal date strip at the top of the screen. The prayer list and completion hero update to reflect that day's logged statuses. Times shown are those that applied on the selected date. The selected day is visually highlighted (teal pill, matching the reference design).

**Why this priority**: Reviewing past days is essential for accountability. The date strip is shown prominently in the reference design and the SRS requires chronological display of daily state.

**Independent Test**: Navigate to yesterday in the date strip, verify the prayer rows show whatever statuses were logged for that date (or all PENDING if no log exists), then navigate back to today and verify today's state is restored.

**Acceptance Scenarios**:

1. **Given** the date strip shows a 7-day window centred on today, **When** the user taps a past date, **Then** the prayer list and completion hero update to that date's data.
2. **Given** any non-today date is selected (past or future), **Then** all check and missed actions are disabled — the list is read-only.
3. **Given** today is selected, **When** any prayer is toggled, **Then** the change persists and is immediately visible.

---

### User Story 4 — Offline-First Prayer Times (Priority: P3)

A user opens the app with no internet connection. Prayer times for today are already available because they were fetched and cached during the last successful network call. The screen loads without any spinner or error state.

**Why this priority**: The SRS (NFR-5.1) requires offline-first operation. Prayer times must never block the user.

**Independent Test**: Fetch times once with network available. Disable the network. Restart the app. Confirm prayer times are displayed without a network call.

**Acceptance Scenarios**:

1. **Given** prayer times were previously fetched, **When** the app is opened with no network, **Then** times are displayed from the local cache with no error.
2. **Given** prayer times have never been fetched AND there is no network, **Then** the screen shows a placeholder time (e.g., "—") with a prompt to connect to fetch times, and all status toggles remain functional.
3. **Given** cached times are from a previous day AND network is available, **When** the screen loads, **Then** the app silently refreshes times in the background.

---

### Edge Cases

- What if the device location/timezone cannot be determined? → Use a saved last-known location or a user-selectable city as fallback; never crash.
- What if the remote prayer-times API returns an error? → Serve cached times silently; show a subtle retry indicator only if no cache exists for today.
- What if the user crosses midnight while the app is open? → The date strip and prayer list must update to the new day without requiring a restart.
- What if the user's timezone changes (travel)? → The cache is keyed by calendar date only. After midnight in the new timezone, the next screen load triggers a fresh fetch using the current location. No mid-day cache busting on location change (post-MVP).
- What happens if all 5 prayers are marked COMPLETED? → The completion hero shows "5 / 5" and the ring is fully filled.
- What if a prayer time is in the past when the screen first loads? → Its row is still shown in order; no automatic status change happens.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST seed the 5 obligatory prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) into the existing habits table as core habits (`isCore = true`) on first launch if not already present, so they appear on the Prayer screen and the Habits screen.
- **FR-002**: The `LogStatus` enum MUST be extended to include a `MISSED` value alongside the existing `PENDING` and `COMPLETED` values, with the following constraints to prevent breaking changes:
  - Every `when(status)` expression that exhausts `LogStatus` across `feature:habits` and `feature:prayer` MUST add an explicit `MISSED` branch; no `else` shortcut is permitted.
  - The primary check-button toggle cycle is strictly `PENDING → COMPLETED → PENDING`. `MISSED` is never entered via the tap toggle; it is only reachable via the long-press action sheet (FR-010). Tapping the check button on a MISSED prayer sets it to COMPLETED (recovery), not PENDING.
  - Because `LogStatus` is stored as a plain `String` in `HabitLogEntity`, adding `MISSED` requires no database schema migration — existing rows with `"PENDING"` or `"COMPLETED"` continue to deserialise correctly. Any row with an unrecognised status value MUST be treated as `PENDING` at the mapper layer.
- **FR-003**: The system MUST fetch daily prayer times from the Aladhan public API using the device's current location (latitude, longitude) and timezone.
- **FR-004**: Fetched prayer times MUST be cached locally (keyed by date and location) so that subsequent loads within the same day do not require a network call.
- **FR-005**: Cached prayer times are valid for the calendar day they were fetched. When a new calendar day begins (midnight crossing detected via `TimeProvider`), the cache for that day is considered absent and a fresh fetch is triggered on the next screen load. Location-drift-based invalidation is deferred to a post-MVP iteration.
- **FR-006**: The Prayer screen MUST display all 5 prayers in chronological order, each showing: prayer name, scheduled time in 12-hour format, icon, and current status (PENDING / COMPLETED / MISSED).
- **FR-007**: The Prayer screen MUST show a daily completion hero card displaying the count of COMPLETED prayers out of 5, with a circular progress ring.
- **FR-008**: The Prayer screen MUST include a horizontal 7-day date strip; the current day is highlighted; tapping a past date updates the list to show that day's logged statuses.
- **FR-009**: Users MUST be able to tap a prayer row to cycle its status: PENDING → COMPLETED (primary action via check button).
- **FR-010**: Users MUST be able to mark a prayer as MISSED via a long-press context action or secondary action sheet on a prayer row.
- **FR-011**: Any date other than today displayed via the date strip is read-only. This includes both past days and future days — users cannot pre-mark a prayer as completed or missed before its calendar date arrives. Status-change controls (check button, long-press sheet) are visually disabled and non-interactive for non-today dates.
- **FR-012**: The Prayer screen MUST replace the existing `PrayerPlaceholderScreen` and be reachable via the Prayers tab in the bottom navigation bar.

### Key Entities

- **Prayer**: A fixed, named obligatory prayer (one of 5). Represented as a core habit in the existing habits table. Attributes: name (Fajr / Dhuhr / Asr / Maghrib / Isha), icon key, fixed daily frequency (all 7 days), type BOOLEAN, `isCore = true`.
- **PrayerTime**: The scheduled time for a specific prayer on a specific calendar date at a specific location. Fetched remotely, cached locally. Attributes: prayer name, date, time (HH:mm), location key (lat/lon or city).
- **PrayerLog**: A daily record of a prayer's completion state. Reuses the existing `HabitLog` structure. Status is one of: PENDING, COMPLETED, MISSED.
- **PrayerTimesCache**: Local store of fetched prayer times. Keyed by calendar date. One cache entry covers all 5 prayer times for that date. Considered absent (triggers re-fetch) when a new calendar day begins.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All 5 prayers appear on screen within 1 second of navigating to the Prayers tab when prayer times are cached locally.
- **SC-002**: A user can mark a prayer as completed or missed in 1 tap / 2 taps respectively with no intermediate loading state.
- **SC-003**: The daily completion counter and progress ring update within 200 ms of a status change, with no full-screen reload.
- **SC-004**: Prayer times are available offline for any date on which they were previously fetched, with zero visible error state.
- **SC-005**: The first-time prayer times fetch (fresh install, network available) completes in under 3 seconds on a standard 4G connection.
- **SC-006**: Switching between dates in the 7-day strip shows the correct historical data within 500 ms.
- **SC-007**: The `MISSED` status is correctly persisted and survives app restarts — it does not revert to PENDING.

---

## Assumptions

- The Aladhan API endpoint `https://api.aladhan.com/v1/timings` is used; it accepts latitude, longitude, and date as parameters and returns times in 24-hour format. The `method` parameter defaults to 2 (ISNA) and is not user-configurable in this iteration.
- Device location permission is assumed to already be requested by the app before the Prayer screen loads. If denied, the app falls back to a hardcoded default location (Mecca) and shows a subtle notice. A new `LocationProvider` interface will be added to `shared:core`, with platform-specific implementations (Android: FusedLocationProviderClient; iOS: CLLocationManager) wired via `expect`/`actual`. This interface exposes a single suspend function returning the last-known latitude and longitude.
- Prayer times are fetched per calendar day. One API call covers all 5 prayers for that day.
- The existing `HabitLog` / `HabitLogEntity` structures are reused directly for prayer logs, with the addition of the `MISSED` status. No separate prayer log table is introduced.
- Prayer habit seeds are inserted via a database migration or app-startup check; they are never re-inserted if already present (idempotent seeding).
- The 7-day date strip shows 3 past days + today + 3 future days. All non-today dates (both past and future) are read-only — prayer status cannot be changed for any date other than today.
- Calculation method selection and Qibla direction are out of scope for this feature.

---

## Dependencies

- `shared:core:database` — `HabitEntity`, `HabitLogEntity`, `HabitDao`, `HabitLogDao` (existing).
- `LogStatus` enum in `feature:habits:domain` — must be extended with `MISSED`; this is a shared enum affecting both the habits and prayer features.
- `feature:prayer:data` — new module; adds Ktor Client dependency for Aladhan API calls.
- `LocationProvider` — new interface in `shared:core`; provides last-known latitude and longitude via platform-specific implementations (Android: FusedLocationProviderClient; iOS: CLLocationManager).
- `TimeProvider` from `shared:core:time` — for logical date and midnight-crossing detection.
