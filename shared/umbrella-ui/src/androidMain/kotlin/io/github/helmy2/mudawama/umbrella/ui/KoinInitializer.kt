package io.github.helmy2.mudawama.umbrella.ui

import io.github.helmy2.mudawama.core.data.di.androidCoreDataModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules(): KoinApplication {
    return modules(androidCoreDataModule)
}
