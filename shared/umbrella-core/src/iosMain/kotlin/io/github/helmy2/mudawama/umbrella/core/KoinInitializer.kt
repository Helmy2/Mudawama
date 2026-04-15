package io.github.helmy2.mudawama.umbrella.core

import io.github.helmy2.mudawama.athkar.data.di.athkarDataModule
import io.github.helmy2.mudawama.athkar.domain.di.athkarDomainModule
import io.github.helmy2.mudawama.core.data.di.iosCoreDataModule
import io.github.helmy2.mudawama.core.data.di.iosNotificationsModule
import io.github.helmy2.mudawama.core.database.di.iosCoreDatabaseModule
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionProvider
import io.github.helmy2.mudawama.core.domain.session.Encryptor
import io.github.helmy2.mudawama.core.location.LocationProvider
import io.github.helmy2.mudawama.core.time.di.timeModule
import io.github.helmy2.mudawama.feature.qibla.data.di.qiblaDataModule
import io.github.helmy2.mudawama.feature.qibla.domain.di.qiblaDomainModule
import io.github.helmy2.mudawama.habits.data.di.habitsDataModule
import io.github.helmy2.mudawama.habits.domain.di.habitsDomainModule
import io.github.helmy2.mudawama.prayer.data.di.PrayerDataModule
import io.github.helmy2.mudawama.prayer.domain.di.PrayerDomainModule
import io.github.helmy2.mudawama.quran.data.di.quranDataModule
import io.github.helmy2.mudawama.quran.domain.di.quranDomainModule
import io.github.helmy2.mudawama.settings.data.di.settingsDataModule
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppLanguageUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppThemeUseCase
import io.github.helmy2.mudawama.settings.domain.SetCalculationMethodUseCase
import io.github.helmy2.mudawama.settings.domain.SetDynamicThemeUseCase
import io.github.helmy2.mudawama.settings.domain.SetEveningNotificationUseCase
import io.github.helmy2.mudawama.settings.domain.SetLocationModeUseCase
import io.github.helmy2.mudawama.settings.domain.SetMorningNotificationUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.SetGoalUseCase
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initializeKoin(
    iosEncryptor: Encryptor,
    iosLocationProvider: LocationProvider,
    iosNotificationProvider: NotificationPermissionProvider,
) {
    startKoin {
        modules(
            iosCoreDataModule(
                iosEncryptor = iosEncryptor,
                iosLocationProvider = iosLocationProvider,
                iosNotificationProvider = iosNotificationProvider,
            ),
            iosCoreDatabaseModule(),
            iosNotificationsModule,
            timeModule(),
            // Habits
            habitsDomainModule(),
            habitsDataModule(),
            // Prayer
            PrayerDomainModule,
            PrayerDataModule,
            // Quran
            quranDomainModule,
            quranDataModule(),
            // Athkar + Tasbeeh
            athkarDomainModule,
            athkarDataModule(),
            // Qibla
            qiblaDomainModule,
            qiblaDataModule,
            // Settings
            settingsDataModule,
            // Settings use cases (registered here since we don't include settingsPresentationModule)
            module {
                factory { ObserveSettingsUseCase(repository = get()) }
                factory { SetCalculationMethodUseCase(repository = get()) }
                factory { SetLocationModeUseCase(repository = get()) }
                factory { SetAppThemeUseCase(repository = get()) }
                factory { SetAppLanguageUseCase(repository = get()) }
                factory { SetMorningNotificationUseCase(repository = get()) }
                factory { SetEveningNotificationUseCase(repository = get()) }
                factory { SetDynamicThemeUseCase(repository = get()) }
                factory { SetGoalUseCase(repo = get(), timeProvider = get(), dispatcher = get()) }
            },
        )
    }
}
