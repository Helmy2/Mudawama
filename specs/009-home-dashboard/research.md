# Research: 009 Home Dashboard

## Decision Log

---

### D-001: Next Prayer Derivation — Where and How

**Decision:** Derive "next prayer" in `HomeViewModel` by filtering `List<PrayerWithStatus>` for the first entry with `LogStatus.PENDING`, using `ObservePrayersForDateUseCase` directly (which is already in `feature:prayer:domain`).

**Rationale:** `ObservePrayersForDateUseCase` already emits a live `Flow<Result<List<PrayerWithStatus>, PrayerError>>` ordered by `PrayerName.ordinal` (Fajr→Isha). The first `PENDING` entry is definitionally the next prayer. No new use case is needed. `PrayerWithStatus` (`name: PrayerName`, `timeString: String`, `status: LogStatus`) provides everything the summary card needs.

**Alternatives considered:**
- New `GetNextPrayerUseCase` in `feature:prayer:domain` — unnecessary abstraction; the filtering logic is a single `firstOrNull { it.status == LogStatus.PENDING }` expression that belongs in the ViewModel.
- Reading from `PrayerViewModel` state — violates the "no direct ViewModel-to-ViewModel dependency" rule and would require `feature:prayer:presentation` as a dependency (forbidden).

**Note on coordinates:** `ObservePrayersForDateUseCase` requires a `Coordinates` argument. `HomeViewModel` must inject `LocationProvider` from `shared:core:domain` to resolve the device location. On failure, it falls back to Mecca coordinates (same pattern as `PrayerViewModel`).

---

### D-002: Athkar Completion Data — Which UseCase

**Decision:** Use `ObserveAthkarCompletionUseCase` from `feature:athkar:domain`, which emits `Flow<Map<AthkarGroupType, Boolean>>` for a given date string. Only `MORNING` and `EVENING` entries are surfaced on the card; `POST_PRAYER` is excluded from the summary card.

**Rationale:** `ObserveAthkarCompletionUseCase.invoke(date)` is exactly the right level of abstraction — it returns completion booleans without exposing individual item counters. The card only needs two booleans. `AthkarGroupType.POST_PRAYER` has 5 prayer slots making it unsuitable for a simple done/pending binary, so it is omitted from the summary card.

**Alternatives considered:**
- Using `ObserveAthkarLogUseCase` per group — returns `Flow<AthkarDailyLog?>` with full counter maps; higher granularity than needed.
- Reading directly from `AthkarViewModel` — forbidden (cross-ViewModel dependency, wrong module).

**"Never used Athkar" edge case:** When no DB row exists, `AthkarRepository.observeCompletionStatus` emits `false` for all group types (confirmed from `AthkarRepository` contract: "Returns a map of AthkarGroupType → isComplete (false when no log row exists)"). `HomeViewModel` maps `null` / absent entries to a dedicated `NotStarted` card state rather than `false`/incomplete, satisfying the FR-013 / spec edge case.

---

### D-003: Quran Progress Data — Which Fields

**Decision:** Use `ObserveQuranStateUseCase` from `feature:quran:domain`, which emits `Flow<QuranScreenState>`. The card consumes `pagesReadToday`, `goalPages`, and the pre-computed `progressFraction` (derived in `QuranUiState` as `(pagesReadToday / goalPages).coerceIn(0f, 1f)`). `HomeViewModel` applies the same formula.

**Rationale:** `ObserveQuranStateUseCase` combines goal + daily log + bookmark in one observable. The `progressFraction` formula is trivial and can be replicated in `HomeUiState` without duplicating business logic.

**Zero-goal edge case:** When `goalPages == 0` (impossible via `SetGoalUseCase` which rejects `< 1`, but defensive), `progressFraction` is clamped to `0f`. The card renders as 0% with "0 / 0 pages" rather than crashing.

---

### D-004: Navigation Event Pattern — HomeUiEvent.NavigateTo

