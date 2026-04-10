package io.github.helmy2.mudawama.quran.data.di

import io.github.helmy2.mudawama.quran.data.repository.QuranBookmarkRepositoryImpl
import io.github.helmy2.mudawama.quran.data.repository.QuranDailyLogRepositoryImpl
import io.github.helmy2.mudawama.quran.data.repository.QuranGoalRepositoryImpl
import io.github.helmy2.mudawama.quran.data.repository.QuranPageRepositoryImpl
import io.github.helmy2.mudawama.quran.domain.repository.QuranBookmarkRepository
import io.github.helmy2.mudawama.quran.domain.repository.QuranDailyLogRepository
import io.github.helmy2.mudawama.quran.domain.repository.QuranGoalRepository
import io.github.helmy2.mudawama.quran.domain.repository.QuranPageRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val quranHttpClientQualifier = named("quran")

fun quranDataModule() = module {
    // Dedicated unauthenticated HttpClient for alquran.cloud API
    single<HttpClient>(quranHttpClientQualifier) {
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

    single<QuranDailyLogRepository> { QuranDailyLogRepositoryImpl(get()) }
    single<QuranGoalRepository> { QuranGoalRepositoryImpl(get()) }
    single<QuranBookmarkRepository> { QuranBookmarkRepositoryImpl(get(), get()) }
    single<QuranPageRepository> { QuranPageRepositoryImpl(get(quranHttpClientQualifier)) }
}
