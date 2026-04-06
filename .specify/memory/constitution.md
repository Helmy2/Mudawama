<!--
Sync Impact Report

- Version change: 1.2.0 -> 1.3.0
- Modified principles: 2026-04-06 amendment (v1.2.0)
  - Added: UI Fidelity & String Resources principle
    (no hardcoded strings; all text via stringResource; UI MUST match docs/ui/ references)
- Modified principles: 2026-04-06 amendment (v1.3.0)
  - Added: String Resource Consolidation rule
    (single strings.xml in shared/designsystem; packageOfResClass convention documented)
- Modified principles: 2026-04-06 amendment (v1.3.1)
  - Added: surfaceVariant token rule and dark-mode color constants rule
    (MudawamaColors now has surfaceVariant; icon chips must use surfaceVariant not surface;
     dark-mode Color values must be named constants in Colors.kt, not inline literals in Theme.kt)
- Runtime docs updated: docs/ARCHITECTURE.md, docs/DESIGN.md
-->

# Mudawama Constitution

## Core Principles

### Architectural Boundaries (Clean Architecture & Multi-Module)

Packaging-by-feature with a strict three-layer split for each feature:
`:domain`, `:data`, `:presentation`. Shared modules are `shared:core`
and `shared:designsystem`.

- Domain Layer MUST be 100% pure Kotlin: NO Android SDK, NO iOS
  frameworks, NO Ktor clients, NO Room/SQLDelight access, NO Compose UI
  imports.
- Presentation Layer MUST use Jetpack Compose Multiplatform only.
  NO Android XML layouts, Context references, or UIKit/SwiftUI.
- Dependency direction MUST be: `:presentation -> :domain <- :data`.
  Feature modules MUST NOT depend on other feature modules. Allowed
  cross-feature dependencies are only `shared:core` and
  `shared:designsystem`.

Rationale

This separation keeps business logic portable, testable, and platform
agnostic. A strict dependency graph prevents accidental coupling between
features and preserves a single source of truth for business rules.

### Error Handling & State Management

- Railway-Oriented Programming: Domain and Presentation layers MUST NOT
  throw or catch exceptions as part of their public APIs. UseCases and
  Repositories MUST return a custom generic wrapper: `Result<D, E>`
  (where `D` is data and `E` is a specific `DataError` interface). Do NOT use the standard `kotlin.Result` as it forces Throwables.
- Safe Boundary: All database and network calls in the Data layer MUST be
  wrapped in a `safeCall { }` extension which catches standard Exceptions (e.g. `SQLiteException`, `IOException`) and converts them into
  `DataError` enum values.
- MVI Architecture: Presentation MUST follow an Orbit-style MVI:
    - immutable State data class
    - Action sealed interface / sealed class for user intents
    - Event sealed interface / sealed class for one-shot side effects

Rationale

Explicit error channels and immutable states make flows deterministic
and easier to test. Mapping all throwable errors at the Data boundary
keeps domain logic free of plumbing concerns.

### Build System & Convention Plugins

Convention plugins in `build-logic` MUST be single-responsibility: they only apply and configure Gradle plugins (toolchain, compiler plugins, resource packaging). Plugins MUST NOT inject library dependencies via `implementation(...)` or `api(...)`.

The sole permitted exception is `mudawama.kmp.koin`: a dependency-shorthand plugin that injects `koin.bom` (platform), `bundles.koin`, and `koin.android`. This exception is justified because all three declarations always travel together across 8+ KMP modules and never appear independently.

- `androidApp` and other Android-only modules are not KMP modules and therefore cannot use `mudawama.kmp.koin`; they MUST declare Koin dependencies inline.
- When a convention plugin is used in only one module, its config MUST be inlined directly into that module's `build.gradle.kts` instead.

Rationale

Keeping plugins free of hidden dependency injection makes the dependency graph explicit and auditable. A reviewer reading any `build.gradle.kts` sees the complete picture without having to trace through convention plugin source code.

- UI: Jetpack Compose Multiplatform.
- Database: androidx.room (Room for Kotlin Multiplatform). NO SQLDelight.
- Network: io.ktor:ktor-client-core. NO Retrofit.
- DI: io.insert-koin:koin-core for KMP. NO Dagger/Hilt.
- Coroutines: ALWAYS inject CoroutineDispatcher; DO NOT hardcode
  Dispatchers.IO or Dispatchers.Main.

