# Data Model: Quran Tracking (007)

**Branch**: `007-quran-tracking` | **Date**: 2026-04-10

---

## 1. Room Entities (Database Layer — `shared:core:database`)

### `QuranDailyLogEntity`

**Table:** `quran_daily_logs`  
**Purpose:** Stores one reading session. Multiple rows per calendar day are additive. The daily page total for a given date = `SUM(pagesRead) WHERE date = :date`.

```
@Entity(tableName = "quran_daily_logs")
data class QuranDailyLogEntity(
    @PrimaryKey val id: String,          // UUID string (generated in domain)
    val date: String,                    // "yyyy-MM-dd" ISO-8601
    val pagesRead: Int,                  // pages read in this session (1..604)
    val loggedAt: Long                   // epoch millis (TimeProvider.nowInstant())
)
```

**Constraints:**
- `pagesRead` stored as-is; validation (1..604) enforced in `LogReadingUseCase`.
- No FK to any other table — logs are self-contained.
- No `UNIQUE` constraint on `(date)` — multiple rows per date are intentional.

---

### `QuranGoalEntity`

**Table:** `quran_goals`  
**Purpose:** Singleton. Stores the user's active daily reading goal.

```
@Entity(tableName = "quran_goals")
data class QuranGoalEntity(
    @PrimaryKey val id: Int = 1,         // always 1 (singleton row)
    val pagesPerDay: Int,                // 1..MAX_DAILY_GOAL_PAGES (= 60)
    val updatedAt: Long                  // epoch millis
)
```

**Upsert strategy:** `@Upsert` (INSERT OR REPLACE). Only one row ever exists.  
**Default:** If no row exists, `QuranGoalRepository` returns `pagesPerDay = 5` (spec FR-011, A-008).

---

### `QuranBookmarkEntity` (existing — modified)

**Table:** `quran_bookmarks` (existing — no rename)  
**Purpose:** Singleton. Stores the user's reading position (Surah + Ayah).

**Before (version 2):**
```
id: Int = 1, surah: Int, ayah: Int,
dailyGoalPages: Int, pagesReadToday: Int, lastUpdated: Long
```

**After (version 3 — two columns removed):**
```
@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey val id: Int = 1,         // singleton row
    val surah: Int,                      // 1..114
    val ayah: Int,                       // 1..surah.ayahCount
    val lastUpdated: Long                // epoch millis
)
```

**Migration note:** `dailyGoalPages` and `pagesReadToday` are removed (replaced by `QuranGoalEntity` and `QuranDailyLogEntity`). The Room auto-migration from version 2→3 must use `@DeleteColumn` spec annotations on `QuranBookmarkEntity`.

---

## 2. DAOs

### `QuranDailyLogDao`

```kotlin
@Dao interface QuranDailyLogDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertLog(log: QuranDailyLogEntity)

    @Query("SELECT * FROM quran_daily_logs WHERE date = :date ORDER BY loggedAt ASC")
    fun getLogsForDate(date: String): Flow<List<QuranDailyLogEntity>>

    // For recent logs list: last N distinct dates
    @Query("""
        SELECT * FROM quran_daily_logs
        WHERE date < :beforeDate
        ORDER BY date DESC, loggedAt DESC
    """)
    fun getLogsBefore(beforeDate: String): Flow<List<QuranDailyLogEntity>>

    // For streak computation: all distinct logged dates, descending
    @Query("SELECT DISTINCT date FROM quran_daily_logs ORDER BY date DESC")
    suspend fun getAllLoggedDates(): List<String>
}
```

### `QuranGoalDao`

```kotlin
@Dao interface QuranGoalDao {
    @Upsert
    suspend fun upsertGoal(goal: QuranGoalEntity)

    @Query("SELECT * FROM quran_goals WHERE id = 1")
    fun getGoal(): Flow<QuranGoalEntity?>
}
```

### `QuranBookmarkDao` (updated signatures — existing DAO)

