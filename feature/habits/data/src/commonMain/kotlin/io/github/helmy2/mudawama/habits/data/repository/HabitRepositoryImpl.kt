package io.github.helmy2.mudawama.habits.data.repository

import io.github.helmy2.mudawama.core.database.dao.HabitDao
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.habits.data.mapper.toDomain
import io.github.helmy2.mudawama.habits.data.mapper.toEntity
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class HabitRepositoryImpl(
    private val dao: HabitDao,
    private val timeProvider: TimeProvider,
) : HabitRepository {

    override fun observeAllHabits(): Flow<List<Habit>> =
        dao.getAllHabits().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertHabit(habit: Habit) =
        // insertHabit uses OnConflictStrategy.REPLACE; inserting an entity with an existing UUID id
        // performs an in-place replacement (upsert)
        dao.insertHabit(habit.toEntity())

    override suspend fun deleteHabit(habitId: String) =
        dao.deleteHabit(habitId)

    override suspend fun getHabitById(habitId: String): Habit? =
        dao.getHabitById(habitId)?.toDomain()
}

