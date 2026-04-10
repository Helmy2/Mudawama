package io.github.helmy2.mudawama.quran.domain.repository

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark
import kotlinx.coroutines.flow.Flow

interface QuranBookmarkRepository {
    fun observeBookmark(): Flow<QuranBookmark?>   // null if never set
    suspend fun upsertBookmark(bookmark: QuranBookmark): EmptyResult<QuranError>
}
