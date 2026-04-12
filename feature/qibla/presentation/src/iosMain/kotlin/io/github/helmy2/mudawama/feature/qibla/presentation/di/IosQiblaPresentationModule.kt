package io.github.helmy2.mudawama.feature.qibla.presentation.di

import io.github.helmy2.mudawama.feature.qibla.domain.ui.QiblaViewControllerProvider
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosQiblaPresentationModule(
    iosQiblaViewControllerProvider: QiblaViewControllerProvider,
): Module {
    return module {
        includes(qiblaPresentationModule())
        single<QiblaViewControllerProvider> { iosQiblaViewControllerProvider }
    }
}
