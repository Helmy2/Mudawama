package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.helmy2.mudawama.core.database.entity.TasbeehGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbeehGoalDao {

    @Query("SELECT * FROM tasbeeh_goals WHERE id = 1")
    fun getGoal(): Flow<TasbeehGoalEntity?>

    @Upsert
    suspend fun upsertGoal(entity: TasbeehGoalEntity)
}
