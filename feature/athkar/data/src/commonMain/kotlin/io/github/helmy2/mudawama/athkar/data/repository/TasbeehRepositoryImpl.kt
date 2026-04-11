package io.github.helmy2.mudawama.athkar.data.repository

import io.github.helmy2.mudawama.athkar.data.mapper.toDomain
import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.model.TasbeehDailyTotal
import io.github.helmy2.mudawama.athkar.domain.model.TasbeehGoal
import io.github.helmy2.mudawama.athkar.domain.repository.TasbeehRepository
import io.github.helmy2.mudawama.core.database.dao.TasbeehDailyTotalDao
import io.github.helmy2.mudawama.core.database.dao.TasbeehGoalDao
import io.github.helmy2.mudawama.core.database.entity.TasbeehDailyTotalEntity
import io.github.helmy2.mudawama.core.database.entity.TasbeehGoalEntity
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class TasbeehRepositoryImpl(
    private val goalDao: TasbeehGoalDao,
    private val dailyTotalDao: TasbeehDailyTotalDao,
    private val dispatcher: CoroutineDispatcher,
) : TasbeehRepository {

    override fun observeGoal(): Flow<TasbeehGoal> =
        goalDao.getGoal().map { it?.toDomain() ?: TasbeehGoal(goalCount = 100) }

    override suspend fun setGoal(goalCount: Int): EmptyResult<AthkarError> =
        withContext(dispatcher) {
            safeCall(
                block = { goalDao.upsertGoal(TasbeehGoalEntity(id = 1, goalCount = goalCount)) },
                onError = { AthkarError.DatabaseError },
            )
        }

    override fun observeDailyTotal(date: String): Flow<TasbeehDailyTotal> =
        dailyTotalDao.getTotalForDate(date).map { it?.toDomain() ?: TasbeehDailyTotal(date = date, totalCount = 0) }

    override suspend fun addToDaily(date: String, amount: Int): EmptyResult<AthkarError> =
        withContext(dispatcher) {
            if (amount == 0) return@withContext io.github.helmy2.mudawama.core.domain.Result.Success(Unit)

            val current = dailyTotalDao.getTotalForDate(date).first()
            val currentTotal = current?.totalCount ?: 0
            val newTotal = currentTotal + amount

            safeCall(
                block = { dailyTotalDao.upsertTotal(TasbeehDailyTotalEntity(date = date, totalCount = newTotal)) },
                onError = { AthkarError.DatabaseError },
            )
        }
}
