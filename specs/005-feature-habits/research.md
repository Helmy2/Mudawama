# Research: feature:habits

**Phase**: 0 — Outline & Research
**Date**: 2026-04-05
**Feeds**: [plan.md](./plan.md), [data-model.md](./data-model.md)

---

## Decision 1 — UUID generation in `commonMain` (no external dependency)

**Question**: `Habit.id` and `HabitLog.id` are `String` UUIDs set by the domain layer. No UUID library is
declared in `libs.versions.toml`. What is the correct KMP approach that works in `commonMain` without adding
a new dependency?

**Decision**: Use `kotlin.uuid.Uuid.random().toString()` from the Kotlin standard library (Kotlin 2.0+),
decorated with `@OptIn(ExperimentalUuidApi::class)`. The project already targets Kotlin 2.3.20 so this API
is available in all KMP targets (`commonMain`, Android, iOS arm64/x64/simulatorArm64).

```kotlin
@OptIn(ExperimentalUuidApi::class)
internal fun generateId(): String = Uuid.random().toString()
```

Placed in a `domain/util/IdGenerator.kt` file in `feature:habits:domain` and called inside
`CreateHabitUseCase` (for new `Habit.id`) and `ToggleHabitCompletionUseCase` /
`IncrementHabitCountUseCase` (for new `HabitLog.id` when no log exists for today).

**Rationale**: Avoids adding an extra Gradle dependency. `kotlin.uuid.Uuid.random()` internally delegates
to `SecureRandom` (Android JVM) and `SecRandomCopyBytes` (iOS) via the Kotlin stdlib — the same entropy
sources a third-party library would use. The `ExperimentalUuidApi` opt-in is stable enough for shipping
production code per Kotlin 2.x deprecation policy.

**Alternatives considered**:
- `kotlinx-uuid` (external library) — adds a transitive dependency for a single function. Rejected.
- `java.util.UUID.randomUUID()` in `androidMain` only — would require platform-specific source sets in the
  domain module, violating the `commonMain`-only constraint. Rejected.
- Delegating ID assignment to the data layer (`HabitRepositoryImpl`) — blurs domain/data responsibilities;
  `Habit` would have to be created with an empty/placeholder ID. Rejected.

---

## Decision 2 — `DayOfWeek` source: `kotlinx.datetime` vs. Kotlin stdlib

**Question**: `Habit.frequencyDays: Set<DayOfWeek>` appears in the domain model. Two `DayOfWeek` types exist
in KMP: `kotlinx.datetime.DayOfWeek` and the JVM-only `java.time.DayOfWeek`. Which should be used?

**Decision**: Use `kotlinx.datetime.DayOfWeek` (from `org.jetbrains.kotlinx:kotlinx-datetime`). The domain
module must add `implementation(libs.kotlinx.datetime)` to its `commonMain` dependencies.

```kotlin
import kotlinx.datetime.DayOfWeek

data class Habit(
    // ...
    val frequencyDays: Set<DayOfWeek>,
    // ...
)
```

Serialisation in the data layer uses `DayOfWeek.isoDayNumber` (1 = Monday, 7 = Sunday) as the ordinal for
the comma-separated string stored in `HabitEntity.frequencyDays`.

**Rationale**: `java.time.DayOfWeek` is JVM-only and unavailable in `commonMain` without a polyfill.
`kotlinx.datetime.DayOfWeek` is the KMP-native enum and is already an existing project dependency (declared
in `libs.versions.toml` as `kotlinxDatetime = "0.7.1"`). Adding it to the domain module requires only
a single line in `build.gradle.kts`.

**Alternatives considered**:
- Custom `enum class DayOfWeek` in the domain module — avoids the dependency but adds duplicate code and
  loses interoperability with `kotlinx-datetime` date/time APIs in the data layer. Rejected.
- `java.util.Calendar.DAY_OF_WEEK` — JVM-only. Rejected.

---

## Decision 3 — Navigation wiring: `MudawamaAppShell` content-slot refactor

**Question**: `HabitsPlaceholderScreen` is hardcoded inside `MudawamaAppShell` in `shared:navigation`.
Replacing it with `HabitsScreen` requires `shared:navigation` to know about `feature:habits:presentation`.
That would make the navigation module depend on a feature module — a unidirectional dependency violation
(feature → navigation is the correct direction, not navigation → feature).

**Decision**: Refactor `MudawamaAppShell` to accept a **content-slot lambda** for each feature destination:

```kotlin
@Composable
fun MudawamaAppShell(
    // Default: placeholders (existing behaviour; no breaking change)
    habitsScreen: @Composable () -> Unit = { HabitsPlaceholderScreen() },
)
```

