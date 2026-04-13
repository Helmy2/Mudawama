# Feature Specification: Android Dynamic Theming

**Feature Branch**: `012-dynamic-theming`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "Implement Android Dynamic Theming"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Dynamic Theming on Android 12+ (Priority: P1)

As a user with an Android 12+ device, I want the app to automatically use my phone's wallpaper colors (Material You) so that the app feels native and personalized to my device.

**Why this priority**: This is the primary value proposition - providing a modern, personalized experience on supported devices. Users expect apps to leverage Android's dynamic color system.

**Independent Test**: Can be tested by launching the app on an Android 12+ device and verifying that colors adapt to the wallpaper.

**Acceptance Scenarios**:

1. **Given** a user opens the app on Android 12+ with dynamic theming enabled, **When** the app launches, **Then** the app displays colors extracted from the device wallpaper (dynamic colors)
2. **Given** a user opens the app on Android 12+ with dynamic theming enabled, **When** the user toggles Dark/Light mode, **Then** the dynamic colors adapt appropriately to the dark or light variant

---

### User Story 2 - Toggle Dynamic Theming in Settings (Priority: P2)

As a user, I want to be able to enable or disable dynamic theming from the Settings screen so that I can choose between dynamic colors or the app's default brand colors.

**Why this priority**: Provides user control over their visual experience. Some users may prefer consistent branding over dynamic colors.

**Independent Test**: Can be tested by navigating to Settings and toggling the dynamic theme switch, then verifying the app's colors change accordingly.

**Acceptance Scenarios**:

1. **Given** a user is on Android 12+, **When** they navigate to Settings, **Then** they see a "Dynamic Theme" toggle switch
2. **Given** a user enables the Dynamic Theme toggle, **When** they return to the app, **Then** the app uses dynamic colors from the wallpaper
3. **Given** a user disables the Dynamic Theme toggle, **When** they return to the app, **Then** the app uses Mudawama's default brand colors

---

### User Story 3 - Graceful Fallback on Unsupported Devices (Priority: P3)

As a user on an older Android device (below API 31) or on iOS, I want the app to use its default brand colors without any dynamic theme options, so that I have a consistent and visually appealing experience.

**Why this priority**: Ensures all users have a good experience regardless of their device capabilities. No degraded experience for users on older devices.

**Independent Test**: Can be tested on iOS or Android 11 device by verifying the app uses brand colors and Settings has no dynamic theme option.

**Acceptance Scenarios**:

1. **Given** a user opens the app on iOS, **When** they navigate to Settings, **Then** they do NOT see a Dynamic Theme toggle
2. **Given** a user opens the app on Android 10, **When** they navigate to Settings, **Then** they do NOT see a Dynamic Theme toggle
3. **Given** a user on any unsupported device, **When** the app displays, **Then** it uses Mudawama's default brand colors

---

### Edge Cases

- What happens when the wallpaper changes while the app is open?
- How does the system handle devices that support dynamic colors but have no wallpaper set?
- What happens if the dynamic color extraction fails (corrupted wallpaper, permission issues)?
- How does dark/light mode switching interact with dynamic theming mid-session?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST automatically detect if the device supports Android dynamic theming (API 31+) on Android
- **FR-002**: The system MUST default to using dynamic colors on supported Android devices unless the user has disabled it
- **FR-003**: Users MUST be able to toggle dynamic theming on or off from the Settings screen on supported devices
- **FR-004**: The system MUST persist the user's dynamic theme preference across app sessions
- **FR-005**: The system MUST seamlessly apply dynamic colors when the Dark/Light mode preference changes
- **FR-006**: The system MUST hide the dynamic theme toggle on iOS devices
- **FR-007**: The system MUST hide the dynamic theme toggle on Android devices below API 31
- **FR-008**: On unsupported devices, the system MUST use Mudawama's default brand colors (Light and Dark variants)
- **FR-009**: The system MUST define `expect val isDynamicColorSupported: Boolean` and `@Composable expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?` in commonMain, with actual implementations for androidMain (checking SDK version and using dynamicLight/DarkColorScheme) and iosMain (returning false/null)
- **FR-010**: The MudawamaTheme composable MUST accept the useDynamicTheme preference as a parameter and apply the dynamic ColorScheme only if isDynamicColorSupported is true AND the user preference is enabled
- **FR-011**: The system MUST add `use_dynamic_theme` boolean to DataStore preferences (defaulting to true) and expose it via the SettingsRepository

### Key Entities *(include if feature involves data)*

- **UserSettings**: Contains user preferences including `useDynamicTheme` (boolean, defaults to `true` on supported Android devices)
- **PlatformCapability**: Represents whether the current device supports dynamic theming

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: On supported Android 12+ devices, dynamic colors are applied within 500ms of app launch
- **SC-002**: Users on supported devices can toggle dynamic theming on/off with immediate visual feedback
- **SC-003**: 100% of Android 12+ users have the dynamic theming feature available (no false negatives in capability detection)
- **SC-004**: Zero visual regressions when switching between dynamic and static themes

---

## Assumptions

- Android 12 (API 31) is the minimum version for reliable dynamic color support
- The WallpaperManager API will be available and functional on supported devices
- Users who disable dynamic theming prefer the consistent brand color experience
- Dynamic color schemes from Android provide sufficient contrast for accessibility
- The feature does not require additional runtime permissions beyond what the app already has