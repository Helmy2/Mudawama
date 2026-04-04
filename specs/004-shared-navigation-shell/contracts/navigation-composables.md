# Public API Contract: shared:navigation Composables

**Module**: `:shared:navigation`
**Package**: `io.github.helmy2.mudawama.navigation`
**Feature**: `004-shared-navigation-shell`
**Date**: 2026-04-04 (revised)
**Type**: KMP Compose Multiplatform module — composable function signatures & behaviour contracts

---

## Overview

The `:shared:navigation` module exposes **two public composable entry points**
(`MudawamaAppShell`, `MudawamaBottomBar`), **four public placeholder screens**, and **six public
types** (`Route` sealed interface, `HomeRoute`, `PrayerRoute`, `AthkarRoute`, `HabitsRoute`,
`BottomNavItem`). Everything else is `internal`.

Navigation is powered by **Navigation 3** (`org.jetbrains.androidx.navigation3:navigation3-ui`).
There is no `NavController`, no `NavHost`, no `currentBackStackEntryAsState`, no
`NavBackStackEntry`, and no `hasRoute()` anywhere in this module.

---

## 1. `MudawamaAppShell`

**File**: `MudawamaAppShell.kt`

```kotlin
/**
 * Root entry-point composable for the Mudawama application.
 *
 * This is the **only** composable that platform host files should call:
 * - Android [MainActivity.setContent { MudawamaAppShell() }]
 * - iOS [ComposeUIViewController { MudawamaAppShell() }]
 *
 * Responsibilities:
 * - Wraps all content in [MudawamaTheme] with `darkTheme = isSystemInDarkTheme()` (FR-002).
 * - Creates the Navigation 3 backstack via [rememberNavBackStack] with
 *   [SavedStateConfiguration] for process-death recovery, starting at [HomeRoute].
 * - Passes `backStack.lastOrNull()` as [currentRoute] to [MudawamaBottomBar] —
 *   no separate remembered tab-index variable (FR-008).
 * - Renders a [Scaffold] with [MudawamaBottomBar] in the `bottomBar` slot.
 * - Inside the content slot: renders a [NavDisplay] with an exhaustive `when(route)`
 *   covering all four [Route] subtypes (FR-005).
 *
 * Satisfies: FR-001, FR-002, FR-003, FR-004, FR-005, FR-008.
 */
@Composable
fun MudawamaAppShell()

// Companion preview — renders inside MudawamaTheme to match production appearance:
@Preview
@Composable
fun MudawamaAppShellPreview()
```

**Behaviour contract**:
- No parameters. The shell bootstraps itself.
- `darkTheme` is always derived from `isSystemInDarkTheme()` — never hardcoded.
- Backstack initial entry is always `HomeRoute` (US-1 scenario 2 — Home selected by default).
- Bottom bar receives `backStack.lastOrNull(): NavKey?`. No local
  `remember { mutableStateOf<Int>(...) }` exists anywhere in the file.
- `Scaffold` inner padding is forwarded to the `NavDisplay` `modifier` parameter so content is
  not obscured by the bottom bar.
- Tab-switch navigation: `if (backStack.lastOrNull() != route) { backStack.clear(); backStack.add(route) }`.
  Tapping the current tab is a no-op — backstack depth does not increase (FR-012, SC-007).

**Breaking-change policy**: Changing the signature of `MudawamaAppShell` requires coordinated
updates to `androidApp/MainActivity.kt` and `iosApp/ContentView.swift`. Announce in the PR
description.

---

## 2. `MudawamaBottomBar`

**File**: `MudawamaBottomBar.kt`

