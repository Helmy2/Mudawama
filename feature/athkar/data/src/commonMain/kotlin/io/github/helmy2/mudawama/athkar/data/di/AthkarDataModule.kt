package io.github.helmy2.mudawama.athkar.data.di

import io.github.helmy2.mudawama.athkar.data.repository.AthkarNotificationRepositoryImpl
import io.github.helmy2.mudawama.athkar.data.repository.AthkarRepositoryImpl
import io.github.helmy2.mudawama.athkar.data.repository.TasbeehRepositoryImpl
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarNotificationRepository
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarRepository
import io.github.helmy2.mudawama.athkar.domain.repository.TasbeehRepository
import org.koin.dsl.module

fun athkarDataModule() = module {
    single<AthkarRepository> { AthkarRepositoryImpl(get(), get()) }
    single<TasbeehRepository> { TasbeehRepositoryImpl(get(), get(), get()) }
    single<AthkarNotificationRepository> { AthkarNotificationRepositoryImpl(get(), get(), get()) }
}