**Decision:** `HomeViewModel` emits `HomeUiEvent.NavigateTo(route: Route)` (typed to `Route` sealed interface, not `Any`). `MudawamaAppShell` observes this event via `ObserveAsEvents` and calls `backStack.clear(); backStack.add(route)` — the same single-top navigation guard already used by `MudawamaBottomBar`.

**Rationale:** Using the existing sealed `Route` hierarchy (from `shared:navigation`) as the type avoids `Any` and keeps the event type-safe. The spec says `Any` but the existing routes are all `Route` subtypes, so this is strictly stronger. `shared:navigation` is an explicit direct dependency of `feature:home:presentation` — no transitive dependency verification needed.

**Alternatives considered:**
- `HomeUiEvent.NavigateTo(route: Any)` — weaker typing; rejected in favour of the sealed `Route` hierarchy.
- Passing `navController` to ViewModel — explicitly forbidden by FR-016 and the constitution.

---

### D-005: Settings Route and Placeholder Location

**Decision:** `SettingsRoute` (`@Serializable data object`) is added to `Routes.kt` in `shared:navigation`. The `SettingsScreen` placeholder composable is added to `Placeholders.kt` in the same module, following the `QuranPlaceholderScreen` / `AthkarPlaceholderScreen` pattern already there.

**Rationale:** `Placeholders.kt` already exists in `shared:navigation` for exactly this purpose. `SettingsRoute` needs to be in `Routes.kt` so `MudawamaAppShell` can register it. Adding it to an existing file avoids new files and new modules.

**SettingsRoute in serializers module:** `MudawamaAppShell` builds a `SerializersModule` with explicit `subclass(...)` entries for each route. `SettingsRoute` must be added there. The `backStack` initial state is `HomeRoute`; `SettingsRoute` is a pushed destination, not a tab, so it does NOT appear in `BottomNavItem`.

---

### D-006: HomeViewModel Location — New Module

**Decision:** A new `feature:home:presentation` module is created. `HomeViewModel.kt` and `HomeScreen.kt` live in this new module under the package `io.github.helmy2.mudawama.home.presentation`. `feature:habits:presentation` is entirely untouched — `HabitsViewModel`, `HabitsScreen`, `HabitsUiState/Action/Event`, and `HabitsPresentationModule` all remain unchanged.

**`HomeUiState` fields:**

```
nextPrayer: PrayerWithStatus?         // null = loading or all prayers done
isPrayerLoading: Boolean = true
prayerTimesAvailable: Boolean = false // false → show "unavailable" fallback on card

athkarStatus: Map<AthkarGroupType, Boolean>   // MORNING + EVENING booleans
isAthkarLoading: Boolean = true

quranPagesReadToday: Int = 0
quranGoalPages: Int = 5
isQuranLoading: Boolean = true
```

**`HomeUiAction` additions:**

```
data object NavigateToPrayer   : HomeUiAction
data object NavigateToAthkar   : HomeUiAction
data object NavigateToQuran    : HomeUiAction
data object NavigateToSettings : HomeUiAction
```

**`HomeUiEvent` additions:**

```
data class NavigateTo(val route: Route) : HomeUiEvent
```
nextPrayer: PrayerWithStatus? = null          // null = loading or all prayers done
isPrayerLoading: Boolean = true
prayerTimesAvailable: Boolean = false         // false = show "unavailable" fallback

athkarStatus: Map<AthkarGroupType, Boolean>   // MORNING + EVENING booleans
isAthkarLoading: Boolean = true

