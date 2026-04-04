# Research: shared:navigation — Navigation 3, Backstack Observation & Glassmorphism

**Feature**: `004-shared-navigation-shell`
**Phase**: 0 — Research
**Date**: 2026-04-04 (revised)

---

## R-001: KMP Navigation Library for Type-Safe `@Serializable` Routes

**Unknown**: Which navigation library and version should be used for a 100 % `commonMain` module
that needs type-safe `@Serializable` route objects and a mutable observable backstack?

**Decision**: Use **`org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha06`** — the
JetBrains Compose Multiplatform port of Google Navigation 3.

**Rationale**:
- `org.jetbrains.androidx.navigation3` is published by JetBrains under the same artifact-group
  naming convention used for all JetBrains KMP ports of Jetpack libraries (e.g.
  `org.jetbrains.androidx.lifecycle`, `org.jetbrains.androidx.navigation`). It targets
  `commonMain` and compiles for Android, iOS arm64, iOS x64, and iOS SimulatorArm64.
- Navigation 3 replaces the `NavHost` + `NavController` abstraction with a plain
  `MutableList<NavKey>` backstack (`rememberNavBackStack`). This is significantly simpler to
  wire into KMP code because there are no platform-specific `NavController` implementations to
  bridge.
- Route objects implement `NavKey` (a marker interface from
  `androidx.navigation3.runtime.NavKey`). Tab-selection is derived by direct equality comparison
  (`backStack.lastOrNull() == item.route`) — no `hasRoute()` reflection, no `NavBackStackEntry`,
  no `currentBackStackEntryAsState()`.
- Backstack mutations (`backStack.add(route)`, `backStack.clear()`) are plain list operations on
  a Compose-observable `SnapshotStateList`, so every composable that reads `backStack.lastOrNull()`
  recomposes automatically when the top-of-stack changes. This directly satisfies FR-008 and US-3.
- Single-top semantics are trivially expressed:
  ```kotlin
  if (backStack.lastOrNull() != route) {
      backStack.clear()
      backStack.add(route)
  }
  ```
  No `NavOptions`, no `launchSingleTop` DSL required.
- `SavedStateConfiguration` with a `SerializersModule` handles backstack state-save/restore
  across process death — the same feature Nav 2.x solved with `@Serializable` routes, now
  exposed directly at the backstack level.

**Version**: `1.0.0-alpha06`
**Lib catalog version alias**: `navigation3`
**Lib catalog library alias**: `navigation3-ui`
**Gradle coordinates**: `org.jetbrains.androidx.navigation3:navigation3-ui`

**`libs.versions.toml` additions**:
```toml
[versions]
navigation3 = "1.0.0-alpha06"

[libraries]
navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
```

**Alternatives considered**:
- `org.jetbrains.androidx.navigation:navigation-compose:2.9.0` (Nav 2.x KMP) — **rejected**:
  Uses the old `NavHost` / `NavController` / `currentBackStackEntryAsState()` paradigm. Backstack
  observation requires `NavBackStackEntry`, tab matching requires `hasRoute()` + reflection, and
  single-top requires a verbose `NavOptions` builder. All of these are unnecessary complexity that
  Navigation 3 eliminates.
- `androidx.navigation3:navigation3-ui` (Google's Android-only artifact, no `org.jetbrains`
  prefix) — **rejected**: Not a KMP artifact; fails to compile for iOS targets.
- Hand-rolled `mutableStateListOf<NavKey>()` — **rejected**: Reinvents Navigation 3's backstack
  primitive without `SavedStateConfiguration`, `NavDisplay`, or any of the framework plumbing.

---

## R-002: Backstack Observation Pattern (FR-008)

**Unknown**: How to drive `MudawamaBottomBar` selected-tab state from the Navigation 3 backstack
without a separate local `var selectedTab: Int` or `remember { mutableStateOf(...) }` for the
active tab index?

**Decision**: Pass `backStack.lastOrNull()` (a `NavKey?`) as `currentRoute: NavKey?` parameter to
`MudawamaBottomBar`. The composable derives the active `BottomNavItem` by direct equality —
no separate local state variable exists anywhere in the selection path.

**Rationale**:
- `rememberNavBackStack(...)` returns a `SnapshotStateList<NavKey>`. Compose automatically
  tracks reads of `lastOrNull()` on a `SnapshotStateList`; when the list changes (tab switch,
  back-press) every composable that read `backStack.lastOrNull()` recomposes. No explicit
  `collectAsState()` or `derivedStateOf` wrapper needed.
- By passing `currentRoute: NavKey?` as a parameter, `MudawamaBottomBar` is a **fully stateless
  composable**: zero `remember { ... }` for tab selection.
- This satisfies FR-008 ("derive the selected tab solely from the current backstack state"),
  US-3 (programmatic navigation auto-syncs the bar), and SC-002 (no desync).
- The bar can be tested in isolation: inject any `NavKey?` and assert visual state — no
  navigation framework needed in the test.

