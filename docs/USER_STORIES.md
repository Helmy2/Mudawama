# User Stories: Mudawama (مُداوَمَة)

---

## Epic 1: Onboarding & Setup
- **US-1.1: First Launch Welcome**
    - _As a_ new user, _I want_ to see a serene welcome screen explaining the app’s purpose, _so that_ I understand the value of tracking my daily consistency (Mudawama).
- **US-1.2: Core Goals Setup**
    - _As a_ new user, _I want_ to easily set my daily Quran reading goal (e.g., 5 pages) and default Tasbeeh goal (e.g., 100), _so that_ my app is personalized to my spiritual capacity from day one.

## Epic 2: Core Rituals (The Non-Negotiables)
- **US-2.1: View Today's Prayers**
    - _As a_ user, _I want_ to see the five daily obligatory prayers listed in chronological order with their local start times, _so that_ I know when my next prayer is due.
- **US-2.2: Track Prayer Completion**
    - _As a_ user, _I want_ to tap a button to mark a specific prayer as "Completed" or "Missed", _so that_ I have an accurate log of my daily Salah performance.
- **US-2.3: Quran Reading Progress**
    - _As a_ user, _I want_ to see my daily Quran reading goal alongside a progress ring, _so that_ I know how many pages I still need to read today.
- **US-2.4: Log Quran Reading (Bottom Sheet)**
    - _As a_ user, _I want_ to open a quick bottom sheet, adjust a numeric counter to log how many pages I just read, and tap "Save", _so that_ tracking my reading is fast and frictionless.
- **US-2.5: Update Quran Bookmark**
    - _As a_ user, _I want_ to select a specific Surah from a list (1-114) and type in an Ayah number, _so that_ I can easily resume reading from the exact spot later.

## Epic 3: Athkar & Supplications
- **US-3.1: Morning and Evening Athkar Checklists**
    - _As a_ user, _I want_ to open a checklist for my Morning or Evening Athkar and tap a counter button for each specific supplication, _so that_ I can ensure I read them the correct number of times (e.g., 3 times, 100 times).
- **US-3.2: Custom Tasbeeh Counter**
    - _As a_ user, _I want_ to tap a large digital button to increment a tasbeeh counter, _so that_ I don't lose track of my count while doing general dhikr.
- **US-3.3: Reset Tasbeeh**
    - _As a_ user, _I want_ the ability to reset my tasbeeh counter back to zero, _so that_ I can start a new recitation cycle.

## Epic 4: Custom Personal Habits
- **US-4.1: Create a Custom Habit**
    - _As a_ user, _I want_ to tap an "Add Habit" button, enter a name (e.g., "Fasting Mondays"), pick an icon, and choose how often it repeats, _so that_ I can track spiritual goals beyond the core rituals.
- **US-4.2: Track Custom Habits**
    - _As a_ user, _I want_ to see my custom habits on my Home Dashboard and tap to mark them as completed for the day, _so that_ everything I track is in one unified list.
- **US-4.3: Edit or Delete Habit**
    - _As a_ user, _I want_ to long-press a custom habit to edit its name or delete it entirely, _so that_ I can manage habits that are no longer relevant to my routine.

## Epic 5: Dashboard & Insights
- **US-5.1: The Home Dashboard Summary**
    - _As a_ user, _I want_ my Home screen to show my "Next Prayer" prominently and a quick summary ring of my overall daily completion, _so that_ I get an instant snapshot of my spiritual day.
- **US-5.2: 7-Day Consistency Heatmap**
    - _As a_ user, _I want_ to view a simple calendar row showing whether I completed all my core rituals over the last 7 days, _so that_ I feel motivated to keep my streak alive.

## Epic 6: Technical / Background Tasks (Developer Use Cases)
- **TC-6.1: Fetch Prayer Times via API**
    - _As the_ system, _I need_ to connect to the Aladhan API (via Ktor) to fetch prayer times for the user's location and cache them locally in the Room database, _so that_ the app works offline after the initial sync.
- **TC-6.2: Daily Log Generation**
    - _As the_ system, _I need_ to generate fresh, empty `HabitLogEntity` rows in the database for all active habits when the clock strikes midnight, _so that_ the user has a clean slate for the new day without losing yesterday's data.
