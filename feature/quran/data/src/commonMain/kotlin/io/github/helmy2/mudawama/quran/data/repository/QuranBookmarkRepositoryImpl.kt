package io.github.helmy2.mudawama.quran.data.repository

import io.github.helmy2.mudawama.core.database.dao.QuranBookmarkDao
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.safeCall
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.quran.data.mapper.toDomain
import io.github.helmy2.mudawama.quran.data.mapper.toEntity
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark
import io.github.helmy2.mudawama.quran.domain.repository.QuranBookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class QuranBookmarkRepositoryImpl(
    private val dao: QuranBookmarkDao,
    private val timeProvider: TimeProvider,
) : QuranBookmarkRepository {

    override fun observeBookmark(): Flow<QuranBookmark?> =
        dao.getBookmark().map { it?.toDomain() }

    override suspend fun upsertBookmark(bookmark: QuranBookmark): EmptyResult<QuranError> =
        safeCall(
            block = { dao.upsertBookmark(bookmark.toEntity(timeProvider.nowInstant().toEpochMilliseconds())) },
            onError = { QuranError.DatabaseError },
        )
}