**Implementation pattern in `MudawamaBottomBar`**:
```kotlin
@Composable
fun MudawamaBottomBar(
    currentRoute: NavKey?,          // = backStack.lastOrNull(), passed in by MudawamaAppShell
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Direct equality — no hasRoute(), no reflection (FR-008)
    val selectedItem: BottomNavItem? = BottomNavItem.entries.find { it.route == currentRoute }
    // ...render NavigationBar using selectedItem for visual state only...
}
```

**Wiring in `MudawamaAppShell`**:
```kotlin
val backStack = rememberNavBackStack(
    SavedStateConfiguration(
        serializer = ListSerializer(PolymorphicSerializer(NavKey::class)),
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(HomeRoute::class)
                subclass(PrayerRoute::class)
                subclass(AthkarRoute::class)
                subclass(HabitsRoute::class)
            }
        }
    ),
    HomeRoute   // initial entry
)

Scaffold(
    bottomBar = {
        MudawamaBottomBar(
            currentRoute = backStack.lastOrNull(),   // reactive: recomposes on stack change
            onNavigate = { route ->
                if (backStack.lastOrNull() != route) {   // single-top guard (FR-012, SC-007)
                    backStack.clear()
                    backStack.add(route)
                }
            }
        )
    }
) { innerPadding ->
    NavDisplay(
        backStack = backStack,
        modifier = Modifier.padding(innerPadding),
    ) { route ->
        when (route) {
            HomeRoute   -> HomePlaceholderScreen()
            PrayerRoute -> PrayerPlaceholderScreen()
            AthkarRoute -> AthkarPlaceholderScreen()
            HabitsRoute -> HabitsPlaceholderScreen()
        }
    }
}
```

**Alternatives considered**:
- `remember { mutableStateOf(0) }` for tab index — **rejected**: violates FR-008; diverges from
  actual backstack on programmatic navigation (US-3 fails).
- Nav 2.x `navController.currentBackStackEntryAsState()` — **rejected**: requires
  `NavBackStackEntry` and `hasRoute()` which are absent from Navigation 3; introduces indirection
  that Nav 3's list-based backstack eliminates entirely.
- `LaunchedEffect(backStack.last())` to sync a local `var` — **rejected**: still a separate
  remembered variable that could theoretically diverge.

---

## R-003: Glassmorphism Styling (FR-009, FR-010)

**Unknown**: How to achieve the DESIGN.md §2 spec of "`surface_container_lowest` at 80 % opacity
with a `20 dp` backdrop-blur" in `commonMain` without any platform source sets (FR-013)?

**Decision**: Use a **layered Box** composable:

1. **80 % opacity on background**: Apply `.copy(alpha = 0.80f)` to `MudawamaTheme.colors.surface`.
   This is a pure-Compose color operation — no platform API needed.
2. **Blur**: Apply `Modifier.blur(radius = 20.dp)` to the background layer only.
   `androidx.compose.ui.draw.blur` is part of Compose UI `commonMain` from CMP 1.5+ and delegates
   to platform-native blur on Android (`BlurMaskFilter` / `RenderEffect`) and iOS (CoreImage
   gaussian filter via the CMP runtime bridge).

**Implementation sketch** (`GlassmorphismSurface`, internal):
```kotlin
@Composable
internal fun GlassmorphismSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColor = MudawamaTheme.colors.surface.copy(alpha = 0.80f)  // FR-009, SC-004

    Box(modifier = modifier) {
        // Layer 1: blurred semi-transparent background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(28.dp))   // floating pill shape (FR-010)
                .background(glassColor)            // 80 % opacity (FR-009)
                .blur(radius = 20.dp)              // software/hardware blur (FR-009)
        )
        // Layer 2: content — icons and labels rendered sharp (no blur applied here)
        content()
    }
}
```

**Floating outer modifier** (FR-010, FR-011, SC-003):
```kotlin
Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 8.dp)     // ≥ 16 dp from screen edges (SC-003)
    .windowInsetsPadding(WindowInsets.navigationBars)  // respect system nav bar (FR-011)
```

**True backdrop blur caveat**: `Modifier.blur` blurs the composable's *own rendered pixels*, not
the content behind it. The spec's Assumptions explicitly accept this: *"a solid semi-transparent
surface at 80 % opacity is the accepted fallback."* SC-004 only requires the 80 % alpha to be
confirmable, which the chosen approach satisfies directly via the `.copy(alpha = 0.80f)` call.

**Alternatives considered**:
- `graphicsLayer { renderEffect = BlurEffect(...) }` — **rejected**: Android API 31+ only; not
  available in `commonMain`.
- `UIVisualEffectView` — **rejected**: requires `iosMain` source set (violates FR-013).

---

## R-004: Single-Top / Tab-Switch Semantics (FR-012, SC-007)

**Decision**: Implement single-top semantics with a plain guard check on the Navigation 3
backstack — no NavOptions DSL required:

```kotlin
onNavigate = { route ->
    if (backStack.lastOrNull() != route) {   // single-top guard (FR-012, SC-007)
        backStack.clear()                    // clear the stack when switching tabs
        backStack.add(route)                 // push the new tab's root destination
    }
    // If already on this route: no-op — backstack depth unchanged (SC-007)
}
```

