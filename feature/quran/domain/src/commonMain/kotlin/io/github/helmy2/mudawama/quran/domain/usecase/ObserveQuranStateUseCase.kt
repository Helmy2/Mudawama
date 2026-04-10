package io.github.helmy2.mudawama.quran.domain.usecase

import io.github.helmy2.mudawama.quran.domain.model.QuranScreenState
import io.github.helmy2.mudawama.quran.domain.model.QuranDailyLog
import io.github.helmy2.mudawama.quran.domain.repository.QuranBookmarkRepository
import io.github.helmy2.mudawama.quran.domain.repository.QuranDailyLogRepository
import io.github.helmy2.mudawama.quran.domain.repository.QuranGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveQuranStateUseCase(
    private val logRepo: QuranDailyLogRepository,
    private val goalRepo: QuranGoalRepository,
    private val bookmarkRepo: QuranBookmarkRepository,
) {
    operator fun invoke(date: LocalDate): Flow<QuranScreenState> {
        val dateStr = date.toString()
        // Use today's date as the "before" cutoff for recent logs
        return combine(
            goalRepo.observeGoal(),
            logRepo.observeLogsForDate(dateStr),
            bookmarkRepo.observeBookmark(),
            logRepo.observeRecentLogs(dateStr),
        ) { goal, logsForDate, bookmark, recentRaw ->
            val pagesTotal = logsForDate.sumOf { it.pagesRead }
            QuranScreenState(
                pagesReadToday = pagesTotal,
                goalPages = goal.pagesPerDay,
                bookmark = bookmark,
                recentLogs = buildRecentEntries(recentRaw, goal.pagesPerDay),
            )
        }
    }

    private fun buildRecentEntries(
        recentRaw: List<QuranDailyLog>,
        goalPages: Int,
    ): List<QuranScreenState.RecentLogEntry> {
        return recentRaw
            .groupBy { it.date }
            .entries
            .take(3)
            .map { (date, logs) ->
                QuranScreenState.RecentLogEntry(
                    date = date,
                    pagesRead = logs.sumOf { it.pagesRead },
                    goalPages = goalPages,
                )
            }
    }
}
