package io.github.helmy2.mudawama.settings.domain

data class AppSettings(
    val calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val locationMode: LocationMode = LocationMode.Gps,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val morningNotificationEnabled: Boolean = false,
    val eveningNotificationEnabled: Boolean = false,
    val morningNotificationHour: Int = 6,
    val morningNotificationMinute: Int = 0,
    val eveningNotificationHour: Int = 18,
    val eveningNotificationMinute: Int = 0,
    val useDynamicTheme: Boolean = true
) {
    companion object {
        val DEFAULT = AppSettings()
    }
}