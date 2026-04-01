package io.github.helmy2.mudawama.core.database.di

import io.github.helmy2.mudawama.core.database.MudawamaDatabase
import io.github.helmy2.mudawama.core.database.getDatabaseBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidCoreDatabaseModule = module {
    includes(coreDatabaseModule)
    single<MudawamaDatabase> {
        getDatabaseBuilder(androidContext())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

