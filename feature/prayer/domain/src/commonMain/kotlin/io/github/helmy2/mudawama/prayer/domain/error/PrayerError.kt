package io.github.helmy2.mudawama.prayer.domain.error

import io.github.helmy2.mudawama.core.domain.DomainError

sealed interface PrayerError : DomainError {
    data object NetworkError : PrayerError
    data object LocationError : PrayerError
    data object DatabaseError : PrayerError
    data object GenericError : PrayerError
}
