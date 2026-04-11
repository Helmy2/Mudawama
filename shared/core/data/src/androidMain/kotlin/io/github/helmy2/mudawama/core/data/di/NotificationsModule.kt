package io.github.helmy2.mudawama.core.data.di

import io.github.helmy2.mudawama.core.data.notification.AndroidNotificationPermissionChecker
import io.github.helmy2.mudawama.core.data.notification.AndroidNotificationScheduler
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionChecker
import io.github.helmy2.mudawama.core.domain.notification.NotificationScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidNotificationsModule = module {
    single<NotificationScheduler> { AndroidNotificationScheduler(androidContext()) }
    single<NotificationPermissionChecker> { AndroidNotificationPermissionChecker(androidContext()) }
}
