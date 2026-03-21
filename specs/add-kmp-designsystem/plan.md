 # plan.md — Implementation Plan: Add KMP shared design system (shared:designsystem)

 Last updated: 2026-03-21

 Summary and goals (one paragraph)
 - Build a Kotlin Multiplatform `shared:designsystem` module providing a Compose Multiplatform-based Material 3 theme and a minimal set of components to standardize Mudawama UI. Package root: `io.github.helmy2.mudawama.designsystem`. The design system will use `compose.material3` (Material 3) APIs where appropriate. For the MVP, fonts will be platform system fonts (FontFamily.Default) to preserve native typography and accessibility; an override hook in the Theme API will allow consumers to supply custom fonts later.

 Theme-first approach
 - Rationale: Implement a functional `MudawamaTheme` early (shell) so components, previews, and integration samples can be developed and validated immediately. The theme shell uses placeholder tokens that match spec values; tokens (Colors/Typography/Shapes) are implemented next and then wired into the theme (refinement step). This enables parallel work: component authors and preview creation can start while token files are finalized.

 Assumptions
 - Module name: `:shared:designsystem` under `shared/designsystem`.
 - Compose Multiplatform and org.jetbrains.compose plugin version are aligned with the repo.
 - System fonts used for MVP; no bundled fonts.

 Phases and tasks (high level)
 - Phase 0 — Research & repo validation (DS-000)
 - Phase 1 — Scaffolding (DS-001)
 - Phase 2 — Theme-first: Theme shell (DS-002)
 - Phase 3 — Token implementation (DS-003..DS-005)
 - Phase 4 — Theme refinement with tokens (DS-006)
 - Phase 5 — Accessibility helpers (DS-007)
 - Phase 6 — Components (DS-008..DS-010)
 - Phase 7 — Previews, samples, tests, and docs (DS-011..DS-015)
 - Phase 8 — Final QA & merge (DS-016)

 Detailed implementation checklists (by file)

 Colors.kt (DS-003)
 - Path: `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Colors.kt`
 - Provide exact color constants: DeepTeal, CalmEmerald, OffWhiteBackground, PureWhiteSurface, CharcoalText, MutedRedError.
 - Provide `data class MudawamaColors(...)` and `val LightColors`, `val DarkColors`.
 - Provide helper `primaryDisabled` and `primaryDark` tokens.

 Typography.kt (DS-004)
 - Path: `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Typography.kt`
 - Provide `data class MudawamaTypography` and a `defaultTypography(fontFamily: FontFamily = FontFamily.Default)` factory.
 - Token sizes and weights per spec.

 Shapes.kt (DS-005)
 - Path: `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Shapes.kt`
 - Provide `MudawamaShapes` with small=8.dp, medium=16.dp, large=16.dp and converters to Compose `Shapes`.

 Theme.kt (DS-002 then DS-006 refinement)
 - Path: `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Theme.kt`
 - Initial shell (DS-002): provide `@Composable fun MudawamaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)` mapping inline placeholder tokens to `androidx.compose.material3.MaterialTheme`, and expose `object MudawamaTheme { val colors, typography, shapes }` via CompositionLocals for early usage.
 - Refinement (DS-006): replace placeholders with `MudawamaColors`, `MudawamaTypography`, and `MudawamaShapes`. Map `MudawamaColors` -> `ColorScheme` for Material 3. Provide `LocalMudawamaColors`, `LocalMudawamaTypography`, `LocalMudawamaShapes` composition locals.

 MudawamaPrimaryButton.kt (DS-008)
 - Path: `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/MudawamaPrimaryButton.kt`
 - Public API per spec.
 - Use `androidx.compose.material3.Button` with `ButtonDefaults.buttonColors(containerColor = MudawamaTheme.colors.primary, contentColor = MudawamaTheme.colors.onPrimary)` and `shape = MudawamaTheme.shapes.medium`.
 - Apply content padding (horizontal 16.dp, vertical 12.dp), minHeight 48.dp.
 - Disabled behavior: `enabled = false` maps to `containerColor = MudawamaTheme.colors.primaryDisabled` (or alpha) and adjust content color alpha.

 GhostButton.kt (DS-009)
 - Path: `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/GhostButton.kt`
 - Use `TextButton` for ghost/text mode, `OutlinedButton` for outlined mode, with `colors = ButtonDefaults.textButtonColors(contentColor = MudawamaTheme.colors.primary)` and `border` when outlined.

 SurfaceCard.kt (DS-010)
 - Path: `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/SurfaceCard.kt`
 - Use `androidx.compose.material3.Card` or `Surface` with `shape = MudawamaTheme.shapes.large`, `elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)`, padding 16.dp inside, optional `onClick` for clickability.

 Utilities (DS-007)
 - `utils/Accessibility.kt`: Provide `Modifier.mudawamaButtonSemantics` helper adding `role = Role.Button` and `contentDescription` semantics when appropriate.

 Previews & Samples (DS-011)
 - `samples/Previews.kt`: per-component previews using `@Preview` for light/dark/large text.

 Testing (DS-012)
 - `commonTest` token assertions (hex equality), typography size assertions, shapes radii.
 - Compose UI tests: semantics and click behavior for MudawamaPrimaryButton and GhostButton using Compose test rules (AndroidHost or Compose MPP test frameworks).

 CI & GitHub Actions (DS-014)
 - Minimal pipeline: `assemble` and `commonTest` on Linux; optional macOS job to assemble Android or iOS targets.
 - Provide `designsystem-ci.yml` with jobs `build-jvm` and optional `android-assemble`.

 Branching & PR strategy
 - Branch: `feat/add-kmp-designsystem`
 - Commit structure: scaffold → theme-shell → tokens → theme-wiring → components → tests → docs → CI
 - PR checklist: module added to settings, tokens match spec, MudawamaTheme exists, components have previews, tests pass, README present, QA screenshots attached.

 Acceptance criteria (mapped to tasks/tests)
 - (1) MudawamaTheme present — DS-002 + DS-006; verify via local preview and unit tests.
 - (2) Color tokens match hex — DS-003. Token/unit tests in `commonTest` are DEPRECATED/REMOVED for the MVP; visual verification will be performed via `DesignSystemGallery` and previews.
 - (3) Typography tokens — DS-004; test TypographyTest.buttonWeightAndSize.
 - (4) Shapes tokens — DS-005; test ShapesTest.mediumRadius_is16dp.
 - (5) Components implemented in commonMain — DS-008..DS-010; tests MudawamaPrimaryButtonTest, GhostButtonTest, SurfaceCardTest.
 - (6) Components render correctly — DS-011 + DS-016 manual QA + sample integration DS-013.
 - (7) Accessibility semantics — DS-007 + tests in DS-012.

 Risk assessment & mitigations
 - Compose MPP / Material3 version mismatches: pin to repo plugin version and run early builds (DS-000/DS-001).
 - Material3 API differences: encapsulate mapping in `Theme.kt` to minimize widespread changes.
 - iOS hosting fragility: document interop options and avoid iOS-specific requirements for MVP.
 - Accessibility/contrast failures: run manual checks early and provide alternate tokens if necessary.

 Deliverables (exact file paths)
 - MVP prioritized list (theme-first):
   - `shared/designsystem/build.gradle.kts` (DS-001)
   - `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Theme.kt` (DS-002)
   - `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Colors.kt` (DS-003)
   - `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Typography.kt` (DS-004)
   - `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Shapes.kt` (DS-005)
   - `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Tokens.kt` (DS-006)
   - `shared/designsystem/src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/MudawamaPrimaryButton.kt` (DS-008)
     - Tests: token/unit tests are deprecated and removed per project decision (DS-012 deprecated).
   - `shared/designsystem/README.md` (DS-015)

 Timeboxed schedule (single developer)
 - Total conservative estimate: 3–4 working days (~24–32 hours)
 - Day-by-day plan (theme-first):
   - Day 1: DS-000, DS-001, DS-002 (theme shell)
   - Day 2: DS-003, DS-004, DS-005, start DS-006 (token wiring)
   - Day 3: DS-006 finish, DS-007, DS-008 MudawamaPrimaryButton, DS-009 GhostButton
    - Day 4: DS-010 SurfaceCard, DS-011 Previews, DS-013 Android sample, DS-015 README (DS-012 tests deprecated)

 Try-it commands (local)
 ```bash
 ./gradlew :shared:designsystem:assemble
 ./gradlew :shared:designsystem:commonTest
 ./gradlew :shared:designsystem:check
 ```

 Next recommended action
 - Implement DS-000 and DS-001 to scaffold the module, then implement DS-002 (Theme shell) immediately so component development can begin in parallel.

 End of plan
