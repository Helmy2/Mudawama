package io.github.helmy2.mudawama.quran.data.mapper

import io.github.helmy2.mudawama.core.database.entity.QuranBookmarkEntity
import io.github.helmy2.mudawama.quran.domain.model.ALL_SURAHS
import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark

fun QuranBookmarkEntity.toDomain(): QuranBookmark = QuranBookmark(
    surah = surah,
    surahName = ALL_SURAHS[surah - 1].nameEn,
    ayah = ayah,
)

fun QuranBookmark.toEntity(nowMillis: Long): QuranBookmarkEntity = QuranBookmarkEntity(
    id = 1,
    surah = surah,
    ayah = ayah,
    lastUpdated = nowMillis,
)