Add `feature:habits:presentation` as a dependency of `shared:umbrella-ui`. Create
`shared/umbrella-ui/src/commonMain/.../MudawamaApp.kt` that wraps `MudawamaAppShell` with all real screens:

```kotlin
@Composable
fun MudawamaApp() {
    MudawamaAppShell(
        habitsScreen = { HabitsScreen() }
    )
}
```

Platform hosts (`androidApp`, `iosApp`) call `MudawamaApp()` instead of `MudawamaAppShell()` directly.

**Rationale**: Keeps `shared:navigation` feature-agnostic (it only owns routing primitives and placeholders).
The `umbrella-ui` module is already the composition root that aggregates all shared UI modules; adding
feature screens there is architecturally correct. The default lambda maintains backward compatibility —
existing tests calling `MudawamaAppShell()` with no arguments still render placeholders.

**Alternatives considered**:
- Add `feature:habits:presentation` directly to `shared:navigation` — creates navigation → feature coupling;
  forbidden by the project's dependency-direction constitution rule. Rejected.
- Use a `NavController`-style callback registry (feature modules register their own composable) — requires
  a custom DI or registry mechanism; significantly more complex. Rejected.
- Keep `HabitsPlaceholderScreen` until a dedicated "navigation refactor" spec — defers working software;
  FR-020 requires the replacement to be in scope for this delivery. Rejected.

---

## Decision 4 — 7-day date window: static calculation at Flow creation time

**Question**: `observeLogsForDateRange(startDate, endDate)` is a Room query Flow with fixed SQL parameters.
If the user keeps the screen open past the logical midnight rollover, the window would become stale
(today = `startDate + 7`; new logs created by the rollover job fall outside the range). How should this be
handled?

**Decision**: Calculate `startDate` and `endDate` once at ViewModel initialisation (when the use case's
`invoke()` is first called). Accept the following constraint for v1 delivery:

> **Rollover limitation**: If the user keeps the Habits screen open past the logical date rollover, the
> date window observed by the UI does not advance until the ViewModel is re-created (e.g., the user
> navigates away and back). New logs created by the rollover job for the new date will not appear in the
> existing `observeLogsForDateRange` Flow.

This matches the spec's own rollover edge case note: "when the daily rollover job creates fresh empty logs
for the new logical date, the UI updates reactively." The DAO Flow will emit reactively for log changes
*within* the current window, but not for the new day's logs until re-subscription.

**Post-v1 enhancement** (documented as a known gap, not a spec blocker): expose a
`fun observeLogicalDateChanges(): Flow<LocalDate>` from `TimeProvider` (a ticker Flow that emits once per
logical day boundary). The ViewModel listens to it and re-subscribes to `ObserveHabitsWithTodayStatusUseCase`
on emission. This is deferred to a follow-up spec.

**Rationale**: The spec §Assumptions state "The Habits screen reads and displays whatever logs already
exist; it does not trigger the rollover job." The primary session use-case (user opens app, completes
habits, closes app) is fully handled. A midnight-open session is a rare edge case that gracefully degrades
(habits appear but new-day heatmap cell is not visible until re-entry).

**Alternatives considered**:
- `repeatLatest` + periodic ticker inside the use case — adds `kotlinx.coroutines.delay` in a `flow { }`,
  coupling the use case to a background timer. Unnecessary complexity for v1. Deferred.
- Dynamic parameterized Room query (`WHERE date BETWEEN ? AND ?` re-evaluated per emission) — Room does not
  support re-evaluating query parameters on a live Flow without re-subscribing. Not possible.

---

## Decision 5 — `observeLogsForDateRange` correctness for ISO date lexicographic ordering

**Question**: Room's `getLogsForDateRange` uses `WHERE date >= :startDate AND date <= :endDate`. Is ISO
`yyyy-MM-dd` string comparison lexicographically equivalent to chronological comparison?

**Decision**: Yes. ISO-8601 `yyyy-MM-dd` strings with zero-padded month and day are lexicographically
identical to chronological order. `"2026-03-30" < "2026-04-05"` evaluates correctly in both string and
date comparisons. The existing DAO query requires no modification.

**Rationale**: This is a well-documented property of ISO-8601 date strings. `DateFormatters.toIsoDateString`
(from `shared:core:time`) delegates to `LocalDate.toString()` which is documented by `kotlinx-datetime` to
always produce `yyyy-MM-dd` with zero-padded month and day.

---

## Decision 6 — `HabitLog` upsert strategy (toggle / increment)

**Question**: `ToggleHabitCompletionUseCase` and `IncrementHabitCountUseCase` must update a log if it
exists, or create it if it doesn't. `HabitLogDao` has separate `insertLog` and `updateLog` operations
(with `OnConflictStrategy.REPLACE` on insert). Should the data layer use `insert(onConflict = REPLACE)` as
an upsert, or `getLogForHabitOnDate` + conditional insert/update?

