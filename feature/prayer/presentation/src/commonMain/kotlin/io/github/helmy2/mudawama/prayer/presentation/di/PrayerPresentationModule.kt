package io.github.helmy2.mudawama.prayer.presentation.di

import io.github.helmy2.mudawama.prayer.presentation.PrayerViewModel
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun prayerPresentationModule() = module {
    factory { ObserveSettingsUseCase(repository = get()) }
    viewModelOf(::PrayerViewModel)
}