```kotlin
/**
 * Floating glassmorphism bottom navigation bar with four tab items.
 *
 * This composable is **stateless** — it owns zero `remember { mutableStateOf(...) }` calls.
 * The selected tab is derived purely from [currentRoute] via direct object equality (FR-008).
 * There is no [NavBackStackEntry], no [hasRoute], and no NavController reference here.
 *
 * Visual specification (DESIGN.md §2 "Floating Navigation"):
 * - Background: [MudawamaTheme.colors.surface] at 80 % opacity (FR-009, SC-004).
 * - Blur: 20 dp radius applied to the background layer via [Modifier.blur] (FR-009).
 * - Shape: [RoundedCornerShape] with 28 dp radius; bar does not span edge-to-edge (FR-010).
 * - Margin: ≥ 16 dp horizontal padding from screen edges (SC-003).
 * - Insets: [WindowInsets.navigationBars] bottom padding respected (FR-011).
 *
 * @param currentRoute The current top-of-stack key from [backStack.lastOrNull()].
 *   Pass `null` when the backstack is empty; no tab will render as selected (graceful fallback).
 * @param onNavigate Invoked with the [Route] object ([HomeRoute], [PrayerRoute], etc.) when a
 *   tab is tapped. The caller performs the actual backstack mutation with the single-top guard.
 * @param modifier Optional [Modifier] for the outermost container.
 *
 * Satisfies: FR-007, FR-008, FR-009, FR-010, FR-011, FR-012, FR-014, FR-015.
 */
@Composable
fun MudawamaBottomBar(
    currentRoute: NavKey?,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
)

// Companion preview:
@Preview
@Composable
fun MudawamaBottomBarPreview()
```

**Behaviour contract**:
- Renders exactly **four** `NavigationBarItem` composables, one per `BottomNavItem.entries`
  value, in enum declaration order (HOME, PRAYER, ATHKAR, HABITS).
- `selected` state for each item: `item.route == currentRoute` (direct equality). If
  `currentRoute` is `null`, all items render as unselected.
- Each item renders its `icon` (from `BottomNavItem.icon`) and `label` text (from string resource
  lookup using `BottomNavItem.labelKey`). No hardcoded label strings.
- Tapping any item invokes `onNavigate(item.route)`. The caller is expected to apply the
  single-top guard — the bar itself does not call `backStack.clear()` directly.
- All color tokens sourced from `MudawamaTheme.colors` (FR-014). `NavigationBar` container
  color: `Color.Transparent` (background provided by `GlassmorphismSurface`).
- The bar is wrapped in `GlassmorphismSurface` (internal) which applies the layered opacity +
  blur + floating shape + inset padding.

**Non-contract (intentional omissions)**:
- No animation for tab transitions — the Compose runtime handles implicit recomposition.
- No badge support in this version.
- No scroll-to-hide behavior.

---

## 3. Placeholder Screens

**File**: `Placeholders.kt`

```kotlin
/**
 * Placeholder screen for the Home destination.
 *
 * Displays the screen name as a centred text label using
 * [MudawamaTheme.typography] and [MudawamaTheme.colors.onSurface].
 * Composes synchronously with zero I/O, zero ViewModels, zero coroutines (FR-006).
 *
 * Satisfies: FR-006.
 */
@Composable fun HomePlaceholderScreen()
@Preview @Composable fun HomePlaceholderScreenPreview()

// Identical pattern for the remaining three screens:
@Composable fun PrayerPlaceholderScreen()
@Preview @Composable fun PrayerPlaceholderScreenPreview()

@Composable fun AthkarPlaceholderScreen()
@Preview @Composable fun AthkarPlaceholderScreenPreview()

@Composable fun HabitsPlaceholderScreen()
@Preview @Composable fun HabitsPlaceholderScreenPreview()
```

**Behaviour contract**:
- Renders a `Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize())`
  containing a single `Text`.
- Text value: the screen name (e.g., `"Home"`, `"Prayer"`). Retrieved from a string resource —
  not hardcoded (Constitution §Code Quality).
- Typography: `MudawamaTheme.typography.titleLarge` (or equivalent token).
- Color: `MudawamaTheme.colors.onSurface`.
- No state, no effects, no ViewModels. Composes in O(1).
- Each placeholder **must** continue to compile and render correctly after real screens replace
  them in future iterations; the function signatures are stable.

---

## 4. Route Types

**File**: `Routes.kt`

