# Quickstart: 009 Home Dashboard

> **Status**: ✅ Complete — this document reflects the **as-built** implementation,
> which deviates in several important ways from the original spec draft.

## Overview

This feature creates a new `feature:home:presentation` module containing the Home Dashboard screen.
`feature:habits:presentation` is **not modified**. Changes touched four areas:

1. **`feature:home:presentation`** (NEW MODULE) — `HomeScreen.kt`, `HomeViewModel.kt`, five summary
   card/section composables (`NextPrayerCard`, `AthkarSummaryCard`, `QuranProgressCard`,
   `TasbeehSummaryCard`, `HabitsSummarySection`), `SkeletonBlock`, MVI model files, Koin DI module,
   and `build.gradle.kts`.
2. **`shared:navigation`** — add `SettingsRoute`, `HabitsRoute` to `Routes.kt`; add `SettingsScreen`
   placeholder to `Placeholders.kt`; wire `homeScreen` param (6 callbacks) + `SettingsRoute` +
   `HabitsRoute` + `TasbeehRoute` in `MudawamaAppShell`; add `AppBackHandler` expect/actual;
   reduce bottom bar to 4 tabs (remove TASBEEH).
3. **`shared:umbrella-ui`** — add `homeScreen` argument (6 callbacks) in `App.kt`; add
   `HabitsScreen` import for HabitsRoute slot; add `homePresentationModule()` to both
   `KoinInitializer`s.
4. **`shared:designsystem`** — add new string keys to `strings.xml`.

---

## Key Deviations from Original Spec

| Area | Spec Said | As Built |
|---|---|---|
| `feature:home:presentation` deps | Includes `shared:navigation` | **NO `shared:navigation` dep** — navigation via plain callbacks |
| Navigation event type | `HomeUiEvent.NavigateTo(route: Route)` | `HomeUiEvent.Navigate` nested sealed interface with typed objects |
| Summary cards | Next Prayer + Athkar + Quran | + **TasbeehSummaryCard** (4th card) |
| Layout | Athkar + Quran in 2-col row | **Athkar full-width**, then Row(Quran + Tasbeeh) |
| `MudawamaAppShell` `homeScreen` param | `(onNavigate: (Route) -> Unit) -> Unit` | **6 plain `() -> Unit` callbacks** |
| Bottom bar tabs | 5 tabs (includes Tasbeeh) | **4 tabs** (Home, Prayer, Quran, Athkar) |
| Push destinations | Settings only | Settings + **HabitsRoute** + **TasbeehRoute** |
| Back navigation | Not specified | **`AppBackHandler` expect/actual** → `goHome()` on all push destinations |
| `shared:navigation` source sets | commonMain only | + `androidMain/` + `iosMain/` for `AppBackHandler` |

---

## Files Created

| File | Location |
|---|---|
| `build.gradle.kts` | `feature/home/presentation/` |
| `HomeScreen.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/` |
| `HomeViewModel.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/` |
| `components/NextPrayerCard.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/components/` |
| `components/AthkarSummaryCard.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/components/` |
| `components/QuranProgressCard.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/components/` |
| `components/TasbeehSummaryCard.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/components/` |
| `components/HabitsSummarySection.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/components/` |
| `components/SkeletonBlock.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/components/` |
| `model/HomeUiState.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/model/` |
| `model/HomeUiAction.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/model/` |
| `model/HomeUiEvent.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/model/` |
| `di/HomePresentationModule.kt` | `feature/home/presentation/src/commonMain/.../home/presentation/di/` |
| `AppBackHandler.kt` (expect) | `shared/navigation/src/commonMain/.../navigation/` |
| `AppBackHandler.kt` (Android actual) | `shared/navigation/src/androidMain/.../navigation/` |
| `AppBackHandler.kt` (iOS actual) | `shared/navigation/src/iosMain/.../navigation/` |

## Files Modified

| File | Change |
|---|---|
| `settings.gradle.kts` | Add `include(":feature:home:presentation")` |
| `shared/navigation/Routes.kt` | Add `SettingsRoute`, `HabitsRoute`; remove TASBEEH from `BottomNavItem` (4 tabs) |
| `shared/navigation/Placeholders.kt` | Add `SettingsScreen(onBack: () -> Unit = {})` composable |
| `shared/navigation/MudawamaAppShell.kt` | 6-callback `homeScreen` param; `AppBackHandler` on all non-Home entries; `goHome()`; `isTopLevel` bar visibility; `HabitsRoute` + `TasbeehRoute` + `SettingsRoute` wired |
| `shared/navigation/build.gradle.kts` | Add `androidMain` + `iosMain` source sets for `AppBackHandler` |
| `shared/umbrella-ui/src/commonMain/.../App.kt` | 6 callbacks wired; `HabitsScreen` import for HabitsRoute slot |
| `shared/umbrella-ui/src/androidMain/.../KoinInitializer.kt` | Add `homePresentationModule()` |
| `shared/umbrella-ui/src/iosMain/.../KoinInitializer.kt` | Add `homePresentationModule()` |
| `shared/umbrella-ui/build.gradle.kts` | Add `feature:home:presentation` dep |
| `shared/designsystem/.../strings.xml` | Add 16 new string keys (14 planned + 2 Tasbeeh keys) |

