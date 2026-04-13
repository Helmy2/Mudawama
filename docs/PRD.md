# Product Requirements Document: Mudawama (مُداوَمَة)

**Platform:** Android & iOS (via Kotlin Multiplatform + Compose Multiplatform)  
**Phase:** MVP (Minimum Viable Product)  
**Status:** MVP Shipped (Prayer + Habits + Quran Tracking + Athkar & Tasbeeh + Home Dashboard)

---

## 1. Product Overview

Mudawama is a serene, open-source Muslim habit tracker designed to help users build and maintain consistency in their daily spiritual obligations (Wird). The core focus is on tracking daily prayers, Quran reading, and Athkar (remembrances) without the clutter or aggressive gamification found in other apps.

### 1.1 Project Goals

- **Product Goal:** Provide Muslims with a clean, offline-first, premium-feeling application to track their daily spiritual habits.
    
- **Educational Goal:** Serve as a flagship open-source tutorial project for an Arabic YouTube series demonstrating Kotlin Multiplatform (KMP), Compose Multiplatform (CMP), and AI-driven development using Spec Kit.
    

---

## 2. Target Audience

- **End-Users:** Muslims looking for a distraction-free, privacy-focused way to log their daily worship consistency.
    
- **Developers:** Mobile engineers learning KMP, CMP, Spec Kit, and clean architectural patterns.
    

---

## 3. Scope & Features (MVP)

The MVP strictly limits scope to foundational habits to ensure high execution quality and to keep the codebase digestible for tutorial purposes. _(Note: Hijri Calendar integration has been explicitly dropped from the MVP scope )_

### 3.1 Core Features (The "Daily Rituals")

- **Prayer Tracking (Salah):**
    
    - Track completion of the 5 daily Fard (obligatory) prayers.
        
    - Connect to a remote API to fetch accurate prayer times based on location/settings.
        
- **Quran Reading Tracker:**
    
    - Allow users to set a daily reading goal (pages per day) via a dedicated Goal sheet.
        
    - Feature a "Log Reading" bottom sheet to quickly log pages read; each log is stored as a `QuranDailyLogEntity` keyed by logical date.
        
    - Auto-advance the reading bookmark (Surah + Ayah) on log confirmation using the `alquran.cloud` API; falls back gracefully when offline.
        
    - Display current reading streak and recent 7-day log history on the Quran screen.
        
    - 7-day date strip for navigating to past days in read-only mode.
        
    - Save the current bookmark (Surah and Ayah) manually via an "Update Position" sheet.
        
- **Athkar & Tasbeeh:**

    - Morning Athkar checklist: tap-to-count per dhikr item, clamped at the required repetition count, persisted daily in Room (`AthkarDailyLogEntity`).

    - Evening Athkar checklist: same tap-to-count model as Morning.

    - Post-Prayer Athkar checklist: 5 prayer slots (Fajr–Isha) selectable via filter chips; counter key format `"itemId#slotIndex"`.

    - Group completion is derived and stored (`is_complete` flag in `AthkarDailyLogEntity`); overview cards show a completion badge when all items reach target.

    - Digital Tasbeeh counter: in-memory session count with haptic feedback on each tap; goal bottom sheet for setting target (default 100); session flushed to `TasbeehDailyTotalEntity` on Reset; daily total shown alongside session count.

    - Long-pressing a completed Athkar item resets its individual counter to 0.

- **Appearance:**

    - Dynamic Theming (Android 12+): Support Material You colors, with a Settings toggle to switch between dynamic wallpaper-based themes and static brand colors.
        

### 3.2 Personal Habits (Custom)

- Allow users to create custom habits (e.g., "Fasting Mondays", "Daily Sadaqah").
    
- Users can define a custom habit by name, icon, frequency, and type (Check-off vs. Numeric counter). For Numeric habits, users set a daily goal count (`goalCount`).
    

### 3.3 Insights & Progress

- A simple dashboard showing current active streaks.
    
- A 7-day visual heatmap of consistency for prayers and overall habits.
    

---

## 4. Technical Architecture

The application is built using a modern, scalable, and modular approach designed to showcase KMP best practices.

- **UI Framework:** Compose Multiplatform (CMP) for both Android and iOS, architected with base ViewModels to allow for a future native SwiftUI migration if desired.
    
- **UI Design System:** Designed via Google Stitch, adhering to an 8pt grid with a calm, minimal aesthetic (Emerald/Teal, Off-white).
    
- **Shared Logic:** Kotlin Multiplatform handling domain models, use cases, and database operations.
    
- **Local Database:** Room (Offline-first approach for all habit tracking).
    
- **Build System:** Gradle Convention Plugins for clean, modular build script management.
    
- **AI Tooling:** Spec Kit used for specification-driven development and code generation.
    

---

## 5. User Experience (UX) Flow

1. **Onboarding:** A 2-step setup to welcome the user and set their initial Quran and Tasbeeh goals.
    
2. **Home Dashboard:** The central hub showing the "Next Prayer" hero card, Athkar daily status (Morning/Evening done/pending), Quran reading progress ring, Tasbeeh daily progress card, and a scrollable summary of today's habits. Tapping any summary card navigates to the corresponding feature screen. A gear icon in the top bar navigates to the Settings placeholder.
    
3. **Bottom Navigation:** Quick access to Home, Prayers, Quran, and Athkar via a **4-tab** floating glassmorphism bar. Tasbeeh is accessible via the Tasbeeh card on the Home Dashboard (push destination, no bottom bar). The Habits full screen is accessible via "View All" on the Home Dashboard (push destination, no bottom bar).
    
4. **Interactions:** Heavy reliance on fast, one-thumb interactions such as inline list toggles and bottom sheets (e.g., Log Reading, Surah Picker) to keep the user in context.
    
2. **Home Dashboard:** The central hub displaying the "Next Prayer", today's daily rituals progress, and a scrollable list of pending/completed custom habits.
    
3. **Bottom Navigation:** Quick access to Home, Prayers, Quran, Athkar, and Tasbeeh via a 5-tab floating glassmorphism bar.
    
4. **Interactions:** Heavy reliance on fast, one-thumb interactions such as inline list toggles and bottom sheets (e.g., Log Reading, Surah Picker) to keep the user in context.
    

---

## 6. Out of Scope for MVP (Phase 2+)

To maintain the MVP timeline, the following features will be excluded:

- Cloud synchronization / Firebase backend (Data is strictly local via Room for KMP).
    
- Quran Mushaf (reading the actual Arabic text inside the app).
    
- Complex analytics or multi-month historical graphs.
    
- Qada (make-up prayer) management.
    
- Hijri calendar integration.