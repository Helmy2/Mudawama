# shared:designsystem

A Kotlin Multiplatform Compose-based design system for Mudawama (MVP).

Module coordinates: :shared:designsystem

Quickstart

Add the module to your Gradle settings (already included) and depend on `project(":shared:designsystem")` from Android modules.

Sample usage

```kotlin
MudawamaTheme {
    PrimaryButton(onClick = {}, text = "Start")
}
```

Integration (Android)

- In `androidApp` add dependency:

```kotlin
implementation(project(":shared:designsystem"))
```

- Use `MudawamaTheme` in Activities/Composables.


