# Data Model: shared:navigation — Routes & UI Entities

**Feature**: `004-shared-navigation-shell`
**Phase**: 1 — Design
**Date**: 2026-04-04 (revised)

---

## 1. Route Hierarchy

Routes are a `@Serializable` sealed interface that extends `NavKey` (the Navigation 3 route
marker). Concrete routes are `@Serializable data object` declarations. They carry **no mutable
state** and serve only as typed identifiers for navigation destinations.

The sealed interface hierarchy gives two benefits:
- `when(route)` branches in `NavDisplay` are **exhaustive** — the compiler enforces that every
  destination is handled; no `else` branch is needed.
- Polymorphic serialization via `PolymorphicSerializer(NavKey::class)` can enumerate all
  subclasses for the `SavedStateConfiguration` used by `rememberNavBackStack`.

```kotlin
// File: Routes.kt
package io.github.helmy2.mudawama.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ── Sealed interface — the contract every top-level route must satisfy ────────

@Serializable
sealed interface Route : NavKey

// ── Four top-level route objects ─────────────────────────────────────────────

@Serializable data object HomeRoute   : Route
@Serializable data object PrayerRoute : Route
@Serializable data object AthkarRoute : Route
@Serializable data object HabitsRoute : Route
```

**Validation rules**:
- Route objects MUST NOT contain mutable, non-serializable, or platform-specific fields.
- Route names MUST exactly match the four top-level navigation destinations (FR-004).
- Routes carry no domain logic, no constructor parameters, and no default values that could
  affect serialization.
- All route types MUST be registered as polymorphic subclasses of `NavKey` in the
  `SerializersModule` passed to `SavedStateConfiguration` (required for process-death recovery).

**State transitions**: None — routes are immutable singletons used only as navigation keys.

**Serialization**: Each `data object` serializes to `{}` (empty JSON object) tagged with its
class discriminator. Navigation 3's `SavedStateConfiguration` uses the list of serialized route
objects to restore the backstack after process death.

---

## 2. `BottomNavItem` Enum

An ordered, enum-driven list binding each route to its display metadata. `MudawamaBottomBar`
iterates over `BottomNavItem.entries` to render the four tab items. The `route` field type is
now `Route` (not `Any`) — this makes the enum a compile-time verified mapping from visual tab
to type-safe navigation key.

```kotlin
// File: Routes.kt (same file as route objects — one import location for consumers)
package io.github.helmy2.mudawama.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: Route,           // typed as Route (not Any) — compile-time verified (FR-004)
    val icon: ImageVector,      // temporary Material3 placeholder; replace with Res.drawable.* icons
    val labelKey: String,       // string-resource key; implementation uses stringResource(Res.string.*)
) {
    HOME   (HomeRoute,    Icons.Default.Home,          "tab_home"),
    PRAYER (PrayerRoute,  Icons.Default.Star,           "tab_prayer"),
    ATHKAR (AthkarRoute,  Icons.Default.FavoriteBorder, "tab_athkar"),
    HABITS (HabitsRoute,  Icons.Default.CheckCircle,    "tab_habits"),
}
```

**Design notes**:
- `route: Route` (sealed interface) means the compiler will error if a new `Route` subclass is
  added without updating `BottomNavItem` — intentional coupling for the tab menu.
- `labelKey` indicates the string resource key to look up via `stringResource(Res.string.tab_home)`.
  Final implementation must use a CMP resource accessor — no hardcoded user-facing strings.
- `icon` uses `Icons.Default.*` temporarily (spec Assumptions). Replace with resource-backed
  `DrawableResource` once `shared:designsystem` ships icon assets.
- Enum order determines left-to-right visual order of tabs in `MudawamaBottomBar`.

---

## 3. Composable State Flow (Navigation 3)

