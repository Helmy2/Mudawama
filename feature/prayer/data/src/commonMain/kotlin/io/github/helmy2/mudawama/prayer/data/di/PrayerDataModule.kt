package io.github.helmy2.mudawama.prayer.data.di

import io.github.helmy2.mudawama.prayer.data.repository.PrayerHabitRepositoryImpl
import io.github.helmy2.mudawama.prayer.data.repository.PrayerTimesRepositoryImpl
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerHabitRepository
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerTimesRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val prayerHttpClientQualifier = named("prayer")

val PrayerDataModule = module {
    // Dedicated unauthenticated HttpClient for Aladhan API — no Auth, no defaultRequest
    single<HttpClient>(prayerHttpClientQualifier) {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(Logging) {
                level = LogLevel.HEADERS
            }
        }
    }

    single<PrayerTimesRepository> {
        PrayerTimesRepositoryImpl(
            httpClient = get(prayerHttpClientQualifier),
            cacheDao = get(),
            timeProvider = get()
        )
    }
    single<PrayerHabitRepository> { PrayerHabitRepositoryImpl(get(), get(), get()) }
}
