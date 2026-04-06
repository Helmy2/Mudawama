package io.github.helmy2.mudawama.habits.data.di

import io.github.helmy2.mudawama.core.database.dao.HabitDao
import io.github.helmy2.mudawama.core.database.dao.HabitLogDao
import io.github.helmy2.mudawama.habits.data.repository.HabitLogRepositoryImpl
import io.github.helmy2.mudawama.habits.data.repository.HabitRepositoryImpl
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository
import io.github.helmy2.mudawama.habits.domain.repository.HabitRepository
import org.koin.dsl.module

/**
 * Koin module for the habits data layer.
 *
 * Required upstream bindings (must be registered before this module loads):
 * - `HabitDao` — provided by `coreDatabaseModule` (via `androidCoreDatabaseModule` / `iosCoreDatabaseModule`)
 * - `HabitLogDao` — provided by `coreDatabaseModule`
 * - `TimeProvider` — provided by `timeModule(policy)`
 */
fun habitsDataModule() = module {
    single<HabitRepository> {
        HabitRepositoryImpl(
            dao = get<HabitDao>(),
            timeProvider = get(),
        )
    }
    single<HabitLogRepository> {
        HabitLogRepositoryImpl(
            dao = get<HabitLogDao>(),
        )
    }
}

