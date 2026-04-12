package io.github.helmy2.mudawama.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.helmy2.mudawama.settings.domain.AppLanguage
import io.github.helmy2.mudawama.settings.domain.AppSettings
import io.github.helmy2.mudawama.settings.domain.AppTheme
import io.github.helmy2.mudawama.settings.domain.CalculationMethod
import io.github.helmy2.mudawama.settings.domain.LocationMode
import io.github.helmy2.mudawama.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val LOCATION_MODE_IS_GPS = booleanPreferencesKey("location_mode_is_gps")
        val LOCATION_MODE_LATITUDE = doublePreferencesKey("location_mode_latitude")
        val LOCATION_MODE_LONGITUDE = doublePreferencesKey("location_mode_longitude")
        val APP_THEME = stringPreferencesKey("app_theme")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val MORNING_NOTIFICATION_ENABLED = booleanPreferencesKey("morning_notification_enabled")
        val EVENING_NOTIFICATION_ENABLED = booleanPreferencesKey("evening_notification_enabled")
        val MORNING_NOTIFICATION_HOUR = intPreferencesKey("morning_notification_hour")
        val MORNING_NOTIFICATION_MINUTE = intPreferencesKey("morning_notification_minute")
        val EVENING_NOTIFICATION_HOUR = intPreferencesKey("evening_notification_hour")
        val EVENING_NOTIFICATION_MINUTE = intPreferencesKey("evening_notification_minute")
    }

    override fun observeSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        val method = prefs[Keys.CALCULATION_METHOD]?.let {
            runCatching { CalculationMethod.valueOf(it) }.getOrNull()
        } ?: CalculationMethod.MUSLIM_WORLD_LEAGUE

        val isGps = prefs[Keys.LOCATION_MODE_IS_GPS] ?: true
        val lat = prefs[Keys.LOCATION_MODE_LATITUDE]
        val lon = prefs[Keys.LOCATION_MODE_LONGITUDE]

        val locationMode = if (isGps || lat == null || lon == null) {
            LocationMode.Gps
        } else {
            LocationMode.Manual(lat, lon)
        }

        val theme = prefs[Keys.APP_THEME]?.let {
            runCatching { AppTheme.valueOf(it) }.getOrNull()
        } ?: AppTheme.SYSTEM

        val language = prefs[Keys.APP_LANGUAGE]?.let {
            runCatching { AppLanguage.valueOf(it) }.getOrNull()
        } ?: AppLanguage.ENGLISH

        AppSettings(
            calculationMethod = method,
            locationMode = locationMode,
            appTheme = theme,
            appLanguage = language,
            morningNotificationEnabled = prefs[Keys.MORNING_NOTIFICATION_ENABLED] ?: false,
            eveningNotificationEnabled = prefs[Keys.EVENING_NOTIFICATION_ENABLED] ?: false,
            morningNotificationHour = prefs[Keys.MORNING_NOTIFICATION_HOUR] ?: 6,
            morningNotificationMinute = prefs[Keys.MORNING_NOTIFICATION_MINUTE] ?: 0,
            eveningNotificationHour = prefs[Keys.EVENING_NOTIFICATION_HOUR] ?: 18,
            eveningNotificationMinute = prefs[Keys.EVENING_NOTIFICATION_MINUTE] ?: 0
        )
    }

    override suspend fun setCalculationMethod(method: CalculationMethod) {
        dataStore.edit { prefs ->
            prefs[Keys.CALCULATION_METHOD] = method.name
        }
    }

    override suspend fun setLocationMode(mode: LocationMode) {
        dataStore.edit { prefs ->
            when (mode) {
                is LocationMode.Gps -> {
                    prefs[Keys.LOCATION_MODE_IS_GPS] = true
                }
                is LocationMode.Manual -> {
                    prefs[Keys.LOCATION_MODE_IS_GPS] = false
                    prefs[Keys.LOCATION_MODE_LATITUDE] = mode.latitude
                    prefs[Keys.LOCATION_MODE_LONGITUDE] = mode.longitude
                }
            }
        }
    }

    override suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = theme.name
        }
    }

    override suspend fun setAppLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = language.name
        }
    }

    override suspend fun setMorningNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.MORNING_NOTIFICATION_ENABLED] = enabled
        }
    }

    override suspend fun setEveningNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.EVENING_NOTIFICATION_ENABLED] = enabled
        }
    }

    override suspend fun setMorningNotificationTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.MORNING_NOTIFICATION_HOUR] = hour
            prefs[Keys.MORNING_NOTIFICATION_MINUTE] = minute
        }
    }

    override suspend fun setEveningNotificationTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.EVENING_NOTIFICATION_HOUR] = hour
            prefs[Keys.EVENING_NOTIFICATION_MINUTE] = minute
        }
    }
}