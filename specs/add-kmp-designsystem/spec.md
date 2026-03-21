Feature: Add KMP shared design system (shared:designsystem)

Overview and Goals
------------------
Purpose: Create a Kotlin Multiplatform (KMP) "shared:designsystem" module that centralizes visual tokens, theme, and a small set of common UI components for the Mudawama app. The module must be implemented with Compose Multiplatform (org.jetbrains.compose), expose idiomatic Kotlin APIs from commonMain, and be consumable by Android and iOS (via SwiftUI interop where applicable). Desktop targets are optional.

Goals:
- Provide a stable, well-documented theme API (MudawamaTheme) exposing colors, typography, and shapes.
 - Ship a minimal, tested set of components: MudawamaPrimaryButton, MudawamaGhostButton, MudawamaSurfaceCard.
- Offer platform-friendly tokens (colors, type, shapes) from commonMain so features can share consistent styling.
- Make the API ergonomic, KMP-friendly, and resilient to dark mode and dynamic type.

Constraints and Compatibility
----------------------------
- Platform: Kotlin Multiplatform (commonMain + platform-specific source sets). Targets: Android and iOS (iosArm64 / iosX64 / iosSimulatorArm64). Desktop/JVM target optional (recommend jvm target when needed).
- UI implementation: org.jetbrains.compose (Compose Multiplatform) for common UI code. Provide guidance for SwiftUI interop (UIView/UIViewRepresentable or SwiftUI hosting when Compose for iOS is used) but do not mandate specific bridging on iOS consumers.
- API surface must be idiomatic Kotlin and available from commonMain. Avoid exposing platform-only types in public APIs.
- Keep module footprint minimal; avoid bringing large transitive dependencies.

Color Palette
-------------
Named tokens (use these exact hex values):

- DeepTeal (primary): #02594F
- CalmEmerald (primaryVariant / accent): #1B8049
- OffWhiteBackground (background): #F7F7F4
- PureWhiteSurface (surface): #FFFFFF
- CharcoalText (onSurface / primary text): #1D2322
- MutedRedError (error): #C45151