Rationale

Using an explicit, constrained stack enforces cross-platform portability
and makes reviews and CI automation straightforward.

### Code Quality & Style

- Code MUST target idiomatic Kotlin 2.x styles: small focused
  components, composition over inheritance, and clear separation of
  concerns.
- UI components MUST provide a Preview wrapper and separate state
  hoisting from stateless UI functions.
- Strings and icons MUST be retrieved via
  `org.jetbrains.compose.resources.stringResource` or equivalent
  resource accessors; NO hardcoded user-facing strings in UI code.

Rationale

Modern Kotlin idioms and resource-driven UIs improve maintainability and
localization support.

### UI Fidelity & String Resources

Every screen, bottom sheet, and component MUST match the reference
designs in `docs/ui/`. The reference set is the single source of truth
for layout, labeling, and copy.

- ALL user-visible strings MUST be declared in
  `shared/designsystem/src/commonMain/composeResources/values/strings.xml`
  and accessed exclusively via `stringResource(Res.string.*)`.
  Hardcoded string literals in any `@Composable` function are a
  build-blocking violation.
- String keys MUST follow `snake_case` and be scoped by screen/component
  (e.g., `home_next_prayer_label`, `habits_add_new_habit_button`,
  `quran_log_reading_title`).
- Placeholder / format strings MUST use `%1$s` / `%1$d` positional
  arguments (Compose Resources format), never string concatenation.
- Icons and drawables MUST be declared as Compose Resources
  (`Res.drawable.*`) — no hardcoded `R.drawable` Android references in
  `commonMain` code.

#### String Resource Consolidation (v1.3.0)

There is exactly **one** `strings.xml` in the entire project:
`shared/designsystem/src/commonMain/composeResources/values/strings.xml`.
Feature modules MUST NOT create their own `strings.xml` or
`composeResources/values/` directory. All strings — navigation labels,
screen titles, section headers, button labels, error messages, content
descriptions — belong in this single file.

**Import path rule.** The `mudawama.kmp.compose` convention plugin
derives the generated resource class package from the Gradle module path:

```
packageOfResClass = "mudawama." + gradlePath.trimStart(':').replace(':', '.')
```

For `shared/designsystem` (Gradle path `:shared:designsystem`) this
produces:

```kotlin
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.<string_key>
```

This is the **only** `Res` import that should ever appear in any `.kt`
file that references string resources. The legacy pattern
`io.github.helmy2.mudawama.designsystem.generated.resources.Res` is
wrong and will not compile. Using a `as DsRes` alias is also forbidden —
there is only one `Res`, so no alias is needed.

**`publicResClass = true` requirement.** By default the Compose Resources
plugin generates `Res` as `internal`. Because `shared/designsystem`'s
`Res` is imported by other modules, its `build.gradle.kts` MUST declare:

```kotlin
compose.resources {
    publicResClass = true
}
```

Any module whose generated `Res` is consumed outside its own compilation
unit MUST set `publicResClass = true`. Modules that keep resources
private (i.e., not shared outward) may omit this setting.

#### Design System Color Tokens (v1.3.1)

`MudawamaColors` has the following semantic slots. ALL slots must be
populated in both `LightMudawamaColors` and `DarkMudawamaColors`:

- `primary` — CTAs, progress rings, active icons
- `onPrimary` — text/icons on primary-filled surfaces
- `background` — deepest page layer
- `surface` — cards and sheets
- `surfaceVariant` — icon chips, secondary containers (must be visually distinct from `surface`)
- `onSurface` — body text, secondary icons
- `error` — destructive / validation states

**`surfaceVariant` rule.** Icon chip backgrounds MUST use
`MudawamaTheme.colors.surfaceVariant`, not `surface`. On dark themes,
`surface` and the card background are nearly identical; using `surface`
makes chips invisible. Icon tint inside chips MUST use `primary`.

**Dark-mode color constants rule.** Every dark-mode `Color(0xFF…)` value
MUST be a named constant declared in
`shared/designsystem/…/designsystem/Colors.kt`. Inline `Color(0xFF…)`
literals inside `Theme.kt` for dark values are forbidden — they are
invisible to code search and impossible to audit.

Reference UI screens (canonical filenames in `docs/ui/`):

