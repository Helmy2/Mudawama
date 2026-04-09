# Quickstart: shared:navigation — Implementation Guide

**Feature**: `004-shared-navigation-shell`
**Date**: 2026-04-04 (revised — Navigation 3)

---

## Prerequisites

Before writing any code, confirm:

1. `shared:designsystem` compiles cleanly (`./gradlew :shared:designsystem:compileKotlinAndroid`).
2. You are on branch `004-shared-navigation-shell`.
3. The research and data-model documents are read (`research.md`, `data-model.md`).

---

## Step 1 — Register the module in `settings.gradle.kts`

```kotlin
// settings.gradle.kts — add after include(":shared:designsystem")
include(":shared:navigation")
```

---

## Step 2 — Add Navigation 3 to `gradle/libs.versions.toml`

```toml
[versions]
# ... existing entries ...
navigation3 = "1.0.0-alpha06"       # JetBrains KMP port of Navigation 3

[libraries]
# ... existing entries ...
navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
```

> ⚠️ **Do NOT add** `navigation = "2.9.0"` or `navigation-compose`. This module uses Navigation
> 3 exclusively — there is no `NavHost`, no `NavController`, and no `currentBackStackEntryAsState`.

No other version catalog changes are needed. `kotlinxSerialization` plugin and
`kotlinx-serialization-json` library already exist.

---

## Step 3 — Create `shared/navigation/build.gradle.kts`

```kotlin
plugins {
    id("mudawama.kmp.presentation")           // Compose MPP + Material3 + lifecycle + Koin + previews
    alias(libs.plugins.kotlinxSerialization)  // required for @Serializable route objects (FR-003)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.navigation"
    }

    // Exports a named iOS framework so iosApp can link against it
    configureIosFramework("MudawamaNavigation", isStatic = true)

    sourceSets {
        commonMain.dependencies {
            // api — consumers of :shared:navigation get MudawamaTheme on their classpath
            api(projects.shared.designsystem)

            // Navigation 3 KMP: NavKey, NavDisplay, rememberNavBackStack, SavedStateConfiguration
            implementation(libs.navigation3.ui)

            // Explicit serialization dep for auditing (also brought in transitively)
            implementation(libs.kotlinx.serialization.json)
        }
        // NO androidMain / iosMain — FR-013 mandates 100% commonMain
    }
}
```

---

## Step 4 — Create the source directory tree

```
shared/navigation/src/commonMain/kotlin/io/github/helmy2/mudawama/navigation/
```

Create exactly **four** files in this order (dependencies flow downward):

```
1. Routes.kt               ← no dependencies on other files in this module
2. Placeholders.kt         ← depends on shared:designsystem (MudawamaTheme)
3. MudawamaBottomBar.kt    ← depends on Routes.kt + shared:designsystem
4. MudawamaAppShell.kt     ← depends on all three above
```

---

## Step 5 — Implement `Routes.kt`

```kotlin
package io.github.helmy2.mudawama.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ── Route sealed interface ────────────────────────────────────────────────────

@Serializable
sealed interface Route : NavKey

// ── Top-level route objects ───────────────────────────────────────────────────
// Note: HabitsRoute does not exist. HomeRoute renders HabitsScreen directly.

@Serializable data object HomeRoute   : Route   // Home tab → Daily Habits screen
@Serializable data object PrayerRoute : Route
@Serializable data object AthkarRoute : Route
@Serializable data object QuranRoute  : Route

// ── Bottom nav item metadata ──────────────────────────────────────────────────
// Tabs: Home, Prayer, Quran, Athkar (no Habits tab)

enum class BottomNavItem(
    val route: Route,
    val icon: ImageVector,
    val labelKey: String,
) {
    HOME   (HomeRoute,    Icons.Default.Home,          "tab_home"),
    PRAYER (PrayerRoute,  Icons.Default.Star,           "tab_prayer"),
    QURAN  (QuranRoute,   Icons.Default.Book,           "tab_quran"),
    ATHKAR (AthkarRoute,  Icons.Default.FavoriteBorder, "tab_athkar"),
}
```

---

## Step 6 — Implement `Placeholders.kt`

Only two placeholder screens remain (`QuranPlaceholderScreen`, `AthkarPlaceholderScreen`).
`HomePlaceholderScreen` and `HabitsPlaceholderScreen` have been removed — `HomeRoute` renders
`HabitsScreen` directly and `PrayerRoute` renders `PrayerScreen` directly.

