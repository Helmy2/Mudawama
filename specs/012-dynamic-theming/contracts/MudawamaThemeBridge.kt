# Contracts: Dynamic Theme

## Theme Provider Bridge (shared/designsystem)

```kotlin
// Interface to bridge dynamic color support
public expect val isDynamicColorSupported: Boolean

// Provider for ColorScheme
@Composable
public expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?
```
