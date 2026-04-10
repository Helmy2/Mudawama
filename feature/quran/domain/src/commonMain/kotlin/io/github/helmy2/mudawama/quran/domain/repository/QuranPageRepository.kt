package io.github.helmy2.mudawama.quran.domain.repository

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark

interface QuranPageRepository {
    /**
     * Fetches the first ayah on the given Madinah Mushaf [page] (1–604) from the
     * alquran.cloud API and returns it as a [QuranBookmark] (surah + ayah resolved).
     *
     * Returns [Result.Failure] with [QuranError.NetworkError] on any network issue.
     */
    suspend fun getFirstAyahOnPage(page: Int): Result<QuranBookmark, QuranError>
}
