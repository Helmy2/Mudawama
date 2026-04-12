package io.github.helmy2.mudawama.feature.qibla.domain.di

import io.github.helmy2.mudawama.feature.qibla.domain.usecase.CalculateQiblaAngleUseCase
import org.koin.dsl.module

val qiblaDomainModule = module {
    factory { CalculateQiblaAngleUseCase() }
}