# Feature Specification: Athkar & Tasbeeh

**Feature Branch**: `008-athkar-tasbeeh`  
**Created**: 2026-04-10  
**Status**: Draft  
**Input**: Athkar checklists (Morning, Evening, Post-Prayer) + standalone Tasbeeh counter with daily completion tracking and configurable notifications

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Morning & Evening Athkar Checklists (Priority: P1)

A user opens the Daily Athkar screen and sees three Athkar groups: Morning, Evening, and Post-Prayer. They tap the Morning Athkar card to open the reading session screen. Inside, each individual dhikr is listed with its required repetition count (e.g., "Subhanallah × 33"). The user taps the item's counter button repeatedly; the counter increments on each tap. When the counter reaches the required count, the item is visually marked complete. Once all items in the group reach their targets, the entire group is marked complete for the day. Completing a group on the Daily Athkar screen shows a visual completion badge on that group's card.

**Why this priority**: The Athkar checklists are the primary differentiating feature of this screen and the most-used daily interaction. They deliver immediate, standalone value and directly fulfil the product's spiritual habit-tracking mission.

**Independent Test**: Can be fully tested by navigating to the Athkar tab, opening Morning Athkar, tapping through all dhikr items, and confirming that each item and then the group is marked complete — no notifications or database persistence required for initial validation.

**Acceptance Scenarios**:

1. **Given** the user opens the Morning Athkar session screen, **When** they view the list, **Then** each dhikr item displays its Arabic title (transliteration label), a current count (starting at 0), and the required target count.
2. **Given** a dhikr item has not yet reached its target count, **When** the user taps the counter button, **Then** the displayed count increments by one.
3. **Given** a dhikr item's count reaches its target, **When** the count equals the target, **Then** the item is visually marked as complete (e.g., checked/greyed-out state).
4. **Given** all dhikr items in a group are complete, **When** the last item reaches its target, **Then** the group (Morning/Evening/Post-Prayer) is marked complete for today, and the Daily Athkar screen reflects this with a completion indicator on the card.
5. **Given** the user has partially completed a group, **When** they navigate away and return later the same day, **Then** each item's counter and completion state are preserved exactly as they left them.
6. **Given** the Evening Athkar group exists, **When** the user opens it, **Then** it behaves identically to Morning Athkar with its own distinct list of dhikr items and repeat targets.

---

### User Story 2 - Post-Prayer Athkar Checklist (Priority: P2)

After completing a prayer, the user can open the Post-Prayer Athkar checklist. This checklist follows the same tap-to-count interaction model as Morning and Evening Athkar. The Post-Prayer group includes the canonical Tasbeeh Al-Fatima (33 × SubhanAllah, 33 × Alhamdulillah, 34 × Allahu Akbar), key Quranic verses, and other standard post-prayer supplications. The group is tracked independently per session (not per prayer name, since the content is the same after each prayer).

**Why this priority**: Post-Prayer Athkar is the second most important checklist; however, since it reuses the same UI pattern as Morning/Evening Athkar, it can be developed as an extension of P1 with minimal additional effort. It represents a distinct use case triggered in a different context (after a prayer).

**Independent Test**: Can be fully tested by opening the Athkar tab, selecting Post-Prayer Athkar, tapping through all items, and verifying group completion — independent of Morning/Evening Athkar.

**Acceptance Scenarios**:

1. **Given** the user opens the Post-Prayer Athkar session screen, **When** they view the list, **Then** the list includes the Tasbeeh Al-Fatima counters (SubhanAllah × 33, Alhamdulillah × 33, Allahu Akbar × 34), key Quranic verse items, and other supplications.
2. **Given** the user taps a Post-Prayer Athkar item, **When** the counter increments, **Then** the behavior is identical to Morning/Evening Athkar counter interactions.
3. **Given** all Post-Prayer Athkar items are complete, **When** the last item reaches its target, **Then** the Post-Prayer Athkar group on the Daily Athkar screen shows a completion indicator.

---

### User Story 3 - Standalone Tasbeeh Counter (Priority: P2)