quranPagesReadToday: Int = 0
quranGoalPages: Int = 5
isQuranLoading: Boolean = true
```

**`HomeUiAction` additions:**

```
data object NavigateToPrayer   : HomeUiAction
data object NavigateToAthkar   : HomeUiAction
data object NavigateToQuran    : HomeUiAction
data object NavigateToSettings : HomeUiAction
```

**`HomeUiEvent` additions:**

```
data class NavigateTo(val route: Route) : HomeUiEvent
```

---

### D-007: UI Reference Alignment (home_dashboard.png)

From the UI reference image:
- The screen has a `TopAppBar` with the app name centred and the settings gear icon in the **trailing (actions) slot**.
- Below the top bar: a 7-day `DateStrip` (deferred — see D-009).
- "Next Prayer" is a **large full-width card** with a solid primary-colour fill, showing prayer name, countdown/time, and a progress bar.
- "Daily Rituals" section shows two **side-by-side cards** in a 2-column grid: Prayers and Quran (circular progress rings + icon + label + subtitle). Athkar is NOT shown as a progress ring in the reference grid; it appears as a habit row in the habits list ("Morning Athkar — Sunnah Habit").

**Resolution — Athkar card layout:**  
`AthkarSummaryCard` (FR-002) IS a separate card rendered in the "Daily Rituals" summary section. The Athkar habit row visible in the reference's habits list is the *habit tracking entry* (a core ritual row), which is a different concept from the summary card. The two can coexist on screen.

The summary section layout is:
1. `NextPrayerCard` — full-width, above the grid.
2. A 2-column row: **`AthkarSummaryCard`** (left) and **`QuranProgressCard`** (right).

This matches the reference's 2-column grid while satisfying FR-002 (Athkar) and FR-003 (Quran). `AthkarSummaryCard` shows Morning / Evening done/pending as text + icon chips (not a progress ring), consistent with the reference aesthetic and the spec's "done / pending per group" wording.

**DateStrip on Home:** Deferred — see D-009.

**Important note on `LogStatus`:** `PrayerWithStatus.status` is typed `LogStatus` imported from `feature:habits:domain`. There is no `PrayerStatus` type in the codebase. The `data-model.md` "Domain models consumed" table correctly lists `LogStatus (feature:habits:domain)`.

---

### D-008: Constitution Compliance Check

All requirements pass — **no violations, no exceptions.**

| Rule | Status |
|---|---|
| Domain layer: zero Android/Compose/Ktor/Room imports | ✓ — domain models from `prayer/athkar/quran` are pure Kotlin |
| Presentation → Domain ← Data dependency direction | ✓ — `feature:home:presentation` adds `implementation` deps on domain modules only; no data modules |
| Feature modules MUST NOT depend on other feature modules' **presentation** | ✓ — only domain modules are added; no presentation-to-presentation dependency |
| MVI: immutable State, Action sealed, Event sealed | ✓ — HomeUiState/Action/Event follow the pattern |
| No hardcoded strings | ✓ — all new string keys go to `shared/designsystem/src/commonMain/composeResources/values/strings.xml` |
| `Res` import from `mudawama.shared.designsystem` only | ✓ |
| No new Room entities / DAOs / repos | ✓ — presentation-layer only |
| `CoroutineDispatcher` injected | ✓ — `HomeViewModel` constructor-injects dispatcher |
| No Dispatchers.IO / Dispatchers.Main hardcoded | ✓ |
| `@Preview` import from `androidx.compose.ui.tooling.preview.Preview` | ✓ |

**No flag raised.** The cross-feature domain dependency (`feature:home:presentation` → `feature:prayer:domain`, `feature:athkar:domain`, `feature:quran:domain`, `feature:habits:domain`) is architecturally correct: Home is the aggregating dashboard and its purpose is precisely to compose data from multiple features. Depending on `:domain` modules (pure Kotlin, no Android/Room/Ktor) is not a constitution violation. The old ⚠️ exception was an artefact of incorrectly placing this code inside `feature:habits:presentation`; with its own module, no exception is needed.

---

### D-009: DateStrip on Home Screen — Deferred

**Decision:** The 7-day date strip visible in the UI reference image is **deferred from
feature 009**. The Home screen shows today's data only and requires no date navigation.
The DateStrip component will be considered for a future polish pass if the product
direction calls for week-level history on the Home screen.

**Rationale:** The spec (FR-001 through FR-016) contains no requirement for date
navigation on the Home screen. All three summary cards (Prayer, Athkar, Quran) are
today-only by design. Adding the DateStrip without a spec requirement would be
undocumented scope creep. The component already exists in `feature:quran:presentation`
and `feature:prayer:presentation` and can be reused trivially if required later.

**Alternatives considered:**
- Adding `FR-017` to the spec — rejected because no stakeholder requirement exists
  for date navigation on the Home screen in this iteration.

---

### D-010: Tasbeeh Summary Card Added to Home Dashboard

**Decision:** A fourth summary card (`TasbeehSummaryCard`) is added to the Home Dashboard alongside `QuranProgressCard` in the 2-column row. It displays the Tasbeeh daily total vs. goal with a circular progress ring. Tapping it pushes `TasbeehRoute` (no bottom bar).

**Rationale:** The Home Dashboard aggregates all core features. Tasbeeh is a completed feature with daily goals and totals — surfacing it on the Home screen is consistent with the product's promise of a single at-a-glance view. The data is already available via `ObserveTasbeehGoalUseCase` + `ObserveTasbeehDailyTotalUseCase` from `feature:athkar:domain`, requiring only two additional injections into `HomeViewModel`.

**Layout change:** `AthkarSummaryCard` is promoted to full-width (to accommodate Morning/Evening row content legibly). The 2-column row below it contains `QuranProgressCard` (left) and `TasbeehSummaryCard` (right).

**Alternatives considered:**
- Keeping Tasbeeh accessible only from the Tasbeeh tab — rejected because the tab was removed from the bottom bar in 009.
- Adding Tasbeeh to a third column — rejected; 3 columns is too narrow on standard phone screens.

---

### D-011: Navigation via Plain Callbacks — No `shared:navigation` Dep in Home Module

**Decision:** `feature:home:presentation` has **no dependency on `shared:navigation`**. Navigation is done via six plain `() -> Unit` callbacks (`onNavigateToPrayer`, `onNavigateToAthkar`, `onNavigateToQuran`, `onNavigateToSettings`, `onNavigateToHabits`, `onNavigateToTasbeeh`) passed from `MudawamaAppShell`. `HomeUiEvent` uses a nested `sealed interface Navigate` with typed objects (`ToPrayer`, `ToAthkar`, `ToQuran`, `ToSettings`, `ToHabits`, `ToTasbeeh`) — no `Route` types appear in the home module.

**Rationale:** Adding `shared:navigation` as a dependency would pull the routing graph into the home module, creating a circular concern (the shell wires home, home imports from shell). Using plain callbacks keeps the module self-contained, independently compilable, and testable. This also satisfies the principle that feature modules should never depend on the navigation infrastructure.

**Alternatives considered:**
- `HomeUiEvent.NavigateTo(route: Route)` — requires `shared:navigation` dep; rejected.
- `HomeUiEvent.NavigateTo(route: Any)` — weak typing and still requires navigation dep; rejected.

---

### D-012: `AppBackHandler` Expect/Actual + `goHome()` Pattern

**Decision:** An `AppBackHandler` expect/actual composable is added to `shared:navigation`. The Android actual wraps `androidx.activity.compose.BackHandler`; the iOS actual is a no-op (iOS back is handled natively by swipe gesture). Every non-Home `entryProvider` branch in `MudawamaAppShell` wraps its content in `AppBackHandler { goHome() }`. `goHome()` clears the back-stack and adds `HomeRoute`.

**Rationale:** Without `AppBackHandler`, pressing the Android system back button from a push destination (Habits, Tasbeeh, Settings) would exit the app or behave unexpectedly. The `goHome()` pattern ensures consistent back behaviour: all push destinations return to Home. The iOS no-op is correct because iOS swipe-back is handled at the native UINavigationController level.

**Source set additions:** `shared/navigation` added `androidMain/` and `iosMain/` source sets for the `AppBackHandler` actual implementations. Previously the module was 100% `commonMain`.

**Alternatives considered:**
- Per-destination `onBack` lambdas — rejected; inconsistent and more boilerplate.
- Platform-specific back handling in `androidApp/` — rejected; breaks the KMP abstraction.