```kotlin
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey

@Serializable data object HomeRoute   : Route
@Serializable data object PrayerRoute : Route
@Serializable data object AthkarRoute : Route
@Serializable data object HabitsRoute : Route
```

**Contract**:
- `Route` is a `@Serializable sealed interface` extending `NavKey`. The sealed hierarchy makes
  `when(route)` in `NavDisplay` exhaustive — the compiler enforces coverage of all destinations.
- Concrete routes are immutable Kotlin singletons (`data object`).
- No fields, no constructor parameters.
- Equality is singleton-identity equality (standard for `data object`). Tab selection relies on
  this: `item.route == currentRoute` uses standard `==`.
- All four types **must** be registered as polymorphic subclasses of `NavKey` in the
  `SerializersModule` passed to `SavedStateConfiguration` inside `MudawamaAppShell`.
- Must remain in the `io.github.helmy2.mudawama.navigation` package. Deep-link schemes or
  external references that encode these route types will break if the package changes.
- Not intended to be subclassed outside this module.

---

## 5. `BottomNavItem` Enum

**File**: `Routes.kt`

```kotlin
enum class BottomNavItem(
    val route: Route,          // typed as Route (sealed interface) — compile-time safe
    val icon: ImageVector,
    val labelKey: String,
)
```

**Contract**:
- `entries` is ordered: HOME, PRAYER, ATHKAR, HABITS. Reordering changes the visual tab order
  in the bar.
- `route: Route` (not `Any`) — adding a new `Route` subclass without updating this enum produces
  a compile error (intentional coupling for the tab menu).
- Public for testability (allows consumers to iterate entries in tests without relying on
  `MudawamaBottomBar` internals).
- `icon` and `labelKey` are temporary placeholders. They will be updated in a follow-up without
  a breaking-change: no external code should depend on the specific `ImageVector` values or
  `labelKey` strings.

---

## 6. Internal Symbols (not part of public API)

| Symbol | File | Visibility | Purpose |
|--------|------|------------|---------|
| `GlassmorphismSurface` | `MudawamaBottomBar.kt` | `internal` | Layered Box composable implementing 80 % opacity + 20 dp blur + rounded shape + inset padding |

`GlassmorphismSurface` is `internal` because its implementation may change (e.g.,
platform-specific blur enhancement) without affecting public consumers.

---

## 7. `build.gradle.kts` Contract

The module's build file must satisfy:

```kotlin
plugins {
    id("mudawama.kmp.presentation")          // brings Compose, Material3, lifecycle, Koin, ui-tooling-preview
    alias(libs.plugins.kotlinxSerialization) // required for @Serializable route objects (FR-003)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.navigation"
    }
    configureIosFramework("MudawamaNavigation", isStatic = true)

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.designsystem)              // transitive theme tokens for consumers
            implementation(libs.navigation3.ui)            // NavKey, NavDisplay, rememberNavBackStack
            implementation(libs.kotlinx.serialization.json) // explicit, auditable serialization dep
        }
        // No androidMain or iosMain — FR-013
    }
}
```

**Invariants**:
- `androidMain` and `iosMain` source sets MUST NOT be created (FR-013).
- `api(projects.shared.designsystem)` — not `implementation` — because consumers of
  `:shared:navigation` must be able to access `MudawamaTheme` tokens without an additional
  explicit dependency.
- The `kotlinxSerialization` plugin must be applied at the module level, not inherited, to ensure
  the code-generation step runs for this module's route hierarchy.
- `libs.navigation3.ui` resolves to the catalog entry `navigation3-ui` with version
  `navigation3 = "1.0.0-alpha06"`.

---

## 8. `libs.versions.toml` Contract

The following entries must be added before implementation begins:

```toml
[versions]
navigation3 = "1.0.0-alpha06"

[libraries]
navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
```

**No other version catalog changes required** — `kotlinxSerialization` plugin and
`kotlinx-serialization-json` library are already present.

**Removed** (compared to any prior Nav 2.x plan):
- `navigation = "2.9.0"` — not used; Navigation 3 supersedes it.
- `navigation-compose` library entry — replaced by `navigation3-ui`.
