package io.github.helmy2.mudawama.core.database.di

import io.github.helmy2.mudawama.core.database.MudawamaDatabase
import io.github.helmy2.mudawama.core.database.dao.AthkarDailyLogDao
import io.github.helmy2.mudawama.core.database.dao.HabitDao
import io.github.helmy2.mudawama.core.database.dao.HabitLogDao
import io.github.helmy2.mudawama.core.database.dao.PrayerTimeCacheDao
import io.github.helmy2.mudawama.core.database.dao.QuranBookmarkDao
import io.github.helmy2.mudawama.core.database.dao.QuranDailyLogDao
import io.github.helmy2.mudawama.core.database.dao.QuranGoalDao
import io.github.helmy2.mudawama.core.database.dao.TasbeehDailyTotalDao
import io.github.helmy2.mudawama.core.database.dao.TasbeehGoalDao
import org.koin.dsl.module

internal val coreDatabaseModule = module {
    single<HabitDao> { get<MudawamaDatabase>().habitDao() }
    single<HabitLogDao> { get<MudawamaDatabase>().habitLogDao() }
    single<QuranBookmarkDao> { get<MudawamaDatabase>().quranBookmarkDao() }
    single<QuranDailyLogDao> { get<MudawamaDatabase>().quranDailyLogDao() }
    single<QuranGoalDao> { get<MudawamaDatabase>().quranGoalDao() }
    single<PrayerTimeCacheDao> { get<MudawamaDatabase>().prayerTimeCacheDao() }
    single<AthkarDailyLogDao> { get<MudawamaDatabase>().athkarDailyLogDao() }
    single<TasbeehGoalDao> { get<MudawamaDatabase>().tasbeehGoalDao() }
    single<TasbeehDailyTotalDao> { get<MudawamaDatabase>().tasbeehDailyTotalDao() }
}
