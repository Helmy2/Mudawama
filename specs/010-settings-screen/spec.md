# Feature Specification: Settings Screen

**Feature Branch**: `[010-settings-screen]`  
**Created**: 2026-04-11  
**Status**: Draft  
**Input**: User description: "Settings Screen"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Prayer Calculation Method (Priority: P1) 🎯 MVP

As a user, I want to select my preferred Islamic prayer calculation method so that prayer times are computed according to the methodology I trust.

**Why this priority**: Prayer calculation method is a fundamental setting that affects daily app usage for all users. The app currently uses a hardcoded default.

**Independent Test**: Can be tested by changing the calculation method and verifying prayer times update accordingly in both Prayer and Home screens.

**Acceptance Scenarios**:

1. **Given** the current default method, **When** user selects "Egyptian General Authority", **Then** prayer times recalculate using the new method
2. **Given** a method is selected, **When** the app restarts, **Then** the selected method persists and is applied on next prayer time calculation
3. **Given** user saves a new method in Settings, **When** they return to the Prayer screen, **Then** prayer times reflect the newly selected method

---

### User Story 2 - Location Mode (Priority: P1) 🎯 MVP

As a user, I want to choose between automatic GPS location or manual coordinate entry so that prayer times reflect my actual or desired location.

**Why this priority**: Location directly determines prayer times. Users traveling or in areas with poor GPS signal need manual entry option.

**Independent Test**: Can be tested by switching to Manual mode, entering coordinates, and verifying prayer times use those coordinates instead of GPS.

**Acceptance Scenarios**:

1. **Given** location mode is GPS, **When** user switches to Manual and enters latitude 40.7 and longitude -74.0, **Then** prayer times calculate for New York
2. **Given** location mode is Manual with valid coordinates, **When** the app restarts, **Then** saved coordinates are used (not GPS)
3. **Given** location mode is Manual, **When** GPS signal is available, **Then** Manual coordinates still take precedence (no GPS override)

---

### User Story 3 - Quran Daily Goal (Priority: P2)

As a user, I want to adjust my daily Quran reading goal so that the Home screen progress ring accurately reflects my personal target.

**Why this priority**: Users have varying reading capacities. The goal affects motivation tracking on the Home dashboard.

**Independent Test**: Can be tested by changing the daily goal from 5 to 10 pages and verifying the Home screen progress ring shows the new denominator.

**Acceptance Scenarios**:

1. **Given** daily goal is 5 pages, **When** user changes it to 10 pages, **Then** the Home screen Quran progress ring displays "X / 10 pages"
2. **Given** goal is changed, **When** app restarts, **Then** the new goal persists and is applied
3. **Given** user enters "0" as goal, **Then** save is rejected with inline error "Minimum goal is 1 page"

---

### User Story 4 - App Theme (Priority: P3)

As a user, I want to select my preferred visual theme so that the app appearance matches my lighting preference.

**Why this priority**: Theme customization improves user comfort across different environments and personal preferences.

**Independent Test**: Can be tested by changing theme and verifying the app palette updates immediately without restart.

**Acceptance Scenarios**:

1. **Given** theme is System, **When** user selects Dark, **Then** the app immediately switches to dark colors
2. **Given** Dark is selected, **When** app restarts, **Then** Dark theme is preserved and applied on launch
3. **Given** theme is set to Light, **When** user selects System, **Then** theme follows device setting

---

### User Story 5 - App Language / Arabic Support (Priority: P2)

As a user, I want to switch the app display language to Arabic so that the entire UI renders in Arabic with right-to-left layout direction.

**Why this priority**: Arabic-speaking users need native language support. This is a critical accessibility requirement for the app's target audience. Arabic numerals display automatically in the Arabic locale context.

**Independent Test**: Can be tested by selecting Arabic and verifying all UI strings change to Arabic and layout direction flips to RTL across all screens.

**Acceptance Scenarios**:

1. **Given** language is English, **When** user selects Arabic, **Then** all UI strings display in Arabic and layout mirrors to RTL
2. **Given** Arabic is selected, **When** app restarts, **Then** Arabic and RTL persist
3. **Given** Arabic is selected, **When** user navigates between screens, **Then** all screens render in RTL layout

---

### Edge Cases

