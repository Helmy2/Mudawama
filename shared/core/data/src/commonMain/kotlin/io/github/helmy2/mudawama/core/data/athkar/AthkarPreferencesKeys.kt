package io.github.helmy2.mudawama.core.data.athkar

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object AthkarPreferencesKeys {
    val MORNING_NOTIF_ENABLED = booleanPreferencesKey("athkar_morning_notif_enabled")
    val MORNING_NOTIF_HOUR = intPreferencesKey("athkar_morning_notif_hour")
    val MORNING_NOTIF_MINUTE = intPreferencesKey("athkar_morning_notif_minute")
    val EVENING_NOTIF_ENABLED = booleanPreferencesKey("athkar_evening_notif_enabled")
    val EVENING_NOTIF_HOUR = intPreferencesKey("athkar_evening_notif_hour")
    val EVENING_NOTIF_MINUTE = intPreferencesKey("athkar_evening_notif_minute")
}