| File | Screen |
|---|---|
| `welcome_to_mudawama.png` | Onboarding / Welcome |
| `home_dashboard.png` | Home — daily overview + next prayer card |
| `daily_habits.png` | Daily Habits list (core rituals + personal) |
| `daily_prayer_tracker.png` | Today's Prayers detail |
| `quran_daily_reading_tracker.png` | Quran Reading tracker |
| `daily_athkar_tracker.png` | Daily Athkar overview |
| `morning_athkar_reading.png` | Morning Athkar session (tap-to-count) |
| `post_prayer_athkar.png` | Post-Prayer Athkar checklist |
| `tasbeeh_counter.png` | Tasbeeh Counter |
| `insights_progress.png` | Insights / Progress |
| `settings.png` | Settings |
| `new_habit_bottom_sheet.png` | New Habit bottom sheet |
| `manage_habit_bottom_sheet.png` | Manage Habit bottom sheet |
| `log_reading_bottom_sheet.png` | Log Reading bottom sheet |
| `quran_reading_goal_bottom_sheet.png` | Daily Quran Goal bottom sheet |
| `select_surah_ayah_bottom_sheet.png` | Update Position (Surah/Ayah) bottom sheet |
| `tasbeeh_goal_bottom_sheet.png` | Daily Tasbeeh Goal bottom sheet |

Rationale

Anchoring every string to a resource file guarantees full Arabic
localization support (a core product value) without touching Kotlin
source. Binding implementation to the reference UI images prevents
design drift and gives reviewers an unambiguous acceptance criterion.

## Additional Constraints (Implementation Details)

- UseCases and Repository interfaces live in `:domain` and use only
  primitive Kotlin types and project-defined domain models.
- Data implementations live in `:data` and may depend on platform
  libraries (Room, Ktor) but must map results to domain models and
  `Result<D, E>` before returning to domain.
- Presentation lives in `:presentation` and exposes a single State and
  Action interface to the UI. ViewModels expose flows or state holders
  representing immutable State and emit Events for one-shots.

## Governance

- Amendment procedure: Changes MUST be proposed through a Pull Request
  with a clear rationale and the required version bump. The PR MUST be
  approved by at least one maintainer listed in the CODEOWNERS file.
- Versioning policy: Use semantic versioning for the constitution itself
  (MAJOR.MINOR.PATCH) and update `CONSTITUTION_VERSION` and
  `LAST_AMENDED_DATE` when merging amendments.
    - MAJOR: Backward-incompatible principle removal or redefinition.
    - MINOR: New principle or material expansion of guidance.
    - PATCH: Wording clarifications, typos, or small non-semantic edits.
- Compliance review: A periodic (suggest quarterly) audit SHOULD be
  scheduled to ensure the codebase and templates continue to match the
  constitution.

## Enforcement Checklist (for reviewers and CI)

- Repository layout: Ensure each feature has `domain`, `data`, and
  `presentation` submodules where applicable.
- Domain layer:
    - Confirm no forbidden imports (android.*, io.ktor.*, androidx.compose.*, androidx.room.*).
    - Confirm interfaces return `Result<D, E>` and contain no platform APIs.
- Data layer:
    - Confirm network/db calls are wrapped in `safeCall { }`.
    - Confirm only allowed libraries (Ktor, Room) are used for
      implementations.
- Presentation layer:
    - Confirm ViewModels implement MVI shapes (State, Action, Event)
      and do not throw exceptions.
    - Confirm zero hardcoded user-facing string literals in `@Composable`
      functions (grep for `Text("` with a literal argument).
- Cross-cutting:
    - Confirm DI uses Koin only; no Dagger/Hilt imports.
    - Confirm CoroutineDispatcher is injected and no direct calls to
      Dispatchers.IO/Main are present.

## Machine-readable forbidden import regexes

- Domain forbidden import regexes (applied to files in domain modules):

    - ^import\s+android\.
    - ^import\s+androidx\.
    - ^import\s+io\.ktor\.
    - ^import\s+androidx\.room\.
    - ^import\s+UIKit\b
    - ^import\s+SwiftUI\b

- Presentation forbidden import regexes (applied to shared presentation code):

    - ^import\s+android\.content\.Context
    - ^import\s+android\.view\.
    - ^import\s+SwiftUI\b
    - ^import\s+UIKit\b
    - ^import\s+com\.squareup\.retrofit2\.

- Data forbidden import regexes (disallowed libraries in data implementations):

    - ^import\s+com\.squareup\.retrofit2\.
    - ^import\s+com\.squareup\.sqldelight\.

