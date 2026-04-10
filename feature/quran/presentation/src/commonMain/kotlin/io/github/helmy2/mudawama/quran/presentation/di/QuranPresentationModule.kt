package io.github.helmy2.mudawama.quran.presentation.di

import io.github.helmy2.mudawama.quran.presentation.QuranViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun quranPresentationModule() = module {
    viewModelOf(::QuranViewModel)
}
