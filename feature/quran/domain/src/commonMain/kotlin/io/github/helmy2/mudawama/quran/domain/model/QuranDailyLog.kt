package io.github.helmy2.mudawama.quran.domain.model

data class QuranDailyLog(
    val id: String,
    val date: String,       // "yyyy-MM-dd"
    val pagesRead: Int,
    val loggedAt: Long,
)
