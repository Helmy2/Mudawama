package io.github.helmy2.mudawama.athkar.domain.di

import io.github.helmy2.mudawama.athkar.domain.usecase.AddToTasbeehDailyUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.GetAthkarGroupUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.IncrementAthkarItemUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveAthkarCompletionUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveAthkarLogUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehDailyTotalUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehGoalUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ResetAthkarItemUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveNotificationPreferenceUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.SaveNotificationPreferenceUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.SetTasbeehGoalUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val athkarDomainModule = module {
    factory { Dispatchers.Default }
    factoryOf(::GetAthkarGroupUseCase)
    factoryOf(::IncrementAthkarItemUseCase)
    factoryOf(::ResetAthkarItemUseCase)
    factoryOf(::ObserveAthkarCompletionUseCase)
    factoryOf(::ObserveAthkarLogUseCase)
    factoryOf(::ObserveTasbeehGoalUseCase)
    factoryOf(::SetTasbeehGoalUseCase)
    factoryOf(::ObserveTasbeehDailyTotalUseCase)
    factoryOf(::AddToTasbeehDailyUseCase)
    factoryOf(::ObserveNotificationPreferenceUseCase)
    factoryOf(::SaveNotificationPreferenceUseCase)
}
