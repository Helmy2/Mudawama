package io.github.helmy2.mudawama.home.presentation.di

import io.github.helmy2.mudawama.home.presentation.HomeViewModel
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun homePresentationModule() = module {
    factory { ObserveSettingsUseCase(repository = get()) }
    viewModelOf(::HomeViewModel)
}
