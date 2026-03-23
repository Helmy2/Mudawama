# 🌙 Mudawama (مُداوَمَة)

**Mudawama** is a serene, offline-first, open-source Muslim habit tracker designed to help users build and maintain consistency in their daily spiritual obligations (Wird).

Built entirely with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)**, this project demonstrates modern, AI-driven mobile development using an enterprise-grade modular architecture.

> 🏗 **Developers:** Please read the [Architecture & Module Structure Guide](docs/ARCHITECTURE.md) before exploring the codebase.

---

## ✨ Features (MVP)
* **Prayer Tracking:** Log your 5 daily obligatory prayers with local times fetched via API.
* **Quran Connection:** Set daily reading goals, log your progress, and save your bookmark (Surah & Ayah).
* **Athkar & Tasbeeh:** Dedicated checklists for Morning, Evening, and Post-Prayer remembrances, alongside a digital tap counter.
* **Custom Habits:** Add personal spiritual goals (e.g., "Fasting Mondays", "Daily Sadaqah").
* **Offline-First:** All your data stays on your device via Room SQLite. No account required. No ads. No tracking.

---

## 🎨 UI & Design System
The entire application UI was designed using **Google Stitch** following a strict 8pt grid with a calm, premium aesthetic.

You can view the complete set of high-fidelity UI mockups in the [`docs/ui`](docs/ui/) directory:
* [Home Dashboard](docs/ui/home_dashboard.png)
* [Prayer Tracker](docs/ui/daily_prayer_tracker.png)
* [Quran Reading](docs/ui/quran_daily_reading_tracker.png)
* [Athkar & Tasbeeh](docs/ui/daily_athkar_tracker.png)
* ...and [more](docs/ui/).

---

## 📚 Project Documentation
This repository is thoroughly documented to simulate a complete product lifecycle. All foundational documents can be found in the [`docs/`](docs/) folder:

* 🗺️ **[Architecture Blueprint](docs/ARCHITECTURE.md):** Explanation of the Multi-Module "Dual-Umbrella" strategy.
* 📝 **[System Design Document (SDD)](docs/SDD.md):** Deep dive into data models, MVI flow, and error handling.
* 📋 **[Product Requirements Document (PRD)](docs/PRD.md):** Scope, goals, and feature definitions.
* ⚙️ **[Software Requirements Spec (SRS)](docs/SRS.md):** Technical and non-functional requirements.
* 👥 **[User Stories](docs/USER_STORIES.md):** Agile use cases for the MVP.
* 🖌️ **[Design Guidelines](docs/DESIGN.md):** Global design system rules and theming constraints.

---

## 🛠 Tech Stack
Mudawama leverages the latest in cross-platform mobile technology:
* **Language:** Kotlin 2.3.20+ (strong K2 compiler enforcement)
* **UI:** Jetpack Compose Multiplatform 1.10.3+ (Android & iOS)
* **Architecture:** Clean Architecture + Custom MVI (Orbit-style)
* **Local Database:** Room for KMP (SQLite)
* **Networking:** Ktor 3.4.1+ (with Content Negotiation & Logging)
* **Dependency Injection:** Koin 4.2.0 (with BOM & Platform Extensions)
* **Tooling:** Optimized Gradle Convention Plugins (Configuration Cache ready) & GitHub Spec Kit

---

## 🏗 Project Structure
The project follows a modular "Packaging by Feature" strategy to ensure scalability and isolation.

```mermaid
graph TD
    App[androidApp / iosApp] --> Umbrella[shared:umbrella-ui]
    Umbrella --> Feature[feature:habits / feature:prayer]
    Feature --> Core[shared:core:presentation / data / domain]
    Feature --> Design[shared:designsystem]
```

---

## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest Stable or Preview)
* Xcode (for iOS development)
* JDK 17+

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/Helmy2/mudawama.git
   ```
2. Open the project in Android Studio.
3. Sync the Gradle files.
4. **To run Android:** Select the `androidApp` run configuration and click Play.
5. **To run iOS:** Select the `iosApp` run configuration (ensure you have an iOS simulator selected) and click Play.
   *(Alternatively, open `iosApp/iosApp.xcodeproj` in Xcode and hit Run).*

---

## 🤝 Contributing
Contributions are welcome! If you spot a bug or want to add a feature from the Phase 2 roadmap, please open an Issue first to discuss it before submitting a Pull Request.