```
MudawamaAppShell
│
├── MudawamaTheme(darkTheme = isSystemInDarkTheme())              [FR-002]
│
└── [inside theme]
    ├── rememberNavBackStack(
    │       SavedStateConfiguration(
    │           serializer = ListSerializer(PolymorphicSerializer(NavKey::class)),
    │           serializersModule = SerializersModule {
    │               polymorphic(NavKey::class) {
    │                   subclass(HomeRoute::class)
    │                   subclass(PrayerRoute::class)
    │                   subclass(AthkarRoute::class)
    │                   subclass(HabitsRoute::class)
    │               }
    │           }
    │       ),
    │       HomeRoute                   ← initial entry (US-1 scenario 2)
    │   )
    │   → backStack: SnapshotStateList<NavKey>   [Compose-observable mutable list]
    │
    └── Scaffold(
          bottomBar = {
              MudawamaBottomBar(
                  currentRoute = backStack.lastOrNull(),  [NavKey? — no separate remembered var]
                  onNavigate = { route ->                              [FR-008]
                      if (backStack.lastOrNull() != route) {          [single-top guard, FR-012]
                          backStack.clear()
                          backStack.add(route)
                      }
                  }
              )
              │
              └── selectedItem: BottomNavItem?
                    = BottomNavItem.entries.find { it.route == currentRoute }
                    // Direct equality — no hasRoute(), no reflection   [FR-008]
          }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier  = Modifier.padding(innerPadding),
            ) { route ->
                when (route) {                  // exhaustive — sealed interface guarantees it
                    HomeRoute   -> HomePlaceholderScreen()    [FR-005, FR-006]
                    PrayerRoute -> PrayerPlaceholderScreen()
                    AthkarRoute -> AthkarPlaceholderScreen()
                    HabitsRoute -> HabitsPlaceholderScreen()
                }
            }
        }
```

**Key invariants**:
1. `selectedItem` is always derived by direct equality from `backStack.lastOrNull()`, never
   stored. Changing the selected tab requires mutating the actual backstack.
2. `MudawamaBottomBar` is stateless — all inputs are parameters. No side effects inside it.
3. `onNavigate` is the **only** mutation point. It guards against duplicate entries with a single
   `if` check (FR-012, SC-007) — no `NavOptions` builder needed.
4. `NavDisplay` renders the content for the current top-of-stack key. Unlike `NavHost`, there is
   no separate `NavController` or `currentBackStackEntry` type — the backstack IS the source of
   truth.

---

## 4. `GlassmorphismSurface` (internal helper)

Not a public entity. Documented here for completeness of the module's internal design.

```
GlassmorphismSurface
│
├── outerBox  Modifier.padding(horizontal=16.dp, vertical=8.dp)
│             .windowInsetsPadding(WindowInsets.navigationBars)   [FR-011]
│             .fillMaxWidth()
│
├── backgroundBox  Modifier.matchParentSize()
│                  .clip(RoundedCornerShape(28.dp))               [FR-010 — rounded floating shape]
│                  .background(surface.copy(alpha=0.80f))         [FR-009 — 80% opacity]
│                  .blur(radius=20.dp)                            [FR-009 — blur effect]
│
└── contentSlot  (NavigationBar placed here, above blur layer)
                 ContainerColor = Color.Transparent               [lets backgroundBox show through]
```

---

## 5. Source File → Responsibility Mapping

| File | Contents | Public symbols |
|------|----------|----------------|
| `Routes.kt` | `Route` sealed interface, `HomeRoute`, `PrayerRoute`, `AthkarRoute`, `HabitsRoute`, `BottomNavItem` | All six |
| `Placeholders.kt` | `HomePlaceholderScreen`, `PrayerPlaceholderScreen`, `AthkarPlaceholderScreen`, `HabitsPlaceholderScreen` + `@Preview` companions | All four screen composables |
| `MudawamaBottomBar.kt` | `GlassmorphismSurface` (internal), `MudawamaBottomBar` + `@Preview` | `MudawamaBottomBar` only |
| `MudawamaAppShell.kt` | `MudawamaAppShell` + `@Preview` | `MudawamaAppShell` only |

**Total public surface**: 2 composable entry points + 4 placeholder composables + 6 type
declarations = 12 public symbols across 4 files. Satisfies SC-006 (≤ 10 source files).

---

## 6. Dependency Graph

```
:shared:navigation
    api  ──→  :shared:designsystem          (MudawamaTheme, MudawamaColors, MudawamaTypography)
    impl ──→  navigation3-ui:1.0.0-alpha06  (NavKey, NavDisplay, rememberNavBackStack,
              (org.jetbrains.androidx)        SavedStateConfiguration)
    impl ──→  kotlinx-serialization-json    (transitive; @Serializable + PolymorphicSerializer)
    impl ──→  compose-material3             (transitive via mudawama.kmp.presentation)
    impl ──→  ui-tooling-preview            (transitive via mudawama.kmp.presentation — @Preview)
```

**Why `api` for designsystem?**: `MudawamaAppShell` exposes `MudawamaTheme` implicitly (its
content slot composes inside the theme). Platform shells consuming `:shared:navigation` need the
theme tokens on their compile classpath. Using `api` avoids requiring every consumer to manually
declare `:shared:designsystem`.
