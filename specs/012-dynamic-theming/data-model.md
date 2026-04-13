# Data Model: Dynamic Theming

## UserSettings Preferences
- **Entity**: `UserSettings`
- **Fields**:
  - `use_dynamic_theme`: `Boolean`, default `true` on Android 12+, `false` on iOS/older Android.
- **Persistence**: `androidx.datastore.preferences.preferencesDataStore`.
- **Relationship**: Accessed by `SettingsRepository` and `MudawamaAppShell` for theme selection.

## Platform Capability
- **Entity**: `PlatformCapability`
- **Fields**:
  - `isDynamicColorSupported`: `Boolean`, determined by runtime `Build.VERSION.SDK_INT` on Android (API 31+).
- **Access**: Determined at runtime via `commonMain` `expect` bridge.
