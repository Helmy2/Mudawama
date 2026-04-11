# UI Contracts: 009 Home Dashboard

This feature exposes no external API, CLI, or network surface. Its "contracts" are the internal
UI component signatures and the ViewModel MVI interface — the boundaries between the composable
layer and the ViewModel, and between the home screen and the navigation shell.

---

## Contract 1: HomeViewModel MVI Interface

The public surface that `HomeScreen` binds to.

```
State: HomeUiState
  (see data-model.md for full field list)

Actions (user intent → ViewModel):
  // Habit interactions (toggle/count only; add/edit/delete managed in Habits tab)
  ToggleCompletion(habitId: String)
  IncrementCount(habitId: String)
  DecrementCount(habitId: String)

  // Navigation actions
  PrayerCardTapped
  AthkarCardTapped
  QuranCardTapped
  SettingsIconTapped
  HabitsViewAllTapped

Events (ViewModel → UI, one-shot):
  ShowSnackbar(message: StringResource)   // reserved for future use
  NavigateTo(route: Route)                // consumed by MudawamaAppShell
```

**Invariants:**
- `NavigateTo` is only emitted; `HomeScreen` itself does not consume it. It is forwarded upstream to `MudawamaAppShell`.
- The ViewModel MUST NOT hold a reference to `NavBackStack` or any navigation controller.

---

## Contract 2: `HomeScreen` Composable Signature

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigate: (Route) -> Unit,   // injected by MudawamaAppShell to handle NavigateTo events
)
```

**Why `onNavigate` parameter?**  
`HomeScreen` observes `HomeUiEvent.NavigateTo(route)` via `ObserveAsEvents` and calls `onNavigate(route)`. This keeps `NavBackStack` out of the composable body and lets `MudawamaAppShell` own back-stack manipulation.

---

## Contract 3: `MudawamaAppShell` Extension

`MudawamaAppShell` has been updated to:

1. Add `SettingsRoute` to `SerializersModule.polymorphic(NavKey::class)`.
2. Add `entry<SettingsRoute> { settingsScreen { backStack.removeLastOrNull() } }` in `entryProvider`.
3. Replace `habitsScreen: @Composable () -> Unit` with `homeScreen: @Composable (onNavigate: (Route) -> Unit) -> Unit = { _ -> }` — **`habitsScreen` is removed entirely**.
4. Accept a new `settingsScreen: @Composable (onBack: () -> Unit) -> Unit = { _ -> }` parameter (takes `onBack` lambda — NOT `@Composable () -> Unit`).
5. `entry<HomeRoute>` calls `homeScreen(onNavigate)` where `onNavigate` distinguishes tab-swap vs push.

**Tab routes** (clear + add for single-top): `PrayerRoute`, `QuranRoute`, `AthkarRoute`  
**Push routes** (add only): `SettingsRoute`

---

## Contract 4: Summary Card Composable Signatures

Three new stateless composable components, all in `feature/home/presentation`:

```kotlin
// Next Prayer summary card — feature/home/presentation/src/commonMain/.../home/presentation/components/NextPrayerCard.kt
@Composable
fun NextPrayerCard(
    prayer: PrayerWithStatus?,          // null = loading
    isLoading: Boolean,
    timesAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)

// Athkar daily status card — feature/home/presentation/src/commonMain/.../home/presentation/components/AthkarSummaryCard.kt
@Composable
fun AthkarSummaryCard(
    athkarStatus: Map<AthkarGroupType, Boolean>,  // MORNING + EVENING only
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)

// Quran progress ring card — feature/home/presentation/src/commonMain/.../home/presentation/components/QuranProgressCard.kt
@Composable
fun QuranProgressCard(
    pagesReadToday: Int,
    goalPages: Int,
    progressFraction: Float,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

**Common rendering rules for all cards:**
- When `isLoading == true`: render skeleton placeholder (shimmer or low-alpha box).
- Use `MudawamaSurfaceCard` as the outer container.
- Must be tappable (`onClick`) with no nested tappable children that interfere.
- Content descriptions for all interactive elements must be declared via `stringResource`.

---

## Contract 5: `SettingsScreen` Placeholder Signature

```kotlin
// In shared/navigation/src/commonMain/.../navigation/Placeholders.kt
@Composable
fun SettingsScreen(onBack: () -> Unit = {})
```

Renders a minimal screen with:
- A `TopAppBar` with back navigation `IconButton` calling `onBack()` (leading slot) and the title from `settings_placeholder_title`.
- A centred body showing `settings_placeholder_coming_soon`.
- No bottom bar (Settings is a pushed destination, not a tab — bottom bar stays anchored to tab roots in `MudawamaAppShell`).
- **DO NOT use `LocalOnBackPressedDispatcherOwner`** — it is Android-only and will not compile in `commonMain`.