```kotlin
@Dao interface QuranBookmarkDao {
    @Upsert
    suspend fun upsertBookmark(bookmark: QuranBookmarkEntity)

    @Query("SELECT * FROM quran_bookmarks WHERE id = 1")
    fun getBookmark(): Flow<QuranBookmarkEntity?>

    // resetDailyPages() — REMOVED (column no longer exists)
}
```

---

## 3. Domain Models (`feature:quran:domain`)

### `QuranDailyLog`

```kotlin
data class QuranDailyLog(
    val id: String,
    val date: String,       // "yyyy-MM-dd"
    val pagesRead: Int,
    val loggedAt: Long,
)
```

### `QuranGoal`

```kotlin
data class QuranGoal(
    val pagesPerDay: Int,
    val updatedAt: Long,
)
```

**Default constant:** `val DEFAULT_DAILY_GOAL = QuranGoal(pagesPerDay = 5, updatedAt = 0L)`

### `QuranBookmark`

```kotlin
data class QuranBookmark(
    val surah: Int,         // 1..114
    val surahName: String,  // derived from SurahMetadata at domain layer
    val ayah: Int,          // 1..surahAyahCount
)
```

### `SurahMetadata`

```kotlin
/**
 * @param startPage Page number (1–604) in the Madinah Mushaf (King Fahd Complex,
 *                  604-page edition) on which this surah begins.
 *                  Used by [AdvanceBookmarkUseCase] to compute the new bookmark
 *                  page after the user logs a reading session.
 */
data class SurahMetadata(
    val number: Int,        // 1..114
    val nameEn: String,     // transliterated English name
    val ayahCount: Int,     // total ayahs in this surah
    val startPage: Int,     // Madinah Mushaf start page (1..604)
)

// In-memory constant — all 114 entries, no DB/network
val ALL_SURAHS: List<SurahMetadata> = listOf(
    SurahMetadata(1,   "Al-Fatihah",       7,   1),
    SurahMetadata(2,   "Al-Baqarah",     286,   2),
    SurahMetadata(3,   "Aal-E-Imran",    200,  50),
    SurahMetadata(4,   "An-Nisa",        176,  77),
    SurahMetadata(5,   "Al-Maidah",      120, 106),
    SurahMetadata(6,   "Al-Anam",        165, 128),
    SurahMetadata(7,   "Al-Araf",        206, 151),
    SurahMetadata(8,   "Al-Anfal",        75, 177),
    SurahMetadata(9,   "At-Tawbah",      129, 187),
    SurahMetadata(10,  "Yunus",           109, 208),
    SurahMetadata(11,  "Hud",             123, 221),
    SurahMetadata(12,  "Yusuf",           111, 235),
    SurahMetadata(13,  "Ar-Rad",           43, 249),
    SurahMetadata(14,  "Ibrahim",          52, 255),
    SurahMetadata(15,  "Al-Hijr",          99, 262),
    SurahMetadata(16,  "An-Nahl",         128, 267),
    SurahMetadata(17,  "Al-Isra",         111, 282),
    SurahMetadata(18,  "Al-Kahf",         110, 293),
    SurahMetadata(19,  "Maryam",           98, 305),
    SurahMetadata(20,  "Ta-Ha",           135, 312),
    SurahMetadata(21,  "Al-Anbiya",       112, 322),
    SurahMetadata(22,  "Al-Hajj",          78, 332),
    SurahMetadata(23,  "Al-Muminun",      118, 342),
    SurahMetadata(24,  "An-Nur",           64, 350),
    SurahMetadata(25,  "Al-Furqan",        77, 359),
    SurahMetadata(26,  "Ash-Shuara",      227, 367),
    SurahMetadata(27,  "An-Naml",          93, 377),
    SurahMetadata(28,  "Al-Qasas",         88, 385),
    SurahMetadata(29,  "Al-Ankabut",       69, 396),
    SurahMetadata(30,  "Ar-Rum",           60, 404),
    SurahMetadata(31,  "Luqman",           34, 411),
    SurahMetadata(32,  "As-Sajdah",        30, 415),
    SurahMetadata(33,  "Al-Ahzab",         73, 418),
    SurahMetadata(34,  "Saba",             54, 428),
    SurahMetadata(35,  "Fatir",            45, 434),
    SurahMetadata(36,  "Ya-Sin",           83, 440),
    SurahMetadata(37,  "As-Saffat",       182, 446),
    SurahMetadata(38,  "Sad",              88, 453),
    SurahMetadata(39,  "Az-Zumar",         75, 458),
    SurahMetadata(40,  "Ghafir",           85, 467),
    SurahMetadata(41,  "Fussilat",         54, 477),
    SurahMetadata(42,  "Ash-Shura",        53, 483),
    SurahMetadata(43,  "Az-Zukhruf",       89, 489),
    SurahMetadata(44,  "Ad-Dukhan",        59, 496),
    SurahMetadata(45,  "Al-Jathiyah",      37, 499),
    SurahMetadata(46,  "Al-Ahqaf",         35, 502),
    SurahMetadata(47,  "Muhammad",         38, 507),
    SurahMetadata(48,  "Al-Fath",          29, 511),
    SurahMetadata(49,  "Al-Hujurat",       18, 515),
    SurahMetadata(50,  "Qaf",              45, 518),
    SurahMetadata(51,  "Adh-Dhariyat",     60, 520),
    SurahMetadata(52,  "At-Tur",           49, 523),
    SurahMetadata(53,  "An-Najm",          62, 526),
    SurahMetadata(54,  "Al-Qamar",         55, 528),
    SurahMetadata(55,  "Ar-Rahman",        78, 531),
    SurahMetadata(56,  "Al-Waqiah",        96, 534),
    SurahMetadata(57,  "Al-Hadid",         29, 537),
    SurahMetadata(58,  "Al-Mujadila",      22, 542),
    SurahMetadata(59,  "Al-Hashr",         24, 545),
    SurahMetadata(60,  "Al-Mumtahanah",    13, 549),
    SurahMetadata(61,  "As-Saf",           14, 551),
    SurahMetadata(62,  "Al-Jumuah",        11, 553),
    SurahMetadata(63,  "Al-Munafiqun",     11, 554),
    SurahMetadata(64,  "At-Taghabun",      18, 556),
    SurahMetadata(65,  "At-Talaq",         12, 558),
    SurahMetadata(66,  "At-Tahrim",        12, 560),
    SurahMetadata(67,  "Al-Mulk",          30, 562),
    SurahMetadata(68,  "Al-Qalam",         52, 564),
    SurahMetadata(69,  "Al-Haqqah",        52, 566),
    SurahMetadata(70,  "Al-Maarij",        44, 568),
    SurahMetadata(71,  "Nuh",              28, 570),
    SurahMetadata(72,  "Al-Jinn",          28, 572),
    SurahMetadata(73,  "Al-Muzzammil",     20, 574),
    SurahMetadata(74,  "Al-Muddaththir",   56, 575),
    SurahMetadata(75,  "Al-Qiyamah",       40, 577),
    SurahMetadata(76,  "Al-Insan",         31, 578),
    SurahMetadata(77,  "Al-Mursalat",      50, 580),
    SurahMetadata(78,  "An-Naba",          40, 582),
    SurahMetadata(79,  "An-Naziat",        46, 583),
    SurahMetadata(80,  "Abasa",            42, 585),
    SurahMetadata(81,  "At-Takwir",        29, 586),
    SurahMetadata(82,  "Al-Infitar",       19, 587),
    SurahMetadata(83,  "Al-Mutaffifin",    36, 587),
    SurahMetadata(84,  "Al-Inshiqaq",      25, 589),
    SurahMetadata(85,  "Al-Buruj",         22, 590),
    SurahMetadata(86,  "At-Tariq",         17, 591),
    SurahMetadata(87,  "Al-Ala",           19, 591),
    SurahMetadata(88,  "Al-Ghashiyah",     26, 592),
    SurahMetadata(89,  "Al-Fajr",          30, 593),
    SurahMetadata(90,  "Al-Balad",         20, 594),
    SurahMetadata(91,  "Ash-Shams",        15, 595),
    SurahMetadata(92,  "Al-Layl",          21, 595),
    SurahMetadata(93,  "Ad-Duha",          11, 596),
    SurahMetadata(94,  "Ash-Sharh",         8, 596),
    SurahMetadata(95,  "At-Tin",            8, 597),
    SurahMetadata(96,  "Al-Alaq",          19, 597),
    SurahMetadata(97,  "Al-Qadr",           5, 598),
    SurahMetadata(98,  "Al-Bayyinah",       8, 598),
    SurahMetadata(99,  "Az-Zalzalah",       8, 599),
    SurahMetadata(100, "Al-Adiyat",        11, 599),
    SurahMetadata(101, "Al-Qariah",        11, 600),
    SurahMetadata(102, "At-Takathur",       8, 600),
    SurahMetadata(103, "Al-Asr",            3, 601),
    SurahMetadata(104, "Al-Humazah",        9, 601),
    SurahMetadata(105, "Al-Fil",            5, 601),
    SurahMetadata(106, "Quraysh",           4, 602),
    SurahMetadata(107, "Al-Maun",           7, 602),
    SurahMetadata(108, "Al-Kawthar",        3, 602),
    SurahMetadata(109, "Al-Kafirun",        6, 603),
    SurahMetadata(110, "An-Nasr",           3, 603),
    SurahMetadata(111, "Al-Masad",          5, 603),
    SurahMetadata(112, "Al-Ikhlas",         4, 604),
    SurahMetadata(113, "Al-Falaq",          5, 604),
    SurahMetadata(114, "An-Nas",            6, 604),
)

/**
 * Returns the surah that contains the given [page] (1–604).
 * Finds the last surah whose startPage ≤ page (i.e., the surah currently open
 * at that page). Falls back to Al-Fatihah if page < 1.
 */
fun surahForPage(page: Int): SurahMetadata {
    val clamped = page.coerceIn(1, 604)
    return ALL_SURAHS.lastOrNull { it.startPage <= clamped } ?: ALL_SURAHS.first()
}
```

