# Feature Specification: Quran Reading Tracker

**Feature Branch**: `007-quran-tracking`  
**Created**: 2026-04-10  
**Status**: Draft  
**Input**: User description: "Quran Tracking screen — log daily reading (pages or Surah/Ayah range), reading streak and weekly consistency, daily goal (pages/day), navigate past days read-only like the prayer screen"

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Log Today's Quran Reading (Priority: P1)

A user finishes a reading session and wants to record how many pages they read today. They open the Quran tab, tap "Log Reading", adjust a page counter using the stepper or quick-add chips (+1, +5, 1 Juz), then tap "Done". The main screen immediately reflects their updated progress on the daily progress ring and the running total for the day.

**Why this priority**: This is the primary daily interaction. Without the ability to log reading, the screen has no purpose. Delivering this alone constitutes a usable MVP for the Quran tab.

**Independent Test**: Can be fully tested by logging pages from zero and observing the progress ring update, delivering the core tracking value.

**Acceptance Scenarios**:

1. **Given** the Quran screen is open and today's logged pages = 0, **When** the user taps "Log Reading", adjusts to 5 pages, and taps "Done", **Then** the daily progress ring shows 5 of [goal] pages and a Recent Log entry for today appears.
2. **Given** the user has already logged 5 pages today, **When** they log 3 more pages via the bottom sheet, **Then** the total for today becomes 8 pages (additive, not replacement).
3. **Given** the user opens the Log Reading sheet, **When** they tap the "+1 Page" chip, **Then** the stepper increments by 1; tapping "+5 Pages" increments by 5; tapping "1 Juz" increments by 20 (standard Juz = 20 pages).
4. **Given** the Log Reading sheet is open, **When** the user taps "Done" without changing the counter (value = 0), **Then** no log entry is created and the sheet dismisses silently.
5. **Given** the user is viewing a past day (read-only), **When** they attempt to open "Log Reading", **Then** the Log Reading button is not available / disabled.

---

### User Story 2 — View Daily Progress & Goal (Priority: P2)

A user opens the Quran tab and instantly sees how many pages they have read today relative to their daily goal, displayed as a large progress ring with the count and goal visible. A Goal card below shows the active target and a motivational subtitle. If no goal is set, the app uses a sensible default of 5 pages.

**Why this priority**: Context and motivation for logging — users need to see progress at a glance before deciding to log. Builds directly on Story 1.

**Independent Test**: Can be tested by setting a goal, logging pages, and verifying the ring and numeric display reflect the correct ratio.

**Acceptance Scenarios**:

1. **Given** the user's goal is 10 pages and they have read 6 today, **When** the Quran screen loads, **Then** the progress ring shows approximately 60% filled, with "6 OF 10 PAGES" displayed in the center.
2. **Given** the user has never set a goal, **When** the Quran screen loads, **Then** a default goal of 5 pages is applied and displayed on the Goal card.
3. **Given** the user has met or exceeded the daily goal, **When** the screen loads, **Then** the ring is shown as fully complete (100%) and the motivational subtitle reflects completion.
4. **Given** the user taps the Goal card, **When** the Daily Quran Goal bottom sheet opens, **Then** the current goal value is pre-filled in the stepper and popular goal chips (1 Page, 5 Pages, 10 Pages, 1 Juz) are shown.
5. **Given** the user changes the goal from 5 to 10 pages and taps "Save", **Then** the Goal card immediately reflects the new target and the progress ring re-calculates against the new goal.

---

### User Story 3 — Update Reading Position (Bookmark) (Priority: P3)

