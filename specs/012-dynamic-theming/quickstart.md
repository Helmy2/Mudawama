# Quickstart: Android Dynamic Theming

This feature enables Android's Material You dynamic colors on supported devices.

## Setup
1. Define the `expect` interface in `shared:designsystem`.
2. Implement the `actual` providers in `androidMain` and `iosMain`.
3. Update `DataStore` to persist the dynamic theme preference.
4. Modify `MudawamaTheme` to select between dynamic and brand colors.
5. Update `SettingsViewModel` to conditionally render the toggle.

## Architecture
- **Expect/Actual**: Keeps dynamic coloring encapsulated.
- **DataStore**: Maintains user preference state.
- **Koin**: DI for repositories/viewmodels.