### `QuranError`

```kotlin
// Domain-layer constants
const val MAX_SESSION_PAGES = 604    // total pages in the Uthmani mushaf (per-session log cap)
const val MAX_DAILY_GOAL_PAGES = 60  // 3 Juz/day — reasonable upper bound for a daily goal
const val DEFAULT_DAILY_GOAL_PAGES = 5

sealed interface QuranError : DomainError {
    data object DatabaseError    : QuranError
    data object InvalidPageCount : QuranError   // pages < 1 or > MAX_SESSION_PAGES
    data object InvalidGoal      : QuranError   // pagesPerDay < 1 or > MAX_DAILY_GOAL_PAGES
    data object InvalidSurah     : QuranError   // surahNumber not in 1..114
    data object InvalidAyah      : QuranError   // ayah > surah.ayahCount
    data object GenericError     : QuranError
}
```

---

## 4. Repository Interfaces (`feature:quran:domain`)

### `QuranDailyLogRepository`

```kotlin
interface QuranDailyLogRepository {
    fun observeLogsForDate(date: String): Flow<List<QuranDailyLog>>
    fun observeRecentLogs(beforeDate: String): Flow<List<QuranDailyLog>>
    suspend fun insertLog(log: QuranDailyLog): EmptyResult<QuranError>
    suspend fun getAllLoggedDates(): Result<List<String>, QuranError>
}
```

