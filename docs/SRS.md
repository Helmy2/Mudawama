# Software Requirements Specification: Mudawama (مُداوَمَة)

**Version:** 1.0 (MVP)

---

## 1. Introduction

### 1.1 Purpose
The purpose of this document is to define the software requirements for Mudawama, an open-source, multiplatform mobile application used for tracking Islamic daily habits (Wird). It is intended to guide development, particularly leveraging Kotlin Multiplatform (KMP) and Spec Kit for AI-driven code generation.

### 1.2 Scope
Mudawama MVP provides an offline-first habit-tracking experience for Android and iOS. It manages four core domains: Prayers, Quran, Athkar, and Custom Habits. Cloud synchronization, user authentication, and built-in Arabic text reading (Mushaf) are strictly out of scope for this version.

---

## 2. Overall Description

### 2.1 User Characteristics
- **End-Users:** Muslims of all ages who want a distraction-free, privacy-respecting tool to maintain consistency in their religious duties.
- **Developer Audience:** Developers following the open-source repository and YouTube series to learn KMP, Compose Multiplatform (CMP), Room database integration, and Spec Kit usage.

### 2.2 Operating Environment
- **Android:** Minimum SDK as defined in `libs.versions.toml` (see project root), developed via Compose Multiplatform.
- **iOS:** iOS 15+, utilizing Compose Multiplatform UI (with ViewModels architected to support a future native SwiftUI implementation).

---

## 3. Functional Requirements (FR)

### FR-1: Prayer Tracking System
- **FR-1.1:** The system shall display the 5 daily obligatory prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) in chronological order.
- **FR-1.2:** The system shall allow users to mark a prayer as "Pending", "Completed", or "Missed".
- **FR-1.3:** The system shall fetch and display prayer times based on a 3rd-party Prayer Times API (e.g., Aladhan API) and cache them locally.

### FR-2: Quran Reading Tracker
- **FR-2.1:** The system shall allow the user to set a daily reading goal (pages per day) via a dedicated Goal bottom sheet. The goal persists in `QuranGoalEntity`.
- **FR-2.2:** The system shall provide a "Log Reading" bottom sheet to log pages read for the current day. Each log creates or updates a `QuranDailyLogEntity` row keyed by the logical date.
- **FR-2.3:** The system shall allow the user to manually update their bookmark (Surah 1–114 + Ayah) via an "Update Position" bottom sheet.
- **FR-2.4:** Upon confirming a reading log, the system shall automatically advance the bookmark to the next page. The new Surah+Ayah shall be resolved via the `alquran.cloud` API; the system shall fall back to `ayah = 1` on network failure.
- **FR-2.5:** The system shall display recent reading logs (up to the last 7 days) as a scrollable list on the Quran screen.
- **FR-2.6:** The system shall display a 7-day date strip on the Quran screen. Tapping a past day switches the view to read-only mode showing that day's logged pages; the current day is always editable.
- **FR-2.7:** The system shall calculate and display the user's current consecutive reading-day streak.

### FR-3: Athkar & Tasbeeh
- **FR-3.1:** The system shall provide static tap-to-count checklists for "Morning Athkar", "Evening Athkar", and "Post-Prayer Athkar". Each item has a required repetition count; tapping increments the counter up to (and clamped at) that target.
- **FR-3.2:** The system shall persist the daily completion status and per-item counter state for each Athkar group in Room (`AthkarDailyLogEntity`), keyed by `(groupType, date)`.
- **FR-3.3:** The Post-Prayer Athkar checklist shall support 5 independent prayer slot sessions (Fajr, Dhuhr, Asr, Maghrib, Isha), each tracking its own set of counters.
- **FR-3.4:** The system shall display group completion badges on the Athkar overview when all items in a group reach their target for the current day.
- **FR-3.5:** The system shall include a digital Tasbeeh counter that increments via a tap gesture with haptic feedback, supports goal setting via a bottom sheet (default 100), and resets to zero (flushing the session count to the daily total in Room).
- **FR-3.6:** The system shall display the cumulative Tasbeeh daily total (sum of all flushed sessions since midnight) alongside the current in-memory session count.
- **FR-3.7:** Long-pressing a completed Athkar item shall reset that item's counter to 0.

### FR-4: Custom Habit Management
- **FR-4.1:** The system shall allow users to create custom habits specifying: Name, Icon, Frequency (Days of week), and Type (Boolean Check-off vs. Numeric Counter).
- **FR-4.2:** When creating or editing a habit of type **Numeric**, the system shall display a goal count input field (`goalCount`) so the user can set the daily target repetitions.
- **FR-4.3:** The system shall display custom habits alongside core rituals on the Home/Habits screen.
- **FR-4.4:** The system shall allow users to Edit and Delete custom habits via a bottom sheet interface. Core rituals (Prayers, Quran) cannot be deleted.

