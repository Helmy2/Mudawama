package io.github.helmy2.mudawama.quran.domain.di

import io.github.helmy2.mudawama.quran.domain.usecase.AdvanceBookmarkUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.ComputeStreakUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.LogReadingUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.ObserveQuranStateUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.SetGoalUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.UpdateBookmarkUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val quranDomainModule = module {
    factory { Dispatchers.Default }
    factoryOf(::LogReadingUseCase)
    factoryOf(::ObserveQuranStateUseCase)
    factoryOf(::SetGoalUseCase)
    factoryOf(::UpdateBookmarkUseCase)
    factoryOf(::AdvanceBookmarkUseCase)
    factoryOf(::ComputeStreakUseCase)
}
