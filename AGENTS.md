# Mudawama Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-13

## Active Technologies
- **Kotlin** 2.3.20 (Kotlin Multiplatform) — Android (minSdk 30) + iOS 15+
- **Compose Multiplatform** 1.10.3 (UI)
- **Room** 2.8.4 (local storage) — `shared:core:database`, current schema version **4**
- **Koin** 4.2.0 (DI)
- **Ktor** 3.4.1 (network) — used in `feature:prayer:data` and `feature:quran:data`
- **kotlinx-serialization-json** 1.10.0 (JSON / TypeConverter for Athkar counter map)
- **kotlinx-datetime** 0.7.1 (date handling)
- **kotlinx-coroutines** 1.10.2
- **androidx.datastore.preferences** — session storage + 6 Athkar notification pref keys
- **Android Sensor API** (TYPE_ROTATION_VECTOR) + **iOS CoreLocation** (CLLocationManager) — compass sensor (011-qibla-compass)
- DataStore (settings location), no new DB entities required (011-qibla-compass)
- Kotlin 2.3.20 + Compose Multiplatform, DataStore, Koin (012-dynamic-theming)
- DataStore (for `use_dynamic_theme` preference) (012-dynamic-theming)

## Project Structure

```text
feature/
  home/
    presentation/         ← NEW (009-home-dashboard): HomeScreen, HomeViewModel,
                            NextPrayerCard, AthkarSummaryCard, QuranProgressCard,
                            TasbeehSummaryCard, HabitsSummarySection, SkeletonBlock
  athkar/
    domain/   presentation/   data/
  habits/
    domain/   presentation/   data/
  prayer/
    domain/   presentation/   data/
  quran/
    domain/   presentation/   data/
shared/
  core/
    database/   domain/   data/   time/   presentation/   location/
  designsystem/
  navigation/             ← androidMain/ + iosMain/ added for AppBackHandler expect/actual
  umbrella-ui/
specs/
  007-quran-tracking/
  008-athkar-tasbeeh/
  009-home-dashboard/
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
- **Notification strings** must be resolved in the UI layer (Composable) and passed as action parameters to the ViewModel — never resolved inside the ViewModel itself (KMP limitation)
- **`feature:home:presentation` navigation rule**: this module has **no dependency on `shared:navigation`**. Navigation is done via plain `() -> Unit` callbacks passed from `MudawamaAppShell`. `HomeUiEvent` uses a nested `sealed interface Navigate` with typed objects (`ToPrayer`, `ToAthkar`, `ToQuran`, `ToSettings`, `ToHabits`, `ToTasbeeh`) — no `Route` types in the home module.
- **`AppBackHandler` pattern**: every non-Home `entryProvider` branch in `MudawamaAppShell` wraps content in `AppBackHandler { goHome() }`. `goHome()` clears the back-stack and adds `HomeRoute`. `AppBackHandler` is an `expect/actual` composable in `shared:navigation` — Android actual wraps `BackHandler`; iOS actual is a no-op.
- **Bottom bar visibility**: only shown on top-level routes (`HomeRoute`, `PrayerRoute`, `QuranRoute`, `AthkarRoute`). Hidden on push destinations (`HabitsRoute`, `TasbeehRoute`, `SettingsRoute`, `QiblaRoute`).

## iOS SwiftUI Integration Pattern (011-qibla-compass)

For performance-critical features requiring 60-120fps updates or complex platform APIs, Mudawama uses **native SwiftUI views** on iOS while keeping Compose for Android:

1. **Define Kotlin interface** in `:domain` layer (e.g., `QiblaViewControllerProvider`)
2. **Implement in Swift** (`IosQiblaViewControllerProvider: QiblaViewControllerProvider`)
3. **Register via `initializeKoin()`** — Swift instance passed alongside `IosLocationProvider`, `IosEncryptor`, etc.
4. **Platform-specific Koin module** — `iosQiblaPresentationModule(iosQiblaViewControllerProvider)` registers provider
5. **expect/actual screen** — Android: full Compose; iOS: `UIKitViewController` + `koinInject<QiblaViewControllerProvider>()`
6. **Communication bridge** — Kotlin `object` stores ViewModel + callbacks for Swift to retrieve

**Example**: `IosQiblaViewControllerProvider` creates `UIHostingController(rootView: QiblaViewContent)`, observes Kotlin `StateFlow` via Timer (100ms / 10 FPS), uses `UIImpactFeedbackGenerator` for haptics. See `specs/011-qibla-compass/IMPLEMENTATION.md` for full details.

**When to use**: 60-120fps animations, complex platform delegates (CLLocationManagerDelegate), or native UX patterns. For CRUD screens, use Compose Multiplatform.

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
- Current version: **4** (AutoMigration 3→4 adds `athkar_daily_logs`, `tasbeeh_goals`, `tasbeeh_daily_totals`)
- Schema JSON files: `schemas/2.json`, `3.json`, `4.json`
- All entities in v4: `HabitEntity`, `HabitLogEntity`, `PrayerStatusEntity`, `QuranBookmarkEntity`, `QuranDailyLogEntity`, `QuranGoalEntity`, `AthkarDailyLogEntity`, `TasbeehGoalEntity`, `TasbeehDailyTotalEntity`

## Navigation (as built — 009-home-dashboard)

- **4-tab bottom bar**: Home, Prayers, Quran, Athkar
- **Push destinations** (no bottom bar): `HabitsRoute`, `TasbeehRoute`, `SettingsRoute`
- **`BottomNavItem`** entries: HOME, PRAYER, QURAN, ATHKAR (TASBEEH removed in 009)
- `isTopLevel` check: `backStack.lastOrNull()?.let { it::class in topLevelRoutes } ?: false`
- `goHome()`: `backStack.clear(); backStack.add(HomeRoute)`
- `AppBackHandler` added to `shared/navigation/src/commonMain/` (expect), `androidMain/` (BackHandler actual), `iosMain/` (no-op actual)
- Named args forbidden when calling Kotlin function types — use positional args in `entryProvider` lambda calls

## Notification Infrastructure (008-athkar-tasbeeh)

- `NotificationScheduler` interface in `shared/core/domain` — `scheduleDailyReminder(id, hour, minute, title, body)`, `cancelReminder(id)`
- `NotificationPermissionChecker` interface in `shared/core/domain` — `hasPermission()`, `requestPermission()`
- **Android**: `AndroidNotificationScheduler` (AlarmManager + `setExactAndAllowWhileIdle`), `AthkarNotificationReceiver` (BroadcastReceiver — re-schedules next day), `AndroidNotificationPermissionChecker` — all in `shared/core/data/src/androidMain`
- **iOS**: `IosNotificationScheduler` (UNCalendarNotificationTrigger, repeats=true), `IosNotificationPermissionChecker` (UNUserNotificationCenter) — all in `shared/core/data/src/iosMain`
- Koin modules: `androidNotificationsModule` (androidMain), `iosNotificationsModule` (iosMain) — both in `shared/core/data/src/.../di/NotificationsModule.kt`
- Notification IDs stable constants: `AthkarNotificationIds.MORNING = 1001`, `AthkarNotificationIds.EVENING = 1002`
- DataStore keys: `AthkarPreferencesKeys` in `shared/core/data/src/commonMain`
- `AthkarNotificationRepository` → `AthkarNotificationRepositoryImpl` in `feature:athkar:data`
- `AthkarNotificationViewModel` in `feature:athkar:presentation/notification/` — observes prefs, dispatches toggle/time actions

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

## Completed Features

- **007-quran-tracking** (commit `761c827`): Full Quran tracking feature — daily log, goal, bookmark with `alquran.cloud` API for accurate Surah+Ayah, reading streak, recent logs, 7-day date strip, read-only past-day navigation. Adds `MudawamaBottomSheet` and updated `MudawamaSurfaceCard` to shared designsystem. Adds `goalCount` TextField to `HabitBottomSheet` for `NUMERIC` habit type. Room DB bumped to v3.

- **008-athkar-tasbeeh** ✅ **Complete**: Daily Athkar tracking (Morning/Evening/Post-Prayer), Tasbeeh counter with daily totals and goal, configurable daily notification reminders. Room DB bumped to v4 (3 new entities). All UI edge-to-edge, dark theme safe. Notification infra: Android AlarmManager + BroadcastReceiver; iOS UNCalendarNotificationTrigger.

- **009-home-dashboard** ✅ **Complete**: Home Dashboard aggregating all features into a single scrollable screen. New `feature:home:presentation` module. Summary cards: `NextPrayerCard` (full-width), `AthkarSummaryCard`, `QuranProgressCard`, `TasbeehSummaryCard` (2-column row), `HabitsSummarySection`. Navigation via 6 plain callbacks (no `shared:navigation` dep in home module). `AppBackHandler` expect/actual added to `shared:navigation`. Bottom bar reduced to 4 tabs (TASBEEH removed). `HabitsRoute` + `TasbeehRoute` added as push destinations. `SettingsRoute` + `SettingsScreen` placeholder added. No new DB entities.

- **011-qibla-compass** ✅ **Complete**: Qibla Compass feature with real-time compass updates on both platforms. New `feature:qibla:domain`, `feature:qibla:data`, `feature:qibla:presentation` modules. **iOS uses native SwiftUI** via `IosQiblaViewControllerProvider` (implements `QiblaViewControllerProvider` Kotlin interface, passed via `initializeKoin()`). Android uses full Compose. Sensors: Android TYPE_ROTATION_VECTOR; iOS CLLocationManager with callbackFlow (object delegation pattern — no NSObject subclass). Haversine formula for Qibla angle calculation. Haptic feedback on alignment (±2°). Calibration warnings for LOW/UNRELIABLE accuracy. Location permission fallback with "Go to Settings". All strings via stringResource. Navigation from HomeScreen Qibla card. Prayer habits now seeded in `HomeViewModel.init` to ensure prayers show on first app launch. **Architecture pattern**: See `specs/011-qibla-compass/IMPLEMENTATION.md` for full Swift-Kotlin interop details.


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->


## Recent Changes
- 012-dynamic-theming: Added Kotlin 2.3.20 + Compose Multiplatform, DataStore, Koin
- 011-qibla-compass: Fixed iOS CompassSensorManager to use callbackFlow + object delegation instead of NSObject inheritance (Kotlin/Native limitation)
- 011-qibla-compass: Integrated native iOS SwiftUI view via `IosQiblaViewControllerProvider` pattern for optimal performance