---

## `feature/home/presentation/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp.compose")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.home.presentation"
    }

    configureIosFramework("FeatureHomePresentation", isStatic = true)

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            // NOTE: NO projects.shared.navigation dependency
            // Aggregator — explicit cross-feature domain deps (architecturally correct)
            implementation(projects.feature.habits.domain)
            implementation(projects.feature.prayer.domain)
            implementation(projects.feature.athkar.domain)
            implementation(projects.feature.quran.domain)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.compose)
            implementation(libs.bundles.lifecycle)
            implementation(libs.compose.resources)
            implementation(libs.koin.compose.viewmodel)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)
            implementation(libs.kotlinx.datetime)
            implementation(libs.material.icons.extended)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ui.tooling)
            implementation(libs.ui.tooling.preview)
            implementation(libs.koin.android)
        }
    }
}
```

---

## `HomeViewModel` Constructor (as built)

```kotlin
class HomeViewModel(
    // Habits use cases (for the existing habits list section)
    private val observeHabitsUseCase: ObserveHabitsWithTodayStatusUseCase,
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val toggleCompletionUseCase: ToggleHabitCompletionUseCase,
    private val incrementCountUseCase: IncrementHabitCountUseCase,
    private val decrementCountUseCase: DecrementHabitCountUseCase,
    private val resetTodayLogUseCase: ResetHabitTodayLogUseCase,

    // Cross-feature summary use cases
    private val observePrayersForDateUseCase: ObservePrayersForDateUseCase,
    private val observeAthkarCompletionUseCase: ObserveAthkarCompletionUseCase,
    private val observeQuranStateUseCase: ObserveQuranStateUseCase,
    private val observeTasbeehGoalUseCase: ObserveTasbeehGoalUseCase,        // NEW
    private val observeTasbeehDailyTotalUseCase: ObserveTasbeehDailyTotalUseCase, // NEW
    private val locationProvider: LocationProvider,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
) : MviViewModel<HomeUiState, HomeUiAction, HomeUiEvent>(HomeUiState())
```

`HomeViewModel` also contains a `nextPendingFromNow()` helper that parses prayer `"HH:mm"` time strings
and compares against the current `LocalTime` to find the first future pending prayer.

---

## `HomeUiEvent.Navigate` (as built — no Route types)

```kotlin
sealed interface HomeUiEvent {
    sealed interface Navigate : HomeUiEvent {
        data object ToPrayer   : Navigate
        data object ToAthkar   : Navigate
        data object ToQuran    : Navigate
        data object ToSettings : Navigate
        data object ToHabits   : Navigate
        data object ToTasbeeh  : Navigate
    }
    // ... other events
}
```

`HomeScreen` maps each `Navigate` subtype to the appropriate `() -> Unit` callback received
from `MudawamaAppShell`. No `Route` type is imported in `feature:home:presentation`.

---

## `MudawamaAppShell` Change Summary (as built)

```kotlin
@Composable
fun MudawamaAppShell(
    homeScreen: @Composable (
        onNavigateToPrayer: () -> Unit,
        onNavigateToAthkar: () -> Unit,
        onNavigateToQuran: () -> Unit,
        onNavigateToSettings: () -> Unit,
        onNavigateToHabits: () -> Unit,
        onNavigateToTasbeeh: () -> Unit,
    ) -> Unit = { _, _, _, _, _, _ -> },
    prayerScreen: @Composable () -> Unit = {},
    quranScreen: @Composable () -> Unit = {},
    athkarScreen: @Composable () -> Unit = {},
    // ... other params
) {
    fun goHome() { backStack.clear(); backStack.add(HomeRoute) }

    // Bottom bar only visible on top-level routes
    val topLevelRoutes = BottomNavItem.entries.map { it.route::class }.toSet()
    val isTopLevel = backStack.lastOrNull()?.let { it::class in topLevelRoutes } ?: false

    // entryProvider includes:
    // entry<HomeRoute> { homeScreen(toPrayer, toAthkar, toQuran, toSettings, toHabits, toTasbeeh) }
    // entry<PrayerRoute> { AppBackHandler { goHome() }; prayerScreen() }
    // entry<HabitsRoute> { AppBackHandler { goHome() }; HabitsScreen() }
    // entry<TasbeehRoute> { AppBackHandler { goHome() }; tasbeehScreen() }
    // entry<SettingsRoute> { AppBackHandler { goHome() }; SettingsScreen { goHome() } }
}
```

**Critical**: Named arguments are forbidden when calling Kotlin function types — use positional
args in `entryProvider` lambda calls.

---

## New String Keys (`strings.xml`)

```xml
<!-- Home Dashboard — Next Prayer card -->
<string name="home_next_prayer_label">NEXT PRAYER</string>
<string name="home_next_prayer_unavailable">Prayer times unavailable</string>
<string name="home_all_prayers_done">All prayers done today!</string>

