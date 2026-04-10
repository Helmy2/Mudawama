package io.github.helmy2.mudawama.quran.domain.repository

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranGoal
import kotlinx.coroutines.flow.Flow

interface QuranGoalRepository {
    fun observeGoal(): Flow<QuranGoal>   // emits DEFAULT_DAILY_GOAL if no row exists
    suspend fun setGoal(goal: QuranGoal): EmptyResult<QuranError>
}
