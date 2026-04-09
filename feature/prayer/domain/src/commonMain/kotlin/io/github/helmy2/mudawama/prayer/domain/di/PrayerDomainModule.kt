package io.github.helmy2.mudawama.prayer.domain.di

import io.github.helmy2.mudawama.prayer.domain.usecase.MarkPrayerMissedUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.MarkPrayerPendingUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.ObservePrayersForDateUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.SeedPrayerHabitsUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.TogglePrayerStatusUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val PrayerDomainModule = module {
    factory<kotlinx.coroutines.CoroutineDispatcher> { Dispatchers.Default }
    factoryOf(::ObservePrayersForDateUseCase)
    factoryOf(::TogglePrayerStatusUseCase)
    factoryOf(::MarkPrayerMissedUseCase)
    factoryOf(::MarkPrayerPendingUseCase)
    factoryOf(::SeedPrayerHabitsUseCase)
}