### `QuranGoalRepository`

```kotlin
interface QuranGoalRepository {
    fun observeGoal(): Flow<QuranGoal>     // emits DEFAULT_DAILY_GOAL if no row exists
    suspend fun setGoal(goal: QuranGoal): EmptyResult<QuranError>
}
```

### `QuranBookmarkRepository`

```kotlin
interface QuranBookmarkRepository {
    fun observeBookmark(): Flow<QuranBookmark?>   // null if never set
    suspend fun upsertBookmark(bookmark: QuranBookmark): EmptyResult<QuranError>
}
```

---

## 5. Use Cases (`feature:quran:domain`)

### `LogReadingUseCase`
```
constructor(repo: QuranDailyLogRepository, timeProvider: TimeProvider, dispatcher: CoroutineDispatcher)
suspend operator fun invoke(pages: Int, date: LocalDate): EmptyResult<QuranError>
```

### `ObserveQuranStateUseCase`
```
constructor(logRepo: QuranDailyLogRepository, goalRepo: QuranGoalRepository,
            bookmarkRepo: QuranBookmarkRepository)
operator fun invoke(date: LocalDate): Flow<QuranScreenState>
// combines 3 flows; emits QuranScreenState(pagesReadToday, goalPages, bookmark, recentLogs)
```

