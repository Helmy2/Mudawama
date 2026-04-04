# Implementation Plan: shared:navigation — App Shell, Bottom Navigation Bar & Routing Graph

**Branch**: `004-shared-navigation-shell` | **Date**: 2026-04-04 (revised) | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-shared-navigation-shell/spec.md`

---

## Summary

Add `shared:navigation` — a 100 % `commonMain` Kotlin Multiplatform module that provides the
root `MudawamaAppShell` composable, a type-safe routing graph using **Navigation 3** (`NavKey`
sealed interface routes + `rememberNavBackStack` + `NavDisplay`), four placeholder screens, and a
floating glassmorphism `MudawamaBottomBar` that derives its active-tab state **directly from
`backStack.lastOrNull()`** via plain equality comparison — no `NavBackStackEntry`, no `hasRoute()`,
no separate remembered index variable is ever created. The bottom bar renders at 80 % opacity
(`surface.copy(alpha = 0.80f)`) with a 20 dp blur (`Modifier.blur`) per DESIGN.md §2 "Floating
Navigation".

---

## Technical Context

**Language/Version**: Kotlin 2.3.20 (KMP — `commonMain` only)
**Primary Dependencies**: `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha06`
(JetBrains KMP port of Navigation 3; `NavKey`, `NavDisplay`, `rememberNavBackStack`,
`SavedStateConfiguration`); `kotlinx.serialization` Gradle plugin (already in version catalog as
`kotlinxSerialization`); `shared:designsystem` (colors, typography, shapes)
**Storage**: N/A — no persistence in this module
**Testing**: Visual verification via `@Preview` composables per FR-015; Compose UI tests for
backstack assertions are a future iteration
**Target Platform**: Android (minSdk 30) + iOS (arm64, x64, simulatorArm64) — shared `commonMain`
**Project Type**: KMP shared UI module (`mudawama.kmp.presentation` convention plugin)
**Performance Goals**: All placeholder screens compose synchronously; bottom bar re-composition
triggered only on `SnapshotStateList` change (backstack mutation)
**Constraints**: Zero `androidMain`/`iosMain` source sets (FR-013); no hardcoded colors/dimensions
(FR-014); ≤ 10 source files total (SC-006)
**Scale/Scope**: 4 source files + 1 `build.gradle.kts` in `shared/navigation/`; 0 test files in
this iteration (visual verification only)

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| # | Rule | Status | Notes |
|---|------|--------|-------|
| 1 | Presentation layer — Compose MPP only; no XML, no Context, no UIKit | ✅ PASS | 100 % `commonMain`; only Compose Multiplatform APIs (FR-013) |
| 2 | Dependency direction — no feature → feature imports | ✅ PASS | `shared:navigation` depends only on `shared:designsystem`; it becomes a dependency of platform shells |
| 3 | DI uses Koin only | ✅ N/A | No DI in this module; the shell is pure composable — no ViewModels, no Koin |
| 4 | No `Dispatchers.IO` / `Dispatchers.Main` hardcoded | ✅ PASS | No coroutines; all state managed by Compose runtime via `SnapshotStateList` |
| 5 | No SQLDelight, no Retrofit | ✅ PASS | No data layer |
| 6 | No hardcoded colors or dimension literals | ✅ PASS | All tokens from `MudawamaTheme` (FR-014); spacing constants expressed as `dp` local vals |
| 7 | UI components MUST provide `@Preview` wrappers | ✅ PASS | FR-015 mandates companion `@Preview` function for every public composable |
| 8 | Code style — idiomatic Kotlin 2.x, composition over inheritance | ✅ PASS | `sealed interface` + `data object` routes, enum-driven tab items, stateless composables with hoisted state |

**Post-design re-check** (after Phase 1): All checks remain green. The `kotlinx.serialization`
plugin application is an explicit spec requirement (FR-003, Assumptions); it does not conflict
with any constitution rule. Navigation 3's alpha status is acceptable per the spec Assumptions
("Navigation 3 … is available or will be added to the version catalog as part of implementing
this spec").

---

## Project Structure

### Documentation (this feature)

```text
specs/004-shared-navigation-shell/
├── plan.md              # This file (/speckit.plan output)
├── research.md          # Phase 0 output (/speckit.plan)
├── data-model.md        # Phase 1 output (/speckit.plan)
├── quickstart.md        # Phase 1 output (/speckit.plan)
├── contracts/
│   └── navigation-composables.md   # Phase 1 output — public composable API contract
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
shared/navigation/
├── build.gradle.kts
└── src/
    └── commonMain/
        └── kotlin/io/github/helmy2/mudawama/navigation/
            ├── Routes.kt               # Route sealed interface + @Serializable data objects + BottomNavItem enum
            ├── Placeholders.kt         # Four placeholder screen composables + @Preview functions
            ├── MudawamaBottomBar.kt    # Glassmorphism floating bar + GlassmorphismSurface + @Preview
            └── MudawamaAppShell.kt     # Root shell: MudawamaTheme + rememberNavBackStack + NavDisplay + @Preview
