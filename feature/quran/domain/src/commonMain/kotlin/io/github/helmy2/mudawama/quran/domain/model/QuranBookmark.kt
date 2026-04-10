package io.github.helmy2.mudawama.quran.domain.model

data class QuranBookmark(
    val surah: Int,         // 1..114
    val surahName: String,  // derived from SurahMetadata at domain layer
    val ayah: Int,          // 1..surahAyahCount
)
