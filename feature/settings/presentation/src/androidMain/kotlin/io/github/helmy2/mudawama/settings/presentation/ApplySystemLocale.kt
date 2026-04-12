package io.github.helmy2.mudawama.settings.presentation

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

actual fun applySystemLocale(languageCode: String) {
    val context = getAppContext() ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java)?.applicationLocales =
            android.os.LocaleList.forLanguageTags(languageCode)
    } else {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
        )
    }
}

private fun getAppContext(): Context? {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val instance = activityThreadClass.getMethod("currentApplication").invoke(null)
        instance as Context
    } catch (e: Exception) {
        null
    }
}