# Implementation Plan: Settings Screen

**Branch**: `[010-settings-screen]` | **Date**: 2026-04-11 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/010-settings-screen/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Settings screen replaces the placeholder with a functional settings UI supporting 5 user stories: prayer calculation method, location mode (GPS/Manual), Quran daily goal, app theme, and Arabic language with RTL layout.

**Primary Requirement**: Allow users to configure prayer calculation method, location, theme, and language via a dedicated settings screen with DataStore persistence.

**Technical Approach**: Feature module follows Clean Architecture with domain/data/presentation split. Uses DataStore Preferences for all settings storage (not Room). Integrates with existing PrayerViewModel and HomeViewModel via ObserveSettingsUseCase. Arabic translation via Compose resources (values-ar folder).

## Technical Context

**Language/Version**: Kotlin 2.3.20 (Kotlin Multiplatform)  
**Primary Dependencies**: Compose Multiplatform 1.10.3, Koin 4.2.0, Ktor 3.4.1, kotlinx-serialization-json 1.10.0, kotlinx-datetime 0.7.1  
**Storage**: DataStore Preferences (settings) + Room 2.8.4 (other data) — Settings MUST use DataStore ONLY per FR-010  
**Testing**: Kotlin Multiplatform tests (commonTest), Android instrumented tests  
**Target Platform**: Android (minSdk 30) + iOS 15+  
**Project Type**: Mobile app (Kotlin Multiplatform) — Islamic prayer &Dhikr tracker  
**Performance Goals**: Immediate UI response, offline-first operation  
**Constraints**: <200ms prayer time calculation, offline-capable (no network required for core features)  
**Scale/Scope**: 5 feature modules (Prayer, Quran, Athkar, Habits, Settings) + shared modules

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| Feature has domain/data/presentation split | ✓ PASS | Settings uses 3-layer structure per architecture |
| Domain layer has no platform imports | ✓ PASS | AppLanguage uses isRtl (Boolean), not LayoutDirection |
| Presentation uses MVI | ✓ PASS | SettingsViewModel implements State/Action/Event |
| No hardcoded strings in Composables | ✓ PASS | All strings via stringResource(Res.string.*) |
| DataStore is the only settings storage | ✓ PASS | FR-010 explicitly requires DataStore ONLY |
| Strings consolidated in designsystem | ✓ PASS | single strings.xml via constitution rule |
| DI uses Koin only | ✓ PASS | No Dagger/Hilt allowed |
| CoroutineDispatcher injected | ✓ PASS | All ViewModels inject dispatcher |

### Post-Phase 1 Re-evaluation

All gates PASS after Phase 1 design:
- Data model follows Clean Architecture (domain = pure Kotlin, data = DataStore, presentation = MVI)
- AppLanguage uses isRtl: Boolean (constitution-compliant, no Compose imports)
- SettingsEvent uses StringResource (codebase-consistent)
- Adhan package uses com.batoulapps.adhan (verified correct)
- Koin module named SettingsPresentationModule (no collision)
- LocationMode stored as 3 keys in DataStore (scalar storage)
- Adhan mapper lives in feature:prayer:data (not settings:data)

## Project Structure

### Documentation (this feature)

```text
specs/010-settings-screen/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
└── contracts/           # Phase 1 output (if needed - N/A for settings)
```

### Source Code (repository root)

```text
feature/
settings/
domain/          ← Enums (CalculationMethod, AppTheme, AppLanguage)
                   Sealed class (LocationMode)
                   AppSettings data class
                   SettingsRepository interface
                   Use Cases: ObserveSettingsUseCase, etc.
data/            ← DataStore implementation of SettingsRepository
                   Adhan CalculationMethod mapper
presentation/    ← SettingsScreen, SettingsViewModel
                   SettingsPresentationModule (Koin)
```

**Structure Decision**: Feature 010 follows the existing 3-layer Clean Architecture. The settings module is new and parallels existing features (athkar, quran, habits, prayer).

**Key Integration Points**:
- `feature:settings:domain` → consumed by `feature:prayer:presentation` and `feature:home:presentation`
- `feature:settings:presentation` replaces `SettingsScreen` placeholder in shared/navigation/Placeholders.kt
- SettingsRepository implementation delegates to DataStore (not Room)

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

N/A — no Constitution violations in this feature.
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
