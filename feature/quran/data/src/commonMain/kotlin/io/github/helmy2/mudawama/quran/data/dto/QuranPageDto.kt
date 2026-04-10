package io.github.helmy2.mudawama.quran.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuranPageResponseDto(
    val code: Int,
    val data: QuranPageDataDto,
)

@Serializable
data class QuranPageDataDto(
    val ayahs: List<QuranAyahDto>,
)

@Serializable
data class QuranAyahDto(
    val number: Int,
    @SerialName("numberInSurah") val numberInSurah: Int,
    val page: Int,
    val surah: QuranSurahRefDto,
)

@Serializable
data class QuranSurahRefDto(
    val number: Int,
    @SerialName("englishName") val englishName: String,
)
