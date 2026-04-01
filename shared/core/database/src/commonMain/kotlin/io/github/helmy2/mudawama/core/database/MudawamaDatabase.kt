package io.github.helmy2.mudawama.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import io.github.helmy2.mudawama.core.database.dao.HabitDao
import io.github.helmy2.mudawama.core.database.dao.HabitLogDao
import io.github.helmy2.mudawama.core.database.dao.QuranBookmarkDao
import io.github.helmy2.mudawama.core.database.entity.HabitEntity
import io.github.helmy2.mudawama.core.database.entity.HabitLogEntity
import io.github.helmy2.mudawama.core.database.entity.QuranBookmarkEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        QuranBookmarkEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(MudawamaDatabaseConstructor::class)
abstract class MudawamaDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun quranBookmarkDao(): QuranBookmarkDao
}

// Room KSP generates the actual implementation for each platform
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object MudawamaDatabaseConstructor : RoomDatabaseConstructor<MudawamaDatabase> {
    override fun initialize(): MudawamaDatabase
}
