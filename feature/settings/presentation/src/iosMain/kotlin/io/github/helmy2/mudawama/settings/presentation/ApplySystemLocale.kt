package io.github.helmy2.mudawama.settings.presentation

import platform.Foundation.NSUserDefaults

actual fun applySystemLocale(languageCode: String) {
    val prefs = NSUserDefaults.standardUserDefaults
    prefs.setObject(listOf(languageCode), forKey = "AppleLanguages")
    prefs.setObject(languageCode, forKey = "appLanguage")
    prefs.synchronize()
}