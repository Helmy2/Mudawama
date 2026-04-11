package io.github.helmy2.mudawama.athkar.presentation.di

import io.github.helmy2.mudawama.athkar.presentation.athkar.AthkarViewModel
import io.github.helmy2.mudawama.athkar.presentation.notification.AthkarNotificationViewModel
import io.github.helmy2.mudawama.athkar.presentation.tasbeeh.TasbeehViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun athkarPresentationModule() = module {
    viewModelOf(::AthkarViewModel)
    viewModelOf(::TasbeehViewModel)
    viewModelOf(::AthkarNotificationViewModel)
}
