# Data Model: Settings Screen

**Feature**: 010-settings-screen  
**Date**: 2026-04-11

## Entities

### Domain Entities (feature:settings:domain)

#### CalculationMethod

Enum representing prayer calculation methods supported by the Adhan library.

```kotlin
enum class CalculationMethod {
    MUSLIM_WORLD_LEAGUE,
    EGYPTIAN,
    UMM_AL_QURA,
    KARACHI,
    ISNA,
    DUBAI,
    KUWAIT,
    QATAR,
    MOON_SIGHTING_COMMITTEE,
    SINGAPORE,
    TURKEY,
    TEHRAN
}
```

Relationships: Used by PrayerViewModel/HomeViewModel to configure prayer time calculations. Persisted as string key in DataStore.

#### LocationMode

Sealed class for GPS vs Manual location entry.

```kotlin
sealed class LocationMode {
    data object Gps : LocationMode()
    data class Manual(
        val latitude: Double,  // -90.0 to +90.0
        val longitude: Double  // -180.0 to +180.0
    ) : LocationMode()
}
```

Validation rules (per FR-004):
- latitude: -90.0 ≤ value ≤ 90.0
- longitude: -180.0 ≤ value ≤ 180.0

#### AppTheme

Enum for theme selection.

```kotlin
enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}
```

#### AppLanguage

Enum for language selection. Pure Kotlin — no Compose imports.

```kotlin
enum class AppLanguage(
    val code: String,  // "en" or "ar"
    val isRtl: Boolean
) {
    ENGLISH("en", false),
    ARABIC("ar", true)
}
```

In App.kt / MudawamaAppShell:
```kotlin
val layoutDirection = if (settings.appLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
```

#### AppSettings

Data class grouping all settings except Quran goal (per spec).

```kotlin
data class AppSettings(
    val calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val locationMode: LocationMode = LocationMode.Gps,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH
)
```

#### SettingsRepository

Interface for settings persistence (domain layer).

```kotlin
interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setCalculationMethod(method: CalculationMethod)
    suspend fun setLocationMode(mode: LocationMode)
    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setAppLanguage(language: AppLanguage)
}
```

Note: Does NOT store Quran goal per spec. Quran goal is managed by SetGoalUseCase in feature:quran:domain.

#### Use Cases (feature:settings:domain)

```kotlin
class ObserveSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.observeSettings()
}

class SetCalculationMethodUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(method: CalculationMethod) =
        repository.setCalculationMethod(method)
}

// Similar pattern for SetLocationModeUseCase, SetAppThemeUseCase, SetAppLanguageUseCase
```

### Data Layer Implementation (feature:settings:data)

#### SettingsRepositoryImpl

DataStore Preferences implementation. Stores LocationMode as three separate keys (DataStore stores scalars only):

```kotlin
class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    // Keys:
    // - "calculation_method": String (enum name)
    // - "location_mode_is_gps": Boolean (true = GPS, false = Manual)
    // - "location_mode_latitude": Double (meaningful only when is_gps = false)
    // - "location_mode_longitude": Double (meaningful only when is_gps = false)
    // - "app_theme": String (enum name)
    // - "app_language": String ("en" or "ar")
    //
    // observeSettings() combines is_gps + latitude + longitude → LocationMode
}
```

**No Adhan mapper in settings:data** — Clean Architecture: mapper lives in feature:prayer:data where Adhan is a dependency. PrayerViewModel passes domain CalculationMethod to a prayer use case; the use case's data-layer maps to Adhan internally.

### Presentation (feature:settings:presentation)

#### SettingsState

```kotlin
data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val error: StringResource? = null,
    // Validation errors for manual coordinate input
    val latitudeInput: String = "",
    val longitudeInput: String = "",
    val latitudeError: StringResource? = null,
    val longitudeError: StringResource? = null,
    val goalInput: String = "",
    val goalError: StringResource? = null
)
```

**Input field initialization**: When ObserveSettingsUseCase emits and LocationMode is Manual, SettingsViewModel MUST initialize:
```kotlin
latitudeInput = locationMode.latitude.toString()
longitudeInput = locationMode.longitude.toString()
```

#### SettingsAction

```kotlin
sealed interface SettingsAction {
    data class SetCalculationMethod(val method: CalculationMethod) : SettingsAction
    // SetLocationMode is ONLY for switching to GPS mode
    data class SetLocationMode(val mode: LocationMode) : SettingsAction
    data class UpdateLatitudeInput(val value: String) : SettingsAction
    data class UpdateLongitudeInput(val value: String) : SettingsAction
    // SaveManualLocation validates input fields, then sets LocationMode.Manual
    data object SaveManualLocation : SettingsAction
    data class SetAppTheme(val theme: AppTheme) : SettingsAction
    data class SetAppLanguage(val language: AppLanguage) : SettingsAction
    data class UpdateGoalInput(val value: String) : SettingsAction
    data object SaveGoal : SettingsAction
}
```

#### SettingsEvent

```kotlin
sealed interface SettingsEvent {
    data class ShowError(val message: StringResource) : SettingsEvent
    // GoalSaved: UI dismisses goal input dialog and shows brief confirmation snackbar
    data object GoalSaved : SettingsEvent
}
```

## State Transitions

| State | Action | New State |
|-------|--------|----------|
| Loading | Load complete | Data loaded |
| Data loaded | User changes method | Persisting → Saved |
| Persisting | Save success | Data loaded |
| Persisting | Save failure | Error shown |

## Validation Rules

- **Manual coordinates**: latitude [-90, 90], longitude [-180, 180]
- **Quran goal**: minimum 1 page (0 or empty rejected)

## Key Relationships

1. SettingsRepository → DataStore (storage as 6 separate keys)
2. SettingsViewModel ← ObserveSettingsUseCase → SettingsRepository (read)
3. SettingsViewModel → SetGoalUseCase (Quran goal write per FR-007)
4. PrayerViewModel ← ObserveSettingsUseCase (get CalculationMethod + LocationMode)
5. PrayerViewModel → prayer use case → feature:prayer:data (maps to Adhan)
6. HomeViewModel ← ObserveSettingsUseCase (get CalculationMethod + LocationMode)
7. App.kt / MudawamaAppShell ← ObserveSettingsUseCase (theme + language)

## Implementation Notes

- All settings string keys must be added to: `shared/designsystem/src/commonMain/composeResources/values/strings.xml`
- Arabic translations must be added to: `shared/designsystem/src/commonMain/composeResources/values-ar/strings.xml`
- DataStore Preferences already in libs.versions.toml (per Assumptions)