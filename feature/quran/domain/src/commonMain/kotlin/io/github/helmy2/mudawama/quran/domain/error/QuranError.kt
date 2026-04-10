package io.github.helmy2.mudawama.quran.domain.error

import io.github.helmy2.mudawama.core.domain.DomainError

const val MAX_SESSION_PAGES = 604       // total pages in the Uthmani mushaf (per-session log cap)
const val MAX_DAILY_GOAL_PAGES = 60     // 3 Juz/day — reasonable upper bound for a daily goal
const val DEFAULT_DAILY_GOAL_PAGES = 5

sealed interface QuranError : DomainError {
    data object DatabaseError    : QuranError
    data object NetworkError     : QuranError   // API call failed (network/timeout)
    data object InvalidPageCount : QuranError   // pages < 1 or > MAX_SESSION_PAGES
    data object InvalidGoal      : QuranError   // pagesPerDay < 1 or > MAX_DAILY_GOAL_PAGES
    data object InvalidSurah     : QuranError   // surahNumber not in 1..114
    data object InvalidAyah      : QuranError   // ayah > surah.ayahCount
    data object GenericError     : QuranError
}
