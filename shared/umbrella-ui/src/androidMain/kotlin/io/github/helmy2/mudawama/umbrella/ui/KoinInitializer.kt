package io.github.helmy2.mudawama.umbrella.ui

import io.github.helmy2.mudawama.athkar.data.di.athkarDataModule
import io.github.helmy2.mudawama.athkar.domain.di.athkarDomainModule
import io.github.helmy2.mudawama.athkar.presentation.di.athkarPresentationModule
import io.github.helmy2.mudawama.core.data.di.androidCoreDataModule
import io.github.helmy2.mudawama.core.data.di.androidNotificationsModule
import io.github.helmy2.mudawama.core.database.di.androidCoreDatabaseModule
import io.github.helmy2.mudawama.core.time.di.timeModule
import io.github.helmy2.mudawama.habits.data.di.habitsDataModule
import io.github.helmy2.mudawama.habits.domain.di.habitsDomainModule
import io.github.helmy2.mudawama.habits.presentation.di.habitsPresentationModule
import io.github.helmy2.mudawama.feature.qibla.data.di.qiblaDataModule
import io.github.helmy2.mudawama.feature.qibla.domain.di.qiblaDomainModule
import io.github.helmy2.mudawama.feature.qibla.presentation.di.qiblaPresentationModule
import io.github.helmy2.mudawama.home.presentation.di.homePresentationModule
import io.github.helmy2.mudawama.prayer.data.di.PrayerDataModule
import io.github.helmy2.mudawama.prayer.domain.di.PrayerDomainModule
import io.github.helmy2.mudawama.prayer.presentation.di.prayerPresentationModule
import io.github.helmy2.mudawama.quran.data.di.quranDataModule
import io.github.helmy2.mudawama.quran.domain.di.quranDomainModule
import io.github.helmy2.mudawama.quran.presentation.di.quranPresentationModule
import io.github.helmy2.mudawama.settings.data.di.settingsDataModule
import io.github.helmy2.mudawama.settings.presentation.di.settingsPresentationModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules(): KoinApplication {
    return modules(
        androidCoreDataModule,
        androidCoreDatabaseModule,
        androidNotificationsModule,
        timeModule(),
        habitsDomainModule(),
        habitsDataModule(),
        habitsPresentationModule(),
        PrayerDomainModule,
        PrayerDataModule,
        prayerPresentationModule(),
        quranDomainModule,
        quranDataModule(),
        quranPresentationModule(),
        athkarDomainModule,
        athkarDataModule(),
        athkarPresentationModule(),
        homePresentationModule(),
        qiblaDomainModule,
        qiblaDataModule,
        qiblaPresentationModule(),
        settingsDataModule,
        settingsPresentationModule,
    )
}
