package io.github.helmy2.mudawama.core.data.di

import io.github.helmy2.mudawama.core.data.session.TinkEncryptor
import io.github.helmy2.mudawama.core.data.session.createDataStore
import io.github.helmy2.mudawama.core.domain.session.Encryptor
import io.github.helmy2.mudawama.core.data.networking.AndroidConnectivityObserver
import io.github.helmy2.mudawama.core.domain.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val androidCoreDataModule = module {
    includes(coreDataModule)
    single<Encryptor> { TinkEncryptor(androidContext()) }
    single { createDataStore(androidContext()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
}