A user wants to record exactly where they stopped in the Quran so they can resume from the right spot next time. When the user taps "Done" in the Log Reading sheet, the bookmark automatically advances to the surah that starts on the target page (current bookmark's surah startPage + pages logged), eliminating the need for manual entry in the common case. For precise mid-surah positioning, the user can tap the "Resume Reading" card to open the "Update Position" sheet and pick any Surah and Ayah manually.

**Why this priority**: Enhances the reading workflow but is not required to track daily totals. The auto-advance covers the common case; manual override remains available.

**Independent Test**: Can be tested by logging pages and verifying the "Resume Reading" card advances to the correct surah, and separately by manually overriding via the Update Position sheet.

**Acceptance Scenarios**:

1. **Given** the user's bookmark is at Al-Baqarah (page 2) and they log 50 pages, **When** they tap "Done", **Then** the bookmark automatically advances to the surah that begins on or before page 52 (Al-Baqarah, since it spans pages 2–49; actual result: Aal-E-Imran, page 50) without any extra sheet opening.
2. **Given** no bookmark has ever been set, **When** the user logs 5 pages, **Then** the bookmark is set to Al-Fatihah (page 1 + 5 = page 6, still Al-Fatihah).
3. **Given** the auto-advanced bookmark is not precise enough, **When** the user taps the "Resume Reading" card, **Then** the Update Position sheet opens with the current surah pre-selected, and they can scroll the ayah picker to a specific verse.
4. **Given** the Update Position sheet is open, **When** the user selects "Al-Baqarah" from the Surah list, **Then** the Ayah picker on the right updates its maximum value to 286 (total ayahs in Al-Baqarah).
5. **Given** the user types "imran" in the Search Surah field, **When** the list filters, **Then** only "Aal-E-Imran" (and similar matches) are visible.
6. **Given** the user selects Surah 2, Ayah 142, and taps "Done", **When** they return to the main Quran screen, **Then** the "Resume Reading" card shows "Surah Al-Baqarah / Ayah 142".
7. **Given** no bookmark has ever been set, **When** the "Resume Reading" card is shown, **Then** it displays a prompt to set a position (e.g., "Tap to set your reading position").

---

### User Story 4 — View Reading Streak & Recent Logs (Priority: P4)

A user wants to see how consistent they have been with their daily Quran reading. The "Recent Logs" section at the bottom of the Quran screen shows the last several entries with the date, pages read, and a status label (OVER GOAL / UNDER GOAL / HIT GOAL). A "VIEW ALL" link reveals the full log history.

**Why this priority**: Motivational and retrospective; relies on data accumulated from Stories 1–2. Adds engagement but is not required to begin using the tracker.

**Independent Test**: Can be tested by logging readings on multiple days and verifying log rows appear with correct labels and streak increments correctly.

**Acceptance Scenarios**:

1. **Given** the user has logged reading on 3 consecutive days, **When** they open the Quran screen, **Then** the streak count shown is 3.
2. **Given** a full calendar day passes with zero pages logged, **When** the next day begins, **Then** the streak resets to 0.
3. **Given** yesterday's log was 12 pages against a 10-page goal, **When** the Recent Logs row for yesterday is shown, **Then** the status label reads "OVER GOAL".
4. **Given** a day's log was 8 pages against a 10-page goal, **When** the row is displayed, **Then** the label reads "UNDER GOAL".
5. **Given** a day's log exactly matched the goal, **When** the row is displayed, **Then** the label reads "HIT GOAL".
6. **Given** the user taps "VIEW ALL", **When** the full history loads, **Then** all historical log entries are shown in reverse-chronological order.

---

### User Story 5 — Navigate Past Days (Read-Only) (Priority: P5)

A user wants to review what they read on a previous day. A horizontal date strip (matching the prayer screen pattern) allows them to scroll back through recent dates. When a past day is selected, the screen shows that day's logged pages, the goal active on that day, and the log entries — all read-only (no Log Reading button).

**Why this priority**: Parity with the prayer screen navigation pattern. Useful for review but does not affect core logging.

**Independent Test**: Can be tested by selecting a past day and confirming the progress ring and log list reflect historical data only, with no ability to modify.

**Acceptance Scenarios**:

1. **Given** the date strip is visible, **When** the user taps a date from the past 7 days, **Then** the screen updates to show that day's progress ring, log total, and Recent Logs for that date.
2. **Given** a past day is selected, **When** the screen renders, **Then** the "Log Reading" button is hidden or visually disabled and no logging interaction is possible.
3. **Given** the user taps today's date in the strip, **When** the screen refreshes, **Then** the interactive "Log Reading" button reappears and all write operations are re-enabled.
4. **Given** a past day had zero pages logged, **When** that day is selected, **Then** the progress ring shows 0% and no log entries appear.

---

### Edge Cases

- What happens when a user tries to log more than 604 pages in a single session? The stepper must cap at 604 (total pages in the standard Quran) and not allow higher values.
- What happens on first launch with no data? The screen must render a zero-state with the default 5-page goal card visible and no crash or blank UI.
- What happens if the user tries to enter an Ayah number exceeding the selected Surah's verse count? The picker enforces the correct maximum and does not accept out-of-range values.
- What happens if the page stepper goes below 0? The stepper must be hard-clamped at 0.
- What happens to the streak if the app is not opened for several days? On next open, the streak is re-evaluated against past calendar days. If any day between the last logged day and today (exclusive) has zero pages, the streak resets to 0. Today's zero-page state is not penalised until midnight closes the day.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST display a Quran Reading screen accessible via the Quran tab in the bottom navigation bar.
- **FR-002**: The system MUST show a daily progress ring displaying pages read today vs. the active daily goal (e.g., "6 OF 10 PAGES").
- **FR-003**: The system MUST display a Goal card showing the active daily page goal, its status badge ("ACTIVE GOAL"), and a motivational subtitle.
- **FR-004**: The system MUST provide a "Log Reading" bottom sheet with a page count stepper (−/+), quick-add chips (+1 Page, +5 Pages, 1 Juz = 20 pages), and a "Done" action.
- **FR-005**: Each "Log Reading" session MUST be additive: logging 3 pages when 5 are already logged for that day results in a total of 8 pages, not a replacement.
- **FR-006**: The system MUST persist each log session as a discrete entry linked to the calendar date.
- **FR-007**: The system MUST display a "Resume Reading" card showing the user's saved reading bookmark (Surah name + Ayah number).
- **FR-008**: The system MUST provide an "Update Position" bottom sheet with a searchable list of all 114 Surahs and an Ayah number picker constrained to the selected Surah's verse count.
- **FR-009**: The Ayah picker MUST enforce the correct maximum Ayah count per Surah (e.g., Al-Fatiha = 7, Al-Baqarah = 286).
- **FR-010**: The system MUST provide a "Daily Quran Goal" bottom sheet with a stepper and popular goal presets (1 Page, 5 Pages, 10 Pages, 1 Juz = 20 pages).
- **FR-011**: The system MUST apply a default daily goal of 5 pages if the user has never explicitly set one.
- **FR-012**: The system MUST display a "Recent Logs" list showing past reading entries with date, pages read, and a status label: "OVER GOAL", "UNDER GOAL", or "HIT GOAL".
- **FR-013**: The system MUST track a reading streak: the count of consecutive calendar days on which at least one page was logged. The streak is broken only when a past calendar day (yesterday or earlier) has zero pages logged. Today's zero-page state does not break the streak until midnight closes the current day.
- **FR-014**: The system MUST display a horizontal date strip allowing navigation across at least the past 7 days, consistent with the prayer screen pattern.
- **FR-015**: When a past date is selected in the date strip, the screen MUST enter read-only mode: the Log Reading button is hidden/disabled and no data modifications are permitted for that day.
- **FR-016**: When the user taps "Done" in the Log Reading sheet with a page count ≥ 1, the system MUST automatically advance the reading bookmark by the number of pages logged. The new bookmark is set to ayah 1 of the surah whose `startPage` (Madinah Mushaf) is the highest value ≤ (current surah `startPage` + pages logged), clamped to page 604. This advance happens silently — no additional sheet is opened. If no bookmark exists, page 1 (Al-Fatihah) is used as the starting page.
- **FR-017**: The page count stepper in the Log Reading sheet MUST NOT allow values below 0 or above 604.
- **FR-018**: All user-visible strings MUST be declared in the shared design system `strings.xml` and accessed via string resources, following the project's string resource naming convention (`quran_<element>_<type>`).

### Key Entities *(include if feature involves data)*

- **QuranDailyLog**: Represents one reading session. Key attributes: calendar date, pages logged in this session. Multiple sessions per day are stored individually; the daily total is the sum of all sessions for a date.
- **QuranGoal**: The user's active daily page target. Key attributes: pages-per-day value, last-updated timestamp. Only one goal is active at a time; changing the goal does not retroactively alter past log evaluations.
- **QuranBookmark**: The user's saved reading position in the Quran. Key attributes: Surah number (1–114), Surah name (display), Ayah number (bounded by the Surah's total verse count). Singleton — only one bookmark is stored.
- **ReadingStreak**: Derived metric computed from QuranDailyLog records. Represents the number of consecutive calendar days with at least one page logged. Not stored as a separate entity; computed on query.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete a full "log reading" interaction (open sheet → set page count → save) in under 15 seconds.
- **SC-002**: The daily progress ring and log total update to reflect newly saved entries within 1 second of tapping "Done" on the Log Reading sheet.
- **SC-003**: Users can update their reading position (Surah + Ayah) in under 30 seconds, including searching for a Surah by name.
- **SC-004**: All reading data (logs, goal, bookmark) persists across app restarts with zero data loss.
- **SC-005**: Navigating to a past day via the date strip and returning to today requires no more than 2 taps and completes in under 2 seconds.
- **SC-006**: The streak counter is accurate to the calendar day — a missed day resets it and a logged day increments it — requiring zero manual correction from the user.
- **SC-007**: The screen renders correctly in zero-state (no logs, no goal ever set) without crashes, empty UI, or layout breakage.
- **SC-008**: The feature functions fully offline with no network dependency at any point in the reading tracking flow.