<!-- Home Dashboard — Athkar card -->
<string name="home_athkar_morning_label">Morning</string>
<string name="home_athkar_evening_label">Evening</string>
<string name="home_athkar_not_started">Tap to get started</string>
<string name="home_athkar_done">Done</string>
<string name="home_athkar_pending">Pending</string>

<!-- Home Dashboard — Quran card -->
<string name="home_quran_pages_progress">%1$d / %2$d pages</string>
<string name="home_quran_label">QURAN</string>

<!-- Home Dashboard — Tasbeeh card (NEW) -->
<string name="home_tasbeeh_label">TASBEEH</string>
<string name="home_tasbeeh_count_progress">%1$d / %2$d</string>

<!-- Home Dashboard — general -->
<string name="home_daily_rituals_label">DAILY RITUALS</string>
<string name="home_settings_icon_description">Settings</string>

<!-- Settings placeholder -->
<string name="settings_placeholder_title">Settings</string>
<string name="settings_placeholder_coming_soon">Coming soon</string>
```

---

## Key Patterns to Follow

### `HomeUiEvent.Navigate` consumption (in HomeScreen)
```kotlin
ObserveAsEvents(flow = viewModel.eventFlow) { event ->
    when (event) {
        is HomeUiEvent.Navigate.ToPrayer   -> onNavigateToPrayer()
        is HomeUiEvent.Navigate.ToAthkar   -> onNavigateToAthkar()
        is HomeUiEvent.Navigate.ToQuran    -> onNavigateToQuran()
        is HomeUiEvent.Navigate.ToSettings -> onNavigateToSettings()
        is HomeUiEvent.Navigate.ToHabits   -> onNavigateToHabits()
        is HomeUiEvent.Navigate.ToTasbeeh  -> onNavigateToTasbeeh()
    }
}
```

### `AppBackHandler` (expect/actual)
```kotlin
// commonMain expect:
@Composable
expect fun AppBackHandler(onBack: () -> Unit)

// androidMain actual:
@Composable
actual fun AppBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

// iosMain actual:
@Composable
actual fun AppBackHandler(onBack: () -> Unit) {
    // no-op — iOS back handled natively by swipe gesture
}
```

### `ObserveAthkarCompletionUseCase` date parameter
The use case takes a **`String`** date, not a `LocalDate`:
```kotlin
observeAthkarCompletionUseCase(timeProvider.logicalDate().toString())
```

### `ObserveQuranStateUseCase` date parameter
The use case takes a **`LocalDate`** directly:
```kotlin
observeQuranStateUseCase(timeProvider.logicalDate())
```

### `Result` unwrapping (no `.fold()`)
`Result` is a plain sealed interface — use `when`:
```kotlin
when (result) {
    is Result.Success -> result.data
    is Result.Failure -> result.error
}
```

---

## Acceptance Verification Checklist

- [x] Home screen shows Next Prayer, Athkar status, Quran ring, Tasbeeh card, Habits section without switching tabs
- [x] Tapping Next Prayer card → Prayer tab
- [x] Tapping Athkar card → Athkar tab
- [x] Tapping Quran card → Quran tab
- [x] Tapping Tasbeeh card → TasbeehRoute (push, no bottom bar)
- [x] Habits "View All" → HabitsRoute (push, no bottom bar)
- [x] Settings gear icon in trailing slot of TopAppBar
- [x] Tapping Settings → SettingsScreen placeholder (push, no bottom bar)
- [x] Back from Settings → Home (via AppBackHandler + goHome())
- [x] Back from Habits → Home (via AppBackHandler + goHome())
- [x] Back from Tasbeeh → Home (via AppBackHandler + goHome())
- [x] No existing Habits functionality broken (add/edit/delete/toggle/increment all work)
- [x] When prayer times unavailable → card shows fallback string (not a crash)
- [x] When Athkar never opened → card shows "Tap to get started"
- [x] When Quran goal = 5, pages = 0 → ring shows 0%
- [x] Bottom bar hidden on HabitsRoute, TasbeehRoute, SettingsRoute
- [x] Bottom bar shown on HomeRoute, PrayerRoute, QuranRoute, AthkarRoute (4 tabs only)
- [x] All new strings in `strings.xml` (no hardcoded literals in composables)
- [x] No `import io.github.helmy2.mudawama.*.generated.resources.Res` patterns
- [x] `CoroutineDispatcher` constructor-injected in `HomeViewModel`
- [x] No `Dispatchers.IO` or `Dispatchers.Main` hardcoded anywhere new