### `UpdateBookmarkUseCase`
```
constructor(repo: QuranBookmarkRepository, dispatcher: CoroutineDispatcher)
suspend operator fun invoke(surahNumber: Int, ayahNumber: Int): EmptyResult<QuranError>
// validates via ALL_SURAHS; builds QuranBookmark with surahName lookup
```

### `SetGoalUseCase`
```
constructor(repo: QuranGoalRepository, timeProvider: TimeProvider, dispatcher: CoroutineDispatcher)
suspend operator fun invoke(pagesPerDay: Int): EmptyResult<QuranError>
```

### `AdvanceBookmarkUseCase`
```
constructor(repo: QuranBookmarkRepository, dispatcher: CoroutineDispatcher)
suspend operator fun invoke(currentBookmark: QuranBookmark?, pagesRead: Int): EmptyResult<QuranError>
// Algorithm:
//   startPage = if (currentBookmark != null) ALL_SURAHS[currentBookmark.surah - 1].startPage else 1
//   targetPage = (startPage + pagesRead).coerceAtMost(604)
//   newSurah   = surahForPage(targetPage)
//   repo.upsertBookmark(QuranBookmark(newSurah.number, newSurah.nameEn, ayah = 1))
// Returns Failure(InvalidPageCount) if pagesRead < 1.
// Called by QuranViewModel inside ConfirmLogReading after LogReadingUseCase succeeds.
// Errors are silently ignored in the ViewModel — the log itself already succeeded.
```

### `ComputeStreakUseCase`
```
constructor(repo: QuranDailyLogRepository, timeProvider: TimeProvider, dispatcher: CoroutineDispatcher)
suspend operator fun invoke(): Int
// walks from yesterday backward; counts consecutive days with sum(pagesRead) >= 1
// today excluded from count until day closes (FR-013)
```

---

## 6. Domain Screen State Projection

```kotlin
data class QuranScreenState(
    val pagesReadToday: Int,          // sum of all log sessions for the date
    val goalPages: Int,               // from active QuranGoal (default 5)
    val bookmark: QuranBookmark?,     // null if never set
    val recentLogs: List<RecentLogEntry>,  // last 3 distinct-date summaries
) {
    data class RecentLogEntry(
        val date: String,             // "yyyy-MM-dd"
        val pagesRead: Int,           // sum for that date
        val goalPages: Int,           // goal active at time of display (current goal)
    ) {
        val status: LogStatus get() = when {
            pagesRead > goalPages  -> LogStatus.OVER
            pagesRead == goalPages -> LogStatus.HIT
            else                   -> LogStatus.UNDER
        }
    }
    enum class LogStatus { OVER, HIT, UNDER }
}
```

