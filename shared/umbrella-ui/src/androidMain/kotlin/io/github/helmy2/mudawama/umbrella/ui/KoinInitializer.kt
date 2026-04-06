package io.github.helmy2.mudawama.umbrella.ui

import io.github.helmy2.mudawama.core.data.di.androidCoreDataModule
import io.github.helmy2.mudawama.core.database.di.androidCoreDatabaseModule
import io.github.helmy2.mudawama.core.time.di.timeModule
import io.github.helmy2.mudawama.habits.data.di.habitsDataModule
import io.github.helmy2.mudawama.habits.domain.di.habitsDomainModule
import io.github.helmy2.mudawama.habits.presentation.di.habitsPresentationModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules(): KoinApplication {
    return modules(
        androidCoreDataModule,
        androidCoreDatabaseModule,
        timeModule(),
        habitsDomainModule(),
        habitsDataModule(),
        habitsPresentationModule(),
    )
}
