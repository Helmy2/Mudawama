package io.github.helmy2.mudawama.core.data.di

import io.github.helmy2.mudawama.core.data.notification.IosNotificationPermissionChecker
import io.github.helmy2.mudawama.core.data.notification.IosNotificationScheduler
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionChecker
import io.github.helmy2.mudawama.core.domain.notification.NotificationScheduler
import org.koin.dsl.module

val iosNotificationsModule = module {
    single<NotificationScheduler> { IosNotificationScheduler() }
    single<NotificationPermissionChecker> { IosNotificationPermissionChecker() }
}
