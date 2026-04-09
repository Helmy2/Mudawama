package io.github.helmy2.mudawama.prayer.presentation.di

import io.github.helmy2.mudawama.prayer.presentation.PrayerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun prayerPresentationModule() = module {
    viewModelOf(::PrayerViewModel)
}
