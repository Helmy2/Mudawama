package io.github.helmy2.mudawama.core.data.di

import io.github.helmy2.mudawama.core.data.session.createDataStore
import io.github.helmy2.mudawama.core.domain.session.Encryptor
import io.github.helmy2.mudawama.core.data.networking.IosConnectivityObserver
import io.github.helmy2.mudawama.core.domain.ConnectivityObserver
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosCoreDataModule(iosEncryptor: Encryptor): Module {
    return module {
        includes(coreDataModule)
        single<Encryptor> { iosEncryptor }
        single { createDataStore() }
        single<ConnectivityObserver> { IosConnectivityObserver() }
    }
}