```

**Also required (Gradle wiring)**:

```text
settings.gradle.kts                    # include(":shared:navigation")
gradle/libs.versions.toml             # Add navigation3 = "1.0.0-alpha06" + navigation3-ui library entry
shared/umbrella-ui/build.gradle.kts   # api(projects.shared.navigation)  [optional — out of scope per spec Assumptions]
```

**Structure Decision**: Single flat KMP library module under `shared/` following the exact pattern
of `shared:designsystem`. All production code resides in `commonMain` (FR-013). Module registered
as `:shared:navigation` in `settings.gradle.kts`. Source file count = 4 (well under SC-006
limit of 10).

---

## Phase 0: Research Findings (resolved)

All NEEDS CLARIFICATION items resolved in `research.md`. Summary of decisions:

| Item | Decision |
|------|----------|
| Navigation library + version | `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha06` (JetBrains KMP port of Navigation 3) |
| Backstack primitive | `rememberNavBackStack(SavedStateConfiguration(...), HomeRoute)` → `SnapshotStateList<NavKey>` |
| Backstack observation (FR-008) | `backStack.lastOrNull()` → passed as `currentRoute: NavKey?` to `MudawamaBottomBar` |
| Tab derivation | `BottomNavItem.entries.find { it.route == currentRoute }` (direct equality, no `hasRoute()`) |
| Single-top semantics (FR-012) | `if (backStack.lastOrNull() != route) { backStack.clear(); backStack.add(route) }` |
| Routing display | `NavDisplay(backStack) { route -> when(route) { ... } }` |
| Process-death state save | `SavedStateConfiguration` with `ListSerializer(PolymorphicSerializer(NavKey::class))` + `SerializersModule` |
| Glassmorphism (FR-009) | `graphicsLayer { alpha = 0.80f }` + `Modifier.blur(radius = 20.dp)` on background layer |
| Floating shape (FR-010) | `Modifier.padding(horizontal = 16.dp)` + `clip(RoundedCornerShape(28.dp))` |
| Bottom insets (FR-011) | `Modifier.windowInsetsPadding(WindowInsets.navigationBars)` |
| Icons | `Icons.Default.*` (temporary; replace with designsystem resources) |

---

## Phase 1: Design Decisions

### 1. Gradle Module Setup

**Convention plugin**: `mudawama.kmp.presentation` — identical to `shared:designsystem`. This
brings in Compose Multiplatform, Compose Compiler, Material3, ui-tooling-preview, lifecycle, and
Koin without manual repetition.

**Extra plugin needed**: `alias(libs.plugins.kotlinxSerialization)` — required to process
`@Serializable` annotations on the `Route` sealed interface and its concrete subtypes. The plugin
is already declared in `libs.versions.toml` as `kotlinxSerialization`; it just needs to be
applied to this module.

**Extra dependency**: `implementation(libs.navigation3.ui)` — the Navigation 3 KMP library (new
entry in version catalog: `navigation3-ui` under version `navigation3 = "1.0.0-alpha06"`).

**`kotlinx-serialization-json`**: Polymorphic serialization via `PolymorphicSerializer` and
`SerializersModule` requires the `kotlinx-serialization-core` runtime (already transitive via the
serialization plugin). Explicitly adding `libs.kotlinx.serialization.json` in `commonMain` is
optional but recommended for auditability.

### 2. File Responsibilities

#### `Routes.kt`
Contains:
- `Route` — `@Serializable sealed interface : NavKey`. The sealed hierarchy makes `when(route)`
  in `NavDisplay` exhaustive at compile time.
- Four `@Serializable data object` declarations implementing `Route`.
- `BottomNavItem` enum binding each `Route` to its `ImageVector` icon and label string-resource
  key. `route` field is typed `Route` (not `Any`) for compile-time safety.

One file for all route-related types keeps imports minimal in consuming files.

#### `Placeholders.kt`
Contains all four placeholder composables. Each is a `Box` with a centred `Text` reading the
screen name from `MudawamaTheme.typography.titleLarge` style and `MudawamaTheme.colors.onSurface`
colour (FR-006, FR-014). Each has a `@Preview` companion.

#### `MudawamaBottomBar.kt`
Contains:
- `GlassmorphismSurface` (internal) — layered Box composable implementing 80 % opacity + blur
- `MudawamaBottomBar` (public) — receives `currentRoute: NavKey?` and `onNavigate: (Route) -> Unit`;
  derives `selectedItem` via `BottomNavItem.entries.find { it.route == currentRoute }` (direct
  equality, no local state); applies floating shape, inset padding, and renders `NavigationBar`
  with four `NavigationBarItem` children
- `@Preview` companion for `MudawamaBottomBar`

#### `MudawamaAppShell.kt`
Contains:
- `MudawamaAppShell()` — the single public entry point for platform hosts
  - Wraps in `MudawamaTheme(darkTheme = isSystemInDarkTheme())` (FR-002)
  - Creates the backstack via `rememberNavBackStack(SavedStateConfiguration(...), HomeRoute)`
  - Renders `Scaffold(bottomBar = { MudawamaBottomBar(backStack.lastOrNull(), onNavigate) })`
  - Inside content slot: `NavDisplay(backStack) { route -> when(route) { ... } }` with four
    branches for all `Route` subtypes (FR-005)
- `@Preview` companion

### 3. Backstack Observation — The FR-008 Guarantee

```
rememberNavBackStack(SavedStateConfiguration(...), HomeRoute)
  └── returns SnapshotStateList<NavKey>   [Compose-observable mutable list — "backStack"]
      └── backStack.lastOrNull()
              → NavKey?  [reads the Snapshot list; triggers recompose on any mutation]
              → passed as `currentRoute: NavKey?` param to MudawamaBottomBar
                  └── MudawamaBottomBar derives:
                      val selectedItem = BottomNavItem.entries.find { it.route == currentRoute }
                      // Direct object equality — no hasRoute(), no reflection
                      // selectedItem recomputed on every recomposition triggered by backstack change
                      // No remember { mutableStateOf(...) } anywhere in the selection path
