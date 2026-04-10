package io.github.helmy2.mudawama.quran.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.ALL_SURAHS
import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark
import io.github.helmy2.mudawama.quran.domain.repository.QuranBookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class UpdateBookmarkUseCase(
    private val repo: QuranBookmarkRepository,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(surahNumber: Int, ayahNumber: Int): EmptyResult<QuranError> =
        withContext(dispatcher) {
            if (surahNumber < 1 || surahNumber > 114) {
                return@withContext Result.Failure(QuranError.InvalidSurah)
            }
            val surah = ALL_SURAHS[surahNumber - 1]
            if (ayahNumber < 1 || ayahNumber > surah.ayahCount) {
                return@withContext Result.Failure(QuranError.InvalidAyah)
            }
            repo.upsertBookmark(
                QuranBookmark(
                    surah = surahNumber,
                    surahName = surah.nameEn,
                    ayah = ayahNumber,
                )
            )
        }
}
