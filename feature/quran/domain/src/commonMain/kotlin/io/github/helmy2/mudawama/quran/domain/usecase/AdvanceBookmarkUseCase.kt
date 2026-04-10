package io.github.helmy2.mudawama.quran.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.ALL_SURAHS
import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark
import io.github.helmy2.mudawama.quran.domain.model.surahForPage
import io.github.helmy2.mudawama.quran.domain.repository.QuranBookmarkRepository
import io.github.helmy2.mudawama.quran.domain.repository.QuranPageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Advances the reading bookmark forward by [pagesRead] pages using the
 * Madinah Mushaf page map embedded in [SurahMetadata.startPage].
 *
 * Algorithm:
 * 1. If no bookmark exists, resolve page 1 + pagesRead (start of Al-Fatihah).
 * 2. Start from the current bookmark's surah startPage.
 * 3. Add [pagesRead] to get the target page (clamped to 604).
 * 4. Call the alquran.cloud API to get the exact surah + ayah at that page.
 * 5. On API success, persist the exact bookmark.
 *    On API failure, fall back to ayah = 1 of the resolved surah (safe anchor).
 */
class AdvanceBookmarkUseCase(
    private val bookmarkRepo: QuranBookmarkRepository,
    private val pageRepo: QuranPageRepository,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        currentBookmark: QuranBookmark?,
        pagesRead: Int,
    ): EmptyResult<QuranError> = withContext(dispatcher) {
        if (pagesRead < 1) return@withContext Result.Failure(QuranError.InvalidPageCount)

        val startPage = if (currentBookmark != null) {
            // Use the start page of the bookmarked surah as the current position.
            // Conservative anchor: always advance from the beginning of the surah.
            ALL_SURAHS[currentBookmark.surah - 1].startPage
        } else {
            1 // No bookmark — start from page 1 (Al-Fatihah)
        }

        val targetPage = (startPage + pagesRead).coerceAtMost(604)

        // Try to resolve the exact surah + ayah at the target page via API.
        // Fall back to ayah = 1 of the closest surah if the API call fails.
        val newBookmark = when (val apiResult = pageRepo.getFirstAyahOnPage(targetPage)) {
            is Result.Success -> apiResult.data
            is Result.Failure -> {
                val fallbackSurah = surahForPage(targetPage)
                QuranBookmark(
                    surah = fallbackSurah.number,
                    surahName = fallbackSurah.nameEn,
                    ayah = 1,
                )
            }
        }

        bookmarkRepo.upsertBookmark(newBookmark)
    }
}
