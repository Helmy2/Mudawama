package io.github.helmy2.mudawama.prayer.data.di

import io.github.helmy2.mudawama.prayer.data.repository.PrayerHabitRepositoryImpl
import io.github.helmy2.mudawama.prayer.data.repository.PrayerTimesRepositoryImpl
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerHabitRepository
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerTimesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val PrayerDataModule = module {
    singleOf(::PrayerHabitRepositoryImpl) bind PrayerHabitRepository::class
    singleOf(::PrayerTimesRepositoryImpl) bind PrayerTimesRepository::class
}
