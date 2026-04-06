# Contract: feature:habits — Domain Layer Public API

**Module**: `feature:habits:domain`
**Date**: 2026-04-05
**Consumer**: `feature:habits:data` (implements), `feature:habits:presentation` (uses via Koin)

---

## Repository Interfaces

### `HabitRepository`

```kotlin
interface HabitRepository {
    fun observeAllHabits(): Flow<List<Habit>>
    suspend fun upsertHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    suspend fun getHabitById(habitId: String): Habit?
}
```

### `HabitLogRepository`

```kotlin
interface HabitLogRepository {
    fun observeLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLog>>
    suspend fun upsertLog(log: HabitLog)
    suspend fun getLogForHabitOnDate(habitId: String, date: String): HabitLog?
}
```

**Date string format**: ISO-8601 `yyyy-MM-dd`, produced exclusively via
`toIsoDateString(TimeProvider.logicalDate())` from `shared:core:time`.
Direct `Clock.System` calls are forbidden (spec constraint SC-002).

---

## Use Case Signatures

| Use Case | Parameters | Returns | Notes |
|----------|-----------|---------|-------|
| `ObserveHabitsWithTodayStatusUseCase` | none | `Flow<List<HabitWithStatus>>` | Combines `observeAllHabits` with `observeLogsForDateRange`; 7-day window. |
| `CreateHabitUseCase` | `name, iconKey, frequencyDays, type, goalCount` | `EmptyResult<HabitError>` | Validates non-blank name and non-empty frequencyDays before inserting. |
| `UpdateHabitUseCase` | `habit: Habit` | `EmptyResult<HabitError>` | Same validations as Create; calls `upsertHabit`. |
| `DeleteHabitUseCase` | `habitId: String` | `EmptyResult<HabitError>` | Returns `CoreHabitCannotBeDeleted` if `habit.isCore == true`. |
| `ToggleHabitCompletionUseCase` | `habitId: String` | `EmptyResult<HabitError>` | Flips BOOLEAN log between PENDING/COMPLETED for today's logical date. |
| `IncrementHabitCountUseCase` | `habitId: String` | `EmptyResult<HabitError>` | Increments `completedCount` for today's log (creates if absent). |
| `ObserveWeeklyHeatmapUseCase` | `habitId: String` | `Flow<List<HabitLog?>>` | Emits 7 entries (today at index 0); `null` = no log for that day. |

**`EmptyResult<HabitError>`** is `Result<Unit, HabitError>` from `shared:core:domain`.

---

## Domain Error Codes

| Error | Trigger | Recovery |
|-------|---------|---------|
| `CoreHabitCannotBeDeleted` | `DeleteHabitUseCase` on a habit where `isCore == true` | Show message; no state change |
| `EmptyHabitName` | `CreateHabitUseCase` or `UpdateHabitUseCase` with blank name | Show inline validation on name field |
| `NoFrequencyDaySelected` | `CreateHabitUseCase` or `UpdateHabitUseCase` with empty `frequencyDays` | Show inline validation on frequency selector |
| `HabitNotFound` | Any use case that loads by ID finds no record | Show error snackbar |

---

## Koin Module

```kotlin
// feature:habits:domain — exposed as a top-level function
fun habitsDomainModule() = module {
    factory { ObserveHabitsWithTodayStatusUseCase(get(), get(), get()) }
    factory { CreateHabitUseCase(get()) }
    factory { UpdateHabitUseCase(get()) }
    factory { DeleteHabitUseCase(get(), get()) }
    factory { ToggleHabitCompletionUseCase(get(), get()) }
    factory { IncrementHabitCountUseCase(get(), get()) }
    factory { ObserveWeeklyHeatmapUseCase(get(), get()) }
}
```

Koin prerequisite bindings (provided by upstream modules):
- `HabitRepository` — provided by `habitsDataModule()`
- `HabitLogRepository` — provided by `habitsDataModule()`
- `TimeProvider` — provided by `timeModule(policy)`

---

## `ObserveHabitsWithTodayStatusUseCase` — Flow Combination Detail

```
TimeProvider.logicalDate()           ─── today: LocalDate
today.minus(DatePeriod(days = 6))    ─── startDate: String  (ISO)
today                                ─── endDate: String    (ISO)

observeAllHabits()                   ─── Flow<List<Habit>>
observeLogsForDateRange(start, end)  ─── Flow<List<HabitLog>>

combine(habitsFlow, logsFlow) { habits, logs ->
    val logsByHabitByDate: Map<String, Map<String, HabitLog>> =
        logs.groupBy { it.habitId }
            .mapValues { (_, l) -> l.associateBy { it.date } }

    val last7Dates: List<String> = (0..6).map { daysAgo ->
        toIsoDateString(today.minus(DatePeriod(days = daysAgo)))
    }                                         // [today, today-1, ..., today-6]

    habits.map { habit ->
        val logMap = logsByHabitByDate[habit.id] ?: emptyMap()
        HabitWithStatus(
            habit    = habit,
            todayLog = logMap[endDate],
            weekLogs = last7Dates.map { date -> logMap[date] }
        )
    }
} : Flow<List<HabitWithStatus>>
```

**Note on date window**: `today`, `startDate`, and `endDate` are captured once when
`invoke()` is first called (at ViewModel init). The Room Flow emits reactively for log
changes within the window. Full window advance on midnight rollover requires
ViewModel re-creation (navigate away and back). This is a documented v1 limitation
(see `research.md` Decision 4).
