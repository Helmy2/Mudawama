# Quickstart: Settings Screen

**Feature**: 010-settings-screen  
**Date**: 2026-04-11

## Implementation Order

### Phase 1: Domain Layer (feature:settings:domain)

*Clean Architecture: Domain has no dependencies, so it comes first.*

1. Create enums:
   - `CalculationMethod` (12 options: MWL, Egyptian, UmmAlQura, Karachi, ISNA, Dubai, Kuwait, Qatar, MSC, Singapore, Turkey, Tehran)
   - `AppTheme` (SYSTEM, LIGHT, DARK)
   - `AppLanguage` (ENGLISH with code "en", ARABIC with code "ar", isRtl: Boolean)

2. Create `LocationMode` sealed class:
   - `Gps` object
   - `Manual(latitude: Double, longitude: Double)` data class

3. Create `AppSettings` data class

4. Create `SettingsRepository` interface (observe + setters)

5. Create use cases:
   - `ObserveSettingsUseCase`
   - `SetCalculationMethodUseCase`
   - `SetLocationModeUseCase`
   - `SetAppThemeUseCase`
   - `SetAppLanguageUseCase`

### Phase 2: Data Layer (feature:settings:data)

*Data implements domain interfaces.*

1. Add dependencies to build.gradle.kts:
   - androidx.datastore:datastore-preferences
   - kotlinx-serialization-json

2. Create `SettingsRepositoryImpl`:
   - Path: `feature/settings/data/src/commonMain/kotlin/.../SettingsRepositoryImpl.kt`
   - Uses DataStore<Preferences>
   - Keys: "calculation_method", "location_mode", "app_theme", "app_language"

3. Create Adhan mapper:
   - Maps domain `CalculationMethod` ↔ Adhan library (`com.batoulapps.adhan.CalculationMethod`)
   - Used by PrayerViewModel/HomeViewModel after obtaining settings

### Phase 3: Presentation Layer (feature:settings:presentation)

1. Create `SettingsViewModel`:
   - MVI pattern: State, Action, Event
   - Inject ObserveSettingsUseCase, SetGoalUseCase (from quran:domain)
   - Inject CoroutineDispatcher

2. Create `SettingsScreen` Composable:
   - Section: Calculation Method (dropdown/picker)
   - Section: Location (GPS/Manual toggle + coordinate text fields)
   - Section: Quran Goal (numeric input)
   - Section: Theme (System/Light/Dark)
   - Section: Language (English/Arabic)

3. Add Koin module:
   - `SettingsPresentationModule` — NOT HomePresentationModule (avoids collision)

### Phase 4: Integration

1. **Update PrayerViewModel** (feature:prayer:presentation):
   - Inject ObserveSettingsUseCase
   - Use persisted method + location mode (not hardcoded)

2. **Update HomeViewModel** (feature:home:presentation):
   - Inject ObserveSettingsUseCase
   - Use persisted method + location mode

3. **Update App.kt / MudawamaAppShell**:
   - Observe settings for theme → apply immediately
   - Observe settings for language → apply LayoutDirection.Rtl when Arabic

4. **Replace placeholder**:
   - Replace SettingsScreen in Placeholders.kt with real composable

5. **Add settings dependency**:
   - feature:prayer:presentation adds feature:settings:domain
   - feature:home:presentation adds feature:settings:domain

### Phase 5: String Resources

1. **Add English strings** to `values/strings.xml`:
   - All string keys from spec Arabic Translations Reference (Feature 010)

2. **Create Arabic strings** in `values-ar/strings.xml`:
   - All keys from Feature 009 + Feature 010 tables
   - RTL applied automatically via Compose

## Key Code Locations

| File | Path |
|------|------|
| Strings (EN) | shared/designsystem/src/commonMain/composeResources/values/strings.xml |
| Strings (AR) | shared/designsystem/src/commonMain/composeResources/values-ar/strings.xml |
| SettingsRoute | shared/navigation/Routes.kt |
| Placeholders | shared/navigation/Placeholders.kt |
| App.kt | shared/umbrella-ui/src/commonMain/kotlin/.../App.kt |
| PrayerViewModel | feature/prayer/presentation/.../PrayerViewModel.kt |
| HomeViewModel | feature/home/presentation/.../HomeViewModel.kt |

## Testing Checklist

- [ ] Calculation method change reflects in Prayer screen
- [ ] Manual coordinates used (not GPS or Mecca) when in Manual mode
- [ ] Mecca fallback used only on GPS mode + GPS failure
- [ ] Quran goal updates Home screen ring
- [ ] Theme changes immediately (no restart)
- [ ] Arabic shows RTL layout + translated strings
- [ ] Settings persist after cold restart
- [ ] No regression in Prayer/Athkar/Quran/Habits

## Common Pitfalls

1. **Don't use Room** for settings — DataStore ONLY per FR-010
2. **Don't use hardcoded strings** — use stringResource()
3. **Don't import Dispatchers.IO** in ViewModels — inject CoroutineDispatcher
4. **Don't create multiple strings.xml** — use shared/designsystem only
5. **Don't duplicate SetGoalUseCase logic** — invoke existing use case
6. **Don't use HomePresentationModule** — use SettingsPresentationModule
7. **Don't build data before domain** — follows Clean Architecture dependency order