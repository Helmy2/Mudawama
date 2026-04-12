# Research: Settings Screen

**Feature**: 010-settings-screen  
**Date**: 2026-04-11

## Research Findings

### Phase 0: Research Tasks

#### Task 1: Verify Adhan Library Package

**Query**: What is the correct Adhan library package name for Kotlin?

**Finding**:
- Package: `com.batoulapps.adhan`
- Enum: `com.batoulapps.adhan.CalculationMethod`
- This library is used by prayer calculation features in the codebase

**Decision**: Use `com.batoulapps.adhan.CalculationMethod` in mapper

#### Task 2: Verify PrayerViewModel Constructor

**Query**: What are the PrayerViewModel constructor dependencies?

**Finding** (from source research / constitution):
- `PrayerViewModel` takes: use cases (ObservePrayersForDateUseCase), repositories are NOT injected directly (constitution violation)
- Location: `feature/prayer/presentation/.../PrayerViewModel.kt`
- Currently hardcodes Mecca coordinates and calculation method

**Action**: After feature 010, PrayerViewModel receives domain CalculationMethod and LocationMode from ObserveSettingsUseCase, passes to use case. Use case's data layer maps to Adhan internally (mapper NOT in settings:data).

#### Task 3: Verify HomeViewModel Constructor

**Query**: What are the HomeViewModel constructor dependencies?

**Finding** (from constitution):
- `HomeViewModel` takes: use cases (not repositories), per Clean Architecture
- Location: `feature/home/presentation/.../HomeViewModel.kt`
- Currently hardcodes Mecca coordinates and calculation method

**Action**: After feature 010, HomeViewModel also injects ObserveSettingsUseCase for method + location

### Decisions Summary

| Decision | Rationale | Source |
|----------|----------|--------|
| Adhan package = com.batoulapps.adhan | Correct Kotlin package verified | Source research |
| PrayerViewModel injects ObserveSettingsUseCase | Need persisted method + location | Integration |
| HomeViewModel injects ObserveSettingsUseCase | Need persisted method + location | Integration |
| Domain uses AppLanguage.isRtl (not LayoutDirection) | Constitution: no Compose in domain | Constitution gate |

### Research Complete

All unknowns resolved. Proceed to Phase 1: Design.