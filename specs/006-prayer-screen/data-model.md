# Phase 1: Data Model (Prayer Tracking Screen)

**Branch**: `006-prayer-screen` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)

---

## 1. Shared Core Models

### `LogStatus` (Update)
Location: `feature/habits/domain/src/commonMain/.../model/LogStatus.kt`
```kotlin
enum class LogStatus {
    PENDING,
    COMPLETED,
    MISSED // Added
}
```

### `Coordinates`
Location: `shared/core/src/commonMain/.../location/Coordinates.kt`
```kotlin
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
```

---

## 2. Domain Models

Location: `feature/prayer/domain/src/commonMain/.../prayer/domain/model/`

### `PrayerName`
```kotlin
enum class PrayerName {
    FAJR,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA
}
```

### `PrayerTime`
Domain representation of a fetched API time.
```kotlin
data class PrayerTime(
    val name: PrayerName,
    val timeString: String // "HH:mm"
)
```

### `PrayerWithStatus`
Projection combining habit log state and fetched times. Used by UI.
```kotlin
data class PrayerWithStatus(
    val habitId: String,
    val name: PrayerName,
    val timeString: String, // "HH:mm" from cache, or "—" if unavailable
    val status: LogStatus
)
```

---

## 3. Data Transfer Objects (DTOs)

Location: `feature/prayer/data/src/commonMain/.../prayer/data/dto/`

### `AladhanResponseDto`
```kotlin
@Serializable
data class AladhanResponseDto(
    val code: Int,
    val data: AladhanDataDto
)

@Serializable
data class AladhanDataDto(
    val timings: AladhanTimingsDto
)

@Serializable
data class AladhanTimingsDto(
    @SerialName("Fajr") val fajr: String,
    @SerialName("Dhuhr") val dhuhr: String,
    @SerialName("Asr") val asr: String,
    @SerialName("Maghrib") val maghrib: String,
    @SerialName("Isha") val isha: String
)
```

---

## 4. Database Entities

Location: `shared/core/database/src/commonMain/.../entity/`

### `PrayerTimeCacheEntity`
```kotlin
@Entity(tableName = "prayer_time_cache")
data class PrayerTimeCacheEntity(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val fajr: String, // "HH:mm"
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val fetchedAt: Long // epoch ms
)
```

---

## 5. Mappers

Location: `feature/prayer/data/src/commonMain/.../prayer/data/mapper/`

### `AladhanMapper`
```kotlin
fun AladhanTimingsDto.toEntity(date: String, fetchedAt: Long): PrayerTimeCacheEntity

fun PrayerTimeCacheEntity.toDomainList(): List<PrayerTime>
```

---

## 6. Presentation State / Action / Event

Location: `feature/prayer/presentation/src/commonMain/.../prayer/presentation/model/`

### `PrayerUiState`
```kotlin
data class PrayerUiState(
    val selectedDate: LocalDate,
    val dateStrip: List<LocalDate>, // 7 dates: 3 past, today, 3 future
    val prayers: List<PrayerWithStatus>, // 5 items sorted chronologically
    val isLoading: Boolean = false,
    val timesAvailable: Boolean = false, // false = location/network error, display placeholder times
    val usingFallbackLocation: Boolean = false, // true = permission denied, using Mecca
    val missedSheetPrayer: PrayerWithStatus? = null // non-null means show action sheet
)
```

### `PrayerUiAction`
```kotlin
sealed interface PrayerUiAction {
    data class SelectDate(val date: LocalDate) : PrayerUiAction
    data class TogglePrayer(val prayerHabitId: String) : PrayerUiAction
    data class MarkMissedRequested(val prayer: PrayerWithStatus) : PrayerUiAction
    data class ConfirmMarkMissed(val prayerHabitId: String) : PrayerUiAction
    data object DismissMissedSheet : PrayerUiAction
}
```

### `PrayerUiEvent`
```kotlin
import org.jetbrains.compose.resources.StringResource

sealed interface PrayerUiEvent {
    data class ShowError(val message: StringResource) : PrayerUiEvent
}
```
