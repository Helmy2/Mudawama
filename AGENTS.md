# Mudawama Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-10

## Active Technologies

- **Kotlin** 2.3.20 (Kotlin Multiplatform) — Android (minSdk 30) + iOS 15+
- **Compose Multiplatform** 1.10.3 (UI)
- **Room** 2.8.4 (local storage) — `shared:core:database`, current schema version **3**
- **Koin** 4.2.0 (DI)
- **Ktor** 3.4.1 (network) — used in `feature:prayer:data` and `feature:quran:data`
- **kotlinx-serialization-json** 1.10.0 (JSON / Ktor content negotiation)
- **kotlinx-datetime** 0.7.1 (date handling)
- **kotlinx-coroutines** 1.10.2

## Project Structure

```text
feature/
  habits/
    domain/   presentation/   data/
  prayer/
    domain/   presentation/   data/
  quran/
    domain/   presentation/   data/
shared/
  core/
    database/   domain/   time/   presentation/   location/
  designsystem/
  navigation/
  umbrella-ui/
specs/
  007-quran-tracking/
```

## Architecture Rules (non-negotiable)

- **Clean Architecture** strict 3-layer split: `:domain` · `:data` · `:presentation`
- Dependency direction: `:presentation → :domain ← :data`
- Domain layer = 100% pure Kotlin — zero Android / Room / Ktor / Compose imports
- MVI pattern: `MviViewModel<State, Action, Event>`
- `Result<D,E>` + `DomainError` for all public APIs; `safeCall{}` for DAO writes
- Koin only for DI; `CoroutineDispatcher` always constructor-injected
- `Clock.System` calls forbidden outside `SystemTimeProvider` (SC-002)
- All strings via `stringResource(Res.string.*)` — zero hardcoded strings in Composables
- `Res` import: `mudawama.shared.designsystem.Res`
- `@Preview` import in `commonMain`: `androidx.compose.ui.tooling.preview.Preview`

## Shared Design System Components

All in `shared/designsystem/src/commonMain/kotlin/.../designsystem/components/`:

| Component | File | Purpose |
|---|---|---|
| `MudawamaSurfaceCard` | `SurfaceCard.kt` | Layout-agnostic card surface. `color = MaterialTheme.colorScheme.surface`, `shadowElevation = 1.dp`, `tonalElevation = 0.dp`. Accepts `shape` param (default `RoundedCornerShape(16.dp)`) and optional `onClick`. No forced inner padding. |
| `MudawamaBottomSheet` | `BottomSheet.kt` | App-wide bottom sheet wrapper. `containerColor = MudawamaTheme.colors.background`, `shape = RoundedCornerShape(topStart/End = 24.dp)`, `dragHandle = null`, `skipPartiallyExpanded = true`, 20dp top padding. Use for all feature bottom sheets. |
| `DateStrip` | `DateStrip.kt` | Horizontal 7-day date strip chip row. Shared across Prayer and Quran screens. |
| `PrimaryButton` | `PrimaryButton.kt` | Full-width primary CTA button. |
| `GhostButton` | `GhostButton.kt` | Outlined secondary button. |

## Database

- Room schema lives in `shared/core/database/`
- Current version: **3** (AutoMigration 2→3 removes `dailyGoalPages` + `pagesReadToday` from `quran_bookmarks`)
- Schema JSON files: `shared/core/database/schemas/…/2.json` and `3.json`
- New entities added in v3: `QuranDailyLogEntity`, `QuranGoalEntity`

## Ktor HttpClient Pattern

Each data module that needs network access provisions its **own named `HttpClient`** singleton in its Koin module — no shared client. Follow the prayer pattern:

```kotlin
internal val myHttpClientQualifier = named("my_feature")

val myDataModule = module {
    single<HttpClient>(myHttpClientQualifier) {
        HttpClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true }) }
            install(Logging) { level = LogLevel.HEADERS }
        }
    }
    // ...
}
```

`build.gradle.kts` for any data module needing Ktor:
```kotlin
plugins {
    alias(libs.plugins.kotlinxSerialization)
    // ...
}
sourceSets {
    commonMain.dependencies { implementation(libs.bundles.ktor) }
    androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
    iosMain.dependencies    { implementation(libs.ktor.client.darwin) }
}
```

## Recent Changes

- **007-quran-tracking** (commit `761c827`): Full Quran tracking feature — daily log, goal, bookmark with `alquran.cloud` API for accurate Surah+Ayah, reading streak, recent logs, 7-day date strip, read-only past-day navigation. Adds `MudawamaBottomSheet` and updated `MudawamaSurfaceCard` to shared designsystem. Adds `goalCount` TextField to `HabitBottomSheet` for `NUMERIC` habit type. Room DB bumped to v3.

- **006-prayer-screen**: Prayer times screen with Aladhan API, location-based fetching, habit log integration, 7-day date strip.

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
