package io.github.helmy2.mudawama.core.database.di

import io.github.helmy2.mudawama.core.database.MudawamaDatabase
import io.github.helmy2.mudawama.core.database.getDatabaseBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosCoreDatabaseModule(): Module = module {
    includes(coreDatabaseModule)
    single<MudawamaDatabase> {
        getDatabaseBuilder()
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }
}

