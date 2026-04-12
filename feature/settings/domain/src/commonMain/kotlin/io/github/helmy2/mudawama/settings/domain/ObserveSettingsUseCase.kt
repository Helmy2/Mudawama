package io.github.helmy2.mudawama.settings.domain

import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.observeSettings()
}