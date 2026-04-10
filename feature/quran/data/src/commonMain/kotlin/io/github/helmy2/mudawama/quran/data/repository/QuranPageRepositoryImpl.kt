package io.github.helmy2.mudawama.quran.data.repository

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.quran.data.dto.QuranPageResponseDto
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark
import io.github.helmy2.mudawama.quran.domain.repository.QuranPageRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class QuranPageRepositoryImpl(
    private val httpClient: HttpClient,
) : QuranPageRepository {

    override suspend fun getFirstAyahOnPage(page: Int): Result<QuranBookmark, QuranError> {
        return try {
            val response: QuranPageResponseDto =
                httpClient.get("https://api.alquran.cloud/v1/page/$page/quran-uthmani").body()

            val firstAyah = response.data.ayahs.firstOrNull()
                ?: return Result.Failure(QuranError.NetworkError)

            Result.Success(
                QuranBookmark(
                    surah = firstAyah.surah.number,
                    surahName = firstAyah.surah.englishName,
                    ayah = firstAyah.numberInSurah,
                )
            )
        } catch (e: Exception) {
            Result.Failure(QuranError.NetworkError)
        }
    }
}