**Rationale**:
- Navigation 3's `SnapshotStateList<NavKey>` is a plain mutable list. "Single-top" is just
  "do nothing if the new destination is already the last element" — a single `if` check.
- `backStack.clear()` then `backStack.add(route)` achieves the correct bottom-nav tab-switch
  behavior: each tab always starts from its root destination; back-stack depth stays at 1
  for top-level tab routes.
- No `NavOptions`, no `popUpTo`, no `saveState` / `restoreState` required. State restoration
  for individual screens within a tab is a future concern (out of scope for this spec).

**Alternatives considered**:
- Nav 2.x `launchSingleTop = true; restoreState = true; popUpTo(startDestination) { saveState = true }` —
  **rejected**: This is the Nav 2.x idiom; none of these APIs exist in Navigation 3.
- Allowing duplicates in the stack — **rejected**: violates FR-012 and SC-007.

---

## R-005: `SavedStateConfiguration` for Process-Death Recovery

**Decision**: Configure `rememberNavBackStack` with a `SavedStateConfiguration` that uses
`ListSerializer(PolymorphicSerializer(NavKey::class))` and a `SerializersModule` registering
all four concrete route types as polymorphic subclasses of `NavKey`.

**Rationale**:
- Navigation 3 persists the backstack across process death via the Compose saved-state mechanism.
  `SavedStateConfiguration` is the bridge between the `NavKey` list and the serialization layer.
- Since `NavKey` is an interface (not a sealed class), kotlinx-serialization requires explicit
  polymorphic registration to know which deserializer to invoke for each concrete type.
- Each concrete route (`HomeRoute`, `PrayerRoute`, etc.) is `@Serializable` via the
  `kotlinx.serialization` compiler plugin. The `sealed interface Route : NavKey` declaration
  allows a single exhaustive `when(route)` — no `else` branch needed, compiler-enforced.
- The `@Serializable` annotation on the `Route` sealed interface enables the kotlinx-serialization
  plugin to generate a polymorphic descriptor, required for the
  `PolymorphicSerializer(NavKey::class)` call.

```kotlin
val backStack = rememberNavBackStack(
    SavedStateConfiguration(
        serializer = ListSerializer(PolymorphicSerializer(NavKey::class)),
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(HomeRoute::class)
                subclass(PrayerRoute::class)
                subclass(AthkarRoute::class)
                subclass(HabitsRoute::class)
            }
        }
    ),
    HomeRoute
)
```

---

## R-006: Icons for Navigation Tabs

**Decision**: Use **Material 3 `Icons.Default` set** (already a transitive dependency through
`compose-material3` declared by `mudawama.kmp.presentation`):
- Home → `Icons.Default.Home`
- Prayer → `Icons.Default.Star` (pending custom icon in `shared:designsystem`)
- Athkar → `Icons.Default.FavoriteBorder` (pending custom icon)
- Habits → `Icons.Default.CheckCircle` (pending custom icon)

**Rationale**: The spec's Assumptions explicitly state "Material3 icon alternatives from the
Compose Material3 icons bundle are an acceptable temporary substitute." Replace with
`Res.drawable.*` from `shared:designsystem` in a follow-up.

**Note**: `Icons.Default.FavoriteBorder` and `Icons.Default.CheckCircle` may require
`compose.material.icons.extended`. Add `implementation(compose.materialIconsExtended)` to
`commonMain.dependencies` if they fail to resolve at compile time.

---

## Resolution Summary

| Item | Status | Artifact / Approach |
|------|--------|---------------------|
| Navigation library | ✅ Resolved | `org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha06` |
| `libs.versions.toml` changes | ✅ Resolved | Add `navigation3 = "1.0.0-alpha06"` + `navigation3-ui` library entry |
| Backstack primitive | ✅ Resolved | `rememberNavBackStack(SavedStateConfiguration(...), HomeRoute)` |
| Backstack observation (FR-008) | ✅ Resolved | `backStack.lastOrNull(): NavKey?` → `currentRoute` param to bottom bar |
| Tab derivation (no local var) | ✅ Resolved | `BottomNavItem.entries.find { it.route == currentRoute }` |
| Single-top semantics (FR-012) | ✅ Resolved | `if (backStack.lastOrNull() != route) { backStack.clear(); backStack.add(route) }` |
| State-save on process death | ✅ Resolved | `SavedStateConfiguration` with `PolymorphicSerializer` + `SerializersModule` |
| Routing display (NavHost equiv) | ✅ Resolved | `NavDisplay(backStack) { route -> when(route) { ... } }` |
| Glassmorphism: 80 % opacity (FR-009) | ✅ Resolved | `.copy(alpha = 0.80f)` on surface color |
| Glassmorphism: blur (FR-009) | ✅ Resolved | `Modifier.blur(radius = 20.dp)` on background layer (commonMain) |
| Floating bar shape (FR-010) | ✅ Resolved | `padding(horizontal = 16.dp)` + `clip(RoundedCornerShape(28.dp))` |
| Bottom insets (FR-011) | ✅ Resolved | `Modifier.windowInsetsPadding(WindowInsets.navigationBars)` |
| Icons | ✅ Resolved | `Icons.Default.*` (temporary; replace with designsystem resources) |