```kotlin
package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun QuranPlaceholderScreen() {
    PlaceholderContent(label = "Quran")
}

@Composable
fun AthkarPlaceholderScreen() {
    PlaceholderContent(label = "Athkar")
}

@Composable
private fun PlaceholderContent(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MudawamaTheme.typography.titleLarge,
            color = MudawamaTheme.colors.onSurface,
        )
    }
}

@Preview @Composable fun QuranPlaceholderScreenPreview()  { QuranPlaceholderScreen() }
@Preview @Composable fun AthkarPlaceholderScreenPreview() { AthkarPlaceholderScreen() }
```

---

## Step 7 — Implement `MudawamaBottomBar.kt`

The bar uses a custom `Row` of `BottomBarTab` composables instead of `NavigationBar`/`NavigationBarItem`.
Active tab: deep-teal rounded-square pill (16dp corners). Inactive: transparent, 55% opacity.

Key design decisions:
- `currentRoute: NavKey?` is the only selection source (FR-008) — no local state variable.
- Tab selection: `BottomNavItem.entries.find { it.route == currentRoute }` (direct equality).
- All colors from `MudawamaTheme.colors` — no hex literals (FR-014).

```kotlin
package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// ── Private design constants (no hardcoded colors — FR-014) ───────────────────
private val BarHorizontalPadding  = 16.dp   // SC-003: ≥ 16 dp from screen edge
private val BarVerticalPadding    = 8.dp
private val BarCornerRadius       = 28.dp   // FR-010: rounded floating shape
private val GlassBlurRadius       = 20.dp   // FR-009: DESIGN.md §2 — 20 dp blur
private const val GlassAlpha      = 0.80f   // FR-009, SC-004: 80 % opacity

// ── Public composable ─────────────────────────────────────────────────────────

/**
 * Floating glassmorphism bottom navigation bar.
 *
 * [currentRoute] is the ONLY source of truth for which tab is selected (FR-008).
 * Selected tab is derived by [it.route == currentRoute] — direct object equality,
 * no hasRoute(), no NavBackStackEntry, no remembered local variable.
 */
@Composable
fun MudawamaBottomBar(
    currentRoute: NavKey?,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Derive active tab from current route — no separate remembered variable (FR-008)
    val selectedItem: BottomNavItem? = BottomNavItem.entries.find { it.route == currentRoute }

    GlassmorphismSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BarHorizontalPadding, vertical = BarVerticalPadding)
            .windowInsetsPadding(WindowInsets.navigationBars),  // FR-011
    ) {
        NavigationBar(
            containerColor = Color.Transparent,   // background handled by GlassmorphismSurface
            contentColor = MudawamaTheme.colors.onSurface,
        ) {
            BottomNavItem.entries.forEach { item ->
                NavigationBarItem(
                    selected = item == selectedItem,
                    onClick  = { onNavigate(item.route) },
                    icon     = { Icon(item.icon, contentDescription = item.labelKey) },
                    label    = { Text(item.labelKey) },  // TODO: replace with stringResource(Res.string.*)
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = MudawamaTheme.colors.primary,
                        selectedTextColor   = MudawamaTheme.colors.primary,
                        unselectedIconColor = MudawamaTheme.colors.onSurface,
                        unselectedTextColor = MudawamaTheme.colors.onSurface,
                        indicatorColor      = MudawamaTheme.colors.primary.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

// ── Internal glassmorphism helper ─────────────────────────────────────────────

/**
 * Layered Box that provides the glassmorphism background (80 % opacity + blur).
 *
 * Layer 1 (background): semi-transparent surface color + blur — creates the frosted-glass look.
 * Layer 2 (content):    NavigationBar rendered above the blur layer — icons/labels stay sharp.
 *
 * See research.md R-003 for the rationale and future platform-specific enhancement path.
 */
@Composable
internal fun GlassmorphismSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val glassColor = MudawamaTheme.colors.surface.copy(alpha = GlassAlpha)  // FR-009, SC-004

    Box(modifier = modifier) {
        // Background layer: blurred semi-transparent fill
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(BarCornerRadius))    // FR-010
                .background(glassColor)                       // 80% opacity surface
                .blur(radius = GlassBlurRadius),              // FR-009 — software/hardware blur
        )
        // Content layer: rendered above blur — no blur applied to icons/labels
        content()
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
fun MudawamaBottomBarPreview() {
    MudawamaTheme {
        // Pass HomeRoute to simulate the Home tab selected in preview
        MudawamaBottomBar(
            currentRoute = HomeRoute,
            onNavigate = {},
        )
    }
}
```

---

## Step 8 — Implement `MudawamaAppShell.kt`

Key changes from the original spec:
- Signature: `fun MudawamaAppShell(habitsScreen: @Composable () -> Unit, prayerScreen: @Composable () -> Unit)`
- `SerializersModule` registers `QuranRoute` (not `HabitsRoute`)
- `HomeRoute` branch calls `habitsScreen()`, `PrayerRoute` calls `prayerScreen()`
- 150 ms `fadeIn`/`fadeOut` applied to `NavDisplay` for smooth iOS transitions

