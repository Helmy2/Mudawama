package io.github.helmy2.mudawama.habits.domain.repository

import io.github.helmy2.mudawama.habits.domain.model.Habit
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for [Habit] persistence.
 *
 * `upsertHabit`: Acts as both create and update — the data layer implementation
 * uses `OnConflictStrategy.REPLACE` on the UUID primary key.
 */
interface HabitRepository {
    fun observeAllHabits(): Flow<List<Habit>>
    suspend fun upsertHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    suspend fun getHabitById(habitId: String): Habit?
}