**Decision**: Use the get-then-branch pattern in the **use case** (domain layer), not the data layer, to
keep upsert semantics explicit:

```kotlin
// In ToggleHabitCompletionUseCase:
val existing: HabitLog? = habitLogRepository.getLogForHabitOnDate(habitId, today)
if (existing == null) {
    habitLogRepository.upsertLog(HabitLog(id = generateId(), habitId, date = today,
        status = LogStatus.COMPLETED, completedCount = 0, loggedAt = now))
} else {
    val newStatus = if (existing.status == LogStatus.COMPLETED) LogStatus.PENDING else LogStatus.COMPLETED
    habitLogRepository.upsertLog(existing.copy(status = newStatus, loggedAt = now))
}
```

`HabitLogRepository.upsertLog` is implemented as `insertLog` (with `OnConflictStrategy.REPLACE`). Since
`HabitLogEntity.id` is the `@PrimaryKey`, `REPLACE` on the same ID is an upsert. The get-then-branch
is used to distinguish "create with COMPLETED" vs. "toggle existing status".

**Rationale**: The explicit get-then-branch makes the business logic readable and testable at the domain
level without requiring the data layer to expose a combined "toggle" query. The `REPLACE` strategy on
`HabitLogDao` handles the underlying upsert safely. `ToggleHabitCompletionUseCase` stays pure/testable
with a `FakeTimeProvider` and in-memory stubs.

**Alternatives considered**:
- Raw SQL `INSERT OR REPLACE` with a single toggle expression — would push business logic (toggle semantics)
  into SQL. Domain rule belongs in the domain layer. Rejected.
- Separate `toggleLog(habitId, date)` DAO method — leaks domain logic to the data layer. Rejected.

---

## Decision 7 — Rapid-tap debouncing via `MviViewModel.exclusiveIntent`

**Question**: The spec's edge case says "Rapid double-taps are debounced in the ViewModel to prevent
duplicate log writes." `MviViewModel` has `exclusiveIntent(key, block)` which cancels the previous job
with the same key. Is this sufficient debouncing, or is explicit `kotlinx.coroutines.flow.debounce`
needed?

**Decision**: `exclusiveIntent(key)` is sufficient for this use case. For a given `habitId`, the key
is `"toggle_$habitId"` or `"increment_$habitId"`. A second tap while the first coroutine is still running
(e.g., awaiting the suspend DAO call) cancels the in-flight job and starts a fresh one. Since the DAO
call is atomic and any mid-flight cancellation is handled by Room's coroutine cancellation (the write
either completes or does not), this provides the required "no duplicate writes" guarantee.

```kotlin
is HabitsUiAction.ToggleCompletion -> exclusiveIntent("toggle_${action.habitId}") {
    toggleHabitCompletionUseCase(action.habitId)
}
```

For the rare case where two taps arrive in the same coroutine slot before the first suspend point,
the `exclusiveIntent` cancellation guarantees only one write proceeds.

**Rationale**: `debounce` adds a time-based delay (e.g., 300 ms) which would make the UI feel sluggish
for intentional rapid increments (a Numeric habit). `exclusiveIntent` instead serialises the operations
by key: the second tap cancels the first if still in-flight, then runs its own operation. For a typical
DAO write (< 10 ms), the practical difference is negligible, but `exclusiveIntent` preserves correctness
without introducing perceptible latency.

**Alternatives considered**:
- `Flow.debounce(300)` on the action bus — adds 300 ms to all completion interactions; bad UX for Numeric
  counter habits where rapid taps are intentional. Rejected.
- `AtomicBoolean` guard — adds non-coroutine synchronisation; fragile. Rejected.

---

## Summary of Resolved Unknowns

| # | Unknown | Resolved Decision |
|---|---------|-------------------|
| 1 | UUID generation | `kotlin.uuid.Uuid.random()` (stdlib, Kotlin 2.0+, `@ExperimentalUuidApi`) |
| 2 | `DayOfWeek` source | `kotlinx.datetime.DayOfWeek`; add `kotlinx-datetime` dep to domain module |
| 3 | Navigation wiring | Content-slot lambda on `MudawamaAppShell`; real screens assembled in `umbrella-ui` |
| 4 | Date window rollover | Static window at init time; v1 limitation documented; dynamic ticker deferred |
| 5 | ISO date ordering | Lexicographic = chronological for `yyyy-MM-dd`; no DAO changes needed |
| 6 | Log upsert strategy | Domain get-then-branch + `insertLog(REPLACE)` in data layer |
| 7 | Rapid-tap debounce | `exclusiveIntent(key)` per `habitId` is sufficient |

