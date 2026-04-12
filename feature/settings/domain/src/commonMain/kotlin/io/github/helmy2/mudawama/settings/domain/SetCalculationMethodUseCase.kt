package io.github.helmy2.mudawama.settings.domain

class SetCalculationMethodUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(method: CalculationMethod) =
        repository.setCalculationMethod(method)
}