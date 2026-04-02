
# tasks.md — Add KMP shared design system (dependency-ordered, theme-first)

Notes:
- Package: `io.github.helmy2.mudawama.designsystem`
- Compose API: `compose.material3` (Compose Multiplatform)
- Module path: `shared/designsystem` (module id: `:shared:designsystem`)
- Owners are placeholders (e.g., `@owner`). Replace with team names.

PHASE 1 — Setup

- [ ] DS-000 Owner: @owner | Effort: 1h | Depends: none — Validate repository Kotlin/Compose MPP versions, confirm org.jetbrains.compose plugin version and Gradle Kotlin DSL conventions; record findings. Outputs: N/A
- [ ] DS-001 Owner: @owner | Effort: 3h | Depends: DS-000 — Scaffold module `shared/designsystem`, add to `settings.gradle.kts` (if required), create `build.gradle.kts` with Kotlin MPP targets (android, iosX64, iosArm64, iosSimulatorArm64) and `org.jetbrains.compose` plugin; create `src/commonMain` & `src/commonTest` layouts. Outputs: `shared/designsystem/build.gradle.kts`, `settings.gradle.kts` (updated), `shared/designsystem/src/commonMain/` (skeleton)

PHASE 2 — Foundational (theme-first)

- [ ] DS-002 Owner: @owner | Effort: 4h | Depends: DS-001 — Theme-first shell: Implement `MudawamaTheme` shell in `Theme.kt` that wires Compose Material 3 `MaterialTheme`, exposes a `MudawamaTheme` accessor object and CompositionLocals. Use placeholder token values so components and previews can be implemented in parallel. (MVP) Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Theme.kt`

PHASE 3 — Tokens (can run in parallel after DS-001; DS-002 shell enables earlier component work)

- [ ] DS-003 [P] Owner: @owner | Effort: 2h | Depends: DS-001 — Implement `Colors.kt` with exact hex color constants and `MudawamaColors` light/dark palettes, include `primaryDisabled` and `primaryDark` helpers and conversion helpers for Material 3 `ColorScheme`. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Colors.kt`
- [ ] DS-004 [P] Owner: @owner | Effort: 2h | Depends: DS-001 — Implement `Typography.kt` with `MudawamaTypography` tokens (h1..h6, body1/2, caption, button) using `TextStyle`/`sp` and `FontFamily.Default`. Provide `defaultTypography()` factory. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Typography.kt`
- [ ] DS-005 [P] Owner: @owner | Effort: 1.5h | Depends: DS-001 — Implement `Shapes.kt` with `MudawamaShapes` (small=8.dp, medium=16.dp, large=16.dp) and conversion helpers to Compose Shapes. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Shapes.kt`
- [ ] DS-006 Owner: @owner | Effort: 1.5h | Depends: DS-002, DS-003, DS-004, DS-005 — `Tokens.kt` and Theme refinement: replace `Theme.kt` placeholders with concrete tokens, implement `Tokens.kt` helpers, and wire tokens into Material 3 `ColorScheme`/`Typography`/`Shapes`. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/Tokens.kt`, updated `Theme.kt`


PHASE 4 — Utilities & Components (component implementation can begin after DS-002; token wiring (DS-006) improves fidelity)

- [ ] DS-007 Owner: @owner | Effort: 2h | Depends: DS-002 — Implement `utils/Accessibility.kt` and optional `utils/Platform.kt` with common semantics helpers and expect/actual platform helpers. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/utils/Accessibility.kt`, `.../utils/Platform.kt`
- [ ] DS-008 [P] Owner: @owner | Effort: 5h | Depends: DS-002, DS-007 — Implement `MudawamaPrimaryButton` in `MudawamaPrimaryButton.kt` following API and visual specs (padding, min size 48dp, shape medium, colors from `MudawamaTheme`, disabled state, semantics). Provide composable previews. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/MudawamaPrimaryButton.kt`, preview in `samples/Previews.kt`
- [ ] DS-009 [P] Owner: @owner | Effort: 4h | Depends: DS-002, DS-007 — Implement `MudawamaGhostButton` in `MudawamaGhostButton.kt` (outlined and text variants, shape small/medium, semantics). Provide composable previews. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/MudawamaGhostButton.kt`, preview in `samples/Previews.kt`
- [ ] DS-010 [P] Owner: @owner | Effort: 3.5h | Depends: DS-002 — Implement `MudawamaSurfaceCard` in `MudawamaSurfaceCard.kt` (shape large, padding 16dp, optional onClick, elevation). Provide composable previews. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/components/MudawamaSurfaceCard.kt`, preview in `samples/Previews.kt`

PHASE 5 — Samples, Tests, CI, Docs

- [ ] DS-011 Owner: @owner | Effort: 3h | Depends: DS-008, DS-009, DS-010 — Add previews and sample screens demonstrating components (light/dark/large text), and add a `DesignSystemGallery` composable under `samples/` that showcases tokens and all components. Outputs: `src/commonMain/kotlin/io/github/helmy2/mudawama/designsystem/samples/Previews.kt`, `samples/DesignSystemGallery.kt`
- [ ] DS-012 Owner: @owner | Effort: 0h | Depends: DS-003, DS-004, DS-005, DS-008 — DEPRECATED — Unit tests in `commonTest` (token/unit tests) are intentionally omitted per project decision for MVP. No token/unit tests required for initial landing. (Documented: Tests are deprecated; add tests later if policy changes.)
- [ ] DS-013 Owner: @owner | Effort: 3h | Depends: DS-001, DS-002, DS-011 — Android integration sample: integrate `DesignSystemGallery` into `androidApp`'s `App.kt` (or sample activity) and add integration notes in README. Outputs: `androidApp/src/main/.../App.kt` (update to reference DesignSystemGallery), README snippet
- [ ] DS-014 Owner: @owner | Effort: 2.5h | Depends: DS-001 — Add CI workflow to build the KMP module and run common checks (GitHub Actions). Outputs: `.github/workflows/designsystem.yml`
- [ ] DS-015 Owner: @owner | Effort: 2h | Depends: DS-011, DS-013 — Create `README.md` for the module with quickstart, API summary, integration steps, and sample usages. Outputs: `shared/designsystem/README.md`
- [ ] DS-016 Owner: @owner / QA | Effort: 4h | Depends: DS-011, DS-013 — Final QA: visual review, contrast checks (WCAG AA guidance), accessibility (TalkBack/VoiceOver), and cross-device verification. Outputs: QA checklist and issue backlog items

DEPENDENCIES (graph)
 - DS-000 → DS-001 → DS-002 → {DS-003, DS-004, DS-005} → DS-006 → DS-007 → {DS-008, DS-009, DS-010} → DS-011 → DS-013 → DS-014 → DS-015 → DS-016

PARALLEL EXECUTION EXAMPLES
- After DS-001: DS-003, DS-004, DS-005 can run in parallel to define tokens.
- After DS-002 (Theme shell): component implementation (DS-008, DS-009, DS-010) can begin in parallel even before DS-006 completes — components should consume the Theme shell placeholders and be updated once DS-006 is merged.
- After DS-006 + DS-007: components DS-008/DS-009/DS-010 should be wired to real tokens; DS-011 previews can be created in parallel to component polishing.

IMPLEMENTATION STRATEGY
- MVP first: deliver a minimal, usable theme and MudawamaPrimaryButton so app teams can adopt the look-and-feel:
  - MVP tasks: DS-001, DS-002 (Theme shell), DS-003, DS-004, DS-005, DS-006 (token wiring), DS-008 (MudawamaPrimaryButton), DS-012 (basic tests), DS-015 (README). These tasks are marked as the minimum viable set for a safe initial rollout.
- Iterative delivery:
  1. Scaffold module (DS-001) and land Theme shell (DS-002) quickly to unblock parallel work.
 2. Implement tokens (DS-003/DS-004/DS-005) in parallel; wire them (DS-006).
 3. Implement Accessibility utils (DS-007) then components in parallel (DS-008/DS-009/DS-010).
 4. Add previews and tests (DS-011/DS-012), integrate into app (DS-013), and add CI/docs (DS-014/DS-015).
 5. Run final QA (DS-016).

OUTPUT SUMMARY
- Total tasks: 17 (DS-000 .. DS-016)
 - MVP tasks: DS-001, DS-002, DS-003, DS-004, DS-005, DS-006, DS-008, DS-015

FORMAT VALIDATION
- All tasks are listed as checklist items with Task IDs DS-000..DS-016. Each includes Owner placeholder, Effort estimate, explicit Dependencies, and Outputs where applicable. Parallelizable tasks are marked with [P].