The user navigates to the dedicated Tasbeeh Counter screen (accessible from the Athkar tab or bottom navigation). A large circular counter prominently displays the current count and a progress arc. The user taps anywhere on the large tap target to increment the counter. On each tap the device provides a short haptic (vibration) feedback. A "Reset" button clears the counter to zero. A "Goal" button opens a bottom sheet where the user can choose a preset target (33, 100, 300) or enter a custom number. When the count reaches the goal, the progress arc completes and a celebratory haptic is triggered.

**Why this priority**: The Tasbeeh Counter is a standalone, independently navigable screen with its own route (`TasbeehRoute`). It delivers immediate value as a general-purpose dhikr counter and was explicitly designed in the UI reference (`tasbeeh_counter.png`).

**Independent Test**: Can be fully tested by navigating to the Tasbeeh screen, tapping the counter, observing increments and haptic feedback, resetting, and setting a goal via the bottom sheet.

**Acceptance Scenarios**:

1. **Given** the user is on the Tasbeeh Counter screen, **When** they tap the large tap target, **Then** the count increments by 1 and a short vibration/haptic feedback occurs.
2. **Given** a goal is set, **When** the count reaches the goal, **Then** the progress arc completes (full ring), the count display highlights, and a distinct completion haptic fires.
3. **Given** any count value, **When** the user taps the Reset button, **Then** the count resets to 0 and the progress arc clears.
4. **Given** the user taps the Goal button, **When** the goal bottom sheet opens, **Then** they can select a preset (33, 100, 300) or enter a custom positive integer, and confirming saves the new goal.
5. **Given** the user navigates away and returns to the Tasbeeh screen, **When** the screen reopens, **Then** the goal value is preserved, but the current session count resets to 0 (session-scoped, not persisted across navigation).
6. **Given** the screen shows stats, **When** the user taps, **Then** "TODAY'S TOTAL" accumulates across multiple sessions throughout the day and "CURRENT SESSION" shows the count since last reset.

---

### User Story 4 - Daily Completion Tracking (Priority: P3)

The system records whether each Athkar group (Morning, Evening, Post-Prayer) was fully completed for each calendar date. This enables the Daily Athkar screen to show today's completion status for each group and allows the Insights screen to include Athkar consistency data. If the user opens the app the next day, the completion states from yesterday are preserved (read-only) and today's session starts fresh.

**Why this priority**: Persistence of completion data is required to build meaningful streaks and insights. However, it adds no visible value without the UI interactions from P1/P2, so it is P3.

**Independent Test**: Can be tested by completing a group on one day, then advancing the device date and verifying that the previous day's data is preserved and today starts at zero.

**Acceptance Scenarios**:

1. **Given** the user completes all items in the Morning Athkar group today, **When** the calendar date advances to the next day, **Then** today's Morning Athkar completion state starts at zero and yesterday's completed state is preserved.
2. **Given** the user partially completes Evening Athkar and closes the app, **When** the user reopens the app the same day, **Then** the partial progress is restored correctly.
3. **Given** the Insights screen displays Athkar data, **When** the user views weekly consistency, **Then** completed dates for Morning and Evening Athkar are correctly reflected in the heatmap.

---

### User Story 5 - Configurable Notification Reminders (Priority: P4)

The user can configure daily reminder notifications for Morning Athkar and Evening Athkar. From the Settings screen, the user taps the Morning Athkar reminder entry to open a time picker, selects a time (e.g., 05:30), and enables the notification. A notification appears at the configured time each day prompting the user to recite their Morning Athkar. The same flow applies to Evening Athkar. The user can disable either notification at any time. On first launch or first access to the Athkar feature, the app requests notification permission if not yet granted.

**Why this priority**: Notifications require platform permission APIs and add substantial implementation complexity. They are a convenience feature that adds value but do not block the core counting and tracking functionality. Users can still use the Athkar feature without notifications.

**Independent Test**: Can be tested by setting a Morning Athkar notification time a few minutes in the future, backgrounding the app, and verifying the notification arrives at the correct time with the correct title and body copy.

**Acceptance Scenarios**:

