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
- **FR-2.1:** The system shall allow the user to set a daily reading goal (numeric value, e.g., 5 pages).
- **FR-2.2:** The system shall provide a "Log Reading" mechanism (via Bottom Sheet) to increment the pages read for the current day.
- **FR-2.3:** The system shall allow the user to save a bookmark consisting of a specific Surah (1-114) and Ayah number.

### FR-3: Athkar & Tasbeeh
- **FR-3.1:** The system shall provide static checklists for "Morning Athkar", "Evening Athkar", and "Post-Prayer Athkar".
- **FR-3.2:** The system shall track the completion status of the Athkar groups for the current day.
- **FR-3.3:** The system shall include a digital Tasbeeh counter that increments via a tap gesture, resets to zero, and allows goal setting (e.g., 33, 100).

### FR-4: Custom Habit Management
- **FR-4.1:** The system shall allow users to create custom habits specifying: Name, Icon, Frequency (Days of week), and Type (Boolean Check-off vs. Numeric Counter).
- **FR-4.2:** The system shall display custom habits alongside core rituals on the Home/Habits screen.
- **FR-4.3:** The system shall allow users to Edit and Delete custom habits via a bottom sheet interface. Core rituals (Prayers, Quran) cannot be deleted.

### FR-5: Daily Logs and Insights
- **FR-5.1:** The system shall generate a new daily log for all active habits automatically at the start of a new day (Islamic or standard midnight, configurable).
- **FR-5.2:** The system shall display a basic visual heatmap showing the user's consistency (streak) over the last 7 days.

---

## 4. External Interface Requirements

### 4.1 User Interfaces (UI)
- **Design System:** Developed using Compose Multiplatform, strictly adhering to an 8pt grid system.
- **Theme:** "Serene and Minimal" using a primary palette of Deep Teal/Emerald and Off-white. Support for Dark Mode and Light Mode.
- **Navigation:** Shallow navigation relying on a Bottom Navigation Bar and context-preserving Modal Bottom Sheets (e.g., Log Reading, Add Habit).

### 4.2 Application Programming Interfaces (APIs)
- **Prayer Times API:** The app shall integrate with a public REST API (such as Aladhan API) via Ktor Client to fetch prayer times based on device coordinates/timezone.

---

## 5. Non-Functional Requirements (NFR)

### 5.1 Performance & Offline Capability
- The application must be **offline-first**. All habit creation, tracking, logging, and insight generation must occur locally on the device with zero network dependency.
- Network calls are restricted to fetching prayer times, which must be cached.

### 5.2 Architecture & Code Quality
- **Separation of Concerns:** The project must use a clean architecture with Base ViewModels. Domain logic must be 100% shared in the `commonMain` module.
- **Build System:** The project must utilize Gradle Convention Plugins for dependency management and module configuration.
- **AI Specification:** Code generation and architectural bootstrapping will be guided by specification files via Spec Kit.

### 5.3 Data Storage (Database)
- The system shall utilize **Room for Kotlin Multiplatform** to persist data.
- Data schema must include:
    - `HabitEntity`: Stores metadata for both Core Rituals and Custom Habits.
    - `HabitLogEntity`: Stores daily completion instances linked to dates and specific habits.
    - `QuranBookmarkEntity`: Stores the singleton state of the user's reading position.