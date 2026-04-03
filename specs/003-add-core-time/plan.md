# Implementation Plan: shared:core:time — Centralised Time & Logical Date Module

**Branch**: `003-add-core-time` | **Date**: 2026-04-02 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/003-add-core-time/spec.md`

---

## Summary

Add `shared:core:time` — a pure-Kotlin Multiplatform module (100 % `commonMain`) that
provides a single, injectable `TimeProvider` abstraction for the current `Instant` and a
configurable *logical date* based on a `RolloverPolicy`. The policy allows Islamic-style
evening rollovers (e.g., 18:00 → next calendar day becomes "today") as well as night-owl
morning offsets. All time arithmetic is delegated to `kotlinx-datetime 0.6.2`; no
platform-specific source sets are created. The module exposes a `FakeTimeProvider` in
`commonMain` for deterministic testing in consumer modules, and wires `TimeProvider` as a
Koin singleton via a factory function that accepts the policy externally (FR-011).

---

## Technical Context

**Language/Version**: Kotlin 2.3.20 (KMP — `commonMain` only)  
**Primary Dependencies**: `org.jetbrains.kotlinx:kotlinx-datetime:0.6.2`, `io.insert-koin:koin-core` (via BOM `4.2.0`)  
**Storage**: N/A — no persistence in this module  
**Testing**: Kotlin Multiplatform test framework (`kotlin.test`); runs on Android JVM and iOS Simulator  
**Target Platform**: Android (minSdk 30) + iOS (arm64, x64, simulatorArm64) — shared `commonMain`  
**Project Type**: KMP shared library module (`mudawama.kmp.library` convention plugin)  
**Performance Goals**: Koin resolves `TimeProvider` in < 50 ms (SC-003); full test suite < 5 s (SC-001)  
**Constraints**: Zero system-clock calls outside `SystemTimeProvider`; no third-party date libraries; no platform source sets (FR-009)  
**Scale/Scope**: ~6 source files, ~3 test files, < 300 lines of production code

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| # | Rule | Status | Notes |
|---|------|--------|-------|
| 1 | Domain layer — no `android.*`, `androidx.*`, `io.ktor.*`, `UIKit`, `SwiftUI` imports | ✅ PASS | 100 % `commonMain`; only `kotlinx-datetime` and `koin-core` |
| 2 | DI uses Koin only — no Dagger/Hilt | ✅ PASS | `koin-core` via explicit dep; no Dagger anywhere |
| 3 | `CoroutineDispatcher` injection — no hardcoded `Dispatchers.IO/.Main` | ✅ PASS | Module is synchronous; `TimeProvider` methods are non-suspending |
| 4 | Dependency direction — no feature→feature imports | ✅ PASS | `shared:core:time` is a leaf module with no inter-feature deps |
| 5 | No SQLDelight, no Retrofit | ✅ PASS | No network or database access |
| 6 | No platform-specific source sets | ✅ PASS | FR-009 mandates `commonMain` only; convention plugin adds Android/iOS targets transparently |
| 7 | Feature modules `:domain/:data/:presentation` split | ✅ N/A | `shared:core:*` modules are flat utilities; split applies to feature modules only |
| 8 | Code style — idiomatic Kotlin 2.x, composition over inheritance | ✅ PASS | `data class` for `RolloverPolicy`, `interface` for `TimeProvider` |

**Post-design re-check** (after Phase 1): All checks remain green. `FakeTimeProvider` in
`commonMain` is an intentional spec-mandated exception documented in KDoc (spec §Assumptions, Decision 4 in research.md).

---

## Project Structure

### Documentation (this feature)

```text
specs/003-add-core-time/
├── plan.md              # This file
├── research.md          # Phase 0 output (already present)
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── time-provider-api.md   # Phase 1 output — public library API contract
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```text
shared/core/time/
├── build.gradle.kts
└── src/
    ├── commonMain/
    │   └── kotlin/io/github/helmy2/mudawama/core/time/
    │       ├── RolloverPolicy.kt          # Value type; offsetHour 0–23; serialisable to Int
    │       ├── TimeProvider.kt            # Interface: nowInstant(), logicalDate(TimeZone)
    │       ├── SystemTimeProvider.kt      # Real impl — delegates to Clock.System
    │       ├── FakeTimeProvider.kt        # ⚠️ Test double — ships in commonMain intentionally
    │       ├── DateFormatters.kt          # toIsoDateString(LocalDate) / toIsoDateString(Instant,TZ)
    │       └── di/
    │           └── TimeModule.kt          # fun timeModule(policy): Module — Koin factory fn
    └── commonTest/
        └── kotlin/io/github/helmy2/mudawama/core/time/
            ├── LogicalDateCalculatorTest.kt  # Covers all rollover scenarios + DST edge case
            ├── DateFormattersTest.kt          # ISO string formatting assertions
            └── TimeModuleTest.kt              # Koin resolution smoke test
```

**Also required (Gradle wiring)**:

```text
gradle/libs.versions.toml               # Add kotlinxDatetime = "0.6.2" + library entry
settings.gradle.kts                     # include(":shared:core:time")
shared/umbrella-core/build.gradle.kts   # api(projects.shared.core.time)
```

**Structure Decision**: Single-project KMP library module following the existing
`shared:core:*` flat-module pattern (identical to `shared:core:domain`). No
`androidMain`/`iosMain` source sets because FR-009 mandates `commonMain` only and
`kotlinx-datetime` provides a unified KMP API across all targets.

---

## Complexity Tracking

> No constitution violations — section left intentionally empty.
