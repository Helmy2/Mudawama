package io.github.helmy2.mudawama.settings.domain

class SetLocationModeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: LocationMode) =
        repository.setLocationMode(mode)
}