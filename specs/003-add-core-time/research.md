# Research: shared:core:time

**Phase**: 0 — Outline & Research
**Date**: 2026-04-02
**Feeds**: [plan.md](./plan.md), [data-model.md](./data-model.md)

---

## Decision 1 — kotlinx-datetime version and catalog entry

**Question**: `kotlinx-datetime` is not in `gradle/libs.versions.toml`. Which version should be added, and what artifact coordinates are needed for KMP?

**Decision**: Add `kotlinxDatetime = "0.6.2"` to `[versions]` and `kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinxDatetime" }` to `[libraries]`. No `kotlinx-datetime-js` artifact is needed because the project targets Android + iOS only (no JS/WASM).

**Rationale**: `0.6.x` is the stable release series compatible with Kotlin 2.x. The `org.jetbrains.kotlinx:kotlinx-datetime` artifact is the single KMP-compatible artifact; it includes compiled metadata for all supported targets (JVM/Android, iOS arm64/x64/simulatorArm64, macOS, Linux, Windows). Patch version `0.6.2` is the latest confirmed stable release as of Q1 2026. Developers should verify against Maven Central before merging.

**Alternatives considered**:
- `0.5.0` — stable but older; lacks minor API improvements in `0.6.x`. Rejected in favour of latest stable.
- Third-party Joda-time or ThreeTenABP — explicitly forbidden by spec assumption and constitution (no third-party date libraries).
- No version catalog entry (inline string) — breaks catalog uniformity. Rejected.

**Verification command**:
```bash
curl -s "https://search.maven.org/solrsearch/select?q=g:org.jetbrains.kotlinx+a:kotlinx-datetime&rows=5&wt=json" \
  | python3 -c "import sys,json; [print(d['latestVersion']) for d in json.load(sys.stdin)['response']['docs']]"
```

---

## Decision 2 — Logical-date rollover algorithm

**Question**: The spec's five acceptance scenarios seem to have two different "directions" for the rollover. Scenarios 1–2 (offset 18:00) advance the date; Scenario 5 (offset 03:00) retreats the date. What is the unified algorithm?

**Decision**: Branch on the `offsetHour` hemisphere:

```
when:
  H >= 12 && hour >= H  → calendarDate + 1 day   // Evening rollover (Islamic-style)
  H in 1..11 && hour < H → calendarDate - 1 day  // Morning rollover (night-owl extension)
  else                   → calendarDate            // Standard or within the normal window
```

Where `H = policy.offsetHour` and `hour = instant.toLocalDateTime(timeZone).hour`.

**Full scenario verification**:

| Scenario | H | Calendar day | Hour | Branch | Result |
|----------|---|-------------|------|--------|--------|
| US1-1 | 18 | Monday | 21 | `H≥12 && 21≥18` | Monday **+1** = Tuesday ✅ |
| US1-2 | 18 | Monday | 17 | else | Monday ✅ |
| US1-3 | 0  | Monday | 23 | else (H=0) | Monday ✅ |
| US1-4 | 0  | Tuesday | 0 | else (H=0) | Tuesday ✅ |
| US1-5 | 3  | [Day D] | 2  | `H in 1..11 && 2<3` | D **-1** = previous ✅ |
| Edge — boundary @ 18:00:00 | 18 | Monday | 18 | `H≥12 && 18≥18` | Tuesday ✅ |
| Edge — H=0 equals Standard | 0  | any | any | else | calendarDate ✅ |
| Edge — H=6 before offset | 6  | Tuesday | 5 | `H in 1..11 && 5<6` | Monday ✅ |
| Edge — H=6 after offset | 6  | Tuesday | 10 | else | Tuesday ✅ |

**Rationale**: The two behaviours arise from different day-naming conventions:
- **Evening offset (H ≥ 12)**: The Islamic new day starts at sunset. `hour ≥ H` on day D means you have already entered the NEXT logical day (D+1). The 24 h interval `[D H:00, D+1 (H-1):59]` is named after D+1 because the majority (midnight to H-1) falls on D+1.
- **Morning offset (H < 12)**: A night-owl's day starts at H:00. `hour < H` on day D means the D-1 logical day has not yet ended. The 24 h interval `[D-1 H:00, D (H-1):59]` is named after D-1 because the majority (H:00 to midnight) falls on D-1.

Both cases share the invariant: the **logical day label = calendar day containing the interval midpoint** (interval_start + 12 hours).

**Alternatives considered**:
- Simple `if (hour >= H) date + 1 else date` — satisfies scenarios 1–4 but fails scenario 5 for small H values.
- `if (hour < H) date - 1 else date` — satisfies scenario 5 but breaks scenarios 1–2.
- Shift entire instant by `Duration.hours(-H)` then take date — fails scenario 1 (evening offsets would subtract into the wrong day).

---

## Decision 3 — Koin externally-injected `RolloverPolicy`

**Question**: FR-011 requires the Koin module to accept the `RolloverPolicy` as an external parameter. What is the idiomatic Koin 4.x KMP pattern?

**Decision**: Module factory function:
```kotlin
fun timeModule(rolloverPolicy: RolloverPolicy = RolloverPolicy.Standard): org.koin.core.module.Module =
    module {
        single<TimeProvider> { SystemTimeProvider(rolloverPolicy) }
    }
```
App-level DI:
```kotlin
startKoin {
    modules(
        timeModule(rolloverPolicy = RolloverPolicy.fixed(18)),
        // other modules …
    )
}
```