1. **Given** the user has not granted notification permission, **When** they first attempt to enable an Athkar reminder, **Then** the system requests notification permission before scheduling any notification.
2. **Given** permission is granted and the user sets a Morning Athkar reminder time, **When** the configured time arrives on a given day, **Then** a notification appears with a message prompting the user to read their Morning Athkar.
3. **Given** a reminder is enabled, **When** the user disables it in Settings, **Then** no further notifications are scheduled for that Athkar group.
4. **Given** the user changes the reminder time, **When** the new time is saved, **Then** the previously scheduled notification is cancelled and a new one is scheduled at the updated time.
5. **Given** notification permission is denied, **When** the user attempts to enable a reminder, **Then** the app shows an explanatory message and links to system notification settings; no notification is silently dropped.

---

### Edge Cases

- What happens when the user taps an Athkar counter rapidly (faster than UI can render)? The count must not be lost; all taps must be registered.
- What happens if the device date/time changes while an Athkar session is open? The in-progress session should continue to the current day; on return visits the correct day's data must be shown.
- What happens when the Tasbeeh count reaches the maximum safe integer value? The counter must stop incrementing and not overflow (display should cap with a visual indicator).
- What happens when a goal is set to a value lower than the current session count? The progress arc should immediately show "complete" state.
- What happens if notification permission is revoked after a reminder is saved? The app must handle gracefully on the next scheduling attempt (display a status warning in Settings but preserve the saved time preference).
- What happens if the user completes an Athkar group, then manually resets individual item counters? The group completion status should update accordingly (group becomes incomplete if any item falls below target).

---

## Requirements *(mandatory)*

### Functional Requirements

**Athkar Checklists**

- **FR-001**: The system MUST provide three Athkar group checklists: Morning Athkar, Evening Athkar, and Post-Prayer Athkar, each accessible from the Daily Athkar screen.
- **FR-002**: Each Athkar group MUST contain a static, hardcoded ordered list of dhikr items. Each item has a transliteration label, a meaning/translation label, and a required repetition count.
- **FR-003**: Users MUST be able to tap a counter button on each dhikr item to increment its count by one per tap.
- **FR-004**: The system MUST visually distinguish completed items (count ≥ target) from incomplete items.
- **FR-005**: The system MUST mark an Athkar group as complete when all of its dhikr items have reached their individual target counts.
- **FR-006**: The Daily Athkar screen MUST display a completion indicator (badge/checkmark/ring) on each Athkar group card reflecting its completed/incomplete status for today.
- **FR-007**: Users MUST NOT be able to decrement an individual dhikr item counter below zero.
- **FR-007b**: An individual dhikr item counter MUST be clamped at its target count; tapping beyond the target MUST NOT increment the counter further. The item remains in the completed state and the excess taps are discarded.

**Tasbeeh Counter**

- **FR-008**: The system MUST provide a dedicated Tasbeeh Counter screen with a large tap target that increments a counter by one on each tap.
- **FR-009**: The system MUST trigger a short haptic/vibration feedback on every counter tap.
- **FR-010**: The system MUST display a progress arc on the Tasbeeh screen that fills proportionally to the counter's progress toward the current goal.
- **FR-011**: Users MUST be able to reset the current session count to zero via a Reset action.
- **FR-012**: Users MUST be able to set a Tasbeeh goal via a bottom sheet offering preset values (33, 100, 300) and a custom numeric input. The goal must be a positive integer.
- **FR-013**: When the session count reaches or exceeds the goal, the system MUST trigger a completion haptic and visually complete the progress arc.
- **FR-014**: The Tasbeeh screen MUST show a "TODAY'S TOTAL" count (sum of all completed session counts since midnight) and a "CURRENT SESSION" count.
- **FR-015**: The Tasbeeh goal value MUST be persisted across app sessions (survives app restart).

**Daily Completion Tracking**

- **FR-016**: The system MUST persist the completion status and per-item counter values for each Athkar group (Morning, Evening, Post-Prayer) per calendar date locally on the device.
- **FR-016b**: The system MUST persist the cumulative Tasbeeh tap count for each calendar date locally on the device. At the start of each new calendar day, the daily Tasbeeh total for the new day starts at zero; prior days' totals are preserved in read-only state.
- **FR-017**: At the start of each new calendar day, the system MUST present a fresh, zeroed session for all Athkar groups; prior days' data MUST remain accessible in read-only state.
- **FR-018**: The system MUST restore in-progress Athkar counters when the user returns to a group within the same calendar day.

