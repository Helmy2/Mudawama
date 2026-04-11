package io.github.helmy2.mudawama.athkar.domain.repository

import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.model.TasbeehDailyTotal
import io.github.helmy2.mudawama.athkar.domain.model.TasbeehGoal
import io.github.helmy2.mudawama.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface TasbeehRepository {
    fun observeGoal(): Flow<TasbeehGoal>
    suspend fun setGoal(goalCount: Int): EmptyResult<AthkarError>
    fun observeDailyTotal(date: String): Flow<TasbeehDailyTotal>
    suspend fun addToDaily(date: String, amount: Int): EmptyResult<AthkarError>
}