1. **Invalid manual coordinate**: User enters latitude 100 or longitude -200 → inline validation error "Latitude must be between -90 and 90"
2. **Quran goal input of 0 or empty**: User enters "0" or clears field → save blocked, error message "Minimum goal is 1"
3. **DataStore write failure**: DataStore write throws exception → snackbar error shown, previous value retained in UI state
4. **First launch**: No saved settings exist → all defaults applied (MWL, GPS, 5 pages, System, English)
5. **Manual coordinates (0.0, 0.0)**: User enters 0.0, 0.0 → accepted without warning (valid coordinates off Africa's coast, near Ghana)
6. **Language changed mid-screen**: User changes to Arabic while viewing Prayer screen → applied on next screen navigation (not required to be immediate mid-composition)
7. **GPS mode with GPS failure**: Location mode is GPS but GPS returns error → Mecca fallback (21.3891, 39.8579) used as silent backup

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide calculation method selection with at least 12 options: Muslim World League, Egyptian General Authority, Umm Al-Qura, University of Islamic Sciences Karachi, ISNA, Dubai, Kuwait, Qatar, Moon Sighting Committee, Singapore, Turkey, Tehran
- **FR-002**: System MUST persist the selected calculation method and apply it to all prayer time computations
- **FR-003**: System MUST allow location mode selection between GPS (Automatic) and Manual entry
- **FR-004**: System MUST validate manual coordinates: latitude −90.0 to +90.0, longitude −180.0 to +180.0
- **FR-005**: System MUST validate that coordinates are numeric before accepting Manual mode
- **FR-006**: System MUST use Mecca coordinates (21.3891, 39.8579) ONLY when location mode is GPS AND GPS call fails — never when mode is Manual
- **FR-007**: System MUST invoke `SetGoalUseCase` from `feature:quran:domain` to save Quran daily goal — Settings does NOT duplicate persistence logic. Settings displays the current goal by observing `ObserveQuranStateUseCase` (or equivalent) and calls `SetGoalUseCase` on save.
- **FR-008**: System MUST apply theme immediately at the `App.kt` level by observing `ObserveSettingsUseCase` — no restart required
- **FR-009**: System MUST apply language selection at the `App.kt` / `MudawamaAppShell` level using `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` when Arabic is selected
- **FR-010**: System MUST store all settings in DataStore Preferences ONLY — no Room, no SharedPreferences
- **FR-011**: System MUST define `SettingsRepository` interface in `feature:settings:domain` with implementation in `feature:settings:data`
- **FR-012**: System MUST require `PrayerViewModel` and `HomeViewModel` to inject `ObserveSettingsUseCase` and use persisted calculation method and location mode
- **FR-013**: System MUST require `feature:prayer:presentation` and `feature:home:presentation` to add `feature:settings:domain` as a dependency
- **FR-014**: System MUST replace the placeholder `SettingsScreen` in `Placeholders.kt` with the real composable from `feature:settings:presentation`
- **FR-015**: System MUST NOT define `SettingsRoute` — it already exists in `shared/navigation/Routes.kt`
- **FR-016**: System MUST provide Arabic translations for ALL strings via `values-ar/strings.xml`, including: feature 009 strings AND all new strings added in feature 010 (calculation method names, location mode labels, theme options, language options, settings section headers, validation error messages, goal input labels)

### Key Entities *(include if feature involves data)*

- **CalculationMethod**: Enum representing prayer calculation methods (MWL, Egyptian, UmmAlQura, Karachi, ISNA, Dubai, Kuwait, Qatar, MSC, Singapore, Turkey, Tehran)
- **LocationMode**: Sealed class with `Gps` and `Manual(latitude: Double, longitude: Double)` variants
- **AppTheme**: Enum for System, Light, Dark
- **AppLanguage**: Enum for English (en), Arabic (ar)
- **AppSettings**: Data class grouping all settings except Quran goal (method, location mode, theme, language)
- **SettingsRepository**: Interface with observe and setter methods for each setting type. Does NOT store Quran goal.
- **Use Cases** (defined in `feature:settings:domain`): ObserveSettingsUseCase, SetCalculationMethodUseCase, SetLocationModeUseCase, SetAppThemeUseCase, SetAppLanguageUseCase

### Default Values

| Setting | Default |
|---------|---------|
| Calculation method | Muslim World League |
| Location mode | GPS (Automatic) |
| Quran daily goal | 5 pages |
| App theme | System |
| App language | English |

**† Quran daily goal default is owned by `feature:quran:data`, not by SettingsRepository. This row is informational only.**

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: When user changes calculation method, prayer times reflect the new method on the next visit to the Prayer or Home screen — no app restart required
- **SC-002**: When user enters Manual coordinates and saves, Prayer and Home screens use those coordinates (not GPS or Mecca)
- **SC-003**: When user changes Quran goal, the Home screen progress ring shows the new denominator on next visit
- **SC-004**: When user changes theme, the app palette updates immediately — no restart required
- **SC-005**: When user selects Arabic, Settings and Home screens display Arabic strings (those with translations) and layout direction is RTL across all screens
- **SC-006**: After cold restart, all settings persist and are applied correctly on app launch
- **SC-007**: Zero regression — Prayer tracking, Athkar tracking, Quran tracking, and Habits tracking all function identically before and after feature 010

---

## Assumptions

- DataStore Preferences is already available in the project version catalog (adding to libs.versions.toml is part of this feature's implementation task)
- Adhan library CalculationMethod enum is decoupled from the app's own enum via a mapper in `feature:settings:data`
- `SetGoalUseCase` already owns Quran goal persistence — Settings invokes it without duplicating storage logic
- Language switching uses Compose's `CompositionLocalProvider` + Compose resources' built-in locale resolution (values-ar folder) — not Android's locale system
- Arabic numerals (%1$d / %2$d) display as Eastern Arabic numerals (٣/٥) automatically based on locale context — no custom formatter required
- Generic confirm/cancel strings already exist in `shared/designsystem/strings.xml` and are reused by feature 010 dialogs
- Arabic translations for existing feature strings (Prayer, Athkar, Quran, Habits, Tasbeeh) are out of scope for feature 010. A separate values-ar/strings.xml entry is added only for the strings listed in the Arabic Translations Reference tables. Other feature strings will show their English fallback until a dedicated translation pass (future feature).

## Arabic Translations Reference

### Feature 009 Strings

The following table provides Arabic translations for feature 009 strings that must be added to `values-ar/strings.xml`:

| String Key | English | Arabic |
|-----------|---------|-------|
| home_next_prayer_label | Next Prayer | الصلاة القادمة |
| home_next_prayer_unavailable | Prayer times unavailable | أوقات الصلاة غير متاحة |
| home_all_prayers_done | All prayers completed today! | أتممت جميع الصلوات اليوم! |
| home_athkar_morning_label | Morning | الصباح |
| home_athkar_evening_label | Evening | المساء |
| home_athkar_not_started | Tap to get started | اضغط للبدء |
| home_athkar_done | Complete | مكتمل |
| home_athkar_pending | Pending | لم يكتمل |
| home_quran_pages_progress | %1$d / %2$d pages | %1$d / %2$d صفحة |
| home_quran_label | Quran | القرآن |
| home_daily_rituals_label | Daily Rituals | العبادات اليومية |
| home_settings_icon_description | Settings | الإعدادات |
| settings_placeholder_title | Settings | الإعدادات |
| settings_placeholder_coming_soon | Coming soon | قريباً |

### Feature 010 Strings

The following table provides Arabic translations for new strings added by feature 010. All new strings in this feature MUST have Arabic translations from day one:

| String Key                          | English                                | Arabic                              |
|-------------------------------------|----------------------------------------|-------------------------------------|
| settings_section_calculation_method | Prayer Calculation Method              | طريقة حساب الصلاة                   |
| settings_section_location           | Location                               | الموقع                              |
| settings_section_quran_goal         | Daily Quran Goal                       | الهدف اليومي للقرآن                 |
| settings_section_appearance         | Appearance                             | المظهر                              |
| settings_section_language           | Language                               | اللغة                               |
| settings_option_gps_automatic       | GPS (Automatic)                        | نظام تحديد المواقع (تلقائي)         |
| settings_option_manual              | Manual                                 | يدوي                                |
| settings_label_latitude             | Latitude                               | خط العرض                            |
| settings_label_longitude            | Longitude                              | خط الطول                            |
| settings_label_goal_pages           | Pages per day                          | صفحات في اليوم                      |
| settings_option_theme_system        | System                                 | النظام                              |
| settings_option_theme_light         | Light                                  | فاتح                                |
| settings_option_theme_dark          | Dark                                   | داكن                                |
| settings_option_language_english    | English                                | الإنجليزية                          |
| settings_option_language_arabic     | Arabic                                 | العربية                             |
| settings_error_invalid_latitude     | Latitude must be between -90 and 90    | يجب أن يكون خط العرض بين -90 و 90   |
| settings_error_invalid_longitude    | Longitude must be between -180 and 180 | يجب أن يكون خط الطول بين -180 و 180 |
| settings_error_minimum_goal         | Minimum goal is 1 page                 | أقل هدف هو صفحة واحدة               |
| settings_method_mwl                 | Muslim World League                    | رابطة العالم الإسلامي               |
| settings_method_egyptian            | Egyptian General Authority             | الهيئة المصرية العامة               |
| settings_method_umm_alqura          | Umm Al-Qura                            | أم القرى                            |
| settings_method_karachi             | University of Islamic Sciences Karachi | جامعة العلوم الإسلامية بكراتشي      |
| settings_method_isna                | ISNA                                   | الجمعية الإسلامية لأمريكا الشمالية  |
| settings_method_dubai               | Dubai                                  | دبي                                 |
| settings_method_kuwait              | Kuwait                                 | الكويت                              |
| settings_method_qatar               | Qatar                                  | قطر                                 |
| settings_method_msc                 | Moon Sighting Committee                | لجنة رؤية الهلال                    |
| settings_method_singapore           | Singapore                              | سنغافورة                            |
| settings_method_turkey              | Turkey                                 | تركيا                               |
| settings_method_tehran              | Tehran                                 | طهران                               |