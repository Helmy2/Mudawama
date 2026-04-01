package io.github.helmy2.mudawama.umbrella.ui

import io.github.helmy2.mudawama.core.data.di.androidCoreDataModule
import io.github.helmy2.mudawama.core.database.di.androidCoreDatabaseModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules(): KoinApplication {
    return modules(
        androidCoreDataModule,
        androidCoreDatabaseModule,
    )
}