- Global forbidden patterns:

    - ^import\s+com\.google\.dagger\.
    - ^import\s+dagger\.
    - Dispatchers\.IO
    - Dispatchers\.Main

- Presentation hardcoded-string pattern (applied to commonMain Composables):

    - Text\(\s*"[^"R][^"]*"\s*[,)]

- Wrong Res import pattern (applies to all modules):

    - ^import\s+io\.github\.helmy2\.mudawama\..*\.generated\.resources\.
    - ^import\s+mudawama\.(feature|shared\.(?!designsystem))\S+\.Res

## CI Script Snippet (suggested `scripts/check_constitution.sh`)

```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel)"
echo "Running Mudawama constitution checks in $ROOT_DIR"

fail=0

check() {
  local pattern="$1"; shift
  local path="$1"; shift
  if grep -REn --line-number --exclude-dir=build --exclude-dir=.gradle -e "$pattern" "$path"; then
    echo "-- Violation found for pattern: $pattern in $path" >&2
    fail=1
  fi
}

# Scan shared code for domain violations
check "^import\\s+android\\." "feature/*/domain/" || true
check "^import\\s+io\\.ktor\\." "feature/*/domain/" || true
check "^import\\s+androidx\\.room\\." "feature/*/domain/" || true
check "^import\\s+androidx\\.compose\\." "feature/*/domain/" || true

# Presentation forbidden imports
check "^import\\s+android\\.content\\.Context" "feature/*/presentation/" || true
check "^import\\s+SwiftUI" "feature/*/presentation/" || true
check "^import\\s+UIKit" "feature/*/presentation/" || true

# Data forbidden libraries
check "^import\\s+com\\.squareup\\.retrofit2" "feature/*/data/" || true
check "^import\\s+com\\.squareup\\.sqldelight" "feature/*/data/" || true

# DI and dispatcher forbidden usages
check "com\\.google\\.dagger" "." || true
check "Dispatchers\\\\.IO|Dispatchers\\\\.Main" "." || true

# Hardcoded strings in Composables (commonMain)
check 'Text(\s*"[^"R][^"]*"\s*[,)]' "shared/" || true
check 'Text(\s*"[^"R][^"]*"\s*[,)]' "feature/" || true

if [ "$fail" -ne 0 ]; then
  echo "Constitution checks detected violations" >&2
  exit 2
fi

echo "Constitution checks passed (no obvious violations)"
```

## Appendix: Examples

Domain - allowed example

```kotlin
// package feature.habits.domain

// import io.github.helmy2.mudawama.core.domain.Result
// import io.github.helmy2.mudawama.core.domain.DataError
// import kotlinx.coroutines.CoroutineDispatcher

interface HabitRepository {
  suspend fun getHabits(): Result<List<Habit>, DataError>
}

class GetHabitsUseCase(private val repo: HabitRepository,
                       private val dispatcher: CoroutineDispatcher) {
  suspend operator fun invoke() = repo.getHabits()
}
```

Domain - forbidden example (platform/API leakage)

```kotlin
// FORBIDDEN in domain
// import android.content.Context
// import androidx.room.Room
```

Presentation - allowed example (Compose MPP)

```kotlin
// import androidx.compose.runtime.Composable
// import org.jetbrains.compose.ui.tooling.preview.Preview

data class HabitState(val items: List<Habit> = emptyList())

@Composable
fun HabitScreen(state: HabitState, onAction: (HabitAction) -> Unit) {
  // Stateless UI
}

@Preview
@Composable
fun HabitScreenPreview() {
  HabitScreen(state = HabitState()) {}
}
```

Presentation - forbidden example

```kotlin
// FORBIDDEN in shared presentation
// import android.view.View
// import android.content.Context
```

Data - allowed example (Ktor + safeCall)

```kotlin
suspend fun getRemoteHabits(): Result<List<HabitDto>, DataError.Remote> =
  safeCall {
    client.get("/habits").body()
  }
```

Data - forbidden example

```kotlin
// FORBIDDEN: using Retrofit or SQLDelight
// import com.squareup.retrofit2.Retrofit
// import com.squareup.sqldelight.db.SqlDriver
```

## Revision information

- CONSTITUTION_VERSION: 1.3.1
- RATIFICATION_DATE: 2026-03-21
- LAST_AMENDED_DATE: 2026-04-06