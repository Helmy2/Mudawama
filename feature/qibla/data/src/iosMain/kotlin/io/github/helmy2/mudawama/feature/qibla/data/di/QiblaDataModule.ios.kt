package io.github.helmy2.mudawama.feature.qibla.data.di

import io.github.helmy2.mudawama.feature.qibla.data.sensor.CompassSensorManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val qiblaDataModule: Module
    get() = module {
        single { CompassSensorManager() }
    }