package io.github.helmy2.mudawama.settings.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setCalculationMethod(method: CalculationMethod)
    suspend fun setLocationMode(mode: LocationMode)
    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setAppLanguage(language: AppLanguage)
    suspend fun setMorningNotificationEnabled(enabled: Boolean)
    suspend fun setEveningNotificationEnabled(enabled: Boolean)
    suspend fun setMorningNotificationTime(hour: Int, minute: Int)
    suspend fun setEveningNotificationTime(hour: Int, minute: Int)
}