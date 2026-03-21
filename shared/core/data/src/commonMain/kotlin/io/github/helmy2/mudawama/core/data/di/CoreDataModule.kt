package io.github.helmy2.mudawama.core.data.di

import io.github.helmy2.mudawama.core.data.session.DataStoreSessionStorage
import io.github.helmy2.mudawama.core.domain.session.SessionStorage
import io.github.helmy2.mudawama.core.data.networking.HttpClientFactory
import io.github.helmy2.mudawama.core.data.networking.KermitLogger
import io.github.helmy2.mudawama.core.domain.MudawamaLogger
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single


internal val coreDataModule = module {
    single<SessionStorage> {
        DataStoreSessionStorage(
            dataStore = get(),
            encryptor = get()
        )
    }
    single<HttpClientFactory>()
    single<MudawamaLogger> { KermitLogger() }
}