**Notifications**

- **FR-019**: The system MUST allow users to enable and configure a daily reminder notification for Morning Athkar (user-selectable time).
- **FR-020**: The system MUST allow users to enable and configure a daily reminder notification for Evening Athkar (user-selectable time).
- **FR-021**: The system MUST request notification permission from the platform before scheduling any reminder, using the same permission-gating pattern employed elsewhere in the app.
- **FR-022**: If notification permission is denied, the system MUST display an informative message and must NOT silently schedule or lose notification settings.
- **FR-023**: Users MUST be able to disable either Athkar notification independently without affecting the other.
- **FR-024**: When the user changes a reminder time, the previously scheduled notification MUST be cancelled and a new one scheduled at the updated time.

### Key Entities

- **AthkarGroup**: Represents one of the three checklist groups (Morning, Evening, Post-Prayer). Has an identifier and an ordered list of AthkarItems.
- **AthkarItem**: A single dhikr within a group. Has a stable identifier, a transliteration key (maps to a string resource), a translation key, and a required repetition count. This list is static and embedded in the app binary.
- **AthkarDailyLog**: Records the per-item counter state and overall group completion status for a specific AthkarGroup on a specific calendar date. There is one row per (groupId, date) pair; the per-item counters are stored as a map of itemId → current count within that single row.
- **TasbeehGoal**: A singleton record storing the user's currently configured Tasbeeh goal count. Persisted in local storage.
- **TasbeehDailyTotal**: Stores the cumulative count of Tasbeeh taps for a given calendar date (today's total). Stored in local database, keyed by date.
- **AthkarNotificationPreference**: Per-group (Morning, Evening) notification settings: enabled flag and configured time. Persisted locally on the device.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can open an Athkar group, tap through all required items, and achieve group completion in a single focused session without any blocking errors or data loss.
- **SC-002**: Per-item counter state is never lost mid-session; tapping rapidly (multiple taps per second) does not result in missed increments.
- **SC-003**: The Daily Athkar screen correctly reflects the completion status for all three groups on any given day, including days where the user partially completed a group.
- **SC-004**: The Tasbeeh Counter increments and provides haptic feedback with a perceptible response time of under 100ms after each tap.
- **SC-005**: Athkar group completion data is retained across app restarts, device reboots, and calendar date rollovers with zero data loss.
- **SC-006**: A configured Athkar notification fires within 60 seconds of the scheduled time, regardless of whether the app is open or closed.
- **SC-007**: All user-visible text in the Athkar and Tasbeeh screens is fully localizable (English and Arabic) with no hardcoded strings.
- **SC-008**: The feature functions entirely offline; no network connectivity is required at any point.

---

## Assumptions

- The Athkar item lists (Morning, Evening, Post-Prayer) are static and defined at build time in code; no server-side or user-customizable Athkar list management is required for this feature.
- The transliteration and meaning labels for each dhikr item are string resource keys — the actual Arabic text rendering (right-to-left Mushaf-style text) is out of scope; only transliteration labels are required.
- "Post-Prayer Athkar" is a single universal list (not varied per prayer name); the same checklist applies after Fajr, Dhuhr, Asr, Maghrib, and Isha.
- The Tasbeeh Counter session count resets to zero on navigation away from the screen (session-scoped); only the cumulative daily total and the goal are persisted.
- Notification scheduling uses the platform's built-in alarm/notification capability (no third-party push service); notifications are local only.
- The Morning and Evening Athkar notifications are independent of whether the user has already completed those groups on a given day (the reminder fires regardless; no "smart suppression" is implemented in this version).
- The Athkar completion data per date is retained indefinitely (no automated purge within the MVP; data volume is trivially small).
- Post-Prayer Athkar does not have a notification reminder (it is contextually triggered by the user after prayer, not by a scheduled time).