```

This design means:
- Programmatic navigation (test navigation, deep-link simulation) automatically syncs the bottom
  bar (US-3) because `backStack.lastOrNull()` is read directly in recomposition scope.
- Empty backstack → `currentRoute = null` → `selectedItem = null` → no tab highlighted, no crash
  (edge case §Backstack empty).
- `MudawamaBottomBar` is **pure-stateless** — fully testable by injecting any `NavKey?` value.
- The `when(route)` in `NavDisplay` is **exhaustive** because `Route` is a sealed interface —
  the compiler errors if a new destination is added without handling it.

### 4. Glassmorphism Approach (FR-009, FR-010)

The bar uses a **layered Box** approach:

```
Box (outer — applies floating modifiers: padding 16dp, clip 28dp radius, windowInsetsPadding)
├── Box (background layer)
│     Modifier
│       .matchParentSize()
│       .background(MudawamaTheme.colors.surface.copy(alpha = 0.80f))  ← 80% opacity (SC-004)
│       .blur(radius = 20.dp)                                          ← blur (FR-009)
│       .clip(RoundedCornerShape(28.dp))
└── NavigationBar (content layer — icons + labels, no blur applied to text)
      background = Color.Transparent
```

**Key notes**:
- `Modifier.blur` is available in CMP `commonMain` (Compose UI 1.7+). It applies a software blur
  to the *composable's own rendered pixels* — not a true backdrop/scene blur. The spec's
  Assumptions explicitly accept this as the fallback.
- The `alpha = 0.80f` is set via `.copy(alpha = 0.80f)` on the background color, making it
  directly inspectable in screenshot tests (SC-004).
- All colors are sourced from `MudawamaTheme.colors` — no hex literals (FR-014).

### 5. Single-Top Navigation (FR-012)

All `onNavigate` calls from `MudawamaBottomBar` go through:

```kotlin
onNavigate = { route ->
    if (backStack.lastOrNull() != route) {   // single-top guard — no NavOptions needed
        backStack.clear()
        backStack.add(route)
    }
}
```

The `if (backStack.lastOrNull() != route)` guard ensures that tapping the currently-active tab
is a no-op at the backstack level, satisfying FR-012 and US-2 scenario 3 (SC-007). Unlike
Nav 2.x, there is no `launchSingleTop`, `restoreState`, or `popUpTo` — these concepts do not
exist in Navigation 3's list-based backstack.

---

## Complexity Tracking

> No constitution violations — section left intentionally empty.