```kotlin
package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.SavedStateConfiguration
import androidx.navigation3.ui.NavDisplay
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.PolymorphicSerializer
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MudawamaAppShell(
    habitsScreen: @Composable () -> Unit,
    prayerScreen: @Composable () -> Unit,
) {
    MudawamaTheme(darkTheme = isSystemInDarkTheme()) {

        val backStack = rememberNavBackStack(
            SavedStateConfiguration(
                serializer = ListSerializer(PolymorphicSerializer(NavKey::class)),
                serializersModule = SerializersModule {
                    polymorphic(NavKey::class) {
                        subclass(HomeRoute::class)
                        subclass(PrayerRoute::class)
                        subclass(AthkarRoute::class)
                        subclass(QuranRoute::class)   // ← QuranRoute, not HabitsRoute
                    }
                }
            ),
            HomeRoute
        )

        Scaffold(
            bottomBar = {
                MudawamaBottomBar(
                    // FR-008: backStack.lastOrNull() is the ONLY source of truth — no local var
                    currentRoute = backStack.lastOrNull(),
                    onNavigate = { route ->
                        // FR-012 / SC-007: single-top guard
                        if (backStack.lastOrNull() != route) {
                            backStack.clear()
                            backStack.add(route)
                        }
                    },
                )
            },
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier  = Modifier.padding(innerPadding),
                // 150ms fade eliminates iOS animation lag
                transitionSpec    = { fadeIn(150) togetherWith fadeOut(150) },
                popTransitionSpec = { fadeIn(150) togetherWith fadeOut(150) },
            ) { route ->
                when (route) {
                    HomeRoute   -> habitsScreen()               // real HabitsScreen
                    PrayerRoute -> prayerScreen()               // real PrayerScreen
                    AthkarRoute -> AthkarPlaceholderScreen()
                    QuranRoute  -> QuranPlaceholderScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun MudawamaAppShellPreview() {
    MudawamaAppShell(
        habitsScreen = {},
        prayerScreen = {},
    )
}
```

---

## Step 9 — Wire into `androidApp`

In `androidApp/src/main/.../MainActivity.kt`:

```kotlin
import io.github.helmy2.mudawama.navigation.MudawamaAppShell

// Inside onCreate():
setContent {
    MudawamaAppShell()   // replaces the previous App() from shared:umbrella-ui
}
```

Add `:shared:navigation` to `androidApp/build.gradle.kts`:

```kotlin
commonMain.dependencies {
    implementation(projects.shared.navigation)
}
```

---

## Step 10 — Wire into `iosApp`

In `iosApp/iosApp/ContentView.swift`:

```swift
import shared  // the MudawamaNavigation framework

struct ContentView: View {
    var body: some View {
        ComposeUIViewControllerRepresentable {
            MudawamaAppShellKt.MudawamaAppShell()
        }
    }
}
```

> **Note**: Migration of platform call sites is marked as out-of-scope by the spec's Assumptions.
> Steps 9 and 10 are provided for reference only; they are not part of this feature's delivery
> criteria.

---

## Verification Checklist

- [ ] `./gradlew :shared:navigation:compileKotlinAndroid` — zero errors
- [ ] `./gradlew :shared:navigation:compileKotlinIosSimulatorArm64` — zero errors
- [ ] All `@Preview` composables render without exceptions in Android Studio
- [ ] Running on Android: bottom bar visible with four tabs **Home, Prayer, Quran, Athkar**; Home tab selected by default (SC-001)
- [ ] Home tab shows Daily Habits screen (HabitsScreen), not a placeholder
- [ ] Tapping each tab switches content and updates selected indicator; only one tab selected at a time (SC-001, SC-002)
- [ ] Tapping the already-active tab does not increase backstack depth (SC-007)
- [ ] Bottom bar has ≥ 16 dp horizontal clearance from screen edges (SC-003)
- [ ] Bottom bar is not obscured by system navigation bar on Android gesture-nav devices (FR-011)
- [ ] Running on iOS (simulator): same behavioral checks pass (SC-001–SC-005)
- [ ] Source file count in `commonMain/kotlin/` = 4 (SC-006)
- [ ] No `NavController`, `NavHost`, `currentBackStackEntryAsState`, `NavBackStackEntry`, `hasRoute`, or `HabitsRoute` anywhere in the module

---

## Estimated File Sizes

| File | Estimated lines |
|------|----------------|
| `Routes.kt` | ~40 |
| `Placeholders.kt` | ~55 |
| `MudawamaBottomBar.kt` | ~100 |
| `MudawamaAppShell.kt` | ~70 |
| `build.gradle.kts` | ~22 |
| **Total** | **~287** |