---

## Assumptions

- **A-001**: "1 Juz" is treated as 20 pages (standard Uthmani mushaf pagination) for the "+1 Juz" quick-add chip and goal preset.
- **A-002**: Daily logging is page-count-based for MVP. The Surah/Ayah bookmark is a separate reading position record, not a per-session log range.
- **A-003**: Streak calculation uses the device's local calendar date (midnight boundary), consistent with the existing daily log generation approach in the habits module.
- **A-004**: Past-day navigation is limited to the 7-day window visible in the date strip, consistent with the prayer screen.
- **A-005**: The "Recent Logs" section on the main screen shows the 3 most recent entries; "VIEW ALL" reveals the full history.
- **A-006**: Goal changes take effect immediately for the current day's progress calculation; past log entries are evaluated against the goal that was active when they were created.
- **A-007**: The weekly heatmap (7-day cell grid, consistent with feature:habits) is deferred to the Insights screen. The Quran screen itself shows only a streak count and a recent-logs list.
- **A-008**: QuranGoal is stored as a single-row entity in the shared Room database (`shared:core:database`). If no row exists, the default of 5 pages is applied in the repository layer. DataStore is not used.
- **A-009**: Both QuranBookmark and QuranGoal use an INSERT OR REPLACE strategy with a hardcoded `id = 1`, ensuring only one row ever exists for each. The DAO upserts on every write; there is no separate INSERT vs. UPDATE path.
- **A-010**: The auto-advance bookmark is a page-level approximation. Because no ayah-to-page table exists, the bookmark always lands on **ayah 1** of the resolved surah. Users who need precise mid-surah positioning can tap the "Resume Reading" card to open the Update Position sheet and set any Surah/Ayah manually.