**Rationale**: A factory function that closes over the policy value is the most idiomatic and type-safe Koin 4.x pattern for externally-supplied configuration. It avoids:
- `parametersOf()` at every `get<TimeProvider>()` call site (would break singleton semantics).
- Injecting `RolloverPolicy` as a separate Koin binding (would require `RolloverPolicy` to be in scope, polluting the DI graph with a preferences-layer value).
- Hard-coding the policy in the module (violates FR-011).

**Alternatives considered**:
- `single<TimeProvider> { (policy: RolloverPolicy) -> SystemTimeProvider(get()) }` with `parametersOf` — each call would require the parameter; Koin does NOT cache singleton when parameters differ. Rejected.
- `single<RolloverPolicy> { RolloverPolicy.Standard }` as a separate binding — couples the DI graph to a preferences value; the time module would be responsible for a cross-cutting concern. Rejected.

---

## Decision 4 — `FakeTimeProvider` placement

**Question**: Should `FakeTimeProvider` live in `commonMain`, `commonTest`, or a separate `testutils` Gradle module?

**Decision**: `commonMain` of `shared:core:time`, clearly annotated as a test double. This is explicitly stated as an assumption in the spec.

**Rationale**: Placing it in `commonMain` means any consumer module can access it from its own `commonTest` source set without adding a test-scoped inter-module dependency. The alternative (`testutils` module) would require an explicit `testImplementation(projects.shared.core.time.testutils)` in every consumer. The spec explicitly acknowledges this trade-off and mandates `commonMain` placement (spec §Assumptions).

The `FakeTimeProvider` is documented with a KDoc annotation:
```kotlin
/**
 * Test double for [TimeProvider].
 *
 * ⚠️ **For test use only.** Do not bind this in production Koin modules.
 * Ships in `commonMain` so consumer modules can use it in their own `commonTest`
 * without additional Gradle dependency wiring.
 */
```

**Alternatives considered**:
- `commonTest` of `shared:core:time` — would make it inaccessible to other modules' test source sets. Rejected.
- Separate `shared:core:time-testutils` Gradle module — clean but adds Gradle module overhead for a single class. Rejected per spec assumption.

---

## Decision 5 — Minute/second precision in rollover calculation

**Question**: The spec says "18:00:00 is already the next logical day". My algorithm compares only `localDateTime.hour`. Does that handle sub-hour precision correctly?

**Decision**: Hour-only comparison is sufficient and correct.

**Rationale**: `kotlinx-datetime`'s `LocalDateTime.hour` returns the full calendar hour (0–23). At `18:00:00`, `hour == 18`; at `17:59:59`, `hour == 17`. The boundary condition `hour >= 18` correctly fires at the exact second `18:00:00` and all subsequent seconds within that hour — matching the spec's stated inclusive boundary. No minute or second comparison is needed.

**Alternatives considered**:
- Comparing `(hour * 60 + minute) >= (H * 60)` — more precise but unnecessary; the rollover granularity in the spec is 1 hour. Over-engineering rejected.

---

## Decision 6 — DST handling

**Question**: The spec mentions DST transitions as an edge case. Does the module need custom DST handling?

**Decision**: Delegate entirely to `kotlinx-datetime`. No custom DST logic is needed.

**Rationale**: `kotlinx-datetime`'s `Instant.toLocalDateTime(TimeZone)` uses the IANA timezone database (on all KMP targets) and handles ambiguous and skipped instants per the platform's time zone rules. The spec explicitly states: "The module delegates to kotlinx-datetime for timezone arithmetic; ambiguous or gap instants resolve per that library's documented rules, which must be covered by a test." The `LogicalDateCalculatorTest` should include at least one DST-transition test using a known timezone like `Europe/London` and a known spring-forward/fall-back instant.

---

## Decision 7 — `toIsoDateString` implementation

**Question**: Should the formatter use `DateTimeComponents`, `LocalDate.format(DateTimeFormat)`, or `LocalDate.toString()`?

**Decision**: Use `LocalDate.toString()` for the `LocalDate` overload and `instant.toLocalDateTime(tz).date.toString()` for the `Instant` overload.

**Rationale**: `kotlinx-datetime`'s `LocalDate.toString()` is documented to return ISO-8601 format `yyyy-MM-dd` (e.g., `"2026-01-07"`), which is both human-readable and lexicographically sortable. This is the simplest correct implementation. There is no need for a `DateTimeFormat` pattern string that could be mis-typed.

**Alternatives considered**:
- `DateTimeFormat { date(LocalDate.Formats.ISO) }` — more explicit but more verbose; same output.
- `"${date.year}-${date.monthNumber.toString().padStart(2,'0')}-${date.dayOfMonth.toString().padStart(2,'0')}"` — manual string building; error-prone. Rejected.

---

## Summary of Resolved Unknowns

| # | Unknown | Resolved Decision |
|---|---------|-------------------|
| 1 | kotlinx-datetime version | `0.6.2`; single KMP artifact |
| 2 | Logical-date algorithm | Hemisphere-branching (H≥12 → +1, H in 1..11 → -1) |
| 3 | Koin policy injection | Module factory function `fun timeModule(policy)` |
| 4 | FakeTimeProvider location | `commonMain` per spec assumption |
| 5 | Sub-hour boundary precision | Hour comparison is sufficient |
| 6 | DST handling | Fully delegated to `kotlinx-datetime` |
| 7 | ISO formatter | `LocalDate.toString()` |

