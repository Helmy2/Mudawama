package io.github.helmy2.mudawama.settings.presentation.di

import io.github.helmy2.mudawama.quran.domain.usecase.SetGoalUseCase
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppLanguageUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppThemeUseCase
import io.github.helmy2.mudawama.settings.domain.SetCalculationMethodUseCase
import io.github.helmy2.mudawama.settings.domain.SetDynamicThemeUseCase
import io.github.helmy2.mudawama.settings.domain.SetEveningNotificationUseCase
import io.github.helmy2.mudawama.settings.domain.SetLocationModeUseCase
import io.github.helmy2.mudawama.settings.domain.SetMorningNotificationUseCase
import io.github.helmy2.mudawama.settings.presentation.SettingsViewModel
import org.koin.dsl.module

val settingsPresentationModule = module {
    factory { ObserveSettingsUseCase(repository = get()) }
    factory { SetCalculationMethodUseCase(repository = get()) }
    factory { SetLocationModeUseCase(repository = get()) }
    factory { SetAppThemeUseCase(repository = get()) }
    factory { SetAppLanguageUseCase(repository = get()) }
    factory { SetMorningNotificationUseCase(repository = get()) }
    factory { SetEveningNotificationUseCase(repository = get()) }
    factory { SetDynamicThemeUseCase(repository = get()) }
    factory { SetGoalUseCase(repo = get(), timeProvider = get(), dispatcher = get()) }
    factory {
        SettingsViewModel(
            observeSettingsUseCase = get(),
            setCalculationMethodUseCase = get(),
            setLocationModeUseCase = get(),
            setAppThemeUseCase = get(),
            setAppLanguageUseCase = get(),
            setGoalUseCase = get(),
            setMorningNotificationUseCase = get(),
            setEveningNotificationUseCase = get(),
            setDynamicThemeUseCase = get(),
            notificationScheduler = get()
        )
    }
}