package io.github.helmy2.mudawama.feature.qibla.presentation.di

import io.github.helmy2.mudawama.feature.qibla.presentation.navigation.NativeNavigationController
import io.github.helmy2.mudawama.feature.qibla.presentation.viewmodel.QiblaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun qiblaPresentationModule() = module {
    viewModelOf(::QiblaViewModel)
    single<NativeNavigationController> { QiblaNavigationBridge() }
}

class QiblaNavigationBridge : NativeNavigationController {
    override fun navigateToQibla() {
        // TODO: Implement navigation - will be handled by iOS native code
    }
}