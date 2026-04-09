# Phase 1: Domain API Contracts (Prayer Tracking Screen)

**Branch**: `006-prayer-screen` | **Date**: 2026-04-08 | **Spec**: [../spec.md](../spec.md)

---

## 1. Shared Core Interfaces

### `LocationProvider`
Location: `shared/core/src/commonMain/.../location/LocationProvider.kt`
```kotlin
interface LocationProvider {
    /**
     * Gets the current location of the device.
     * Returns a `Result` to encapsulate platform-specific errors (e.g. permission denied, disabled GPS).
     */
    suspend fun getCurrentLocation(): Result<Coordinates>
}
```

---

## 2. Repositories

Location: `feature/prayer/domain/src/commonMain/.../prayer/domain/repository/`

### `PrayerTimesRepository`
```kotlin
interface PrayerTimesRepository {
    /**
     * Fetches prayer times for the specified date.
     * Checks the local cache first. If absent, fetches from Aladhan API, caches, and returns.
     */
    suspend fun getPrayerTimes(date: LocalDate, coordinates: Coordinates): Result<List<PrayerTime>>
    
    /**
     * Optional: Just read from cache, do not trigger network fetch.
     * Useful for building projections when offline without triggering errors.
     */
    suspend fun getCachedPrayerTimes(date: LocalDate): List<PrayerTime>?
}
```

### `PrayerHabitRepository`
```kotlin
interface PrayerHabitRepository {
    /**
     * Observes the 5 obligatory prayer habits combined with their log status for the given date.
     * Under the hood, this delegates to `HabitDao` and `HabitLogDao`, filtering for `category = "prayer"`.
     */
    fun observePrayerHabitsWithStatus(date: LocalDate): Flow<List<HabitWithStatus>>
    
    /**
     * Checks if the 5 prayers exist. If not, inserts them with stable IDs.
     */
    suspend fun seedPrayerHabitsIfNeeded(): Result<Unit>
}
```

---

## 3. Use Cases

Location: `feature/prayer/domain/src/commonMain/.../prayer/domain/usecase/`

### `ObservePrayersForDateUseCase`
```kotlin
class ObservePrayersForDateUseCase(
    private val prayerHabitRepository: PrayerHabitRepository,
    private val prayerTimesRepository: PrayerTimesRepository,
    private val locationProvider: LocationProvider,
    private val dispatcher: CoroutineDispatcher
) {
    /**
     * Returns a Flow that emits the fully resolved `PrayerWithStatus` list.
     *
     * Flow logic:
     * 1. Observe prayer habits + status for the given date.
     * 2. For the given date, attempt to fetch prayer times.
     *    - If success, merge times with the habits based on `PrayerName` ordinal.
     *    - If failure (e.g. network/location), emit the list with time strings as "—" and handle error gracefully.
     */
    operator fun invoke(date: LocalDate): Flow<Result<List<PrayerWithStatus>>>
}
```

### `TogglePrayerStatusUseCase`
```kotlin
class TogglePrayerStatusUseCase(
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val dispatcher: CoroutineDispatcher
) {
    /**
     * Toggles a prayer's status.
     * Behavior: PENDING -> COMPLETED, COMPLETED -> PENDING, MISSED -> COMPLETED.
     */
    suspend operator fun invoke(prayerHabitId: String, date: LocalDate): Result<Unit>
}
```

### `MarkPrayerMissedUseCase`
```kotlin
class MarkPrayerMissedUseCase(
    private val habitRepository: HabitRepository, // Needs access to log habit
    private val dispatcher: CoroutineDispatcher
) {
    /**
     * Explicitly marks a prayer as MISSED for the given date.
     */
    suspend operator fun invoke(prayerHabitId: String, date: LocalDate): Result<Unit>
}
```

### `SeedPrayerHabitsUseCase`
```kotlin
class SeedPrayerHabitsUseCase(
    private val prayerHabitRepository: PrayerHabitRepository,
    private val dispatcher: CoroutineDispatcher
) {
    /**
     * Ensures the 5 obligatory prayers are seeded in the database.
     * Called at startup.
     */
    suspend operator fun invoke(): Result<Unit>
}
```
