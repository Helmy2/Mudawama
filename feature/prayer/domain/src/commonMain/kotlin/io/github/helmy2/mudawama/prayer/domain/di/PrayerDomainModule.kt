package io.github.helmy2.mudawama.prayer.domain.di

import io.github.helmy2.mudawama.prayer.domain.usecase.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlinx.coroutines.Dispatchers

val PrayerDomainModule = module {
    factory { Dispatchers.Default }
    factoryOf(::ObservePrayersForDateUseCase)
    factoryOf(::TogglePrayerStatusUseCase)
    factoryOf(::MarkPrayerMissedUseCase)
    factoryOf(::SeedPrayerHabitsUseCase)
}