---

## 7. Database Migration Specification

### Version Bump: 2 → 3

Changes:
1. **Add** table `quran_daily_logs` (`QuranDailyLogEntity`)
2. **Add** table `quran_goals` (`QuranGoalEntity`)
3. **Remove** columns `dailyGoalPages` and `pagesReadToday` from `quran_bookmarks`

Room auto-migration handles adds automatically. Column removal requires:

```kotlin
// In MudawamaDatabase.kt
@DeleteColumn(tableName = "quran_bookmarks", columnName = "dailyGoalPages")
@DeleteColumn(tableName = "quran_bookmarks", columnName = "pagesReadToday")
class AutoMigration_2_3 : AutoMigrationSpec

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        QuranBookmarkEntity::class,   // modified (2 columns removed)
        QuranDailyLogEntity::class,   // NEW
        QuranGoalEntity::class,       // NEW
        PrayerTimeCacheEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = MudawamaDatabase.AutoMigration_2_3::class),
    ],
    version = 3,
    exportSchema = true
)
```

---

## 8. Mapper Summary (Data Layer)

| Entity | Domain | File |
|--------|--------|------|
| `QuranDailyLogEntity` ↔ `QuranDailyLog` | `QuranDailyLog` | `QuranDailyLogMapper.kt` |
| `QuranGoalEntity` ↔ `QuranGoal` | `QuranGoal` | `QuranGoalMapper.kt` |
| `QuranBookmarkEntity` ↔ `QuranBookmark` | `QuranBookmark` | `QuranBookmarkMapper.kt` — derives `surahName` from `ALL_SURAHS[surah - 1].nameEn` |

---

## 9. Presentation State & MVI Contract

### `QuranUiState`
```kotlin
data class QuranUiState(
    val selectedDate: LocalDate,
    val dateStrip: List<LocalDate>,   // exactly 7 entries: [today-6 … today]; today is rightmost; no future dates
    val today: LocalDate,
    val pagesReadToday: Int = 0,
    val goalPages: Int = 5,
    val bookmark: QuranBookmark? = null,
    val recentLogs: List<QuranScreenState.RecentLogEntry> = emptyList(),
    val streak: Int = 0,
    val isLoading: Boolean = true,
    // Sheet visibility
    val logReadingSheetVisible: Boolean = false,
    val setGoalSheetVisible: Boolean = false,
    val updatePositionSheetVisible: Boolean = false,
    // Log Reading sheet input
    val logReadingPageInput: Int = 0,
) {
    val isReadOnly: Boolean   get() = selectedDate != today
    val progressFraction: Float get() =
        if (goalPages > 0) (pagesReadToday / goalPages.toFloat()).coerceIn(0f, 1f) else 0f
}
```

### `QuranUiAction`
```kotlin
sealed interface QuranUiAction {
    data class SelectDate(val date: LocalDate)          : QuranUiAction
    // Log Reading sheet
    data object OpenLogReadingSheet                     : QuranUiAction
    data object DismissLogReadingSheet                  : QuranUiAction
    data class UpdateLogPageInput(val pages: Int)       : QuranUiAction
    data class ConfirmLogReading(val pages: Int)        : QuranUiAction
    // Set Goal sheet
    data object OpenSetGoalSheet                        : QuranUiAction
    data object DismissSetGoalSheet                     : QuranUiAction
    data class ConfirmSetGoal(val pages: Int)           : QuranUiAction
    // Update Position sheet
    data object OpenUpdatePositionSheet                 : QuranUiAction   // from main screen or Resume Reading card
    data object DismissUpdatePositionSheet              : QuranUiAction
    data class ConfirmUpdatePosition(val surah: Int, val ayah: Int) : QuranUiAction
}
```

### `QuranUiEvent`
```kotlin
sealed interface QuranUiEvent {
    data class ShowSnackbar(val message: StringResource) : QuranUiEvent
}
```
