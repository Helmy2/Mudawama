package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.error.HabitError
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.repository.HabitRepository
import io.github.helmy2.mudawama.habits.domain.util.generateId
import kotlinx.datetime.DayOfWeek
import kotlin.uuid.ExperimentalUuidApi

class CreateHabitUseCase(
    private val habitRepository: HabitRepository,
    private val timeProvider: TimeProvider,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        name: String,
        iconKey: String,
        frequencyDays: Set<DayOfWeek>,
        type: HabitType,
        goalCount: Int?,
        category: String = "custom",
    ): EmptyResult<HabitError> {
        if (name.isBlank()) return Result.Failure(HabitError.EmptyHabitName)
        if (frequencyDays.isEmpty()) return Result.Failure(HabitError.NoFrequencyDaySelected)

        val habit = Habit(
            id = generateId(),
            name = name.trim(),
            iconKey = iconKey,
            type = type,
            category = category,
            frequencyDays = frequencyDays,
            isCore = false,
            goalCount = if (type == HabitType.NUMERIC) goalCount else null,
            createdAt = timeProvider.nowInstant().toEpochMilliseconds(),
        )
        habitRepository.upsertHabit(habit)
        return Result.Success(Unit)
    }
}

