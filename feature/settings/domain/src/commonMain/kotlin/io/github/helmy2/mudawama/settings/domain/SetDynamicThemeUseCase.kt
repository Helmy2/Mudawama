package io.github.helmy2.mudawama.settings.domain

class SetDynamicThemeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) =
        repository.setDynamicThemeEnabled(enabled)
}
