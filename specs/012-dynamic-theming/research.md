# Research: Android Dynamic Theming (Material You)

**Decision**: Implement dynamic theming using Android-native APIs (`dynamicLightColorScheme`, `dynamicDarkColorScheme`) conditionally via a shared interface defined in `shared:designsystem`.

## Findings

### Android Material You (Dynamic Coloring)
- **API**: `androidx.compose.material3.dynamicLightColorScheme` and `dynamicDarkColorScheme` are available since API 31 (Android 12).
- **Implementation**: Needs `LocalContext.current` for `DynamicColors.getColorScheme`.
- **Condition**: Only call when `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`.

### KMP Expect/Actual
- **Expect**: `isDynamicColorSupported` (Boolean), `getDynamicColorScheme(darkTheme: Boolean)` (ColorScheme?).
- **Actual (Android)**: Check `Build.VERSION.SDK_INT`, then use `dynamicLightColorScheme` or `dynamicDarkColorScheme`.
- **Actual (iOS)**: `isDynamicColorSupported = false`, `getDynamicColorScheme = null`.

### DataStore Implementation
- Add `boolean` key to existing `SettingsDataStore`.
- Update `SettingsRepository` to expose `Flow<Boolean>` for dynamic theme state.

### Settings UI
- Settings Screen ViewModel consumes `SettingsRepository`.
- Conditionally render the toggle switch only if `isDynamicColorSupported` is true.

## Rationale
- Leveraging native APIs ensures 100% adherence to Android's Material You guidelines.
- The `expect/actual` pattern maintains clean separation between Android-specific theme logic and the platform-agnostic `shared:designsystem`.
- DataStore preference ensures persistence across sessions, following existing patterns in the project.

## Alternatives Considered
- **Custom Theme Calculation**: Rejected (too complex, prone to breaking changes in OS, does not provide native feel).
- **Platform-Independent Dynamic Calculation**: Rejected (unreliable, does not match device wallpaper-derived colors).

## Conclusion
The approach is solid and adheres to the Constitution's dependency and clean architecture principles.
