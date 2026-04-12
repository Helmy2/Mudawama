package io.github.helmy2.mudawama.settings.data.di

import io.github.helmy2.mudawama.settings.data.SettingsRepositoryImpl
import io.github.helmy2.mudawama.settings.domain.SettingsRepository
import org.koin.dsl.module

val settingsDataModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(dataStore = get()) }
}