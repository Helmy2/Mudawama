package io.github.helmy2.mudawama.core.data.di

import io.github.helmy2.mudawama.core.data.session.DataStoreSessionStorage
import io.github.helmy2.mudawama.core.domain.session.SessionStorage
import io.github.helmy2.mudawama.core.data.networking.HttpClientFactory
import io.github.helmy2.mudawama.core.data.networking.KermitLogger
import io.github.helmy2.mudawama.core.domain.MudawamaLogger
import io.ktor.client.HttpClient
import org.koin.dsl.module


internal val coreDataModule = module {
    single<SessionStorage> {
        DataStoreSessionStorage(
            dataStore = get(),
            encryptor = get()
        )
    }
    single {
        HttpClientFactory(
            sessionStorage = get(),
            baseUrl = "https://api.example.com"
        )
    }
    single<HttpClient> { get<HttpClientFactory>().create() }
    single<MudawamaLogger> { KermitLogger() }
}