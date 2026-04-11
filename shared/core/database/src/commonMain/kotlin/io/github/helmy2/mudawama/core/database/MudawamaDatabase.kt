package io.github.helmy2.mudawama.core.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import io.github.helmy2.mudawama.core.database.converter.AthkarCountersConverter
import io.github.helmy2.mudawama.core.database.dao.AthkarDailyLogDao
import io.github.helmy2.mudawama.core.database.dao.HabitDao
import io.github.helmy2.mudawama.core.database.dao.HabitLogDao
import io.github.helmy2.mudawama.core.database.dao.PrayerTimeCacheDao
import io.github.helmy2.mudawama.core.database.dao.QuranBookmarkDao
import io.github.helmy2.mudawama.core.database.dao.QuranDailyLogDao
import io.github.helmy2.mudawama.core.database.dao.QuranGoalDao
import io.github.helmy2.mudawama.core.database.dao.TasbeehDailyTotalDao
import io.github.helmy2.mudawama.core.database.dao.TasbeehGoalDao
import io.github.helmy2.mudawama.core.database.entity.AthkarDailyLogEntity
import io.github.helmy2.mudawama.core.database.entity.HabitEntity
import io.github.helmy2.mudawama.core.database.entity.HabitLogEntity
import io.github.helmy2.mudawama.core.database.entity.PrayerTimeCacheEntity
import io.github.helmy2.mudawama.core.database.entity.QuranBookmarkEntity
import io.github.helmy2.mudawama.core.database.entity.QuranDailyLogEntity
import io.github.helmy2.mudawama.core.database.entity.QuranGoalEntity
import io.github.helmy2.mudawama.core.database.entity.TasbeehDailyTotalEntity
import io.github.helmy2.mudawama.core.database.entity.TasbeehGoalEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        QuranBookmarkEntity::class,
        QuranDailyLogEntity::class,
        QuranGoalEntity::class,
        PrayerTimeCacheEntity::class,
        AthkarDailyLogEntity::class,
        TasbeehGoalEntity::class,
        TasbeehDailyTotalEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = MudawamaDatabase.AutoMigration_2_3::class),
        AutoMigration(from = 3, to = 4),
    ],
    version = 4,
    exportSchema = true
)
@ConstructedBy(MudawamaDatabaseConstructor::class)
@TypeConverters(AthkarCountersConverter::class)
abstract class MudawamaDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun quranBookmarkDao(): QuranBookmarkDao
    abstract fun quranDailyLogDao(): QuranDailyLogDao
    abstract fun quranGoalDao(): QuranGoalDao
    abstract fun prayerTimeCacheDao(): PrayerTimeCacheDao
    abstract fun athkarDailyLogDao(): AthkarDailyLogDao
    abstract fun tasbeehGoalDao(): TasbeehGoalDao
    abstract fun tasbeehDailyTotalDao(): TasbeehDailyTotalDao

    @DeleteColumn(tableName = "quran_bookmarks", columnName = "dailyGoalPages")
    @DeleteColumn(tableName = "quran_bookmarks", columnName = "pagesReadToday")
    class AutoMigration_2_3 : AutoMigrationSpec
}

// Room KSP generates the actual implementation for each platform
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object MudawamaDatabaseConstructor : RoomDatabaseConstructor<MudawamaDatabase> {
    override fun initialize(): MudawamaDatabase
}