Roles and usages:
- primary: DeepTeal (#02594F)
- primaryVariant / accent: CalmEmerald (#1B8049)
- background: OffWhiteBackground (#F7F7F4) — page background, behind scrollable content
- surface: PureWhiteSurface (#FFFFFF) — cards, sheets, navigation bars
- onSurface: CharcoalText (#1D2322) — primary text on surface/background
- onPrimary: PureWhiteSurface (#FFFFFF) — text/icons on top of primary buttons
- error: MutedRedError (#C45151)

Light / Dark considerations:
- Light theme: use tokens as defined above.
- Dark theme recommendations (semantic mappings):
  - primary: DeepTeal -> use a lighter tint for elevated contrast on dark backgrounds (suggest blending Lighten 30% or provide 80% brightness alternative). For implementers: provide a secondary palette entry `primaryDark` derived from DeepTeal (e.g., #47A88B) so buttons remain legible.
  - background (dark): map OffWhiteBackground -> CharcoalText-like deep background (#0F1413 or #0B0E0E). Keep surface darker than background by a small elevation delta.
  - onSurface (dark): use OffWhiteBackground (#F7F7F4) or PureWhiteSurface (#FFFFFF) for high contrast on dark surfaces.

Suggested alpha usages (light theme):
- Disabled text: CharcoalText at 60% alpha (#1D2322@0.60)
- Secondary text / hint: CharcoalText at 54% alpha
- Dividers / strokes: CharcoalText at 12-16% alpha depending on elevation
- Overlay scrim (e.g., modal dim): CharcoalText at 60-72% alpha

Typography
----------
Design goals: highly readable scale optimized for prayer/reading-focused app: generous line heights, clear hierarchy, and support for dynamic scaling.

Token naming (suitable for Compose Multiplatform):
- H1: weight=700, size=28sp, lineHeight=34sp, letterSpacing=0sp
- H2: weight=600, size=22sp, lineHeight=28sp, letterSpacing=0sp
- H3: weight=600, size=18sp, lineHeight=24sp, letterSpacing=0sp
- H4: weight=600, size=16sp, lineHeight=22sp, letterSpacing=0sp
- H5: weight=500, size=14sp, lineHeight=20sp, letterSpacing=0.15sp
- H6: weight=500, size=12sp, lineHeight=18sp, letterSpacing=0.15sp

- body1: weight=400, size=16sp, lineHeight=24sp, letterSpacing=0.25sp
- body2: weight=400, size=14sp, lineHeight=20sp, letterSpacing=0.25sp
- caption: weight=400, size=12sp, lineHeight=16sp, letterSpacing=0.4sp
- button: weight=600, size=14sp, lineHeight=16sp, letterSpacing=1.25sp (uppercase optional at call-site)

Font guidance:
- Default: use platform system fonts via Compose Multiplatform (e.g., FontFamily.Default). This ensures native typography elasticity and accessibility.
- Optional: allow module consumers to provide a bundled font family via a Theme parameter (see MudawamaTheme API). If consumers provide a custom FontFamily, it should be applied across typographic tokens.

Shapes
------
Standard shape tokens (use DP references that map to platform units in Compose):
- ShapeSmall (controls, chips): 8.dp radius
- ShapeMedium (buttons, inputs): 16.dp radius
- ShapeLarge (cards, surfaced containers): 16.dp radius (or 12.dp where a slightly softer corner is desired)

Usage recommendations:
- Buttons: ShapeMedium (16dp) for primary controls to give a friendly rounded appearance.
- Cards and surfaces: ShapeLarge (16dp) or 8dp depending on visual density; default to 16dp for Mudawama's approachable aesthetic.
- Icon buttons / chips: ShapeSmall (8dp) for compact elements.

Theme API
---------
Provide a single entry composable `MudawamaTheme` that wraps Compose Material 3 (compose.material3) MaterialTheme and exposes tokens via a Kotlin object.

API sketch (Kotlin, commonMain):

```kotlin
package io.github.helmy2.mudawama.designsystem

@Composable
fun MudawamaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Provide Colors, Typography, Shapes to MaterialTheme (compose.material3)
}

object MudawamaTheme {
    val colors: MudawamaColors
    val typography: MudawamaTypography
    val shapes: MudawamaShapes
}

// Access tokens: MudawamaTheme.colors.primary
```

Parameters & defaults:
- darkTheme: Boolean = isSystemInDarkTheme() — allows consumer to toggle dark mode explicitly.
- optional parameters considered (but keep API lean): colorOverrides: MudawamaColors? = null, typographyOverrides: MudawamaTypography? = null, shapesOverrides: MudawamaShapes? = null. Use these only if consumers need to override tokens programmatically.

Behavior:
- `MudawamaTheme` sets up a Colors implementation (MudawamaColors) for light and dark schemes, a typography object (MudawamaTypography) and shapes (MudawamaShapes). The composable should call MaterialTheme(colors, typography, shapes, content) under the hood so all Compose components consume the tokens.
- Provide extension properties and convenience accessors from commonMain:
  - MudawamaTheme.colors.primary
  - MudawamaTheme.colors.background
  - MudawamaTheme.typography.h1
  - MudawamaTheme.shapes.medium

Dark mode guidance:
- Default darkTheme follows system setting. The caller can pass darkTheme = true to force dark mode.
- Provide contrast-adjusted color variants within the MudawamaColors data class so that components don't need to compute color transforms.

Components
----------
For each component below, the public API is defined for use from commonMain and should not expose platform-specific types.

1) MudawamaPrimaryButton
-------------------------
- Purpose: Emphasized primary CTA button.

Public API (commonMain):

```kotlin
@Composable
fun MudawamaPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    contentDescription: String? = null,
)
```

Behavior & visual spec:
- Background: MudawamaTheme.colors.primary
- Content (text + optional icons) color: MudawamaTheme.colors.onPrimary (PureWhiteSurface)
- Shape: MudawamaTheme.shapes.medium (16dp)
- Padding: 16dp horizontal, 12dp vertical (resulting min-height ~48dp)
- Min size: minHeight = 48.dp, minWidth = 64.dp
- Text: use MudawamaTheme.typography.button, weight 600
- Icon spacing: 8dp gap between icon and text
- Disabled state: background uses primary at 32-40% alpha or a designated `primaryDisabled` token; text uses onPrimary at 60% alpha
- Pressed/ripple: use default Compose ripple with onPrimary color overlay; pressed elevation increase of 2dp (optional)

Accessibility:
- If `contentDescription` provided, add semantics for the icon region; otherwise ensure text is announced. Use `role = Role.Button` and `enabled` semantics.

Compose MPP notes:
- Accept and forward `modifier: Modifier` so consumers can add layout constraints.
- Expose `onClick` as simple lambda. Use `Clickable` or `Button` from Compose, not platform-clicks.
- Keep internal state stateless (stateless composable). Selection, loading, or other states should be handled by caller-provided booleans or higher-order wrappers.

Example usage:

```kotlin
MudawamaPrimaryButton(onClick = { /* navigate */ }, text = "Start")

MudawamaPrimaryButton(
    onClick = { /* save */ },
    text = "Save",
    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
)
```

2) MudawamaGhostButton
-----------------------
- Purpose: Low-emphasis button for secondary actions; transparent background, border or text-only.

Public API:

```kotlin
@Composable
fun MudawamaGhostButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    outlined: Boolean = false, // if true, show 1dp border with primary color; otherwise text-only
    contentDescription: String? = null
)
```

Behavior & visual spec:
- Text color: MudawamaTheme.colors.primary
- If outlined: border 1.dp in MudawamaTheme.colors.primary at 24-32% alpha for subtlety
- Background: transparent (use elevation overlay only on press)
- Shape: MudawamaTheme.shapes.small (8dp) or medium for larger variants
- Padding: 12dp horizontal, 8-10dp vertical; minHeight 40dp
- Disabled state: text at 40-60% alpha
- Ripple/pressed: subtle tint of primary at 16-24% alpha

Accessibility:
- Ensure semantics Role.Button; allow contentDescription for icon-only variants.

Compose MPP notes:
- Stateless, accept modifier, onClick as lambda.

Example:

```kotlin
MudawamaGhostButton(onClick = { /*cancel*/ }, text = "Cancel")

MudawamaGhostButton(onClick = { /*more*/ }, text = "More", outlined = true)
```

3) MudawamaSurfaceCard
-----------------------
- Purpose: Surface container for grouped content, lists, or small feature cards.

Public API:

```kotlin
@Composable
fun MudawamaSurfaceCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

Behavior & visual spec:
- Background: MudawamaTheme.colors.surface (PureWhiteSurface)
- Shape: MudawamaTheme.shapes.large (16dp) by default
- Padding inside card: 16dp
- Elevation: default 4.dp, increase on press if clickable
- If `onClick` provided, card is clickable; provide ripple using content color of onSurface.

Accessibility:
- If clickable, expose role = Role.Button and include contentDescription if provided via semantics.

Compose MPP notes:
- Implement using Surface/Box from Compose; keep commonMain implementation.

Example:

```kotlin
MudawamaSurfaceCard(onClick = { /* open */ }) {
   Text("Card title", style = MudawamaTheme.typography.h4)
   Spacer(modifier = Modifier.height(8.dp))
   Text("Subtitle or description", style = MudawamaTheme.typography.body2)
}
```

Module Structure
----------------
Suggested KMP source layout:

    specs/add-kmp-designsystem/
    - src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/
  - Theme.kt                    // MudawamaTheme composable, public token accessors
  - Colors.kt                   // MudawamaColors data class + light/dark palettes
  - Typography.kt               // MudawamaTypography tokens
  - Shapes.kt                   // MudawamaShapes tokens
  - Tokens.kt                   // Lightweight mapping of color/typography/shape tokens
  - components/
    - MudawamaPrimaryButton.kt          // MudawamaPrimaryButton implementation
    - GhostButton.kt            // MudawamaGhostButton implementation
    - SurfaceCard.kt            // MudawamaSurfaceCard implementation
  - utils/
    - Accessibility.kt         // common semantics helpers
    - Platform.kt              // small helpers if needed for platform detection

Files description:
- Theme.kt: exposes the MudawamaTheme composable and MudawamaTheme object with colors/typography/shapes properties. Maps tokens into Compose MaterialTheme.
- Colors.kt: contains color constants and palette definitions for light/dark.
- Typography.kt: defines typography tokens and a public MudawamaTypography data class.
- Shapes.kt: defines shape tokens as a data class and converters to Compose shapes.
- MudawamaPrimaryButton.kt, GhostButton.kt, SurfaceCard.kt: component implementations using Compose primitives.
- Accessibility.kt: common helpers to attach semantic properties consistently.

Build and Dependencies
----------------------
Add a new module `shared:designsystem` and include the following minimal snippet in the module's `build.gradle.kts` (Kotlin DSL). This is a suggested starting point; adjust targets to match repo conventions.

```kotlin
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.5.0" // align with repo's compose version
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    // optional: jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.10.1")
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
        }
    }
}

// Keep publishable artifact lightweight
```

Testing & Previews
------------------
Unit tests:
- Token tests: simple unit tests in commonTest to assert hex values and token mappings (e.g., primary == Color(0xFF02594F)).
- Typography tests: assert that token sizes and weights are defined as expected.

Compose previews:
- Provide preview composables for Android Studio and Compose Multiplatform preview tooling that show each component in light and dark themes and with accessibility (large text) scales.

Example preview (Android/Compose preview):

```kotlin
@Preview(showBackground = true)
@Composable
fun PrimaryButtonPreview() {
    MudawamaTheme {
        MudawamaPrimaryButton(onClick = {}, text = "Start")
    }
}
```

Accessibility, Localization, and Theming Notes
---------------------------------------------
- Contrast: ensure text colors on primary and surface meet WCAG AA (>=4.5:1 for normal text). Validate contrast at common font sizes. If a token fails contrast under certain conditions (e.g., small captions on primary button), provide a more contrast-friendly variant.
- Dynamic type: scale typography tokens using Compose's text scaling (sp units) and test at large accessibility scales. Allow consumers to supply alternative typography via MudawamaTheme parameters.
- RTL: Layouts and components must support RTL; use CompositionLocalLayoutDirection or rely on Compose's built-in directionality. Avoid hard-coded paddings that assume LTR only.
- Localization: Do not embed literal strings in components. Accept text parameters and contentDescription for icons. Provide guidance for component consumers to pass localized strings.

Deliverables (concrete files)
----------------------------
The following files should be created in the module (task dependencies noted):

1) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Theme.kt    (depends on Colors.kt, Typography.kt, Shapes.kt)
2) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Colors.kt   (independent)
3) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Typography.kt (independent)
4) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Shapes.kt   (independent)
5) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Tokens.kt   (helper mappings)
6) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/MudawamaPrimaryButton.kt  (depends on Theme.kt, Shapes.kt)
7) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/GhostButton.kt    (depends on Theme.kt)
8) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/SurfaceCard.kt    (depends on Theme.kt)
9) src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/utils/Accessibility.kt      (optional helper)
10) README.md (module usage, integration notes)
11) build.gradle.kts (module build config)
12) samples/ (small sample screens showing components usage) — optional but recommended

Acceptance Criteria
-------------------
The feature is complete when all items below pass verification:

1. MudawamaTheme exists in `commonMain` and provides Colors, Typography, and Shapes accessible via `MudawamaTheme.colors`, `MudawamaTheme.typography`, and `MudawamaTheme.shapes`.
2. Color tokens match the exact hex values specified (DeepTeal #02594F, CalmEmerald #1B8049, OffWhiteBackground #F7F7F4, PureWhiteSurface #FFFFFF, CharcoalText #1D2322, MutedRedError #C45151).
3. Typography tokens defined with the sizes/weights listed and available from commonMain.
4. Shapes tokens include small/medium/large with 8dp/16dp radii and are used by the components.
5. MudawamaPrimaryButton, MudawamaGhostButton, MudawamaSurfaceCard are implemented in commonMain with the public APIs specified, including modifier and onClick usage.
6. Components render correctly on Android (Compose) with requested paddings/min-sizes and corner radii.
7. Accessibility: tappable components have Role.Button semantics, accept contentDescription for icons, and pass basic provider checks (focusable, enabled state exposure).
8. (Deprecated) Unit tests in commonTest were removed per project decision; token validation is manual for now.
9. Preview composables demonstrate components in light/dark and large text scale.
10. Module build.gradle.kts compiles for Android and iOS targets and adds minimal Compose Multiplatform dependencies.

Assumptions
-----------
- Consumers will integrate the KMP compose plugin and match compose versions used in the repo.
- No bundled custom fonts are required by default; system fonts are acceptable unless consumer requests otherwise.
- iOS consumers will either host Compose UI via the Compose for iOS runtime or bridge using native hosting patterns; the design system provides tokens and components from commonMain.

Questions / Clarifications
-------------------------
1) Fonts: Should the design system bundle a custom typeface (e.g., a bespoke Arabic-friendly font) or should it rely on platform system fonts and allow consumers to inject a FontFamily? (This affects assets and build configuration.)

If no response, default behavior is to use system fonts and provide an optional API to inject a FontFamily.

Specification status: READY for planning

