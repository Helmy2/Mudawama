package io.github.helmy2.mudawama.core.database.di

import io.github.helmy2.mudawama.core.database.MudawamaDatabase
import io.github.helmy2.mudawama.core.database.dao.HabitDao
import io.github.helmy2.mudawama.core.database.dao.HabitLogDao
import io.github.helmy2.mudawama.core.database.dao.PrayerTimeCacheDao
import io.github.helmy2.mudawama.core.database.dao.QuranBookmarkDao
import org.koin.dsl.module

internal val coreDatabaseModule = module {
    single<HabitDao> { get<MudawamaDatabase>().habitDao() }
    single<HabitLogDao> { get<MudawamaDatabase>().habitLogDao() }
    single<QuranBookmarkDao> { get<MudawamaDatabase>().quranBookmarkDao() }
    single<PrayerTimeCacheDao> { get<MudawamaDatabase>().prayerTimeCacheDao() }
}

