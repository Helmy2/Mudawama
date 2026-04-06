package io.github.helmy2.mudawama.habits.domain.di

import io.github.helmy2.mudawama.habits.domain.usecase.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for the habits domain layer.
 *
 * Required upstream bindings (must be registered before this module loads):
 * - `HabitRepository` (provided by `habitsDataModule()`)
 * - `HabitLogRepository` (provided by `habitsDataModule()`)
 * - `TimeProvider` (provided by `timeModule(policy)`)
 */
fun habitsDomainModule() = module {
    factoryOf(::ObserveHabitsWithTodayStatusUseCase)
    factoryOf(::CreateHabitUseCase)
    factoryOf(::UpdateHabitUseCase)
    factoryOf(::DeleteHabitUseCase)
    factoryOf(::ToggleHabitCompletionUseCase)
    factoryOf(::IncrementHabitCountUseCase)
    factoryOf(::ObserveWeeklyHeatmapUseCase)
}
