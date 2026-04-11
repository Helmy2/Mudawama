package io.github.helmy2.mudawama.core.data.di

import io.github.helmy2.mudawama.core.data.session.createDataStore
import io.github.helmy2.mudawama.core.domain.session.Encryptor
import io.github.helmy2.mudawama.core.data.networking.IosConnectivityObserver
import io.github.helmy2.mudawama.core.domain.ConnectivityObserver
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionProvider
import io.github.helmy2.mudawama.core.location.LocationProvider
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosCoreDataModule(
    iosEncryptor: Encryptor,
    iosLocationProvider: LocationProvider,
    iosNotificationProvider: NotificationPermissionProvider,
): Module {
    return module {
        includes(coreDataModule)
        single<Encryptor> { iosEncryptor }
        single { createDataStore() }
        single<ConnectivityObserver> { IosConnectivityObserver() }
        single<LocationProvider> { iosLocationProvider }
        single<NotificationPermissionProvider> { iosNotificationProvider }
    }
}
