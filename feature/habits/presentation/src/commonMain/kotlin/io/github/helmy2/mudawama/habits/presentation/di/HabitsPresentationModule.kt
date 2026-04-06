package io.github.helmy2.mudawama.habits.presentation.di

import io.github.helmy2.mudawama.habits.presentation.HabitsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for the habits presentation layer.
 *
 * Requires [habitsDomainModule] to be loaded first so all use case factory
 * bindings are available when the ViewModel is first resolved.
 */
fun habitsPresentationModule() = module {
    viewModelOf(::HabitsViewModel)
}