### FR-5: Daily Logs, Insights & Home Dashboard
- **FR-5.1:** The system shall generate a new daily log for all active habits automatically at the start of a new day (Islamic or standard midnight, configurable).
- **FR-5.2:** The system shall display a basic visual heatmap showing the user's consistency (streak) over the last 7 days.
- **FR-5.3:** The Home Dashboard screen (`feature:home:presentation`) shall aggregate and display read-only summary cards from all core features: (a) a "Next Prayer" card showing the next upcoming prayer name and time, (b) an Athkar status card showing Morning/Evening done/pending state, (c) a Quran progress ring card showing pages read vs. goal, (d) a Tasbeeh daily progress card showing daily total vs. goal, and (e) a Habits summary section. Tapping any summary card shall navigate the user to the corresponding feature screen. No data entry is permitted from the Home Dashboard — it is read-only.
- **FR-5.4:** The bottom navigation bar shall have **4 tabs**: Home, Prayers, Quran, Athkar. Tasbeeh is accessible as a push destination from the Tasbeeh summary card on the Home Dashboard. The full Habits screen is accessible as a push destination from the Home Dashboard. Push destinations do not show the bottom navigation bar.
- **FR-5.5:** Back navigation from any push destination (Habits, Tasbeeh, Settings) shall return the user to the Home Dashboard via `AppBackHandler` (Android: system back gesture; iOS: no-op — swipe handled natively).

---

## 4. External Interface Requirements

### 4.1 User Interfaces (UI)
- **Design System:** Developed using Compose Multiplatform, strictly adhering to an 8pt grid system.
- **Theme:** "Serene and Minimal" using a primary palette of Deep Teal/Emerald and Off-white. Support for Dark Mode and Light Mode.
- **Navigation:** Shallow navigation relying on a Bottom Navigation Bar and context-preserving Modal Bottom Sheets (e.g., Log Reading, Add Habit).

### 4.2 Application Programming Interfaces (APIs)
- **Prayer Times API:** The app shall integrate with the Aladhan API (`api.aladhan.com`) via Ktor Client to fetch prayer times based on device coordinates/timezone.
- **Quran Page API:** The app shall integrate with the alquran.cloud API (`api.alquran.cloud`) via Ktor Client to resolve the first Surah+Ayah on a given Madinah Mushaf page number (used when auto-advancing the reading bookmark).

---

## 5. Non-Functional Requirements (NFR)

### 5.1 Performance & Offline Capability
- The application must be **offline-first**. All habit creation, tracking, logging, and insight generation must occur locally on the device with zero network dependency.
- Network calls are restricted to fetching prayer times (Aladhan API) and resolving Quran page positions (alquran.cloud API); both must degrade gracefully when offline.

### 5.2 Architecture & Code Quality
- **Separation of Concerns:** The project must use a clean architecture with Base ViewModels. Domain logic must be 100% shared in the `commonMain` module.
- **Build System:** The project must utilize Gradle Convention Plugins for dependency management and module configuration.
- **AI Specification:** Code generation and architectural bootstrapping will be guided by specification files via Spec Kit.

### 5.3 Data Storage (Database)
- The system shall utilize **Room for Kotlin Multiplatform** to persist data. Current schema version: **4**.
- Data schema must include:
    - `HabitEntity`: Stores metadata for both Core Rituals and Custom Habits.
    - `HabitLogEntity`: Stores daily completion instances linked to dates and specific habits.
    - `QuranBookmarkEntity`: Stores the singleton state of the user's current reading position (Surah + Ayah). Does **not** store goal or daily progress.
    - `QuranDailyLogEntity`: Stores per-day reading log entries (pages read per calendar date).
    - `QuranGoalEntity`: Stores the singleton daily reading goal (pages per day).
    - `AthkarDailyLogEntity`: Stores per-group per-day Athkar progress. Composite PK `(group_type, date)`. Counter map stored as JSON via `AthkarCountersConverter`.
    - `TasbeehGoalEntity`: Singleton row (`id = 1`) storing the user's Tasbeeh target count.
    - `TasbeehDailyTotalEntity`: Stores the cumulative Tasbeeh count flushed per calendar day.

### FR-6: Dynamic Theming
- **FR-6.1:** The system shall automatically detect if the device supports Android dynamic theming (API 31+).
- **FR-6.2:** The system shall enable dynamic theming by default on supported devices.
- **FR-6.3:** The system shall provide a toggle in the Settings screen to enable/disable dynamic theming.
- **FR-6.4:** The system shall hide the dynamic theming toggle on unsupported devices (Android < 12 and iOS).
- **FR-6.5:** The system shall fallback to Mudawama's default brand colors (Light/Dark variants) when dynamic theming is disabled or unsupported.
