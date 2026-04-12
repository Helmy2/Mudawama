package io.github.helmy2.mudawama.settings.domain

class SetAppLanguageUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(language: AppLanguage) =
        repository.setAppLanguage(language)
}