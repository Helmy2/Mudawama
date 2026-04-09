package io.github.helmy2.mudawama.umbrella.ui

import io.github.helmy2.mudawama.core.data.di.iosCoreDataModule
import io.github.helmy2.mudawama.core.database.di.iosCoreDatabaseModule
import io.github.helmy2.mudawama.core.domain.session.Encryptor
import io.github.helmy2.mudawama.core.location.LocationProvider
import io.github.helmy2.mudawama.core.time.di.timeModule
import io.github.helmy2.mudawama.habits.data.di.habitsDataModule
import io.github.helmy2.mudawama.habits.domain.di.habitsDomainModule
import io.github.helmy2.mudawama.habits.presentation.di.habitsPresentationModule
import io.github.helmy2.mudawama.prayer.data.di.PrayerDataModule
import io.github.helmy2.mudawama.prayer.domain.di.PrayerDomainModule
import io.github.helmy2.mudawama.prayer.presentation.di.prayerPresentationModule
import org.koin.core.context.startKoin

fun initializeKoin(iosEncryptor: Encryptor, iosLocationProvider: LocationProvider) {
    startKoin {
        modules(
            iosCoreDataModule(iosEncryptor = iosEncryptor, iosLocationProvider = iosLocationProvider),
            iosCoreDatabaseModule(),
            timeModule(),
            habitsDomainModule(),
            habitsDataModule(),
            habitsPresentationModule(),
            PrayerDomainModule,
            PrayerDataModule,
            prayerPresentationModule(),
        )
    }
}
