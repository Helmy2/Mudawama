package io.github.helmy2.mudawama.settings.domain

class SetMorningNotificationUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) =
        repository.setMorningNotificationEnabled(enabled)
}

class SetEveningNotificationUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) =
        repository.setEveningNotificationEnabled(enabled)
}