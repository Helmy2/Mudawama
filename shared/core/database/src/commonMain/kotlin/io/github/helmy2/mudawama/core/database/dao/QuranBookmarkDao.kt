package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.helmy2.mudawama.core.database.entity.QuranBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranBookmarkDao {

    @Upsert
    suspend fun upsertBookmark(bookmark: QuranBookmarkEntity)

    @Query("SELECT * FROM quran_bookmarks WHERE id = 1")
    fun getBookmark(): Flow<QuranBookmarkEntity?>
}
