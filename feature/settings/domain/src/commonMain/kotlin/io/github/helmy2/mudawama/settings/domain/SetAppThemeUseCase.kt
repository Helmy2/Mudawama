package io.github.helmy2.mudawama.settings.domain

class SetAppThemeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(theme: AppTheme) =
        repository.setAppTheme(theme)
}