package io.github.helmy2.mudawama.quran.data.repository

import io.github.helmy2.mudawama.core.database.dao.QuranGoalDao
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.safeCall
import io.github.helmy2.mudawama.quran.data.mapper.toDomain
import io.github.helmy2.mudawama.quran.data.mapper.toEntity
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.DEFAULT_DAILY_GOAL
import io.github.helmy2.mudawama.quran.domain.model.QuranGoal
import io.github.helmy2.mudawama.quran.domain.repository.QuranGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class QuranGoalRepositoryImpl(
    private val dao: QuranGoalDao,
) : QuranGoalRepository {

    override fun observeGoal(): Flow<QuranGoal> =
        dao.getGoal().map { it?.toDomain() ?: DEFAULT_DAILY_GOAL }

    override suspend fun setGoal(goal: QuranGoal): EmptyResult<QuranError> =
        safeCall(
            block = { dao.upsertGoal(goal.toEntity()) },
            onError = { QuranError.DatabaseError },
        )
}
