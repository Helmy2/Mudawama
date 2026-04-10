package io.github.helmy2.mudawama.quran.domain.model

data class QuranScreenState(
    val pagesReadToday: Int,
    val goalPages: Int,
    val bookmark: QuranBookmark?,
    val recentLogs: List<RecentLogEntry>,
) {
    data class RecentLogEntry(
        val date: String,       // "yyyy-MM-dd"
        val pagesRead: Int,     // sum for that date
        val goalPages: Int,     // goal active at time of display (current goal)
    ) {
        val status: LogStatus get() = when {
            pagesRead > goalPages  -> LogStatus.OVER
            pagesRead == goalPages -> LogStatus.HIT
            else                   -> LogStatus.UNDER
        }
    }

    enum class LogStatus { OVER, HIT, UNDER }
}
