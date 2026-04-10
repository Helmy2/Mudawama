package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.helmy2.mudawama.core.database.entity.QuranGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranGoalDao {

    @Upsert
    suspend fun upsertGoal(goal: QuranGoalEntity)

    @Query("SELECT * FROM quran_goals WHERE id = 1")
    fun getGoal(): Flow<QuranGoalEntity?>
}
