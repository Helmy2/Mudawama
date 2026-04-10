package io.github.helmy2.mudawama.quran.domain.model

import io.github.helmy2.mudawama.quran.domain.error.DEFAULT_DAILY_GOAL_PAGES

data class QuranGoal(
    val pagesPerDay: Int,
    val updatedAt: Long,
)

val DEFAULT_DAILY_GOAL = QuranGoal(pagesPerDay = DEFAULT_DAILY_GOAL_PAGES, updatedAt = 0L)
