package io.github.helmy2.mudawama.settings.domain

enum class AppLanguage(
    val code: String,
    val isRtl: Boolean
) {
    ENGLISH("en", false),
    ARABIC("ar", true)
}